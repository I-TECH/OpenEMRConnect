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
package ke.go.moh.oec.adt.data;

import java.util.Map;

/**
 * @date Apr 25, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class Transaction {

    private final int id;
    private final String tableName;
    private final TransactionType type;
    private Map<Column, String> primaryKey;

    public Transaction(int id, String tableName, TransactionType type) {
        this.id = id;
        this.tableName = tableName;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getTableName() {
        return tableName;
    }

    public TransactionType getType() {
        return type;
    }

    public Map<Column, String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Map<Column, String> primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transaction other = (Transaction) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
