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
 * Defines any GUI component used to display fingerprint images taken using
 * an implementation of this api.
 * 
 * @author Gitahi Ng'ang'a
 */
public interface FingerprintingComponent {

    /**
     * Shows any messages from the fingerprinting api on the user interface.
     * 
     * @param message the message to display
     */
    void showMessage(String message);

        /**
     * Shows fingerprint image quality ratings from the fingerprinting api on the
     * user interface.
     * 
     * @param quality the quality rating to display
     */
    void showQuality(int quality);

    
        /**
     * Shows fingerprint images from the fingerprinting api on the user interface.
     * 
     * @param fingerprintImage the fingerprint image to display
     */
    void showImage(BufferedImage fingerprintImage);
}
