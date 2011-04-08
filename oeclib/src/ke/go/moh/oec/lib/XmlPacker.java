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

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import ke.go.moh.oec.LogEntry;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Date;
import javax.naming.spi.DirStateFactory.Result;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * [Purity to supply description]
 *
 * @author Purity Chemutai
 */
class XmlPacker {

    /**
     * Packs a data object into a HL7 XML string.
     *
     * @param messageType type of message we are packing - see {@link(MessageTypeRepository)}
     * @param data object containing data to be packed into the message
     * @param messageID code to put into this message to identify it
     * @return the packed XML in a string
     */
    protected String pack(MessageType messageType, Object data, String messageID) {
        Document doc = makeDocument(messageType, data, messageID);
        String xml = packDocument(doc);
        return xml;
    }

    /**
     * Packs a data object into HL7 DOM Document structure.
     *
     * @param messageType type of message we are packing - see {@link(MessageTypeRepository)}
     * @param data object containing data to be packed into the message
     * @param messageID code to put into this message to identify it
     * @return DOM Document structure
     */
    protected Document makeDocument(MessageType messageType, Object requestData, String messageId) {
        Document doc = null;
        if (messageType == MessageTypeRegistry.sendLogEntry) {
            doc = makeSendLogEntry((LogEntry) requestData, messageId);
        }
        return doc;
    }

    private Document makeSendLogEntry(LogEntry logEntry, String messageId) {

        //Create instance of DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //Get the DocumentBuilder
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        //create a blank document
        Document doc = db.newDocument();
        //create the root element
        Element root = doc.createElement("LogEntry");
        //all to xml tree
        doc.appendChild(root);
        //to get the value of the severity element
        Element sourceElement = doc.createElement("source");
        sourceElement.setNodeValue(Mediator.getProperty("Instance.Address"));
        root.appendChild(sourceElement);
        //to get the value of the severity element
        Element messageIdElement = doc.createElement("messageId");
        messageIdElement.setNodeValue(messageId);
        root.appendChild(messageIdElement);
        //to get the value of the severity element
        Element severityElement = doc.createElement("severity");
        severityElement.setNodeValue(logEntry.getSeverity());
        root.appendChild(severityElement);
        //to get the value of the class element
        Element classElement = doc.createElement("class");
        classElement.setNodeValue(logEntry.getClassName());
        root.appendChild(classElement);

        //to get the value of the datetime element and add to the child
        Element datetimeElement = doc.createElement("dateTime");
        datetimeElement.setNodeValue(formatDateTime(logEntry.getDateTime()));
        root.appendChild(datetimeElement);
        //to get the value of the message element and add to the child
        Element messageElement = doc.createElement("message");
        messageElement.setNodeValue(logEntry.getMessage());
        root.appendChild(messageElement);

        Element instanceElement = doc.createElement("instance");
        instanceElement.setNodeValue(logEntry.getInstance());
        root.appendChild(instanceElement);

        return doc;
    }

    /**
     * Packs a DOM Document structure into an XML string
     * @param doc the DOM Document structure to pack
     * @return the packed XML string
     */
    protected String packDocument(Document doc) {
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer transformer = tranFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            Source src = new DOMSource(doc);
            transformer.transform(src, new StreamResult(stringWriter));
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stringWriter.toString();
    }

    private Document makeStringSendLogEntry(LogEntry logEntry, String messageId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unpacks a HL7 XML string into an object.
     *
     * @param xml String containing XML request message
     * @return unpacked message data
     */
    protected UnpackedMessage unpack(String xml) {
        Document doc = parseXml(xml);
        UnpackedMessage unpackedMessage = decodeDocument(doc);
        return unpackedMessage;
    }

    /**
     * Unpacks a HL7 XML string into DOM Document structure
     *
     * @param xml String containing XML request message
     * @return the DOM Document structure
     */
    protected Document parseXml(String xml) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            doc = db.parse(is);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    /**
     * Decodes a DOM Document structure into message data
     *
     * @param doc the DOM Document structure to decode
     * @return unpacked message data
     */
    protected UnpackedMessage decodeDocument(Document doc) {
        UnpackedMessage um = null;
        MessageTypeRegistry messageTypeRegistry = new MessageTypeRegistry();
        Element rootElement = doc.getDocumentElement();
        String rootElementName = rootElement.getTagName();
        MessageType messageType = messageTypeRegistry.find(rootElementName);

        if (messageType == MessageTypeRegistry.sendLogEntry) {
            um = decodeSendLogEntry(doc);
        }
        um.setMessageType(messageType);
        return um;
    }

    private UnpackedMessage decodeSendLogEntry(Document doc) {
        UnpackedMessage um = new UnpackedMessage();
        LogEntry logEntry = new LogEntry();
        um.setReturnData(logEntry);
//         logEntry.(doc.getElementsByTagName("source").item(0).getNodeValue());
//         logEntry.setClassName(doc.getElementsByTagName("messageId").item(0).getNodeValue());
        logEntry.setSeverity(doc.getElementsByTagName("severity").item(0).getNodeValue());
        logEntry.setClassName(doc.getElementsByTagName("class").item(0).getNodeValue());
        logEntry.setDateTime(parseDateTime(doc.getElementsByTagName("dateTime").item(0).getNodeValue()));
        logEntry.setMessage(doc.getElementsByTagName("message").item(0).getNodeValue());
        logEntry.setInstance(doc.getElementsByTagName("instance").item(0).getNodeValue());
        return um;
    }

    private Date parseDateTime(String sDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date convertedDate = null;
        try {
            convertedDate = dateFormat.parse(sDate);
        } catch (ParseException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return convertedDate;
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = null;
        dateString = dateFormat.format(date);
        return dateString;
    }
}
