package mpi.eudico.server.corpora.clomimpl.theme;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * Stores properties for customization of export to Theme format.
 *  
 * @author Han Sloetjes
 */
public class ThemeEncoderInfo implements EncoderInfo {

	private boolean tierNameAsActor = false;
	private boolean useCVforVVT = true;
	private String encoding;
	
	public ThemeEncoderInfo() {
		super();
	}

	/**
	 * If true each tier name will be exported as a Theme Actor, otherwise 
	 * the participant attribute will be used as Actor and the tier name part of the event value. 
	 * 
	 * @return the flag that determines whether the tier name is Actor or the participant attribute
	 */
	public boolean isTierNameAsActor() {
		return tierNameAsActor;
	}

	public void setTierNameAsActor(boolean tierNameAsActor) {
		this.tierNameAsActor = tierNameAsActor;
	}

	/**
	 * 
	 * @return if true the controlled vocabulary is used for export to the vvt.vvt file, 
	 * otherwise all values of a tier are added to the vvt
	 */
	public boolean isUseCVforVVT() {
		return useCVforVVT;
	}

	public void setUseCVforVVT(boolean useCVforVVT) {
		this.useCVforVVT = useCVforVVT;
	}

	/**
	 * @return the file encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
