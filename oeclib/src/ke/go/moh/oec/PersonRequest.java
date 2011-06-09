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
    private Person person;
    /**
     * A request reference number. This is not
     * supplied by the client when making a new request.
     * But it is always supplied by the library to the server when the
     * request is delivered to the server. It may also be specified by the caller
     * in order to associate a request with an earlier request.
     * <p>
     * For example, a client would specify the reference number from
     * a prior person search when making a subsequent call relating
     * to the same search. It is also supplied by the client when taking
     * action such as add person or modify person, based on the results
     * of a previous find person. This lets the server know which person
     * was chosen (or, in the event of ADD PERSON, that no person was
     * chosen from the search results.)
     */
    private String requestReference;
    /**
     * Address of the destination to send this request to. In most cases
     * this is null because the OEC library will determine the request
     * destination address based on the request type. However in the
     * case of a proactive notification of revised person information,
     * the requester may specify where to send the revised person data.
     */
    private String destinationAddress;
    /**
     * Name of the destination to send this request to. In most cases
     * this is null because the OEC library will determine the request
     * destination address based on the request type. However in the
     * case of a proactive notification of revised person information,
     * the requester may specify where to send the revised person data.
     */
    private String destinationName;
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
    /**
     * Does the caller want a response to this message?
     * This applies to createPerson and modifyPerson requests,
     * where the response is optional. Default is false.
     */
    private boolean responseRequested = false;
    /**
     * The message in XML form.
     * <p>
     * This can be used to deliver the message to a destination in raw, XML form.
     * It may also be used to send a pre-formatted XML message to a destination.
     * Otherwise this value is not used.
     */
    private String xml;

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getMatchAlgorithm() {
        return matchAlgorithm;
    }

    public void setMatchAlgorithm(String matchAlgorithm) {
        this.matchAlgorithm = matchAlgorithm;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
    }

    public boolean isResponseRequested() {
        return responseRequested;
    }

    public void setResponseRequested(boolean responseRequested) {
        this.responseRequested = responseRequested;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
