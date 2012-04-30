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

import ke.go.moh.oec.adt.format.RecordFormat;
import ke.go.moh.oec.adt.data.Record;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import ke.go.moh.oec.adt.data.LinkedRecord;

/**
 * This class provides a mechanism by which a list of {@link Record}s can be
 * written out to disk as a single .csv file.
 *
 * @date Apr 21, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class RecordCsvWriter {

    private final RecordFormat format;

    public RecordCsvWriter(RecordFormat format) {
        this.format = format;
    }

    public void writeToCsv(List<LinkedRecord> linkedRecordList, String fileName) throws IOException {
        CSVWriter csvWriter = null;
        try {
            csvWriter = new CSVWriter(new FileWriter(fileName + ".csv"));
            csvWriter.writeAll(format.format(linkedRecordList));
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (csvWriter != null) {
                csvWriter.close();
            }
        }
    }
}
