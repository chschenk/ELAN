package mpi.eudico.util;

import java.util.Comparator;

/**
 * A comparator that makes sure that null, empty String and "-" values are
 * greater than other strings (at the end of a sorted list).
 * 
 * @author Han Sloetjes
 */
public class EmptyStringComparator implements Comparator<String> {
	public final String HYPH = "-";
	/**
	 * Constructor.
	 */
	public EmptyStringComparator() {
		super();
	}

	/**
	 * Uses the standard string compare except for null values, empty values
	 * and the value "-". 
	 */
	@Override
	public int compare(String o1, String o2) {
		if (o1 == null && o2 != null) {
			return 1;
		}
		if (o1 != null && o2 == null) {
			return -1;
		}
		if ((o1 != null && o1.equals(HYPH)) && (o2 != null && !o2.equals(HYPH))) {
			return 1;
		}
		if (o2 != null && o2.equals(HYPH) && (o1 != null && !o1.equals(HYPH))) {
			return -1;
		}
		if (o1 != null && o2 != null) {
			return o1.compareTo(o2);
		}
		return 0;
	}

}
