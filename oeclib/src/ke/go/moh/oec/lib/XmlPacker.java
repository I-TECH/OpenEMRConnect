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

import org.w3c.dom.Document;

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
    protected Document makeDocument(MessageType messageType, Object requestData, String messageID) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Packs a DOM Document structure into an XML string
     * @param doc the DOM Document structure to pack
     * @return the packed XML string
     */
    protected String packDocument(Document doc) {
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
        throw new UnsupportedOperationException();
    }

    /**
     * Decodes a DOM Document structure into message data
     *
     * @param doc the DOM Document structure to decode
     * @return unpacked message data
     */
    protected UnpackedMessage decodeDocument(Document doc) {
        throw new UnsupportedOperationException();
    }
}
