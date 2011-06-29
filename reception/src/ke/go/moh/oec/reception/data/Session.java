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
import ke.go.moh.oec.Person;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersonWrapper;

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
    private static final String sessionReference = OECReception.generateSessionReference();
    private boolean clinicId = false;
    private boolean fingerprint = true;
    private boolean clinicName = false;
    private PersonWrapper personWrapper;
    private List<ImagedFingerprint> imagedFingerprintList = new ArrayList<ImagedFingerprint>();
    private ImagedFingerprint activeImagedFingerprint;
    private static final int MAX_FINGERPRINTS = 2;
    private RequestResult mpiRequestResult = null;
    private RequestResult lpiRequestResult = null;
    private List<Person> mpiPersonList = null;
    private List<Person> lpiPersonList = null;
    private boolean mpiShown = false;
    private boolean lpiShown = false;
    private PersonWrapper mpiMatchPersonWrapper = null;
    private PersonWrapper lpiMatchPersonWrapper = null;
    private boolean mpiIdentifierSearchDone = false;
    private List<String> rejectedLPIGuidList;
    private List<String> rejectedMPIGuidList;

    public Session(CLIENT_TYPE clientType) {
        this.clientType = clientType;
        clinicName = (clientType == CLIENT_TYPE.VISITOR || clientType == CLIENT_TYPE.TRANSFER_IN);
    }

    public ImagedFingerprint getActiveImagedFingerprint() {
        return activeImagedFingerprint;
    }

    public void setActiveImagedFingerprint(ImagedFingerprint activeImagedFingerprint) {
        this.activeImagedFingerprint = activeImagedFingerprint;
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

    public void setClinicName(boolean clinicName) {
        this.clinicName = clinicName;
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

    public PersonWrapper getLpiMatchPersonWrapper() {
        return lpiMatchPersonWrapper;
    }

    public void setLpiMatchPersonWrapper(PersonWrapper lpiMatchPersonWrapper) {
        this.lpiMatchPersonWrapper = lpiMatchPersonWrapper;
    }

    public List<Person> getLpiPersonList() {
        return lpiPersonList;
    }

    public void setLpiPersonList(List<Person> lpiPersonList) {
        this.lpiPersonList = lpiPersonList;
    }

    public RequestResult getLpiRequestResult() {
        if (lpiRequestResult == null) {
            lpiRequestResult = new RequestResult();
        }
        return lpiRequestResult;
    }

    public boolean isLpiShown() {
        return lpiShown;
    }

    public void setLpiShown(boolean lpiShown) {
        this.lpiShown = lpiShown;
    }

    public boolean isMpiIdentifierSearchDone() {
        return mpiIdentifierSearchDone;
    }

    public void setMpiIdentifierSearchDone(boolean mpiIdentifierSearchDone) {
        this.mpiIdentifierSearchDone = mpiIdentifierSearchDone;
    }

    public PersonWrapper getMpiMatchPersonWrapper() {
        return mpiMatchPersonWrapper;
    }

    public void setMpiMatchPersonWrapper(PersonWrapper mpiMatchPersonWrapper) {
        this.mpiMatchPersonWrapper = mpiMatchPersonWrapper;
    }

    public List<Person> getMpiPersonList() {
        return mpiPersonList;
    }

    public void setMpiPersonList(List<Person> mpiPersonList) {
        this.mpiPersonList = mpiPersonList;
    }

    public RequestResult getMpiRequestResult() {
        if (mpiRequestResult == null) {
            mpiRequestResult = new RequestResult();
        }
        return mpiRequestResult;
    }

    public boolean isMpiShown() {
        return mpiShown;
    }

    public void setMpiShown(boolean mpiShown) {
        this.mpiShown = mpiShown;
    }

    public PersonWrapper getPersonWrapper() {
        if (personWrapper == null) {
            personWrapper = new PersonWrapper(new Person());
        }
        return personWrapper;
    }

    public void setPersonWrapper(PersonWrapper personWrapper) {
        this.personWrapper = personWrapper;
    }

    public List<String> getRejectedLPIGuidList() {
        return rejectedLPIGuidList;
    }

    public void setRejectedLPIGuidList(List<String> rejectedLPIGuidList) {
        this.rejectedLPIGuidList = rejectedLPIGuidList;
    }

    public List<String> getRejectedMPIGuidList() {
        return rejectedMPIGuidList;
    }

    public void setRejectedMPIGuidList(List<String> rejectedMPIGuidList) {
        this.rejectedMPIGuidList = rejectedMPIGuidList;
    }

    public static String getRequestReference() {
        return sessionReference;
    }

    public boolean hasAllRequiredFingerprints() {
        boolean allFingerprintsTaken = false;
        if (!fingerprint) {
            allFingerprintsTaken = true;
        } else {
            if (imagedFingerprintList != null
                    && imagedFingerprintList.size() >= MAX_FINGERPRINTS) {
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
