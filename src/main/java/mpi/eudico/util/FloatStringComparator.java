package mpi.eudico.util;

import java.util.Comparator;
/**
 * A Comparator for float values as String objects. 
 * The natural String ordering yields inconsistent sorting for float 
 * values in a String.
 * Null or NaN values will be at the end of a sorted list.
 * 
 * @author Han Sloetjes
 */
public class FloatStringComparator<T> implements Comparator<T> {

	public FloatStringComparator() {
		super();
	}

	/**
	 * Converts two strings to Floats and compares the Floats.
	 * Values that cannot be converted to Floats are "greater than" Strings that can,
	 * so that they will be placed at the end of an ordered list. 
	 * 
	 * @throws a ClassCastException if one of the objects is not a string
	 */
	@Override
	public int compare(T o1, T o2) {
		if (!(o1 instanceof String)) {
			throw new  ClassCastException("The first object is not a String");
		}
		if (!(o2 instanceof String)) {
			throw new  ClassCastException("The second object is not a String");
		}
		Float f1 = null;
		Float f2 = null;
		try {
			f1 = Float.parseFloat((String) o1);
		} catch (NumberFormatException nfe){}
		
		try {
			f2 = Float.parseFloat((String) o2);
		} catch (NumberFormatException nfe){}
		
		if (f1 != null && f2 != null) {
			return Float.compare(f1, f2);
		} else if (f1 != null) {// f2 = null, null values at the end of the list
			return -1;
		} else if (f2 != null) {// f1 = null, null values at the end of the list
			return 1;
		}
		
		return 0;
	}

}
