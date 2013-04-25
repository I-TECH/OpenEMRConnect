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
package ke.go.moh.oec.kisumuhdss;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author EWere
 */
public class KisumuHdss {

    private static Mediator mediator = new Mediator();

    public static Mediator getMediator() {
        return mediator;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            FindPersonResponder fpr = new FindPersonResponder();
            Mediator.registerCallback(fpr);
            String driverName = Mediator.getProperty("Shadow.driver");
            String method = Mediator.getProperty("scheduler.method");
            long interval = Integer.parseInt(Mediator.getProperty("scheduler.interval"));
            String timeOfDay = Mediator.getProperty("scheduler.timeOfDay");
            Class.forName(driverName).newInstance();
            Updater updater = new Updater();
            try {
                if ("interval".equalsIgnoreCase(method)) {
                    while (true) {
                        Mediator.getLogger(KisumuHdss.class.getName()).log(Level.INFO, "Resuming update service.");
                        updater.updateAllTransactions();
                        Mediator.getLogger(KisumuHdss.class.getName()).log(Level.INFO, "Done updating!");
                        Mediator.getLogger(KisumuHdss.class.getName()).log(Level.INFO, "Suspending update service for {0} seconds.",
                                interval / 1000);
                        Thread.sleep(interval);
                    }
                } else {
                    DateFormat sdf = new SimpleDateFormat("HH:mm");
                    String currentTime = sdf.format(new java.util.Date());
                    while (true) {
                        if (currentTime.equalsIgnoreCase(timeOfDay)) {
                            Mediator.getLogger(KisumuHdss.class.getName()).log(Level.INFO, "Starting update service.");
                            updater.updateAllTransactions();
                            Mediator.getLogger(KisumuHdss.class.getName()).log(Level.INFO, "Done updating!");
                        }
                    }
                }
            } catch (SQLException ex) {
                Mediator.getLogger(KisumuHdss.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Mediator.getLogger(KisumuHdss.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception ex) {
            Mediator.getLogger(KisumuHdss.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
}
