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
package ke.go.moh.oec;

/**
 * Contains pending work or alerts message to be processed when the reception program starts ,
 * after the  work or alerts are processed message and if the
 * work has to be Reassign to  Clinical Document Store.
 * getWork , getWorkDone, reassign
 *
 * @author pchemutai
 */
public class Work {

    /** Notification Id parameter to be passed to the getData */
    private String notificationId;
    /** Reassign  Id parameter to be passed when transferring data to Clinical Document Store **/
    private String reassignAddress;
    /** Address of the message source application */
    private String sourceAddress;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getReassignAddress() {
        return reassignAddress;
    }

    public void setReassignAddress(String reassignAddress) {
        this.reassignAddress = reassignAddress;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
}
