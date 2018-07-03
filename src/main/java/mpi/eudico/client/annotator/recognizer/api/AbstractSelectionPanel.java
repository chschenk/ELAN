package mpi.eudico.client.annotator.recognizer.api;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JPanel;

/**
 * A panel providing 2 or 3 options for passing selections/segments (or annotations)
 * to a recognizer: 
 * 1. manually created selections
 * 2. segments extracted from a tier; the list of tiers is provided by the host
 * 3. from a file; the segments are extracted from a file that has been selected in a file chooser
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractSelectionPanel extends JPanel {

	public static final int AUDIO_MODE = 0;
	public static final int VIDEO_MODE = 1;
	public static final String SELECTIONS = "Selections";
	public static final String TIER = "Tier";
	public static final String FILE_NAME = "FileName";
	public static final String TIER_NAME = "TierName";

	/**
	 * Enables or disables the file selection option
	 * 
	 * @param enable
	 */
	public abstract void enableFileSelection(boolean enable);

	/**
	 * Gets the current mode of this panel
	 * 
	 * @return avMode - AbstractSelectionPanel.AUDIO_MODE / 
	 * 					AbstractSelectionPanel.VIDEO_MODE
	 */
	public abstract int getMode();

	/**
	 * Returns the path to the file or folder or selected media file.
	 * 
	 * @see mpi.eudico.client.annotator.recognizer.gui.AbstractParamPanel#getParamValue()
	 * @return a Map<String, Object> containing parameter name - value pairs
	 */
	public abstract Map<String, Object> getParamValue();

	/**
	 * Returns the current selection value. 
	 * Depending on the selection mode (manual selections, tier or file path)
	 * it returns a List of selections (manual or from a tier) or a file name.
	 * 
	 * @return List<RSelection> or String or null
	 */
	public abstract Object getSelectionValue();

	/**
	 * Converts the given map into a new map in the format which 
	 * can be stored in the ELAN preferences
	 * 
	 * @param map map to be converted to a storable map
	 * @return Map - map which can be stored by ELAN preferences
	 */
	public abstract Map<String, Object> getStorableParamPreferencesMap(
			Map<String, Object> map);

	/**
	 * Sets the value for the file parameter 
	 * 
	 * @param string value of the file path
	 */
	public abstract void setParamValue(String value);

	/**
	 * Sets the value for the current parameters as retrieved from stored preferences. 
	 * 
	 * @param map map which contains the parameter name and its value. The parameter keys 
	 * are SELECTIONS, TIER_NAME or FILE_NAME
	 */
	public abstract void setParamValue(Map<String, ? extends Object> map);
	
	/**
	 * Can be called by the Recognizer when the locale changes in the user interface
	 * 
	 * @param locale the new Locale
	 */
	public abstract void updateLocale(Locale locale);
	
	/**
	 * Can be implemented optionally to support locale changes in the user interface.
	 * Gives access to the strings and translations available in ELAN.
	 * 
	 * @param bundle the resource bundle for the currently selected language
	 */
	public abstract void updateLocaleBundle(ResourceBundle bundle);

}