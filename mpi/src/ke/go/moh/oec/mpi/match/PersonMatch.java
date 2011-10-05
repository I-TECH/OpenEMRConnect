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
import java.util.logging.Level;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;
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
    /** Person's three names. */
    private PersonNamesMatch self;
    /** Mother's three names. */
    private PersonNamesMatch mother;
    /** Father's three names. */
    private PersonNamesMatch father;
    /** Head of Compound's three names. */
    private PersonNamesMatch compoundHead;
    /** The person's date of birth. */
    private DateMatch birthdateMatch;
    /** Any other name by which a person is commonly known. */
    private NameMatch otherNameMatch;
    /** The name of the clan to which a person belongs. */
    private NameMatch clanNameMatch;
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
        self = new PersonNamesMatch(p.getFirstName(), p.getMiddleName(), p.getLastName());
        mother = new PersonNamesMatch(p.getMothersFirstName(), p.getMothersMiddleName(), p.getMothersLastName());
        father = new PersonNamesMatch(p.getFathersFirstName(), p.getFathersMiddleName(), p.getFathersLastName());
        compoundHead = new PersonNamesMatch(p.getCompoundHeadFirstName(), p.getCompoundHeadMiddleName(), p.getCompoundHeadLastName());
        otherNameMatch = NameMatch.getNameMatch(p.getOtherName());
        clanNameMatch = NameMatch.getNameMatch(p.getClanName());
        villageNameMatch = NameMatch.getNameMatch(p.getVillageName());
        if (p.getFingerprintList() != null) {
            fingerprintMatchList = new ArrayList<FingerprintMatch>();
            for (Fingerprint f : p.getFingerprintList()) {
                FingerprintMatch mf = new FingerprintMatch(f);
                fingerprintMatchList.add(mf);
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

    public PersonNamesMatch getCompoundHead() {
        return compoundHead;
    }

    public void setCompoundHead(PersonNamesMatch compoundHead) {
        this.compoundHead = compoundHead;
    }

    public PersonNamesMatch getFather() {
        return father;
    }

    public void setFather(PersonNamesMatch father) {
        this.father = father;
    }

    public List<FingerprintMatch> getFingerprintMatchList() {
        return fingerprintMatchList;
    }

    public void setFingerprintMatchList(List<FingerprintMatch> fingerprintMatchList) {
        this.fingerprintMatchList = fingerprintMatchList;
    }

    public PersonNamesMatch getMother() {
        return mother;
    }

    public void setMother(PersonNamesMatch mother) {
        this.mother = mother;
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

    public PersonNamesMatch getSelf() {
        return self;
    }

    public void setSelf(PersonNamesMatch self) {
        this.self = self;
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
            self.score(s, p.self);
            mother.score(s, p.mother);
            father.score(s, p.father);
            compoundHead.score(s, p.compoundHead);
            otherNameMatch.score(s, p.otherNameMatch);
            clanNameMatch.score(s, p.clanNameMatch);
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
        if (sex1 == null) {
            s.addScore(Scorecard.SEARCH_TERM_MISSING_WEIGHT, 0, Scorecard.SearchTerm.MISSING);
        } else if (sex2 == null) {
            s.addScore(Scorecard.MPI_VALUE_MISSING_WEIGHT, 0);
        } else {
            double score = 0.0; // Score if sex doesn't match.
            double weight = Scorecard.SEX_MISS_WEIGHT; // Weight if sex doesn't match.
            if (sex1.ordinal() == sex2.ordinal()) {
                score = 1.0; // Score if sex matches.
                weight = Scorecard.SEX_MATCH_WEIGHT; // Weignt if sex matches.
            }
            s.addScore(score, weight);
            if (Mediator.testLoggerLevel(Level.FINEST)) {
                Mediator.getLogger(DateMatch.class.getName()).log(Level.FINEST,
                        "Score {0},{1} total {2},{3},{4} comparing {5} with {6}",
                        new Object[]{score, weight, s.getTotalScore(), s.getTotalWeight(), s.getSearchTermScore(), sex1.name(), sex2.name()});
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
        if (list1 == null) {
            s.addScore(Scorecard.SEARCH_TERM_MISSING_WEIGHT, 0, Scorecard.SearchTerm.MISSING);
        } else if (list2 == null) {
            s.addScore(Scorecard.MPI_VALUE_MISSING_WEIGHT, 0);
        } else {
            for (PersonIdentifier pi1 : list1) {
                for (PersonIdentifier pi2 : list2) {
                    if (pi1.getIdentifierType() == pi2.getIdentifierType()) {
                        if (identifierNeedsSite(pi1) && this.getSiteCandidateSet() != null) {
                            for (SiteCandidate sc : this.getSiteCandidateSet()) {
                                String identifier = sc.getSitePersonIdentifier();
                                if (identifier.equals(pi2.getIdentifier())) {
                                    recordIdentifierMatchScore(s, identifier);
                                    s.setSiteName(sc.getSiteMatch().getSiteName());
                                    return; // EARLY RETURN, match found, no more looping needed.
                                }
                            }
                        }
                        if (pi1.getIdentifier().equals(pi2.getIdentifier())) {
                            recordIdentifierMatchScore(s, pi1.getIdentifier());
                            return; // EARLY RETURN, match found, no more looping needed.
                        }
                    }
                }
            }
            // If we fall through the end of the loop, it means there was no identifier match.
            s.addScore(0.0, Scorecard.ID_MISS_WEIGHT);
            if (Mediator.testLoggerLevel(Level.FINEST)) {
                Mediator.getLogger(PersonMatch.class.getName()).log(Level.FINEST,
                        "Score {0},{1} total {2},{3},{4} no identifier matched with person {5}",
                        new Object[]{0.0, Scorecard.ID_MISS_WEIGHT, s.getTotalScore(),
                            s.getSearchTermScore(), s.getTotalWeight(), p.getPersonGuid()});
            }
        }
    }

    /**
     * Records an identifier match in the scorecard.
     * 
     * @param s Scorecard in which to record the match.
     * @param matchingIdentifier matching identifier (for tracing in the log).
     */
    private void recordIdentifierMatchScore(Scorecard s, String matchingIdentifier) {
        final double PERSON_IDENTIFIER_SCORE = 1.0;
        s.addScore(PERSON_IDENTIFIER_SCORE, Scorecard.ID_MATCH_WEIGHT);
        if (Mediator.testLoggerLevel(Level.FINEST)) {
            Mediator.getLogger(PersonMatch.class.getName()).log(Level.FINEST,
                    "Score {0},{1} total {2},{3},{4} matching identifier {5}",
                    new Object[]{PERSON_IDENTIFIER_SCORE, Scorecard.ID_MATCH_WEIGHT, s.getTotalScore(),
                        s.getSearchTermScore(), s.getTotalWeight(), matchingIdentifier});
        }
    }

    /**
     * Tests to see if a person identifier needs a site name.
     * 
     * @param pi PersonIdentifier to test
     * @return true if the PersonIdentifier needs a site name to be complete,
     * or false if the PersonIdentifier already includes a site name.
     */
    public static boolean identifierNeedsSite(PersonIdentifier pi) {
        boolean returnValue = false;
        if (pi.getIdentifierType() == PersonIdentifier.Type.cccLocalId) {
            String identifier = pi.getIdentifier();
            if (!identifier.matches("^[0-9]{5}-*.")) {
                returnValue = true;
            }
        }
        return returnValue;
    }
}
