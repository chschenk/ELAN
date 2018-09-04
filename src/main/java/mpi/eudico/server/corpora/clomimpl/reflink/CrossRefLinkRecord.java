package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.Map;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;

/**
 * A just-parsed form of a CrossRefLink.
 * Also works as a factory.
 * 
 * @author olasei
 */
public class CrossRefLinkRecord extends AbstractRefLinkRecord {
	protected String ref1;
	protected String ref2;
	protected String directionality;
	
	/**
	 * @return the ref1
	 */
	public String getRef1() {
		return ref1;
	}
	/**
	 * @param ref1 the ref1 to set
	 */
	public void setRef1(String ref1) {
		this.ref1 = ref1;
	}
	/**
	 * @return the ref2
	 */
	public String getRef2() {
		return ref2;
	}
	/**
	 * @param ref2 the ref2 to set
	 */
	public void setRef2(String ref2) {
		this.ref2 = ref2;
	}
	/**
	 * @return the directionality
	 */
	public String getDirectionality() {
		return directionality;
	}
	/**
	 * @param directionality the directionality to set
	 */
	public void setDirectionality(String directionality) {
		this.directionality = directionality;
	}
	
	@Override
	public RefLink fabricate(
			Map<String, ? extends ExternalReference> externalReferences) {
		CrossRefLink refLink = new CrossRefLink();
		fabricate(refLink, externalReferences);
		refLink.setRef1(ref1);
		refLink.setRef2(ref2);
		refLink.setDirectionality(directionality);
		
		return refLink;
	}
}
