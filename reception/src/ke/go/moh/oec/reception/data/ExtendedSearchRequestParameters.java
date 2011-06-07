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

import java.util.Date;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person.Sex;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class ExtendedSearchRequestParameters implements RequestParameters {

    private String clinicId;
    private String clinicName;
    private String firstName;
    private String middleName;
    private String lastName;
    private Sex sex;
    private Date birthdate;
    private String villageName;
    List<Fingerprint> fingerprintList;

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthDate) {
        this.birthdate = birthDate;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<Fingerprint> getFingerprint() {
        return fingerprintList;
    }

    public void setFingerprint(List<Fingerprint> fingerprintList) {
        this.fingerprintList = fingerprintList;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExtendedSearchRequestParameters other = (ExtendedSearchRequestParameters) obj;
        if ((this.clinicId == null) ? (other.clinicId != null) : !this.clinicId.equals(other.clinicId)) {
            return false;
        }
        if ((this.clinicName == null) ? (other.clinicName != null) : !this.clinicName.equals(other.clinicName)) {
            return false;
        }
        if ((this.firstName == null) ? (other.firstName != null) : !this.firstName.equals(other.firstName)) {
            return false;
        }
        if ((this.middleName == null) ? (other.middleName != null) : !this.middleName.equals(other.middleName)) {
            return false;
        }
        if ((this.lastName == null) ? (other.lastName != null) : !this.lastName.equals(other.lastName)) {
            return false;
        }
        if (this.sex != other.sex) {
            return false;
        }
        if (this.birthdate != other.birthdate && (this.birthdate == null || !this.birthdate.equals(other.birthdate))) {
            return false;
        }
        if ((this.villageName == null) ? (other.villageName != null) : !this.villageName.equals(other.villageName)) {
            return false;
        }
        if (this.fingerprintList != other.fingerprintList && (this.fingerprintList == null || !this.fingerprintList.equals(other.fingerprintList))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.clinicId != null ? this.clinicId.hashCode() : 0);
        hash = 83 * hash + (this.clinicName != null ? this.clinicName.hashCode() : 0);
        hash = 83 * hash + (this.firstName != null ? this.firstName.hashCode() : 0);
        hash = 83 * hash + (this.middleName != null ? this.middleName.hashCode() : 0);
        hash = 83 * hash + (this.lastName != null ? this.lastName.hashCode() : 0);
        hash = 83 * hash + (this.sex != null ? this.sex.hashCode() : 0);
        hash = 83 * hash + (this.birthdate != null ? this.birthdate.hashCode() : 0);
        hash = 83 * hash + (this.villageName != null ? this.villageName.hashCode() : 0);
        hash = 83 * hash + (this.fingerprintList != null ? this.fingerprintList.hashCode() : 0);
        return hash;
    }
}
