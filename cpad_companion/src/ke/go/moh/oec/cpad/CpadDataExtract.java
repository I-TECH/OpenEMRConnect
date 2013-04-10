package ke.go.moh.oec.cpad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

public class CpadDataExtract {
    
    final static String MONTH_MILLIS = "2629743833";
    final static String SESQUICENTENNIAL_MILLIS = "4717440000000";
    final static String YEAR_MILLIS = "31556925994";
    final static int FILLER_CNT = 73;
    final static int MAX_FAMILY_MEMBERS = 8;
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
    static Properties companionProps;
    
    public static void main(String[] args) {
        try {
            companionProps = loadProperties("cpad_companion.properties");
            String method = companionProps.getProperty("scheduler.method");
            int interval = Integer.parseInt(companionProps.getProperty("scheduler.interval"));
            String timeOfDay = companionProps.getProperty("scheduler.timeOfDay");
            if ("interval".equalsIgnoreCase("interval")) {
                while (true) {
                    CpadDataExtract.work();
                    Thread.sleep(interval);
                }
            } else if ("timeofday".equalsIgnoreCase("interval")) {
                DateFormat sdf = new SimpleDateFormat("HH:mm");
                String currentTime = sdf.format(new java.util.Date());
                while (true) {
                    if (currentTime.equalsIgnoreCase(timeOfDay)) {
                        CpadDataExtract.work();
                    }
                }
            } else {
                logAndQuit(Level.SEVERE, "Scheduler method could not be determoned. "
                        + "Check the value set for the property [scheduler.method] in the cpad_companion.properties file. "
                        + "It should either be set to 'interval' or 'timeofday'.");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void work() {
        OutputStreamWriter out = null;
        Connection con = null;
        Connection shadowCon = null;
        Statement stmt = null;
        Statement shadowStmt = null;
        HeaderData header = new HeaderData();
        VisitData visits[] = new VisitData[MAX_VISIT_CNT];
        for (int i = 0; i < MAX_VISIT_CNT; i++) {
            visits[i] = new VisitData();
        }
        
        try {
            Properties sourceProps = loadProperties("source_database.properties");
            Properties shadowProps = loadProperties("shadow_database.properties");
            out = new OutputStreamWriter(new FileOutputStream(companionProps.getProperty("csv.output.filename")), "UTF-8");
            Class.forName(sourceProps.getProperty("driver"));
            con = DriverManager.getConnection(sourceProps.getProperty("url"));
            stmt = con.createStatement();

            // Query shadow database to determine which patients to pull records for
            Class.forName(shadowProps.getProperty("driver"));
            shadowCon = DriverManager.getConnection(shadowProps.getProperty("url"),
                    shadowProps.getProperty("username"),
                    shadowProps.getProperty("password"));
            shadowStmt = shadowCon.createStatement();

            // See if any transactions have happened in the time frame we're interested in for the tables we care about
            // First, get the list of tables that we're interested in
            String tableList = "('" + sourceProps.getProperty("tableList").replace(",", "','") + "')";
            if ("".equals(tableList) || tableList == null) {
                logAndQuit(Level.SEVERE, "No tables listed in properties file.");
            }

            // Next, get the date we want to use when checking for recent transactions
            java.util.Date now = Calendar.getInstance().getTime();
            String transSince = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(now.getTime() - new Long(companionProps.getProperty("check.interval.seconds")) * 1000);
            if ("".equals(transSince) || transSince == null) {
                logAndQuit(Level.SEVERE, "Could not calculate date to use: " + transSince + ".");
            }

            // Finally, query the transaction_data table to get a list of patient_ids associated with the transaction(s)
            ResultSet rs = shadowStmt.executeQuery("SELECT DISTINCT td.data AS data FROM transaction_data td, "
                    + "transaction tr "
                    + "WHERE td.column_id IN "
                    + "(SELECT id FROM `column` "
                    + "WHERE name = 'patient_id' AND table_id IN "
                    + "(SELECT id FROM `table` WHERE name IN " + tableList + " " + ")) "
                    + "AND td.data IS NOT NULL "
                    + "AND LTRIM(RTRIM(td.data)) != '' "
                    + "AND td.transaction_id = tr.id "
                    + "AND tr.created_datetime >= '" + transSince + "'");
            
            ArrayList<String> shadowPids = new ArrayList<String>();
            while (rs.next()) {
                shadowPids.add(rs.getString("data").replace(".0", ""));
            }

            // Need to make sure the patient_ids found in shadow still exist in C-PAD
            rs = stmt.executeQuery("SELECT DISTINCT patient_id FROM tblpatient_information "
                    + "WHERE patient_id IS NOT NULL");
            ArrayList<String> cpadPids = new ArrayList<String>();
            while (rs.next()) {
                cpadPids.add(rs.getString("patient_id").replace(".0", ""));
            }
            
            for (int i = 0; i < cpadPids.size(); i++) {
                if (!shadowPids.contains(cpadPids.get(i))) {
                    cpadPids.remove(cpadPids.get(i));
                    System.out.println("i = " + i);
                }
            }
            
            int recCnt = cpadPids.size();
            if (recCnt == 0) {
                logAndQuit(Level.INFO, "No updated patient records found in the shadow database since " + transSince + ".");
            }
            
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.INFO, "Extracting data for " + recCnt + " patient" + (recCnt == 1 ? "." : "s."));
            
            PreparedStatement headerStmts[] = new PreparedStatement[6];
            headerStmts[0] = con.prepareStatement("select pi.patient_id, pi.first_name, pi.last_name, pi.dob, "
                    + "pi.age, pi.agemnth, pi.date_entered, s.sexname, m.maritalname "
                    + "from (tlkSex s INNER JOIN (tblpatient_information pi LEFT OUTER JOIN "
                    + "tlkmarital m ON pi.marital_status = m.maritalcode) ON s.sexcode = pi.sex) "
                    + "where pi.patient_id = ?");
            headerStmts[1] = con.prepareStatement("select postal_address, telephone, district, "
                    + "location, sub_location "
                    + "from tbladdress "
                    + "where patient_id = ?");
            headerStmts[2] = con.prepareStatement("select ts.first_name, ts.last_name, ts.postal_address, ts.telephone, "
                    + "ts.relationship as rel1, ts.relationship_other, sr.relationship as rel2 "
                    + "from tbltreatment_supporter ts "
                    + "left join tlkSupporter_relationships sr on ts.relationship = sr.relationid "
                    + "where ts.patient_id = ?");
            headerStmts[3] = con.prepareStatement("select fm.FmailyMemAge as age, fm.FmailyMemRel as rel1, "
                    + "sr.relationship as rel2, fm.FmailyMemHIV as hiv_status, fm.FmailyMemCare as in_care, "
                    + "fm.FmailyMemCCCN as pid "
                    + "from tblFamilyMembers fm "
                    + "left join tlkSupporter_relationships sr on fm.FmailyMemRel = sr.relationid "
                    + "where fm.patient_id = ?");
            headerStmts[4] = con.prepareStatement("select label "
                    + "from Tbl_Values tv "
                    + "where tv.category = ? "
                    + "and tv.[value] = ?");
            headerStmts[5] = con.prepareStatement("select Organization, SiteCode, District, Province "
                    + "from tblOrganization");
            
            PreparedStatement visitStmts[] = new PreparedStatement[9];
            visitStmts[0] = con.prepareStatement("select count(visit_id) as visits from tblvisit_information where patient_id = ?");
            visitStmts[1] = con.prepareStatement("select top " + MAX_VISIT_CNT + " vi.visit_id, vi.visit_date, vi.weight, vi.height, "
                    + "p.yesno as pregnancy, vi.delivery_date, t.tbstatus as tbstatus, vi.other_medication, vi.cd4_result, "
                    + "cs.yesno as cotrim, ca.adherence as cotrim_adherence, fs.yesno as fp_status, "
                    + "vi.cd4_results_percent, vi.hb_result, vi.RPR_result, vi.TBSputum_result, "
                    + "vi.art_regimen, ar.firstregimen, vi.art_other, aa.adherence, vi.ARTDose, "
                    + "vi.other_testType, vi.other_test_result, vi.other_testType2, vi.other_test_result2, "
                    + "vi.referred_to, vi.next_visit_date, vi.clinician_initial, vi.WHOstage, "
                    + "vi.BMI, vi.TBStDate, vi.VisitType, vi.DuraSART, vi.DuraCReg, vi.tb_Tx, vi.INH, vi.RiskPopu, "
                    + "vi.PwPDis, vi.PwPPaT, vi.PwPCon, vi.PwPSTI, pi.artstart_date "
                    + "from (tblpatient_information pi INNER JOIN "
                    + "(((((((tblvisit_information vi LEFT OUTER JOIN "
                    + "tlkyesno p ON vi.pregnancy = p.yesnocode) LEFT OUTER JOIN "
                    + "tlktbstatus t ON vi.tb_status = t.tbcode) LEFT OUTER JOIN "
                    + "tlkadherencestatus aa ON vi.art_adherence = aa.adherecode) LEFT OUTER JOIN "
                    + "tlkregimenfirst ar ON vi.art_regimen = ar.regnum) LEFT OUTER JOIN "
                    + "tlkyesno cs ON vi.cotrim = cs.yesnocode) LEFT OUTER JOIN "
                    + "tlkyesno fs ON vi.fp_status = fs.yesnocode) LEFT OUTER JOIN "
                    + "tlkadherencestatus ca ON vi.cotrim_adherence = ca.adherecode) ON pi.patient_id = vi.patient_id) "
                    + "where vi.patient_id = ? "
                    + "and vi.visit_date <= now() "
                    + "order by vi.visit_date desc");
            visitStmts[2] = con.prepareStatement("select vi.visit_date, vi.art_regimen, vi.art_other, ar.firstregimen "
                    + "from tblvisit_information vi "
                    + "left join tlkregimenfirst ar on vi.art_regimen = ar.regnum "
                    + "where vi.patient_id = ? "
                    + "and vi.visit_id <> ? "
                    + "and vi.visit_date <= ? "
                    + "order by vi.visit_date desc");
            visitStmts[3] = con.prepareStatement("select au.unsatisfactoryadherence, uc.UnsatCotriReaon, uc.UnsatCotriother "
                    + "from tblUnsatisfactorycotrimoxazole uc, tlkadherenceunsatisfactory au "
                    + "where uc.patient_id = ? "
                    + "and uc.visit_id = ? "
                    + "and uc.UnsatCotriReaon = au.adherencecode");
            visitStmts[4] = con.prepareStatement("select au.unsatisfactoryadherence, ua.UnsatARTReason, ua.UnsatARTOth "
                    + "from tblUnsatisfactoryart ua, tlkadherenceunsatisfactory au "
                    + "where ua.patient_id = ? "
                    + "and ua.visit_id = ? "
                    + "and ua.UnsatARTReason = au.adherencecode");
            visitStmts[5] = con.prepareStatement("select fp.fpmethod as method, fp.fpother, fl.fpmethod as method2 "
                    + "from tblfpmethod fp "
                    + "left join tlkfpmethod fl on fp.fpmethod = fl.fpmethodcode "
                    + "where fp.patient_id = ? "
                    + "and fp.visit_id = ?");
            visitStmts[6] = con.prepareStatement("select se.artsideeffects, se.othersideeffects, sl.artsideeffects as effects2 "
                    + "from tblARTSideEffects se "
                    + "left join tlkartsideeffects sl on se.artsideeffects = sl.sideeffectscode "
                    + "where se.patient_id = ? "
                    + "and se.visit_id = ?");
            visitStmts[7] = con.prepareStatement("select oi.newoi, oi.newoiother, il.oi_name "
                    + "from tblNewOI oi "
                    + "left join tlkoi_code il on oi.newoi = il.oi_id "
                    + "where oi.patient_id = ? "
                    + "and oi.visit_id = ?");
            visitStmts[8] = con.prepareStatement("select label "
                    + "from Tbl_Values tv "
                    + "where tv.category = 'VisitType' "
                    + "and tv.[value] = ?");
            
            int cnt = 0;
            for (int a = 0; a < cpadPids.size(); a++) {
                int pid = Integer.parseInt(cpadPids.get(a));
                
                header.reset();
                ExtractHeaderData(headerStmts, pid, header, companionProps);
                
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
                    if (i < visits.length - 1) {
                        finalCsv += "\t";
                    }
                }
                out.write(finalCsv + "\n");
                if (++cnt % 100 == 0) {
                    Logger.getLogger(CpadDataExtract.class.getName()).log(Level.INFO, "(" + cnt + ")");
                }
            }
            // Send file to remote Mirth instance if configured to do so
            if ("remote".equalsIgnoreCase(companionProps.getProperty("mirth.location"))) {
                if (!"".equals(companionProps.getProperty("mirth.url"))
                        && companionProps.getProperty("mirth.url") != null) {
                    if (sendMessage(companionProps.getProperty("mirth.url"), companionProps.getProperty("csv.output.filename"))) {
                        Logger.getLogger(CpadDataExtract.class.getName()).log(Level.INFO, "File sent!");
                    } else {
                        Logger.getLogger(CpadDataExtract.class.getName()).log(Level.INFO, "File not sent!");
                    }
                } else {
                    Logger.getLogger(CpadDataExtract.class.getName()).log(Level.INFO, "No URL provided for remote Mirth instance.  The file was not sent!");
                }
            }
            
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.INFO, "Done!");
        } catch (ClassNotFoundException e) {
            System.out.println(e.toString());
        } catch (SQLException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (con != null) {
                    con.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (shadowCon != null) {
                    shadowCon.close();
                }
                if (shadowStmt != null) {
                    shadowStmt.close();
                }
            } catch (Exception ex) {
                Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE,
                        "Exception thrown when attempting to dispose resources! {0}", ex.getMessage());
            }
        }
    }
    
    private static void ExtractHeaderData(PreparedStatement stmts[], int pid, HeaderData header, Properties props) throws SQLException {
        // Fill in prepared statement parameters
        for (int i = 0; i < stmts.length - 2; i++) {
            stmts[i].setInt(1, pid);
        }

        // Get header data fields - first from tblpatient_information
        ResultSet rs = stmts[0].executeQuery();
        while (rs.next()) {
            header.setGivenName(0, rs.getString("first_name"));
            header.setFamName(0, rs.getString("last_name"));
            header.setPid(Integer.toString(pid));
            Date dob = rs.getDate("dob");
            if (dob != null && dob.getTime() <= new GregorianCalendar().getTimeInMillis()
                    && dob.getTime() >= new GregorianCalendar().getTimeInMillis() - new Long(SESQUICENTENNIAL_MILLIS)) {
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
                        header.setDob(new SimpleDateFormat(validMos ? "yyyyMM" : "yyyy").format(input.getTime()
                                - ((ageYrs * new Long(YEAR_MILLIS))
                                + (validMos ? ageMos * new Long(MONTH_MILLIS) : 0))));
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

        // Grab family member data - from tblFamilyMembers
        rs = stmts[3].executeQuery();
        i = 0;
        while (rs.next() && i < MAX_FAMILY_MEMBERS) {
            header.setFamMemberAge(i, rs.getString("age"));
            header.setFamMemberPid(i, rs.getString("pid"));
            int rel = rs.getInt("rel1");
            if (!rs.wasNull()) {
                if (rel == OTHER_RELATIONSHIP_CODE) {
                    header.setFamMemberRelation(i, "Other");
                } else {
                    header.setFamMemberRelation(i, rs.getString("rel2"));
                }
            }
            // Lookup HIV status label from Tbl_Values, if not null
            int z = rs.getInt("hiv_status");
            if (!rs.wasNull()) {
                stmts[4].setString(1, "HIVStat");
                stmts[4].setString(2, Integer.toString(z));
                ResultSet subRs = stmts[4].executeQuery();
                if (subRs.next()) {
                    header.setFamMemberHivStatus(i, subRs.getString("label"));
                }
                subRs.close();
            }
            // Lookup "In Care" label from Tbl_Values, if not null
            z = rs.getInt("in_care");
            if (!rs.wasNull()) {
                stmts[4].setString(1, "tlkyesno");
                stmts[4].setString(2, Integer.toString(z));
                ResultSet subRs = stmts[4].executeQuery();
                if (subRs.next()) {
                    header.setFamMemberInCare(i, subRs.getString("label"));
                }
                subRs.close();
            }
            i++;
        }

        // Set facility name and then output extracted data
        rs = stmts[5].executeQuery();
        if (rs.next()) {
//			header.setFacName(rs.getString("Organization"));
//			header.setFacCode(rs.getString("SiteCode"));
            // Read organization, site code and source system from the properties file instead of from the database
            header.setFacName(props.getProperty("site.name"));
            header.setFacCode(props.getProperty("site.code"));
            header.setSourceSystem(props.getProperty("source.system"));
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
        if (rs.next()) {
            visitCnt = rs.getInt("visits");
        }
        if (visitCnt == 0) {
            return;
        }
        
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
            visits[cnt].setOtherLabResult(0, rs.getString("other_test_result"));
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
    
    private static boolean sendMessage(String url, String filename) {
        int returnStatus = HttpStatus.SC_CREATED;
        HttpClient httpclient = new HttpClient();
        HttpConnectionManager connectionManager = httpclient.getHttpConnectionManager();
        connectionManager.getParams().setSoTimeout(120000);
        
        PostMethod httpPost = new PostMethod(url);
        
        RequestEntity requestEntity = null;
        try {
            FileInputStream message = new FileInputStream(filename);
            Base64InputStream message64 = new Base64InputStream(message, true, -1, null);
            requestEntity = new InputStreamRequestEntity(message64, "application/octet-stream");
        } catch (FileNotFoundException e) {
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "File not found.", e);
        }
        httpPost.setRequestEntity(requestEntity);
        
        try {
            httpclient.executeMethod(httpPost);
            returnStatus = httpPost.getStatusCode();
        } catch (SocketTimeoutException e) {
            returnStatus = HttpStatus.SC_REQUEST_TIMEOUT;
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "Request timed out.  Not retrying.", e);
        } catch (HttpException e) {
            returnStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "HTTP exception.", e);
        } catch (ConnectException e) {
            returnStatus = HttpStatus.SC_SERVICE_UNAVAILABLE;
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "Service unavailable.", e);
        } catch (UnknownHostException e) {
            returnStatus = HttpStatus.SC_NOT_FOUND;
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "Not found.", e);
        } catch (IOException e) {
            returnStatus = HttpStatus.SC_GATEWAY_TIMEOUT;
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "IO exception.", e);
        } finally {
            httpPost.releaseConnection();
        }
        return returnStatus == HttpStatus.SC_OK;
    }
    
    private static Properties loadProperties(String propertiesFile) throws FileNotFoundException {
        try {
            Properties properties = new Properties();
            File propFile = new File(propertiesFile);
            String propFilePath = propFile.getAbsolutePath();
            FileInputStream fis = new FileInputStream(propFilePath);
            properties.load(fis);
            return properties;
        } catch (IOException ex) {
            Logger.getLogger(CpadDataExtract.class.getName()).log(Level.SEVERE, "Properties file not found: " + propertiesFile, ex);
            throw new FileNotFoundException("Properties file not found: " + propertiesFile);
        }
    }
    
    private static void logAndQuit(Level level, String msg) {
        Logger.getLogger(CpadDataExtract.class.getName()).log(level, msg);
        System.exit(0);
    }
}
