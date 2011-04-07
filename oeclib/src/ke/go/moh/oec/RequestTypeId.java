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
package ke.go.moh.oec;

/**
 * Define data request type IDs for the getData() method of the IService interface.
 * <p>
 * These are defined as integers, rather than an enumerated type, to avoid
 * problems of compatibility between versions of the OEC library. This allows
 * a client to build against an old version of the library but use a new
 * version of the library where additional request type IDs have been defined.
 *
 * @author Jim Grace
 */
public class RequestTypeId {

    /**
     * Searches the Master Patient Index to find a person.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with FIND_PERSON_MPI, the <code>requestData</code> parameter object
     * and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, filled
     * with any person attribute(s) to search for.
     * <p>
     * returns: <code>List<{@link Person}></code> - Zero or more search
     * results matching (exactly or approximately) the search parameters.
     */
    public final static int FIND_PERSON_MPI = 1;
    /**
     * Searches the Local Patient Index to find a person.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with FIND_PERSON_LPI, the <code>requestData</code> parameter object
     * and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, filled
     * with any person attribute(s) to search for.
     * <p>
     * returns: <code>List<{@link Person}></code> - Zero or more search
     * results matching (exactly or approximately) the search parameters.
     */
    public final static int FIND_PERSON_LPI = 2;
    /**
     * Requests the Master Patient Index to create a new person entry.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with CREATE_PERSON_MPI, the <code>requestData</code> parameter object
     * and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, filled
     * with the attribute to assign to the new person.
     * <p>
     * returns: <code>null</code>
     */
    public final static int CREATE_PERSON_MPI = 3;
    /**
     * Requests the Local Patient Index to create a new person entry.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with CREATE_PERSON_LPI, the <code>requestData</code> parameter object
     * and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, filled
     * with the attribute to assign to the new person.
     * <p>
     * returns: <code>null</code>
     */
    public final static int CREATE_PERSON_LPI = 4;
    /**
     * Requests the Master Patient Index to modify an existing person entry.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with MODIFY_PERSON_MPI, the <code>requestData</code> parameter object
     * and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, filled
     * with the changed attribute(s) to assign to the existing person.
     * <p>
     * returns: <code>null</code>
     */
    public final static int MODIFY_PERSON_MPI = 5;
    /**
     * Requests the Local Patient Index to modify an existing person entry.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with MODIFY_PERSON_LPI, the <code>requestData</code> parameter object
     * and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, filled
     * with the changed attribute(s) to assign to the existing person.
     * <p>
     * returns: <code>null</code>
     */
    public final static int MODIFY_PERSON_LPI = 6;
    /**
     * Requests from the HDSS a clinical document for the person.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with GET_CLINICAL_DOCUMENT_HDSS, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, identifying
     * the person whose clinical document is requested.
     * <p>
     * returns: {@link ClinicalDocument} - The clinical document containing
     * clinical information for the person.
     */
    public final static int GET_CLINICAL_DOCUMENT_HDSS = 7;
    /**
     * Requests from the local Clinical Document Store a clinical document
     * for the person.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with GET_CLINICAL_DOCUMENT_CDS, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link Person} object, identifying
     * the person whose clinical document is requested.
     * <p>
     * returns: {@link ClinicalDocument} - The clinical document containing
     * clinical information for the person.
     */
    public final static int GET_CLINICAL_DOCUMENT_CDS = 8;
    /**
     * Transmits a {@link ClinicalDocument}. This can be either in response to
     * a get clinical document request, or as a proactive notification when
     * clinical information has changed.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with SET_CLINICAL_DOCUMENT, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link ClinicalDocument} - The clinical
     * document containing clinical information for the person.
     * <p>
     * returns: <code>null</code>
     */
    public final static int SET_CLINICAL_DOCUMENT = 9;
    /**
     * Transmits a log entry to the logging server.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with SEND_LOG_ENTRY, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link LogEntry} - The information
     * to be logged.
     * <p>
     * returns: <code>null</code>
     */
    public final static int SEND_LOG_ENTRY = 1000;
}
