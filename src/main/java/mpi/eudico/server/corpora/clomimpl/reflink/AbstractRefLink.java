package mpi.eudico.server.corpora.clomimpl.reflink;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;

/**
 * AbstractRefLink is the abstract superclass for the GROUP_REF_LINK and CROSS_REF_LINK
 * elements in the EAF file. It implements common attributes, but leaves abstract which
 * other elements are actually linked together.
 * 
 * @author olasei
 */

public abstract class AbstractRefLink implements RefLink, Comparable<AbstractRefLink> {
	protected String id;
	protected String refName;
	protected ExternalReference extRef;
	protected String langRef;
	protected String cveRef;
	protected String refType;
	protected String content;
	
	/**
	 * @param id
	 * @param extRef
	 * @param langRef
	 * @param cveRef
	 * @param refType
	 * @param content
	 */

	public AbstractRefLink() {
		super();
		this.id = "";
		this.refName = null;
		this.extRef = null;
		this.langRef = null;
		this.cveRef = null;
		this.refType = "";
		this.content = "";
	}
	/**
	 * @return the id
	 */
	@Override
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
	@Override
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
	 * @return the extRef
	 */
	@Override
	public ExternalReference getExtRef() {
		return extRef;
	}
	/**
	 * @param extRef the extRef to set
	 */
	public void setExtRef(ExternalReference extRef) {
		this.extRef = extRef;
	}
	/**
	 * @return the langRef
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override // Comparable<AbstractRefLink>
	public int compareTo(AbstractRefLink l) {
		return this.id.compareTo(l.getId());		
	}
	
}
