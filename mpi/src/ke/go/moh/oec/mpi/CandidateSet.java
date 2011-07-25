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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import ke.go.moh.oec.Person;

/**
 * Contains a list of candidates that will be (or could be) returned
 * by the findPatient method. Actually, two lists of candidates are
 * maintained, ones that contain a fingerprint match, and ones that
 * do not. For the fingerprint match list, all matching candidates
 * are returned. For the non-fingerprint match list, all matching
 * candidates are returned up to a maximum number. If more than this
 * maximum number qualify, the maximum number with the highest scores
 * are returned.
 * 
 * @author Jim Grace
 */
public class CandidateSet {

    public static final int MIN_SCORE = 50;
    private static final int MAX_SET_SIZE = 8;
    private TreeSet<Candidate> candidateSet = new TreeSet<Candidate>();
    private TreeSet<Candidate> fingerprintMatchedSet = new TreeSet<Candidate>();
    private int minScore = 100;

    /**
     * Adds a qualifying candidate to the set to be returned. Although if
     * the candidate does not match on fingerprint, and if the score is
     * lower than all candidates currently in the set, the candidate will
     * not be added to the set.
     * <p>
     * If the non-fingerprint candidate set is already at the maximum size
     * and the candidate has a higher score than any of the current set members,
     * the current member with the lowest score will be dropped, and the
     * candidate added in its place.
     * 
     * @param personMatch Describes the person to be added as a candidate.
     * @param scorecard Describes the person's matching score.
     */
    public synchronized void add(PersonMatch personMatch, Scorecard scorecard) {
        int score = scorecard.getScore();
        if (scorecard.isFingerprintMatched()) {
            fingerprintMatchedSet.add(new Candidate(personMatch, scorecard));
        } else if (candidateSet.size() < MAX_SET_SIZE) {
            candidateSet.add(new Candidate(personMatch, scorecard));
            if (score < minScore) {
                minScore = score;
            }
        } else if (score > minScore) {
            candidateSet.remove(candidateSet.last());
            candidateSet.add(new Candidate(personMatch, scorecard));
            minScore = candidateSet.last().getScore();
        }
    }

    /**
     * Exports a list of Person objects from the candidate set. Before
     * the export, the list of fingerprint matches is merged into the
     * list of non-fingerprint matches. So just call this function once,
     * when you are through matching!
     * <p>
     * Programming note: the Person objects are returned with the
     * matching score set, and the indication of whether they were a
     * fingerprint match is also set. We want to avoid setting these
     * values in the person object that is the in-memory copy of the
     * database. So we do a shallow clone of the in-memory database
     * person object. Then we can set these return object properties.
     * 
     * @return list of Person objects from the candidate set.
     */
    public List<Person> export() {
        List<Person> personList = null;
        if (!candidateSet.isEmpty() || !fingerprintMatchedSet.isEmpty()) {
            candidateSet.addAll(fingerprintMatchedSet);
            personList = new ArrayList<Person>();
            for (Candidate c : candidateSet) {
                Person p = c.getPersonMatch().getPerson().clone();
                p.setMatchScore(c.getScore());
                p.setSiteName(c.getSiteName());
                p.setFingerprintMatched(c.isFingerprintMatched());
                personList.add(p);
            }
        }
        return personList;
    }
}
