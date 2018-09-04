package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import java.io.File;
import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * An encoder class for export to tab delimited text.
 * 
 * Follows other encoder classes that have private fields 
 * with getters and setters.
 * 
 * Options that could be added in the future: other delimiters (e.g. ; or ,),
 * text values in quotes (""), new line character(s) (\n, \r\n), writing BOM etc.
 *  
 */
public abstract class DelimitedTextEncoderInfo implements EncoderInfo {
	/** the list of tiers to export */
	private List<String> tierNames;
	/** the file to export to */
	private File exportFile;
	/** the encoding to use, defaults to UTF-8 */
	private String charEncoding          = "UTF-8";
	/** begin time if only part of the annotations have to be exported */
	private long beginTime               = 0L; 
	/** end time if only part of the annotations have to be exported */
	private long endTime                 = Long.MAX_VALUE;
	/** flags whether the time values should be corrected based on the offset of the master media */
	private boolean addMasterMediaOffset = false;
	/** flags whether the cv description should be included in the output*/
	private boolean includeCVDescription = false;
	/** flags whether the begin time should be included in the output */
	private boolean includeBeginTime     = true;
	/** flags whether the end time should be included in the output */
	private boolean includeEndTime       = true;
	/** flags whether the duration should be included in the output */
	private boolean includeDuration      = true;
	/** flags whether the time should be formatted as hh:mm:ss.ms */
	private boolean includeHHMM          = true;
	/** flags whether the time should be formatted as ss.ms */
	private boolean includeSSMS          = true;
	/** flags whether the time should be in milliseconds */
	private boolean includeMS            = false;
	/** flags whether the time should be in SMPTE code */
	private boolean includeSMPTE         = false;
	/** flags whether the time should be in PAL code */
	private boolean palFormat            = false;
	/** flags whether the time should be in PAL-50fps code */
	private boolean pal50Format            = false;
	/** flags whether the tier names should be included */
	private boolean includeNames         = true;
	/** flags whether the participant attribute should be included */
	private boolean includeParticipants  = true;
	/** if true the annotation id is exported with the value */
	private boolean includeAnnotationId  = false;
	
	// fields and defaults for the case of separate column per tier export
	//private boolean separateColumnPerTier = false;
	/**
     * flags whether values of annotations spanning other annotations should be
     * repeated
     */
    private boolean repeatValues  = true;
    /**
     * flags whether annotations of different "blocks" should be combined in
     * the same row  and if values should be repeated
     */
    private boolean combineBlocks = true;
    /** the default assumption is that the export is to a tab delimited .txt file,
     * when the format is .csv all text fields need to be enclosed in "" etc. */
    private boolean exportCSVFormat = false;
	
	public List<String> getTierNames() {
		return tierNames;
	}
	public void setTierNames(List<String> tierNames) {
		this.tierNames = tierNames;
	}
	public File getExportFile() {
		return exportFile;
	}
	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}
	public String getCharEncoding() {
		return charEncoding;
	}
	public void setCharEncoding(String charEncoding) {
		this.charEncoding = charEncoding;
	}
	public long getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public boolean isAddMasterMediaOffset() {
		return addMasterMediaOffset;
	}
	public void setAddMasterMediaOffset(boolean addMasterMediaOffset) {
		this.addMasterMediaOffset = addMasterMediaOffset;
	}
	public boolean isIncludeCVDescription() {
		return includeCVDescription;
	}
	public void setIncludeCVDescription(boolean includeCVDescription) {
		this.includeCVDescription = includeCVDescription;
	}
	public boolean isIncludeBeginTime() {
		return includeBeginTime;
	}
	public void setIncludeBeginTime(boolean includeBeginTime) {
		this.includeBeginTime = includeBeginTime;
	}
	public boolean isIncludeEndTime() {
		return includeEndTime;
	}
	public void setIncludeEndTime(boolean includeEndTime) {
		this.includeEndTime = includeEndTime;
	}
	public boolean isIncludeDuration() {
		return includeDuration;
	}
	public void setIncludeDuration(boolean includeDuration) {
		this.includeDuration = includeDuration;
	}
	public boolean isIncludeHHMM() {
		return includeHHMM;
	}
	public void setIncludeHHMM(boolean includeHHMM) {
		this.includeHHMM = includeHHMM;
	}
	public boolean isIncludeSSMS() {
		return includeSSMS;
	}
	public void setIncludeSSMS(boolean includeSSMS) {
		this.includeSSMS = includeSSMS;
	}
	public boolean isIncludeMS() {
		return includeMS;
	}
	public void setIncludeMS(boolean includeMS) {
		this.includeMS = includeMS;
	}
	public boolean isIncludeSMPTE() {
		return includeSMPTE;
	}
	public void setIncludeSMPTE(boolean includeSMPTE) {
		this.includeSMPTE = includeSMPTE;
	}
	public boolean isPalFormat() {
		return palFormat;
	}
	public void setPalFormat(boolean palFormat) {
		this.palFormat = palFormat;
	}
	public boolean isPal50Format() {
		return pal50Format;
	}
	public void setPal50Format(boolean pal50Format) {
		this.pal50Format = pal50Format;
	}
	public boolean isIncludeNames() {
		return includeNames;
	}
	public void setIncludeNames(boolean includeNames) {
		this.includeNames = includeNames;
	}
	public boolean isIncludeParticipants() {
		return includeParticipants;
	}
	public void setIncludeParticipants(boolean includeParticipants) {
		this.includeParticipants = includeParticipants;
	}
	
	// getters and setters for the column-per-tier export with repeating of values
	public boolean isRepeatValues() {
		return repeatValues;
	}
	public void setRepeatValues(boolean repeatValues) {
		this.repeatValues = repeatValues;
	}
	public boolean isCombineBlocks() {
		return combineBlocks;
	}
	public void setCombineBlocks(boolean combineBlocks) {
		this.combineBlocks = combineBlocks;
	}

	public boolean isIncludeAnnotationId() {
		return includeAnnotationId;
	}
	public void setIncludeAnnotationId(boolean includeAnnotationId) {
		this.includeAnnotationId = includeAnnotationId;
	}
	public boolean isExportCSVFormat() {
		return exportCSVFormat;
	}
	public void setExportCSVFormat(boolean exportCSVFormat) {
		this.exportCSVFormat = exportCSVFormat;
	}
	
}
