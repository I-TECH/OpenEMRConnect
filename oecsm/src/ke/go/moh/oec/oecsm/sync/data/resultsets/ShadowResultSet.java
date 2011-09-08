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
package ke.go.moh.oec.oecsm.sync.data.resultsets;

import ke.go.moh.oec.oecsm.data.Column;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import ke.go.moh.oec.oecsm.data.Cell;

/**
 * @date Jun 1, 2011
 * 
 * @author Jim Grace
 */
public class ShadowResultSet {

    private ResultSet rs; // The "real" ResultSet from the query to shadow cells table.
    private HashMap<String, Cell> valueMap = new HashMap<String, Cell>();
    private boolean hasNext = false;

    public ShadowResultSet(ResultSet rs) throws SQLException {
        this.rs = rs;
        hasNext = rs.next();// rs now contains the first query result row, yet to be processed by us, if available.
    }

    public Cell getCell(Column column) throws SQLException {
        return valueMap.get("C" + column.getId());
    }

    public Cell getCell(String columnLabel) throws SQLException {
        return valueMap.get(columnLabel);
    }

    public boolean next() throws SQLException {
        if (!hasNext) {
            return false;
        } else {
            valueMap.clear();
            String pkValue = rs.getString("PRIMARY_KEY_VALUE");
            valueMap.put("PK", new Cell(rs.getInt("ID"), pkValue, pkValue));
            do {
                valueMap.put("C" + rs.getInt("COLUMN_ID"), new Cell(rs.getInt("ID"),
                        pkValue, rs.getString("DATA")));
                hasNext = rs.next();
            } while (hasNext && rs.getString("PRIMARY_KEY_VALUE").equals(pkValue));
        }
        return true;
    }

    public void close() throws SQLException {
        rs.close();
    }
}
