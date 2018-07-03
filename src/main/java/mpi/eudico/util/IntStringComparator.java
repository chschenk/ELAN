package mpi.eudico.util;

import java.util.Comparator;
/**
 * A Comparator for int values as String objects. 
 * The natural String ordering yields inconsistent sorting for int 
 * values in a String.
 * Null values or values that cannot be converted to an int will be at the 
 * end of a sorted list.
 * 
 * @author Han Sloetjes
 */
public class IntStringComparator<T> implements Comparator<T> {

	public IntStringComparator() {
		super();
	}

	/**
	 * Converts two strings to Integers and compares the Integers.
	 * Values that cannot be converted to Integer are "greater than" Strings that can,
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
		Integer in1 = null;
		Integer in2 = null;
		try {
			in1 = Integer.parseInt((String) o1);
		} catch (NumberFormatException nfe){}
		
		try {
			in2 = Integer.parseInt((String) o2);
		} catch (NumberFormatException nfe){}
		
		if (in1 != null && in2 != null) {
			return in1.compareTo(in2);
		} else if (in1 != null) {// in2 = null, null values at the end of the list
			return -1;
		} else if (in2 != null) {// in1 = null, null values at the end of the list
			return 1;
		}
		
		return 0;
	}

}
