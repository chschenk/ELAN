package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.util.List;

import mpi.eudico.server.corpora.clom.DecoderInfo;

/**
 * A decoderinfo object for Toolbox/Shoebox files.
 */
public class ToolboxDecoderInfo implements DecoderInfo {
    public static final int DEFAULT_BLOCK_DURATION = 1000;

    private boolean timeInRefMarker;
    private boolean allUnicode;
    private long blockDuration;
    private String shoeboxFilePath;
    private String typeFile = "";
    private List<MarkerRecord> shoeboxMarkers;
    
    /**
     * Creates a new ToolboxDecoderInfo object
     */
    public ToolboxDecoderInfo() {
        
    }
    
    /**
     * Creates a new ToolboxDecoderInfo object
     * @param shoeboxFilePath the path to the Toolbox/Shoebox file
     */
    public ToolboxDecoderInfo(String shoeboxFilePath) {
        this.shoeboxFilePath = shoeboxFilePath;
    }
    
    /**
     * Returns the block duration
     * 
     * @return the block duration.
     */
    public long getBlockDuration() {
        return blockDuration;
    }
    
    /**
     * Sets the preferred/default block duration.
     * 
     * @param blockDuration the blockDuration to set.
     */
    public void setBlockDuration(long blockDuration) {
        this.blockDuration = blockDuration;
    }
    
    /**
     * Returns the path to the Shoebox file.
     * 
     * @return Returns the path to the Shoebox file
     */
    @Override
	public String getSourceFilePath() {
        return shoeboxFilePath;
    }
    
    /**
     * Sets the path to the Shoebox/Toolbox file.
     * 
     * @param shoeboxFilePath the path to the Shoebox/Toolbox file
     */
    public void setSourceFilePath(String shoeboxFilePath) {
        this.shoeboxFilePath = shoeboxFilePath;
    }
    
    /**
     * Returns whether or not the begintime of a marker unit/annotation should be extracted from 
     * the record marker.
     * 
     * @return the time in ref marker flag
     */
    public boolean isTimeInRefMarker() {
        return timeInRefMarker;
    }
    
    /**
     * Sets whether or not the begintime of a marker unit/annotation should be extracted from 
     * the record marker.
     * 
     * @param timeInRefMarker the new time in ref marker flag
     */
    public void setTimeInRefMarker(boolean timeInRefMarker) {
        this.timeInRefMarker = timeInRefMarker;
    }
    
    /**
     * Returns the Shoebox Markers.
     * 
     * @return Returns the Shoebox Markers.
     */
    public List<MarkerRecord> getShoeboxMarkers() {
        return shoeboxMarkers;
    }
    
    /**
     * Sets the ShoeboxMarkers.
     * 
     * @param shoeboxMarkers The Shoebox Markers to set.
     */
    public void setShoeboxMarkers(List<MarkerRecord> shoeboxMarkers) {
        this.shoeboxMarkers = shoeboxMarkers;
    }
    
    /**
     * Returns the path to the .typ file.
     * 
     * @return Returns the .typ File.
     */
    public String getTypeFile() {
        return typeFile;
    }
    
    /**
     * Sets the path to the .typ file.
     * 
     * @param typeFile The typeFile to set.
     */
    public void setTypeFile(String typeFile) {
        this.typeFile = typeFile;
    }
    
    /**
     * Returns whether all marker fields in the Toolbox file should be considered as Unicode, 
     * instead of the default assumption of ISO Latin.
     * @return Returns the allUnicode value.
     */
    public boolean isAllUnicode() {
        return allUnicode;
    }
    
    /**
     * Sets whether all markers should be treated as Unicode markers.
     * @param allUnicode if true all fields should be parsed as Unicode fields 
     */
    public void setAllUnicode(boolean allUnicode) {
        this.allUnicode = allUnicode;
    }
}
