/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OpenEMRConnect.
 *
 * The Initial Developer of the Original Code is International Training &
 * Education Center for Health (I-TECH) <http://www.go2itech.org/>
 *
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
package ke.go.moh.oec.kisumuhdss;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;

/**
 * Looks for all new transactions from the shadow database, updates the MPI accordingly.
 * 
 * @author Jim Grace
 */
public class Updater {

    private static int lastReceivedTransaction = -1;
    private Connection connMaster = null; // Connection for master query
    private Connection connDetail = null; // Connection for detail query
    private final static String HDSS_COMPANION_NAME = "HDSS COMPANION";
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    /**
     * Used within the Updater class to hold a single row of transaction detail.
     */
    private class Row {

        private String name;
        private String value;

        /**
         * Constructs a row of transaction detail.
         * A transaction detail consists of a (name, value) pair, with the
         * name of the database column to be inserted/updated
         * and value it will contain.
         * 
         * @param name name of database column to be inserted/updated
         * @param value value of that database column
         */
        private Row(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * Creates and initializes an instance of the updater class.
     * Creates connections to the shadow database to be used for finding new
     * shadow transactions and transaction details.
     */
    public Updater() {
        try {
            String url = Mediator.getProperty("Shadow.url");
            String username = Mediator.getProperty("Shadow.username");
            String password = Mediator.getProperty("Shadow.password");
            connMaster = DriverManager.getConnection(url, username, password);
            connDetail = DriverManager.getConnection(url, username, password);
        } catch (Exception ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        }
    }

    /**
     * Executes a SQL query on a database connection.
     * Logs the query using a caller-supplied logging level.
     *
     * @param sql the SQL query.
     * @return results of the query, as a ResultSet.
     */
    private ResultSet query(Connection conn, String sql) {
        Mediator.getLogger(Updater.class.getName()).log(Level.FINER, "SQL Query:\n{0}", sql);
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;
    }

    /**
     * Executes any SQL statement on a database connection.
     *
     * @param conn Connection to use.
     * @param sql SQL statement.
     * @return true if first result is a ResultSet.
     */
    public boolean execute(Connection conn, String sql) {
        Mediator.getLogger(Updater.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        boolean returnValue = false;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
            int updateCount = stmt.getUpdateCount();
            Mediator.getLogger(Updater.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
        } catch (SQLException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    /**
     * Find the last transaction ID that we processed in the past.
     * If there is no record of what we processed in the past, set the
     * last transaction ID to 0 in the database and return 0.
     * 
     * @return ID of the last transaction we processed in the past, or 0 if none.
     */
    private int getLastReceivedTransaction() {
        if (lastReceivedTransaction < 0) {
            lastReceivedTransaction = 0;
            String sql = "SELECT last_received_transaction_id FROM destination WHERE name = '" + HDSS_COMPANION_NAME + "'";
            ResultSet rs = query(connMaster, sql);
            try {
                if (rs.next()) {
                    lastReceivedTransaction = rs.getInt("last_received_transaction_id");
                } else {
                    rs.close();
                    sql = "INSERT INTO destination SET name = '" + HDSS_COMPANION_NAME + "', last_received_transaction_id = 0";
                    execute(connMaster, sql);
                }
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lastReceivedTransaction;
    }

    /**
     * Looks for all new transactions from the shadow database, updates the MPI accordingly.
     * 
     * @throws SQLException 
     */
    public void updateAllTransactions() throws SQLException {
        int trans = getLastReceivedTransaction();
        String sql = "SELECT `id`, `type`, `table_id` FROM `transaction` WHERE `id` > " + trans + " ORDER BY `id`";
        ResultSet rsMaster = query(connMaster, sql);
        while (rsMaster.next()) {
            int transId = rsMaster.getInt("id");
            String transactionType = rsMaster.getString("type");
            int tableId = rsMaster.getInt("table_id");
            List<Row> rowList = new ArrayList<Row>();
            sql = "select d.data, c.name as column_name\n"
                    + "from transaction_data d\n"
                    + "join `column` c on c.id = d.column_id\n"
                    + "where d.transaction_id = " + transId;
            ResultSet rsDetail = query(connDetail, sql);
            String hdssId = null;
            while (rsDetail.next()) {
                String columnName = rsDetail.getString("column_name");
                String data = rsDetail.getString("data");
                if (columnName.equals("individid")) {
                    hdssId = data;
                } else {
                    Row row = new Row(rsDetail.getString("column_name"), rsDetail.getString("data"));
                    rowList.add(row);
                }
            }
            rsDetail.close();
            //TODO: Uncoment if statement
//            if (
            updateTransaction(transactionType, hdssId, rowList);
//                    ) {
            lastReceivedTransaction = transId;
            sql = "UPDATE `destination` SET `last_received_transaction_id` = " + transId + " WHERE `name` = '" + HDSS_COMPANION_NAME + "'";
            try {
                execute(connDetail, sql);
            } catch (Exception e) {
                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Couldn't update MPI.");
//                break;
            }
//            } else {
//                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Couldn't update MPI.");
//                break;
//            }
        }
        rsMaster.close();
    }

    /**
     * Processes a single new transaction from the shadow database and requests the MPI
     * to make the change.
     * 
     * @param transactionType Type of the transaction, "INSERT", "UPDATE", or "DELETE".
     * @param hdssId HDSS ID of the person this transaction is for.
     * @param rowList List of transaction row details (column name/value pairs).
     * @return true if we succeeded, otherwise false.
     */
    private boolean updateTransaction(String transactionType, String hdssId, List<Row> rowList) {
        Person p = new Person();
        int requestTypeId = 0;
        if (transactionType.equals("INSERT")) {
            requestTypeId = RequestTypeId.CREATE_PERSON_MPI;
        } else if (transactionType.equals("UPDATE")) {
            requestTypeId = RequestTypeId.MODIFY_PERSON_MPI;
        } else if (transactionType.equals("DELETE")) {
            //TODO: Determine and code how deletes be treated
            //Treat as modify but remove hdssid?
            requestTypeId = RequestTypeId.MODIFY_PERSON_MPI;
        } else {
            //this should never happen and should be logged as an error
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, "Unknown transaction type [{0}] "
                    + "exists in the shadow database", transactionType);
            return false;
        }
        String marriageStatus = null;
        String marriageType = null;
        Fingerprint fp1 = new Fingerprint();
        Fingerprint fp2 = new Fingerprint();
        String event = "";
        Date eventDate = null;
        for (Row r : rowList) {
            if (r.name.equals("fname")) {
                p.setFirstName(r.value);
            } else if (r.name.equals("jname")) {
                p.setMiddleName(r.value);
            } else if (r.name.equals("lname")) {
                p.setLastName(r.value);
            } else if (r.name.equals("famcla")) {
                p.setClanName(r.value);
            } else if (r.name.equals("akaname")) {
                p.setOtherName(r.value);
            } else if (r.name.equals("gender")) {
                p.setSex(parseSex(r.value));
            } else if (r.name.equals("dob")) {
                p.setBirthdate(parseDate(r.value));
            } else if (r.name.equals("mfname")) {
                p.setMothersFirstName(r.value);
            } else if (r.name.equals("mjname")) {
                p.setMothersMiddleName(r.value);
            } else if (r.name.equals("mlname")) {
                p.setMothersLastName(r.value);
            } else if (r.name.equals("ffname")) {
                p.setFathersFirstName(r.value);
            } else if (r.name.equals("fjname")) {
                p.setFathersMiddleName(r.value);
            } else if (r.name.equals("flname")) {
                p.setFathersLastName(r.value);
            } else if (r.name.equals("mtal")) {
                marriageStatus = r.value;
            } else if (r.name.equals("mtyp")) {
                marriageType = r.value;
            } else if (r.name.equals("cfname")) {
                p.setCompoundHeadFirstName(r.value);
            } else if (r.name.equals("cjname")) {
                p.setCompoundHeadMiddleName(r.value);
            } else if (r.name.equals("clname")) {
                p.setCompoundHeadLastName(r.value);
            } else if (r.name.equals("villname")) {
                p.setVillageName(r.value);
            } else if (r.name.equals("lasteventdate")) {
                eventDate = parseDate(r.value);
            } else if (r.name.equals("lastevent")) {
                event = r.value;
            } else if (r.name.equals("expectedDeliveryDate")) {
                p.setExpectedDeliveryDate(parseDate(r.value));
            } else if (r.name.equals("pregnancyEndDate")) {
                p.setPregnancyEndDate(parseDate(r.value));
            } else if (r.name.equals("pregnancyOutcome")) {
                p.setPregnancyOutcome(Person.PregnancyOutcome.valueOf(r.value));
            } else if (r.name.equals("f_Template")) {
                fp1.setTemplate(parseHex(r.value));
            } else if (r.name.equals("f_Type")) {
                fp1.setFingerprintType(parseFingerprintType(r.value));
            } else if (r.name.equals("f_Technology")) {
                fp1.setTechnologyType(parseTechnologyType(r.value));
            } else if (r.name.equals("f_DateEntered")) {
                fp1.setDateEntered(parseDate(r.value));
            } else if (r.name.equals("f_DateModified")) {
                fp1.setDateChanged(parseDate(r.value));
            } else if (r.name.equals("s_Template")) {
                fp2.setTemplate(parseHex(r.value));
            } else if (r.name.equals("s_Type")) {
                fp2.setFingerprintType(parseFingerprintType(r.value));
            } else if (r.name.equals("s_Technology")) {
                fp2.setTechnologyType(parseTechnologyType(r.value));
            } else if (r.name.equals("s_DateEntered")) {
                fp2.setDateEntered(parseDate(r.value));
            } else if (r.name.equals("s_DateModified")) {
                fp2.setDateChanged(parseDate(r.value));
            }
        }
        if (event.equalsIgnoreCase("DTH")) {
            p.setAliveStatus(Person.AliveStatus.no);
            p.setDeathdate(eventDate);
        } else if (event.equalsIgnoreCase("EXT")) {
            p.setLastMoveDate(eventDate);
        }
        //
        // Set marital status, if present.
        //
        if (marriageStatus != null) {
            Person.MaritalStatus ms = null;
            if (marriageStatus.equals("Married")) {
                if (marriageType != null) {
                    if (marriageType.equals("Monogamous")) {
                        ms = Person.MaritalStatus.marriedMonogamous;
                    } else if (marriageType.equals("Polygamous")) {
                        ms = Person.MaritalStatus.marriedPolygamous;
                    }
                }
            } else if (marriageStatus.equals("Single")) {
                ms = Person.MaritalStatus.single;
            } else if (marriageStatus.equals("Widowed")) {
                ms = Person.MaritalStatus.widowed;
            } else if (marriageStatus.equals("Divorced/Separated")) {
                ms = Person.MaritalStatus.divorced;
            }
            if (ms != null) {
                p.setMaritalStatus(ms);
            }
        }
        //
        // Set the Kisumu HDSS person identifier.
        //
        List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier(hdssId);
        pi.setIdentifierType(PersonIdentifier.Type.kisumuHdssId);
        personIdentifierList.add(pi);
        p.setPersonIdentifierList(personIdentifierList);
        //
        // Set the fingerprints if present.
        //
        List<Fingerprint> fingerprintList = new ArrayList<Fingerprint>();
        if (fp1.getTemplate() != null) {
            fingerprintList.add(fp1);
        }
        if (fp2.getTemplate() != null) {
            fingerprintList.add(fp2);
        }
        if (fingerprintList.size() > 0) {
            p.setFingerprintList(fingerprintList);
        } else {
            p.setFingerprintList(null); // No need to return exisiting fingerprints.
        }
        PersonResponse pr = requestMpi(p, requestTypeId);
        boolean returnStatus = false; // Assume failure for the moment.
        if (pr != null && pr.isSuccessful()) {
            returnStatus = true; // We succeeded!
        }
        return returnStatus;
    }

//    private Person getPersonFromMpi(String hdssId) {
//        Person p = new Person();
//        List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
//        PersonIdentifier pi = new PersonIdentifier();
//        pi.setIdentifier(hdssId);
//        pi.setIdentifierType(PersonIdentifier.Type.kisumuHdssId);
//        personIdentifierList.add(pi);
//        p.setPersonIdentifierList(personIdentifierList);
//        PersonResponse pr = requestMpi(p, RequestTypeId.FIND_PERSON_MPI);
//        Person returnPerson = null;
//        if (pr != null) {
//            List<Person> personList = pr.getPersonList();
//            if (personList != null && personList.size() > 0) {
//                returnPerson = personList.get(0);
//            }
//        }
//        return returnPerson;
//    }
    private PersonResponse requestMpi(Person p, int requestTypeId) {
        PersonRequest personRequest = new PersonRequest();
        personRequest.setPerson(p);
        personRequest.setResponseRequested(true); // Always request a response.
        Mediator mediator = KisumuHdss.getMediator();
        PersonResponse personResponse = (PersonResponse) mediator.getData(requestTypeId, personRequest);
        return personResponse;
    }

    /**
     * Parses a date and time in MySQL string format into a <code>Date</code>
     *
     * @param sDateTime contains date and time
     * @return the date and time in <code>Date</code> format
     * Returns null if the date and time string was null.
     */
    private Date parseDate(String sDateTime) {
        Date returnDateTime = null;
        if (sDateTime != null) {
            try {
                returnDateTime = SIMPLE_DATE_TIME_FORMAT.parse(sDateTime);
            } catch (ParseException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDateTime;
    }

    /**
     * Parses a hexadecimal-encoded string into a binary byte array.
     *
     * @param hex the hexadecimal string to unpack
     * @return the resulting binary byte array.
     * Returns null if the hex string was null.
     */
    private byte[] parseHex(String hex) {
        byte[] bytes = null;
        if (hex != null) {
            bytes = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
        }
        return bytes;
    }

    private Person.Sex parseSex(String sexString) {
        Person.Sex sex = null;
        try {
            sex = Person.Sex.valueOf(sexString);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sex;
    }

    private Person.MaritalStatus parseMaritalStatus(String maritalStatusString) {
        Person.MaritalStatus maritalStatus = null;
        try {
            maritalStatus = Person.MaritalStatus.valueOf(maritalStatusString);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        return maritalStatus;
    }

    private Fingerprint.TechnologyType parseTechnologyType(String technologyTypeString) {
        Fingerprint.TechnologyType technologyType = null;
        try {
            technologyType = Fingerprint.TechnologyType.valueOf(technologyTypeString);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        return technologyType;
    }

    private Fingerprint.Type parseFingerprintType(String fingerprintTypeString) {
        Fingerprint.Type type = null;
        try {
            type = Fingerprint.Type.valueOf(fingerprintTypeString);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
        return type;
    }
}
