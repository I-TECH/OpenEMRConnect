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
import ke.go.moh.oec.Fingerprint;

/**
 * MPI Methods to operate on a list of fingerprints.
 *
 * @author Jim Grace
 */
public class FingerprintList {

    /**
     * Loads into memory the fingerprints for a given person.
     *
     * @param conn Connection on which to do the query.
     * @param dbPersonId Internal database ID for the person associated with these fingerprints.
     * @return The list of fingerprints.
     */
    public static List<Fingerprint> load(Connection conn, int dbPersonId) {
        List<Fingerprint> fingerprintList = null;
        String sql = "SELECT f.fingerprint_template, f.fingerprint_type_id, f.fingerprint_technology_type_id\n"
                + "FROM fingerprint f\n"
                + "WHERE f.person_id = " + dbPersonId;
        ResultSet rs = Sql.query(conn, sql, Level.FINEST);
        try {
            while (rs.next()) {
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
            }
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return fingerprintList;
    }

    public static void update(Connection conn, int personId, List<Fingerprint> fingerprintList) {
        if (fingerprintList != null && fingerprintList.size() > 0) {
            String sql = "DELETE FROM fingerprint WHERE person_id = " + personId;
            Sql.execute(conn, sql);
            for (Fingerprint f : fingerprintList) {
                String fingerprintTypeId = ValueMap.FINGERPRINT_TYPE.getDb().get(f.getFingerprintType());
                String technologyTypeId = ValueMap.FINGERPRINT_TECHNOLOGY_TYPE.getDb().get(f.getTechnologyType());
                byte[] template = f.getTemplate();
                sql = "INSERT INTO fingerprint (person_id, fingerprint_type_id, fingerprint_technology_type_id, fingerprint_template) VALUES (\n"
                        + personId + ", " + fingerprintTypeId + ", " + technologyTypeId + ",\n"
                        + Sql.quote(template) + ")";
                Sql.execute(conn, sql);
            }
        }
    }
}
