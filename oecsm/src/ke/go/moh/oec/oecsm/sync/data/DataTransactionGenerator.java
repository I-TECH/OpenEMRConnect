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
import java.sql.ResultSet;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class DataTransactionGenerator {

    public List<Transaction> generate() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        SourceDataMiner sourceDataMiner = new SourceDataMiner();
        ShadowDataMiner shadowDataMiner = new ShadowDataMiner();
        sourceDataMiner.start();
        shadowDataMiner.start();
        Database database = new ShadowSchemaMiner().mine(true);
        for (Table table : database.getTableList()) {
            SourceResultSet source = sourceDataMiner.mine(table);
            ShadowResultSet shadow = shadowDataMiner.mine(table);
            //
            //  Do a merge comparison between the two ResultSets
            //  that are already sorted by primary key value.
            //
            source.next(); // Advance to the first source row of the table.
            shadow.next(); // Advance to the first shadow row of the table.
            while (!source.isAfterLast() || !shadow.isAfterLast()) {

                if (shadow.isAfterLast() || (!source.isAfterLast() && shadow.getString("PK").compareTo(source.getString("PK")) < 0)) {
                    //
                    //    I  N  S  E  R  T         (Source PK value is smaller)
                    //
                    LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.INSERT);
                    List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
                    for (Column column : table.getColumnList()) {
                        Cell cell = new Cell(source.getString("PK"), source.getString(column));
                        cell.setColumn(column);
                        transactionList.add(new DataTransaction(cell, TransactionType.INSERT));
                        loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
                    }
                    loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
                    transactionList.add(loggableTransaction);
                    source.next();

                } else if (source.isAfterLast() || source.getString("PK").compareTo(shadow.getString("PK")) < 0) {
                    //
                    //    D  E  L  E  T  E         (Shadow PK value is smaller)
                    //
                    LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.DELETE);
                    List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
                    for (Column column : table.getColumnList()) {
                        Cell cell = new Cell(shadow.getString("PK"), shadow.getString(column));
                        cell.setColumn(column);
                        transactionList.add(new DataTransaction(cell, TransactionType.DELETE));
                        loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
                    }
                    loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
                    transactionList.add(loggableTransaction);
                    shadow.next();

                } else { // PKs are the same. Test if the rest of the record also matches.
                    //
                    //    U  P  D  A  T  E    -  o  r  -    S  A  M  E         (PK values are equal)
                    //
                    LoggableTransaction loggableTransaction = new LoggableTransaction(table, TransactionType.UPDATE);
                    List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
                    for (Column column : table.getColumnList()) {
                        if (!shadow.getString(column).equals(source.getString(column))) {
                            Cell cell = new Cell(source.getString("PK"), source.getString(column));
                            cell.setColumn(column);
                            transactionList.add(new DataTransaction(cell, TransactionType.UPDATE));
                            loggableTransactionDatumList.add(new LoggableTransactionDatum(cell, loggableTransaction));
                        }
                    }
                    if (!loggableTransactionDatumList.isEmpty()) { // If not the same:
                        loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
                        transactionList.add(loggableTransaction);
                    }
                    source.next();
                    shadow.next();
                }
            }
            source.close();
            shadow.close();
        }
        sourceDataMiner.finish();
        shadowDataMiner.finish();
        return transactionList;
    }
//    public List<Transaction> generate() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
//        List<DataTransaction> dataTransactionList = new ArrayList<DataTransaction>();
//        List<Cell> sourceCellList = new SourceDataMiner().mine(new ShadowSchemaMiner().mine(true));
//        List<Cell> shadowCellList = new ShadowDataMiner().mine();
//        Collections.sort(sourceCellList);
//        Collections.sort(shadowCellList);
//        for (Cell cell : sourceCellList) {
//            if (!shadowCellList.contains(cell)) {
//                dataTransactionList.add(new DataTransaction(cell, TransactionType.INSERT));
//            } else {
//                Cell equivalentShadowDatum = shadowCellList.get(shadowCellList.indexOf(cell));
//                if (cell.getData() != null) {
//                    if (!cell.getData().equals(equivalentShadowDatum.getData())) {
//                        cell.setId(equivalentShadowDatum.getId());
//                        dataTransactionList.add(new DataTransaction(cell, TransactionType.UPDATE));
//                    }
//                } else {
//                    if (!(cell.getData() == null & equivalentShadowDatum.getData() == null)) {
//                        cell.setId(equivalentShadowDatum.getId());
//                        dataTransactionList.add(new DataTransaction(cell, TransactionType.UPDATE));
//                    }
//                }
//
//            }
//        }
//        for (Cell datum : shadowCellList) {
//            if (!sourceCellList.contains(datum)) {
//                dataTransactionList.add(new DataTransaction(datum, TransactionType.DELETE));
//            }
//        }
//        List<Transaction> transactionList = new ArrayList<Transaction>();
//        transactionList.addAll(dataTransactionList);
//        transactionList.addAll(generate(dataTransactionList));
//        return transactionList;
//    }
//
//    private List<LoggableTransaction> generate(List<DataTransaction> dataTransactionList) {
//        List<LoggableTransaction> loggableTransactionList = new ArrayList<LoggableTransaction>();
//        Collections.sort(dataTransactionList);
//        if (!dataTransactionList.isEmpty()) {
//            int i = 0;
//            int n = dataTransactionList.size();
//            TransactionType type = dataTransactionList.get(i).getType();
//            Table table = dataTransactionList.get(i).getCell().getColumn().getTable();
//            String primaryKeyValue = dataTransactionList.get(i).getCell().getPrimaryKeyValue();
//            for (int j = i; j < n; j++) {
//                if (dataTransactionList.get(j).getType().equals(type)) {
//                    if (dataTransactionList.get(j).getCell().getColumn().getTable().equals(table)) {
//                        LoggableTransaction loggableTransaction = new LoggableTransaction(table, type);
//                        List<LoggableTransactionDatum> = new ArrayList<LoggableTransactionDatum>();
//                        for (int k = j;; k++) {
//                            if (dataTransactionList.get(k).getCell().getPrimaryKeyValue().equals(primaryKeyValue)) {
//                                loggableTransactionDatumList.add(new LoggableTransactionDatum(dataTransactionList.get(k).getCell(), loggableTransaction));
//                                if (k == n - 1) {
//                                    loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
//                                    loggableTransactionList.add(loggableTransaction);
//                                    primaryKeyValue = dataTransactionList.get(k).getCell().getPrimaryKeyValue();
//                                    j = k;
//                                    break;
//                                }
//                            } else {
//                                loggableTransaction.setLoggableTransactionDatumList(loggableTransactionDatumList);
//                                loggableTransactionList.add(loggableTransaction);
//                                primaryKeyValue = dataTransactionList.get(k).getCell().getPrimaryKeyValue();
//                                j = k;
//                                j--;
//                                break;
//                            }
//                        }
//                    } else {
//                        if (j == n - 1) {
//                            break;
//                        }
//                        table = dataTransactionList.get(j).getCell().getColumn().getTable();
//                        j--;
//                    }
//                } else {
//                    if (j == n - 1) {
//                        break;
//                    }
//                    type = dataTransactionList.get(j).getType();
//                    j--;
//                }
//            }
//        }
//        return loggableTransactionList;
//    }
}
