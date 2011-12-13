package ke.go.moh.oec.pisinterfaces.controller;

import java.io.BufferedReader;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import ke.go.moh.oec.pisinterfaces.beans.PatientIdentification;
import ke.go.moh.oec.pisinterfaces.util.JavaToXML;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * 
 * @author Fiston
 * 
 *         The controller that controls all requests used by this interface.
 * 
 */
@Controller
@RequestMapping("/*.htm")
@SessionAttributes("patientId")
public class CdaInterfaceController {
	String stringUrl = null;
	@Context
	ServletContext context;

	public CdaInterfaceController() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * all "get" requests to "/sentPatientId.htm" are caught here and the page
	 * to display is returned.
	 * 
	 * @param model
	 * @return String representing the page too display
	 */

	@RequestMapping(value = "/sentPatientId.htm", method = RequestMethod.GET)
	public String showUserForm(ModelMap model) {
		PatientIdentification patientId = new PatientIdentification();
		model.addAttribute(patientId);

		return "sendPatientInfo";
	}

	/**
	 * Gets patient Identification and transform this to XML. It pass the XML to
	 * URL.
	 * 
	 * @param patientId
	 * @return
	 */

	@RequestMapping(value = "/sentPatientId.htm", method = RequestMethod.POST)
	public String onSubmit(
			@ModelAttribute("patientIdentification") PatientIdentification patientId) {

//		BufferedReader rd = null;
		OutputStreamWriter wr = null;
		Properties props = new Properties();

		try {
			JavaToXML.getPropertiesFile("config.properties");
			InputStream in = JavaToXML.getPropertiesFile("config.properties");
			props.load(in);
			in.close();

			String stringUrl = props.getProperty("urlMirth");
			String s = JavaToXML.objectToXml(patientId);
			System.out.println("Connecting to URL"+stringUrl);
			URL url = new URL(stringUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(s);
			wr.flush();
//			System.out.println(s);
//			// Get the response
//			rd = new BufferedReader(
//					new InputStreamReader(conn.getInputStream()));
//			String line;
//			String resp = "";
//			while ((line = rd.readLine()) != null) {
//				System.out.println(line);
//				resp += line;
//			}
			wr.close();
//			rd.close();
			return "redirect:sendSuccess.htm";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "sendPatientInfo";
	}

	@RequestMapping("/sendSuccess.htm")
	public String sendSuccess() {

		return "sendSuccess";

	}

	/**
	 * Need to be refined after the channel is created
	 * 
	 * @param model
	 * @param params
	 * @return
	 */

	@RequestMapping(value = "/receiveCda.htm", method = RequestMethod.POST)
	public String receiveCda(ModelMap model, @RequestParam String[] params) {
		model.addAttribute("params", params);
		return "viewCda";

	}
	/**
	 * Need to be refined after the channel is created
	 * 
	 * @param model
	 * @param params
	 * @return
	 */

	@RequestMapping(value = "/receiveCda.htm", method = RequestMethod.GET)
	public String receiveCdaGet(ModelMap model, @RequestParam String[] params) {
		
		return receiveCda(model,params);

	}

}
