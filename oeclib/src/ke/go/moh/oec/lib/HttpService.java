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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.Writer;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

class HttpService {

    /**
     * {@link Mediator} class instance to which we pass any received HTTP
     * requests.
     */
    private Mediator mediator = null;

    /**
     * Constructor to set {@link Mediator} callback object
     *
     * @param mediator {@link Mediator} callback object for listener
     */
    protected HttpService(Mediator mediator) {
        this.mediator = mediator;
    }

    private HttpService() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Sends a HTTP message.
     *
     * @param message contents of message to send
     * @param ipAddressPort IP address:port to which to send the message
     * @param destination ultimate destination of the message (encode in URL)
     * @param toBeQueued is the message to be queued for store-and-forward? (encode in URL)
     * @param hopCount is the count of hops to detect routing loops (encode in URL)
     * @return true if message was sent and HTTP response received, otherwise false
     */
    protected boolean send(String message, String ipAddressPort, String destination, boolean toBeQueued, int hopCount) throws MalformedURLException, IOException {
        boolean success = false;
        //   throw new UnsupportedOperationException();
        /**
         * Starts listening for HTTP messages.
         * <p>
         * For each message received, call mediator.processReceivedMessage()
         */
        try {
            /*Code thats performing a task should be placed in the try catch statement especially in the try part*/
            new HttpService().send(message, ipAddressPort, destination, toBeQueued, hopCount);
            URLConnection connection = new URL(ipAddressPort).openConnection();
            connection.setDoOutput(true);
            Writer output = new OutputStreamWriter(connection.getOutputStream());
            output.write(message);
            output.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str = "";
            String s;
            while ((s = br.readLine()) != null) {
                str = str + s + "\n";
            }
            success = true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    /**
     *
     * @param port
     * @throws IOException
     */
    protected void start() throws IOException {
        //throw new UnsupportedOperationException("Not supported yet.");
        int port = 0;
        InetSocketAddress addr = new InetSocketAddress(port);
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/", (HttpHandler) new Handler(mediator));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    /**
     * Stops listening for HTTP messages.
     */
    protected void stop() throws IOException {
        int port = 0;
        // throw new UnsupportedOperationException("Not supported yet.");
        InetSocketAddress addr = new InetSocketAddress(port);
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/", new Handler(mediator));
        server.setExecutor(Executors.newCachedThreadPool());
        server.stop(port);
    }

    private class Handler implements HttpHandler {

        private Mediator mediator = null;

        protected Handler(Mediator mediator) {
            this.mediator = mediator;
        }

        public void handle(HttpExchange exchange) throws IOException {

            //mediator.processReceivedMessage()
            String requestMethod = exchange.getRequestMethod();
            //Check for request type is it "GET" or "POST"
            if (requestMethod.equalsIgnoreCase("GET")) {
                Headers responseHeaders = exchange.getResponseHeaders();

                responseHeaders.set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                /*Byte Stream is the lowest level of data representation.
                First we use the "exchange.getRequestBody" method to obtain the inputstream which is a byte stream
                format of the data. We then use InputStreamReader reader to convert the byte inputstream byte code into
                character streams. The InputStreamReader is then wrapped around a BufferedReader to enable reading line by line
                 */
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                String s1 = "";
                while (true) {
                    String s2 = br.readLine();
                    if (s2 == null) {
                        break;
                    }
                    s1 = s1 + s2;
                }
                System.out.println("request body = " + s1);

                OutputStream responseBody = exchange.getResponseBody();
                Headers requestHeaders = exchange.getRequestHeaders();
                Set<String> keySet = requestHeaders.keySet();
                Iterator<String> iter = keySet.iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
                    List values = requestHeaders.get(key);
                    String s = key + " = " + values.toString() + "\n";
                    System.out.println(s);
                    responseBody.write(s.getBytes());
                }
                responseBody.close();
            } else if (requestMethod.equalsIgnoreCase("POST")) {
                Headers responseHeaders = exchange.getResponseHeaders();

                responseHeaders.set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                OutputStream responseBody = exchange.getResponseBody();
                /*Byte Stream is the lowest level of data representation.
                First we use the "exchange.getRequestBody" method to obtain the inputstream which is a byte stream
                format of the data. We then use InputStreamReader reader to convert the byte inputstream byte code into
                character streams. The InputStreamReader is then wrapped around a BufferedReader to enable reading line by line
                 */
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                String st;
                String request = "";
                while ((st = br.readLine()) != null) {
                    request = request + st + "\n";
                }
                System.out.println("Request Body: ");
                System.out.println(request);

                /***********************/
                Headers requestHeaders = exchange.getRequestHeaders();
                Set<String> keySet = requestHeaders.keySet();
                Iterator<String> iter = keySet.iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
                    List values = requestHeaders.get(key);
                    String s = key + " = " + values.toString() + "\n";
                    System.out.println(s);
                    responseBody.write(s.getBytes());
                }
                responseBody.close();
            }
        }
    }
}

