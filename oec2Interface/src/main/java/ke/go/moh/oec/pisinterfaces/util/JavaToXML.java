package ke.go.moh.oec.pisinterfaces.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import ke.go.moh.oec.pisinterfaces.beans.PatientIdentification;

public class JavaToXML {
	public static String objectToXml(PatientIdentification patientIdentification) {
		try {
			JAXBContext context = JAXBContext
					.newInstance(PatientIdentification.class);
			StringWriter sw = new StringWriter();
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			m.marshal(patientIdentification, sw);
			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static InputStream getPropertiesFile(String fileName) {
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
				String propertiesPath = path.toString() + fileName;
				File f = new File(propertiesPath);
				if (!f.exists()) {
					check = 0;
					System.out
							.println("File not found, default file created, check "
									+ f.getPath()
									+ " to edit according to your configurations");
					FileInputStream in = new FileInputStream(f);
					pro.load(in);
					pro.setProperty("urlMirth", "http://localhost:8090");
					pro.store(new FileOutputStream(propertiesPath), null);

				} else {

					System.out.println(f.getAbsolutePath());
					return new FileInputStream(f);

				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;

	}
}