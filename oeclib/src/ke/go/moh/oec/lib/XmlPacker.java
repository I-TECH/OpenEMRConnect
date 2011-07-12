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

import ke.go.moh.oec.LogEntry;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RelatedPerson;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.Work;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Packs message data into XML strings, and unpacks XML strings into message data.
 * <p>
 * Note that all the methods in this class start with "pack", "unpack" or
 * "common". "pack" methods are used only for packing XML messages. "unpack"
 * methods are used only for unpacking XML messages. "common" methods are
 * used by both.
 *
 * @author Purity Chemutai
 * @author Jim Grace
 */
class XmlPacker {

    /*
     * Define the Object IDs (OIDs) we need to know in the HL7 messages.
     */
    private static final String OID_ROOT = "1.3.6.1.4.1.150.2474.11.1.";
    private static final String OID_MESSAGE_ID = OID_ROOT + "1";
    private static final String OID_APPLICATION_ADDRESS = OID_ROOT + "2";
    private static final String OID_QUERY_ID = OID_ROOT + "3";
    private static final String OID_OTHER_NAME = OID_ROOT + "4.1";
    private static final String OID_CLAN_NAME = OID_ROOT + "4.2";
    private static final String OID_ALIVE_STATUS = OID_ROOT + "4.3";
    private static final String OID_MOTHERS_FIRST_NAME = OID_ROOT + "4.4";
    private static final String OID_MOTHERS_MIDDLE_NAME = OID_ROOT + "4.5";
    private static final String OID_MOTHERS_LAST_NAME = OID_ROOT + "4.6";
    private static final String OID_FATHERS_FIRST_NAME = OID_ROOT + "4.7";
    private static final String OID_FATHERS_MIDDLE_NAME = OID_ROOT + "4.8";
    private static final String OKD_FATHERS_LAST_NAME = OID_ROOT + "4.9";
    private static final String OID_COMPOUND_HEAD_FIRST_NAME = OID_ROOT + "4.10";
    private static final String OID_COMPOUND_HEAD_MIDDLE_NAME = OID_ROOT + "4.11";
    private static final String OID_COMPOUND_HEAD_LAST_NAME = OID_ROOT + "4.12";
    private static final String OID_MARITAL_STATUS = OID_ROOT + "4.13";
    private static final String OID_CONSENT_SIGNED = OID_ROOT + "4.14";
    private static final String OID_VILAGE_NAME = OID_ROOT + "4.15";
    private static final String OID_PREVIOUS_VILLAGE_NAME = OID_ROOT + "4.16";
    private static final String OID_LAST_MOVE_DATE = OID_ROOT + "4.17";
    private static final String OID_EXPECTED_DELIVERY_DATE = OID_ROOT + "4.18";
    private static final String OID_PREGNANCY_END_DATE = OID_ROOT + "4.19";
    private static final String OID_PREGNANCY_OUTCOME = OID_ROOT + "4.20";
    private static final String OID_SITE_NAME = OID_ROOT + "4.21";
    private static final String OID_FINGERPRINT_MATCHED = OID_ROOT + "4.22";
    private static final String OID_PATIENT_REGISTRY_ID = OID_ROOT + "5.1";
    private static final String OID_MASTER_PATIENT_REGISTRY_ID = OID_ROOT + "5.2";
    private static final String OID_CCC_UNIVERSAL_UNIQUE_ID = OID_ROOT + "5.3";
    private static final String OID_CCC_LOCAL_PATIENT_ID = OID_ROOT + "5.4";
    private static final String KISUMU_HDSS_ID = OID_ROOT + "5.5";
    private static final String OID_REGULAR_VISIT_ADDRESS = OID_ROOT + "6.1";
    private static final String OID_REGULAR_VISIT_DATE = OID_ROOT + "6.2";
    private static final String OID_ONEOFF_VISIT_ADDRESS = OID_ROOT + "6.3";
    private static final String OID_ONEOFF_VISIT_DATE = OID_ROOT + "6.4";
    private static final String OID_FINGERPRINT_LEFT_INDEX = OID_ROOT + "7.1";
    private static final String OID_FINGERPRINT_LEFT_MIDDLE = OID_ROOT + "7.2";
    private static final String OID_FINGERPRINT_LEFT_RING = OID_ROOT + "7.3";
    private static final String OID_FINGERPRINT_RIGHT_INDEX = OID_ROOT + "7.4";
    private static final String OID_FINGERPRINT_RIGHT_MIDDLE = OID_ROOT + "7.5";
    private static final String OID_FINGERPRINT_RIGHT_RING = OID_ROOT + "7.6";

    /*
     * Define other constant objects used in message processing.
     */
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /*
     * ---------------------------------------------------------------------------------------
     *
     *                          P  A  C  K        M  E  T  H  O  D  S
     *
     * ---------------------------------------------------------------------------------------
     */
    /**
     * Packs a data object into a XML string.
     *
     * @param m message to be packed
     * @return the packed XML in a string
     */
    String pack(Message m) {
        Document doc = packMessage(m);
        String xml = packDocument(doc);
        return xml;
    }

