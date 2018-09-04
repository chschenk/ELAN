package mpi.eudico.server.corpora.clomimpl.dobes;

/**
 * A record storing the tier's attributes e.g. as parsed from the eaf.
 *
 * Dec 2006 HS: added a field for Annotator, the person who created 
 * (the annotations of) the tier.
 * @author Han Sloetjes
 */
public class TierRecord {
    /** the name of the tier */
    private String name;

    /** the default locale */
    private String defaultLocale;

    /** the linguistic type name */
    private String linguisticType;

    /** the participant */
    private String participant;
    
    /** the annotator */
    private String annotator;

    /** the parent tier name */
    private String parentTier;

	private String extRef;

	private String langRef;

    /**
     * Returns the locale name
     *
     * @return the locale name
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Returns the linguistic type's name
     *
     * @return the linguistic type
     */
    public String getLinguisticType() {
        return linguisticType;
    }

    /**
     * Returns the tier's name
     *
     * @return the tier's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the tier's parent tier's name
     *
     * @return the parent's name
     */
    public String getParentTier() {
        return parentTier;
    }

    /**
     * Returns the participant
     *
     * @return the participant
     */
    public String getParticipant() {
        return participant;
    }

    /**
     * Returns the annotator
     *
     * @return the annotator
     */
	public String getAnnotator() {
		return annotator;
	}
	
    /**
     * Sets the locale
     *
     * @param locale the locale
     */
    public void setDefaultLocale(String locale) {
        defaultLocale = locale;
    }

    /**
     * Sets the linguistic type
     *
     * @param type the linguistic type name
     */
    public void setLinguisticType(String type) {
        linguisticType = type;
    }

    /**
     * Sets the name
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the parent's name
     *
     * @param name parent's name
     */
    public void setParentTier(String name) {
        parentTier = name;
    }

    /**
     * Sets the participant
     *
     * @param part the participant
     */
    public void setParticipant(String part) {
        participant = part;
    }

    /**
     * Sets the annotator
     *
     * @param annotator the annotator
     */
	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public String getExtRef() {
		return extRef;
	}

	public void setExtRef(String value) {
		this.extRef = value;
	}
	
	public String getLangRef() {
		return langRef;
	}

	public void setLangRef(String value) {
		this.langRef = value;
	}
}
