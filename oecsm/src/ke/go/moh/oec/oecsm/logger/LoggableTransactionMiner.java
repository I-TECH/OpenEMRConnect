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
package ke.go.moh.oec.oecsm.logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ke.go.moh.oec.oecsm.data.LoggableTransaction;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.data.Cell;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.LoggableTransactionDatum;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.data.TransactionType;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @author JGitahi
 *
 * @date Sep 9, 2010 
 */
public class LoggableTransactionMiner extends DatabaseConnector {

    public List<LoggableTransaction> generate() throws InaccessibleConfigurationFileException, SQLException, DriverNotFoundException {
        List<LoggableTransaction> loggableTransactionList = new ArrayList<LoggableTransaction>();
        try {
            connectToShadow();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT `transaction`.`ID`, `transaction`.`TYPE`, `transaction`.`TABLE_ID` FROM `transaction` WHERE `transaction`.`ID` > " + getLastProcessedId() + ";");
            while (rs.next()) {
                LoggableTransaction loggableTransaction = new LoggableTransaction(rs.getInt("ID"), findTable(rs.getInt("TABLE_ID")), TransactionType.valueOf(rs.getString("TYPE")));
                loggableTransaction.setLoggableTransactionDatumList(generateLoggableTransactionDatumList(loggableTransaction));
                loggableTransactionList.add(loggableTransaction);
            }
            if (!loggableTransactionList.isEmpty()) {
                updateLastProcessedId(loggableTransactionList.get(loggableTransactionList.size() - 1).getId());
            }

        } finally {
            disconnectFromShadow();
        }
        return loggableTransactionList;
    }

    private List<LoggableTransactionDatum> generateLoggableTransactionDatumList(LoggableTransaction loggableTransaction) throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
        List<LoggableTransactionDatum> loggableTransactionDatumList = new ArrayList<LoggableTransactionDatum>();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT `transaction_data`.`ID`, `transaction_data`.`DATA`, `transaction_data`.`COLUMN_ID`, `transaction_data`.`TRANSACTION_ID` FROM `transaction_data` WHERE `transaction_data`.`TRANSACTION_ID` = " + loggableTransaction.getId() + ";");
        while (rs.next()) {
            Cell cell = new Cell(rs.getInt("ID"), rs.getString("DATA"));
            cell.setColumn(findColumn(rs.getInt("COLUMN_ID")));
            LoggableTransactionDatum loggableTransactionDatum = new LoggableTransactionDatum(cell, loggableTransaction);
            loggableTransactionDatumList.add(loggableTransactionDatum);
        }
        return loggableTransactionDatumList;
    }

    private Column findColumn(int id) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        Column column = null;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT `column`.`ID`, `column`.`NAME`, `column`.`ORDINAL_POSITION`, `column`.`DATA_TYPE`, `column`.`SIZE`, `column`.`REPLICABLE`, `column`.`TABLE_ID` FROM `column` WHERE `column`.`ID` = " + id + "");
        while (rs.next()) {
            column = new Column(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("ORDINAL_POSITION"), rs.getString("DATA_TYPE"), rs.getInt("SIZE"), rs.getBoolean("REPLICABLE"));
            column.setTable(findTable(rs.getInt("TABLE_ID")));
        }
        rs.close();
        statement.close();
        return column;
    }

    private Table findTable(int id) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        Table table = null;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT `table`.`ID`, `table`.`NAME`, `table`.`PRIMARY_KEYS`, `table`.`DATABASE_ID` FROM `table` WHERE `table`.`ID` = " + id + "");
        if (rs.next()) {
            table = new Table(rs.getInt("ID"), rs.getString("NAME"), rs.getString("PRIMARY_KEYS"));
            table.setDatabase(findDatabase(rs.getInt("DATABASE_ID")));
        }
        rs.close();
        statement.close();
        return table;
    }

    private Database findDatabase(int id) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        Database db = null;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT `database`.`ID`, `database`.`NAME` FROM `database` WHERE `database`.`ID` = " + id + "");
        if (rs.next()) {
            db = new Database(rs.getInt("ID"), rs.getString("NAME"));
        }
        rs.close();
        statement.close();
        return db;
    }

    private int getLastProcessedId() throws SQLException {
        int lastId = 0;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT `destination`.`LAST_RECEIVED_TRANSACTION_ID` FROM `destination` WHERE `destination`.`ID` = 1");
        if (rs.next()) {
            lastId = rs.getInt("LAST_RECEIVED_TRANSACTION_ID");
        }
        rs.close();
        statement.close();
        return lastId;
    }

    private int updateLastProcessedId(int newLastId) throws SQLException {
        int lastId = 0;
        Statement statement = connection.createStatement();
        statement.executeUpdate("UPDATE `destination` SET `destination`.`LAST_RECEIVED_TRANSACTION_ID` = " + newLastId + " WHERE `destination`.`ID` = 1");
        statement.close();
        return lastId;
    }
}
