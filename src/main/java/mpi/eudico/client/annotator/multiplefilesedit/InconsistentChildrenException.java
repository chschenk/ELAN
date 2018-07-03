package mpi.eudico.client.annotator.multiplefilesedit;

public class InconsistentChildrenException extends Exception {
	private static final long serialVersionUID = -2374884117873928484L;
	private String parent;
	private String child;
	private String loadedParents;
	
	public InconsistentChildrenException(String parent, String child) {
		this.parent = parent;
		this.child = child;
	}
	
	public InconsistentChildrenException(String parent, String child, String loadedParents) {
		this(parent, child);
		this.loadedParents = loadedParents;
	}
	
	public String getParent() {
		return parent;
	}
	
	public String getChild() {
		return child;
	}

	public String getLoadedParents() {
		return loadedParents;
	}
}
