package ke.go.moh.oec.pisinterfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import ke.go.moh.oec.pisinterfaces.util.PatientIdType;
/**
 * 
 * @author Fiston
 * 
 * this class represent identification that has to be sent from interface to Mirth channel. 
 * It'll be marshaled to XML before being sent to URL that Mirth channel listen to.
 *
 */
@XmlRootElement
public class PatientIdentification {
	private String identification;
	private String identificationType;
	private String requestSource;

	/**
	 * @return the identification
	 */
	public String getIdentification() {
		return identification;
	}

	/**
	 * @param identification
	 *            the identification to set
	 */
	public void setIdentification(String identification) {
		this.identification = identification;
	}

	/**
	 * @return the identificationType
	 */
	public String getIdentificationType() {
		return identificationType;
	}

	/**
	 * @param identificationType
	 *            the identificationType to set
	 */
	public void setIdentificationType(String identificationType) {
		if (identificationType == "1") {
			this.identificationType = PatientIdType.ClinicalId.toString();
		} else if (identificationType == "2") {
			this.identificationType = PatientIdType.ClinicalId.toString();

		}
	}

	/**
	 * @param requestSource
	 *            the requestSource to set
	 */
	public void setRequestSource(String requestSource) {
		this.requestSource = requestSource;
	}

	/**
	 * @return the requestSource
	 */
	public String getRequestSource() {
		return requestSource;
	}

}
