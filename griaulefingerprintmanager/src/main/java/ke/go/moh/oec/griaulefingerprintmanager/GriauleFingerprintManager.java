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
package ke.go.moh.oec.griaulefingerprintmanager;

import com.griaule.grfingerjava.FingerprintImage;
import com.griaule.grfingerjava.GrFingerJava;
import com.griaule.grfingerjava.GrFingerJavaException;
import com.griaule.grfingerjava.IFingerEventListener;
import com.griaule.grfingerjava.IImageEventListener;
import com.griaule.grfingerjava.IStatusEventListener;
import com.griaule.grfingerjava.MatchingContext;
import com.griaule.grfingerjava.Template;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.fingerprintmanager.FingerprintManager;
import ke.go.moh.oec.fingerprintmanager.FingerprintManagerException;
import ke.go.moh.oec.fingerprintmanager.FingerprintManagerMetadata;
import ke.go.moh.oec.fingerprintmanager.FingerprintingComponent;
import ke.go.moh.oec.fingerprintmanager.MissingFingerprintManagerImpException;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class GriauleFingerprintManager implements IFingerEventListener,
        IImageEventListener, IStatusEventListener, FingerprintManager {

    private MatchingContext matchingContext;
    private FingerprintingComponent fingerprintingComponent;
    private Template template;

    public GriauleFingerprintManager() throws MissingFingerprintManagerImpException {
        initialize();
    }

    @Override
    public void setFingerprintingComponent(FingerprintingComponent fingerprintingComponent) {
        this.fingerprintingComponent = fingerprintingComponent;
    }

    private void initialize() throws MissingFingerprintManagerImpException {
        try {
            String dir = System.getProperty("user.dir");
            GrFingerJava.setLicenseDirectory(new File(dir));
            GrFingerJava.installLicense("RXFAI-LOUMO-BHIIG-NNDIF");
            matchingContext = new MatchingContext();
            //TODO: Investigate why this line sometimes hangs
            GrFingerJava.initializeCapture(this);
            showMessage("Waiting for device.");
        } catch (GrFingerJavaException ex) {
            showMessage(ex.getMessage());
            throw new MissingFingerprintManagerImpException(ex.getMessage());
        }
    }

    @Override
    public void destroy() throws FingerprintManagerException {
        try {
            //TODO: Investigate why this line sometimes hangs
            GrFingerJava.finalizeCapture();
            showMessage("Disconnected from device.");
        } catch (GrFingerJavaException ex) {
            throw new FingerprintManagerException(ex);
        }
    }

    @Override
    public void onSensorPlug(String sensorId) {
        try {
            showMessage(sensorId + " plugged.");
            GrFingerJava.startCapture(sensorId, this, this);
        } catch (GrFingerJavaException ex) {
            showMessage(ex.getMessage());
        }
    }

    @Override
    public void onSensorUnplug(String sensorId) {
        try {
            showMessage(sensorId + " unplugged.");
            GrFingerJava.stopCapture(sensorId);
        } catch (GrFingerJavaException ex) {
            showMessage(ex.getMessage());
        }
    }

    @Override
    public void onFingerDown(String string) {
        showMessage("Finger placed.");
    }

    @Override
    public void onFingerUp(String string) {
        showMessage("Finger removed.");
    }

    @Override
    public void onImageAcquired(String sensorId, FingerprintImage fingerprintImage) {
        showMessage("Image Captured!");
        extract(fingerprintImage);
    }

    private String getFingerprintSDKVersion() {
        try {
            return "Fingerprint SDK version " + GrFingerJava.getMajorVersion() + "." + GrFingerJava.getMinorVersion() + "\n"
                    + "License type is '" + (GrFingerJava.getLicenseType() == GrFingerJava.GRFINGER_JAVA_FULL ? "Identification" : "Verification") + "'.";
        } catch (GrFingerJavaException ex) {
            Logger.getLogger(GriauleFingerprintManager.class.getName()).log(Level.SEVERE, null, ex);
            return "Fingerprint SDK version is unknown.";
        }
    }

    private void extract(FingerprintImage fingerprintImage) {
        try {
            template = matchingContext.extract(fingerprintImage);
            if (fingerprintingComponent != null) {
                fingerprintingComponent.showImage(fingerprintImage);
                fingerprintingComponent.showQuality(template.getQuality());
            }
        } catch (GrFingerJavaException e) {
            showMessage(e.getMessage());
        }
    }

    private boolean identify(Template template) {
        boolean match = false;
        try {
            match = matchingContext.identify(template);
        } catch (GrFingerJavaException ex) {
            Logger.getLogger(GriauleFingerprintManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GriauleFingerprintManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return match;
    }

    public static void setFingerprintSDKNativeDirectory(String nativeDirectory) {
        try {
            File directory = new File(nativeDirectory);
            GrFingerJava.setNativeLibrariesDirectory(directory);
            GrFingerJava.setLicenseDirectory(directory);
        } catch (GrFingerJavaException ex) {
            Logger.getLogger(GriauleFingerprintManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GriauleFingerprintManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showMessage(String message) {
        if (fingerprintingComponent != null) {
            fingerprintingComponent.showMessage(message);
        }
    }

    @Override
    public FingerprintManagerMetadata getMetaData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean identify(Object template) {
        return identify((Template) template);
    }

    @Override
    public boolean verify(Object template1, Object template2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onImageAcquired(String sensorId, BufferedImage fingerprintImage) {
        onImageAcquired(sensorId, null);
    }

    @Override
    public byte[] getData() {
        if (template != null) {
            return template.getData();
        }
        return null;
    }

    @Override
    public int getQuality() {
        if (template != null) {
            return template.getQuality();
        }
        return -1;
    }
}
