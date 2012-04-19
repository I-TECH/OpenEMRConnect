/*
 * Copyright (C) 2011 International Training & Education Center for Health (I-TECH)
 * Contact information can be found at <http://www.go2itech.org/>
 *
 * This file is part of OpenEMRConnect.
 *
 * OpenEMRConnect is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenEMRConnect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenEMRConnect.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    /** Name of the instance (program) making the log entry. */
    private String instance;
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

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

}
