package ke.go.moh.oec.pisinterfaces.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import ke.go.moh.oec.pisinterfaces.beans.PatientIdentification;

public class JavaToXML {
	public static String objectToXml(PatientIdentification patientIdentification)
                throws SiteException {
		try {
			JAXBContext context = JAXBContext
					.newInstance(PatientIdentification.class);
			StringWriter sw = new StringWriter();
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			m.marshal(patientIdentification, sw);
			return sw.toString();
		} catch (Exception e) {
			throw new SiteException("Unable to package XML for CDA query", e);
		}
	}

	public static InputStream getPropertiesFile(String fileName) throws SiteException {
                String propertiesPath = null;
		try {
			int check = 0;
			while (check == 0) {
				check = 1;
				Properties pro = new Properties();
				StringBuilder path = (new StringBuilder(String.valueOf(System
						.getProperty("user.home"))))
						.append(System.getProperty("file.separator"))
						.append("OecConfig")
						.append(System.getProperty("file.separator"));
				propertiesPath = path.toString() + fileName;
				File f = new File(propertiesPath);
				if (!f.exists()) {
					check = 0;
                                        Logger.getLogger(JavaToXML.class.getName()).log(Level.WARNING,
                                                "Cofig file not found, default file created, check "
						+ f.getPath()
						+ " to edit according to your configurations");

					pro.setProperty("urlMirth", "http://localhost:8090");
					pro.store(new FileOutputStream(propertiesPath), null);

				} else {

					return new FileInputStream(f);

				}
			}
		} catch (IOException e) {
			throw new SiteException("Unable to load properties file: " +
                                propertiesPath, e);
		}
		return null;

	}
}