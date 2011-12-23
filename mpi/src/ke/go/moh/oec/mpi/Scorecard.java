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

/**
 * Provides data for scoring a set of search terms against a single person
 * from the database, using weighted scores. Allows various tests to add their
 * weighted scores to the scorecard, so that the composite score can be
 * computed in the end.
 * Also records whether there was a fingerprint match or not,
 * and which site, if any, a site-specific person identifier matched against.
 * 
 * @author Jim Grace
 */
public class Scorecard {

    /**
     * Is a given search term missing or present in the patient search?
     */
    public enum SearchTerm {

        MISSING,
        PRESENT
    }

    /** Weight if the search term is missing. */
    public static final double SEARCH_TERM_MISSING_WEIGHT = 0.25;
    /** Weight if the search term is present but the MPI value is missing. */
    public static final double MPI_VALUE_MISSING_WEIGHT = 0.5;
    /** ...MISS_WEIGHT values are the weight to use if there is no match */
    public static final double ID_MISS_WEIGHT = 1.0;
    /** ...MATCH_WEIGHT values are the weight to use if there is a partial or exact match */
    public static final double ID_MATCH_WEIGHT = 2.0;
    public static final double FINGERPRINT_MISS_WEIGHT = 0.75;
    public static final double FINGERPRINT_MATCH_WEIGHT = 4.0;
    public static final double SEX_MISS_WEIGHT = 1.0;
    public static final double SEX_MATCH_WEIGHT = 0.25;
    public static final double DATE_MISS_WEIGHT = 1.0;
    public static final double DATE_MATCH_WEIGHT = 0.4;
    public static final double OTHER_MISS_WEIGHT = 1.0;
    public static final double OTHER_MATCH_WEIGHT = 0.8;
    
    /** Sum of the scores so far. */
    private double totalScore = 0.0;
    /** Sum of the weights so far. */
    private double totalWeight = 0.0;
    /** Sum of the weights so far for search terms that are present in the query. */
    private double presentSearchTermsWeight = 0.0;
    /** Indication whether there was a fingerprint match */
    private boolean fingerprintMatched = false;
    /** Site name, if any, against which we had a site-specific person identifier match. */
    private String siteName = null;
    /** Scale within which to report the final score. */
    private static final int SCORE_SCALE = 100;

    public boolean isFingerprintMatched() {
        return fingerprintMatched;
    }

    public void setFingerprintMatched(boolean fingerprintMatched) {
        this.fingerprintMatched = fingerprintMatched;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String clinicName) {
        this.siteName = clinicName;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Adds a weighted score to the scorecard.
     * <p>
     * The weights are accomplished as follows:
     * The score for each individual item is a double precision floating point number
     * between 0 and 1.
     * The weight for each individual item is a positive double precision floating point number.
     * 
     * @param score raw score to add (0.0 - 1.0)
     * @param weight weight for this score - how heavily it should count.
     * @param searchTermStatus was this search term present in the query?
     */
    public void addScore(double score, double weight, SearchTerm searchTermStatus) {
        totalScore += score * weight;
        totalWeight += weight;
        if (searchTermStatus == SearchTerm.PRESENT) {
            presentSearchTermsWeight += weight;
        }
    }

    /**
     * Same as addScore(double score, double weight, SearchTerm searchTermStatus),
     * but defaults the value of searchTermStatus to PRESENT.
     * 
     * @param score raw score to add (0.0 - 1.0)
     * @param weight weight for this score - how heavily it should count.
     */
    public void addScore(double score, double weight) {
        addScore(score, weight, Scorecard.SearchTerm.PRESENT);
    }

    /**
     * Gets the composite score from this scorecard. The composite score
     * is initially computed as a double precision floating point number
     * by adding all the weighted scores and dividing by the total weights.
     * This gives a score in the range 0.0 to 1.0.
     * This score is then converted to an integer by multiplying it by the
     * scale value SCORE_SCALE to return an integer in the range 0 to SCORE_SCALE.
     * 
     * @return the composite score (including consideration of how many search terms there were.)
     */
    public int getScore() {
        if (totalWeight != 0.0) { // Protect against divide by zero.
            double doubleScore = totalScore / totalWeight;
            int intScore = (int) (doubleScore * SCORE_SCALE);
            return intScore;
        } else {
            return 0;
        }
    }

    /**
     * Gets the composite score, but only weighted by the search terms
     * that were present. This can be useful for setting a cutoff threshold
     * for returning values, based on what search parameters were present.
     * 
     * @return the composite score (only relative to number of search terms.)
     */
    public int getSearchTermScore() {
        if (presentSearchTermsWeight != 0.0) { // Protect against divide by zero.
            double doubleScore = totalScore / presentSearchTermsWeight;
            int intScore = (int) (doubleScore * SCORE_SCALE);
            return intScore;
        } else {
            return 0;
        }
    }
}
