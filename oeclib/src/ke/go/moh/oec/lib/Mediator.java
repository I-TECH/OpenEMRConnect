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
package ke.go.moh.oec.lib;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import ke.go.moh.oec.IService;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;

/**
 * Mediator between OEC clients and services to forward requests
 * and return responses. This may include storing and forwarding requests
 * and/or responses when network connections cannot be made.
 * <p>
 * If the user of this class
 * accepts unsolicited messages (as a server), then it must register
 * a callback object that is compliant with the IService interface. If the
 * user of this class is just making client requests, this is not necessary.
 *
 * @author JGitahi
 * @author Jim Grace
 */
public class Mediator implements IService {

    /**
     * Maximum number of times a message may be transmitted through the
     * network on its way from source to destination.
     * <p>
     * For example, the first time a message is sent, the hop count is 1.
     * If the message is received by another program and forwarded on,
     * the hop count is 2, and so on.
     * <p>
     * In the present design, the hop count should never exceed 4. If it does,
     * it probably indicates a loop where one system is routing the message
     * to a second system, and the second system is routing it back to the
     * first system.
     * <p>
     * If the hop count exceeds the maximum, the message is discarded and
     * an error reported.
     */
    private static final int MAX_HOP_COUNT = 4;
    /**
     * The number of protocol message IDs generated since this library
     * was initialized. The sequence number forms part of the message ID.
     */
    private static int messageSequenceNumber = 0;
    /**
     * The queue of messages we have sent, for which we expect a response.
     */
    private static MessagePendingQueue pendingQueue = new MessagePendingQueue();
    /*
     * Allocate objects that we will use for this instance of Mediator.
     * These are instance variables, so they will be thread safe in the
     * event that different instances of Mediator are operating concurrently
     * on different threads.
     */
    /**
     * Instance to handle HTTP protocol.
     * Start the instance with a reference to us, so it knows where to
     * deliver any HTTP messages received.
     */
    private HttpService httpService;
    /*
     * Instance to manage the store-and-forward message queue.
     * Start the instance with a reference to the HTTP Handler, so it
     * knows how to send any messages on the network.
     */
    private QueueManager queueManager;
    /** Instance to pack and unpack XML */
    private XmlPacker xmlPacker;
    /**
     * Reference to our caller's callbackObject that implements
     * {@link IService#getData(int, java.lang.Object)}.
     */
    static private IService myCallbackObject = null;
    /**
     * A copy of the properties from standard file location
     */
    static Properties properties = null;
    /** Lock the properties file we are using, so multiple instances will use multiple properties files. */
    static FileLock pathLock = null;
    /** The logger level to use, configured from the properties file. */
    static Level loggerLevel = null;
    /** Should we use the logging service? */
    static boolean useLoggingService = true;
    /** Directory where we find our properties and QueueManager embedded database. */
    static String runtimeDirectory;

    /**
     * Initialize -- set up the runtime directory.
     */
    static {
        setRuntimeDirectory(); // Do this first!
    }

