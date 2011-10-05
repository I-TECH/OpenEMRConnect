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
 * Represents the three names of a person for matching.
 * When match person names, the names will match with the highest score if
 * they are in the same order (matching first name against first name,
 * middle name against middle name, and last name against last name.)
 * However names will still match if they are out of order, just at a slightly
 * lower score. (For example a first name search might best match the middle
 * name, middle name match the first name and last name match the last name.)
 * <p>
 * Note that in some countries (like Kenya) a person is said to have three names,
 * while in other countries (like the U.S.) a person is said to have three
 * parts to their (one) name. The documentation in this module uses the former
 * convention. This class represents the three names of a person: first, middle and last.
 * 
 * @author Jim Grace
 */
public class PersonNamesMatch {

    /** The first, middle and last names for this person. */
    private NameMatch[] nameArray = new NameMatch[3];
    /** The weight (how many names are present) for this person. */
    private double weight = 0.0;
    /** All the combinations for matching 3 names against 3 test names. */
    static final int[][] combinations = {
        {0, 1, 2}, // first<->first, middle<->middle, last<->last
        {0, 2, 1}, // first<->first, middle<->last, last<->middle
        {1, 0, 2}, // first<->middle, middle<->first, last<->last
        {1, 2, 0}, // first<->middle, middle<->last, last<->first
        {2, 0, 1}, // first<->last, middle<->first, last<->middle
        {2, 1, 0}}; // first<->last, middle<->middle, last<->first

    /**
     * Constructs a PersonNamesMatch from first, middle and last names.
     * 
     * @param firstName person's first name
     * @param middleName person's middle name
     * @param lastName person's last name
     */
    public PersonNamesMatch(String firstName, String middleName, String lastName) {
        add(firstName, 0);
        add(middleName, 1);
        add(lastName, 2);
    }

    /**
     * Adds one of the person's names to our name array.
     * Used by the constructor.
     * 
     * @param name person's name to be added.
     * @param position array position where to add it.
     */
    private void add(String name, int position) {
        if (name != null && name.trim().length() != 0) {
            nameArray[position] = NameMatch.getNameMatch(name);
            weight++; // Count of names that are present.
        }
    }

    /**
     * Finds the score for a match between two sets of three person names.
     * 
     * @param s The Scorecard in which to add the score.
     * @param other The other set of person names to match against.
     */
    public void score(Scorecard s, PersonNamesMatch other) {
        double minWeight = Math.min(weight, other.weight);
        if (minWeight > 0) { // Do both persons have at least one name?
            /*
             * Construct a 3 x 3 array, matching each of one person's 3 names
             * against each of the other person's 3 names.
             * 
             * This array is constructed ahead of time so we only have
             * to compute the match once between any pair of names.
             */
            Double[][] scores = new Double[3][3];
            for (int i = 0; i < 3; i++) {
                NameMatch thisName = nameArray[i];
                if (thisName != null) {
                    for (int j = 0; j < 3; j++) {
                        NameMatch otherName = other.nameArray[j];
                        if (otherName != null) {
                            Double dScore = NameMatch.computeScore(thisName, otherName);
                            if (i != j && dScore != null) {
                                dScore *= 0.9; // Penalty for matching names but out of order.
                            }
                            scores[i][j] = dScore;
                        }
                    }
                }
            }
            /*
             * Go through all six combinations of matching one set of 3 names
             * with another set of 3 names. Find which of these combinations
             * produces the best score, and use that.
             */
            double maxScore = 0.0;
            for (int[] combo : combinations) {
                Double dScore = 0.0;
                for (int i = 0; i < 3; i++) {
                    Double d = scores[i][combo[i]];
                    if (d != null) {
                        dScore += d;
                    }
                }
                if (dScore > maxScore) {
                    maxScore = dScore;
                }
            }
            s.addScore(maxScore / minWeight, minWeight);
            if (Mediator.testLoggerLevel(Level.FINEST)) {
                Mediator.getLogger(PersonNamesMatch.class.getName()).log(Level.FINEST,
                        "Score {0},{1} total {2},{3} matching {4} {5} {6} with {7} {8} {9}",
                        new Object[]{maxScore / minWeight, minWeight, s.getTotalScore(), s.getTotalWeight(),
                            naName(nameArray[0]), naName(nameArray[1]), naName(nameArray[2]),
                            naName(other.nameArray[0]), naName(other.nameArray[1]), naName(other.nameArray[2])});
            }
        }
    }

    /**
     * Gets original name from a NameMatch, protecting against nulls.
     * 
     * @param str the NameMatch to fetch the name from.
     * @return "(null)" if the NameMatch or the name is null, otherwise the name.
     */
    private String naName(NameMatch nm) {
        String returnString = "(null)";
        if (nm != null) {
            if (nm.getOriginal() != null) {
                returnString = nm.getOriginal();
            }
        }
        return returnString;
    }
}