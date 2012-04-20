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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.mpi.match.PersonMatch;

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
        String sourceAddress = req.getSourceAddress();
        String messageId = req.getRequestReference();
        if (sourceAddress != null && messageId != null) { // Could be null for a local test, in which case don't log.
            Connection conn = Sql.connect();
            String addressId = Sql.getAddressId(conn, sourceAddress);
            Person p = req.getPerson();
            String linkedId = null;
            String sql = "SELECT max(search_history_id) as id FROM search_history WHERE address_id = " + addressId
                    + " and message_id = " + Sql.quote(messageId);
            ResultSet rs = Sql.query(conn, sql);
            try {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    if (id != 0) {
                        linkedId = Integer.toString(id);
                    }
                }
                Sql.close(rs);
            } catch (SQLException ex) {
                Logger.getLogger(SearchHistory.class.getName()).log(Level.SEVERE,
                        "create() error getting max(search_history_id) for address_id = "
                        + addressId + " and message_id = " + messageId, ex);
            }
            String sex = ValueMap.SEX.getDb().get(p.getSex());
            sql = "INSERT INTO search_history (address_id, message_id, linked_search_id, search_datetime,\n"
                    + "s_first_name, s_middle_name, s_last_name, s_birthdate, s_sex, s_clan_name,\n"
                    + "s_village_name, s_site_name, s_guid) VALUES ("
                    + addressId + ", "
                    + Sql.quote(messageId) + ", "
                    + Sql.quote(linkedId) + ", "
                    + "NOW(),\n"
                    + Sql.quote(p.getFirstName()) + ", "
                    + Sql.quote(p.getMiddleName()) + ", "
                    + Sql.quote(p.getLastName()) + ", "
                    + Sql.quote(p.getBirthdate()) + ", "
                    + Sql.quote(sex) + ", "
                    + Sql.quote(p.getClanName()) + ",\n"
                    + Sql.quote(p.getVillageName()) + ", "
                    + Sql.quote(p.getSiteName()) + ", "
                    + Sql.quote(p.getPersonGuid()) + ")";
            try {
                Sql.execute(conn, sql);
            } catch (Exception ex) {
                Logger.getLogger(SearchHistory.class.getName()).log(Level.SEVERE,
                        "Error inserting into search_history:\n" + sql, ex);
                Sql.close(conn);
                return;
            }
            String searchHistoryId = Sql.getLastInsertId(conn);
            List<PersonIdentifier> piList = p.getPersonIdentifierList();
            if (piList != null) {
                for (PersonIdentifier pi : piList) {
                    PersonIdentifier.Type piType = pi.getIdentifierType();
                    String dbType = ValueMap.PERSON_IDENTIFIER_TYPE.getDb().get(piType);
                    sql = "INSERT INTO search_history_person_identifier (search_history_id, identifier_type_id, identifier) VALUES (\n"
                            + searchHistoryId + ", "
                            + Sql.quote(dbType) + ", "
                            + Sql.quote(pi.getIdentifier()) + ")";
                    try {
                        Sql.execute(conn, sql);
                    } catch (Exception ex) {
                        Logger.getLogger(SearchHistory.class.getName()).log(Level.SEVERE,
                                "Error inserting into search_history_person_identifier:\n" + sql, ex);
                        Sql.close(conn);
                        return;
                    }
                    Sql.execute(conn, sql);
                }
            }
            List<Fingerprint> fList = p.getFingerprintList();
            if (fList != null) {
                for (Fingerprint f : fList) {
                    Fingerprint.Type fType = f.getFingerprintType();
                    String dbType = ValueMap.FINGERPRINT_TYPE.getDb().get(fType);
                    sql = "INSERT INTO search_history_fingerprint (search_history_id, fingerprint_type_id,\n"
                            + "fingerprint_template, fingerprint_technology_type_id) VALUES (\n"
                            + searchHistoryId + ", "
                            + dbType + ", "
                            + Sql.quote(f.getTemplate()) + ", "
                            + "1)";
                    try {
                        Sql.execute(conn, sql);
                    } catch (Exception ex) {
                        Logger.getLogger(SearchHistory.class.getName()).log(Level.SEVERE,
                                "Error inserting into search_history_fingerprint:\n" + sql, ex);
                        Sql.close(conn);
                        return;
                    }
                }
            }
            Sql.close(conn);
        }
    }

    /**
     * Update a search history record to show the search results.
     * 
     * @param req Search parameters.
     * @param pm found person (if any), null if person was not found.
     * @param person person object (if any), from the user
     */
    public static void update(PersonRequest req, PersonMatch pm, Person person) {
        String sourceAddress = req.getSourceAddress();
        String messageId = req.getRequestReference();
        if (sourceAddress != null && messageId != null) { // Could be null for a local test, in which case don't log.
            Connection conn = Sql.connect();
            String addressId = Sql.getAddressId(conn, sourceAddress);
            String linkedId = null;
            String sql = "SELECT max(search_history_id) as id FROM search_history WHERE address_id = " + addressId
                    + " and message_id = " + Sql.quote(messageId);
            ResultSet rs = Sql.query(conn, sql);
            try {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    if (id != 0) {
                        linkedId = Integer.toString(id);
                    }
                }
                Sql.close(rs);
            } catch (SQLException ex) {
                Logger.getLogger(SearchHistory.class.getName()).log(Level.SEVERE,
                        "update() error getting max(search_history_id) for address_id = "
                        + addressId + " and message_id = " + messageId, ex);
                Sql.close(conn);
                return;
            }
            /*
             * Only try to update search_history if this create or update person request
             * referred to a previous search. Otherwise, it may be an unrealted
             * create or update person request, in which case there is no problem.
             */
            if (linkedId != null) {
                if (pm == null) { // No person, so the user did not pick one of the candidates:
                    sql = "UPDATE search_history SET outcome = 0, m_datetime = NOW() WHERE address_id = " + addressId
                            + " and message_id = " + Sql.quote(messageId);
                } else { // The user picked a candidate. The user might also be changing some of the
                    Person p = pm.getPerson();
                    String sex = ValueMap.SEX.getDb().get(p.getSex());
                    sql = "UPDATE search_history SET outcome = 1, m_datetime = NOW()"
                            + ", m_person_id = " + pm.getDbPersonId()
                            + ", m_first_name = " + Sql.quote(p.getFirstName())
                            + ", m_middle_name = " + Sql.quote(p.getMiddleName())
                            + ", m_last_name = " + Sql.quote(p.getLastName())
                            + ", m_birthdate = " + Sql.quote(p.getBirthdate())
                            + ", m_sex = " + Sql.quote(sex)
                            + ", m_clan_name = " + Sql.quote(p.getClanName())
                            + ", m_village_name = " + Sql.quote(p.getVillageName())
                            + " WHERE address_id = " + addressId
                            + " and message_id = " + Sql.quote(messageId);
                }
                try {
                    Sql.execute(conn, sql);
                } catch (Exception ex) {
                    Logger.getLogger(SearchHistory.class.getName()).log(Level.SEVERE,
                            "Error updating search_history:\n" + sql, ex);
                }
            }
            Sql.close(conn);
        }
    }
}
