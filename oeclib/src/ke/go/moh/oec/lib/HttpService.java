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
    protected HttpService(Mediator mediator) {
        this.mediator = mediator;
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
        boolean returnStatus = false;
        //   throw new UnsupportedOperationException();
        String url = "http://" + ipAddressPort + "/oecmessage?destination="
                + destination + "&tobequeued=" + toBeQueued + "&hopcount=" + hopCount;

        try {
            /*Code thats performing a task should be placed in the try catch statement especially in the try part*/
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            Writer output = new OutputStreamWriter(connection.getOutputStream());
            output.write(message);
            output.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (br.readLine() != null) {
                //content not required, just acknowlegment that message was received.
            }
            returnStatus = true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
//            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
//            There was some transmission error we return false.
        }
        return returnStatus;
    }

    /**
     * Starts listening for HTTP messages.
     * <p>
     * For each message received, call mediator.processReceivedMessage()
     * @throws IOException
     */
    protected void start() throws IOException {
        //throw new UnsupportedOperationException("Not supported yet.");
        int port = Integer.parseInt(Mediator.getProperty("HTTPHandler.ListenPort"));
        InetSocketAddress addr = new InetSocketAddress(port);
        server = HttpServer.create(addr, 0);
        server.createContext("/oecmessage", (HttpHandler) new Handler(mediator));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    /**
     * Stops listening for HTTP messages.
     */
    protected void stop() {
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
            String destination = null;
            boolean toBeQueued = false;
            int hopCount = 0;

            //mediator.processReceivedMessage()
            String requestMethod = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair[0].equals("destination")) {
                    destination = pair[1];
                } else if (pair[0].equals("hopcount")) {
                    hopCount = Integer.parseInt(pair[1]);
                } else if (pair[0].equals("tobequeued")) {
                    toBeQueued = Boolean.parseBoolean(pair[1]);
                }
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String st;
            String request = "";
            while ((st = br.readLine()) != null) {
                request = request + st + "\n";
            }
            mediator.processReceivedMessage(request, destination, hopCount, toBeQueued);
            //The two lines below can be uncommented and used for debugging to print out test data.
            //            System.out.println("Request Body: ");
            //            System.out.println(request);
            /***********************/
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = exchange.getResponseBody();
            responseBody.close();
        }
    }
}
