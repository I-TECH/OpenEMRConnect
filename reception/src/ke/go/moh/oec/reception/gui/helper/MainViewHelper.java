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
package ke.go.moh.oec.reception.gui.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RelatedPerson;
import ke.go.moh.oec.Work;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.reception.controller.NotificationListener;
import ke.go.moh.oec.reception.controller.NotificationManager;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersonWrapper;
import ke.go.moh.oec.reception.controller.RequestDispatcher;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.Notification;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.data.Session;
import ke.go.moh.oec.reception.data.Server;
import ke.go.moh.oec.reception.gui.MainView;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class MainViewHelper implements NotificationManager {

    private final MainView mainView;
    private Session session;
    private BufferedImage missingFingerprintImage;
    private BufferedImage refusedFingerprintImage;

    public MainViewHelper(MainView mainView) {
        this.mainView = mainView;
        initialize();
    }

    private void initialize() {
        this.getWork();
        new Thread(new NotificationListener(this)).start();
    }

    public void startSession(Session.ClientType clientType) {
        session = new Session(clientType);
    }

    public void changeSessionClientType(Session.ClientType clientType) {
        if (session == null) {
            session = new Session(clientType);
        } else {
            session.changeSessionClientType(clientType);
        }
    }

    public Session getSession() {
        return session;
    }

    public SearchProcessResult findHouseholdMembers(PersonWrapper searchPersonWrapper) {
        List<Person> householdMemberList = new ArrayList<Person>();
        RequestResult kisumuHdssRequestResult = new RequestResult();
        PersonRequest personRequest = new PersonRequest();
        personRequest.setRequestReference(session.getReference());
        personRequest.setPerson(searchPersonWrapper.unwrap());
        RequestDispatcher.dispatch(personRequest, kisumuHdssRequestResult,
                RequestDispatcher.DispatchType.FIND, Server.KISUMU_HDSS);
        if (kisumuHdssRequestResult.isSuccessful()) {
            List<Person> searchPersonList = (List<Person>) kisumuHdssRequestResult.getData();
            if (!searchPersonList.isEmpty()) {
                Person searchPerson = searchPersonList.get(0);
                for (RelatedPerson relatedPerson : searchPerson.getHouseholdMembers()) {
                    householdMemberList.add(relatedPerson.getPerson());
                }
            }
            return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.KISUMU_HDSS, householdMemberList));
        } else {
            return new SearchProcessResult(SearchProcessResult.Type.UNREACHABLE_SERVER, null);
        }
    }

    public SearchProcessResult findPerson(int targetServer) {
        return findPerson(targetServer, session.getSearchPersonWrapper(), false);
    }

    public SearchProcessResult findPerson(int targetServer, PersonWrapper searchPersonWrapper) {
        return findPerson(targetServer, searchPersonWrapper, false);
    }

    public SearchProcessResult findPerson(int targetServer, PersonWrapper searchPersonWrapper, boolean lastResort) {
        //reset results display
        if (targetServer == Server.MPI_LPI) {
            session.setMpiResultDisplayed(false);
            session.setLpiResultDisplayed(false);
        }
        if (targetServer == Server.MPI) {
            session.setMpiResultDisplayed(false);
        }
        if (targetServer == Server.LPI) {
            session.setLpiResultDisplayed(false);
        }
        //we only maintain rejected candidate lists if we are doing a last resort search
        //otherwise we clear them
        if (!lastResort) {
            session.setRejectedMPICandidateList(null);
            session.setRejectedLPICandidateList(null);
        }
        List<Person> mpiPersonList = null;
        List<Person> lpiPersonList = null;
        session.setSearchPersonWrapper(searchPersonWrapper);
        RequestResult mpiRequestResult = session.getMpiRequestResult();
        RequestResult lpiRequestResult = session.getLpiRequestResult();
        RequestDispatcher.dispatch(createPersonRequest(searchPersonWrapper),
                mpiRequestResult, lpiRequestResult, RequestDispatcher.DispatchType.FIND, targetServer);
        if (mpiRequestResult.isSuccessful()
                && lpiRequestResult.isSuccessful()) {
            mpiPersonList = (List<Person>) mpiRequestResult.getData();
            lpiPersonList = (List<Person>) lpiRequestResult.getData();

            if (!lastResort || (lastResort && targetServer == Server.MPI_LPI)) {
                if (checkForLinkedCandidates(lpiPersonList)) {
                    return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.LPI, removeRejectedLpiCandidates(lpiPersonList)));
                } else {
                    if (checkForFingerprintCandidates(mpiPersonList)) {
                        return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.MPI, removeRejectedMpiCandidates(mpiPersonList)));
                    } else {
                        if (!minimumSearchFingerprintsTaken()) {
                            return new SearchProcessResult(SearchProcessResult.Type.NEXT_FINGERPRINT, null);
                        } else {
                            if (!session.getAnyUnsentFingerprints().isEmpty()) {
                                for (ImagedFingerprint imagedFingerprint : session.getAnyUnsentFingerprints()) {
                                    session.setActiveImagedFingerprint(imagedFingerprint);
                                    searchPersonWrapper.addFingerprint(imagedFingerprint);
                                    break;
                                }
                                return findPerson(Server.MPI_LPI, searchPersonWrapper);
                            } else {
                                if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                                    return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.LPI, lpiPersonList));
                                } else {
                                    if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                                        return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.MPI, mpiPersonList));
                                    } else {
                                        return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (lastResort && targetServer == Server.MPI) {
                if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                    return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.MPI, mpiPersonList));
                } else {
                    return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
                }
            } else if (lastResort && targetServer == Server.LPI) {
                if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                    return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.LPI, lpiPersonList));
                } else {
                    return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
                }
            } else {
                //this is not expected to happen
                Mediator.getLogger(MainViewHelper.class.getName()).log(Level.SEVERE, "Illogical condition occured in findPerson() at line 195.");
                return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
            }

        } else {
            //only offer retry for non-last resort searches
            if (!lastResort) {
                if (!mpiRequestResult.isSuccessful()
                        && !lpiRequestResult.isSuccessful()) {
                    if (mainView.showConfirmMessage("Both the Master and the Local Person Indices could not be contacted. "
                            + "Would you like to try contacting them again?", ((MainView) mainView).getFrame())) {
                        return findPerson(Server.MPI_LPI, searchPersonWrapper);
                    }
                } else {
                    if (!mpiRequestResult.isSuccessful()
                            && lpiRequestResult.isSuccessful()) {
                        lpiPersonList = (List<Person>) lpiRequestResult.getData();
                        if (mainView.showConfirmMessage("The Master Person Index could not be contacted. "
                                + "Would you like to try contacting it again?", ((MainView) mainView).getFrame())) {
                            return findPerson(Server.MPI, searchPersonWrapper);
                        }
                        if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                            return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.LPI, lpiPersonList));
                        } else {
                            return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
                        }
                    } else if (!lpiRequestResult.isSuccessful()
                            && mpiRequestResult.isSuccessful()) {
                        mpiPersonList = (List<Person>) mpiRequestResult.getData();
                        if (mainView.showConfirmMessage("The Local Person Index could not be contacted. "
                                + "Would you like to try contacting it again?", ((MainView) mainView).getFrame())) {
                            return findPerson(Server.LPI, searchPersonWrapper);
                        }
                        if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                            return new SearchProcessResult(SearchProcessResult.Type.LIST, new SearchServerResponse(Server.MPI, mpiPersonList));
                        } else {
                            return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
                        }
                    }
                }
                return new SearchProcessResult(SearchProcessResult.Type.UNREACHABLE_SERVER, null);
            } else {
                return new SearchProcessResult(SearchProcessResult.Type.EXIT, null);
            }
        }
    }

    public void createPerson(int targetServer, PersonWrapper personWrapper) {
        RequestDispatcher.dispatch(createPersonRequest(personWrapper), RequestDispatcher.DispatchType.CREATE, targetServer);
    }

    public void modifyPerson(int targetServer, PersonWrapper personWrapper) {
        RequestDispatcher.dispatch(createPersonRequest(personWrapper), RequestDispatcher.DispatchType.MODIFY, targetServer);
    }

    public void requireClinicId() {
        session.setClinicId(true);
    }

    public void doNotRequireClinicId() {
        session.setClinicId(false);
    }

    public void undoMpiResultDisplay() {
        session.setMpiResultDisplayed(false);
    }

    public void undoLpiResultDisplay() {
        session.setLpiResultDisplayed(false);
    }

    public ImagedFingerprint getActiveImagedFingerprint() {
        return session.getActiveImagedFingerprint();
    }

    public PersonWrapper getSearchPersonWrapper() {
        return session.getSearchPersonWrapper();
    }

    public List<Person> getMpiResultList() {
        return (List<Person>) session.getMpiRequestResult().getData();
    }

    public List<Person> getLpiResultList() {
        return (List<Person>) session.getLpiRequestResult().getData();
    }

    public void noMatchFound(int targetServer) {
        if (targetServer == Server.MPI) {
            session.setMpiMatchPersonWrapper(null);
            saveRejectedMPICandidateList();
        } else if (targetServer == Server.LPI) {
            session.setLpiMatchPersonWrapper(null);
            saveRejectedLPICandidateList();
        }
    }

    private void saveRejectedMPICandidateList() {
        List<Person> rejectedMPICandidateList = session.getRejectedMPICandidateList();
        List<Person> mpiPersonList = (List<Person>) session.getMpiRequestResult().getData();
        if (rejectedMPICandidateList == null) {
            rejectedMPICandidateList = new ArrayList<Person>();
        }
        rejectedMPICandidateList.clear();
        for (Person person : mpiPersonList) {
            Person p = new Person();
            p.setPersonGuid(person.getPersonGuid());
            rejectedMPICandidateList.add(p);
        }
        session.setRejectedMPICandidateList(rejectedMPICandidateList);
    }

    private void saveRejectedLPICandidateList() {
        List<Person> rejectedLPICandidateList = session.getRejectedLPICandidateList();
        List<Person> lpiPersonList = (List<Person>) session.getLpiRequestResult().getData();
        if (rejectedLPICandidateList == null) {
            rejectedLPICandidateList = new ArrayList<Person>();
        }
        rejectedLPICandidateList.clear();
        for (Person person : lpiPersonList) {
            Person p = new Person();
            p.setPersonGuid(person.getPersonGuid());
            rejectedLPICandidateList.add(p);
        }
        session.setRejectedLPICandidateList(rejectedLPICandidateList);
    }

    public boolean isMpiResultDisplayed() {
        return session.isMpiResultDisplayed();
    }

    public boolean isLpiResultDisplayed() {
        return session.isLpiResultDisplayed();
    }

    public void acceptMatch(int targetServer, PersonWrapper personWrapper) {
        if (targetServer == Server.MPI) {
            session.setMpiMatchPersonWrapper(personWrapper);
            session.setRejectedMPICandidateList(null);
        } else if (targetServer == Server.LPI) {
            session.setLpiMatchPersonWrapper(personWrapper);
            session.setRejectedLPICandidateList(null);
        }
    }

    public boolean requiresClinicId() {
        return session.isClinicId();
    }

