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
package ke.go.moh.oec.cds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author DevWakhutu
 */
public class Sql {

    public static Connection connect() {
        Connection conn = null;

        try {
            String url = Mediator.getProperty("CDS.url");
            String username = Mediator.getProperty("CDS.username");
            String password = Mediator.getProperty("CDS.password");
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception ex) {
            Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);

            System.exit(1);
        }
        return conn;

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

    public static boolean execute(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        boolean returnValue = false;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
            int updateCount = stmt.getUpdateCount();
            Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
        } catch (SQLException ex) {
            Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    public static int executeUpdate(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        int returnValue = -1;
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                returnValue = rs.getInt(1);
            }
            int updateCount = stmt.getUpdateCount();
            Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
        } catch (SQLException ex) {
            Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    public static ResultSet query(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Query:\n{0}", sql);
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;
    }
}
