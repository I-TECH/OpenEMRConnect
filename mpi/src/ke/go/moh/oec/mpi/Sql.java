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
package ke.go.moh.oec.mpi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.lib.Mediator;

/**
 * SQL Database methods for the MPI.
 * 
 * @author Jim Grace
 */
public class Sql {

    private static class CachedConnection {

        protected Connection connection;
        protected long cachedTime;
    }
    public static final int WAIT_TIMEOUT_SECONDS = 1800; // Connections idle for longer than this in seconds will not be reused.
    public static final int REGULAR_VISIT_TYPE_ID = 1;
    public static final int ONE_OFF_VISIT_TYPE_ID = 2;
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Queue<CachedConnection> connectionPool = new LinkedList<CachedConnection>();

    /**
     * Creates a connection to the MPI(/LPI) database. For a query that is run while the results
     * are being fetched from another query, a separate connection is needed.
     * <p>
     * Connections are pooled for speed, and to avoid running out of connections.
     *
     * @return the connection.
     */
    public static Connection connect() {
        CachedConnection cc = null;
        Connection conn = null;
        try {
            // Try to get a previously-used connection from our pool. But if it was used
            // too long ago, then close it and look for another one. This is done to
            // avoid getting connection timeout errors from a connection that has been
            // idle for too long and the server has timed out the connection.
            synchronized (connectionPool) {
                while ((cc = connectionPool.poll()) != null) {
                    if (System.currentTimeMillis() - cc.cachedTime < WAIT_TIMEOUT_SECONDS * 1000) {
                        conn = cc.connection;
                        Mediator.getLogger(Sql.class.getName()).log(Level.FINER, "connect() - Reusing connection.");
                        break;
                    } else {
                        Mediator.getLogger(Sql.class.getName()).log(Level.FINER, "connect() - Timing out connection.");
                        cc.connection.close();
                    }
                }
            }
            // If we had no recent database connection in the pool that we can reuse,
            // then create a new connection.
            if (conn == null) {
                String url = Mediator.getProperty("MPI.url");
                String username = Mediator.getProperty("MPI.username");
                String password = Mediator.getProperty("MPI.password");
                conn = DriverManager.getConnection(url, username, password);
                Mediator.getLogger(Sql.class.getName()).log(Level.FINER, "connect() - Allocating new connection.");
            }
        } catch (Exception ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        }
        return conn;
    }

    /**
     * Closes a connection. 
     * 
     * @param conn the connection to close
     */
    public static void close(Connection conn) {
        // Load this connection and the current time into a CachedConnection object
        CachedConnection cc = new CachedConnection();
        cc.connection = conn;
        cc.cachedTime = System.currentTimeMillis();
        // Put the CachedConnection object into the pool for later reuse.
        synchronized (connectionPool) {
            connectionPool.add(cc);   // Actually, keep the connection for later reuse.
        }
    }

