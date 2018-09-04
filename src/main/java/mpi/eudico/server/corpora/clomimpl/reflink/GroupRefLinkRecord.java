package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.RefLink;

/**
 * A just-parsed form of a GroupRefLink.
 * Also works as a factory.
 *  
 * @author olasei
 */
public class GroupRefLinkRecord extends AbstractRefLinkRecord {
	String refs;

	/**
	 * @return the refs
	 */
	public String getRefs() {
		return refs;
	}

	/**
	 * @param refs the refs to set
	 */
	public void setRefs(String refs) {
		this.refs = refs;
	}

	@Override
	public RefLink fabricate(
			Map<String, ? extends ExternalReference> externalReferences) {
		GroupRefLink refLink = new GroupRefLink();
		fabricate(refLink, externalReferences);
		
		Set<String> ids =  new HashSet<String>();
		for (String id : refs.split(" ")) {
			ids.add(id);
		}
		refLink.setRefs(ids);
		
		return refLink;
	}
}
