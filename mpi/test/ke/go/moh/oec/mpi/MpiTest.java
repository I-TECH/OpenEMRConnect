/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.mpi;

import java.util.List;
import java.util.logging.Level;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RequestTypeId;
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
public class MpiTest {

    public MpiTest() {
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
     * Test of getData method, of class Mpi.
     */
    @Test
    public void testFindPerson() {
        System.out.println("testFindPerson");
        Mpi.setTestQueryLimit(10);
        // slows things down: Mpi.setTestOrderBy("person_id");
        Mpi mpi = new Mpi();
        Mpi.setLoggerLevel(Level.ALL);

        int requestTypeId = RequestTypeId.FIND_PERSON_MPI;
        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;

        // Clan name that will not be found
        System.out.println("testFindPerson - Clan name that will not be found");
        p.setClanName("ThisClanNameWillNotBeFound");
        result = mpi.getData(requestTypeId, requestData);
        assertNotNull(result);
        assertSame(PersonResponse.class, result.getClass());
        pr = (PersonResponse) result;
        assertTrue(pr.isSuccessful());
        assertNull(pr.getPersonList());

        // Clan name having 4 matches in the first 10 people
        System.out.println("testFindPerson - Clan name returning 4 matches");
        p.setClanName("KONYANGO");
        result = mpi.getData(requestTypeId, requestData);
        assertNotNull(result);
        assertSame(PersonResponse.class, result.getClass());
        pr = (PersonResponse) result;
        assertTrue(pr.isSuccessful());
        List<Person> pList = pr.getPersonList();
        assertNotNull(pList);
        int pCount = pList.size();
        assertEquals(pCount, 4);
        Person p0 = pList.get(0);
        int score = p0.getMatchScore();
        assertEquals(score, 100);
    }
}