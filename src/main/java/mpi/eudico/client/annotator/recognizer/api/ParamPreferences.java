package mpi.eudico.client.annotator.recognizer.api;

import java.util.Map;

/**
 * An interface that defines methods for handling parameter value preferences. 
 *  
 * @author Han Sloetjes
 */
public interface ParamPreferences {

	/**
	 * Passes the stored parameter values to the implementer.
	 * 
	 * @param storedPrefs the stored parameters, as key-value pairs
	 */
	public void setParamPreferences(Map<String, Object> storedPrefs);
	
	/**
	 * Returns the current parameter settings as key-value pairs. 
	 * May return null (in which case no preferences will be stored).
	 * 
	 * @return the current parameter settings as key-value pairs
	 */
	public Map<String, Object> getParamPreferences();
}
