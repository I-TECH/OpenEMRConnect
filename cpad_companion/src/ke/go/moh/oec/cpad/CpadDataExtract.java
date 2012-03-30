package ke.go.moh.oec.cpad;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import ke.go.moh.oec.cpad.HeaderData;
import ke.go.moh.oec.cpad.VisitData;

public class CpadDataExtract {
	final static String MONTH_MILLIS = "2629743833";
	final static String ODBC_URL = "jdbc:odbc:cpad_companion";
	final static String OUTPUT_FILENAME = "out.csv";
	final static String SESQUICENTENNIAL_MILLIS = "4717440000000";
	final static String YEAR_MILLIS = "31556925994";
	final static int FILLER_CNT = 197;
	final static int MAX_FAMILY_PLANNING_METHODS = 5;
	final static int MAX_NEW_OI = 5;
	final static int MAX_OTHER_MED = 7;
	final static int MAX_POOR_ADHERENCE_REASONS = 5;
	final static int MAX_SIDE_EFFECTS = 5;
	final static int MAX_SUPPORTERS = 3;
	final static int MAX_VISIT_CNT = 12;
	final static int OTHER_ART_REG_CODE = 36;
	final static int OTHER_FAMILY_PLANNING_CODE = 88;
	final static int OTHER_OI_CODE = 88;
	final static int OTHER_POOR_ADHERENCE_CODE = 13;
	final static int OTHER_RELATIONSHIP_CODE = 16;
	final static int OTHER_SIDE_EFFECTS_CODE = 88;

	public static void main(String[] args) {
		Connection con;
		HeaderData header = new HeaderData();
		VisitData visits[] = new VisitData[MAX_VISIT_CNT];
		for (int i = 0; i < MAX_VISIT_CNT; i++) {
			visits[i] = new VisitData();
		}
		
		try{
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME), "UTF-8");
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			con = DriverManager.getConnection(ODBC_URL);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select count(patient_id) as total from tblpatient_information");
			int recCnt = 0;
			if (rs.next()) {
				recCnt = rs.getInt("total");
			}
			System.out.println("Found " + recCnt + " patients");
			
			rs = stmt.executeQuery("select distinct patient_id from tblpatient_information");

			PreparedStatement headerStmts[] = new PreparedStatement[4];
			headerStmts[0] = con.prepareStatement("select pi.patient_id, pi.first_name, pi.last_name, pi.dob, " +
				"pi.age, pi.agemnth, pi.date_entered, s.sexname, m.maritalname " +
				"from (tlkSex s INNER JOIN (tblpatient_information pi LEFT OUTER JOIN " +
	            "tlkmarital m ON pi.marital_status = m.maritalcode) ON s.sexcode = pi.sex) " +
				"where pi.patient_id = ?");
			headerStmts[1] = con.prepareStatement("select postal_address, telephone, district, " +
				"location, sub_location " +
			    "from tbladdress " +
			    "where patient_id = ?");
			headerStmts[2] = con.prepareStatement("select ts.first_name, ts.last_name, ts.postal_address, ts.telephone, " +
			    "ts.relationship as rel1, ts.relationship_other, sr.relationship as rel2 " +
			    "from tbltreatment_supporter ts " +
			    "left join tlkSupporter_relationships sr on ts.relationship = sr.relationid " +
			    "where ts.patient_id = ?");
			headerStmts[3] = con.prepareStatement("select Organization, SiteCode, District, Province " +
				"from tblOrganization");
			
