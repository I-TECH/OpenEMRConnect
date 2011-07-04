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
import ke.go.moh.oec.reception.controller.OECReception;
import ke.go.moh.oec.reception.controller.PersonWrapper;
import ke.go.moh.oec.reception.controller.RequestDispatcher;
import ke.go.moh.oec.reception.data.ImagedFingerprint;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.data.Session;
import ke.go.moh.oec.reception.gui.MainView;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class GuiHelper {

    private HelpableGui helpableGUI;
    private Session session;

    public GuiHelper(HelpableGui helpableGUI) {
        this.helpableGUI = helpableGUI;
    }

    public Session getSession() {
        return session;
    }

    public ProcessResult doSearch(int targetIndex) {
        return doSearch(targetIndex, session.getSearchPersonWrapper());
    }

    public ProcessResult doSearch(int targetIndex, PersonWrapper searchPersonWrapper) {
        session.setSearchPersonWrapper(searchPersonWrapper);
        List<Person> mpiPersonList = null;
        List<Person> lpiPersonList = null;
        RequestResult mpiRequestResult = session.getMpiRequestResult();
        RequestResult lpiRequestResult = session.getLpiRequestResult();
        RequestDispatcher.dispatch(searchPersonWrapper,
                mpiRequestResult, lpiRequestResult, RequestDispatcher.DispatchType.FIND, targetIndex);
        if (mpiRequestResult.isSuccessful()
                && lpiRequestResult.isSuccessful()) {
            mpiPersonList = (List<Person>) mpiRequestResult.getData();
            lpiPersonList = (List<Person>) lpiRequestResult.getData();
            if (OECReception.checkForLinkedCandidates(lpiPersonList)) {
                return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(RequestDispatcher.TargetIndex.LPI, removeRejectedLpiCandidates(lpiPersonList)));
            } else {
                if (OECReception.checkForFingerprintCandidates(mpiPersonList)) {
                    return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(RequestDispatcher.TargetIndex.MPI, removeRejectedMpiCandidates(mpiPersonList)));
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
                            return doSearch(RequestDispatcher.TargetIndex.BOTH, searchPersonWrapper);
                        } else {
                            if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                                return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(RequestDispatcher.TargetIndex.LPI, lpiPersonList));
                            } else {
                                if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                                    return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(RequestDispatcher.TargetIndex.MPI, mpiPersonList));
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
                if (helpableGUI.showConfirmMessage("Both the Master and the Local Person Indices could not be contacted. "
                        + "Would you like to try contacting them again?", ((MainView) helpableGUI).getFrame())) {
                    return doSearch(RequestDispatcher.TargetIndex.BOTH, searchPersonWrapper);
                }
            } else {
                if (!mpiRequestResult.isSuccessful()
                        && lpiRequestResult.isSuccessful()) {
                    lpiPersonList = (List<Person>) lpiRequestResult.getData();
                    if (helpableGUI.showConfirmMessage("The Master Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", ((MainView) helpableGUI).getFrame())) {
                        return doSearch(RequestDispatcher.TargetIndex.MPI, searchPersonWrapper);
                    }
                    if (!removeRejectedLpiCandidates(lpiPersonList).isEmpty()) {
                        return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(RequestDispatcher.TargetIndex.MPI, lpiPersonList));
                    } else {
                        return new ProcessResult(ProcessResult.Type.EXIT, null);
                    }
                } else if (!lpiRequestResult.isSuccessful()
                        && mpiRequestResult.isSuccessful()) {
                    mpiPersonList = (List<Person>) mpiRequestResult.getData();
                    if (helpableGUI.showConfirmMessage("The Local Person Index could not be contacted. "
                            + "Would you like to try contacting it again?", ((MainView) helpableGUI).getFrame())) {
                        return doSearch(RequestDispatcher.TargetIndex.LPI, searchPersonWrapper);
                    }
                    if (!removeRejectedMpiCandidates(mpiPersonList).isEmpty()) {
                        return new ProcessResult(ProcessResult.Type.LIST, new PersonIndexListData(RequestDispatcher.TargetIndex.MPI, mpiPersonList));
                    } else {
                        return new ProcessResult(ProcessResult.Type.EXIT, null);
                    }
                }
            }
            return new ProcessResult(ProcessResult.Type.UNREACHABLE_INDICES, null);
        }
    }

    public void startSession(Session.ClientType clientType) {
        session = new Session(clientType);
    }

    public void requireClinicId() {
        session.setClinicId(true);
    }

    public void doNotRequireClinicId() {
        session.setClinicId(false);
    }

    public void undoMpiResultDisplayed() {
        session.setMpiResultDisplayed(false);
    }

    public void undoLpiResultDisplayed() {
        session.setLpiResultDisplayed(false);
    }

    public boolean d() {
        return session.getClientType() == Session.ClientType.ENROLLED;
    }

    public boolean f() {
        return session.getClientType() == Session.ClientType.ENROLLED;
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
        if (targetIndex == RequestDispatcher.TargetIndex.MPI) {
            session.setMpiMatchPersonWrapper(null);
            saveRejectedMPICandidateList();
        } else if (targetIndex == RequestDispatcher.TargetIndex.LPI) {
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
        if (targetIndex == RequestDispatcher.TargetIndex.MPI) {
            session.setMpiMatchPersonWrapper(personWrapper);
            session.setRejectedMPICandidateList(null);
        } else if (targetIndex == RequestDispatcher.TargetIndex.LPI) {
            session.setLpiMatchPersonWrapper(personWrapper);
            session.setRejectedLPICandidateList(null);
        }
    }

    public boolean requiresClinicId() {
        return session.isClinicId();
    }

    public void createPerson(int targetIndex, PersonWrapper personWrapper) {
        RequestDispatcher.dispatch(personWrapper, session.getMpiRequestResult(), session.getLpiRequestResult(),
                RequestDispatcher.DispatchType.CREATE, targetIndex);
    }

    public void modifyPerson(int targetIndex, PersonWrapper personWrapper) {
        RequestDispatcher.dispatch(personWrapper, session.getMpiRequestResult(), session.getLpiRequestResult(),
                RequestDispatcher.DispatchType.MODIFY, targetIndex);
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
}
