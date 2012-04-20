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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Handles HTTP requests and responses between OpenEMRConnect nodes.
 *
 * @author John Gitau
 * @author Jim Grace
 */
class HttpService {

    /**
     * {@link Mediator} class instance to which we pass any received HTTP
     * requests.
     */
    private Mediator mediator = null;
    private int id = 0;
    private int port = 0;
    HttpServer server;
    MessageDigest messageDigest;
    Map<String, Date> unreachableIpPorts = new HashMap<String, Date>();
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String HTTP_CONTENT_XML = "application/xml";
    private static final String HTTP_CONTENT_ZIP = "application/zip";
    private static final int HTTP_RESPONSE_OK = 200;
    private static final int HTTP_RESPONSE_LENGTH_REQUIRED = 411;
    private static final int HTTP_RESPONSE_MD5_MISMATCH = 449; // No obvious choice here, this code is Microsoft "Retry With"
    private static final int HTTP_RESPONSE_MD5_REQUIRED = 455; // OEC-defined code

    /**
     * Stores a partial message that is being received in segments from a given source.
     */
    private class PartialMessage {

        /** ID number of the partial message in progress. */
        private int id;
        /** Most recent segment number within the partial message. */
        private int segment = 0;
        /** Total length of all the segments received so far. */
        private int length = 0;
        /** Array of all segments received so far. */
        private List<byte[]> messageSegments = new ArrayList<byte[]>();
    }
    /**
     * Stores all partial messages that are in the process of being received in segments.
     * This HashMap is keyed by the IP address and (listening) port number of the sender.
     */
    private Map<String, PartialMessage> partialMessages = new HashMap<String, PartialMessage>();

