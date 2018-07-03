package mpi.eudico.client.annotator.interannotator.modkappa;

import java.util.Arrays;

/**
 * Class for calculating several agreement values (loosely) based on Cohen's Kappa and variations thereof
 * by Holle & Rein.
 * 
 * Holle, H., & Rein, R. (2014). EasyDIAg: A tool for easy determination of interrater agreement. 
 * Behavior Research Methods, published online August 2014. 
 * doi:10.3758/s13428-014-0506-7
 * 
 * The input is expected to be a matrix that includes a row and column for "unmatched" or "nil" values 
 * (representing samples identified by only one of the raters). But kappa and max kappa are calculated 
 * for the matrix excluding the unmatched values as well.
 * 
 * The private methods don't perform (m)any checks
 */
public class GlobalCKappa {
	private double[][] matrixDouble;// always only for the matrix with unmatched row and column
	
	// instead of having a lot of getter methods, all results are public
	public final double rawAgreementMatched;// po, or observed agreement
	public final double chanceAgreementMatched;// pe or pc, the chance agreement
	public final double kappaMatched;// k (U+03BA) 
	public final double maxKappaMatched;// kMax
	public final int numTotalMatched;// N
	public final double numDiagonalTotalMatched;
	private double diagonalTotalFitted;
	public final double rawAgreementAll;
	public final double chanceAgreementAll;
	public final double kappaAll;
	public final double maxKappaAll;
	public final double kappaAllIPF;
	public final int numTotalAll;
	public final double numDiagonalTotalAll;
	
	/**
	 * It is assumed that the matrix contains a last row and last column for "unmatched" of "unlinked" scores 
	 * (sometimes referred to as nil values)
	 * @param matrix a square matrix, not null
	 * @param matrixIncludesUnmatched a flag to indicate whether the matrix contains a row and column for "nil" values,
	 * units that have been coded by one rater only (unlinked or unmatched annotations)
	 */
	public GlobalCKappa(int[][] matrix, boolean matrixIncludesUnmatched) {
		// throw exceptions
		if (matrix.length != matrix[0].length) {
			throw new IllegalArgumentException("The table should be square.");
		}
		if (matrix.length < 2) {
			throw new IllegalArgumentException("The size of the table should be 2 or more.");
		}
		
		//int[][] matrixInt = matrix;
		int[][] matrixIntMatched = matrix;
		if (matrixIncludesUnmatched) {
			// create a new table excluding the last row and column
			matrixIntMatched = getSubMatrix(matrix);
		}
		// always calculate matched agreements
		int[] rowMarginalsMatched = getRowMarginals(matrixIntMatched);
		int[] colMarginalsMatched = getColMarginals(matrixIntMatched);
		numTotalMatched = getSum(matrixIntMatched);
		if (numTotalMatched == 0) {
			// error condition, no scores at all
			//throw new IllegalArgumentException("The table should not contain only zero's.");
		}
		
		numDiagonalTotalMatched = (double) getDiagonalSum(matrixIntMatched);
		// raw or observed agreement
		if (numTotalMatched != 0) {
			rawAgreementMatched = numDiagonalTotalMatched / numTotalMatched;
		} else {
			rawAgreementMatched = Double.NaN;
		}
		// chance agreement of the matched values
		chanceAgreementMatched = calcChanceAgreement(rowMarginalsMatched, colMarginalsMatched, numTotalMatched);
		if (chanceAgreementMatched == 1 || Double.isNaN(chanceAgreementMatched)) {
			kappaMatched = Double.NaN;
			maxKappaMatched = Double.NaN;
		} else {
			// bound by zero? HS changed in Sept 2015
//			if (rawAgreementMatched == chanceAgreementMatched) {
//				System.out.println("Raw = Chance = " + rawAgreementMatched);
//			}
			//kappaMatched =  Math.max( (rawAgreementMatched - chanceAgreementMatched) / (1 - chanceAgreementMatched), 0);
			kappaMatched = (rawAgreementMatched - chanceAgreementMatched) / (1 - chanceAgreementMatched);
			
			double poM = calcPMax(rowMarginalsMatched, colMarginalsMatched, numTotalMatched);
			maxKappaMatched = (poM - chanceAgreementMatched) / (1 - chanceAgreementMatched);
		}
		// don't perform the same calculations twice
		if (!matrixIncludesUnmatched) {
			chanceAgreementAll = chanceAgreementMatched;
			rawAgreementAll = rawAgreementMatched;
			kappaAll = kappaMatched;
			maxKappaAll = maxKappaMatched;
			kappaAllIPF = 0;
			numTotalAll = numTotalMatched;
			numDiagonalTotalAll = numDiagonalTotalMatched;
			return;
		}
		// #################################################### //
		// calculate some values including the unmatched scores, scores identified by one rater only
		int[] rowMarginals = getRowMarginals(matrix);
		int[] colMarginals = getColMarginals(matrix);
		
		numTotalAll = getSum(rowMarginals);
		numDiagonalTotalAll = (double) getDiagonalSum(matrix);
		if (numTotalAll == 0) {
			rawAgreementAll = Double.NaN;
		} else {
			rawAgreementAll =  numDiagonalTotalAll/ numTotalAll;
		}
		chanceAgreementAll = calcChanceAgreement(rowMarginals, colMarginals, numTotalAll);//traditional chance agreement
		// calculate normal kappa and maxkappa
		if (chanceAgreementAll != 1) {
			kappaAll = (rawAgreementAll - chanceAgreementAll) / (1 - chanceAgreementAll);
			
			double poMAll = calcPMax(rowMarginals, colMarginals, numTotalAll);
			maxKappaAll = (poMAll - chanceAgreementAll) / (1 - chanceAgreementAll);
		} else {
			kappaAll = Double.NaN;
			maxKappaAll = Double.NaN;
		}
		// ipf based kappa value
		int[][] seedMatrix = createMatrix(matrix.length, 1);
		seedMatrix[seedMatrix.length - 1][seedMatrix.length - 1] = 0; // last cell 0?? check, compare to easyDIAg
		
		IPFUtil ipfUtil = new IPFUtil();
		matrixDouble = ipfUtil.applyIPF(rowMarginals, colMarginals, seedMatrix, 20);
		// get the total of the diagonal of agreements of the ipf matrix		
		diagonalTotalFitted = getDiagonalSum(matrixDouble);
		// non standard way of calculating the chance agreement, see ckappa.m by Rein
		// chance agreement seems to be the raw agreement of the ipf treated matrix
		double chanceAgreementAllIPF = Double.NaN;
		if (numTotalAll != 0) {
			chanceAgreementAllIPF = diagonalTotalFitted / numTotalAll;
		}
		
		if (chanceAgreementAllIPF != 1) {
			kappaAllIPF = (rawAgreementAll - chanceAgreementAllIPF) / (1 - chanceAgreementAllIPF);
		} else {
			kappaAllIPF = Double.NaN;
		}		
	}
	
