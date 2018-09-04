package mpi.dcr;

import java.util.Comparator;


/**
 * Compares two DCSmall objects! by comparing the identifier fields.
 *
 * @author Han Sloetjes
 */
public class DCIdentifierComparator implements Comparator {
    /**
     * Creates a new DCIdentifierComparator instance
     */
    public DCIdentifierComparator() {
        super();
    }

    /**
     * Compares two DCSmall objects! by comparing the identifier fields.
     * To do: check class (ClassCastException), check nulls
     *
     * @param o1 the first DCSmall object
     * @param o2 the second DCSmall object
     *
     * @return the comparison of the (string) identifiers of the data categories
     */
    @Override
	public int compare(Object o1, Object o2) {
        DCSmall dc1 = (DCSmall) o1;
        DCSmall dc2 = (DCSmall) o2;

        return dc1.getIdentifier().compareTo(dc2.getIdentifier());
    }
}
