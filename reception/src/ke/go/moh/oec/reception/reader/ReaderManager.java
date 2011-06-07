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
/*
 * FingerprintDialog.java
 *
 * Created on May 26, 2011, 9:30:59 AM
 */
package ke.go.moh.oec.reception.reader;

import com.griaule.grfingerjava.FingerprintImage;
import com.griaule.grfingerjava.GrFingerJava;
import com.griaule.grfingerjava.GrFingerJavaException;
import com.griaule.grfingerjava.IFingerEventListener;
import com.griaule.grfingerjava.IImageEventListener;
import com.griaule.grfingerjava.IStatusEventListener;
import com.griaule.grfingerjava.MatchingContext;
import com.griaule.grfingerjava.Template;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import ke.go.moh.oec.reception.gui.FingerprintDialog;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class ReaderManager implements IStatusEventListener, IFingerEventListener, IImageEventListener {

    private MatchingContext matchingContext;
    private FingerprintDialog fingerprintDialog;
    private boolean autoExtract = true;
    private boolean autoIdentify = false;
    private FingerprintImage fingerprintImage;
    private Template template;

    public ReaderManager(FingerprintDialog fingerprintDialog) throws ClassNotFoundException, GrFingerJavaException {
        this.fingerprintDialog = fingerprintDialog;
        initialize();
    }

    public ReaderManager(FingerprintDialog fingerprintDialog, boolean autoExtract, boolean autoIdentify) throws ClassNotFoundException, GrFingerJavaException {
        this.fingerprintDialog = fingerprintDialog;
        initialize();
        this.autoExtract = autoExtract;
        this.autoIdentify = autoIdentify;
    }

    public boolean isAutoExtract() {
        return autoExtract;
    }

    public void setAutoExtract(boolean autoExtract) {
        this.autoExtract = autoExtract;
    }

    public boolean isAutoIdentify() {
        return autoIdentify;
    }

    public void setAutoIdentify(boolean autoIdentify) {
        this.autoIdentify = autoIdentify;
    }

    public FingerprintImage getFingerprintImage() {
        return fingerprintImage;
    }

    public void setFingerprintImage(FingerprintImage fingerprintImage) {
        this.fingerprintImage = fingerprintImage;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    private void initialize() throws GrFingerJavaException {
        try {
            matchingContext = new MatchingContext();
            GrFingerJava.initializeCapture(this);
            fingerprintDialog.log("Reader initialized!");
        } catch (GrFingerJavaException ex) {
            fingerprintDialog.log(ex.getMessage());
            throw ex;
        }
    }

    public void destroy() throws GrFingerJavaException {
        GrFingerJava.finalizeCapture();
    }

    public void onSensorPlug(String sensorId) {
        try {
            fingerprintDialog.log(sensorId + " plugged.");
            GrFingerJava.startCapture(sensorId, this, this);
        } catch (GrFingerJavaException ex) {
            fingerprintDialog.log(ex.getMessage());
        }
    }

    public void onSensorUnplug(String sensorId) {
        try {
            fingerprintDialog.log(sensorId + " unplugged.");
            GrFingerJava.stopCapture(sensorId);
        } catch (GrFingerJavaException ex) {
            fingerprintDialog.log(ex.getMessage());
        }
    }

    public void onFingerDown(String string) {
        fingerprintDialog.log("Finger placed.");
    }

    public void onFingerUp(String string) {
        fingerprintDialog.log("Finger removed.");
    }

    public void onImageAcquired(String sensorId, FingerprintImage fingerprintImage) {
        fingerprintDialog.log("Image Captured!");
        this.fingerprintImage = fingerprintImage;
        fingerprintDialog.showImage(fingerprintImage);
        if (autoExtract) {
            extract();
        }
    }

    public String getFingerprintSDKVersion() throws GrFingerJavaException {
        return "Fingerprint SDK version " + GrFingerJava.getMajorVersion() + "." + GrFingerJava.getMinorVersion() + "\n"
                + "License type is '" + (GrFingerJava.getLicenseType() == GrFingerJava.GRFINGER_JAVA_FULL ? "Identification" : "Verification") + "'.";
    }

    public void saveImageToFile(File file, ImageWriterSpi spi) {
        try {
            ImageWriter writer = spi.createWriterInstance();
            ImageOutputStream output = ImageIO.createImageOutputStream(file);
            writer.setOutput(output);
            writer.write(fingerprintImage);
            output.close();
            writer.dispose();
        } catch (IOException e) {
            fingerprintDialog.log(e.toString());
        }
    }

    public void extract() {

        try {
            template = matchingContext.extract(fingerprintImage);
            String message = "";
            switch (template.getQuality()) {
                case Template.HIGH_QUALITY:
                    message = "High quality.";
                    break;
                case Template.MEDIUM_QUALITY:
                    message = "Medium quality.";
                    break;
                case Template.LOW_QUALITY:
                    message = "Low quality.";
                    break;
            }
            fingerprintDialog.showQuality(message);
            fingerprintDialog.showImage(GrFingerJava.getBiometricImage(template, fingerprintImage));
        } catch (GrFingerJavaException e) {
            fingerprintDialog.log(e.getMessage());
        }
    }

    public static void setFingerprintSDKNativeDirectory(String nativeDirectory) {
        try {
            File directory = new File(nativeDirectory);
            GrFingerJava.setNativeLibrariesDirectory(directory);
            GrFingerJava.setLicenseDirectory(directory);
        } catch (GrFingerJavaException ex) {
            Logger.getLogger(ReaderManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ReaderManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
