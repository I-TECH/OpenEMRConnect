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
package ke.go.moh.oec.reception.security;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.reception.security.exceptions.ExistingUserException;
import ke.go.moh.oec.reception.security.exceptions.UserManagerCreationException;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public final class UserManager {

    private static UserManager instance;
    private Connection connection;
    private final String database = "reception_database";
    private final String userTable = "reception_user";
    private final String username = "reception";
    private final String password = "r2e8c0e6p9t4i3o9n";
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String protocol = "jdbc:derby";

    private UserManager() throws Exception {
        registerDriver();
        connect();
        createUserTable();
        createDefaultUser();
    }

    public static UserManager getInstance() throws UserManagerCreationException {
        if (instance == null) {
            try {
                instance = new UserManager();
            } catch (Exception ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new UserManagerCreationException();
            }
        }
        return instance;
    }

    public boolean authenticateUser(User user) throws UserManagerCreationException {
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
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new UserManagerCreationException();
        } finally {
            this.close(statement);
            this.close(resultSet);
        }
        return authentic;
    }

    public void createUser(User user) throws UserManagerCreationException, ExistingUserException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("INSERT INTO " + userTable + "(username, password, admin) "
                    + "VALUES ('" + user.getUsername() + "', '" + new String(user.getPassword()) + "', "
                    + (user.isAdmin() ? 1 : 0) + ")");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 30000) {
                throw new ExistingUserException();
            } else {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
                throw new UserManagerCreationException();
            }
        } finally {
            this.close(statement);
        }
    }

    public void modifyUser(User user) throws UserManagerCreationException {
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeUpdate("UPDATE " + userTable + " SET password = '" + new String(user.getPassword())
                    + "', admin = " + (user.isAdmin() ? 1 : 0) + " WHERE username = '" + user.getUsername() + "'");
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new UserManagerCreationException();
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
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.close(statement);
            this.close(resultSet);
        }
        return userList;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (InstantiationException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void connect() throws SQLException {
        try {
            connection = DriverManager.getConnection(protocol + ":" + database + ";create=true;"
                    + "user=" + username + ";password=" + password + "");
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void createUserTable() throws SQLException {
        Statement statement = null;
        try {
            if (!userTableExists()) {
                statement = this.getStatement();
                statement.execute("CREATE TABLE " + userTable + "(username VARCHAR(50) NOT NULL,"
                        + "password VARCHAR(50) NOT NULL, admin SMALLINT, PRIMARY KEY(username))");
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            this.close(statement);
        }
    }

    private boolean userTableExists() {
        boolean exists = true;
        Statement statement = null;
        try {
            statement = this.getStatement();
            statement.executeQuery("SELECT username FROM " + userTable);
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("42Y07")) {
                exists = false;
            } else {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            this.close(statement);
        }
        return exists;
    }

    private void createDefaultUser() throws SQLException, UserManagerCreationException, ExistingUserException {
        if (!noUsersExist()) {
            char[] defaultPassword = {'a', 'd', 'm', 'i', 'n'};
            createUser(new User("admin", defaultPassword, true));
        }
    }

    private boolean noUsersExist() throws SQLException {
        boolean exist = false;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = this.getStatement();
            resultSet = statement.executeQuery("SELECT username FROM " + userTable);
            if (resultSet.next()) {
                exist = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
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
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();


            } catch (SQLException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
