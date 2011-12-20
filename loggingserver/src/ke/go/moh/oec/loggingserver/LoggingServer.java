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
package ke.go.moh.oec.loggingserver;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.IService;
import ke.go.moh.oec.LogEntry;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;

/**
 * OpenEMRConnect Logging Server.
 * 
 * @author Jim Grace
 */
public class LoggingServer implements IService {

    static FileHandler fileHandler;

    /**
     * Runs the Logging Server.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Mediator.suppressLoggingService(); // Make sure logging calls don't send to us!
        renameOldLogFile();
        try {
            fileHandler = new FileHandler("oec.log");
            fileHandler.setLevel(Level.INFO); // Only log INFO and higher to disk.
        } catch (Exception ex) {
        }
        LoggingServer ls = new LoggingServer();
        Mediator.registerCallback(ls);
        Mediator m = new Mediator();
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Renames any former log file.
     * Also removes any former lock file.
     */
    public static void renameOldLogFile() {
        File oldLockFile = new File("oec.log.lck");
        if (oldLockFile.exists()) {
            oldLockFile.delete();
        }
        File oldFile = new File("oec.log");
        if (oldFile.exists()) {
            for (int i = 1;; i++) {
                File backupFile = new File("oec.backup." + i + ".log");
                if (!backupFile.exists()) {
                    oldFile.renameTo(backupFile);
                    break;
                }
            }
        }
    }

    /**
     * Receives a log entry and logs it.
     * 
     * @param requestTypeId Request type, should be LOG_ENTRY.
     * @param requestData Request data, in a LogEntry object.
     * @return null (Log Entry requests have no return data.)
     */
    @Override
    public Object getData(int requestTypeId, Object requestData) {
        if (requestTypeId == RequestTypeId.LOG_ENTRY
                && requestData.getClass() == LogEntry.class) {
            LogEntry le = (LogEntry) requestData;
            Logger logger = Logger.getLogger(le.getClassName());
            logger.addHandler(fileHandler);
            Level level = Level.parse(le.getSeverity());
            logger.log(level, "++++ {0} {1} {2}", new Object[]{le.getDateTime().toString(), le.getInstance(), le.getMessage()});
            // fileHandler.flush();
        }
        return null; // No object returned.
    }
}
