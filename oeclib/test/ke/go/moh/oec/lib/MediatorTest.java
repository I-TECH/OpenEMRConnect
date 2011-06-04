/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.lib;

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
        expResult = "9724";
        result = Mediator.getProperty(propertyName);
        assertEquals(expResult, result);
    }

    /**
     * FindPerson test of getData method, of class Mediator.
     */
    @Test
    public void testFindPerson() {
        System.out.println("getData - findPerson");
        String instanceName = Mediator.getProperty("Instance.Name");
        System.out.println("Instance.Name = '" + instanceName + "'");
        Mediator mediator = new Mediator();
        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;
        List<Person> pList;
        
        // Clan name that will not be found
        p.setClanName("ThisClanNameWillNotBeFound");
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
        for (int i = 0; i < pList.size(); i++) {
            Person person = pList.get(i);
            String guid = person.getPersonGuid();
            // Make sure every returned person GUID is unique:
            for (int j = 0; j < i; j++) {
                assertFalse(pList.get(j).getPersonGuid().equals(guid));
            }
        }
        Person p0 = pList.get(0);
        int score = p0.getMatchScore();
        assertEquals(score, 100);
    }
}
