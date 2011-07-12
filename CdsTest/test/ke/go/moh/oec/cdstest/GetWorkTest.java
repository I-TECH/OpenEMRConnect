/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.cdstest;

import java.text.ParseException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import ke.go.moh.oec.lib.Mediator;
import java.util.Date;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.Work;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class GetWorkTest {

    private static Mediator mediator;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public GetWorkTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        mediator = new Mediator();
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
     * Test of main method, of class Main.
     */
    @Test
    public void testGetWork() {
        System.out.println("Testing GET_WORK");
        int requestTypeId = RequestTypeId.GET_WORK;
        Work work = new Work();
        work.setSourceAddress(Mediator.getProperty("Instance.Address"));
        work.setNotificationId("7");
        work.setReassignAddress("xyz");
        mediator.getData(requestTypeId, work);

    }

    private static Date parseDate(String sDate) {
        Date returnDate = null;
        if (sDate != null) {
            try {
                returnDate = SIMPLE_DATE_FORMAT.parse(sDate);
            } catch (ParseException ex) {
                Logger.getLogger(GetWorkTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDate;
    }
}
