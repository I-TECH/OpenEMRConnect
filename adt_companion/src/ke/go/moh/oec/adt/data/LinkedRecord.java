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

/**
 * @date Apr 27, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class LinkedRecord {

    private final Record record;
    private final boolean master;
    private LinkedRecord masterLinkedRecord;
    private List<LinkedRecord> linkedRecordList;
    private RecordSource recordSource;

    public LinkedRecord(Record record, boolean master) {
        this.record = record;
        this.master = master;
    }

    public Record getRecord() {
        return record;
    }

    public boolean isMaster() {
        return master;
    }

    public LinkedRecord getMasterLinkedRecord() {
        return masterLinkedRecord;
    }

    public void setMasterLinkedRecord(LinkedRecord masterLinkedRecord) {
        this.masterLinkedRecord = masterLinkedRecord;
    }

    public List<LinkedRecord> getLinkedRecordList() {
        return linkedRecordList;
    }

    public void setLinkedRecordList(List<LinkedRecord> linkedRecordList) {
        this.linkedRecordList = linkedRecordList;
    }

    public RecordSource getRecordSource() {
        return recordSource;
    }

    public void setRecordSource(RecordSource recordSource) {
        this.recordSource = recordSource;
    }
}
