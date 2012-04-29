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

import java.util.List;
import java.util.Map;

/**
 * @date Apr 22, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class RecordSource implements Comparable<RecordSource> {

    public enum Relationship {

        MASTER,
        ONE,
        MANY,
        NONE
    }
    private Relationship relationship;
    private final Integer order;
    private String tableName;
    private Map<String, Column> primaryKeyColumnMap;
    private Map<String, Column> foreignKeyColumnMap;
    private boolean cumulate;
    private int limit;
    private List<Column> columnList;

    public RecordSource(Integer order) {
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public boolean isCumulate() {
        return cumulate;
    }

    public void setCumulate(boolean cumulate) {
        this.cumulate = cumulate;
    }

    public Map<String, Column> getForeignKeyColumnMap() {
        return foreignKeyColumnMap;
    }

    public void setForeignKeyColumnMap(Map<String, Column> foreignKeyColumnMap) {
        this.foreignKeyColumnMap = foreignKeyColumnMap;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Map<String, Column> getPrimaryKeyColumnMap() {
        return primaryKeyColumnMap;
    }

    public void setPrimaryKeyColumnMap(Map<String, Column> primaryKeyColumnMap) {
        this.primaryKeyColumnMap = primaryKeyColumnMap;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RecordSource other = (RecordSource) obj;
        if ((this.tableName == null) ? (other.tableName != null) : !this.tableName.equals(other.tableName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.tableName != null ? this.tableName.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(RecordSource that) {
        return this.order.compareTo(that.order);
    }
}
