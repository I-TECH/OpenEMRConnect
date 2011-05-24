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
import java.util.List;

/**
 * Describe a person with respect to a person index. This may be a person
 * who is already in a person index, a person who is to be added to a
 * person index, or a person who is to be searched for in a person index.
 *
 * @author Jim Grace
 */
public class Person implements Cloneable {

    public enum MaritalStatus {

        marriedPolygamous,
        marriedMonogamous,
        divorced,
        widowed,
        cohabitating
    }

    public enum Sex {

        F,
        M
    }

    public enum AliveStatus {

        yes,
        no
    }

    public enum ConsentSigned {

        yes,
        no,
        notAnswered
    }

    public enum PregnancyOutcome {

        stillBirth,
        singleBirth,
        multipleBirths
    }
    /**
     * A Globally Unique IDentifier (GUID) for this person, as stored in the
     * person index being referenced.
     */
    private String personGuid;
    /** A person's first (or given) name. */
    private String firstName;		// Need from HDSS add/modify
    /** A person's middle (Luo: juok) name. */
    private String middleName;		// Need from HDSS add/modify
    /** A person's last (or family) name. */
    private String lastName;		// Need from HDSS add/modify
    /** Any other name by which a person is commonly known. */
    private String otherName;		 // Need from HDSS add/modify
    /** The name of the clan to which a person belongs. */
    private String clanName;		// Need from HDSS add/modify
    /** The person's gender: M or F. */
    private Sex sex;		// Need from HDSS add/modify
    /** The person's date of birth. */
    private Date birthdate;		// Need from HDSS add/modify
    /** The person's date of death (if any). */
    private Date deathdate;		// Need from HDSS add/modify
    /** Whether a person is living at the moment: 0 = N/A, 1 = Yes, 2 = No (only used as a person search term) */
    private AliveStatus aliveStatus;
    /** The first (or given) name of the person's mother. */
    private String mothersFirstName;		// Need from HDSS add/modify
    /** The middle (Luo: juok) name of the person's mother. */
    private String mothersMiddleName;		// Need from HDSS add/modify
    /** The last (or family) name of the person's mother. */
    private String mothersLastName;		// Need from HDSS add/modify
    /** The first (or given) name of the person's father. */
    private String fathersFirstName;		// Need from HDSS add/modify
    /** The middle (Luo: juok) name of the person's father. */
    private String fathersMiddleName;		// Need from HDSS add/modify
    /** The last (or family) name of the person's father. */
    private String fathersLastName;		// Need from HDSS add/modify
    /** The first (or given) name of the head of the person's compound. */
    private String compoundHeadFirstName;		// Need from HDSS add/modify
    /** The middle (Luo: juok) name of the head of the person's compound. */
    private String compoundHeadMiddleName;		// Need from HDSS add/modify
    /** The last (or family) name of the head of the person's compound. */
    private String compoundHeadLastName;		// Need from HDSS add/modify
    /** The name of the site from which a remote visiting patient comes (only used as a person search term.) */
    private String siteName;
    /**
     * The name of the village in which the person lives.
     * If the person moves outside the HDSS area to a known location or area,
     * the HDSS should provide here the name of the place to which they moved.
     * If the person moves outside the HDSS area to an unknown location,
     * the village name should be null.
     */
    private String villageName;		// Need from HDSS add/modify
    /** The name of the village in which the person previously lived most recently. */
    private String previousVillageName;
    /**
     * Date of most recent move between villages.
     * Only needed from the HDSS when the villageName changes.
     */
    private Date lastMoveDate;		// Need from HDSS modify
    /** The person's marital status */
    private MaritalStatus maritalStatus;		// Need from HDSS add/modify
    /** Has the person given consent for HDSS data to be transfered to clinics? */
    private ConsentSigned consentSigned;
    /** Expected Delivery Date if pregnant */
    private Date expectedDeliveryDate;		// Need from HDSS add/modify
    /** Date at which the most recent pregnancy was ended */
    private Date pregnancyEndDate;		// Need from HDSS add/modify
    /** Most recent pregnancy outcome, one of the following values:
     *       Live birth
     *       Still birth
     */
    private PregnancyOutcome pregnancyOutcome;		// Need from HDSS add/modify
    /** Information for the person's most recent regular clinic visit (if any) */
    private Visit lastRegularVisit;
    /** Information for the person's most recent one-off clinic visit (if any) */
    private Visit lastOneOffVisit;
    /** The score when matching this person in a person/patient index (returns with match results) */
    private int matchScore;
    /** A list containing each {@link PersonIdentifier} assigned to this person. */
    private List<PersonIdentifier> personIdentifierList;		// Need from HDSS add/modify
    /** A list containing each {@link Fingerprint} taken from this person. */
    private List<Fingerprint> fingerprintList;		// Need from HDSS add/modify (HDSS ID only)
    /** A list of household members */
    private List<RelatedPerson> householdMembers;	// Need on HDSS findPerson query response
    /** Used on a findPerson response: there was a fingerprint match for this candidate. */
    private boolean fingerprintMatched;

