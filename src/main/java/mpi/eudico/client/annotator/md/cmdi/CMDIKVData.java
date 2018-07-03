package mpi.eudico.client.annotator.md.cmdi;

import mpi.eudico.client.annotator.md.imdi.MDKVData;

/**
 * Class that extends the imdi metadata key-value pair class by adding a data category
 * field and a map of localized keys. 
 * 
 * @author Han Sloetjes
 */
public class CMDIKVData extends MDKVData {
	/** the data category identifier for the key */
	String keyDatCatID;
	/** the data category identifier for the value */
	String valueDatCatID;
	String defaultKey;// default in the sense of not localized
	String defaultValue;// default in the sense of not localized
	
	/**
	 * Constructor.
	 * 
	 * @param key the default key (english?)
	 * @param value the value of the metadata field
	 * @param keyDatCatID the ID of the data category of the key, can be null
	 */
	public CMDIKVData(String key, String value, String keyDatCatID) {
		super(key, value);
		this.keyDatCatID = keyDatCatID;
		defaultKey = key;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param key the default key (english?)
	 * @param value the value of the metadata field
	 * @param keyDatCatID the ID of the data category of the key, can be null
	 * @param valueDatCatID the ID of the data category of the value, can be null
	 */
	public CMDIKVData(String key, String value, String keyDatCatID, String valueDatCatID) {
		super(key, value);
		this.keyDatCatID = keyDatCatID;
		defaultKey = key;
		this.valueDatCatID = valueDatCatID;
		defaultValue = value;
	}
	
	/**
	 * 
	 * @param datCatID the data category ID of the key part
	 */
	public void setKeyDatCatID(String keyDatCatID) {
		this.keyDatCatID = keyDatCatID;
	}
	
	/**
	 * 
	 * @return the data category ID of the key field, can be null
	 */
	public String getKeyDatCatID() {
		return keyDatCatID;
	}

	/**
	 * 
	 * @param datCatID the data category ID of the value part
	 */
	public void setValueDatCatID(String valueDatCatID) {
		this.valueDatCatID = valueDatCatID;
	}
	
	/**
	 * 
	 * @return the data category ID of the value field, can be null
	 */
	public String getValueDatCatID() {
		return valueDatCatID;
	}
	
	/**
	 * 
	 * @param locKey the localized version of the key. 
	 * Administration of languages is done elsewhere, this object is unaware of what language the key is. 
	 */
	public void setLocalizedKey(String locKey) {
		if (locKey != null) {
			key = locKey;
		} else {
			key = defaultKey;
		}
	}
	
	/**
	 * 
	 * @param locValue the localized version of the value. 
	 * Administration of languages is done elsewhere, this object is unaware of what language the key is. 
	 */
	public void setLocalizedValue(String locValue) {
		if (locValue != null) {
			value = locValue;
		} else {
			value = defaultValue;
		}
	}
	
}
