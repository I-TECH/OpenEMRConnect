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

    public enum ClientType {

        ENROLLED,
        VISITOR,
        NEW,
        TRANSFER_IN,
        UNSPECIFIED;

        @Override
        public String toString() {
            if (this == ENROLLED) {
                return "Enrolled";
            } else if (this == VISITOR) {
                return "Visitor";
            } else if (this == NEW) {
                return "New";
            } else if (this == TRANSFER_IN) {
                return "Transfer in";
            } else {
                return "Unspecified";
            }
        }
        
    }
    private ClientType clientType;
    private final String reference;
    private PersonWrapper searchPersonWrapper;
    private RequestResult mpiRequestResult;
    private RequestResult lpiRequestResult;
    private PersonWrapper mpiMatchPersonWrapper;
    private PersonWrapper lpiMatchPersonWrapper;
    private List<Person> rejectedMPICandidateList;
    private List<Person> rejectedLPICandidateList;
    private List<ImagedFingerprint> imagedFingerprintList;
    private ImagedFingerprint activeImagedFingerprint;
    private boolean clinicId;
    private boolean fingerprint;
    private boolean clinicName;
    private boolean mpiResultDisplayed;
    private boolean lpiResultDisplayed;
    private boolean mpiIdentifierSearchDone;
    private boolean lastResortSearchDone;

    public Session(ClientType clientType) {
        this.clientType = clientType;
        this.reference = OECReception.generateSessionReference();
        searchPersonWrapper = new PersonWrapper(new Person());
        mpiRequestResult = new RequestResult();
        lpiRequestResult = new RequestResult();
        fingerprint = true;
        imagedFingerprintList = new ArrayList<ImagedFingerprint>();
        clinicName = (clientType == ClientType.VISITOR || clientType == ClientType.TRANSFER_IN);
    }

    public void changeSessionClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public ImagedFingerprint getActiveImagedFingerprint() {
        return activeImagedFingerprint;
    }

    public void setActiveImagedFingerprint(ImagedFingerprint activeImagedFingerprint) {
        this.activeImagedFingerprint = activeImagedFingerprint;
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

    public boolean isLastResortSearchDone() {
        return lastResortSearchDone;
    }

    public void setLastResortSearchDone(boolean lastResortSearchDone) {
        this.lastResortSearchDone = lastResortSearchDone;
    }

    public PersonWrapper getLpiMatchPersonWrapper() {
        return lpiMatchPersonWrapper;
    }

    public void setLpiMatchPersonWrapper(PersonWrapper lpiMatchPersonWrapper) {
        this.lpiMatchPersonWrapper = lpiMatchPersonWrapper;
    }

    public RequestResult getLpiRequestResult() {
        return lpiRequestResult;
    }

    public boolean isLpiResultDisplayed() {
        return lpiResultDisplayed;
    }

    public void setLpiResultDisplayed(boolean lpiShown) {
        this.lpiResultDisplayed = lpiShown;
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

    public RequestResult getMpiRequestResult() {
        return mpiRequestResult;
    }

    public boolean isMpiResultDisplayed() {
        return mpiResultDisplayed;
    }

    public void setMpiResultDisplayed(boolean mpiShown) {
        this.mpiResultDisplayed = mpiShown;
    }

    public PersonWrapper getSearchPersonWrapper() {
        return searchPersonWrapper;
    }

    public void setSearchPersonWrapper(PersonWrapper personWrapper) {
        if (personWrapper == null) {
            this.searchPersonWrapper = new PersonWrapper(new Person());
        } else {
            this.searchPersonWrapper = personWrapper;
        }
    }

    public List<Person> getRejectedLPICandidateList() {
        return rejectedLPICandidateList;
    }

    public void setRejectedLPICandidateList(List<Person> rejectedLPICandidateList) {
        this.rejectedLPICandidateList = rejectedLPICandidateList;
    }

    public List<Person> getRejectedMPICandidateList() {
        return rejectedMPICandidateList;
    }

    public void setRejectedMPICandidateList(List<Person> rejectedMPICandidateList) {
        this.rejectedMPICandidateList = rejectedMPICandidateList;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public String getReference() {
        return reference;
    }

    //TODO: Disallow getAnyUnsentFingerprints() from ever being necessary by ensuing that each
    //fingerprint takes is sent to the indices
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
