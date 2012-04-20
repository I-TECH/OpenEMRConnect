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
package ke.go.moh.oec.mpi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;

/**
 * This class maps values between Java Enum values and database string values.
 * It does this by storing the same value pairs in two different HashMaps.
 * One hashes by the Java Enum value, and the other hashes by the database string value.
 * 
 * @author Jim Grace
 */
public class ValueMap {

    /** Map to find the database string value from the Java enum value. */
    private HashMap<Enum, String> db = new HashMap<Enum, String>();
    /** Map to find the Java enum value from the database string value. */
    private HashMap<String, Enum> val = new HashMap<String, Enum>();

    /**
     * Create a ValueMap from a list of value translation objects.
     * 
     * @param valueList List of value translation objects. Each Value entry
     * in the list tells how to translate between one Java enum value and
     * one database string value.
     */
    private ValueMap(List<Value> valueList) {
        for (Value v : valueList) {
            db.put(v.getVal(), v.getDb());
            val.put(v.getDb(), v.getVal());
        }
    }

    public HashMap<Enum, String> getDb() {
        return db;
    }

    public HashMap<String, Enum> getVal() {
        return val;
    }

    public static final ValueMap SEX = new ValueMap(Arrays.asList(
            new Value(Person.Sex.F, "F"),
            new Value(Person.Sex.M, "M")));
    
    public static final ValueMap MARITAL_STATUS = new ValueMap(Arrays.asList(
            new Value(Person.MaritalStatus.single, "Single"),
            new Value(Person.MaritalStatus.marriedPolygamous, "Married Polygamous"),
            new Value(Person.MaritalStatus.marriedMonogamous, "Married Monogamous"),
            new Value(Person.MaritalStatus.divorced, "Divorced"),
            new Value(Person.MaritalStatus.widowed, "Widowed"),
            new Value(Person.MaritalStatus.cohabitating, "Cohabitating")));
    
    public static final ValueMap CONSENT_SIGNED = new ValueMap(Arrays.asList(
            new Value(Person.ConsentSigned.yes, "1"),
            new Value(Person.ConsentSigned.no, "0"),
            new Value(Person.ConsentSigned.notAnswered, null)));

    public static final ValueMap PERSON_IDENTIFIER_TYPE = new ValueMap(Arrays.asList(
            new Value(PersonIdentifier.Type.kisumuHdssId, "1"),
            new Value(PersonIdentifier.Type.cccUniqueId, "2"),
            new Value(PersonIdentifier.Type.masterPatientRegistryId, "3"),
            new Value(PersonIdentifier.Type.cccLocalId, "4")));

    public static final ValueMap FINGERPRINT_TYPE = new ValueMap(Arrays.asList(
            new Value(Fingerprint.Type.rightIndexFinger, "1"),
            new Value(Fingerprint.Type.leftIndexFinger, "2"),
            new Value(Fingerprint.Type.rightMiddleFinger, "3"),
            new Value(Fingerprint.Type.leftMiddleFinger, "4"),
            new Value(Fingerprint.Type.rightRingFinger, "5"),
            new Value(Fingerprint.Type.leftRingFinger, "6")));

    public static final ValueMap FINGERPRINT_TECHNOLOGY_TYPE = new ValueMap(Arrays.asList(
            new Value(Fingerprint.TechnologyType.griauleTemplate, "1")));

}
