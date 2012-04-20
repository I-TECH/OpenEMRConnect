package ke.go.moh.oec.adt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.adt.HeaderData;
import ke.go.moh.oec.adt.DispensedData;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

public class adtDataExtractFromShadow extends DatabaseConnector {

    final static String OUTPUT_FILENAME = "out.csv";
    final static String SESQUICENTENNIAL_MILLIS = "4717440000000";
    final static String YEAR_MILLIS = "31556925994";
    final static int FILLER_CNT = 0;
    final static int MAX_VISIT_CNT = 100;
    private static int lastReceivedTransaction = -1;
    public Connection connTransaction = null;
    private Connection connTransactionId = null; // Connection for the shadow datatabase transaction id.
    private final static String ADT_COMPANION = "ADT COMPANION";
    Statement statement;

    public static void main(String[] args) throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        try {
            new adtDataExtractFromShadow().start();
//            Updater updater = new Updater();
            adtDataExtractFromShadow dataExtractFromShadow = new adtDataExtractFromShadow();
            dataExtractFromShadow.updateAllTransactions();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public adtDataExtractFromShadow() throws SQLException {
        try {
            connectToShadow();
            statement = connection.createStatement();
            statement.setFetchSize(Integer.MIN_VALUE);
        } catch (Exception ex) {
            Logger.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        }
        disconnectFromShadow();
    }

    public void start() throws InaccessibleConfigurationFileException, DriverNotFoundException, ClassNotFoundException, SQLException {
        HeaderData header = new HeaderData();
        DispensedData dispensed[] = new DispensedData[MAX_VISIT_CNT];
        for (int i = 0; i < MAX_VISIT_CNT; i++) {
            dispensed[i] = new DispensedData();
        }
        Connection con = Sql.connect();

        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME), "UTF-8");
            connectToShadow();
//            Statement stmt = connection.createStatement();
//            tblpatientMasterInformation
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select count(c.data) as total "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblARTpatientMasterInformation' "
                    + "and l.name = 'ArtID' "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id");
            int recCnt = 0;
            if (rs.next()) {
                recCnt = rs.getInt("total");
            }
            System.out.println("Found " + recCnt + " patients");

//            getLastReceivedTransaction();
            rs = stmt.executeQuery("select id AS trans_id from transaction where id > " + lastReceivedTransaction);


            System.out.println("Done!");
            connection.close();
            out.close();
        } catch (SQLException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());

        }

    }

    private static void ExtractHeaderData(String pid, HeaderData header) throws SQLException, ClassNotFoundException {
        Connection conn = Sql.connect();

        String sPatientData = "SELECT * FROM tblARTpatientMasterInformation "
                + "WHERE tblARTpatientMasterInformation.ARTid = '" + pid + "'";
        ResultSet rsPtData = Sql.query(conn, sPatientData);

        rsPtData.next();
        header.setArtID(pid);
        header.setFirstname(rsPtData.getString("Firstname"));
        header.setSurname(rsPtData.getString("Surname"));
        header.setGender(rsPtData.getString("sex"));
        rsPtData.close();

//        System.out.println(header.getFirstname());
    }

    private static void ExtractDispenseData(String pid, DispensedData[] dispensedData) throws SQLException, ClassNotFoundException {
        Connection con = Sql.connect();

        String sPtTransaction = "select ARTId, "
                + "dateofVisit,drugname,duration,dose FROM tblARTPatientTransactions "
                + "WHERE ARTId = '" + pid + "'";

        /*For each patient, read the transaction table to get the prescription information in all the visits.*/
        /**Loop over visits (only the most recent number as defined by MAX_VISIT_CNT variable) and pull required data**/
        ResultSet rsPtTransaction = Sql.query(con, sPtTransaction);
        int cnt = 0;
        while (rsPtTransaction.next() && cnt < MAX_VISIT_CNT) {
            dispensedData[cnt].setDateofVisit(rsPtTransaction.getString("dateofVisit"));
            dispensedData[cnt].setDose(rsPtTransaction.getString("dose"));
            dispensedData[cnt].setDrugname(rsPtTransaction.getString("drugname"));
            dispensedData[cnt].setDuration(rsPtTransaction.getString("duration"));

            cnt++;
        }
        rsPtTransaction.close();

    }

    private int getLastReceivedTransaction() throws SQLException {
        if (lastReceivedTransaction < 0) {
            lastReceivedTransaction = 0;
            String sql = "SELECT last_received_transaction_id FROM destination WHERE name = '" + ADT_COMPANION + "'";

            ResultSet rs = statement.executeQuery(sql);


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
                Logger.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lastReceivedTransaction;
    }

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

    public void updateAllTransactions() throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        int trans = getLastReceivedTransaction();
        String sql = "SELECT t.`id`, t.`type`, d.`data`, c.`name`\n"
                + "FROM `transaction` t\n"
                + "JOIN `transaction_data` d on d.transaction_id = t.id\n"
                + "JOIN `column` c on c.id = d.column_id\n"
                + "WHERE t.`id` > " + trans + "\n"
                + "ORDER BY t.`id`";
        //ResultSet rs = query(connTransaction, sql);
        ResultSet rs = statement.executeQuery(sql);
        int currentTransactionId = 0;
        List<Row> rowList = null;
        rs.next();
        String transactionType = rs.getString("type");
        String colname = rs.getString("name");
        String data = rs.getString("data");
        String artId = null;
        while (rs.next()) {
            if (transactionType.equals("UPDATE")) {
                if (colname.equals("ARTID")) {
                    artId = data;
                    System.out.println(" artid " + artId);
                }
            }
        }
//        if (currentTransactionId != 0) {
//            // update the destination table boolean status = updateTransactionAndId(transactionType, artId, rowList, currentTransactionId);
//        }
    }

    public boolean execute(Connection conn, String sql) {
        Mediator.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        boolean returnValue = false;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
            int updateCount = stmt.getUpdateCount();
            stmt.close();
            Mediator.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
        } catch (SQLException ex) {
            Logger.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    /**
     * Executes a SQL query on a database connection.
     * Logs the query using a caller-supplied logging level.
     *
     * @param sql the SQL query.
     * @return results of the query, as a ResultSet.
     */
    private ResultSet query(Connection conn, String sql) {
        Mediator.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.FINER, "SQL Query:\n{0}", sql);
        ResultSet rs = null;
        try {
            Statement statement = conn.createStatement();
            // The following call sets the fetch size equal to the minimum integer value (largest negative value).
            // This is a special value that is interpreted by the MySQL JDBC driver to fetch only one line
            // at a time from the database into memory. Otherwise it would try to fetch the entire query
            // result into memory. Because of the size of the  data, this can cause
            // out of memory errors.
            statement.setFetchSize(Integer.MIN_VALUE);
            rs = statement.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(adtDataExtractFromShadow.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;
    }
}
