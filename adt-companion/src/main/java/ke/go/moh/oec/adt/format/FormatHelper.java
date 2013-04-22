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
import java.util.Map;
import ke.go.moh.oec.adt.data.Column;

/**
 * @date Apr 22, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class FormatHelper {

    public static List<String[]> convertToStringArrayList(List<Record> recordList) {
        List<String[]> stringArrayList = new ArrayList<String[]>();
        if (recordList != null && !recordList.isEmpty()) {
            stringArrayList.add(convertToStringArray(recordList.get(0), true));
            for (Record record : recordList) {
                stringArrayList.add(convertToStringArray(record));
            }
        }
        return stringArrayList;
    }

    private static String[] convertToStringArray(Record record) {
        return convertToStringArray(record, false);
    }

    private static String[] convertToStringArray(Record record, boolean asHeader) {
        if (record != null) {
            Map<Column, String> cellMap = record.getOrdinaryCellMap();
            if (cellMap != null && !cellMap.isEmpty()) {
                String[] stringArray = new String[cellMap.size()];
                int index = 0;
                for (Column column : cellMap.keySet()) {
                    String stringArrayEntry;
                    if (asHeader) {
                        stringArrayEntry = column.getName();
                    } else {
                        stringArrayEntry = cellMap.get(column);
                    }
                    stringArray[index] = ((stringArrayEntry != null) ? stringArrayEntry : "");
                    index++;
                }
                return stringArray;
            }
        }
        return null;
    }

    public static String[] lineSeparator(int length) {
        String[] lineSeparator = new String[length];
        for (int i = 0; i < length; i++) {
            lineSeparator[i] = "";
        }
        return lineSeparator;
    }
}
