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

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.data.User;


/**
 *
 * @author Gitahi Ng'ang'a
 */
public class OECReception {

    public static final int MINIMUM_FINGERPRINTS_FOR_SEARCH = 2;
    public static final int MINIMUM_FINGERPRINTS_FOR_REGISTRATION = 2;
    private static User user;

    public static String applicationAddress() {
        String instanceName = Mediator.getProperty("Instance.Address");
        if (instanceName == null) {
            instanceName = applicationName();
        }
        return instanceName;
    }

    public static String applicationName() {
        String instanceName = Mediator.getProperty("Instance.Name");
        if (instanceName == null) {
            instanceName = "OEC Reception";
        }
        return instanceName;
    }

    public static Image applicationIcon() {
        return Toolkit.getDefaultToolkit().getImage("faceless.png");
    }

    public static String facilityCode() {
        String instanceName = Mediator.getProperty("Instance.FacilityCode");
        if (instanceName == null) {
            instanceName = "OEC Reception";
        }
        return instanceName;
    }

    public static String facilityName() {
        String instanceName = Mediator.getProperty("Instance.FacilityName");
        if (instanceName == null) {
            instanceName = "OEC Reception";
        }
        return instanceName;
    }

    public static String fingerprintManager() {
        String instanceName = Mediator.getProperty("Instance.FingerprintManager");
        if (instanceName == null) {
            instanceName = "";
        }
        return instanceName;
    }

    public static String generateSessionReference() {
        return Mediator.generateMessageId();
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        OECReception.user = user;
    }

    public static String extractFacilityCode(String clinicId) {
        String facilityCode = "";
        String[] partsOfWhole = clinicId.split("-");
        int m = partsOfWhole.length;
        if (m == 2) {
            if (partsOfWhole[0].length() == 5) {
                facilityCode = partsOfWhole[0];
            }
        }
        return facilityCode;
    }

    /*
     * Returns true if the string passed is a number not exceeding the specified
     * length.
     */
    private static boolean isNumber(String string, int maxLength) {
        return string != null ? string.matches("\\d{1," + maxLength + "}") : false;
    }

    /*
     * Returns a String representing the integer passed padded with zeros to the left
     * to achieve the specified size.
     */
    private static String padWithZeros(int input, int toSize) {
        return String.format("%0" + toSize + "d", input);
    }

    /*
     * This method inteprets the integer passed as a year in the 2nd millennium AD
     * This is only necessary for the usual two-digit year representations or the
     * unlikely one or three digit year representation.
     */
    private static int qualifyYear(int year) {
        if (year > 9 && year < 100) {
            return Integer.parseInt("20" + year);
        } else if (year > -1 && year < 10) {
            return Integer.parseInt("200" + year);
        } else if (year > 99 && year < 1000) {
            return Integer.parseInt("2" + year);
        } else {
            return year;
        }
    }

    /**
     * This method examines the String passed to determine the kind of clinic id 
     * it is. If it can recognize the pattern, it uses the String passed to create
     * a fully qualified clinic id of that type. If it cannot deduce the clinic type
     * represented by the passed String, it returns null.
     * 
     * @param clinicId
     * 
     * @return  
     */
    public static PersonIdentifier createPersonIdentifier(String clinicId) {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(clinicId);
        personIdentifier.setIdentifierType(PersonIdentifier.Type.cccLocalId);
        return personIdentifier;
//        if (clinicId != null && !clinicId.isEmpty()) {
//            if (OECReception.isNumber(clinicId, 5)) {
//                PersonIdentifier personIdentifier = new PersonIdentifier();
//                personIdentifier.setIdentifier(facilityCode() + "-"
//                        + padWithZeros(Integer.parseInt(clinicId), 5));
//                personIdentifier.setIdentifierType(PersonIdentifier.Type.cccUniqueId);
//                return personIdentifier;
//            }
//            if (clinicId.contains("/") && !clinicId.contains("-") && clinicId.split("/").length == 2) {
//                String parts[] = clinicId.split("/");
//                String patientNumber = parts[0];
//                String year = parts[1];
//                if (isNumber(patientNumber, 5)) {
//                    int yr = 0;
//                    if (isNumber(year, 4)) {
//                        yr = qualifyYear(Integer.parseInt(year));
//                    }
//                    int yrToday = new GregorianCalendar().get(Calendar.YEAR);
//                    if (yr > 2000 && yr <= yrToday) {
//                        PersonIdentifier personIdentifier = new PersonIdentifier();
//                        personIdentifier.setIdentifier(facilityCode() + "-"
//                                + padWithZeros(Integer.parseInt(patientNumber), 5)
//                                + "/" + yr);
//                        personIdentifier.setIdentifierType(PersonIdentifier.Type.cccLocalId);
//                        return personIdentifier;
//                    }
//                }
//            }
//            if (clinicId.contains("-") && clinicId.split("-").length == 2) {
//                String[] mainParts = clinicId.split("-");
//                String facilityPart = mainParts[0];
//                if (facilityPart.length() == 5 && isNumber(facilityPart, 5)) {
//                    String patientPart = mainParts[1];
//                    if (!patientPart.contains("/")) {
//                        if (OECReception.isNumber(patientPart, 5)) {
//                            PersonIdentifier personIdentifier = new PersonIdentifier();
//                            personIdentifier.setIdentifier(facilityPart + "-"
//                                    + padWithZeros(Integer.parseInt(patientPart), 5));
//                            personIdentifier.setIdentifierType(PersonIdentifier.Type.cccUniqueId);
//                            return personIdentifier;
//                        }
//                    } else if (patientPart.contains("/") && patientPart.split("/").length == 2) {
//                        String parts[] = patientPart.split("/");
//                        String patientNumber = parts[0];
//                        String year = parts[1];
//                        if (isNumber(patientNumber, 5)) {
//                            int yr = 0;
//                            if (isNumber(year, 4)) {
//                                yr = qualifyYear(Integer.parseInt(year));
//                            }
//                            int yrToday = new GregorianCalendar().get(Calendar.YEAR);
//                            if (yr > 2000 && yr <= yrToday) {
//                                PersonIdentifier personIdentifier = new PersonIdentifier();
//                                personIdentifier.setIdentifier(facilityPart + "-"
//                                        + padWithZeros(Integer.parseInt(patientNumber), 5)
//                                        + "/" + yr);
//                                personIdentifier.setIdentifierType(PersonIdentifier.Type.cccLocalId);
//                                return personIdentifier;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
    }

    private OECReception() {
    }
}
