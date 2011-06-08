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
package ke.go.moh.oec.reception.data;

import ke.go.moh.oec.Person.MaritalStatus;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class ComprehensiveRequestParameters implements RequestParameters {

    private ExtendedRequestParameters extendedRequestParameters;
    private String fathersFirstName;
    private String fathersMiddleName;
    private String fathersLastName;
    private String mothersFirstName;
    private String mothersMiddleName;
    private String mothersLastName;
    private String compoundHeadsFirstName;
    private String compoundHeadsMiddleName;
    private String compoundHeadsLastName;
    private MaritalStatus maritalStatus;

    public String getCompoundHeadsFirstName() {
        return compoundHeadsFirstName;
    }

    public void setCompoundHeadsFirstName(String compoundHeadsFirstName) {
        this.compoundHeadsFirstName = compoundHeadsFirstName;
    }

    public String getCompoundHeadsLastName() {
        return compoundHeadsLastName;
    }

    public void setCompoundHeadsLastName(String compoundHeadsLastName) {
        this.compoundHeadsLastName = compoundHeadsLastName;
    }

    public String getCompoundHeadsMiddleName() {
        return compoundHeadsMiddleName;
    }

    public void setCompoundHeadsMiddleName(String compoundHeadsMiddleName) {
        this.compoundHeadsMiddleName = compoundHeadsMiddleName;
    }

    public ExtendedRequestParameters getExtendedRequestParameters() {
        return extendedRequestParameters;
    }

    public void setExtendedRequestParameters(ExtendedRequestParameters extendedRequestParameters) {
        this.extendedRequestParameters = extendedRequestParameters;
    }

    public String getFathersFirstName() {
        return fathersFirstName;
    }

    public void setFathersFirstName(String fathersFirstName) {
        this.fathersFirstName = fathersFirstName;
    }

    public String getFathersLastName() {
        return fathersLastName;
    }

    public void setFathersLastName(String fathersLastName) {
        this.fathersLastName = fathersLastName;
    }

    public String getFathersMiddleName() {
        return fathersMiddleName;
    }

    public void setFathersMiddleName(String fathersMiddleName) {
        this.fathersMiddleName = fathersMiddleName;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getMothersFirstName() {
        return mothersFirstName;
    }

    public void setMothersFirstName(String mothersFirstName) {
        this.mothersFirstName = mothersFirstName;
    }

    public String getMothersLastName() {
        return mothersLastName;
    }

    public void setMothersLastName(String mothersLastName) {
        this.mothersLastName = mothersLastName;
    }

    public String getMothersMiddleName() {
        return mothersMiddleName;
    }

    public void setMothersMiddleName(String mothersMiddleName) {
        this.mothersMiddleName = mothersMiddleName;
    }
}
