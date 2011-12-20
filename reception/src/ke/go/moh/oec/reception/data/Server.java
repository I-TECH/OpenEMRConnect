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

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class Server {

    /**
     * Signals the RequestDispatcher to dispatch a request to both the 
     * Master Person Index and the Local Person Index. See {@link MessageTypeRegistry} in oeclib.
     */
    public static final int MPI_LPI = 1;
    /**
     * Signals the RequestDispatcher to dispatch a request to the Master 
     * Person Index. See {@link MessageTypeRegistry} in oeclib.
     */
    public static final int MPI = 2;
    /**
     * Signals the RequestDispatcher to dispatch a request to the Local 
     * Person Index. See {@link MessageTypeRegistry} in oeclib.
     */
    public static final int LPI = 3;
    /**
     * Signals the RequestDispatcher to dispatch a request to the Kisumu 
     * HDSS. See {@link MessageTypeRegistry} in oeclib.
     */
    public static final int KISUMU_HDSS = 4;
    /**
     * Signals the RequestDispatcher to dispatch a request to the CDS.
     * See {@link MessageTypeRegistry} in oeclib.
     */
    public static final int CDS = 5;
}
