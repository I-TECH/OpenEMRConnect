/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.cds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.IService;
import ke.go.moh.oec.PersonRequest;
import ke.go.moh.oec.Person;
import ke.go.moh.oec.Visit;
import ke.go.moh.oec.RequestTypeId;
import ke.go.moh.oec.Work;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author Brian Wakhutu
 */
public class Cds implements IService {

    public Cds() {
        final String driverName = Mediator.getProperty("CDS.driver");
        try {
            Class.forName(driverName).newInstance();
        } catch (Exception ex) {
            Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Can''t load JDBC driver " + driverName, ex);
            System.exit(1);
        }
    }

    @Override
    public Object getData(int requestTypeId, Object requestData) {

        Mediator mediator = Main.getMediator();

        switch (requestTypeId) {

            case RequestTypeId.NOTIFY_PERSON_CHANGED:


                // Receive the Notify Message from the MPI and insert the values into the Cds database
                PersonRequest personRequest = (PersonRequest) requestData;
                Person person = personRequest.getPerson();
                Visit lastRegularVisit = person.getLastRegularVisit();
                String visitAddress = lastRegularVisit.getAddress();
                String xml = personRequest.getXml();

                Mediator.getLogger(Cds.class.getName()).log(Level.FINER, "Notify");

                Connection notifyConn = Sql.connect();

                String notifySql = "INSERT INTO cds_store(destination,message,voided,received_datetime) "
                        + " VALUES(" + Sql.quote(visitAddress) + "," + Sql.quote(xml) + ",0,NOW())";
                Sql.execute(notifyConn, notifySql);
                break;

            case RequestTypeId.GET_WORK:

                //Enable any Reception user to get any Message from the database given the reception Address
                //Where voided is false(voided means it has not been processed)

                Work getWork = (Work) requestData;
                String getWorkAddress = getWork.getSourceAddress();

                Mediator.getLogger(Cds.class.getName()).log(Level.FINER, "GetWork");
                Connection getWorkConn = Sql.connect();


                String getWorkSql = " SELECT message FROM cds_store WHERE destination = "
                        + " " + Sql.quote(getWorkAddress) + ""
                        + "AND voided = 0";
                ResultSet rs = Sql.query(getWorkConn, getWorkSql);

                try {
                    // retrieve the values for the current row
                    while (rs.next()) {
                        String message = rs.getString("message");//get message from cds to be send
                        String destinationAddress = rs.getString("destination");//get destination

                        /* Send notify to destination
                        instantiate person request so that you can set the new 
                        information held in the record set*/
                        PersonRequest prOut = new PersonRequest();
                        prOut.setDestinationAddress(destinationAddress);
                        prOut.setXml(message);

                        //Invoke the mediator to send out new person request changes (prout)
                        mediator.getData(RequestTypeId.NOTIFY_PERSON_CHANGED, prOut);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Cds.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;

            case RequestTypeId.REASSIGN_WORK:

                Work reassignWork = (Work) requestData;
                String reassignAddress = reassignWork.getSourceAddress();

                Mediator.getLogger(Cds.class.getName()).log(Level.FINER, "Reassign");
                //Update the destination of the Message to a different destination
                Connection reassignConn = Sql.connect();

                String reassignSql = "UPDATE cds_store SET destination = " + Sql.quote(reassignAddress) + ""
                        + " WHERE destination = " + Sql.quote(reassignAddress) + " AND voided = 0";

                Sql.execute(reassignConn, reassignSql);
                break;

            case RequestTypeId.WORK_DONE:
                Work workDone = (Work) requestData;
                String workDoneAdress = workDone.getSourceAddress();

                Mediator.getLogger(Cds.class.getName()).log(Level.FINER, "workdone");
                //Mark the message as voided
                Connection workDoneConn = Sql.connect();
                String workDoneSql = "UPDATE cds_store SET voided = 1 where "
                        + " destination ='" + Sql.quote(workDoneAdress) + "' ";

                Sql.execute(workDoneConn, workDoneSql);
                break;


            default:
                Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                        "getData() called with unepxected requestTypeId {0}", requestTypeId);
                break;
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }
}
