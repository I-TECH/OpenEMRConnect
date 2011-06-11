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

/**
 * Implements the edit distance algorithms Levenshtein and Damerau-Levenshtein.
 *
 * @author Jim Grace
 */
public class Levenshtein {

    /**
     * Returns the minimum of three integers.
     * 
     * @param a First integer to compare.
     * @param b Second integer to compare.
     * @param c Third integer to compare.
     * @return minimum of the three integers.
     */
    private static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * Computes the Levenshtein distance between two strings. A description of
     * the Levenshtein distance, along with pseudocode can be found at
     * http://en.wikipedia.org/wiki/Levenshtein_distance
     * 
     * @param s1 first string to compare.
     * @param s2 second string to compare.
     * @return Levenshtein distance between the two strings.
     */
    public static int levenshteinDistance(CharSequence s1, CharSequence s2) {

        /*
         * Note: Java string indices are 0-based. Pseudocode for the Levenshtein
         * distance code often assummes 1-based string indices for simplicity of
         * the pseudocode. When this happens,
         * a pseudocode index of "i" is coded as "i - 1", and
         * a pseudocode index of "i - 1" is coded as "i - 2".
         */

        // Allocate an integer matrix of s1.length() + 1 rows and s2.length() + 1 columns.
        int length1 = s1.length();
        int length2 = s2.length();
        int[][] matrix = new int[length1 + 1][length2 + 1];

        for (int i = 0; i <= length1; i++) {
            matrix[i][0] = i;
        }
        for (int j = 1; j <= length2; j++) { // (Note: matrix[0][0] is already initialized.)
            matrix[0][j] = j;
        }

        for (int i = 1; i <= length1; i++) {
            for (int j = 1; j <= length2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                matrix[i][j] = min(
                        matrix[i - 1][j] + 1, // Deletion
                        matrix[i][j - 1] + 1, // Insertion
                        matrix[i - 1][j - 1] + cost); // Match or Substitution
            }
        }
        return matrix[length1][length2];
    }

    /**
     * Computes the Damerau-Levenshtein distance between two strings. A description of
     * the Damerau-Levenshtein distance, along with pseudocode can be found at
     * http://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance
     * 
     * @param s1 first string to compare.
     * @param s2 second string to compare.
     * @return Damerau-Levenshtein distance between the two strings.
     */
    public static int damerauLevenshteinDistance(String s1, String s2) {

        /*
         * Note: Java string indices are 0-based. Pseudocode for the Damerau-Levenshtein
         * distance code often assummes 1-based string indices for simplicity of
         * the pseudocode. When this happens,
         * a pseudocode index of "i" is coded as "i - 1", and
         * a pseudocode index of "i - 1" is coded as "i - 2".
         */

        // Allocate an integer matrix of s1.length() + 1 rows and s2.length() + 1 columns.
        int length1 = s1.length();
        int length2 = s2.length();
        int[][] matrix = new int[length1 + 1][length2 + 1];

        for (int i = 0; i <= length1; i++) {
            matrix[i][0] = i;
        }
        for (int j = 1; j <= length2; j++) { // (Note: matrix[0][0] is already initialized.)
            matrix[0][j] = j;
        }

        for (int i = 1; i <= length1; i++) {
            for (int j = 1; j <= length2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                matrix[i][j] = min(
                        matrix[i - 1][j] + 1, // Deletion
                        matrix[i][j - 1] + 1, // Insertion
                        matrix[i - 1][j - 1] + cost); // Match or Substitution
                if (i > 1 && j > 1 && s1.charAt(i - 1) == s2.charAt(j - 2) && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    matrix[i][j] = Math.min(
                            matrix[i][j],
                            matrix[i - 2][j - 2] + cost); // Transposition
                }
            }
        }
        return matrix[length1][length2];
    }
}
