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
package ke.go.moh.oec.reception.data;

import java.awt.image.BufferedImage;
import ke.go.moh.oec.Fingerprint;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class ImagedFingerprint {

    private Fingerprint fingerprint;
    private BufferedImage image;
    private String quality = "Unknown quality.";
    private boolean sent = false;
    private boolean placeholder = false;

    public ImagedFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    public ImagedFingerprint(Fingerprint fingerprint, BufferedImage image, String quality) {
        this.fingerprint = fingerprint;
        this.image = image;
        this.quality = quality;
    }

    public ImagedFingerprint(BufferedImage image, boolean placeholder) {
        this.image = image;
        this.placeholder = placeholder;
    }

    public ImagedFingerprint(Fingerprint fingerprint, BufferedImage image, String quality, boolean sent) {
        this.fingerprint = fingerprint;
        this.image = image;
        this.quality = quality;
        this.sent = sent;
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImagedFingerprint other = (ImagedFingerprint) obj;
        if (this.fingerprint == null || other.fingerprint == null) {
            return false;
        }
        if (this.fingerprint.getFingerprintType() != other.fingerprint.getFingerprintType()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.fingerprint != null ? this.fingerprint.hashCode() : 0);
        return hash;
    }
}