    /**
     * Constructor to set {@link Mediator} callback object
     *
     * @param mediator {@link Mediator} callback object for listener
     */
    HttpService(Mediator mediator) {
        this.mediator = mediator;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, "Can't get an instance of the MD5 algorithm", ex);
        }
    }

    /**
     * Sends a HTTP message.
     *
     * @param m Message to send
     * @return true if message was sent and HTTP response received, otherwise false
     */
    boolean send(Message m) throws MalformedURLException, IOException {
        if (port == 0) {
            port = Integer.parseInt(Mediator.getProperty("HTTPHandler.ListenPort"));
        }
        boolean returnStatus = false;
        String destinationAddress = m.getDestinationAddress();
        NextHop nextHop = m.getNextHop();
        if (nextHop == null) {
            // If we are called from the QueueManager, we may not have the next hop information because they are not stored in the queue database.
            // If this is the case, then get the IP address and port now, from the destination address.
            nextHop = NextHop.getNextHopByAddress(destinationAddress);
            m.setNextHop(nextHop);
        }
        String ipAddressPort = nextHop.getIpAddressPort();
        int maxSize = nextHop.getMaxSize();
        String url = "http://" + ipAddressPort + "/oecmessage?destination=" + destinationAddress
                + "&tobequeued=" + m.isToBeQueued() + "&hopcount=" + m.getHopCount() + "&port=" + port;
        try {
            /*Code thats performing a task should be placed in the try catch statement especially in the try part*/
            byte[] messageBytes;
            int messageLength;
            String contentType;
            if (nextHop.isZip()) {
                messageBytes = m.getCompressedXml();
                messageLength = m.getCompressedXmlLength();
                contentType = HTTP_CONTENT_ZIP;
            } else {
                String xml = m.getXml();
                messageBytes = xml.getBytes();
                messageLength = messageBytes.length;
                contentType = HTTP_CONTENT_XML;
            }
            int sent = 0;
            int toSend = messageLength;
            if (messageLength > maxSize) { // If we're going to split this message:
                // Append the next message ID onto the URL.
                url += "&id=" + ++id + "&segment=";
            }
            int segment = 0;
            while (sent < messageLength) {
                String thisUrl = url;
                if (messageLength > maxSize) {
                    thisUrl = url + Integer.toString(++segment);
                    if (messageLength - sent > maxSize) {
                        toSend = maxSize;
                    } else {
                        toSend = messageLength - sent;
                        thisUrl = thisUrl + "&end";
                    }
                }
                HttpURLConnection connection = (HttpURLConnection) new URL(thisUrl).openConnection();
                String md5 = computeMd5(messageBytes, sent, toSend);
                connection.setRequestProperty("Content-MD5", md5);
                connection.setRequestProperty("Content-Type", contentType);
                connection.setDoOutput(true);
                OutputStream output = connection.getOutputStream();

                output.write(messageBytes, sent, toSend);
                output.close();
                int responseCode = connection.getResponseCode();
                //
                // Check the response code. It may be one of the response codes that
                // we know we generate from the other side if the message was garbled.
                // If it is one of these messages, then we assume the message was garbled,
                // because we know we formatted it correctly. If this is the case,
                // then just keep retrying to send the same message over and over.
                // As long as something is getting through, then the whole message should go through.
                //
                // If we get any other kind of response, either it was OK or the receiver
                // was not us. In either case, account for the number of bytes
                // sent, and continue sending (or finish if everything was sent.)
                //
                if (responseCode != HTTP_RESPONSE_LENGTH_REQUIRED
                        && responseCode != HTTP_RESPONSE_MD5_MISMATCH
                        && responseCode != HTTP_RESPONSE_MD5_REQUIRED) {
                    if (responseCode != HTTP_RESPONSE_OK) {
                        Logger.getLogger(HttpService.class.getName()).log(Level.FINE,
                                "HTTP response code {0}, sending message to {1} at {2}",
                                new Object[]{responseCode, m.getDestinationAddress(), url});
                    }
                    sent = sent + toSend;
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    while (br.readLine() != null) {
                        //content not required, just acknowlegment that message was received.
                    }
                    br.close();
                    inputStreamReader.close();
                } else {
                    Logger.getLogger(HttpService.class.getName()).log(Level.FINE,
                            "HTTP response code {0}. Retrying sending message to {1} at {2}",
                            new Object[]{responseCode, m.getDestinationAddress(), url});
                }
            }
            returnStatus = true;
            canReach(ipAddressPort);
        } catch (ConnectException ex) {
            cannotReach(ipAddressPort, "Can't connect to " + ipAddressPort + " for message to " + destinationAddress);
        } catch (UnknownHostException ex) {
            cannotReach(ipAddressPort, "Unknown Host " + ipAddressPort + " for message to " + destinationAddress);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE,
                    "While sending to " + m.getDestinationAddress() + " at " + url, ex);
        } catch (IOException ex) {
            String message = ex.getMessage();
            if (message.equals("Premature EOF")
                    || message.equals("Unexpected end of file from server")) {
                returnStatus = true; // We expect End of File at some point
            } else {
                Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE,
                        "While sending to " + m.getDestinationAddress() + " at " + url, ex);
//            There was some transmission error we return false.
            }
        }
        return returnStatus;
    }

    /**
     * Handles the case where we can't reach a given IP address / port.
     * <p>
     * If this is the first time we have this problem: (a) log an error message,
     * and (b) add this IP address / port to a list of IP addresses / ports with
     * whom we are having trouble communicating.
     * <p>
     * If this IP address / port is already on the list of destinations we cannot
     * reach, do nothing. This prevents trying to send a message to the
     * logging server every time we retry sending to this IP address / port.
     * If we did so, this could result in a lot of traffic to the logging server.
     * Worse yet, the message to the logging server might itself not be able to
     * be sent. Instead, we will send a single message to the logging server
     * at a later time when we can send to this IP address / port again.
     * 
     * @param ipAddressPort IP Address and Port we cannot reach
     * @param errorMessage Error why we cannot reach this IP address / port.
     */
    private synchronized void cannotReach(String ipAddressPort, String errorMessage) {
        if (!unreachableIpPorts.containsKey(ipAddressPort)) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, errorMessage);
            unreachableIpPorts.put(ipAddressPort, new Date());
        }
    }

    /**
     * Handles the case where we can reach a given IP address / port.
     * <p>
     * If we were previously having trouble reaching the given IP address / port,
     * it will be on a list of destinations with which we were having trouble.
     * In this case, log an informational message that the trouble is now over.
     * Include in this message the time when the trouble started. And remove
     * this IP address / port combination from our trouble list.
     * 
     * @param ipAddressPort IP Address and port we can reach
     */
    private void canReach(String ipAddressPort) {
        if (unreachableIpPorts.containsKey(ipAddressPort)) {
            Date sinceDate = unreachableIpPorts.get(ipAddressPort);
            Logger.getLogger(HttpService.class.getName()).log(Level.INFO,
                    "Can reach {0} for the first time since {1}",
                    new Object[]{ipAddressPort, SIMPLE_DATE_TIME_FORMAT.format(sinceDate)});
            unreachableIpPorts.remove(ipAddressPort);
        }
    }

    /**
     * Starts listening for HTTP messages.
     * <p>
     * For each message received, call mediator.processReceivedMessage()
     * @throws IOException
     */
    void start() throws IOException {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (port == 0) {
            port = Integer.parseInt(Mediator.getProperty("HTTPHandler.ListenPort"));
        }
        InetSocketAddress addr = new InetSocketAddress(port);
        server = HttpServer.create(addr, 0);
        server.createContext("/oecmessage", (HttpHandler) new Handler(mediator));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        Mediator.getLogger(HttpService.class.getName()).log(Level.INFO,
                Mediator.getProperty("Instance.Name") + " "
                + Mediator.getProperty("Instance.Address") + " listening on port {0}",
                Integer.toString(port)); // (Explicitly convert to string to avoid "," thousands seperator formatting.)
    }

    /**
     * Stops listening for HTTP messages.
     */
    void stop() {
        final int delaySeconds = 0;
        server.stop(delaySeconds);
    }

    /**
     * The handler class below implements the HttpHandler interface properties and is called up to process
     * HTTP exchanges.
     */
    private class Handler implements HttpHandler {

        private Mediator mediator = null;

        private Handler(Mediator mediator) {
            this.mediator = mediator;
        }

        /**
         *
         * @param exchange
         * @throws IOException
         */
        public void handle(HttpExchange exchange) throws IOException {
            Message m = new Message();
            /*
             * Unpack the URL.
             */
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            int id = 0;
            int segment = 0;
            boolean end = false;
            boolean zipped = false;
            //
            // Parse the URL arguments
            //
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair[0].equals("destination")) {
                    m.setDestinationAddress(pair[1]);
                } else if (pair[0].equals("hopcount")) {
                    m.setHopCount(Integer.parseInt(pair[1]));
                } else if (pair[0].equals("tobequeued")) {
                    m.setToBeQueued(Boolean.parseBoolean(pair[1]));
                } else if (pair[0].equals("port")) {
                    m.setSendingPort(Integer.parseInt(pair[1]));
                } else if (pair[0].equals("id")) {
                    id = Integer.parseInt(pair[1]);
                } else if (pair[0].equals("segment")) {
                    segment = Integer.parseInt(pair[1]);
                } else if (pair[0].equals("end")) {
                    end = true;
                }
            }
            InetSocketAddress remoteAddress = exchange.getRemoteAddress();
            String sendingIpAddress = remoteAddress.getAddress().getHostAddress();
            String sendingIpAddressAndPort = sendingIpAddress;
            if (m.getSendingPort() != 0) {
                sendingIpAddressAndPort += ":" + m.getSendingPort();
            }
            NextHop hop = NextHop.getNextHopByIpPort(sendingIpAddressAndPort);
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equals("POST")) {
                /*
                 * Read the posted content
                 */
                Headers headers = exchange.getRequestHeaders();
                int responseCode = HTTP_RESPONSE_OK;
                String contentType = headers.getFirst("Content-Type");
                if (contentType != null && contentType.compareTo(HTTP_CONTENT_ZIP) == 0) {
                    zipped = true;
                }
                boolean outOfSequence = false;
                int bufferSize = 50000; // Default buffer size if no Content-Length header is present.
                String contentLength = headers.getFirst("Content-Length");
                if (contentLength != null) {
                    bufferSize = Integer.parseInt(contentLength);
                } else if (hop != null && hop.isLengthRequired()) {
                    responseCode = HTTP_RESPONSE_LENGTH_REQUIRED;
                }
                InputStream input = exchange.getRequestBody();
                byte[] messageBytes = new byte[bufferSize];
                int messageLength = input.read(messageBytes);
                input.close();
                String md5Reported = headers.getFirst("Content-MD5");
                if (md5Reported != null) {
                    String md5Computed = computeMd5(messageBytes, 0, messageLength);
                    if (md5Reported.compareTo(md5Computed) != 0) {
                        responseCode = HTTP_RESPONSE_MD5_MISMATCH;
                        Logger.getLogger(HttpService.class.getName()).log(Level.FINE,
                                "MD5 reported as {0}, computed as {1}, length expected {2}, found {3}",
                                new Object[]{md5Reported, md5Computed, bufferSize, messageLength});
                    }
                } else if (hop != null && hop.isMd5Required()) {
                    responseCode = HTTP_RESPONSE_MD5_REQUIRED;
                }
                if (responseCode == HTTP_RESPONSE_OK) {
                    boolean completeMessage = true;
                    m.setSendingIpAddress(sendingIpAddress);
                    m.setSegmentCount(1);
                    m.setLongestSegmentLength(messageLength);
                    if (id > 0) {
                        completeMessage = false;
                        PartialMessage pm = null;
                        if (segment == 1) {
                            pm = new PartialMessage();
                            pm.id = id;
                            pm.segment = segment;
                            byte[] a = Arrays.copyOf(messageBytes, messageLength);
                            pm.messageSegments.add(a);
                            pm.length += messageLength;
                            partialMessages.put(sendingIpAddressAndPort, pm);
                        } else {
                            pm = partialMessages.get(sendingIpAddressAndPort);
                            if (pm != null) {
                                if (pm.id == id && ++pm.segment == segment) {
                                    byte[] a = Arrays.copyOf(messageBytes, messageLength);
                                    pm.messageSegments.add(a);
                                    pm.length += messageLength;
                                    if (end) {
                                        messageLength = pm.length;
                                        messageBytes = new byte[messageLength];
                                        int offset = 0;
                                        int longest = 0;
                                        for (byte[] seg : pm.messageSegments) {
                                            System.arraycopy(seg, 0, messageBytes, offset, seg.length);
                                            offset += seg.length;
                                            if (seg.length > longest) {
                                                longest = seg.length;
                                            }
                                        }
                                        m.setSegmentCount(pm.messageSegments.size());
                                        m.setLongestSegmentLength(longest);
                                        completeMessage = true;
                                        partialMessages.remove(sendingIpAddressAndPort);
                                    }
                                } else {
                                    if (pm.id != id) {
                                        Logger.getLogger(HttpService.class.getName()).log(Level.FINE,
                                                "Message id mismatch from {0}. Expected id {1}, found {2}, expected sequence {3}, found {4}",
                                                new Object[]{sendingIpAddressAndPort, pm.id, id, pm.segment, segment});
                                    } else {
                                        Logger.getLogger(HttpService.class.getName()).log(Level.FINE,
                                                "Message segment out of sequence from {0}, message id {1}, expected sequence {2}, found {3}",
                                                new Object[]{sendingIpAddressAndPort, id, pm.segment, segment});
                                    }
                                    outOfSequence = true;
                                    partialMessages.remove(sendingIpAddressAndPort);
                                }
                            } else {
                                Logger.getLogger(HttpService.class.getName()).log(Level.FINE,
                                        "Received segment from {0}, id {1}, segment {2} but no partial message previously stored.",
                                        new Object[]{sendingIpAddressAndPort, id, segment});
                            }
                        }
                    }
                    if (completeMessage) {
                        m.setSendingIpAddress(sendingIpAddress);
                        if (zipped) {
                            m.setCompressedXml(messageBytes);
                            m.setCompressedXmlLength(messageLength);
                        } else {
                            String xml = new String(messageBytes, 0, messageLength);
                            m.setXml(xml);
                        }
                        /*
                         * Process the message.
                         */
                        mediator.processReceivedMessage(m);
                    }
                }
                if (!outOfSequence) {
                    /*
                     * Acknoweldge to the sender that we received the message.
                     * (Don't acknowledge an out-of-sequence message).
                     */
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(responseCode, 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.close();
                }
                exchange.close();
            }
        }
    }

    /**
     * Computes the MD5 hash for an array of bytes.
     * 
     * @param bytes array of bytes for which the MD5 hash will be computed
     * @param offset starting offset for computing the MD5 hasn
     * @param length length for computing the MD5 hash
     * @return the MD5 hash in 32 characters hexadecimal.
     */
    String computeMd5(byte[] bytes, int offset, int length) {
        messageDigest.reset();
        messageDigest.update(bytes, offset, length);
        byte[] digest = messageDigest.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);
        // Now we need to zero pad it to get the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}
