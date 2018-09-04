package mpi.eudico.client.annotator.recognizer.data;

/**
 * Data container for a segment boundary
 * 
 * @author albertr
 *
 */
public class Boundary implements Comparable {
	public long time;
	public String label;
	
	public Boundary(long time, String label) {
		this.time = time;
		this.label = label;
	}
	
	@Override
	public int compareTo(Object otherBoundary) {
		return (int)(time - ((Boundary) otherBoundary).time);
	}
}