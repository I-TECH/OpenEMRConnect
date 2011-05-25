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
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;

/**
 * SQL Database methods for the MPI.
 * 
 * @author Jim Grace
 */
public class Sql {

    /**
     * Creates a connection to the MPI(/LPI) database. For a query that is run while the results
     * are being fetched from another query, a separate connection is needed.
     *
     * @return The connection.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/mpi", "root", "root");
        } catch (Exception ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        }
        return conn;
    }

    /**
     * Executes a SQL query on a database connection.
     *
     * @param conn The Connection to use.
     * @param sql The SQL query.
     * @return the results of the query, in ResultSet form.
     */
    public static ResultSet query(Connection conn, String sql) {
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;
    }

    /**
     * Executes any SQL statement on a database connection.
     *
     * @param conn The Connection to use.
     * @param sql The SQL statement.
     * @return true if first result is a ResultSet.
     */
    public static boolean execute(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "Mpi.query({0})", sql);
        boolean returnValue = false;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    /**
     * Quotes a string for use in a SQL statement.
     * Doubles single quotes (') and backslashes (\).
     * If the string is null, returns "null".
     * If the string is not null, returns the string with single quotes (') around it.
     * 
     * @param s The string to quote
     * @return the quoted string
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
     * Quotes an enumerated value name for use in a SQL statement
     * @param e
     * @return 
     */
    public static String quote(Enum e) {
        if (e != null) {
            return quote(e.name());
        } else {
            return "null";
        }
    }
}
