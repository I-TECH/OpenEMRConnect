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
package ke.go.moh.oec.reception.controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.IService;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;

/**
 * This class runs on a separate thread to listen to incoming (unsolicited) CDS
 * requests. Specifically, it handles the NOTIFY_PERSON_CHANGED message type. 
 * See {@link MessageTypeRegistry} in oeclib. Because it is an OEC server, 
 * it implements the interface IService.
 * 
 * @author Gitahi Ng'ang'a
 */
public class NotificationListener implements IService, Runnable {

    /**
     * Whether or not this instance has already been registered with the Mediator.
     * See {@link Mediator}
     */
    private boolean registeredWithMediator = false;
    /**
     * The {@link NotificationManager} object to handle incoming notifications.
     */
    private final NotificationManager notificationManager;

    /**
     * Construct a new instance of NotificationListener
     * 
     * @param notificationManager {@link NotificationManager} to handle incoming notifications.
     */
    public NotificationListener(NotificationManager notificationManager) {
        Thread.currentThread().setName("Notification Listener");
        this.notificationManager = notificationManager;
    }

    /**
     * Call back method to be called by the Mediator when new requests come in from the
     * CDS.
     * 
     * @param notificationManager {@link NotificationManager} to handle incoming notifications.
     */
    public Object getData(int requestTypeId, Object requestData) {
        if (requestData != null) {
            if (requestTypeId == RequestTypeId.NOTIFY_PERSON_CHANGED) {
                PersonRequest personRequest = (PersonRequest) requestData;
                Person person = personRequest.getPerson();
                PersonWrapper personWrapper = new PersonWrapper(person);
                personWrapper.setReference(personRequest.getRequestReference());
                notificationManager.addNotifications(personWrapper.getNotificationList());
            } else {
                Logger.getLogger(NotificationListener.class.getName()).log(Level.SEVERE,
                        "getData() called with unepxected requestTypeId {0}", requestTypeId);
            }
        } else {
            Logger.getLogger(NotificationListener.class.getName()).log(Level.SEVERE,
                    "getData() called with null requestData");
        }
        return null;
    }

    /**
     * Registers this object with the Mediator to be called back to handle incoming
     * requests and then sleeps.
     */
    public void run() {
        if (!registeredWithMediator) {
            Mediator.registerCallback(this);
            registeredWithMediator = true;
        }
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                Logger.getLogger(NotificationListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
