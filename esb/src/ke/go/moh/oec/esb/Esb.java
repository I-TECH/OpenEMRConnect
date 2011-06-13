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
package ke.go.moh.oec.esb;

import ke.go.moh.oec.lib.Mediator;

/**
 * Enterprise Service Bus - Forwards OpenEMRConnect messages on to
 * (or towards) their destination.
 * <p>
 * This module may be deployed at any number of nodes in the network
 * where messages need to be forwarded. For example, messages may sent
 * over more than one local area network (LAN) segment. There may be
 * a system that is connected to two different LAN segments, and the
 * messages need to pass on from one LAN segment to the other.
 * The ESB may be deployed on the machine that is connected to both
 * LAN segments. If properly configured, it will forward messages from
 * one LAN segment onto the other.
 * <p>
 * The ESB may also be deployed as a central dispatching system on a
 * single LAN segment. That way all the end systems only need to exchange
 * messages with the ESB, and the ESB knows where to route the messages
 * to the other systems.
 * <p>
 * Consider the following case where both of these functions are combined.
 * There are three LAN segments: <br>
 * 1. Local Site LAN (LSL) - within a healthcare site. <br>
 * 2. Bridging LAN (BL) - Between the healthcare site and a central site. <br>
 * 3. Central Site LAN (CSL) - Within a central site providing OpenEMRConnect services.
 * <p>
 * This module may be deployed on the machine connecting the LSL to the BL,
 * accessing both the Local Site LAN and the Bridging LAN.
 * In this location, the module may be known as the Facility Service Bus (FSB).
 * The FSB acts as the main message-routing node for the Loacl Site LAN.
 * OpenEMRConnect systems on the LSL may send all outgoing messages
 * to the FSB for routing to their destination. If the destination is also on
 * the LSL, the FSB forwards the message back on the LSL to the destination.
 * If the destination is on the Central Site LAN, the FSB forward the message
 * on to the Bridging LAN so it can reach its destination. Any messages coming
 * from the CSL and destined for a system on the LSL will first come to the
 * FSB. The FSB then routes the message onto the LSL so it reaches its ultimate
 * destination.
 * <p>
 * This module may also be deployed on the machine connecting the Bridging LAN
 * to the Central Site LAN. Here it is simply known as the Enterprise Service Bus (ESB).
 * Messages coming from the BL to the ESB are forwarded to their ultimate destination
 * on the CSL. Messages coming from the CSL that are destined to a remote site
 * can be forwarded on the BL to the FSB at the local site. Finally, the ESB can
 * also act as a central message hub for the CSL. Messages going between two
 * different systems on the CSL may be sent first to the FSB for routing to
 * the other OpenEMRConnect system on the CSL.
 * <p>
 * The message forwarding code is in the OEC Library (oeclib). The configuration
 * of message forwarding addresses is handled in the properties file openemrconnect.properties.
 * Therefore all this module has to do is to initialize the oeclib, and then
 * just stay running, while the oeclib handles all the message forwarding functions.
 * @author Jim Grace
 */
public class Esb {

    /**
     * Allocate an instance of the OEC Library,
     * and then just let the library run and take care of things.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Mediator mediator = new Mediator();
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
            }
        }
    }
}
