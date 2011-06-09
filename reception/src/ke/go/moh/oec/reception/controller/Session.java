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

import ke.go.moh.oec.reception.data.ExtendedRequestParameters;
import ke.go.moh.oec.reception.data.BasicRequestParameters;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.data.ComprehensiveRequestParameters;

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
    private static CLIENT_TYPE clientType;
    private static BasicRequestParameters basicRequestParameters = new BasicRequestParameters();
    private static ExtendedRequestParameters extendedRequestParameters = new ExtendedRequestParameters();
    private static ComprehensiveRequestParameters comprehensiveRequestParameters = new ComprehensiveRequestParameters();
    private static BufferedImage currentFingerprintImage = null;
    private static boolean nonFingerprint = false;

    public Session() {
    }

    public Session(CLIENT_TYPE clientType) {
        Session.clientType = clientType;
    }

    public static BasicRequestParameters getBasicRequestParameters() {
        return basicRequestParameters;
    }

    public static CLIENT_TYPE getClientType() {
        return clientType;
    }

    public static void setClientType(CLIENT_TYPE clientType) {
        Session.clientType = clientType;
    }

    public static ComprehensiveRequestParameters getComprehensiveRequestParameters() {
        return comprehensiveRequestParameters;
    }

    public static ExtendedRequestParameters getExtendedRequestParameters() {
        return extendedRequestParameters;
    }

    public static boolean isNonFingerprint() {
        return nonFingerprint;
    }

    public void addImagedFingerprint(ImagedFingerprint imagedFingerprint) {
        if (basicRequestParameters.getFingerprintList() == null) {
            basicRequestParameters.setFingerprintList(new ArrayList<Fingerprint>());
        }
        basicRequestParameters.getFingerprintList().add(imagedFingerprint.getFingerprint());
        currentFingerprintImage = imagedFingerprint.getImage();
    }

    public boolean hasAllFingerprintsTaken() {
        boolean allFingerprintsTaken = false;
        if (nonFingerprint) {
            allFingerprintsTaken = true;
        } else {
            if (basicRequestParameters.getFingerprintList() != null
                    && basicRequestParameters.getFingerprintList().size() > 5) {
                allFingerprintsTaken = true;
            }
        }
        return allFingerprintsTaken;
    }

    public void setNonFingerprint(boolean nonFingerprint) {
        Session.nonFingerprint = nonFingerprint;
    }

    public static BufferedImage getCurrentFingerprintImage() {
        return currentFingerprintImage;
    }

    public static void setCurrentFingerprintImage(BufferedImage currentFingerprintImage) {
        Session.currentFingerprintImage = currentFingerprintImage;
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

    public static PersonIdentifier.Type deducePersonIdentifierType(String personIdentifier) {
        PersonIdentifier.Type clinicIdType = PersonIdentifier.Type.indeterminate;
        if (personIdentifier != null && !personIdentifier.isEmpty()) {
            if (personIdentifier.contains("-") && !personIdentifier.contains("/")) {
                if ((personIdentifier.split("-").length == 2 && personIdentifier.split("-")[0].length() == 5)
                        && (personIdentifier.split("-").length == 2 && personIdentifier.split("-")[1].length() == 5)) {
                    clinicIdType = PersonIdentifier.Type.cccUniqueId;
                } else if (personIdentifier.split("-").length == 4) {
                    clinicIdType = PersonIdentifier.Type.kisumuHdssId;
                }
            } else if (personIdentifier.contains("/") && !personIdentifier.contains("-")) {
                if ((personIdentifier.split("/").length == 2 && personIdentifier.split("/")[0].length() == 5)
                        && (personIdentifier.split("/").length == 2 && personIdentifier.split("/")[1].length() == 4)) {
                    clinicIdType = PersonIdentifier.Type.cccLocalId;
                }
            }
        }
        return clinicIdType;
    }

    public static boolean validateClinicId(String clinicId) {
        return deducePersonIdentifierType(clinicId) != PersonIdentifier.Type.indeterminate;
    }

    public static String tweakLocalClinicId(String clinicId) {
        if (Session.getClientType() == Session.CLIENT_TYPE.ENROLLED) {
            clinicId = Mediator.getProperty("Instance.FacilityCode") + "-" + clinicId;
        }
        return clinicId;
    }
}
