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
 * This class represents a record of data. A record of data is composed of a
 * {@link PrimaryKey} and one or more {@link Cell}s.
 *
 * @date Apr 21, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class Record {

    private Map<Column, String> primaryKeyCellMap;
    private Map<Column, String> ordinaryCellMap;
    private TransactionType transactionType;

    public Map<Column, String> getOrdinaryCellMap() {
        return ordinaryCellMap;
    }

    public void setOrdinaryCellMap(Map<Column, String> ordinaryCellMap) {
        this.ordinaryCellMap = ordinaryCellMap;
    }

    public Map<Column, String> getPrimaryKeyCellMap() {
        return primaryKeyCellMap;
    }

    public void setPrimaryKeyCellMap(Map<Column, String> primaryKeyCellMap) {
        this.primaryKeyCellMap = primaryKeyCellMap;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
