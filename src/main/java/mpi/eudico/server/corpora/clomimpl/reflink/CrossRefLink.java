package mpi.eudico.server.corpora.clomimpl.reflink;

import java.util.Set;

/**
 * CrossRefLink is the internal class representing CROSS_REF_LINK
 * elements in the EAF file.
 * It links two Annotations together.
 * <p>
 * There may be a direction in the link between id1 and id2:
 * undirected, unidirectional, or bidirectional.
 * <p>
 * It has yet to be decided if it can also link other objects, such as
 * GroupRefLinks or other CrossRefLinks.
 * 
 * @author olasei
 */

public class CrossRefLink extends AbstractRefLink {
	public enum Directionality {
		undirected,		// lowercase, so that toString() returns lowercase text
		unidirectional,
		bidirectional,
	};
	protected String ref1;
	protected String ref2;
	protected Directionality directionality;
	
	public CrossRefLink() {
		super();
	}

	/**
	 * This returns the ID of the first of the two elements that are linked together.
	 * 
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
	 * This returns the ID of the second of the two elements that are linked together.
	 * 
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
	public Directionality getDirectionality() {
		return directionality;
	}
	/**
	 * @param directionality the directionality to set
	 */
	public void setDirectionality(Directionality directionality) {
		this.directionality = directionality;
	}
	
	public void setDirectionality(String directionality) {
		try {
			this.directionality = Directionality.valueOf(directionality);
		} catch (Exception e) {
			this.directionality = Directionality.undirected;
		}
	}
	
	@Override
	public boolean references(Set<String> ids) {
		return ids.contains(ref1) || ids.contains(ref2);
	}
	
	@Override
	public String toString() {
		return "CrossRefLink:{" + id + "="  + ref1 + "," + ref2 + ":" + content + "}";
		
	}
}
