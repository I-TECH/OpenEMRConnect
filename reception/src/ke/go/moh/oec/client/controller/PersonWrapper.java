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
package ke.go.moh.oec.client.controller;

import ke.go.moh.oec.reception.controller.OECReception;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.client.controller.exceptions.MalformedCliniIdException;
import ke.go.moh.oec.client.data.ImagedFingerprint;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class PersonWrapper {

    private final Person person;
    private boolean confirmed = false;

    public PersonWrapper(Person person) {
        this.person = person;
    }

    public Person unwrap() {
        return person;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setPersonGuid(String personGUID) {
        person.setPersonGuid(personGUID);
    }

    public String getPersonGuid() {
        return person.getPersonGuid();
    }

    public void setClinicId(String clinicId) throws MalformedCliniIdException {
        if (!OECReception.validateClinicId(clinicId)) {
            throw new MalformedCliniIdException();
        }
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(clinicId);
        personIdentifier.setIdentifierType(OECReception.deducePersonIdentifierType(clinicId));
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList == null) {
            personIdentifierList = new ArrayList<PersonIdentifier>();
        }
        if (!personIdentifierList.contains(personIdentifier)) {
            personIdentifierList.add(personIdentifier);
        }
        person.setPersonIdentifierList(personIdentifierList);
    }

    public String getClinicId() {
        String clinicId = "";
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList != null) {
            for (PersonIdentifier personIdentifier : personIdentifierList) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.cccLocalId
                        || personIdentifier.getIdentifierType() == PersonIdentifier.Type.cccUniqueId) {
                    clinicId = personIdentifier.getIdentifier();
                    break;
                }
            }
        }
        return clinicId;
    }

    public void setMPIIdentifier(String mpiPersonIdentifier) throws IllegalArgumentException {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(mpiPersonIdentifier);
        personIdentifier.setIdentifierType(PersonIdentifier.Type.masterPatientRegistryId);
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList == null) {
            personIdentifierList = new ArrayList<PersonIdentifier>();
        }
        if (!personIdentifierList.contains(personIdentifier)) {
            personIdentifierList.add(personIdentifier);
        }
        person.setPersonIdentifierList(personIdentifierList);
    }

    public String getMPIIdentifier() {
        String personGuid = "";
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList != null) {
            for (PersonIdentifier personIdentifier : personIdentifierList) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.masterPatientRegistryId) {
                    personGuid = personIdentifier.getIdentifier();
                    break;
                }
            }
        }
        return personGuid;
    }

    public void setClinicName(String clinicName) {
        person.setSiteName(clinicName);
    }

    public String getClinicName() {
        String clinicName = person.getSiteName();
        if (clinicName == null) {
            return "";
        }
        return clinicName;
    }

    public void addFingerprint(Fingerprint fingerprint) {
        List<Fingerprint> fingerprintList = person.getFingerprintList();
        if (fingerprintList == null) {
            fingerprintList = new ArrayList<Fingerprint>();
        }
        fingerprintList.add(fingerprint);
        person.setFingerprintList(fingerprintList);
    }

    public void setBirthdate(Date birthdate) {
        person.setBirthdate(birthdate);
    }

    public Date getBirthdate() {
        Date birthdate = person.getBirthdate();
        if (birthdate == null) {
            return new Date();
        }
        return birthdate;
    }

    public void setFirstName(String firstName) {
        person.setFirstName(firstName);
    }

    public String getFirstName() {
        String firstName = person.getFirstName();
        if (firstName == null) {
            return "";
        }
        return firstName;
    }

    public void setLastName(String lastName) {
        person.setLastName(lastName);
    }

    public String getLastName() {
        String lastName = person.getLastName();
        if (lastName == null) {
            return "";
        }
        return lastName;
    }

    public void setMiddleName(String middleName) {
        person.setMiddleName(middleName);
    }

    public String getMiddleName() {
        String middleName = person.getMiddleName();
        if (middleName == null) {
            return "";
        }
        return middleName;
    }

    public void setSex(Person.Sex sex) {
        person.setSex(sex);
    }

    public Person.Sex getSex() {
        return person.getSex();
    }

    public void setVillageName(String villageName) {
        person.setVillageName(villageName);
    }

    public String getVillageName() {
        String villageName = person.getVillageName();
        if (villageName == null) {
            return "";
        }
        return villageName;
    }

    public void setMaritalStatus(Person.MaritalStatus maritalStatus) {
        person.setMaritalStatus(maritalStatus);
    }

    public Person.MaritalStatus getMaritalStatus() {
        return person.getMaritalStatus();
    }

    public void setFathersFirstName(String fathersFirstName) {
        person.setFathersFirstName(fathersFirstName);
    }

    public String getFathersFirstName() {
        String fathersFirstName = person.getFathersFirstName();
        if (fathersFirstName == null) {
            return "";
        }
        return fathersFirstName;
    }

    public void setFathersMiddleName(String fathersMiddleName) {
        person.setFathersMiddleName(fathersMiddleName);
    }

    public String getFathersMiddleName() {
        String fathersMiddleName = person.getFathersMiddleName();
        if (fathersMiddleName == null) {
            return "";
        }
        return fathersMiddleName;
    }

    public void setFathersLastName(String fathersLastName) {
        person.setFathersLastName(fathersLastName);
    }

    public String getFathersLastName() {
        String fathersLastName = person.getFathersLastName();
        if (fathersLastName == null) {
            return "";
        }
        return fathersLastName;
    }

    public void setMothersFirstName(String fathersFirstName) {
        person.setMothersFirstName(fathersFirstName);
    }

    public String getMothersFirstName() {
        String mothersFirstName = person.getMothersFirstName();
        if (mothersFirstName == null) {
            return "";
        }
        return mothersFirstName;
    }

    public void setMothersMiddleName(String fathersMiddleName) {
        person.setMothersMiddleName(fathersMiddleName);
    }

    public String getMothersMiddleName() {
        String mothersMiddleName = person.getMothersMiddleName();
        if (mothersMiddleName == null) {
            return "";
        }
        return mothersMiddleName;
    }

    public void setMothersLastName(String fathersLastName) {
        person.setMothersLastName(fathersLastName);
    }

    public String getMothersLastName() {
        String mothersLastName = person.getMothersLastName();
        if (mothersLastName == null) {
            return "";
        }
        return mothersLastName;
    }

    public void setCompoundHeadsFirstName(String fathersFirstName) {
        person.setCompoundHeadFirstName(fathersFirstName);
    }

    public String getCompoundHeadFirstName() {
        String compoundHeadFirstName = person.getCompoundHeadFirstName();
        if (compoundHeadFirstName == null) {
            return "";
        }
        return compoundHeadFirstName;
    }

    public void setCompoundHeadsMiddleName(String fathersMiddleName) {
        person.setCompoundHeadMiddleName(fathersMiddleName);
    }

    public String getCompoundHeadMiddleName() {
        String compoundHeadMiddleName = person.getCompoundHeadMiddleName();
        if (compoundHeadMiddleName == null) {
            return "";
        }
        return compoundHeadMiddleName;
    }

    public void setCompoundHeadsLastName(String fathersLastName) {
        person.setCompoundHeadLastName(fathersLastName);
    }

    public String getCompoundHeadLastName() {
        String compoundHeadLastName = person.getCompoundHeadLastName();
        if (compoundHeadLastName == null) {
            return "";
        }
        return compoundHeadLastName;
    }

    public void setConsentSigned(Person.ConsentSigned consentSigned) {
        person.setConsentSigned(consentSigned);
    }

    public Person.ConsentSigned getConsentSigned() {
        return person.getConsentSigned();
    }

    public void setLastRegularVisit(Visit visit) {
        person.setLastRegularVisit(visit);
    }

    public void setLastOneOffVisit(Visit visit) {
        person.setLastOneOffVisit(visit);
    }

    public void setLastMoveDate(Date lastMoveDate) {
        person.setLastMoveDate(lastMoveDate);
    }

    //TODO: When and how many fingerprints should I send for addition to MPI?
    public void setImagedFingerprintList(List<ImagedFingerprint> imagedFingerprintList) {
        if (person.getFingerprintList() == null) {
            person.setFingerprintList(new ArrayList<Fingerprint>());
        }
        for (ImagedFingerprint imagedFingerprint : imagedFingerprintList) {
            person.getFingerprintList().add(imagedFingerprint.getFingerprint());
        }
    }

    public Object getFingerprintList() {
        return person.getFingerprintList();
    }

    public void setFingerprintList(List<Fingerprint> fingerprintList) {
        person.setFingerprintList(fingerprintList);
    }
}