	/**
	 * Returns an array of int, each element containing the sum of the corresponding
	 * row in the input table.
	 * 
	 * @param matrix a square matrix
	 * @return int array containing the totals per row
	 */
	private int[] getRowMarginals(int[][] matrix) {
		int[] rowMarginals = new int[matrix.length];
		
		int sum = 0;
		int[] row;
		
		for (int i = 0; i < matrix.length; i++) {
			row = matrix[i];
			sum = 0;
			for (int j = 0; j < row.length; j++) {
				sum += row[j];
			}
			rowMarginals[i] = sum;
		}
		
		return rowMarginals;
	}
	
	/**
	 * Returns an array of int, each element containing the sum of the corresponding
	 * row in the input table, excluding the last column, the unmatched or nil values.
	 * 
	 * @param matrix a square matrix
	 * @return int array containing the totals per row excluding the last value per row
	 */
	private int[] getRowMarginalsMatched(int[][] matrix) {
		int[] rowMarginals = new int[matrix.length - 1];
		
		int sum = 0;
		int[] row;
		
		for (int i = 0; i < matrix.length - 1; i++) {
			row = matrix[i];
			sum = 0;
			for (int j = 0; j < row.length - 1; j++) {
				sum += row[j];
			}
			rowMarginals[i] = sum;
		}
		
		return rowMarginals;
	}

	/**
	 * Returns an array of int, each element containing the sum of the corresponding
	 * column in the input table.
	 * 
	 * @param matrix a square matrix
	 * @return int array containing the totals per column
	 */
	private int[] getColMarginals(int[][] matrix) {
		int[] colMarginals = new int[matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				colMarginals[j] += matrix[i][j];
			}
		}
		
