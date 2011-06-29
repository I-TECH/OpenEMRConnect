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

import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;

/**  
 * The RequestDispatcher class provides a unified way of dispatching, 
 * {@link RequestDispatcher.DispatchType#FIND}, 
 * {@link RequestDispatcher.DispatchType#CREATE} and 
 * {@link RequestDispatcher.DispatchType#MODIFY} requests to Person Indices 
 * (MPI/LPI). See {@link RequestTypeId} in oeclib.
 * <p>
 * Depending on the {@link RequestDispatcher.DispatchType} and 
 * {@link RequestDispatcher.TargetIndex} specified, it creates and spawns the 
 * necessary number of {@link RequestDispatchingThread}s to satisfy the operation.
 * 
 * @author Gitahi Ng'ang'a
 */
public class RequestDispatcher {

    public class TargetIndex {

        /**
         * Signals the RequestDispatcher to dispatch a request to both the 
         * Master Person Index and the Local Person Index. See 
         * {@link RequestTypeId} in oeclib.
         */
        public static final int BOTH = 0;
        /**
         * Signals the RequestDispatcher to dispatch a request to the Master 
         * Person Index only. See {@link RequestTypeId} in oeclib.
         */
        public static final int MPI = 1;
        /**
         * Signals the RequestDispatcher to dispatch a request to the Local 
         * Person Index only. See {@link RequestTypeId} in oeclib.
         */
        public static final int LPI = 2;
    }

    public class DispatchType {

        /**
         * Signals the RequestDispatcher to dispatch a find person request to a 
         * Person Index or Indices. See {@link RequestTypeId} in oeclib.
         * <p>
         * This value makes no assumptions about the index or indices to contact.
         * That must be specified separately by setting the value {@link TargetIndex}
         */
        public static final int FIND = 1;
        /**
         * Signals the RequestDispatcher to dispatch a create person request to a 
         * Person Index or Indices. See {@link RequestTypeId} in oeclib.
         * <p>
         * This value makes no assumptions about the index or indices to contact.
         * That must be specified separately by setting the value {@link TargetIndex}
         */
        public static final int CREATE = 2;
        /**
         * Signals the RequestDispatcher to dispatch a modify person request to a 
         * Person Index or Indices. See {@link RequestTypeId} in oeclib.
         * <p>
         * This value makes no assumptions about the index or indices to contact.
         * That must be specified separately by setting the value {@link TargetIndex}
         */
        public static final int MODIFY = 3;
    }
    private static Mediator mediator = new Mediator();

    /**
     * Dispatches a request of a specific type to the specified indices.
     * <p>
     * If the dispatchType specified is {@link RequestDispatcher.DispatchType#FIND}, 
     * any {@link RequestDispatchingThread}s spawned join the current thread and 
     * cause this method to wait until they return.
     * <p>
     * If the dispatchType specified is {@link RequestDispatcher.DispatchType#CREATE} 
     * or {@link RequestDispatcher.DispatchType#MODIFY}, any 
     * {@link RequestDispatchingThread}s spawned do not join the current thread 
     * and this method returns immediately.
     * <p>
     * The number of {@link RequestDispatchingThread}s spawned depends on the
     * {@link TargetIndex} specified. Only one is spawned for {@link TargetIndex#LPI}
     * or {@link TargetIndex#MPI} and two for {@link TargetIndex#BOTH}.
     *
     * @param personWrapper object containing data for the request
     * @param mpiRequestResult object to contain the MPI results of the request
     * if any
     * @param lpiRequestResult object to contain the LPI results of the request
     * if any
     * @param dispatchType the general type of request to be dispatched
     * @param targetIndex the Person Index (Indices) to be contacted
     */
    public static void dispatch(PersonWrapper personWrapper,
            RequestResult mpiRequestResult, RequestResult lpiRequestResult,
            int dispatchType, int targetIndex) {
        if (targetIndex == TargetIndex.BOTH) {
            if (dispatchType == DispatchType.FIND) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.FIND_PERSON_MPI, mpiRequestResult), true);
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.FIND_PERSON_LPI, lpiRequestResult), true);
            } else if (dispatchType == DispatchType.CREATE) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.CREATE_PERSON_MPI, mpiRequestResult), false);
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.CREATE_PERSON_LPI, lpiRequestResult), false);
            } else if (dispatchType == DispatchType.MODIFY) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.MODIFY_PERSON_MPI, mpiRequestResult), false);
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.MODIFY_PERSON_LPI, lpiRequestResult), false);
            }
        } else if (targetIndex == TargetIndex.MPI) {
            if (dispatchType == DispatchType.FIND) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.FIND_PERSON_MPI, mpiRequestResult), true);
            } else if (dispatchType == DispatchType.CREATE) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.CREATE_PERSON_MPI, mpiRequestResult), false);
            } else if (dispatchType == DispatchType.MODIFY) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.MODIFY_PERSON_MPI, mpiRequestResult), false);
            }
        } else if (targetIndex == TargetIndex.LPI) {
            if (dispatchType == DispatchType.FIND) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.FIND_PERSON_LPI, lpiRequestResult), true);
            } else if (dispatchType == DispatchType.CREATE) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.CREATE_PERSON_LPI, lpiRequestResult), false);
            } else if (dispatchType == DispatchType.MODIFY) {
                spawn(new RequestDispatchingThread(
                        mediator, personWrapper, RequestTypeId.MODIFY_PERSON_LPI, lpiRequestResult), true);
            }
        }
    }

    /*
     * Spawn a new thread and specify whether or not it should join
     * the current one.
     */
    private static void spawn(RequestDispatchingThread rdt, boolean join) {
        rdt.start();
        if (join) {
            try {
                rdt.join();
            } catch (Exception ex) {
                Logger.getLogger(RequestDispatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
