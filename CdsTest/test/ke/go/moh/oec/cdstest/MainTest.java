/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.cdstest;

import ke.go.moh.oec.PersonResponse;
import java.text.ParseException;
import java.util.logging.Logger;
import java.util.logging.Level;
import ke.go.moh.oec.Visit;
import java.text.SimpleDateFormat;
import ke.go.moh.oec.lib.Mediator;
import java.util.Date;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RequestTypeId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author DevWakhutu
 */
public class MainTest {

    private static Mediator mediator;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public MainTest() {
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
    public void testNotifyPersonChanged() {
        System.out.println("Testing Nofify Person Changed");
        int requestTypeId = RequestTypeId.NOTIFY_PERSON_CHANGED;
        PersonRequest requestData = new PersonRequest();
        Person p = new Person();
        requestData.setPerson(p);
        Object result;
        PersonResponse pr;
        p.setFirstName("Brian");
        p.setLastName("Wakhutu");

        p.setDeathdate(parseDate("2011-06-01"));


        Visit visit = new Visit();
        visit.setAddress("ke.go.moh.facility.14080.tb.ccc");
        visit.setVisitDate(new Date());
        p.setLastRegularVisit(visit);

        requestData.setDestinationName("Clinical Document Store");
        requestData.setDestinationAddress("ke.go.moh.facility.14080.cds");
        mediator.getData(requestTypeId, requestData);

    }

    private static Date parseDate(String sDate) {
        Date returnDate = null;
        if (sDate != null) {
            try {
                returnDate = SIMPLE_DATE_FORMAT.parse(sDate);
            } catch (ParseException ex) {
                Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDate;
    }
}