		return colMarginals;
	}
	
	/**
	 * Returns an array of int, each element containing the sum of the corresponding
	 * column in the input table.
	 * 
	 * @param matrix a square matrix
	 * @return int array containing the totals per column
	 */
	private int[] getColMarginalsMatched(int[][] matrix) {
		int[] colMarginals = new int[matrix.length];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				colMarginals[j] += matrix[i][j];
			}
		}
		
		return colMarginals;
	}
	
	/**
	 * Creates a square table with all cells initialized to the specified value.
	 * 
	 * @param size the size of the matrix
	 * @param value the value for the cells
	 * @return a filled square table
	 */
	private int[][] createMatrix(int size, int value) {
		int[][] filledMatrix = new int[size][size];
		
		for (int[] row : filledMatrix) {
			Arrays.fill(row, value);
		}
		return filledMatrix;
	}
	
	/**
	 * Creates a new table excluding the last row and column.
	 * @param matrix input matrix
	 * @return the sub table
	 */
	private int[][] getSubMatrix(int[][] matrix) {
		int[][] subMatrix = new int[matrix.length - 1][matrix.length - 1];
		
		for (int i = 0; i < matrix.length - 1; i++) {
			//subMatrix[i] = Arrays.copyOf(matrix[i], matrix.length - 1);
			for (int j = 0; j < matrix.length - 1; j++) {
				subMatrix[i][j] = matrix[i][j];
			}
		}
		
		return subMatrix;
	}
	
	/**
	 * Adds all values in the array.
	 * 
	 * @param marginals an array of row or column totals
	 * @return the sum
	 */
	private int getSum(int[] marginals) {
		int sum = 0;
		for (int i : marginals) {
			sum += i;
		}
		
		return sum;
	}
	
	/**
	 * Calculates the total sum of all "matched" values, i.e. it calculates the total of
	 * all cell excluding those in the last column and last row.
	 * 
	 * @param matrix the matrix including unmatched or nil values
	 * @return the sum
	 */
	private int getSum(int[][] matrix) {
		int sum = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				sum += matrix[i][j];
			}
		}
		
		return sum;
	}
	
	/**
	 * Returns the total of the values on the agreement diagonal, from top left corner
	 * to lower right corner.
	 * 
	 * @param matrix the matrix
	 * @return the agreement total
	 */
	private int getDiagonalSum(int[][] matrix) {
		int sum = 0;
		for (int i = 0; i < matrix.length; i++) {
			sum += matrix[i][i];
		}
		
		return sum;
	}
	
	/**
	 * Returns the total of the values on the agreement diagonal, from top left corner
	 * to lower right corner, excluding the very last cell.
	 * 
	 * @param doubleMatrix a matrix of double values, obtained after iterative proportional fitting
	 * 
	 * @return the sum of the agreement cells
	 */
	private double getDiagonalSum(double[][] doubleMatrix) {
		double sum = 0d;
		
		for (int i = 0; i < doubleMatrix.length - 1; i++) {
			sum += doubleMatrix[i][i];
		}
		
		return sum;
	}
	
	/**
	 * Calculates the overall chance agreement as the sum of the product of corresponding row- (R1) and 
	 * column (R2) marginal values-as-percentages-of-total.
	 * The length of the two arrays should be the same.
	 * 
	 * @param rowMarginals the row marginal values (total per row)
	 * @param colMarginals the column marginal values (total per column)
	 * @param totalSum the total number of scores, not 0
	 * @return the overall chance agreement
	 */
	private double calcChanceAgreement(int[] rowMarginals, int[] colMarginals, int totalSum) {
		if (totalSum == 0) {
			return Double.NaN;
		}
		double sumCA = 0d;
		double totalDouble = (double) totalSum;
		double perc1, perc2;
		
		for (int i = 0; i < rowMarginals.length; i++) {
			perc1 = rowMarginals[i] / totalDouble;
			perc2 = colMarginals[i] / totalDouble;
			sumCA += (perc1 * perc2);
		}
		
		return sumCA;
	}
	
	/**
	 * Calculates the maximal marginal probability (poM or Pmax) value used for calculating 
	 * the maximum kappa given the marginal distribution. The values is based on the minimum values
	 * of corresponding row and column totals.
	 *  
	 * @param rowMarginals the row marginal values (total per row)
	 * @param colMarginals the column marginal values (total per column)
	 * @param totalSum the total number of scores, not 0
	 * 
	 * @return the sum of the minimal values of row/column pairs divided by the total sum 
	 */
	private double calcPMax(int[] rowMarginals, int[] colMarginals, int totalSum) {
		if (totalSum == 0) {
			return Double.NaN;
		}
		int minimalRC = 0;
		
		for (int i = 0; i < rowMarginals.length; i++) {
			minimalRC += Math.min(rowMarginals[i], colMarginals[i]);
		}
		
		return minimalRC / (double) totalSum;
	}
}
