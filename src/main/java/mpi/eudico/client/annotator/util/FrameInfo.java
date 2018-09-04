package mpi.eudico.client.annotator.util;

import javax.swing.JFrame;


/**
 * Stores information on currently opened frames.
 *
 * @author Han Sloetjes, MPI
 */
public class FrameInfo {
    private String frameId;
    private String frameName;
    private JFrame frame;
    private String filePath;

    /**
     * Creates a new FrameInfo instance
     *
     * @param frame the frame
     * @param frameId the id
     */
    public FrameInfo(JFrame frame, String frameId) {
        this.frame = frame;
        this.frameId = frameId;
    }

    /**
     * Returns the path to the file loaded in this frame
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the file path
     *
     * @param filePath the file path (url)
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns the frame
     *
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Sets the frame
     *
     * @param frame the frame
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Returns the frame id
     *
     * @return the frame id
     */
    public String getFrameId() {
        return frameId;
    }

    /**
     * Sets the frame id
     *
     * @param frameId the frame id
     */
    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    /**
     * Returns the frame name, which is not always the same as the title.
     * Normally it is the filename, for documents that not have been 
     * saved yet it is something like "Untitled-n"
     *
     * @return the frame name (for display in menu)
     */
    public String getFrameName() {
        return frameName;
    }

    /**
     * Sets the frame name
     *
     * @see #getFrameName()
     * @param frameName the frame name
     */
    public void setFrameName(String frameName) {
        this.frameName = frameName;
    }
}
