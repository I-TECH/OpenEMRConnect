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
package ke.go.moh.oec.adt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import ke.go.moh.oec.adt.controller.*;
import ke.go.moh.oec.adt.data.LinkedRecord;
import ke.go.moh.oec.adt.data.Record;
import ke.go.moh.oec.adt.data.RecordSource;
import ke.go.moh.oec.adt.data.Transaction;
import ke.go.moh.oec.adt.exceptions.BadRecordSourceException;
import ke.go.moh.oec.adt.format.OneLineRecordFormat;
import ke.go.moh.oec.adt.format.RecordFormat;
import org.xml.sax.SAXException;

/**
 * @date Apr 29, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class Daemon implements Runnable {

    private final long snooze;
    private final long lookback;

    public Daemon(long snooze, long lookback) {
        this.snooze = snooze;
        this.lookback = lookback;
    }

    @Override
    public void run() {
        try {
            Date since = null;
            if (lookback > 0) {
                Date now = new Date();
                since = new Date(now.getTime() - lookback);
            }
            while (true) {
                List<RecordSource> recordSourceList = new ResourceManager().loadRecordSources();
                TransactionMiner transactionMiner = new TransactionMiner();
                Map<RecordSource, Map<Integer, Transaction>> transactionMap =
                        transactionMiner.mine(recordSourceList, since);
                RecordMiner recordMiner = new RecordMiner();
                Map<RecordSource, List<Record>> recordMap = recordMiner.mine(transactionMap);
                List<LinkedRecord> linkedRecordList = new RecordLinker(recordMiner).link(recordMap);
                if (!linkedRecordList.isEmpty()) {
                    RecordFormat oneLineFormat = new OneLineRecordFormat();
                    RecordCsvWriter csvWriter = new RecordCsvWriter(oneLineFormat);
                    csvWriter.writeToCsv(linkedRecordList, "Extract No. " + new Date().getTime());
                }
                transactionMiner.saveLastTransactionId();
                Thread.sleep(snooze);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadRecordSourceException ex) {
            Logger.getLogger(TransactionMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TransactionMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(TransactionMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TransactionMiner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TransactionMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
