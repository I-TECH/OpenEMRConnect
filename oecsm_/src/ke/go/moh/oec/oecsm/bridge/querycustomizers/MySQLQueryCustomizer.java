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

import ke.go.moh.oec.oecsm.data.Table;

/**
 * @date Aug 21, 2010
 *
 * @author JGitahi
 */
public class MySQLQueryCustomizer implements QueryCustomizer {

    public String buildCompositePrimaryKey(Table tableStructure) {
        String compositePK = "";
        String[] pks = tableStructure.getPk().split(",");
        compositePK = "CONCAT(";
        for (int i = 0; i < pks.length; i++) {
            compositePK = compositePK + pks[i];
            if (i != pks.length - 1) {
                compositePK = compositePK + ",";
            } else {
                compositePK = compositePK + ")";
            }
        }
        return compositePK;
    }

    public String getOpenningSafetyPad() {
        return "`";
    }

    public String getClosingSafetyPad() {
        return getOpenningSafetyPad();
    }
}
