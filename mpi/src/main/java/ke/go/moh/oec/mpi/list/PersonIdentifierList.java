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
import ke.go.moh.oec.mpi.Sql;
import ke.go.moh.oec.mpi.ValueMap;

/**
 * MPI Methods to operate on a list of person identifiers.
 * 
 * @author Jim Grace
 */
public class PersonIdentifierList {

    private Connection conn;    // Connection for loading fingerprints
    private ResultSet rs;       // Result set for loading fingerprints
    private boolean rsNext;     // Is there a next record in the result set?
    private int personId;       // Remember current person ID.

    /**
     * Positions to the next person identifier record and gets the personId from that record.
     * This is encapsulated mainly to insure that rs.getInt() is only called once per record
     * for the person_id field. It turns out that this call is moderately CPU expensive
     * since it parses the database value into an integer every time. So we do this call
     * only once and save the integer result. We may then reference it more than once.
     */
    private void next() {
        try {
            rsNext = rs.next();
            if (rsNext) {
                personId = rs.getInt("person_id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(PersonIdentifierList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Starts loading person identifiers into memory.
     * <p>
     * For efficiency, person identifiers for all patients are loaded by a single query,
     * in order by personId. Then a merge sort on person identifiers is done between the
     * loaded person identifiers list and the loaded person list.
     */
    public void loadStart(int minPersonId, int maxPersonId) {
        conn = Sql.connect();
        String sql = "SELECT pi.person_id, pi.identifier, pi.identifier_type_id\n"
                + "FROM person_identifier pi\n"
                + "WHERE pi.person_id BETWEEN " + minPersonId + " AND " + maxPersonId + "\n"
                + "ORDER BY pi.person_id";
        rs = Sql.query(conn, sql, Level.FINEST);
        next();     // Position at the first result record.
    }

    /**
     * Loads the next person identifiers into memory matching the given personId.
     * 
     * @param dbPersonId Database person_id value for person identifiers to load.
     * @return list of person identifiers for this person.
     */
    public List<PersonIdentifier> loadNext(int dbPersonId) {
        List<PersonIdentifier> personIdentifierList = null;
        try {
            // If we haven't yet reached the requested personId, skip over any
            // database records that come before that personId.
            while (rsNext && personId < dbPersonId) {
                next();
            }
            // While we are matching the requested personId, include any
            // person identifiers that match that Id.
            while (rsNext && personId == dbPersonId) {
                if (personIdentifierList == null) {
                    personIdentifierList = new ArrayList<PersonIdentifier>();
                }
                String idType = Integer.toString(rs.getInt("identifier_type_id"));
                PersonIdentifier.Type pit = (PersonIdentifier.Type) ValueMap.PERSON_IDENTIFIER_TYPE.getVal().get(idType);
                PersonIdentifier pi = new PersonIdentifier();
                pi.setIdentifier(rs.getString("identifier"));
                pi.setIdentifierType(pit);
                personIdentifierList.add(pi);
                next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(FingerprintList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return personIdentifierList;
    }

    /**
     * Ends loading fingerprints.
     * Releases any resources used.
     */
    public void loadEnd() {
        Sql.close(rs);
        Sql.close(conn);
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
            String sql;
            for (PersonIdentifier newP : updateList) { // Go through all the identifiers on the update list:
                PersonIdentifier.Type piType = newP.getIdentifierType();
                String id = newP.getIdentifier();
                String dbType = ValueMap.PERSON_IDENTIFIER_TYPE.getDb().get(piType);
                boolean idExists = false; // Does the new ID exist in the current list?
                if (piList != null) { // Might we have any existing identifiers to delete?
                    for (PersonIdentifier oldP : piList) {
                        if (oldP.getIdentifierType() == piType) {
                            String existingId = oldP.getIdentifier();
                            if (existingId.equals(id)) {
                                idExists = true; // Existing ID matches new ID -- don't need to delete or add.
                            } else if ((piType != PersonIdentifier.Type.cccLocalId && piType != PersonIdentifier.Type.cccUniqueId)
                                    || (id != null && id.substring(0, 5).equals(existingId.substring(0, 5)))) {
                                deleteList.add(oldP);
                            }
                        }
                    }
                }
                if (id != null && !id.isEmpty() && !idExists) { // If have a new identifier (and not already existing), then add it.
                    // Make sure we don't add a clinic ID of the form '12345' or '12345-'
                    // If this form is specified, it is a spacial request to delete any IDs from this clinic
                    // but to not add a new ID for this clinic. (See the JavaDoc for this method, above.)
                    if (!((piType == PersonIdentifier.Type.cccLocalId || piType == PersonIdentifier.Type.cccUniqueId)
                            && id.length() <= 6)) {
                        addList.add(newP);
                    }
                }
            }
            if (!deleteList.isEmpty()) { // Do we have any identifiers to delete from the database / remove from memory?
                for (PersonIdentifier pi : deleteList) {
                    String dbType = ValueMap.PERSON_IDENTIFIER_TYPE.getDb().get(pi.getIdentifierType());
                    sql = "DELETE FROM person_identifier "
                            + " WHERE person_id = " + personId
                            + " AND identifier_type_id = " + dbType
                            + " AND identifier = " + Sql.quote(pi.getIdentifier());
                    Sql.execute(conn, sql);
                }
                piList.removeAll(deleteList);
            }
            if (!addList.isEmpty()) { // Do we have anything to insert into the database / add to memory?
                for (PersonIdentifier pi : addList) {
                    String dbType = ValueMap.PERSON_IDENTIFIER_TYPE.getDb().get(pi.getIdentifierType());
                    sql = "INSERT INTO person_identifier (person_id, identifier_type_id, identifier) values ("
                            + personId + ", " + dbType + ", " + Sql.quote(pi.getIdentifier()) + ")";
                    Sql.execute(conn, sql);
                }
                if (piList == null) {
                    piList = new ArrayList<PersonIdentifier>();
                }
                piList.addAll(addList);
            }
        }
        return piList;
    }
}
