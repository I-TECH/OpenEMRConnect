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

import java.util.List;

/**
 * Data that is returned in response to a request for person information.
 * This can include information about a single person, or query results
 * returning possible matches for zero or more people. It includes
 * return status to report whether or not there was an error processing
 * the request.
 * 
 * @author Jim Grace
 */
public class PersonResponse {

    /** Data for a list of zero or more persons to satisfy the request. */
    private List<Person> personList;
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
     * Is the request successful? Note that in the case of a person search,
     * this does not indicate whether matching entries were found. It
     * simply indicates that the operation completed successfully, whether
     * or not any matching entries were found.
     */
    private boolean successful;

    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
