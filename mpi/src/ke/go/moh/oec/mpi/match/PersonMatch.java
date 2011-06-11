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
package ke.go.moh.oec.mpi.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.mpi.Scorecard;
import ke.go.moh.oec.mpi.SiteCandidate;

/**
 * Represents a Person for matching.
 * 
 * @author Jim Grace
 */
public class PersonMatch {

    /**
     * Stores all the data that doesn't need to be pre-processed
     * for approximate matching.
     */
    private Person person;
    /** Database person_id (primary key) for this person */
    private int DbPersonId;
    /** A person's first (or given) name. */
    private NameMatch firstNameMatch;
    /** The person's date of birth. */
    private DateMatch birthdateMatch;
    /** A person's middle (Luo: juok) name. */
    private NameMatch middleNameMatch;
    /** A person's last (or family) name. */
    private NameMatch lastNameMatch;
    /** Any other name by which a person is commonly known. */
    private NameMatch otherNameMatch;
    /** The name of the clan to which a person belongs. */
    private NameMatch clanNameMatch;
    /** The first (or given) name of the person's mother. */
    private NameMatch mothersFirstNameMatch;
    /** The middle (Luo: juok) name of the person's mother. */
    private NameMatch mothersMiddleNameMatch;
    /** The last (or family) name of the person's mother. */
    private NameMatch mothersLastNameMatch;
    /** The first (or given) name of the person's father. */
    private NameMatch fathersFirstNameMatch;
    /** The middle (Luo: juok) name of the person's father. */
    private NameMatch fathersMiddleNameMatch;
    /** The last (or family) name of the person's father. */
    private NameMatch fathersLastNameMatch;
    /** The first (or given) name of the head of the person's compound. */
    private NameMatch compoundHeadFirstNameMatch;
    /** The middle (Luo: juok) name of the head of the person's compound. */
    private NameMatch compoundHeadMiddleNameMatch;
    /** The last (or family) name of the head of the person's compound. */
    private NameMatch compoundHeadLastNameMatch;
    /** The name of the village in which the person lives. */
    private NameMatch villageNameMatch;
    /** A list containing each FingerprintMatch for this person. */
    private List<FingerprintMatch> fingerprintMatchList;
    /** A list of possible site candidates if the site corresponding to an ID is uncertain. */
    private Set<SiteCandidate> siteCandidateSet;

    /**
     * Construct a PersonMatch from a Person object.
     * <p>
     * Information about the person is extracted and stored ahead of time for quick matching.
     * For persons coming from the database, this information is extracted when all the
     * database values are loaded into memory. Then a database value can be compared
     * more quickly with multiple searches. For the person search terms,
     * this information is extracted before comparing the search terms with all
     * the database values. Then a search term can be compared more quickly with
     * multiple database values.
     *
     * @param p the Person to use in matching.
     */
    public PersonMatch(Person p) {
        person = p;
        birthdateMatch = new DateMatch(p.getBirthdate());
        firstNameMatch = new NameMatch(p.getFirstName());
        middleNameMatch = new NameMatch(p.getMiddleName());
        lastNameMatch = new NameMatch(p.getLastName());
        otherNameMatch = new NameMatch(p.getOtherName());
        clanNameMatch = new NameMatch(p.getClanName());
        mothersFirstNameMatch = new NameMatch(p.getMothersFirstName());
        mothersMiddleNameMatch = new NameMatch(p.getMothersMiddleName());
        mothersLastNameMatch = new NameMatch(p.getMothersLastName());
        fathersFirstNameMatch = new NameMatch(p.getFathersFirstName());
        fathersMiddleNameMatch = new NameMatch(p.getFathersMiddleName());
        fathersLastNameMatch = new NameMatch(p.getFathersLastName());
        compoundHeadFirstNameMatch = new NameMatch(p.getCompoundHeadFirstName());
        compoundHeadMiddleNameMatch = new NameMatch(p.getCompoundHeadMiddleName());
        compoundHeadLastNameMatch = new NameMatch(p.getCompoundHeadLastName());
        villageNameMatch = new NameMatch(p.getVillageName());
        if (p.getFingerprintList() != null) {
            List<FingerprintMatch> fml = new ArrayList<FingerprintMatch>();
            for (Fingerprint f : p.getFingerprintList()) {
                FingerprintMatch mf = new FingerprintMatch(f);
                fml.add(mf);
            }
        }
    }

    public int getDbPersonId() {
        return DbPersonId;
    }

