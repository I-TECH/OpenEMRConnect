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

    /** NOTIFY PERSON CHANGED uses HL7 Patient Registry Record Revised, PRPA_IN201302UV02 */
    static final String NOTIFY_PERSON_CHANGED_ROOT_TAG = "PRPA_IN201302UV02";
    /** FIND PERSON uses HL7 Patient Registry Find Candidates Query, PRPA_IN201305UV02 */
    static final String FIND_PERSON_ROOT_TAG = "PRPA_IN201305UV02";
    /** FIND PERSON RESPONSE uses HL7 Patient Registry Find Candidates Query Response, PRPA_IN201306UV02  */
    static final String FIND_PERSON_RESPONSE_ROOT_TAG = "PRPA_IN201306UV02";
    /** CREATE PERSON uses HL7 Patient Registry Add Request, PRPA_IN201311UV02 */
    static final String CREATE_PERSON_ROOT_TAG = "PRPA_IN201311UV02";
    /** CREATE PERSON ACCEPTED uses HL7 Patient Registry Add Request Accepted message, PRPA_IN201312UV02 */
    static final String CREATE_PERSON_ACCEPTED_ROOT_TAG = "PRPA_IN201312UV02";
    /** MODIFY PERSON uses HL7 Patient Registry Revise Request, PRPA_IN201314UV02 */
    static final String MODIFY_PERSON_ROOT_TAG = "PRPA_IN201314UV02";
    /** MODIFY PERSON ACCEPTED uses HL7 Patient Registry Revise Request Accepted message, PRPA_IN201315UV02 */
    static final String MODIFY_PERSON_ACCEPTED_ROOT_TAG = "PRPA_IN201315UV02";
    /** LOG ENTRY uses LogEntry for a root tag */
    static final String LOG_ENTRY_ROOT_TAG = "LogEntry";
    /** GET WORK  uses getWork for a root tag*/
    static final String GET_WORK_ROOT_TAG = "GetWork";
    /**WORK DONE uses Work Done for a root tag*/
    static final String WORK_DONE_ROOT_TAG = "WorkDone";
    /**Reassign uses Reassign for a root tag*/
    static final String REASSIGN_ROOT_TAG = "Reassign";
    /**
     * Find Person response
     */
    static final MessageType findPersonResponse = new MessageType(
            0, // No request type (this message is only a response).
            null, // No response type (this mssage does not have a response to answer it).
            MessageType.TemplateType.findPersonResponse,
            FIND_PERSON_RESPONSE_ROOT_TAG, //HL7 Patient Registry Find Candidates Query Response
            null, // No default destination address property
            null, // No default destination name
            false); // Don't store and forward if it doesn't go immediately
    /**
     * Find Person (MPI) request
     */
    static final MessageType findPersonMpi = new MessageType(
            RequestTypeId.FIND_PERSON_MPI,
            findPersonResponse,
            MessageType.TemplateType.findPerson,
            FIND_PERSON_ROOT_TAG, //HL7 Patient Registry Find Candidates Query
            "MPI.Address", // The Master Person List address property
            "Master Person Index", // The Master Person List
            false); // Don't store and forward if it doesn't go immediately
    /**
     * Find Person (LPI) request
     */
    static final MessageType findPersonLpi = new MessageType(
            RequestTypeId.FIND_PERSON_LPI,
            findPersonResponse,
            MessageType.TemplateType.findPerson,
            FIND_PERSON_ROOT_TAG, //HL7 Patient Registry Find Candidates Query
            "LPI.Address", // The Local Person List address property
            "Local Person Index", // The Local Person List
            false); // Don't store and forward if it doesn't go immediately
    /**
     * Create Person Accepted response
     */
    static final MessageType createPersonAccepted = new MessageType(
            0, // No request type (this message is only a response).
            null, // No response type (this mssage does not have a response to answer it).
            MessageType.TemplateType.createPersonAccepted,
            CREATE_PERSON_ACCEPTED_ROOT_TAG, //HL7 Patient Registry Add Request
            null, // No default destination address property
            null, // No default destination name
            true); // Store and forward if it doesn't send immediately
    /**
     * Create Person (MPI) request
     */
    static final MessageType createPersonMpi = new MessageType(
            RequestTypeId.CREATE_PERSON_MPI,
            createPersonAccepted, // Response to this message -- if sender requests it.
            MessageType.TemplateType.createPerson,
            CREATE_PERSON_ROOT_TAG, //HL7 Patient Registry Add Request
            "MPI.Address", // The Master Person List address property
            "Master Person Index", // The Master Person List
            true); // Store and forward if it doesn't send immediately
    /**
     * Create Person (LPI) request
     */
    static final MessageType createPersonLpi = new MessageType(
            RequestTypeId.CREATE_PERSON_LPI,
            createPersonAccepted, // Response to this message -- if sender requests it.
            MessageType.TemplateType.createPerson,
            CREATE_PERSON_ROOT_TAG, //HL7 Patient Registry Add Request
            "LPI.Address", // The Local Person List address property
            "Local Person Index", // The Local Person List
            true); // Store and forward if it doesn't send immediately
    /**
     * Modify Person Accepted response
     */
    static final MessageType modifyPersonAccepted = new MessageType(
            0, // No request type (this message is only a response).
            null, // No response type (this mssage does not have a response to answer it).
            MessageType.TemplateType.modifyPersonAccepted,
            MODIFY_PERSON_ACCEPTED_ROOT_TAG, //HL7 Patient Registry Add Request
            null, // No default destination address property
            null, // No default destination name
            true); // Store and forward if it doesn't send immediately
    /**
     * Modify Person (MPI) request
     */
    static final MessageType modifyPersonMpi = new MessageType(
            RequestTypeId.MODIFY_PERSON_MPI,
            modifyPersonAccepted, // Response to this message -- if sender requests it.
            MessageType.TemplateType.modifyPerson,
            MODIFY_PERSON_ROOT_TAG, //HL7 Patient Registry Revise Request
            "MPI.Address", // The Master Person List address property
            "Master Person Index", // The Master Person List
            true); // Store and forward if it doesn't send immediately
    /**
     * Modify Person (LPI) request
     */
    static final MessageType modifyPersonLpi = new MessageType(
            RequestTypeId.MODIFY_PERSON_LPI,
            modifyPersonAccepted, // Response to this message -- if sender requests it.
            MessageType.TemplateType.modifyPerson,
            MODIFY_PERSON_ROOT_TAG, //HL7 Patient Registry Revise Request
            "LPI.Address", // The Local Person List address property
            "Local Person Index", // The Local Person List
            true); // Store and forward if it doesn't send immediately
    /**
     * Set Clinical Document
     */
    static final MessageType notifyPersonChanged = new MessageType(
            RequestTypeId.NOTIFY_PERSON_CHANGED,
            null, // No response to this message.
            MessageType.TemplateType.notifyPersonChanged,
            NOTIFY_PERSON_CHANGED_ROOT_TAG, //HL7 Patient Registry Record Revised
            "MPI.Address", // The Master Person List address property
            "Master Person Index", // The Master Person List
            true); // Store and forward if it doesn't send immediately
    /**
     * Find person (to get household members) from HDSS
     */
    static final MessageType findPersonHdss = new MessageType(
            RequestTypeId.FIND_PERSON_HDSS,
            findPersonResponse,
            MessageType.TemplateType.findPerson,
            FIND_PERSON_ROOT_TAG, //HL7 Patient Registry Find Candidates Query
            "HDSS.Address", // The HDSS companion address property
            "HDSS Companion", // The HDSS companion
            true); // Store and forward if it doesn't send immediately
    /**
     * Send Log Entry
     */
    static final MessageType logEntry = new MessageType(
            RequestTypeId.LOG_ENTRY,
            null, // No response to this message.
            MessageType.TemplateType.logEntry,
            LOG_ENTRY_ROOT_TAG, // XML root tag To Be Defined
            "LoggingServer.Address", // The Logging Server address property
            "Logging Server", // The Logging Server
            true); // Store and forward if it doesn't send immediately
    /**
     * Send get work
     */
    static final MessageType getWork = new MessageType(
            RequestTypeId.GET_WORK,
            null, // No response to this message.
            MessageType.TemplateType.getWork,
            GET_WORK_ROOT_TAG, // XML root tag To Be Defined
            "CDS.Address", // The Clinical Document Store address property
            "Clinical Document Store", // The Clinical Document Store
            true); // Store and forward if it doesn't send immediately
    /**
     * Send WorkDone
     */
    static final MessageType WorkDone = new MessageType(
            RequestTypeId.WORK_DONE,
            null, // No response to this message.
            MessageType.TemplateType.workDone,
            WORK_DONE_ROOT_TAG, // XML root tag To Be Defined
            "CDS.Address", // The Clinical Document Store address property
            "Clinical Document Store", // The Clinical Document Store
            true); // Store and forward if it doesn't send immediately
    /**
     * Send Reassign
     */
    static final MessageType ReassignWork = new MessageType(
            RequestTypeId.REASSIGN_WORK,
            null, // No response to this message.
            MessageType.TemplateType.reassignWork,
            REASSIGN_ROOT_TAG, // XML root tag To Be Defined
            "CDS.Address", // The Clinical Document Store address property
            "Clinical Document Store", // The Clinical Document Store
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
            createPersonAccepted,
            modifyPersonMpi,
            modifyPersonLpi,
            modifyPersonAccepted,
            notifyPersonChanged,
            findPersonHdss,
            logEntry,
            getWork,
            WorkDone,
            ReassignWork);

    /**
     * Finds a message type based on the request type ID (integer).
     *
     * @param requestTypeId request type to search for
     * @return <code>MessageType</code>, or <code>null</code> if not found.
     */
    static MessageType find(int requestTypeId) {
        for (MessageType m : messageTypeList) {
            if (m.getRequestTypeId() == requestTypeId) {
                return m;
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
     * @param rootXmlTag message root XML tag to search for
     * @return <code>MessageType</code>, or <code>null</code> if not found.
     */
    static MessageType find(String rootXmlTag) {
        for (MessageType m : messageTypeList) {
            if (m.getRootXmlTag().equals(rootXmlTag)) {
                return m;
            }
        }
        return null;
    }
}
