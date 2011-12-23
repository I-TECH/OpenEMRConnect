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
 * Represents a site name for matching.
 * 
 * @author Jim Grace
 */
public class SiteMatch {

    int siteCode;
    String siteName;
    NameMatch siteNameMatch;

    /**
     * Constructs an entry from a site code and name.
     * 
     * @param siteCode 5-letter site code.
     * @param siteName Name of the site.
     */
    public SiteMatch(int siteCode, String siteName) {
        this.siteCode = siteCode;
        this.siteName = siteName.trim(); // Just in case.
        // Note: the NameMatch cache is not used, since duplicate site names are not expected.
        this.siteNameMatch = new NameMatch(siteName);
    }

    public int getSiteCode() {
        return siteCode;
    }

    public String getSiteName() {
        return siteName;
    }

    /**
     * Calculates the score for a site name search term matching the name of this site.
     * 
     * @param siteNameSearchTerm Search term for a site name.
     * @return score resulting from the comparison.
     */
    public Double computeScore(NameMatch siteNameSearchTerm) {
        Double score = NameMatch.computeScore(siteNameMatch, siteNameSearchTerm, StringMatch.MatchType.SUBSTRING);
        return (score);
    }
}
