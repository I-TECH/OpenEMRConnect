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
     * <code>requestData</code>: {@link PersonRequest} object, filled
     * with any person attribute(s) to search for.
     * <p>
     * returns: {@link PersonResponse} - Zero or more search
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
     * <code>requestData</code>: {@link PersonRequest} object, filled
     * with any person attribute(s) to search for.
     * <p>
     * returns: {@link PersonResponse} - Zero or more search
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
     * <code>requestData</code>: {@link PersonRequest} object, filled
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
     * <code>requestData</code>: {@link PersonRequest} object, filled
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
     * <code>requestData</code>: {@link PersonRequest} object, filled
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
     * <code>requestData</code>: {@link PersonRequest} object, filled
     * with the changed attribute(s) to assign to the existing person.
     * <p>
     * returns: <code>null</code>
     */
    public final static int MODIFY_PERSON_LPI = 6;
    /**
     * Notifies of a change in person registry information.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with NOTIFY_PERSON_CHANGED, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link PersonRequest} object, identifying
     * the person whose clinical document is requested.
     * <p>
     * returns: {@link PersonResponse} - The clinical document containing
     * clinical information for the person.
     */
    public final static int NOTIFY_PERSON_CHANGED = 7;
    /**
     * Requests data about the person from the HDSS.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with FIND_PERSON_HDSS, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link PersonRequest} object, identifying
     * the person whose clinical document is requested.
     * <p>
     * returns: {@link ClinicalDocument} - The clinical document containing
     * clinical information for the person.
     */
    public final static int FIND_PERSON_HDSS = 8;
    /**
     * Transmits a log entry to the logging server.
     * <p>
     * When calling {@link IService#getData(int, java.lang.Object)}
     * with LOG_ENTRY, the <code>requestData</code>
     * parameter object and the returned object are follows:
     * <p>
     * <code>requestData</code>: {@link LogEntry} - The information
     * to be logged.
     * <p>
     * returns: <code>null</code>
     */
    public final static int LOG_ENTRY = 1000;
}
