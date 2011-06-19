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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.mpi.CandidateSet;
import ke.go.moh.oec.mpi.match.DateMatch;
import ke.go.moh.oec.mpi.FindPersonThread;
import ke.go.moh.oec.mpi.Mpi;
import ke.go.moh.oec.mpi.Notifier;
import ke.go.moh.oec.mpi.match.PersonMatch;
import ke.go.moh.oec.mpi.Scorecard;
import ke.go.moh.oec.mpi.SearchHistory;
import ke.go.moh.oec.mpi.SiteCandidate;
import ke.go.moh.oec.mpi.Sql;
import ke.go.moh.oec.mpi.ValueMap;

/**
 * Holds in memory the information about all persons in the MPI.
 * <p>
 * The persons are stored in memory as an ArrayList, so they can be
 * referenced by index. This is required for iterating through them to
 * find candidates entries that match search terms. But we also reference
 * them in a HashMap by the person GUID. This allows for fast finding in
 * memory a person based on their GUID.
 * 
 * @author Jim Grace
 */
public class PersonList {

    private List<PersonMatch> personList = new ArrayList<PersonMatch>();
    private Map<String, PersonMatch> personMap = new HashMap<String, PersonMatch>();
    private SiteList siteList;

    public void setSiteList(SiteList siteList) {
        this.siteList = siteList;
    }

    /**
     * Adds a person to our in-memory person list.
     * 
     * @param personMatch the person to be added.
     */
    private void add(PersonMatch personMatch) {
        personList.add(personMatch);
        personMap.put(personMatch.getPerson().getPersonGuid(), personMatch);
    }

    /**
     * Removes a person from our in-memory person list.
     * 
     * @param personMatch the person to be removed.
     */
    private void remove(PersonMatch personMatch) {
        personList.remove(personMatch);
        personMap.remove(personMatch.getPerson().getPersonGuid());
    }

    /**
     * Gets a person from the in-memory list, by list index.
     * 
     * @param index index of the person to get.
     * @return person from the in-memory list.
     */
    public PersonMatch get(int index) {
        return personList.get(index);
    }

    /**
     * Gets a person from the in-memory list, by person GUID.
     * 
     * @param personGuid GUID of the person to search for.
     * @return person from the in-memory list, or null if the GUID was not found.
     */
    public PersonMatch get(String personGuid) {
        return personMap.get(personGuid);
    }

