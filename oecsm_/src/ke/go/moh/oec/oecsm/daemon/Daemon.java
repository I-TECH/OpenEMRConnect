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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.oecsm.data.LoggableTransaction;
import ke.go.moh.oec.oecsm.gui.DaemonFrame;
import ke.go.moh.oec.oecsm.sync.data.DataSynchronizer;
import ke.go.moh.oec.oecsm.logger.LoggableTransactionMiner;
import ke.go.moh.oec.oecsm.logger.XMLTransactionGenerator;
import ke.go.moh.oec.oecsm.sync.schema.SchemaSynchronizer;

/**
 * @date Aug 19, 2010
 *
 * @author JGitahi
 */
public class Daemon extends Thread {

    private int snooze;
    private DaemonFrame daemonFrame;
    private static Properties properties = null;

    public Daemon(int snooze, DaemonFrame daemonFrame) {
        this.snooze = snooze;
        this.daemonFrame = daemonFrame;
    }

    public int getSnooze() {
        return snooze;
    }

    public void setSnooze(int snooze) {
        this.snooze = snooze;
    }

    public void quit() {
        this.stop();
    }

    @Override
    public void run() {
        try {
            while (true) {
                new SchemaSynchronizer().synchronize();
                new DataSynchronizer().Synchronize();

                // T H I S    I S    L O G I C    F O R     X M L    L O G G I N G//

//                List<LoggableTransaction> transactionList = new LoggableTransactionMiner().generate();
//                String transactionXmlOutput = getProperty("transaction.xml.output");
//                if (transactionXmlOutput != null && transactionXmlOutput.equalsIgnoreCase("true")) {
//                    new XMLTransactionGenerator().generate(transactionList);
//                }

                // T H I S    I S    L O G I C    F O R     X M L    L O G G I N G//

                // TODO: Implement time of day scheduler method
                Thread.sleep(snooze);
            }
        } catch (Exception ex) {
            Logger.getLogger(Daemon.class.getName()).log(Level.SEVERE, null, ex);
            daemonFrame.getOutputTextArea().append(ex.getMessage() + "\n");
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
