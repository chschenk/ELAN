package mpi.eudico.server.corpora.clomimpl.shoebox;

/**
 * Interface for a Toolbox line, consisting of a marker name and a line (that
 * can either be simple or complex).
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public interface ToolboxLine {
    /**
     * Returns the parent Toolbox line
     *
     * @return the parent line
     */
    public ToolboxLine getParent();

    /**
     * Sets the parent ToolboxLine for this line.
     *
     * @param toolboxLine the parent
     */
    public void setParent(ToolboxLine toolboxLine);

    /**
     * Returns the marker name of the ToolboxLine. Equivalent (more or less) to
     * an ELAN tier name
     *
     * @return the marker name
     */
    public String getMarkerName();

    /**
     * Appends a line to the ToolboxLine. Used in case of Toolbox records with
     * wrapped marker lines.
     *
     * @param appLine the line to append to the current line
     */
    public void appendLine(String appLine);
}
