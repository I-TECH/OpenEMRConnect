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
package ke.go.moh.oec.lib;

import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Fingerprint;
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
        System.out.println("JUnit Test getProperty");
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
        System.out.println("JUnit Test getData - findPerson");
        String instanceName = Mediator.getProperty("Instance.Name");
        System.out.println("Instance.Name = '" + instanceName + "'");
        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;
        List<Person> pList;

        // Name that will not be found
        p.setFirstName("O<NotAFirstName");
        p.setClanName("O\"NotAClanName");
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
        
        // Exercise fingerprint matching code
        p0 = new Person();
        List<Fingerprint> fpList = new ArrayList<Fingerprint>();
        byte[] b = {1, 2, 3};
        Fingerprint f = new Fingerprint();
        f.setTemplate(b);
        f.setFingerprintType(Fingerprint.Type.rightRingFinger);
        f.setTechnologyType(Fingerprint.TechnologyType.griauleTemplate);
        fpList.add(f);
        p0.setFingerprintList(fpList);
        requestData.setPerson(p0);
        pr = callFindPerson(requestData);
    }

    /**
     * FindPerson test of getData method, of class Mediator.
     */
    @Test
    public void testFindPersonLPI() {
        System.out.println("JUnit Test getData - findPerson in the LPI");
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
        assertNotNull(pr);
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
    /**
     * Test of getData method, request type CREATE_PERSON_MPI
     */
    @Test
    public void testModifyPerson() {
        System.out.println("testModifyPerson");
        PersonRequest requestData = new PersonRequest();
        Person p;
        List<Person> pList;
        PersonIdentifier pi;
        List<PersonIdentifier> piList;
        int pCount;
        Object result;
        PersonResponse pr;

        // Modify the person (will not exist) -- just to test QueueManager
        p = new Person();
        requestData.setPerson(p);
        pi = new PersonIdentifier();
        piList = new ArrayList<PersonIdentifier>();
        pi.setIdentifier("33333-44444");
        pi.setIdentifierType(PersonIdentifier.Type.patientRegistryId);
        piList.add(pi);
        p.setPersonIdentifierList(piList);
        pr = (PersonResponse) mediator.getData(RequestTypeId.MODIFY_PERSON_MPI, requestData);
        try {
            Thread.sleep(1000*1000); // Sleep 10 seconds.
        } catch (InterruptedException ex) {
            Logger.getLogger(MediatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
