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
import ke.go.moh.oec.oecsm.data.TransactionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.bridge.TransactionConverter;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.SchemaTransaction;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 13, 2010
 *
 * @author JGitahi
 */
public class SchemaSynchronizer extends DatabaseConnector {

    public void synchronize() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        try {
            List<SchemaTransaction> schemaTransactionList = new SchemaSynchronizer().generate();
            connectToShadow();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (SchemaTransaction schemaTransaction : schemaTransactionList) {
                System.out.println(TransactionConverter.convertToSQL(schemaTransaction));
                if (statement.executeUpdate(TransactionConverter.convertToSQL(schemaTransaction), Statement.RETURN_GENERATED_KEYS) == 1) {
                    ResultSet rs = statement.getGeneratedKeys();
                    if (rs.next()) {
                        if (schemaTransaction.getTarget().getClass() == Database.class) {
                            ((Database) schemaTransaction.getTarget()).setId(rs.getInt(1));
                        } else if (schemaTransaction.getTarget().getClass() == Table.class) {
                            ((Table) schemaTransaction.getTarget()).setId(rs.getInt(1));
                        } else if (schemaTransaction.getTarget().getClass() == Column.class) {
                            ((Column) schemaTransaction.getTarget()).setId(rs.getInt(1));
                        }
                    }
                }
            }
            connection.commit();
            statement.close();
        } finally {
            disconnectFromShadow();
        }
    }

    private List<SchemaTransaction> generate() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        List<SchemaTransaction> schemaTransactionList = new ArrayList<SchemaTransaction>();
        Database sourceDatabase = new SourceSchemaMiner().mine();
        Database shadowDatabase = new ShadowSchemaMiner().mine();
        if (shadowDatabase == null) {
            schemaTransactionList.add(new SchemaTransaction(null, TransactionType.INITIALIZE));
            schemaTransactionList.add(new SchemaTransaction(sourceDatabase, TransactionType.INSERT));
        }
        schemaTransactionList.addAll(generateTableTransactions(sourceDatabase, shadowDatabase));
        return schemaTransactionList;
    }

    private List<SchemaTransaction> generateTableTransactions(Database sourceDatabase, Database shadowDatabase) {
        List<SchemaTransaction> schemaTransactionList = new ArrayList<SchemaTransaction>();
        if (shadowDatabase == null) {
            for (Table table : sourceDatabase.getTableList()) {
                schemaTransactionList.add(new SchemaTransaction(table, TransactionType.INSERT));
                schemaTransactionList.addAll(generateColumnTransactions(table, null));
            }
        } else {
            Collections.sort(sourceDatabase.getTableList());
            Collections.sort(shadowDatabase.getTableList());
            for (Table table : sourceDatabase.getTableList()) {
                if (!shadowDatabase.getTableList().contains(table)) {
                    table.setDatabase(shadowDatabase);
                    schemaTransactionList.add(new SchemaTransaction(table, TransactionType.INSERT));
                    schemaTransactionList.addAll(generateColumnTransactions(table, null));
                } else {
                    Table shadowTable = shadowDatabase.getTableList().get(shadowDatabase.getTableList().indexOf(table));
                    if (!table.getPk().equals(shadowTable.getPk())) {
                        table.setId(shadowTable.getId());
                        schemaTransactionList.add(new SchemaTransaction(table, TransactionType.UPDATE));
                    }
                    schemaTransactionList.addAll(generateColumnTransactions(table, shadowTable));
                }
            }
            for (Table table : shadowDatabase.getTableList()) {
                if (!sourceDatabase.getTableList().contains(table)) {
                    schemaTransactionList.add(new SchemaTransaction(table, TransactionType.DELETE));
                }
            }
        }
        return schemaTransactionList;
    }

    private List<SchemaTransaction> generateColumnTransactions(Table sourceTable, Table shadowTable) {
        List<SchemaTransaction> shemaTransactionList = new ArrayList<SchemaTransaction>();
        if (shadowTable == null) {
            for (Column column : sourceTable.getColumnList()) {
                shemaTransactionList.add(new SchemaTransaction(column, TransactionType.INSERT));
            }
        } else {
            Collections.sort(sourceTable.getColumnList());
            Collections.sort(shadowTable.getColumnList());
            for (Column column : sourceTable.getColumnList()) {
                if (!shadowTable.getColumnList().contains(column)) {
                    column.setTable(shadowTable);
                    shemaTransactionList.add(new SchemaTransaction(column, TransactionType.INSERT));
                } else {
                    Column shadowColumn = shadowTable.getColumnList().get(shadowTable.getColumnList().indexOf(column));
                    if (column.getOrdinalPosition() != (shadowColumn.getOrdinalPosition())
                            || !column.getDataType().equals(shadowColumn.getDataType())
                            || column.getSize() != shadowColumn.getSize()) {
                        column.setId(shadowColumn.getId());
                        shemaTransactionList.add(new SchemaTransaction(column, TransactionType.UPDATE));
                    }
                }
            }
            for (Column column : shadowTable.getColumnList()) {
                if (!sourceTable.getColumnList().contains(column)) {
                    shemaTransactionList.add(new SchemaTransaction(column, TransactionType.DELETE));
                }
            }
        }
        return shemaTransactionList;
    }
}
