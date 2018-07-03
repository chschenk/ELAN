package mpi.eudico.server.corpora.clomimpl.shoebox;

/**
 * Holds a line of a marker that is not part of an interlinearized 
 * group of markers. Is one-to-one related to the record marker or
 * another SimpleToolboxLine.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class SimpleToolboxLine implements ToolboxLine {
    private ToolboxLine parent;
    private String marker = "";
    private String line = "";

    /**
     * Creates a new SimpleToolboxLine instance
     *
     * @param marker the marker
     * @param line the (first) line of this marker in a record
     */
    public SimpleToolboxLine(String marker, String line) {
        this.marker = marker;
        this.line = line;
    }

    /**
     * Returns the parent line.
     *
     * @return the parent
     */
    @Override
	public ToolboxLine getParent() {
        return parent;
    }

    /**
     * Sets the parent line
     *
     * @param parent the parent to set
     */
    @Override
	public void setParent(ToolboxLine parent) {
        this.parent = parent;
    }

    /**
     * Returns the marker name
     *
     * @return the marker name
     */
    @Override
	public String getMarkerName() {
        return marker;
    }

    /**
     * Appends a line, if the same marker has multiple lines in a record.
     *
     * @param appLine the line to append
     */
    @Override
	public void appendLine(String appLine) {
        if (appLine != null) {
            line = line + " " + appLine;
        }
    }

    /**
     * Returns the line.
     *
     * @return the line
     */
    public String getLine() {
        return line;
    }
}
