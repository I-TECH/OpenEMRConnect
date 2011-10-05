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
package ke.go.moh.oec.mpi.list;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.mpi.SiteCandidate;
import ke.go.moh.oec.mpi.Sql;
import ke.go.moh.oec.mpi.match.NameMatch;
import ke.go.moh.oec.mpi.match.PersonMatch;
import ke.go.moh.oec.mpi.match.SiteMatch;

/**
 *
 * @author Jim Grace
 */
public class SiteList implements Runnable {

    // The number of candidate sites can be large, because we may not find
    // the given identifier at most of them.
    private static final int MAX_SITE_SET_SIZE = 200;
    private List<SiteMatch> siteList = new ArrayList<SiteMatch>();
    private Map<Integer, SiteMatch> siteMap = new HashMap<Integer, SiteMatch>();

    /**
     * Loads the site list from the database.
     * <p>
     * Implements java.lang.Runnable.run().
     */
    public void run() {
        long startTime = System.currentTimeMillis();
        Connection conn = Sql.connect();
        String sql = "SELECT facility_code, facility_name FROM facility";
        ResultSet rs = Sql.query(conn, sql);
        int recordCount = 0;
        try {
            while (rs.next()) {
                int siteCode = rs.getInt("facility_code");
                String siteName = rs.getString("facility_name");
                SiteMatch s = new SiteMatch(siteCode, siteName);
                siteList.add(s);
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(SiteList.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        double timeInterval = (System.currentTimeMillis() - startTime);
        Mediator.getLogger(SiteList.class.getName()).log(Level.INFO,
                "Loaded {0} site entries in {1} milliseconds.",
                new Object[]{siteList.size(), timeInterval});
    }

    /**
     * Adds a person to our in-memory site list.
     * 
     * @param siteMatch the site to add.
     */
    private void add(SiteMatch siteMatch) {
        siteList.add(siteMatch);
        siteMap.put(siteMatch.getSiteCode(), siteMatch);
    }

    /**
     * Find a set of site candidates that match (approximately) a site name search string.
     * 
     * @param siteName the site name search term to search for.
     * @param identifier the identifier that might be associated with the site.
     * @return the set of candidates whose site name match (approximately) the site name search string.
     */
    public Set<SiteCandidate> find(String siteName, String identifier) {
        List<SiteMatch> siteMatchList = null;
        TreeSet<SiteCandidate> siteCandidateSet = new TreeSet<SiteCandidate>();
        // Note: the NameMatch cache is not used, since duplicate site names are not expected.
        NameMatch testNameMatch = new NameMatch(siteName);
        double minScore = 100;

        for (SiteMatch s : siteList) {
            Double score = s.computeScore(testNameMatch);
            // System.out.println("Site " + s.getSiteCode() + " " + s.getSiteName() + " score " + score);
            if (score != null) {
                if (siteCandidateSet.size() < MAX_SITE_SET_SIZE) {
                    siteCandidateSet.add(new SiteCandidate(s, score, identifier));
                    // System.out.println("---> " + s.getSiteCode() + " " + s.getSiteName() + " score " + score);
                    if (score < minScore) {
                        minScore = score;
                    }
                } else if (score > minScore) {
                    siteCandidateSet.remove(siteCandidateSet.last());
                    siteCandidateSet.add(new SiteCandidate(s, score, identifier));
                    // System.out.println("++++ " + s.getSiteCode() + " " + s.getSiteName() + " score " + score);
                    minScore = siteCandidateSet.last().getScore();
                }
            }
        }
        return siteCandidateSet;
    }

    /**
     * Finds a set of site names that might match a site name search term,
     * if there is a person identifier that needs such a match.
     * 
     * @param searchTerms complete set of search terms for the person
     * @return the set of site names, or null if not applicable.
     */
    public Set<SiteCandidate> findIfNeeded(PersonMatch searchTerms) {
        Set<SiteCandidate> siteCandidateSet = null;
        Person person = searchTerms.getPerson();
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        String searchSiteName = person.getSiteName();
        if (searchSiteName != null && !searchSiteName.isEmpty() && personIdentifierList != null) {
            for (PersonIdentifier pi : personIdentifierList) {
                if (PersonMatch.identifierNeedsSite(pi)) {
                    siteCandidateSet = find(searchSiteName, pi.getIdentifier());
                    break;
                }
            }
        }
        return siteCandidateSet;
    }
}
