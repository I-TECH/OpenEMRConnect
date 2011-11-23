package ke.go.moh.oec.cpad;

public class VisitData {

	private String arvAdh;
	private String arvDosage;
	private String arvName;
	private String arvNoDays;
	private String arvPoorAdh[] = new String[5];
	private String atRiskPop;
	private String bmi;
	private String bp;
	private String cd4Count;
	private String cd4Perc;
	private String clinicianInit;
	private String condomsDisp;
	private String ctxAdh;
	private String ctxDisp;
	private String ctxPoorAdh[] = new String[5];
	private String disclosure;
	private String edd;
	private String famPlanMethod[] = new String[5];
	private String famPlanStat;
	private String hgb;
	private String ht;
	private String inhDisp;
	private String mosOnArt;
	private String mosOnRegimen;
	private String nextAppt;
	private String oiProblem[] = new String[5];
	private String otherLabName[] = new String[5];
	private String otherLabResult[] = new String[5];
	private String otherMedDosage[] = new String[8];
	private String otherMedFreq[] = new String[8];
	private String otherMedName[] = new String[8];
	private String partnerTested;
	private String preg;
	private String referral;
	private String rpr;
	private String sideEffect[] = new String[5];
	private String sputum;
	private String stiScreen;
	private String tbStartMo;
	private String tbStartYr;
	private String tbStat;
	private String tbTreatNo;
	private String visDate;
	private String visId;
	private String visType;
	private String whoStage;
	private String wt;

    public String getArvAdh() {
        return arvAdh;
    }

    public void setArvAdh(String in) {
        this.arvAdh = in;
    }

    public String getArvDosage() {
        return arvDosage;
    }

    public void setArvDosage(String in) {
        this.arvDosage = in;
    }

    public String getArvName() {
        return arvName;
    }

    public void setArvName(String in) {
        this.arvName = in;
    }

    public String getArvNoDays() {
        return arvNoDays;
    }

    public void setArvNoDays(String in) {
        this.arvNoDays = in;
    }

    public String getArvPoorAdh(int i) {
        return arvPoorAdh[i];
    }

    public void setArvPoorAdh(int i, String in) {
        this.arvPoorAdh[i] = in;
    }

    public String getAtRiskPop() {
        return atRiskPop;
    }

    public void setAtRiskPop(String in) {
        this.atRiskPop = in;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String in) {
        this.bmi = in;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String in) {
        this.bp = in;
    }

    public String getCd4Count() {
        return cd4Count;
    }

    public void setCd4Count(String in) {
        this.cd4Count = in;
    }

    public String getCd4Perc() {
        return cd4Perc;
    }

    public void setCd4Perc(String in) {
        this.cd4Perc = in;
    }

    public String getClinicianInit() {
        return clinicianInit;
    }

    public void setClinicianInit(String in) {
        this.clinicianInit = in;
    }

    public String getCondomsDisp() {
        return condomsDisp;
    }

    public void setCondomsDisp(String in) {
        this.condomsDisp = in;
    }

    public String getCtxAdh() {
        return ctxAdh;
    }

    public void setCtxAdh(String in) {
        this.ctxAdh = in;
    }

    public String getCtxDisp() {
        return ctxDisp;
    }

    public void setCtxDisp(String in) {
        this.ctxDisp = in;
    }

    public String getCtxPoorAdh(int i) {
        return ctxPoorAdh[i];
    }

    public void setCtxPoorAdh(int i, String in) {
        this.ctxPoorAdh[i] = in;
    }

    public String getDisclosure() {
        return disclosure;
    }

    public void setDisclosure(String in) {
        this.disclosure = in;
    }

    public String getEdd() {
        return edd;
    }

    public void setEdd(String in) {
        this.edd = in;
    }

    public String getFamPlanMethod(int i) {
        return famPlanMethod[i];
    }

    public void setFamPlanMethod(int i, String in) {
        this.famPlanMethod[i] = in;
    }

    public String getFamPlanStat() {
        return famPlanStat;
    }

    public void setFamPlanStat(String in) {
        this.famPlanStat = in;
    }

    public String getHgb() {
        return hgb;
    }

    public void setHgb(String in) {
        this.hgb = in;
    }

    public String getHt() {
        return ht;
    }

