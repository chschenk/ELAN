package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

/**
 * DOCUMENT ME!
 * $Id: LinguisticType.java 44075 2015-07-21 15:11:43Z olasei $
 * @author $Author$
 * @version $Revision$
 */
public class LinguisticType {
    /** Holds value of property DOCUMENT ME! */
    String typeName;

    /** Holds value of property DOCUMENT ME! */
    Constraint constraints; // can be composite

    /** Holds value of property DOCUMENT ME! */
    boolean timeAlignable = true;

    /* HS: added jun 04 support for controlled vocabularies */
    /** the identifier of the Controlled Vocabulary in use by this lin. type */
    String controlledVocabularyName;
    
    /*HS: added april 08 support for reference to a ISO Data Category. Initially a string,
     * might need an Object later */
    /** a reference to a (ISO) Data Category. Can be a simple id (when it is a category from the ISO DCR)
     * or a combination of DCR identifier + id, e.g. ISO12620#32 */
    String dataCategory;
    
	/**
	 * Holds a lexicon connection
	 */
	LexiconQueryBundle2 lexiconQueryBundle = null;

    /**
     * Creates a new LinguisticType instance
     *
     * @param theName DOCUMENT ME!
     */
    public LinguisticType(String theName) {
        typeName = theName;
    }
    
