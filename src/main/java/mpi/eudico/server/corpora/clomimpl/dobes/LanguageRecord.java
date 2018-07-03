package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * Stores information needed to construct a ControlledVocabulary object
 * 
 * @see mpi.util.ControlledVocabulary
 * 
 * @author Olaf Seibert
 * @version march 2014
 */
public class LanguageRecord {
	private String id;
	private String def;
	private String label;                  
	
	LanguageRecord(String id, String def, String label) {
		this.id = id;
		this.def = def;
		this.label = label != null ? label : "";
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the def
	 */
	public String getDef() {
		return def;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}	
}