    /**
     * Packs a DOM Document structure into an XML string.
     *
     * @param doc the DOM Document structure to pack
     * @return the packed XML string
     */
    String packDocument(Document doc) {
        StringWriter stringWriter = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.STANDALONE, "yes");
            Source source = new DOMSource(doc);
            t.transform(source, new StreamResult(stringWriter));
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringWriter.toString();
    }

    /**
     * Packs a message into a DOM Document structure.
     *
     * @param m message contents to pack
     * @return message packed in a <code>Document</code>
     */
    Document packMessage(Message m) {
        Document doc = null;
        switch (m.getMessageType().getTemplateType()) {
            case findPerson:
                doc = packFindPersonMessage(m);
                break;

            case findPersonResponse:
                doc = packFindPersonResponseMessage(m);
                break;

            case createPerson: // Uses packGenericPersonMessage(), below.
            case modifyPerson: // Uses packGenericPersonMessage(), below.
            case createPersonAccepted: // Uses packGenericPersonMessage(), below.
            case modifyPersonAccepted: // Uses packGenericPersonMessage(), below.
            case notifyPersonChanged:
                doc = packGenericPersonMessage(m);
                break;

            case logEntry:
                doc = packLogEntryMessage(m);
                break;
            /** added on 19th may, 2011
            To handle work_message */
            case getWork: // Uses packWorkMessage(m)
            case workDone: // Uses packWorkMessage(m)
            case reassignWork://Uses packWorkMessage(m)
                doc = packWorkMessage(m);
                break;
        }
        return doc;
    }

    /**
     * Packs a generic HL7 person-related message into a <code>Document</code>.
     * <p>
     * Several of the HL7 person-related messages use the same formatting
     * rules, even though the templates differ. (The templates differ only
     * in the boilerplate parts that do not concern us directly.)
     * These messages are:
     * <p>
     * CREATE PERSON <br>
     * MODIFY PERSON <br>
     * CREATE PERSON ACCEPTED <br>
     * MODIFY PERSON ACCEPTED <br>
     * NOTIFY PERSON CHANGED
     *
     * @param m notification message contents to pack
     * @return packed notification messages
     */
    private Document packGenericPersonMessage(Message m) {
        Document doc = packTemplate(m);
        Element root = doc.getDocumentElement();
        packHl7Header(root, m);
        Element personNode = (Element) root.getElementsByTagName("patient").item(0);
        if (!(m.getMessageData() instanceof PersonRequest)) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packGenericPersonMessage() - Expected data class PersonRequest, got {0}",
                    m.getMessageData().getClass().getName());
        }
        if (m.getXml() == null) { // Skip the following if we have pre-formed XML:
            PersonRequest personRequest = (PersonRequest) m.getMessageData();
            Person p = personRequest.getPerson();
            packPerson(personNode, p);
            if (personRequest.isResponseRequested()) {
                packTagValue(root, "acceptAckCode", "AL"); // Request "ALways" acknowedge.
            }
        }
        return doc;
    }

    /**
     * Packs work messages.
     *
     * Several of the Work-related messages use the same formatting
     * rules, even though the templates differ only on the root tag.
     *
     * These messages are:
     * <p>
     * GET WORK <br>
     * WORK DONE <br>
     * REASSIGN WORK
     * @param m notification message contents to pack
     * @return packed notification messages
     */
    private Document packWorkMessage(Message m) {

        Work work = (Work) m.getMessageData();
        // Create instance of DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Get the DocumentBuilder
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = db.newDocument(); // Create a blank Document
        MessageType messageType = m.getMessageType();
        String rootName = messageType.getRootXmlTag();//get's the root tag
        Element root = doc.createElement(rootName);
        doc.appendChild(root); // Root element is child of the Document
        packNewElement(doc, root, "sourceAddress", work.getSourceAddress());
        packNewElement(doc, root, "notificationId", work.getNotificationId());
        packNewElement(doc, root, "reassignAddress", work.getReassignAddress());

        return doc;
    }

    /**
     * Packs a Find Person message into a <code>Document</code>.
     * Uses HL7 Patient Registry Find Candidates Query, PRPA_IN201305UV02.
     *
     * @param m search message contents to pack
     * @return packed search message
     */
    private Document packFindPersonMessage(Message m) {
        Document doc = packTemplate(m);
        Element root = doc.getDocumentElement();
        packHl7Header(root, m);
        // The rest of what we want is in the subtree under <queryByParameter>
        Element q = (Element) root.getElementsByTagName("queryByParameter").item(0);
        if (!(m.getMessageData() instanceof PersonRequest)) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packFindPersonMessage() - Expected data class PersonRequest, got {0}",
                    m.getMessageData().getClass().getName());
        }
        if (m.getXml() == null) { // Skip the following if we have pre-formed XML:
            PersonRequest personRequest = (PersonRequest) m.getMessageData();
            Person p = personRequest.getPerson();
            packPersonName(q, p, "livingSubjectName");
            packTagValueAttribute(q, "livingSubjectAdministrativeGender", "code", packEnum(p.getSex()));
            packTagValueAttribute(q, "livingSubjectBirthTime", "value", packDate(p.getBirthdate()));
            packTagValueAttribute(q, "livingSubjectDeceasedTime", "value", packDate(p.getDeathdate()));
            packLivingSubjectId(q, OID_OTHER_NAME, p.getOtherName());
            packLivingSubjectId(q, OID_CLAN_NAME, p.getClanName());
            packLivingSubjectId(q, OID_ALIVE_STATUS, packEnum(p.getAliveStatus())); // (Used only on findPerson.)
            packLivingSubjectId(q, OID_MOTHERS_FIRST_NAME, p.getMothersFirstName());
            packLivingSubjectId(q, OID_MOTHERS_MIDDLE_NAME, p.getMothersMiddleName());
            packLivingSubjectId(q, OID_MOTHERS_LAST_NAME, p.getMothersLastName());
            packLivingSubjectId(q, OID_FATHERS_FIRST_NAME, p.getFathersFirstName());
            packLivingSubjectId(q, OID_FATHERS_MIDDLE_NAME, p.getFathersMiddleName());
            packLivingSubjectId(q, OKD_FATHERS_LAST_NAME, p.getFathersLastName());
            packLivingSubjectId(q, OID_COMPOUND_HEAD_FIRST_NAME, p.getCompoundHeadFirstName());
            packLivingSubjectId(q, OID_COMPOUND_HEAD_MIDDLE_NAME, p.getCompoundHeadMiddleName());
            packLivingSubjectId(q, OID_COMPOUND_HEAD_LAST_NAME, p.getCompoundHeadLastName());
            packLivingSubjectId(q, OID_MARITAL_STATUS, packEnum(p.getMaritalStatus()));
            packLivingSubjectId(q, OID_CONSENT_SIGNED, packEnum(p.getConsentSigned()));
            packLivingSubjectId(q, OID_SITE_NAME, p.getSiteName()); // (Used only on findPerson.)
            packLivingSubjectId(q, OID_VILAGE_NAME, p.getVillageName());
            packLivingSubjectPersonIdentifiers(q, p, OID_PATIENT_REGISTRY_ID, PersonIdentifier.Type.patientRegistryId);
            packLivingSubjectPersonIdentifiers(q, p, OID_MASTER_PATIENT_REGISTRY_ID, PersonIdentifier.Type.masterPatientRegistryId);
            packLivingSubjectPersonIdentifiers(q, p, OID_CCC_UNIVERSAL_UNIQUE_ID, PersonIdentifier.Type.cccUniqueId);
            packLivingSubjectPersonIdentifiers(q, p, OID_CCC_LOCAL_PATIENT_ID, PersonIdentifier.Type.cccLocalId);
            packLivingSubjectPersonIdentifiers(q, p, KISUMU_HDSS_ID, PersonIdentifier.Type.kisumuHdssId);
            packLivingSubjectFingerprints(q, p, OID_FINGERPRINT_LEFT_INDEX, Fingerprint.Type.leftIndexFinger);
            packLivingSubjectFingerprints(q, p, OID_FINGERPRINT_LEFT_MIDDLE, Fingerprint.Type.leftMiddleFinger);
            packLivingSubjectFingerprints(q, p, OID_FINGERPRINT_LEFT_RING, Fingerprint.Type.leftRingFinger);
            packLivingSubjectFingerprints(q, p, OID_FINGERPRINT_RIGHT_INDEX, Fingerprint.Type.rightIndexFinger);
            packLivingSubjectFingerprints(q, p, OID_FINGERPRINT_RIGHT_MIDDLE, Fingerprint.Type.rightMiddleFinger);
            packLivingSubjectFingerprints(q, p, OID_FINGERPRINT_RIGHT_RING, Fingerprint.Type.rightRingFinger);
        }
        return doc;
    }

    /**
     * Packs a Find Person Response message into a <code>Document</code>.
     * Uses HL7 Patient Registry Find Candidates Query Response, PRPA_IN201306UV02.
     *
     * @param m search message contents to pack
     * @return packed response message
     */
    private Document packFindPersonResponseMessage(Message m) {
        Document doc = packTemplate(m);
        Element root = doc.getDocumentElement();
        packHl7Header(root, m);
        if (!(m.getMessageData() instanceof PersonResponse)) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packFindPersonResponseMessage() - Expected data class PersonResponse, got {0}",
                    m.getMessageData().getClass().getName());
        }
        if (m.getXml() == null) { // Skip the following if we have pre-formed XML:
            PersonResponse pr = (PersonResponse) m.getMessageData();
            List<Person> personList = pr.getPersonList();
            /*
             * Find the <subject> subtree in the template. If there are no person results returned, remove it.
             * If there is one result, pack it into the template. If there are more than one results,
             * clone the <subject> subtree so we will have one to pack for each result.
             *
             * Note that we do all the cloning first, before any of the filling in.
             * This is because template elements may be deleted as a person object is packed --
             * we want all the template elements there in case some return person structures
             * have more properties filled in than others.
             */
            Element subject = (Element) root.getElementsByTagName("subject").item(0);
            if (personList == null || personList.isEmpty()) {
                packRemoveNode(subject);
            } else {
                List<Element> elementList = new ArrayList<Element>();
                elementList.add(subject); // Always the first element in the list.
                for (int i = 1; i < personList.size(); i++) { // From 2nd person (index 1) onwards...
                    subject = packCloneElement(subject);
                    elementList.add(subject); // Add the elements in order to preserve order for debugging/testing
                }
                for (int i = 0; i < personList.size(); i++) { // From 1st person (index 0) ondwards...
                    packCandidate(elementList.get(i), personList.get(i));
                }
            }
        }
        return doc;
    }

    /**
     * Packs one of the candidates to return in a FindPerson response message.
     *
     * @param e top element of the subtree to contain the details for the candidate.
     * @param p candidate person information.
     */
    private void packCandidate(Element e, Person p) {
        packPerson(e, p);
        packRelatedPersons(e, p);
        packTagValueAttribute(e, "queryMatchObservation", "value", Integer.toString(p.getMatchScore()));
    }

    /**
     * Packs a standard header for a HL7 V3 message.
     * Standard header elements include message id, and receiver and sender addresses and names.
     * @param root root of document template to fill in
     * @param m message parameters containing data to fill in
     */
    private void packHl7Header(Element root, Message m) {
        packId(root, OID_MESSAGE_ID, m.getMessageId());
        Element receiver = (Element) root.getElementsByTagName("receiver").item(0);
        packId(receiver, OID_APPLICATION_ADDRESS, m.getDestinationAddress());
        packTagValue(receiver, "name", m.getDestinationName());
        Element sender = (Element) root.getElementsByTagName("sender").item(0);
        packId(sender, OID_APPLICATION_ADDRESS, m.getSourceAddress());
        packTagValue(sender, "name", m.getSourceName());
    }

    /**
     * Packs person information into a <code>Document</code> subtree
     * @param e head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person data to pack into the subtree
     */
    private void packPerson(Element e, Person p) {
        if (p == null) {
            p = new Person();
        }
        packPersonName(e, p, "name");
        packTagAttribute(e, "administrativeGenderCode", "code", packEnum(p.getSex()));
        packTagAttribute(e, "birthTime", "value", packDate(p.getBirthdate()));
        packTagAttribute(e, "deceasedTime", "value", packDate(p.getDeathdate()));
        packId(e, OID_OTHER_NAME, p.getOtherName());
        packId(e, OID_CLAN_NAME, p.getClanName());
        packId(e, OID_ALIVE_STATUS, packEnum(p.getAliveStatus()));
        packId(e, OID_MOTHERS_FIRST_NAME, p.getMothersFirstName());
        packId(e, OID_MOTHERS_MIDDLE_NAME, p.getMothersMiddleName());
        packId(e, OID_MOTHERS_LAST_NAME, p.getMothersLastName());
        packId(e, OID_FATHERS_FIRST_NAME, p.getFathersFirstName());
        packId(e, OID_FATHERS_MIDDLE_NAME, p.getFathersMiddleName());
        packId(e, OKD_FATHERS_LAST_NAME, p.getFathersLastName());
        packId(e, OID_COMPOUND_HEAD_FIRST_NAME, p.getCompoundHeadFirstName());
        packId(e, OID_COMPOUND_HEAD_MIDDLE_NAME, p.getCompoundHeadMiddleName());
        packId(e, OID_COMPOUND_HEAD_LAST_NAME, p.getCompoundHeadLastName());
        packId(e, OID_MARITAL_STATUS, packEnum(p.getMaritalStatus()));
        packId(e, OID_CONSENT_SIGNED, packEnum(p.getConsentSigned()));
        packId(e, OID_VILAGE_NAME, p.getVillageName());
        packId(e, OID_PREVIOUS_VILLAGE_NAME, p.getPreviousVillageName());
        packId(e, OID_LAST_MOVE_DATE, packDate(p.getLastMoveDate()));
        packId(e, OID_EXPECTED_DELIVERY_DATE, packDate(p.getExpectedDeliveryDate()));
        packId(e, OID_PREGNANCY_END_DATE, packDate(p.getPregnancyEndDate()));
        packId(e, OID_PREGNANCY_OUTCOME, packEnum(p.getPregnancyOutcome()));
        packId(e, OID_SITE_NAME, p.getSiteName());
        packId(e, OID_FINGERPRINT_MATCHED, packBoolean(p.isFingerprintMatched()));
        packVisit(e, p.getLastRegularVisit(), OID_REGULAR_VISIT_ADDRESS, OID_REGULAR_VISIT_DATE);
        packVisit(e, p.getLastOneOffVisit(), OID_ONEOFF_VISIT_ADDRESS, OID_ONEOFF_VISIT_DATE);
        packPersonIdentifiers(e, p, OID_PATIENT_REGISTRY_ID, PersonIdentifier.Type.patientRegistryId);
        packPersonIdentifiers(e, p, OID_MASTER_PATIENT_REGISTRY_ID, PersonIdentifier.Type.masterPatientRegistryId);
        packPersonIdentifiers(e, p, OID_CCC_UNIVERSAL_UNIQUE_ID, PersonIdentifier.Type.cccUniqueId);
        packPersonIdentifiers(e, p, OID_CCC_LOCAL_PATIENT_ID, PersonIdentifier.Type.cccLocalId);
        packPersonIdentifiers(e, p, KISUMU_HDSS_ID, PersonIdentifier.Type.kisumuHdssId);
        packFingerprints(e, p, OID_FINGERPRINT_LEFT_INDEX, Fingerprint.Type.leftIndexFinger);
        packFingerprints(e, p, OID_FINGERPRINT_LEFT_MIDDLE, Fingerprint.Type.leftMiddleFinger);
        packFingerprints(e, p, OID_FINGERPRINT_LEFT_RING, Fingerprint.Type.leftRingFinger);
        packFingerprints(e, p, OID_FINGERPRINT_RIGHT_INDEX, Fingerprint.Type.rightIndexFinger);
        packFingerprints(e, p, OID_FINGERPRINT_RIGHT_MIDDLE, Fingerprint.Type.rightMiddleFinger);
        packFingerprints(e, p, OID_FINGERPRINT_RIGHT_RING, Fingerprint.Type.rightRingFinger);
    }

    /**
     * Packs a person's name. For findPerson the tagname containing the name elements
     * is "livingSubjectName". For other person messages the tagName is "name".
     * <p>
     * The first and middle names are packed in two consecutive &lt;given&gt; nodes.
     * The last name is packed in the &lt;family&gt; node.
     * If none of the names are present, remove the whole &lt;name&gt; tag.
     * Otherwise, remove the tag for any part of the name that is not present.
     * The exception to this is if the first name is absent and the middle name
     * is present, keep the first &lt;given&gt; node, but pack it with an
     * empty string. That way the middle name will still go in the second &lt;given&gt; tag.
     *
     * @param e head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person data to pack into the subtree
     * @param tagName name of the enclosing element for the person's name
     */
    private void packPersonName(Element e, Person p, String tagName) {
        Element eName = (Element) e.getElementsByTagName(tagName).item(0);
        /*
         * If all three names are null, remove the whole <name> subtree.
         */
        if (p.getFirstName() == null && p.getMiddleName() == null && p.getLastName() == null) {
            packRemoveNode(eName);
        } else {
            /* First name and middle name are both stored as <given> nodes. */
            NodeList givenList = eName.getElementsByTagName("given");
            /*
             * Pack the first name.
             * If the first name is not present, we will take care of that
             * later, when we know if we have a middle name.
             */
            if (p.getFirstName() != null) {
                givenList.item(0).setTextContent(p.getFirstName());
            }
            /*
             * Pack the middle name.
             * If the first name was null, then either make it empty (if there was a middle name)
             * or remove it completedly (if we are also removing the middle name.)
             */
            if (p.getMiddleName() != null) {
                givenList.item(1).setTextContent(p.getMiddleName());
                if (p.getFirstName() == null) {
                    givenList.item(0).setTextContent("");
                }
            } else {
                packRemoveNode(givenList.item(1));
                if (p.getFirstName() == null) {
                    packRemoveNode(givenList.item(0));
                }
            }
            /*
             * Pack the last name.
             */
            packTagValue(eName, "family", p.getLastName());
        }
    }

    /**
     * Packs visit information into a person subtree of a <code>Document</code>
     *
     * @param e head of the <code>Document</code> subtree in which this person is to be packed
     * @param v visit information to pack
     * @param oidVisitAddress OID for the XML id tag containing the visit address
     * @param oidVisitDate OID for the XML id tag containing the visit date
     */
    private void packVisit(Element e, Visit v, String oidVisitAddress, String oidVisitDate) {
        if (v != null) {
            packId(e, oidVisitAddress, v.getAddress());
            packId(e, oidVisitDate, packDate(v.getVisitDate()));
        } else {
            packId(e, oidVisitAddress, null);
            packId(e, oidVisitDate, null);
        }
    }

    /**
     * Packs all person identifiers of a given type into a person subtree of a <code>Document</code>
     * <p>
     * Searches through all the identifiers for a person to find identifiers of the given
     * type. The first such identifier replaces the template value. Subsequent identifiers
     * are inserted into clones of the template value. If there is no identifier of the given type,
     * the template value is removed.
     * <p>
     * The patient registry ID type is a special case. It doesn't come from the
     * list of identifiers, but rather from person.personGuid.
     *
     * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person information containing the list of identifiers
     * @param oidPersonIdentifier the XML template OID for this person identifier type
     * @param type the person identifier type
     */
    private void packPersonIdentifiers(Element subtree, Person p, String oidPersonIdentifier, PersonIdentifier.Type type) {
        Element idElement = commonGetId(subtree, oidPersonIdentifier);
        if (idElement == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "PersonIdentifier type {0}, OID {1} was not found in the template XML file.",
                    new Object[]{type.name(), oidPersonIdentifier});
            return;
        }
        boolean idTypeFound = false;
        if (type == PersonIdentifier.Type.patientRegistryId) {
            String personGuid = p.getPersonGuid();
            if (personGuid != null) {
                packAttribute(idElement, "extension", personGuid);
                idTypeFound = true;
            }
        } else {
            if (p.getPersonIdentifierList() != null) {
                for (PersonIdentifier pi : p.getPersonIdentifierList()) {
                    if (pi.getIdentifierType() == type && pi.getIdentifier() != null) {
                        Element e = idElement;
                        if (idTypeFound) {
                            e = packCloneElement(idElement);
                        }
                        packAttribute(e, "extension", pi.getIdentifier());
                        idTypeFound = true;
                    }
                }
            }
        }
        if (!idTypeFound) {
            packRemoveNode(idElement);
        }
    }

    /**
     * Packs all person identifiers of a given type into a livingSubjectId subtree of a findPrson request.
     * <p>
     * Searches through all the identifiers for a person to find identifiers of the given
     * type. The first such identifier replaces the livingSubjectId template. Subsequent identifiers
     * are inserted into clones of the template. If there is no identifier of the given type,
     * the livingSubjectId template is removed.
     *
     * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person information containing the list of identifiers
     * @param oidPersonIdentifier the XML template OID for this person identifier type
     * @param type the person identifier type
     */
    private void packLivingSubjectPersonIdentifiers(Element subtree, Person p, String oidPersonIdentifier, PersonIdentifier.Type type) {
        Element idElement = commonGetLivingSubjectId(subtree, oidPersonIdentifier);
        if (idElement == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "LivingSubjectId type {0}, OID {1} was not found in the template XML file.",
                    new Object[]{type.name(), oidPersonIdentifier});
            return;
        }
        boolean idTypeFound = false;
        if (p.getPersonIdentifierList() != null) {
            for (PersonIdentifier pi : p.getPersonIdentifierList()) {
                if (pi.getIdentifierType() == type && pi.getIdentifier() != null) {
                    Element e = idElement;
                    if (idTypeFound) {
                        e = packCloneElement(idElement);
                    }
                    Element v = (Element) e.getElementsByTagName("value").item(0);
                    if (v == null) {
                        Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                                "LivingSubjectId type {0}, OID {1} had no value element in the template XML file.",
                                new Object[]{type.name(), oidPersonIdentifier});
                        return;
                    }
                    packAttribute(v, "extension", pi.getIdentifier());
                    idTypeFound = true;
                }
            }
        }
        if (!idTypeFound) {
            packRemoveNode(idElement);
        }
    }

    /**
     * Packs all fingerprints of a given type into a person subtree of a <code>Document</code>
     * <p>
     * Searches through through the person data for all fingerprints of the given type.
     * The first such fingerprint replaces the template value. Subsequent fingerprints
     * of the same type are inserted into clones of the template value. If there is no
     * fingerprint of this type, the template for fingerprints of this type is removed.
     * <p>
     * Note that we don't really expect multiple fingerprints of the same type in the
     * same message. But the list of fingerprints in the <code>Person</code> object
     * allows for this possibility, as does the XML message template. So this
     * method also allows for this possibility.
     *
     * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person information containing the list of identifiers
     * @param oidFingerprint the XML template OID for this fingerprint type
     * @param type the fingerprint type
     */
    private void packFingerprints(Element subtree, Person p, String oidFingerprint, Fingerprint.Type type) {
        Element fpElement = commonGetId(subtree, oidFingerprint);
        if (fpElement == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "Fingerprint type {0}, OID {1} was not found in the template XML file.",
                    new Object[]{type.name(), oidFingerprint});
            return;
        }
        boolean fpTypeFound = false;
        if (p.getFingerprintList() != null) {
            for (Fingerprint f : p.getFingerprintList()) {
                if (f.getFingerprintType() == type && f.getTemplate() != null) {
                    Element e = fpElement;
                    if (fpTypeFound) {
                        e = packCloneElement(fpElement);
                    }
                    packAttribute(e, "extension", packByteArray(f.getTemplate()));
                    fpTypeFound = true;
                }
            }
        }
        if (!fpTypeFound) {
            packRemoveNode(fpElement);
        }
    }

    /**
     * Packs all fingerprints of a given type into a livingSubjectId subtree of a findPrson request.
     * <p>
     * Searches through through the person data for all fingerprints of the given type.
     * The first such fingerprint replaces the livingSubjectId template. Subsequent fingerprints
     * of the same type are inserted into clones of the template. If there is no
     * fingerprint of this type, the template for fingerprints of this type is removed.
     * <p>
     * Note that we don't really expect multiple fingerprints of the same type in the
     * same message. But the list of fingerprints in the <code>Person</code> object
     * allows for this possibility, as does the XML message template. So this
     * method also allows for this possibility.
     *
     * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person information containing the list of identifiers
     * @param oidFingerprint the XML template OID for this fingerprint type
     * @param type the fingerprint type
     */
    private void packLivingSubjectFingerprints(Element subtree, Person p, String oidFingerprint, Fingerprint.Type type) {
        Element fpElement = commonGetLivingSubjectId(subtree, oidFingerprint);
        if (fpElement == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "LivingSubject Fingerprint type {0}, OID {1} was not found in the template XML file.",
                    new Object[]{type.name(), oidFingerprint});
            return;
        }
        boolean fpTypeFound = false;
        if (p.getFingerprintList() != null) {
            for (Fingerprint f : p.getFingerprintList()) {
                if (f.getFingerprintType() == type && f.getTemplate() != null) {
                    Element e = fpElement;
                    if (fpTypeFound) {
                        e = packCloneElement(fpElement);
                    }
                    Element v = (Element) e.getElementsByTagName("value").item(0);
                    if (v == null) {
                        Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                                "LivingSubject Fingerprint type {0}, OID {1} had no value element in the template XML file.",
                                new Object[]{type.name(), oidFingerprint});
                        return;
                    }
                    packAttribute(v, "extension", packByteArray(f.getTemplate()));
                    fpTypeFound = true;
                }
            }
        }
        if (!fpTypeFound) {
            packRemoveNode(fpElement);
        }
    }

    /**
     * Packs related person information into a <code>Document</code> subtree
     * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
     * @param p person data to pack into the subtree
     */
    private void packRelatedPersons(Element subtree, Person p) {
        Element eRelation = (Element) subtree.getElementsByTagName("personalRelationship").item(0);
        List<RelatedPerson> householdMembers = p.getHouseholdMembers();
        if (householdMembers == null || householdMembers.isEmpty()) {
            packRemoveNode(eRelation);
        } else {
            List<Element> elementList = new ArrayList<Element>();
            elementList.add(eRelation); // Always the first element in the list.
            for (int i = 1; i < householdMembers.size(); i++) { // From 2nd person (index 1) onwards...
                elementList.add(packCloneElement(eRelation));
            }
            for (int i = 0; i < householdMembers.size(); i++) { // From 1st person (index 0) ondwards...
                Element e = elementList.get(i);
                RelatedPerson rp = householdMembers.get(i);
                packTagAttribute(e, "code", "value", packEnum(rp.getRelation()));
                Person per = rp.getPerson();
                if (per == null) {
                    per = new Person(); // Empty person, so items below will be empty but avoid nullpointer.
                }
                packPersonName(e, per, "name");
                packTagAttribute(e, "administrativeGenderCode", "code", packEnum(per.getSex()));
                packTagAttribute(e, "birthTime", "value", packDate(per.getBirthdate()));
            }
        }
    }

    /**
     * Clones an element for packing additional values. Adds the element
     * as a new child to the same parent, placing it just after the
     * element that is cloned. If there is white space preceeding the
     * element to be cloned, that white space is also cloned, to
     * preserve formatting.
     *
     * @param e the element to clone
     * @return the cloned element
     */
    private Element packCloneElement(Element e) {
        Element clone = (Element) e.cloneNode(true);
        Node parent = e.getParentNode();
        Node previous = e.getPreviousSibling();
        Node next = e.getNextSibling();
        Node whiteSpace = null;
        if (previous != null
                && previous.getNodeType() == Node.TEXT_NODE
                && previous.getNodeValue().trim().length() == 0) {
            whiteSpace = previous.cloneNode(false); // deep clone not needed for whitespace
        }
        if (next != null) {
            if (whiteSpace != null) {
                parent.insertBefore(whiteSpace, next);
            }
            parent.insertBefore(clone, next);
        } else {
            if (whiteSpace != null) {
                parent.appendChild(whiteSpace);
            }
            parent.appendChild(clone);
        }
        return clone;
    }

    /**
     * Packs an attribute of a block's value element.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <(tag)>
     *         ...
     *         <value (attribute)=(value)/>
     *         ...
     *     </(tag)>
     *     ....
     * </subtree>
     * } </pre>
     * Finds the "value" element within the block and inserts the
     * value into the named attribute.
     *
     * @param subtree Document subtree in which to look for the element
     * @param tag name of the element under which to pack the value
     * @param attribute name of the element attribute to receive the value
     * @param value value to place in the attribute. If null, the element
     * is removed from the template.
     */
    private void packTagValueAttribute(Element subtree, String tag, String attribute, String value) {
        Element e = (Element) subtree.getElementsByTagName(tag).item(0);
        if (e == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packTagValueAttribute() could not find element {0} in the template XML file.", tag);
            return;
        }
        if (value != null) {
            Element v = (Element) e.getElementsByTagName("value").item(0);
            if (v == null) {
                Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                        "packTagValueAttribute() could not find ''value'' element under tag {0} in the template XML file.", tag);
                return;
            }
            packAttribute(v, attribute, value);
        } else {
            packRemoveNode(e);
        }
    }

    /**
     * Packs a value into an attribute of a tag.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <(tag) (attribute)=(value)/>
     *     ....
     * </subtree>
     * } </pre>
     * or removes the element if the value is null.
     * 
     * @param subtree Document subtree in which to look for the element
     * @param tag name of the element in which to pack the value
     * @param attribute name of the element attribute to receive the value
     * @param value value to place in the attribute. If null, the element
     * is removed from the template.
     */
    private void packTagAttribute(Element subtree, String tag, String attribute, String value) {
        Element e = (Element) subtree.getElementsByTagName(tag).item(0);
        if (e == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packTagAttribute() could not find element {0} (with attribute {1}) in the template XML file.",
                    new Object[]{tag, attribute});
            return;
        }
        packAttribute(e, attribute, value);
    }

    /**
     * Packs data into an element's attribute value:
     * <pre>
     * {@code
     * <tag (attribute)=(value)/>
     * } </pre>
     * or removes the element if the value is null.
     * 
     * @param e the element whose attribute we are to pack
     * @param attribute the name of the attribute to pack
     * @param value the value to put in the attribute
     */
    private void packAttribute(Element e, String attribute, String value) {
        Node attr = e.getAttributeNode(attribute);
        if (attr == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packAttribute() could not find attribute {0} on element {1} in the template XML file.",
                    new Object[]{attribute, e.getNodeName()});
            return;
        }
        if (value != null) {
            attr.setNodeValue(value);
        } else {
            packRemoveNode(e);
        }
    }

    /**
     * Packs data into the value of an element, by element tag.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <(tag)>
     *         (value)
     *     </(tag)>
     *     ....
     * </subtree>
     * } </pre>
     * or removes the element if the value is null.
     *
     * @param subtree Document subtree in which to look for the element
     * @param tag name of the element in which to pack the value
     * @param value value to pack in the element. If null, the element
     * is removed from the template.
     */
    private void packTagValue(Element subtree, String tag, String value) {
        Element e = (Element) subtree.getElementsByTagName(tag).item(0);
        if (e == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packTagValue() could not find element{0}", tag);
            return;
        }
        if (value != null) {
            e.setTextContent(value);
        } else {
            packRemoveNode(e);
        }
    }

    /**
     * Packs a value into the extension attribute of an id tag, by OID.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <id root=(oid) extension=(value)>
     *     ....
     * </subtree>
     * } </pre>
     * or removes the id tag if the value is null.
     *
     * @param subtree head of subtree within which to look for the id element.
     * @param oid the root attribute value for the id element we are looking for.
     * @param value the value to assign to the id tag, or null if the id tag should be removed.
     */
    private void packId(Element subtree, String oid, String value) {
        Element id = commonGetId(subtree, oid);
        if (id == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packId() could not find ID with root attribute {0} in the XML template file", oid);
            return;
        }
        packAttribute(id, "extension", value);
    }

    /**
     * Packs a livingSubjectId subtree.
     * Searches for a block matching the passed (oid) value:
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <livingSubjectId>
     *         <value root="(oid)" extension="(value)">
     *         <semanticsText>LivingSubject.xxx</semanticsText>
     *     </livingSubjectId>
     *     ....
     * </subtree>
     * } </pre>
     * Inserts (value) into the block, or deletes the block if the supplied (value) is null.
     * 
     * @param subtree head of subtree within which to look for the id element.
     * @param oid the root attribute value for the id element we are looking for.
     * @param value the value to assign inside the livingSubjectId block, or null if the block should be removed.
     */
    private void packLivingSubjectId(Element subtree, String oid, String value) {
        Element id = commonGetLivingSubjectId(subtree, oid);
        if (id == null) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packLivingSubjectId() could not find livingSubjectId with root attribute {0} in the XML template file", oid);
            return;
        }
        if (value != null) {
            Element v = (Element) id.getElementsByTagName("value").item(0);
            if (v == null) {
                Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                        "packLivingSubjectId() could not find ''value'' element under tag {0} in the template XML file.", oid);
                return;
            }
            packAttribute(v, "extension", value);
        } else {
            packRemoveNode(id);
        }
    }

    /**
     * Loads a XML message template into a <code>Document</code>.
     * The message template file is assumed to be among the resources available
     * to this class, in the "messages/" package relative to the package
     * storing the current class. In other words, the XML message template files
     * are packed into the .jar file containing this class.
     * <p>
     * Note: If the caller has a pre-formed XML message to use as the template
     * instead of the fixed template, then it will be used instead.
     *
     * @param m message to load the template for
     * @return the loaded template <code>Document</code>
     */
    private Document packTemplate(Message m) {
        Document doc = null;
        try {
            String templateFileName = "messages/" + m.getMessageType().getTemplateType().name() + ".xml";
            InputStream is = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            if (m.getXml() != null) {
                is = new ByteArrayInputStream(m.getXml().getBytes());
            } else {
                is = XmlPacker.class.getResourceAsStream(templateFileName);
            }
            doc = db.parse(is);
        } catch (SAXException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        packRemoveComments(doc); // Remove all comments from template to save space when sending.
        return doc;
    }

    /**
     * Removes all comments from a document.
     * (Used recursively, removes all comments below this element Node.)
     * <p>
     * The purpose of this method is so that XML comments may be used liberally
     * within an XML template to document the expected values, and these comments
     * may be removed before sending a protocol message using this template.
     * <p>
     * If a comment is on a line by itself, the line is also removed.
     * More precisely, if a comment node is preceeded by a text node consisting
     * of nothing but white space, the preceeding text node (as well as the
     * comment) is removed. This effectively removes the whole line if the
     * comment is on the line by itself. It also removes any white space
     * before the comment if the comment is on the end of the line after
     * other tags.
     *
     * @param node the Node below which we remove all comments.
     */
    private void packRemoveComments(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int c = 0; c < childNodes.getLength(); c++) {
            Node child = childNodes.item(c);
            switch (child.getNodeType()) {
                case Node.COMMENT_NODE:
                    c -= packRemoveNode(child);
                    break;
                case Node.ELEMENT_NODE:
                    packRemoveComments(childNodes.item(c)); // Recursively remove comments.
                    break;
                default:
                    break; // Other node types we don't expect to have child comments.
            }
        }
    }

    /**
     * Removes a node from the document node tree.
     * <p>
     * If the node is on a line by itself, the line is also removed.
     * More precisely, if the node is preceeded by a text node consisting
     * of nothing but white space, the preceeding text node (as well as the
     * node) is removed. This effectively removes the whole line if the
     * node is on the line by itself. It also removes any white space
     * before the node if the node is on the end of the line after
     * another node.
     *
     * @param n
     * @return the number of nodes removed (1 if only the node was removed,
     * 2 if a whitespace-only text node before it was also removed.)
     * This may be useful to our caller if they are stepping through a
     * list of nodes using an index. It tells them how many to subtract
     * from the index to compensate for the deleted nodes and still
     * properly evaluate all the nodes in the list.
     */
    private int packRemoveNode(Node n) {
        int numberRemoved = 1;
        Node parent = n.getParentNode();
        Node previous = n.getPreviousSibling();
        parent.removeChild(n);
        if (previous != null
                && previous.getNodeType() == Node.TEXT_NODE
                && previous.getNodeValue().trim().length() == 0) {
            parent.removeChild(previous);
            numberRemoved = 2;
        }
        return numberRemoved;
    }

    /**
     * Packs a Send Log Entry message into a document
     * Uses LogEntry message type.
     *
     * @param m message to be packed
     * @return DOM Document structure
     */
    private Document packLogEntryMessage(Message m) {
        if (!(m.getMessageData() instanceof LogEntry)) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE,
                    "packLogEntryMessage() - Expected data class LogEntry, got {0}",
                    m.getMessageData().getClass().getName());
        }
        LogEntry logEntry = (LogEntry) m.getMessageData();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // Create instance of DocumentBuilderFactory
        DocumentBuilder db = null; 		// Get the DocumentBuilder
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = db.newDocument(); // Create a blank Document
        Element root = doc.createElement("LogEntry"); // Create the root element
        doc.appendChild(root); // Root element is child of the Document
        packNewElement(doc, root, "sourceAddress", Mediator.getProperty("Instance.Address"));
        packNewElement(doc, root, "sourceName", Mediator.getProperty("Instance.Name"));
        packNewElement(doc, root, "messageId", m.getMessageId());
        packNewElement(doc, root, "severity", logEntry.getSeverity());
        packNewElement(doc, root, "class", logEntry.getClassName());
        packNewElement(doc, root, "dateTime", packDateTime(logEntry.getDateTime()));
        packNewElement(doc, root, "message", logEntry.getMessage());
        return doc;
    }

    /**
     * Packs a value into a new <code>Element</code>, and links it to a parent <code>Element</code>.
     * If the value is null , the new element is not added
     *
     * @param doc the document we are packing into
     * @param parent parent element for our new element
     * @param elementName name of the new element to create
     * @param value value of the new element to create
     */
    private void packNewElement(Document doc, Element parent, String elementName, String value) {
        if (value != null) {
            Element element = doc.createElement(elementName);
            element.setTextContent(value);
            parent.appendChild(element);
        }
    }

    /**
     * Packs a binary array of bytes into a hexadecimal-encoded string.
     * If the byte array is <code>null</code>, returns <code>null</code>.
     *
     * @param byteArray binary array of bytes to pack
     * @return the array of bytes encoded as a hexadecimal string
     */
    private String packByteArray(byte[] byteArray) {
        if (byteArray != null) {
            StringBuilder hex = new StringBuilder(byteArray.length * 2);
            for (byte b : byteArray) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } else {
            return null;
        }
    }

    /**
     * Packs the value of an enumerated type into a string.
     * If the enumerated type is <code>null</code>, returns <code>null</code>.
     *
     * @param e the enumerated value to be packed
     * @return the original enumerated value, packed into a string
     */
    private String packEnum(Enum e) {
        if (e != null) {
            return e.name();
        } else {
            return null;
        }
    }

    /**
     * Packs the value of a boolean into a string.
     * If the value is <code>false</code>, returns <code>null</code>.
     *
     * @param b the boolean value to be packed
     * @return the string, "true" if b is true, otherwise null.
     */
    private String packBoolean(boolean b) {
        if (b) {
            return Boolean.toString(b);
        } else {
            return null;
        }
    }

    /**
     * Packs a <code>Date</code> (not including time) into a string.
     * If the date is <code>null</code>, returns <code>null</code>.
     *
     * @param date the date value to be packed
     * @return <code>String</code> containing the packed date.
     */
    private String packDate(Date date) {
        String dateString = null;
        if (date != null) {
            dateString = SIMPLE_DATE_FORMAT.format(date);
        }
        return dateString;
    }

    /**
     * Packs a <code>Date</code> (including time) into a string.
     *
     * @param dateTime the date and time value to be packed
     * @return <code>String</code> containing the packed date and time.
     */
    private String packDateTime(Date dateTime) {
        String dateString = null;
        if (dateTime != null) {
            dateString = SIMPLE_DATE_TIME_FORMAT.format(dateTime);
        }
        return dateString;
    }

    /*
     * ---------------------------------------------------------------------------------------
     *
     *                       U  N  P  A  C  K        M  E  T  H  O  D  S
     *
     * ---------------------------------------------------------------------------------------
     */
    /**
     * Unpacks a XML string into an object.
     *
     * @param m Message to unpack
     */
    void unpack(Message m) {
        Document doc = unpackXml(m.getXml());
        unpackDocument(m, doc);
    }

    /**
     * Unpacks a XML string into DOM Document structure
     *
     * @param xml String containing XML request message
     * @return the DOM Document structure
     */
    Document unpackXml(String xml) {
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
     * Unpacks a DOM Document structure into message data
     *
     * @param m the message to unpack
     * @param doc the DOM Document structure to decode
     */
    void unpackDocument(Message m, Document doc) {
        Element root = doc.getDocumentElement();
        String rootName = root.getTagName();
        MessageType messageType = MessageTypeRegistry.find(rootName);
        m.setMessageType(messageType);

        switch (messageType.getTemplateType()) {
            case findPerson:
                unpackFindPersonMessage(m, root);
                break;

            case findPersonResponse:
                unpackFindPersonResponseMessage(m, root);
                break;

            case createPerson: // Uses unpackGenericPersonMessage(), below.
            case modifyPerson: // Uses unpackGenericPersonMessage(), below.
            case notifyPersonChanged:
                unpackGenericPersonMessage(m, root);
                break;

            case logEntry:
                unpackLogEntryMessage(m, root);
                break;
//@pchemutai added on 19th May 2011 to handle Work messages'
            case getWork: //Uses unpackWorkMessage(), below.
            case workDone://Uses unpackWorkMessage(), below.
            case reassignWork://Uses unpackWorkMessage(), below.
                unpackWorkMessage(m, root);
                break;
        }
    }

    /**
     * Unpacks a generic HL7 person-related message into a <code>Document</code>.
     * <p>
     * Several of the HL7 person-related messages use the same formatting
     * rules, even though the templates differ. (The templates differ only
     * in the boilerplate parts that do not concern us directly.)
     * These messages are:
     * <p>
     * CREATE PERSON <br>
     * MODIFY PERSON <br>
     * CREATE PERSON ACCEPTED <br>
     * MODIFY PERSON ACCEPTED <br>
     * NOTIFY PERSON CHANGED
     *
     * @param m the message contents to fill in
     * @param e root node of the person message <code>Document</code> parsed from XML
     */
    private void unpackGenericPersonMessage(Message m, Element e) {
        PersonRequest personRequest = new PersonRequest();
        m.setMessageData(personRequest);
        Person p = new Person();
        personRequest.setPerson(p);
        unpackHl7Header(m, e);
        Element ePerson = (Element) e.getElementsByTagName("patient").item(0);
        unpackPerson(p, ePerson);
        if (unpackTagValue(e, "acceptAckCode").equals("AL")) {
            personRequest.setResponseRequested(true);
        }
    }

    /**
     * Unpacks a find person request <code>Document</code> into message data.
     * Uses HL7 Patient Registry Find Candidates Query, PRPA_IN201305UV02.
     *
     * @param m the message contents to fill in
     * @param e root of the person message <code>Document</code> parsed from XML
     */
    private void unpackFindPersonMessage(Message m, Element e) {
        PersonRequest personRequest = new PersonRequest();
        m.setMessageData(personRequest);
        Person p = new Person();
        personRequest.setPerson(p);
        unpackHl7Header(m, e);
        Element q = (Element) e.getElementsByTagName("queryByParameter").item(0);
        unpackPersonName(p, q, "livingSubjectName");
        p.setSex((Person.Sex) unpackEnum(Person.Sex.values(), unpackTagValueAttribute(e, "livingSubjectAdministrativeGender", "code")));
        p.setBirthdate(unpackDate(unpackTagValueAttribute(e, "livingSubjectBirthTime", "value")));
        p.setDeathdate(unpackDate(unpackTagValueAttribute(e, "livingSubjectDeceasedTime", "value")));
        p.setOtherName(unpackLivingSubjectId(q, OID_OTHER_NAME));
        p.setClanName(unpackLivingSubjectId(q, OID_CLAN_NAME));
        p.setAliveStatus((Person.AliveStatus) unpackEnum(Person.AliveStatus.values(), unpackLivingSubjectId(q, OID_ALIVE_STATUS)));
        p.setMothersFirstName(unpackLivingSubjectId(q, OID_MOTHERS_FIRST_NAME));
        p.setMothersMiddleName(unpackLivingSubjectId(q, OID_MOTHERS_MIDDLE_NAME));
        p.setMothersLastName(unpackLivingSubjectId(q, OID_MOTHERS_LAST_NAME));
        p.setFathersFirstName(unpackLivingSubjectId(q, OID_FATHERS_FIRST_NAME));
        p.setFathersMiddleName(unpackLivingSubjectId(q, OID_FATHERS_MIDDLE_NAME));
        p.setFathersLastName(unpackLivingSubjectId(q, OKD_FATHERS_LAST_NAME));
        p.setCompoundHeadFirstName(unpackLivingSubjectId(q, OID_COMPOUND_HEAD_FIRST_NAME));
        p.setCompoundHeadMiddleName(unpackLivingSubjectId(q, OID_COMPOUND_HEAD_MIDDLE_NAME));
        p.setCompoundHeadLastName(unpackLivingSubjectId(q, OID_COMPOUND_HEAD_LAST_NAME));
        p.setMaritalStatus((Person.MaritalStatus) unpackEnum(Person.MaritalStatus.values(), unpackLivingSubjectId(q, OID_MARITAL_STATUS)));
        p.setConsentSigned((Person.ConsentSigned) unpackEnum(Person.ConsentSigned.values(), unpackLivingSubjectId(q, OID_CONSENT_SIGNED)));
        p.setSiteName(unpackLivingSubjectId(q, OID_SITE_NAME));
        p.setVillageName(unpackLivingSubjectId(q, OID_VILAGE_NAME));
        unpackLivingSubjectPersonIdentifiers(p, q, OID_PATIENT_REGISTRY_ID, PersonIdentifier.Type.patientRegistryId);
        unpackLivingSubjectPersonIdentifiers(p, q, OID_MASTER_PATIENT_REGISTRY_ID, PersonIdentifier.Type.masterPatientRegistryId);
        unpackLivingSubjectPersonIdentifiers(p, q, OID_CCC_UNIVERSAL_UNIQUE_ID, PersonIdentifier.Type.cccUniqueId);
        unpackLivingSubjectPersonIdentifiers(p, q, OID_CCC_LOCAL_PATIENT_ID, PersonIdentifier.Type.cccLocalId);
        unpackLivingSubjectPersonIdentifiers(p, q, KISUMU_HDSS_ID, PersonIdentifier.Type.kisumuHdssId);
        unpackLivingSubjectFingerprints(p, q, OID_FINGERPRINT_LEFT_INDEX, Fingerprint.Type.leftIndexFinger);
        unpackLivingSubjectFingerprints(p, q, OID_FINGERPRINT_LEFT_MIDDLE, Fingerprint.Type.leftMiddleFinger);
        unpackLivingSubjectFingerprints(p, q, OID_FINGERPRINT_LEFT_RING, Fingerprint.Type.leftRingFinger);
        unpackLivingSubjectFingerprints(p, q, OID_FINGERPRINT_RIGHT_INDEX, Fingerprint.Type.rightIndexFinger);
        unpackLivingSubjectFingerprints(p, q, OID_FINGERPRINT_RIGHT_MIDDLE, Fingerprint.Type.rightMiddleFinger);
        unpackLivingSubjectFingerprints(p, q, OID_FINGERPRINT_RIGHT_RING, Fingerprint.Type.rightRingFinger);
    }

    /**
     * Unpacks a find person response <code>Document</code> into message data.
     * Uses HL7 Patient Registry Find Candidates Query Response, PRPA_IN201306UV02.
     *
     * @param m the message contents to fill in
     * @param e root of the person message <code>Document</code> parsed from XML
     */
    private void unpackFindPersonResponseMessage(Message m, Element e) {
        PersonResponse personResponse = new PersonResponse();
        m.setMessageData(personResponse);
        unpackHl7Header(m, e);
        NodeList nodeList = e.getElementsByTagName("subject");
        if (nodeList.getLength() != 0) {
            List<Person> personList = new ArrayList<Person>();
            personResponse.setPersonList(personList);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Person p = new Person();
                Element el = (Element) nodeList.item(i);
                unpackCandidate(p, el);
                personList.add(p);
            }
        }
    }

    /**
     * Unpack a candidate in a findPerson response.
     * 
     * @param p the person data to fill in
     * @param e head of the <code>Document</code> subtree in which this person is found
     */
    private void unpackCandidate(Person p, Element e) {
        unpackPerson(p, e);
        unpackRelatedPersons(p, e);
        p.setMatchScore(unpackInt(unpackTagValueAttribute(e, "queryMatchObservation", "value")));
    }

    /**
     * Unpacks related persons into person data
     *
     * @param p the person data to fill in
     * @param e head of the <code>Document</code> subtree in which this person is found
     */
    private void unpackRelatedPersons(Person p, Element e) {
        NodeList nodeList = e.getElementsByTagName("personalRelationship");
        if (nodeList.getLength() != 0) {
            List<RelatedPerson> relatedPersonList = new ArrayList<RelatedPerson>();
            p.setHouseholdMembers(relatedPersonList);
            for (int i = 0; i < nodeList.getLength(); i++) {
                RelatedPerson rp = new RelatedPerson();
                relatedPersonList.add(rp);
                Element el = (Element) nodeList.item(i);
                // Set the type of relation:
                rp.setRelation((RelatedPerson.Relation) unpackEnum(RelatedPerson.Relation.values(), unpackTagAttribute(el, "code", "value")));
                // Set the details of the new person:
                Person per = new Person();
                rp.setPerson(per);
                unpackPersonName(per, el, "name");
                per.setSex((Person.Sex) unpackEnum(Person.Sex.values(), unpackTagAttribute(e, "administrativeGenderCode", "code")));
                per.setBirthdate(unpackDate(unpackTagAttribute(e, "birthTime", "value")));
            }
        }
    }

    /**
     * Unpacks a standard HL7 message header into message data.
     *
     * @param m the message contents to fill in
     * @param e root of the person message <code>Document</code> parsed from XML
     */
    private void unpackHl7Header(Message m, Element e) {
        m.setMessageId(unpackId(e, OID_MESSAGE_ID));
        Element receiver = (Element) e.getElementsByTagName("receiver").item(0);
        if (receiver != null) {
            m.setDestinationAddress(unpackId(receiver, OID_APPLICATION_ADDRESS));
            m.setDestinationName(unpackTagValue(receiver, "name"));
        }
        Element sender = (Element) e.getElementsByTagName("sender").item(0);
        if (sender != null) {
            m.setSourceAddress(unpackId(sender, OID_APPLICATION_ADDRESS));
            m.setSourceName(unpackTagValue(sender, "name"));
        }
    }

    /**
     * Unpacks a person document subtree into person data
     *
     * @param p the person data to fill in
     * @param e head of the <code>Document</code> subtree in which this person is found
     */
    private void unpackPerson(Person p, Element e) {
        unpackPersonName(p, e, "name");
        p.setSex((Person.Sex) unpackEnum(Person.Sex.values(), unpackTagAttribute(e, "administrativeGenderCode", "code")));
        p.setBirthdate(unpackDate(unpackTagAttribute(e, "birthTime", "value")));
        p.setDeathdate(unpackDate(unpackTagAttribute(e, "deceasedTime", "value")));
        p.setOtherName(unpackId(e, OID_OTHER_NAME));
        p.setClanName(unpackId(e, OID_CLAN_NAME));
        p.setAliveStatus((Person.AliveStatus) unpackEnum(Person.AliveStatus.values(), unpackId(e, OID_ALIVE_STATUS)));
        p.setMothersFirstName(unpackId(e, OID_MOTHERS_FIRST_NAME));
        p.setMothersMiddleName(unpackId(e, OID_MOTHERS_MIDDLE_NAME));
        p.setMothersLastName(unpackId(e, OID_MOTHERS_LAST_NAME));
        p.setFathersFirstName(unpackId(e, OID_FATHERS_FIRST_NAME));
        p.setFathersMiddleName(unpackId(e, OID_FATHERS_MIDDLE_NAME));
        p.setFathersLastName(unpackId(e, OKD_FATHERS_LAST_NAME));
        p.setCompoundHeadFirstName(unpackId(e, OID_COMPOUND_HEAD_FIRST_NAME));
        p.setCompoundHeadMiddleName(unpackId(e, OID_COMPOUND_HEAD_MIDDLE_NAME));
        p.setCompoundHeadLastName(unpackId(e, OID_COMPOUND_HEAD_LAST_NAME));
        p.setMaritalStatus((Person.MaritalStatus) unpackEnum(Person.MaritalStatus.values(), unpackId(e, OID_MARITAL_STATUS)));
        p.setConsentSigned((Person.ConsentSigned) unpackEnum(Person.ConsentSigned.values(), unpackId(e, OID_CONSENT_SIGNED)));
        p.setVillageName(unpackId(e, OID_VILAGE_NAME));
        p.setPreviousVillageName(unpackId(e, OID_PREVIOUS_VILLAGE_NAME));
        p.setLastMoveDate(unpackDate(unpackId(e, OID_LAST_MOVE_DATE)));
        p.setExpectedDeliveryDate(unpackDate(unpackId(e, OID_EXPECTED_DELIVERY_DATE)));
        p.setPregnancyEndDate(unpackDate(unpackId(e, OID_PREGNANCY_END_DATE)));
        p.setPregnancyOutcome((Person.PregnancyOutcome) unpackEnum(Person.PregnancyOutcome.values(), unpackId(e, OID_PREGNANCY_OUTCOME)));
        p.setSiteName(unpackId(e, OID_SITE_NAME));
        p.setFingerprintMatched(unpackBoolean(unpackId(e, OID_FINGERPRINT_MATCHED)));
        p.setLastRegularVisit(unpackVisit(e, OID_REGULAR_VISIT_ADDRESS, OID_REGULAR_VISIT_DATE));
        p.setLastOneOffVisit(unpackVisit(e, OID_ONEOFF_VISIT_ADDRESS, OID_ONEOFF_VISIT_DATE));
        unpackPersonIdentifiers(p, e, OID_PATIENT_REGISTRY_ID, PersonIdentifier.Type.patientRegistryId);
        unpackPersonIdentifiers(p, e, OID_MASTER_PATIENT_REGISTRY_ID, PersonIdentifier.Type.masterPatientRegistryId);
        unpackPersonIdentifiers(p, e, OID_CCC_UNIVERSAL_UNIQUE_ID, PersonIdentifier.Type.cccUniqueId);
        unpackPersonIdentifiers(p, e, OID_CCC_LOCAL_PATIENT_ID, PersonIdentifier.Type.cccLocalId);
        unpackPersonIdentifiers(p, e, KISUMU_HDSS_ID, PersonIdentifier.Type.kisumuHdssId);
        unpackFingerprints(p, e, OID_FINGERPRINT_LEFT_INDEX, Fingerprint.Type.leftIndexFinger);
        unpackFingerprints(p, e, OID_FINGERPRINT_LEFT_MIDDLE, Fingerprint.Type.leftMiddleFinger);
        unpackFingerprints(p, e, OID_FINGERPRINT_LEFT_RING, Fingerprint.Type.leftRingFinger);
        unpackFingerprints(p, e, OID_FINGERPRINT_RIGHT_INDEX, Fingerprint.Type.rightIndexFinger);
        unpackFingerprints(p, e, OID_FINGERPRINT_RIGHT_MIDDLE, Fingerprint.Type.rightMiddleFinger);
        unpackFingerprints(p, e, OID_FINGERPRINT_RIGHT_RING, Fingerprint.Type.rightRingFinger);
    }

    /**
     * Unpacks a person name into a <code>Person</code> object.
     * For findPerson the tagname containing the name elements is
     * "livingSubjectName". For other person messages the tagName is "name".
     *
     * @param p the person data into which to put the person name.
     * @param e head of the <code>Document</code> subtree in which this person is found
     * @param tagName name of the enclosing element for the person's name
     */
    private void unpackPersonName(Person p, Element e, String tagName) {
        Element eName = (Element) e.getElementsByTagName("name").item(0);
        if (eName != null) {
            NodeList givenList = eName.getElementsByTagName("given");
            if (givenList.getLength() > 0) {
                p.setFirstName(givenList.item(0).getTextContent());
                if (givenList.getLength() > 1) {
                    p.setMiddleName(givenList.item(1).getTextContent());
//                    String firstName = (givenList.item(0).getNodeValue());
//                    p.setFirstName(firstName);
//                    if (givenList.getLength() > 1) {
//                        String middleName = givenList.item(1).getNodeValue();
//                        p.setMiddleName(middleName);
//                    }
                }
                p.setLastName(unpackTagValue(eName, "family"));
            }
        }
    }

    /**
     * Unpacks visit information into a <code>Visit</code>. If any of the
     * visit information is present, allocates a <code>Visit</code> object
     * and sets the information into the object. If none of the visit
     * information is present, returns <code>null</code>.
     *
     * @param e head of the <code>Document</code> subtree in which this visit is found
     * @param oidVisitAddress OID for the XML id tag containing the visit address
     * @param oidVisitDate OID for the XML id tag containing the visit date
     * @return v unpacked visit data
     */
    private Visit unpackVisit(Element e, String oidVisitAddress, String oidVisitDate) {
        Visit v = null;
        String address = unpackId(e, oidVisitAddress);
        Date visitDate = unpackDate(unpackId(e, oidVisitDate));
        if (address != null || visitDate != null) {
            v = new Visit();
            v.setAddress(address);
            v.setVisitDate(visitDate);
        }
        return v;
    }

    /**
     * Unpacks all ID-tagged person identifiers of a given type.
     * <p>
     * Searches through all the person identifiers in a <code>Document</code> subtree
     * to find identifiers of the given type. For each such identifier, allocates
     * a <code>PersonIdentifier</code> object and attaches it to the <code>Person</code> object.
     * <p>
     * An identifier of type patientRegistryId is unpacked into the person.personGuid field.
     * All other identifiers are unpacked into the array of PersonIdentifier.
     * 
     * @param p person information
     * @param e head of the <code>Document</code> subtree in which
     * these person identifiers are to be found
     * @param oidPersonIdentifier the XML template OID for this person identifier type
     * @param type the person identifier type
     */
    private void unpackPersonIdentifiers(Person p, Element e, String oidPersonIdentifier, PersonIdentifier.Type type) {
        List<Element> idList = unpackGetIdList(e, oidPersonIdentifier);
        for (Element id : idList) {
            if (type == PersonIdentifier.Type.patientRegistryId) {
                p.setPersonGuid(unpackAttribute(id, "extension"));
            } else {
                PersonIdentifier pi = new PersonIdentifier();
                pi.setIdentifier(unpackAttribute(id, "extension"));
                pi.setIdentifierType(type);
                if (p.getPersonIdentifierList() == null) {
                    p.setPersonIdentifierList(new ArrayList<PersonIdentifier>());
                }
                p.getPersonIdentifierList().add(pi);
            }
        }
    }

    /**
     * Unpacks all LivingSubjectID-tagged person identifiers of a given type.
     * <p>
     * Searches through all the LivingSubject person identifiers in a <code>Document</code> subtree
     * to find identifiers of the given type. For each such identifier, allocates
     * a <code>PersonIdentifier</code> object and attaches it to the <code>Person</code> object.
     *
     * @param p person information
     * @param e head of the <code>Document</code> subtree in which
     * these person identifiers are to be found
     * @param oidPersonIdentifier the XML template OID for this person identifier type
     * @param type the person identifier type
     */
    private void unpackLivingSubjectPersonIdentifiers(Person p, Element e, String oidPersonIdentifier, PersonIdentifier.Type type) {
        List<Element> idList = unpackGetLivingSubjectIdList(e, oidPersonIdentifier);
        for (Element id : idList) {
            Element v = (Element) id.getElementsByTagName("value").item(0); // unpackGetLivingSubjectIdList() guarantees this exists.
            PersonIdentifier pi = new PersonIdentifier();
            pi.setIdentifier(unpackAttribute(v, "extension"));
            pi.setIdentifierType(type);
            if (p.getPersonIdentifierList() == null) {
                p.setPersonIdentifierList(new ArrayList<PersonIdentifier>());
            }
            p.getPersonIdentifierList().add(pi);
        }
    }

    /**
     * Unpacks all ID-tagged fingerprints of a given type.
     * <p>
     * Searches through all the person identifiers in a <code>Document</code> subtree
     * to find identifiers of the given type. For each such identifier, allocates
     * a <code>PersonIdentifier</code> object and attaches it to the <code>Person</code> object.
     * <p>
     * Note that we don't really expect multiple fingerprints of the same type in the
     * same message. But the list of fingerprints in the <code>Person</code> object
     * allows for this possibility, as does the XML message template. So this
     * method also allows for this possibility.
     *
     * @param p person information
     * @param e head of the <code>Document</code> subtree in which
     * these fingerprints are to be found
     * @param oidFingerprint the XML template OID for this fingerprint type
     * @param type fingerprint type
     */
    private void unpackFingerprints(Person p, Element e, String oidFingerprint, Fingerprint.Type type) {
        List<Element> idList = unpackGetIdList(e, oidFingerprint);
        for (Element id : idList) {
            Fingerprint f = new Fingerprint();
            f.setTemplate(unpackByteArray(unpackAttribute(id, "extension")));
            f.setFingerprintType(type);
            if (p.getFingerprintList() == null) {
                p.setFingerprintList(new ArrayList<Fingerprint>());
            }
            p.getFingerprintList().add(f);
        }
    }

    /**
     * Unpacks all LivingSubjectID-tagged fingerprints of a given type.
     * <p>
     * Searches through all the person identifiers in a <code>Document</code> subtree
     * to find identifiers of the given type. For each such identifier, allocates
     * a <code>PersonIdentifier</code> object and attaches it to the <code>Person</code> object.
     * <p>
     * Note that we don't really expect multiple fingerprints of the same type in the
     * same message. But the list of fingerprints in the <code>Person</code> object
     * allows for this possibility, as does the XML message template. So this
     * method also allows for this possibility.
     *
     * @param p person information
     * @param e head of the <code>Document</code> subtree in which
     * these fingerprints are to be found
     * @param oidFingerprint the XML template OID for this fingerprint type
     * @param type fingerprint type
     */
    private void unpackLivingSubjectFingerprints(Person p, Element e, String oidFingerprint, Fingerprint.Type type) {
        List<Element> idList = unpackGetLivingSubjectIdList(e, oidFingerprint);
        for (Element id : idList) {
            Element v = (Element) id.getElementsByTagName("value").item(0); // unpackGetLivingSubjectIdList() guarantees this exists.
            Fingerprint f = new Fingerprint();
            f.setTemplate(unpackByteArray(unpackAttribute(v, "extension")));
            f.setFingerprintType(type);
            if (p.getFingerprintList() == null) {
                p.setFingerprintList(new ArrayList<Fingerprint>());
            }
            p.getFingerprintList().add(f);
        }
    }

    /**
     * Finds a list of &lt;id&gt; elements with a given "root" attribute value.
     *
     * @param subtree head of the subtree in which to search
     * @param name root attribute value to search for
     * @return the list of elements (empty list if none are found)
     */
    private List<Element> unpackGetIdList(Element subtree, String name) {
        NodeList idList = subtree.getElementsByTagName("id");
        List<Element> returnList = new ArrayList<Element>();
        for (int i = 0; i < idList.getLength(); i++) {
            Element id = (Element) idList.item(i);
            Node aRoot = id.getAttributeNode("root");
            if (aRoot != null && aRoot.getNodeValue().equals(name)) {
                returnList.add(id);
            }
        }
        return returnList;
    }

    /**
     * Finds a list of &lt;LivingSubjectId&gt; elements whose value element
     * has a given "root" attribute value.
     *
     * @param subtree head of the subtree in which to search
     * @param name root attribute value to search for
     * @return the list of elements (empty list if none are found)
     */
    private List<Element> unpackGetLivingSubjectIdList(Element subtree, String name) {
        NodeList idList = subtree.getElementsByTagName("livingSubjectId");
        List<Element> returnList = new ArrayList<Element>();
        for (int i = 0; i < idList.getLength(); i++) {
            Element id = (Element) idList.item(i);
            Element v = (Element) id.getElementsByTagName("value").item(0);
            if (v != null) {
                Node aRoot = v.getAttributeNode("root");
                if (aRoot != null && aRoot.getNodeValue().equals(name)) {
                    returnList.add(id);
                }
            }
        }
        return returnList;
    }

    /**
     * Unpacks an element's attribute value, by element tag.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <(tag) (attribute)=(value)/>
     *     ....
     * </subtree>
     * } </pre>
     *
     * @param subtree Document subtree in which to look for the element
     * @param name name of the element from which to unpack the value
     * @param attribute name of the element attribute containing the value
     * @return the attribute value. If the element was not found, returns null.
     */
    private String unpackTagAttribute(Element subtree, String name, String attribute) {
        Element e = (Element) subtree.getElementsByTagName(name).item(0);
        if (e != null) {
            return unpackAttribute(e, attribute);
        } else {
            return null;
        }
    }

    /**
     * Unpacks an attribute of a block's value element.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <(tag)>
     *         ...
     *         <value (attribute)=(value)/>
     *         ...
     *     </(tag)>
     *     ....
     * </subtree>
     * } </pre>
     *
     * @param subtree Document subtree in which to look for the block
     * @param tag name of the block from which to unpack the value
     * @param attribute name of the element attribute containing the value
     * @return the attribute value. If the element was not found, returns null.
     */
    private String unpackTagValueAttribute(Element subtree, String tag, String attribute) {
        Element e = (Element) subtree.getElementsByTagName(tag).item(0);
        if (e != null) {
            Element v = (Element) e.getElementsByTagName("value").item(0);
            if (v != null) {
                return unpackAttribute(v, attribute);
            }
        }
        return null;
    }

    /**
     * Unpacks an element's attribute value:
     * <pre>
     * {@code
     * <tag (attribute)=(value)/>
     * } </pre>
     *
     * @param e element from which to unpack the attribute
     * @param attribute name of the attribute in which to find the value
     * @return value of the attribute, or null if the attribute was not present.
     *
     */
    private String unpackAttribute(Element e, String attribute) {
        Node attr = e.getAttributeNode(attribute);
        if (attr != null) {
            return attr.getNodeValue();
        } else {
            return null;
        }
    }

    /**
     * Unpacks data from an element value.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <(tag)>
     *         (value)
     *     </(tag)>
     *     ....
     * </subtree>
     * } </pre>
     *
     * @param subtree Document subtree in which to look for the element
     * @param tag name of the element from which to unpack the value
     * @return value value of the element. If the element was not found,
     * returns null.
     */
    private String unpackTagValue(Element subtree, String tag) {
        Element e = (Element) subtree.getElementsByTagName(tag).item(0);
        if (e != null) {
            return e.getTextContent();
        } else {
            return null;
        }
    }

    /**
     * Unpacks the extension value of an id tag, by OID.
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <id root=(oid) extension=(value)>
     *     ....
     * </subtree>
     * } </pre>
     * <p>
     *
     * @param subtree head of subtree within which to look for the id element.
     * @param oid the root attribute value for the id element we are looking for.
     * @return value of the extension attribute, or null if tag not found.
     */
    private String unpackId(Element subtree, String oid) {
        Element id = commonGetId(subtree, oid);
        if (id != null) {
            Node aExtension = id.getAttributeNode("extension");
            if (aExtension != null) {
                return aExtension.getNodeValue();
            }
        }
        return null;
    }

    /**
     * Unpacks a livingSubjectId subtree.
     * Searches for a block matching the passed (oid) value:
     * <pre>
     * {@code
     * <subtree>
     *     ....
     *     <livingSubjectId>
     *         <value root="(oid)" extension="(value)">
     *         <semanticsText>LivingSubject.xxx</semanticsText>
     *     </livingSubjectId>
     *     ....
     * </subtree>
     * }
     *</pre>
     * Returns the extension value if found, or null if not.
     *
     * @param subtree head of subtree within which to look for the livingSubjectId element.
     * @param oid the root attribute value for the id element we are looking for.
     * @return value of the extension attribute, or null if tag not found.
     */
    private String unpackLivingSubjectId(Element subtree, String oid) {
        Element id = commonGetLivingSubjectId(subtree, oid);
        if (id != null) {
            Element v = (Element) id.getElementsByTagName("value").item(0);
            if (v != null) {
                Node aExtension = v.getAttributeNode("extension");
                if (aExtension != null) {
                    return aExtension.getNodeValue();
                }
            }
        }
        return null;
    }

    /**
     * Unpacks a Log Entry <code>Document</code> into message data.
     * Uses LogEntry message type.
     *
     * @param m the message contents to fill in
     * @param e root of the person message <code>Document</code> parsed from XML
     */
    private void unpackLogEntryMessage(Message m, Element e) {
        LogEntry logEntry = new LogEntry();
        m.setMessageData(logEntry);
        logEntry.setSeverity(e.getElementsByTagName("severity").item(0).getTextContent());
        logEntry.setClassName(e.getElementsByTagName("class").item(0).getTextContent());
        logEntry.setDateTime(unpackDateTime(e.getElementsByTagName("dateTime").item(0).getTextContent()));
        logEntry.setMessage(e.getElementsByTagName("message").item(0).getTextContent());
        logEntry.setInstance(e.getElementsByTagName("sourceAddress").item(0).getTextContent());
    }

    /**
     * Unpacks a Work message <code>Document</code> into message data.
     * Uses Work message type.
     *
     * @param m the message contents to fill in
     * @param e root of the person message <code>Document</code> parsed from XML
     */
    private void unpackWorkMessage(Message m, Element e) {
        Work work = new Work();
        m.setMessageData(work);
        work.setSourceAddress(e.getElementsByTagName("sourceAddress").item(0).getTextContent());
        work.setNotificationId(e.getElementsByTagName("notificationId").item(0).getTextContent());
        work.setReassignAddress(e.getElementsByTagName("reassignAddress").item(0).getTextContent());
    }

    /**
     * Unpacks a hexadecimal-encoded string into a binary byte array.
     *
     * @param hex the hexadecimal string to unpack
     * @return the resulting binary byte array.
     * Returns null if the hex string was null.
     */
    private byte[] unpackByteArray(String hex) {
        byte[] bytes = null;
        if (hex != null) {
            bytes = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
        }
        return bytes;
    }

    /**
     * Given a string and a set of enumerated values, find which enumerated
     * member has a name matching the string.
     *
     * @param values the set of enumerated values in which to search
     * @param text text to match against the value names
     * @return the enumerated value if there was a match, otherwise null
     */
    private Enum unpackEnum(Enum[] values, String text) {
        for (Enum e : values) {
            if (e.name().equalsIgnoreCase(text)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Parse a string to a integer value. If the string is null, return zero.
     *
     * @param text text to parse into an integer.
     * @return the integer value if could be parsed, otherwise zero.
     */
    private int unpackInt(String text) {
        int returnInt = 0;
        if (text != null) {
            try {
                returnInt = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                Logger.getLogger(XmlPacker.class.getName()).log(Level.WARNING, "Can't parse integer '" + text + "'", ex);
            }
        }
        return returnInt;
    }

    /**
     * Parse a string to a boolean value. If the string is null, return false.
     *
     * @param text text to parse into a boolean.
     * @return the boolean value if is true, otherwise return false
     */
    private boolean unpackBoolean(String text) {
        return Boolean.parseBoolean(text);
    }

    /**
     * Unpacks a <code>String</code> date into a <code>Date</code>
     *
     * @param sDate contains the date in <code>String</code> format
     * @return the date in <code>Date</code> format.
     * Returns null if the date string was null.
     */
    private Date unpackDate(String sDate) {
        Date returnDate = null;
        if (sDate != null) {
            try {
                returnDate = SIMPLE_DATE_FORMAT.parse(sDate);
            } catch (ParseException ex) {
                Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDate;
    }

    /**
     * Unpacks a <code>String</code> date and time into a <code>Date</code>
     *
     * @param sDateTime contains date and time
     * @return the date and time in <code>Date</code> format
     * Returns null if the date and time string was null.
     */
    private Date unpackDateTime(String sDateTime) {
        Date returnDateTime = null;
        if (sDateTime != null) {
            try {
                returnDateTime = SIMPLE_DATE_TIME_FORMAT.parse(sDateTime);
            } catch (ParseException ex) {
                Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnDateTime;
    }

    /*
     * ---------------------------------------------------------------------------------------
     *
     *                       C  O  M  M  O  N        M  E  T  H  O  D  S
     *
     * ---------------------------------------------------------------------------------------
     */
    /**
     * Finds an id element with a given root OID attribute value,
     * or <code>null</code> if not found.
     * <pre>
     * {@code
     * <id root=(oid) ...>
     * } </pre>
     *
     * @param subtree head of the subtree in which to search
     * @param oid root attribute value to search for
     * @return the element if found, otherwise null
     */
    private Element commonGetId(Element subtree, String oid) {
        NodeList idList = subtree.getElementsByTagName("id");
        for (int i = 0; i < idList.getLength(); i++) {
            Element id = (Element) idList.item(i);
            Node aRoot = id.getAttributeNode("root");
            if (aRoot != null && aRoot.getNodeValue().equals(oid)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Finds a livingSubjectId element matching a root attribute OID value.
     * <pre>
     * {@code
     * <livingSubjectId>
     *     <value root="(oid)" ...>
     *     <semanticsText>LivingSubject.xxx</semanticsText>
     * </livingSubjectId>
     * } </pre>
     *
     * @param subtree head of the subtree in which to search
     * @param oid root attribute value to search for
     * @return the element if found, otherwise null
     */
    private Element commonGetLivingSubjectId(Element subtree, String oid) {
        NodeList idList = subtree.getElementsByTagName("livingSubjectId");
        for (int i = 0; i < idList.getLength(); i++) {
            Element id = (Element) idList.item(i);
            Element eVal = (Element) id.getElementsByTagName("value").item(0);
            if (eVal != null) {
                Node aRoot = eVal.getAttributeNode("root");
                if (aRoot != null && aRoot.getNodeValue().equals(oid)) {
                    return id;
                }
            }
        }
        return null;
    }
}
