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

/**
 * [Gitau to provide a description.]
 *
 * @author John Gitau
 */
/*
 * Class to send and receive HTTP messages.
 */
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

class HttpService {

    /**
     * {@link Mediator} class instance to which we pass any received HTTP
     * requests.
     */
    private Mediator mediator = null;
    private int id = 0;
    private int port = 0;
    HttpServer server;
    Map<String, Date> unreachableIpPorts = new HashMap<String, Date>();
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private class PartialMessage {

        private int id;
        private int segment = 0;
        private int length = 0;
        private List<byte[]> messageSegments = new ArrayList<byte[]>();
    }
    private Map<String, PartialMessage> partialMessages = new HashMap<String, PartialMessage>();

    /**
     * Constructor to set {@link Mediator} callback object
     *
     * @param mediator {@link Mediator} callback object for listener
     */
    HttpService(Mediator mediator) {
        this.mediator = mediator;
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
            nextHop = NextHop.getNextHop(destinationAddress);
            m.setNextHop(nextHop);
        }
        String ipAddressPort = nextHop.getIpAddressPort();
        int maxSize = nextHop.getMaxSize();
        String url = "http://" + ipAddressPort + "/oecmessage?destination="
                + destinationAddress + "&tobequeued=" + m.isToBeQueued() + "&hopcount=" + m.getHopCount();
        try {
            /*Code thats performing a task should be placed in the try catch statement especially in the try part*/
            byte[] compressedXml = m.getCompressedXml();
            int compressedXmlLength = m.getCompressedXmlLength();
            int sent = 0;
            int toSend = compressedXmlLength;
            if (compressedXmlLength > maxSize) { // If we're going to split this message:
                // Append the next message ID onto the URL.
                url += "&port=" + port + "&id=" + ++id + "&segment=";
            }
            int segment = 0;
            while (sent < compressedXmlLength) {
                String thisUrl = url;
                if (compressedXmlLength > maxSize) {
                    thisUrl = url + Integer.toString(++segment);
                    if (compressedXmlLength - sent > maxSize) {
                        toSend = maxSize;
                    } else {
                        toSend = compressedXmlLength - sent;
                        thisUrl = thisUrl + "&end";
                    }
                }
                URLConnection connection = new URL(thisUrl).openConnection();
                connection.setDoOutput(true);
                OutputStream output = connection.getOutputStream();

                output.write(compressedXml, sent, toSend);
                sent = sent + toSend;
                output.close();

                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(inputStreamReader);
                while (br.readLine() != null) {
                    //content not required, just acknowlegment that message was received.
                }
                br.close();
                inputStreamReader.close();
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
            String requestMethod = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            int port = 0;
            int id = 0;
            int segment = 0;
            boolean end = false;
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
                    port = Integer.parseInt(pair[1]);
                } else if (pair[0].equals("id")) {
                    id = Integer.parseInt(pair[1]);
                } else if (pair[0].equals("segment")) {
                    segment = Integer.parseInt(pair[1]);
                } else if (pair[0].equals("end")) {
                    end = true;
                }
            }
            if (requestMethod.equals("POST")) {
                /*
                 * Read the posted content
                 */
                Headers headers = exchange.getRequestHeaders();
                InputStream input = exchange.getRequestBody();
                byte[] compressedXml = new byte[50000];
                int compressedXmlLength = input.read(compressedXml);
                input.close();
                InetSocketAddress remoteAddress = exchange.getRemoteAddress();
                String sendingIpAddress = remoteAddress.getAddress().getHostAddress();
                boolean completeMessage = true;
                boolean outOfSequence = false;
                m.setSegmentCount(1);
                m.setLongestSegmentLength(compressedXmlLength);
                if (id > 0) {
                    completeMessage = false;
                    String sendingIpAddressAndPort = sendingIpAddress + ":" + port;
                    PartialMessage pm = null;
                    if (segment == 1) {
                        pm = new PartialMessage();
                        pm.id = id;
                        pm.segment = segment;
                        byte[] a = Arrays.copyOf(compressedXml, compressedXmlLength);
                        pm.messageSegments.add(a);
                        pm.length += compressedXmlLength;
                        partialMessages.put(sendingIpAddressAndPort, pm);
                    } else {
                        pm = partialMessages.get(sendingIpAddressAndPort);
                        if (pm != null) {
                            if (pm.id == id && ++pm.segment == segment) {
                                byte[] a = Arrays.copyOf(compressedXml, compressedXmlLength);
                                pm.messageSegments.add(a);
                                pm.length += compressedXmlLength;
                                if (end) {
                                    compressedXmlLength = pm.length;
                                    compressedXml = new byte[compressedXmlLength];
                                    int offset = 0;
                                    int longest = 0;
                                    for (byte[] seg : pm.messageSegments) {
                                        System.arraycopy(seg, 0, compressedXml, offset, seg.length);
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
                                outOfSequence = true;
                                partialMessages.remove(sendingIpAddressAndPort);
                            }
                        }
                    }
                }
                if (completeMessage) {
                    m.setSendingIpAddress(sendingIpAddress);
                    m.setCompressedXml(compressedXml);
                    m.setCompressedXmlLength(compressedXmlLength);
                    /*
                     * Process the message.
                     */
                    mediator.processReceivedMessage(m);
                }
                if (!outOfSequence) {
                    /*
                     * Acknoweldge to the sender that we received the message.
                     * (Don't acknowledge an out-of-sequence message).
                     */
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.close();
                }
                exchange.close();
            }
        }
    }
}
