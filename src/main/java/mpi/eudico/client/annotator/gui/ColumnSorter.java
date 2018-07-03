package mpi.eudico.client.annotator.gui;

import java.util.Comparator;
import java.util.Vector;

/**
 * 
 * @author alekoe
 * 
 * a helper class for sorting columns of a table
 */
class ColumnSorter implements Comparator<Vector<Object>> {
	  int colIndex;

	  ColumnSorter(int colIndex) {
	    this.colIndex = colIndex;
	  }

	  /**
	   * a method to compare two input vectors for sorting purposes
	   * 
	   * @param a first input vector
	   * @param b second input vector
	   * 
	   * @return 	less than 0 if a should go before b
	   * 			more than 0 if b should go before a
	   * 			0 if both are equal and order between them doesn't matter 
	   */
	  @Override
	public int compare(Vector<Object> v1, Vector<Object> v2) {
	    Object o1 = v1.get(colIndex);
	    Object o2 = v2.get(colIndex);

	    if (o1 instanceof String && ((String) o1).length() == 0) {
	      o1 = null;
	    }
	    if (o2 instanceof String && ((String) o2).length() == 0) {
	      o2 = null;
	    }

	    if (o1 == null && o2 == null) {
	      return 0;
	    } else if (o1 == null) {
	      return 1;
	    } else if (o2 == null) {
	      return -1;
	    } else if (o1 instanceof Comparable) {

	      return ((Comparable) o1).compareTo(o2);
	    } else {

	      return o1.toString().compareTo(o2.toString());
	    }
	  }
	}