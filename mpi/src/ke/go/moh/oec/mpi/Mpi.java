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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.Fingerprint;
import ke.go.moh.oec.IService;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.PersonIdentifier;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.RequestTypeId;

/**
 * Provides Master Patient Index (or Local Patient Index) services.
 * 
 * @author Jim Grace
 */
public class Mpi implements IService {

    private static int testQueryLimit = 10;
    private static String testOrderBy = null;
    private PersonList personList = new PersonList();
    private static Level loggerLevel = Level.INFO;
    private static final Logger logger = Logger.getLogger(Mpi.class.getName());

    /**
     * Start up MPI processing. Load the database into memory so it can be
     * searched more quickly. Approximate string matching and fingerprint
     * matching require that search terms be matched against every database
     * entry.
     */
    public Mpi() {
        logger.setLevel(loggerLevel);
        final String driverName = "com.mysql.jdbc.Driver";
        try {
            Class.forName(driverName).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Can''t load JDBC driver " + driverName, ex);
            System.exit(1);
        }
        loadPersonMatchList();
    }

    /**
     * Gets the Mpi logger.
     *
     * @return logger used for logging MPI messages.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Sets the level that will be used for the MPI logger. The level
     * can be set for testing or debugging before creating the MPI object.
     *
     * @param loggerLevel Level to set for the MPI logger.
     */
    public static void setLoggerLevel(Level loggerLevel) {
        Mpi.loggerLevel = loggerLevel;
        Mpi.logger.setLevel(loggerLevel);
    }

    /**
     * Sets a column (or columns) to order by for database loading.
     * A column name of null means no ordering will be done on database loading
     * of persons (this is the default for production use.)
     * This column may be set for testing in conjunction with queryTestLimit,
     * so that the persons loaded will be from a predictable set.
     *
     * @param testOrderBy The SQL table column(s) to ORDER BY.
     */
    public static void setTestOrderBy(String testOrderBy) {
        Mpi.testOrderBy = testOrderBy;
    }

    /**
     * Sets a limit on how many persons from the database will be loaded.
     * A limit of zero means all persons will be loaded.
     * This limit may be set for testing, so that tests will complete in a
     * shorter time period than would be required for the complete database.
     *
     * @param testQueryLimit
     */
    public static void setTestQueryLimit(int testQueryLimit) {
        Mpi.testQueryLimit = testQueryLimit;
    }

