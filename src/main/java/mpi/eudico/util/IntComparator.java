/**
 * 
 */
package mpi.eudico.util;

import java.util.Comparator;

/**
 * Comparator for Integer objects. 
 * (I would expect there was a standard class for comparing int's?)
 * 
 * @author Han Sloetjes
 */
public class IntComparator<T> implements Comparator<Integer> {

	/**
	 * Constructor.
	 */
	public IntComparator() {
		super();
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		if (o1 == null) {
			throw new NullPointerException("The first Integer objects is null");
		}
		if (o2 == null) {
			throw new NullPointerException("The second Integer objects is null");
		}
		return o1.compareTo(o2);
	}

}
