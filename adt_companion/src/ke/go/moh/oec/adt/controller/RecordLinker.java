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
package ke.go.moh.oec.adt.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ke.go.moh.oec.adt.data.*;
import ke.go.moh.oec.adt.exceptions.BadRecordSourceException;

/**
 * @date Apr 27, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class RecordLinker {

    private final RecordMiner recordMiner;
    private final Map<String, LinkedRecord> cachedMasterLinkedRecordMap;

    public RecordLinker(RecordMiner recordMiner) {
        this.recordMiner = recordMiner;
        cachedMasterLinkedRecordMap = new LinkedHashMap<String, LinkedRecord>();
    }

    public List<LinkedRecord> link(Map<RecordSource, List<Record>> recordMap) throws SQLException, BadRecordSourceException {
        List<LinkedRecord> linkedRecordList = new ArrayList<LinkedRecord>();
        Map<RecordSource, List<Record>> masterRecordMap = new LinkedHashMap<RecordSource, List<Record>>();
        Map<RecordSource, List<Record>> slaveRecordMap = new LinkedHashMap<RecordSource, List<Record>>();
        for (RecordSource recordSource : recordMap.keySet()) {
            if (recordSource.getRelationship() == RecordSource.Relationship.NONE) {
                for (Record record : recordMap.get(recordSource)) {
                    linkedRecordList.add(new LinkedRecord(record, false));
                }
            } else {
                if (recordSource.getRelationship() == RecordSource.Relationship.MASTER) {
                    masterRecordMap.put(recordSource, recordMap.get(recordSource));
                } else {
                    slaveRecordMap.put(recordSource, recordMap.get(recordSource));
                }
            }
        }
        if (masterRecordMap.isEmpty()) {
            if (!slaveRecordMap.isEmpty()) {
                linkedRecordList.addAll(doNotLinkWithMaster(slaveRecordMap));
            }
        } else {
            if (slaveRecordMap.isEmpty()) {
                linkedRecordList.addAll(doNotLinkWithMaster(masterRecordMap));
            } else {
                linkedRecordList.addAll(linkWithMaster(masterRecordMap, slaveRecordMap));
                linkedRecordList.addAll(getUnlinkedMasterRecords(masterRecordMap));
            }
        }
        if (Boolean.parseBoolean(ResourceManager.getSetting("siteinfo"))) {
            for (LinkedRecord linkedRecord : linkedRecordList) {
                addSiteInformation(linkedRecord);
            }
        }
        return linkedRecordList;
    }

    private List<LinkedRecord> doNotLinkWithMaster(Map<RecordSource, List<Record>> recordMap) {
        List<LinkedRecord> linkedRecordList = new ArrayList<LinkedRecord>();
        for (RecordSource recordSource : recordMap.keySet()) {
            for (Record record : recordMap.get(recordSource)) {
                linkedRecordList.add(new LinkedRecord(record, false));
            }
        }
        return linkedRecordList;
    }

    private List<LinkedRecord> linkWithMaster(Map<RecordSource, List<Record>> masterRecordMap,
            Map<RecordSource, List<Record>> slaveRecordMap) throws SQLException, BadRecordSourceException {
        cachedMasterLinkedRecordMap.clear();
        Map.Entry<RecordSource, List<Record>> entry = new ArrayList<Map.Entry<RecordSource, List<Record>>>(masterRecordMap.entrySet()).get(0);
        RecordSource masterRecordSource = entry.getKey();
        List<Record> masterRecordList = entry.getValue();
        for (RecordSource recordSource : slaveRecordMap.keySet()) {
            for (Record record : slaveRecordMap.get(recordSource)) {
                LinkedRecord masterLinkedRecord = getMasterLinkedRecord(masterRecordSource, masterRecordList, recordSource, record);
                masterLinkedRecord.setRecordSource(masterRecordSource);
                if (masterLinkedRecord.getLinkedRecordList() == null) {
                    masterLinkedRecord.setLinkedRecordList(new ArrayList<LinkedRecord>());
                }
                LinkedRecord slaveLinkedRecord = new LinkedRecord(record, false);
                slaveLinkedRecord.setRecordSource(recordSource);
                slaveLinkedRecord.setMasterLinkedRecord(masterLinkedRecord);
                masterLinkedRecord.getLinkedRecordList().add(slaveLinkedRecord);
            }
        }
        return new ArrayList<LinkedRecord>(cachedMasterLinkedRecordMap.values());
    }

    private LinkedRecord getMasterLinkedRecord(RecordSource masterRecordSource, List<Record> masterRecordList,
            RecordSource slaveRecordSource, Record slaveRecord) throws SQLException, BadRecordSourceException {
        LinkedRecord masterLinkedRecord = null;
        Map<String, Column> foreignKeyColumnMap = slaveRecordSource.getForeignKeyColumnMap();
        Map<Column, String> foreignKeyMap = new LinkedHashMap<Column, String>();
        int foreignKeySize = foreignKeyColumnMap.size();
        for (String columnName : foreignKeyColumnMap.keySet()) {
            for (Column column : slaveRecord.getOrdinaryCellMap().keySet()) {
                if (column.getName().equals(columnName)) {
                    foreignKeyMap.put(column, slaveRecord.getOrdinaryCellMap().get(column));
                }
                if (foreignKeyMap.size() >= foreignKeySize) {
                    break;
                }
            }
        }
        Map<String, Column> primaryKeyColumnMap = masterRecordSource.getPrimaryKeyColumnMap();
        int primaryKeySize = primaryKeyColumnMap.size();
        if (foreignKeySize != primaryKeySize) {
            throw new BadRecordSourceException("Foreign key - primary key mismatch!");
        }
        String compositePrimaryKeyValue = "";
        List<String> foreignKeyValueList = new ArrayList<String>(foreignKeyMap.values());
        for (String string : foreignKeyValueList) {
            compositePrimaryKeyValue += string;
            compositePrimaryKeyValue += "#";
        }
        if (cachedMasterLinkedRecordMap.containsKey(compositePrimaryKeyValue)) {
            masterLinkedRecord = cachedMasterLinkedRecordMap.get(compositePrimaryKeyValue);
        } else {
            for (Record record : masterRecordList) {
                boolean found = true;
                List<String> primaryKeyValueList = new ArrayList<String>(record.getPrimaryKeyCellMap().values());
                for (int i = 0; i < foreignKeyValueList.size(); i++) {
                    String fkv = foreignKeyValueList.get(i);
                    String pkv = primaryKeyValueList.get(i);
                    if (!fkv.equals(pkv)) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    masterLinkedRecord = new LinkedRecord(record, true);
                    break;
                }
            }
        }
        if (masterLinkedRecord == null) {
            masterLinkedRecord = new LinkedRecord(mineMasterRecord(masterRecordSource, foreignKeyValueList), true);
        }
        cachedMasterLinkedRecordMap.put(compositePrimaryKeyValue, masterLinkedRecord);
        return masterLinkedRecord;
    }

    private Record mineMasterRecord(RecordSource recordSource, List<String> primaryKeyValueList) throws SQLException {
        Transaction transaction = new Transaction(0, recordSource.getTableName(), TransactionType.SELECT);
        if (recordSource.getPrimaryKeyColumnMap().size() != primaryKeyValueList.size()) {
            throw new IllegalArgumentException("Bad primary key value list.");
        }
        Map<Column, String> primaryKey = new LinkedHashMap<Column, String>();
        int index = 0;
        for (Column column : recordSource.getPrimaryKeyColumnMap().values()) {
            primaryKey.put(column, primaryKeyValueList.get(index));
            index++;
        }
        transaction.setPrimaryKey(primaryKey);
        return recordMiner.createRecord(transaction, recordSource, null);
    }

    private List<LinkedRecord> getUnlinkedMasterRecords(Map<RecordSource, List<Record>> masterRecordMap) {
        Map.Entry<RecordSource, List<Record>> entry = new ArrayList<Map.Entry<RecordSource, List<Record>>>(masterRecordMap.entrySet()).get(0);
        RecordSource masterRecordSource = entry.getKey();
        List<Record> allMasterRecordList = entry.getValue();
        List<Record> copyOfAllMasterRecordList = new ArrayList<Record>(allMasterRecordList);
        List<Record> linkedMasterRecordList = new ArrayList<Record>();
        for (LinkedRecord linkedRecord : new ArrayList<LinkedRecord>(cachedMasterLinkedRecordMap.values())) {
            linkedMasterRecordList.add(linkedRecord.getRecord());
        }
        copyOfAllMasterRecordList.removeAll(linkedMasterRecordList);
        List<LinkedRecord> unlinkedMasterRecordList = new ArrayList<LinkedRecord>();
        for (Record record : copyOfAllMasterRecordList) {
            LinkedRecord linkedRecord = new LinkedRecord(record, true);
            linkedRecord.setRecordSource(masterRecordSource);
            unlinkedMasterRecordList.add(linkedRecord);
        }
        return unlinkedMasterRecordList;
    }

    private void addSiteInformation(LinkedRecord linkedRecord) {
        String siteName = ResourceManager.getSetting("site.name");
        String siteCode = ResourceManager.getSetting("site.code");
        String sourceSystem = ResourceManager.getSetting("source.system");
        Map<Column, String> recordCellMap = new LinkedHashMap<Column, String>();
        recordCellMap.put(new Column("Site Name", false), siteName);
        recordCellMap.put(new Column("Site Code", false), siteCode);
        recordCellMap.put(new Column("Source System", false), sourceSystem);
        for (Column column : linkedRecord.getRecord().getOrdinaryCellMap().keySet()) {
            recordCellMap.put(column, linkedRecord.getRecord().getOrdinaryCellMap().get(column));
        }
        linkedRecord.getRecord().setOrdinaryCellMap(recordCellMap);

    }
}
