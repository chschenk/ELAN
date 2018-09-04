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
	 * Copy constructor. Creates a deep clone of the specified bundle, with the
	 * exception of a possible LexiconServiceClient object, part of the LexiconLink.
	 * This object has to be loaded and set separately by the client application.
	 * 
	 * @param otherBundle the bundle to copy, not null
	 */
	public LexiconQueryBundle2(LexiconQueryBundle2 otherBundle) {
		if (otherBundle == null) {
			throw new NullPointerException("The Lexicon Bundle is null");
		}

		LexiconLink otherLL = otherBundle.getLink();
		if (otherLL != null) {
			LexiconIdentification otherLexId = otherLL.getLexId();
			LexiconIdentification nextLexId = null;
			if (otherLexId != null) {
				// use the copy constructor 
				nextLexId = new LexiconIdentification(otherLexId);
			}
			
			this.link = new LexiconLink(otherLL.getName(), otherLL.getLexSrvcClntType(), 
					otherLL.getUrl(), 
					null, 
					nextLexId);
		}
		
		LexicalEntryFieldIdentification otherLEFI = otherBundle.getFldId();
		if (otherLEFI != null) {
			this.fldId = new LexicalEntryFieldIdentification(otherLEFI.getId(), 
					otherLEFI.getName());
			this.fldId.setDescription(otherLEFI.getDescription());
		}
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
	 * @param other the object to compare to 
	 * 
	 * @return true if the two objects are the same object or if the other object is a 
	 * LexiconQueryBundle2 with the same linkName and same lexical entry field id 
	 * (also just based on the name), false otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof LexiconQueryBundle2)) {
			return false;
		}
		LexiconQueryBundle2 olqb = (LexiconQueryBundle2) other;
		boolean sameField = false;
		boolean sameLexLink = false;
		
		if (fldId != null && olqb.getFldId() != null) {
			sameField = (fldId.compareTo(olqb.getFldId()) == 0);// compares based on the name only
		}
		
		if (link != null) {
			sameLexLink = link.compareNameAndLexId(olqb.getLink()) == 0;
		} else if (link == null && olqb.getLink() == null) {
			sameLexLink = true; //treat as same;
		}
		
		return sameField && sameLexLink;
	}
	
	
}
