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
package ke.go.moh.oec;

import java.util.Date;

/**
 * Describe a log entry. This contains data that will be sent through
 * the network from an instance using the OpenEMRConnect library to the
 * logging server.
 *
 * @author Jim Grace
 */
public class LogEntry {

    /**
     * Log entry severity: "ERROR", "WARN", "INFO", "DEBUG" or "TRACE".
     * <p>
     * Note that in practice only ERROR, WARN and INFO will be used in this
     * class, because DEBUG and TRACE are not sent through the network.
     */
    private String severity;
    /** Date and time at which the log entry was made. */
    private Date dateTime;
    /** Log entry message */
    private String message;
    /** Name of the class making the log entry. */
    private String className;
    /**
     * Name of the instance (running program) making the log entry.
     * For example, "Siaya TB Reception".
     */
    private String instanceName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
