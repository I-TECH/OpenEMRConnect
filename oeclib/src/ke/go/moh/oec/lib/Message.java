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
 * Description of a message to be sent, or that has been received.
 *
 * @author Jim Grace
 */
class Message {

    /** Type of the message - see {@link MessageTypeRegistry} */
    private MessageType messageType;
    /** User data contents of the message */
    private Object messageData;
    /**
     * Identifier of the message.
     * In the case of a new client request, this is a newly-generated message ID.
     * In the case of a server response to a request, this is the original
     * message ID from the request.
     */
    private String messageId;
    /** Address of the message source application */
    private String sourceAddress;
    /** Name of the message destination application */
    private String sourceName;
    /**
     * Address of the message destination application. In the case
     * of a server response to a request, this is address of the sender who
     * originally made the request.
     */
    private String destinationAddress;
    /**
     * Name of the message destination application. In the case
     * of a server response to a request, this is name of the sender who
     * originally made the request.
     */
    private String destinationName;
    /**
     * Information about the next hop where this message will be sent
     * including the IP address and port, and any other information
     * about how to send the message to that IP address and port.
     */
    private NextHop nextHop;
    /**
     * IP Address which sent this message to us.
     */
    private String sendingIpAddress;
    /**
     * Count of how many systems have sent this message so far.
     */
    private int hopCount;
    /**
     * Is this message to be queued for storing and forwarding?
     */
    private boolean toBeQueued;
    /**
     * The message in XML form.
     */
    private String xml;
    /**
     * XML string, compressed for network efficiency.
     */
    private byte[] compressedXml;
    /**
     * Length of the compressed XML string.
     */
    private int compressedXmlLength;
    /**
     * Is a response expected to this message?
     */
    private boolean responseExpected;
    /**
     * Count of received message segments (for tracing).
     */
    private int segmentCount;
    /**
     * Longest received message segment length (for tracing.)
     */
    private int longestSegmentLength;

    public byte[] getCompressedXml() {
        return compressedXml;
    }

    public void setCompressedXml(byte[] compressedXml) {
        this.compressedXml = compressedXml;
    }

    public int getCompressedXmlLength() {
        return compressedXmlLength;
    }

    public void setCompressedXmlLength(int compressedXmlLength) {
        this.compressedXmlLength = compressedXmlLength;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getLongestSegmentLength() {
        return longestSegmentLength;
    }

    public void setLongestSegmentLength(int longestSegmentLength) {
        this.longestSegmentLength = longestSegmentLength;
    }

    public Object getMessageData() {
        return messageData;
    }

    public void setMessageData(Object messageData) {
        this.messageData = messageData;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public NextHop getNextHop() {
        return nextHop;
    }

    public void setNextHop(NextHop nextHop) {
        this.nextHop = nextHop;
    }

    public boolean isResponseExpected() {
        return responseExpected;
    }

    public void setResponseExpected(boolean responseExpected) {
        this.responseExpected = responseExpected;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(int segmentCount) {
        this.segmentCount = segmentCount;
    }

    public String getSendingIpAddress() {
        return sendingIpAddress;
    }

    public void setSendingIpAddress(String sendingIpAddress) {
        this.sendingIpAddress = sendingIpAddress;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public boolean isToBeQueued() {
        return toBeQueued;
    }

    public void setToBeQueued(boolean toBeQueued) {
        this.toBeQueued = toBeQueued;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
