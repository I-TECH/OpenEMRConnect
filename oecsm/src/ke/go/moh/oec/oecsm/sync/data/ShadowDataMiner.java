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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.oecsm.data.Cell;
import ke.go.moh.oec.oecsm.data.Column;
import ke.go.moh.oec.oecsm.data.Database;
import ke.go.moh.oec.oecsm.data.Table;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class ShadowDataMiner extends DatabaseConnector {

    Statement statement;

    public void start() throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        connectToShadow();
        statement = connection.createStatement();
    }

    public ShadowResultSet mine(Table table) throws InaccessibleConfigurationFileException, SQLException, DriverNotFoundException {
        ShadowResultSet srs = null;
        try {
            String compositePK = DatabaseConnector.getQueryCustomizer().buildCompositePrimaryKey(table);
            String sql = "SELECT `cell`.`PRIMARY_KEY_VALUE`, `cell`.`DATA`, `cell`.`COLUMN_ID` FROM `cell` WHERE `COLUMN_ID` IN (";
            for (Column c : table.getColumnList()) {
                sql += c.getId() + ", "; // Add anyway, but the final one will be stripped.
            }
            sql = sql.substring(0,sql.length()-2); // Strip the final ", ".
            sql += ") ORDER BY `cell`.`PRIMARY_KEY_VALUE`";
            ResultSet rs = statement.executeQuery(sql);
            srs = new ShadowResultSet(rs);
        } finally {
        }
        return srs;
    }

    public void finish() throws SQLException {
        statement.close();
        disconnect();
    }

//    public List<Cell> mine() throws InaccessibleConfigurationFileException, SQLException, DriverNotFoundException {
//        List<Cell> cellList = new ArrayList<Cell>();
//        try {
//            connectToShadow();
//            Statement statement = connection.createStatement();
//            ResultSet rs = statement.executeQuery("SELECT `cell`.`ID`, `cell`.`PRIMARY_KEY_VALUE`, `cell`.`DATA`, `cell`.`COLUMN_ID` FROM `cell`");
//            while (rs.next()) {
//                Cell cell = new Cell(rs.getInt("ID"), rs.getString("PRIMARY_KEY_VALUE"), rs.getString("DATA"));
//                cell.setColumn(findColumn(rs.getInt("COLUMN_ID")));
//                cellList.add(cell);
//            }
//            rs.close();
//            statement.close();
//        } finally {
//            disconnect();
//        }
//        return cellList;
//    }
//
//    private Column findColumn(int id) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
//        Column column = null;
//        Statement statement = connection.createStatement();
//        ResultSet rs = statement.executeQuery("SELECT `column`.`ID`, `column`.`NAME`, `column`.`ORDINAL_POSITION`, `column`.`DATA_TYPE`, `column`.`SIZE`, `column`.`REPLICABLE`, `column`.`TABLE_ID` FROM `column` WHERE `column`.`ID` = " + id + "");
//        while (rs.next()) {
//            column = new Column(rs.getInt("ID"), rs.getString("NAME"), rs.getInt("ORDINAL_POSITION"), rs.getString("DATA_TYPE"), rs.getInt("SIZE"), rs.getBoolean("REPLICABLE"));
//            column.setTable(findTable(rs.getInt("TABLE_ID")));
//        }
//        rs.close();
//        statement.close();
//        return column;
//    }
//
//    private Table findTable(int id) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
//        Table table = null;
//        Statement statement = connection.createStatement();
//        ResultSet rs = statement.executeQuery("SELECT `table`.`ID`, `table`.`NAME`, `table`.`PRIMARY_KEYS`, `table`.`DATABASE_ID` FROM `table` WHERE `table`.`ID` = " + id + "");
//        if (rs.next()) {
//            table = new Table(rs.getInt("ID"), rs.getString("NAME"), rs.getString("PRIMARY_KEYS"));
//            table.setDatabase(findDatabase(rs.getInt("DATABASE_ID")));
//        }
//        rs.close();
//        statement.close();
//        return table;
//    }
//
//    private Database findDatabase(int id) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
//        Database db = null;
//        Statement statement = connection.createStatement();
//        ResultSet rs = statement.executeQuery("SELECT `database`.`ID`, `database`.`NAME` FROM `database` WHERE `database`.`ID` = " + id + "");
//        if (rs.next()) {
//            db = new Database(rs.getInt("ID"), rs.getString("NAME"));
//        }
//        rs.close();
//        statement.close();
//        return db;
//    }
}
