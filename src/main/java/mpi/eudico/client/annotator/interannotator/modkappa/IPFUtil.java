package mpi.eudico.client.annotator.interannotator.modkappa;

/**
 * A utility class for performing Iterative Proportional Fitting on a two dimensional matrix given 
 * row and column marginals.
 * Loosely based on the code of the EasyDIAg application (implemented in Matlab) and on work
 * of Eddie Hunsinger (and others). 
 * IPF is applied to adjust data cells of a two dimensional table such that the rows and columns add up
 * to the given marginal values.
 */
public class IPFUtil {
	private final double ZERO_REPLACE = 0.001d;
	
	/**
	 * No-arg consructor
	 */
	public IPFUtil() {
		super();
	}

	/**
	 * Calculates adjusted cell values as doubles. For each iteration first the row values are adjusted, 
	 * then the column values. 
	 * Algorithm: if CV is a cell's value, CT is the current total of the cells in the row or column 
	 * and MT is the (target) marginal total value, to each cell apply: 
	 *     (CV / CT) * MT
	 * In words: the cell value divided by the actual total of the cells and then multiplied by the
	 * marginal total.
	 * 
	 * @param rowMarginals the array of row totals
	 * @param columnMarginals the array of column totals
	 * @param matrix the array of arrays of cell values. The length of the outer array (the rows) should be 
	 * equal to the length of the row marginals array, the length of the inner arrays should be equal to length of
	 * the column marginals
	 * @param numIterations the number of times the calculations should be applied to the table
	 * 
	 * @return an array of arrays of doubles
	 */
	public double[][] applyIPF(int[] rowMarginals, int[] columnMarginals, 
			int[][] matrix, int numIterations) {
		// first perform some checks
		// could/should check the size of the matrix, number of iterations should be > 0
		if (matrix.length != rowMarginals.length) {
			throw new IllegalArgumentException(
					"The number of row marginals should be equal to the number of rows in the table.");//add the numbers?
		}
		if (matrix[0].length != columnMarginals.length) {// assume there is at least 1 row in the table
			throw new IllegalArgumentException(
					"The number of column marginals should be equal to the number of columns in the table.");//add the numbers?
		}
		
		double[][] ipfMatrix = new double[matrix.length][matrix[0].length];
		// convert table
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] != 0) {
					ipfMatrix[i][j] = (double) matrix[i][j];
				} else {
					ipfMatrix[i][j] = ZERO_REPLACE;
				}
			}
		}
		// row and column marginals should not be 0 either, convert to doubles if needed
		double[] rowm = new double[rowMarginals.length];
		for (int i = 0; i < rowMarginals.length; i++) {
			if (rowMarginals[i] != 0) {
				rowm[i] = (double) rowMarginals[i];
			} else {
				rowm[i] = ZERO_REPLACE;
			}
		}
		
		double[] colm = new double[columnMarginals.length];
		for (int i = 0; i < columnMarginals.length; i++) {
			if (columnMarginals[i] != 0) {
				colm[i] = (double) columnMarginals[i];;
			} else {
				colm[i] = ZERO_REPLACE;
			}
		}
		
		// start the adjustment iterations
		double cell;
		for (int i = 0; i < numIterations; i++) {
			// rows first
			for (int j = 0; j < ipfMatrix.length; j++) {
				double rowTotal = sum(ipfMatrix[j]);
				for (int k = 0; k < ipfMatrix[j].length; k++) {
					cell = ipfMatrix[j][k];
					ipfMatrix[j][k] = (cell / rowTotal) * rowm[j];
				}
			}
			// then columns
			for (int j = 0; j < colm.length; j++) { // or ipfMatrix[0].length instead of colm.length 
				double colTotal = sumCol(ipfMatrix, j);
				for (int k = 0; k < ipfMatrix.length; k++) {
					cell = ipfMatrix[k][j];
					ipfMatrix[k][j] = (cell / colTotal) * colm[j];
				}
			}
		}
		
		return ipfMatrix;
	}
	
	/**
	 * Calculates the sum of all values in an array.
	 * No error checking.
	 * 
	 * @param values the array
	 * @return the sum
	 */
	private double sum(double[] values) {
		double total = 0d;
		for(double d : values) {
			total += d;
		}
		
		return total;
	}
	
	/**
	 * Calculates the sum of a column in a table, i.e. the sum of all n-th element of
	 * each array in the array.
	 * No error checking.
	 *   
	 * @param matrix the two dimensional array, all arrays in the array should have the same length
	 * @param col the column, the n-th element
	 * @return the sum
	 */
	private double sumCol(double[][] matrix, int col) {
		double total = 0d;
		for (double[] row : matrix) {
			total += row[col]; 
		}
		return total;
	}
}
