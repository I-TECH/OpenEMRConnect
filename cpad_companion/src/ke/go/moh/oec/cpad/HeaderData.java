package ke.go.moh.oec.cpad;


public class HeaderData {
	final static int MAX_SUPPORTERS = 3;
	final static int MAX_FAMILY_MEMBERS = 8;
	final static int FILLER_CNT = 45;
	private class SupportData {
	    private String supAddr[] = new String[2];
	    private String supCity;
	    private String supCounty;
	    private String supFamName[] = new String[2];
	    private String supGivenName[] = new String[3];
	    private String supPhone;
	    private String supRelation;
	    private String supState;
	}
	private class FamMemberData {
	    private String relation;
	    private String age;
	    private String hivStatus;
	    private String inCare;
	    private String pid;		
	}
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
    private String sourceSystem;
	private SupportData support[] = new SupportData[MAX_SUPPORTERS];
	private FamMemberData famMembers[] = new FamMemberData[MAX_FAMILY_MEMBERS];

    public String getAddr(int i) {
        return addr[i];
    }

    public void setAddr(int i, String in) {
        addr[i] = in;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String in) {
        city = in;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String in) {
        county = in;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String in) {
        dob = in;
    }

    public String getFacCode() {
        return facCode;
    }

    public void setFacCode(String in) {
        facCode = in;
    }

    public String getFacCounty() {
        return facCounty;
    }

    public void setFacCounty(String in) {
        facCounty = in;
    }

    public String getFacName() {
        return facName;
    }

    public void setFacName(String in) {
        facName = in;
    }

    public String getFacState() {
        return facState;
    }

    public void setFacState(String in) {
        facState = in;
    }

    public String getFamName(int i) {
        return famName[i];
    }

    public void setFamName(int i, String in) {
        famName[i] = in;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String in) {
        gender = in;
    }

    public String getGivenName(int i) {
        return givenName[i];
    }

    public void setGivenName(int i, String in) {
        givenName[i] = in;
    }

    public String getMarStatus() {
        return marStatus;
    }

    public void setMarStatus(String in) {
        marStatus = in;
    }

    public String getPhone(int i) {
        return phone[i];
    }

    public void setPhone(int i, String in) {
        phone[i] = in;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String in) {
        pid = in;
    }

    public String getState() {
        return state;
    }

    public void setState(String in) {
        state = in;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String in) {
    	sourceSystem = in;
    }

    public String getSupAddr(int i, int j) {
        return support[i].supAddr[j];
    }

    public void setSupAddr(int i, int j, String in) {
        support[i].supAddr[j] = in;
    }

    public String getSupCity(int i) {
        return support[i].supCity;
    }

    public void setSupCity(int i, String in) {
        support[i].supCity = in;
    }

    public String getSupCounty(int i) {
        return support[i].supCounty;
    }

    public void setSupCounty(int i, String in) {
        support[i].supCounty = in;
    }

    public String getSupFamName(int i, int j) {
        return support[i].supFamName[j];
    }

    public void setSupFamName(int i, int j, String in) {
        support[i].supFamName[j] = in;
    }

    public String getSupGivenName(int i, int j) {
        return support[i].supGivenName[j];
    }

    public void setSupGivenName(int i, int j, String in) {
        support[i].supGivenName[j] = in;
    }

    public String getSupPhone(int i) {
        return support[i].supPhone;
    }

    public void setSupPhone(int i, String in) {
        support[i].supPhone = in;
    }

    public String getSupRelation(int i) {
        return support[i].supRelation;
    }

    public void setSupRelation(int i, String in) {
        support[i].supRelation = in;
    }

    public String getSupState(int i) {
        return support[i].supState;
    }

    public void setSupState(int i, String in) {
        support[i].supState = in;
    }

    public String getFamMemberRelation(int i) {
        return famMembers[i].relation;
    }

    public void setFamMemberRelation(int i, String in) {
        famMembers[i].relation = in;
    }

    public String getFamMemberAge(int i) {
        return famMembers[i].age;
    }

    public void setFamMemberAge(int i, String in) {
        famMembers[i].age = in;
    }

    public String getFamMemberHivStatus(int i) {
        return famMembers[i].hivStatus;
    }

    public void setFamMemberHivStatus(int i, String in) {
        famMembers[i].hivStatus = in;
    }

    public String getFamMemberInCare(int i) {
        return famMembers[i].inCare;
    }

    public void setFamMemberInCare(int i, String in) {
        famMembers[i].inCare = in;
    }