    /**
     * Constructs an instance of the Mediator.
     * (Note: there should be only one instance of the mediator. At some
     * point in the future, this class, and all who call it, should
     * properly be refactored to follow the Java singleton pattern.)
     * <p>
     * If we are to use our own distributed logging service, set up the
     * LoggingServiceHandler to handle all calls to the standard logger.
     * <p>
     * Allocate other library class objects as needed, and start them
     * as needed. In particular, the HttpManager and QueueManager need
     * to be started.
     */
    public Mediator() {
        setLoggerLevel();
        LogManager man = LogManager.getLogManager();
        Logger rootLogger = man.getLogger("");
        if (useLoggingService) {
            Handler loggingServiceHandler = new LoggingServiceHandler(this);
            Formatter formatter = new SimpleFormatter();
            loggingServiceHandler.setFormatter(formatter);
            rootLogger.addHandler(loggingServiceHandler);
        }
        String loggerFile = getProperty("Logger.File");
        if (loggerFile != null && Boolean.parseBoolean(loggerFile)) {
            try {
                String logFileName = runtimeDirectory + "openemrconnect%g.log";
                Handler loggingFileHandler = new FileHandler(logFileName, 100000, 100);
                Formatter formatter = new SimpleFormatter();
                loggingFileHandler.setFormatter(formatter);
                loggingFileHandler.setLevel(loggerLevel);
                rootLogger.addHandler(loggingFileHandler);
            } catch (IOException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, "Can''t start file logger.", ex);
            } catch (SecurityException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, "Can''t start file logger.", ex);
            }
        }
        httpService = new HttpService(this);
        queueManager = new QueueManager(httpService);
        xmlPacker = new XmlPacker();
        try {
            httpService.start();
        } catch (IOException ex) {
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
        }
        queueManager.start();
        Logger.getLogger(Mediator.class.getName()).log(Level.FINE,
                "{0} started.", getProperty("Instance.Name"));
    }

    /**
     * Sets up the runtimeDirectory string to select a working directory relative
     * to the default application directory. The purpose is to allow multiple
     * instances of the code to run from the same directory. This can be
     * especially useful in debugging and testing environments. The default
     * directory for each instance is used to contain the openemrconnect.properties
     * file used by that instance. It may contain other directories or files as
     * well, such as the embedded JavaDB database used for the QueueManager.
     * <p>
     * The runtimeDirectory is determined as follows: The first time an application
     * is run, it places a lock on a dummy file in the default application directory.
     * When this lock is successfully in place, it then leaves the runtimeDirectory
     * set to an empty string -- meaning that the runtime directory is the same
     * as the default application directory.
     * <p>
     * The second time an application is run concurrently from the same
     * application directory, we will find that the dummy file is already
     * locked by the first instance of the application. In that event, we
     * will try a subdirectory of "runtime2/", relative to the default
     * application directory.
     */
    static void setRuntimeDirectory() {
        try {
            runtimeDirectory = "";
            for (int i = 2;; i++) { // Try current directory, then "runtime2/", "runtime3/", etc.
                RandomAccessFile raf = new RandomAccessFile(runtimeDirectory + "lockfile.lck", "rw");
                FileChannel fc = raf.getChannel();
                pathLock = fc.tryLock();
                if (pathLock != null) {
                    break;
                }
                runtimeDirectory = "runtime" + i + "/"; // Construct a subdirectory name "runtime2/", "runtime3/", etc.
            }
        } catch (Exception ex) {
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "Can''t lock directory {0}. please either create the directory or run the app fewer times.",
                    runtimeDirectory);
            System.exit(1);
        }
    }

    static public String getRuntimeDirectory() {
        return runtimeDirectory;
    }

    /**
     * Stops the OpenEMRConnect library services.
     *
     * This routine should be called for an orderly shut-down
     * of the Mediator library.
     * <p>
     * Call this method last, after you are through using the services.
     */
    public void stop() {
        Logger.getLogger(Mediator.class.getName()).log(Level.INFO, "OpenEMRConnect library services stopped.");
        queueManager.stop();
        httpService.stop();
    }

    /**
     * Supresses the use of the logging service to send messages to the logging server.
     * This method is intended for use by the logging service itself,
     * so it won't try to send all log entries to itself.
     */
    public static void suppressLoggingService() {
        useLoggingService = false;
    }

    /**
     * Sets the logging level according to the properties file.
     * The logging level is also set in the root handler if it is not the default.
     * Otherwise, new loggers will not be able to log anything at a
     * lower level than the default level.
     */
    private static void setLoggerLevel() {
        if (loggerLevel == null) {
            loggerLevel = Level.INFO; // Default unless changed below.
            String loggerLevelName = getProperty("Logger.Level");
            if (loggerLevelName != null) {
                try {
                    loggerLevel = Level.parse(loggerLevelName);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Mediator.class.getName()).log(Level.WARNING,
                            "Logger.Level property ''{0}'' not a valid logger level.", loggerLevelName);
                }
                if (loggerLevel != Level.INFO) { // Need to change default handler level?
                    LogManager m = LogManager.getLogManager();
                    Logger rootLogger = m.getLogger("");
                    Handler rootHandler = rootLogger.getHandlers()[0];
                    rootHandler.setLevel(loggerLevel);
                }
            }
        }
    }

    /**
     * Tests to see if we should log something at a given level.
     * <p>
     * This method can be used to save the CPU time of a call to a logger.
     * It can be useful if the call to the logger itself may use a non-trivial amount of CPU.
     * For example, a logger call may invoke other methods to get some of the
     * arguments needed for the call. These tests need only be done if the
     * logger level is set low enough that the logger call will actually do something.
     *
     * @param testLevel Level to check if it would be logged.
     * @return true if this Level would be logged, otherwise false.
     */
    public static boolean testLoggerLevel(Level testLevel) {
        boolean returnValue;
        if (loggerLevel == null) {
            returnValue = Level.INFO.intValue() <= testLevel.intValue();
        } else {
            returnValue = loggerLevel.intValue() <= testLevel.intValue();
        }
        return returnValue;
    }

    /**
     * Gets a standard java.util.logging.Logger, set to the logging level property, if any.
     * If the logging level property is not the default Level.INFO, the logging level
     * is also changed in the root logging handler currently defined.
     * 
     * @param loggerName Name of the logger to create.
     * @return The Logger requested.
     */
    public static Logger getLogger(String loggerName) {
        setLoggerLevel();
        Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(loggerLevel);
        return logger;
    }

    /**
     * Gets the standard properties class.
     * @return properties.
     * <p>
     * The default property file is named openemrconnect.properties.
     */
    public static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            String propFileName = runtimeDirectory + "openemrconnect.properties";
            try {
                FileInputStream fis = new FileInputStream(propFileName);
                properties.load(fis);
                fis.close();
            } catch (Exception ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                        "getProperty() Can''t open ''{0}'' -- Please create the properties file if it doesn''t exist and then restart the app",
                        propFileName);
                System.exit(1);
            }
        }
        return properties;
    }
    
    /**
     * Gets the value of a named property from the standard properties list.
     *
     * @param propertyName name of the property whose value we want
     * @return the value of the requested property,
     * or null if the property is not found.
     */
    public static String getProperty(String propertyName) {
        if (properties == null) {
            getProperties();
        }
        return properties.getProperty(propertyName);
    }

    /**
     * Registers a listener to receive data requests
     *
     * This method is used by server software to register an object supporting
     * the IService interface getData() method. When a message for this server
     * is received (and is not a response to an outstanding request), it will
     * be given to the getData() method of this object.
     *
     * @param callbackObject object implementing IService interface
     */
    public static void registerCallback(IService callbackObject) {
        myCallbackObject = callbackObject;
    }

    /**
     * Makes a remote data request. This version is called by our user
     * to send a new data request to a server.
     *
     * @param requestTypeId type of request (see RequestType.java)
     * @param requestData object containing data for the request
     * @return object containing response data resulting from the request, or null if none
     */
    public Object getData(int requestTypeId, Object requestData) {
        Message m = new Message();
        m.setMessageData(requestData);
        m.setSourceAddress(getProperty("Instance.Address"));
        m.setSourceName(getProperty("Instance.Name"));

        /*
         * Determine the Type of message we are to send.
         */
        MessageType messageType = MessageTypeRegistry.find(requestTypeId);
        m.setMessageType(messageType);
        if (messageType == null) {
            /*
             * This is most likely an error on the part of our caller. We were
             * called with a request type ID that is not found as a request
             * in our MessageType list.
             */
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "getData() - Message type not found for Request type ID ''{0}''", requestTypeId);
            return null;
        }
        /*
         * If there is a response message type, then set responseExpected to true.
         * Note that if the message type is createPerson or modifyPerson, this
         * may be overridden by the caller's desire, below.
         */
        if (messageType.getResponseMessageType() != null) {
            m.setResponseExpected(true);
        } else {
            m.setResponseExpected(false);
        }
        /*
         * Find the destination address and name. This is usually the default
         * destination for the message type. However if our caller is passing
         * us <code>PersonRequest</code> data, they may choose to explicitly
         * specify the destination rather than leaving it to the default.
         * Also, if we have a <code>PersonRequest</code>, then the caller
         * has the option of specifying an XML string to be used
         * instead of the standard template for the message.
         */
        String defaultDestinationAddress = getProperty(messageType.getDefaultDestinationAddressProperty());
        m.setDestinationAddress(defaultDestinationAddress);
        m.setDestinationName(messageType.getDefaultDestinationName());
        String messageId = generateMessageId();
        m.setMessageId(messageId);
        m.setToBeQueued(messageType.isToBeQueued());
        if (requestData instanceof PersonRequest) {
            PersonRequest pr = (PersonRequest) requestData;
            if (pr.getDestinationAddress() != null) {
                m.setDestinationAddress(pr.getDestinationAddress());
            }
            if (pr.getDestinationName() != null) {
                m.setDestinationName(pr.getDestinationName());
            }
            m.setXml(pr.getXml());
            if (pr.getRequestReference() != null) {
                m.setMessageId(pr.getRequestReference()); // Overwrite the auto-generated message ID.
            }
            if (!pr.isResponseRequested()) {
                MessageType.TemplateType templateType = messageType.getTemplateType();
                if (templateType == MessageType.TemplateType.createPerson
                        || templateType == MessageType.TemplateType.modifyPerson) {
                    m.setResponseExpected(false);
                    m.setToBeQueued(true);
                }
            }
        }
        Object returnData = null;
        if (m.getDestinationAddress() == null) {
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "getData() - Can''t find {0} in properties file.",
                    messageType.getDefaultDestinationAddressProperty());
        } else {
            /*
             * Send the request to the server.
             */
            returnData = sendData(m);
        }
        return returnData;
    }

    /**
     * Constructs a new message ID to use for a message. The ID must be
     * unique for requests coming from this instance address (this
     * client or service running on this machine.)
     * <p>
     * We construct the message ID by putting two components together. The first
     * is the current time in milliseconds (since January 1, 1970.) The
     * second is a sequence number that starts at 0 when this process
     * started running. So even if we generate two message IDs without the
     * system time changing, they will be unique.
     * <p>
     * Note that if a message is a response to another message, it typically
     * uses the request message ID as its own, so the request and response
     * can be correlated. In this case, a new ID is not generated for the
     * response message.
     *
     * @return the new request ID.
     */
    public static synchronized String generateMessageId() {
        long milliseconds = new Date().getTime();
        return Long.toString(milliseconds) + Long.toString(messageSequenceNumber++);
    }

    /**
     * Sends data to a remote destination. This version is called internally, in one of two ways:
     * <p>
     * 1. From the public <code>getData</code> request, to continue processing a user
     * client request that needs to be sent to a server.
     * <p>
     * 2. From our received message handling. This is when an unsolicited request has
     * come here for a server that is bound to us. We have delivered the request to
     * the server. The server has given us back a response. And now we need to pack
     * up the response and return it to the client.
     *
     * @param m message to be sent
     * @return object containing response data from the request
     */
    Object sendData(Message m) {
        Object returnData = null;
        MessageType messageType = m.getMessageType(); // For handy reference.
        NextHop nextHop = NextHop.getNextHopByAddress(m.getDestinationAddress());
        m.setNextHop(nextHop);
        if (nextHop == null) {
            /*
             * This is an error in our routing mechanism. We have a desination
             * address, but we were unable to translate it into next hop information.
             */
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "getData() - Next hop information not found for ''{0}'': {1}",
                    new Object[]{m.getDestinationAddress(), m.summarize()});
            return null;
        }
        /*
         * Pack the data into the XML message, and compress it.
         */
        String xml = xmlPacker.pack(m);
        m.setXml(xml);
        Compresser.compress(m);
        /*
         * If we may get a response to this message, add it to the list of responses we are expecting.
         */
        MessagePendingQueue.Entry queueEntry = null;
        if (m.isResponseExpected()) {
            queueEntry = pendingQueue.enqueue(m);
        }
        /*
         * Send the message.
         */
        m.setHopCount(1); // This will be the first hop.
        if (Mediator.testLoggerLevel(Level.FINE)) {
            Mediator.getLogger(Mediator.class.getName()).log(Level.FINE, "Sending message {0}", m.summarize());
        }
        boolean messageSent = sendMessage(m);
        /*
         * If we expect a response to this message, wait for the response.
         * When we get the response, return it to our caller. If there is no
         * response message after a defined timeout period, return failure.
         */
        if (m.isResponseExpected()) {
            Message responseMessage = null;
            if (messageSent) {
                responseMessage = pendingQueue.waitForResponse(queueEntry);
            } else {
                pendingQueue.dequeue(queueEntry);
            }
            MessageType.TemplateType templateType = m.getMessageType().getTemplateType();
            switch (templateType) {
                case findPerson:
                case createPerson:
                case modifyPerson:
                    //added above two lines in place of the two below  to specify the message 
                    //templates that expect a response
//                case createPersonAccepted:
//                case modifyPersonAccepted:
                    PersonResponse personResponse;
                    if (responseMessage != null) {
                        returnData = responseMessage.getMessageData();
                        personResponse = (PersonResponse) returnData;
                        personResponse.setSuccessful(true);
                    } else {
                        personResponse = new PersonResponse();
                        personResponse.setSuccessful(false);
                    }
                    break;

                default:
                    Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                            "sendData() Message with requestId {0} and templateType {1} is expecting a response but not handled.",
                            new Object[]{m.getMessageType().getRequestTypeId(), templateType.name()});
                    break;
            }
        }
        /*
         * Return.
         */
        return returnData;
    }

    /**
     * Process a received HTTP message. Either this is a message that is
     * destined for us, or we are an intermediate node that should forward the
     * message on its way to the next node. If we find that we have an
     * IP address to which we should forward this message, than forward it
     * on its way. Otherwise we will unpack and process it locally.
     *
     * @param m Message received
     */
    void processReceivedMessage(Message m) {
        String destinationAddress = m.getDestinationAddress();
        if (destinationAddress == null) {
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "Message has no destination address.");
        } else {
            String ourInstanceAddress = getProperty("Instance.Address");
            if (destinationAddress.equalsIgnoreCase(ourInstanceAddress)) { // If the message is addressed to us:
                Compresser.decompress(m);
                xmlPacker.unpack(m);
                if (m.getMessageData() == null) {
                    Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                            "Received message did not unpack into messageData: {0}", m.summarize());
                } else {
                    if (m.getMessageData().getClass() == PersonRequest.class) {
                        PersonRequest req = (PersonRequest) m.getMessageData();
                        req.setSourceAddress(m.getSourceAddress());
                        req.setSourceName(m.getSourceName());
                        req.setRequestReference(m.getMessageId());
                        req.setXml(m.getXml()); // Return raw XML through the API in case it is wanted.
                    } else if (m.getMessageData().getClass() == PersonResponse.class) {
                        PersonResponse rsp = (PersonResponse) m.getMessageData();
                        rsp.setSuccessful(true);
                        rsp.setRequestReference(m.getMessageId());
                    }
                    boolean responseDelivered = pendingQueue.findRequest(m);
                    if (responseDelivered) { // Was the message a response to a request that we just delivered?
                        if (Mediator.testLoggerLevel(Level.FINE)) {
                            Mediator.getLogger(Mediator.class.getName()).log(Level.FINE,
                                    "Received message delivered as response to API: {0}", m.summarize());
                        }
                    } else {
                        if (Mediator.testLoggerLevel(Level.FINE)) {
                            Mediator.getLogger(Mediator.class.getName()).log(Level.FINE,
                                    "Received message delivered unsolicited to API: {0}", m.summarize());
                        }
                        processUnsolicitedMessage(m);
                    }
                }
            } else {    // If the message is not addressed to us:
                NextHop nextHop = NextHop.getNextHopByAddress(destinationAddress);
                if (nextHop == null) {
                    /*
                     * The message destination does not match our own,
                     * and the router is not giving us next hop information.
                     * This is a configuration error.
                     */
                    Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                            "Next hop not found for received message {0}", m.summarize());
                } else {
                    /*
                     * The message destination does not match our own,
                     * and we have found an external IP address/port for it.
                     * It is not destined for us, so we will pass it though
                     * to its destination.
                     */
                    m.setNextHop(nextHop);
                    int hopCount = m.getHopCount();
                    hopCount++;
                    m.setHopCount(hopCount);
                    if (Mediator.testLoggerLevel(Level.FINE)) {
                        Mediator.getLogger(Mediator.class.getName()).log(Level.FINE,
                                "Relaying message {0}", m.summarize());
                    }
                    sendMessage(m);
                }
            }
        }
    }

    /**
     * Process an unsolicited message. We have already determined that this
     * was not the response to a request that we were waiting for.
     *
     * @param m the unpacked message information.
     */
    private void processUnsolicitedMessage(Message m) {
        MessageType messageType = m.getMessageType(); // For convenience below (code readability).
        if (messageType.getRequestTypeId() != 0) { // Does this message have a request ID?
            if (myCallbackObject != null) { // Yes. Did the user register a callback routine?
                CallbackThread c = new CallbackThread(this, myCallbackObject, m);
                Thread t = new Thread(c);
                t.start();
            } else {
                /*
                 * The user has not defined a callback routine. Meanwhile, someone sent us
                 * an unsolicited message -- at least a message that was not a reply we were
                 * waiting for.
                 */
                Logger.getLogger(Mediator.class.getName()).log(Level.WARNING,
                        "Unsolicited message with request type {0} received. No user callback is registered: {1}",
                        new Object[]{messageType.getRequestTypeId(), m.summarize()});
            }
        } else {
            /*
             * We received a message that wasn't a response we were waiting for. Also, it
             * didn't have a request message type.
             *
             * This could happen if we sent a message for which we were expecting a response,
             * and meanwhile our program restarted, loosing the memory of which responses
             * we were expecting. Then the response finally came but we weren't expecting it.
             *
             * Or this could be an error of some sort.
             */
            Logger.getLogger(Mediator.class.getName()).log(Level.WARNING,
                    "Unsolicited message with XML root ''{0}'' received, but it isn''t registered as a request: {1}",
                    new Object[]{messageType.getRootXmlTag(), m.summarize()});
        }
    }

    /**
     * Sends a packed XML message. This is common code that is called
     * for any of the following reasons:
     * <p>
     * 1. Forward a received message that is not ultimately destined for us.
     * <p>
     * 2. Send a new request to a server.
     * <p>
     * 3. Send a response back from a server.
     * <p>
     * We check the hop count to make sure the message is not caught in a
     * routing loop. Then we see whether the message should be sent
     * with our without the queuing mechanism for storing and forwarding.
     * Then we send it.
     *
     * @param m Message to send
     * @return true if the message was queued or sent successfully (to the next hop), otherwise false
     */
    private boolean sendMessage(Message m) {
        boolean messageSent = false;
        if (m.getHopCount() > MAX_HOP_COUNT) {
            /*
             * A message has been forwarded too many times, exceeding the maximum hop count.
             * This may indicate a routing loop between two or more systems.
             */
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "sendMessage() - Hop count {0} exceeds maximum hop count {1} for destination ''{2}'', routed to ''{3}'': {4}",
                    new Object[]{m.getHopCount(), MAX_HOP_COUNT, m.getDestinationAddress(), m.getNextHop().getIpAddressPort(), m.summarize()});
        } else if (m.isToBeQueued()) {
            messageSent = queueManager.enqueue(m);
        } else {
            try {
                messageSent = httpService.send(m); // (toBeQueued = false)
            } catch (MalformedURLException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, "Error sending: " + m.summarize(), ex);
            } catch (IOException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, "Error sending: " + m.summarize(), ex);
            }
        }
        return messageSent;
    }
}
