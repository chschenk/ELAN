package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.AnnotationCore;

/**
 * Basic implementation of an annotation core object, consisting of a value,
 * a begin time and an end time.
 * Is an immutable core object.
 * 
 * @author Han Sloetjes
 *
 */
public class AnnotationCoreImpl implements AnnotationCore {
	private long bt;
	private long et;
	private String value;
	
	
	/**
	 * Constructor initializing all fields of the class.
	 * 
	 * @param bt begin time
	 * @param et end time
	 * @param value the vale
	 */
	public AnnotationCoreImpl(String value, long bt, long et) {
		super();
		this.bt = bt;
		this.et = et;
		this.value = value;
	}

	@Override
	public long getBeginTimeBoundary() {
		return bt;
	}

	@Override
	public long getEndTimeBoundary() {
		return et;
	}

	@Override
	public String getValue() {
		return value;
	}

}
