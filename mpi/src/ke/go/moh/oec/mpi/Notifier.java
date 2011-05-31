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
     * who are regular clients at those clinics.
     * 
     * @param p updated person
     */
    public static void notify(Person p) {
        if (p.getPreviousVillageName() != null
                || p.getExpectedDeliveryDate() != null
                || p.getPregnancyEndDate() != null
                || p.getAliveStatus() == Person.AliveStatus.no) {
            Visit v = p.getLastRegularVisit();
            if (v != null && v.getAddress() != null) {
                PersonRequest pr = new PersonRequest();
                pr.setPerson(p);
                pr.setDestinationAddress(v.getAddress());
                pr.setDestinationName("MOH Facility"); // We really don't know the name.
                Mediator mediator = Main.getMediator();
                mediator.getData(RequestTypeId.NOTIFY_PERSON_CHANGED, pr);
            }
        }
    }
}
