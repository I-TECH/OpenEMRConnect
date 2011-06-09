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
package ke.go.moh.oec.reception.data;

import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Fingerprint;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class BasicRequestParameters implements RequestParameters {

    private String clinicId;
    private String clinicName;
    List<Fingerprint> fingerprintList;

    public BasicRequestParameters() {
        this.fingerprintList = new ArrayList<Fingerprint>();
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public List<Fingerprint> getFingerprintList() {
        return fingerprintList;
    }

    public void setFingerprintList(List<Fingerprint> fingerprintList) {
        this.fingerprintList = fingerprintList;
    }
}
