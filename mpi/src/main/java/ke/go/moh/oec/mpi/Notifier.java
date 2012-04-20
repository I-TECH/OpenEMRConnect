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
package ke.go.moh.oec.mpi;

import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.lib.Mediator;

/**
 * This class is responsible for notifying clinics of certain changes in person
 * status for those who are regular clients at those clinics.
 *
 * @author Jim Grace
 */
public class Notifier {

    /**
     * Notifies clinics of certain changes in person status for those
     * who are regular clients at those clinics. Notification is sent only
     * if the consent form has been signed.
     * <p>
     * The notification is sent to the Clinical Document Store at the same
     * facility as the reception system where the person was last seen.
     * For example, say the patient was last seen at a reception system
     * with application address ke.go.moh.facility.14080.tb.reception.
     * The notification will be sent to the Clinical Document Store at address
     * ke.go.moh.facility.14080.cds.
     * <p>
     * If there is more than one reason to notify about a patient update,
     * then send a different notification message for each reason.
     * 
     * @param p updated person
     */
    public static void notify(Person p) {
        if (p.getConsentSigned() == Person.ConsentSigned.yes) {
            Visit v = p.getLastRegularVisit();
            if (v != null) {
                String address = v.getAddress();
                if (address != null) {
                    if (p.getLastMoveDate() != null) {
                        Person per = p.clone();
                        // Remove any other possible alert data, so this alert is for one purpose only:
                        per.setExpectedDeliveryDate(null);
                        per.setPregnancyEndDate(null);
                        per.setDeathdate(null);
                        // Send the alert (notification):
                        sendNotify(per, address);
                        // Remove this data from our in-memory person, so we don't keep sending the same alert next time.
                        p.setLastMoveDate(null);
                    }
                    if (p.getExpectedDeliveryDate() != null) {
                        Person per = p.clone();
                        // Remove any other possible alert data, so this alert is for one purpose only:
                        per.setLastMoveDate(null);
                        per.setPregnancyEndDate(null);
                        per.setDeathdate(null);
                        // Send the alert (notification):
                        sendNotify(per, address);
                        // Remove this data from our in-memory person, so we don't keep sending the same alert next time.
                        p.setExpectedDeliveryDate(null);
                    }
                    if (p.getPregnancyEndDate() != null) {
                        Person per = p.clone();
                        // Remove any other possible alert data, so this alert is for one purpose only:
                        per.setLastMoveDate(null);
                        per.setExpectedDeliveryDate(null);
                        per.setDeathdate(null);
                        // Send the alert (notification):
                        sendNotify(per, address);
                        // Remove this data from our in-memory person, so we don't keep sending the same alert next time.
                        p.setPregnancyEndDate(null);
                    }
                    if (p.getDeathdate() != null) {
                        Person per = p.clone();
                        // Remove any other possible alert data, so this alert is for one purpose only:
                        per.setLastMoveDate(null);
                        per.setExpectedDeliveryDate(null);
                        per.setPregnancyEndDate(null);
                        // Send the alert (notification):
                        sendNotify(per, address);
                        // Remove this data from our in-memory person, so we don't keep sending the same alert next time.
                        p.setDeathdate(null);
                    }
                }
            }
        }
    }

    /**
     * Sends a notify message to the appropriate Clinical Document Store (CDS)
     * 
     * @param per Person including the field to notify.
     * @param address Address of the reception system where the person was last seen for a regular visit.
     */
    private static void sendNotify(Person per, String address) {
        String facility = address.substring(0, address.indexOf(".facility.") + 15);
        String destination = facility + ".cds";
        PersonRequest pr = new PersonRequest();
        pr.setPerson(per);
        pr.setDestinationAddress(destination);
        pr.setDestinationName("Clinical Document Store");
        Mediator mediator = Main.getMediator();
        mediator.getData(RequestTypeId.NOTIFY_PERSON_CHANGED, pr);
    }
}
