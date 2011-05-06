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
 * A recorded fingerprint, used as a method of identifying a person.
 *
 * @author Jim Grace
 */
public class Fingerprint {
	
	public enum Type {

		leftIndexFinger,
		leftMiddleFinger,
		leftRingFinger,
		rightIndexFinger,
		rightMiddleFinger,
		rightRingFinger
	}
	
	public enum TechnologyType {

		griauleTemplate
	}

    /** The type of fingerprint (which hand and which finger). */
    private Type fingerprintType;
    /** The fingerprint template stores the actual fingerprint data. */
    private byte[] template;
    /**
     * The technology used to capture the fingerprint template.
     * <p>
     * In general, fingerprint templates collected using different
     * technologies cannot be compared.
     */
    private TechnologyType technologyType;
    /** Date on which the fingerprint was collected. */
    private Date dateEntered;
    /** Date on which the fingerprint was changed. */
    private Date dateChanged;

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public Date getDateEntered() {
		return dateEntered;
	}

	public void setDateEntered(Date dateEntered) {
		this.dateEntered = dateEntered;
	}

	public Type getFingerprintType() {
		return fingerprintType;
	}

	public void setFingerprintType(Type fingerprintType) {
		this.fingerprintType = fingerprintType;
	}

	public TechnologyType getTechnologyType() {
		return technologyType;
	}

	public void setTechnologyType(TechnologyType technologyType) {
		this.technologyType = technologyType;
	}

	public byte[] getTemplate() {
		return template;
	}

	public void setTemplate(byte[] template) {
		this.template = template;
	}

}
