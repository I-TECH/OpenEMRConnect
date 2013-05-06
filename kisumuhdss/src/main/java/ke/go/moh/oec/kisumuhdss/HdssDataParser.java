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
package ke.go.moh.oec.kisumuhdss;

import java.util.logging.Level;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.lib.Mediator;

/**
 * Parses (and cleans) HDSS values to values expected by the MPI
 * @author Jim Grace
 */
public class HdssDataParser {

    /**
     * Parses and cleans HDSS pregnancyOutcome string value
     * 
     * @param s pregnancyOutcome string value
     * @param hdssId HDSS ID for the person we are processing
     * @return pregnancyOutcome enumerated value
     */
    static public Person.PregnancyOutcome pregnancyOutcome(String val, String hdssId) {
        Person.PregnancyOutcome p = null;
        if (val != null && !val.isEmpty()) {
            String s = val.trim();
            if (s.compareTo("SINLBR") == 0) {
                p = Person.PregnancyOutcome.singleBirth;
            } else if (s.compareTo("LBR") == 0) {
                p = Person.PregnancyOutcome.singleBirth;
            } else if (s.compareTo("NOTAPP") == 0) {
                p = null; // Don't know how to interpret NOTAPP (Not Applicable?)
            } else if (s.compareTo("CEN") == 0) {
                p = null; // Don't know how to interpret CEN (Censored?).
            } else if (s.compareTo("MLB") == 0) {
                p = Person.PregnancyOutcome.multipleBirths;
            } else if (s.compareTo("SLB") == 0) {
                p = Person.PregnancyOutcome.singleBirth;
            } else if (s.compareTo("MULLBR") == 0) {
                p = Person.PregnancyOutcome.multipleBirths;
            } else if (s.compareTo("NAP") == 0) {
                p = null; // Don't know how to interpret NAP (Not Applicable?).
            } else if (s.compareTo("SINSTB") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("MISCAR") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("DTH") == 0) {
                p = Person.PregnancyOutcome.stillBirth; // Death is not really a stillbirth, but it tells the clinic that the baby died
            } else if (s.compareTo("MIS") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("STB") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("SST") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("LBRSTB") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("MULSTB") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("MST") == 0) {
                p = Person.PregnancyOutcome.stillBirth;
            } else if (s.compareTo("SINLBR") == 0) {
                p = Person.PregnancyOutcome.singleBirth;
            } else {
                Mediator.getLogger(HdssDataParser.class.getName()).log(Level.WARNING,
                        "Unexpected pregnancy outcome value from HDSS ID {0}: ''{1}''",
                        new Object[]{hdssId, s});
            }
        }
        return p;
    }

    /**
     * Parses and cleans HDSS maritalStatus string value
     * 
     * @param s maritalStatus string value
     * @param hdssId HDSS ID for the person we are processing
     * @return maritalStatus enumerated value
     */
    static public Person.MaritalStatus maritalStatus(String mtalVal, String mtypVal, String hdssId) {
        Person.MaritalStatus m = null;
        String mtal = mtalVal;
        String mtyp = mtypVal;
        if (mtyp != null) {
            mtyp = mtyp.toUpperCase();
        }
        if (mtal != null && !mtal.isEmpty()) {
            mtal = mtal.toUpperCase();
            if (mtal.compareTo("SINGLE") == 0) {
                m = Person.MaritalStatus.single;
            } else if (mtal.compareTo("MARRIED") == 0 || mtal.compareTo("MARRIED/COHABITING") == 0) {
                if (mtyp != null && mtyp.compareTo("POLYGAMOUS") == 0) {
                    m = Person.MaritalStatus.marriedPolygamous;
                } else {
                    m = Person.MaritalStatus.marriedMonogamous;
                }
            } else if (mtal.compareTo("NA") == 0) { // Assume NA means single children?
                m = Person.MaritalStatus.single;
            } else if (mtal.compareTo("WIDOWED") == 0) {
                m = Person.MaritalStatus.widowed;
            } else if (mtal.compareTo("DIVORCE/SEPARATED") == 0 || mtal.compareTo("DIVORCE/SEPERATED") == 0) {
                m = Person.MaritalStatus.widowed;
            } else if (mtal.compareTo("DON'T KNOW") == 0) {
                m = Person.MaritalStatus.widowed;
            } else {
                Mediator.getLogger(HdssDataParser.class.getName()).log(Level.WARNING,
                        "Unexpected marital status from HDSS ID {0}: ''{1}''",
                        new Object[]{hdssId, mtalVal});
            }
        } else if (mtyp != null) { // Marital Status was not present, but Marital type was.
            if (mtyp.compareTo("MONOGAMOUS") == 0) {
                m = Person.MaritalStatus.marriedMonogamous;
            } else if (mtyp.compareTo("POLYGAMOUS") == 0) {
                m = Person.MaritalStatus.marriedPolygamous;
            }
        }
        return m;
    }

