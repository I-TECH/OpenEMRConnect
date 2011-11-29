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

import java.util.logging.Level;

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
     * (Listening) port number from system which sent this message to us.
     */
    private int sendingPort;
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

    public int getSendingPort() {
        return sendingPort;
    }

    public void setSendingPort(int sendingPort) {
        this.sendingPort = sendingPort;
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

    /**
     * Summarizes the message in question. It will be decompressed if necessary
     * to show the contents.
     * 
     * @return summary of the message.
     */
    public String summarize() {
        return summarize(true);
    }
    
    /**
     * Summarizes the message in question. Returns the message type if known,
     * otherwise returns the root tag of the XML message. Also returns information
     * such as from and to addresses, if known, and hop count and queuing status.
     * If the logging level is FINER or greater, also returns the message itself.
     * <p>
     * Note that this routine uncompresses the message if necessary. It should
     * only be called if the caller knows that the result will be used.
     * For example, if the result will be used for logging a message at
     * level FINE, the caller should test to be sure that we are logging
     * level FINE messages before calling this method.
     * 
     * @param decompressIfNeeded if true, the message will be decompressed
     * if needed to display the contents. This should be false to report
     * errors from the decompress method, so the method does not call itself.
     * 
     * @return summary of the message.
     */
    public String summarize(boolean decompressIfNeeded) {
        String summary = "[can't decode message type]";
        if (messageType != null) { // If message originaed here, we know its type.
            summary = messageType.getTemplateType().name(); // Use type as message label.
        } else {
            if (xml == null && decompressIfNeeded) {
                Compresser.decompress(this);
            }
            if (xml != null) {
                int line2 = xml.indexOf('\n') + 1;
                if (line2 > 0) {
                    int endTag = xml.indexOf('>', line2);
                    int space = xml.indexOf(' ', line2);
                    if (space > 0 && space < endTag) {
                        endTag = space; // Strip off any root tag attributes...
                    }
                    if (endTag > 0) {
                        summary = xml.substring(line2, endTag) + " ...";
                    }
                }
            }
        }
        if (sendingIpAddress != null) {
            summary += " from " + sendingIpAddress;
            if (sendingPort != 0) {
                summary += ":" + sendingPort;
            }
        }
        if (destinationAddress != null) {
            summary += " to " + destinationAddress;
        }
        if (nextHop != null) {
            summary += " via " + nextHop.getIpAddressPort();
        }
        summary += " toBeQueued=" + toBeQueued
                + " hopCount=" + hopCount
                + " length=" + compressedXmlLength;
        if (segmentCount != 0) {
            summary += " segments=" + segmentCount;
        }
        if (longestSegmentLength != 0) {
            summary += ", longest=" + longestSegmentLength;
        }
        if (Mediator.testLoggerLevel(Level.FINER)) {
            if (xml == null && decompressIfNeeded) {
                Compresser.decompress(this);
            }
            if (xml != null) {
                summary += "\n" + xml; // Include the whole message.
            }
        }
        return summary;
    }
}
