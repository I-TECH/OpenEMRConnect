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
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.mpi.Mpi;
import ke.go.moh.oec.mpi.Sql;
import ke.go.moh.oec.mpi.ValueMap;

/**
 * MPI Methods to operate on a list of fingerprints.
 *
 * @author Jim Grace
 */
public class FingerprintList {

    private Connection conn;    // Connection for loading fingerprints
    private ResultSet rs;       // Result set for loading fingerprints
    private boolean rsNext;     // Is there a next record in the result set?

    /**
     * Starts loading fingerprints into memory.
     * <p>
     * For efficiency, fingerprints for all patients are loaded by a single query,
     * in order by personId. Then a merge sort on fingerprintId is done between the
     * loaded fingerprint list and the loaded person list.
     */
    public void loadStart() {
        conn = Sql.connect();
        String sql = "SELECT f.person_id, f.fingerprint_template, f.fingerprint_type_id, f.fingerprint_technology_type_id\n"
                + "FROM fingerprint f\n"
                + "ORDER BY f.person_id";
        rs = Sql.query(conn, sql, Level.FINEST);
        try {
            rsNext = rs.next();
        } catch (SQLException ex) {
            Logger.getLogger(FingerprintList.class.getName()).log(Level.SEVERE, null, ex);
            rsNext = false;
        }
    }

    /**
     * Loads the next fingerprint into memory matching the given personId.
     * 
     * @param dbPersonId Database person_id value for fingerprints to load.
     * @return list of fingerprints for this person.
     */
    public List<Fingerprint> loadNext(int dbPersonId) {
        List<Fingerprint> fingerprintList = null;
        try {
            // If we haven't yet reached the requested personId, skip over any
            // database records that come before that personId.
            while (rsNext && rs.getInt("person_id") < dbPersonId) {
                rsNext = rs.next();
            }
            // While we are matching the requested personId, include any
            // fingerprints that match that Id.
            while (rsNext && rs.getInt("person_id") == dbPersonId) {
                if (fingerprintList == null) {
                    fingerprintList = new ArrayList<Fingerprint>();
                }
                String typeId = Integer.toString(rs.getInt("fingerprint_type_id"));
                Fingerprint.Type fingerprintType = (Fingerprint.Type) ValueMap.FINGERPRINT_TYPE.getVal().get(typeId);
                String technologyTypeId = Integer.toString(rs.getInt("fingerprint_technology_type_id"));
                Fingerprint.TechnologyType technologyType = (Fingerprint.TechnologyType) ValueMap.FINGERPRINT_TECHNOLOGY_TYPE.getVal().get(technologyTypeId);
                Fingerprint f = new Fingerprint();
                f.setTemplate(rs.getBytes("fingerprint_template"));
                f.setFingerprintType(fingerprintType);
                f.setTechnologyType(technologyType);
                fingerprintList.add(f);
                rsNext = rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(FingerprintList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fingerprintList;
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
     * Updates list of fingerprints for a person.
     * If no new fingerprints are given, then nothing is changed
     * (existing fingerprints, if any, remain in place).
     * If new fingerprints are given, then any old fingerprints are
     * removed, and the new ones inserted.
     * <p>
     * If a new fingerprint is given but is of zero length, then
     * any old fingerprints are removed, and the new zero-length fingerprint
     * is not inserted. This is the way that an update can remove any
     * existing fingerprints.
     * 
     * @param conn
     * @param personId
     * @param newList new list of fingerprints (if any) 
     */
    public static List<Fingerprint> update(Connection conn, int personId, List<Fingerprint> newList, List<Fingerprint> oldList) {
        List<Fingerprint> returnList = new ArrayList<Fingerprint>();
        boolean newEntries = (newList != null && newList.size() > 0);
        boolean oldEntries = (oldList != null && oldList.size() > 0);
        if (newEntries) {
            String sql;
            if (oldEntries) {
                sql = "DELETE FROM fingerprint WHERE person_id = " + personId;
                Sql.execute(conn, sql);
            }
            for (Fingerprint f : newList) {
                byte[] template = f.getTemplate();
                if (template != null && template.length > 0) {
                    returnList.add(f);
                    String fingerprintTypeId = ValueMap.FINGERPRINT_TYPE.getDb().get(f.getFingerprintType());
                    String technologyTypeId = ValueMap.FINGERPRINT_TECHNOLOGY_TYPE.getDb().get(f.getTechnologyType());
                    sql = "INSERT INTO fingerprint (person_id, fingerprint_type_id, fingerprint_technology_type_id, fingerprint_template) VALUES (\n"
                            + personId + ", " + fingerprintTypeId + ", " + technologyTypeId + ",\n"
                            + Sql.quote(template) + ")";
                    Sql.execute(conn, sql);
                }
            }
        } else if (oldEntries) {
            returnList = oldList;
        }
        if (returnList.isEmpty()) {
            returnList = null;
        }
        return returnList;
    }
}
