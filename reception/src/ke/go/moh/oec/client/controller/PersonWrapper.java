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
import ke.go.moh.oec.client.data.ImagedFingerprint;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class PersonWrapper {

    private final Person person;

    public PersonWrapper(Person person) {
        this.person = person;
    }

    public Person unwrap() {
        return person;
    }

    public void setClinicId(String clinicId) throws IllegalArgumentException {
        if (!OECReception.validateClinicId(clinicId)) {
            throw new IllegalArgumentException("The Clinic ID you entered is in the wrong format. "
                    + "Please use the format '12345-00001' for Universal Clinic IDs and '00001/2005' "
                    + "for Local Clinic IDs");
        }
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(clinicId);
        personIdentifier.setIdentifierType(OECReception.deducePersonIdentifierType(clinicId));
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList == null) {
            personIdentifierList = new ArrayList<PersonIdentifier>();
        }
        personIdentifierList.add(personIdentifier);
        person.setPersonIdentifierList(personIdentifierList);
    }

    public void setPersonGuid(String personGuid) throws IllegalArgumentException {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(personGuid);
        personIdentifier.setIdentifierType(PersonIdentifier.Type.masterPatientRegistryId);
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList == null) {
            personIdentifierList = new ArrayList<PersonIdentifier>();
        }
        personIdentifierList.add(personIdentifier);
        person.setPersonIdentifierList(personIdentifierList);
    }

    public void setClinicName(String clinicName) {
        person.setSiteName(clinicName);
    }

    public void setFingerprint(Fingerprint fingerprint) {
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

    public void setFirstName(String firstName) {
        person.setFirstName(firstName);
    }

    public void setLastName(String lastName) {
        person.setLastName(lastName);
    }

    public void setMiddleName(String middleName) {
        person.setMiddleName(middleName);
    }

    public void setSex(Person.Sex sex) {
        person.setSex(sex);
    }

    public void setVillageName(String villageName) {
        person.setVillageName(villageName);
    }

    public void setMaritalStatus(Person.MaritalStatus maritalStatus) {
        person.setMaritalStatus(maritalStatus);
    }

    public void setFathersFirstName(String fathersFirstName) {
        person.setFathersFirstName(fathersFirstName);
    }

    public void setFathersMiddleName(String fathersMiddleName) {
        person.setFathersMiddleName(fathersMiddleName);
    }

    public void setFathersLastName(String fathersLastName) {
        person.setFathersLastName(fathersLastName);
    }

    public void setMothersFirstName(String fathersFirstName) {
        person.setMothersFirstName(fathersFirstName);
    }

    public void setMothersMiddleName(String fathersMiddleName) {
        person.setMothersMiddleName(fathersMiddleName);
    }

    public void setMothersLastName(String fathersLastName) {
        person.setMothersLastName(fathersLastName);
    }

    public void setCompoundHeadsFirstName(String fathersFirstName) {
        person.setCompoundHeadFirstName(fathersFirstName);
    }

    public void setCompoundHeadsMiddleName(String fathersMiddleName) {
        person.setCompoundHeadMiddleName(fathersMiddleName);
    }

    public void setCompoundHeadsLastName(String fathersLastName) {
        person.setCompoundHeadLastName(fathersLastName);
    }

    public void setConsentSigned(Person.ConsentSigned consentSigned) {
        person.setConsentSigned(consentSigned);
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
    public void setFingerprintList(List<ImagedFingerprint> imagedFingerprintList) {
        if (person.getFingerprintList() == null) {
            person.setFingerprintList(new ArrayList<Fingerprint>());
        }
        for (ImagedFingerprint imagedFingerprint : imagedFingerprintList) {
            person.getFingerprintList().add(imagedFingerprint.getFingerprint());
        }
    }
}
