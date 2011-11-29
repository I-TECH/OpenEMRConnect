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
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.lib.Mediator;
import ke.go.moh.oec.mpi.list.FingerprintList;
import ke.go.moh.oec.mpi.list.PersonIdentifierList;
import ke.go.moh.oec.mpi.match.PersonMatch;

/**
 * One thread that can be used for concurrent processing to load the person list into memory
 *
 * @author Jim Grace
 */
public class LoadPersonThread implements Runnable {

    /** The list of person identities in memory against which we will search for candidates. */
    private List<PersonMatch> personMatchList;
    /** Index of this thread (used for debugging and tracing purposes.) */
    private int threadIndex;
    /** The minimum personId we will get from the database. */
    private int minPersonId;
    /** The maximum personId we will get from the database. */
    private int maxPersonId;

    public LoadPersonThread(int threadIndex, int minPersonId, int maxPersonId) {
        this.threadIndex = threadIndex;
        this.minPersonId = minPersonId;
        this.maxPersonId = maxPersonId;
    }

    public List<PersonMatch> getPersonMatchList() {
        return personMatchList;
    }

    /**
     * Do the person matching requested for this thread.
     * <p>
     * Implements java.lang.Runnable.run().
     */
    public void run() {
        long startTime = System.currentTimeMillis();
        Mediator.getLogger(LoadPersonThread.class.getName()).log(Level.FINE,
                "LoadPersonThread {0} starting.", threadIndex);
        personMatchList = new ArrayList<PersonMatch>(); // Our output is collected here, to be retrieved later.
        Connection conn = Sql.connect();
        Calendar cal = Calendar.getInstance(); // Default Calendar, used for getting database date fields.
        String sql = "SELECT p.person_id, p.person_guid, p.sex, p.birthdate, p.deathdate,\n"
                + "       p.first_name, p.middle_name, p.last_name, p.other_name, p.clan_name,\n"
                + "       p.mothers_first_name, p.mothers_middle_name, p.mothers_last_name,\n"
                + "       p.fathers_first_name, p.fathers_middle_name, p.fathers_last_name,\n"
                + "       p.compoundhead_first_name, p.compoundhead_middle_name, p.compoundhead_last_name,\n"
                + "       v.village_name, m.marital_status_name, p.consent_signed,\n"
                + "       MAX(v_reg.visit_date) AS visit_reg_date,\n"
                + "       MAX(v_one.visit_date) AS visit_one_date,\n"
                + "       MID(MAX(CONCAT(v_reg.visit_date, a_reg.address)),20) AS visit_reg_address,\n"
                + "       MID(MAX(CONCAT(v_one.visit_date, a_one.address)),20) AS visit_one_address,\n"
                + "       MID(MAX(CONCAT(v_reg.visit_date, a_reg.facility_name)),20) AS visit_reg_facility\n"
                + "FROM person p\n"
                + "LEFT OUTER JOIN village v ON v.village_id = p.village_id\n"
                + "LEFT OUTER JOIN marital_status_type m ON m.marital_status_type_id = p.marital_status\n"
                + "LEFT OUTER JOIN visit v_reg ON v_reg.person_id = p.person_id and v_reg.visit_type_id = " + Sql.REGULAR_VISIT_TYPE_ID + "\n"
                + "LEFT OUTER JOIN visit v_one ON v_one.person_id = p.person_id and v_one.visit_type_id = " + Sql.ONE_OFF_VISIT_TYPE_ID + "\n"
                + "LEFT OUTER JOIN address a_reg ON a_reg.address_id = v_reg.address_id\n"
                + "LEFT OUTER JOIN address a_one ON a_one.address_id = v_one.address_id\n" //Gitau added to enable passing of facility name to Visit.getvisit
                + "WHERE p.person_id BETWEEN " + minPersonId + " AND " + maxPersonId + " -- Thread " + threadIndex + "\n" // Comment for logging.
                + "GROUP BY p.person_id\n"
                + "ORDER BY p.person_id";
        ResultSet rs = Sql.query(conn, sql);
        int recordCount = 0;
        FingerprintList fingerprintList = new FingerprintList();
        fingerprintList.loadStart(minPersonId, maxPersonId);
        PersonIdentifierList personIdentifierList = new PersonIdentifierList();
        personIdentifierList.loadStart(minPersonId, maxPersonId);
        try {
            // Define ingeger variables to hold column index numbers.
            //
            // When loading the MPI from the database, it is substantially faster to
            // access the recordset by column number than by column name. This was
            // discovered by CPU profiling of the original code which used column name.
            //
            // On the other hand, it would be less code just to refer to the column numbers
            // rather than go through the trouble of defining integers and then looking
            // up the column indices. But this would be more error prone, in the event
            // that the list of columns is ever changed. So we define ingteger variables
            // for all the column indices, and then load them on the first result set.
            int colPersonId = 0, colPersonGuid = 0, colSex = 0, colBirthdate = 0, colDeathdate = 0,
                    colFirstName = 0, colMiddleName = 0, colLastName = 0, colOtherName = 0, colClanName = 0,
                    colMothersFirstName = 0, colMothersMiddleName = 0, colMothersLastName = 0,
                    colFathersFirstName = 0, colFathersMiddleName = 0, colFathersLastName = 0,
                    colCompoundheadFirstName = 0, colCompoundheadMiddleName = 0, colCompoundheadLastName = 0,
                    colVillageName = 0, colMaritalStatusName = 0, colConsentSigned = 0,
                    colVisitRegDate = 0, colVisitOneDate = 0, colVisitRegAddress = 0,colVisitRegFacility = 0, colVisitOneAddress = 0;
            boolean colsFound = false; // Have we found the column numbers yet?
            while (rs.next()) {
                if (!colsFound) {
                    colPersonId = rs.findColumn("person_id");
                    colPersonGuid = rs.findColumn("person_guid");
                    colSex = rs.findColumn("sex");
                    colBirthdate = rs.findColumn("birthdate");
                    colDeathdate = rs.findColumn("deathdate");
                    colFirstName = rs.findColumn("first_name");
                    colMiddleName = rs.findColumn("middle_name");
                    colLastName = rs.findColumn("last_name");
                    colOtherName = rs.findColumn("other_name");
                    colClanName = rs.findColumn("clan_name");
                    colMothersFirstName = rs.findColumn("mothers_first_name");
                    colMothersMiddleName = rs.findColumn("mothers_middle_name");
                    colMothersLastName = rs.findColumn("mothers_last_name");
                    colFathersFirstName = rs.findColumn("fathers_first_name");
                    colFathersMiddleName = rs.findColumn("fathers_middle_name");
                    colFathersLastName = rs.findColumn("fathers_last_name");
                    colCompoundheadFirstName = rs.findColumn("compoundhead_first_name");
                    colCompoundheadMiddleName = rs.findColumn("compoundhead_middle_name");
                    colCompoundheadLastName = rs.findColumn("compoundhead_last_name");
                    colVillageName = rs.findColumn("village_name");
                    colMaritalStatusName = rs.findColumn("marital_status_name");
                    colConsentSigned = rs.findColumn("consent_signed");
                    colVisitRegDate = rs.findColumn("visit_reg_date");
                    colVisitOneDate = rs.findColumn("visit_reg_address");
                    colVisitRegAddress = rs.findColumn("visit_one_date");
                    colVisitOneAddress = rs.findColumn("visit_one_address");
                    colVisitRegFacility = rs.findColumn("visit_reg_facility");
                    colsFound = true;
                }
                Person p = new Person();
                int dbPersonId = rs.getInt(colPersonId);
                p.setPersonGuid(getRsString(rs, colPersonGuid));
                p.setSex((Person.Sex) ValueMap.SEX.getVal().get(getRsString(rs, colSex)));
                p.setBirthdate(rs.getDate(colBirthdate, cal));
                p.setDeathdate(rs.getDate(colDeathdate, cal));
                p.setFirstName(getRsString(rs, colFirstName));
                p.setMiddleName(getRsString(rs, colMiddleName));
                p.setLastName(getRsString(rs, colLastName));
                p.setOtherName(getRsString(rs, colOtherName));
                p.setClanName(getRsString(rs, colClanName));
                p.setMothersFirstName(getRsString(rs, colMothersFirstName));
                p.setMothersMiddleName(getRsString(rs, colMothersMiddleName));
                p.setMothersLastName(getRsString(rs, colMothersLastName));
                p.setFathersFirstName(getRsString(rs, colFathersFirstName));
                p.setFathersMiddleName(getRsString(rs, colFathersMiddleName));
                p.setFathersLastName(getRsString(rs, colFathersLastName));
                p.setCompoundHeadFirstName(getRsString(rs, colCompoundheadFirstName));
                p.setCompoundHeadMiddleName(getRsString(rs, colCompoundheadMiddleName));
                p.setCompoundHeadLastName(getRsString(rs, colCompoundheadLastName));
                p.setVillageName(getRsString(rs, colVillageName));
                p.setMaritalStatus((Person.MaritalStatus) ValueMap.MARITAL_STATUS.getVal().get(getRsString(rs, colMaritalStatusName)));
                p.setPersonIdentifierList(personIdentifierList.loadNext(dbPersonId));
                p.setFingerprintList(fingerprintList.loadNext(dbPersonId));
                p.setConsentSigned((Person.ConsentSigned) ValueMap.CONSENT_SIGNED.getVal().get(getRsString(rs, colConsentSigned)));
                p.setLastRegularVisit(Visit.getVisit(rs.getDate(colVisitRegDate), getRsString(rs, colVisitOneDate),getRsString(rs, colVisitRegFacility)));
                p.setLastOneOffVisit(Visit.getVisit(rs.getDate(colVisitRegAddress), getRsString(rs, colVisitOneAddress),getRsString(rs, colVisitRegFacility)));
                PersonMatch per = new PersonMatch(p);
                per.setDbPersonId(dbPersonId);
                personMatchList.add(per);
                if (++recordCount % 10000 == 0) {
                    double timeInterval = (System.currentTimeMillis() - startTime);
                    Mediator.getLogger(LoadPersonThread.class.getName()).log(Level.FINE,
                            "Thread {0} loaded {1} entries in {2} milliseconds.",
                            new Object[]{threadIndex, recordCount, timeInterval});
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(LoadPersonThread.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        Sql.close(rs);
        Sql.close(conn);
        fingerprintList.loadEnd();
        personIdentifierList.loadEnd();
                double timeInterval = (System.currentTimeMillis() - startTime);
                Mediator.getLogger(LoadPersonThread.class.getName()).log(Level.FINE,
                        "Thread {0} finished loading {1} entries in {2} milliseconds.",
                        new Object[]{threadIndex, personMatchList.size(), timeInterval});
    }

    /**
     * Does an efficient result set get string, assuming that the byte coding
     * in the database does not need translation into the character coding of
     * the string. This effectively does the same job as rs.getString(), but
     * without the overhead of doing a generalized character coding translation.
     * <p>
     * In CPU profiling, it was discovered that significant time was being
     * spent translating bytes into characters inside the rs.getString() method.
     * So this method was written to replace it for loading the MPI into memory.
     * 
     * @param rs ResultSet
     * @param columnIndex index of the column to get
     * @return string value of the column
     * @throws SQLException 
     */
    private String getRsString(ResultSet rs, int columnIndex) throws SQLException {
        String s = null;
        byte[] bytes = rs.getBytes(columnIndex);
        if (bytes != null) {
            char[] chars = new char[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                chars[i] = (char) bytes[i];
            }
            s = new String(chars);
        }
        return s;
    }
}
