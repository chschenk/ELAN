package mpi.eudico.server.corpora.clom;

import java.util.Set;

/**
 * RefLink is the common interface for the GROUP_REF_LINK and CROSS_REF_LINK
 * elements in the EAF file.
 * <p>
 * These elements link together Annotations, either one-on-one or in groups.
 * <p>
 * Implementation note:
 * don't override hash() or equals(), since we want RefLinks to be in
 * HashSets based on their reference value only.
 * 
 * @author olasei
 */

public interface RefLink {

	/**
	 * The ID of the reference link itself.
	 */
	public String getId();
	
	/**
	 * A name of the reference link
	 */
	public String getRefName();

	/**
	 * Any External Reference that there may be
	 */
	public ExternalReference getExtRef();

	/**
	 * Which language is the Controlled Vocabulary Entry in
	 */
	public String getLangRef();

	/**
	 * A Controlled Vocabulary Entry
	 */
	public String getCveRef();

	/**
	 * What sort of reference is this, the type of reference
	 */
	public String getRefType();

	/**
	 * The text inside the element (may be removed from the schema)
	 */
	public String getContent();
	
	/**
	 * Check if this RefLink refers in some way to any of the given ids.
	 */
	public boolean references(Set<String> ids);
	
	/**
	 * Convert the RefLink to some readable representation.
	 */
	@Override
	public String toString();
}
