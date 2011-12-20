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

import ke.go.moh.oec.reception.data.RequestResult;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.lib.Mediator;

/**
 * The RequestDispatchingThread is responsible for asynchronously forwarding 
 * client requests to servers.
 * <p>
 * One instance of this class should be started for each unique client request.
 * 
 * @author Gitahi Ng'ang'a
 */
final class RequestDispatchingThread extends Thread {

    /**
     * The Mediator instance used to satisfy a client request
     */
    private final Mediator mediator;
    /**
     * The {@link RequestTypeId} for the request to be dispatched.
     */
    private final int requestTypeId;
    /**
     * The {@link PersonWrapper} object containing data for the request to be
     * dispatched.
     */
    private final Object requestData;
    /**
     * The {@link RequestResult} object to contain the response data for the 
     * request to be dispatched.
     */
    private RequestResult requestResult;
    /**
     * A variable signaling whether a server response should be awaited. It is
     * always true if {@link  RequestDispatchingThread#requestResult} is not null
     * and false otherwise.
     */
    private final boolean waitForResponse;

    /**
     * This constructor is used when a synchronous server response is expected for 
     * the request to be dispatched. 
     * 
     * A server response will only be awaited if the value of 
     * {@link  RequestDispatchingThread#requestResult} is not null.
     * 
     * @param mediator Mediator to be used to satisfy this request
     * @param requestTypeId {@link RequestTypeId} of the request to be dispatched
     * @param requestData Object containing request data
     * @param requestResult Object to contain response data
     */
    RequestDispatchingThread(Mediator mediator, int requestTypeId,
            Object requestData, RequestResult requestResult) {
        this.mediator = mediator;
        this.requestTypeId = requestTypeId;
        this.requestData = requestData;
        this.requestResult = requestResult;
        waitForResponse = (requestResult != null);
    }

    /**
     * This constructor is used when no server response is expected for the 
     * request to be dispatched.  It always sets the value of 
     * {@link RequestDispatchingThread#waitForResponse} to false.
     * 
     * @param mediator Mediator to be used to satisfy this request
     * @param requestTypeId {@link RequestTypeId} of the request to be dispatched
     * @param requestData Object containing request data
     */
    RequestDispatchingThread(Mediator mediator, int requestTypeId,
            Object requestData) {
        this.mediator = mediator;
        this.requestTypeId = requestTypeId;
        this.requestData = requestData;
        waitForResponse = (requestResult != null);
    }

    /**
     * Asynchronously forwards a request to a server. If {@link RequestDispatchingThread#requestResult}
     * is not null, a response will be awaited. Otherwise the method will return immediately.
     */
    @Override
    public void run() {
        if (waitForResponse) {
            PersonResponse personResponse = (PersonResponse) mediator.getData(requestTypeId, requestData);
            if (personResponse != null) {
                if (personResponse.isSuccessful()) {
                    List<Person> personList = personResponse.getPersonList();
                    if (personList != null) {
                        requestResult.setData(personList);
                    } else {
                        requestResult.setData(new ArrayList<Person>());
                    }
                    requestResult.setSuccessful(true);
                }
            }
        } else {
            mediator.getData(requestTypeId, requestData);
        }
    }
}
