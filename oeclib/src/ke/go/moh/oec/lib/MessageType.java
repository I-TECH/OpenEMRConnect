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

/**
 * Class that records the properties of a message type. This can be used
 * to examine the properties of a message type for various purposes
 * during message processing.
 *
 * @author Jim Grace
 * @author Pchemutai
 * added getwork, workdone, reassignwork
 */
class MessageType {

    protected enum TemplateType {

        notifyPersonChanged,
        findPerson,
        findPersonResponse,
        createPerson,
        modifyPerson,
        logEntry,
        getWork,
        workDone,
        reassignWork
    }
    /** RequestTypeId of the request message, or 0 if this message is only used as a response. */
    private int requestTypeId;
    /** MessageType of any expected response to this message, or 0 if this message is only used as a response. */
    private MessageType responseMessageType;
    /** The type of message template we use */
    private TemplateType templateType;
    /** The root XML tag for this message template. For HL7 messages this is the HL7 V3 message type */
    private String rootXmlTag;
    /** Property name of the default destination address, or null if no default. */
    private String defaultDestinationAddressProperty;
    /** Name name of the default destination, or null if no default. */
    private String defaultDestinationName;
    /** Is this message to be queued for store-and-forward if it can't send immediately (true), or not (false) */
    private boolean toBeQueued;

    /**
     * Constructor to initialize all the fields of a message type.
     *
     * @param requestTypeId RequestTypeId of the request message, or 0 if this message is only used as a response
     * @param responseMessageType MessageType of any expected response to this message, or 0 if this message is only used as a response
     * @param templateType the type of XML message template used by this message
     * @param rootXmlTag The (HL7 V3) root XML tag that identifies this type of message when parsing
     * @param defaultDestinationProperty Property name for finding the default destination address and name
     * @param toBeQueued Is this message to be queued for store-and-forward if it can't send immediately (true), or not (false)
     */
    protected MessageType(int requestTypeId, MessageType responseMessageType, TemplateType templateType,
            String rootXmlTag, String defaultDestinationAddressProperty, String defaultDestinationName, boolean toBeQueued) {
        this.requestTypeId = requestTypeId;
        this.responseMessageType = responseMessageType;
        this.templateType = templateType;
        this.rootXmlTag = rootXmlTag;
        this.defaultDestinationAddressProperty = defaultDestinationAddressProperty;
        this.defaultDestinationName = defaultDestinationName;
        this.toBeQueued = toBeQueued;
    }

    public String getDefaultDestinationAddressProperty() {
        return defaultDestinationAddressProperty;
    }

    public String getDefaultDestinationName() {
        return defaultDestinationName;
    }

    public int getRequestTypeId() {
        return requestTypeId;
    }

    public MessageType getResponseMessageType() {
        return responseMessageType;
    }

    public String getRootXmlTag() {
        return rootXmlTag;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public boolean isToBeQueued() {
        return toBeQueued;
    }

    /*
     * Note that only the getter is defined for each field.
     * The MessageType fields are always set through the constructor.
     */
}
