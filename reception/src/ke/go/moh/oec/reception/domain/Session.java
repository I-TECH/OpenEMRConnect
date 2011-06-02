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
package ke.go.moh.oec.reception.domain;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class Session {

    public enum CLIENT_TYPE {

        ENROLLED,
        VISITOR,
        NEW,
        TRANSFER_IN
    }

    private CLIENT_TYPE clientType;
    private String clinicId;
    //private String firstName
    private List<ImagedFingerprint> fingerprintList;

    public Session() {
    }

    public Session(CLIENT_TYPE clientType) {
        this.clientType = clientType;
    }

    public CLIENT_TYPE getClientType() {
        return clientType;
    }

    public void setClientType(CLIENT_TYPE clientType) {
        this.clientType = clientType;
    }

    public List<ImagedFingerprint> getFingerprintList() {
        return fingerprintList;
    }

    public void addImagedFingerprint(ImagedFingerprint imagedFingerprint) {
        if (fingerprintList == null) {
            fingerprintList = new ArrayList<ImagedFingerprint>();
        }
        fingerprintList.add(imagedFingerprint);
    }
}
