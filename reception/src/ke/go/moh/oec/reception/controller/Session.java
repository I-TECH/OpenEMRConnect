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

import ke.go.moh.oec.reception.data.ExtendedSearchRequestParameters;
import ke.go.moh.oec.reception.data.BasicSearchRequestParameters;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;

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
    private BasicSearchRequestParameters basicSearchRequestParameters = new BasicSearchRequestParameters();
    private ExtendedSearchRequestParameters extendedSearchRequestParameters = new ExtendedSearchRequestParameters();
    private BufferedImage currentFingerprintImage = null;
    private boolean nonFingerprint = false;

    public Session() {
    }

    public Session(CLIENT_TYPE clientType) {
        this.clientType = clientType;
    }

    public BasicSearchRequestParameters getBasicSearchParameters() {
        return basicSearchRequestParameters;
    }

    public void setBasicSearchParameters(BasicSearchRequestParameters basicSearchParameters) {
        this.basicSearchRequestParameters = basicSearchParameters;
    }

    public CLIENT_TYPE getClientType() {
        return clientType;
    }

    public void setClientType(CLIENT_TYPE clientType) {
        this.clientType = clientType;
    }

    public ExtendedSearchRequestParameters getExtendedSearchParameters() {
        return extendedSearchRequestParameters;
    }

    public void setExtendedSearchParameters(ExtendedSearchRequestParameters extendedSearchParameters) {
        this.extendedSearchRequestParameters = extendedSearchParameters;
    }

    public BufferedImage getCurrentFingerprintImage() {
        return currentFingerprintImage;
    }

    public void addImagedFingerprint(ImagedFingerprint imagedFingerprint) {
        if (basicSearchRequestParameters.getFingerprintList() == null) {
            basicSearchRequestParameters.setFingerprintList(new ArrayList<Fingerprint>());
        }
        basicSearchRequestParameters.getFingerprintList().add(imagedFingerprint.getFingerprint());
        currentFingerprintImage = imagedFingerprint.getImage();
    }

    public boolean hasAllFingerprintsTaken() {
        boolean allFingerprintsTaken = false;
        if (nonFingerprint) {
            allFingerprintsTaken = true;
        } else {
            if (basicSearchRequestParameters.getFingerprintList() != null
                    && basicSearchRequestParameters.getFingerprintList().size() > 5) {
                allFingerprintsTaken = true;
            }
        }
        return allFingerprintsTaken;
    }

    public void setNonFingerprint(boolean nonFingerprint) {
        this.nonFingerprint = nonFingerprint;
    }

    public static boolean checkPersonListForFingerprintCandidates(List<Person> personList) {
        for (Person person : personList) {
            if (person.isFingerprintMatched()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPersonListForLinkedCandidates(List<Person> personList) {
        for (Person person : personList) {
            for (PersonIdentifier personIdentifier : person.getPersonIdentifierList()) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.masterPatientRegistryId) {
                    return true;
                }
            }
        }
        return false;
    }
}
