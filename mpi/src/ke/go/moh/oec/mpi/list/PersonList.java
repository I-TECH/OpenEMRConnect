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
 * Contributor(sql):
 *
 * ***** END LICENSE BLOCK ***** */
package ke.go.moh.oec.mpi.list;

import java.lang.reflect.Field;
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
                + "       MID(MAX(CONCAT(v_reg.visit_date, a_reg.address)),20) AS v_reg_address,\n"
                + "       MID(MAX(CONCAT(v_one.visit_date, a_one.address)),20) AS v_one_address\n"
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
                p.setConsentSigned((Person.ConsentSigned) ValueMap.CONSENT_SIGNED.getVal().get(rs.getString("consent_signed")));
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
        //
        // Make a special case if we are searching by GUID and not trying to match fingerprints.
        // In this case, just look to see if we have person matching the GUID search term.
        // If we do, then we are done. If not, then we test for matching the usual way...
        //
        PersonMatch guidMatch = null;
        if (p.getPersonGuid() != null
                && (p.getFingerprintList() == null || p.getFingerprintList().isEmpty())) {
            guidMatch = this.get(p.getPersonGuid());
            //TODO: Fix logic.
            if (guidMatch != null) {
                final double GUID_MATCH_SCORE = 1.0;
                final double GUID_MATCH_WEIGHT = 1.0;
                Scorecard s = new Scorecard();
                s.addScore(GUID_MATCH_SCORE, GUID_MATCH_WEIGHT);
                candidateSet.add(guidMatch, s);
                if (Mediator.testLoggerLevel(Level.FINEST)) {
                    Mediator.getLogger(PersonList.class.getName()).log(Level.FINEST,
                            "Score {0},{1} total {2},{3} comparing GUID {4} with {5}",
                            new Object[]{GUID_MATCH_SCORE, GUID_MATCH_WEIGHT, s.getTotalScore(), s.getTotalWeight(),
                                p.getPersonGuid(), guidMatch.getPerson().getPersonGuid()});
                }
            }
        }
        int personMatchCount = personList.size();
        if (guidMatch == null && personMatchCount > 0) { // Skip if matched already, or if MPI is empty
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
        if (req.isResponseRequested()) {    // Has the client requested a response?
            returnData = new PersonResponse();
            returnData.setSuccessful(false); // Until we succeed, assume that we failed.
        }
        Person p = req.getPerson().clone(); // Clone because we may modify our copy below.
        if (p == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "CREATE PERSON called with no person data.");
            return returnData;
        }
        //Check to see if this person'sql hdssid, if available, already exists in the mpi. Log error if it does.
        boolean exists = false;
        if (p.getPersonIdentifierList() != null
                && !p.getPersonIdentifierList().isEmpty()) {
            for (PersonIdentifier personIdentifier : p.getPersonIdentifierList()) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.kisumuHdssId) {
                    if (personIdentifier.getIdentifier() != null
                            && !personIdentifier.getIdentifier().isEmpty()) {
                        List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
                        personIdentifierList.add(personIdentifier);
                        Person find = new Person();
                        find.setPersonIdentifierList(personIdentifierList);
                        PersonRequest personRequest = new PersonRequest();
                        personRequest.setPerson(find);
                        PersonResponse personResponse = (PersonResponse) find(personRequest);
                        if (personResponse != null
                                && personResponse.getPersonList() != null
                                && !personResponse.getPersonList().isEmpty()) {
                            exists = true;
                        }
                        break;
                    }
                }
            }
        }
        if (exists) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "CREATE PERSON called with existing kisumuhdssid person identifier.");
            return returnData;
        }
        Connection conn = Sql.connect();
        ResultSet rs = Sql.query(conn, "SELECT UUID() AS uuid");
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
                + "       other_name, clan_name, sex, birthdate, deathdate,\n"
                + "       mothers_first_name, mothers_middle_name, mothers_last_name,\n"
                + "       fathers_first_name, fathers_middle_name, fathers_last_name,\n"
                + "       compoundhead_first_name, compoundhead_middle_name, compoundhead_last_name,\n"
                + "       village_id, marital_status, consent_signed, date_created) values (\n   "
                + Sql.quote(guid) + ", "
                + Sql.quote(p.getFirstName()) + ", "
                + Sql.quote(p.getMiddleName()) + ", "
                + Sql.quote(p.getLastName()) + ",\n   "
                + Sql.quote(p.getOtherName()) + ", "
                + Sql.quote(p.getClanName()) + ", "
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
        if (returnData != null) {
            List<Person> returnList = new ArrayList<Person>();
            returnList.add(p);
            returnData.setPersonList(returnList);
            returnData.setSuccessful(true); // We have succeeded.
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
        if (req.isResponseRequested()) {    // Has the client requested a response?
            returnData = new PersonResponse();
            returnData.setSuccessful(false); // Until we succeed, assume that we failed.
        }
        Person newPerson = req.getPerson();//person containing modified data
        if (newPerson == null) {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with no person data.");
            return returnData;
        }
        String personGuid = newPerson.getPersonGuid();
        if (personGuid == null
                || personGuid.isEmpty()) {//True if this update is NOT the result of a 'sought and found' person i.e. 
            //it came from kisumuhdss. We now try to find this person (using their kisumuhdssid) just to make sure 
            //we agree with kisumu hdss that they are already known to us. 
            if (newPerson.getPersonIdentifierList() != null
                    && !newPerson.getPersonIdentifierList().isEmpty()) {
                for (PersonIdentifier personIdentifier : newPerson.getPersonIdentifierList()) {
                    if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.kisumuHdssId) {
                        if (personIdentifier.getIdentifier() != null
                                && !personIdentifier.getIdentifier().isEmpty()) {
                            List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
                            personIdentifierList.add(personIdentifier);
                            Person find = new Person();
                            find.setPersonIdentifierList(personIdentifierList);
                            PersonRequest personRequest = new PersonRequest();
                            personRequest.setPerson(find);
                            //Find the person locally. No network communication is involved.
                            PersonResponse personResponse = (PersonResponse) find(personRequest);
                            if (personResponse != null
                                    && personResponse.getPersonList() != null
                                    && !personResponse.getPersonList().isEmpty()) {
                                if (personResponse.getPersonList().isEmpty()) {
                                    Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with non-existing kisumuHdssId person identifier.");
                                    return returnData;
                                } else if (personResponse.getPersonList().size() == 1) {
                                    //We think this person exists once and we know their person guid. We'll use it to
                                    //find him in the personList (this).
                                    personGuid = personResponse.getPersonList().get(0).getPersonGuid();
                                } else {
                                    Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with a duplicated kisumuHdssId person identifier.");
                                    return returnData;
                                }
                            }
                            //We only break if we find a NON-EMPTY kisumuhdssid, otherwise we keep going.
                            break;
                        }
                    }
                }
            }
        }
        PersonMatch oldPersonMatch = null;
        if (personGuid != null
                && !personGuid.isEmpty()) {
            oldPersonMatch = this.get(personGuid);
            if (oldPersonMatch == null) {
                Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with no person GUID.");
                return returnData;
            }
        } else {
            Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, "MODIFY PERSON GUID {0} not found.", personGuid);
            return returnData;
        }
        newPerson.setPersonGuid(personGuid);
        SearchHistory.update(req, oldPersonMatch, newPerson); // Log the search result (if any) BEFORE modifying the person.
        int dbPersonId = oldPersonMatch.getDbPersonId();
        Person oldPerson = oldPersonMatch.getPerson();
        Connection conn = Sql.connect();
        String sex = ValueMap.SEX.getDb().get(newPerson.getSex());
        String villageId = Sql.getVillageId(conn, newPerson.getVillageName());
        String maritalStatusId = Sql.getMaritalStatusId(conn, newPerson.getMaritalStatus());
        String consentSigned = ValueMap.CONSENT_SIGNED.getDb().get(newPerson.getConsentSigned());
        int columnCount = 0;
        String sql = "UPDATE person SET\n";
        if (newPerson.getFirstName() != null) {
            if (newPerson.getFirstName().isEmpty()) {
                sql += (separate(columnCount) + "first_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "first_name = " + Sql.quote(newPerson.getFirstName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getMiddleName() != null) {
            if (newPerson.getMiddleName().isEmpty()) {
                sql += (separate(columnCount) + "middle_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "middle_name = " + Sql.quote(newPerson.getMiddleName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getLastName() != null) {
            if (newPerson.getLastName().isEmpty()) {
                sql += (separate(columnCount) + "last_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "last_name = " + Sql.quote(newPerson.getLastName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getClanName() != null) {
            if (newPerson.getClanName().isEmpty()) {
                sql += (separate(columnCount) + "clan_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "clan_name = " + Sql.quote(newPerson.getClanName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getSex() != null) {
            sql += (separate(columnCount) + "sex = " + Sql.quote(sex) + "\n");
            columnCount++;
        }
        if (newPerson.getBirthdate() != null) {
            sql += (separate(columnCount) + "birthdate = " + Sql.quote(newPerson.getBirthdate()) + "\n");
            columnCount++;
        }
        if (newPerson.getDeathdate() != null) {
            sql += (separate(columnCount) + "deathdate = " + Sql.quote(newPerson.getDeathdate()) + "\n");
            columnCount++;
        }
        if (newPerson.getMothersFirstName() != null) {
            if (newPerson.getMothersFirstName().isEmpty()) {
                sql += (separate(columnCount) + "mothers_first_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "mothers_first_name = " + Sql.quote(newPerson.getMothersFirstName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getMothersMiddleName() != null) {
            if (newPerson.getMothersMiddleName().isEmpty()) {
                sql += (separate(columnCount) + "mothers_middle_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "mothers_middle_name = " + Sql.quote(newPerson.getMothersMiddleName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getMothersLastName() != null) {
            if (newPerson.getMothersLastName().isEmpty()) {
                sql += (separate(columnCount) + "mothers_last_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "mothers_last_name = " + Sql.quote(newPerson.getMothersLastName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getFathersFirstName() != null) {
            if (newPerson.getFathersFirstName().isEmpty()) {
                sql += (separate(columnCount) + "fathers_first_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "fathers_first_name = " + Sql.quote(newPerson.getFathersFirstName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getFathersMiddleName() != null) {
            if (newPerson.getFathersMiddleName().isEmpty()) {
                sql += (separate(columnCount) + "fathers_middle_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "fathers_middle_name = " + Sql.quote(newPerson.getFathersMiddleName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getFathersLastName() != null) {
            if (newPerson.getFathersLastName().isEmpty()) {
                sql += (separate(columnCount) + "fathers_last_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "fathers_last_name = " + Sql.quote(newPerson.getFathersLastName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getCompoundHeadFirstName() != null) {
            if (newPerson.getCompoundHeadFirstName().isEmpty()) {
                sql += (separate(columnCount) + "compoundhead_first_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "compoundhead_first_name = " + Sql.quote(newPerson.getCompoundHeadFirstName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getCompoundHeadMiddleName() != null) {
            if (newPerson.getCompoundHeadMiddleName().isEmpty()) {
                sql += (separate(columnCount) + "compoundhead_middle_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "compoundhead_middle_name = " + Sql.quote(newPerson.getCompoundHeadMiddleName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getCompoundHeadLastName() != null) {
            if (newPerson.getCompoundHeadLastName().isEmpty()) {
                sql += (separate(columnCount) + "compoundhead_last_name = NULL\n");
            } else {
                sql += (separate(columnCount) + "compoundhead_last_name = " + Sql.quote(newPerson.getCompoundHeadLastName()) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getVillageName() != null) {
            if (newPerson.getVillageName().isEmpty()) {
                sql += (separate(columnCount) + "village_id = NULL\n");
            } else {
                sql += (separate(columnCount) + "village_id = " + Sql.quote(villageId) + "\n");
            }
            columnCount++;
        }
        if (newPerson.getMaritalStatus() != null) {
            sql += (separate(columnCount) + "marital_status = " + Sql.quote(maritalStatusId) + "\n");
            columnCount++;
        }
        if (newPerson.getConsentSigned() != null) {
            sql += (separate(columnCount) + "consent_signed = " + Sql.quote(consentSigned) + "\n");
            columnCount++;
        }
        sql += " WHERE person_id = " + dbPersonId;
        if (columnCount != 0) {//some 'real' mpi data has changed and needs to be updated
            Sql.startTransaction(conn);
            Sql.execute(conn, sql);
            List<PersonIdentifier> pList = PersonIdentifierList.update(conn, dbPersonId, newPerson.getPersonIdentifierList(), oldPerson.getPersonIdentifierList());
            newPerson.setPersonIdentifierList(pList);
            List<Fingerprint> fList = FingerprintList.update(conn, dbPersonId, newPerson.getFingerprintList(), oldPerson.getFingerprintList());
            newPerson.setFingerprintList(fList);
            VisitList.update(conn, Sql.REGULAR_VISIT_TYPE_ID, dbPersonId, newPerson.getLastRegularVisit());
            VisitList.update(conn, Sql.ONE_OFF_VISIT_TYPE_ID, dbPersonId, newPerson.getLastOneOffVisit());
            if (newPerson.getLastRegularVisit() == null) {
                newPerson.setLastRegularVisit(oldPerson.getLastRegularVisit());
            }
            if (newPerson.getLastOneOffVisit() == null) {
                newPerson.setLastOneOffVisit(oldPerson.getLastOneOffVisit());
            }
            Sql.commit(conn);
        }
        if (newPerson.getLastMoveDate() != null) {
            newPerson.setPreviousVillageName(oldPerson.getVillageName());
        }
        Person mergedPerson = merge(newPerson, oldPersonMatch.getPerson());//merge old and new person
        PersonMatch newPersonMatch = new PersonMatch(mergedPerson);
        newPersonMatch.setDbPersonId(dbPersonId);
        this.remove(oldPersonMatch); // Remove old person from our in-memory list.
        this.add(newPersonMatch); // Add new person to our in-memory list.
        Notifier.notify(mergedPerson);
        if (returnData != null) {
            List<Person> returnList = new ArrayList<Person>();
            returnList.add(mergedPerson);
            returnData.setPersonList(returnList);
            returnData.setSuccessful(true); // We have succeeded.
        }
        return returnData;
    }

    /**
     * Returns a String containing either a space ' ' or a comma and a space ', ' depending on the
     * number of columns included in the update sql statement.
     * 
     * @param columnCount number of columns in the sql statement
     * @return " " if the count is zero, otherwise ", ".
     */
    private String separate(int columnCount) {
        if (columnCount == 0) {
            return " ";
        } else {
            return ", ";
        }
    }

    /**
     * Merges the fields of two Person objects to override null fields where applicable.
     * Where the same field from both Person objects is non-null, the value of newPerson prevails
     * because it is the one most recently updated.
     * 
     * @param newPerson new Person object
     * @param oldPerson old Person object
     * @return merged Person object
     */
    private Person merge(Person newPerson, Person oldPerson) {
        Person mergedPerson = new Person();
        Class personClass = Person.class;
        Field[] personFields = personClass.getDeclaredFields();
        for (Field field : personFields) {
            try {
                field.setAccessible(true);
                Object p1FieldValue = field.get(newPerson);
                Object p2FieldValue = field.get(oldPerson);
                if (!(p1FieldValue == null && p2FieldValue == null)) {
                    Object p3FieldValue = null;
                    if (p1FieldValue != null && p2FieldValue != null) {
                        p3FieldValue = p1FieldValue;
                    } else {
                        if (p1FieldValue != null && p2FieldValue == null) {
                            p3FieldValue = p1FieldValue;
                        } else if (p1FieldValue == null && p2FieldValue != null) {
                            p3FieldValue = p2FieldValue;
                        }
                    }
                    field.set(mergedPerson, p3FieldValue);
                }
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(PersonList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        mergedPerson.setPersonIdentifierList(mergePersonIdentifierLists(newPerson.getPersonIdentifierList(),
                oldPerson.getPersonIdentifierList()));
        return mergedPerson;
    }
    
    /**
     * Copies over the contents of both newPersonIdentifierList and
     * oldPersonIdentifierList into the mergedPersonIdentifierList
     * 
     * @param newPersonIdentifierList new PersonIdentifier list to merge
     * @param oldPersonIdentifierList old PersonIdentifier list to merge
     * @return merged PersonIdentifier list
     */
    private List<PersonIdentifier> mergePersonIdentifierLists(List<PersonIdentifier> newPersonIdentifierList,
            List<PersonIdentifier> oldPersonIdentifierList) {
        List<PersonIdentifier> mergedPersonIdentifierList = new ArrayList<PersonIdentifier>();
        boolean newExists = (newPersonIdentifierList != null && !newPersonIdentifierList.isEmpty());
        boolean oldExists = (oldPersonIdentifierList != null && !oldPersonIdentifierList.isEmpty());
        if (newExists && oldExists) {
            mergedPersonIdentifierList.addAll(newPersonIdentifierList);
            for (PersonIdentifier personIdentifier : oldPersonIdentifierList) {
                if (!mergedPersonIdentifierList.contains(personIdentifier)) {
                    mergedPersonIdentifierList.add(personIdentifier);
                }
            }
        } else {
            if (newExists && !oldExists) {
                mergedPersonIdentifierList.addAll(newPersonIdentifierList);
            } else if (!newExists && oldExists) {
                mergedPersonIdentifierList.addAll(oldPersonIdentifierList);
            }
        }
        return mergedPersonIdentifierList;
    }
}
