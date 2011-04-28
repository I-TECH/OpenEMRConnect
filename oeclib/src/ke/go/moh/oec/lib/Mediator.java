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
import ke.go.moh.oec.IService;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import ke.go.moh.oec.PersonRequest;

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
     * In the present design, the hop count should never exceed 3. If it does,
     * it probably indicates a loop where one system is routing the message
     * to a second system, and the second system is routing it back to the
     * first system.
     * <p>
     * If the hop count exceeds the maximum, the message is discarded and
     * an error reported.
     */
    private static final int MAX_HOP_COUNT = 3;
    /**
     * The number of protocol message IDs generated since this library
     * was initialized. The sequence number forms part of the message ID.
     */
    private static int messageSequenceNumber = 0;

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

    public Mediator() {
        httpService = new HttpService(this);
        queueManager = new QueueManager(httpService);
        xmlPacker = new XmlPacker();
        try {
            httpService.start();
        } catch (IOException ex) {
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
        }
        queueManager.start();
        Logger.getLogger(Mediator.class.getName()).log(Level.INFO, "OpenEMRConnect library services started.");
    }

    /**
     * Stop the OpenEMRConnect library services.
     *
     * This routine should be called for an orderly shut-down
     * of the Mediator library.
     * <p>
     * Call this method last, after you are through using the services.
     */
    public void stop() {
        Logger.getLogger(Mediator.class.getName()).log(Level.INFO, "OpenEMRConnect library services stopped.");
        queueManager.stop();
//        try {
//            httpService.stop();
//        } catch (IOException ex) {
//            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Gets the value of a named property from a standard properties file.
     *
     * @param propertyName name of the property whose value we want
     * @return the value of the requested property
     */
    public static String getProperty(String propertyName) {
        if (properties == null) {
            properties = new Properties();
            final String propertiesFileName = "openemrconnect.properties";
            try {
                properties.load(new FileInputStream(propertiesFileName));
            } catch (Exception e) {
                /*
                 * We somehow failed to open our default propoerties file.
                 * This should not happen. It should always be there.
                 */
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                        "getProperty() Can''t open ''{0}''", propertiesFileName);
            }
        }
        return properties.getProperty(propertyName);
    }

    /**
     * Authenticates a user to use the system. Returns true if the username
     * and password are valid and the user is authorized to use this system.
     *
     * @param username the username to be authenticated.
     * @param password the password to be authenticated.
     * @return a boolean value indicating authentication success or failure.
     */
    public static boolean authenticate(String username, String password) {
        throw new UnsupportedOperationException();
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
		m.setData(requestData);
		
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
         * Find the destination address and name. This is usually the default
         * destination for the message type. However if our caller is passing
		 * us <code>PersonRequest</code> data, they may choose to explicitly
		 * specify the destination rather than leaving it to the default.
         */
        m.setDestinationAddress(messageType.getDefaultDestinationAddress());
        m.setDestinationName(messageType.getDefaultDestinationName());
		if (requestData instanceof PersonRequest) {
            PersonRequest pr = (PersonRequest)requestData;
            if (pr.getDestinationAddress() != null) {
                m.setDestinationAddress(pr.getDestinationAddress());
            }
            if (pr.getDestinationName() != null) {
                m.setDestinationName(pr.getDestinationName());
            }
        }
        Object returnData = null;
        if (m.getDestinationAddress() == null) {
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "getData() - Destination not found for Request type ''{0}''",
                    messageType.getRequestTypeId());
        } else {
            /*
             * Generate a new request ID, and send the request to the server.
             */
            String messageId = generateMessageId();
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
    private synchronized String generateMessageId() {
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
    private Object sendData(Message m) {
		MessageType messageType = m.getMessageType(); // For handy reference.
        String ipAddressPort = getIpAddressPort(m.getDestinationAddress());
        if (ipAddressPort == null) {
            /*
             * This is an error in our routing mechanism. We have a desination
             * address, but we were unable to translate it into an IP address
             * and port number combination.
             */
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                        "getData() - Routing address not found for ''{0}''", m.getDestinationAddress());
            return null;
        }
        /*
         * Pack the data into the XML message.
         */
        String xml = xmlPacker.pack(m);
        /*
         * If we may get a response to this message, add it to the list of responses we are expecting.
         */
        if (messageType.getResponseMessageType() != null) {
            registerRequest(m);
        }
        /*
         * Send the message.
         */
        sendMessage(xml, ipAddressPort, m.getDestinationAddress(), 1, messageType.isToBeQueued());
        /*
         * If we expect a response to this message, wait for the response.
         */
        Object returnData = null;
        if (messageType.getResponseMessageType() != null) {
            returnData = waitForResponse(m);
        }
        /*
         * Return.
         */
        return returnData;
    }

    /**
     * Adds this message to a list of messages for which we expect a response.
     * @param m message we are expecting a response to
     */
    private synchronized void registerRequest(Message m) {
        //TO DO: Write the contents
    }

    /**
     * Waits for a response to our request message. When we get the
     * response, return it to our caller. If there is no response message
     * after a defined timeout period, return null.
     *
     * @param m message we are waiting for a response to
     * @return data object to return to our caller, or null if timed out.
     */
    private Object waitForResponse(Message m) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets IP address and port (next hop from here) for a destination
     * <p>
     * The IP address and port corresponding to a destination address is found
     * in properties for this application starting with "IPAddressPort."
     * If the full address is not found, then we look for successively
     * higher levels in the address name, where levels are separated by
     * dots. Finally we look for the catch-all entry "IPAddressPort.*"
     * to which we forward any otherwise-unresolved address.
     * <p>
     * For example, if the address to find is "aa.bb.cc", we will look
     * for the following properties in this order until we find a value:
     * <p>
     * IpAddressPort.aa.bb.cc   <br>
     * IpAddressPort.aa.bb      <br>
     * IpAddressPort.aa         <br>
     * IpAddressPort.*          <br>
     *
     * @param destination where the message is to be sent
     * @return IP address:port to which to forward the message.
     * Returns <code>null</code> if the destination is ourselves,
     * or the destination address cannot be translated to IP + port.
     */
    private String getIpAddressPort(String destination) {
        final String propertyPrefix = "IPAddressPort.";
        /*
         * If the destination is us, return null. This means that
         * we don't have to go to the network to find the address;
         * the address is our own.
         */
        if (destination.equals(getProperty("Instance.Address"))) {
            return null;
        }
        /*
         * Check for an explicit entry for this destination address.
         */
        String returnValue = getProperty(propertyPrefix + destination);
        /*
         * If there was no entry for the whole address, try successively
         * shorter strings by chopping off the end from the last '.' character.
         */
        while (returnValue == null) {
            int i = destination.lastIndexOf('.');
            /*
             * If there are no more segments to chop, try for
             * the catch-all wildcard entry.
             */
            if (i < 0) {
                returnValue = getProperty(propertyPrefix + "*");
                break;
            }
            /*
             * Chop the string and look for the next higher level.
             */
            destination = destination.substring(0, i);
            returnValue = getProperty(propertyPrefix + destination);
        }
        return returnValue;
    }

    /**
     * Process a received HTTP message. Either this is a message that is
     * destined for us, or we are an intermediate node that should forward the
     * message on its way to the next node. If we find that we have an
     * IP address to which we should forward this message, than forward it
     * on its way. Otherwise we will unpack and process it locally.
     *
     * @param message the body of the HTTP message received
     * @param destination the ultimate message destination (from the URL)
     * @param hopCount the forwarding hop count (from the URL)
     * @param storeAndForward the indicator of whether this message should be stored and forwarded (from the URL)
     */
    protected void processReceivedMessage(String xml, String destination, int hopCount, boolean toBeQueued) {
        String ourInstanceAddress = getProperty("Instance.Address");
        String ipAddressPort = getIpAddressPort(destination);
        if (destination.equals(ourInstanceAddress)) { // If the message is addressed to us:
            if (ipAddressPort == null) {
                /*
                 * The message destination matches our own instance address
                 * and we do not find a different IP address/port for the
                 * message destination. It is really destined for us. Process it.
                 */
                processLocalMessage(xml);
            } else {
                /*
                 * The message destination matches our own instance address
                 * but the router has returned a IP address/port for the
                 * destination that is not us. Somehing funny is happening.
                 */
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                        "Message destination ''{0}'' matches our own name, but router returns IP Address/port of ''{1}''",
                        new Object[]{destination, ipAddressPort});
            }
        } else {    // If the message is not addressed to us:
            if (ipAddressPort == null) {
                /*
                 * The message destination does not match our own,
                 * and the router is not giving us an IP Port/Address for it.
                 */
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                        "IP Address/port not found for message with destination ''{0}'', our instance address is ''{1}''",
                        new Object[]{destination, ourInstanceAddress});
            } else {
                /*
                 * The message destination does not match our own,
                 * and we have found an external IP address/port for it.
                 * It is not destined for us, so we will pass it though
                 * to its destination.
                 */
                sendMessage(xml, ipAddressPort, destination, hopCount + 1, toBeQueued);
            }
        }
    }

    /**
     * Process a HTTP message that is destined for us. We have already determined that it
     * is not a message that we are just routing through to somewhere else.
     * 
     * @param xml the message received from the HTTP listener.
     */
    private void processLocalMessage(String xml) {
        /*
         * Unpack the XML message.
         *
         * Check to see if this message is a response we are waiting for.
         * If so, deliver it. If not, process this as an unsolicited message.
         *
         */
        Message m = xmlPacker.unpack(xml);
        boolean responseDelivered = deliverResponse(m);
        if (!responseDelivered) { // Was the message a response to a request that we just delivered?
            processUnsolicitedMessage(m);
        }
    }

    /**
     * Deliver a response that we were waiting for. This method is called in the
     * thread that was listening for HTTP messages. If the message matches one
     * for which we are waiting, then wake up the thread waiting for the message
     * and pass the response to it.
     * <p>
     * Otherwise, indicate that we were not waiting for this message.
     *
     * @param m the unpacked message information.
     * @return true if we were waiting for the message and it was delivered,
     * otherwise false.
     */
    private synchronized boolean deliverResponse(Message m) {
        throw new UnsupportedOperationException();
    }

    /**
     * Process an unsolicited message. We have already determined that this
     * was not the response to a request that we were waiting for.
     *
     * @param m the unpacked message information.
     * @param data unpacked message data that we received.
     * @param requestID requestID found in the message we received.
     * @param source message address where the message came from.
     */
    private void processUnsolicitedMessage(Message m) {
        MessageType messageType = m.getMessageType(); // For convenience below (code readability).
        if (messageType.getRequestTypeId() != 0) { // Does this message have a request ID?
            if (myCallbackObject != null) { // Yes. Did the user register a callback routine?
                /*
                 * The message type is allowed to be unsolicited, and our caller has registered a callback routine.
                 * So deliver the message to our user.
                 * Then see if any response is returned.
                 * If a response is returned, and it is expected, deliver the response back to the original requesting source.
                 */
                Object responseData = myCallbackObject.getData(messageType.getRequestTypeId(), m.getData());
                if (responseData != null) {
                    if (messageType.getResponseMessageType() != null) {
						Message response = new Message();
						response.setMessageType(messageType.getResponseMessageType());
						response.setData(m.getMessageId());
						response.setDestinationAddress(m.getSourceAddress());
						response.setDestinationName(m.getSourceName());
						response.setMessageId(m.getMessageId());
                        Object responseToResponseData = sendData(response);
                        if (responseToResponseData != null) {
                            /*
                             * We have delivered a request to the server, and it has given us back a response.
                             * We have delivered the response back to the original requesting source.
                             * The requesting source has sent us back a message (a response to the response.)
                             * We did not expect this.
                             */
                Logger.getLogger(Mediator.class.getName()).log(Level.WARNING,
                        "After returning a response to requestTypeId {0} from source ''{1}'', the source returned more data back to us!",
                        new Object[]{messageType.getRequestTypeId(), m.getSourceAddress()});
                        }
                    } else {
                        /*
                         * This appears to be an error in our MessageType registry. We have a MessageType
                         * entry for the message we received, but there is no response message type.
                         */
                        Logger.getLogger(Mediator.class.getName()).log(Level.WARNING,
                                "Message type for requestTypeId {0} does not reference a return message type.",
                                messageType.getRequestTypeId());
                    }
                }
            } else {
                /*
                 * The user has not defined a callback routine. Meanwhile, someone sent us
                 * an unsolicited message -- at least a message that was not a reply we were
                 * waiting for.
                 */
                Logger.getLogger(Mediator.class.getName()).log(Level.WARNING,
                        "Unsolicited message with request type {0} received from ''{1}''. No user callback is registered.",
                        new Object[]{messageType.getRequestTypeId(), m.getSourceAddress()});
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
                        "Unsolicited message with XML root ''{0}'' received from ''{1}'', but it isn''t registered as a request.",
                        new Object[]{messageType.getRootXmlTag(), m.getSourceAddress()});
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
     * with our without the queueing mechanism for storing and forwarding.
     * Then we send it.
     * 
     * @param xml the XML packed message to send
     * @param ipAddressPort IP address and port to which to send the message
     * @param destination ultimate destination address for this message
     * @param hopCount how many times this message has been sent
     * @param toBeQueued is this message to be queued for later if it can't be sent now?
     */
    private void sendMessage(String xml, String ipAddressPort, String destination, int hopCount, boolean toBeQueued) {
        if (hopCount > MAX_HOP_COUNT) {
            /*
             * A message has been forwarded too many times, exceeding the maximum hop count.
             * This may indicate a routing loop between two or more systems.
             */
            Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE,
                    "sendMessage() - Hop count {0} exceeds maximum hop count {1} for destination ''{2}'', routed to ''{3}''",
                    new Object[]{hopCount, MAX_HOP_COUNT, destination, ipAddressPort});
        } else if (toBeQueued) {
            queueManager.enqueue(xml, ipAddressPort, destination, hopCount);
        } else {
            try {
                httpService.send(xml, ipAddressPort, destination, toBeQueued, hopCount); // (toBeQueued = false)
            } catch (MalformedURLException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
