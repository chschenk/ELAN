package mpi.eudico.client.annotator.interannotator.modkappa;

import java.util.ArrayList;
import java.util.List;

/**
 * A special agreement table for a single score/code; the cells represents
 * Y/Y, Y/N
 * N/Y, N/N 
 */
public class TwoSquareTable extends AgreementTable {

	/**
	 * Creates a 2x2 table with Y(es) and N(o) label.
	 * @param idLabel
	 */
	public TwoSquareTable(String idLabel) {
		super(idLabel);

		scoreLabels = new ArrayList<String>(2);
		scoreLabels.add("Y");
		scoreLabels.add("N");
		
		table = new int[2][2];
	}
	
	/**
	 * Creates a 2x2 table allowing to specify custom labels.
	 * 
	 * @param idLabel the label, the code value
	 * @param labels two custom labels
	 */
	public TwoSquareTable(String idLabel, List<String> labels) {
		super(idLabel);

		if (labels == null || labels.size() != 2) {
			scoreLabels = new ArrayList<String>(2);
			scoreLabels.add("Y");
			scoreLabels.add("N");
		}
		
		table = new int[2][2];
	}

	/**
	 * Increment one of the four cells.
	 * 
	 * @param row the row index 
	 * @param column the column index
	 */
	public void increment(int row, int column) {
		if (row < 0 || row > 1) {
			throw new ArrayIndexOutOfBoundsException("The row is out of the [0, 1] range: " + row);
		}
		if (column < 0 || column > 1) {
			throw new ArrayIndexOutOfBoundsException("The column is out of the [0, 1] range: " + column);
		}
		table[row][column]++;
	}
	
	/**
	 * Add a number to one of the four cells.
	 * 
	 * @param row the row index 
	 * @param column the column index
	 * @param count the number to add
	 */
	public void addToCell(int row, int column, int count) {
		if (row < 0 || row > 1) {
			throw new ArrayIndexOutOfBoundsException("The row is out of the [0, 1] range: " + row);
		}
		if (column < 0 || column > 1) {
			throw new ArrayIndexOutOfBoundsException("The column is out of the [0, 1] range: " + column);
		}
		table[row][column] += count;
	}
}
