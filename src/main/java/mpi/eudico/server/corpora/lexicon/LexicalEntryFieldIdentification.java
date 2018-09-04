package mpi.eudico.server.corpora.lexicon;

/**
 * Class to hold the identification of a Lexical Entry Field: ID and name (and description)
 * @author Micha Hulsbosch
 *
 */
public class LexicalEntryFieldIdentification implements Comparable<LexicalEntryFieldIdentification> {
	private String id;
	private String name;
	private String description;
	
	public LexicalEntryFieldIdentification(String id, String name) {
		this.id = id;
		this.name = name;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public int compareTo(LexicalEntryFieldIdentification o) {
		return name.compareToIgnoreCase(o.getName());
	}
}
