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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonRequest;

/**
 *
 * @author Jim Grace
 */
public class CreatePerson {
    
    public void create(PersonList personList, PersonRequest req) {
        Person p = req.getPerson();
        if (p == null) {
            Logger.getLogger(ModifyPerson.class.getName()).log(Level.SEVERE, "MODIFY PERSON called with no person data.");
            return;
        }
        Connection conn = Mpi.dbConnect();
        ResultSet rs = Mpi.query(conn, "select uuid() as uuid");
        String guid = null;
        try {
            rs.next();
            guid = rs.getString("uuid");
        } catch (SQLException ex) { // Won't happen
            Logger.getLogger(CreatePerson.class.getName()).log(Level.SEVERE, null, ex);
        }
        p.setPersonGuid(guid);
        Mpi.execute(conn, "start transaction;");
        String sql = "INSERT INTO person (person_guid, first_name, middle_name, last_name, "
                + "other_name, sex, birthdate, deathdate, "
                + "mothers_first_name, mothers_middle_name, mothers_last_name, "
                + "fathers_first_name, fathers_middle_name, fathers_last_name, "
                + "mothers_first_name, mothers_middle_name, mothers_last_name, "
                + "compound_head_first_name, compound_head_middle_name, compound_head_last_name, "
                + "village_id, marital_status, consent_signed, date_created) values ("
                + Mpi.quote(p.getPersonGuid()) + ", "
                + Mpi.quote(p.getFirstName()) + ", "
                + Mpi.quote(p.getMiddleName()) + ", "
                + Mpi.quote(p.getLastName()) + ", "
                + Mpi.quote(p.getOtherName()) + ", "
                + Mpi.quote(p.getSex().name()) + ", ";
        
        // TO DO: Finish code
    }
}
