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

import ke.go.moh.oec.RequestTypeId;
import java.util.Arrays;
import java.util.List;

/**
 * Record and retrieve the message types that we can process. This class
 * maintains a list of the message types that we know about and their
 * properties. The list can be searched by request type or by the root
 * XML tag of a message.
 * 
 * @author Jim Grace
 */
class MessageTypeRegistry {
    /*
    protected MessageType(int requestTypeId, MessageType responseMessageType, boolean unsolicited,
    String rootXmlTag, String defaultDestination, boolean toBeQueued) {
     */

    /**
     * Find Person response
     */
    protected static final MessageType findPersonResponse = new MessageType(
            0, // No request type (this message is only a response).
            null, // No response type (this mssage does not have a response to answer it).
            "PRPA_IN201306UV02", //HL7 Patient Registry Find Candidates Query Response
            null, // No default destination
            false); // Don't store and forward if it doesn't go immediately
    /**
     * Find Person (MPI) request
     */
    protected static final MessageType findPersonMpi = new MessageType(
            RequestTypeId.FIND_PERSON_MPI,
            findPersonResponse,
            "PRPA_IN201305UV02", //HL7 Patient Registry Find Candidates Query
            "ke.go.moh.mpi", // The Master Person List
            false); // Don't store and forward if it doesn't go immediately
    /**
     * Find Person (LPI) request
     */
    protected static final MessageType findPersonLpi = new MessageType(
            RequestTypeId.FIND_PERSON_LPI,
            findPersonResponse,
            "PRPA_IN201305UV02", //HL7 Patient Registry Find Candidates Query
            ".lpi", // The Local Person List (relative to where we are now.)
            false); // Don't store and forward if it doesn't go immediately
    /**
     * Create Person (MPI) request
     */
    protected static final MessageType createPersonMpi = new MessageType(
            RequestTypeId.CREATE_PERSON_MPI,
            null, // No response to this message.
            "PRPA_IN201311UV02", //HL7 Patient Registry Add Request
            "ke.go.moh.mpi", // The Local Person List (relative to where we are now.)
            true); // Store and forward if it doesn't send immediately
    /**
     * Find Person (LPI) request
     */
    protected static final MessageType createPersonLpi = new MessageType(
            RequestTypeId.CREATE_PERSON_LPI,
            null, // No response to this message.
            "PRPA_IN201311UV02", //HL7 Patient Registry Add Request
            ".lpi", // The Local Person List (relative to where we are now.)
            true); // Store and forward if it doesn't send immediately
    /**
     * Modify Person (MPI) request
     */
    protected static final MessageType modifyPersonMpi = new MessageType(
            RequestTypeId.MODIFY_PERSON_MPI,
            null, // No response to this message.
            "PRPA_IN201314UV02", //HL7 Patient Registry Revise Request
            "ke.go.moh.mpi", // The Local Person List (relative to where we are now.)
            true); // Store and forward if it doesn't send immediately
    /**
     * Modify Person (LPI) request
     */
    protected static final MessageType modifyPersonLpi = new MessageType(
            RequestTypeId.MODIFY_PERSON_LPI,
            null, // No response to this message.
            "PRPA_IN201314UV02", //HL7 Patient Registry Revise Request
            ".lpi", // The Local Person List (relative to where we are now.)
            true); // Store and forward if it doesn't send immediately
    /**
     * Set Clinical Document
     */
    protected static final MessageType setClinicalDocument = new MessageType(
            RequestTypeId.SET_CLINICAL_DOCUMENT,
            null, // No response to this message.
            "ClinicalDocument", //HL7 Patient Registry Revise Request
            null, // No default destination
            true); // Store and forward if it doesn't send immediately
    /**
     * Get Clinical Document (from HDSS)
     */
    protected static final MessageType getClinicalDocumentHdss = new MessageType(
            RequestTypeId.GET_CLINICAL_DOCUMENT_HDSS,
            setClinicalDocument, // Respond with setClinicalDocument message
            "(tbd)", // XML root tag To Be Defined
            "org.kemri.kisumu.hdss", // The HDSS companion
            true); // Store and forward if it doesn't send immediately
    /**
     * Get Clinical Document (from Clinical Document Store)
     */
    protected static final MessageType getClinicalDocumentCds = new MessageType(
            RequestTypeId.GET_CLINICAL_DOCUMENT_HDSS,
            setClinicalDocument, // Respond with setClinicalDocument message
            "(tbd)", // XML root tag To Be Defined
            ".cds", // The HDSS companion
            true); // Store and forward if it doesn't send immediately
    /**
     * Send Log Entry
     */
    protected static final MessageType sendLogEntry = new MessageType(
            RequestTypeId.SEND_LOG_ENTRY,
            null, // No response to this message.
            "(tbd)", // XML root tag To Be Defined
            ".cds", // The HDSS companion
            true); // Store and forward if it doesn't send immediately
    /**
     * List of all OpenEMRConnect message types. This list can be searched
     * by either RequestTypeId (for requests only) or xmlRootTag (for any message).
     */
    private static final List<MessageType> messageTypeList = Arrays.asList(
            findPersonResponse,
            findPersonMpi,
            findPersonLpi,
            createPersonMpi,
            createPersonLpi,
            modifyPersonMpi,
            modifyPersonLpi,
            setClinicalDocument,
            getClinicalDocumentHdss,
            getClinicalDocumentCds,
            sendLogEntry);

    /**
     * Finds a message type based on the request type ID (integer).
     *
     * @param requestType request type to search for
     * @return <code>MessageType</code>, or <code>null</code> if not found.
     */
    protected MessageType find(int requestTypeId) {
        for (int i = 0; i < messageTypeList.size(); i++) {
            if (messageTypeList.get(i).getRequestTypeId() == requestTypeId) {
                return messageTypeList.get(i);
            }
        }
        return null;
    }

    /**
     * Finds a message type based on the root XML tag in the message.
     * <p>
     * Note that sometimes more than one message type has the same root XML tag.
     * In this case, the first such message type will be returned. This should
     * be good enough for our purposes.
     *
     * @param xmlTag message root XML tag to search for
     * @return <code>MessageType</code>, or <code>null</code> if not found.
     */
    protected MessageType find(String rootXmlTag) {
        for (int i = 0; i < messageTypeList.size(); i++) {
            if (messageTypeList.get(i).getRootXmlTag().equals(rootXmlTag)) {
                return messageTypeList.get(i);
            }
        }
        return null;
    }
}
