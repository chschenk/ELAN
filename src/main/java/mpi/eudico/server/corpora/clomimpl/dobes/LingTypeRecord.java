/*
 * Created on Jun 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * @author hennie
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LingTypeRecord {

	public static final String ALIGNABLE = "alignable";
	public static final String REFERENCE = "reference";
	
	private String lingTypeId;
	private String timeAlignable;
	private String stereotype;
	private String controlledVocabulary;
	private String extRefId;
	private String lexiconService;
	
	public String getLingTypeId() {
		return lingTypeId;
	}
	
	public void setLingTypeId(String lingTypeId) {
		this.lingTypeId = lingTypeId;
	}
	
	public String getTimeAlignable() {
		return timeAlignable;
	}
	
	public void setTimeAlignable(String timeAlignable) {
		this.timeAlignable = timeAlignable;
	}
	
	public String getStereoType() {
		return stereotype;
	}
	
	public void setStereoType(String stereotype) {
		this.stereotype = stereotype;
	}
	
	
	/**
	 * The name of the Controlled Vocabulary to be used by this type.
	 * 
	 * @return name of the Controlled Vocabulary to be used by this type
	 */
	public String getControlledVocabulary() {
		return controlledVocabulary;
	}

	/**
	 * Sets the name of the Controlled Vocabulary to be used by this type.
	 * 
	 * @param name he name of the Controlled Vocabulary to be used by this type
	 */
	public void setControlledVocabulary(String name) {
		controlledVocabulary = name;
	}

	/**
	 * Returns the id of an external reference object
	 * 
	 * @return the extRefId the id of an external reference, e.g. a concept defined in ISO DCR
	 */
	public String getExtRefId() {
		return extRefId;
	}

	/**
	 * Sets the external reference id.
	 * 
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	
	/**
	 * 
	 * @param name name of the lexicon
	 */
	public void setLexiconReference(String name) {
		this.lexiconService = name;
	}

	/**
	 * 
	 * @return the name of the lexicon
	 */
	public String getLexiconReference() {
		return lexiconService;
	}

}
