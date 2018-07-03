package mpi.eudico.client.annotator;
/**
 * Interface for classes that allow to set and get the base
 * font size.
 * 
 * @author Han Sloetjes
 */
public interface FontSizer {

	/**
	 * Sets the base font size.
	 * 
	 * @param fontSize the new base font size
	 */
	public void setFontSize(int fontSize);
	
	/**
	 * Returns the base font size.
	 * 
	 * @return the base font size
	 */
	public int getFontSize(); 
}
