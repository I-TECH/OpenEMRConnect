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
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;

/**
 * Represents a name string value for matching.
 * <p>
 * Name matching extends string matching as follows: string matching can look
 * for approximate matches such as edit distance that can detect typographic errors.
 * Name matching does this too. But name matching also assumes that the item
 * to be matched is a spoken word that may be misspelled or alternately spelled
 * depending on how it sounds. So name matching may use approximate matching
 * based on word sounds.
 * 
 * @author Jim Grace
 */
public class NameMatch extends StringMatch {

    /** Object for Soundex calculations. */
    private static Soundex soundex = new Soundex();
    /** Object for RefinedSoundex calculations. */
    private static RefinedSoundex refinedSoundex = new RefinedSoundex();
    /** Object for DoubleMetaphone calculations. */
    private static DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
    /** (modified) Soundex */
    private String soundexValue = null;
    /** (modified) Soundex */
    private String refinedSoundexValue = null;
    /** Metaphone result 1 */
    private String metaphone1 = null;
    /** Metaphone result 2 */
    private String metaphone2 = null;

    /**
     * Construct a NameMatch from a name string.
     * <p>
     * Information about the name is extracted and stored ahead of time for quick matching.
     * For names coming from the database, this information is extracted when all the
     * database values are loaded into memory. Then a database value can be compared
     * more quickly with multiple searches. For names coming from the search terms,
     * this information is extracted before comparing the search terms with all
     * the database values. Then a search term can be compared more quickly with
     * multiple database values.
     *
     * @param original the name string to use in matching.
     */
    public NameMatch(String original) {
        super(original);
        if (original != null) {
            String modifiedOriginal = "A" + original;
            soundexValue = soundex.encode(modifiedOriginal);
            refinedSoundexValue = refinedSoundex.encode(modifiedOriginal);
            metaphone1 = doubleMetaphone.doubleMetaphone(original);
            metaphone2 = doubleMetaphone.doubleMetaphone(original, true);
        }
    }

    /**
     * Finds the score for a match between two names.
     * 
     * @param s The Scorecard in which to add the score.
     * @param other The other name to match against.
     */
    public void score(Scorecard s, NameMatch other) {
        Double score = computeScore(this, other);
        if (score != null) {
            s.addScore(score, 1.0);
        }
    }

    /**
     * Computes the score as a result of matching two NameMatch objects,
     * using approximate matching.
     * The score is returned as a double precision floating point number on a
     * scale of 0 to 1, where 0 means no match at all, and 1 means a perfect match.
     * <p>
     * Returns 1 if the names are an exact match.
     * Returns between 0 and 1 if the names are not an exact match but the
     * edit distance between the two is not large (string match) and/or the
     * two strings sound alike using one or more sound-alike functions (e.g., Soundex).
     * <p>
     * Returns null if one or both of the strings is null.
     * 
     * @param n1 the first name to match
     * @param n2 the second name to match
     * @return the score from matching the two names.
     */
    public static Double computeScore(NameMatch n1, NameMatch n2) {
        Double score = StringMatch.computeScore(n1, n2);
        if (score != null && score < 1.0) {
            if (n1.soundexValue.equals(n2.soundexValue)
                    || n1.refinedSoundexValue.equals(n2.refinedSoundexValue)
                    || n1.metaphone1.equals(n2.metaphone1)
                    || n1.metaphone2.equals(n2.metaphone2)
                    || n1.metaphone1.equals(n2.metaphone2)
                    || n1.metaphone2.equals(n2.metaphone1)) {
                score = 0.8 + (score / 5.0);
            }
            Mediator.getLogger(NameMatch.class.getName()).log(Level.FINEST,
                    "NameMatch.computeScore({0},{1}) = {2}", new Object[]{n1.getOriginal(), n2.getOriginal(), score});
        }
        return score;
    }
}
