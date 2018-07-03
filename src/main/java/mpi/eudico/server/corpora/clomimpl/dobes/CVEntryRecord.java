package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information needed to construct a CVEntry object.
 * 
 * @see mpi.eudico.util.CVEntry
 * 
 * @author Han Sloetjes
 * @version jun 2004
 */
public class CVEntryRecord {
	private String description;
	private String value;
	private String extRefId;
	private String id;
	private List<CVEntryRecord> subEntries;
	private String subEntryLangRef;

	/**
	 * Returns the description.
	 * @return the description, or null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the value.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the description.
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the value.
	 * @param value the value
	 */
	public void setValue(String value) {
		this.value = value;
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
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/*
	 * The same record is used for old-style one-language vocabularies
	 * and new-style multi-language vocabularies, so that the new parser
	 * can also parse the old files.
	 * This makes the data structure a bit messy.
	 */
	public boolean isSubEntry() {
		return subEntryLangRef != null;
	}
	
	public boolean isMLEntry() {
		return subEntries != null;
	}

	public String getSubEntryLangRef() {
		return subEntryLangRef;
	}

	public void setSubEntryLangRef(String subEntryLangRef) {
		this.subEntryLangRef = subEntryLangRef;
	}

	public void addSubEntry(CVEntryRecord cve) {
		assert(!this.isSubEntry());
		assert(cve.isSubEntry());
		assert(this.description == null);
		assert(this.subEntryLangRef == null);
		assert(this.value == null);
		
		if (subEntries == null) {
			subEntries = new ArrayList<CVEntryRecord>(2);
		}
		subEntries.add(cve);
	}
	
	public List<CVEntryRecord> getSubEntries() {
		return subEntries;
	}
}
