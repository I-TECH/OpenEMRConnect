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

import java.util.logging.Level;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.mpi.Scorecard;

/**
 * Represents a string value for matching.
 * 
 * @author Jim Grace
 */
public class StringMatch {

    /**
     * Defines different ways of matching.
     * Normal matching just considers the edit distance between two strings.
     * Substring matching considers that one string may be a substring of the other.
     */
    public enum MatchType {

        NORMAL,
        SUBSTRING;
    }
    /** Original string (all lower case and trimmed) */
    private String original = null;

    public StringMatch(String original) {
        if (original != null) {
            original = original.toLowerCase().trim();
            if (original.length() > 0) {
                this.original = original; // (For matching, always store empty string as null.)
            }
        }
    }

    public String getOriginal() {
        return original;
    }

    /**
     * Scores this StringMatch for possible matching against another, using a scorecard.
     * 
     * @param s scorecard
     * @param other other string to compare with
     */
    public void score(Scorecard s, StringMatch other) {
        if (original == null) {
            s.addScore(Scorecard.SEARCH_TERM_MISSING_WEIGHT, 0, Scorecard.SearchTerm.MISSING);
        } else if (other.original == null) {
            s.addScore(Scorecard.MPI_VALUE_MISSING_WEIGHT, 0);
        } else {
            Double score = computeScore(this, other);
            if (score != null) {
                double weight = Scorecard.OTHER_MATCH_WEIGHT;
                if (score == 0) {
                    weight = Scorecard.OTHER_MISS_WEIGHT;
                }
                s.addScore(weight, score);
                if (Mediator.testLoggerLevel(Level.FINEST)) {
                    Mediator.getLogger(StringMatch.class.getName()).log(Level.FINEST,
                            "Score {0},{1} total {2},{3},{4} comparing {5} with {6}",
                            new Object[]{score, weight, s.getTotalScore(), s.getTotalWeight(), s.getSearchTermScore(), original, other.original});
                }
            }
        }
    }

    /**
     * Computes the score as a result of matching two StringMatch objects,
     * using approximate matching.
     * The score is returned as a double precision floating point number on a
     * scale of 0 to 1, where 0 means no match at all, and 1 means a perfect match.
     * <p>
     * Returns a score of 1 if the two strings are identical.
     * Returns between 0 and 1 if the strings are not identical, but
     * are close according to the edit distance between the two strings.
     * <p>
     * Returns null if one or both of the strings is null.
     * <p>
     * For a string match of type SUBSTRING, it is assumed that one string may
     * be a substring of the other. This is used, for example, for site matching.
     * The site name may be "Siaya District Hospital", while the search term
     * entered for this may be "Siaya". For a SUBSTRING match, we expect that
     * a substring may have a high score, but there may be many unmatched characters
     * in one of the strings.
     * 
     * @param sm1 the first string to match
     * @param sm2 the second string to match
     * @param type type of string match to use
     * @return the score from matching the two strings.
     * Returns null if one or the other string is null.
     */
    public static Double computeScore(StringMatch sm1, StringMatch sm2, MatchType type) {
        Double score = null;
        String s1 = sm1.original;
        String s2 = sm2.original;
        if (s1 != null && s2 != null) {
            score = 0.0;
            if (s1.equals(s2)) {
                score = 1.0;
            } else {
                int distance = Levenshtein.damerauLevenshteinDistance(s1, s2);
                switch (type) {
                    case NORMAL:
                        break;

                    case SUBSTRING:
                        int lengthDiff = Math.abs(s1.length() - s2.length());
                        if (lengthDiff > 2) {
                            distance -= (lengthDiff - 2);
                            break;
                        }
                }
                score = 1.0 - (0.2 * distance);
                if (score < 0.0) {
                    score = 0.0;
                }
            }
            Mediator.getLogger(StringMatch.class.getName()).log(Level.FINEST,
                    "StringMatch.computeScore({0},{1}) = {2}", new Object[]{s1, s2, score});
        }
        return score;
    }

    /**
     * Computes the score as a result of matching two StringMatch objects,
     * using approximate matching, and using NORMAL string matching.
     * See the documentation for the other computeScore() overload.
     *
     * @param sm1 the first string to match
     * @param sm2 the second string to match
     * @return the score from matching the two strings.
     */
    public static Double computeScore(StringMatch sm1, StringMatch sm2) {
        return computeScore(sm1, sm2, StringMatch.MatchType.NORMAL);
    }
}
