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
package ke.go.moh.oec.reception.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.data.Department;
import ke.go.moh.oec.reception.data.User;
import ke.go.moh.oec.reception.controller.exceptions.ExistingUserException;
import ke.go.moh.oec.reception.controller.exceptions.PersistenceManagerException;
import ke.go.moh.oec.reception.controller.exceptions.TableCreationException;

/**
 * This class manages all local persistence for the application. Specifically,
 * it allows for the access and manipulation of user information (usernames and
 * passwords) and also departments within a given facility. It is a singleton class
 * and therefore all consumers must request for an instance of it by calling the
 * getInstance() method.
 * 
 * @author Gitahi Ng'ang'a
 */
public final class PersistenceManager {

    private static PersistenceManager instance;
    private Connection connection;
    private final String database = "reception_database";
    private final String userTable = "reception_user";
    private final String departmentTable = "department";
    private final String username = "reception";
    private final String password = "r2e8c0e6p9t4i3o9n";
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String protocol = "jdbc:derby";

    private PersistenceManager() throws Exception {
        registerDriver();
        connect();
    }

    /**
     * Returns an instance of PersistenceManager if any exists. If none does,
     * a new one is returned and then stored to be returned during future calls.
     */
    public static PersistenceManager getInstance() throws PersistenceManagerException {
        if (instance == null) {
            try {
                instance = new PersistenceManager();
            } catch (Exception ex) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new PersistenceManagerException();
            }
        }
        return instance;
    }

    /**
     * Authenticates a user against the local database of users
     * 
     * @throws PersistenceManagerException if the database cannot be accessed for any reason
     * 
     * @returns true if the user has been successfully authenticated and false otherwise
     */
    public boolean authenticateUser(User user) throws PersistenceManagerException {
        boolean authentic = false;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connect();
            statement = getStatement();
            resultSet = statement.executeQuery("SELECT username, password, admin FROM "
                    + userTable + " WHERE username = '" + user.getUsername() + "' AND password = '"
                    + new String(user.getPassword()) + "'");
            if (resultSet.next()) {
                user.setAdmin(resultSet.getBoolean("admin"));
                authentic = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException();
        } finally {
            this.close(statement);
            this.close(resultSet);
        }
        log("User '" + user.getUsername() + "' attempted to log into reception with password '******' [success = '" + authentic + "'].");
        return authentic;
    }

    public void createUser(User user) throws PersistenceManagerException, ExistingUserException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("INSERT INTO " + userTable + "(username, password, admin) "
                    + "VALUES ('" + user.getUsername() + "', '" + new String(user.getPassword()) + "', "
                    + (user.isAdmin() ? 1 : 0) + ")");
            log("User '" + user.getUsername() + "' created with password = '******' and admin = '" + user.isAdmin()+ "'");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 30000) {
                throw new ExistingUserException();
            } else {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new PersistenceManagerException();
            }
        } finally {
            this.close(statement);
        }
    }

    public void modifyUser(User user) throws PersistenceManagerException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("UPDATE " + userTable + " SET password = '" + new String(user.getPassword())
                    + "', admin = " + (user.isAdmin() ? 1 : 0) + " WHERE username = '" + user.getUsername() + "'");
            log("User '" + user.getUsername() + "' modified with password = '******' and admin = '" + user.isAdmin()+ "'");
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException();
        } finally {
            this.close(statement);
        }
    }

    public void deleteUser(User user) throws PersistenceManagerException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("DELETE FROM " + userTable + " WHERE username = '" + user.getUsername() + "'");
           log("User '" + user.getUsername() + "' deleted with password = '******' and admin = '" + user.isAdmin()+ "'");
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException();
        } finally {
            this.close(statement);
        }
    }

    public List<User> getUserList() {
        List<User> userList = new ArrayList<User>();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = this.getStatement();
            resultSet = statement.executeQuery("SELECT username, password, admin FROM " + userTable);
            while (resultSet.next()) {
                userList.add(new User(resultSet.getString("username"), resultSet.getString("password").toCharArray(),
                        resultSet.getBoolean("admin")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.close(statement);
            this.close(resultSet);
        }
        return userList;
    }

    public void createDepartment(Department department) throws PersistenceManagerException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("INSERT INTO " + departmentTable + "(name, code) VALUES ('"
                    + department.getName() + "', '" + department.getCode() + "')");
            log("Department '" + department.getName() + "' created with code = '" + department.getCode()+ "'");
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException();
        } finally {
            this.close(statement);
        }
    }

    public void modifyDepartment(Department department) throws PersistenceManagerException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("UPDATE " + departmentTable + " SET code = '"
                    + department.getCode() + "' WHERE name = '" + department.getName() + "'");
            log("Department '" + department.getName() + "' modified with code = '" + department.getCode()+ "'");
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException();
        } finally {
            this.close(statement);
        }
    }

    public void deleteDepartment(Department department) throws PersistenceManagerException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("DELETE FROM " + departmentTable + " WHERE name = '" + department.getName() + "'");
            log("Department '" + department.getName() + "' deleted with code = '" + department.getCode()+ "'");
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException();
        } finally {
            this.close(statement);
        }
    }

    public List<Department> getDepartmentList() {
        List<Department> departmentList = new ArrayList<Department>();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = this.getStatement();
            resultSet = statement.executeQuery("SELECT name, code FROM " + departmentTable);
            while (resultSet.next()) {
                departmentList.add(new Department(resultSet.getString("name"), resultSet.getString("code")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.close(statement);
            this.close(resultSet);
        }
        return departmentList;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private void registerDriver() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        try {
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (InstantiationException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void connect() throws SQLException {
        try {
            connection = DriverManager.getConnection(protocol + ":" + Mediator.getRuntimeDirectory() + database + ";create=true;"
                    + "user=" + username + ";password=" + password + "");
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public void createUserTable() throws TableCreationException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.execute("CREATE TABLE " + userTable + "(username VARCHAR(50) NOT NULL,"
                    + "password VARCHAR(50) NOT NULL, admin SMALLINT, PRIMARY KEY(username))");
        } catch (SQLException ex) {
            if (!ex.getSQLState().equalsIgnoreCase("X0Y32")) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new TableCreationException(ex.getMessage());
            }
        } finally {
            this.close(statement);
        }
    }

    public void createDepartmentTable() throws TableCreationException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.execute("CREATE TABLE " + departmentTable + "(name VARCHAR(50) NOT NULL,"
                    + "code VARCHAR(50) NOT NULL, PRIMARY KEY(name))");
        } catch (SQLException ex) {
            if (!ex.getSQLState().equalsIgnoreCase("X0Y32")) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new TableCreationException(ex.getMessage());
            }
        } finally {
            this.close(statement);
        }
    }

    public void createDefaultUser() throws PersistenceManagerException {
        if (noUsersExist()) {
            try {
                char[] defaultPassword = {'a', 'd', 'm', 'i', 'n'};
                createUser(new User("admin", defaultPassword, true));
            } catch (ExistingUserException ex) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.INFO, null,
                        "Tried to create a default user when one already existed. " + ex);
            }
        }
    }

    private boolean noUsersExist() throws PersistenceManagerException {
        boolean exist = true;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = this.getStatement();
            resultSet = statement.executeQuery("SELECT username FROM " + userTable);
            if (resultSet.next()) {
                exist = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new PersistenceManagerException(ex.getMessage());
        }
        return exist;
    }

    private Statement getStatement() throws SQLException {
        if (connection == null) {
            connect();
        }
        return connection.createStatement();
    }

    private void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();


            } catch (SQLException ex) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();


            } catch (SQLException ex) {
                Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void log(String message) {
        Mediator.getLogger(PersistenceManager.class.getName()).log(Level.INFO, "{0} [Time = {1}, User = {2}]", new Object[]{message, new Date().toString(), 
            OECReception.getUser() != null ? OECReception.getUser() : "None"});
    }
}
