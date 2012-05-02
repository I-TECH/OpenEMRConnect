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
package ke.go.moh.oec.adt.format;

import ke.go.moh.oec.adt.data.Record;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.adt.data.Column;
import ke.go.moh.oec.adt.data.LinkedRecord;
import ke.go.moh.oec.adt.data.RecordSource;

/**
 * @date Apr 24, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class OneLineRecordFormat implements RecordFormat {

    @Override
    public List<String[]> format(List<LinkedRecord> linkedRecordList) {
        List<Record> recordList = new ArrayList<Record>();
        for (LinkedRecord linkedRecord : linkedRecordList) {
            recordList.addAll(extractRecordList(linkedRecord));
        }
        int recordSize = 0;
        Record biggestRecord = null;
        for (Record record : recordList) {
            int rs = record.getOrdinaryCellMap().size();
            if (rs > recordSize) {
                recordSize = rs;
                biggestRecord = record;
            }
        }
        recordList.remove(biggestRecord);
        List<Record> orderedRecordList = new ArrayList<Record>(recordList);
        orderedRecordList.add(0, biggestRecord);
        List<String[]> stringArrayList = FormatHelper.convertToStringArrayList(orderedRecordList);
        return stringArrayList;
    }

    private List<Record> extractRecordList(LinkedRecord linkedRecord) {
        List<Record> recordList = new ArrayList<Record>();
        if (linkedRecord.getRecordSource().getRelationship() == RecordSource.Relationship.NONE) {
            recordList.add(linkedRecord.getRecord());
        } else if (linkedRecord.getRecordSource().getRelationship() == RecordSource.Relationship.MASTER) {
            if (linkedRecord.getLinkedRecordList() != null) {
                for (LinkedRecord lr : linkedRecord.getLinkedRecordList()) {
                    for (Column column : lr.getRecord().getOrdinaryCellMap().keySet()) {
                        Column newColumn = new Column(column.getName(), true);
                        String newData = lr.getRecord().getOrdinaryCellMap().get(column);
                        linkedRecord.getRecord().getOrdinaryCellMap().put(newColumn, newData);
                    }
                }
            }
            recordList.add(linkedRecord.getRecord());
        }
        return recordList;
    }
}
