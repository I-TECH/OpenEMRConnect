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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.PersonIdentifier;

/**
 * MPI Methods to operate on a list of person identifiers.
 * 
 * @author Jim Grace
 */
public class PersonIdentifierList {

    /**
     * Loads into memory the person identifiers for a given person.
     *
     * @param conn Connection on which to do the query.
     * @param dbPersonId Internal database ID for the person associated with these identifiers.
     * @return The list of person identifiers.
     */
    public static List<PersonIdentifier> load(Connection conn, int dbPersonId) {
        List<PersonIdentifier> personIdentifierList = null;
        String sql = "SELECT pi.identifier, pi.identifier_type_id\n"
                + "FROM person_identifier pi\n"
                + "WHERE pi.person_id = " + dbPersonId;
        ResultSet rs = Sql.query(conn, sql, Level.FINEST);
        try {
            while (rs.next()) {
                if (personIdentifierList == null) {
                    personIdentifierList = new ArrayList<PersonIdentifier>();
                }
                String idType = Integer.toString(rs.getInt("identifier_type_id"));
                PersonIdentifier.Type pit = (PersonIdentifier.Type) ValueMap.PERSON_IDENTIFIER_TYPE.getVal().get(idType);
                PersonIdentifier pi = new PersonIdentifier();
                pi.setIdentifier(rs.getString("identifier"));
                pi.setIdentifierType(pit);
                personIdentifierList.add(pi);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return personIdentifierList;
    }
}
