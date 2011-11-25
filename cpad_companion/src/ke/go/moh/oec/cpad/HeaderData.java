package ke.go.moh.oec.cpad;

public class HeaderData {

    private String addr[] = new String[2];
    private String city;
    private String county;
    private String dob;
    private String facCode;
    private String facCounty;
    private String facName;
    private String facState;
    private String famName[] = new String[2];
    private String gender;
    private String givenName[] = new String[3];
    private String marStatus;
    private String phone[] = new String[3];
    private String pid;
    private String state;
    private String supAddr[] = new String[2];
    private String supCity;
    private String supCounty;
    private String supFamName[] = new String[2];
    private String supGivenName[] = new String[3];
    private String supPhone;
    private String supRelation;
    private String supState;

    public String getAddr(int i) {
        return addr[i];
    }

    public void setAddr(int i, String in) {
        this.addr[i] = in;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String in) {
        this.city = in;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String in) {
        this.county = in;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String in) {
        this.dob = in;
    }

    public String getFacCode() {
        return facCode;
    }

    public void setFacCode(String in) {
        this.facCode = in;
    }

    public String getFacCounty() {
        return facCounty;
    }

    public void setFacCounty(String in) {
        this.facCounty = in;
    }

    public String getFacName() {
        return facName;
    }

    public void setFacName(String in) {
        this.facName = in;
    }

    public String getFacState() {
        return facState;
    }

    public void setFacState(String in) {
        this.facState = in;
    }

    public String getFamName(int i) {
        return famName[i];
    }

    public void setFamName(int i, String in) {
        this.famName[i] = in;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String in) {
        this.gender = in;
    }

    public String getGivenName(int i) {
        return givenName[i];
    }

    public void setGivenName(int i, String in) {
        this.givenName[i] = in;
    }

    public String getMarStatus() {
        return marStatus;
    }

    public void setMarStatus(String in) {
        this.marStatus = in;
    }

    public String getPhone(int i) {
        return phone[i];
    }

    public void setPhone(int i, String in) {
        this.phone[i] = in;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String in) {
        this.pid = in;
    }

    public String getState() {
        return state;
    }

    public void setState(String in) {
        this.state = in;
    }

    public String getSupAddr(int i) {
        return supAddr[i];
    }

    public void setSupAddr(int i, String in) {
        this.supAddr[i] = in;
    }

    public String getSupCity() {
        return supCity;
    }

    public void setSupCity(String in) {
        this.supCity = in;
    }

    public String getSupCounty() {
        return supCounty;
    }

    public void setSupCounty(String in) {
        this.supCounty = in;
    }

    public String getSupFamName(int i) {
        return supFamName[i];
    }

    public void setSupFamName(int i, String in) {
        this.supFamName[i] = in;
    }

    public String getSupGivenName(int i) {
        return supGivenName[i];
    }

    public void setSupGivenName(int i, String in) {
        this.supGivenName[i] = in;
    }

    public String getSupPhone() {
        return supPhone;
    }

    public void setSupPhone(String in) {
        this.supPhone = in;
    }

    public String getSupRelation() {
        return supRelation;
    }

    public void setSupRelation(String in) {
        this.supRelation = in;
    }

    public String getSupState() {
        return supState;
    }

    public void setSupState(String in) {
        this.supState = in;
    }

    public void reset() {
        this.facName = null;
        this.facCode = null;
        this.facCounty = null;
        this.facState = null;
        this.pid = null;
        this.famName[0] = null;
        this.famName[1] = null;
        this.givenName[0] = null;
        this.givenName[1] = null;
        this.givenName[2] = null;
        this.dob = null;
        this.gender = null;
        this.addr[0] = null;
        this.addr[1] = null;
        this.city = null;
        this.county = null;
        this.state = null;
        this.phone[0] = null;
        this.phone[1] = null;
        this.phone[2] = null;
        this.marStatus = null;
        this.supFamName[0] = null;
        this.supFamName[1] = null;
        this.supGivenName[0] = null;
        this.supGivenName[1] = null;
        this.supGivenName[2] = null;
        this.supRelation = null;
        this.supAddr[0] = null;
        this.supAddr[1] = null;
        this.supCity = null;
        this.supCounty = null;
        this.supPhone = null;
        this.supState = null;
    }

    public String printHeaderDelim(String delim) {
        return((getFacName() == null ? "" : clean(getFacName())) + delim +
        	   (getFacCode() == null ? "" : clean(getFacCode())) + delim +
        	   (getFacCounty() == null ? "" : clean(getFacCounty())) + delim +
        	   (getFacState() == null ? "" : clean(getFacState())) + delim +
        	   (getPid() == null ? "" : clean(getPid())) + delim +
        	   (getFamName(0) == null ? "" : clean(getFamName(0))) + delim +
        	   (getFamName(1) == null ? "" : clean(getFamName(1))) + delim +
        	   (getGivenName(0) == null ? "" : clean(getGivenName(0))) + delim +
        	   (getGivenName(1) == null ? "" : clean(getGivenName(1))) + delim +
        	   (getGivenName(2) == null ? "" : clean(getGivenName(2))) + delim +
        	   (getDob() == null ? "" : clean(getDob())) + delim +
        	   (getGender() == null ? "" : clean(getGender())) + delim +
        	   (getAddr(0) == null ? "" : clean(getAddr(0))) + delim +
        	   (getAddr(1) == null ? "" : clean(getAddr(1))) + delim +
        	   (getCity() == null ? "" : clean(getCity())) + delim +
        	   (getCounty() == null ? "" : clean(getCounty())) + delim +
        	   (getState() == null ? "" : clean(getState())) + delim +
        	   delim + // Address landmark/directions, not used in the header
        	   (getPhone(0) == null ? "" : clean(getPhone(0))) + delim +
        	   (getPhone(1) == null ? "" : clean(getPhone(1))) + delim +
        	   (getPhone(2) == null ? "" : clean(getPhone(2))) + delim +
        	   (getMarStatus() == null ? "" : clean(getMarStatus())) + delim +
        	   delim + // Nearest health center, not used in the header
        	   (getSupFamName(0) == null ? "" : clean(getSupFamName(0))) + delim +
        	   (getSupFamName(1) == null ? "" : clean(getSupFamName(1))) + delim +
        	   (getSupGivenName(0) == null ? "" : clean(getSupGivenName(0))) + delim +
        	   (getSupGivenName(1) == null ? "" : clean(getSupGivenName(1))) + delim +
        	   (getSupGivenName(2) == null ? "" : clean(getSupGivenName(2))) + delim +
        	   (getSupRelation() == null ? "" : clean(getSupRelation())) + delim +
        	   (getSupAddr(0) == null ? "" : clean(getSupAddr(0))) + delim +
        	   (getSupAddr(1) == null ? "" : clean(getSupAddr(1))) + delim +
        	   (getSupCity() == null ? "" : clean(getSupCity())) + delim +
        	   (getSupCounty() == null ? "" : clean(getSupCounty())) + delim +
        	   (getSupState() == null ? "" : clean(getSupState())) + delim +
        	   (getSupPhone() == null ? "" : clean(getSupPhone())));
    }

	private String clean(String in) {
		return in.replaceAll("\n", " ").replaceAll("\t", " ");
	}
}