			PreparedStatement visitStmts[] = new PreparedStatement[9];
			visitStmts[0] = con.prepareStatement("select count(visit_id) as visits from tblvisit_information where patient_id = ?");
			visitStmts[1] = con.prepareStatement("select top " + MAX_VISIT_CNT + " vi.visit_id, vi.visit_date, vi.weight, vi.height, " +
				"p.yesno as pregnancy, vi.delivery_date, t.tbstatus as tbstatus, vi.other_medication, vi.cd4_result, " +
				"cs.yesno as cotrim, ca.adherence as cotrim_adherence, fs.yesno as fp_status, " + 
				"vi.cd4_results_percent, vi.hb_result, vi.RPR_result, vi.TBSputum_result, " +
				"vi.art_regimen, ar.firstregimen, vi.art_other, aa.adherence, vi.ARTDose, " +
				"vi.other_testType, vi.other_test_result, vi.other_testType2, vi.other_test_result2, " +
				"vi.referred_to, vi.next_visit_date, vi.clinician_initial, vi.WHOstage, " +
				"vi.BMI, vi.TBStDate, vi.VisitType, vi.DuraSART, vi.DuraCReg, vi.tb_Tx, vi.INH, vi.RiskPopu, " +
				"vi.PwPDis, vi.PwPPaT, vi.PwPCon, vi.PwPSTI, pi.artstart_date " +
				"from (tblpatient_information pi INNER JOIN " +
				"(((((((tblvisit_information vi LEFT OUTER JOIN " +
				"tlkyesno p ON vi.pregnancy = p.yesnocode) LEFT OUTER JOIN " +
				"tlktbstatus t ON vi.tb_status = t.tbcode) LEFT OUTER JOIN " +
				"tlkadherencestatus aa ON vi.art_adherence = aa.adherecode) LEFT OUTER JOIN " +
				"tlkregimenfirst ar ON vi.art_regimen = ar.regnum) LEFT OUTER JOIN " +
				"tlkyesno cs ON vi.cotrim = cs.yesnocode) LEFT OUTER JOIN " +
				"tlkyesno fs ON vi.fp_status = fs.yesnocode) LEFT OUTER JOIN " +
				"tlkadherencestatus ca ON vi.cotrim_adherence = ca.adherecode) ON pi.patient_id = vi.patient_id) " +
				"where vi.patient_id = ? " +
				"order by vi.visit_date desc");
			visitStmts[2] = con.prepareStatement("select vi.visit_date, vi.art_regimen, vi.art_other, ar.firstregimen " +
				"from tblvisit_information vi " +
				"left join tlkregimenfirst ar on vi.art_regimen = ar.regnum " +
				"where vi.patient_id = ? " +
				"and vi.visit_id <> ? " +
				"and vi.visit_date <= ? " +
				"order by vi.visit_date desc");
			visitStmts[3] = con.prepareStatement("select au.unsatisfactoryadherence, uc.UnsatCotriReaon, uc.UnsatCotriother " +
				"from tblUnsatisfactorycotrimoxazole uc, tlkadherenceunsatisfactory au " +
				"where uc.patient_id = ? " +
				"and uc.visit_id = ? " +
				"and uc.UnsatCotriReaon = au.adherencecode");
			visitStmts[4] = con.prepareStatement("select au.unsatisfactoryadherence, ua.UnsatARTReason, ua.UnsatARTOth " +
				"from tblUnsatisfactoryart ua, tlkadherenceunsatisfactory au " +
				"where ua.patient_id = ? " +
				"and ua.visit_id = ? " +
				"and ua.UnsatARTReason = au.adherencecode");
			visitStmts[5] = con.prepareStatement("select fp.fpmethod as method, fp.fpother, fl.fpmethod as method2 " +
				"from tblfpmethod fp " +
				"left join tlkfpmethod fl on fp.fpmethod = fl.fpmethodcode " +
				"where fp.patient_id = ? " +
				"and fp.visit_id = ?");
			visitStmts[6] = con.prepareStatement("select se.artsideeffects, se.othersideeffects, sl.artsideeffects as effects2 " +
				"from tblARTSideEffects se " +
				"left join tlkartsideeffects sl on se.artsideeffects = sl.sideeffectscode " +
				"where se.patient_id = ? " +
				"and se.visit_id = ?");
			visitStmts[7] = con.prepareStatement("select oi.newoi, oi.newoiother, il.oi_name " +
				"from tblNewOI oi " +
				"left join tlkoi_code il on oi.newoi = il.oi_id " +
				"where oi.patient_id = ? " +
				"and oi.visit_id = ?");
			visitStmts[8] = con.prepareStatement("select label " +
				"from Tbl_Values tv " +
				"where tv.category = 'VisitType' " + 
				"and tv.[value] = ?");

