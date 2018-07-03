package mpi.search.content.query.model;

import mpi.search.content.result.model.ContentResult;

/**
 * Created on Oct 7, 2004
 * @author Alexander Klassmann
 * @version Oct 7, 2004
 */
public class RestrictedAnchorConstraint extends AnchorConstraint {
	final private ContentResult result;
	final private String comment;

	public RestrictedAnchorConstraint(ContentResult result, String comment) {
		super(result.getTierNames());
		this.result = result;
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof RestrictedAnchorConstraint ? super.equals(o) : false;
	}

	public ContentResult getResult() {
		return result;
	}

	@Override
	public boolean isEditable(){
		return false;
	}
	
	public String getComment() {
		return comment;
	}
}