    /**
     * Loads into the PersonList the entire person table from the MPI, along with
     * any joining person relation data.
     */
    public void load() {
        /*
         * Create two connections to the database. One will be used for
         * the main query to the person table. The other will be used for
         * the queries to load a list of identifiers or fingerprints for
         * a single person.
         */
        Connection personConn = Sql.connect();
        Connection listConn = Sql.connect();
        long startTime = System.currentTimeMillis();
        /*
         * For quick debugging, set queryLimit to a limite number of rows,
         * to avoide loading the entire database. For production uses, set
         * queryLimit to zero.
         */
        Calendar cal = Calendar.getInstance(); // Default Calendar, used for getting database date fields.
        String sql = "SELECT p.person_id, p.person_guid, p.sex, p.birthdate, p.deathdate,\n"
                + "       p.first_name, p.middle_name, p.last_name, p.other_name, p.clan_name,\n"
                + "       p.mothers_first_name, p.mothers_middle_name, p.mothers_last_name,\n"
                + "       p.fathers_first_name, p.fathers_middle_name, p.fathers_last_name,\n"
                + "       p.compoundhead_first_name, p.compoundhead_middle_name, p.compoundhead_last_name,\n"
                + "       v.village_name, m.marital_status_name, p.consent_signed,\n"
                + "       MAX(v_reg.visit_date) AS v_reg_date,\n"
                + "       MAX(v_one.visit_date) AS v_one_date,\n"
                + "       MID(MAX(CONCAT(v_reg.visit_date, a_reg.address)),21) AS v_reg_address,\n"
                + "       MID(MAX(CONCAT(v_one.visit_date, a_one.address)),21) AS v_one_address\n"
                + "FROM person p\n"
                + "LEFT OUTER JOIN village v ON v.village_id = p.village_id\n"
                + "LEFT OUTER JOIN marital_status_type m ON m.marital_status_type_id = p.marital_status\n"
                + "LEFT OUTER JOIN visit v_reg ON v_reg.person_id = p.person_id and v_reg.visit_type_id = " + Sql.REGULAR_VISIT_TYPE_ID + "\n"
                + "LEFT OUTER JOIN visit v_one ON v_one.person_id = p.person_id and v_one.visit_type_id = " + Sql.ONE_OFF_VISIT_TYPE_ID + "\n"
                + "LEFT OUTER JOIN address a_reg ON a_reg.address_id = v_reg.address_id\n"
                + "LEFT OUTER JOIN address a_one ON a_one.address_id = v_one.address_id\n"
                + "GROUP BY p.person_id\n"
                + "ORDER BY p.person_id";
        String queryLimitString = Mediator.getProperty("Query.Limit");
        if (queryLimitString != null) {
            sql = sql + "\nLIMIT " + Integer.parseInt(queryLimitString);
        }
        ResultSet rs = Sql.query(personConn, sql);
        int recordCount = 0;
        try {
            while (rs.next()) {
                Date d = rs.getDate("birthdate");
                Person p = new Person();
                int dbPersonId = rs.getInt("person_id");
                p.setPersonGuid(rs.getString("person_guid"));
                p.setSex((Person.Sex) ValueMap.SEX.getVal().get(rs.getString("sex")));
                p.setBirthdate(rs.getDate("birthdate", cal));
                p.setDeathdate(rs.getDate("deathdate", cal));
                p.setFirstName(rs.getString("first_name"));
                p.setMiddleName(rs.getString("middle_name"));
                p.setLastName(rs.getString("last_name"));
                p.setOtherName(rs.getString("other_name"));
                p.setClanName(rs.getString("clan_name"));
                p.setMothersFirstName(rs.getString("mothers_first_name"));
                p.setMothersMiddleName(rs.getString("mothers_middle_name"));
                p.setMothersLastName(rs.getString("mothers_last_name"));
                p.setFathersFirstName(rs.getString("fathers_first_name"));
                p.setFathersMiddleName(rs.getString("fathers_middle_name"));
                p.setFathersLastName(rs.getString("fathers_last_name"));
                p.setCompoundHeadFirstName(rs.getString("compoundhead_first_name"));
                p.setCompoundHeadMiddleName(rs.getString("compoundhead_middle_name"));
                p.setCompoundHeadLastName(rs.getString("compoundhead_last_name"));
                p.setVillageName(rs.getString("village_name"));
                p.setMaritalStatus((Person.MaritalStatus) ValueMap.MARITAL_STATUS.getVal().get(rs.getString("marital_status_name")));
                p.setPersonIdentifierList(PersonIdentifierList.load(listConn, dbPersonId));
                p.setFingerprintList(FingerprintList.load(listConn, dbPersonId));
                p.setLastRegularVisit(Visit.getVisit(rs.getDate("v_reg_date"), rs.getString("v_reg_address")));
                p.setLastOneOffVisit(Visit.getVisit(rs.getDate("v_one_date"), rs.getString("v_one_address")));
                PersonMatch per = new PersonMatch(p);
                per.setDbPersonId(dbPersonId);
                this.add(per);
                if (++recordCount % 1000 == 0) {
                    Mediator.getLogger(PersonList.class.getName()).log(Level.FINE, "Loaded {0}.", recordCount);
                }
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        double timeInterval = (System.currentTimeMillis() - startTime);
        Mediator.getLogger(PersonList.class.getName()).log(Level.FINE,
                "Loaded {0} person entries in {1} milliseconds.",
                new Object[]{personList.size(), timeInterval});
    }

    /**
     * Searches the person list for one or more candidates matching
     * a given set of search terms.
     *
     * @param req Request containing the search terms to look for.
     * @return The response data to the request.
     */
    public Object find(PersonRequest req) {
        PersonResponse resp = new PersonResponse();
        Person p = req.getPerson();
        if (p == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "FIND PERSON called with no person data.");
            return resp;
        }
        PersonMatch searchTerms = new PersonMatch(p);
        CandidateSet candidateSet = new CandidateSet();
        Set<SiteCandidate> siteCandidateSet = siteList.findIfNeeded(searchTerms);
        searchTerms.setSiteCandidateSet(siteCandidateSet);
        DateMatch.setToday();
        if (p.getPersonGuid() != null
                && (p.getFingerprintList() == null || p.getFingerprintList().isEmpty())) {
            PersonMatch match = this.get(p.getPersonGuid());
            if (match != null) {
                Scorecard scorecard = new Scorecard();
                scorecard.addScore(1.0, 1.0);
                candidateSet.add(match, scorecard);
            }
        } else {
            int personMatchCount = personList.size();
            int threadCount = Mpi.getMaxThreadCount();
            if (threadCount > personMatchCount) {
                threadCount = personMatchCount;
            }
            int countPerThread = (personMatchCount + threadCount - 1) / threadCount;
            long startTime = System.currentTimeMillis();
            List<Thread> threadArray = new ArrayList<Thread>();
            for (int i = 0; i < threadCount; i++) {
                int startIndex = countPerThread * i;
                int endIndex = (countPerThread * (i + 1)) - 1;
                if (endIndex >= personMatchCount) {
                    endIndex = personMatchCount - 1;
                }
                FindPersonThread fpt = new FindPersonThread(this, searchTerms, candidateSet, startIndex, endIndex);
                Thread t = new Thread(fpt);
                threadArray.add(t);
                t.start();
            }
            for (Thread t : threadArray) {
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "Error joining FindPersonThread", ex);
                }
            }
            double timeInterval = (System.currentTimeMillis() - startTime);
            Mediator.getLogger(PersonList.class.getName()).log(Level.FINE,
                    "Searched {0} entries in {1} milliseconds.",
                    new Object[]{personMatchCount, timeInterval});
        }
        List<Person> candidateList = candidateSet.export();
        resp.setPersonList(candidateList);
        resp.setSuccessful(true);
        SearchHistory.create(req);
        return resp;
    }

    /**
     * Creates a new person in the database and our in-memory list.
     * 
     * @param PersonRequest person search parameters to be stored
     */
    public Object create(PersonRequest req) {
        PersonResponse returnData = null;
        Person p = req.getPerson().clone(); // Clone because we may modify our copy below.
        if (p == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "CREATE PERSON called with no person data.");
            return returnData;
        }
        Connection conn = Sql.connect();
        ResultSet rs = Sql.query(conn, "select uuid() as uuid");
        String guid = null;
        try {
            rs.next();
            guid = rs.getString("uuid");
            rs.close();
        } catch (SQLException ex) { // Won't happen
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.setPersonGuid(guid);

        String sex = ValueMap.SEX.getDb().get(p.getSex());
        String villageId = Sql.getVillageId(conn, p.getVillageName());
        String maritalStatusId = Sql.getMaritalStatusId(conn, p.getMaritalStatus());
        String consentSigned = ValueMap.CONSENT_SIGNED.getDb().get(p.getConsentSigned());
        String sql = "INSERT INTO person (person_guid, first_name, middle_name, last_name,\n"
                + "       other_name, sex, birthdate, deathdate,\n"
                + "       mothers_first_name, mothers_middle_name, mothers_last_name,\n"
                + "       fathers_first_name, fathers_middle_name, fathers_last_name,\n"
                + "       compoundhead_first_name, compoundhead_middle_name, compoundhead_last_name,\n"
                + "       village_id, marital_status, consent_signed, date_created) values (\n   "
                + Sql.quote(guid) + ", "
                + Sql.quote(p.getFirstName()) + ", "
                + Sql.quote(p.getMiddleName()) + ", "
                + Sql.quote(p.getLastName()) + ",\n   "
                + Sql.quote(p.getOtherName()) + ", "
                + Sql.quote(sex) + ", "
                + Sql.quote(p.getBirthdate()) + ", "
                + Sql.quote(p.getDeathdate()) + ",\n   "
                + Sql.quote(p.getMothersFirstName()) + ", "
                + Sql.quote(p.getMothersMiddleName()) + ", "
                + Sql.quote(p.getMothersLastName()) + ",\n   "
                + Sql.quote(p.getFathersFirstName()) + ", "
                + Sql.quote(p.getFathersMiddleName()) + ", "
                + Sql.quote(p.getFathersLastName()) + ",\n   "
                + Sql.quote(p.getCompoundHeadFirstName()) + ", "
                + Sql.quote(p.getCompoundHeadMiddleName()) + ", "
                + Sql.quote(p.getCompoundHeadLastName()) + ",\n   "
                + villageId + ", "
                + maritalStatusId + ", "
                + consentSigned + ", "
                + "NOW()"
                + ");";
        Sql.startTransaction(conn);
        Sql.execute(conn, sql);
        int dbPersonId = Integer.parseInt(Sql.getLastInsertId(conn));
        PersonIdentifierList.update(conn, dbPersonId, p.getPersonIdentifierList(), null);
        FingerprintList.update(conn, dbPersonId, p.getFingerprintList(), null);
        VisitList.update(conn, Sql.REGULAR_VISIT_TYPE_ID, dbPersonId, p.getLastRegularVisit());
        VisitList.update(conn, Sql.ONE_OFF_VISIT_TYPE_ID, dbPersonId, p.getLastOneOffVisit());
        Sql.commit(conn);
        PersonMatch newPer = new PersonMatch(p.clone()); // Clone to protect from unit test modifications.
        newPer.setDbPersonId(dbPersonId);
        this.add(newPer);
        SearchHistory.update(req, null, null); // Update search history showing that no candidate was selected.
        if (req.isResponseRequested()) {
            returnData = new PersonResponse();
            List<Person> returnList = new ArrayList<Person>();
            returnList.add(p);
            returnData.setPersonList(returnList);
        }
        return returnData;
    }

    /**
     * Modifies a person entry in the database and in our in-memory list.
     * 
     * @param req The modify request.
     */
    public Object modify(PersonRequest req) {
        PersonResponse returnData = null;
        Person p = req.getPerson();
        if (p == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with no person data.");
            return returnData;
        }

        String personGuid = p.getPersonGuid();
        if (p == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with no person GUID.");
            return returnData;
        }

        PersonMatch oldPer = this.get(personGuid);
        if (oldPer == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON GUID {0} not found.", personGuid);
            return returnData;
        }

        SearchHistory.update(req, oldPer, p); // Log the search result (if any) BEFORE modifying the person.
        int dbPersonId = oldPer.getDbPersonId();
        Person oldP = oldPer.getPerson();
        Connection conn = Sql.connect();
        String sex = ValueMap.SEX.getDb().get(p.getSex());
        String villageId = Sql.getVillageId(conn, p.getVillageName());
        String maritalStatusId = Sql.getMaritalStatusId(conn, p.getMaritalStatus());
        String consentSigned = ValueMap.CONSENT_SIGNED.getDb().get(p.getConsentSigned());
        String sql = "UPDATE person set\n       "
                + "first_name = " + Sql.quote(p.getFirstName()) + ", "
                + "middle_name = " + Sql.quote(p.getMiddleName()) + ", "
                + "last_name = " + Sql.quote(p.getLastName()) + ",\n       "
                + "other_name = " + Sql.quote(p.getOtherName()) + ", "
                + "sex = " + Sql.quote(sex) + ", "
                + "birthdate = " + Sql.quote(p.getBirthdate()) + ", "
                + "deathdate = " + Sql.quote(p.getDeathdate()) + ",\n       "
                + "mothers_first_name = " + Sql.quote(p.getMothersFirstName()) + ", "
                + "mothers_middle_name = " + Sql.quote(p.getMothersMiddleName()) + ", "
                + "mothers_last_name = " + Sql.quote(p.getMothersLastName()) + ",\n       "
                + "fathers_first_name = " + Sql.quote(p.getFathersFirstName()) + ", "
                + "fathers_middle_name = " + Sql.quote(p.getFathersMiddleName()) + ", "
                + "fathers_last_name = " + Sql.quote(p.getFathersLastName()) + ",\n       "
                + "compoundhead_first_name = " + Sql.quote(p.getCompoundHeadFirstName()) + ", "
                + "compoundhead_middle_name = " + Sql.quote(p.getCompoundHeadMiddleName()) + ", "
                + "compoundhead_last_name = " + Sql.quote(p.getCompoundHeadLastName()) + ",\n       "
                + "village_id = " + villageId + ", "
                + "marital_status = " + maritalStatusId + ", "
                + "consent_signed = " + consentSigned + ", "
                + "date_changed = NOW()\n"
                + "WHERE person_id = " + dbPersonId;
        Sql.startTransaction(conn);
        Sql.execute(conn, sql);
        List<PersonIdentifier> pList = PersonIdentifierList.update(conn, dbPersonId, p.getPersonIdentifierList(), oldP.getPersonIdentifierList());
        p.setPersonIdentifierList(pList);
        List<Fingerprint> fList = FingerprintList.update(conn, dbPersonId, p.getFingerprintList(), oldP.getFingerprintList());
        p.setFingerprintList(fList);
        VisitList.update(conn, Sql.REGULAR_VISIT_TYPE_ID, dbPersonId, p.getLastRegularVisit());
        VisitList.update(conn, Sql.ONE_OFF_VISIT_TYPE_ID, dbPersonId, p.getLastOneOffVisit());
        if (p.getLastRegularVisit() == null) {
            p.setLastRegularVisit(oldP.getLastRegularVisit());
        }
        if (p.getLastOneOffVisit() == null) {
            p.setLastOneOffVisit(oldP.getLastOneOffVisit());
        }
        Sql.commit(conn);
        PersonMatch newPer = new PersonMatch(p);
        newPer.setDbPersonId(dbPersonId);
        this.remove(oldPer); // Remove old person from our in-memory list.
        this.add(newPer); // Add new person to our in-memory list.
        Notifier.notify(p);
        if (req.isResponseRequested()) {
            returnData = new PersonResponse();
            List<Person> returnList = new ArrayList<Person>();
            returnList.add(p);
            returnData.setPersonList(returnList);
        }
        return returnData;
    }
}
