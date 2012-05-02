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
import java.util.logging.Level;
import java.util.logging.Logger;
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
        FindPersonResponder fpr = new FindPersonResponder();
        Mediator.registerCallback(fpr);
        String driverName = Mediator.getProperty("Shadow.driver");
        try {
            Class.forName(driverName).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(KisumuHdss.class.getName()).log(Level.SEVERE,
                    "Can''t load JDBC driver " + driverName, ex);
            System.exit(1);
        }

        Updater updater = new Updater();
        while (true) {
            try {
                updater.updateAllTransactions();
                if (1 > 2) {
                    throw new SQLException();
                }
                Thread.sleep(10 * 1000);
            } catch (SQLException ex) {
                Logger.getLogger(KisumuHdss.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(KisumuHdss.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
