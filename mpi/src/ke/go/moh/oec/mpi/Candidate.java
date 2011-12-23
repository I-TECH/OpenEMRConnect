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
package ke.go.moh.oec.mpi;

import ke.go.moh.oec.mpi.match.PersonMatch;

/**
 * Describes a candidate to be returned in the result set of findPerson.
 *
 * @author Jim Grace
 */
public class Candidate implements Comparable<Candidate> {

    /** Data about the person to be returned. */
    private PersonMatch personMatch;
    /** Match score for this candidate. */
    private int score;
    /** If we did approximate matching on clinic name, which clinic name did we match against? */
    private String siteName;
    /** Did this candidate have a fingerprint match? */
    private boolean fingerprintMatched;

    public Candidate(PersonMatch personMatch, Scorecard s) {
        this.personMatch = personMatch;
        this.score = s.getScore();
        this.siteName = s.getSiteName();
        this.fingerprintMatched = s.isFingerprintMatched();
    }

    public boolean isFingerprintMatched() {
        return fingerprintMatched;
    }

    public PersonMatch getPersonMatch() {
        return personMatch;
    }

    public int getScore() {
        return score;
    }

    public String getSiteName() {
        return siteName;
    }

    /**
     * Compares this candidate to another, to see the order in which
     * they will be returned in the set of candidates.
     * The order is based on the reverse order of the score.
     * In other words, the candidate with the higher score will have a
     * lower rank (be sooner in the set to return.) This is the comparison
     * method called by TreeSet in order to establish and maintain
     * the order of the tree.
     * <p>
     * Programming note: For efficiency, the candidates are stored
     * in a TreeSet (a set implemented as a tree). This allows the
     * set to be stored in order, and if too many candidates qualify
     * the one with the lowest score can be dropped.
     * However, a set does not allow duplicates.
     * More precisely, if a new set element to be added to the set
     * compares by this function as equal to one already in the set,
     * it will not be added.
     * But the set should be able to store two candidates that happen
     * to have the same score.
     * To get around this problem, for candidates with equal score,
     * this comparison function compares the internal database ID
     * of the two candidates. That way two different candidates will
     * always compare as unequal.
     * 
     * @param other Other candidate to compare against.
     * @return less than 0 if this candidate should be earlier in the set
     * (higher score), or greater than 0 if this candidate should be
     * later in the list (lower score.)
     */
    public int compareTo(Candidate other) {
        if (other.score != score) {
            return other.score - score;
        } else {
            int dbId = getPersonMatch().getDbPersonId();
            int otherDbId = other.getPersonMatch().getDbPersonId();
            return (dbId - otherDbId);
        }
    }
}
