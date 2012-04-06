/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.pisinterfaces.beans;

/**
 * Session friendly class to house data returned from a CDA query
 */
public class CdaRecord {
    private String cdaID;  // Database Primary Key
    private String clinicId;
    private String hdssId;
    private String firstName;
    private String lastName;
    private String sourceSystem;
    private String cdaDOB;
    private String gender;
    private String dateGenerated;
    private String CDA;

    public String getCdaDOB() {
        return cdaDOB;
    }

    public void setCdaDOB(String cdaDOB) {
        this.cdaDOB = cdaDOB;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(String dateGenerated) {
        this.dateGenerated = dateGenerated;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHdssId() {
        return hdssId;
    }

    public void setHdssId(String hdssId) {
        this.hdssId = hdssId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCDA() {
        return CDA;
    }

    public void setCDA(String CDA) {
        this.CDA = CDA;
    }

    public String getCdaID() {
        return cdaID;
    }

    public void setCdaID(String CdaID) {
        this.cdaID = CdaID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String FirstName) {
        this.firstName = FirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String LastName) {
        this.lastName = LastName;
    }

}
