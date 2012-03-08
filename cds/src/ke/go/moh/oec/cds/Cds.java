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
package ke.go.moh.oec.cds;

import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.IService;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.Work;
import ke.go.moh.oec.lib.Mediator;

/**
 * By calling up the cdsHelper this class saves changes to the cds_store (clinical document store) database.
 * @author Brian Wakhutu
 * @author Gitahi Ng'ang'a
 */
public class Cds implements IService {

    private final CdsHelper cdsHelper = new CdsHelper(Main.getMediator());

    public Cds() {
        final String driverName = Mediator.getProperty("CDS.driver");
        try {
            Class.forName(driverName).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Can''t load JDBC driver " + driverName, ex);
            System.exit(1);
        }
    }

    @Override
    public Object getData(int requestTypeId, Object requestData) {
        if (requestData != null) {
            switch (requestTypeId) {
                case RequestTypeId.NOTIFY_PERSON_CHANGED:
                    cdsHelper.processNotifyPersonChanged((PersonRequest) requestData);
                    break;
                case RequestTypeId.GET_WORK:
                    cdsHelper.processGetWork((Work) requestData);
                    break;
                case RequestTypeId.REASSIGN_WORK:
                    cdsHelper.processReassignWork((Work) requestData);
                    break;
                case RequestTypeId.WORK_DONE:
                    cdsHelper.processWorkDone((Work) requestData);
                    break;
                default:
                    Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                            "getData() called with unepxected requestTypeId {0}", requestTypeId);
                    break;
            }
        } else {
            Mediator.getLogger(Cds.class.getName()).log(Level.SEVERE, "Null requestData received by CDS. "
                    + "No processing done!");
        }
        return null;
    }
}
