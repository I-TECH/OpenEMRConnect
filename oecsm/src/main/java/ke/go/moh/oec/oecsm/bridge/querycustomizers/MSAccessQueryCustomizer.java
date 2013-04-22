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
package ke.go.moh.oec.oecsm.bridge.querycustomizers;

import ke.go.moh.oec.oecsm.daemon.Daemon;
import ke.go.moh.oec.oecsm.data.Table;

/**
 * @date Aug 21, 2010
 *
 * @author Gitahi Ng'ang'a
 */
public class MSAccessQueryCustomizer implements QueryCustomizer {

    public String buildCompositePrimaryKey(Table tableStructure) {
        String pkDelim = Daemon.getProperty("primary.key.value.delimiter");
        String compositePK = "";
        String[] pks = tableStructure.getPk().split(",");
        for (int i = 0; i < pks.length; i++) {
            compositePK = compositePK + pks[i];
            if (i != pks.length - 1) {
                compositePK = compositePK + "& '" + pkDelim + "' &";
            }
        }
        if (pks.length == 1) { // Make sure PK values always sort as a string.
            compositePK = "CStr(" + compositePK + ")";
        }
        return compositePK;
    }

    public String buildAsciiCompositePrimaryKey(Table table) {
        return "ASC(" + buildCompositePrimaryKey(table) + ")";
    }

    public String buildAsciiCompositePrimaryKey(String compositePk) {
        return "ASC(" + compositePk + ")";
    }

    public String getOpenningSafetyPad() {
        return "[";
    }

    public String getClosingSafetyPad() {
        return "]";
    }
}
