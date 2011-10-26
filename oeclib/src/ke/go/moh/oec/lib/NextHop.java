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

import java.util.HashMap;
import java.util.Map;

/**
 * Information about the next hop to which we will send a message.
 * Information includes the IP Address and port of the next hop,
 * the maximum packet size allowed (if any maximum), and
 * whether or not to zip compress the packet before transmission.
 * 
 * @author Jim Grace
 */
public class NextHop {

    private String ipAddressPort;
    private int maxSize;
    private boolean zip;
    static private Map<String, NextHop> nextHopCache = new HashMap<String, NextHop>();

    public String getIpAddressPort() {
        return ipAddressPort;
    }

    public void setIpAddressPort(String ipAddressPort) {
        this.ipAddressPort = ipAddressPort;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(boolean zip) {
        this.zip = zip;
    }

    /**
     * Gets next hop information for a destination
     * <p>
     * The next hop information corresponding to a destination address is found
     * in properties for this application starting with "IPAddressPort."
     * If the full address is not found, then we look for successively
     * higher levels in the address name, where levels are separated by
     * dots. Finally we look for the catch-all entry "IPAddressPort.*"
     * to which we forward any otherwise-unresolved address.
     * <p>
     * For example, if the address to find is "aa.bb.cc", we will look
     * for the following properties in this order until we find a value:
     * <p>
     * IpAddressPort.aa.bb.cc   <br>
     * IpAddressPort.aa.bb      <br>
     * IpAddressPort.aa         <br>
     * IpAddressPort.*          <br>
     *
     * @param destination where the message is to be sent
     * @return IP address:port to which to forward the message.
     * Returns <code>null</code> if the destination is ourselves,
     * or the destination address cannot be translated to IP + port.
     */
    public static synchronized NextHop getNextHop(String destination) {
        //
        // If the destination is us, return null. This means that
        // we don't have to go to the network to find the address;
        // the address is our own.
        //
        if (destination.equalsIgnoreCase(Mediator.getProperty("Instance.Address"))) {
            return null;
        }
        //
        // If we have the next hop already in our cache, return it.
        //
        NextHop hop = nextHopCache.get(destination);
        //
        // If it wasn't in our cache, parse it from the properties.
        //
        if (hop == null) {
            final String propertyPrefix = "IPAddressPort.";
            //
            // Check for an explicit entry for this destination address.
            //
            String hopString = Mediator.getProperty(propertyPrefix + destination);
            //
            // If there was no entry for the whole address, try successively
            // shorter strings by chopping off the end from the last '.' character.
            //
            String d = destination;
            while (hopString == null) {
                int i = d.lastIndexOf('.');
                //
                // If there are no more segments to chop, try for
                // the catch-all wildcard entry.
                //
                if (i < 0) {
                    hopString = Mediator.getProperty(propertyPrefix + "*");
                    break;
                }
                //
                // Chop the string and look for the next higher level.
                //
                d = d.substring(0, i);
                hopString = Mediator.getProperty(propertyPrefix + d);
            }
            if (hopString != null) {
                hop = new NextHop();
                hop.zip = false; // Default zip setting
                hop.maxSize = Integer.MAX_VALUE; // Default maxSize setting
                int slash = hopString.indexOf("/");
                if (slash > 0) {
                    String options = hopString.substring(slash + 1);
                    hopString = hopString.substring(0, slash);
                    for (String opt : options.split("/")) {
                        if (opt.equalsIgnoreCase("zip")) {
                            hop.zip = true;
                        }
                        String[] pair = opt.split("=");
                        if (pair[0].equalsIgnoreCase("maxSize")) {
                            hop.maxSize = Integer.parseInt(pair[1]);
                        }
                    }
                }
                hop.ipAddressPort = hopString;
                nextHopCache.put(destination, hop);
            }
        }
        return hop;
    }
}
