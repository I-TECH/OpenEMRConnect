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
package ke.go.moh.oec.reception.gui.helper;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * @author Scott Davis
 */
public class NotificationSoundPlayer {
    //sound variables

    private Clip soundClip;
    private AudioInputStream audioInputStream;
    private File soundFile;
    //make sure this file path is correct if you'd like to hear a sound
    private String filePath = new File("notification.wav").getAbsolutePath();
    //timer variables for the sound
    private boolean ready = true;
    // TIME_DELAY is the time that must pass before letting the sound play again.
    private final int TIME_DELAY = 5000; // 5 sec 
    private static NotificationSoundPlayer instance;

    private NotificationSoundPlayer() {
        this.soundFile = new File(filePath);
        try {
            soundClip = AudioSystem.getClip();
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            soundClip.open(audioInputStream);
        } catch (Exception ex) {
            Logger.getLogger(MainViewHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        //start the timer
        soundTimer.start();
    }

    public static NotificationSoundPlayer getInstance() {
        if (instance == null) {
            instance = new NotificationSoundPlayer();
        }
        return instance;
    }

    public boolean play() {
        if (ready) {
            try //just incase the sound doesn't load correctly
            {    //we don't want it throwing errors
                soundClip.setFramePosition(0);
                soundClip.start();
                ready = false;
                soundTimer.restart();
            } catch (Exception ex) {
                Logger.getLogger(MainViewHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ready;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    private javax.swing.Action timerActionListener = new javax.swing.Action() {

        public Object getValue(String key) {
            return null;
        }

        public void putValue(String key, Object value) {
        }

        public void setEnabled(boolean b) {
        }

        public boolean isEnabled() {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void actionPerformed(ActionEvent e) {
            //this code is called whenever the timer expires
            // then the sound is ready to be played again
            ready = true;
        }
    };
    private javax.swing.Timer soundTimer = new javax.swing.Timer(TIME_DELAY, timerActionListener);
}