    public void setDbPersonId(int DbPersonId) {
        this.DbPersonId = DbPersonId;
    }

    public DateMatch getBirthdateMatch() {
        return birthdateMatch;
    }

    public void setBirthdateMatch(DateMatch birthdateMatch) {
        this.birthdateMatch = birthdateMatch;
    }

    public NameMatch getClanNameMatch() {
        return clanNameMatch;
    }

    public void setClanNameMatch(NameMatch clanNameMatch) {
        this.clanNameMatch = clanNameMatch;
    }

    public NameMatch getCompoundHeadFirstNameMatch() {
        return compoundHeadFirstNameMatch;
    }

    public void setCompoundHeadFirstNameMatch(NameMatch compoundHeadFirstNameMatch) {
        this.compoundHeadFirstNameMatch = compoundHeadFirstNameMatch;
    }

    public NameMatch getCompoundHeadLastNameMatch() {
        return compoundHeadLastNameMatch;
    }

    public void setCompoundHeadLastNameMatch(NameMatch compoundHeadLastNameMatch) {
        this.compoundHeadLastNameMatch = compoundHeadLastNameMatch;
    }

    public NameMatch getCompoundHeadMiddleNameMatch() {
        return compoundHeadMiddleNameMatch;
    }

    public void setCompoundHeadMiddleNameMatch(NameMatch compoundHeadMiddleNameMatch) {
        this.compoundHeadMiddleNameMatch = compoundHeadMiddleNameMatch;
    }

    public NameMatch getFathersFirstNameMatch() {
        return fathersFirstNameMatch;
    }

    public void setFathersFirstNameMatch(NameMatch fathersFirstNameMatch) {
        this.fathersFirstNameMatch = fathersFirstNameMatch;
    }

    public NameMatch getFathersLastNameMatch() {
        return fathersLastNameMatch;
    }

    public void setFathersLastNameMatch(NameMatch fathersLastNameMatch) {
        this.fathersLastNameMatch = fathersLastNameMatch;
    }

    public NameMatch getFathersMiddleNameMatch() {
        return fathersMiddleNameMatch;
    }

    public void setFathersMiddleNameMatch(NameMatch fathersMiddleNameMatch) {
        this.fathersMiddleNameMatch = fathersMiddleNameMatch;
    }

    public List<FingerprintMatch> getFingerprintMatchList() {
        return fingerprintMatchList;
    }

    public void setFingerprintMatchList(List<FingerprintMatch> fingerprintMatchList) {
        this.fingerprintMatchList = fingerprintMatchList;
    }

    public NameMatch getFirstNameMatch() {
        return firstNameMatch;
    }

    public void setFirstNameMatch(NameMatch firstNameMatch) {
        this.firstNameMatch = firstNameMatch;
    }

    public NameMatch getLastNameMatch() {
        return lastNameMatch;
    }

    public void setLastNameMatch(NameMatch lastNameMatch) {
        this.lastNameMatch = lastNameMatch;
    }

    public NameMatch getMiddleNameMatch() {
        return middleNameMatch;
    }

    public void setMiddleNameMatch(NameMatch middleNameMatch) {
        this.middleNameMatch = middleNameMatch;
    }

    public NameMatch getMothersFirstNameMatch() {
        return mothersFirstNameMatch;
    }

    public void setMothersFirstNameMatch(NameMatch mothersFirstNameMatch) {
        this.mothersFirstNameMatch = mothersFirstNameMatch;
    }

    public NameMatch getMothersLastNameMatch() {
        return mothersLastNameMatch;
    }

    public void setMothersLastNameMatch(NameMatch mothersLastNameMatch) {
        this.mothersLastNameMatch = mothersLastNameMatch;
    }

    public NameMatch getMothersMiddleNameMatch() {
        return mothersMiddleNameMatch;
    }

    public void setMothersMiddleNameMatch(NameMatch mothersMiddleNameMatch) {
        this.mothersMiddleNameMatch = mothersMiddleNameMatch;
    }

    public NameMatch getOtherNameMatch() {
        return otherNameMatch;
    }

