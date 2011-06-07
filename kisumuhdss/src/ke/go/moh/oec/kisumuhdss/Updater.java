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
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author EWere
 */
public class Updater {

    static int lastTransaction = -1;
    Connection conn = null;

    public Updater() {
        try {
            String url = Mediator.getProperty("Shadow.url");
            String username = Mediator.getProperty("Shadow.username");
            String password = Mediator.getProperty("Shadow.password");
            conn = DriverManager.getConnection(url, username, password);
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
    private ResultSet query(String sql) {
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

    private int getLastTransaction() {
        if (lastTransaction < 0) {
            lastTransaction = 0;
            ResultSet rs = query("SELECT MAX(id) as max_id FROM transaction");
            try {
                if (rs.next()) {
                    lastTransaction = rs.getInt("max_id");
                }
            } catch (SQLException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lastTransaction;
    }

    public void update() {
        int trans = getLastTransaction();
        
    }
}
