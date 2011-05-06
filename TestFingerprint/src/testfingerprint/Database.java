/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testfingerprint;

import com.griaule.grfingerjava.Template;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jim Grace
 */
public class Database {

	public static List<Template> loadTemplates(int limit) {
		List<Template> returnTemplates = new ArrayList<Template>();
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}

		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/fingerprints", "root", "root");
		} catch (SQLException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		PreparedStatement sql = null;
		try {
			sql = conn.prepareStatement("SELECT Template, TemplateSize from nodup_fingerprints LIMIT " + limit);
		} catch (SQLException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		ResultSet rs = null;
		try {
			rs = sql.executeQuery();
		} catch (SQLException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		try {
			while (rs.next()) {
				byte buffer[] = rs.getBytes("Template"); // From database
				int templateLength = rs.getInt("TemplateSize");
				byte templateBytes[] = Arrays.copyOf(buffer, templateLength);
				Template template = new Template(templateBytes);
				returnTemplates.add(template);
			}
		} catch (SQLException ex) {
			Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
		}
		return returnTemplates;
	}
}
