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

import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.reception.controller.PersonWrapper;
import ke.go.moh.oec.reception.controller.RequestDispatcher;
import ke.go.moh.oec.reception.data.RequestResult;
import ke.go.moh.oec.reception.reader.ReaderManager;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class RequestMarshaller {

    public static void doSearch(ReaderManager readerManager, RequestResult mpiRequestResult, RequestResult lpiRequestResult) {
        PersonWrapper personWrapper = new PersonWrapper(new Person());
        Fingerprint fingerprint = new Fingerprint();
        fingerprint.setFingerprintType(Fingerprint.Type.rightIndexFinger);
        fingerprint.setTechnologyType(Fingerprint.TechnologyType.griauleTemplate);
        fingerprint.setTemplate(readerManager.getTemplate().getData());
        personWrapper.addFingerprint(fingerprint);
        RequestDispatcher.dispatch(personWrapper, mpiRequestResult, lpiRequestResult,
                RequestDispatcher.DispatchType.FIND,
                RequestDispatcher.TargetIndex.BOTH);
    }
}
