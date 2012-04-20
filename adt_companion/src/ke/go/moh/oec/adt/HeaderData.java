/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.adt;

/**
 *
 * @author Administrator
 */
public class HeaderData {

    private String artID;
    private String firstname;
    private String surname;
    private int age;
    private String gender;
    private String dateofVisit;
    private String drugname;
    private String brandName;
    private String unit;
    private String arvQty;
    private String dose;
    private String duration;
    private String regimen;
    private String lastRegimen;
    private String comment;
    private String indication;
    private String weight;
    private String pillCount;
    private String adherence;
    private String daysLate;
    private String reasonsForChange;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAdherence() {
        return adherence;
    }

    public void setAdherence(String adherence) {
        this.adherence = adherence;
    }

    public String getArtID() {
        return artID;
    }

    public void setArtID(String artID) {
        this.artID = artID;
    }

    public String getArvQty() {
        return arvQty;
    }

    public void setArvQty(String arvQty) {
        this.arvQty = arvQty;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateofVisit() {
        return dateofVisit;
    }

    public void setDateofVisit(String dateofVisit) {
        this.dateofVisit = dateofVisit;
    }

    public String getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(String daysLate) {
        this.daysLate = daysLate;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getDrugname() {
        return drugname;
    }

    public void setDrugname(String drugname) {
        this.drugname = drugname;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getIndication() {
        return indication;
    }

    public void setIndication(String indication) {
        this.indication = indication;
    }

    public String getLastRegimen() {
        return lastRegimen;
    }

    public void setLastRegimen(String lastRegimen) {
        this.lastRegimen = lastRegimen;
    }

    public String getPillCount() {
        return pillCount;
    }

    public void setPillCount(String pillCount) {
        this.pillCount = pillCount;
    }

    public String getReasonsForChange() {
        return reasonsForChange;
    }

    public void setReasonsForChange(String reasonsForChange) {
        this.reasonsForChange = reasonsForChange;
    }

    public String getRegimen() {
        return regimen;
    }

    public void setRegimen(String regimen) {
        this.regimen = regimen;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void reset() {
        artID = null;
        firstname = null;
        surname = null;
        gender = null;
        age = 0;

    }

    public String printHeaderDelim(String delim) {

        String finalStr = "";
        finalStr += (getArtID() == null ? "" : clean(getArtID()) + delim
                + getFirstname() == null ? "" : clean(getFirstname()) + delim
                + (getSurname() == null ? "" : clean(getSurname()) + delim
                + (getGender() == null ? "" : clean(getGender()))));
        return (finalStr);

    }

    private String clean(String in) {
        return (in == null ? "" : in.replaceAll("\n", " ").replaceAll("\t", " "));
    }
    //System.out.println (" ");
}
