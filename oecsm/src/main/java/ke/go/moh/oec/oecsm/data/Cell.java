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

/**
 * @date Aug 19, 2010
 *
 * @author Gitahi Ng'ang'a
 */
public class Cell implements Comparable<Cell> {

    private int id;
    private Column column;
    private String primaryKeyValue;
    private String data;

    public Cell(String primaryKeyValue, String data) {
        this.primaryKeyValue = primaryKeyValue;
        this.data = data;
    }

    public Cell(int id, String data) {
        this.id = id;
        this.data = data;
    }

    public Cell(int id, String primaryKeyValue, String data) {
        this.id = id;
        this.primaryKeyValue = primaryKeyValue;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public String getPrimaryKeyValue() {
        return primaryKeyValue;
    }

    public void setPrimaryKeyValue(String pkValue) {
        this.primaryKeyValue = pkValue;
    }

    public String getData() {
        return data;
    }

    public void setData(String value) {
        this.data = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cell other = (Cell) obj;
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        if ((this.primaryKeyValue == null) ? (other.primaryKeyValue != null) : !this.primaryKeyValue.equals(other.primaryKeyValue)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public String toString() {
        if (column != null) {
            return "(" + this.primaryKeyValue + ")" + " - " + this.data + " - " + this.column.toString();
        } else {
            return "(" + this.primaryKeyValue + ")" + " - " + this.data;
        }
    }

    public int compareTo(Cell that) {
        int tableComparison = this.column.getTable().compareTo(that.column.getTable());
        if (tableComparison != 0) {
            return tableComparison;
        } else {
            int pkComparison = this.primaryKeyValue.compareToIgnoreCase(that.primaryKeyValue);
            if (pkComparison != 0) {
                return pkComparison;
            } else {
                return this.column.compareTo(that.column);
            }
        }
    }
}
