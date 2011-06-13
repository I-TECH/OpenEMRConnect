/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.lib;

import java.util.ArrayList;
import ke.go.moh.oec.PersonIdentifier;
import java.util.Date;
import ke.go.moh.oec.RequestTypeId;
import java.util.List;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jim Grace
 */
public class MediatorTest {

    static Mediator mediator = new Mediator();

    public MediatorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getProperty method, of class Mediator.
     */
    @Test
    public void testGetProperty() {
        System.out.println("getProperty");
        String propertyName = "Instance.Name";
        String expResult = "Siaya TB Reception";
        String result = Mediator.getProperty(propertyName);
        assertEquals(expResult, result);

        propertyName = "HTTPHandler.ListenPort";
        expResult = "9723";
        result = Mediator.getProperty(propertyName);
        assertEquals(expResult, result);
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
        System.out.println(log);
        Object result = mediator.getData(RequestTypeId.FIND_PERSON_MPI, personRequest);
        assertNotNull(result);
        assertSame(PersonResponse.class, result.getClass());
        PersonResponse personResponse = (PersonResponse) result;
        assertTrue(personResponse.isSuccessful());
        List<Person> pList = personResponse.getPersonList();
        if (pList == null || pList.isEmpty()) {
            System.out.println("No persons returned.");
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
                System.out.println(log);
            }
        }

        System.out.flush(); // (So debugging printing isn't interspersed with subsequent printing.)

        return personResponse;
    }

    /**
     * FindPerson test of getData method, of class Mediator.
     */
    @Test
    public void testFindPerson() {
        System.out.println("getData - findPerson");
        String instanceName = Mediator.getProperty("Instance.Name");
        System.out.println("Instance.Name = '" + instanceName + "'");
        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;
        List<Person> pList;

        // Clan name that will not be found
        p.setClanName("NotAClanName");
        result = mediator.getData(RequestTypeId.FIND_PERSON_MPI, requestData);
        assertNotNull(result);
        assertSame(PersonResponse.class, result.getClass());
        pr = (PersonResponse) result;
        assertTrue(pr.isSuccessful());
        assertNull(pr.getPersonList());

        // Clan name having 8 matches in the first 100 people
        p.setClanName("KONYANGO");
        result = mediator.getData(RequestTypeId.FIND_PERSON_MPI, requestData);
        assertNotNull(result);
        assertSame(PersonResponse.class, result.getClass());
        pr = (PersonResponse) result;
        assertTrue(pr.isSuccessful());
        pList = pr.getPersonList();
        assertNotNull(pList);
        int pCount = pList.size();
        assertEquals(pCount, 8);

        for (Person person : pList) {
            System.out.println("guid: " + person.getPersonGuid()
                    + " score: " + person.getMatchScore()
                    + " name: " + n(person.getFirstName()) + " " + n(person.getMiddleName()) + " " + n(person.getLastName()) + " [" + n(person.getOtherName()) + "]"
                    + " sex: " + n(person.getSex())
                    + " birth/death: " + n(person.getBirthdate()) + "/" + n(person.getDeathdate())
                    + " clan: " + n(person.getClanName())
                    + " mother: " + n(person.getMothersFirstName()) + " " + n(person.getMothersMiddleName()) + " " + n(person.getMothersLastName())
                    + " father: " + n(person.getFathersFirstName()) + " " + n(person.getFathersMiddleName()) + " " + n(person.getFathersLastName())
                    + " compHead: " + n(person.getCompoundHeadFirstName()) + " " + n(person.getCompoundHeadMiddleName()) + " " + n(person.getCompoundHeadLastName())
                    + " village: " + n(person.getVillageName())
                    + " marital: " + n(person.getMaritalStatus()));
        }

        for (int i = 0; i < pList.size(); i++) {
            Person person = pList.get(i);
            assertNotNull(person.getFirstName());
            assertNotNull(person.getMiddleName());
            assertNotNull(person.getLastName());
            // Make sure every returned person GUID is unique:
            // Make sure every returned birthdate is unique:
            for (int j = 0; j < i; j++) {
                Person pj = pList.get(j);
                assertFalse(pj.getPersonGuid().equals(person.getPersonGuid()));
                assertFalse(pj.getBirthdate().equals(person.getBirthdate()));
            }
        }
        Person p0 = pList.get(0);
        int score = p0.getMatchScore();
        assertEquals(score, 100);
        requestData.setPerson(p0);
        requestData.setRequestReference(pr.getRequestReference());
        result = mediator.getData(RequestTypeId.MODIFY_PERSON_MPI, requestData);
    }

    /**
     * FindPerson test of getData method, of class Mediator.
     */
    @Test
    public void testFindPersonLPI() {
        System.out.println("getData - findPerson in the LPI");
        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;
        List<Person> pList;

        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("00007/2004");
        pi.setIdentifierType(PersonIdentifier.Type.cccLocalId);
        List<PersonIdentifier> piList = new ArrayList<PersonIdentifier>();
        piList.add(pi);
        p.setPersonIdentifierList(piList);
        p.setSiteName("Siaya");
        requestData.setPerson(p);
        pr = callFindPerson(requestData);
        if (pr != null) {
            pList = pr.getPersonList();
            if (pList != null && !pList.isEmpty()) {
                p = pList.get(0);
                requestData.setPerson(p);
                requestData.setRequestReference(pr.getRequestReference());
                result = mediator.getData(RequestTypeId.MODIFY_PERSON_MPI, requestData);
            }
        }
    }
}
