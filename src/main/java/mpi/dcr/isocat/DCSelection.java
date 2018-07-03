package mpi.dcr.isocat;

import mpi.dcr.DCSmall;

import java.util.ArrayList;
import java.util.List;


/**
 * A class that represents a simplified DC selection, mainly consisting of a
 * list of DCSmall objects (summaries of Data Categories).
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DCSelection {
    private String name;
    private List<DCSmall> dataCategories;

    /**
     * Creates a new DCSelection instance
     */
    public DCSelection() {
        name = "unknown";
        dataCategories = new ArrayList<DCSmall>();
    }

    /**
     * Creates a new DCSelection instance with specified name.
     *
     * @param name the name of the selection
     */
    public DCSelection(String name) {
        this.name = name;
        dataCategories = new ArrayList<DCSmall>();
    }

    /**
     * Returns a list of data categories.
     *
     * @return the list of data categories
     */
    public List<DCSmall> getDataCategories() {
        return dataCategories;
    }

    /**
     * Sets the list containing data categories.
     *
     * @param dataCategories the new list with data categories
     */
    public void setDataCategories(List<DCSmall> dataCategories) {
        this.dataCategories = dataCategories;
    }

    /**
     * Returns the name of the selection
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of selection
     *
     * @param name the name of the selection
     */
    public void setName(String name) {
        this.name = name;
    }
}