//    public boolean noMatchWasFound() {
//        return (session.getMpiMatchPersonWrapper() == null
//                && session.getLpiMatchPersonWrapper() == null);
//    }
    public boolean noMPIMatchWasFound() {
        return (session.getMpiMatchPersonWrapper() == null);
    }

    public boolean noLPIMatchWasFound() {
        return (session.getLpiMatchPersonWrapper() == null);
    }

    public boolean hasLastResortSearchDone() {
        return session.isLastResortSearchDone();
    }

    public void setLastResortSearchDone(boolean done) {
        session.setLastResortSearchDone(done);
    }

    private List<Person> removeRejectedMpiCandidates(List<Person> mpiPersonList) {
        List<Person> rejectedMPICandidateList = session.getRejectedMPICandidateList();
        if (rejectedMPICandidateList != null) {
            for (Person person : rejectedMPICandidateList) {
                mpiPersonList.remove(person);
            }
        }
        return mpiPersonList;
    }

    private List<Person> removeRejectedLpiCandidates(List<Person> lpiPersonList) {
        List<Person> rejectedLPICandidateList = session.getRejectedLPICandidateList();
        if (rejectedLPICandidateList != null) {
            for (Person person : rejectedLPICandidateList) {
                lpiPersonList.remove(person);
            }
        }
        return lpiPersonList;
    }

    public void addNotifications(List<Notification> notificationList) {
        mainView.addNotifications(notificationList);
    }

    private void getWork() {
        RequestDispatcher.dispatch(createDummyWork(), RequestDispatcher.DispatchType.GET_WORK, Server.CDS);
    }

    private PersonRequest createPersonRequest(PersonWrapper personWrapper) {
        PersonRequest personRequest = new PersonRequest();
        personRequest.setPerson(personWrapper.unwrap());
        personRequest.setRequestReference(session.getReference());
        return personRequest;
    }

    private Work createDummyWork() {
        Work work = new Work();
        work.setNotificationId("-1");
        work.setReassignAddress(OECReception.applicationAddress());
        work.setSourceAddress(OECReception.applicationAddress());
        return work;
    }

    private boolean checkForFingerprintCandidates(List<Person> personList) {
        for (Person person : personList) {
            if (person.isFingerprintMatched()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkForLinkedCandidates(List<Person> personList) {
        for (Person person : personList) {
            if (getMPIIdentifier(person) != null) {
                return true;
            }
        }
        return false;
    }

    public ImagedFingerprint getMissingFingerprint() {
        if (missingFingerprintImage == null) {
            try {
                String filePath = new File("missing_fingerprint.png").getAbsolutePath();
                missingFingerprintImage = ImageIO.read(new File(filePath));
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ImagedFingerprint(missingFingerprintImage, true);
    }

    public ImagedFingerprint getRefusedFingerprint() {
        if (refusedFingerprintImage == null) {
            try {
                String filePath = new File("refused_fingerprint.png").getAbsolutePath();
                refusedFingerprintImage = ImageIO.read(new File(filePath));
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ImagedFingerprint(refusedFingerprintImage, true);
    }

    private String getMPIIdentifier(Person person) {
        String mpiIdentifier = null;
        if (person != null
                && person.getPersonIdentifierList() != null
                && !person.getPersonIdentifierList().isEmpty()) {
            for (PersonIdentifier personIdentifier : person.getPersonIdentifierList()) {
                if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.masterPatientRegistryId) {
                    mpiIdentifier = personIdentifier.getIdentifier();
                }
            }
        }
        return mpiIdentifier;
    }

    public String getSexString(Person.Sex sex) {
        String sexString = "";
        if (sex != null) {
            if (sex == Person.Sex.M) {
                sexString = "Male";
            } else if (sex == Person.Sex.F) {
                sexString = "Female";
            }
        }
        return sexString;
    }

    private Person.Sex getSex(String sexString) {
        Person.Sex sex = null;
        if (sexString != null) {
            if (sexString.equalsIgnoreCase("Male")) {
                sex = Person.Sex.M;
            } else if (sexString.equalsIgnoreCase("Female")) {
                sex = Person.Sex.F;
            }
        }
        return sex;
    }

    public String getConsentSignedString(Person.ConsentSigned consentSigned) {
        String consentSignedString = "";
        if (consentSigned != null) {
            if (consentSigned == Person.ConsentSigned.yes) {
                consentSignedString = "Yes";
            } else if (consentSigned == Person.ConsentSigned.no) {
                consentSignedString = "No";
            } else if (consentSigned == Person.ConsentSigned.notAnswered) {
                consentSignedString = "No answer";
            }
        }
        return consentSignedString;
    }

    private Person.ConsentSigned getConsentSigned(String consentSignedString) {
        Person.ConsentSigned consentSigned = null;
        if (consentSigned != null) {
            if (consentSignedString.equalsIgnoreCase("Yes")) {
                consentSigned = Person.ConsentSigned.yes;
            } else if (consentSignedString.equalsIgnoreCase("No")) {
                consentSigned = Person.ConsentSigned.no;
            } else if (consentSignedString.equalsIgnoreCase("Not answered")) {
                consentSigned = Person.ConsentSigned.notAnswered;
            }
        }
        return consentSigned;
    }

    public String getMaritalStatusString(Person.MaritalStatus maritalStatus) {
        String maritalStatusString = "";
        if (maritalStatus != null) {
            if (maritalStatus == Person.MaritalStatus.cohabitating) {
                maritalStatusString = "Cohabiting";
            } else if (maritalStatus == Person.MaritalStatus.divorced) {
                maritalStatusString = "Divorced";
            } else if (maritalStatus == Person.MaritalStatus.marriedMonogamous) {
                maritalStatusString = "Married monogamous";
            } else if (maritalStatus == Person.MaritalStatus.marriedPolygamous) {
                maritalStatusString = "Married polygamous";
            } else if (maritalStatus == Person.MaritalStatus.single) {
                maritalStatusString = "Single";
            } else if (maritalStatus == Person.MaritalStatus.widowed) {
                maritalStatusString = "Widowed";
            }
        }
        return maritalStatusString;
    }

    private Person.MaritalStatus getMaritalStatus(String maritalStatusString) {
        Person.MaritalStatus maritalStatus = null;
        if (maritalStatus != null) {
            if (maritalStatusString.equalsIgnoreCase("Cohabiting")) {
                maritalStatus = Person.MaritalStatus.cohabitating;
            } else if (maritalStatusString.equalsIgnoreCase("Divorced")) {
                maritalStatus = Person.MaritalStatus.divorced;
            } else if (maritalStatusString.equalsIgnoreCase("Married monogamous")) {
                maritalStatus = Person.MaritalStatus.marriedMonogamous;
            } else if (maritalStatusString.equalsIgnoreCase("Married polygamous")) {
                maritalStatus = Person.MaritalStatus.marriedPolygamous;
            } else if (maritalStatusString.equalsIgnoreCase("Single")) {
                maritalStatus = Person.MaritalStatus.single;
            } else if (maritalStatusString.equalsIgnoreCase("Widowed")) {
                maritalStatus = Person.MaritalStatus.widowed;
            }
        }
        return maritalStatus;
    }

    private boolean minimumSearchFingerprintsTaken() {
        boolean minimumRequiredFingerprintsTaken = false;
        if (!session.isFingerprint()) {
            minimumRequiredFingerprintsTaken = true;
        } else {
            if (session.getImagedFingerprintList() != null
                    && session.getImagedFingerprintList().size() >= OECReception.MINIMUM_FINGERPRINTS_FOR_SEARCH) {
                minimumRequiredFingerprintsTaken = true;
            }
        }
        return minimumRequiredFingerprintsTaken;
    }

    public void endSession() {
        this.session = null;
    }
}
