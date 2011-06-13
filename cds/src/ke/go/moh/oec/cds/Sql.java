/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.cds;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;
/**
 *
 * @author DevWakhutu
 */
public class Sql {
    
    public static Connection connect(){
     Connection conn = null;
     
     try {
         String url = Mediator.getProperty("CDS.url");
         String username = Mediator.getProperty("CDS.username");
         String  password = Mediator.getProperty("CDS.password");
         conn = DriverManager.getConnection(url, username,password);
     }
     catch(Exception ex){
         Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Can''t connect to the database -- Please check the database and try again.", ex);    
         
         System.exit(1);
     }
     return conn;
     
    }
    
     /**
     * Quotes a string for use in a SQL statement.
     * Doubles single quotes (') and backslashes (\).
     * If the string is null, returns "null".
     * If the string is not null, returns the string with single quotes (') around it.
     * 
     * @param s string to quote.
     * @return quoted string.
     */
        public static String quote(String s) {
        if (s == null) {
            s = "null";
        } else {
            s = s.replace("'", "''");
            s = s.replace("\\", "\\\\");
            s = "'" + s + "'";
        }
        return s;
    }

    
      public static boolean execute(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Execute:\n{0}", sql);
        boolean returnValue = false;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            returnValue = stmt.execute();
            int updateCount = stmt.getUpdateCount();
            Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "{0} rows updated.", updateCount);
        } catch (SQLException ex) {
            Logger.getLogger(Cds.class.getName()).log(Level.SEVERE,
                    "Error executing SQL statement " + sql, ex);
            System.exit(1);
        }
        return returnValue;
    }
      
      public static ResultSet query(Connection conn, String sql) {
        Mediator.getLogger(Sql.class.getName()).log(Level.FINE, "SQL Query:\n{0}", sql);
        ResultSet rs = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Sql.class.getName()).log(Level.SEVERE,
                    "Error executing SQL Query " + sql, ex);
            System.exit(1);
        }
        return rs;
    }
    
}
