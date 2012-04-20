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
import ke.go.moh.oec.mpi.match.FingerprintMatch;
import ke.go.moh.oec.mpi.list.PersonList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import ke.go.moh.oec.lib.Mediator;

/**
 * One thread that can be used for concurrent processing of a Find Person request.
 *
 * @author Jim Grace
 */
public class FindPersonThread implements Runnable {

    /** The list of person identities in memory against which we will search for candidates. */
    private PersonList personList;
    /** The search terms we will look for. */
    private PersonMatch searchTerms;
    /** The set of candidates we will add to. */
    private CandidateSet candidateSet;
    /** The starting index of the person identifier list in which this thread will look. */
    private int startIndex;
    /** The starting index of the person identifier list in which this thread will look. */
    private int endIndex;

    public FindPersonThread(PersonList personList, PersonMatch searchTerms, CandidateSet candidateSet, int startIndex, int endIndex) {
        this.personList = personList;
        this.searchTerms = searchTerms;
        this.candidateSet = candidateSet;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     * Do the person matching requested for this thread.
     * <p>
     * Implements java.lang.Runnable.run().
     */
    public void run() {
        /*
         * If the search terms include any fingerprints, prepare them for
         * matching. This is done independently for each thread because each
         * thread needs its own prepared context for fingerprint matching.
         * This is done within the thread itself to save time -- multiple
         * threads can be preparing their own fingerprint matching contexts
         * at the same time.
         */
        List<FingerprintMatch> fSearchList = null;
        if (searchTerms.getFingerprintMatchList() != null) {
            fSearchList = new ArrayList<FingerprintMatch>();
            for (FingerprintMatch fm : searchTerms.getFingerprintMatchList()) {
                FingerprintMatch f = fm.clone();
                f.prepare();
                fSearchList.add(f);
            }
        }
        /*
         * Loop through that portion of the person match array that was
         * assigned for processing by this thread.
         *
         * If any person matched against results in a fingerprint match, or
         * a non-fingerprint match above the minimum score threshold, then
         * add the person to the list of candidates.
         *
         * (Note: loop from startIndex to endIndex INCLUSIVE.)
         */
        for (int i = startIndex; i <= endIndex; i++) {
            PersonMatch pm = personList.get(i);
            Scorecard s = searchTerms.scorePersonMatch(pm);
            List<FingerprintMatch> fMatchList = pm.getFingerprintMatchList();
            if (fSearchList == null) {
                s.addScore(Scorecard.SEARCH_TERM_MISSING_WEIGHT, 0, Scorecard.SearchTerm.MISSING);
            } else if (fMatchList == null) {
                s.addScore(Scorecard.MPI_VALUE_MISSING_WEIGHT, 0);
            } else {
                double maxScore = 0.0;
                for (FingerprintMatch fSearch : fSearchList) {
                    for (FingerprintMatch fMatch : fMatchList) {
                        boolean match = fSearch.match(fMatch);
                        if (match) {
                            s.setFingerprintMatched(true);
                            double score = fSearch.score();
                            if (score > maxScore) {
                                maxScore = score;
                            }
                        }
                    }
                }
                double weight = Scorecard.FINGERPRINT_MATCH_WEIGHT;
                if (maxScore == 0.0) {
                    weight = Scorecard.FINGERPRINT_MISS_WEIGHT;
                }
                s.addScore(weight, maxScore);
                if (Mediator.testLoggerLevel(Level.FINEST)) {
                    Mediator.getLogger(FindPersonThread.class.getName()).log(Level.FINEST,
                            "Score {0},{1} total {2},{3},{4} comparing fingerprints with person GUID {5}}",
                            new Object[]{maxScore, weight, s.getTotalScore(), s.getTotalWeight(), s.getSearchTermScore(), pm.getPerson().getPersonGuid()});
                }
            }
            if (s.getSearchTermScore() > CandidateSet.MIN_SCORE || s.isFingerprintMatched()) {
                candidateSet.add(pm, s);
            }
        }
        if (fSearchList != null && !fSearchList.isEmpty()) {
            for (FingerprintMatch f : fSearchList) {
                f.destroy();
            }
        }
    }
}