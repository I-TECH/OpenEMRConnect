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

/**
 * Offers a store-and-forward queueing facility to send messages to a
 * network destination. If the message cannot be sent immediately, it is
 * queued for later sending. The Queue Manager periodically tries to
 * send queued messages to their respective destinations.
 *
 * @author Jim Grace
 */
class QueueManager implements Runnable {

    /** HTTP Handler object we can call to send HTTP messages. */
    private HttpService httpService;

    /** The thread under which the polling runs for the queue manager. */
    private Thread queueManagerThread;

    /**
     * Constructor to set <code>HttpService</code> object for sending messages
     *
     * @param httpService <code>HttpService</code> object for sending messages
     */
    protected QueueManager(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Start the queue manager (call before first use).
     * <p>
     * This code is a placeholder for now, and is expected to change.
     * Instead of using a single thread for all destinations, we may
     * use a thread for each distinct IP address in the queue (and
     * no threads if the queue is empty.)
     */
    protected void start() {
        /*
        if (queueManagerThread == null) {
            queueManagerThread = new Thread(new QueueManager(httpService));
            queueManagerThread.start();
        }
         */
    }

    /**
     * Stop the queue manager (call after last use).
     */
    protected void stop() {
        // Stop the thread.
    }

    /**
     * Queues a message for sending. The message will be immediately added to
     * the queue on disk for safe-keeping, and then this method returns.
     * If there is network connectivity to the IP address, it will then be
     * sent immediately. If it fails to send, it will be periodically retried
     * until it succeeds.
     *
     * @param message packed XML message to send.
     * @param IPAddressPort IP Address/port to which to send the message.
     * @param destination ultimate message destination (to encode in URL.)
     * @param hopCount current hop count (to encode in URL.)
     */
    protected void enqueue(String message, String IPAddressPort, String destination, int hopCount) {
        try {
            httpService.send(message, IPAddressPort, destination, true, hopCount);
        } catch (MalformedURLException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Run the Queue Manager thread for processing queue entries.
     * QueueManager implements Runnable. The run method, specified in Runnable, is used
     * to create a thread, starting the thread causes the Queue Manager's run method to be
     * called in that separately executing thread.
     */
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
