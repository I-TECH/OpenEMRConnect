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

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import ke.go.moh.oec.adt.data.Column;
import ke.go.moh.oec.adt.data.RecordSource;
import ke.go.moh.oec.adt.data.Transaction;
import ke.go.moh.oec.adt.data.TransactionType;
import ke.go.moh.oec.adt.exceptions.BadRecordSourceException;

/**
 * @date Apr 25, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class TransactionMiner {

    private int lastTransactionId = -1;

    public Map<RecordSource, Map<Integer, Transaction>> mine(List<RecordSource> recordSourceList, Date since) throws SQLException,
            BadRecordSourceException {
        Map<RecordSource, Map<Integer, Transaction>> transactionMap =
                new LinkedHashMap<RecordSource, Map<Integer, Transaction>>();
        for (RecordSource recordSource : recordSourceList) {
            validatePrimaryKeys(recordSource);
            transactionMap.put(recordSource, mine(recordSource, since));
        }
        return transactionMap;
    }

    public void saveLastTransactionId() throws SQLException {
        if (lastTransactionId != -1) {
            Statement statement = null;
            String query;
            int lastTxId = retrieveLastTransactionId();
            if (lastTxId == -1) {
                query = "INSERT INTO `destination`(`name`, `last_received_transaction_id`, `last_processed_transaction_id`)\n"
                        + "VALUES('" + ResourceManager.getSetting("name") + "', " + lastTransactionId + ", " + lastTransactionId + ")";
            } else {
                query = "UPDATE `destination`\n"
                        + "SET `last_received_transaction_id` = " + lastTransactionId
                        + ", `last_processed_transaction_id` = " + lastTransactionId + "\n"
                        + "WHERE `name` = '" + ResourceManager.getSetting("name") + "'";
            }
            try {
                statement = getConnection().createStatement();
                statement.executeUpdate(query);
            } catch (SQLException ex) {
                throw ex;
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
        }
    }

    private Map<Integer, Transaction> mine(RecordSource recordSource, Date since) throws SQLException {
        Map<Integer, Transaction> transactionMap = new LinkedHashMap<Integer, Transaction>();
        Statement statement = null;
        ResultSet resultSet = null;
        String query = "SELECT t.`id`, t.`type`, b.`name` AS `table_name`, b.`primary_keys`\n"
                + "FROM `transaction` t\n"
                + "JOIN `table` b ON b.`id` = t.`table_id`\n"
                + "WHERE (`type` = 'INSERT' OR `type` = 'UPDATE')\n"
                + "AND b.`name` = '" + recordSource.getTableName() + "'\n"
                + "AND t.`id` > " + retrieveLastTransactionId() + "\n";
        if (since != null) {
            query += "AND created_datetime > '" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(since) + "'\n";
        }
        query += "ORDER BY t.`id` DESC\n";
        if (recordSource.getLimit() >= 0) {
            query += "LIMIT " + recordSource.getLimit();
        }
        try {
            statement = getConnection().createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                Transaction transaction = new Transaction(resultSet.getInt("id"), resultSet.getString("table_name"),
                        TransactionType.valueOf(resultSet.getString("type")));
                transactionMap.put(transaction.getId(), transaction);
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
        setPrimaryKeyMaps(recordSource, transactionMap);
        return coalesceSameRecordTransactions(transactionMap);
    }

private void setPrimaryKeyMaps(RecordSource recordSource, Map<Integer, Transaction> transactionMap) throws SQLException {
        if (transactionMap != null && !transactionMap.isEmpty()) {
            List<Integer> keyList = new ArrayList<Integer>(transactionMap.keySet());
            int firstId = keyList.get(keyList.size() - 1);
            int lastId = keyList.get(0);
            lastTransactionId = (lastTransactionId > lastId ? lastTransactionId : lastId);
            Statement statement = null;
            ResultSet resultSet = null;
            String query = "SELECT c.`name` AS `column_name`, t.`data` AS `column_value`, t.`transaction_id`, t.`transaction_id`, b.`name`\n"
                    + "FROM `transaction_data` t\n"
                    + "JOIN `column` c ON c.`id` = t.`column_id`\n"
                    + "JOIN `table` b ON b.`id` = c.`table_id`\n"
                    + "WHERE t.`transaction_id`\n"
                    + "BETWEEN " + firstId + " AND " + lastId + "\n"
                    + "AND c.`name`IN (" + concatenatePkColumns(recordSource.getPrimaryKeyColumnMap().keySet()) + ")\n"
                    + "AND b.`name` = '" + recordSource.getTableName() + "'\n"
                    + "ORDER BY t.`transaction_id`";
            try {
                statement = getConnection().createStatement();
                resultSet = statement.executeQuery(query);
                Transaction transaction = null;
                if (resultSet.next()) {
                    int transactionId = resultSet.getInt("transaction_id");
                    String primaryKeyColumnName = resultSet.getString("column_name");
                    String primaryKeyColumnValue = resultSet.getString("column_value");
                    while (resultSet.next()) {
                        int txId = resultSet.getInt("transaction_id");
                        if (txId != transactionId) {//211691//185730
                            transaction = transactionMap.get(transactionId);
                            if (transaction == null) {
                                transaction = transactionMap.get(txId);
                            }
                            addPrimaryKey(transaction, recordSource, primaryKeyColumnName, primaryKeyColumnValue);
                            primaryKeyColumnName = resultSet.getString("column_name");
                            primaryKeyColumnValue = resultSet.getString("column_value");
                            transactionId = txId;
                        } else {
                            primaryKeyColumnName = resultSet.getString("column_name");
                            primaryKeyColumnValue = resultSet.getString("column_value");
                            addPrimaryKey(transaction, recordSource, primaryKeyColumnName, primaryKeyColumnValue);
                        }
                    }
                    transaction = transactionMap.get(transactionId);
                    addPrimaryKey(transaction, recordSource, primaryKeyColumnName, primaryKeyColumnValue);
                }
            } finally {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
    }

    private void addPrimaryKey(Transaction transaction, RecordSource recordSource, String primaryKeyColumnName, String primaryKeyColumnValue) {
        Column column = recordSource.getPrimaryKeyColumnMap().get(primaryKeyColumnName);
        if (column != null) {
            if (transaction.getPrimaryKey() == null) {
                transaction.setPrimaryKey(new LinkedHashMap<Column, String>());
            }
            transaction.getPrimaryKey().put(column, primaryKeyColumnValue);
        }
    }

    private Map<Integer, Transaction> coalesceSameRecordTransactions(Map<Integer, Transaction> txMap) {
        List<String> compositePrimaryKeyList = new ArrayList<String>();
        Map<Integer, Transaction> transactionMap = new LinkedHashMap<Integer, Transaction>();
        for (Integer id : txMap.keySet()) {
            String compositePrimaryKey = createCompositePrimaryKey(txMap.get(id));
            if (!compositePrimaryKeyList.contains(compositePrimaryKey)) {
                compositePrimaryKeyList.add(compositePrimaryKey);
                transactionMap.put(id, txMap.get(id));
            }
        }
        return transactionMap;
    }

    private String createCompositePrimaryKey(Transaction transaction) {
        String compositePrimaryKey = "";
        for (Column column : transaction.getPrimaryKey().keySet()) {
            compositePrimaryKey += transaction.getPrimaryKey().get(column) + "#";
        }
        compositePrimaryKey += transaction.getTableName();
        return compositePrimaryKey;
    }

    private int retrieveLastTransactionId() throws SQLException {
        int lastTxId = -1;
        Statement statement = null;
        ResultSet resultSet = null;
        String query = "SELECT `last_processed_transaction_id`\n"
                + "FROM `destination`\n"
                + "WHERE `name` = '" + ResourceManager.getSetting("name") + "'";
        try {
            statement = getConnection().createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                lastTxId = resultSet.getInt("last_processed_transaction_id");
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return lastTxId;
    }

    private void validatePrimaryKeys(RecordSource recordSource) throws SQLException, BadRecordSourceException {
        Statement statement = null;
        ResultSet resultSet = null;
        String query = "SELECT t.`primary_keys`\n"
                + "FROM `column` c, `table` t\n"
                + "WHERE t.`name` = '" + recordSource.getTableName() + "'\n"
                + "LIMIT 1";
        try {
            statement = getConnection().createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                List<String> truePrimaryKeyList = Arrays.asList(resultSet.getString("primary_keys").split(","));
                int configuredPkCount = recordSource.getPrimaryKeyColumnMap().size();
                int truePkCount = truePrimaryKeyList.size();
                if (configuredPkCount != truePkCount) {
                    throw new BadRecordSourceException("The number of configured primary key columns (" + configuredPkCount
                            + ") does not match the number of true primary key columns (" + truePkCount + ")");
                }
                for (Column column : recordSource.getPrimaryKeyColumnMap().values()) {
                    if (!truePrimaryKeyList.contains(column.getName())) {
                        throw new BadRecordSourceException("The column " + column.getName()
                                + " is configured as a primary key for the table " + recordSource.getTableName()
                                + " but it is not actually a primary key.");
                    }
                }
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private String concatenatePkColumns(Collection<String> pkColumnNameCollection) {
        String concatenatedPkColumns = "";
        int index = 0;
        int count = pkColumnNameCollection.size();
        for (String pkColumnName : pkColumnNameCollection) {
            concatenatedPkColumns += "'" + pkColumnName + "'";
            if (index < (count - 1)) {
                concatenatedPkColumns += ", ";
            }
            index++;
        }
        return concatenatedPkColumns;
    }

    /*
     * Returns a usable database connection based on the settings specified in
     * the properties file.
     */
    private Connection getConnection() throws SQLException {
        return ResourceManager.getDatabaseConnection("shadow");
    }
}
