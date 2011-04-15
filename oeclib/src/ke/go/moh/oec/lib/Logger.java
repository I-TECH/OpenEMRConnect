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

import ke.go.moh.oec.LogEntry;
import java.util.logging.Level;

import ke.go.moh.oec.RequestTypeId;
import java.util.Date;
//import org.apache.log4j.Logger;

/**
 * [Gitau to provide a description.]
 *
 * @author John Gitau
 */
public class Logger {

    /**
     * [Gitau to complete javadoc for all methods.]
     *
     * @param className
     * @param msg
     */
    public static void error(String className, String msg) {
        java.util.logging.Logger.getLogger(className).log(Level.SEVERE, msg);
        sendLoggerMessage("ERROR", className, msg, null);
    }

    public static void error(String className, String msg, Throwable e) {
        java.util.logging.Logger.getLogger(className).log(Level.SEVERE, msg, e);
        sendLoggerMessage("ERROR", className, msg, e);
    }

    public static void warn(String className, String msg) {
        java.util.logging.Logger.getLogger(className).log(Level.WARNING, msg);
        sendLoggerMessage("WARN", className, msg, null);
    }

    public static void warn(String className, String msg, Throwable e) {
        java.util.logging.Logger.getLogger(className).log(Level.WARNING, msg, e);
        sendLoggerMessage("WARN", className, msg, e);
    }

    public static void info(String className, String msg) {
        java.util.logging.Logger.getLogger(className).log(Level.INFO, msg);
        sendLoggerMessage("INFO", className, msg, null);
    }

    public static void info(String className, String msg, Throwable e) {
        java.util.logging.Logger.getLogger(className).log(Level.INFO, msg, e);
        sendLoggerMessage("INFO", className, msg, e);
    }

    public static void debug(String className, String msg) {
        java.util.logging.Logger.getLogger(className).log(Level.FINE, msg);
    }

    public static void debug(String className, String msg, Throwable e) {
        java.util.logging.Logger.getLogger(className).log(Level.FINE, msg, e);
    }

    public static void trace(String className, String msg) {
        java.util.logging.Logger.getLogger(className).log(Level.FINEST, msg);
    }

    public static void trace(String className, String msg, Throwable e) {
        java.util.logging.Logger.getLogger(className).log(Level.FINEST, msg, e);
    }

    private static void sendLoggerMessage(String severity, String className, String msg, Throwable e) {
        LogEntry logEntry = new LogEntry();
        logEntry.setClassName(className);
        logEntry.setDateTime(new Date());
        logEntry.setSeverity(severity);
        logEntry.setInstanceName(Mediator.getProperty("Instance.Name"));
        String m = msg;

        if (e != null) {
            StackTraceElement[] s = e.getStackTrace();
            int levelCount = s.length;
            if (levelCount > 3) {
                levelCount = 3;
            }
            for (int i = 0; i < levelCount; i++) {
                m = m + ";  " + s[i].getClassName() + "." + s[i].getMethodName() + ", line " + s[i].getLineNumber();
            }
        }
        logEntry.setMessage(m);

        Mediator mediator = new Mediator();
        mediator.getData(RequestTypeId.LOG_ENTRY, (Object) logEntry);
    }
}
