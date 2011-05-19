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
 * from the database. Allows various tests to add to the score so that
 * the score can be averaged in the end. Records whether there was a
 * fingerptint match or not.
 * 
 * @author Jim Grace
 */
public class Scorecard {
    private int sum = 0;
    private int count = 0;
    private boolean fingerprintMatched = false;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isFingerprintMatched() {
        return fingerprintMatched;
    }

    public void setFingerprintMatched(boolean fingerprintMatched) {
        this.fingerprintMatched = fingerprintMatched;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public void addScore(int score) {
        this.sum += score;
        count++;
    }
    
    public int getScore() {
        return sum / count;
    }
}
