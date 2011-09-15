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

    public static String generateSessionReference() {
        return Mediator.generateMessageId();
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        OECReception.user = user;
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

    public static boolean isPrependedLocalClinicId(String personIdentifier) {
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
            String clinicCode = OECReception.facilityCode();
            if (clinicCode != null) {
                clinicId = clinicCode + "-" + clinicId;
            }
        }
        return clinicId;
    }

    public static String extractFacilityCode(String clinicId) {
        String facilityCode = "";
        int m = clinicId.split("-").length;
        if (m == 2) {
            String[] partsOfWhole = clinicId.split("-");
            if (partsOfWhole[0].length() == 5) {
                facilityCode = partsOfWhole[0];
            }
        }
        return facilityCode;
    }
}
