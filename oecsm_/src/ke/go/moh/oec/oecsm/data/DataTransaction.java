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
package ke.go.moh.oec.oecsm.data;

import ke.go.moh.oec.oecsm.bridge.Transaction;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class DataTransaction extends Transaction implements Comparable<DataTransaction> {

    private Cell cell;

    public DataTransaction(Cell cell, TransactionType type) {
        this.cell = cell;
        this.type = type;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell datum) {
        this.cell = datum;
    }

    @Override
    public String toString() {
        return this.type + " - " + this.cell.toString();
    }

    public int compareTo(DataTransaction that) {
        int datumComparison = this.cell.compareTo(that.cell);
        if (datumComparison != 0) {
            return datumComparison;
        } else {
            return this.type.compareTo(that.type);
        }
    }
}
