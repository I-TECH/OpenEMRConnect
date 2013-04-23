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

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.oecsm.gui.DaemonFrame;

/**
 * @date Sep 14, 2010
 *
 * @author jgitahi
 */
public class DaemonManager {

    private static DaemonFrame daemonFrame = new DaemonFrame();
    private static Daemon daemon = null;

    public static void main(String[] args) {
        //Initialize mediator to set up logging and http facilities
        new Mediator();
        DaemonManager daemonManager = new DaemonManager();
        DaemonManager.startDaemon();
        daemonFrame.getOutputTextArea().append("OECSM Daemon started.\n");
        daemonManager.minimizeToTray();
    }

    private static void startDaemon() {
        int pollingTime;
        String timeOfDay;
        try {
            String pollingMethod = Mediator.getProperty("scheduler.method");
            if (pollingMethod.equalsIgnoreCase("interval")) {
                pollingTime = Integer.parseInt(Mediator.getProperty("scheduler.interval"));
                daemon = new Daemon(pollingTime, daemonFrame);
            } else if (pollingMethod.equalsIgnoreCase("timeOfDay")) {
                timeOfDay = Mediator.getProperty("scheduler.timeOfDay");
                daemon = new Daemon(timeOfDay, daemonFrame);
            }
            daemon.setDaemon(true);
            daemon.start();
        } catch (Exception ex) {
            daemonFrame.getOutputTextArea().append(ex.toString() + "\n");
        }
    }

    public static void restartDaemon(int snooze) {
        try {
            if (!daemon.isAlive()) {
                startDaemon();
            }
            daemon.setInterval(snooze);
        } catch (Exception ex) {
            daemonFrame.getOutputTextArea().append(ex.getMessage() + "\n");
        }
    }

    public static void stopDaemon() {
        daemon.stop();
    }

    private void minimizeToTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("tray.gif"), "OECSM", popup);//new TrayIcon(createImage("images/bulb.gif", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem showItem = new MenuItem("Show");
        MenuItem hideItem = new MenuItem("Hide");
        MenuItem exitItem = new MenuItem("Exit");

        showItem.addActionListener(showListener);
        hideItem.addActionListener(hideListener);
        exitItem.addActionListener(exitListener);

        popup.add(showItem);
        popup.addSeparator();
        popup.add(hideItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.out.println(ex.toString() + " TrayIcon could not be added.");
        }
    }
    ActionListener exitListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };
    ActionListener showListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            daemonFrame.setVisible(true);
        }
    };
    ActionListener hideListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            daemonFrame.setVisible(false);
        }
    };
}
