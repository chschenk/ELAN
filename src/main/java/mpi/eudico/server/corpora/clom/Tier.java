package mpi.eudico.server.corpora.clom;

import java.util.List;

import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.util.ACMEditableObject;


/**
 * A Tier represents a layer or 'stream' of sequential events/phenomena.
 * Transcriptions can contain multiple Tiers. Tiers can contain Annotations, 
 * Annotations have a label and can possibly refer to a time interval.
 * <h1>
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 1-Jun-1999
 * @version Aug 2005 Identity removed
 * @version April 2015 several methods moved from TierImpl to this interface
 */
public interface Tier extends ACMEditableObject {
	
	/**
	 * Returns the transcription.
	 * 
	 * @version Dec 2012 introduced 
	 * @version April 2015 getParent renamed to getTranscription
	 * @return the transcription this tier is part of
	 */
	public Transcription getTranscription();
	
    /**
     * Returns the Tier's name.
     *
     * @return the name of the Tier
     */
    public String getName();

    /**
     * Sets the tier's name. Must be unique.
     */
    public void setName(String theName);

	public String getExtRef();
	public void setExtRef(String extRef);

	public String getLangRef();
	public void setLangRef(String langRef);
	
	public List<? extends Annotation> getAnnotations();
	public int getNumberOfAnnotations();
	public void addAnnotation(Annotation theAnnotation);
	public void removeAnnotation(Annotation theAnnotation);
	public void removeAllAnnotations();
	
	public void setLinguisticType(LinguisticType theType);
	public LinguisticType getLinguisticType();
	public void setAnnotator(String annotator);
	public String getAnnotator();
	public void setParticipant(String theParticipant);
	public String getParticipant();
	
	// Tier hierarchy
	public void setParentTier(Tier newParent);
	public Tier getParentTier();
	public boolean hasParentTier();
	public Tier getRootTier();
	public boolean hasAncestor(Tier ancestor);
	/**
	 * Returns all direct children of this tier.
	 */
	public List<? extends Tier> getChildTiers();
	/**
	 * Returns all direct and indirect children of this tier.
	 */
	public List<? extends Tier> getDependentTiers();

}
