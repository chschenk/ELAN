package mpi.eudico.server.corpora.lexicon;

/**
 * Contains a Lexicon Link and a Lexical Field Identification for querying a lexicon
 * @author Micha Hulsbosch
 *
 */
public class LexiconQueryBundle2 {
	private LexicalEntryFieldIdentification fldId;
	private LexiconLink link;
	
	public LexiconQueryBundle2(LexiconLink link,
			LexicalEntryFieldIdentification lexicalEntryFieldIdentification) {
		this.link = link;
		this.fldId = lexicalEntryFieldIdentification;
	}

	/**
	 * @return the fldId
	 */
	public LexicalEntryFieldIdentification getFldId() {
		return fldId;
	}

	public String getLinkName() {
		if (link != null) {
			return link.getName();
		}
		return null;
	}
	
	public LexiconLink getLink() {
		return link;
	}
	
	/**
	 * Returns the name of the lexicon link.
	 * 
	 * @return the name of the lexicon link (and the bundle)
	 */
	@Override
	public String toString() {
		if (link != null) {
			return link.getName();
		}
		return "No Name";
	}

	/**
	 * Returns true if the two objects are the same object or if they have the same
	 * linkName and same lexical entry field id.
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof LexiconQueryBundle2)) {
			return false;
		}
		
		//...
		return super.equals(other);
	}
	
	
}
