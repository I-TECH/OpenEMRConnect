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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.reception.data.ExtendedRequestParameters;
import ke.go.moh.oec.reception.data.BasicRequestParameters;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;

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
    private BasicRequestParameters basicRequestParameters = new BasicRequestParameters();
    private ExtendedRequestParameters extendedRequestParameters = new ExtendedRequestParameters();
    private static List<ImagedFingerprint> imagedFingerprintList = new ArrayList<ImagedFingerprint>();
    private ImagedFingerprint currentImagedFingerprint = null;
    private boolean nonFingerprint = false;
    private boolean knownClinicId = false;
    private static BufferedImage missingFingerprintImage = null;
    private static BufferedImage refusedFingerprintImage = null;

    public Session(CLIENT_TYPE clientType) {
        Session.clientType = clientType;
        if (Session.clientType == CLIENT_TYPE.NEW) {
            this.knownClinicId = false;
        }
        imagedFingerprintList = new ArrayList<ImagedFingerprint>();
    }

    public BasicRequestParameters getBasicRequestParameters() {
        return basicRequestParameters;
    }

    public static CLIENT_TYPE getClientType() {
        return clientType;
    }

    public ExtendedRequestParameters getExtendedRequestParameters() {
        return extendedRequestParameters;
    }

    public boolean isNonFingerprint() {
        return nonFingerprint;
    }

    public void setNonFingerprint(boolean nonFingerprint) {
        this.nonFingerprint = nonFingerprint;
    }

    public ImagedFingerprint getCurrentImagedFingerprint() {
        return currentImagedFingerprint;
    }

    public void setCurrentImagedFingerprint(ImagedFingerprint currentImagedFingerprint) {
        this.currentImagedFingerprint = currentImagedFingerprint;
    }

    public static List<ImagedFingerprint> getImagedFingerprintList() {
        return imagedFingerprintList;
    }

    public boolean hasKnownClinicId() {
        return knownClinicId;
    }

    public void setKnownClinicId(boolean knownClinicId) {
        this.knownClinicId = knownClinicId;
    }

    public void addImagedFingerprint(ImagedFingerprint imagedFingerprint) {
        currentImagedFingerprint = imagedFingerprint;
        imagedFingerprintList.add(imagedFingerprint);
    }

    public boolean hasAllFingerprintsTaken() {
        boolean allFingerprintsTaken = false;
        if (nonFingerprint) {
            allFingerprintsTaken = true;
        } else {
            if (imagedFingerprintList != null
                    && imagedFingerprintList.size() > 5) {
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

    public static boolean checkForFingerprintCandidates(List<Person> personList) {
        for (Person person : personList) {
            if (person.isFingerprintMatched()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkForLinkedCandidates(List<Person> personList) {
        for (Person person : personList) {
            if (Session.getMPIIdentifier(person) != null) {
                return true;
            }
        }
        return false;
    }

    public static String getMPIIdentifier(Person person) {
        String mpiIdentifier = null;
        if (person != null
                && person.getPersonIdentifierList() != null
                && !person.getPersonIdentifierList().isEmpty()) {
            for (PersonIdentifier personIdentifier : person.getPersonIdentifierList()) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.masterPatientRegistryId) {
                    mpiIdentifier = personIdentifier.getIdentifier();
                }
            }
        }
        return mpiIdentifier;
    }

    public static PersonIdentifier getGUID(Person person) {
        PersonIdentifier guid = null;
        if (person != null && person.getPersonIdentifierList() != null) {
            for (PersonIdentifier personIdentifier : person.getPersonIdentifierList()) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.masterPatientRegistryId) {
                    guid = personIdentifier;
                }
            }
        }
        return guid;
    }

    public static PersonIdentifier.Type deduceIdentifierType(String personIdentifier) {
        PersonIdentifier.Type identifierType = PersonIdentifier.Type.indeterminate;
        if (personIdentifier != null && !personIdentifier.isEmpty()) {
            if (personIdentifier.contains("-") && !personIdentifier.contains("/")) {
                if ((personIdentifier.split("-").length == 2 && personIdentifier.split("-")[0].length() == 5)
                        && (personIdentifier.split("-").length == 2 && personIdentifier.split("-")[1].length() == 5)) {
                    identifierType = PersonIdentifier.Type.cccUniqueId;
                } else if (personIdentifier.length() < 20
                        && personIdentifier.split("-").length == 4) {
                    identifierType = PersonIdentifier.Type.kisumuHdssId;
                }
            } else if (personIdentifier.contains("/") && !personIdentifier.contains("-")) {
                if ((personIdentifier.split("/").length == 2 && personIdentifier.split("/")[0].length() == 5)
                        && (personIdentifier.split("/").length == 2 && personIdentifier.split("/")[1].length() == 4)) {
                    identifierType = PersonIdentifier.Type.cccLocalId;
                }
            }
            if (personIdentifier.length() > 20) {
                identifierType = PersonIdentifier.Type.masterPatientRegistryId;
            }
        }
        return identifierType;
    }

    public static boolean validateClinicId(String clinicId) {
        return deduceIdentifierType(clinicId) != PersonIdentifier.Type.indeterminate;
    }

    public static String prependClinicCode(String clinicId) {
        String clinicCode = Mediator.getProperty("Instance.FacilityCode");
        if (clinicCode != null) {
            clinicId = clinicCode + "-" + clinicId;
        }
        return clinicId;
    }

    public static String getApplicationName() {
        String instanceName = Mediator.getProperty("Instance.Name");
        if (instanceName == null) {
            instanceName = "OEC Clinic Reception Software";
        }
        return instanceName;
    }

    public static ImagedFingerprint getMissingFingerprint() {
        if (missingFingerprintImage == null) {
            try {
                missingFingerprintImage = ImageIO.read(new File("missing_fingerprint.png"));
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ImagedFingerprint(missingFingerprintImage, true);
    }

    public static ImagedFingerprint getRefusedFingerprint() {
        if (refusedFingerprintImage == null) {
            try {
                refusedFingerprintImage = ImageIO.read(new File("refused_fingerprint.png"));
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ImagedFingerprint(refusedFingerprintImage, true);
    }
}
