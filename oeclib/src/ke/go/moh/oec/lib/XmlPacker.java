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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import ke.go.moh.oec.Person.Sex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Date;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.Visit;
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
	protected String pack(Message m) {
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
	protected String packDocument(Document doc) {
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
	protected Document packMessage(Message m) {
		Document doc = null;
		switch (m.getMessageType().getTemplateType()) {
			case findPerson:
				doc = packFindPerson(m);
				break;

			case findPersonResponse:
				doc = packFindPersonResponse(m);
				break;

			case createPerson: // Uses packGenericPersonMessage(), below.
			case modifyPerson: // Uses packGenericPersonMessage(), below.
			case notifyPersonChanged:
				doc = packGenericPersonMessage(m);
				break;

			case logEntry:
				doc = packLogEntry(m);
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
		Person p = (Person) m.getData();
		packPerson(personNode, p);
		return doc;
	}

	/**
	 * Packs a Find Person message into a <code>Document</code>.
	 * Uses HL7 Patient Registry Find Candidates Query, PRPA_IN201305UV02.
	 *
	 * @param m search message contents to pack
	 * @return packed search message
	 */
	private Document packFindPerson(Message m) {
		Document doc = packTemplate(m);
		Element root = doc.getDocumentElement();
		packHl7Header(root, m);
		// Pack the message
		return doc;
	}

	/**
	 * Packs a Find Person Response message into a <code>Document</code>.
	 * Uses HL7 Patient Registry Find Candidates Query Response, PRPA_IN201306UV02.
	 *
	 * @param m search message contents to pack
	 * @return packed response message
	 */
	private Document packFindPersonResponse(Message m) {
		Document doc = packTemplate(m);
		Element root = doc.getDocumentElement();
		packHl7Header(root, m);
		// Pack the message
		return doc;
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
		packElementValue(receiver, "name", m.getDestinationName());
		Element sender = (Element) root.getElementsByTagName("sender").item(0);
		packId(sender, OID_APPLICATION_ADDRESS, m.getSourceAddress());
		packElementValue(sender, "name", m.getSourceName());
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
		packPersonName(e, p);
		packAttribute(e, "administrativeGenderCode", "code", packEnum(p.getSex()));
		packAttribute(e, "birthTime", "value", packDate(p.getBirthdate()));
		packAttribute(e, "deceasedTime", "value", packDate(p.getDeathdate()));
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
		packVisit(e, p.getLastRegularVisit(), OID_REGULAR_VISIT_ADDRESS, OID_REGULAR_VISIT_DATE);
		packVisit(e, p.getLastOneOffVisit(), OID_ONEOFF_VISIT_ADDRESS, OID_ONEOFF_VISIT_DATE);
		packPersonIdentifier(e, p, OID_PATIENT_REGISTRY_ID, PersonIdentifier.Type.patientRegistryId);
		packPersonIdentifier(e, p, OID_MASTER_PATIENT_REGISTRY_ID, PersonIdentifier.Type.masterPatientRegistryId);
		packPersonIdentifier(e, p, OID_CCC_UNIVERSAL_UNIQUE_ID, PersonIdentifier.Type.cccUniqueId);
		packPersonIdentifier(e, p, OID_CCC_LOCAL_PATIENT_ID, PersonIdentifier.Type.cccLocalId);
		packPersonIdentifier(e, p, KISUMU_HDSS_ID, PersonIdentifier.Type.kisumuHdssId);
		packFingerprint(e, p, OID_FINGERPRINT_LEFT_INDEX, Fingerprint.Type.leftIndexFinger);
		packFingerprint(e, p, OID_FINGERPRINT_LEFT_MIDDLE, Fingerprint.Type.leftMiddleFinger);
		packFingerprint(e, p, OID_FINGERPRINT_LEFT_RING, Fingerprint.Type.leftRingFinger);
		packFingerprint(e, p, OID_FINGERPRINT_RIGHT_INDEX, Fingerprint.Type.rightIndexFinger);
		packFingerprint(e, p, OID_FINGERPRINT_RIGHT_MIDDLE, Fingerprint.Type.rightMiddleFinger);
		packFingerprint(e, p, OID_FINGERPRINT_RIGHT_RING, Fingerprint.Type.rightRingFinger);
	}

	/**
	 * Packs a person's name. The first and middle names are packed in two
	 * consecutive &lt;given&gt; nodes. The last name is packed in the &lt;family&gt; node.
	 * If none of the names are present, remove the whole &lt;name&gt; tag.
	 * Otherwise, remove the tag for any part of the name that is not present.
	 * The exception to this is if the first name is absent and the middle name
	 * is present, keep the first &lt;given&gt; node, but pack it with an
	 * empty string. That way the middle name will still go in the second &lt;given&gt; tag.
	 * @param e head of the <code>Document</code> subtree in which this person is to be packed
	 * @param p person data to pack into the subtree
	 */
	private void packPersonName(Element e, Person p) {
		Element eName = (Element) e.getElementsByTagName("name").item(0);
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
			 */
			if (p.getFirstName() != null) {
				givenList.item(0).setNodeValue(p.getFirstName());
			} else if (p.getMiddleName() != null) {
				/*
				 * First name is null but middle name is not. Set first name to the
				 * empty string so middle name can still be the second <given> node.
				 */
				givenList.item(0).setNodeValue("");
			} else {
				packRemoveNode(givenList.item(0));
			}
			/*
			 * Pack the middle name.
			 */
			if (p.getMiddleName() != null) {
				givenList.item(1).setNodeValue(p.getMiddleName());
			} else {
				packRemoveNode(givenList.item(0));
			}
			/*
			 * Pack the last name.
			 */
			packElementValue(eName, "family", p.getLastName());
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
	 *
	 * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
	 * @param p person information containing the list of identifiers
	 * @param oidPersonIdentifier the XML template OID for this person identifier type
	 * @param type the person identifier type
	 */
	private void packPersonIdentifier(Element subtree, Person p, String oidPersonIdentifier, PersonIdentifier.Type type) {
		Element idElement = commonGetId(subtree, oidPersonIdentifier);
		boolean idTypeFound = false;
		if (p.getPersonIdentifierList() != null) {
			for (PersonIdentifier pi : p.getPersonIdentifierList()) {
				if (pi.getIdentifierType() == type && pi.getIdentifier() != null) {
					Element e = idElement;
					if (idTypeFound) {
						e = (Element) idElement.cloneNode(true);
						idElement.getParentNode().appendChild(e);
					}
					Node aExtension = e.getAttributeNode("extension");
					aExtension.setNodeValue(pi.getIdentifier());
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
	 * are inserted into clones of the template value. If there is no fingerprint of this type,
	 * the template for fingerprints of this type is removed.
	 * 
	 * @param subtree head of the <code>Document</code> subtree in which this person is to be packed
	 * @param p person information containing the list of identifiers
	 * @param oidFingerprint the XML template OID for this fingerprint type
	 * @param type the fingerprint type
	 */
	private void packFingerprint(Element subtree, Person p, String oidFingerprint, Fingerprint.Type type) {
		Element fpElement = commonGetId(subtree, oidFingerprint);
		boolean fpTypeFound = false;
		if (p.getFingerprintList() != null) {
			for (Fingerprint f : p.getFingerprintList()) {
				if (f.getFingerprintType() == type && f.getTemplate() != null) {
					Element e = fpElement;
					if (fpTypeFound) {
						e = (Element) fpElement.cloneNode(true);
						fpElement.getParentNode().appendChild(e);
					}
					Node aExtension = e.getAttributeNode("extension");
					aExtension.setNodeValue(packByteArray(f.getTemplate()));
					fpTypeFound = true;
				}
			}
		}
		if (!fpTypeFound) {
			packRemoveNode(fpElement);
		}
	}

	/**
	 * Packs data into an element attribute in the <code>Document</code>.
	 *
	 * @param subtree Document subtree in which to look for the element
	 * @param name name of the element in which to pack the value
	 * @param attribute name of the element attribute to receive the value
	 * @param value value to place in the attribute. If null, the element
	 * is removed from the template.
	 */
	private void packAttribute(Element subtree, String name, String attribute, String value) {
		Element e = (Element) subtree.getElementsByTagName(name).item(0);
		Node attr = e.getAttributeNode(attribute);
		if (value != null) {
			attr.setNodeValue(value);
		} else {
			packRemoveNode(e);
		}
	}

	/**
	 * Packs data into the value of an element.
	 *
	 * @param subtree Document subtree in which to look for the element
	 * @param name name of the element in which to pack the value
	 * @param value value to pack in the element. If null, the element
	 * is removed from the template.
	 */
	private void packElementValue(Element subtree, String name, String value) {
		Element e = (Element) subtree.getElementsByTagName(name).item(0);
		if (value != null) {
			e.setNodeValue(value);
		} else {
			packRemoveNode(e);
		}
	}

	/**
	 * Packs an element of the form &lt;id root="name", extension="value"&gt;.
	 * <p>
	 * If value is not null, inserts the value into the extension attribute
	 * of the matching &lt;id&gt; node. If the value is null, it removes
	 * the &lt;id&gt; node from the template.
	 *
	 * @param subtree head of subtree within which to look for the id element.
	 * @param name the root attribute value for the id element we are looking for.
	 * @param value the value to assign to the id tag, or null if the id tag should be removed.
	 */
	private void packId(Element subtree, String name, String value) {
		Element id = commonGetId(subtree, name);
		if (value != null) {
			Node aExtension = id.getAttributeNode("extension");
			aExtension.setNodeValue(value);
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
	 * 
	 * @param m message to load the template for
	 * @return the loaded template <code>Document</code>
	 */
	private Document packTemplate(Message m) {
		Document doc = null;
		String templateFileName = "messages/" + m.getMessageType().getTemplateType().name() + ".xml";
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = XmlPacker.class.getResourceAsStream(templateFileName);
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
	private Document packLogEntry(Message m) {
		LogEntry logEntry = (LogEntry) m.getData();
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
		packNewElement(doc, root, "sourceAddress", Mediator.getProperty("Instance.Name"));
		packNewElement(doc, root, "messageId", m.getMessageId());
		packNewElement(doc, root, "severity", logEntry.getSeverity());
		packNewElement(doc, root, "class", logEntry.getClassName());
		packNewElement(doc, root, "dateTime", packDateTime(logEntry.getDateTime()));
		packNewElement(doc, root, "message", logEntry.getMessage());
		return doc;
	}

	/**
	 * Packs a value into a new <code>Element</code>, and links it to a parent <code>Element</code>.
	 * 
	 * @param doc the document we are packing into
	 * @param parent parent element for our new element
	 * @param elementName name of the new element to create
	 * @param value value of the new element to create
	 */
	private void packNewElement(Document doc, Element parent, String elementName, String value) {
		Element element = doc.createElement(elementName);
		element.setNodeValue(value);
		parent.appendChild(element);
	}

	/**
	 * Packs a binary array of bytes into a hexidecimal-encoded string.
	 * If the byte array is <code>null</code>, returns <code>null</code>.
	 *
	 * @param byteArray binary array of bytes to pack
	 * @return the array of bytes encoded as a hexidecimal string
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
	 * Packs a <code>Date</code> (not including time) into a string.
	 * If the date is <code>null</code>, returns <code>null</code>.
	 *
	 * @param date the date value to be packed
	 * @return <code>String</code> containing the packed date.
	 */
	private String packDate(Date date) {
		if (date != null) {
			return SIMPLE_DATE_FORMAT.format(date);
		} else {
			return null;
		}
	}

	/**
	 * Packs a <code>Date</code> (including time) into a string.
	 *
	 * @param dateTime the date and time value to be packed
	 * @return <code>String</code> containing the packed date and time.
	 */
	private String packDateTime(Date dateTime) {
		if (dateTime != null) {
			return SIMPLE_DATE_TIME_FORMAT.format(dateTime);
		} else {
			return null;
		}
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
	 * @param xml String containing XML request message
	 * @return unpacked message data
	 */
	protected Message unpack(String xml) {
		Document doc = unpackXml(xml);
		Message m = unpackDocument(doc);
		return m;
	}

	/**
	 * Unpacks a XML string into DOM Document structure
	 *
	 * @param xml String containing XML request message
	 * @return the DOM Document structure
	 */
	protected Document unpackXml(String xml) {
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
	 * @param doc the DOM Document structure to decode
	 * @return unpacked message data
	 */
	protected Message unpackDocument(Document doc) {
		Message m = new Message();
		Element root = doc.getDocumentElement();
		String rootName = root.getTagName();
		MessageType messageType = MessageTypeRegistry.find(rootName);
		m.setMessageType(messageType);

		switch (messageType.getTemplateType()) {
			case findPerson:
				unpackFindPerson(m, root);
				break;

			case findPersonResponse:
				unpackFindPersonResponse(m, root);
				break;

			case createPerson: // Uses unpackGenericPersonMessage(), below.
			case modifyPerson: // Uses unpackGenericPersonMessage(), below.
			case notifyPersonChanged:
				unpackGenericPersonMessage(m, root);
				break;

			case logEntry:
				unpackLogEntry(m, root);
		}
		return m;
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
	 * NOTIFY PERSON CHANGED
	 *
	 * @param m the message contents to fill in
	 * @param e root node of the person message <code>Document</code> parsed from XML
	 */
	private void unpackGenericPersonMessage(Message m, Element e) {
		unpackGenericPersonMessage(m, e);
		Element ePerson = (Element) e.getElementsByTagName("patient").item(0);
		unpackPerson(m, ePerson);
		// TO DO: Finish the work
	}

	/**
	 * Unpacks a find person request <code>Document</code> into message data.
	 * Uses HL7 Patient Registry Find Candidates Query, PRPA_IN201305UV02.
	 *
	 * @param m the message contents to fill in
	 * @param e root of the person message <code>Document</code> parsed from XML
	 */
	private void unpackFindPerson(Message m, Element e) {
		// TO DO: Do the work
	}

	/**
	 * Unpacks a find person response <code>Document</code> into message data.
	 * Uses HL7 Patient Registry Find Candidates Query Response, PRPA_IN201306UV02.
	 *
	 * @param m the message contents to fill in
	 * @param e root of the person message <code>Document</code> parsed from XML
	 */
	private void unpackFindPersonResponse(Message m, Element e) {
		// TO DO: Do the work
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
			m.setDestinationName(unpackElementValue(receiver, "name"));
		}
		Element sender = (Element) e.getElementsByTagName("sender").item(0);
		if (sender != null) {
			m.setSourceAddress(unpackId(sender, OID_APPLICATION_ADDRESS));
			m.setSourceName(unpackElementValue(sender, "name"));
		}
	}

	/**
	 * Unpacks a person subtree into message data
	 *
	 * @param m the message contents to fill in
	 * @param e head of the <code>Document</code> subtree in which this person is found
	 */
	private void unpackPerson(Message m, Element e) {
		Person p = new Person();
		m.setData(p);
		unpackPersonName(p, e);
		p.setSex((Sex) unpackEnum(Person.Sex.values(), unpackAttribute(e, "administrativeGenderCode", "code")));
		p.setBirthdate(unpackDate(unpackAttribute(e, "birthTime", "value")));
		p.setDeathdate(unpackDate(unpackAttribute(e, "deceasedTime", "value")));
		p.setOtherName(unpackId(e, OID_OTHER_NAME));
		p.setClanName(unpackId(e, OID_CLAN_NAME));

		// TO DO: finish unpacking the person object
	}

	/**
	 * Unpacks a person name into a <code>Person</code> object.
	 *
	 * @param p the person data into which to put the person name.
	 * @param e head of the <code>Document</code> subtree in which this person is found
	 */
	private void unpackPersonName(Person p, Element e) {
		Element eName = (Element) e.getElementsByTagName("name").item(0);
		if (eName != null) {
			NodeList givenList = eName.getElementsByTagName("given");
			if (givenList.getLength() > 0) {
				p.setFirstName(givenList.item(0).getNodeValue());
				if (givenList.getLength() > 1) {
					p.setMiddleName(givenList.item(1).getNodeValue());
				}
			}
			p.setLastName(unpackElementValue(eName, "family"));
		}
	}

	/**
	 * Unpacks data from an element attribute in the <code>Document</code>.
	 *
	 * @param subtree Document subtree in which to look for the element
	 * @param name name of the element from which to unpack the value
	 * @param attribute name of the element attribute containing the value
	 * @return the attribute value. If the element was not found, returns null.
	 */
	private String unpackAttribute(Element subtree, String name, String attribute) {
		Element e = (Element) subtree.getElementsByTagName(name).item(0);
		if (e != null) {
			Node attr = e.getAttributeNode(attribute);
			if (attr != null) {
				return attr.getNodeValue();
			}
		}
		return null;
	}

	/**
	 * Unpacks data from an element value.
	 *
	 * @param subtree Document subtree in which to look for the element
	 * @param name name of the element from which to unpack the value
	 * @return value value of the element. If the element was not found,
	 * returns null.
	 */
	private String unpackElementValue(Element subtree, String name) {
		Element e = (Element) subtree.getElementsByTagName(name).item(0);
		if (e != null) {
			return e.getNodeValue();
		} else {
			return null;
		}
	}

	/**
	 * Unpacks an element of the form &lt;id root="name", extension="value"&gt;.
	 * <p>
	 * If the element is found, returns the value of the extension attribute
	 * of the matching &lt;id&gt; node. If the element is not found, returns null
	 *
	 * @param subtree head of subtree within which to look for the id element.
	 * @param name the root attribute value for the id element we are looking for.
	 * @return value of the extension attribute, or null if tag not found.
	 */
	private String unpackId(Element subtree, String name) {
		Element id = commonGetId(subtree, name);
		if (id != null) {
			Node aExtension = id.getAttributeNode("extension");
			if (aExtension != null) {
				return aExtension.getNodeValue();
			}
		}
		return null;
	}

	/**
	 * Unpacks a Log Entry <code>Document</code> into message data.
	 * Uses LogEntry message type.
	 *
	 * @param doc the log entry <code>Document</code> parsed from XML
	 * @return the log entry message data
	 */
	private void unpackLogEntry(Message m, Element root) {
		LogEntry logEntry = new LogEntry();
		m.setData(logEntry);
		logEntry.setSeverity(root.getElementsByTagName("severity").item(0).getNodeValue());
		logEntry.setClassName(root.getElementsByTagName("class").item(0).getNodeValue());
		logEntry.setDateTime(unpackDateTime(root.getElementsByTagName("dateTime").item(0).getNodeValue()));
		logEntry.setMessage(root.getElementsByTagName("message").item(0).getNodeValue());
		logEntry.setInstance(root.getElementsByTagName("instance").item(0).getNodeValue());
	}

	/**
	 * Unpacks a hexidecimal-encoded string into a binary byte array.
	 *
	 * @param hex the hexidecimal string to unpack
	 * @return the resulting binary byte array
	 */
	private byte[] unpackByteArray(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
			bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
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
	 * Unpacks a <code>String</code> date into a <code>Date</code>
	 *
	 * @param sDate contains the date in <code>String</code> format
	 * @return the date in <code>Date</code> format
	 */
	private Date unpackDate(String sDate) {
		Date date = null;
		try {
			date = SIMPLE_DATE_FORMAT.parse(sDate);
		} catch (ParseException ex) {
			Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
		}
		return date;
	}

	/**
	 * Unpacks a <code>String</code> date and time into a <code>Date</code>
	 *
	 * @param sDateTime contains date and time
	 * @return the date and time in <code>Date</code> format
	 */
	private Date unpackDateTime(String sDateTime) {
		Date dateTime = null;
		try {
			dateTime = SIMPLE_DATE_TIME_FORMAT.parse(sDateTime);
		} catch (ParseException ex) {
			Logger.getLogger(XmlPacker.class.getName()).log(Level.SEVERE, null, ex);
		}
		return dateTime;
	}

	/*
	 * ---------------------------------------------------------------------------------------
	 *
	 *                       C  O  M  M  O  N        M  E  T  H  O  D  S
	 *
	 * ---------------------------------------------------------------------------------------
	 */
	/**
	 * Finds an &lt;id&gt; element with a given "root" attribute value,
	 * or <code>null</code> if not found.
	 *
	 * @param subtree head of the subtree in which to search
	 * @param name root attribute value to search for
	 * @return the element if found, otherwise null
	 */
	private Element commonGetId(Element subtree, String name) {
		NodeList idList = subtree.getElementsByTagName("id");
		for (int i = 0; i < idList.getLength(); i++) {
			Element id = (Element) idList.item(i);
			Node aRoot = id.getAttributeNode("root");
			if (aRoot != null && aRoot.getNodeValue().equals(name)) {
				return id;
			}
		}
		return null;
	}
}