    /**
     * Parses and cleans HDSS sex string value
     * 
     * @param s sex string value
     * @param hdssId HDSS ID for the person we are processing
     * @return sex enumerated value
     */
    static public Person.Sex sex(String s, String hdssId) {
        Person.Sex sex = null;
        if (s != null && !s.isEmpty()) {
            s = s.trim().toUpperCase();
            if (s.compareTo("F") == 0) {
                sex = Person.Sex.F;
            } else if (s.compareTo("M") == 0) {
                sex = Person.Sex.M;
            } else {
                Mediator.getLogger(HdssDataParser.class.getName()).log(Level.WARNING,
                        "Unexpected sex value from HDSS ID {0}: ''{1}''",
                        new Object[]{hdssId, s});
            }
        }
        return sex;
    }

    /**
     * Parses and cleans HDSS fingerprintType string value
     * 
     * @param s fingerprintType string value
     * @param hdssId HDSS ID for the person we are processing
     * @return fingerprintType enumerated value
     */
    static public Fingerprint.Type fingerprintType(String s, String hdssId) {
        Fingerprint.Type type = null;
        if (s != null && !s.isEmpty()) {
            s = s.trim().toUpperCase();
            if (s.compareTo("RIGHT INDEX") == 0) {
                type = Fingerprint.Type.rightIndexFinger;
            } else if (s.compareTo("LEFT INDEX") == 0) {
                type = Fingerprint.Type.leftIndexFinger;
            } else if (s.compareTo("RIGHT MIDDLE") == 0) {
                type = Fingerprint.Type.rightMiddleFinger;
            } else if (s.compareTo("LEFT MIDDLE") == 0) {
                type = Fingerprint.Type.leftMiddleFinger;
            } else if (s.compareTo("RIGHT RING") == 0) {
                type = Fingerprint.Type.rightRingFinger;
            } else if (s.compareTo("LEFT RING") == 0) {
                type = Fingerprint.Type.leftRingFinger;
            } else if (s.compareTo("RIGHT LITTLE") == 0) {
                type = Fingerprint.Type.rightLittleFinger;
            } else if (s.compareTo("LEFT LITTLE") == 0) {
                type = Fingerprint.Type.leftLittleFinger;
            } else {
                Mediator.getLogger(HdssDataParser.class.getName()).log(Level.WARNING,
                        "Unexpected fingerprint type value from HDSS ID {0}: ''{1}''",
                        new Object[]{hdssId, s});
            }
        }
        return type;
    }

    /**
     * Parses and cleans HDSS fingerprintTechnologyType string value
     * 
     * @param s fingerprintTechnologyType string value
     * @param hdssId HDSS ID for the person we are processing
     * @return fingerprintTechnologyType enumerated value
     */
    static public Fingerprint.TechnologyType fingerprintTechnologyType(String s, String hdssId) {
        Fingerprint.TechnologyType technologyType = null;
        if (s != null && !s.isEmpty()) {
            s = s.trim();
            if (s.compareTo("GrFinger") == 0) {
                technologyType = Fingerprint.TechnologyType.griauleTemplate;
            } else {
                Mediator.getLogger(HdssDataParser.class.getName()).log(Level.WARNING,
                        "Unexpected fingerprint technology value from HDSS ID {0}: ''{1}''",
                        new Object[]{hdssId, s});
            }
        }
        return technologyType;
    }
}
