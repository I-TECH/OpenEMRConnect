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

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import ke.go.moh.oec.adt.Main;
import ke.go.moh.oec.adt.data.LinkedRecord;
import ke.go.moh.oec.adt.data.Record;
import ke.go.moh.oec.adt.format.RecordFormat;
import ke.go.moh.oec.lib.Mediator;

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

    public void writeToCsv(List<LinkedRecord> linkedRecordList, String outputDir, String fileName) throws IOException {
        CSVWriter csvWriter = null;
        try {
            String fullFileName = fileName;
            if (outputDir != null) {
                File outputDirFile = new File(outputDir);
                if (!outputDirFile.exists()) {
                    Mediator.getLogger(Main.class.getName()).log(Level.FINE, "Attempting to create missing directory {0}...",
                            outputDir);
                    if (!outputDirFile.mkdirs()) {
                        Mediator.getLogger(Main.class.getName()).log(Level.FINE, "Failed to create missing directory {0}. "
                                + "Output will be placed in application path instead.", outputDir);
                    } else {
                        Mediator.getLogger(Main.class.getName()).log(Level.FINE, "Succeeded to create missing directory {0}.", outputDir);
                    }
                } else {
                    fullFileName = outputDir + "\\" + fileName;
                }
            }
            Mediator.getLogger(Main.class.getName()).log(Level.INFO, "Writing output for {0} records to file [{1}]...", 
                    new Object[]{linkedRecordList.size(), fullFileName});
            csvWriter = new CSVWriter(new FileWriter(new File(fullFileName)));
            csvWriter.writeAll(format.format(linkedRecordList));
            Mediator.getLogger(Main.class.getName()).log(Level.INFO,"Finished writing output");
        } finally {
            if (csvWriter != null) {
                csvWriter.close();
            }
        }
    }
}
