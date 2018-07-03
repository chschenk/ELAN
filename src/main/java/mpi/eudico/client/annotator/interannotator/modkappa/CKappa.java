package mpi.eudico.client.annotator.interannotator.modkappa;

/**
 * Class for calculations of kappa agreement values based on a 2x2
 * contingency table.
 * Also calculates kappa max as in Holle&Rein's EasyDIAg application 
 */
public class CKappa {

	public CKappa() {
		super();
	}

	/**
	 * Calculates the raw agreement. Algorithm:
	 * sum of diagonal ([0,0],[1,1]) / total (of all positions)
	 * 
	 * @param matrix 2x2 matrix, not null
	 * @return raw agreement
	 */
	public double calcRawAgreement(int[][] matrix) {
		if (matrix.length != 2 || matrix[0].length != 2) {
			throw new IllegalArgumentException("The table should be square, 2 x 2.");
		}
		return calcRawAgreementPr(matrix);
	}
	
	/**
	 * Calculates the raw agreement. Algorithm:
	 * sum of diagonal ([0,0],[1,1]) / total (of all positions)
	 * 
	 * @see #calcRawAgreement(int[][])
	 * @param matrix 2x2 matrix, not null
	 * @return raw agreement or NaN if the matrix is empty (only contains zeros)
	 */
	private double calcRawAgreementPr(int[][] matrix) {
		double total = totalSumD(matrix);
		
		if (total == 0) {// an "empty" matrix
			return Double.NaN;
		}
		
		return (matrix[0][0] + matrix[1][1]) / total;
	}
	
	/**
	 * Calculates chance agreement. Algorithm:
	 * percentage positive scores of R1 x percentage positive scores of R2
	 * plus
	 * percentage negative scores of R1 x percentage negative scores of R2
	 *   
	 * N = total sum
	 * (([0,0] + [0,1]) / N)  *  (([0,0] + [1,0]) / N)
	 * +
	 * (([1,0] + [1,1]) / N)  *  (([0,1] + [1,1]) / N)
	 *  
	 * @param matrix 2x2 matrix, not null
	 * 
	 * @return chance agreement
	 */
	public double calcChanceAgreement(int[][] matrix) {
		if (matrix.length != 2 || matrix[0].length != 2) {
			throw new IllegalArgumentException("The table should be square, 2 x 2.");
		}
		return calcChanceAgreementPr(matrix);
	}

	/**
	 * @see #calcChanceAgreement(int[][])
	 *  
	 * @param matrix 2x2 matrix, not null
	 * 
	 * @return chance agreement or NaN if the matrix is empty (only contains zeros)
	 */
	private double calcChanceAgreementPr(int[][] matrix) {
		double total = totalSumD(matrix);
		
		if (total == 0) {// an empty matrix
			return Double.NaN;
		}
		
		return ((matrix[0][0] + matrix[0][1]) / total) * ((matrix[0][0] + matrix[1][0]) / total)
				+
				((matrix[1][0] + matrix[1][1]) / total) * ((matrix[0][1] + matrix[1][1]) / total);
	}
	
	/**
	 * Calculates (roughly) Cohen's kappa, algorithm:
	 * (raw agreement - chance agreement) / (1 - chance agreement)
	 * 
	 * @param matrix 2x2 matrix, not null
	 * @return kappa value
	 */
	public double calcKappa(int[][] matrix) {
		if (matrix.length != 2 || matrix[0].length != 2) {
			throw new IllegalArgumentException("The table should be square, 2 x 2.");
		}
		double chanceAgree = calcChanceAgreementPr(matrix);
		if (chanceAgree == 1 || Double.isNaN(chanceAgree)) {
			return Double.NaN;
		}
		double rawAgree = calcRawAgreementPr(matrix);
		
		return (rawAgree - chanceAgree) / (1 - chanceAgree);
	}
	
	/**
	 * Calculates kappa based on specified raw and chance agreement.
	 * 
	 * @param rawAgreement the raw agreement
	 * @param chanceAgreement the chance agreement
	 * @return kappa or NaN if chanceAgreement = 1
	 */
	public double calcKappa(double rawAgreement, double chanceAgreement) {
		if (chanceAgreement == 1) {
			return Double.NaN;
		}

		// should we return 0 in case the outcome is < 0 ?
		return (rawAgreement - chanceAgreement) / (1 - chanceAgreement);
	}
	
	/**
	 * Calculates the sum of all 4 positions.
	 * 
	 * @param matrix a 2x2 table, not null
	 * 
	 * @return the sum as an int
	 */
	public int totalSum(int[][] matrix) {
		if (matrix.length != 2 || matrix[0].length != 2) {
			throw new IllegalArgumentException("The table should be square, 2 x 2.");
		}
		
		return matrix[0][0] + matrix[1][0] + matrix[0][1] + matrix[1][1];
	}
	
	/**
	 * Calculates the sum of all 4 positions.
	 * 
	 * @param matrix a 2x2 table, not null
	 * 
	 * @return the sum as a double
	 */
	private double totalSumD(int[][] matrix) {
		return (double) (matrix[0][0] + matrix[1][0] + matrix[0][1] + matrix[1][1]);
	}
	
	/**
	 * Calculates the maximum kappa value for the given matrix. 
	 * Algorithm according to Holle & Rein:
	 * k_max = (poM - pe) / (1 - pe)
	 * pe = change agreement
	 * poM = sum of minimal values of corresponding row and column totals (marginals) 
	 *       divided by the total sum
	 * 
	 * @param matrix the 2x2 table not null
	 * @return the maximum kappa value or NaN in case of empty matrix or chance agreement = 1
	 */
	public double calcMaxKappa(int[][] matrix) {
		if (matrix.length != 2 || matrix[0].length != 2) {
			throw new IllegalArgumentException("The table should be square, 2 x 2.");
		}
		
		return calcMaxKappaPr(matrix);
	}
	
	/**
	 * See {@link #calcMaxKappa(int[][])}
	 * @param matrix
	 * @return 
	 */
	private double calcMaxKappaPr(int[][] matrix) {
		double total = totalSumD(matrix);
		
		if (total == 0) {
			return Double.NaN;
		}
		double pe = calcChanceAgreementPr(matrix);
		if (pe == 1) {
			return Double.NaN;
		}
		
		int minSum = 0;
		minSum += Math.min(matrix[0][0] + matrix [0][1], matrix[0][0] + matrix[1][0]);//minimal value of sum of row(0) and sum of col(0)
		minSum += Math.min(matrix[0][1] + matrix [1][1], matrix[1][0] + matrix[1][1]);//minimal value of sum of row(1) and sum of col(1)

		double poM = minSum / total;
		
		return (poM - pe) / (1 - pe);
	}
}
