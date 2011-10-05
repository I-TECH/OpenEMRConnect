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
 * @date Aug 13, 2010
 *
 * @author JGitahi
 */
public class Column implements SchemaTransactionTarget, Comparable<Column> {

    private int id;
    private String name;
    private boolean replicable = true;
    private int ordinalPosition;
    private String dataType;
    private boolean binaryType;
    private int size;
    private Table table;

    public Column() {
    }

    public Column(String name, int ordinalPosition, String dataType, int size) {
        this.name = name;
        this.ordinalPosition = ordinalPosition;
        this.dataType = dataType;
        this.size = size;
    }

    public Column(int id, String name, int order, String dataType, int size, boolean replicable) {
        this.id = id;
        this.name = name;
        this.ordinalPosition = order;
        this.dataType = dataType;
        this.size = size;
        this.replicable = replicable;
    }

    public boolean isBinaryType() {
        return binaryType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
        if ("BINARY|VARBINARY|BLOB|IMAGE|BYTEA".indexOf(dataType) >= 0) {
            binaryType = true;
        } else {
            binaryType = false;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public boolean isReplicable() {
        return replicable;
    }

    public void setReplicable(boolean replicable) {
        this.replicable = replicable;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Column(String name, Table table) {
        this.name = name;
        this.table = table;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Column other = (Column) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.table != other.table && (this.table == null || !this.table.equals(other.table))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.table != null ? this.table.hashCode() : 0);
        return hash;
    }

    public int compareTo(Column that) {
        int tableComparison = this.table.compareTo(that.table);
        if (tableComparison != 0) {
            return tableComparison;
        } else {
            return Integer.valueOf(this.ordinalPosition).compareTo(Integer.valueOf(that.ordinalPosition));
        }
    }

    @Override
    public String toString() {
        return this.name + " - " + this.table.toString();
    }
}
