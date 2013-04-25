package ke.go.moh.oec.cpad;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import ke.go.moh.oec.oecsm.bridge.DatabaseConnector;

public class CpadDataExtractFromShadow extends DatabaseConnector {

    final static String MONTH_MILLIS = "2629743833";
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
        new CpadDataExtractFromShadow().start();
    }

    public void start() {
        HeaderData header = new HeaderData();
        VisitData visits[] = new VisitData[MAX_VISIT_CNT];
        for (int i = 0; i < MAX_VISIT_CNT; i++) {
            visits[i] = new VisitData();
        }

        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME), "UTF-8");
            connectToShadow();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select count(c.data) as total "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblpatient_information' "
                    + "and l.name = 'patient_id' "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id");
            int recCnt = 0;
            if (rs.next()) {
                recCnt = rs.getInt("total");
            }
            System.out.println("Found " + recCnt + " patients");

            rs = stmt.executeQuery("select distinct c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblpatient_information' "
                    + "and l.name = 'patient_id' "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id");

            PreparedStatement lookupStmts[] = new PreparedStatement[2];
            lookupStmts[0] = connection.prepareStatement("select c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = ? "
                    + "and l.name = ? "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id "
                    + "and c.primary_key_value = ?");
            lookupStmts[1] = connection.prepareStatement("select c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2 "
                    + "where t.name = ? "
                    + "and l1.name = ? "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = ? "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ?");

            PreparedStatement headerStmts[] = new PreparedStatement[5];
            headerStmts[0] = connection.prepareStatement("select l.name, c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblpatient_information' "
                    + "and l.name in ('first_name', 'last_name', 'dob', 'age', 'agemnth', 'date_entered', 'sex', 'marital_status') "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id "
                    + "and c.primary_key_value = ?");
            headerStmts[1] = connection.prepareStatement("select l1.name, c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2 "
                    + "where t.name = 'tbladdress' "
                    + "and l1.name in ('postal_address', 'telephone', 'district', 'location', 'sub_location') "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'patient_id' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ?");
            headerStmts[2] = connection.prepareStatement("select c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2 "
                    + "where t.name = 'tbltreatment_supporter' "
                    + "and l1.name = 'treatment_supporter_id' "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'patient_id' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ?");
            headerStmts[3] = connection.prepareStatement("select l.name, c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tbltreatment_supporter' "
                    + "and l.name in ('first_name', 'last_name', 'postal_address', 'telephone', 'relationship', 'relationship_other') "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id "
                    + "and c.primary_key_value = ?");
            headerStmts[4] = connection.prepareStatement("select l.name, c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblOrganization' "
                    + "and l.name in ('Organization', 'SiteCode', 'District', 'Province') "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id");

            PreparedStatement visitStmts[] = new PreparedStatement[8];
            visitStmts[0] = connection.prepareStatement("select count(c1.data) as visits "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2 "
                    + "where t.name = 'tblvisit_information' "
                    + "and l1.name = 'visit_id' "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'patient_id' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ?");
            visitStmts[1] = connection.prepareStatement("select c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2, `column` l3, `cell` c3 "
                    + "where t.name = 'tblvisit_information' "
                    + "and l1.name = 'visit_id' "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'patient_id' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ? "
                    + "and c1.primary_key_value = c3.primary_key_value "
                    + "and l3.name = 'visit_date' "
                    + "and t.id = l3.table_id "
                    + "and l3.id = c3.column_id "
                    + "order by c3.data desc "
                    + "limit " + MAX_VISIT_CNT);
            visitStmts[2] = connection.prepareStatement("select c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2, `column` l3, `cell` c3 "
                    + "where t.name = 'tblvisit_information' "
                    + "and l1.name = 'visit_id' "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'patient_id' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ? "
                    + "and c1.primary_key_value = c3.primary_key_value "
                    + "and l3.name = 'visit_date' "
                    + "and t.id = l3.table_id "
                    + "and l3.id = c3.column_id "
                    + "and c1.data <> ? "
                    + "and c3.data <= ? "
                    + "order by c3.data desc");
            visitStmts[3] = connection.prepareStatement("select l.name, c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblvisit_information' "
                    + "and l.name in ('visit_date', 'weight', 'height', "
                    + "'pregnancy', 'delivery_date', 'tb_status', 'other_medication', 'cd4_result', "
                    + "'cotrim', 'cotrim_adherence', 'fp_status', "
                    + "'cd4_results_percent', 'hb_result', 'RPR_result', 'TBSputum_result', "
                    + "'art_regimen', 'art_regimen', 'art_other', 'art_adherence', 'ARTDose', "
                    + "'other_testType', 'other_test_result', 'other_testType2', 'other_test_result2', "
                    + "'referred_to', 'next_visit_date', 'clinician_initial', 'WHOstage', "
                    + "'BMI', 'TBStDate', 'VisitType', 'DuraSART', 'DuraCReg', 'tb_Tx', 'INH', 'RiskPopu', "
                    + "'PwPDis', 'PwPPaT', 'PwPCon', 'PwPSTI') "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id "
                    + "and c.primary_key_value = ?");
            visitStmts[4] = connection.prepareStatement("select l.name, c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = 'tblvisit_information' "
                    + "and l.name in ('art_regimen', 'art_other') "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id "
                    + "and c.primary_key_value = ?");
            visitStmts[5] = connection.prepareStatement("select c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2 "
                    + "where t.name = ? "
                    + "and l1.name = ? "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'visit_id' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = ?");
            visitStmts[6] = connection.prepareStatement("select l.name, c.data "
                    + "from `table` t, `column` l, `cell` c "
                    + "where t.name = ? "
                    + "and l.name = ? "
                    + "and t.id = l.table_id "
                    + "and l.id = c.column_id "
                    + "and c.primary_key_value = ?");
            visitStmts[7] = connection.prepareStatement("select c1.data "
                    + "from `table` t, `column` l1, `cell` c1, `column` l2, `cell` c2, `column` l3, `cell` c3 "
                    + "where t.name = 'Tbl_Values' "
                    + "and l1.name = 'label' "
                    + "and t.id = l1.table_id "
                    + "and l1.id = c1.column_id "
                    + "and c1.primary_key_value = c2.primary_key_value "
                    + "and l2.name = 'category' "
                    + "and t.id = l2.table_id "
                    + "and l2.id = c2.column_id "
                    + "and c2.data = 'VisitType' "
                    + "and c1.primary_key_value = c3.primary_key_value "
                    + "and l3.name = 'value' "
                    + "and t.id = l3.table_id "
                    + "and l3.id = c3.column_id "
                    + "and c3.data = ?");

            int cnt = 0;
            while (rs.next()) {
                int pid = rs.getInt("data");

                header.reset();
                ExtractHeaderData(lookupStmts, headerStmts, pid, header);

                for (int i = 0; i < MAX_VISIT_CNT; i++) {
                    visits[i].reset();
                }
                ExtractVisitData(lookupStmts, visitStmts, pid, visits);

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
                    System.out.println("(" + cnt + ")");
                }
            }
            System.out.println("Done!");
            stmt.close();
            connection.close();
            out.close();
        } catch (ClassNotFoundException e) {
            System.out.println(e.toString());
        } catch (SQLException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println(e.toString());
        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }

    private static void ExtractHeaderData(PreparedStatement lookups[], PreparedStatement[] stmts, int pid,
            HeaderData header) throws SQLException, ParseException {
        // Fill in prepared statement parameters
        for (int i = 0; i < stmts.length - 2; i++) {
            stmts[i].setInt(1, pid);
        }

        // Get header data fields - first from tblpatient_information
        ResultSet rs = stmts[0].executeQuery();
        Map<String, String> map = mapResultSet(rs);
        header.setGivenName(0, map.get("first_name"));
        header.setFamName(0, map.get("last_name"));
        header.setPid(Integer.toString(pid));
        Date dob = null;
        if (map.get("dob") != null) {
            dob = Date.valueOf(map.get("dob").substring(0, 10));
        }
        if (dob != null && dob.getTime() <= new GregorianCalendar().getTimeInMillis()
                && dob.getTime() >= new GregorianCalendar().getTimeInMillis() - new Long(SESQUICENTENNIAL_MILLIS)) {
            header.setDob(new SimpleDateFormat("yyyyMMdd").format(dob.getTime()));
        } else {
            Integer ageYrs = null;
            if (map.get("age") != null) {
                ageYrs = Integer.parseInt(map.get("age"));
            }
            if (ageYrs != null && ageYrs >= 0) {
                boolean validMos = false;
                Integer ageMos = null;
                if (map.get("agemnth") != null) {
                    ageMos = Integer.parseInt(map.get("agemnth"));
                }
                if (ageMos != null && ageMos >= 0 && ageMos <= 12) {
                    validMos = true;
                }
                Date input = null;
                if (map.get("date_entered") != null) {
                    input = Date.valueOf(map.get("date_entered").substring(0, 10));
                }
                if (input != null && input.getTime() <= new GregorianCalendar().getTimeInMillis()) {
                    header.setDob(new SimpleDateFormat(validMos ? "yyyyMM" : "yyyy").format(input.getTime()
                            - ((ageYrs * new Long(YEAR_MILLIS))
                            + (validMos ? ageMos * new Long(MONTH_MILLIS) : 0))));
                }
            }
        }

        if (map.get("sex") != null) {
            lookups[0].setString(1, "tlkSex");
            lookups[0].setString(2, "sexname");
            lookups[0].setInt(3, Integer.parseInt(map.get("sex")));
            rs = lookups[0].executeQuery();
            if (rs.next()) {
                header.setGender(rs.getString("data"));
            }
        }
        if (map.get("marital_status") != null) {
            lookups[0].setString(1, "tlkmarital");
            lookups[0].setString(2, "maritalname");
            lookups[0].setInt(3, Integer.parseInt(map.get("marital_status")));
            rs = lookups[0].executeQuery();
            if (rs.next()) {
                header.setMarStatus(rs.getString("data"));
            }
        }

        // More header data fields - now from tbladdress
        rs = stmts[1].executeQuery();
        map = mapResultSet(rs);
        header.setAddr(0, map.get("postal_address"));
        header.setPhone(0, map.get("telephone"));
        header.setState(map.get("district"));
        header.setCounty(map.get("location"));
        header.setCity(map.get("sub_location"));

        // Last of the header data fields - from tbltreatment_supporter
        rs = stmts[2].executeQuery();
        int i = 0;
        while (rs.next() && i < MAX_SUPPORTERS) {
            Integer id = rs.getInt("data");
            if (!rs.wasNull() && id != null) {
                stmts[3].setInt(1, rs.getInt("data"));
                ResultSet rs2 = stmts[3].executeQuery();
                map = mapResultSet(rs2);
                header.setSupGivenName(i, 0, map.get("first_name"));
                header.setSupFamName(i, 0, map.get("last_name"));
                header.setSupAddr(i, 0, map.get("postal_address"));
                header.setSupPhone(i, map.get("telephone"));
                Integer rel = null;
                if (map.get("relationship") != null) {
                    rel = Integer.parseInt(map.get("relationship"));
                }
                if (rel != null) {
                    if (rel == OTHER_RELATIONSHIP_CODE) {
                        String tmp = null;
                        if (map.get("relationship_other") != null) {
                            tmp = map.get("relationship_other");
                        }
                        if (tmp != null) {
                            header.setSupRelation(i, "Other: " + tmp.toLowerCase());
                        }
                    } else {
                        lookups[0].setString(1, "tlkSupporter_relationships");
                        lookups[0].setString(2, "relationship");
                        lookups[0].setInt(3, rel);
                        ResultSet rs3 = lookups[0].executeQuery();
                        if (rs3.next()) {
                            header.setSupRelation(i, rs3.getString("data"));
                        }
                        rs3.close();
                    }
                }
                rs2.close();
            }
            i++;
        }

        // Set facility name and then output extracted data
        rs = stmts[4].executeQuery();
        map = mapResultSet(rs);
        header.setFacName(map.get("Organization"));
        header.setFacCode(map.get("SiteCode"));
        header.setFacCounty(map.get("District"));
        header.setFacState(map.get("Province"));

        rs.close();
    }

    private static void ExtractVisitData(PreparedStatement lookups[], PreparedStatement[] stmts, int pid, VisitData[] visits) throws SQLException {
        int visitCnt = 0;

        // Fill in some of the prepared statement parameters (just 'pid' at this point)
        for (int i = 0; i < 3; i++) {
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
            String visId = rs.getString("data");
            stmts[2].setInt(2, Integer.parseInt(visId));
            stmts[3].setInt(1, Integer.parseInt(visId));
            visits[cnt].setVisId(visId);
            ResultSet rs2 = stmts[3].executeQuery();
            Map<String, String> map = mapResultSet(rs2);
            // Lookup visit type label from Tbl_Values, if not null
            Integer z = null;
            if (map.get("VisitType") != null) {
                z = Integer.parseInt(map.get("VisitType"));
            }
            if (z != null) {
                stmts[7].setInt(1, z);
                ResultSet rs3 = stmts[7].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setVisType(rs3.getString("data"));
                }
                rs3.close();
            }
            Date vDate = null;
            if (map.get("visit_date") != null) {
                vDate = Date.valueOf(map.get("visit_date").substring(0, 10));
            }
            if (vDate != null) {
                visits[cnt].setVisDate(new SimpleDateFormat("yyyyMMdd").format(vDate.getTime()));

                // Determine prior regimen to be used when completing adherence elements
                stmts[2].setString(3, new SimpleDateFormat("yyyy-MM-dd").format(vDate.getTime()));
                ResultSet rs3 = stmts[2].executeQuery();
                String prevReg = "";
                while (rs3.next() && "".equals(prevReg)) {
                    stmts[4].setInt(1, rs.getInt("data"));
                    ResultSet rs4 = stmts[4].executeQuery();
                    Map<String, String> map2 = mapResultSet(rs4);
                    Integer regCode = null;
                    if (map2.get("art_regimen") != null) {
                        regCode = Integer.parseInt(map2.get("art_regimen"));
                    }
                    if (regCode != null) {
                        if (regCode == OTHER_ART_REG_CODE) {
                            String tmp = null;
                            if (map2.get("art_other") != null) {
                                tmp = map2.get("art_other");
                            }
                            if (tmp != null) {
                                prevReg = tmp;
                            } else {
                                prevReg = "";
                            }
                        } else {
                            lookups[1].setString(1, "tlkregimenfirst");
                            lookups[1].setString(2, "firstregimen");
                            lookups[1].setString(3, "regnum");
                            lookups[1].setInt(4, regCode);
                            ResultSet rs5 = lookups[1].executeQuery();
                            if (rs5.next()) {
                                prevReg = rs5.getString("data");
                            }
                            if (rs5.wasNull()) {
                                prevReg = "";
                            }
                            rs5.close();
                        }
                    }
                }
                visits[cnt].setPriorArvName(prevReg);
            }
            visits[cnt].setWt(map.get("weight"));
            visits[cnt].setHt(map.get("height"));
            visits[cnt].setBmi(map.get("BMI"));
            if (map.get("pregnancy") != null) {
                lookups[0].setString(1, "tlkyesno");
                lookups[0].setString(2, "yesno");
                lookups[0].setInt(3, Integer.parseInt(map.get("pregnancy")));
                ResultSet rs3 = lookups[0].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setPreg(rs3.getString("data"));
                }
                rs3.close();
            }
            Date tmpDate = null;
            if (map.get("delivery_date") != null) {
                tmpDate = Date.valueOf(map.get("delivery_date").substring(0, 10));
            }
            if (tmpDate != null) {
                visits[cnt].setEdd(new SimpleDateFormat("yyyyMMdd").format(tmpDate.getTime()));
            }
            if (map.get("fp_status") != null) {
                lookups[0].setString(1, "tlkyesno");
                lookups[0].setString(2, "yesno");
                lookups[0].setInt(3, Integer.parseInt(map.get("fp_status")));
                ResultSet rs3 = lookups[0].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setFamPlanStat(rs3.getString("data"));
                }
                rs3.close();
            }
            if (map.get("tb_status") != null) {
                lookups[0].setString(1, "tlktbstatus");
                lookups[0].setString(2, "tbstatus");
                lookups[0].setInt(3, Integer.parseInt(map.get("tb_status")));
                ResultSet rs3 = lookups[0].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setTbStat(rs3.getString("data"));
                }
                rs3.close();
            }
            tmpDate = null;
            if (map.get("TBStDate") != null) {
                tmpDate = Date.valueOf(map.get("TBStDate").substring(0, 10));
            }
            if (tmpDate != null) {
                visits[cnt].setTbStartMo(new SimpleDateFormat("MM").format(tmpDate.getTime()));
                visits[cnt].setTbStartYr(new SimpleDateFormat("yyyy").format(tmpDate.getTime()));
            }
            visits[cnt].setMosOnArt(map.get("DuraSART"));
            visits[cnt].setMosOnRegimen(map.get("DuraCReg"));
            visits[cnt].setTbTreatNo(map.get("tb_Tx"));
            visits[cnt].setWhoStage(map.get("WHOstage"));
            if (map.get("cotrim_adherence") != null) {
                lookups[0].setString(1, "tlkadherencestatus");
                lookups[0].setString(2, "adherence");
                lookups[0].setInt(3, Integer.parseInt(map.get("cotrim_adherence")));
                ResultSet rs3 = lookups[0].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setCtxAdh(rs3.getString("data"));
                }
                rs3.close();
            }
            if (map.get("cotrim") != null) {
                lookups[0].setString(1, "tlkyesno");
                lookups[0].setString(2, "yesno");
                lookups[0].setInt(3, Integer.parseInt(map.get("cotrim")));
                ResultSet rs3 = lookups[0].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setCtxDisp(rs3.getString("data"));
                }
                rs3.close();
            }
            visits[cnt].setInhDisp(map.get("INH"));
            String tmp = null;
            if (map.get("other_medication") != null) {
                tmp = map.get("other_medication");
            }
            if (tmp != null) {
                String tmpArr[] = tmp.split(",");
                for (int i = 0; i < tmpArr.length; i++) {
                    if (i > MAX_OTHER_MED) {
                        break;
                    } else {
                        visits[cnt].setOtherMedName(i, tmpArr[i]);
                    }
                }
            }
            Integer regCode = null;
            if (map.get("art_regimen") != null) {
                regCode = Integer.parseInt(map.get("art_regimen"));
            }
            if (regCode != null) {
                if (regCode == OTHER_ART_REG_CODE) {
                    tmp = null;
                    if (map.get("art_other") != null) {
                        tmp = map.get("art_other");
                    }
                    if (tmp != null) {
                        visits[cnt].setArvName("Other: " + tmp.toUpperCase());
                    }
                } else {
                    lookups[1].setString(1, "tlkregimenfirst");
                    lookups[1].setString(2, "firstregimen");
                    lookups[1].setString(3, "regnum");
                    lookups[1].setInt(4, regCode);
                    ResultSet rs4 = lookups[1].executeQuery();
                    if (rs4.next()) {
                        visits[cnt].setArvName(rs4.getString("data"));
                    }
                    rs4.close();
                }
            }
            if (map.get("art_adherence") != null) {
                lookups[0].setString(1, "tlkadherencestatus");
                lookups[0].setString(2, "adherence");
                lookups[0].setInt(3, Integer.parseInt(map.get("art_adherence")));
                ResultSet rs3 = lookups[0].executeQuery();
                if (rs3.next()) {
                    visits[cnt].setPriorArvAdh(rs3.getString("data"));
                }
                rs3.close();
            }
            visits[cnt].setArvDosage(map.get("ARTDose"));
            visits[cnt].setCd4Count(map.get("cd4_result"));
            visits[cnt].setCd4Perc(map.get("cd4_results_percent"));
            visits[cnt].setHgb(map.get("hb_result"));
            visits[cnt].setRpr(map.get("RPR_result"));
            visits[cnt].setSputum(map.get("TBSputum_result"));
            visits[cnt].setOtherLabName(0, map.get("other_testType"));
            visits[cnt].setOtherLabResult(0, map.get("other_test_result"));
            visits[cnt].setOtherLabName(1, map.get("other_testType2"));
            visits[cnt].setOtherLabResult(1, map.get("other_test_result2"));
            visits[cnt].setReferral(map.get("referred_to"));
            visits[cnt].setAtRiskPop(map.get("RiskPopu"));
            visits[cnt].setDisclosure(map.get("PwPDis"));
            visits[cnt].setPartnerTested(map.get("PwPPaT"));
            visits[cnt].setCondomsDisp(map.get("PwPCon"));
            visits[cnt].setStiScreen(map.get("PwPSTI"));
            tmpDate = null;
            if (map.get("next_visit_date") != null) {
                tmpDate = Date.valueOf(map.get("next_visit_date").substring(0, 10));
            }
            if (tmpDate != null) {
                visits[cnt].setNextAppt(new SimpleDateFormat("yyyyMMdd").format(tmpDate.getTime()));
            }
            visits[cnt].setClinicianInit(map.get("clinician_initial"));

            // More visit data fields - now from tblUnsatisfactorycotrimoxazole
            Map<Integer, String> codedVals = visitRepCodedValues(lookups, stmts, visId, "tblUnsatisfactorycotrimoxazole",
                    "UnsatCotriReaon", "UnsatCotriother",
                    "tlkadherenceunsatisfactory", "unsatisfactoryadherence",
                    MAX_POOR_ADHERENCE_REASONS, OTHER_POOR_ADHERENCE_CODE);
            int i = 0;
            while (codedVals.get(i) != null) {
                visits[cnt].setCtxPoorAdh(i, codedVals.get(i));
                i++;
            }

            // More visit data fields - now from tblUnsatisfactoryart
            codedVals = visitRepCodedValues(lookups, stmts, visId, "tblUnsatisfactoryart",
                    "UnsatARTReason", "UnsatARTOth",
                    "tlkadherenceunsatisfactory", "unsatisfactoryadherence",
                    MAX_POOR_ADHERENCE_REASONS, OTHER_POOR_ADHERENCE_CODE);
            i = 0;
            while (codedVals.get(i) != null) {
                visits[cnt].setPriorArvPoorAdh(i, codedVals.get(i));
                i++;
            }

            // More visit data fields - now from tblfpmethod
            codedVals = visitRepCodedValues(lookups, stmts, visId, "tblfpmethod",
                    "fpmethod", "fpother",
                    "tlkfpmethod", "fpmethod",
                    MAX_FAMILY_PLANNING_METHODS, OTHER_FAMILY_PLANNING_CODE);
            i = 0;
            while (codedVals.get(i) != null) {
                visits[cnt].setFamPlanMethod(i, codedVals.get(i));
                i++;
            }

            // More visit data fields - now from tblARTSideEffects
            codedVals = visitRepCodedValues(lookups, stmts, visId, "tblARTSideEffects",
                    "artsideeffects", "othersideeffects",
                    "tlkartsideeffects", "artsideeffects",
                    MAX_SIDE_EFFECTS, OTHER_SIDE_EFFECTS_CODE);
            i = 0;
            while (codedVals.get(i) != null) {
                visits[cnt].setSideEffect(i, codedVals.get(i));
                i++;
            }

            // Last of the visit data fields - from tblNewOI
            codedVals = visitRepCodedValues(lookups, stmts, visId, "tblNewOI",
                    "newoi", "newoiother",
                    "tlkoi_code", "oi_name",
                    MAX_NEW_OI, OTHER_OI_CODE);
            i = 0;
            while (codedVals.get(i) != null) {
                visits[cnt].setOiProblem(i, codedVals.get(i));
                i++;
            }

            rs2.close();
            cnt++;
        }
        rs.close();
    }

    private static Map<Integer, String> visitRepCodedValues(PreparedStatement lookups[], PreparedStatement stmts[], String visId,
            String tblName, String colName, String othName, String lookupTblName,
            String lookupColName, int max, int othCode) {
        Map<Integer, String> valueMap = new HashMap<Integer, String>();
        try {
            stmts[5].setString(1, tblName);
            stmts[5].setString(2, colName);
            stmts[5].setInt(3, Integer.parseInt(visId));
            ResultSet rs3 = stmts[5].executeQuery();
            int i = 0;
            while (rs3.next() && i < max) {
                int code = rs3.getInt("data");
                if (!rs3.wasNull()) {
                    stmts[6].setString(1, tblName);
                    stmts[6].setString(2, othName);
                    stmts[6].setString(3, visId + Integer.toString(code));
                    ResultSet rs4 = stmts[6].executeQuery();
                    Map<String, String> map2 = mapResultSet(rs4);
                    if (code == othCode) {
                        String tmp = null;
                        if (map2.get(othName) != null) {
                            tmp = map2.get(othName);
                        }
                        if (tmp != null) {
                            valueMap.put(i, "Other: " + tmp.toLowerCase());
                        }
                    } else {
                        lookups[0].setString(1, lookupTblName);
                        lookups[0].setString(2, lookupColName);
                        lookups[0].setInt(3, code);
                        ResultSet rs5 = lookups[0].executeQuery();
                        if (rs5.next()) {
                            valueMap.put(i, rs5.getString("data"));
                        }
                        rs5.close();
                    }
                }
                i++;
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return valueMap;
    }

    private static Map<String, String> mapResultSet(ResultSet rs) {
        Map<String, String> valueMap = new HashMap<String, String>();
        try {
            if (rs.next()) {
                do {
                    valueMap.put(rs.getString("name"), rs.getString("data"));
                } while (rs.next());
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
        return valueMap;
    }
}