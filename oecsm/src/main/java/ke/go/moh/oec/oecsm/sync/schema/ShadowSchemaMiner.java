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
package ke.go.moh.oec.oecsm.sync.schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 13, 2010
 *
 * @author Gitahi Ng'ang'a
 */
public class ShadowSchemaMiner extends DatabaseConnector {

    public Database mine(boolean replicable) throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        Database db = null;
        try {
            connectToShadow();
            db = extractDatabase();
            if (db == null) {
                return null;
            }
            populateTableList(db, replicable);
        } finally {
            disconnectFromShadow();
        }
        return db;
    }

    public Database mine() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        return mine(false);
    }

    private Database extractDatabase() throws SQLException {
        Database db = null;
        Statement statement = connection.createStatement();
        String sql = "SELECT `database`.`ID`, `database`.`NAME` FROM `database` WHERE `database`.`NAME` = '" + database + "'";
        Mediator.getLogger(ShadowSchemaMiner.class.getName()).log(Level.FINEST, sql);
        ResultSet rs = statement.executeQuery(sql);
        if (rs.next()) {
            db = new Database(rs.getInt("ID"), rs.getString("NAME"));
        }
        rs.close();
        statement.close();
        return db;
    }

    private void populateTableList(Database db, boolean replicable) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "SELECT `table`.`ID`, `table`.`NAME`, `PRIMARY_KEYS` FROM `table` WHERE `table`.`DATABASE_ID` = " + db.getId() + "";
        Mediator.getLogger(ShadowSchemaMiner.class.getName()).log(Level.FINEST, sql);
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Table table = new Table(rs.getInt("ID"), rs.getString("NAME"), rs.getString("PRIMARY_KEYS"));
            table.setDatabase(db);
            if (replicable) {
                populateColumnList(table);
            } else {
                populateReplicableColumnList(table);
            }
            db.getTableList().add(table);
        }
        rs.close();
        statement.close();
    }

    private void populateColumnList(Table table) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "SELECT `column`.`ID`, `column`.`NAME`, `column`.`ORDINAL_POSITION`, `column`.`DATA_TYPE`, `column`.`SIZE`, `column`.`REPLICABLE` FROM `column` WHERE `column`.`TABLE_ID` = " + table.getId() + "";
        Mediator.getLogger(ShadowSchemaMiner.class.getName()).log(Level.FINEST, sql);
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Column column = new Column(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("ORDINAL_POSITION"), rs.getString("DATA_TYPE"), rs.getInt("SIZE"), rs.getBoolean("REPLICABLE"));
            column.setTable(table);
            table.getColumnList().add(column);
        }
        rs.close();
        statement.close();
    }

    private void populateReplicableColumnList(Table table) throws SQLException {
        Statement statement = connection.createStatement();
        String sql = "SELECT `column`.`ID`, `column`.`NAME`, `column`.`ORDINAL_POSITION`, `column`.`DATA_TYPE`, `column`.`SIZE`, `column`.`REPLICABLE` FROM `column` WHERE `column`.`TABLE_ID` = " + table.getId() + " AND REPLICABLE = TRUE";
        Mediator.getLogger(ShadowSchemaMiner.class.getName()).log(Level.FINEST, sql);
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Column cs = new Column(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("ORDINAL_POSITION"), rs.getString("DATA_TYPE"), rs.getInt("SIZE"), rs.getBoolean("REPLICABLE"));
            cs.setTable(table);
            table.getColumnList().add(cs);
        }
        rs.close();
        statement.close();
    }
}
