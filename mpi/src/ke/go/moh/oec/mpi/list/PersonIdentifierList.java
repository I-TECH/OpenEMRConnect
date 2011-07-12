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
package ke.go.moh.oec.mpi.list;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.mpi.Mpi;
import ke.go.moh.oec.mpi.Sql;
import ke.go.moh.oec.mpi.ValueMap;

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
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return personIdentifierList;
    }

    /**
     * Updates the list of person identifiers for a given person.
     * <p>
     * The agent doing the updating may be concerned with only one or more types
     * of person identifiers. So the update that takes place depends on the type(s) of
     * identifiers supplied. For any type of identifier supplied in the update,
     * all existing identifiers of that type are removed, and the new identifier(s)
     * of that type are inserted.
     * <p>
     * If the client wishes to remove all identifiers of a certain type, it should
     * do an update with a null or empty identifier of that type. In this case,
     * all existing identifiers of that type will be removed, and the new
     * null or empty identifier will not be added.
     * <p>
     * If the client wishes to replace an identifier of type cccUniqueId or cccLocalId
     * for a particular clinic, the client should specify one or more IDs of that
     * type with the given 5-digit clinic code. Only old IDs with the same clinic id
     * will be replaced by the new identifier(s).
     * <p>
     * If the client wishes to remove an identifier of type cccUniqueId or cccLocalId
     * for a particular clinic, the client should specify an IDs of that
     * type with the given 5-digit clinic code, but with nothing following the clinic
     * code. Any existing identifiers of that type with that clinic code will be
     * removed, and no new clinic IDs of that type for that clinic will be added.
     * 
     * @param conn database connection to use
     * @param personId database person.person_id primary key value
     * @param updateList list of PersonIdentifiers specified for update
     * @param piList existing list of PersonIdentifiers for the person
     * @return updated list of PersonIdentifiers for the person
     */
    public static List<PersonIdentifier> update(Connection conn, int personId, List<PersonIdentifier> updateList, List<PersonIdentifier> piList) {
        if (updateList != null && updateList.size() > 0) { // Do we have anything to update?
            List<PersonIdentifier> deleteList = new ArrayList<PersonIdentifier>(); // What person identifiers should we delete?
            List<PersonIdentifier> addList = new ArrayList<PersonIdentifier>(); // What person identifiers should we add?
            for (PersonIdentifier newP : updateList) { // Go through all the identifiers on the update list:
                PersonIdentifier.Type piType = newP.getIdentifierType();
                String id = newP.getIdentifier();
                String dbType = ValueMap.PERSON_IDENTIFIER_TYPE.getDb().get(piType);
                String sql;
                if (piList != null) { // Might we have any existing identifiers to delete?
                    for (PersonIdentifier oldP : piList) {
                        if (oldP.getIdentifierType() == piType) {
                            String existingId = oldP.getIdentifier();
                            if ((piType != PersonIdentifier.Type.cccLocalId && piType != PersonIdentifier.Type.cccUniqueId)
                                    || (id != null && id.substring(0, 5).equals(existingId.substring(0, 5)))) {
                                deleteList.add(oldP);
                                sql = "DELETE FROM person_identifier "
                                        + " WHERE person_id = " + personId
                                        + " AND identifier_type_id = " + dbType
                                        + " AND identifier = " + Sql.quote(existingId);
                                Sql.execute(conn, sql);
                            }
                        }
                    }
                }
                if (id != null && !id.isEmpty()) { // If have a new identifier, then add it.
//                    if (!(piType == PersonIdentifier.Type.cccLocalId || piType == PersonIdentifier.Type.cccUniqueId)
//                            && id.length() <= 6) {
                        addList.add(newP);
                        sql = "INSERT INTO person_identifier (person_id, identifier_type_id, identifier) values ("
                                + personId + ", " + dbType + ", " + Sql.quote(id) + ")";
                        Sql.execute(conn, sql);
//                    }
                }
            }
            if (!deleteList.isEmpty()) { // Do we have anything to remove from our in-memory list?
                piList.removeAll(deleteList);
            }
            if (!addList.isEmpty()) { // Do we have anything to add to our in-memory list?
                if (piList == null) {
                    piList = new ArrayList<PersonIdentifier>();
                }
                piList.addAll(addList);
            }
        }
        return piList;
    }
}
