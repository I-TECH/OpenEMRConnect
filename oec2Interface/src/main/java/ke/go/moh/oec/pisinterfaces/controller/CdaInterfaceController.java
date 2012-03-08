package ke.go.moh.oec.pisinterfaces.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import ke.go.moh.oec.pisinterfaces.beans.PatientIdentification;
import ke.go.moh.oec.pisinterfaces.util.JavaToXML;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	protected String stringUrl = null;
        private StringBuffer result = new StringBuffer();
	private Log log=LogFactory.getLog(CdaInterfaceController.class);

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
			@ModelAttribute("patientIdentification") PatientIdentification patientId,
                        ModelMap model) {


		OutputStreamWriter wr = null;
		Properties props = new Properties();

		try {
			InputStream in = JavaToXML.getPropertiesFile("config.properties");
			props.load(in);
			in.close();

			String stringUrl = props.getProperty("urlMirth");
			System.out.println(patientId.toString());
			String s = JavaToXML.objectToXml(patientId);
			log.info("Message to send : \n"+s);
			System.out.println("Connecting to URL"+stringUrl);
			URL url = new URL(stringUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("query=" + s);
			wr.flush();
			wr.close();

                        BufferedReader response = new BufferedReader(new InputStreamReader(
                                conn.getInputStream()));
                        String respString;
                        result.setLength(0);
                        while ((respString = response.readLine()) != null){
                            result.append(respString);
                        }
                        if (result.length() == 0) {
                            String[] errors = new String[1];
                            errors[0] = "No Match Found";
                            model.addAttribute("errors", errors);
                            return "sendPatientInfo";
                        }
                        return "redirect:receiveCda.htm";
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

	@RequestMapping(value = "/receiveCda.htm", method = RequestMethod.GET)
	public String receiveCda(ModelMap model) {
                this.addStyleSheet();
                String[] params = new String[1];
                params[0] = this.result.toString();
                model.addAttribute("params", params);
		return "viewCda";
	}

        /**
         * Add the XSLT style sheet element to the CDA, which may be avaliable in this.result
         * 
         */
        protected void addStyleSheet() {
           // This points to the CDA XSL style sheet served from the root resources dir 
           String stylelink = "<?xml-stylesheet type='text/xsl' href='xsl/WebViewLayout_CDA.xsl'?>";
           if (this.result.length() < 1) {
               // No CDA delivered, we're done.
               return;
           }
           // Insert the style sheet element immediately following the xml header
           int endOfHeader = this.result.indexOf(">");
           int i = this.result.indexOf("<?xml-stylesheet", endOfHeader);
           if (i < 0) {
               // No style sheet element, insert ours
               this.result.insert(endOfHeader, stylelink);
           } else {
               // Style sheet present - replace w/ our to be safe
               int endOfStyle = this.result.indexOf(">", i);
               this.result.replace(i, endOfStyle+1, stylelink);
           }
        }
}
