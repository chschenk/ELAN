package mpi.eudico.server.corpora.clomimpl.shoebox;

import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.server.corpora.clomimpl.shoebox.interlinear.Interlinearizer;


/**
 * Toolbox specific EncoderInfo implementation may 06 (HS): added boolean for
 * two export options:<br>
 * - whether or not to recalculate begin and end times of annotations based on
 * the master media offset<br>
 * dec 2006 (HS): added a boolean to indicate that all tiers should be
 * exported using UTF-8 encoding
 *
 * @author hennie
 */
public class ToolboxEncoderInfo implements EncoderInfo {
    /** export with .typ file */
    public static final int TYPFILE = 0;

    /** export with marker file */
    public static final int DEFINED_MARKERS = 1;

    /** export using tier names */
    public static final int TIERNAMES = 2;
    private int pageWidth;
    private int markerSource;
    private int timeFormat;

    // may 2006: add db type to the encoder object instead of a static field in ShoeboxTypFile
    private String databaseType = "";

    // may 2006: add markers to the encoder object instead of a static field in ShoeboxTypFile
    private List<MarkerRecord> markers;
    private boolean correctAnnotationTimes = true;

    // flag to indicate that all markers are in Unicode (UTF-8)
    private boolean allUnicode = false;

    // oct 2007 new fields for extra options for (utf-8 only) Toolbox export
    private boolean wrapLines = false;
    private int lineWrapStyle;
    private boolean includeEmptyMarkers = true;
    private String recordMarker;
    private List<String> orderedVisibleTiers;
    private List<String> markersWithBlankLines;
    private long timeOffset = 0L;
    // media marker
    private boolean includeMediaMarker = false;
    private String mediaMarker;
    private String mediaFileName;

    /**
     * Creates a new ToolboxEncoderInfo instance
     *
     * @param pageWidth the pagewidth (in number of characters)
     * @param markerSource the type of marker source (.typ file, marker files
     *        etc)
     */
    public ToolboxEncoderInfo(int pageWidth, int markerSource) {
        this(pageWidth, markerSource, Interlinearizer.SSMS);
    }

    /**
     * Creates a new ToolboxEncoderInfo instance
     *
     * @param pageWidth the pagewidth (in number of characters)
     * @param markerSource the type of marker source (.typ file, marker files
     *        etc)
     * @param timeFormat the format of time values
     */
    public ToolboxEncoderInfo(int pageWidth, int markerSource, int timeFormat) {
        this.pageWidth = pageWidth;
        this.markerSource = markerSource;
        this.timeFormat = timeFormat;
    }

    /**
     * Returns the page width (number of characters)
     *
     * @return the page width
     */
    public int getPageWidth() {
        return pageWidth;
    }

    /**
     * Returns the marker source type.
     *
     * @return the marker source type
     */
    public int getMarkerSource() {
        return markerSource;
    }

    /**
     * Returns the time format value.
     *
     * @return the time format
     */
    public int getTimeFormat() {
        return timeFormat;
    }

    /**
     * Returns the flag whether annotation time values should be recalculated.
     *
     * @return the correct time values flag
     */
    public boolean getCorrectAnnotationTimes() {
        return correctAnnotationTimes;
    }

    /**
     * Sets whether the time values should be recalculated.
     *
     * @param correctAnnotationTimes if true the master media offset is added
     *        to all time values
     */
    public void setCorrectAnnotationTimes(boolean correctAnnotationTimes) {
        this.correctAnnotationTimes = correctAnnotationTimes;
    }

    /**
     * Returns the database type for the Toolbox header
     *
     * @return Returns the databaseType.
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * Sets the database type for the Toolbox header
     *
     * @param databaseType The databaseType to set.
     */
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    /**
     * Returns the list of marker objects
     *
     * @return Returns the markers.
     */
    public List<MarkerRecord> getMarkers() {
        return markers;
    }

    /**
     * Sets the list of marker objects.
     *
     * @param markers The markers to set.
     */
    public void setMarkers(List<MarkerRecord> markers) {
        this.markers = markers;
    }

    /**
     * Returns whether all annotations are to be exported in Unicode
     *
     * @return whether all annotations are to be exported in Unicode
     */
    public boolean isAllUnicode() {
        return allUnicode;
    }

