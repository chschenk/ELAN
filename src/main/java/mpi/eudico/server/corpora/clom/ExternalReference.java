package mpi.eudico.server.corpora.clom;

import java.io.Serializable;

/**
 * Defines an object for external references to e.g. concepts or resources.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface ExternalReference extends Cloneable, Serializable {
    /** the undefined type reference constant */
    public static final int UNDEFINED = 0;

    /** a constant for a group of references */
    public static final int REFERENCE_GROUP = 1;

    /**
     * a constant for a reference to (the id of) a concept defined in the ISO
     * 12620 DCR
     */
    public static final int ISO12620_DC_ID = 2;

    /**
     * a constant to an external resource identified by a url or uri. The exact
     * type of the resource is not defined
     */
    public static final int RESOURCE_URL = 3;
    
	/**
	 * a constant to an external controlled vocabulary identified by a url or uri. 
	 */
	public static final int EXTERNAL_CV = 4;
	
	/**
	 * A reference to the id of an Entry in an external Controlled Vocabulary (id).
	 */
	public static final int CVE_ID = 5;
	
	/**
	 * A reference to the id of an entry in a lexicon (url, url+id or id)
	 */
	public static final int LEXEN_ID = 6;

    /**
     * Returns the type of reference, e.g. url to a resource, id of concept in
     * DCR or ontology.
     *
     * @return the reference type
     */
    public int getReferenceType();

    /**
     * Sets the type of this reference object, e.g. a resource (url) or a
     * concept (id)
     *
     * @param refType the type of the reference
     */
    public void setReferenceType(int refType);

    /**
     * Returns the value of the reference. It depends on the type of the
     * reference how the  value should be interpreted.
     *
     * @return the reference value
     */
    public String getValue();

    /**
     * Sets the value of the reference, a url or an id depending on the type of
     * reference.
     *
     * @param value the value of the reference
     */
    public void setValue(String value);

    /**
     * Returns a string representation of the external reference object.
     *
     * @return a string representation of the external reference object
     */
    public String paramString();
    
    /**
     * Returns a string representation of the type of the external reference object.
     */
    public String getTypeString();

    /**
     * Creates and returns a copy of this instance.
     *
     * @return a copy of this instance
     *
     * @throws CloneNotSupportedException
     */
    public ExternalReference clone() throws CloneNotSupportedException;
}
