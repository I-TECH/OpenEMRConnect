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

import ke.go.moh.oec.Fingerprint;
import java.util.HashMap;
import ke.go.moh.oec.Person;
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
public class ValueMapTest {
    
    public ValueMapTest() {
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
     * Test of getDb method, of class ValueMap.
     */
    @Test
    public void testGetDb() {
        System.out.println("getDb");
        ValueMap instance = ValueMap.FINGERPRINT_TYPE;
        HashMap<Enum, String> result = instance.getDb();
        assertNotNull(result);
        String dbValue = result.get(Fingerprint.Type.leftIndexFinger);
        assertEquals("2", dbValue);
    }

    /**
     * Test of getVal method, of class ValueMap.
     */
    @Test
    public void testGetVal() {
        System.out.println("getVal");
        ValueMap instance = ValueMap.CONSENT_SIGNED;
        HashMap expResult = null;
        HashMap<String, Enum> result = instance.getVal();
        assertNotNull(result);
        Enum enumVal = result.get("0");
        assertEquals(Person.ConsentSigned.no, enumVal);
    }
}
