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
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;

/**
 * Looks for all new transactions from the shadow database, updates the MPI
 * accordingly.
 *
 * @author Jim Grace
 */
public class Updater {

    private static int lastReceivedTransaction = -1;
    private Connection connTransaction = null; // Connection for the shadow database transaction query
    private Connection connTransactionId = null; // Connection for the shadow datatabase transaction id.
    private final static String HDSS_COMPANION_NAME = "HDSS COMPANION";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private String url = Mediator.getProperty("Shadow.url");
    private String username = Mediator.getProperty("Shadow.username");
    private String password = Mediator.getProperty("Shadow.password");

    /**
     * Used within the Updater class to hold a single row of transaction detail.
     */
    private class Row {

        private String name;
        private String value;

        /**
         * Constructs a row of transaction detail. A transaction detail consists
         * of a (name, value) pair, with the name of the database column to be
         * inserted/updated and value it will contain.
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
     * Creates and initializes an instance of the updater class. Creates
     * connections to the shadow database to be used for finding new shadow
     * transactions and transaction details.
     */
    public Updater() {
    }

//    /**
//     * Executes a SQL query on a database connection. Logs the query using a
//     * caller-supplied logging level.
//     *
//     * @param sql the SQL query.
//     * @return results of the query, as a ResultSet.
//     */
//    private ResultSet query(Connection conn, String sql) {
//        Mediator.getLogger(Updater.class.getName()).log(Level.FINER, "SQL Query:\n{0}", sql);
//        ResultSet rs = null;
//        try {
//            Statement statement = conn.createStatement();
//            // The following call sets the fetch size equal to the minimum integer value (largest negative value).
//            // This is a special value that is interpreted by the MySQL JDBC driver to fetch only one line
//            // at a time from the database into memory. Otherwise it would try to fetch the entire query
//            // result into memory. Because of the size of the MPI data, this can cause
//            // out of memory errors.
//            statement.setFetchSize(Integer.MIN_VALUE);
//            rs = statement.executeQuery(sql);
//        } catch (SQLException ex) {
//            Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE,
//                    "Error executing SQL Query " + sql, ex);
//            System.exit(1);
//        }
//        return rs;
//    }
    /**
     * Executes any SQL statement on a database connection.
     *
     * @param conn Connection to use.
     * @param sql SQL statement.
     * @return true if first result is a ResultSet.
     */
    private boolean execute(Connection conn, String sql) throws SQLException {
        Mediator.getLogger(Updater.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        boolean returnValue = false;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
            int updateCount = stmt.getUpdateCount();
            Mediator.getLogger(Updater.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
        } catch (SQLException ex) {
            Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return returnValue;
    }

    /**
     * Find the last transaction ID that we processed in the past. If there is
     * no record of what we processed in the past, set the last transaction ID
     * to 0 in the database and return 0.
     *
     * @return ID of the last transaction we processed in the past, or 0 if
     * none.
     */
    private int getLastReceivedTransaction() throws SQLException {
        if (lastReceivedTransaction < 0) {
            lastReceivedTransaction = 0;
            String sql = "SELECT last_received_transaction_id FROM destination WHERE name = '" + HDSS_COMPANION_NAME + "'";
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connTransactionId.createStatement();
                stmt.setFetchSize(Integer.MIN_VALUE);
                rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    lastReceivedTransaction = rs.getInt("last_received_transaction_id");
                } else {
                    rs.close();
                    sql = "INSERT INTO destination SET name = '" + HDSS_COMPANION_NAME + "', last_received_transaction_id = 0";
                    execute(connTransactionId, sql);
                }
                rs.close();
            } catch (SQLException ex) {
                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
            }
        }
        return lastReceivedTransaction;
    }

    /**
     * Looks for all new transactions from the shadow database, updates the MPI
     * accordingly.
     *
     * @throws SQLException
     */
    public void updateAllTransactions() throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connTransaction = DriverManager.getConnection(url, username, password);
            connTransactionId = DriverManager.getConnection(url, username, password);
            int trans = getLastReceivedTransaction();
            String sql = "SELECT t.`id`, t.`type`, d.`data`, c.`name`\n"
                    + "FROM `transaction` t\n"
                    + "JOIN `transaction_data` d on d.transaction_id = t.id\n"
                    + "JOIN `column` c on c.id = d.column_id\n"
                    + "WHERE t.`id` > " + trans + "\n"
                    + "ORDER BY t.`id`";

