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
package ke.go.moh.oec.oecsm.bridge;

import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.DataTransaction;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.LoggableTransaction;
import ke.go.moh.oec.oecsm.data.LoggableTransactionDatum;
import ke.go.moh.oec.oecsm.data.SchemaTransaction;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.data.TransactionType;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class TransactionConverter {

    public static String convertToSQL(Transaction transaction) {
        String sql = "";
        if (transaction.getType() == TransactionType.INITIALIZE) {
            sql = "DELETE FROM `database`;";
        } else {
            if (transaction.getClass() == SchemaTransaction.class) {
                SchemaTransaction schemaTransaction = (SchemaTransaction) transaction;
                if (schemaTransaction.getType() == TransactionType.INSERT) {
                    if (schemaTransaction.getTarget().getClass() == Database.class) {
                        Database database = (Database) schemaTransaction.getTarget();
                        sql = "INSERT INTO `database` (`database`.`NAME`) VALUES ('" + TransactionConverter.escapeSQL(database.getName()) + "');";
                    } else if (schemaTransaction.getTarget().getClass() == Table.class) {
                        Table table = (Table) schemaTransaction.getTarget();
                        sql = "INSERT INTO `table` (`table`.`NAME`, `table`.`PRIMARY_KEYS`, `table`.`DATABASE_ID`) VALUES ('" + TransactionConverter.escapeSQL(table.getName()) + "', '" + TransactionConverter.escapeSQL(table.getPk()) + "', " + table.getDatabase().getId() + ");";
                    } else if (schemaTransaction.getTarget().getClass() == Column.class) {
                        Column cs = (Column) schemaTransaction.getTarget();
                        sql = "INSERT INTO `column` (`column`.`NAME`, `column`.`ORDINAL_POSITION`, `column`.`DATA_TYPE`, `column`.`SIZE`, `column`.`REPLICABLE`, `column`.`TABLE_ID`) VALUES('" + TransactionConverter.escapeSQL(cs.getName()) + "', " + cs.getOrdinalPosition() + ", '" + TransactionConverter.escapeSQL(cs.getDataType()) + "', " + cs.getSize() + ", " + cs.isReplicable() + ", " + cs.getTable().getId() + ");";
                    }
                } else if (schemaTransaction.getType() == TransactionType.UPDATE) {
                    if (schemaTransaction.getTarget().getClass() == Table.class) {
                        Table table = (Table) schemaTransaction.getTarget();
                        sql = "UPDATE `table` SET `table`.`PRIMARY_KEYS` = '" + table.getPk() + "' WHERE `table`.`ID` = " + table.getId() + ";";
                    } else if (schemaTransaction.getTarget().getClass() == Column.class) {
                        Column column = (Column) schemaTransaction.getTarget();
                        sql = "UPDATE `column` SET `column`.`ORDINAL_POSITION` = " + column.getOrdinalPosition() + ", `column`.`DATA_TYPE` = '" + column.getDataType() + "', `column`.`SIZE` = " + column.getSize() + " WHERE `column`.`ID` = " + column.getId() + ";";
                    }
                } else if (schemaTransaction.getType() == TransactionType.DELETE) {
                    if (schemaTransaction.getTarget().getClass() == Database.class) {
                        Database database = (Database) schemaTransaction.getTarget();
                        sql = "DELETE FROM `database` WHERE `database`.`ID` = " + database.getId() + ";";
                    } else if (schemaTransaction.getTarget().getClass() == Table.class) {
                        Table table = (Table) schemaTransaction.getTarget();
                        sql = "DELETE FROM `table` WHERE `table`.`ID` = " + table.getId() + ";";
                    } else if (schemaTransaction.getTarget().getClass() == Column.class) {
                        Column column = (Column) schemaTransaction.getTarget();
                        sql = "DELETE FROM `column` WHERE `column`.`ID` = " + column.getId() + ";";
                    }
                }
            } else if (transaction.getClass() == DataTransaction.class) {
                DataTransaction dataTransaction = (DataTransaction) transaction;
                String data = dataTransaction.getCell().getData();
                if (dataTransaction.getType() == TransactionType.INSERT) {
                    if (data != null) {
                        sql = "INSERT INTO `cell`(`cell`.`PRIMARY_KEY_VALUE`, `cell`.`DATA`, `cell`.`COLUMN_ID`) VALUES('" + TransactionConverter.escapeSQL(dataTransaction.getCell().getPrimaryKeyValue()) + "', '" + TransactionConverter.escapeSQL(dataTransaction.getCell().getData()) + "', " + dataTransaction.getCell().getColumn().getId() + ");";
                    } else {
                        sql = "INSERT INTO `cell`(`cell`.`PRIMARY_KEY_VALUE`, `cell`.`DATA`, `cell`.`COLUMN_ID`) VALUES('" + TransactionConverter.escapeSQL(dataTransaction.getCell().getPrimaryKeyValue()) + "', NULL, " + dataTransaction.getCell().getColumn().getId() + ");";
                    }
                } else if (dataTransaction.getType() == TransactionType.UPDATE) {
                    if (data != null) {
                        sql = "UPDATE `cell` SET `cell`.`DATA` = '" + TransactionConverter.escapeSQL(data) + "' WHERE `cell`.`ID` = " + dataTransaction.getCell().getId() + ";";
                    } else {
                        sql = "UPDATE `cell` SET `cell`.`DATA` = NULL WHERE `cell`.`ID` = " + dataTransaction.getCell().getId() + ";";
                    }
                } else if (dataTransaction.getType() == TransactionType.DELETE) {
                    sql = "DELETE FROM `cell` WHERE `cell`.`ID` = " + dataTransaction.getCell().getId() + ";";
                }
            } else if (transaction.getClass() == LoggableTransaction.class) {
                LoggableTransaction loggableTransaction = (LoggableTransaction) transaction;
                sql = "INSERT INTO `transaction`(`transaction`.`TYPE`, `transaction`.`TABLE_ID`, `transaction`.`CREATED_DATETIME`) VALUES('" + loggableTransaction.getType() + "', " + loggableTransaction.getTable().getId() + ", NOW());";
            } else if (transaction.getClass() == LoggableTransactionDatum.class) {
                LoggableTransactionDatum loggableTransactionDatum = (LoggableTransactionDatum) transaction;
                sql = "INSERT INTO `transaction_data`(`transaction_data`.`DATA`, `transaction_data`.`COLUMN_ID`, `transaction_data`.`TRANSACTION_ID`) VALUES('" + TransactionConverter.escapeSQL(loggableTransactionDatum.getCell().getData()) + "', " + loggableTransactionDatum.getCell().getColumn().getId() + ", " + loggableTransactionDatum.getLoggableTransaction().getId() + ");";
            }
        }
        return sql;
    }

    private static String escapeSQL(String sql) {
        String escapedSql = "";
        if (sql != null) {
            escapedSql = sql.replaceAll("'", "''");
            escapedSql = escapedSql.replaceAll("\\\\", "\\\\\\\\");
        }
        return escapedSql;
    }
}
