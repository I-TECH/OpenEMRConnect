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

import java.sql.SQLException;
import ke.go.moh.oec.oecsm.data.DataTransaction;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.oecsm.bridge.Transaction;
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
 * @author JGitahi
 */
public class DataTransactionGenerator {

    private boolean sourceRsHasRecords = false;
    private boolean shadowRsHasRecords = false;

    public void generate() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
//        List<Transaction> transactionList = new ArrayList<Transaction>();
        SourceDataMiner sourceDataMiner = new SourceDataMiner();
        ShadowDataMiner shadowDataMiner = new ShadowDataMiner();
        sourceDataMiner.start();
        shadowDataMiner.start();
        Database database = new ShadowSchemaMiner().mine(true);
   
        for (Table table : database.getTableList()) {
            SourceResultSet sourceRs = sourceDataMiner.mine(table);
            ShadowResultSet shadowRs = shadowDataMiner.mine(table);
            sourceRsHasRecords = sourceRs.next(); // Advance to the first sourceRs row of the table if available.
            shadowRsHasRecords = shadowRs.next(); // Advance to the first shadowRs row of the table if available.
            while (sourceRsHasRecords || shadowRsHasRecords) {
                //We filter by which ResultSet contains any rows just so we are not trying to read
                //out of an empty ResultSet
                if (sourceRsHasRecords && !shadowRsHasRecords) {
                    //This IS an insert
             
                    insert(transactionList, table, sourceRs);
                } else if (!sourceRsHasRecords && shadowRsHasRecords) {
                    //This IS a delete
                    delete(transactionList, table, shadowRs);
                } else if (sourceRsHasRecords && shadowRsHasRecords) {
                    //  This MAY BE  an insert, a delete or an update so we do a merge comparison between the two ResultSets
                    String sourcePk = sourceRs.getString("PK");
                    String shadowPk = shadowRs.getCell("PK").getData();
                    if (sourcePk.compareTo(shadowPk) < 0) {
                        insert(transactionList, table, sourceRs);
                    } else if (sourcePk.compareTo(shadowPk) > 0) {
                        delete(transactionList, table, shadowRs);
                    } else if (sourcePk.compareTo(shadowPk) == 0) {
                        update(transactionList, table, sourceRs, shadowRs);
                    }
                }
            }
            sourceRs.close();
            shadowRs.close();
        }
        sourceDataMiner.finish();
        shadowDataMiner.finish();
        return transactionList;
    }

    private void insert(List<Transaction> transactionList, Table table, SourceResultSet sourceRs) throws SQLException {
        LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.INSERT);
        List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
        for (Column column : table.getColumnList()) {
            Cell cell = new Cell(sourceRs.getString("PK"), sourceRs.getString(column));
            cell.setColumn(column);
            transactionList.add(new DataTransaction(cell, TransactionType.INSERT));
            loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
        }
        loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
        transactionList.add(loggableTransaction);
        sourceRsHasRecords = sourceRs.next();
    }

    private void delete(List<Transaction> transactionList, Table table, ShadowResultSet shadowRs) throws SQLException {
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
        shadowRsHasRecords = shadowRs.next();
    }

    private void update(List<Transaction> transactionList, Table table, SourceResultSet sourceRs, ShadowResultSet shadowRs) throws SQLException {
        LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.UPDATE);
        List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
        for (Column column : table.getColumnList()) {
            /*
             * Ensure cells associated with new columns are created
             */
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

            Cell cell = shadowRs.getCell("PK");
            cell.setData(sourceRs.getString("PK"));
            cell.setColumn(table.getColumnList().get(1));
            loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));

            loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
            transactionList.add(loggableTransaction);
        }
        sourceRsHasRecords = sourceRs.next();
        shadowRsHasRecords = shadowRs.next();
    }
}
