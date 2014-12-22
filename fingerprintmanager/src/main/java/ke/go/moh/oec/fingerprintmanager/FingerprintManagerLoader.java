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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a common mechanism to instantiate any class that implements
 * the {@link FingerprintManager} interface.
 * 
 * @author Gitahi Ng'ang'a
 */
public class FingerprintManagerLoader {

    /**
     * Returns an instance of a {@link FingerprintManager} implementation. The
     * {@link FingerprintManager} implementation must provide a public no arguments
     * constructor for his method to work.
     * 
     * @param className the fully qualified class name of the {@link FingerprintManager} 
     * implementation
     * 
     * @return an instance of the {@link FingerprintManager} implementation whose class
     * name was supplied.
     * 
     * @throws MissingFingerprintManagerImpException  if the className supplied as 
     * an implementation of FingerprintManager is not found.
     * 
     */
    public static FingerprintManager getFingerprintManager(String className) throws MissingFingerprintManagerImpException {
        FingerprintManager fingerprintManager = null;
        try {
            Class<FingerprintManager> fingerprintManagerClass = (Class<FingerprintManager>) Class.forName(className);
            fingerprintManager = fingerprintManagerClass.newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FingerprintManagerLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new MissingFingerprintManagerImpException("Class \"" + className + "\" not found!");
        } catch (InstantiationException ex) {
            Logger.getLogger(FingerprintManagerLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new MissingFingerprintManagerImpException(className + "\" could not be instantiated!");
        } catch (IllegalAccessException ex) {
            Logger.getLogger(FingerprintManagerLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new MissingFingerprintManagerImpException("Access to " + className + " denied!");
        }catch (Exception ex) {
            Logger.getLogger(FingerprintManagerLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new MissingFingerprintManagerImpException("A general exception occured when try to "
                    + "instanciate the class " + className + ".");
        }
        return fingerprintManager;
    }

    /*
     * Prevents this class from being instantiated. Instantiating this class
     * would provide no value since it has no instance methods.
     */
    private FingerprintManagerLoader() {
    }
}
