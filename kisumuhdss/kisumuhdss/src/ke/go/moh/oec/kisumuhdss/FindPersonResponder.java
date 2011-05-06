/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
        PersonResponse pr = new PersonResponse();
        PersonRequest req = (PersonRequest) requestData;
        Person person = req.getPerson();
        List<PersonIdentifier> personIdentifierList = person.getPersonIdentifierList();
        for (PersonIdentifier pi : personIdentifierList) {
            if (pi.getIdentifierType() == PersonIdentifier.Type.kisumuHdssId) {

                String hdssID = pi.getIdentifier(); //get HDSS ID Of the Individual
                List<RelatedPerson> relatedPersonList = new ArrayList<RelatedPerson>();
                person.setHouseholdMembers(relatedPersonList);
                //Connect Database
                try {
                    try {
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    String connectionUrl = "jdbc:sqlserver://dss-kek3;database=DSSHRS;Integrated Security=SSPI;";
                    Connection conn = DriverManager.getConnection(connectionUrl, "Ewere", "12trojan24@@1");

                    PreparedStatement sql = null;
                    //Set Query To Run
                    sql = conn.prepareStatement("SELECT * FROM [DSSHRS].[dbo].[getMembersinIndividualHousehold] ('" + hdssID + "')");
                    ResultSet rs = null;
                    //Execute Query
                    rs = sql.executeQuery(); //excute query
                    while (rs.next()) {
                        //For Each HouseHold Member Add To The List
                        RelatedPerson rp = new RelatedPerson();
                        rp.setRelation(RelatedPerson.Relation.spouse);
                        Person p = new Person();
                        rp.setPerson(p);
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
                        
                        relatedPersonList.add(rp); //Add Person to Related List
                    }
                } catch (SQLException ex) {
                    //status = false;
                    Logger.getLogger(FindPersonResponder.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return pr;
    }
}
