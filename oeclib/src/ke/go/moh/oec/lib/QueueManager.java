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

/*   This file has been worked on by Scott Davis.
 * 
 * Some parts of code have been temporarily removed to allow for testing.
 * These lines of code will be indicated with the tag RESTORE_THIS_CODE.
 * Remove the comments to restore the code and reincorporate this file back
 * into the main project.
 */
package ke.go.moh.oec.lib;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashSet;

/**
 * Offers a store-and-forward queueing facility to send messages to a
 * network destination. If the message cannot be sent immediately, it is
 * queued for later sending. The Queue Manager periodically tries to
 * send queued messages to their respective destinations.
 *
 * @author Jim Grace
 */
class QueueManager implements Runnable {

    /* HTTP Handler object we can call to send HTTP messages. */
    private HttpService httpService;

    /* Indication whether we have started a polling thread. */
    private boolean pollingThreadStarted = false;

    /* Indication if it is time to shut down -- stop() has been called. */
    private boolean timeToShutDown = false;
    /* Disable Queue Manager functions (just pass through -- used for debugging) */
    private boolean queueManagerDisabled = false;

    /* Interval at which to retry sending queued messages. */
    private int pollingInterval;
    //----------------------------------------------------------
    //       DATABASE CONNECTION VARIABLES
    //  Includes Log-on information to connect to the database. 
    //----------------------------------------------------------
    /* The Connection to the database used to perform database operations. */
    private Connection dataBaseConnection;
    /*
     * The connection protocol String to establish a database connection.
     *
     * In embedded mode(local connection), the protocol derby default is 
     * "jdbc:derby:" plus the name of the data base.
     * 
     * In client/server mode, the derby default is: 
     * "jdbc:derby://localhost:" + PORT_NUMBER + "/".
     */
    private String PROTOCOL = "jdbc:derby:";
    /*
     * The name of the java database that will be created.  It can be found 
     * in a folder with the database name in the "derby.system.home" directory.
     */
    private String DATABASE_NAME = "QUEUEMANAGER_DATABASE";
    /* The name of the JavaDB table. */
    private final String TABLE_NAME = "MESSAGE_SENDING_QUEUE";

    /**
     * Constructor to set <code>HttpService</code> object for sending messages.</p>
     * 
     * <p> It sets the link to the HttpSetvice object. Then it establishes a 
     * Connection to a JavaDB Data Base. The Connection can be embedded or client.
     * If the Connection is created successfully, it creates a table and initializes
     * the connectionDownList.</p>
     *
     * @param httpService <code>HttpService</code> object for sending messages
     */
    QueueManager(HttpService httpService) {
        this.httpService = httpService;

        // set polling interval
        String queueManagerPollingInterval = Mediator.getProperty("QueueManager.PollingInterval");
        if (queueManagerPollingInterval != null) {
            pollingInterval = Integer.parseInt(queueManagerPollingInterval);
        } else {
            pollingInterval = 10 * 60 * 1000; // Default polling interval of 10 minutes.
        }

        String disable = Mediator.getProperty("QueueManager.Disable");
        if (disable != null && disable.trim().compareToIgnoreCase("true") == 0) {
            queueManagerDisabled = true;
        } else {
            //link the Connection to the database
            dataBaseConnection = establishDataBaseConnection();

            if (dataBaseConnection != null) {   //create the MESSAGE_SENDING_QUEUE table
                createTable();
            }
        }
    }

    /**
     * Starts a polling thread if needed.
     * <p>
     * If the queue is empty, and a polling thread is not already running,
     * then we start a polling thread.
     */
    synchronized void start() {
        if (!queueManagerDisabled && !isEmpty() && !timeToShutDown) {
            if (pollingThreadStarted) {
                this.notify();
            } else {
                Thread t = new Thread(this);
                //
                // Programming note: set pollingThreadStarted to true before
                // we actually start the thread. That way if the thread exits
                // really quickly (setting pollingThreadStarted to false)
                // we won't set it back to true after it quits.
                //
                pollingThreadStarted = true;
                t.start();
            }
        }
    }

