package mpi.eudico.client.annotator.timeseries.glove;

public interface DataGloveConstants {
	// knowledge about the rows and columns in the samples
	// mandatory are 15 rows, optionally followed by 20 rows of finger coords
	
	/** number of columns for all possible 35 rows;
	 * the first 15 rows vary, all other rows have 4 columns */
	public final int[] COLS_PER_ROW = new int[] {3, 3, 3, 3, 3, 3, 
		4, 3, 3, 4, 6, 4, 6, 4, 6,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
		4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
	
	/**  the minimal number of rows per sample */
	public final int MIN_NUM_ROWS = 15;
	
	/** the maximum number of rows per sample */
	public final int MAX_NUM_ROWS = 35;
	
	// provide a matrix of descriptions per cell
}
