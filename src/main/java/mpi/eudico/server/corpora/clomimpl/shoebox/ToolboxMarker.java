package mpi.eudico.server.corpora.clomimpl.shoebox;

/**
 * A class for storing information about a Toolbox marker.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ToolboxMarker {
    private String marker = "";
    private String parent = null;
    private boolean isSubdivision = false;

    //private boolean isRecordMarker = false;
    private boolean isInterlinearRoot = false;
    private int stereoType = -1;

    /**
     * Creates a new ToolboxMarker instance
     *
     * @param name name of the marker
     */
    public ToolboxMarker(String name) {
        marker = name;
    }

    /**
     * Returns the marker name.
     *
     * @return the marker
     */
    public String getMarker() {
        return marker;
    }

    /**
     * Sets the marker name
     *
     * @param marker the marker to set
     */
    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * Returns the name of the parent marker
     *
     * @return the parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the name of parent marker.
     *
     * @param parent the parent to set
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * Returns whether this marker is one the subdivision types.
     *
     * @return true if this marker is a subdivision type
     */
    public boolean isSubdivision() {
        return isSubdivision;
    }

    /**
     * Sets whether this marker is one the subdivision types.
     *
     * @param isSubdivision the subdivision flag
     */
    public void setSubdivision(boolean isSubdivision) {
        this.isSubdivision = isSubdivision;
    }

    /**
     * Returns whether this marker is the top marker of a group  of
     * interlinearized markers.
     *
     * @return true if is this the first marker of a group of interlinearized
     *         markers
     */
    public boolean isInterlinearRoot() {
        return isInterlinearRoot;
    }

    /**
     * Sets whether this marker is the top marker of a group  of
     * interlinearized markers.
     *
     * @param isInterlinearRoot if true this is the topmarker of a group of
     *        interlinearized markers
     */
    public void setInterlinearRoot(boolean isInterlinearRoot) {
        this.isInterlinearRoot = isInterlinearRoot;
    }

    /**
     * Returns the ELAN stereotype (the stereotype of the constraint object of
     * a linguistic type.
     *
     * @return the ELAN stereotype, one of the constants of  {@link
     *         mpi.eudico.server.corpora.clomimpl.type.Constraint}
     */
    public int getStereoType() {
        return stereoType;
    }

    /**
     * Sets the ELAN stereotype
     *
     * @param stereoType the stereotype constant to set, one of the constants
     *        of  {@link mpi.eudico.server.corpora.clomimpl.type.Constraint}
     */
    public void setStereoType(int stereoType) {
        this.stereoType = stereoType;
    }
}
