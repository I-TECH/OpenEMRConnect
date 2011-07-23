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

/**
 *
 * @author Jim Grace
 */
public class SourceResultSet {

    ResultSet rs; // The "real" ResultSet.

    public SourceResultSet(ResultSet rs) {
        this.rs = rs;
    }

    public String getString(Column column) throws SQLException {
        String returnString = null;
        if (column.isBinaryType()) { // Convert any binary type field to hexadecimal digit string.
            byte[] byteArray = rs.getBytes("C" + column.getId());
            if (byteArray != null) {
                StringBuilder hex = new StringBuilder(byteArray.length * 2);
                for (byte b : byteArray) {
                    hex.append(String.format("%02X", b));
                }
                returnString = hex.toString();
            }
        }
        else {
            returnString = rs.getString("C" + column.getId());
        }
        return returnString;
    }

    public String getString(String columnLabel) throws SQLException {
        return rs.getString(columnLabel);
    }

    public boolean next() throws SQLException {
        return rs.next();
    }

    public void close() throws SQLException {
        rs.close();
    }
}
