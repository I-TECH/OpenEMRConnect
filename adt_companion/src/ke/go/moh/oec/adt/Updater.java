/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.adt;

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
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;

/**
 *
 * @author Administrator
 */
public class Updater extends DatabaseConnector {

    private static int lastReceivedTransaction = -1;
    private Connection connTransaction = null; // Connection for the shadow database transaction query
    private Connection connTransactionId = null; // Connection for the shadow datatabase transaction id.
    private final static String ADT_COMPANION = "ADT COMPANION";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
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
            connectToShadow();
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
            Statement statement = conn.createStatement();
            // The following call sets the fetch size equal to the minimum integer value (largest negative value).
            // This is a special value that is interpreted by the MySQL JDBC driver to fetch only one line
            // at a time from the database into memory. Otherwise it would try to fetch the entire query
            // result into memory. Because of the size of the MPI data, this can cause
            // out of memory errors.
            statement.setFetchSize(Integer.MIN_VALUE);
            rs = statement.executeQuery(sql);
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
            stmt.close();
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
    private int getLastReceivedTransaction() throws SQLException {
        if (lastReceivedTransaction < 0) {
            lastReceivedTransaction = 0;
            String sql = "SELECT last_received_transaction_id FROM destination WHERE name = '" + ADT_COMPANION + "'";
            ResultSet rs = Sql.query(connection, sql);

            try {
                if (rs.next()) {
                    lastReceivedTransaction = rs.getInt("last_received_transaction_id");
                } else {
                    rs.close();
                    sql = "INSERT INTO destination SET name = '" + ADT_COMPANION + "', last_received_transaction_id = 0";
                    execute(connTransactionId, sql);
                }
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lastReceivedTransaction;
    }

    /**
     * Looks for all new transactions from the shadow database, updates the  accordingly.
     * 
     * @throws SQLException 
     */
    public void updateAllTransactions() throws SQLException {
        int trans = getLastReceivedTransaction();
        String sql = "SELECT t.`id`, t.`type`, d.`data`, c.`name`\n"
                + "FROM `transaction` t\n"
                + "JOIN `transaction_data` d on d.transaction_id = t.id\n"
                + "JOIN `column` c on c.id = d.column_id\n"
                //                + "WHERE t.`id` > " + trans + "\n"
                + "WHERE t.`id` =1\n"
                + "ORDER BY t.`id`";
        ResultSet rs = Sql.query(connection, sql);
        int currentTransactionId = 0;
        List<Row> rowList = null;
        String transactionType = null;
        String ARTID = null;
        while (rs.next()) {
            int transactionId = rs.getInt("id");
            if (transactionId != currentTransactionId) {
                if (currentTransactionId != 0) {//need to do something here
                    currentTransactionId = transactionId;
                    transactionType = rs.getString("type");
                    rowList = new ArrayList<Row>();
                }
            }
            String columnName = rs.getString("name");
            String data = rs.getString("data");
            if (columnName.equals("ARTID")) {
                ARTID = data;
            } else {
                Row row = new Row(columnName, data);
                rowList.add(row);
                System.out.println(" Row value " + row);
            }

        }
    }

    private boolean updateTransactionAndId(String transactionType, String artId, List<Row> rowList, int transactionId) {
        boolean status = updateTransaction(transactionType, artId, rowList);
        if (status) {
            lastReceivedTransaction = transactionId;
            String sql = "UPDATE `destination` SET `last_received_transaction_id` = " + transactionId + " WHERE `name` = '" + ADT_COMPANION + "'";
            try {
                execute(connTransactionId, sql);
            } catch (Exception e) {
                Mediator.getLogger(Updater.class.getName()).log(Level.SEVERE, "Couldn't update last_received_transaction_id.", e);
                status = false;
            }
        }
        return status;
    }

    private boolean updateTransaction(String transactionType, String artId, List<Row> rowList) {
        boolean returnStatus = true;
        
        if (transactionType.equals("UPDATE")) {
            // insert person pid into patient ids table           ;
            String sql = "INSERT INTO destination SET name = '" + ADT_COMPANION + "', last_received_transaction_id = 0";

        } else if (transactionType.equals("UPDATE")) {
            // check if artId exists in the patient ids table if does do an update based on the date entered 
        } else {
            //this should never happen and should be logged as an error
            Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, "Unknown transaction type [{0}] "
                    + "exists in the shadow database", transactionType);
            return false;
        }

        return returnStatus;
    }

    /**
     * Parses a date and time in MySQL string format into a <code>Date</code>
     *
     * @param sDateTime contains date and time
     * @return the date and time in <code>Date</code> format
     * If the date and time string was null, returns null.
     * If the date and time string was empty, returns January 1, 0001.
     * This is done so there can be a special value if the date is present but empty.
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
     * Returns null if the hex string was null or empty.
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
