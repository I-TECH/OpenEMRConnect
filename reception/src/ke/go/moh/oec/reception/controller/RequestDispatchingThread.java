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
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.PersonResponse;
import ke.go.moh.oec.RequestTypeId;
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

    public RequestDispatchingThread(Mediator mediator, RequestParameters requestParameters, int requestTypeId, RequestResult requestResult) {
        this.mediator = mediator;
        this.requestParameters = requestParameters;
        this.requestTypeId = requestTypeId;
        this.requestResult = requestResult;
    }
    //find out why this is needed

    RequestDispatchingThread(Mediator mediator, RequestParameters requestParameters, int CREATE_PERSON_MPI) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void run() {
        switch (requestTypeId) {
            case RequestTypeId.FIND_PERSON_MPI:
                findCandidates(requestParameters, requestTypeId);
                break;
            case RequestTypeId.FIND_PERSON_LPI:
                findCandidates(requestParameters, requestTypeId);
                break;
            case RequestTypeId.CREATE_PERSON_MPI:
                createPerson(requestParameters, requestTypeId);
                break;
            case RequestTypeId.CREATE_PERSON_LPI:
                createPerson(requestParameters, requestTypeId);
                break;
            case RequestTypeId.MODIFY_PERSON_MPI:
                modifyPerson(requestParameters, requestTypeId);
                break;
            case RequestTypeId.MODIFY_PERSON_LPI:
                modifyPerson(requestParameters, requestTypeId);
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
        if (searchParameters.getClass() == BasicRequestParameters.class) {
            BasicRequestParameters basicSearchParameters = (BasicRequestParameters) searchParameters;
            String clinicId = basicSearchParameters.getClinicId();
            PersonIdentifier.Type clinicIdType = Session.deducePersonIdentifierType(clinicId);
            if (clinicIdType == PersonIdentifier.Type.cccLocalId) {
                clinicId = Session.tweakLocalClinicId(clinicId);
            }
            personIdentifier.setIdentifierType(clinicIdType);
            personIdentifier.setIdentifier(clinicId);
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
            person.setFingerprintList(basicSearchParameters.getFingerprintList());
            //person.setClanName("KONYANGO");
        } else if (searchParameters.getClass() == ExtendedRequestParameters.class) {
            ExtendedRequestParameters extendedSearchParameters = (ExtendedRequestParameters) searchParameters;
            String clinicId = extendedSearchParameters.getBasicRequestParameters().getClinicId();
            PersonIdentifier.Type clinicIdType = Session.deducePersonIdentifierType(clinicId);
            if (clinicIdType == PersonIdentifier.Type.cccLocalId) {
                clinicId = Session.tweakLocalClinicId(clinicId);
            }
            personIdentifier.setIdentifierType(clinicIdType);
            personIdentifier.setIdentifier(clinicId);
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
            person.setFingerprintList(extendedSearchParameters.getBasicRequestParameters().getFingerprintList());
            person.setFirstName(extendedSearchParameters.getFirstName());
            person.setMiddleName(extendedSearchParameters.getMiddleName());
            person.setLastName(extendedSearchParameters.getLastName());
            person.setSex(extendedSearchParameters.getSex());
            person.setBirthdate(extendedSearchParameters.getBirthdate());
            person.setVillageName(extendedSearchParameters.getVillageName());
        } else if (searchParameters.getClass() == ComprehensiveRequestParameters.class) {
            ComprehensiveRequestParameters comprehensiveRequestParameters = (ComprehensiveRequestParameters) searchParameters;
            String clinicId = comprehensiveRequestParameters.getExtendedRequestParameters().getBasicRequestParameters().getClinicId();
            PersonIdentifier.Type clinicIdType = Session.deducePersonIdentifierType(clinicId);
            if (clinicIdType == PersonIdentifier.Type.cccLocalId) {
                clinicId = Session.tweakLocalClinicId(clinicId);
            }
            personIdentifier.setIdentifierType(clinicIdType);
            personIdentifier.setIdentifier(clinicId);
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
            person.setFingerprintList(comprehensiveRequestParameters.getExtendedRequestParameters().getBasicRequestParameters().getFingerprintList());
            person.setFirstName(comprehensiveRequestParameters.getExtendedRequestParameters().getFirstName());
            person.setMiddleName(comprehensiveRequestParameters.getExtendedRequestParameters().getMiddleName());
            person.setLastName(comprehensiveRequestParameters.getExtendedRequestParameters().getLastName());
            person.setSex(comprehensiveRequestParameters.getExtendedRequestParameters().getSex());
            person.setBirthdate(comprehensiveRequestParameters.getExtendedRequestParameters().getBirthdate());
            person.setVillageName(comprehensiveRequestParameters.getExtendedRequestParameters().getVillageName());
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
        }
        personRequest.setPerson(person);
        try {
            personResponse = (PersonResponse) mediator.getData(requestTypeId, personRequest);
        } catch (Exception e) {
            System.out.println("mediator.getData(requestTypeId, personRequest); Exeption " + e.getMessage());
            e.printStackTrace();
        }
        if (personResponse != null) {
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
        } else {
            requestResult.setReturnCode(RequestResult.FAILURE);
        }
    }

    private void createPerson(RequestParameters createPersonParameters, int requestTypeId) {
        PersonRequest personRequest = new PersonRequest();
        Person person = new Person();
        PersonIdentifier personIdentifier = new PersonIdentifier();
        PersonResponse personResponse = null;

        ComprehensiveRequestParameters comprehensiveRequestParameters = (ComprehensiveRequestParameters) createPersonParameters;
        person.setFingerprintList(comprehensiveRequestParameters.getExtendedRequestParameters().getBasicRequestParameters().getFingerprintList());
        person.setFirstName(comprehensiveRequestParameters.getExtendedRequestParameters().getFirstName());
        person.setMiddleName(comprehensiveRequestParameters.getExtendedRequestParameters().getMiddleName());
        person.setLastName(comprehensiveRequestParameters.getExtendedRequestParameters().getLastName());
        person.setSex(comprehensiveRequestParameters.getExtendedRequestParameters().getSex());
        person.setBirthdate(comprehensiveRequestParameters.getExtendedRequestParameters().getBirthdate());
        person.setVillageName(comprehensiveRequestParameters.getExtendedRequestParameters().getVillageName());

        person.setMothersFirstName(comprehensiveRequestParameters.getMothersFirstName());
        person.setMothersMiddleName(comprehensiveRequestParameters.getMothersMiddleName());
        person.setMothersLastName(comprehensiveRequestParameters.getMothersLastName());

        person.setFathersFirstName(comprehensiveRequestParameters.getFathersFirstName());
        person.setFathersMiddleName(comprehensiveRequestParameters.getFathersMiddleName());
        person.setFathersLastName(comprehensiveRequestParameters.getFathersLastName());

        person.setMaritalStatus(comprehensiveRequestParameters.getMaritalStatus());

        person.setCompoundHeadFirstName(comprehensiveRequestParameters.getCompoundHeadsFirstName());
        person.setCompoundHeadMiddleName(comprehensiveRequestParameters.getCompoundHeadsMiddleName());
        person.setCompoundHeadLastName(comprehensiveRequestParameters.getCompoundHeadsLastName());

        personRequest.setPerson(person);
        personResponse = (PersonResponse) mediator.getData(requestTypeId, personRequest);

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void modifyPerson(RequestParameters modifyPersonParameters, int requestTypeId) {

        PersonRequest personRequest = new PersonRequest();
        Person person = new Person();
        PersonIdentifier personIdentifier = new PersonIdentifier();
        PersonResponse personResponse = null;

        ComprehensiveRequestParameters comprehensiveRequestParameters = (ComprehensiveRequestParameters) modifyPersonParameters;
        person.setFingerprintList(comprehensiveRequestParameters.getExtendedRequestParameters().getBasicRequestParameters().getFingerprintList());
        person.setFirstName(comprehensiveRequestParameters.getExtendedRequestParameters().getFirstName());
        person.setMiddleName(comprehensiveRequestParameters.getExtendedRequestParameters().getMiddleName());
        person.setLastName(comprehensiveRequestParameters.getExtendedRequestParameters().getLastName());
        person.setSex(comprehensiveRequestParameters.getExtendedRequestParameters().getSex());
        person.setBirthdate(comprehensiveRequestParameters.getExtendedRequestParameters().getBirthdate());
        person.setVillageName(comprehensiveRequestParameters.getExtendedRequestParameters().getVillageName());

        person.setMothersFirstName(comprehensiveRequestParameters.getMothersFirstName());
        person.setMothersMiddleName(comprehensiveRequestParameters.getMothersMiddleName());
        person.setMothersLastName(comprehensiveRequestParameters.getMothersLastName());

        person.setFathersFirstName(comprehensiveRequestParameters.getFathersFirstName());
        person.setFathersMiddleName(comprehensiveRequestParameters.getFathersMiddleName());
        person.setFathersLastName(comprehensiveRequestParameters.getFathersLastName());

        person.setMaritalStatus(comprehensiveRequestParameters.getMaritalStatus());

        person.setCompoundHeadFirstName(comprehensiveRequestParameters.getCompoundHeadsFirstName());
        person.setCompoundHeadMiddleName(comprehensiveRequestParameters.getCompoundHeadsMiddleName());
        person.setCompoundHeadLastName(comprehensiveRequestParameters.getCompoundHeadsLastName());

        personRequest.setPerson(person);
        personResponse = (PersonResponse) mediator.getData(requestTypeId, personRequest);

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
