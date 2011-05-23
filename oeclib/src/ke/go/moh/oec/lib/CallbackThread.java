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

import ke.go.moh.oec.IService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calls the user's registered callback routine for an unsolicited message.
 * This is done in a separate thread, so the HTTP response to a request
 * can be returned as soon as the server starts processing the request.
 * This prevents HTTP response timeouts from the application that made the request.
 * <p>
 * It also can prevent new HTTP requests from being rejected if the request thread pool
 * should run out because the threads are busy processing requests. This is unlikely,
 * but it is best to do any resource-intensive HTTP request processing
 * on a separate thread.
 * <p>
 * When the callback routine finishes the application-level request,
 * the application-level response will be sent back on a new HTTP request message.
 *
 * @author Jim Grace
 */
class CallbackThread implements Runnable {

    private Mediator mediator;          // Reference to the mediator who called us, so we can call back.
    private IService callbackObject;    // User object we need to call with the unsolicited message.
    private Message message;            // Unsolicited message.

    protected CallbackThread(Mediator mediator, IService callbackObject, Message message) {
        this.mediator = mediator;
        this.callbackObject = callbackObject;
        this.message = message;
    }

    /**
     * Calls the user callback routine with an unsolicited request.
     * Gets the response and (if a response was expected), sends it back to the
     * application who sent the original request.
     */
    public void run() {
        MessageType messageType = message.getMessageType();
        int requestTypeId = messageType.getRequestTypeId();
        Object responseData = callbackObject.getData(requestTypeId, message.getData());
        if (responseData != null) {
            MessageType responseMessageType = messageType.getResponseMessageType();
            if (responseMessageType != null) {
                Message response = new Message();
                response.setMessageType(responseMessageType);
                response.setData(responseData);
                response.setDestinationAddress(message.getSourceAddress());
                response.setDestinationName(message.getSourceName());
                response.setMessageId(message.getMessageId());
                Object responseToResponseData = mediator.sendData(response);
                if (responseToResponseData != null) {
                    /*
                     * We have delivered a request to the server, and it has given us back a response.
                     * We have sent the response back to the original requesting application.
                     * The requesting application has sent us back a message (a response to the response.)
                     * We did not expect this.
                     */
                    Logger.getLogger(CallbackThread.class.getName()).log(Level.WARNING,
                            "After returning a response to requestTypeId {0} from source ''{1}'', the source returned more data back to us!",
                            new Object[]{requestTypeId, message.getSourceAddress()});
                }
            } else {
                /*
                 * This appears to be an error in our MessageType registry. We have a MessageType
                 * entry for the message we received, but there is no response message type.
                 */
                Logger.getLogger(CallbackThread.class.getName()).log(Level.WARNING,
                        "Message type for requestTypeId {0} does not reference a return message type.",
                        requestTypeId);
            }
        }
    }
}
