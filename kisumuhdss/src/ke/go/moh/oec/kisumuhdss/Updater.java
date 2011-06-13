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
 *
 * @author Jim Grace
 */
public class Updater {

    private static int lastReceivedTransaction = -1;
    private Connection connMaster = null; // Connection for master query
    private Connection connDetail = null; // Connection for detail query
    private final static String HDSS_COMPANION_NAME = "HDSS COMPANION";
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private class Row {

        private String name;
        private String value;

        private Row(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

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

    private int getLastReceivedTransaction() {
        if (lastReceivedTransaction < 0) {
            lastReceivedTransaction = 0;
            String sql = "SELECT last_received_transaction_id FROM destination WHERE name = " + HDSS_COMPANION_NAME;
            ResultSet rs = query(connMaster, sql);
            try {
                if (rs.next()) {
                    lastReceivedTransaction = rs.getInt("last_received_transaction_id");
                }
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lastReceivedTransaction;
    }

    public void updateAllTransactions() throws SQLException {
        int trans = getLastReceivedTransaction();
        String sql = "select id, type from transaction where id > " + trans + " order by id";
        ResultSet rsMaster = query(connMaster, sql);
        while (rsMaster.next()) {
            int transId = rsMaster.getInt("id");
            String transType = rsMaster.getString("type");
            List<Row> rowList = new ArrayList<Row>();
            sql = "select d.data, c.name as column_name\n"
                    + "from transaction_data d\n"
                    + "join `column` c on c.id = d.column_id\n"
                    + "where d.transaction_id = " + trans;
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
            if (updateTransaction(transId, hdssId, rowList)) {
                lastReceivedTransaction = transId;
                sql = "UPDATE destination SET last_received_transaction_id = " + transId + " where name = " + HDSS_COMPANION_NAME;
                if (!execute(connDetail, sql)) {
                    Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Couldn't SET last_received_transaction_id.");
                }
            } else {
                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Couldn't update MPI.");
                break;
            }
        }
        rsMaster.close();
    }

    private boolean updateTransaction(int transId, String hdssId, List<Row> rowList) {
        Person p = getPersonFromMpi(hdssId);
        int requestTypeId = RequestTypeId.MODIFY_PERSON_MPI;
        if (p == null) {
            p = new Person();
            requestTypeId = RequestTypeId.CREATE_PERSON_MPI;
        }
        String marriageStatus = null;
        String marriageType = null;
        Fingerprint fp1 = new Fingerprint();
        Fingerprint fp2 = new Fingerprint();
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
                p.setSex(Person.Sex.valueOf(r.value));
            } else if (r.name.equals("dob")) {
                p.setBirthdate(parseDate(r.value));
            } else if (r.name.equals("deathdate")) {
                p.setDeathdate(parseDate(r.value));
            } else if (r.name.equals("mfname")) {
                p.setMothersFirstName(r.value);
            } else if (r.name.equals("mjname")) {
                p.setMothersMiddleName(r.value);
            } else if (r.name.equals("mlname")) {
                p.setMothersLastName(r.value);
            } else if (r.name.equals("ffname")) {
                p.setFathersFirstName(r.value);
            } else if (r.name.equals("fmname")) {
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
            } else if (r.name.equals("village")) {
                p.setFathersMiddleName(r.value);
            } else if (r.name.equals("lasemoveddate")) {
                p.setLastMoveDate(parseDate(r.value));
            } else if (r.name.equals("expDeliveryDate")) {
                p.setFathersMiddleName(r.value);
            } else if (r.name.equals("pEjdDate")) {
                p.setPregnancyEndDate(parseDate(r.value));
            } else if (r.name.equals("pOutCome")) {
                p.setPregnancyOutcome(Person.PregnancyOutcome.valueOf(r.value));
            } else if (r.name.equals("f_Template")) {
                fp1.setTemplate(parseHex(r.value));
            } else if (r.name.equals("f_Type")) {
                fp1.setFingerprintType(Fingerprint.Type.valueOf(r.value));
            } else if (r.name.equals("f_Technology")) {
                fp1.setTechnologyType(Fingerprint.TechnologyType.griauleTemplate);
            } else if (r.name.equals("f_DateEntered")) {
                fp1.setDateEntered(parseDate(r.value));
            } else if (r.name.equals("f_DateModified")) {
                fp1.setDateChanged(parseDate(r.value));
            } else if (r.name.equals("s_Template")) {
                fp2.setTemplate(parseHex(r.value));
            } else if (r.name.equals("s_Type")) {
                fp2.setFingerprintType(Fingerprint.Type.valueOf(r.value));
            } else if (r.name.equals("s_Technology")) {
                fp2.setTechnologyType(Fingerprint.TechnologyType.griauleTemplate);
            } else if (r.name.equals("s_DateEntered")) {
                fp2.setDateEntered(parseDate(r.value));
            } else if (r.name.equals("s_DateModified")) {
                fp2.setDateChanged(parseDate(r.value));
            }
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

    private Person getPersonFromMpi(String hdssId) {
        Person p = new Person();
        List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier(hdssId);
        pi.setIdentifierType(PersonIdentifier.Type.kisumuHdssId);
        personIdentifierList.add(pi);
        PersonResponse pr = requestMpi(p, RequestTypeId.FIND_PERSON_MPI);
        Person returnPerson = null;
        if (pr != null) {
            List<Person> personList = pr.getPersonList();
            if (personList != null && personList.size() > 0) {
                returnPerson = personList.get(0);
            }
        }
        return returnPerson;
    }

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
}
