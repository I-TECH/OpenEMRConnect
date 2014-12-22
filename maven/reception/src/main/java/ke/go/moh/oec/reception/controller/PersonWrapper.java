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
package ke.go.moh.oec.reception.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.reception.controller.exceptions.MalformedCliniIdException;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.Notification;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class PersonWrapper {

    private final Person person;
    private boolean confirmed = false;
    private String reference;

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

    private List<PersonIdentifier> preparedPersonIdentifierList(PersonIdentifier personIdentifier) {
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList == null) {
            personIdentifierList = new ArrayList<PersonIdentifier>();
        } else {
            //remove any existing identifiers of this type
            if (!personIdentifierList.isEmpty()) {
                List<PersonIdentifier> toRemove = new ArrayList<PersonIdentifier>();
                for (PersonIdentifier pi : personIdentifierList) {
                    if (pi.getIdentifierType().equals(personIdentifier.getIdentifierType())) {
                        toRemove.add(pi);
                    }
                }
                personIdentifierList.removeAll(toRemove);
            }
        }
        return personIdentifierList;
    }

    public void setClinicId(String clinicId) throws MalformedCliniIdException {
        PersonIdentifier personIdentifier = OECReception.createPersonIdentifier(clinicId);
        if (personIdentifier == null) {
            throw new MalformedCliniIdException();
        }
        List<PersonIdentifier> personIdentifierList = preparedPersonIdentifierList(personIdentifier);
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

    public void setKisumuHdssId(String kisumuHdssId) {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(kisumuHdssId);
        personIdentifier.setIdentifierType(PersonIdentifier.Type.kisumuHdssId);
        List<PersonIdentifier> personIdentifierList = preparedPersonIdentifierList(personIdentifier);
        personIdentifierList.add(personIdentifier);
        person.setPersonIdentifierList(personIdentifierList);
    }

    public String getKisumuHdssId() {
        String kisumuHdssId = "";
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        if (personIdentifierList != null) {
            for (PersonIdentifier personIdentifier : personIdentifierList) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.kisumuHdssId) {
                    kisumuHdssId = personIdentifier.getIdentifier();
                    break;
                }
            }
        }
        return kisumuHdssId;
    }

    public void setMPIIdentifier(String mpiPersonIdentifier) throws IllegalArgumentException {
        PersonIdentifier personIdentifier = new PersonIdentifier();
        personIdentifier.setIdentifier(mpiPersonIdentifier);
        personIdentifier.setIdentifierType(PersonIdentifier.Type.masterPatientRegistryId);
        List<PersonIdentifier> personIdentifierList = preparedPersonIdentifierList(personIdentifier);
        personIdentifierList.add(personIdentifier);
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

    public void addFingerprint(ImagedFingerprint imagedFingerprint) {
        if (person.getFingerprintList() == null) {
            person.setFingerprintList(new ArrayList<Fingerprint>());
        } else {
            person.getFingerprintList().clear();
        }
        if (imagedFingerprint != null && !imagedFingerprint.isSent()
                && !imagedFingerprint.isPlaceholder()) {
            person.getFingerprintList().add(imagedFingerprint.getFingerprint());
            imagedFingerprint.setSent(true);
        }
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

    public void setOtherName(String lastName) {
        person.setOtherName(lastName);
    }

    public String getOtherName() {
        String lastName = person.getOtherName();
        if (lastName == null) {
            return "";
        }
        return lastName;
    }

    public void setClanName(String lastName) {
        person.setClanName(lastName);
    }

    public String getClanName() {
        String lastName = person.getClanName();
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
    
    public void setAliveStatus(Person.AliveStatus aliveStatus) {
        person.setAliveStatus(aliveStatus);
    }
    
    public Person.AliveStatus getAliveStatus() {
        return person.getAliveStatus();
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

    public List<Fingerprint> getFingerprintList() {
        return person.getFingerprintList();
    }

    public void setFingerprintList(List<Fingerprint> fingerprintList) {
        person.setFingerprintList(fingerprintList);
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String requestReference) {
        this.reference = requestReference;
    }

    public List<Notification> getNotificationList() {
        List<Notification> notificationList = new ArrayList<Notification>();
        if (person.getLastMoveDate() != null) {
            String previousVillageName = person.getPreviousVillageName();
            String additionalInformation = "";
            if (previousVillageName != null) {
                additionalInformation = this.getLongName() + " migrated from '" + previousVillageName + "' village to '"
                        + this.getVillageName() + "' village on "
                        + new SimpleDateFormat("dd/MM/yyyy").format(person.getLastMoveDate()) + ".";
            } else {
                additionalInformation = this.getLongName() + " migrated from an unspecified village to '"
                        + this.getVillageName() + "' village on an unspecified date.";
            }
            notificationList.add(new Notification(this, Notification.Type.MIGRATION, person.getLastMoveDate(), additionalInformation));
        }
        if (person.getExpectedDeliveryDate() != null) {
            String additionalInformation = "";
            additionalInformation = this.getLongName() + " is pregnant. Her expected delivery date is "
                    + new SimpleDateFormat("dd/MM/yyyy").format(person.getExpectedDeliveryDate()) + ".";
            notificationList.add(new Notification(this, Notification.Type.PREGNANCY, person.getExpectedDeliveryDate(), additionalInformation));
        }
        if (person.getPregnancyEndDate() != null) {
            Person.PregnancyOutcome pregnancyOutcome = person.getPregnancyOutcome();
            String additionalInformation = this.getLongName();
            if (pregnancyOutcome != null) {
                if (pregnancyOutcome == Person.PregnancyOutcome.multipleBirths) {
                    additionalInformation = additionalInformation + " had multiple births";
                } else if (pregnancyOutcome == Person.PregnancyOutcome.singleBirth) {
                    additionalInformation = additionalInformation + " had a single birth";
                } else if (pregnancyOutcome == Person.PregnancyOutcome.stillBirth) {
                    additionalInformation = additionalInformation + " had a still birth";
                }
            } else {
                additionalInformation = additionalInformation + "'s pregnancy came to an unspecified end";
            }
            additionalInformation = additionalInformation + " on "
                    + new SimpleDateFormat("dd/MM/yyyy").format(person.getPregnancyEndDate()) + ".";
            notificationList.add(new Notification(this, Notification.Type.PREGNANCY_OUTCOME, person.getPregnancyEndDate(), additionalInformation));
        }
        if (person.getDeathdate() != null) {
            String additionalInformation = "";
            additionalInformation = this.getLongName() + " died on "
                    + new SimpleDateFormat("dd/MM/yyyy").format(person.getDeathdate()) + ".";
            notificationList.add(new Notification(this, Notification.Type.DEATH, person.getDeathdate(), additionalInformation));
        }
        return notificationList;
    }

    public String getShortName() {
        String firstName = this.getFirstName();
        String middleName = this.getMiddleName();
        String lastName = this.getLastName();
        if (!firstName.isEmpty()
                || !middleName.isEmpty()) {
            return (firstName + " " + middleName).trim().replace("  ", " ");
        } else if (!firstName.isEmpty()
                || !lastName.isEmpty()) {
            return (firstName + " " + lastName).trim().replace("  ", " ");
        } else if (!middleName.isEmpty()
                || !lastName.isEmpty()) {
            return (middleName + " " + middleName).trim().replace("  ", " ");
        } else {
            return this.getLongName();
        }
    }

    public String getLongName() {
        return (this.getFirstName() + " " + this.getMiddleName() + " " + this.getLastName()).trim().replace("  ", " ");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersonWrapper other = (PersonWrapper) obj;
        if (this.person != other.person && (this.person == null || !this.person.equals(other.person))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.person != null ? this.person.hashCode() : 0);
        return hash;
    }
}
