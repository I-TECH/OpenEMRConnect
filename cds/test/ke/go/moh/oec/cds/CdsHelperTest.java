/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.cds;

import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.Work;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class CdsHelperTest {
    
    public CdsHelperTest() {
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
     * Test of processNotifyPersonChanged method, of class CdsHelper.
     */
    @Test
    public void testProcessNotifyPersonChanged() {
        System.out.println("processNotifyPersonChanged");
        PersonRequest personRequest = null;
        CdsHelper instance = null;
        instance.processNotifyPersonChanged(personRequest);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of processGetWork method, of class CdsHelper.
     */
    @Test
    public void testProcessGetWork() {
        System.out.println("processGetWork");
        Work work = null;
        CdsHelper instance = null;
        instance.processGetWork(work);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of processReassignWork method, of class CdsHelper.
     */
    @Test
    public void testProcessReassignWork() {
        System.out.println("processReassignWork");
        Work work = null;
        CdsHelper instance = null;
        instance.processReassignWork(work);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of processWorkDone method, of class CdsHelper.
     */
    @Test
    public void testProcessWorkDone() {
        System.out.println("processWorkDone");
        Work work = null;
        CdsHelper instance = null;
        instance.processWorkDone(work);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
