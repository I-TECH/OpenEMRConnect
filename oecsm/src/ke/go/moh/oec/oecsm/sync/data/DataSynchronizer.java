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
import ke.go.moh.oec.oecsm.sync.data.resultsets.ShadowResultSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ke.go.moh.oec.oecsm.data.DataTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.bridge.Transaction;
import ke.go.moh.oec.oecsm.bridge.TransactionConverter;
import ke.go.moh.oec.oecsm.data.Cell;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.LoggableTransaction;
import ke.go.moh.oec.oecsm.data.LoggableTransactionDatum;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;
import ke.go.moh.oec.oecsm.sync.schema.ShadowSchemaMiner;
import ke.go.moh.oec.oecsm.data.TransactionType;

/**
 * @date Aug 19, 2010
 *
 * @author Gitahi Ng'ang'a
 */
public class DataSynchronizer extends DatabaseConnector {

    private boolean sourceRsHasRecords = false;
    private boolean shadowRsHasRecords = false;

    public void synchronize() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        SourceDataMiner sourceDataMiner = new SourceDataMiner();
        ShadowDataMiner shadowDataMiner = new ShadowDataMiner();
        sourceDataMiner.start();
        shadowDataMiner.start();
        Database shadowDb = new ShadowSchemaMiner().mine(true);

        for (Table table : shadowDb.getTableList()) {
            SourceResultSet sourceRs = sourceDataMiner.mine(table);
            ShadowResultSet shadowRs = shadowDataMiner.mine(table);

            sourceRsHasRecords = sourceRs.next();
            shadowRsHasRecords = shadowRs.next();

            String tempSourcePk = null;
            boolean sourceRsMovedNext = true;
            while (sourceRsHasRecords || shadowRsHasRecords) {
                if (sourceRsHasRecords && !shadowRsHasRecords) {
                    String sourcePk = sourceRs.getString("PK");
                    insert(table, sourcePk, sourceRs);
                } else if (!sourceRsHasRecords && shadowRsHasRecords) {
                    delete(table, shadowRs);
                } else if (sourceRsHasRecords && shadowRsHasRecords) {
                    String sourcePk;
                    if (!sourceRsMovedNext) {
                        sourcePk = tempSourcePk;
                    } else {
                        sourcePk = sourceRs.getString("PK");
                    }
                    tempSourcePk = sourcePk;

                    String shadowPk = shadowRs.getCell("PK").getData();
                    if (sourcePk.compareTo(shadowPk) < 0) {
                        insert(table, sourcePk, sourceRs);
                        sourceRsMovedNext = true;
                    } else if (sourcePk.compareTo(shadowPk) > 0) {
                        delete(table, shadowRs);
                        sourceRsMovedNext = false;
                    } else if (sourcePk.compareTo(shadowPk) == 0) {
                        update(table, sourcePk, sourceRs, shadowRs);
                        sourceRsMovedNext = true;
                    }
                }
            }
            sourceRs.close();
            shadowRs.close();
        }
        sourceDataMiner.finish();
        shadowDataMiner.finish();
    }

    private String curePk(String pk) {
        return pk.replace(" ", "#").replace("-", "_").replace(".", "?");
    }

    private void insert(Table table, String sourcePk, SourceResultSet sourceRs) throws SQLException {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.INSERT);
        List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
        String pk = null;
        for (Column column : table.getColumnList()) {
//            if (pk == null) {
//                pk = sourceRs.getString("PK");
//            }
            Cell cell = new Cell(sourcePk, sourceRs.getString(column));
            cell.setColumn(column);
            transactionList.add(new DataTransaction(cell, TransactionType.INSERT));
            loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
        }
        loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
        transactionList.add(loggableTransaction);
        processTransactions(transactionList);
        sourceRsHasRecords = sourceRs.next();
    }

    private void delete(Table table, ShadowResultSet shadowRs) throws SQLException {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.DELETE);
        List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
        for (Column column : table.getColumnList()) {
            Cell cell = shadowRs.getCell(column);
            cell.setColumn(column);
            transactionList.add(new DataTransaction(cell, TransactionType.DELETE));
            loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
        }
        loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
        transactionList.add(loggableTransaction);
        processTransactions(transactionList);
        shadowRsHasRecords = shadowRs.next();
    }

    private void update(Table table, String sourcePk, SourceResultSet sourceRs, ShadowResultSet shadowRs) throws SQLException {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.UPDATE);
        List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
