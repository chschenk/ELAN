package mpi.eudico.server.corpora.clom;

import java.net.URI;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clomimpl.abstr.LicenseRecord;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.reflink.RefLinkSet;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;
import mpi.eudico.server.corpora.util.ACMEditableDocument;
import mpi.eudico.server.corpora.util.ACMEditableObject;
import mpi.eudico.util.ControlledVocabulary;

/**
 * Transcription encapsulates the notion of an annotation document. 
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 5-Nov-1998
 * @version Aug 2005 Identity removed
 * @version Dec 2012 TreeViewable, UnsharedInfoObject, SharedDataObject removed
 */
public interface Transcription extends ACMEditableObject, ACMEditableDocument {

	public static int NORMAL = 0;
	public static int BULLDOZER = 1;
	public static int SHIFT = 2;

	/**
	 * Gives the Transcription name.
	 *
	 * @return	the name
	 */
	public String getName();

	public void setName(String theName);
	
	/**
	 * Returns the full path as a url.
	 * 
	 * @version Dec 2012 this method used to be part of the LanguageResource interface
	 * @return the full path (url)
	 */
	public String getFullPath();
	

	/**
	 * Returns the list of Tiers that are accessible.
	 *
	 * @return	the list of Tiers
	 */
	public List<? extends Tier> getTiers();


	/**
	 * Adds a Tier to the Transcription.
	 *
	 * @param theTier	the Tier to be added
	 */
	public void addTier(Tier theTier);


	/**
	 * Removes a Tier from the Transcription.
	 *
	 * @param theTier	the Tier to be removed
	 */
	public void removeTier(Tier theTier);


	/**
	 * Returns all TimeSlots, ordered in a TimeOrder object.
	 *
	 */
	public TimeOrder getTimeOrder();
	
	public List<Annotation> getAnnotationsUsingTimeSlot(TimeSlot theSlot);

	public void setAuthor(String theAuthor);

	public String getAuthor() ;

	// Linguistic Types
	public void setLinguisticTypes(List<LinguisticType> theTypes);
	public List<LinguisticType> getLinguisticTypes();
	public void addLinguisticType(LinguisticType theType);
	public void removeLinguisticType(LinguisticType theType);
	public void changeLinguisticType(LinguisticType linType,
			String newTypeName,
			List<Constraint> constraints,
			String newControlledVocabularyName,
			boolean newTimeAlignable,
			String dataCategoryId,
			LexiconQueryBundle2 queryBundle);

	LinguisticType getLinguisticTypeByName(String name);
	List<LinguisticType> getLinguisticTypesWithCV(String name);
	public List<? extends Tier> getTiersWithLinguisticType(String typeID);

	// lexicon-link related methods
	/** 
	 * 
	 * @param link
	 */
	public void addLexiconLink(LexiconLink link);
	public Map<String, LexiconLink> getLexiconLinks();
	public LexiconLink getLexiconLink(String linkName);
	public void removeLexiconLink(LexiconLink link);

	public List<MediaDescriptor> getMediaDescriptors();
	public void setMediaDescriptors(List<MediaDescriptor> theMediaDescriptors);
	
	/**
	 * Returns the collection of linked file descriptors
	 * @return the linked file descriptors
	 */
	public List<LinkedFileDescriptor> getLinkedFileDescriptors();
	
	/**
	 * Sets the collection of linked files descriptors.
	 * 
	 * @param descriptors the new descriptors
	 */
	public void setLinkedFileDescriptors(List<LinkedFileDescriptor> descriptors);

	/**
	 * <p>MK:02/06/12<br>
	 * The ID of a tier has yet to be defined.
	 * Tiers so far have only names given by the user,
	 * which have been used in the EAF XML file format as XML IDs.
	 * Because tier name are used as IDs, not two
	 * tiers can have the same name. This is unacceptable, because each tier has
	 * its own speaker/participant. When two persons speak on the same tier, the
	 * tier has to be renamed for each person. For shoebox, this is done by
	 * appending '@'speaker to the tiername. This will confuse the user.
	 * For lifting this restriction, tiers should get a proper ID.
	 * <p>
	 * @param theTierId currently the name of the tier. Has to be changed!
	 * @return Tier with given name, or null
	 * */
	public Tier getTierWithId(String theTierId);

	/**
	 * Returns a List containing all annotations (as ids!) covering the corresponding time
	 * @param time
	 * @return
	 */
	public List<String> getAnnotationIdsAtTime(long time);
	
	/**
	 * Returns the annotation with the corresponding id or null. 
	 * @param id
	 * @return
	 */
	public Annotation getAnnotationById(String id);

	public long getLatestTime();
	
	/**
	 * Returns whether any modifications are made since the last reset (when saving)
	 */
	public boolean isChanged();

	/**
	 * Resets 'changed' status to unchanged
	 */
	public void setUnchanged();

	/**
	 * Sets 'changed' status to changed
	 */
	public void setChanged();
	
	/**
	 * Returns time Change Propagation Mode (normal, bulldozer or shift)
	 * 
	 * @author hennie
	 */
	public int getTimeChangePropagationMode();
	
	/**
	 * Set Time Change Propagation Mode (normal, bulldozer or shift)
	 * @author hennie
	 */
	public void setTimeChangePropagationMode(int theMode);
	
	/**
	 * Each Transcription has a unique/universal resource name which is used to
	 * uniquely identify this transcription and its previous and future versions of it.
	 * @return The Universal Resource Name.
	 */
	public URI getURN();
	
	/**
	 * The license(s) that apply to the Transcription.
	 * Must not return null but the List may be empty.
	 */
	List<LicenseRecord> getLicenses();
	void setLicenses(List<LicenseRecord> licenses);

	List<? extends Annotation> getChildAnnotationsOf(Annotation theAnnot);

	// Controlled Vocabularies
	void setControlledVocabularies(List<ControlledVocabulary> controlledVocabs);
	List<ControlledVocabulary> getControlledVocabularies();
	ControlledVocabulary getControlledVocabulary(String name);

	// Properties
	void addDocProperties(List<Property> props);
	void addDocProperty(Property prop);
	boolean removeDocProperty(Property prop);
	List<? extends Property> getDocProperties();

	// reference links sets
	/**
	 * Adds a set of reference links
	 * @param refLinkSet
	 */
	public void addRefLinkSet(RefLinkSet refLinkSet);
	/**
	 * Removes a set of reference links
	 * @param refLinkSet
	 */
	public void removeRefLinkSet(RefLinkSet refLinkSet);
	/**
	 * @param name the (friendly) name of the set
	 * @return the set of that name or null
	 */
	public RefLinkSet getRefLinkSetByName(String name);
	/**
	 * @param id the (generated) id of the set
	 * @return the set with that id or null
	 */
	public RefLinkSet getRefLinkSetById(String id);
	/**
	 * @return all sets of reference links
	 */
	public List<RefLinkSet> getRefLinkSets();
}
