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
import ke.go.moh.oec.reception.data.ExtendedRequestParameters;
import ke.go.moh.oec.reception.data.BasicRequestParameters;
import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.data.ComprehensiveRequestParameters;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class RequestDispatchingThread extends Thread {

    private final Mediator mediator;
    private final RequestParameters requestParameters;
    private final int requestTypeId;
    private RequestResult requestResult;

    public RequestDispatchingThread(Mediator mediator, RequestParameters requestParameters,
            int requestTypeId, RequestResult requestResult) {
        this.mediator = mediator;
        this.requestParameters = requestParameters;
        this.requestTypeId = requestTypeId;
        this.requestResult = requestResult;
    }

    @Override
    public void run() {
        PersonRequest personRequest = new PersonRequest();
        Person person = new Person();
        PersonResponse personResponse = null;
        if (requestParameters.getClass() == BasicRequestParameters.class) {
            packageRequestParameters(person, (BasicRequestParameters) requestParameters);
        } else if (requestParameters.getClass() == ExtendedRequestParameters.class) {
            packageRequestParameters(person, (ExtendedRequestParameters) requestParameters);
        } else if (requestParameters.getClass() == ComprehensiveRequestParameters.class) {
            packageRequestParameters(person, (ComprehensiveRequestParameters) requestParameters);
        }
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

    private Person packageRequestParameters(Person person, BasicRequestParameters basicRequestParameters) {
        String clinicId = basicRequestParameters.getIdentifier();
        if (clinicId != null && !clinicId.isEmpty()) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
            PersonIdentifier.Type clinicIdType = Session.deduceIdentifierType(clinicId);
            if (clinicIdType == PersonIdentifier.Type.cccLocalId
                    && Session.getClientType() == Session.CLIENT_TYPE.ENROLLED) {
                clinicId = Session.prependClinicCode(clinicId);
            }
            personIdentifier.setIdentifierType(clinicIdType);
            personIdentifier.setIdentifier(clinicId);
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
        }
        if (basicRequestParameters.getFingerprint() != null) {
            List<Fingerprint> fingerprintList = new ArrayList<Fingerprint>();
            fingerprintList.add(basicRequestParameters.getFingerprint());
            person.setFingerprintList(fingerprintList);
        }
        person.setSiteName(basicRequestParameters.getClinicName());
        return person;
    }

    private Person packageRequestParameters(Person person, ExtendedRequestParameters extendedRequestParameters) {
        packageRequestParameters(person, extendedRequestParameters.getBasicRequestParameters());
        person.setFirstName(extendedRequestParameters.getFirstName());
        person.setMiddleName(extendedRequestParameters.getMiddleName());
        person.setLastName(extendedRequestParameters.getLastName());
        person.setSex(extendedRequestParameters.getSex());
        person.setBirthdate(extendedRequestParameters.getBirthdate());
        person.setVillageName(extendedRequestParameters.getVillageName());
        return person;
    }

    private Person packageRequestParameters(Person person, ComprehensiveRequestParameters comprehensiveRequestParameters) {
        packageRequestParameters(person, comprehensiveRequestParameters.getExtendedRequestParameters().getBasicRequestParameters());
        packageRequestParameters(person, comprehensiveRequestParameters.getExtendedRequestParameters());
        person.setMaritalStatus(comprehensiveRequestParameters.getMaritalStatus());
        person.setFathersFirstName(comprehensiveRequestParameters.getFathersFirstName());
        person.setFathersMiddleName(comprehensiveRequestParameters.getFathersMiddleName());
        person.setFathersLastName(comprehensiveRequestParameters.getFathersLastName());
        person.setMothersFirstName(comprehensiveRequestParameters.getMothersFirstName());
        person.setMothersMiddleName(comprehensiveRequestParameters.getMothersMiddleName());
        person.setMothersLastName(comprehensiveRequestParameters.getMothersLastName());
        person.setCompoundHeadFirstName(comprehensiveRequestParameters.getCompoundHeadsFirstName());
        person.setCompoundHeadMiddleName(comprehensiveRequestParameters.getCompoundHeadsMiddleName());
        person.setCompoundHeadLastName(comprehensiveRequestParameters.getCompoundHeadsLastName());
        return person;
    }
}
