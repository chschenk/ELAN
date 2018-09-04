package mpi.eudico.client.annotator.interlinear.edit.model;

/**
 * A class containing the full and short label for a row. 
 * In most cases that is the name of a tier.
 * 
 * @author Han Sloetjes
 */
public class IGTRowHeaderItem {
	private String labelFullText;
	private String labelText;	
	
	/**
	 * Constructor with the full header name as parameter.
	 * 
	 * @param fullText the full header text
	 */
	public IGTRowHeaderItem(String fullText) {
		super();
		labelFullText = fullText;
		labelText = fullText;
	}

	/**
	 * The header text sometimes has to be cut off because of lack of space.
	 * Or an abbreviation might be used
	 * 
	 * @return the actual displayed header text
	 */
	public String getHeaderText() {
		return labelText;
	}
	
	/**
	 * @return the full unmodified header text
	 */
	public String getFullHeaderText() {
		return labelFullText;
	}

	/**
	 * Sets the shortened or abbreviated header text
	 * 
	 * @param cutoffText the short header text
	 */
	public void setHeaderText(String cutoffText) {
		if (cutoffText != null) {
			labelText = cutoffText;
		}
	}
	
	/**
	 * Sets the full header text.
	 * 
	 * @param fullText the full header text
	 */
	public void setFullHeaderText(String fullText) {
		if (fullText != null) {
			labelFullText = fullText;
		}
	}
	
	/**
	 * toString() is useful for debugging.
	 */
	@Override
	public String toString() {
		return "IGTRowHeaderItem: " + String.valueOf(labelFullText) + " (" + String.valueOf(labelText) + ")";
	}

}
