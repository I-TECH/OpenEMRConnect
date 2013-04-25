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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            disconnectFromSource();
        }
        return db;
    }

    private void populateTableList(Database database) throws SQLException {
        // If we have a list of tables from the properties file, then load those tables.
        if (tableList != null) {
            //String tablename;
            String[] tableListArray = tableList.split(",");
            for (int i = 0; i < tableListArray.length; i++) {
                setupTable(database, tableListArray[i].toString(), tableTypes);
            }

        } else {
            // If there was no list of tables in the properties file, then load every table
            // that matches one of the table types.

            String[] tableTypeArray = tableTypes.split(",");
            ResultSet tableRs = databaseMetaData.getTables(null, schemaPattern, "%", tableTypeArray);
            while (tableRs.next()) {
                setupTable(database, tableRs.getString("TABLE_NAME"), tableRs.getString("TABLE_TYPE"));
            }
            tableRs.close();
        }
    }

    private void setupTable(Database database, String tableName, String tableType) throws SQLException {
        Table ts = new Table(tableName);
        String pks;
        if (url.contains("odbc")) {
            pks = extractAccessPrimaryKeys(ts);
        } else {
            pks = extractPrimaryKeys(ts);
        }

        // If it's a table, insist that it has a primary key.
        // If it's a view, we will assume the first column is the primary key.
        if (!pks.equals("") || tableType.equals("VIEW")) {
            ts.setPk(pks);
            ts.setDatabase(database);
            populateColumnList(ts);
            database.getTableList().add(ts);
        }
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

    //This method is specifically for Ms Access databases. Extracting Primary keys from an access database;
    private String extractAccessPrimaryKeys(Table tableStructure) throws SQLException {
        String primaryKeys = "";
        ResultSet pkRs = databaseMetaData.getIndexInfo(null, null, tableStructure.getName(), true, true);
        while (pkRs.next()) {
            String idx = pkRs.getString(6);
            if (idx != null) {
                if (idx.equalsIgnoreCase("PrimaryKey")) {
                    primaryKeys = primaryKeys + pkRs.getString("COLUMN_NAME") + ",";
                }
            }
        }
        pkRs.close();
        return primaryKeys;
    }
}
