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
                    String filename = "ADT Extract No. " + new Date().getTime();
                    csvWriter.writeToCsv(linkedRecordList, filename);

                    // Send extracted file to remote Mirth instance if so configured
                    if ("remote".equalsIgnoreCase(ResourceManager.getSetting("mirth.location"))) {
                        if (!"".equals(ResourceManager.getSetting("mirth.url"))
                                && ResourceManager.getSetting("mirth.url") != null) {
                            if (sendMessage(ResourceManager.getSetting("mirth.url"), filename + ".csv")) {
                                Logger.getLogger(Daemon.class.getName()).log(Level.INFO, "File sent!");
                            } else {
                                Logger.getLogger(Daemon.class.getName()).log(Level.INFO, "File not sent!");
                            }
                        } else {
                            Logger.getLogger(Daemon.class.getName()).log(Level.INFO, "No URL provided for remote Mirth instance.  The file was not sent!");
                        }
                    }
                }
                transactionMiner.saveLastTransactionId();
                Thread.sleep(snooze);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadRecordSourceException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, "File not found.", e);
            return false;
        }
        httpPost.setRequestEntity(requestEntity);
        try {
            httpclient.executeMethod(httpPost);
            returnStatus = httpPost.getStatusCode();
        } catch (SocketTimeoutException e) {
            returnStatus = HttpStatus.SC_REQUEST_TIMEOUT;
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, "Request timed out.  Not retrying.", e);
        } catch (HttpException e) {
            returnStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, "HTTP exception.  Not retrying.", e);
        } catch (ConnectException e) {
            returnStatus = HttpStatus.SC_SERVICE_UNAVAILABLE;
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, "Service unavailable.  Not retrying.", e);
        } catch (UnknownHostException e) {
            returnStatus = HttpStatus.SC_NOT_FOUND;
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, "Not found.  Not retrying.", e);
        } catch (IOException e) {
            returnStatus = HttpStatus.SC_GATEWAY_TIMEOUT;
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, "IO exception.  Not retrying.", e);
        } finally {
            httpPost.releaseConnection();
        }
        return returnStatus == HttpStatus.SC_OK;
    }
}
