package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * Record of a Lexicon Query Bundle element or Lexicon Link element found by the 
 * EAF parser
 * @author Micha Hulsbosch
 *
 */
public class LexiconServiceRecord {
	private String name;
	private String lexiconId;
	private String lexiconName;
	private String type;
	private String datcatId;
	private String datcatName;
	private String url;
	
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
	/**
	 * @return the lexiconId
	 */
	public String getLexiconId() {
		return lexiconId;
	}
	/**
	 * @param lexiconId the lexiconId to set
	 */
	public void setLexiconId(String lexiconId) {
		this.lexiconId = lexiconId;
	}
	/**
	 * @return the lexiconName
	 */
	public String getLexiconName() {
		return lexiconName;
	}
	/**
	 * @param lexiconName the lexiconName to set
	 */
	public void setLexiconName(String lexiconName) {
		this.lexiconName = lexiconName;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the datcatId
	 */
	public String getDatcatId() {
		return datcatId;
	}
	/**
	 * @param datcatId the datcatId to set
	 */
	public void setDatcatId(String datcatId) {
		this.datcatId = datcatId;
	}
	/**
	 * @return the datcatName
	 */
	public String getDatcatName() {
		return datcatName;
	}
	/**
	 * @param datcatName the datcatName to set
	 */
	public void setDatcatName(String datcatName) {
		this.datcatName = datcatName;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
}
