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

    public void score(Scorecard s, StringMatch other) {
        int score = computeScore(this, other);
        if (score >= 0) {
            s.addScore(score);
        }
    }

    public static int computeScore(StringMatch sm1, StringMatch sm2) {
        int score = -1;
        String s1 = sm1.original;
        String s2 = sm2.original;
        if (s1 != null && s2 != null) {
            score = 0;
            if (s1.equals(s2)) {
                score = 100;
            } else {
                int distance = Levenshtein.damerauLevenshteinDistance(s1, s2);
                int lengthDiff = Math.abs(s1.length() - s2.length());
                if (lengthDiff > 2) {
                    distance -= (lengthDiff - 2);
                }
                score = 100 - (20 * distance);
                if (score < 0) {
                    score = 0;
                }
            }
            Mediator.getLogger(StringMatch.class.getName()).log(Level.FINEST,
                    "StringMatch.computeScore({0},{1}) = {2}", new Object[]{s1, s2, score});
        }
        return score;
    }
}