    /**
     * Duplicates a linguistic type instance using a new name.
     * Note that this does not clone the CV if that's desired.
     * 
     * @param theName
     * @param orig
     */
    public LinguisticType(String theName, LinguisticType orig) {
    	this(theName);
    	if (orig.hasConstraints()) {
    		try {
    			addConstraint(orig.getConstraints().clone());
    		}
    		catch(CloneNotSupportedException ex) {
    			ex.printStackTrace(); // can't happen
    		}
    	}
    	setTimeAlignable(orig.isTimeAlignable());
    	// The caller should clone CV but for now, use the same name
    	setControlledVocabularyName(orig.getControlledVocabularyName());
    	setDataCategory(orig.getDataCategory());
    	setLexiconQueryBundle(orig.getLexiconQueryBundle());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String toString() {
        String stereotype = "";

        if (constraints != null) {
            stereotype = Constraint.stereoTypes[constraints.getStereoType()];
        }

        return typeName + ", " + timeAlignable + ", " + stereotype;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getLinguisticTypeName() {
        return typeName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theName DOCUMENT ME!
     */
    public void setLinguisticTypeName(String theName) {
        typeName = theName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasConstraints() {
        if (constraints != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Constraint getConstraints() {
        return constraints;
    }

    /**
     * DOCUMENT ME!
     *
     * @param theConstraint DOCUMENT ME!
     */
    public void addConstraint(Constraint theConstraint) {
        //MK:02/06/30 removed println. If you need debug output, use debugln.
        //System.out.println("add Constraint of stereotype: " + theConstraint.getStereoType() + " to LinguisticType: " + typeName);
        if (constraints == null) {
            constraints = theConstraint;
        } else {
            constraints.addConstraint(theConstraint);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void removeConstraints() {
        constraints = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isTimeAlignable() {
        return timeAlignable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isTimeAlignable DOCUMENT ME!
     */
    public void setTimeAlignable(boolean isTimeAlignable) {
        timeAlignable = isTimeAlignable;
    }

    /**
     * Returns whether or not annotation values on tiers using this LinguisticType 
     * are restricted by a ControlledVocabulary.<br>
     * Current implementation is very loose; the value returned only depends 
     * on the presence of a non-null reference string. Could be more strict 
     * by keeping a flag independent from a reference string or object reference.
     * 
     * @return true if there is a reference to a ControlledVocabulary
     */
    public boolean isUsingControlledVocabulary() {
    	return (controlledVocabularyName == null || 
    		controlledVocabularyName.length() == 0) ? false : true;
    }
    
    /**
     * Returns the name (identifier) of the ControlledVocabulary in use by 
     * this type.<br>
     * The actual CV objects are stored in and managed by the Transcription.
     * (Candidate for change: might store a reference to the CV object itself 
     * instead of using a reference to it.)
     * 
     * @return the name/identifier of the cv
     */
    public String getControlledVocabularyName() {
    	return controlledVocabularyName;
    }
    
    /**
     * Sets the name of the ControlledVocabulary to be used by this type.
     * 
     * @see #getControlledVocabularyName()
     * @param name the name/identifier of the cv
     */
    public void setControlledVocabularyName(String name) {
    	controlledVocabularyName = name;
    }
    
    /**
     * Returns the Data Category reference (the identifier).
     * 
     * @return the Data Category reference
     */
	public String getDataCategory() {
		return dataCategory;
	}
	
	/**
	 * Sets the Data Category reference.
	 * 
	 * @param dataCategory the identifier of the data category
	 */
	public void setDataCategory(String dataCategory) {
		this.dataCategory = dataCategory;
	}
	
	/**
	 * @return true if there is a reference to lexicon service 
	 * @author Micha Hulsbosch
	 */
	public boolean isUsingLexiconQueryBundle() {
		return (lexiconQueryBundle == null) ? false : true;
	}
	
	/**
	 * @return the lexiconQueryBundle
	 * @author Micha Hulsbosch
	 */
	public LexiconQueryBundle2 getLexiconQueryBundle() {
		return lexiconQueryBundle;
	}

	/**
	 * @param lexiconQueryBundle the lexiconQueryBundle to set
	 * @author Micha Hulsbosch
	 */
	public void setLexiconQueryBundle(LexiconQueryBundle2 lexiconQueryBundle) {
		this.lexiconQueryBundle = lexiconQueryBundle;
	}
    
	/**
	 * Overrides <code>Object</code>'s equals method by checking all  fields of
	 * the other object to be equal to all fields in this  object.
	 *
	 * @param obj the reference object with which to compare
	 *
	 * @return true if this object is the same as the obj argument; false
	 *         otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			// null is never equal
			return false;
		}

		if (obj == this) {
			// same object reference 
			return true;
		}

		if (!(obj instanceof LinguisticType)) {
			// it should be a LinguisticType object
			return false;
		}
		
		LinguisticType other = (LinguisticType) obj;
		
		if (typeName != null && !typeName.equals(other.getLinguisticTypeName())) {
			return false;
		} else if (other.getLinguisticTypeName() != null && 
			!other.getLinguisticTypeName().equals(typeName)) {
			return false;
		}
		
		if (isTimeAlignable() != other.isTimeAlignable()) {
			return false;
		}
		
		if (hasConstraints() != other.hasConstraints()) {
			return false;
		} else {
			if (hasConstraints() && !getConstraints().equals(other.getConstraints())) {
				return false;
			}
		}
		
		if (isUsingControlledVocabulary()) {
			if (!other.isUsingControlledVocabulary()) {
				return false;
			} else {
				// can compare CV only by their name
				if ( !controlledVocabularyName.equals(other.getControlledVocabularyName()) ) {
					return false;
				}
			}				
		} else {
			if (other.isUsingControlledVocabulary()) {
				return false;
			}
		}
		
		if (dataCategory == null) {
			if (other.getDataCategory() != null) {
				return false;
			}
		} else if (!dataCategory.equals(other.getDataCategory())) {
			return false;
		}
		
		if (lexiconQueryBundle == null) {
			if (other.getLexiconQueryBundle() != null) {
				return false;
			} else {
				// both null, return true
			}
		} else {
			if (other.getLexiconQueryBundle() == null) {
				return false;
			} else {
				// compare the lexiconQueryBundle (by name)?
				if (!lexiconQueryBundle.equals(other.getLexiconQueryBundle())) {
					return false;
				}
			}
		}
		
		return true;
	}

}
