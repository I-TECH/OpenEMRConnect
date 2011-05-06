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

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
}
