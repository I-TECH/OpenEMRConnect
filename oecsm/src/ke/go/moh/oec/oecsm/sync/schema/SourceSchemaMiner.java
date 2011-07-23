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

import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 13, 2010
 *
 * @author JGitahi
 */
public class SourceSchemaMiner extends DatabaseConnector {

    private DatabaseMetaData databaseMetaData;

    public Database mine() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        Database db = null;
        try {
            connectToSource();
            databaseMetaData = connection.getMetaData();
            db = new Database(database);
            populateTableList(db);
        } finally {
            disconnect();
        }
        return db;
    }

    private void populateTableList(Database database) throws SQLException {
        String[] tableTypeArray = tableTypes.split(",");
        ResultSet tableRs = databaseMetaData.getTables(null, schemaPattern, "%", tableTypeArray);
        while (tableRs.next()) {
            Table ts = new Table(tableRs.getString("TABLE_NAME"));
            String pks = extractPrimaryKeys(ts);
            System.out.println("Name " + tableRs.getString("TABLE_NAME")
                    + ", Cat " + tableRs.getString("TABLE_CAT")
                    + ", Schema " + tableRs.getString("TABLE_SCHEM")
                    + ", Type " + tableRs.getString("TABLE_TYPE"));

            // If it's a table, insist that it has a primary key.
            // If it's a view, we will assume the first column is the primary key.
            if (!pks.equals("") || tableRs.getString("TABLE_TYPE").equals("VIEW")) {
                ts.setPk(pks);
                ts.setDatabase(database);
                populateColumnList(ts);
                database.getTableList().add(ts);
            }
        }
        tableRs.close();
    }

    private void populateColumnList(Table table) throws SQLException {
        ResultSet columnRs = databaseMetaData.getColumns(null, null, table.getName(), null);
        while (columnRs.next()) {
            Column cs = new Column(columnRs.getString("COLUMN_NAME"), columnRs.getInt("ORDINAL_POSITION"), columnRs.getString("TYPE_NAME"), columnRs.getInt("COLUMN_SIZE"));
            if (table.getPk().equals("")) {
                table.setPk(cs.getName()); // First column of a view is considered to be the primary key.
            }
            cs.setTable(table);
            table.getColumnList().add(cs);
        }
        columnRs.close();
    }

    private String extractPrimaryKeys(Table tableStructure) throws SQLException {
        String primaryKeys = "";
        ResultSet pkRs = databaseMetaData.getPrimaryKeys(null, null, tableStructure.getName());
        while (pkRs.next()) {
            primaryKeys = primaryKeys + pkRs.getString("COLUMN_NAME") + ",";
        }
        pkRs.close();
        return primaryKeys;
    }
}
