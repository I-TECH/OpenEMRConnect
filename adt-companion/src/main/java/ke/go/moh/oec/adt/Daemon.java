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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import ke.go.moh.oec.adt.controller.*;
import ke.go.moh.oec.adt.data.LinkedRecord;
import ke.go.moh.oec.adt.data.Record;
import ke.go.moh.oec.adt.data.RecordSource;
import ke.go.moh.oec.adt.data.Transaction;
import ke.go.moh.oec.adt.exceptions.BadRecordSourceException;
import ke.go.moh.oec.adt.format.OneLineRecordFormat;
import ke.go.moh.oec.adt.format.RecordFormat;
import ke.go.moh.oec.lib.Mediator;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.xml.sax.SAXException;

/**
 * @date Apr 29, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class Daemon implements Runnable {

    private final String method;
    private final long interval;
    private final String timeOfDay;
    private final long lookback;

    public Daemon(String method, long interval, String timeOfDay, long lookback) {
        this.method = method;
        this.interval = interval;
        this.timeOfDay = timeOfDay;
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
            List<RecordSource> recordSourceList = new ResourceManager().loadRecordSources();
            if ("interval".equalsIgnoreCase(method)) {
                while (true) {
                    work(recordSourceList, since);
                    Thread.sleep(interval);
                }
            } else {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                while (true) {
                    String currentTime = dateFormat.format(new Date());
                    if (currentTime.equalsIgnoreCase(timeOfDay)) {
                        work(recordSourceList, since);
                    }
                }
            }
        } catch (InterruptedException ex) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (BadRecordSourceException ex) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (ParserConfigurationException ex) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (SAXException ex) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (IOException ex) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        } catch (SQLException ex) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private void work(List<RecordSource> recordSourceList, Date since) throws SQLException, BadRecordSourceException, IOException {
        Mediator.getLogger(Daemon.class.getName()).log(Level.INFO, "Service running...");
        if (!recordSourceList.isEmpty()) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "Mining transactions...");
            TransactionMiner transactionMiner = new TransactionMiner();
            Map<RecordSource, Map<Integer, Transaction>> transactionMap =
                    transactionMiner.mine(recordSourceList, since);
            int transactionCount = countTransactions(transactionMap);
            if (transactionCount != 0) {
                Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "{0} transactions found.", transactionCount);
                Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "Mining records...");
                RecordMiner recordMiner = new RecordMiner();
                Map<RecordSource, List<Record>> recordMap = recordMiner.mine(transactionMap);
                int recordCount = countRecords(recordMap);
                Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "Linking {0} records...", recordCount);
                List<LinkedRecord> linkedRecordList = new RecordLinker(recordMiner).link(recordMap);
                if (!linkedRecordList.isEmpty()) {
                    Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "{0} records linked.", linkedRecordList.size());
                    RecordFormat oneLineFormat = new OneLineRecordFormat();
                    RecordCsvWriter csvWriter = new RecordCsvWriter(oneLineFormat);

                    int recordsPerFile = 100;
                    try {
                        recordsPerFile = Integer.parseInt(Mediator.getProperty("outputrecordlimit"));
                    } catch (Exception ex) {
                        Mediator.getLogger(Daemon.class.getName()).log(Level.INFO, "The outputrecordlimit property is missing, "
                                + "unspecified or not a number. The default value of 100 will be used.", ex);
                    }

                    csvWriter.writeToCsv(linkedRecordList, ResourceManager.getSetting("outputdir"), ResourceManager.getSetting("outputfilename"),
                            ResourceManager.getSetting("outputfileextension"), recordsPerFile);

                    // Send extracted file to remote Mirth instance if so configured
                    if ("remote".equalsIgnoreCase(ResourceManager.getSetting("mirth.location"))) {
                        if (!"".equals(ResourceManager.getSetting("mirth.url"))
                                && ResourceManager.getSetting("mirth.url") != null) {
                            if (sendMessage(ResourceManager.getSetting("mirth.url"), ResourceManager.getSetting("outputfilename") + ".csv")) {
                                Mediator.getLogger(Daemon.class.getName()).log(Level.INFO, "File sent!");
                            } else {
                                Mediator.getLogger(Daemon.class.getName()).log(Level.INFO, "File not sent!");
                            }
                        } else {
                            Mediator.getLogger(Daemon.class.getName()).log(Level.INFO, "No URL provided for remote Mirth instance.  The file was not sent!");
                        }
                    }
                } else {
                    Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "No records linked.");
                }
                transactionMiner.saveLastTransactionId();
                Mediator.getLogger(Daemon.class.getName()).log(Level.INFO, "Done!");
            } else {
                Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "No transactions found.");
            }
        } else {
            Mediator.getLogger(Daemon.class.getName()).log(Level.FINE, "No record sources found.");
        }
        Mediator.getLogger(Main.class.getName()).log(Level.INFO, "Suspending service for {0} seconds...", interval / 1000);
    }

    private int countTransactions(Map<RecordSource, Map<Integer, Transaction>> transactionMap) {
        int transactionCount = 0;
        for (RecordSource rs : transactionMap.keySet()) {
            Map<Integer, Transaction> tm = transactionMap.get(rs);
            if (tm != null) {
                transactionCount += tm.size();
            }
        }
        return transactionCount;
    }

    private int countRecords(Map<RecordSource, List<Record>> recordMap) {
        int recordCount = 0;
        for (RecordSource rs : recordMap.keySet()) {
            List<Record> rl = recordMap.get(rs);
            if (rl != null) {
                recordCount += rl.size();
            }
        }
        return recordCount;
    }

    private static boolean sendMessage(String url, String filename) {
        int returnStatus = HttpStatus.SC_CREATED;
        HttpClient httpclient = new HttpClient();
        HttpConnectionManager connectionManager = httpclient.getHttpConnectionManager();
        connectionManager.getParams().setSoTimeout(120000);

        PostMethod httpPost = new PostMethod(url);

        RequestEntity requestEntity;
        try {
            FileInputStream message = new FileInputStream(filename);
            Base64InputStream message64 = new Base64InputStream(message, true, -1, null);
            requestEntity = new InputStreamRequestEntity(message64, "application/octet-stream");
        } catch (FileNotFoundException e) {
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, "File not found.", e);
            return false;
        }
        httpPost.setRequestEntity(requestEntity);
        try {
            httpclient.executeMethod(httpPost);
            returnStatus = httpPost.getStatusCode();
        } catch (SocketTimeoutException e) {
            returnStatus = HttpStatus.SC_REQUEST_TIMEOUT;
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, "Request timed out.  Not retrying.", e);
        } catch (HttpException e) {
            returnStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, "HTTP exception.  Not retrying.", e);
        } catch (ConnectException e) {
            returnStatus = HttpStatus.SC_SERVICE_UNAVAILABLE;
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, "Service unavailable.  Not retrying.", e);
        } catch (UnknownHostException e) {
            returnStatus = HttpStatus.SC_NOT_FOUND;
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, "Not found.  Not retrying.", e);
        } catch (IOException e) {
            returnStatus = HttpStatus.SC_GATEWAY_TIMEOUT;
            Mediator.getLogger(Daemon.class.getName()).log(Level.SEVERE, "IO exception.  Not retrying.", e);
        } finally {
            httpPost.releaseConnection();
        }
        return returnStatus == HttpStatus.SC_OK;
    }
}
