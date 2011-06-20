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
package ke.go.moh.oec.client.controller;

import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class OECClient {

    public OECClient() {
    }

    public static String getApplicationAddress() {
        String instanceName = Mediator.getProperty("Instance.Address");
        if (instanceName == null) {
            instanceName = getApplicationName();
        }
        return instanceName;
    }

    public static String getApplicationName() {
        String instanceName = Mediator.getProperty("Instance.Name");
        if (instanceName == null) {
            instanceName = "Clinic Reception";
        }
        return instanceName;
    }

    public static PersonIdentifier.Type deducePersonIdentifierType(String personIdentifier) {
        PersonIdentifier.Type identifierType = null;
        if (personIdentifier != null && !personIdentifier.isEmpty()) {
            if (personIdentifier.contains("-") && !personIdentifier.contains("/")) {
                if ((personIdentifier.split("-").length == 2 && personIdentifier.split("-")[0].length() == 5)
                        && (personIdentifier.split("-").length == 2 && personIdentifier.split("-")[1].length() == 5)) {
                    identifierType = PersonIdentifier.Type.cccUniqueId;
                } else if (personIdentifier.length() < 20
                        && personIdentifier.split("-").length == 4) {
                    identifierType = PersonIdentifier.Type.kisumuHdssId;
                }
            } else if (personIdentifier.contains("/") && !personIdentifier.contains("-")) {
                if ((personIdentifier.split("/").length == 2 && personIdentifier.split("/")[0].length() == 5)
                        && (personIdentifier.split("/").length == 2 && personIdentifier.split("/")[1].length() == 4)) {
                    identifierType = PersonIdentifier.Type.cccLocalId;
                }
            }
            if (personIdentifier.length() > 20) {
                identifierType = PersonIdentifier.Type.masterPatientRegistryId;
            }
        }
        return identifierType;
    }
}
