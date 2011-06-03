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
package ke.go.moh.oec.reception.domain;

/**
 *
 * @author Gitahi Ng'ang'a
 */
import ke.go.moh.oec.*;
import java.util.List;
import java.util.ArrayList;
import ke.go.moh.oec.lib.Mediator;

public class RequestDispatcher {

    private static Mediator mediator = new Mediator();

    public static List<Person> findMPICandidates(SearchParameters searchParameters) throws UnreachableMPIException, UnreachableLPIException {
        return findCandidates(searchParameters, RequestTypeId.FIND_PERSON_MPI);
    }

    public static List<Person> findLPICandidates(SearchParameters searchParameters) throws UnreachableMPIException, UnreachableLPIException {
        return findCandidates(searchParameters, RequestTypeId.FIND_PERSON_LPI);
    }

    public void modifyPersonInMPI(int requestTypeId) {
        modifyPerson(RequestTypeId.MODIFY_PERSON_MPI);
    }

    public void modifyPersonInLPI() {
        modifyPerson(RequestTypeId.MODIFY_PERSON_LPI);
    }

    public void createPersonInMPI() {
        createPerson(RequestTypeId.CREATE_PERSON_MPI);
    }

    public void createPersonInLPI() {
        createPerson(RequestTypeId.CREATE_PERSON_LPI);
    }

    private void modifyPerson(int requestTypeId) {
        mediator.getData(requestTypeId, requestTypeId);
    }

    private void createPerson(int requestTypeId) {
        mediator.getData(requestTypeId, requestTypeId);
    }

    private static List<Person> findCandidates(SearchParameters searchParameters, int requestTypeId) throws UnreachableMPIException, UnreachableLPIException {
        List<Person> personList = new ArrayList<Person>();
        PersonRequest personRequest = new PersonRequest();
        Person person = new Person();
        PersonIdentifier personIdentifier = new PersonIdentifier();
        List<PersonIdentifier> personIdentifierList = new ArrayList<PersonIdentifier>();
        PersonResponse personResponse = null;
        if (searchParameters.getClass() == BasicSearchParameters.class) {
            BasicSearchParameters basicSearchParameters = (BasicSearchParameters) searchParameters;
            personIdentifier.setIdentifierType(PersonIdentifier.Type.cccLocalId);
            personIdentifier.setIdentifier(basicSearchParameters.getClinicId());
            personIdentifierList.add(personIdentifier);
            person.setPersonIdentifierList(personIdentifierList);
            person.setFingerprintList(basicSearchParameters.getFingerprintList());
        } else if (searchParameters.getClass() == ExtendedSearchParameters.class) {
            ExtendedSearchParameters extendedSearchParameters = (ExtendedSearchParameters) searchParameters;
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
            List<Person> pList = personResponse.getPersonList();
            if (pList != null) {
                personList = pList;
            }
        } else {
            if (requestTypeId == RequestTypeId.FIND_PERSON_MPI) {
                throw new UnreachableMPIException();
            } else {
                throw new UnreachableLPIException();
            }
        }
        return personList;
    }
}
