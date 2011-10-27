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

import ke.go.moh.oec.mpi.match.SiteMatch;

/**
 * Clinic that might match a visiting ID. A visiting client may give their
 * local ID from another clinic, but without that clinic's 5-digit clinic ID.
 * Instead, they may give the other clinic name. We do approximate matching
 * on the clinic name, to see which of these clinics may have a matching ID.
 * 
 * @author Jim Grace
 */
public class SiteCandidate implements Comparable<SiteCandidate> {

    /** Site that may match the searched for clinic name. */
    private SiteMatch siteMatch;
    /** Match score between the search clinic name and this clinic name. */
    private double score;
    /** The person identifier that may refer to this site. */
    private String sitePersonIdentifier;

    /**
     * Constructs a SiteCandidate, given the site, the matching score with the
     * search term identifier, and the identifier with which it might match.
     * 
     * @param siteMatch Site that may match the searched for clinic name.
     * @param score Match score between the search clinic name and this clinic name.
     * @param identifier The person identifier that may refer to this site.
     */
    public SiteCandidate(SiteMatch siteMatch, double score, String identifier) {
        this.siteMatch = siteMatch;
        this.score = score;
        this.sitePersonIdentifier = siteMatch.getSiteCode() + "-" + identifier;
    }

    public double getScore() {
        return score;
    }

    public SiteMatch getSiteMatch() {
        return siteMatch;
    }

    public String getSitePersonIdentifier() {
        return sitePersonIdentifier;
    }

    /**
     * Compares this site candidate to another one.
     * This is a required method for a TreeSet of sites.
     * A site is "less than" another site (closer to the beginning of the
     * TreeSet) if the score is higher. If the scores are the same,
     * the difference in site codes is used to determine the order.
     * 
     * @param other Site to compare to.
     * @return less than 0 if this site is "less than" the other (this site score is higher than the other),
     * greater than 0 if this site is "greater than" the other (this site score is lower than the other).
     */
    public int compareTo(SiteCandidate other) {
        int intThisScore = (int) (1000000000.0 * score);
        int intOtherScore = (int) (1000000000.0 * other.score);
        if (intThisScore != intOtherScore) {
            return intOtherScore - intThisScore;
        } else {
            int siteCode = siteMatch.getSiteCode();
            int otherSiteCode = other.siteMatch.getSiteCode();
            return (siteCode - otherSiteCode);
        }
    }
}