//        String pk = null;
        for (Column column : table.getColumnList()) {
            /*
             * Ensure cells associated with new columns are created
             */
//            if (pk == null) {
//                pk = sourceRs.getString("PK");               
//            }
            Cell shadowCell = shadowRs.getCell(column);
            String sourceColumnValue = sourceRs.getString(column);
            if (shadowCell == null) {
                Cell cell = new Cell(shadowRs.getCell("PK").getData(), sourceColumnValue);
                cell.setColumn(column);
                transactionList.add(new DataTransaction(cell, TransactionType.INSERT));
                loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
                continue;
            }
            /*
             * Judge which columns we need to compare
             */
            String shadowColumnValue = shadowRs.getCell(column).getData();
            boolean pkColumn = table.getPk().contains(column.getName() + ",");
            boolean compare = (shadowColumnValue != null && sourceColumnValue != null)//compare only if both values are not null and this column is not (part of) the primary key
                    && !pkColumn;
            boolean ignore = (shadowColumnValue == null && sourceColumnValue == null);//if both values are null, no update has taken place
            boolean update = false;
            if (compare) {
                update = !shadowColumnValue.equals(sourceColumnValue);//if both values are not equal then an update has taken place
            } else {
                if (!pkColumn) {
                    update = !ignore;//if only one value is null then an update has taken place
                }
            }
            if (update) {
                Cell cell = shadowRs.getCell(column);
                cell.setData(sourceColumnValue);
                cell.setColumn(column);
                transactionList.add(new DataTransaction(cell, TransactionType.UPDATE));
                loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
            }
        }
        if (!loggableTransactionDatumList.isEmpty()) {
            // String pk = null;
            Cell cell = shadowRs.getCell("PK");
//            cell.setData(sourceRs.getString("PK"));
            cell.setData(sourcePk);

            //TODO: Accommodate cases where the pk is not the first column of the table
            cell.setColumn(table.getColumnList().get(0));
            loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));

            loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
            transactionList.add(loggableTransaction);
        }
        processTransactions(transactionList);
        sourceRsHasRecords = sourceRs.next();
        shadowRsHasRecords = shadowRs.next();
    }
    /*
     * Applies a set of transactions to the database
     */

    private void processTransactions(List<Transaction> dataTransactionList) throws SQLException {
        try {
            connectToShadow();
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (Transaction dataTransaction : dataTransactionList) {
                System.out.println(TransactionConverter.convertToSQL(dataTransaction));
                if (statement.executeUpdate(TransactionConverter.convertToSQL(dataTransaction), Statement.RETURN_GENERATED_KEYS) == 1) {
                    if (dataTransaction.getClass() == LoggableTransaction.class) {
                        ResultSet rs = statement.getGeneratedKeys();
                        LoggableTransaction loggableTransaction = (LoggableTransaction) dataTransaction;
                        if (rs.next()) {
                            loggableTransaction.setId(rs.getInt(1));
                        }
                        for (LoggableTransactionDatum loggableTransactionDatum : loggableTransaction.getLoggableTransactionDatumList()) {
                            System.out.println(TransactionConverter.convertToSQL(loggableTransactionDatum));
                            statement.executeUpdate(TransactionConverter.convertToSQL(loggableTransactionDatum));
                        }
                    }
                }
            }
            connection.commit();
            statement.close();
        } catch (Exception ex) {
            Logger.getLogger(DataSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            disconnectFromShadow();
        }
    }
}