    public String getFamMemberPid(int i) {
        return famMembers[i].pid;
    }

    public void setFamMemberPid(int i, String in) {
        famMembers[i].pid = in;
    }

    public void reset() {
        facName = null;
        facCode = null;
        facCounty = null;
        facState = null;
        pid = null;
        famName[0] = null;
        famName[1] = null;
        givenName[0] = null;
        givenName[1] = null;
        givenName[2] = null;
        dob = null;
        gender = null;
        addr[0] = null;
        addr[1] = null;
        city = null;
        county = null;
        state = null;
        phone[0] = null;
        phone[1] = null;
        phone[2] = null;
        marStatus = null;
        sourceSystem = null;
        for (int i = 0; i < MAX_SUPPORTERS; i++) { 
        	support[i] = new SupportData();
        	support[i].supFamName[0] = null;
        	support[i].supFamName[1] = null;
        	support[i].supGivenName[0] = null;
        	support[i].supGivenName[1] = null;
        	support[i].supGivenName[2] = null;
        	support[i].supRelation = null;
        	support[i].supAddr[0] = null;
        	support[i].supAddr[1] = null;
        	support[i].supCity = null;
        	support[i].supCounty = null;
        	support[i].supPhone = null;
        	support[i].supState = null;
        }
        for (int i = 0; i < MAX_FAMILY_MEMBERS; i++) { 
        	famMembers[i] = new FamMemberData();
        	famMembers[i].relation = null;
        	famMembers[i].age = null;
        	famMembers[i].hivStatus = null;
        	famMembers[i].inCare = null;
        	famMembers[i].pid = null;
        }
    }

    public String printHeaderDelim(String delim) {
    	String finalStr = "";
        finalStr += (getFacName() == null ? "" : clean(getFacName())) + delim +
        			(getFacCode() == null ? "" : clean(getFacCode())) + delim +
        			(getFacCounty() == null ? "" : clean(getFacCounty())) + delim +
        			(getFacState() == null ? "" : clean(getFacState())) + delim +
        			(getSourceSystem() == null ? "" : clean(getSourceSystem())) + delim +
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
        			delim; // Nearest health center, not used in the header
        
        for (int i = 0; i < MAX_SUPPORTERS; i++) {
        	finalStr += (getSupFamName(i, 0) == null ? "" : clean(getSupFamName(i, 0))) + delim +
        				(getSupFamName(i, 1) == null ? "" : clean(getSupFamName(i, 1))) + delim +
        				(getSupGivenName(i, 0) == null ? "" : clean(getSupGivenName(i, 0))) + delim +
        				(getSupGivenName(i, 1) == null ? "" : clean(getSupGivenName(i, 1))) + delim +
        				(getSupGivenName(i, 2) == null ? "" : clean(getSupGivenName(i, 2))) + delim +
        				(getSupRelation(i) == null ? "" : clean(getSupRelation(i))) + delim +
        				(getSupAddr(i, 0) == null ? "" : clean(getSupAddr(i, 0))) + delim +
        				(getSupAddr(i, 1) == null ? "" : clean(getSupAddr(i, 1))) + delim +
        				(getSupCity(i) == null ? "" : clean(getSupCity(i))) + delim +
        				(getSupCounty(i) == null ? "" : clean(getSupCounty(i))) + delim +
        				(getSupState(i) == null ? "" : clean(getSupState(i))) + delim +
        				(getSupPhone(i) == null ? "" : clean(getSupPhone(i)));
        	if (i < MAX_SUPPORTERS - 1) finalStr += delim;
        }

        for (int i = 0; i < FILLER_CNT; i++) {
        	finalStr += delim;
        }

        for (int i = 0; i < MAX_FAMILY_MEMBERS; i++) {
        	finalStr += delim + delim + delim + delim + delim +
        			    (getFamMemberRelation(i) == null ? "" : clean(getFamMemberRelation(i))) + delim +
        				(getFamMemberAge(i) == null ? "" : clean(getFamMemberAge(i))) + delim +
        				(getFamMemberHivStatus(i) == null ? "" : clean(getFamMemberHivStatus(i))) + delim +
        				(getFamMemberInCare(i) == null ? "" : clean(getFamMemberInCare(i))) + delim +
        				(getFamMemberPid(i) == null ? "" : clean(getFamMemberPid(i)));
        	if (i < MAX_FAMILY_MEMBERS - 1) finalStr += delim;
        }

        return (finalStr);
    }

	private String clean(String in) {
		return (in == null ? "" : in.replaceAll("\n", " ").replaceAll("\t", " "));
	}
}