    /**
     * Creates a connection to the MPI(/LPI) database. For a query that is run while the results
     * are being fetched from another query, a separate connection is needed.
     *
     * @return The connection.
     */
    public static Connection dbConnect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/mpi", "root", "root");
        } catch (Exception ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);
            System.exit(1);
        }
        return conn;
    }

    /**
     * Executes a SQL query on a database connection.
     *
     * @param conn The Connection to use.
     * @param sql The SQL query.
     * @return the results of the query, in ResultSet form.
     */
    public static ResultSet query(Connection conn, String sql) {
        Mpi.getLogger().log(Level.FINE, "Mpi.query({0})", sql);
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;
    }

    /**
     * Executes any SQL statement on a database connection.
     *
     * @param conn The Connection to use.
     * @param sql The SQL statement.
     * @return true if first result is a ResultSet.
     */
    public static boolean execute(Connection conn, String sql) {
        Mpi.getLogger().log(Level.FINE, "Mpi.query({0})", sql);
        boolean returnValue = false;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }

    public static String quote(String s) {
        if (s == null) {
            s = "null";
        } else {
            s = s.replace("'", "''");
            s = s.replace("\\", "\\\\");
        }
        return s;
    }

    /**
     * Loads into memory the entire person table from the MPI, along with
     * any joining person relation data.
     */
    private void loadPersonMatchList() {
        /*
         * Create two connections to the database. One will be used for
         * the main query to the person table. The other will be used for
         * the queries to load a list of identifiers or fingerprints for
         * a single person.
         */
        Connection personConn = dbConnect();
        Connection listConn = dbConnect();
        /*
         * For quick debugging, set queryLimit to a limite number of rows,
         * to avoide loading the entire database. For production uses, set
         * queryLimit to zero.
         */
        Calendar cal = Calendar.getInstance(); // Default Calendar, used for getting database date fields.
        String sql = "SELECT p.person_id, p.person_guid, p.sex, p.birthdate, p.deathdate,"
                + " p.first_name, p.middle_name, p.last_name, p.other_name, p.clan_name,"
                + " p.mothers_first_name, p.mothers_middle_name, p.mothers_last_name,"
                + " p.fathers_first_name, p.fathers_middle_name, p.fathers_last_name,"
                + " p.compoundhead_first_name, p.compoundhead_middle_name, p.compoundhead_last_name,"
                + " v.village_name, m.marital_status_name, p.consent_signed"
                + " FROM person p"
                + " JOIN village v on v.village_id = p.village_id"
                + " JOIN marital_status_type m on m.marital_status_type_id = p.marital_status";
        if (testOrderBy != null) {
            sql = sql + " ORDER BY " + testOrderBy;
        }
        if (testQueryLimit != 0) {
            sql = sql + " LIMIT " + testQueryLimit;
        }
        ResultSet rs = query(personConn, sql);
        try {
            while (rs.next()) {
                Person p = new Person();
                int dbPersonId = rs.getInt("person_id");
                p.setPersonGuid(rs.getString("person_guid"));
                p.setSex((Person.Sex) getEnum(Person.Sex.values(), rs.getString("sex")));
                p.setBirthdate(rs.getDate("birthdate", cal));
                p.setDeathdate(rs.getDate("deathdate", cal));
                p.setFirstName(rs.getString("first_name"));
                p.setMiddleName(rs.getString("middle_name"));
                p.setLastName(rs.getString("last_name"));
                p.setOtherName(rs.getString("other_name"));
                p.setClanName(rs.getString("clan_name"));
                p.setMothersFirstName(rs.getString("mothers_first_name"));
                p.setMothersMiddleName(rs.getString("mothers_middle_name"));
                p.setMothersLastName(rs.getString("mothers_last_name"));
                p.setFathersFirstName(rs.getString("fathers_first_name"));
                p.setFathersMiddleName(rs.getString("fathers_middle_name"));
                p.setFathersLastName(rs.getString("fathers_last_name"));
                p.setCompoundHeadFirstName(rs.getString("compoundhead_first_name"));
                p.setCompoundHeadMiddleName(rs.getString("compoundhead_middle_name"));
                p.setCompoundHeadLastName(rs.getString("compoundhead_last_name"));
                p.setVillageName(rs.getString("village_name"));
                p.setMaritalStatus((Person.MaritalStatus) getEnum(Person.MaritalStatus.values(), rs.getString("marital_status_name")));
                p.setPersonIdentifierList(loadPersonIdentifierList(listConn, dbPersonId));
                p.setFingerprintList(loadFingerprintList(listConn, dbPersonId));
                PersonMatch per = new PersonMatch(p);
                per.setDbPersonId(dbPersonId);
                personList.add(per);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

    }

    /**
     * Loads into memory the person identifiers for a given person.
     *
     * @param conn Connection on which to do the query.
     * @param dbPersonId Internal database ID for the person associated with these identifiers.
     * @return The list of person identifiers.
     */
    private List<PersonIdentifier> loadPersonIdentifierList(Connection conn, int dbPersonId) {
        List<PersonIdentifier> personIdentifierList = null;
        String sql = "SELECT pi.identifier, it.identifier_type_name"
                + " FROM person_identifier pi"
                + " JOIN identifier_type it on it.identifier_type_id = pi.identifier_type_id"
                + " WHERE pi.person_id = " + dbPersonId;
        ResultSet rs = query(conn, sql);
        try {
            while (rs.next()) {
                /*
                 * Programming note: If there are no person identifiers, we will return a
                 * null person identifier list. In theory, we could have known this
                 * outside the rs.next() loop by tsting rs.isLast() to see if the
                 * record set is empty. In practice, however, we do this inside the loop.
                 * Why? Because database drivers sometimes have unexpected bugs. This
                 * way we are relying only on one function, rs.next() to tell us
                 * if we are at the end of the result set.
                 */
                if (personIdentifierList == null) {
                    personIdentifierList = new ArrayList<PersonIdentifier>();
                }
                String idType = rs.getString("identifier_type_name");
                PersonIdentifier.Type pit = null;
                if (idType.equals("KEMRI/KISUMU HDSS AREA")) {
                    pit = PersonIdentifier.Type.kisumuHdssId;
                } else if (idType.equals("CCC Unique Patient Number")) {
                    pit = PersonIdentifier.Type.kisumuHdssId;
                } else if (idType.equals("MPI identifier")) {
                    pit = PersonIdentifier.Type.masterPatientRegistryId;
                }
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

    /**
     * Loads into memory the fingerprints for a given person.
     *
     * @param conn Connection on which to do the query.
     * @param dbPersonId Internal database ID for the person associated with these fingerprints.
     * @return The list of fingerprints.
     */
    private List<Fingerprint> loadFingerprintList(Connection conn, int dbPersonId) {
        List<Fingerprint> fingerprintList = null;
        String sql = "SELECT f.fingerprint_template, t.hand_name, t.finger_name, tech.sdk_type"
                + " FROM fingerprint f"
                + " JOIN fingerprint_type t on t.fingerprint_type_id = f.fingerprint_technology_type_id"
                + " JOIN fingerprint_technology_type tech on tech.fingerprint_technology_type_id = f.fingerprint_technology_type_id"
                + " WHERE f.person_id = " + dbPersonId;
        ResultSet rs = query(conn, sql);
        try {
            while (rs.next()) {
                // (See programming note in loadPersonIdentifierList().
                if (fingerprintList == null) {
                    fingerprintList = new ArrayList<Fingerprint>();
                }
                String finger = rs.getString("hand_name") + rs.getString("finger_name");
                Fingerprint.Type fingerprintType = null;
                if (finger.equals("RightIndex")) {
                    fingerprintType = Fingerprint.Type.rightIndexFinger;
                } else if (finger.equals("RightMiddle")) {
                    fingerprintType = Fingerprint.Type.rightMiddleFinger;
                } else if (finger.equals("RightRing")) {
                    fingerprintType = Fingerprint.Type.rightRingFinger;
                } else if (finger.equals("LeftIndex")) {
                    fingerprintType = Fingerprint.Type.leftIndexFinger;
                } else if (finger.equals("LeftMiddle")) {
                    fingerprintType = Fingerprint.Type.leftMiddleFinger;
                } else if (finger.equals("LeftRing")) {
                    fingerprintType = Fingerprint.Type.leftRingFinger;
                }
                String technology = rs.getString("sdk_type");
                Fingerprint.TechnologyType technologyType = null;
                if (technology.equals("Griaule Biometric Fingerprint SDK")) {
                    technologyType = Fingerprint.TechnologyType.griauleTemplate;
                }
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

    /**
     * Given a string and a set of enumerated values, find which enumerated
     * member has a name matching the string.
     *
     * @param values the set of enumerated values in which to search
     * @param text text to match against the value names
     * @return the enumerated value if there was a match, otherwise null
     */
    private Enum getEnum(Enum[] values, String text) {
        for (Enum e : values) {
            if (e.name().equalsIgnoreCase(text)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Process a MPI/LPI request from another application.
     * @param requestTypeId Type of request
     * @param requestData Data for the request
     * @return Return data.
     */
    public synchronized Object getData(int requestTypeId, Object requestData) {
        Object returnData = null;
        switch (requestTypeId) {
            case RequestTypeId.FIND_PERSON_MPI:
            case RequestTypeId.FIND_PERSON_LPI:
                FindPerson findPerson = new FindPerson();
                returnData = findPerson.find(personList, (PersonRequest) requestData);
                break;

            case RequestTypeId.CREATE_PERSON_MPI:
            case RequestTypeId.CREATE_PERSON_LPI:
                //TO DO: createPerson((PersonRequest) requestData);
                break;

            case RequestTypeId.MODIFY_PERSON_MPI:
            case RequestTypeId.MODIFY_PERSON_LPI:
                //TO DO: modifyPerson((PersonRequest) requestData);
                break;

            default:
                Logger.getLogger(Mpi.class.getName()).log(Level.SEVERE,
                        "getData() called with unepxected requestTypeId {0}", requestTypeId);
                break;
        }
        return returnData;
    }
}
