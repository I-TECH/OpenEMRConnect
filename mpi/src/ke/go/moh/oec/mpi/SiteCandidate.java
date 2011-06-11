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
 *
 * @author Jim Grace
 */
public class SiteCandidate implements Comparable {

    private SiteMatch siteMatch;
    private int score;
    private String sitePersonIdentifier;

    public SiteCandidate(SiteMatch siteMatch, int score, String identifier) {
        this.siteMatch = siteMatch;
        this.score = score;
        this.sitePersonIdentifier = siteMatch.getSiteCode() + "-" + identifier;
    }

    public int getScore() {
        return score;
    }

    public SiteMatch getSiteMatch() {
        return siteMatch;
    }

    public String getSitePersonIdentifier() {
        return sitePersonIdentifier;
    }

    public int compareTo(Object other) {
        SiteCandidate cOther = (SiteCandidate) other;
        if (cOther.score != score) {
            return cOther.score - score;
        } else {
            int siteCode = siteMatch.getSiteCode();
            int otherSiteCode = cOther.siteMatch.getSiteCode();
            return (siteCode - otherSiteCode);
        }
    }
}
