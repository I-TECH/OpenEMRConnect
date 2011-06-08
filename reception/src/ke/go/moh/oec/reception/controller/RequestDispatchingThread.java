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

import ke.go.moh.oec.reception.data.RequestParameters;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.data.ExtendedSearchRequestParameters;
import ke.go.moh.oec.reception.data.BasicSearchRequestParameters;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class RequestDispatchingThread extends Thread {

    private final Mediator mediator;
    private final RequestParameters requestParameters;
    private final int requestTypeId;
    private RequestResult requestResult;

    public RequestDispatchingThread(Mediator mediator, RequestParameters requestParameters, int requestTypeId, RequestResult requestResult) {
        this.mediator = mediator;
        this.requestParameters = requestParameters;
        this.requestTypeId = requestTypeId;
        this.requestResult = requestResult;
    }

    @Override
    public void run() {
        switch (requestTypeId) {
            case RequestTypeId.FIND_PERSON_MPI:
                findCandidates(requestParameters, requestTypeId);
                break;
            case RequestTypeId.FIND_PERSON_LPI:
                //TODO: Change this call to go to the LPI
                findCandidates(requestParameters, RequestTypeId.FIND_PERSON_MPI);
                break;
            default:
                //TODO: Add code to handle unspecified request types
                throw new AssertionError();
        }
    }

    private void findCandidates(RequestParameters searchParameters, int requestTypeId) {
        PersonRequest personRequest = new PersonRequest();
        Person person = new Person();
        PersonIdentifier personIdentifier = new PersonIdentifier();
        List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
        PersonResponse personResponse = null;
        if (searchParameters.getClass() == BasicSearchRequestParameters.class) {
            BasicSearchRequestParameters basicSearchParameters = (BasicSearchRequestParameters) searchParameters;
            //TODO: Change Identifier type to Clinic ID types
            personIdentifier.setIdentifierType(PersonIdentifier.Type.kisumuHdssId);
            personIdentifier.setIdentifier(basicSearchParameters.getClinicId());
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
            person.setFingerprintList(basicSearchParameters.getFingerprintList());
            //person.setClanName("KONYANGO");
        } else if (searchParameters.getClass() == ExtendedSearchRequestParameters.class) {
            ExtendedSearchRequestParameters extendedSearchParameters = (ExtendedSearchRequestParameters) searchParameters;
            personIdentifier.setIdentifierType(PersonIdentifier.Type.cccLocalId);
            personIdentifier.setIdentifier(extendedSearchParameters.getClinicId());
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
            person.setFingerprintList(extendedSearchParameters.getFingerprint());
            person.setFirstName(extendedSearchParameters.getFirstName());
            person.setMiddleName(extendedSearchParameters.getMiddleName());
            person.setLastName(extendedSearchParameters.getLastName());
            person.setSex(extendedSearchParameters.getSex());
            person.setBirthdate(extendedSearchParameters.getBirthdate());
            person.setVillageName(extendedSearchParameters.getVillageName());
        }
        personRequest.setPerson(person);
        personResponse = (PersonResponse) mediator.getData(requestTypeId, personRequest);
        if (personResponse.isSuccessful()) {
            requestResult.setReturnCode(RequestResult.SUCCESS);
            List<Person> personList = personResponse.getPersonList();
            if (personList != null) {
                requestResult.setData(personList);
            } else {
                requestResult.setData(new ArrayList<Person>());
            }
        } else {
            requestResult.setReturnCode(RequestResult.FAILURE);
        }
    }
}
