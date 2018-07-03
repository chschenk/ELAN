package mpi.eudico.util;

import mpi.eudico.server.corpora.clom.ExternalReference;

/**
 * A class for an externally defined and stored controlled vocabulary, 
 * i.e. not part of the annotation document.
 *
 */
public class ExternalCV extends ControlledVocabulary {

    private ExternalReference externalRef;
    private boolean isLoadedFromURL;
    private boolean isLoadedFromCache;

	
	public ExternalCV(String name) {
		super(name);
	}

	/**
	 * Create an External CV from a plain CV.
	 * 
	 * @param cv
	 */
	public ExternalCV(BasicControlledVocabulary cv) {
		super(cv.getName());
		cloneStructure(cv);
		cloneEntries(cv);
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param ecv
	 */
	public ExternalCV(ExternalCV ecv) {
		super(ecv.getName());
		cloneStructure(ecv);
		externalRef = ecv.getExternalRef();
		if (externalRef != null) {
			try {
				externalRef = externalRef.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		isLoadedFromURL = ecv.isLoadedFromURL;
		isLoadedFromCache = ecv.isLoadedFromCache;
		cloneEntries(ecv);
	}
	
//	public ExternalCV(String cvName, String extRefId) {
//		this(cvName);
//		setExternalRef(extRefId);
//	}

	public ExternalReference getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(ExternalReference externalRef) {
		this.externalRef = externalRef;
	}
	
	
	@Override
	protected void handleModified() {
		// do nothing, external CV's are not editable
	}

	public boolean isLoadedFromURL() {
		return isLoadedFromURL;
	}

	public void setLoadedFromURL(boolean isLoadedFromURL) {
		this.isLoadedFromURL = isLoadedFromURL;
	}

	public boolean isLoadedFromCache() {
		return isLoadedFromCache;
	}

	public void setLoadedFromCache(boolean isLoadedFromCache) {
		this.isLoadedFromCache = isLoadedFromCache;
	}
	
    /**
     * An implementation of clone() that uses the copy constructor.
     * (This implies that subclasses must override)
     */
	@Override // Cloneable
	public ExternalCV clone() {
		return new ExternalCV(this);
	}
	
    /**
     * This function clones all CVEntries from an original vocabulary.
     * This CV will contain copies of the entries, converted to ExternalCVEntries.
     * <p>
     * This assumes that the languages match.
     * 
     * @param orig CV to clone the entries from
     */
    
    @Override
	public void cloneEntries(BasicControlledVocabulary orig) {
    	initMode = true;
    	entries.clear();
    	for (CVEntry e : orig) {
    		addEntry(new ExternalCVEntry(this, e));
    	}
    	initMode = false;
    }
    
    /**
     * This function clones all ExternalCVEntries from an original external vocabulary.
     * This CV will contain copies of the entries.
     * <p>
     * This assumes that the languages match.
     * 
     * @param orig ExternalCV to clone the entries from
     */
    
    public void cloneEntries(ExternalCV orig) {
    	initMode = true;
    	entries.clear();
    	for (CVEntry e : orig) {
    		addEntry(new ExternalCVEntry(this, (ExternalCVEntry)e));
    	}
    	initMode = false;
    }
	
//	public ArrayList getSuggestions(String input) {
//		ArrayList suggestions = new ArrayList();
//		for(String entry: getEntryValues()) {
//			if(entry.startsWith(input)) {
//				suggestions.add(entry);
//			}
//		}
//		return suggestions;
//	}
}
