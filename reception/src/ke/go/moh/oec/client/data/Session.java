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
package ke.go.moh.oec.client.data;

import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.client.controller.PersonWrapper;

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
    private final CLIENT_TYPE clientType;
    private boolean clinicId = false;
    private boolean fingerprint = true;
    private boolean clinicName = false;
    private PersonWrapper personWrapper;
    private PersonWrapper comprehensivePersonWrapper;
    private List<ImagedFingerprint> imagedFingerprintList = new ArrayList<ImagedFingerprint>();
    private ImagedFingerprint activeFingerprint;
    private int maxFingerprints = 6;

    public Session(CLIENT_TYPE clientType) {
        this.clientType = clientType;
        clinicName = (clientType == CLIENT_TYPE.VISITOR || clientType == CLIENT_TYPE.TRANSFER_IN);
    }

    public Session(CLIENT_TYPE clientType, int maxFingerprints) {
        this(clientType);
        this.maxFingerprints = maxFingerprints;
    }

    public ImagedFingerprint getActiveImagedFingerprint() {
        return activeFingerprint;
    }

    public void setActiveFingerprint(ImagedFingerprint activeFingerprint) {
        this.activeFingerprint = activeFingerprint;
    }

    public PersonWrapper getPersonWrapper() {
        return personWrapper;
    }

    public void setPersonWrapper(PersonWrapper personWrapper) {
        this.personWrapper = personWrapper;
    }

    public CLIENT_TYPE getClientType() {
        return clientType;
    }

    public boolean isClinicId() {
        return clinicId;
    }

    public void setClinicId(boolean clinicId) {
        this.clinicId = clinicId;
    }

    public boolean isClinicName() {
        return clinicName;
    }

    public PersonWrapper getComprehensivePersonWrapper() {
        return comprehensivePersonWrapper;
    }

    public void setComprehensivePersonWrapper(PersonWrapper comprehensivePersonWrapper) {
        this.comprehensivePersonWrapper = comprehensivePersonWrapper;
    }

    public boolean isFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(boolean fingerprint) {
        this.fingerprint = fingerprint;
    }

    public List<ImagedFingerprint> getImagedFingerprintList() {
        return imagedFingerprintList;
    }

    public void setImagedFingerprintList(List<ImagedFingerprint> imagedFingerprintList) {
        this.imagedFingerprintList = imagedFingerprintList;
    }

    public int getMaxFingerprints() {
        return maxFingerprints;
    }

    public void setMaxFingerprints(int maxFingerprints) {
        this.maxFingerprints = maxFingerprints;
    }

    public boolean hasAllFingerprintsTaken() {
        boolean allFingerprintsTaken = false;
        if (!fingerprint) {
            allFingerprintsTaken = true;
        } else {
            if (imagedFingerprintList != null
                    && imagedFingerprintList.size() > maxFingerprints) {
                allFingerprintsTaken = true;
            }
        }
        return allFingerprintsTaken;
    }

    public List<ImagedFingerprint> getAnyUnsentFingerprints() {
        List<ImagedFingerprint> unsentFingerprintList = new ArrayList<ImagedFingerprint>();
        for (ImagedFingerprint imagedFingerprint : imagedFingerprintList) {
            if (!imagedFingerprint.isSent()) {
                unsentFingerprintList.add(imagedFingerprint);
            }
        }
        return unsentFingerprintList;
    }
}
