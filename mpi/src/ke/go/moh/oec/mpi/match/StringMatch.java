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

import ke.go.moh.oec.mpi.Scorecard;


/**
 * Represents a string value for matching.
 * 
 * @author Jim Grace
 */
public class StringMatch {

    /** Original string (all lower case) */
    private String original = null;

    public StringMatch(String original) {
        if (original != null) {
            this.original = original.toLowerCase();
        }
    }

    public int score(Scorecard s, StringMatch other) {
        int returnScore = 0;
        if (original != null && other.original != null) {
            returnScore = stringScore(original, other.original);
            s.addScore(returnScore);
        }
        return returnScore;
    }

    public static int stringScore(String s1, String s2) {
        int returnScore = 0;
        if (s1 != null && s2 != null) {
            if (s1.equals(s2)) {
                returnScore = 100;
            } else {
                returnScore = 0;
            }
        }
        return returnScore;
    }

}
