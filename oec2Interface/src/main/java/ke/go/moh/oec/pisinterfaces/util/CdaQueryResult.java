package ke.go.moh.oec.pisinterfaces.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import ke.go.moh.oec.pisinterfaces.beans.CdaRecord;
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
    Map<String, CdaRecord> recordList = new HashMap<String, CdaRecord>();

    /**
     * SAX parser implementation used to parse a document returned from the 
     * external CDA query service, defining the bulk of the available row data
     * for any matching CDAs.
     * 
     * @param document the result document from a query request
     * @return a map of {cda_id: CdaRecord} for each cda found in the doc.
     */ 
    public Map<String, CdaRecord> parseDocument(InputStream document) throws SiteException {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = spf.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                String elementInProcess;
                String cda_id = null;
                StringBuffer cda_buffer = new StringBuffer();
                CdaRecord record;

                public void startElement(String uri, String localName, String qName,
                        Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("record")) {
                        record = new CdaRecord();
                    }
                    else {
                        elementInProcess = new String(qName);
                    }
                }

                public void endElement(String uri, String localName,
                        String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("record")) {
                        // End of record - push decoded results
                        // The CDA itself is base 64 encrypted by the query
                        // service, to avoid any nested CDATA decoding trouble.
                        byte[] decodedBytes = Base64.decodeBase64(cda_buffer.toString());
                        record.setCDA(new String(decodedBytes));

                        CdaQueryResult.this.recordList.put(cda_id, record);
                        
                        // Reset state for next record
                        cda_id = null;
                        cda_buffer.setLength(0);
                        elementInProcess = null;
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    String value = new String(ch, start, length);
                    
                    if ("cda_id".equals(elementInProcess)) {
                        assert (record.getCdaID() == null);  // catch need to accumulate
                        cda_id = value;
                        record.setCdaID(value);
                    } else if ("first_name".equals(elementInProcess)) {
                        assert (record.getFirstName() == null);  // catch need to accumulate
                        record.setFirstName(value);
                    } else if ("last_name".equals(elementInProcess)) {
                        assert (record.getLastName() == null);  // catch need to accumulate
                        record.setLastName(value);
                    } else if ("patient_clinical_id".equals(elementInProcess)) {
                        assert (record.getClinicId() == null);  // catch need to accumulate
                        record.setClinicId(value);
                    } else if ("hdss_id".equals(elementInProcess)) {
                        assert (record.getHdssId() == null);  // catch need to accumulate
                        record.setHdssId(value);
                    } else if ("source_system".equals(elementInProcess)) {
                        assert (record.getSourceSystem() == null);  // catch need to accumulate
                        record.setSourceSystem(value);
                    } else if ("cda_dob".equals(elementInProcess)) {
                        assert (record.getCdaDOB() == null);  // catch need to accumulate
                        record.setCdaDOB(value);                        
                    } else if ("gender".equals(elementInProcess)) {
                        assert (record.getGender() == null);  // catch need to accumulate
                        record.setGender(value);
                    } else if ("date_generated".equals(elementInProcess)) {
                        assert (record.getDateGenerated() == null);  // catch need to accumulate
                        record.setDateGenerated(value);
                    } else if ("cda".equals(elementInProcess)) {
                        // Frequently breaks over many lines; accumulate.
                        cda_buffer.append(ch, start, length);
                    } else {
                        Logger.getLogger(CdaQueryResult.class.getName()).
                                severe("unexpected XML entity: " + elementInProcess);
                    }
                }
            };
            
            saxParser.parse(document, handler);

        } catch (Exception e) {
            throw new SiteException("Unable to parse response CDA(s)", e);
        }
        return recordList;
    }

}