    /**
     * Closes a ResultSet, also closing the statement that it came from, if any.
     * 
     * @param rs ResultSet to close
     */
    public static void close(ResultSet rs) {
        try {
            if (!rs.isClosed()) {
                Statement stmt = rs.getStatement();
                if (stmt != null) {
                    stmt.close();
                }
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Executes a SQL query on a database connection.
     * Logs the query using logging level FINE.
     * 
     * @param conn the Connection to use.
     * @param sql the SQL query.
     * @return results of the query, as a ResultSet.
     */
    public static ResultSet query(Connection conn, String sql) {
        return query(conn, sql, Level.FINE);
    }

    /**
     * Executes a SQL query on a database connection.
     * Logs the query using a caller-supplied logging level.
     *
     * @param conn the Connection to use.
     * @param sql the SQL query.
     * @param loggerLevel the logger level to use for tracing.
     * @return results of the query, as a ResultSet.
     */
    public static ResultSet query(Connection conn, String sql, Level loggerLevel) {
        Mediator.getLogger(Sql.class.getName()).log(loggerLevel, "SQL Query:\n{0}", sql);
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
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }

        return rs;
    }

    /**
     * Tests to see whether a query returns any ResultSet rows.
     * 
     * @param conn Connection to use.
     * @param sql SQL statement.
     * @return true if there were rows in the ResultSet, otherwise false.
     */
    public static boolean resultExists(Connection conn, String sql) {
        boolean returnValue = false;
        ResultSet rs = query(conn, sql);
        try {
            returnValue = rs.next();
            Sql.close(rs);
        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    /**
     * Executes any SQL statement on a database connection.
     *
     * @param conn Connection to use.
     * @param sql SQL statement.
     * @return true if there was no exception, otherwise false.
     */
    public static boolean execute(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        boolean returnValue = true;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.execute();
            int updateCount = stmt.getUpdateCount();
            Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Starts a transaction on this connection.
     * 
     * @param conn the Connection to use.
     */
    public static void startTransaction(Connection conn) {
        execute(conn, "START TRANSACTION");
    }

    /**
     * Commits a transaction on this connection.
     * 
     * @param conn the Connection to use.
     */
    public static void commit(Connection conn) {
        // TODO: Use JDBC function for this.
        execute(conn, "COMMIT");
    }

    /**
     * Rolls Back a transaction on this connection.
     * 
     * @param conn the Connection to use.
     */
    public static void rollback(Connection conn) {
        // TODO: Use JDBC function for this.
        execute(conn, "ROLLBACK");
    }

    /**
     * Gets the auto-number ID generated from the most recent INSERT statement.
     * 
     * @param conn Connection to use.
     * @return the auto-generated ID.
     */
    public static String getLastInsertId(Connection conn) {
        // TODO: Use JDBC function for this.
        ResultSet rs = query(conn, "SELECT LAST_INSERT_ID()");
        String returnId = null;
        try {
            if (rs.next()) {
                returnId = rs.getString("LAST_INSERT_ID()");
            }
            Sql.close(rs);
        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.WARNING, "Unexpected error getting LAST_INSERT_ID()", ex);
        }
        return returnId;
    }

    /**
     * Gets an ID from a lookup table, given a lookup value.
     * 
     * @param conn Connection to use.
     * @param lookupTable SQL table name to look up in.
     * @param idColumn SQL column name containing the ID to return.
     * @param nameColumn SQL column name containing the name to look up.
     * @param lookupString value of the name to look up.
     * @return ID value.
     */
    public static String getLookupId(Connection conn, String lookupTable, String idColumn, String nameColumn, String lookupString) {
        String returnId = null;
        if (lookupString == null) {
            returnId = "null";
        } else {
            String sql = "SELECT " + idColumn + " FROM " + lookupTable + " WHERE " + nameColumn + " = " + quote(lookupString);
            ResultSet rs = query(conn, sql);
            try {
                if (rs.next()) {
                    returnId = rs.getString(idColumn);
                }
                Sql.close(rs);
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.WARNING, "getLookupId() error executing query " + sql, ex);
            }
        }
        return returnId;
    }

    /**
     * Gets the database ID for a Java enum Person.MaritalStatus value
     * 
     * @param conn Connection to use.
     * @param maritalStatus Enum value to look up
     * @return database ID for that marital status. Returns null if not found or value not supplied.
     */
    public static String getMaritalStatusId(Connection conn, Person.MaritalStatus maritalStatus) {
        String maritalStatusName = ValueMap.MARITAL_STATUS.getDb().get(maritalStatus);
        return getLookupId(conn, "marital_status_type", "marital_status_type_id", "marital_status_name", maritalStatusName);
    }

    /**
     * Gets the village_id corresponding to a village name.
     * Inserts a new village if necessary.
     * <p>
     * It is assumed that this method is called as part of a transaction.
     * This method does not start and and a transaction, so if a new village
     * is inserted, it will need the caller to start and stop the transaction.
     * 
     * @param conn Connection to use.
     * @param villageName village name to find.
     * @return the village ID.
     */
    public static String getVillageId(Connection conn, String villageName) {
        String returnId = null;
        if (villageName == null) {
            returnId = "null";
        } else {
            villageName = villageName.toUpperCase();
            ResultSet rs = query(conn, "SELECT village_id FROM village WHERE village_name = " + quote(villageName));
            try {
                if (rs.next()) {
                    returnId = Integer.toString(rs.getInt("village_id"));
                }
                Sql.close(rs);
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.WARNING, "Error getting village ID for " + quote(villageName), ex);
            }
            if (returnId == null) {
                execute(conn, "INSERT into village (village_name, editable) VALUES (" + quote(villageName) + ", 1)");
                returnId = getLastInsertId(conn);
            }
        }
        return returnId;
    }

    /**
     * Gets the address_id corresponding to a network address.
     * Inserts a new address if necessary.
     * <p>
     * It is assumed that this method is called as part of a transaction.
     * This method does not start and and a transaction, so if a new address
     * is inserted, it will need the caller to start and stop the transaction.
     * 
     * @param conn Connection to use.
     * @param address address to find.
     * @return the address ID.
     */
    public static String getAddressId(Connection conn, String address) {
        return getAddressId(conn, address, null);
    }

    /**
     * Gets the address_id corresponding to a network address.
     * Inserts a new address if necessary.
     * <p>
     * It is assumed that this method is called as part of a transaction.
     * This method does not start and and a transaction, so if a new address
     * is inserted, it will need the caller to start and stop the transaction.
     * 
     * @param conn Connection to use.
     * @param address address to find.
     * @param facilityName facility name (if any) corresponding to the address.
     * @return the address ID.
     */
    public static String getAddressId(Connection conn, String address, String facilityName) {
        String returnId = null;
        if (address == null) {
            returnId = "null";
        } else {
            address = address.toLowerCase(); // By convention, network addresses are all lower case.
            String existingFacilityName = null;
            ResultSet rs = query(conn, "SELECT address_id, facility_name FROM address WHERE address = " + quote(address));
            try {
                if (rs.next()) {
                    returnId = Integer.toString(rs.getInt("address_id"));
                    existingFacilityName = rs.getString("facility_name");
                }
                Sql.close(rs);
            } catch (SQLException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.WARNING, "Error getting address ID for " + quote(address), ex);
            }

            if (returnId == null) {
                execute(conn, "INSERT into address (address, facility_name) VALUES ("
                        + quote(address) + ", " + quote(facilityName) + ")");

                returnId = getLastInsertId(conn);
            } else if (facilityName != null
                    && (existingFacilityName == null || (facilityName.compareTo(existingFacilityName) != 0))) {
                execute(conn, "UPDATE address SET facility_name = " + quote(facilityName)
                        + "WHERE address = " + quote(address));
            }
        }
        return returnId;
    }

    /**
     * Quotes a string for use in a SQL statement.
     * Doubles single quotes (') and backslashes (\).
     * If the string is null, returns "null".
     * If the string is not null, returns the string with single quotes (') around it.
     * 
     * @param s string to quote.
     * @return quoted string.
     */
    public static String quote(String s) {
        if (s == null) {
            s = "null";
        } else {
            s = s.replace("'", "''");
            s = s.replace("\\", "\\\\");
            s = "'" + s + "'";
        }
        return s;
    }

    /**
     * Quotes a Date value for use in a SQL statement
     * <p>
     * If the date is null or January 1, 0001, return a value of null.
     * 
     * @param d Date to quote.
     * @return quoted date string.
     */
    public static String quote(Date d) {
        String returnString = "null"; // Default to return if date is null or January 1, 0001
        if (d != null) {
            String dateString = SIMPLE_DATE_TIME_FORMAT.format(d);
            if (!dateString.equals("0001-01-01 00:00:00.0")) {
                returnString = quote(dateString);
            }
        }
        return returnString;
    }

    /**
     * Quotes a byte array as a hexadecimal binary constant for use in a SQL statement.
     * The hexadecimal constant is of the form X'hexdigits'.
     * For example a byte array containing the byte values 0, 2A, 1 and 0
     * is represented as X'002A0100'
     * 
     * @param byteArray array of bytes to quote.
     * @return quoted binary string.
     */
    public static String quote(byte[] byteArray) {
        if (byteArray != null) {
            StringBuilder hex = new StringBuilder((byteArray.length * 2) + 3);
            hex.append("X'");
            for (byte b : byteArray) {
                hex.append(String.format("%02X", b));
            }
            hex.append("'");
            return hex.toString();
        } else {
            return null;
        }
    }
}