    public void setHt(String in) {
        this.ht = in;
    }

    public String getInhDisp() {
        return inhDisp;
    }

    public void setInhDisp(String in) {
        this.inhDisp = in;
    }

    public String getMosOnArt() {
        return mosOnArt;
    }

    public void setMosOnArt(String in) {
        this.mosOnArt = in;
    }

    public String getMosOnRegimen() {
        return mosOnRegimen;
    }

    public void setMosOnRegimen(String in) {
        this.mosOnRegimen = in;
    }

    public String getNextAppt() {
        return nextAppt;
    }

    public void setNextAppt(String in) {
        this.nextAppt = in;
    }

    public String getOiProblem(int i) {
        return oiProblem[i];
    }

    public void setOiProblem(int i, String in) {
        this.oiProblem[i] = in;
    }

    public String getOtherLabName(int i) {
        return otherLabName[i];
    }

    public void setOtherLabName(int i, String in) {
        this.otherLabName[i] = in;
    }

    public String getOtherLabResult(int i) {
        return otherLabResult[i];
    }

    public void setOtherLabResult(int i, String in) {
        this.otherLabResult[i] = in;
    }

    public String getOtherMedDosage(int i) {
        return otherMedDosage[i];
    }

    public void setOtherMedDosage(int i, String in) {
        this.otherMedDosage[i] = in;
    }

    public String getOtherMedFreq(int i) {
        return otherMedFreq[i];
    }

    public void setOtherMedFreq(int i, String in) {
        this.otherMedFreq[i] = in;
    }

    public String getOtherMedName(int i) {
        return otherMedName[i];
    }

    public void setOtherMedName(int i, String in) {
        this.otherMedName[i] = in;
    }

    public String getPartnerTested() {
        return partnerTested;
    }

    public void setPartnerTested(String in) {
        this.partnerTested = in;
    }

    public String getPreg() {
        return preg;
    }

    public void setPreg(String in) {
        this.preg = in;
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String in) {
        this.referral = in;
    }

    public String getRpr() {
        return rpr;
    }

    public void setRpr(String in) {
        this.rpr = in;
    }

    public String getSideEffect(int i) {
        return sideEffect[i];
    }

    public void setSideEffect(int i, String in) {
        this.sideEffect[i] = in;
    }

    public String getSputum() {
        return sputum;
    }

    public void setSputum(String in) {
        this.sputum = in;
    }

    public String getStiScreen() {
        return stiScreen;
    }

    public void setStiScreen(String in) {
        this.stiScreen = in;
    }

    public String getTbStartMo() {
        return tbStartMo;
    }

    public void setTbStartMo(String in) {
        this.tbStartMo = in;
    }

    public String getTbStartYr() {
        return tbStartYr;
    }

    public void setTbStartYr(String in) {
        this.tbStartYr = in;
    }

    public String getTbStat() {
        return tbStat;
    }

    public void setTbStat(String in) {
        this.tbStat = in;
    }

    public String getTbTreatNo() {
        return tbTreatNo;
    }

    public void setTbTreatNo(String in) {
        this.tbTreatNo = in;
    }

    public String getVisDate() {
        return visDate;
    }

    public void setVisDate(String in) {
        this.visDate = in;
    }

    public String getVisId() {
        return visId;
    }

    public void setVisId(String in) {
        this.visId = in;
    }

    public String getVisType() {
        return visType;
    }

    public void setVisType(String in) {
        this.visType = in;
    }

    public String getWhoStage() {
        return whoStage;
    }

    public void setWhoStage(String in) {
        this.whoStage = in;
    }

    public String getWt() {
        return wt;
    }

    public void setWt(String in) {
        this.wt = in;
    }

