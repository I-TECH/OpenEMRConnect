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
package ke.go.moh.oec.kisumuhdss;

/**
 *
 * @author EWere
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.*;

public class FindPersonResponder implements IService {

    public Object getData(int requestTypeId, Object requestData) {
//        if (requestTypeId == RequestTypeId.FIND_PERSON_HDSS) {
            PersonResponse pr = new PersonResponse();
            if (requestData == null) {
                Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
                        "getData() called with null requestData");
                return null;
            }
            PersonRequest req = (PersonRequest) requestData;
            Person person = req.getPerson();
            if (person == null) {
                Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
                        "getData() called with null Person");
                return null;
            }
            PersonIdentifier hdssPersonIdentifier = null;
            List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
            if (personIdentifierList != null) {
                for (PersonIdentifier personIdentifier : personIdentifierList) {
                    if (personIdentifier.getIdentifierType() == PersonIdentifier.Type.kisumuHdssId) {
                        hdssPersonIdentifier = personIdentifier;
                        break;
                    }
                }
            }
            if (hdssPersonIdentifier != null) {
                String hdssID = hdssPersonIdentifier.getIdentifier(); //get HDSS ID Of the Individual
                List<RelatedPerson> relatedPersonList = new ArrayList<RelatedPerson>();
                //Connect Database
                try {
                    try {
                        Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String connectionUrl = "jdbc:jtds:sqlserver://localhost:57460/hdss;";
                    Connection conn = DriverManager.getConnection(connectionUrl, "sa", "2806");
                    PreparedStatement sql = null;
                    //Set Query To Run
                    sql = conn.prepareStatement("SELECT * FROM [dbo].[household_members] ('" + hdssID + "')");
                    ResultSet rs = null;
                    //Execute Query
                    rs = sql.executeQuery(); //excute query
                    while (rs.next()) {
                        //For Each HouseHold Member Add To The List
                        RelatedPerson rp = new RelatedPerson();
                        // rp.setRelation(RelatedPerson.Relation.spouse);
                        Person p = new Person();
                        rp.setPerson(p);
                        p.setFirstName(rs.getString("first_name")); //FirstName
                        p.setMiddleName(rs.getString("middle_name")); //Middle Name
                        p.setLastName(rs.getString("last_name")); //LastName
                        p.setOtherName(rs.getString("other_name")); //OtherName
                        p.setSex(Person.Sex.valueOf(rs.getString("sex").trim())); //Sex Or Gender
                        p.setBirthdate(rs.getDate("birthdate")); //Birth Date
                        relatedPersonList.add(rp); //Add Person to Related List
                    }
                    person.setHouseholdMembers(relatedPersonList);
                    List<Person> personList = new ArrayList<Person>();
                    personList.add(person);
                    pr.setPersonList(personList);
                } catch (SQLException ex) {
                    //status = false;
                    Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
                        "getData() called with Person without HDSS ID");
                return null;
            }
            return pr;
//        } else {
//            Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
//                    "getData() called with unepxected requestTypeId {0}", requestTypeId);
//            return null;
//        }
    }
}
