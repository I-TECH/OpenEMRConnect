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
    protected boolean send(String message, String ipAddressPort, String destination, boolean toBeQueued, int hopCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts listening for HTTP messages.
     * <p>
     * For each message received, call mediator.processReceivedMessage()
     */
    protected void start() {
    //    throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Stops listening for HTTP messages.
     */
    protected void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