    public void reset() {
    	this.visId = null;
    	this.visDate = null;
    	this.visType = null;
    	this.mosOnArt = null;
    	this.mosOnRegimen = null;
    	this.wt = null;
    	this.bp = null;
    	this.ht = null;
    	this.bmi = null;
    	this.preg = null;
    	this.edd = null;
    	this.famPlanStat = null;
    	this.famPlanMethod[0] = null;
    	this.famPlanMethod[1] = null;
    	this.famPlanMethod[2] = null;
    	this.famPlanMethod[3] = null;
    	this.famPlanMethod[4] = null;
    	this.tbStat = null;
    	this.tbStartMo = null;
    	this.tbStartYr = null;
    	this.tbTreatNo = null;
    	this.sideEffect[0] = null;
    	this.sideEffect[1] = null;
    	this.sideEffect[2] = null;
    	this.sideEffect[3] = null;
    	this.sideEffect[4] = null;
    	this.oiProblem[0] = null;
    	this.oiProblem[1] = null;
    	this.oiProblem[2] = null;
    	this.oiProblem[3] = null;
    	this.oiProblem[4] = null;
    	this.whoStage = null;
    	this.ctxDisp = null;
    	this.ctxAdh = null;
    	this.ctxPoorAdh[0] = null;
    	this.ctxPoorAdh[1] = null;
    	this.ctxPoorAdh[2] = null;
    	this.ctxPoorAdh[3] = null;
    	this.ctxPoorAdh[4] = null;
    	this.inhDisp = null;
    	this.otherMedName[0] = null;
    	this.otherMedDosage[0] = null;
    	this.otherMedFreq[0] = null;
    	this.otherMedName[1] = null;
    	this.otherMedDosage[1] = null;
    	this.otherMedFreq[1] = null;
    	this.otherMedName[2] = null;
    	this.otherMedDosage[2] = null;
    	this.otherMedFreq[2] = null;
    	this.otherMedName[3] = null;
    	this.otherMedDosage[3] = null;
    	this.otherMedFreq[3] = null;
    	this.otherMedName[4] = null;
    	this.otherMedDosage[4] = null;
    	this.otherMedFreq[4] = null;
    	this.otherMedName[5] = null;
    	this.otherMedDosage[5] = null;
    	this.otherMedFreq[5] = null;
    	this.otherMedName[6] = null;
    	this.otherMedDosage[6] = null;
    	this.otherMedFreq[6] = null;
    	this.otherMedName[7] = null;
    	this.otherMedDosage[7] = null;
    	this.otherMedFreq[7] = null;
    	this.arvAdh = null;
    	this.arvPoorAdh[0] = null;
    	this.arvPoorAdh[1] = null;
    	this.arvPoorAdh[2] = null;
    	this.arvPoorAdh[3] = null;
    	this.arvPoorAdh[4] = null;
    	this.arvName = null;
    	this.arvDosage = null;
    	this.arvNoDays = null;
    	this.cd4Count = null;
    	this.cd4Perc = null;
    	this.hgb = null;
    	this.rpr = null;
    	this.sputum = null;
    	this.otherLabName[0] = null;
    	this.otherLabResult[0] = null;
    	this.otherLabName[1] = null;
    	this.otherLabResult[1] = null;
    	this.otherLabName[2] = null;
    	this.otherLabResult[2] = null;
    	this.otherLabName[3] = null;
    	this.otherLabResult[3] = null;
    	this.otherLabName[4] = null;
    	this.otherLabResult[4] = null;
    	this.referral = null;
    	this.atRiskPop = null;
    	this.disclosure = null;
    	this.partnerTested = null;
    	this.condomsDisp = null;
    	this.stiScreen = null;
    	this.nextAppt = null;
    	this.clinicianInit = null;
    }

