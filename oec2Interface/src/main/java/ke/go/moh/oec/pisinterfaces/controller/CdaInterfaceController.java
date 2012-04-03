package ke.go.moh.oec.pisinterfaces.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.pisinterfaces.beans.PatientIdentification;
import ke.go.moh.oec.pisinterfaces.util.CdaQueryResult;
import ke.go.moh.oec.pisinterfaces.util.JavaToXML;
import ke.go.moh.oec.pisinterfaces.util.SiteException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Fiston
 *
 * The controller that controls all requests used by this interface.
 *
 * In order to handle ReSTful URLs (such as "/viewCda/{cdaID}"), it is necessary
 * to serve from a non-root context. See the web.xml file for path info.
 *
 */
@Controller
@SessionAttributes({"patientId", "cdaList"})
public class CdaInterfaceController {

    protected String stringUrl = null;
    //private StringBuffer result = new StringBuffer();
    private Log log = LogFactory.getLog(CdaInterfaceController.class);

    /**
     * Handles all "GET" requests to "/sentPatientId" are caught here and the
     * page to display is returned. The resulting form collects search criteria.
     *
     * @param model
     * @return String naming the .jsp page to display
     */
    @RequestMapping(value = "/sentPatientId", method = RequestMethod.GET)
    public String showUserForm(ModelMap model) {
        PatientIdentification patientId = new PatientIdentification();
        model.addAttribute(patientId);

        return "sendPatientInfo";
    }

    /**
     * Handles all "POST" requests to "/sentPatientId". Uses the patient
     * criteria from the form to hit the external CDA query service, redirecting
     * appropriately depending on found results.
     *
     * @param patientId
     * @return String naming the .jsp page to display
     */
    @RequestMapping(value = "/sentPatientId", method = RequestMethod.POST)
    public String onSubmit(
            @ModelAttribute("patientIdentification") PatientIdentification patientId,
            ModelMap model) throws SiteException {
        Map<String, String> resultList = executeQuery(patientId);
        if (resultList.size() == 0) {
            String[] errors = new String[1];
            errors[0] = "No Match Found";
            model.addAttribute("errors", errors);
            return "sendPatientInfo";
        } else {
            // Store matching CDA docs in session
            model.addAttribute("cdaList", resultList);
        }
        return "redirect:sendSuccess";
    }

    /**
     * Redirection target after receiving query results. Displaying the
     * appropriate meta-data for each matching CDA, with links to view.
     *
     * @param cdaList session attribute, containing the search results
     * @param model
     * @return String naming the .jsp page to display
     */
    @RequestMapping(value = "/sendSuccess", method = RequestMethod.GET)
    public String sendSuccess(
            @ModelAttribute("cdaList") Map<String, String> cdaList,
            ModelMap model) {
        if (cdaList == null || cdaList.size() < 1) {
            // Shouldn't be here without a result list - possible in event
            // of session timeout or bookmarked URL.
            String[] errors = new String[1];
            errors[0] = "No Match Found";
            model.addAttribute("errors", errors);
        }
        return "sendSuccess";
    }

    /**
     * ReSTful view controller to display a single CDA. Looks first to the
     * session for the requested cdaID, making a round trip to the external
     * query service only when necessary.
     *
     * @param cdaList session attribute, containing the search results
     * @param model
     * @return String naming the .jsp page to display
     */
    @RequestMapping(value = "/viewCda/{cdaID}", method = RequestMethod.GET)
    public String viewCda(
            @PathVariable String cdaID,
            @ModelAttribute("cdaList") Map<String, String> cdaList,
            ModelMap model) throws SiteException {
        // Check the session for the requested cda
        String cda = null;
        if (cdaList != null && cdaList.containsKey(cdaID)) {
            cda = cdaList.get(cdaID);
        } else {
            // Need to round trip to query for requested cda
            PatientIdentification pid = new PatientIdentification();
            pid.setCdaID(cdaID);
            Map<String, String> resultList = executeQuery(pid);
            //model.addAttribute("cdaList", resultList);
            cda = resultList.get(cdaID);
            if (cda == null) {
                // No match found, redirect to search w/ error
                String[] errors = new String[1];
                errors[0] = "No Match Found";
                model.addAttribute("errors", errors);
                return "viewCda";
            }
        }
        cda = this.addStyleSheet(new StringBuffer(cda));
        String[] params = new String[1];
        params[0] = cda;

        model.addAttribute(
                "params", params);

        return "viewCda";
    }

    /**
     * Create the "cdaList" session bound attribute if necessary. Called by the
     * spring framework if no such named attribute can be found in the Model.
     *
     * @return the empty (but valid) Map<String, String>
     */
    @ModelAttribute("cdaList")
    public Map<String, String> createCdaList() {
        return new HashMap<String, String>();
    }

    /**
     * Create the "patientIdentification" session bound attribute if necessary.
     * Called by the spring framework if no such named attribute can be found in
     * the Model.
     *
     * @return a new PatientIdentification
     */
    @ModelAttribute("patientIdentification")
    public PatientIdentification createPatientId() {
        return new PatientIdentification();
    }

    /**
     * Add the XSLT style sheet element to the CDA
     *
     * @param cda The CDA, XML document to inject with the configured .xsl
     * @return String representation of the CDA, including the stylesheet.
     */
    protected String addStyleSheet(StringBuffer cda) throws SiteException {
        // This points to the CDA XSL style sheet served from the root resources dir
        String stylelink = "<?xml-stylesheet type='text/xsl' href='xsl/WebViewLayout_CDA.xsl'?>";
        if (cda.length() < 1 || cda.indexOf(">") < 0) {
            // CDA doesn't look like XML, raise
            throw new SiteException("Invalid document", null);
        }
        // Insert the style sheet element immediately following the xml header
        int endOfHeader = cda.indexOf(">");
        int i = cda.indexOf("<?xml-stylesheet", endOfHeader);
        if (i < 0) {
            // No style sheet element, insert ours
            cda.insert(endOfHeader + 1, stylelink);
        } else {
            // Style sheet present - replace w/ our to be safe
            int endOfStyle = cda.indexOf(">", i);
            cda.replace(i, endOfStyle + 1, stylelink);
        }
        return cda.toString();
    }

    /**
     * Execute a query for one or more CDA documents and meta data.
     *
     * Make an HTTP request to external service, i.e. the CDA query mirth
     * channel.
     *
     * @param patientId with attributes set to query.
     * @return map of <cdaId: cda xml document> for any matching CDAs found.
     */
    private Map<String, String> executeQuery(PatientIdentification patientId) 
            throws SiteException {
        Properties props = new Properties();

        try {
            InputStream in = JavaToXML.getPropertiesFile("config.properties");
            props.load(in);
            in.close();
        } catch (IOException ex) {
            throw new SiteException("Unable to access configuration file", ex);
        }

        String stringUrl = props.getProperty("urlMirth");
        String s = JavaToXML.objectToXml(patientId);
        log.info("Message to send : \n" + s);
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException ex) {
            throw new SiteException("Mailformed URL from property `urlMirth`", ex);
        }
        URLConnection conn;
        try {
            conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write("query=" + s);
            wr.flush();
            wr.close();
        } catch (IOException ex) {
            throw new SiteException("External CDA Query client unavailable", ex);
        }

        CdaQueryResult cdaQueryResult = new CdaQueryResult();
        try {
            return cdaQueryResult.parseDocument(conn.getInputStream());
        } catch (IOException ex) {
            throw new SiteException("Unable to parse CdaQuery result", ex);
        }
    }
}
