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

import java.util.ArrayList;
import java.util.List;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.Work;
import ke.go.moh.oec.reception.controller.NotificationListener;
import ke.go.moh.oec.reception.controller.NotificationManager;
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersonWrapper;
import ke.go.moh.oec.reception.controller.RequestDispatcher;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.Notification;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.data.Session;
import ke.go.moh.oec.reception.data.TargetServer;
import ke.go.moh.oec.reception.gui.MainView;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class MainViewHelper implements NotificationManager {

    private final MainView mainView;
    private Session session;

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

    public Session getSession() {
        return session;
    }

    public ProcessResult findPerson(int targetIndex) {
        return findPerson(targetIndex, session.getSearchPersonWrapper());
    }

    public ProcessResult findPerson(int targetIndex, PersonWrapper searchPersonWrapper) {
        List<Person> mpiPersonList = null;
        List<Person> lpiPersonList = null;
        session.setSearchPersonWrapper(searchPersonWrapper);
        RequestResult mpiRequestResult = session.getMpiRequestResult();
        RequestResult lpiRequestResult = session.getLpiRequestResult();
        RequestDispatcher.dispatch(createPersonRequest(searchPersonWrapper),
                mpiRequestResult, lpiRequestResult, RequestDispatcher.DispatchType.FIND, targetIndex);
        if (mpiRequestResult.isSuccessful()
                && lpiRequestResult.isSuccessful()) {
            mpiPersonList = (List<Person>) mpiRequestResult.getData();
            lpiPersonList = (List<Person>) lpiRequestResult.getData();
            if (OECReception.checkForLinkedCandidates(lpiPersonList)) {
                return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(TargetServer.LPI, removeRejectedLpiCandidates(lpiPersonList)));
            } else {
                if (OECReception.checkForFingerprintCandidates(mpiPersonList)) {
                    return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(TargetServer.MPI, removeRejectedMpiCandidates(mpiPersonList)));
                } else {
                    if (!session.hasMinimumRequiredFingerprintsTaken()) {
                        return new ProcessResult(ProcessResult.Type.NEXT_FINGERPRINT, null);
                    } else {
                        if (!session.getAnyUnsentFingerprints().isEmpty()) {
                            for (ImagedFingerprint imagedFingerprint : session.getAnyUnsentFingerprints()) {
                                session.setActiveImagedFingerprint(imagedFingerprint);
                                searchPersonWrapper.addFingerprint(imagedFingerprint.getFingerprint());
                                imagedFingerprint.setSent(true);
                                break;
                            }
                            return findPerson(TargetServer.MPI_LPI, searchPersonWrapper);
                        } else {
                            if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                                return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(TargetServer.LPI, lpiPersonList));
                            } else {
                                if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                                    return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(TargetServer.MPI, mpiPersonList));
                                } else {
                                    return new ProcessResult(ProcessResult.Type.EXIT, null);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (!mpiRequestResult.isSuccessful()
                    && !lpiRequestResult.isSuccessful()) {
                if (mainView.showConfirmMessage("Both the Master and the Local Person Indices could not be contacted. "
                        + "Would you like to try contacting them again?", ((MainView) mainView).getFrame())) {
                    return findPerson(TargetServer.MPI_LPI, searchPersonWrapper);
                }
            } else {
                if (!mpiRequestResult.isSuccessful()
                        && lpiRequestResult.isSuccessful()) {
                    lpiPersonList = (List<Person>) lpiRequestResult.getData();
                    if (mainView.showConfirmMessage("The Master Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", ((MainView) mainView).getFrame())) {
                        return findPerson(TargetServer.MPI, searchPersonWrapper);
                    }
                    if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                        return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(TargetServer.MPI, lpiPersonList));
                    } else {
                        return new ProcessResult(ProcessResult.Type.EXIT, null);
                    }
                } else if (!lpiRequestResult.isSuccessful()
                        && mpiRequestResult.isSuccessful()) {
                    mpiPersonList = (List<Person>) mpiRequestResult.getData();
                    if (mainView.showConfirmMessage("The Local Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", ((MainView) mainView).getFrame())) {
                        return findPerson(TargetServer.LPI, searchPersonWrapper);
                    }
                    if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                        return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(TargetServer.MPI, mpiPersonList));
                    } else {
                        return new ProcessResult(ProcessResult.Type.EXIT, null);
                    }
                }
            }
            return new ProcessResult(ProcessResult.Type.UNREACHABLE_INDICES, null);
        }
    }

    public void createPerson(int targetIndex, PersonWrapper personWrapper) {
        RequestDispatcher.dispatch(createPersonRequest(personWrapper), RequestDispatcher.DispatchType.CREATE, targetIndex);
    }

    public void modifyPerson(int targetIndex, PersonWrapper personWrapper) {
        RequestDispatcher.dispatch(createPersonRequest(personWrapper), RequestDispatcher.DispatchType.MODIFY, targetIndex);
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

    public void noMatchFound(int targetIndex) {
        if (targetIndex == TargetServer.MPI) {
            session.setMpiMatchPersonWrapper(null);
            saveRejectedMPICandidateList();
        } else if (targetIndex == TargetServer.LPI) {
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

    public void acceptMatch(int targetIndex, PersonWrapper personWrapper) {
        if (targetIndex == TargetServer.MPI) {
            session.setMpiMatchPersonWrapper(personWrapper);
            session.setRejectedMPICandidateList(null);
        } else if (targetIndex == TargetServer.LPI) {
            session.setLpiMatchPersonWrapper(personWrapper);
            session.setRejectedLPICandidateList(null);
        }
    }

    public boolean requiresClinicId() {
        return session.isClinicId();
    }

    public boolean noMatchWasFound() {
        return (session.getMpiMatchPersonWrapper() == null
                && session.getLpiMatchPersonWrapper() == null);
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
        RequestDispatcher.dispatch(createDummyWork(), RequestDispatcher.DispatchType.GET_WORK, TargetServer.CDS);
    }

    private PersonRequest createPersonRequest(PersonWrapper personWrapper) {
        PersonRequest personRequest = new PersonRequest();
        personRequest.setPerson(personWrapper.unwrap());
        personRequest.setRequestReference(personWrapper.getRequestReference());
        return personRequest;
    }

    private Work createDummyWork() {
        Work work = new Work();
        work.setNotificationId("-1");
        work.setReassignAddress(OECReception.applicationAddress());
        work.setSourceAddress(OECReception.applicationAddress());
        return work;
    }
}
