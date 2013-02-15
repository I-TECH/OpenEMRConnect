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

import ke.go.moh.oec.Visit;
import java.util.ArrayList;
import ke.go.moh.oec.PersonIdentifier;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Connection;
import java.util.List;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Jim Grace
 */
public class MpiTest {

    static Mpi mpi; // Make it static so it won't reinitialize between tests.
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger logger = Logger.getLogger(MpiTest.class.getName());
    private PersonRequest requestData = new PersonRequest();
    private PersonResponse pr;
    private Person p;
    
    public MpiTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        mpi = new Mpi();
        mpi.initialize();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private void removeTestData(){
        // Remove test data, potentially in place from last run
        Connection conn = Sql.connect();
        String s0 = "DELETE FROM visit WHERE person_id IN (SELECT person_id FROM person WHERE first_name = 'Cain' AND middle_name = 'Human' AND last_name = 'One')";
        String s1 = "DELETE FROM person WHERE first_name = 'Cain' AND middle_name = 'Human' AND last_name = 'One';";
        String s2 = "DELETE FROM village WHERE village_name IN ('Eden', 'OutOfEden');";
        Sql.startTransaction(conn);
        Sql.execute(conn, s0);
        Sql.execute(conn, s1);
        Sql.execute(conn, s2);
        Sql.commit(conn);
        Sql.close(conn);        

        // The mpi keeps in memory lists - purge.
        mpi.initialize();

    }
    
    @Before
    public void setUp() {
        removeTestData();
        
        // Create test person - used in subsequent tests
        int requestTypeId = RequestTypeId.CREATE_PERSON_MPI;
        //PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;

        p.setFirstName("Cain");
        p.setMiddleName("Human");
        p.setLastName("One");
        p.setMothersFirstName("Eve");
        p.setMothersMiddleName("Human");
        p.setMothersLastName("One");
        p.setFathersFirstName("Adam");
        p.setFathersMiddleName("Human");
        p.setFathersLastName("One");
        p.setCompoundHeadFirstName("God");
        p.setCompoundHeadMiddleName("The");
        p.setCompoundHeadLastName("Creator");
        p.setVillageName("Eden");
        p.setClanName("Human");
        p.setBirthdate(parseDate("1986-06-15"));
        p.setSex(Person.Sex.M);

        Visit v = new Visit();
        v.setVisitDate(new Date());
        v.setAddress("ke.go.moh.test.address");
        v.setFacilityName("Test Facility");
        p.setLastRegularVisit(v);
        
        result = mpi.getData(requestTypeId, requestData);
        assertNull(result);
    }

    @After
    public void tearDown() {
        removeTestData();
    }

