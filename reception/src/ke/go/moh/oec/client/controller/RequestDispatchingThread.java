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
package ke.go.moh.oec.client.controller;

import ke.go.moh.oec.client.data.RequestResult;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.lib.Mediator;

/**
 * The RequestDispatchingThread is responsible for asynchronously forwarding 
 * client requests.
 * <p>
 * One instance of this class is initialized and started by the {@link RequestDispatcher}
 * for each {@link RequestDispatcher.TargetIndex} specified.
 * 
 * @author Gitahi Ng'ang'a
 */
class RequestDispatchingThread extends Thread {

    private final Mediator mediator;
    private final PersonWrapper personWrapper;
    private final int requestTypeId;
    private RequestResult requestResult;

    public RequestDispatchingThread(Mediator mediator, PersonWrapper personWrapper,
            int requestTypeId, RequestResult requestResult) {
        this.mediator = mediator;
        this.personWrapper = personWrapper;
        this.requestTypeId = requestTypeId;
        this.requestResult = requestResult;
    }

    /**
     * Forwards the specified request to the specified Person Index and then 
     * waits for the response.
     */
    @Override
    public void run() {
        PersonRequest personRequest = new PersonRequest();
        PersonResponse personResponse = null;
        Person person = ((PersonWrapper) personWrapper).unwrap();
        personRequest.setPerson(person);
        personResponse = (PersonResponse) mediator.getData(requestTypeId, personRequest);
        if (personResponse != null) {
            if (personResponse.isSuccessful()) {
                List<Person> personList = personResponse.getPersonList();
                if (personList != null) {
                    requestResult.setData(personList);
                } else {
                    requestResult.setData(new ArrayList<Person>());
                }
            } else {
                requestResult.setSuccessful(false);
            }
        } else {
            requestResult.setSuccessful(false);
        }
    }
}
