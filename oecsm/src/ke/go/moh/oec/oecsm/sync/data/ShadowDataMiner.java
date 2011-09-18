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
package ke.go.moh.oec.oecsm.sync.data;

import ke.go.moh.oec.oecsm.sync.data.resultsets.ShadowResultSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class ShadowDataMiner extends DatabaseConnector {

    Statement statement;

    public void start() throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        connectToShadow();
        statement = connection.createStatement();
        // The following call sets the fetch size equal to the minimum integer value (largest negative value).
        // This is a special value that is interpreted by the MySQL JDBC driver to fetch only one line
        // at a time from the database into memory. Otherwise it would try to fetch the entire query
        // result into memory. Because of the size of the shadow database, this can cause
        // out of memory errors. So note that this call assumes for now that the shadow database
        // is being stored in MySQL.
        statement.setFetchSize(Integer.MIN_VALUE);
    }

    public ShadowResultSet mine(Table table) throws InaccessibleConfigurationFileException, SQLException, DriverNotFoundException {
        ShadowResultSet srs = null;
        try {
            String compositePK = getQueryCustomizer().buildCompositePrimaryKey(table);
            String sql = "SELECT `cell`.`ID`, `cell`.`PRIMARY_KEY_VALUE`, `cell`.`DATA`, `cell`.`COLUMN_ID` FROM `cell` WHERE `COLUMN_ID` IN (";
            for (Column c : table.getColumnList()) {
                sql += c.getId() + ", "; // Add anyway, but the final one will be stripped.
            }
            sql = sql.substring(0,sql.length()-2); // Strip the final ", ".
            sql += ") ORDER BY `cell`.`PRIMARY_KEY_VALUE`";
            ResultSet rs = statement.executeQuery(sql);
            srs = new ShadowResultSet(rs);
        } finally {
        }
        return srs;
    }

    public void finish() throws SQLException {
        statement.close();
        disconnect();
    }
}
