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
 * @author jgitau
 */
import java.util.Date;
import ke.go.moh.oec.*;
import java.util.List;
import java.util.ArrayList;
import ke.go.moh.oec.lib.Mediator;

//import
public class FindPerson {
    /*
    First search involves the Clinic ID, Clinic Name and a fingerprint template as described in the FS
     */

    Mediator mediator = new Mediator();

    public List<Person> findPerson(int requestTypeId, String clinicId, PersonIdentifier.Type personIdentifierType, String clinicName,
            byte[] fingerprint, Fingerprint.Type fingerprintType) {
        PersonResponse pr = new PersonResponse();
        PersonRequest personRequest = new PersonRequest();
        Fingerprint f = new Fingerprint();
        PersonIdentifier pi = new PersonIdentifier();
        List<PersonIdentifier> pil = new ArrayList<PersonIdentifier>();
        Person p = new Person();
        List<Fingerprint> fList = new ArrayList<Fingerprint>();
        fList.add(f);

        //investigate what kind of clinic ID we have before we proceed - this might already have been done in the GUI
        pi.setIdentifier(clinicId);
        pi.setIdentifierType(personIdentifierType);
        pil.add(pi);
        p.setPersonIdentifierList(pil);

        f.setFingerprintType(fingerprintType);

        p.setFingerprintList(fList);
        personRequest.setPerson(p);
        pr = (PersonResponse) mediator.getData(requestTypeId, personRequest);

        return pr.getPersonList();
    }
    /*
     *Second search signature involves the Clinic ID, Clinic Name and a fingerprint template as described in the FS
     * together with all the clients names, date of birth, sex and village.
     */

    public List<Person> findPerson(int requestTypeId, String clinicId, PersonIdentifier.Type personIdentifierType, String clinicName,
            byte[] fingerprint, Fingerprint.Type fingerprintType, String firstName, String secondName, String lastName, Date DOB,
            String village, String sex, String clinicID) {

        PersonResponse pr = new PersonResponse();
        PersonRequest preq = new PersonRequest();
        PersonIdentifier pi = new PersonIdentifier();
        List<PersonIdentifier> pil = new ArrayList<PersonIdentifier>();
        Person p = new Person();
        pi.setIdentifier(clinicID);
        pi.setIdentifierType(personIdentifierType);
        pil.add(pi);
        p.setPersonIdentifierList(pil);

        preq.setPerson(p);
        pr = (PersonResponse) mediator.getData(requestTypeId, preq);
        return pr.getPersonList();

    }
}