    /**
     * Does a shallow clone of a Person object.
     * @return the cloned Person object.
     */
    @Override
    public Person clone() {
        try {
            return (Person) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen
            throw new InternalError(e.toString());
        }
    }

    public AliveStatus getAliveStatus() {
        return aliveStatus;
    }

    public void setAliveStatus(AliveStatus aliveStatus) {
        this.aliveStatus = aliveStatus;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public String getCompoundHeadFirstName() {
        return compoundHeadFirstName;
    }

    public void setCompoundHeadFirstName(String compoundHeadFirstName) {
        this.compoundHeadFirstName = compoundHeadFirstName;
    }

    public String getCompoundHeadLastName() {
        return compoundHeadLastName;
    }

    public void setCompoundHeadLastName(String compoundHeadLastName) {
        this.compoundHeadLastName = compoundHeadLastName;
    }

    public String getCompoundHeadMiddleName() {
        return compoundHeadMiddleName;
    }

    public void setCompoundHeadMiddleName(String compoundHeadMiddleName) {
        this.compoundHeadMiddleName = compoundHeadMiddleName;
    }

    public ConsentSigned getConsentSigned() {
        return consentSigned;
    }

    public void setConsentSigned(ConsentSigned consentSigned) {
        this.consentSigned = consentSigned;
    }

    public Date getDeathdate() {
        return deathdate;
    }

    public void setDeathdate(Date deathdate) {
        this.deathdate = deathdate;
    }

    public Date getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
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

    public List<Fingerprint> getFingerprintList() {
        return fingerprintList;
    }

    public void setFingerprintList(List<Fingerprint> fingerprintList) {
        this.fingerprintList = fingerprintList;
    }

    public boolean isFingerprintMatched() {
        return fingerprintMatched;
    }

    public void setFingerprintMatched(boolean fingerprintMatched) {
        this.fingerprintMatched = fingerprintMatched;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<RelatedPerson> getHouseholdMembers() {
        return householdMembers;
    }

    public void setHouseholdMembers(List<RelatedPerson> householdMembers) {
        this.householdMembers = householdMembers;
    }

    public Date getLastMoveDate() {
        return lastMoveDate;
    }

    public void setLastMoveDate(Date lastMoveDate) {
        this.lastMoveDate = lastMoveDate;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Visit getLastOneOffVisit() {
        return lastOneOffVisit;
    }

    public void setLastOneOffVisit(Visit lastOneOffVisit) {
        this.lastOneOffVisit = lastOneOffVisit;
    }

    public Visit getLastRegularVisit() {
        return lastRegularVisit;
    }

    public void setLastRegularVisit(Visit lastRegularVisit) {
        this.lastRegularVisit = lastRegularVisit;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
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

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getPersonGuid() {
        return personGuid;
    }

    public void setPersonGuid(String personGuid) {
        this.personGuid = personGuid;
    }

    public List<PersonIdentifier> getPersonIdentifierList() {
        return personIdentifierList;
    }

    public void setPersonIdentifierList(List<PersonIdentifier> personIdentifierList) {
        this.personIdentifierList = personIdentifierList;
    }

    public Date getPregnancyEndDate() {
        return pregnancyEndDate;
    }

    public void setPregnancyEndDate(Date pregnancyEndDate) {
        this.pregnancyEndDate = pregnancyEndDate;
    }

    public PregnancyOutcome getPregnancyOutcome() {
        return pregnancyOutcome;
    }

    public void setPregnancyOutcome(PregnancyOutcome pregnancyOutcome) {
        this.pregnancyOutcome = pregnancyOutcome;
    }

    public String getPreviousVillageName() {
        return previousVillageName;
    }

    public void setPreviousVillageName(String previousVillageName) {
        this.previousVillageName = previousVillageName;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }
}
