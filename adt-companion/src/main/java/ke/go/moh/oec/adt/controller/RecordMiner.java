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
package ke.go.moh.oec.adt.controller;

import ke.go.moh.oec.adt.data.RecordSource;
import ke.go.moh.oec.adt.data.Column;
import ke.go.moh.oec.adt.data.Transaction;
import ke.go.moh.oec.adt.exceptions.BadRecordSourceException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import ke.go.moh.oec.adt.data.Record;
import ke.go.moh.oec.adt.data.TransactionType;
import ke.go.moh.oec.lib.Mediator;

/**
 * @date Apr 25, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class RecordMiner {

    public Map<RecordSource, List<Record>> mine(Map<RecordSource, Map<Integer, Transaction>> transactionMap)
            throws SQLException, BadRecordSourceException {
        Map<RecordSource, List<Record>> recordMap = new LinkedHashMap<RecordSource, List<Record>>();
        Statement statement = null;
        try {
            statement = getConnection().createStatement();
            for (RecordSource recordSource : transactionMap.keySet()) {
                recordMap.put(recordSource, mine(recordSource, transactionMap.get(recordSource), statement));
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return recordMap;
    }

    private List<Record> mine(RecordSource recordSource, Map<Integer, Transaction> transactionMap, Statement statement)
            throws SQLException, BadRecordSourceException {
        List<Record> recordList = new ArrayList<Record>();
        if (!transactionMap.isEmpty()) {
            for (Transaction transaction : transactionMap.values()) {
                Record record = createRecord(transaction, recordSource, statement);
                recordList.add(record);
            }
        }
        return recordList;
    }

    public Record createRecord(Transaction transaction, RecordSource recordSource,
            Statement statement) throws SQLException {
        boolean createStatementHere = (statement == null);
        if (createStatementHere) {
            statement = getConnection().createStatement();
        }
        ResultSet resultSet = null;
        String query = "SELECT " + getColumnNamesAsString(recordSource) + "\n"
                + "FROM " + recordSource.getTableName() + "\n";
        int index = 0;
        for (Column column : transaction.getPrimaryKey().keySet()) {
            String primaryKeyValue = transaction.getPrimaryKey().get(column);
            primaryKeyValue = (column.isQuote() ? "'" + primaryKeyValue + "'" : primaryKeyValue);
            if (index == 0) {
                query += "WHERE " + column.getName() + " = " + primaryKeyValue + "\n";
            } else {
                query += "AND " + column.getName() + " = " + primaryKeyValue + "\n";
            }
            index++;
        }
        Record record = new Record();
        record.setPrimaryKeyCellMap(transaction.getPrimaryKey());
        record.setTransactionType(transaction.getType());
        try {
            resultSet = statement.executeQuery(query);
            Mediator.getLogger(RecordMiner.class.getName()).log(Level.FINER, query);
            Map<Column, String> columnMap;
            if (resultSet.next()) {
                columnMap = new LinkedHashMap<Column, String>();
                for (Column column : recordSource.getColumnList()) {
                    columnMap.put(column, resultSet.getString(column.getName()));
                }
                record.setOrdinaryCellMap(columnMap);
            } else {
                return null;
            }
        } finally {
            if (createStatementHere) {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } else {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
        return record;
    }

    public List<Record> mine(Record masterRecord, RecordSource masterRecordSource, RecordSource slaveRecordSource) throws SQLException {
        List<Record> slaveRecordList = new ArrayList<Record>();
        Statement statement = getConnection().createStatement();
        ResultSet resultSet = null;
        String query = "SELECT " + (slaveRecordSource.getLimit() > 0 ? "TOP " + slaveRecordSource.getLimit() + " " : "")
                + getColumnNamesAsString(slaveRecordSource) + "\n"
                + "FROM " + slaveRecordSource.getTableName() + "\n";
        int index = 0;
        for (Column column : masterRecord.getPrimaryKeyCellMap().keySet()) {
            String primaryKeyValue = masterRecord.getPrimaryKeyCellMap().get(column);
            primaryKeyValue = (column.isQuote() ? "'" + primaryKeyValue + "'" : primaryKeyValue);
            if (index == 0) {
                query += "WHERE " + column.getName() + " = " + primaryKeyValue + "\n";
            } else {
                query += "AND " + column.getName() + " = " + primaryKeyValue + "\n";
            }
            index++;
        }
        try {
            resultSet = statement.executeQuery(query);
            Mediator.getLogger(RecordMiner.class.getName()).log(Level.FINER, query);
            while (resultSet.next()) {
                Record record = new Record();
                record.setPrimaryKeyCellMap(masterRecord.getPrimaryKeyCellMap());
                record.setTransactionType(TransactionType.SELECT);
                Map<Column, String> columnMap;
                columnMap = new LinkedHashMap<Column, String>();
                for (Column column : slaveRecordSource.getColumnList()) {
                    columnMap.put(column, resultSet.getString(column.getName()));
                }
                record.setOrdinaryCellMap(columnMap);
                slaveRecordList.add(record);
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
        return slaveRecordList;
    }

    private String getColumnNamesAsString(RecordSource recordSource) {
        String columnNameString = "";
        int index = 0;
        int count = recordSource.getColumnList().size();
        for (Column column : recordSource.getColumnList()) {
            columnNameString += column.getName();
            if (index < (count - 1)) {
                columnNameString += ", ";
            }
            index++;
        }
        return columnNameString;
    }

    private Connection getConnection() throws SQLException {
        return ResourceManager.getDatabaseConnection("source");
    }
}
