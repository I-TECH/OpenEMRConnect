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
package ke.go.moh.oec.adt;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 *
 * @author pchemtai
 */
public class Sql {

    Connection conn = null;
    String url;
    String username;
    String password;
    String driver;
    String database;

    public static Connection connect() throws SQLException {
        Connection conn = null;
        try {
            String url = Mediator.getProperty("url");
            String username = Mediator.getProperty("username");
            String password = Mediator.getProperty("password");
            String database = Mediator.getProperty("database");
            String driver = Mediator.getProperty("driver");
            conn = DriverManager.getConnection(url, username, password);

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Sql.class.getName()).log(Level.SEVERE, null, ex);
                throw new DriverNotFoundException(ex);
            }

        } catch (Exception ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        }
        //conn.close();
        return conn;

    }

    public void testConnection(String driver, String url, String username, String password) throws DriverNotFoundException, SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new DriverNotFoundException(ex);
        }
        conn = DriverManager.getConnection(url, username, password);
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
            Logger.getLogger(AdtDataExtractor.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    public static ResultSet query(Connection conn, String sql) throws SQLException {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Query:\n{0}", sql);
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

           // stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;

    }
}
