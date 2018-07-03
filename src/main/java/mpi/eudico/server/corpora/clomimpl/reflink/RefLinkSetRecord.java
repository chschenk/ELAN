package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Corresponds to the parse result of an RefLinkSet.
 * Also works as a factory.
 * 
 * @author olasei
 */
public class RefLinkSetRecord {
	// Attributes
	private String linksID;
	private String linksName;
	private String extRefID;
	private String langRef;
	private String cvRef;
	
	// Content
	private List<AbstractRefLinkRecord> refLinks;
	
	/**
	 * Construct the record, ready for use.
	 */
	public RefLinkSetRecord() {
		refLinks = new ArrayList<AbstractRefLinkRecord>();
	}

	/**
	 * @return the linksID
	 */
	public String getLinksID() {
		return linksID;
	}

	/**
	 * @param linksID the linksID to set
	 */
	public void setLinksID(String linksID) {
		this.linksID = linksID;
	}

	/**
	 * @return the linksName
	 */
	public String getLinksName() {
		return linksName;
	}

	/**
	 * @param linksName the linksName to set
	 */
	public void setLinksName(String linksName) {
		this.linksName = linksName;
	}

	/**
	 * @return the extRefID
	 */
	public String getExtRefID() {
		return extRefID;
	}

	/**
	 * @param extRefID the extRefID to set
	 */
	public void setExtRefID(String extRefID) {
		this.extRefID = extRefID;
	}

	/**
	 * @return the langRef
	 */
	public String getLangRef() {
		return langRef;
	}

	/**
	 * @param langRef the langRef to set
	 */
	public void setLangRef(String langRef) {
		this.langRef = langRef;
	}

	/**
	 * @return the cvRef
	 */
	public String getCvRef() {
		return cvRef;
	}

	/**
	 * @param cvRef the cvRef to set
	 */
	public void setCvRef(String cvRef) {
		this.cvRef = cvRef;
	}

	/**
	 * @return the refs
	 */
	public List<AbstractRefLinkRecord> getRefLinks() {
		return refLinks;
	}

	/**
	 * @param refs the refs to set
	 */
	public void setRefLinks(List<AbstractRefLinkRecord> refs) {
		this.refLinks = refs;
	}

	/**
	 * Factory function.
	 * @param externalReferences, to map external reference IDs to objects.

	 * @return a CrossRefLink or a GroupRefLink (or any other subtype of AbstractRefLink).
	 */
	public RefLinkSet fabricate(
			TranscriptionImpl trans,
			Map<String, ? extends ExternalReference> externalReferences) {
		RefLinkSet set = new RefLinkSet(trans);
		
		set.setLinksID(linksID);
		set.setLinksName(linksName);
		set.setCvRef(cvRef);
		set.setLangRef(langRef);
		if (extRefID != null) {
			set.setExtRef(externalReferences.get(extRefID));
		}
		
		List<RefLink> instlist = new ArrayList<RefLink>();
		set.setRefLinks(instlist);
		
		for (AbstractRefLinkRecord rec : refLinks) {
			instlist.add(rec.fabricate(externalReferences));
		}	
		
		return set;
	}

}
