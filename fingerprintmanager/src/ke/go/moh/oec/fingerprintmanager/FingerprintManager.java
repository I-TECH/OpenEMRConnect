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
package ke.go.moh.oec.fingerprintmanager;

import java.awt.image.BufferedImage;

/**
 * This interface specifies the contract between fingerprinting technology
 * SDKs and OpenEMRConnect software modules. Implementations of it must be
 * written to wrap any given SDK in order for them to plug seamlessly into
 * OpenEMRConnect software modules.
 * 
 * @author Gitahi Ng'ang'a
 */
public interface FingerprintManager {

    /**
     * An integer constant designating that the fingerprint captured was of high
     * quality
     */
    public final int HIGH_QUALITY = 3;
    /**
     * An integer constant designating that the fingerprint captured was of medium
     * quality
     */
    public final int MEDIUM_QUALITY = 2;
    /**
     * An integer constant designating that the fingerprint captured was of low
     * quality
     */
    public final int LOW_QUALITY = 1;
    /**
     * An integer constant designating that the fingerprint captured was of unknown
     * quality
     */
    public final int UNKNOWN_QUALITY = 0;
    
    /**
     * Called to release all resources held by the fingerprinting
     * SDK.
     * 
     * @throws FingerprintManagerException if the underlying SDK is unable to 
     * release resources successfully.
     */
    public void destroy() throws FingerprintManagerException;

    /**
     * Returns the {@link FingerprintManagerMetadata} of the underlying SDK.
     * 
     * @return the {@link FingerprintManagerMetadata} of the underlying SDK.
     */
    public FingerprintManagerMetadata getMetaData();

    /**
     * Returns the encoded fingerprint data as an array of raw bytes. The content
     * of this data is bound to be specific to the underlying SDK.
     * 
     * @return the encoded fingerprint data.
     */
    public byte[] getData();

    /**
     * Returns the quality of the fingerprint taken using the underlying SDK.
     * 
     * @return the fingerprint quality
     */
    public int getQuality();

    /**
     * Performs fingerprint identification by checking if the given reference 
     * template matches a previously prepared query template.
     * 
     * @param template the reference template
     * 
     * @return true if identified and false otherwise
     */
    public boolean identify(Object template);

    /**
     * Performs fingerprint verification by checking if the two given templates 
     * match.
     * 
     * @param template1 the first template
     * 
     * @param template2 the second template
     * 
     * @return true if the two templates match and false otherwise
     */
    public boolean verify(Object template1, Object template2);

    /**
     * Called when the SDK reports that the fingerprint hardware sensor has been
     * plugged.
     * 
     * @param sensorId a String identifying the hardware sensor.
     */
    public void onSensorPlug(String sensorId);

    /**
     * Called when the SDK reports that the fingerprint hardware sensor has been
     * unplugged.
     * 
     * @param sensorId a String identifying the hardware sensor.
     */
    public void onSensorUnplug(String sensorId);

    /**
     * Called when the SDK reports that a finger has been placed on the fingerprint
     * hardware sensor.
     * 
     * @param sensorId a String identifying the hardware sensor.
     */
    public void onFingerDown(String sensorId);

    /**
     * Called when the SDK reports that a finger has been removed from the fingerprint
     * hardware sensor.
     * 
     * @param sensorId a String identifying the hardware sensor.
     */
    public void onFingerUp(String sensorId);

    /**
     * Called when the SDK reports that a fingerprint image has been captured.
     * 
     * @param sensorId a String identifying the hardware sensor.
     * 
     * @param fingerprintImage  the image captured by the sensor.
     */
    public void onImageAcquired(String sensorId, BufferedImage fingerprintImage);

    /**
     * Sets the {@link FingerprintingComponent} used to display information 
     * produced by fingerprinting hardware on a GUI.
     * 
     * @param fingerprintingComponent The {@link FingerprintingComponent} to be used.
     */
    public void setFingerprintingComponent(FingerprintingComponent fingerprintingComponent);
}
