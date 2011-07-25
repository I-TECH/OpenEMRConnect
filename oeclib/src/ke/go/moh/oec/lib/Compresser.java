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
package ke.go.moh.oec.lib;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Compress and decompress XML strings for more efficient transmission.
 * 
 * @author Jim Grace
 */
public class Compresser {
    
    static void compress(Message m) {
        String xml = m.getXml();
        byte[] xmlBytes = null;
        try {
            xmlBytes = m.getXml().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Compresser.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] compressedXml = new byte[xmlBytes.length + 1000]; // Insure a minimum size.
        m.setCompressedXml(compressedXml);
        Deflater compresser = new Deflater();
        compresser.setInput(xmlBytes);
        compresser.finish();
        int compressedXmlLength = compresser.deflate(compressedXml);
        m.setCompressedXmlLength(compressedXmlLength);
    }
    
    static void decompress(Message m) {
        byte[] compressedXml = m.getCompressedXml();
        int compressedXmlLength = m.getCompressedXmlLength();
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressedXml, 0, compressedXmlLength);
        byte[] inflatedBytes = new byte[60000];
        int resultLength = 0;
        try {
            resultLength = decompresser.inflate(inflatedBytes);
        } catch (DataFormatException ex) {
            Logger.getLogger(HttpService.class.getName()).log(Level.SEVERE, null, ex);
        }
        decompresser.end();
        String xml = null;
        try {
            xml = new String(inflatedBytes, 0, resultLength, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Compresser.class.getName()).log(Level.SEVERE, null, ex);
        }
        m.setXml(xml);
    }
}
