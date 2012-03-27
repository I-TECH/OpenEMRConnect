/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.pisinterfaces.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author pbugni
 */
public class CdaQueryResultTest extends TestCase {
    
    public CdaQueryResultTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of parseDocument method, of class CdaQueryResult.
     */
    public void testParseDocument() {
        InputStream testFile = null;
        try {
            CdaQueryResult instance = new CdaQueryResult();
            testFile = getClass().getResourceAsStream("/cda_query_result.xml");
            Map <String, String> results = instance.parseDocument(testFile);
            assert(results.size() == 2);
            // Key should be an integer (pk) and Value in each should be a valid CDA
            for (Map.Entry<String, String> entry : results.entrySet()){
                int pk = Integer.parseInt(entry.getKey());
                assert(pk >= 0);
                String cda_doc = entry.getValue();
                assertTrue(cda_doc.startsWith("<?xml version="));
            }
        } finally {
            try {
                testFile.close();
            } catch (IOException ex) {
                fail(ex.getLocalizedMessage());
            }
        }
    }
}
