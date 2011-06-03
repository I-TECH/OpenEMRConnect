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
package ke.go.moh.oec.reception.domain;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import ke.go.moh.oec.Fingerprint;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class Session {

    public enum CLIENT_TYPE {

        ENROLLED,
        VISITOR,
        NEW,
        TRANSFER_IN
    }
    private CLIENT_TYPE clientType;
    private BasicSearchParameters basicSearchParameters = new BasicSearchParameters();
    private ExtendedSearchParameters extendedSearchParameters = new ExtendedSearchParameters();
    private BufferedImage currentFingerprintImage = null;

    public Session() {
    }

    public Session(CLIENT_TYPE clientType) {
        this.clientType = clientType;
    }

    public BasicSearchParameters getBasicSearchParameters() {
        return basicSearchParameters;
    }

    public void setBasicSearchParameters(BasicSearchParameters basicSearchParameters) {
        this.basicSearchParameters = basicSearchParameters;
    }

    public CLIENT_TYPE getClientType() {
        return clientType;
    }

    public void setClientType(CLIENT_TYPE clientType) {
        this.clientType = clientType;
    }

    public ExtendedSearchParameters getExtendedSearchParameters() {
        return extendedSearchParameters;
    }

    public void setExtendedSearchParameters(ExtendedSearchParameters extendedSearchParameters) {
        this.extendedSearchParameters = extendedSearchParameters;
    }

    public BufferedImage getCurrentFingerprintImage() {
        return currentFingerprintImage;
    }

    public void addImagedFingerprint(ImagedFingerprint imagedFingerprint) {
        if (basicSearchParameters.getFingerprintList() == null) {
            basicSearchParameters.setFingerprintList(new ArrayList<Fingerprint>());
        }
        basicSearchParameters.getFingerprintList().add(imagedFingerprint.getFingerprint());
        currentFingerprintImage = imagedFingerprint.getImage();
    }
}