    public void setOtherNameMatch(NameMatch otherNameMatch) {
        this.otherNameMatch = otherNameMatch;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Set<SiteCandidate> getSiteCandidateSet() {
        return siteCandidateSet;
    }

    public void setSiteCandidateSet(Set<SiteCandidate> siteCandidateSet) {
        this.siteCandidateSet = siteCandidateSet;
    }

    public NameMatch getVillageNameMatch() {
        return villageNameMatch;
    }

    public void setVillageNameMatch(NameMatch villageNameMatch) {
        this.villageNameMatch = villageNameMatch;
    }

    /**
     * Allocate and fill a scorecard that describes this person match.
     * <p>
     * Note that this matches everything about the person except fingerprints.
     * Fingerprints are handled separately because they require a different
     * prepared fingerprint context for the search terms for each concurrent thread.
     * 
     * @param p The person to match with (either as a search term or a database entry).
     * @return The scorecard for this person match.
     */
    public Scorecard scorePersonMatch(PersonMatch p) {
        Scorecard s = new Scorecard();
        if ((p.getPerson().getAliveStatus() == Person.AliveStatus.yes && person.getDeathdate() != null)
                || (person.getAliveStatus()) == Person.AliveStatus.yes && p.getPerson().getDeathdate() != null) {
        } else {
            scoreSex(s, person.getSex(), p.getPerson().getSex());
            birthdateMatch.score(s, p.birthdateMatch);
            firstNameMatch.score(s, p.firstNameMatch);
            middleNameMatch.score(s, p.middleNameMatch);
            lastNameMatch.score(s, p.lastNameMatch);
            otherNameMatch.score(s, p.otherNameMatch);
            clanNameMatch.score(s, p.clanNameMatch);
            mothersFirstNameMatch.score(s, p.mothersFirstNameMatch);
            mothersMiddleNameMatch.score(s, p.mothersMiddleNameMatch);
            mothersLastNameMatch.score(s, p.mothersLastNameMatch);
            fathersFirstNameMatch.score(s, p.fathersFirstNameMatch);
            fathersMiddleNameMatch.score(s, p.fathersMiddleNameMatch);
            fathersLastNameMatch.score(s, p.fathersLastNameMatch);
            compoundHeadFirstNameMatch.score(s, p.compoundHeadFirstNameMatch);
            compoundHeadMiddleNameMatch.score(s, p.compoundHeadMiddleNameMatch);
            compoundHeadLastNameMatch.score(s, p.compoundHeadLastNameMatch);
            villageNameMatch.score(s, p.villageNameMatch);
            scorePersonIdentifiers(s, p.getPerson());
        }
        return s;
    }

    /**
     * Score a comparison of two genders.
     *
     * @param s The scorecard in which to record the score.
     * @param sex1 One gender to compare.
     * @param sex2 The other gender to compare.
     */
    private void scoreSex(Scorecard s, Person.Sex sex1, Person.Sex sex2) {
        if (sex1 != null && sex2 != null) {
            if (sex1.ordinal() == sex2.ordinal()) {
                s.addScore(100);
            } else {
                s.addScore(0);
            }
        }
    }

    /**
     * Score a comparison between two lists of person identifiers.
     * <p>
     * Note that when identifiers do not match, no score is added to the scorecard.
     * This is because people may have different identifiers, and a
     * non-match is not considered to reduce the overall average score.
     * But a positive match is given a high score.
     *
     * @param s The scorecard in which to record the score.
     * @param p1 One person identifier list to compare.
     * @param p2 The other person identifier list to compare.
     */
    private void scorePersonIdentifiers(Scorecard s, Person p) {
        Person pSearch = this.getPerson();
        List<PersonIdentifier> list1 = pSearch.getPersonIdentifierList();
        List<PersonIdentifier> list2 = p.getPersonIdentifierList();
        if (list1 != null && list2 != null) {
            for (PersonIdentifier pi1 : list1) {
                for (PersonIdentifier pi2 : list2) {
                    if (pi1.getIdentifierType() == pi2.getIdentifierType()) {
                        if (identifierNeedsSite(pi1)) {
                            for (SiteCandidate sc : this.getSiteCandidateSet()) {
                                String identifier = sc.getSitePersonIdentifier();
                                if (identifier.equals(pi2.getIdentifier())) {
                                    s.addScore(100);
                                }
                            }
                        }
                        if (pi1.getIdentifier().equals(pi2.getIdentifier())) {
                            s.addScore(100);
                        }
                    }
                }
            }
        }
    }
    public static boolean identifierNeedsSite(PersonIdentifier pi) {
        boolean returnValue = false;
        if (pi.getIdentifierType() == PersonIdentifier.Type.cccLocalId) {
            String identifier = pi.getIdentifier();
            if (!identifier.matches("$[0-9]<5>-")) {
                returnValue = true;
            }
        }
        return returnValue;
    }
}
