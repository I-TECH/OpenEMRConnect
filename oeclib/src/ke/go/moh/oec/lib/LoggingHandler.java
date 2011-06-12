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
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import ke.go.moh.oec.LogEntry;
import ke.go.moh.oec.RequestTypeId;

/**
 *
 * @author Jim Grace
 */
public class LoggingHandler extends Handler {

    Mediator mediator;

    LoggingHandler(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            String message = getFormatter().format(record);
            LogEntry le = new LogEntry();
            le.setDateTime(new Date());
            le.setSeverity(record.getLevel().getName());
            le.setClassName(record.getSourceClassName());
            le.setMessage(message);
            mediator.getData(RequestTypeId.LOG_ENTRY, le);
        }
    }

    @Override
    public void flush() {
        // Nothing needs to be done here.
    }

    @Override
    public void close() throws SecurityException {
        // Nothing needs to be done here.
    }
}
