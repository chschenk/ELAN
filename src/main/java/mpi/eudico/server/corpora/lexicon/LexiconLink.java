package mpi.eudico.server.corpora.lexicon;



/**
 * A Lexicon Link contains a Lexicon Service Client and a Lexicon Identification
 * If a Lexicon Service Client does not have a loaded extension, Lexicon Link contains
 * data to store the necessary information when the transcription is saved. 
 * @author Micha Hulsbosch
 *
 */
public class LexiconLink {
	private String name;
	private String lexSrvcClntType;
	private LexiconServiceClient srvcClient;
	private LexiconIdentification lexId;
	private String url;
	
	public LexiconLink(String name, String lexSrvcClntType, String url, 
			LexiconServiceClient srvcClient, LexiconIdentification lexId) {
		this.name = name;
		this.lexSrvcClntType = lexSrvcClntType;
		this.url = url;
		//this.setUrl(url);
		this.srvcClient = srvcClient;
		this.lexId = lexId;
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

	public void setLexSrvcClntType(String lexSrvcClntType) {
		this.lexSrvcClntType = lexSrvcClntType;
	}

	public String getLexSrvcClntType() {
		return lexSrvcClntType;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * @return the srvcClient
	 */
	public LexiconServiceClient getSrvcClient() {
		return srvcClient;
	}
	
	/**
	 * Sets the service client
	 *  
	 * @param srvcClient the service client
	 */
	public void setSrvcClient(LexiconServiceClient srvcClient) {
		this.srvcClient = srvcClient;
	}

	/**
	 * @return the lexId
	 */
	public LexiconIdentification getLexId() {
		return lexId;
	}
	
	public void setLexId(LexiconIdentification lexId) {
		this.lexId = lexId;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