    /**
     * Sets whether all annotations are to be exported in Unicode.
     *
     * @param allUnicode if true all tiers are exported with UTF-8 encoding,
     *        otherwise they  will be encoded in ISO-Latin or a mixture of
     *        encodings
     */
    public void setAllUnicode(boolean allUnicode) {
        this.allUnicode = allUnicode;
    }

    /**
     * Returns whether or not empty markers should be part of the output or be
     * skipped
     *
     * @return the includeEmptyMarkers flag
     */
    public boolean isIncludeEmptyMarkers() {
        return includeEmptyMarkers;
    }

    /**
     * Sets whether empty markers should be included in the output.
     *
     * @param includeEmptyMarkers the includeEmptyMarkers to set
     */
    public void setIncludeEmptyMarkers(boolean includeEmptyMarkers) {
        this.includeEmptyMarkers = includeEmptyMarkers;
    }

    /**
     * Returns the line wrap style (next line or end of block)
     *
     * @return the lineWrapStyle
     */
    public int getLineWrapStyle() {
        return lineWrapStyle;
    }

    /**
     * Sets the line wrap style (next line or end of block)
     *
     * @param lineWrapStyle the lineWrapStyle to set
     */
    public void setLineWrapStyle(int lineWrapStyle) {
        this.lineWrapStyle = lineWrapStyle;
    }

    /**
     * Returns a list of markers that should be followed by a blank line
     *
     * @return the list of markers followed by a blank line
     */
    public List<String> getMarkersWithBlankLines() {
        return markersWithBlankLines;
    }

    /**
     * Sets the list of markers that should be followed by a blank line
     *
     * @param markersWithBlankLines the markersWithBlankLines to set
     */
    public void setMarkersWithBlankLines(List<String> markersWithBlankLines) {
        this.markersWithBlankLines = markersWithBlankLines;
    }

    /**
     * Returns the record marker.
     *
     * @return the recordMarker
     */
    public String getRecordMarker() {
        return recordMarker;
    }

    /**
     * Sets the record marker
     *
     * @param recordMarker the recordMarker to set
     */
    public void setRecordMarker(String recordMarker) {
        this.recordMarker = recordMarker;
    }

    /**
     * Returns whether or not line wrapping should be applied
     *
     * @return the wrapLines flag
     */
    public boolean isWrapLines() {
        return wrapLines;
    }

    /**
     * Sets the linewrapping flag.
     *
     * @param wrapLines the wrapLines to set
     */
    public void setWrapLines(boolean wrapLines) {
        this.wrapLines = wrapLines;
    }

    /**
     * Returns the (master media) time offset; the number of ms to add to all
     * time values.
     *
     * @return the media time offset
     */
    public long getTimeOffset() {
        return timeOffset;
    }

    /**
     * Sets the time offset, the number of ms to add to the time values of
     * annotations.
     *
     * @param timeOffset the time offset
     */
    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    /**
     * Returns the visible tiers, in the right order
     *
     * @return the visible tiers (names)
     */
    public List<String> getOrderedVisibleTiers() {
        return orderedVisibleTiers;
    }

    /**
     * Sets the visible tiers, in the right order
     *
     * @param orderedVisibleTiers the ordered visible tiers (names)
     */
    public void setOrderedVisibleTiers(List<String> orderedVisibleTiers) {
        this.orderedVisibleTiers = orderedVisibleTiers;
    }

    /**
     * Getters and setters for the media marker fields.
     */
	public boolean isIncludeMediaMarker() {
		return includeMediaMarker;
	}

	public void setIncludeMediaMarker(boolean includeMediaMarker) {
		this.includeMediaMarker = includeMediaMarker;
	}

	public String getMediaMarker() {
		return mediaMarker;
	}
	
	/**
	 * Sets the Toolbox marker for the media file that Toolbox can 
	 * use to play fragments.
	 * 
	 * @param mediaMarker
	 */
	public void setMediaMarker(String mediaMarker) {
		this.mediaMarker = mediaMarker;
		if (mediaMarker == null) {
			mediaMarker = "media";
		}
	}

	public String getMediaFileName() {
		return mediaFileName;
	}

	public void setMediaFileName(String mediaFileName) {
		this.mediaFileName = mediaFileName;
	}
}
