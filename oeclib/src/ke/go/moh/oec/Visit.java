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
package ke.go.moh.oec;

import java.util.Date;

/**
 * Record of a visit of a patient to a health care facility.
 * <p>
 * Note that this is how a visit is coded for packing in a HL7 message.
 * It is derived from, but not the same as, how a visit is stored
 * in the database. This visit differs from the database in the following ways:
 * <p>
 * 1. Rather than referencing the full facility information found in the
 * facility table, only the facility name is given here.
 * <p>
 * 2. The visit type in this class is implied. This is because only
 * two visits are referenced in the HL7 patient object: the most recent
 * regular visit and the most recent one-off visit.
 *
 * @author Jim Grace
 */
public class Visit {

    /** The date on which the patient visited the health care facility. */
    private Date visitDate;
    /** The facility visited by the patient. */
    private String facilityName;
    /** The instance (reception station within the facility) where the patient was seen. */
    private String instanceAddress;
}