    /**
     * Stops the queue manager (call after last use).
     */
    synchronized void stop() {
        timeToShutDown = true;
        this.notify(); // Notify polling thread (if any) to wake up and shut down.
    }

    /**
     * Queues a message for sending. The message will be immediately added to
     * the queue on disk for safe-keeping, and then this method returns.
     * If there is network connectivity to the IP address, it will then be
     * sent immediately. If it fails to send, it will be periodically retried
     * until it succeeds.
     *
     * @param m Message to queue for sending
     */
    public boolean enqueue(Message m) {
        boolean messageAdded = false;

        if (queueManagerDisabled) {
            try {
                messageAdded = httpService.send(m); // (toBeQueued = false)
            } catch (MalformedURLException ex) {
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            //prepare SQL Statement to perform the insertion
            String querySQL = "INSERT INTO " + TABLE_NAME
                    + "( DESTINATION, XML_CODE, HOP_COUNT ) VALUES ( "
                    + quote(m.getDestinationAddress()) + ", "
                    + "?, "
                    + m.getHopCount() + ")";
            Logger.getLogger(QueueManager.class.getName()).log(Level.FINER, querySQL);
            try {
                PreparedStatement stmt = dataBaseConnection.prepareStatement(querySQL);
                stmt.setObject(1, m.getCompressedXml());
                stmt.executeUpdate();
                stmt.close();
                messageAdded = true;
                // Log every incoming message (level FINE -- only one incoming log entry for each message.)
                Mediator.getLogger(QueueManager.class.getName()).log(Level.FINE,
                        "Queued message to {0}", m.getDestinationAddress());
            } catch (SQLException ex) {
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return messageAdded;
    }

    /**
     * Tries to send all the Messages in the MESSAGE_SENDING_QUEUE.
     * If a send call is successful, then it removes the message from the
     * table.  Otherwise, it puts the destination in a connectionDownList
     * so that it will not try to send Messages to a link that is down.
     * Message destinations on the connectionDownList are removed after
     * a waiting period so they can be retried.
     */
    public void run() {
        String selectAllQuerySQL = "SELECT * FROM " + TABLE_NAME
                + " ORDER BY MESSAGE_ID";
        try {
            Statement stmt = dataBaseConnection.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectAllQuerySQL);
            /* A HashSet that holds the destinations of the Messages that are not sent
             * successfully.  Any Message with a destination on this list will
             * not try to be sent because it is assumed that the connection is down.
             * The list is cleared after a specified period of time. */
            HashSet connectionDownList = new HashSet();

            //try to send each entry in that table
            while (resultSet.next()) {
                //Check to see if it is on the connectionDownList
                String destination = resultSet.getString("DESTINATION");
                int messageId = resultSet.getInt("MESSAGE_ID");
                if (connectionDownList.contains(destination)) {
                    // It is on the connection down list, so do not try to send it.
                    // Log this (level FINEST -- possibly many such logs for each message.)
                    Mediator.getLogger(QueueManager.class.getName()).log(Level.FINEST,
                            "Skipping message ID {0} to {1}",
                            new Object[]{messageId, destination});
                } else {
                    Message m = new Message();
                    m.setDestinationAddress(destination);
                    m.setCompressedXml(resultSet.getBytes("XML_CODE"));
                    m.setHopCount(resultSet.getInt("HOP_COUNT"));
                    //initialize a boolean to hold the result of the send
                    boolean sent = false;
                    try {
                        sent = httpService.send(m);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (sent) {
                        // Log every outgoing message (level FINE -- only one outgoing log entry for each message.)
                        Mediator.getLogger(QueueManager.class.getName()).log(Level.FINE,
                                "Sent message ID {0} to {1}",
                                new Object[]{messageId, destination});
                        //it was sent correctly, remove it from the list
                        delete(messageId);
                    } else {
                        // Log failed attempt (level FINEST -- possibly many such logs for each message.)
                        Mediator.getLogger(QueueManager.class.getName()).log(Level.FINEST,
                                "Failed to send message ID {0} to {1}",
                                new Object[]{messageId, destination});
                        /* Put this destination on the connectionDownList so
                         * resources are not wasted trying to send on a
                         * connection that is down */
                        connectionDownList.add(resultSet.getString("DESTINATION"));
                    }
                }
            }

            //close the SQL ResultSet and Statement
            resultSet.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException ex) {
            }
        }
        synchronized (this) {
            pollingThreadStarted = false;
            start(); // Start another polling thread if queue is not empty.
        }
    }

    /**
     * Connects to the Java DataBase specified by the data base connection 
     * variables.  This connection is needed to work with the database table 
     * MESSAGE_SENDING_QUEUE.  The connection can be either embedded or client.
     * Be sure that the database connection variables are correct for the
     * program you are working with.
     */
    private Connection establishDataBaseConnection() {
        Connection con = null;

        //load the driver
        try {
            //this driver is for embedded mode and loads "derby.jar"
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ie) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ie);
        } catch (IllegalAccessException iae) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, iae);
        }

        try {
            //creates a new embedded Connection
            String strURL = PROTOCOL + Mediator.getRuntimeDirectory() + DATABASE_NAME
                    + ";create=true;"; // Creates the database if it doesn't already exist.

            //swap these lines if you do not want to use a user name and password
            con = DriverManager.getConnection(strURL);
//            con = DriverManager.getConnection(strURL, USER_NAME, PASSWORD); 
        } catch (SQLException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    /**
     * Creates the MESSAGE_SENDING_QUEUE JavaDataBase table using SQL.
     * This table keeps track of which Messages still need to be sent out.
     * The table only stores a few of the fields of the Message's: 
     * DESTINATION, XML_CODE, and HOP_COUNT.
     */
    private boolean createTable() {
        boolean tableOKforUse = false;
        //create an SQL Statement
        Statement stmt = null;
        String createTableQuery = "CREATE TABLE \"" + TABLE_NAME + "\""
                + "("
                + "MESSAGE_ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                + "DESTINATION VARCHAR(100), "
                + "XML_CODE BLOB(30000), "
                + "HOP_COUNT INTEGER"
                + ")";
        try {
            stmt = dataBaseConnection.createStatement();
            stmt.execute(createTableQuery);
            stmt.close();
            tableOKforUse = true;
        } catch (SQLException ex) {
            /* If the Exception is just saying that the table already exists,
             * that is ok and the table is still safe to use.  Otherwise,
             * we want to report any other errors. */
            if ("X0Y32".equals(ex.getSQLState())) {  //this is the "table already exists" error
                tableOKforUse = true;
            } else {
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tableOKforUse;
    }

    /**
     * Deletes a Message from the MESSAGE_SENDING_QUEUE
     * 
     * @param msgID message_id (table primary key) for message to delete
     */
    private void delete(int msgID) {
        //prepare SQL statement to delete the Message
        Statement stmt = null;
        String deleteQuerySQL = "DELETE FROM " + TABLE_NAME
                + " WHERE MESSAGE_ID = " + msgID;
        Logger.getLogger(QueueManager.class.getName()).log(Level.FINER, deleteQuerySQL);
        //try to make the deletion
        try {
            stmt = dataBaseConnection.createStatement();
            stmt.execute(deleteQuerySQL);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Tests to see if the queue is empty.
     * 
     * @return true if the queue is empty, false if it is not empty.
     */
    private boolean isEmpty() {
        boolean returnValue = true;
        //prepare SQL statement to print
        try {
            String selectAllQuerySQL = "SELECT 1 FROM " + TABLE_NAME; // Dummy select to see if there are any records.
            Statement stmt = dataBaseConnection.createStatement();
            ResultSet resultSet = stmt.executeQuery(selectAllQuerySQL);
            returnValue = !resultSet.next(); // If next() is true, then isEmpty() is false, and visa versa
            resultSet.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }

    /**
     * Quotes a string for use in a SQL statement.
     * Doubles single quotes (') and backslashes (\).
     * If the string is null, returns "null".
     * If the string is not null, returns the string with single quotes (') around it.
     * 
     * @param s string to quote.
     * @return quoted string.
     */
    public static String quote(String s) {
        if (s == null) {
            s = "null";
        } else {
            s = s.replace("'", "''");
            s = s.replace("\\", "\\\\");
            s = "'" + s + "'";
        }
        return s;
    }
}//end class QueueManager
