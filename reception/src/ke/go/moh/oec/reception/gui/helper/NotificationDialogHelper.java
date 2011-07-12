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
package ke.go.moh.oec.reception.gui.helper;

import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Work;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.RequestDispatcher;
import ke.go.moh.oec.reception.data.Clinic;
import ke.go.moh.oec.reception.data.Notification;
import ke.go.moh.oec.reception.data.TargetServer;
import ke.go.moh.oec.reception.gui.NotificationDialog;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class NotificationDialogHelper {

    private final NotificationDialog notificationDialog;

    public NotificationDialogHelper(NotificationDialog notificationDialog) {
        this.notificationDialog = notificationDialog;
    }

    private Work createWork(Notification notification) {
        Work work = new Work();
        work.setSourceAddress(OECReception.applicationAddress());
        work.setNotificationId(notification.getPersonWrapper().getRequestReference());
        work.setReassignAddress(notification.getReassignAggress());
        return work;
    }

    public void flagWorkAsDone(Notification notification) {
        RequestDispatcher.dispatch(createWork(notification), RequestDispatcher.DispatchType.WORK_DONE, TargetServer.CDS);
    }

    public void reassignWork(Notification notification) {
        RequestDispatcher.dispatch(createWork(notification), RequestDispatcher.DispatchType.REASSIGN, TargetServer.CDS);
    }
    
    public List<Clinic> getClinicList() {
        List<Clinic> clinicList = new ArrayList<Clinic>();
        clinicList.add(new Clinic("Siaya CCC", "ke.go.moh.facility.14080.tb.ccc"));
        clinicList.add(new Clinic("Siaya XYZ", "ke.go.moh.facility.14080.tb.xyz"));
        clinicList.add(new Clinic("Siaya ABC", "ke.go.moh.facility.14080.tb.abc"));
        return clinicList;
    }
}
