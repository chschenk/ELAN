package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.io.File;

import java.util.List;


/**
 * A subclass holding a reference to a toolbox typ file and some flags for the
 * Toolbox Parser.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ToolboxDecoderInfo2 extends ToolboxDecoderInfo {
    private ToolboxTypFile toolboxTypFile;
    private boolean recalculateForCharBytes = true;
    private boolean scrubAnnotations = false;

    /**
     * Creates a new ToolboxDecoderInfo2 instance
     */
    public ToolboxDecoderInfo2() {
        super();
    }

    /**
     * Creates a new ToolboxDecoderInfo2 instance
     *
     * @param shoeboxFilePath the path to a Toolbox file
     */
    public ToolboxDecoderInfo2(String shoeboxFilePath) {
        super(shoeboxFilePath);
    }

    /**
     * @see mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo#setShoeboxMarkers(java.util.List)
     */
    @Override
	public void setShoeboxMarkers(List<MarkerRecord> shoeboxMarkers) {
        super.setShoeboxMarkers(shoeboxMarkers);

        if ((shoeboxMarkers == null) || (shoeboxMarkers.size() == 0)) {
            throw new IllegalArgumentException("No markers specified");
        }

        toolboxTypFile = new ToolboxTypFile(shoeboxMarkers);
    }

    /**
     * @see mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo#setTypeFile(java.lang.String)
     */
    @Override
	public void setTypeFile(String typeFile) {
        super.setTypeFile(typeFile);

        if (typeFile == null) {
            throw new IllegalArgumentException("No .typ file specified");
        }

        File tpFile = new File(typeFile);
        toolboxTypFile = new ToolboxTypFile(tpFile);
    }

    /**
     * Returns the Toolbox typ file object
     *
     * @return the toolboxTypFile
     */
    public ToolboxTypFile getToolboxTypFile() {
        return toolboxTypFile;
    }

    /**
     * Returns whether the alignment should be recalculated based on the number
     * of bytes per character. Is user definable
     *
     * @return the recalculateForCharBytes flag
     */
    public boolean isRecalculateForCharBytes() {
        return recalculateForCharBytes;
    }

    /**
     * Sets whether the alignment should be recalculated based on the number of
     * bytes per character.
     *
     * @param recalculateForCharBytes if true indices of individual words are
     *        recalculated based on the number of bytes per character
     */
    public void setRecalculateForCharBytes(boolean recalculateForCharBytes) {
        this.recalculateForCharBytes = recalculateForCharBytes;
    }

    /**
     * If true the import function should remove sequences of multiple white spaces
     * within annotations (after splitting an interlinear line).
     * 
     * @return the scrub flag
     */
	public boolean isScrubAnnotations() {
		return scrubAnnotations;
	}

    /**
     * If set to true the import function should remove sequences of multiple white spaces
     * within annotations (after splitting an interlinear line).
     * 
     * @param scrubAnnotations the scrub flag
     */
	public void setScrubAnnotations(boolean scrubAnnotations) {
		this.scrubAnnotations = scrubAnnotations;
	}
}
