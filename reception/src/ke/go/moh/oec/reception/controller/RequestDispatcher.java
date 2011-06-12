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
package ke.go.moh.oec.reception.controller;

/**
 *
 * @author Gitahi Ng'ang'a
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.data.RequestParameters;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.data.TargetIndex;

public class RequestDispatcher {

    public static final int FIND = 1;
    public static final int CREATE = 2;
    public static final int MODIFY = 3;
    private static Mediator mediator = new Mediator();

    public static void dispatchRequest(RequestParameters requestParameters,
            RequestResult mpiRequestResult, RequestResult lpiRequestResult, int dispatchType, int targetIndex) {
        RequestDispatchingThread mpiThread = null;
        RequestDispatchingThread lpiThread = null;
        if (targetIndex == TargetIndex.BOTH) {
            if (dispatchType == FIND) {
                mpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.FIND_PERSON_MPI, mpiRequestResult);
                lpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.FIND_PERSON_LPI, lpiRequestResult);
                mpiThread.start();
                lpiThread.start();
                try {
                    mpiThread.join();
                    lpiThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(RequestDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (dispatchType == CREATE) {
                mpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.CREATE_PERSON_MPI, mpiRequestResult);
                lpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.CREATE_PERSON_LPI, lpiRequestResult);
                mpiThread.start();
                lpiThread.start();
            } else if (dispatchType == MODIFY) {
                mpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.MODIFY_PERSON_MPI, mpiRequestResult);
                lpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.MODIFY_PERSON_LPI, lpiRequestResult);
                mpiThread.start();
                lpiThread.start();
            } else if (targetIndex == TargetIndex.MPI) {
                if (dispatchType == FIND) {
                    mpiThread = new RequestDispatchingThread(
                            mediator, requestParameters, RequestTypeId.FIND_PERSON_MPI, mpiRequestResult);
                    mpiThread.start();
                } else if (dispatchType == CREATE) {
                    mpiThread = new RequestDispatchingThread(
                            mediator, requestParameters, RequestTypeId.CREATE_PERSON_MPI, mpiRequestResult);
                    mpiThread.start();
                } else if (dispatchType == MODIFY) {
                    mpiThread = new RequestDispatchingThread(
                            mediator, requestParameters, RequestTypeId.MODIFY_PERSON_MPI, mpiRequestResult);
                    mpiThread.start();
                }
            }
        } else if (targetIndex == TargetIndex.LPI) {
            if (dispatchType == FIND) {
                lpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.FIND_PERSON_LPI, lpiRequestResult);
                lpiThread.start();
                try {
                    lpiThread.join();
                } catch (Exception ex) {
                    Logger.getLogger(RequestDispatcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (dispatchType == CREATE) {
                lpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.CREATE_PERSON_LPI, lpiRequestResult);
                lpiThread.start();
            } else if (dispatchType == MODIFY) {
                lpiThread = new RequestDispatchingThread(
                        mediator, requestParameters, RequestTypeId.MODIFY_PERSON_LPI, lpiRequestResult);
                lpiThread.start();
            }
        }
    }
}
