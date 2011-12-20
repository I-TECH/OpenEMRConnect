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

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.oecsm.bridge.querycustomizers.QueryCustomizer;
import ke.go.moh.oec.oecsm.bridge.querycustomizers.MSAccessQueryCustomizer;
import ke.go.moh.oec.oecsm.bridge.querycustomizers.MSSQQueryCustomizer;
import ke.go.moh.oec.oecsm.bridge.querycustomizers.MySQLQueryCustomizer;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;

/**
 * @date Aug 17, 2010
 *
 * @author JGitahi
 */
public class DatabaseConnector {

    protected String database;
    protected String url;
    protected String driver;
    protected String username;
    protected String password;
    protected Connection connection;
    protected String schemaPattern;
    protected String tableTypes;
    private List<Connection> sourceConnectionPool = new ArrayList<Connection>();
    private List<Connection> shadowConnectionPool = new ArrayList<Connection>();

    public void testConnection(String driver, String url, String username, String password) throws DriverNotFoundException, SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new DriverNotFoundException(ex);
        }
        connection = DriverManager.getConnection(url, username, password);
    }

    protected synchronized void connectToSource() throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        if (!sourceConnectionPool.isEmpty()) {
            connection = sourceConnectionPool.get(0);
            sourceConnectionPool.remove(0);
        } else {
            connectToDatabase("source_database.properties");
        }
    }

    protected synchronized void connectToShadow() throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        if (!shadowConnectionPool.isEmpty()) {
            connection = shadowConnectionPool.get(0);
            shadowConnectionPool.remove(0);
        } else {
            connectToDatabase("shadow_database.properties");
        }
    }

    private synchronized void connectToDatabase(String databasePropertiesFile) throws SQLException, InaccessibleConfigurationFileException, DriverNotFoundException {
        loadConnectionProperties(databasePropertiesFile);
        loadDriver();
        connection = DriverManager.getConnection(url, username, password);
    }

    private void loadConnectionProperties(String propertiesFile) throws InaccessibleConfigurationFileException {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            database = properties.getProperty("database");
            url = properties.getProperty("url");
            driver = properties.getProperty("driver");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            schemaPattern = properties.getProperty("schemaPattern");
            tableTypes = properties.getProperty("tableTypes");
            if (tableTypes == null) {
                tableTypes = "TABLE"; // Default: get all tables in the source database.
            }
        } catch (IOException ex) {
            Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            throw new InaccessibleConfigurationFileException(ex);
        }

    }

    private void loadDriver() throws DriverNotFoundException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
            throw new DriverNotFoundException(ex);
        }
    }

    protected synchronized void disconnectFromSource() throws SQLException {
        sourceConnectionPool.add(connection);
    }

    protected synchronized void disconnectFromShadow() throws SQLException {
        shadowConnectionPool.add(connection);
    }

    public QueryCustomizer getQueryCustomizer() {
        if (url.contains("mysql")) {
            return new MySQLQueryCustomizer();
        } else if (url.contains("sqlserver")) {
            return new MSSQQueryCustomizer();
        } else if (url.contains("postgresql")) {
            return new MSSQQueryCustomizer();
        } else if (url.contains("Access")) {
            return new MSAccessQueryCustomizer();
        } else {
            return null;
        }
    }
}
