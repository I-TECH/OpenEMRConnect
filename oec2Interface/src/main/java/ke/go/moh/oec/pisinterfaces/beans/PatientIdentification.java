package ke.go.moh.oec.pisinterfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import ke.go.moh.oec.pisinterfaces.util.PatientIdType;

/**
 * 
 * @author Fiston
 * 
 *         this class represent identification that has to be sent from
 *         interface to Mirth channel. It'll be marshaled to XML before being
 *         sent to URL that Mirth channel listen to.
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

		this.identificationType = identificationType;
		if (this.identificationType.equalsIgnoreCase("1")) {
			PatientIdType idType = PatientIdType.ClinicalId;
			this.identificationType = idType.name();
		} else if (this.identificationType.equalsIgnoreCase("2")) {
			PatientIdType idType = PatientIdType.HDSS;
			this.identificationType = idType.name();

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

	public String toString() {
		return this.identification + " " + this.identificationType + " "
				+ this.requestSource;
	}

}
