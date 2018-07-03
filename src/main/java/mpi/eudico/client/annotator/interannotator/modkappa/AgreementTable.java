package mpi.eudico.client.annotator.interannotator.modkappa;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.interannotator.CompareConstants;

public class AgreementTable {
	protected int [][] table;
	protected String idLabel;
	protected List<String> scoreLabels;
	
	/**
	 * Initializes a two dimensional int array with the number of rows and columns 
	 * equal to the number of labels in the label list.
	 * After creation the list and the sizes of the table are assumed to be immutable.
	 *    
	 * @param idLabel an identifier for the table, can be used for the score value in case of a 
	 * 2 x 2 yes/no agreement table
	 * @param labels the list of labels or codes
	 */
	public AgreementTable(String idLabel, List<String> labels) {		
		if (labels == null) {
			throw new NullPointerException("No labels provided for agreement table.");
		}
		this.idLabel = idLabel;
		scoreLabels = new ArrayList<String>(labels);

		int numLabels = scoreLabels.size();
		table = new int[numLabels][numLabels];
	}

	/**
	 * Package private constructor for classes that extend this class.
	 */
	AgreementTable(String idLabel) {
		this.idLabel = idLabel;
	}
	
	/**
	 * Increments the cell at the row labeled by rowLabel and at the column labeled colLabel.
	 * 
	 * @param rowLabel the row label identifying a row
	 * @param colLabel the column label
	 * 
	 * @return true if both labels are in the list and the cell was incremented, false otherwise
	 */
	public boolean increment(String rowLabel, String colLabel) {
		int row = scoreLabels.indexOf(rowLabel);
		if (row < 0) {
			// log...?
			return false;
		}
		int col = scoreLabels.indexOf(colLabel);
		if (col < 0) {
			// log...?
			return false;
		}
		
		table[row][col]++;
		
		return true;
	}
	
	/**
	 * Increments a cell on the diagonal at the row and the column labeled by scoreLabel.
	 * 
	 * @param scoreLabel the score label identifying both row and column
	 * 
	 * @return true if the label is in the list and the cell was incremented, false otherwise
	 */
	public boolean increment(String scoreLabel) {
		int rowCol = scoreLabels.indexOf(scoreLabel);
		if (rowCol < 0) {
			// log...?
			return false;
		}
		
		table[rowCol][rowCol]++;
		
		return true;
	}
	
	
	/**
	 * Adds to the number in the cell at the row labeled by rowLabel and at the column labeled colLabel.
	 * 
	 * @param rowLabel the row label identifying a row
	 * @param colLabel the column label
	 * 
	 * @return true if both labels are in the list and the cell was updated, false otherwise
	 */
	public boolean addCount(String rowLabel, String colLabel, int count) {
		int row = scoreLabels.indexOf(rowLabel);
		if (row < 0) {
			// log...?
			return false;
		}
		int col = scoreLabels.indexOf(colLabel);
		if (col < 0) {
			// log...?
			return false;
		}
		
		table[row][col] += count;
		
		return true;
	}
	
	/**
	 * Adds to a cell on the diagonal at the row and the column labeled by scoreLabel.
	 * 
	 * @param scoreLabel the score label identifying both row and column
	 * 
	 * @return true if the label is in the list and the cell was incremented, false otherwise
	 */
	public boolean addCount(String scoreLabel, int count) {
		int rowCol = scoreLabels.indexOf(scoreLabel);
		if (rowCol < 0) {
			// log...?
			return false;
		}
		
		table[rowCol][rowCol] += count;
		
		return true;
	}
	
	/**
	 * Following approaches by several others in their algorithms the values in the table are
	 * multiplied by two, with exception of the values in the unmatched values row and column.
	 * One advantage is that the total sum of the cells in the table equals the number of
	 * annotations involved.  
	 */
	public void doubleMatchedValues() {
		int lastMatched = table.length;
		if (scoreLabels.contains(CompareConstants.UNMATCHED)) {
			// if so it's the last column and row
			lastMatched--;
		}
		for (int i = 0; i < lastMatched; i++) {
			for (int j = 0; j < lastMatched; j++) {
				table[i][j] *= 2;
			}
		}
	}
	
	/**
	 * Returns the table (a reference to the array actually, so allows to change the data).
	 * 
	 * @return the table
	 */
	public int[][] getTable() {
		return table;
	}
	
	/**
	 * Returns the list of labels (a reference to the list).
	 * 
	 * @return the list of labels
	 */
	public List<String> getLabels() {
		return scoreLabels;
	}
	
	/**
	 * Returns the id label.
	 * 
	 * @return the id label
	 */
	public String getIdLabel() {
		return idLabel;
	}
	
	/**
	 * Returns the total sum of all cells in the matrix.
	 * 
	 * @return the total sum
	 */
	public int getTotalSum() {
		int totalSum = 0;
		
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table.length; j++) {// table is square
				totalSum += table[i][j];
			}
		}
		
		return totalSum;
	}
}
