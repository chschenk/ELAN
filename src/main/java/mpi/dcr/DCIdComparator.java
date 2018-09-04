package mpi.dcr;

import java.util.Comparator;


/**
 * Compares two DCSmall objects! by comparing the idAsInteger fields.
 *
 * @author Han Sloetjes
 */
public class DCIdComparator implements Comparator {
    /**
     * Creates a new DCIdComparator instance
     */
    public DCIdComparator() {
        super();
    }

    /**
     * Compares to objects containing a summary of the information on a data
     * category. To do: check class (ClassCastException), check nulls
     *
     * @param o1 the first DCSmall object to compare
     * @param o2 the second DCSmall object to compare
     *
     * @return -1 if the id of the first object is less than the id of the
     *         second object
     */
    @Override
	public int compare(Object o1, Object o2) {
        DCSmall dc1 = (DCSmall) o1;
        DCSmall dc2 = (DCSmall) o2;

        return dc1.getIdAsInteger().compareTo(dc2.getIdAsInteger());
    }
}
