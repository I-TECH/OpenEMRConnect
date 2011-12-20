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
package ke.go.moh.oec.lib;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import ke.go.moh.oec.LogEntry;
import ke.go.moh.oec.RequestTypeId;

/**
 * Sends messages to the central logging server. This class extends
 * java.util.logging.Handler to provide a custom handler within OpenEMRConnect.
 * Any messages logged by an OEC application of levels INFO, WARNING and ERROR
 * will be sent to the OEC Logging Server.
 * 
 * @author Jim Grace
 */
public class LoggingHandler extends Handler {

    Mediator mediator;

    LoggingHandler(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Publishes a LogRecord.
     * <p>
     * The logging request was made initially to a Logger object, which initialized the LogRecord and forwarded it here.
     * We send the record off to the Logging Server (if it is important enough.)
     * <p>
     * Programming note: we want to avoid recursion if anything is logged while we are sending
     * the log record to the Logging Server. In that case, we don't want to try
     * sending this message to the Logging Server also (stack overflow!)
     * To prevent recursion, we look at the stack to see if we are on it.
     * More precisely, we start from the third element of the stack -- because
     * the first element is always from the Thread.getStackTrace() method, and
     * the second elements is always from us, as the caller of Thread.getStackTrace().
     * But if we appear again at a higher level of the stack trace, that means
     * that we have called ourselves recursively. If this happens we will not
     * try to send the message to the Logging Server.
     * 
     * @param record description of the log event
     */
    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record) && record.getLevel().intValue() >= Level.INFO.intValue()) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 2; i < stackTrace.length; i++) {
                StackTraceElement e = stackTrace[i];
                if (e.getClassName().equals(LoggingHandler.class.getName())) {
                    return;
                }
            }
            Formatter formatter = getFormatter();
            String message = formatter.format(record);
            LogEntry le = new LogEntry();
            le.setDateTime(new Date());
            le.setSeverity(record.getLevel().getName());
            le.setClassName(record.getSourceClassName());
            le.setMessage(message);
            mediator.getData(RequestTypeId.LOG_ENTRY, le);
        }
    }

    /**
     * Flushes any buffered output.
     */
    @Override
    public void flush() {
        // Nothing needs to be done here.
    }

    /**
     * Closes the Handler and free all associated resources.
     * 
     * @throws SecurityException 
     */
    @Override
    public void close() throws SecurityException {
        // Nothing needs to be done here.
    }
}
