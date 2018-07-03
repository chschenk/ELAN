package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;

/**
 * Abstract base class for the just-parsed forms of a ...RefLink.
 * Also has a base-implementation of a factory for the ...RefLink object.
 * <p>
 * Implementation note:
 * This class is mainly needed because its ExternalReference member cannot
 * be definitively initialised when parsing the EAF file.
 * It needs to be converted from and id to the proper object.
 * 
 * @author olasei
 */
public abstract class AbstractRefLinkRecord {
	protected String id;
	protected String refName;
	protected String extRefID;
	protected String langRef;
	protected String cveRef;
	protected String refType;
	protected String content;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the refName
	 */
	public String getRefName() {
		return refName;
	}
	/**
	 * @param refName the refName to set
	 */
	public void setRefName(String refName) {
		this.refName = refName;
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
	 * @return the cveRef
	 */
	public String getCveRef() {
		return cveRef;
	}
	/**
	 * @param cveRef the cveRef to set
	 */
	public void setCveRef(String cveRef) {
		this.cveRef = cveRef;
	}
	/**
	 * @return the refType
	 */
	public String getRefType() {
		return refType;
	}
	/**
	 * @param refType the refType to set
	 */
	public void setRefType(String refType) {
		this.refType = refType;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	public abstract RefLink fabricate(
			Map<String, ? extends ExternalReference> externalReferences);
	
	protected void fabricate(AbstractRefLink rl, Map<String, ? extends ExternalReference> externalReferences) {
		rl.setId(id);
		rl.setRefName(refName);
		if (extRefID != null) {
			rl.setExtRef(externalReferences.get(extRefID));
		}
		rl.setCveRef(cveRef);
		rl.setLangRef(langRef);
		rl.setRefType(refType);
		rl.setContent(content);
	}
}
