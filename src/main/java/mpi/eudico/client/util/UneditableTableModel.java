package mpi.eudico.client.util;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Extends the default implementation of a table model by overriding 
 * {@link #isCellEditable(int, int)} to always return false. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class UneditableTableModel extends DefaultTableModel {

	public UneditableTableModel() {
		super();
	}

	public UneditableTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
	}

	public UneditableTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public UneditableTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	public UneditableTableModel(Vector columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public UneditableTableModel(Vector data, Vector columnNames) {
		super(data, columnNames);
	}

	/**
	 * @return false, regardless of row and column parameters, making the model uneditable
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
