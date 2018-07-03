package mpi.eudico.server.corpora.clom;

/**
 * Search results may arise from outside the ACM model.
 * For this purpose it is important to have a "minimalist" annotation,
 * containing only time information and value, but no hierachical structure
 * 
 * Created on Jul 23, 2004
 * @author Alexander Klassmann
 * @version Jul 23, 2004
 */
public interface AnnotationCore {
	/**
	 * <p>MK:02/06/18<br>
	 * Only subclass AlignableAnnotation  (ALA) has a getBegin() method, 
	 * which returns the begin time in milliseconds.
	 * RefAnnotions (REA) don't have a begin time. 
	 * REA still need something like
	 * a begin time, because they have to drawn somewhere on screen. 
	 * This time is not the begin time but the begin time boundary.
	 * Note 1:<br>
	 * You cannot always use the begin time 
	 * of the parent/root AlignableAnnotation. Proof: two 
	 * REA have the same parent ALA,
	 * the second REA starts in the middle of the ALA. <br>
	 * Note 2:<br>
	 * For an ALA, which begin timeslot is timealigned, 
	 * getBegin() and getBeginTimeBoundary() are identical.
	 * </p>
	 * 
	 * */	
	public long getBeginTimeBoundary();
	/**
	 * <p>MK:02/06/18<br>
	 * see getBeginTimeBoundary().
	 * </p>
	 * 
	 * */	
	public long getEndTimeBoundary();
	public String getValue();

}
