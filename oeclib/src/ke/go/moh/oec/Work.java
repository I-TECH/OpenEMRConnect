/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec;

/**
 * Contains pending work or alerts message to be processed when the reception program starts ,
 * after the  work or alerts are processed message and if the
 * work has to be Reassign to  Clinical Document Store.
 * getWork , getWorkDone, reassign
 *
 * @author pchemutai
 */
public class Work {

    /** Notification Id parameter to be passed to the getData */
    private String notificationId;
    /** Reassign  Id parameter to be passed when transferring data to Clinical Document Store **/
    private String reassignAddress;
    /** Address of the message source application */
    private String sourceAddress;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getReassignAddress() {
        return reassignAddress;
    }

    public void setReassignAddress(String reassignAddress) {
        this.reassignAddress = reassignAddress;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
}