    public String printHeaderDelim(String delim) {
    	return((getVisId() == null ? "" : clean(getVisId())) + delim +
    		   (getVisDate() == null ? "" : clean(getVisDate())) + delim +
    		   (getVisType() == null ? "" : clean(getVisType())) + delim +
    		   (getMosOnArt() == null ? "" : clean(getMosOnArt())) + delim +
    		   (getMosOnRegimen() == null ? "" : clean(getMosOnRegimen())) + delim +
    		   (getWt() == null ? "" : clean(getWt())) + delim +
    		   (getBp() == null ? "" : clean(getBp())) + delim +
    		   (getHt() == null ? "" : clean(getHt())) + delim +
    		   (getBmi() == null ? "" : clean(getBmi())) + delim +
    		   (getPreg() == null ? "" : clean(getPreg())) + delim +
    		   (getEdd() == null ? "" : clean(getEdd())) + delim +
    		   (getFamPlanStat() == null ? "" : clean(getFamPlanStat())) + delim +
    		   (getFamPlanMethod(0) == null ? "" : clean(getFamPlanMethod(0))) + delim +
    		   (getFamPlanMethod(1) == null ? "" : clean(getFamPlanMethod(1))) + delim +
    		   (getFamPlanMethod(2) == null ? "" : clean(getFamPlanMethod(2))) + delim +
    		   (getFamPlanMethod(3) == null ? "" : clean(getFamPlanMethod(3))) + delim +
    		   (getFamPlanMethod(4) == null ? "" : clean(getFamPlanMethod(4))) + delim +
    		   (getTbStat() == null ? "" : clean(getTbStat())) + delim +
    		   (getTbStartMo() == null ? "" : clean(getTbStartMo())) + delim +
    		   (getTbStartYr() == null ? "" : clean(getTbStartYr())) + delim +
    		   (getTbTreatNo() == null ? "" : clean(getTbTreatNo())) + delim +
    		   (getSideEffect(0) == null ? "" : clean(getSideEffect(0))) + delim +
    		   (getSideEffect(1) == null ? "" : clean(getSideEffect(1))) + delim +
    		   (getSideEffect(2) == null ? "" : clean(getSideEffect(2))) + delim +
    		   (getSideEffect(3) == null ? "" : clean(getSideEffect(3))) + delim +
    		   (getSideEffect(4) == null ? "" : clean(getSideEffect(4))) + delim +
    		   (getOiProblem(0) == null ? "" : clean(getOiProblem(0))) + delim +
    		   (getOiProblem(1) == null ? "" : clean(getOiProblem(1))) + delim +
    		   (getOiProblem(2) == null ? "" : clean(getOiProblem(2))) + delim +
    		   (getOiProblem(3) == null ? "" : clean(getOiProblem(3))) + delim +
    		   (getOiProblem(4) == null ? "" : clean(getOiProblem(4))) + delim +
    		   (getWhoStage() == null ? "" : clean(getWhoStage())) + delim +
    		   (getCtxDisp() == null ? "" : clean(getCtxDisp())) + delim +
    		   (getCtxAdh() == null ? "" : clean(getCtxAdh())) + delim +
    		   (getCtxPoorAdh(0) == null ? "" : clean(getCtxPoorAdh(0))) + delim +
    		   (getCtxPoorAdh(1) == null ? "" : clean(getCtxPoorAdh(1))) + delim +
    		   (getCtxPoorAdh(2) == null ? "" : clean(getCtxPoorAdh(2))) + delim +
    		   (getCtxPoorAdh(3) == null ? "" : clean(getCtxPoorAdh(3))) + delim +
    		   (getCtxPoorAdh(4) == null ? "" : clean(getCtxPoorAdh(4))) + delim +
    		   (getInhDisp() == null ? "" : clean(getInhDisp())) + delim +
    		   (getOtherMedName(0) == null ? "" : clean(getOtherMedName(0))) + delim +
    		   (getOtherMedDosage(0) == null ? "" : clean(getOtherMedDosage(0))) + delim +
    		   (getOtherMedFreq(0) == null ? "" : clean(getOtherMedFreq(0))) + delim +
    		   (getOtherMedName(1) == null ? "" : clean(getOtherMedName(1))) + delim +
    		   (getOtherMedDosage(1) == null ? "" : clean(getOtherMedDosage(1))) + delim +
    		   (getOtherMedFreq(1) == null ? "" : clean(getOtherMedFreq(1))) + delim +
    		   (getOtherMedName(2) == null ? "" : clean(getOtherMedName(2))) + delim +
    		   (getOtherMedDosage(2) == null ? "" : clean(getOtherMedDosage(2))) + delim +
    		   (getOtherMedFreq(2) == null ? "" : clean(getOtherMedFreq(2))) + delim +
    		   (getOtherMedName(3) == null ? "" : clean(getOtherMedName(3))) + delim +
    		   (getOtherMedDosage(3) == null ? "" : clean(getOtherMedDosage(3))) + delim +
    		   (getOtherMedFreq(3) == null ? "" : clean(getOtherMedFreq(3))) + delim +
    		   (getOtherMedName(4) == null ? "" : clean(getOtherMedName(4))) + delim +
    		   (getOtherMedDosage(4) == null ? "" : clean(getOtherMedDosage(4))) + delim +
    		   (getOtherMedFreq(4) == null ? "" : clean(getOtherMedFreq(4))) + delim +
    		   (getOtherMedName(5) == null ? "" : clean(getOtherMedName(5))) + delim +
    		   (getOtherMedDosage(5) == null ? "" : clean(getOtherMedDosage(5))) + delim +
    		   (getOtherMedFreq(5) == null ? "" : clean(getOtherMedFreq(5))) + delim +
    		   (getOtherMedName(6) == null ? "" : clean(getOtherMedName(6))) + delim +
    		   (getOtherMedDosage(6) == null ? "" : clean(getOtherMedDosage(6))) + delim +
    		   (getOtherMedFreq(6) == null ? "" : clean(getOtherMedFreq(6))) + delim +
    		   (getOtherMedName(7) == null ? "" : clean(getOtherMedName(7))) + delim +
    		   (getOtherMedDosage(7) == null ? "" : clean(getOtherMedDosage(7))) + delim +
    		   (getOtherMedFreq(7) == null ? "" : clean(getOtherMedFreq(7))) + delim +
    		   (getArvAdh() == null ? "" : clean(getArvAdh())) + delim +
    		   (getArvPoorAdh(0) == null ? "" : clean(getArvPoorAdh(0))) + delim +
    		   (getArvPoorAdh(1) == null ? "" : clean(getArvPoorAdh(1))) + delim +
    		   (getArvPoorAdh(2) == null ? "" : clean(getArvPoorAdh(2))) + delim +
    		   (getArvPoorAdh(3) == null ? "" : clean(getArvPoorAdh(3))) + delim +
    		   (getArvPoorAdh(4) == null ? "" : clean(getArvPoorAdh(4))) + delim +
    		   (getArvName() == null ? "" : clean(getArvName())) + delim +
    		   (getArvDosage() == null ? "" : clean(getArvDosage())) + delim +
    		   (getArvNoDays() == null ? "" : clean(getArvNoDays())) + delim +
    		   (getCd4Count() == null ? "" : clean(getCd4Count())) + delim +
    		   (getCd4Perc() == null ? "" : clean(getCd4Perc())) + delim +
    		   (getHgb() == null ? "" : clean(getHgb())) + delim +
    		   (getRpr() == null ? "" : clean(getRpr())) + delim +
    		   (getSputum() == null ? "" : clean(getSputum())) + delim +
    		   (getOtherLabName(0) == null ? "" : clean(getOtherLabName(0))) + delim +
    		   (getOtherLabResult(0) == null ? "" : clean(getOtherLabResult(0))) + delim +
    		   (getOtherLabName(1) == null ? "" : clean(getOtherLabName(1))) + delim +
    		   (getOtherLabResult(1) == null ? "" : clean(getOtherLabResult(1))) + delim +
    		   (getOtherLabName(2) == null ? "" : clean(getOtherLabName(2))) + delim +
    		   (getOtherLabResult(2) == null ? "" : clean(getOtherLabResult(2))) + delim +
    		   (getOtherLabName(3) == null ? "" : clean(getOtherLabName(3))) + delim +
    		   (getOtherLabResult(3) == null ? "" : clean(getOtherLabResult(3))) + delim +
    		   (getOtherLabName(4) == null ? "" : clean(getOtherLabName(4))) + delim +
    		   (getOtherLabResult(4) == null ? "" : clean(getOtherLabResult(4))) + delim +
    		   (getReferral() == null ? "" : clean(getReferral())) + delim +
    		   (getAtRiskPop() == null ? "" : clean(getAtRiskPop())) + delim +
    		   (getDisclosure() == null ? "" : clean(getDisclosure())) + delim +
    		   (getPartnerTested() == null ? "" : clean(getPartnerTested())) + delim +
    		   (getCondomsDisp() == null ? "" : clean(getCondomsDisp())) + delim +
    		   (getStiScreen() == null ? "" : clean(getStiScreen())) + delim +
    		   (getNextAppt() == null ? "" : clean(getNextAppt())) + delim +
    		   (getClinicianInit() == null ? "" : clean(getClinicianInit())));
    }
    
	private String clean(String in) {
		return in.replaceAll("\n", " ").replaceAll("\t", " ");
	}
}