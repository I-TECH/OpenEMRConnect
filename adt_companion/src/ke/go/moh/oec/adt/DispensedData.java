/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.adt;

/**
 *
 * @author Administrator
 */
public class DispensedData {

    private String dateofVisit;
    private String drugname;
    private String duration;
    private String dose;

    public String getDateofVisit() {
        return dateofVisit;
    }

    public void setDateofVisit(String dateofVisit) {
        this.dateofVisit = dateofVisit;
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

    public void reset() {

        dateofVisit = null;
        drugname = null;
        duration = null;
        dose = null;

    }

    public String printHeaderDelim(String delim) {
        return ((getDrugname() == null ? "" : clean(getDrugname())) + delim
              + (getDateofVisit() == null ? "" : clean(getDateofVisit())) + delim
                + (getDuration() == null ? "" : clean(getDuration())) + delim
                + (getDose() == null ? "" : clean(getDose())));
    }

    private String clean(String in) {
        return (in == null ? "" : in.replaceAll("\n", " ").replaceAll("\t", " "));
    }
}
