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
package ke.go.moh.oec.oecsm.sync.data;

import ke.go.moh.oec.oecsm.data.Column;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @date Jun 1, 2011
 * 
 * @author Jim Grace
 */
public class ShadowResultSet {

    ResultSet rs; // The "real" ResultSet from the query to shadow cells table.
    HashMap<String, String> valueMap = new HashMap<String, String>();
    boolean afterLast = true;

    public ShadowResultSet(ResultSet rs) throws SQLException {
        this.rs = rs;
        if (!rs.isAfterLast()) {
            afterLast = !rs.next(); // rs now contains the first query result row, yet to be processed by us.
        }
    }

    public String getString(Column column) throws SQLException {
        return valueMap.get("C" + column.getId());
    }
    
    public String getString(String columnLabel) throws SQLException {
        return valueMap.get(columnLabel);
    }

    public boolean isAfterLast() throws SQLException {
        return afterLast;
    }

    public boolean next() throws SQLException {
        if (rs.isAfterLast()) {
            afterLast = true;
            return false; // Return opposize of afterLast.
        } else {
            valueMap.clear();
            String pkValue = rs.getString("PRIMARY_KEY_VALUE");
            valueMap.put("PK", pkValue);
            do {
                valueMap.put("C" + rs.getInt("COLUMN_ID"), rs.getString("DATA"));
            } while (rs.next() && rs.getString("PRIMARY_KEY_VALUE").equals(pkValue));
        }
        return true; // Return opposize of afterLast.
    }
    
    public void close() throws SQLException {
        rs.close();
    }
}
