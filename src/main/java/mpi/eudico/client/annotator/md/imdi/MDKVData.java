package mpi.eudico.client.annotator.md.imdi;

/**
 * A class for storing metadata key-value pairs for use in a data model.
 * If the value is wrapped over multiple lines, the lines can be stored 
 * in a list of strings.
 * 
 * @author Han Sloetjes
 */
public class MDKVData {
	public String key;
	public String value;
	
	/**
	 * Constructor.
	 */
	public MDKVData() {
		super();
	}

	/**
	 * @param key the key
	 * @param value the value
	 */
	public MDKVData(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
}
