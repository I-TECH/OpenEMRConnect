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
package ke.go.moh.oec.oecsm.daemon;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.oecsm.exceptions.DriverNotFoundException;
import ke.go.moh.oec.oecsm.exceptions.InaccessibleConfigurationFileException;
import ke.go.moh.oec.oecsm.gui.DaemonFrame;
import ke.go.moh.oec.oecsm.sync.data.DataSynchronizer;
import ke.go.moh.oec.oecsm.sync.schema.SchemaSynchronizer;

/**
 * @date Aug 19, 2010
 *
 * @author Gitahi Ng'ang'a
 */
public class Daemon extends Thread {

    private final Method method;
    private int interval;
    private String timeOfDay;
    private DaemonFrame daemonFrame;
    private static Properties properties = null;

    private enum Method {

        INTERVAL,
        TIME_OF_DAY
    }

    public Daemon(int interval, DaemonFrame daemonFrame) {
        this.interval = interval;
        this.daemonFrame = daemonFrame;
        this.method = Method.INTERVAL;
    }

    public Daemon(String timeOfDay, DaemonFrame daemonFrame) {
        this.timeOfDay = timeOfDay;
        this.daemonFrame = daemonFrame;
        this.method = Method.TIME_OF_DAY;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int snooze) {
        this.interval = snooze;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public void quit() {
        this.stop();
    }

    @Override
    public void run() {
        int afterWorkPause = 60000;//pause to avoid redoing (potentially the same) work unnecessarily
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try {
            if (method == Method.INTERVAL) {
                while (true) {
                    work();
                    Thread.sleep(interval);
                }
            } else if (method == Method.TIME_OF_DAY) {
                while (true) {
                    String currentTime = dateFormat.format(new Date());
                    if (currentTime.equalsIgnoreCase(timeOfDay)) {
                        work();
                        Thread.sleep(afterWorkPause);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            daemonFrame.getOutputTextArea().append(ex.getMessage() + "\n");
        }
    }
    /*
     * Does the actual source and shadow database synchronization. If another synchronization
     * process is ongoing, a second one is not started.
     */

    private void work() throws InaccessibleConfigurationFileException, DriverNotFoundException, SQLException {
            new SchemaSynchronizer().synchronize();
            new DataSynchronizer().synchronize();
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public static String getProperty(String propertyName) {
        if (properties == null) {
            properties = new Properties();
            final String propertiesFileName = "oecsm.properties";
            File propFile = new File(propertiesFileName);
            String propFilePath = propFile.getAbsolutePath();
            try {
                FileInputStream fis = new FileInputStream(propFilePath);
                properties.load(fis);
            } catch (Exception e) {
                /*
                 * We somehow failed to open our default propoerties file.
                 * This should not happen. It should always be there.
                 */
                Logger.getLogger(DaemonManager.class.getName()).log(Level.SEVERE,
                        "getProperty() Can''t open ''{0}'' -- Please create the properties file if it doesn''t exist and then restart the app",
                        propFilePath);
                System.exit(1);
            }
        }
        return properties.getProperty(propertyName);
    }
}
