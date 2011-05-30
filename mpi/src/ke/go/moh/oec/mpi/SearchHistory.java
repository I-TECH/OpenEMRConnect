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
package ke.go.moh.oec.mpi;

import ke.go.moh.oec.PersonRequest;

/**
 * Maintain a history of the MPI searches and matches.
 * 
 * @author Jim Grace
 */
public class SearchHistory {

    /**
     * Create a search history record for a new search.
     * 
     * @param req Search parameters.
     */
    public static void create(PersonRequest req) {
        // TODO: create a search_history record.
    }

    /**
     * Update a search history record to show the search results.
     * 
     * @param req Search parameters.
     * @param matched true if a candidate matched, false if it did not.
     */
    public static void update(PersonRequest req, boolean matched) {
        // TODO: update a search_history record.
    }
}
