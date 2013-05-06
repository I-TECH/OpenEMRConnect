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
import ke.go.moh.oec.*;
import ke.go.moh.oec.lib.Mediator;

public class FindPersonResponder implements IService {

    String sourceDriver = Mediator.getProperty("Source.driver");
    String sourceUrl = Mediator.getProperty("Source.url");
    String sourceUsername = Mediator.getProperty("Source.username");
    String sourcePassword = Mediator.getProperty("Source.password");
    String hhMemberFunction = Mediator.getProperty("Source.individualHouseholdMembersFunction");

    public Object getData(int requestTypeId, Object requestData) {
        PersonResponse pr = new PersonResponse();
        if (requestData == null) {
            Mediator.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
                    "getData() called with null requestData");
            return null;
        }
        PersonRequest req = (PersonRequest) requestData;
        Person person = req.getPerson();
        if (person == null) {
            Mediator.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
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
            Connection conn = null;
            PreparedStatement sql = null;
            ResultSet rs = null;
            try {
                try {
                    Class.forName(sourceDriver);
                } catch (ClassNotFoundException ex) {
                    Mediator.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
                }
                String connectionUrl = sourceUrl;
                conn = DriverManager.getConnection(connectionUrl, sourceUsername, sourcePassword);
                //Set Query To Run
                sql = conn.prepareStatement("SELECT * FROM " + hhMemberFunction + " ('" + hdssID + "')");
                //Execute Query
                rs = sql.executeQuery(); //excute query
                while (rs.next()) {
                    //For Each HouseHold Member Add To The List
                    RelatedPerson rp = new RelatedPerson();
                    // rp.setRelation(RelatedPerson.Relation.spouse);
                    Person p = new Person();
                    p.setFirstName(rs.getString("fname")); //FirstName
                    p.setMiddleName(rs.getString("jname")); //Middle Name
                    p.setLastName(rs.getString("lname")); //LastName
                    p.setOtherName(rs.getString("akaname")); //OtherName
                    p.setClanName(rs.getString("famcla")); //Clan Name
                    p.setSex(Person.Sex.valueOf(rs.getString("gender"))); //Sex Or Gender
                    p.setBirthdate(rs.getDate("dob")); //Birth Date
                    p.setMothersFirstName(rs.getString("mfname")); //Mother FirstName
                    p.setMothersMiddleName(rs.getString("mjname")); //Mother MiddleName
                    p.setMothersLastName(rs.getString("mlname")); //Mothers Last Name
                    p.setFathersFirstName(rs.getString("ffname")); //Fathers First Name
                    p.setFathersMiddleName(rs.getString("fjname")); //Fathers MiddleName
                    p.setFathersLastName(rs.getString("flname")); //Fathers LastName
                    rp.setPerson(p);
                    relatedPersonList.add(rp);
                }
                person.setHouseholdMembers(relatedPersonList);
                List<Person> personList = new ArrayList<Person>();
                personList.add(person);
                pr.setPersonList(personList);
            } catch (SQLException ex) {
                //status = false;
                Mediator.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (sql != null) {
                        sql.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    Mediator.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            Mediator.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE,
                    "getData() called with Person without HDSS ID");
            return null;
        }
        return pr;
    }
}
