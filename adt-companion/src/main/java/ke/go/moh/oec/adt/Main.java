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
package ke.go.moh.oec.adt;

import java.util.logging.Level;
import ke.go.moh.oec.adt.controller.ResourceManager;
import ke.go.moh.oec.lib.Mediator;

/**
 * @date Apr 26, 2012
 *
 * @author Gitahi Ng'ang'a
 */
public class Main {

    public static void main(String[] args) {
        try {
            //Initialize Mediator so that it sets up logging facilities.
            new Mediator();
            String method = ResourceManager.getSetting("scheduler.method");
            long interval = Integer.parseInt(ResourceManager.getSetting("scheduler.interval"));
            String timeOfDay = ResourceManager.getSetting("scheduler.timeOfDay");
            long lookback = Integer.parseInt(ResourceManager.getSetting("scheduler.lookback"));
            Mediator.getLogger(Main.class.getName()).log(Level.INFO, "Starting service...");
            new Thread(new Daemon(method, interval, timeOfDay, lookback)).start();
        } catch (Exception ex) {
            Mediator.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            System.exit(1);
        }
    }
}
