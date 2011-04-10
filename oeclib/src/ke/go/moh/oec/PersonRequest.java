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

/**
 * Data that is supplied as part of a request for person information.
 *
 * @author Jim Grace
 */
public class PersonRequest {

    /** The <code>Person</code> data for this request. */
    private String person;
    /**
     * A request reference number. This is not normally
     * supplied by the original client when making the request.
     * But it is supplied to the server when the request is delivered
     * to the server. It may also be specified by the caller
	 * in order to associate a request with an earlier request.
     */
    private String requestReference;
    /**
     * Destination address where to send this request. In most cases
     * this is null because the OEC library will determine the request
     * destination address based on the request type. However in the
     * case of a proactive notification of revised person information,
     * the requester may specify where to send the revised person data.
     */
    private String destinationAddress;
	/**
	 * Match algorithm to use. One of the following values:
	 * <p>
	 * Normal - Do a normal, weighted match <br>
	 * FingerprintMustMatch - Like normal except only return results where
	 * fingerprint is above the fingerprint match confidence threshold.
	 * <p>
	 * If not specified, the default is Normal.
	 */
	private String matchAlgorithm;

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getMatchAlgorithm() {
		return matchAlgorithm;
	}

	public void setMatchAlgorithm(String matchAlgorithm) {
		this.matchAlgorithm = matchAlgorithm;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getRequestReference() {
		return requestReference;
	}

	public void setRequestReference(String requestReference) {
		this.requestReference = requestReference;
	}
	
}