    private String n(String s) { // Protect against nulls for printing.
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    private String n(Date d) { // Protect against nulls for printing.
        if (d == null) {
            return "";
        } else {
            return d.toString();
        }
    }

    private String n(Enum e) { // Protect against nulls for printing.
        if (e == null) {
            return "";
        } else {
            return e.name();
        }
    }

    private String searchTerm(String label, String term) {
        return term == null || term.isEmpty() ? "" : " " + label + ": " + term;
    }

    private PersonResponse callFindPerson(PersonRequest personRequest) {
        Person p = personRequest.getPerson();
        String log = "Searching for"
                + searchTerm("guid", p.getPersonGuid())
                + searchTerm("fname", p.getFirstName())
                + searchTerm("mname", p.getMiddleName())
                + searchTerm("lname", p.getLastName())
                + searchTerm("clan", p.getClanName())
                + searchTerm("other", p.getOtherName())
                + searchTerm("mfname", p.getMothersFirstName())
                + searchTerm("mmname", p.getMothersMiddleName())
                + searchTerm("mlname", p.getMothersLastName())
                + searchTerm("ffname", p.getFathersFirstName())
                + searchTerm("fmname", p.getFathersMiddleName())
                + searchTerm("flname", p.getFathersLastName())
                + searchTerm("cfname", p.getCompoundHeadFirstName())
                + searchTerm("cmname", p.getCompoundHeadMiddleName())
                + searchTerm("clname", p.getCompoundHeadLastName())
                + searchTerm("sex", n(p.getSex()))
                + searchTerm("birth", n(p.getBirthdate()))
                + searchTerm("death", n(p.getDeathdate()))
                + searchTerm("marital", n(p.getMaritalStatus()))
                + searchTerm("village", p.getVillageName())
                + searchTerm("clname", p.getCompoundHeadLastName()
                + searchTerm("site", p.getSiteName()));
        List<PersonIdentifier> piList = p.getPersonIdentifierList();
        if (piList != null) {
            for (PersonIdentifier pi : piList) {
                log += " pi(" + pi.getIdentifierType().name() + "): " + pi.getIdentifier();
            }
        }
        MpiTest.logger.fine(log);
        Object result = mpi.getData(RequestTypeId.FIND_PERSON_MPI, personRequest);
        assertNotNull(result);
        assertSame(PersonResponse.class, result.getClass());
        PersonResponse personResponse = (PersonResponse) result;
        assertTrue(personResponse.isSuccessful());
        List<Person> pList = personResponse.getPersonList();
        if (pList == null || pList.isEmpty()) {
            MpiTest.logger.fine("No persons returned.");
        } else {
            for (Person person : pList) {
                log = "guid: " + person.getPersonGuid()
                        + " score: " + person.getMatchScore()
                        + " name: " + n(person.getFirstName()) + " " + n(person.getMiddleName()) + " " + n(person.getLastName()) + " [" + n(person.getOtherName()) + "]"
                        + " sex: " + n(person.getSex())
                        + " birth/death: " + n(person.getBirthdate()) + "/" + n(person.getDeathdate())
                        + " clan: " + n(person.getClanName())
                        + " mother: " + n(person.getMothersFirstName()) + " " + n(person.getMothersMiddleName()) + " " + n(person.getMothersLastName())
                        + " father: " + n(person.getFathersFirstName()) + " " + n(person.getFathersMiddleName()) + " " + n(person.getFathersLastName())
                        + " compHead: " + n(person.getCompoundHeadFirstName()) + " " + n(person.getCompoundHeadMiddleName()) + " " + n(person.getCompoundHeadLastName())
                        + " village: " + n(person.getVillageName())
                        + " site: " + n(person.getSiteName())
                        + " marital: " + n(person.getMaritalStatus());
                piList = person.getPersonIdentifierList();
                if (piList != null) {
                    for (PersonIdentifier pi : piList) {
                        log += " pi(" + pi.getIdentifierType().name() + "): " + pi.getIdentifier();
                    }
                }
                MpiTest.logger.fine(log);
            }
        }
        return personResponse;
    }

    /**
     * Test of getData method, of class Mpi.
     */
    @Ignore
    @Test
    public void testFindPerson() {
        MpiTest.logger.fine("testFindPerson");

        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;

        // Clan name that will not be found
        MpiTest.logger.fine("testFindPerson - Clan name that will not be found");
        p.setClanName("ThisClanNameWillNotBeFound");
        pr = callFindPerson(requestData);
        assertNull(pr.getPersonList());

        // Clan name from test person
        MpiTest.logger.fine("testFindPerson - Clan name returning 1 match");
        p.setClanName("Human");
        pr = callFindPerson(requestData);
        List<Person> pList = pr.getPersonList();
        assertNotNull(pList);
        int pCount = pList.size();
        assertEquals(1, pCount);
        Person p0 = pList.get(0);
        int score = p0.getMatchScore();
        assertTrue(score >= 80);

        // Birthdate alone (.4) or sex (.25) don't count as match
        // together they should meet the threshold
        MpiTest.logger.fine("testFindPerson - Search by sex & birthdate");
        p = new Person(); // Start fresh
        p.setBirthdate(parseDate("1986-06-15"));
        p.setSex(Person.Sex.M);
        String birthdate = p.getBirthdate().toString();
        requestData.setPerson(p);
        pr = callFindPerson(requestData);
        assertNotNull(pr.getPersonList());
        int listSize = pr.getPersonList().size();
        for (Person q : pr.getPersonList()) {
            score = q.getMatchScore();
            Date dq = q.getBirthdate();
            String ds = dq.toString();
            ds = "DOB: " + dq.toString();
        }

        // Search by a fake GUID should match nobody.
        p = new Person(); // Start fresh
        p.setPersonGuid("fake GUID");
        requestData.setPerson(p);
        pr = callFindPerson(requestData);
        assertNull(pr.getPersonList());
    }

    /**
     * Test of getData method, of class Mpi.
     */
    @Test
    public void testFindSomePeople() {
        MpiTest.logger.fine("testFindSomePeople");

        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;

        // Clan name that will not be found
        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("00007/2004");
        pi.setIdentifierType(PersonIdentifier.Type.cccLocalId);
        List<PersonIdentifier> piList = new ArrayList<PersonIdentifier>();
        piList.add(pi);
        p.setPersonIdentifierList(piList);
        p.setSiteName("Siaya");
        requestData.setPerson(p);
        pr = callFindPerson(requestData);
        // Inspect for results -- expect results if LPI data.
    }

    /**
     * Test of getData method, request type CREATE_PERSON_MPI
     */
    @Test
    public void testCreatePerson() {
        MpiTest.logger.fine("testCreatePerson");

        Person p = new Person();
        pr = callFindPerson(requestData);
        List<Person> pList = pr.getPersonList();
        assertNotNull(pList);
        int pCount = pList.size();
//        assertEquals(1, pCount);

        MpiTest.logger.fine("Exepect a high score: Edit distance = 1 and SOUNDEX Match.");
        p.setMiddleName("Hunan");
        pr = callFindPerson(requestData);

        MpiTest.logger.fine("Slightly lower score: Edit distance = 1 but no SOUNDEX Match.");
        p.setMiddleName("Huxan"); // Slightly lower score: SOUN
        pr = callFindPerson(requestData);

        MpiTest.logger.fine("Somewhat lower score: Edit distance = 3");
        p.setMiddleName("Humanism");
        pr = callFindPerson(requestData);

        MpiTest.logger.fine("Still lower score: Edit distance = 5");
        p.setMiddleName("Hmuanities");
        pr = callFindPerson(requestData);

        MpiTest.logger.fine("Lower score, string completely different (but still other strings matching.)");
        p.setMiddleName("XXXXXXXXX");
        pr = callFindPerson(requestData);

    }

    /**
     * Test of getData method, request type CREATE_PERSON_MPI
     */
    @Ignore
    @Test
    public void testModifyPerson() {
        MpiTest.logger.fine("testModifyPerson");

        int requestTypeId;
        PersonRequest requestData = new PersonRequest();
        Person p;
        List<Person> pList;
        int pCount;
        Object result;
        PersonResponse pr;

        // Find the person to modify.
        p = new Person();
        requestData.setPerson(p);
        p.setVillageName("Eden");
        pr = callFindPerson(requestData);

        pList = pr.getPersonList();
        assertNotNull(pList);
        pCount = pList.size();
        assertEquals(1, pCount);
        Person p0 = pList.get(0);
        assertEquals("Cain", p0.getFirstName());
        assertEquals("Human", p0.getMiddleName());
        assertEquals("One", p0.getLastName());

        MpiTest.logger.fine("Modify the village name.");
        requestTypeId = RequestTypeId.MODIFY_PERSON_MPI;
        p0.setVillageName("OutOfEden");
        requestData.setPerson(p0);
        result = mpi.getData(requestTypeId, requestData);
        assertNull(result); // MODIFY PERSON returns no result object.

        MpiTest.logger.fine("Search for residents of village Eden -- should find none at 100%.");
        p = new Person();
        p.setVillageName("Eden");
        requestData.setPerson(p);
        pr = callFindPerson(requestData);
        pList = pr.getPersonList();
        if (pList != null) {
            for (Person per : pList) {
                assertTrue(per.getMatchScore() < 100);
            }
        }

        MpiTest.logger.fine("Search for residents of village OutOfEden -- should find one at 100%.");
        p = new Person();
        p.setVillageName("OutOfEden");
        requestData.setPerson(p);
        pr = callFindPerson(requestData);
        pList = pr.getPersonList();
        assertNotNull(pList);
        pCount = 0;
        for (Person per : pList) {
            if (per.getMatchScore() >= 80) {
                pCount++;
            }
        }
        assertEquals(1, pCount);

        MpiTest.logger.fine("Set marital status to single.");
        p0.setMaritalStatus(Person.MaritalStatus.single);
        requestData.setPerson(p0);
        result = mpi.getData(RequestTypeId.MODIFY_PERSON_MPI, requestData);

    }

    /**
     * Parses a date and time in MySQL string format into a <code>Date</code>
     *
     * @param sDateTime contains date and time
     * @return the date and time in <code>Date</code> format
     * Returns null if the date and time string was null.
     */
    private Date parseDate(String sDateTime) {
        Date returnDateTime = null;
        if (sDateTime != null) {
            try {
                returnDateTime = SIMPLE_DATE_TIME_FORMAT.parse(sDateTime);
            } catch (ParseException ex) {
                Logger.getLogger(MpiTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDateTime;
    }
}
