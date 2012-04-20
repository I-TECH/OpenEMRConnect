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
package ke.go.moh.oec.mpi;

import ke.go.moh.oec.mpi.match.FingerprintMatch;
import ke.go.moh.oec.mpi.list.PersonList;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.IService;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.mpi.list.SiteList;

/**
 * Provides Master Patient Index (or Local Patient Index) services.
 * 
 * @author Jim Grace
 */
public class Mpi implements IService {

    private SiteList siteList = new SiteList();
    private PersonList personList = null;
    private static int maxThreadCount = 0;

    /**
     * Starts up MPI processing. Load the database into memory so it can be
     * searched more quickly. Approximate string matching and fingerprint
     * matching require that search terms be matched against every database
     * entry.
     */
    public void initialize() {
        final String driverName = Mediator.getProperty("MPI.driver");
        try {
            Class.forName(driverName).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Can''t load JDBC driver " + driverName, ex);
            System.exit(1);
        }
        //
        // Load the site list in a separate thread from loding the person list.
        // This is done to speed up loading time.
        Thread loadSitesThread = new Thread(siteList);
        loadSitesThread.start();
        personList = new PersonList();
        personList.load();
        try {
            loadSitesThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE, null, ex);
        }
        personList.setSiteList(siteList); // PersonList will (also) need to know about this list.
    }

    /**
     * Gets the maximum thread count that may be used for matching.
     * This is dependent on the number of processors available to the Java Virtual Machine.
     * It may be further limited by the number of threads that may be used
     * for fingerprint matching.
     * 
     * @return the maximum number of threads that may be used for searching.
     */
    public static int getMaxThreadCount() {
        if (maxThreadCount == 0) {
            maxThreadCount = Runtime.getRuntime().availableProcessors();
            int fingerprintLimit = FingerprintMatch.maxThreadCount();
            if (fingerprintLimit > 0 && fingerprintLimit < maxThreadCount) {
                maxThreadCount = fingerprintLimit;
            }
            Mediator.getLogger(Mpi.class.getName()).log(Level.FINE, "Maximum thread count = {0}.", maxThreadCount);
        }
        return maxThreadCount;
    }

    /**
     * Process a MPI/LPI request from another application.
     * @param requestTypeId Type of request
     * @param requestData Data for the request
     * @return Return data.
     */
    public synchronized Object getData(int requestTypeId, Object requestData) {
        Object returnData = null;
        switch (requestTypeId) {
            case RequestTypeId.FIND_PERSON_MPI:
            case RequestTypeId.FIND_PERSON_LPI:
                Mediator.getLogger(Mpi.class.getName()).log(Level.FINE, "FindPerson");
                returnData = personList.find((PersonRequest) requestData);
                break;
            case RequestTypeId.CREATE_PERSON_MPI:
            case RequestTypeId.CREATE_PERSON_LPI:
                Mediator.getLogger(Mpi.class.getName()).log(Level.FINE, "CreatePerson");
                returnData = personList.create((PersonRequest) requestData);
                break;
            case RequestTypeId.MODIFY_PERSON_MPI:
            case RequestTypeId.MODIFY_PERSON_LPI:
                Mediator.getLogger(Mpi.class.getName()).log(Level.FINE, "ModifyPerson");
                returnData = personList.modify((PersonRequest) requestData);
                break;
            default:
                Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                        "getData() called with unepxected requestTypeId {0}", requestTypeId);
                break;
        }
        return returnData;
    }
}
