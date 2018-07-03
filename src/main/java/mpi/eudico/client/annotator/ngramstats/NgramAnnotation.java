package mpi.eudico.client.annotator.ngramstats;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * Stores the data from an {@link #AbstractAnnotation} so we can mangle it internally
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramAnnotation {
	protected final String value;
	protected final Long endTime;
	protected long beginTime; // not final as we might need to fix it!
	
	// stores the interval before/after this annotation
	protected boolean hasBeforeInterval = false, hasAfterInterval = false;
	protected long beforeInterval, afterInterval;

	public NgramAnnotation(AbstractAnnotation ann) {
		value = ann.getValue();
		beginTime = ann.getBeginTimeBoundary();
		endTime = ann.getEndTimeBoundary();
	}

	@Override
	public String toString() {
		StringBuilder rv = new StringBuilder();
		rv.append("Annotation(" + value + ")[" + this.hashCode() + "]");
		rv.append("\n\t beginTime= " + beginTime);
		rv.append("\n\t endTime= " + endTime);
		rv.append("\n\t beforeInterval= " + ( hasBeforeInterval ? beforeInterval : "NONE" ));
		rv.append("\n\t afterInterval= " + ( hasAfterInterval ? afterInterval : "NONE" ));
		return rv.toString();
	}
}