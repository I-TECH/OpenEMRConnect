/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.adt;

import java.sql.Connection;
import java.sql.ResultSet;
import au.com.bytecode.opencsv.CSVWriter;
//import com.sun.rowset.internal.Row;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileWriter;
import java.io.*;
import java.util.Arrays;
import ke.go.moh.oec.adt.HeaderData;

/**
 *
 * @author Administrator
 */
public class AdtDataExtractor {

    //final static String ODBC_URL = "jdbc:odbc:adt_companion";
    final static String OUTPUT_FILENAME = "out.csv";
    final static int MAX_VISIT_CNT = 12;
    final static int FILLER_CNT = 0;

    public static void main(String[] args) throws SQLException {
        HeaderData header = new HeaderData();
        DispensedData dispensedData[] = new DispensedData[MAX_VISIT_CNT];
        for (int i = 0; i < MAX_VISIT_CNT; i++) {
            dispensedData[i] = new DispensedData();
        }
        Connection con = Sql.connect();

        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(OUTPUT_FILENAME), "UTF-8");
            String sArtid = "select count(artid) as total from tblARTpatientMasterInformation";
            ResultSet rs = Sql.query(con, sArtid);
            int recCnt = 0;
            if (rs.next()) {
                recCnt = rs.getInt("total");
            }
            System.out.println("Found " + recCnt + " patients");

            /* Read patient master table to get the Clinic ID for every patient*/

//            ResultSet rsPtMaster = stmt.executeQuery("select distinct ArtID from tblARTpatientMasterInformation");
            String sPatienttMaster = "select distinct ArtID from tblARTpatientMasterInformation";
            ResultSet rsPtMaster = Sql.query(con, sPatienttMaster);
            /*Loop through the patient master*/
            int cnt = 0;
            int rcnt = 0;
            while (rsPtMaster.next()) {//&& rcnt < 150
                String idx = rsPtMaster.getString("ARTID");
                //System.out.println(" artid " + idx);

                ExtractHeaderData(idx, header);
                ExtractDispenseData(idx, dispensedData);

                rcnt = rcnt + 1;
                System.out.println(" count " + rcnt);

                String finalCsv = "";
                finalCsv += header.printHeaderDelim("\t");
                finalCsv += "\t";
                // Fill in currently unused fields
                for (int i = 0; i < FILLER_CNT; i++) {
                    finalCsv += "\t";
                }
                for (int i = 0; i < dispensedData.length; i++) {
                    finalCsv += dispensedData[i].printHeaderDelim("\t");
                    if (i < dispensedData.length - 1) {
                        finalCsv += "\t";
                    }
                }
                out.write(finalCsv + "\n");
                if (++cnt % 100 == 0) {
                    System.out.println("(" + cnt + ")");

                }

            }

            rsPtMaster.close();
            con.close();
            out.close();


        } catch (Exception e) {
            Logger.getLogger(AdtDataExtractor.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    private static void ExtractHeaderData(String pid, HeaderData header) throws SQLException, ClassNotFoundException {
        Connection conn = Sql.connect();

        String sPatientData = "SELECT * FROM tblARTpatientMasterInformation "
                + "WHERE tblARTpatientMasterInformation.ARTid = '" + pid + "'";
        ResultSet rsPtData = Sql.query(conn, sPatientData);

        rsPtData.next();
        header.setArtID(pid);
        header.setFirstname(rsPtData.getString("Firstname"));
        header.setSurname(rsPtData.getString("Surname"));
        header.setGender(rsPtData.getString("sex"));
        rsPtData.close();

//        System.out.println(header.getFirstname());
    }

    private static void ExtractDispenseData(String pid, DispensedData[] dispensedData) throws SQLException, ClassNotFoundException {
        Connection con = Sql.connect();

        String sPtTransaction = "select ARTId, "
                + "dateofVisit,drugname,duration,dose FROM tblARTPatientTransactions "
                + "WHERE ARTId = '" + pid + "'";

        /*For each patient, read the transaction table to get the prescription information in all the visits.*/
        /**Loop over visits (only the most recent number as defined by MAX_VISIT_CNT variable) and pull required data**/
        ResultSet rsPtTransaction = Sql.query(con, sPtTransaction);
        int cnt = 0;
        while (rsPtTransaction.next() && cnt < MAX_VISIT_CNT) {
            dispensedData[cnt].setDateofVisit(rsPtTransaction.getString("dateofVisit"));
            dispensedData[cnt].setDose(rsPtTransaction.getString("dose"));
            dispensedData[cnt].setDrugname(rsPtTransaction.getString("drugname"));
            dispensedData[cnt].setDuration(rsPtTransaction.getString("duration"));

            cnt++;
        }
        rsPtTransaction.close();

    }
}
