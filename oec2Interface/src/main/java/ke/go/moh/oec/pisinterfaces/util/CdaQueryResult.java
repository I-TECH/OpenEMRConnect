package ke.go.moh.oec.pisinterfaces.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse CDAs and meta-data from XML documents returned from the external
 * service used to query for CDAs.  
 * 
 * Expected format:
 * <query_response>
 *  <record>
 *   <cda_id>[integer]</cda_id>
 *   <cda>[base 64 encoded CDA]</cda>
 *  </record>
 * </query_response>
 * 
 */
public class CdaQueryResult {

    Map<String, String> cdaList = new HashMap<String, String>();

    /**
     * SAX parser implementation used to parse a document returned from the 
     * external CDA query service.
     * 
     * @param document the result document from a query request
     * @return a map of {cda_id: cda} for each cda found in the doc.
     */ 
    public Map<String, String> parseDocument(InputStream document) {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = spf.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                boolean cda_id_element = false;
                boolean cda_element = false;
                String cda_id = null;
                StringBuffer cda_buffer = new StringBuffer();

                public void startElement(String uri, String localName, String qName,
                        Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("cda_id")) {
                        cda_id_element = true;
                    }
                    if (qName.equalsIgnoreCase("cda")) {
                        cda_element = true;
                    }
                }

                public void endElement(String uri, String localName,
                        String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("record")) {
                        // End of record - push decoded results
                        // The CDA itself is base 64 encrypted, to avoid any 
                        // nested CDATA decoding trouble.
                        byte[] decodedBytes = Base64.decodeBase64(cda_buffer.toString());
                        String cda = new String(decodedBytes);

                        CdaQueryResult.this.cdaList.put(cda_id, cda);
                        cda_id = null;
                        cda_buffer.setLength(0);
                    }
                    if (qName.equalsIgnoreCase("cda_id")) {
                        assert (cda_id_element == true);
                        cda_id_element = false;
                    }
                    if (qName.equalsIgnoreCase("cda")) {
                        assert (cda_element == true);
                        cda_element = false;
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    if (cda_id_element) {
                        assert (cda_id == null);
                        cda_id = new String(ch, start, length);
                    }
                    if (cda_element) {
                        cda_buffer.append(ch, start, length);
                    }
                }
            };
            
            saxParser.parse(document, handler);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return cdaList;
    }
}