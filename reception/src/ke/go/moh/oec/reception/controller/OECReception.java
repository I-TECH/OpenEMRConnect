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

import ke.go.moh.oec.reception.data.Session;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.security.User;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class OECReception {

    private static BufferedImage missingFingerprintImage;
    private static BufferedImage refusedFingerprintImage;
    public static final int MINIMUM_FINGERPRINTS_FOR_SEARCH = 2;
    private static User loggeInUser;

    public static User getLoggeInUser() {
        return loggeInUser;
    }

    public static void setLoggeInUser(User loggeInUser) {
        OECReception.loggeInUser = loggeInUser;
    }

    public static String getApplicationAddress() {
        String instanceName = Mediator.getProperty("Instance.Address");
        if (instanceName == null) {
            instanceName = getApplicationName();
        }
        return instanceName;
    }

    public static String getApplicationName() {
        String instanceName = Mediator.getProperty("Instance.Name");
        if (instanceName == null) {
            instanceName = "Clinic Reception";
        }
        return instanceName;
    }

    public static String generateSessionReference() {
        return Mediator.generateMessageId();
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
            if (OECReception.getMPIIdentifier(person) != null) {
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

    public static PersonIdentifier.Type deducePersonIdentifierType(String personIdentifier) {
        PersonIdentifier.Type identifierType = null;
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
            } else if (personIdentifier.contains("/") && personIdentifier.contains("-")) {
                if (isPrependedLocalClinicId(personIdentifier)) {
                    identifierType = PersonIdentifier.Type.cccLocalId;
                }
            } else if (personIdentifier.length() == 30) {
                identifierType = PersonIdentifier.Type.masterPatientRegistryId;
            }
        }
        return identifierType;
    }

    private static boolean isPrependedLocalClinicId(String personIdentifier) {
        boolean x = false;
        int m = personIdentifier.split("-").length;
        if (m == 2) {
            String[] partsOfWhole = personIdentifier.split("-");
            if (partsOfWhole[0].length() == 5) {
                int n = partsOfWhole[1].split("/").length;
                if (n == 2) {
                    String[] partsOfClinicId = partsOfWhole[1].split("/");
                    if (partsOfClinicId[0].length() == 5
                            && partsOfClinicId[1].length() == 4) {
                        x = true;
                    }
                }
            }
        }
        return x;
    }

    public static boolean validateClinicId(String clinicId) {
        return deducePersonIdentifierType(clinicId) != null;
    }

    public static String prependClinicCode(String clinicId) {
        if (!isPrependedLocalClinicId(clinicId)) {
            String clinicCode = Mediator.getProperty("Instance.FacilityCode");
            if (clinicCode != null) {
                clinicId = clinicCode + "-" + clinicId;
            }
        }
        return clinicId;
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

    public static String getSexString(Person.Sex sex) {
        String sexString = "";
        if (sex != null) {
            if (sex == Person.Sex.M) {
                sexString = "Male";
            } else if (sex == Person.Sex.F) {
                sexString = "Female";
            }
        }
        return sexString;
    }

    public static Person.Sex getSex(String sexString) {
        Person.Sex sex = null;
        if (sexString != null) {
            if (sexString.equalsIgnoreCase("Male")) {
                sex = Person.Sex.M;
            } else if (sexString.equalsIgnoreCase("Female")) {
                sex = Person.Sex.F;
            }
        }
        return sex;
    }

    public static String getConsentSignedString(Person.ConsentSigned consentSigned) {
        String consentSignedString = "";
        if (consentSigned != null) {
            if (consentSigned == Person.ConsentSigned.yes) {
                consentSignedString = "Yes";
            } else if (consentSigned == Person.ConsentSigned.no) {
                consentSignedString = "No";
            } else if (consentSigned == Person.ConsentSigned.notAnswered) {
                consentSignedString = "No answer";
            }
        }
        return consentSignedString;
    }

    public static Person.ConsentSigned getConsentSigned(String consentSignedString) {
        Person.ConsentSigned consentSigned = null;
        if (consentSigned != null) {
            if (consentSignedString.equalsIgnoreCase("Yes")) {
                consentSigned = Person.ConsentSigned.yes;
            } else if (consentSignedString.equalsIgnoreCase("No")) {
                consentSigned = Person.ConsentSigned.no;
            } else if (consentSignedString.equalsIgnoreCase("Not answered")) {
                consentSigned = Person.ConsentSigned.notAnswered;
            }
        }
        return consentSigned;
    }

    public static String getMaritalStatusString(Person.MaritalStatus maritalStatus) {
        String maritalStatusString = "";
        if (maritalStatus != null) {
            if (maritalStatus == Person.MaritalStatus.cohabitating) {
                maritalStatusString = "Cohabiting";
            } else if (maritalStatus == Person.MaritalStatus.divorced) {
                maritalStatusString = "Divorced";
            } else if (maritalStatus == Person.MaritalStatus.marriedMonogamous) {
                maritalStatusString = "Married monogamous";
            } else if (maritalStatus == Person.MaritalStatus.marriedPolygamous) {
                maritalStatusString = "Married polygamous";
            } else if (maritalStatus == Person.MaritalStatus.single) {
                maritalStatusString = "Single";
            } else if (maritalStatus == Person.MaritalStatus.widowed) {
                maritalStatusString = "Widowed";
            }
        }
        return maritalStatusString;
    }

    public static Person.MaritalStatus getMaritalStatus(String maritalStatusString) {
        Person.MaritalStatus maritalStatus = null;
        if (maritalStatus != null) {
            if (maritalStatusString.equalsIgnoreCase("Cohabiting")) {
                maritalStatus = Person.MaritalStatus.cohabitating;
            } else if (maritalStatusString.equalsIgnoreCase("Divorced")) {
                maritalStatus = Person.MaritalStatus.divorced;
            } else if (maritalStatusString.equalsIgnoreCase("Married monogamous")) {
                maritalStatus = Person.MaritalStatus.marriedMonogamous;
            } else if (maritalStatusString.equalsIgnoreCase("Married polygamous")) {
                maritalStatus = Person.MaritalStatus.marriedPolygamous;
            } else if (maritalStatusString.equalsIgnoreCase("Single")) {
                maritalStatus = Person.MaritalStatus.single;
            } else if (maritalStatusString.equalsIgnoreCase("Widowed")) {
                maritalStatus = Person.MaritalStatus.widowed;
            }
        }
        return maritalStatus;
    }
}
