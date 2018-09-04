package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.Set;

/**
 * GroupRefLink is the internal class representing GROUP_REF_LINK
 * elements in the EAF file.
 * It links any number of Annotations together.
 * <p>
 * It has yet to be decided if it can also link other objects, such as
 * CrossRefLinks or other GroupRefLinks.
 *
 * @author olasei
 */

public class GroupRefLink extends AbstractRefLink {
	Set<String> refs; // typically a HashSet<String>
	
	@Override
	public boolean references(Set<String> ids) {
		// Since there is no Set.containsAny(Set), we'll have to use a loop.
		for (String id : ids) {
			if (refs.contains(id)) {
				return true;
			}
		}
		return false;
	}

	public GroupRefLink() {
		super();
	}

	/**
	 * This returns the set of IDs of the elements that are linked together.
	 * <p>
	 * It is not decided yet whether these can include only Annotations,
	 * or also CrossRefLinks or even GroupRefLinks.
	 * 
	 * @return the refs
	 */
	public Set<String> getRefs() {
		return refs;
	}

	/**
	 * @param refs the refs to set
	 */
	public void setRefs(Set<String> refs) {
		this.refs = refs;
	}

	@Override
	public String toString() {
		return "GroupRefLink:{" + id + "=" + refs.toString() + ":" + content + "}";
		
	}
}
