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

import ke.go.moh.oec.oecsm.sync.data.resultsets.SourceResultSet;
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
public class SourceDataMiner extends DatabaseConnector {

    Statement statement;

    public void start() throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        connectToSource();
        statement = connection.createStatement();
    }

    /**
     * Gets all the rows from a source table, returned as a ResultSet
     * 
     * @param table
     * @return
     * @throws SQLException 
     */
    public SourceResultSet mine(Table table) throws SQLException {
        SourceResultSet srs = null;
        try {
            String compositePK = getQueryCustomizer().buildCompositePrimaryKey(table);
            String sql = "SELECT ASC(" + compositePK + ") AS ASCII_ID, " + compositePK + " AS PK";
            String prefix = getQueryCustomizer().getOpenningSafetyPad();
            String suffix = getQueryCustomizer().getClosingSafetyPad();
            for (Column column : table.getColumnList()) {
                sql += ", " + prefix + column.getName() + suffix + " AS C" + column.getId();
            }
            sql += " FROM " + prefix + table.getName() + suffix + " ORDER BY 1 ASC, " + compositePK + "ASC";
            ResultSet rs = statement.executeQuery(sql);
            srs = new SourceResultSet(rs);
        } finally {
        }
        return srs;
    }

    public void finish() throws SQLException {
        statement.close();
        disconnectFromSource();
    }
}
