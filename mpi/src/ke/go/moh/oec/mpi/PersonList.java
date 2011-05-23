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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jim Grace
 */
public class PersonList {
    
    private List<PersonMatch> personList = new ArrayList<PersonMatch>();
    private Map<String,PersonMatch> personMap = new HashMap<String,PersonMatch>();

    public boolean add(PersonMatch personMatch) {
        boolean returnValue = personList.add(personMatch);
        personMap.put(personMatch.getPerson().getPersonGuid(), personMatch);
        return returnValue;
    }
    
    public int size() {
        return personList.size();
    }
    
    public PersonMatch get(int index) {
        return personList.get(index);
    }
    
    public PersonMatch get(String personGuid) {
        return personMap.get(personGuid);
    }
}