			int cnt = 0;
			while (rs.next()) {
				int pid = rs.getInt("patient_id");
				
				header.reset();
				ExtractHeaderData(headerStmts, pid, header);

				for (int i = 0; i < MAX_VISIT_CNT; i++) {
					visits[i].reset();
				}
				ExtractVisitData(visitStmts, pid, visits);

				String finalCsv = "";
				finalCsv += header.printHeaderDelim("\t");
				finalCsv += "\t";
				// Fill in currently unused fields
				for (int i = 0; i < FILLER_CNT; i++) {
					finalCsv += "\t";
				}
				for (int i = 0; i < visits.length; i++) {
					finalCsv += visits[i].printHeaderDelim("\t");
					if (i < visits.length - 1) finalCsv += "\t";
				}
				out.write(finalCsv + "\n");
				if (++cnt % 100 == 0) System.out.println("(" + cnt + ")");
			}
			System.out.println("Done!");
			stmt.close();
			con.close();
			out.close();
		} catch(ClassNotFoundException e) {
			System.out.println(e.toString());
		} catch(SQLException e) {
			System.out.println(e.toString());
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}

	private static void ExtractHeaderData(PreparedStatement stmts[], int pid, HeaderData header) throws SQLException {
		// Fill in prepared statement parameters
		for (int i = 0; i < stmts.length - 1; i++) {
			stmts[i].setInt(1, pid);
		}
		
		// Get header data fields - first from tblpatient_information
		ResultSet rs = stmts[0].executeQuery();
		while (rs.next()) {
			header.setGivenName(0, rs.getString("first_name"));
			header.setFamName(0, rs.getString("last_name"));
			header.setPid(Integer.toString(pid));
			Date dob = rs.getDate("dob");
			if (dob != null && dob.getTime() <= new GregorianCalendar().getTimeInMillis() &&
				dob.getTime() >= new GregorianCalendar().getTimeInMillis() - new Long(SESQUICENTENNIAL_MILLIS)) {
				header.setDob(new SimpleDateFormat("yyyyMMdd").format(dob.getTime()));
			} else {
				int ageYrs = rs.getInt("age");
				if (!rs.wasNull() && ageYrs >= 0) {
					boolean validMos = false;
					int ageMos = rs.getInt("agemnth");
					if (!rs.wasNull() && ageMos >= 0 && ageMos <= 12) {
						validMos = true;
					}
					Date input = rs.getDate("date_entered");
					if (!rs.wasNull() && input.getTime() <= new GregorianCalendar().getTimeInMillis()) {
						header.setDob(new SimpleDateFormat(validMos ? "yyyyMM" : "yyyy").format(input.getTime() -
								        	   ((ageYrs * new Long(YEAR_MILLIS)) +
								        		(validMos ? ageMos * new Long(MONTH_MILLIS): 0))));
					}
				}
			}
			header.setGender(rs.getString("sexname"));
			header.setMarStatus(rs.getString("maritalname"));
		}

		// More header data fields - now from tbladdress
		rs = stmts[1].executeQuery();
		while (rs.next()) {
			header.setAddr(0, rs.getString("postal_address"));
			header.setPhone(0, rs.getString("telephone"));
			header.setState(rs.getString("district"));
			header.setCounty(rs.getString("location"));
			header.setCity(rs.getString("sub_location"));
		}

		// Last of the header data fields - from tbltreatment_supporter
		rs = stmts[2].executeQuery();
		int i = 0;
		while (rs.next() && i < MAX_SUPPORTERS) {
			header.setSupGivenName(i, 0, rs.getString("first_name"));
			header.setSupFamName(i, 0, rs.getString("last_name"));
			header.setSupAddr(i, 0, rs.getString("postal_address"));
			header.setSupPhone(i, rs.getString("telephone"));
			int rel = rs.getInt("rel1");
			if (!rs.wasNull()) {
				if (rel == OTHER_RELATIONSHIP_CODE) {
					String tmp = rs.getString("relationship_other");
					if (!rs.wasNull()) {
						header.setSupRelation(i, "Other: " + tmp.toLowerCase());
					}
				} else {
					header.setSupRelation(i, rs.getString("rel2"));
				}
			}
			i++;
		}

		// Set facility name and then output extracted data
		rs = stmts[3].executeQuery();
		if (rs.next()) {
			header.setFacName(rs.getString("Organization"));
			header.setFacCode(rs.getString("SiteCode"));
			header.setFacCounty(rs.getString("District"));
			header.setFacState(rs.getString("Province"));
		}

		rs.close();
	}

	private static void ExtractVisitData(PreparedStatement stmts[], int pid, VisitData[] visits) throws SQLException {
		int visitCnt = 0;
		ResultSet subRs = null;

		// Fill in some of the prepared statement parameters (just 'pid' at this point)
		// Skip the last one, as that needs to be set with data retrieved at a later time
		for (int i = 0; i < stmts.length - 1; i++) {
			stmts[i].setInt(1, pid);
		}
		
		// Loop over visits (only the most recent number as defined by MAX_VISIT_CNT variable) and pull required data
		ResultSet rs = stmts[0].executeQuery();
		if (rs.next()) visitCnt = rs.getInt("visits");
		if (visitCnt == 0) return;
		
		rs = stmts[1].executeQuery();
		int cnt = 0;
		while (rs.next() && cnt < MAX_VISIT_CNT) {
			String visId = rs.getString("visit_id");
			for (int i = 2; i < stmts.length - 1; i++) {
				stmts[i].setInt(2, Integer.parseInt(visId));
			}
			visits[cnt].setVisId(visId);
			int z = rs.getInt("VisitType");
			// Lookup visit type label from Tbl_Values, if not null
			if (!rs.wasNull()) {
				stmts[8].setString(1, Integer.toString(z));
				subRs = stmts[8].executeQuery();
				if (subRs.next()) {
					visits[cnt].setVisType(subRs.getString("label"));
				}
			}
			Date vDate = rs.getDate("visit_date");
			if (!rs.wasNull()) {
				visits[cnt].setVisDate(new SimpleDateFormat("yyyyMMdd").format(vDate.getTime()));

				// Determine prior regimen to be used when completing adherence elements
				stmts[2].setString(3, new SimpleDateFormat("yyyy-MM-dd").format(vDate.getTime()));
				subRs = stmts[2].executeQuery();
				String prevReg = "";
				while (subRs.next() && "".equals(prevReg)) {
					int regCode = subRs.getInt("art_regimen");
					if (!subRs.wasNull()) {
						if (regCode == OTHER_ART_REG_CODE) {
							prevReg = subRs.getString("art_other");
						} else {
							prevReg = subRs.getString("firstregimen");
						}
						if (subRs.wasNull()) {
							prevReg = "";
						}
					}
				}
				visits[cnt].setPriorArvName(prevReg);
			}
			visits[cnt].setWt(rs.getString("weight"));
			visits[cnt].setHt(rs.getString("height"));
			visits[cnt].setBmi(rs.getString("BMI"));
			visits[cnt].setPreg(rs.getString("pregnancy"));
			Date tmpDate = rs.getDate("delivery_date");
			if (tmpDate != null) {
				visits[cnt].setEdd(new SimpleDateFormat("yyyyMMdd").format(tmpDate.getTime()));
			}
			visits[cnt].setFamPlanStat(rs.getString("fp_status"));
			visits[cnt].setTbStat(rs.getString("tbstatus"));
			tmpDate = rs.getDate("TBStDate");
			if (tmpDate != null) {
				visits[cnt].setTbStartMo(new SimpleDateFormat("MM").format(tmpDate.getTime()));
				visits[cnt].setTbStartYr(new SimpleDateFormat("yyyy").format(tmpDate.getTime()));
			}
			z = rs.getInt("DuraSART");
			visits[cnt].setMosOnArt(Integer.toString(z));
			z = rs.getInt("DuraCReg");
			visits[cnt].setMosOnRegimen(Integer.toString(z));
			visits[cnt].setTbTreatNo(rs.getString("tb_Tx"));
			visits[cnt].setWhoStage(rs.getString("WHOstage"));
			visits[cnt].setCtxAdh(rs.getString("cotrim_adherence"));
			visits[cnt].setCtxDisp(rs.getString("cotrim"));
			visits[cnt].setInhDisp(rs.getString("INH"));
			String tmp = rs.getString("other_medication");
			if (!rs.wasNull()) {
				String tmpArr[] = tmp.split(",");
				for (int i = 0; i < tmpArr.length; i++) {
					if (i > MAX_OTHER_MED) {
						break;
					} else {
						visits[cnt].setOtherMedName(i, tmpArr[i]);
					}
				}
			}
			int regCode = rs.getInt("art_regimen");
			if (!rs.wasNull()) {
				if (regCode == OTHER_ART_REG_CODE) {
					tmp = rs.getString("art_other");
					if (!rs.wasNull()) {
						visits[cnt].setArvName("Other: " + tmp.toUpperCase());
					}
				} else {
					visits[cnt].setArvName(rs.getString("firstregimen"));
				}
			}
			visits[cnt].setPriorArvAdh(rs.getString("adherence"));
			visits[cnt].setArvDosage(rs.getString("ARTDose"));
			visits[cnt].setCd4Count(rs.getString("cd4_result"));
			visits[cnt].setCd4Perc(rs.getString("cd4_results_percent"));
			visits[cnt].setHgb(rs.getString("hb_result"));
			visits[cnt].setRpr(rs.getString("RPR_result"));
			visits[cnt].setSputum(rs.getString("TBSputum_result"));
			visits[cnt].setOtherLabName(0, rs.getString("other_testType"));
			visits[cnt].setOtherLabResult(0,rs.getString("other_test_result"));
			visits[cnt].setOtherLabName(1, rs.getString("other_testType2"));
			visits[cnt].setOtherLabResult(1, rs.getString("other_test_result2"));
			visits[cnt].setReferral(rs.getString("referred_to"));
			visits[cnt].setAtRiskPop(rs.getString("RiskPopu"));
			visits[cnt].setDisclosure(rs.getString("PwPDis"));
			visits[cnt].setPartnerTested(rs.getString("PwPPaT"));
			visits[cnt].setCondomsDisp(rs.getString("PwPCon"));
			visits[cnt].setStiScreen(rs.getString("PwPSTI"));
			tmpDate = rs.getDate("next_visit_date");
			if (tmpDate != null) {
				visits[cnt].setNextAppt(new SimpleDateFormat("yyyyMMdd").format(tmpDate.getTime()));
			}
			visits[cnt].setClinicianInit(rs.getString("clinician_initial"));

			// More visit data fields - still from tblvisit_information
			subRs = stmts[3].executeQuery();
			int i = 0;
			while (subRs.next() && i < MAX_POOR_ADHERENCE_REASONS) {
				int adhCode = subRs.getInt("UnsatCotriReaon");
				if (!subRs.wasNull()) {
					if (adhCode == OTHER_POOR_ADHERENCE_CODE) {
						tmp = subRs.getString("UnsatCotriother");
						if (!subRs.wasNull()) {
							visits[cnt].setCtxPoorAdh(i, "Other: " + tmp.toLowerCase());
						}
					} else {
						visits[cnt].setCtxPoorAdh(i, subRs.getString("unsatisfactoryadherence"));
					}
				}
				i++;
			}
			subRs = stmts[4].executeQuery();
			i = 0;
			while (subRs.next() && i < MAX_POOR_ADHERENCE_REASONS) {
				int adhCode = subRs.getInt("UnsatARTReason");
				if (!subRs.wasNull()) {
					if (adhCode == OTHER_POOR_ADHERENCE_CODE) {
						tmp = subRs.getString("UnsatARTOth");
						if (!subRs.wasNull()) {
							visits[cnt].setPriorArvPoorAdh(i, "Other: " + tmp.toLowerCase());
						}
					} else {
						visits[cnt].setPriorArvPoorAdh(i, subRs.getString("unsatisfactoryadherence"));
					}
				}
				i++;
			}

			// More visit data fields - now from tblfpmethod
			subRs = stmts[5].executeQuery();
			i = 0;
			while (subRs.next() && i < MAX_FAMILY_PLANNING_METHODS) {
				int fpCode = subRs.getInt("method");
				if (!subRs.wasNull()) {
					if (fpCode == OTHER_FAMILY_PLANNING_CODE) {
						tmp = subRs.getString("fpother");
						if (!subRs.wasNull()) {
							visits[cnt].setFamPlanMethod(i, "Other: " + tmp.toLowerCase());
						}
					} else {
						visits[cnt].setFamPlanMethod(i, subRs.getString("method2"));
					}
				}
				i++;
			}

			// More visit data fields - now from tblARTSideEffects
			subRs = stmts[6].executeQuery();
			i = 0;
			while (subRs.next() && i < MAX_SIDE_EFFECTS) {
				int seCode = subRs.getInt("artsideeffects");
				if (!subRs.wasNull()) {
					if (seCode == OTHER_SIDE_EFFECTS_CODE) {
						tmp = subRs.getString("othersideeffects");
						if (!subRs.wasNull()) {
							visits[cnt].setSideEffect(i, "Other: " + tmp.toLowerCase());
						}
					} else {
						visits[cnt].setSideEffect(i, subRs.getString("effects2"));
					}
				}
				i++;
			}

			// Last of the visit data fields - from tblNewOI
			subRs = stmts[7].executeQuery();
			i = 0;
			while (subRs.next() && i < MAX_NEW_OI) {
				int oiCode = subRs.getInt("newoi");
				if (!subRs.wasNull()) {
					if (oiCode == OTHER_OI_CODE) {
						tmp = subRs.getString("newoiother");
						if (!subRs.wasNull()) {
							visits[cnt].setOiProblem(i, "Other: " + tmp.toLowerCase());
						}
					} else {
						visits[cnt].setOiProblem(i, subRs.getString("oi_name"));
					}
				}
				i++;
			}
			subRs.close();
			cnt++;
		}
		rs.close();
	}
}