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
import java.util.concurrent.Executors;

class HttpService {

    /**
     * {@link Mediator} class instance to which we pass any received HTTP
     * requests.
     */
    private Mediator mediator = null;
    HttpServer server;

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
        boolean returnStatus = false;
        //   throw new UnsupportedOperationException();
        String url = "http://" + m.getIpAddressPort() + "/oecmessage?destination="
                + m.getDestinationAddress() + "&tobequeued=" + m.isToBeQueued() + "&hopcount=" + m.getHopCount();
        try {
            /*Code thats performing a task should be placed in the try catch statement especially in the try part*/
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            OutputStream output = connection.getOutputStream();
            byte[] compressedXml = m.getCompressedXml();
            int compressedXmlLength = m.getCompressedXmlLength();
            output.write(compressedXml, 0, compressedXmlLength);
            //output.write(xml);
            output.close();

            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            while (br.readLine() != null) {
                //content not required, just acknowlegment that message was received.
            }
            br.close();
            inputStreamReader.close();
            returnStatus = true;
        } catch (ConnectException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE,
                    "Can''t connect to {0} at {1}",
                    new Object[]{m.getDestinationAddress(), url});
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE,
                    "While sending to " + m.getDestinationAddress() + " at " + url, ex);
        } catch (IOException ex) {
            if (ex.getMessage().equals("Premature EOF")
                    || ex.getMessage().equals("Unexpected end of file from server")) {
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
     * Starts listening for HTTP messages.
     * <p>
     * For each message received, call mediator.processReceivedMessage()
     * @throws IOException
     */
    void start() throws IOException {
        //throw new UnsupportedOperationException("Not supported yet.");
        int port = Integer.parseInt(Mediator.getProperty("HTTPHandler.ListenPort"));
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
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair[0].equals("destination")) {
                    m.setDestinationAddress(pair[1]);
                } else if (pair[0].equals("hopcount")) {
                    m.setHopCount(Integer.parseInt(pair[1]));
                } else if (pair[0].equals("tobequeued")) {
                    m.setToBeQueued(Boolean.parseBoolean(pair[1]));
                }
            }
            if (requestMethod.equals("POST")) {
                /*
                 * Read the posted content
                 */
                Headers headers = exchange.getRequestHeaders();
                InputStream input = exchange.getRequestBody();
                byte[] compressedXml = new byte[30000];
                int compressedXmlLength = input.read(compressedXml);
                input.close();
                m.setCompressedXml(compressedXml);
                m.setCompressedXmlLength(compressedXmlLength);
                String sendingIpAddress = exchange.getRemoteAddress().getAddress().getHostAddress();
                m.setSendingIpAddress(sendingIpAddress);
                /*
                 * Process the message.
                 */
                mediator.processReceivedMessage(m);
                /*
                 * Acknoweldge to the sender that we received the message.
                 */
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.close();
                exchange.close();
            }
        }
    }
}