            stmt = connTransaction.createStatement();
            stmt.setFetchSize(Integer.MIN_VALUE);
            rs = stmt.executeQuery(sql);
            int currentTransactionId = 0;
            List<Row> rowList = null;
            String transactionType = null;
            String hdssId = null;
            while (rs.next()) {
                int transactionId = rs.getInt("id");
                if (transactionId != currentTransactionId) {
                    if (currentTransactionId != 0) {
                        boolean status = updateTransactionAndId(transactionType, hdssId, rowList, currentTransactionId);
                        if (!status) {
                            currentTransactionId = 0;
                            break;
                        }
                    }
                    currentTransactionId = transactionId;
                    transactionType = rs.getString("type");
                    rowList = new ArrayList<Row>();
                }
                String columnName = rs.getString("name");
                String data = rs.getString("data");
                if (columnName.equals("individid")) {
                    hdssId = data;
                } else {
                    Row row = new Row(columnName, data);
                    rowList.add(row);
                }
            }
            if (currentTransactionId != 0) {
                boolean status = updateTransactionAndId(transactionType, hdssId, rowList, currentTransactionId);
            }
        } catch (Exception ex) {
            Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (rs != null) {
                rs.close();
            }
            if (connTransaction != null) {
                connTransaction.close();
            }
            if (connTransactionId != null) {
                connTransactionId.close();
            }
        }
    }

    /**
     * Processes a single transaction and, if successful, updates the
     * last_received_transaction_id in the database.
     *
     * @param transactionType Type of the transaction, "INSERT", "UPDATE", or
     * "DELETE".
     * @param hdssId HDSS ID of the person this transaction is for.
     * @param rowList List of transaction row details (column name/value pairs).
     * @param transactionId ID of the transaction we are processing.
     * @return true if transaction processing succeeded, otherwise false.
     */
    private boolean updateTransactionAndId(String transactionType, String hdssId, List<Row> rowList, int transactionId) {
        boolean status = updateTransaction(transactionType, hdssId, rowList);
        if (status) {
            lastReceivedTransaction = transactionId;
            String sql = "UPDATE `destination` SET `last_received_transaction_id` = " + transactionId + " WHERE `name` = '" + HDSS_COMPANION_NAME + "'";
            try {
                execute(connTransactionId, sql);
            } catch (Exception e) {
                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Couldn't update last_received_transaction_id.", e);
                status = false;
            }
        }
        return status;
    }

    /**
     * Processes a single new transaction from the shadow database and requests
     * the MPI to make the change. <p> There is some special logic to handle the
     * case where the UPDATE statement is updating a non-null value to a null
     * value. In this case, we want to pack the person object with a special
     * value indicating that the field was present but null. For strings, this
     * special value is the empty string "". For dates, this special value is
     * January 1, 0001. <p> For enums, there is no such special value. At the
     * time this code was written, the only enums that are in the HDSS database
     * to be updated in the MPI are sex and marital status. It is not expected
     * that these would ever be changed in the HDSS database to null. Note that
     * pregnancy status does not need this special value, because it is not
     * stored in the MPI.
     *
     * @param transactionType Type of the transaction, "INSERT", "UPDATE", or
     * "DELETE".
     * @param hdssId HDSS ID of the person this transaction is for.
     * @param rowList List of transaction row details (column name/value pairs).
     * @return true if we succeeded, otherwise false.
     */
    private boolean updateTransaction(String transactionType, String hdssId, List<Row> rowList) {
        Person p = new Person();
        int requestTypeId;
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
            Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Unknown transaction type [{0}] "
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
            String value = r.value;
            if (value == null) {
                value = "";
            } else {
                value = value.trim(); // HDSS database values have a lot of trailing spaces.
                value = value.replace('\u2018', '\'').replace('\u2019', '\''); // Some values have "curly" quote characters -- cause problems for XML.
            }
            if (r.name.equals("fname")) {
                p.setFirstName(value);
            } else if (r.name.equals("jname")) {
                p.setMiddleName(value);
            } else if (r.name.equals("lname")) {
                p.setLastName(value);
            } else if (r.name.equals("famcla")) {
                p.setClanName(value);
            } else if (r.name.equals("akaname")) {
                p.setOtherName(value);
            } else if (r.name.equals("gender")) {
                p.setSex(HdssDataParser.sex(value, hdssId));
            } else if (r.name.equals("dob")) {
                p.setBirthdate(parseDate(value));
            } else if (r.name.equals("mfname")) {
                p.setMothersFirstName(value);
            } else if (r.name.equals("mjname")) {
                p.setMothersMiddleName(value);
            } else if (r.name.equals("mlname")) {
                p.setMothersLastName(value);
            } else if (r.name.equals("ffname")) {
                p.setFathersFirstName(value);
            } else if (r.name.equals("fjname")) {
                p.setFathersMiddleName(value);
            } else if (r.name.equals("flname")) {
                p.setFathersLastName(value);
            } else if (r.name.equals("mtal")) {
                marriageStatus = value;
            } else if (r.name.equals("mtyp")) {
                marriageType = value;
            } else if (r.name.equals("cfname")) {
                p.setCompoundHeadFirstName(value);
            } else if (r.name.equals("cjname")) {
                p.setCompoundHeadMiddleName(value);
            } else if (r.name.equals("clname")) {
                p.setCompoundHeadLastName(value);
            } else if (r.name.equals("villname")) {
                p.setVillageName(value);
            } else if (r.name.equals("lasteventdate")) {
                eventDate = parseDate(value);
            } else if (r.name.equals("lastevent")) {
                event = value;
            } else if (r.name.equals("expectedDeliveryDate")) {
                p.setExpectedDeliveryDate(parseDate(value));
            } else if (r.name.equals("pregnancyEndDate")) {
                p.setPregnancyEndDate(parseDate(value));
            } else if (r.name.equals("pregnancyOutcome")) {
                p.setPregnancyOutcome(HdssDataParser.pregnancyOutcome(value, hdssId));
            } else if (r.name.equals("f_Template")) {
                fp1.setTemplate(parseHex(value));
            } else if (r.name.equals("f_Type")) {
                fp1.setFingerprintType(HdssDataParser.fingerprintType(value, hdssId));
            } else if (r.name.equals("f_Technology")) {
                fp1.setTechnologyType(HdssDataParser.fingerprintTechnologyType(value, hdssId));
            } else if (r.name.equals("f_DateEntered")) {
                fp1.setDateEntered(parseDate(value));
            } else if (r.name.equals("f_DateModified")) {
                fp1.setDateChanged(parseDate(value));
            } else if (r.name.equals("s_Template")) {
                fp2.setTemplate(parseHex(value));
            } else if (r.name.equals("s_Type")) {
                fp2.setFingerprintType(HdssDataParser.fingerprintType(value, hdssId));
            } else if (r.name.equals("s_Technology")) {
                fp2.setTechnologyType(HdssDataParser.fingerprintTechnologyType(value, hdssId));
            } else if (r.name.equals("s_DateEntered")) {
                fp2.setDateEntered(parseDate(value));
            } else if (r.name.equals("s_DateModified")) {
                fp2.setDateChanged(parseDate(value));
            }
        }
        if (event.equalsIgnoreCase("DTH")) {
            //p.setAliveStatus(Person.AliveStatus.no);
            p.setDeathdate(eventDate);
        } else if (event.equalsIgnoreCase("EXT")) {
            p.setLastMoveDate(eventDate);
        }
        //
        // Set marital status, if present.
        //
        p.setMaritalStatus(HdssDataParser.maritalStatus(marriageStatus, marriageType, hdssId));
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
        boolean returnStatus = false; // Assume failure for the moment.
        //
        // If we are creating a new person entry, and the person has already died,
        // don't bother putting the entry in the MPI. However if this is an update
        // and the person has died, we want to update the person's status in the MPI.
        if (requestTypeId == RequestTypeId.CREATE_PERSON_MPI && p.getDeathdate() != null) {
            returnStatus = true; // Claim success; we won't insert a dead person into the MPI.
        } else {
            PersonResponse pr = requestMpi(p, requestTypeId);
            if (pr != null && pr.isSuccessful()) {
                returnStatus = true; // We succeeded!
            }
        }
        return returnStatus;
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
     * Parses a date and time in MySQL string format into a
     * <code>Date</code>
     *
     * @param sDateTime contains date and time
     * @return the date and time in <code>Date</code> format If the date and
     * time string was null, returns null. If the date and time string was
     * empty, returns January 1, 0001. This is done so there can be a special
     * value if the date is present but empty.
     */
    private Date parseDate(String sDateTime) {
        Date returnDateTime = null;
        if (sDateTime != null) {
            try {
                if (sDateTime.isEmpty()) {
                    returnDateTime = SIMPLE_DATE_FORMAT.parse("0001-01-01");
                } else {
                    returnDateTime = SIMPLE_DATE_TIME_FORMAT.parse(sDateTime);
                }
            } catch (ParseException ex) {
                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDateTime;
    }

    /**
     * Parses a hexadecimal-encoded string into a binary byte array.
     *
     * @param hex the hexadecimal string to unpack
     * @return the resulting binary byte array. Returns null if the hex string
     * was null or empty.
     */
    private byte[] parseHex(String hex) {
        byte[] bytes = null;
        if (hex != null && !hex.isEmpty()) {
            bytes = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
        }
        return bytes;
    }
}
