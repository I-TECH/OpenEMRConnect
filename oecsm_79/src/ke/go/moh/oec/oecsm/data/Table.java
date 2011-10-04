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

import java.util.ArrayList;
import java.util.List;

/**
 * @date Aug 13, 2010
 *
 * @author JGitahi
 */
public class Table implements SchemaTransactionTarget, Comparable<Table> {

    private int id;
    private String name;
    private String pk;
    private Database database;
    private List<Column> columnList = new ArrayList<Column>();

    public Table() {
    }

    public Table(String name) {
        this.name = name;
    }

    public Table(String name, String pk) {
        this.name = name;
        this.pk = pk;
    }

    public Table(int id, String name, String pk) {
        this.id = id;
        this.name = name;
        this.pk = pk;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
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

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Table other = (Table) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.database != other.database && (this.database == null || !this.database.equals(other.database))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.database != null ? this.database.hashCode() : 0);
        return hash;
    }

    public int compareTo(Table that) {
        int databaseComparison = this.database.compareTo(that.database);
        if (databaseComparison != 0) {
            return databaseComparison;
        } else {
            return this.name.compareToIgnoreCase(that.name);
        }
    }

    @Override
    public String toString() {
        return this.name + " - " + this.database.toString();
    }
}
