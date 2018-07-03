package mpi.eudico.server.corpora.clomimpl.delimitedtext;

import mpi.eudico.server.corpora.clom.DecoderInfo;

import java.util.Map;


/**
 * A decoder information object for import of comma separated or tab delimited
 * files.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DelimitedTextDecoderInfo implements DecoderInfo {
    private String filePath;
    private int firstRowIndex = 0;
    private int numColumns = 1;
    private String delimiter = "\t";
    private int[] includedColumns;
    private long defaultDuration = 1000;
    private int beginTimeColumn = -1;
    private int endTimeColumn = -1;
    private int durationColumn = -1;
    private int[] annotationColumns;
    private String[] includedColumnsNames;
    private boolean singleAnnotationPerRow = true;
    private boolean skipEmptyCells = false;

    /**
     * the index of the (single) column containing the tier name in the row
     * cell
     */
    private int tierColumnIndex = -1;

    /** a column index - tier name mapping, tier name is the header */
    private Map<Integer, String> columnsWithTierNames;

    /**
     * Creates a new DelimitedTextDecoderInfo instance
     */
    public DelimitedTextDecoderInfo() {
        super();
    }

    /**
     * Creates a new DelimitedTextDecoderInfo instance
     *
     * @param filePath the path to the file to decode
     */
    public DelimitedTextDecoderInfo(String filePath) {
        super();
        this.filePath = filePath;
    }

    /**
     * Returns the file path of the file to decode.
     *
     * @return the file path of the file to decode
     */
    @Override
	public String getSourceFilePath() {
        return filePath;
    }

    /**
     * Sets the file path of the file to decode.
     *
     * @param filePath the file path of the file to decode
     */
    public void setSourceFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the annotationColumns.
     */
    public int[] getAnnotationColumns() {
        return annotationColumns;
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotationColumns The annotationColumns to set.
     */
    public void setAnnotationColumns(int[] annotationColumns) {
        this.annotationColumns = annotationColumns;
    }

    /**
     * Returns the index of the annotation begin time column.
     *
     * @return the index of beginTimeColumn.
     */
    public int getBeginTimeColumn() {
        return beginTimeColumn;
    }

    /**
     * Sets the index of the annotation begin time column.
     *
     * @param beginTimeColumn the index of the annotation begin time column.
     */
    public void setBeginTimeColumn(int beginTimeColumn) {
        this.beginTimeColumn = beginTimeColumn;
    }

    /**
     * Returns a map with column index to tier name mappings.
     *
     * @return column index to tier name mappings
     */
    public Map<Integer, String> getColumnsWithTierNames() {
        return columnsWithTierNames;
    }

    /**
     * Sets the map with column index to tier name mappings.
     *
     * @param columnsWithTierNames column index to tier name mappings
     */
    public void setColumnsWithTierNames(Map<Integer, String> columnsWithTierNames) {
        this.columnsWithTierNames = columnsWithTierNames;
    }

    /**
     * Returns the default duration of annotations.
     *
     * @return the default duration of annotations
     */
    public long getDefaultDuration() {
        return defaultDuration;
    }

    /**
     * Sets the default duration of annotations.
     *
     * @param defaultDuration the default duration of annotations
     */
    public void setDefaultDuration(long defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    /**
     * Returns the delimiter as a string.
     *
     * @return Returns the delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the delimiter string.
     *
     * @param delimiter The delimiter to set.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Returns the column index with the duration of an annotation
     *
     * @return Returns the index of duration column.
     */
    public int getDurationColumn() {
        return durationColumn;
    }

    /**
     * Sets the index of the duration column
     *
     * @param durationColumn The index of the duration column
     */
    public void setDurationColumn(int durationColumn) {
        this.durationColumn = durationColumn;
    }

    /**
     * Returns the index of the end time column.
     *
     * @return the index of the end time column
     */
    public int getEndTimeColumn() {
        return endTimeColumn;
    }

    /**
     * Sets the index of the end time column.
     *
     * @param endTimeColumn the index of the end time column
     */
    public void setEndTimeColumn(int endTimeColumn) {
        this.endTimeColumn = endTimeColumn;
    }

    /**
     * Returns the index of the first row with actual data (in case of header
     * lines).
     *
     * @return the index of the first row
     */
    public int getFirstRowIndex() {
        return firstRowIndex;
    }

    /**
     * Sets the index of the first row with data
     *
     * @param firstRowIndex the index of the first row
     */
    public void setFirstRowIndex(int firstRowIndex) {
        this.firstRowIndex = firstRowIndex;
    }

    /**
     * Returns an array holding the column indices to read from the file.
     *
     * @return an array of column indices
     *
     * @see #getIncludedColumnsNames()
     */
    public int[] getIncludedColumns() {
        return includedColumns;
    }

    /**
     * Sets the array of column indices to include in the import
     *
     * @param includedColumns the array of column indices tp include
     *
     * @see #setIncludedColumnsNames(String[])
     */
    public void setIncludedColumns(int[] includedColumns) {
        this.includedColumns = includedColumns;
    }

    /**
     * Returns the names/headers of the columns to include in the import.
     *
     * @return the names/headers of the columns
     *
     * @see #getIncludedColumns(int[])
     */
    public String[] getIncludedColumnsNames() {
        return includedColumnsNames;
    }

    /**
     * Sets the array of column names/headers.
     *
     * @param includedColumnsNames the array of column names/headers
     *
     * @see #setIncludedColumns(int[])
     */
    public void setIncludedColumnsNames(String[] includedColumnsNames) {
        this.includedColumnsNames = includedColumnsNames;
    }

    /**
     * Returns the total number of columns.
     *
     * @return total number of columns.
     */
    public int getNumColumns() {
        return numColumns;
    }

    /**
     * Sets the total number of columns
     *
     * @param numColumns he total number of columns
     */
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    /**
     * Returns wether there is only a single annotation on each row or
     * multiple.
     *
     * @return Returns if true there is only one annotation column, otherwise
     *         there are mutiple annotations/tier columns
     */
    public boolean isSingleAnnotationPerRow() {
        return singleAnnotationPerRow;
    }

    /**
     * Sets the flag whether there is only a single annotation on each row
     *
     * @param singleAnnotationPerRow the single annotation flag
     */
    public void setSingleAnnotationPerRow(boolean singleAnnotationPerRow) {
        this.singleAnnotationPerRow = singleAnnotationPerRow;
    }

    /**
     * The column containing the tier name(s) in case of
     * single-annotation-per-row mode.
     *
     * @return Returns the tierColumn index
     */
    public int getTierColumnIndex() {
        return tierColumnIndex;
    }

    /**
     * Sets the index of the column containing the single tier/annotation.
     *
     * @param tierColumnIndex the column index of the single tier and
     *        annotations.
     */
    public void setTierColumnIndex(int tierColumnIndex) {
        this.tierColumnIndex = tierColumnIndex;
    }

    /**
     * Whether empty cells in the spreadsheet should be skipped or not.
     * 
     * @return if true no annotations are created for empty cells
     */
	public boolean isSkipEmptyCells() {
		return skipEmptyCells;
	}

	/**
	 * Sets whether empty cells should be skipped.
	 * 
	 * @param skipEmptyCells if true no annotations are created for empty cells
	 */
	public void setSkipEmptyCells(boolean skipEmptyCells) {
		this.skipEmptyCells = skipEmptyCells;
	}
}
