package mpi.eudico.client.annotator.timeseries.config;

/**
 * A class that stores the position(s) in a complex sample from which the data
 * of a track are read and, if multiple, calculated. The description field can
 * also be used for a description of the kind of calculation that has been
 * performed.(?)
 *
 * @author Han Sloetjes
 */
public class SamplePosition {
    private int[] rows;
    private int[] columns;
    private String description;

    /**
     * Creates a new SamplePosition instance
     * By default the row is 0 and the column is 0.
     */
    public SamplePosition() {
        rows = new int[] { 0 };
        columns = new int[] { 0 };
    }

    /**
     * Creates a new SamplePosition instance
     *
     * @param description a description
     */
    public SamplePosition(String description) {
        this();
        this.description = description;
    }

    /**
     * Creates a new SamplePosition instance
     *
     * @param rows the row index of indices, zero based
     * @param columns the column index or indices, zero based
     */
    public SamplePosition(int[] rows, int[] columns) {
        if ((rows == null) || (columns == null)) {
            throw new NullPointerException();
        }

        if (rows.length != columns.length) {
            throw new IllegalArgumentException(
                "The rows and columns arrays should have the same length.");
        }

        this.rows = rows;
        this.columns = columns;
    }

    /**
     * Creates a new SamplePosition instance
     *
     * @param rows the row index of indices, zero based
     * @param columns the column index or indices, zero based
     * @param description a description
     */
    public SamplePosition(int[] rows, int[] columns, String description) {
        this(rows, columns);
        this.description = description;
    }

    /**
     * Returns the row indices.
     *
     * @return the row indices
     */
    public int[] getRows() {
        return rows;
    }

    /**
     * Returns the column indices.
     *
     * @return the column indices
     */
    public int[] getColumns() {
        return columns;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param string the description
     */
    public void setDescription(String string) {
        description = string;
    }
}
