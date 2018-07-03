package mpi.eudico.client.annotator.comments;

import java.util.Collections;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A table model that adds an extra column to an existing table model.
 * It is assumed that the number of columns in the delegate is fixed.
 * The elements in the new column are not editable.
 * <p>
 * For reasons of practicality, it includes the extra methods from 
 * CommentTableModelInterface, but that can be split off to make it
 * more general if needed.
 * 
 * @author olasei
 */
public class TableModelExtender<T> implements TableModel, CommentTableModel {
	
	private CommentTableModel delegate;
	private String columnName;
	private Class<T> clazz;
	private List<T> column; 
	private int delegateColumns;

	/**
	 * Create a new TableModel that adds an extra column to an existing model.
	 * 
	 * @param delegate the TableModel to extend
	 * @param columnName the name of the new column
	 * @param clazz the object type shown in the column
	 */
	public TableModelExtender(CommentTableModel delegate, String columnName, Class<T> clazz) {
		super();
		this.delegate = delegate;
		this.columnName = columnName;
		this.clazz = clazz;
		column = Collections.emptyList();
		delegateColumns = delegate.getColumnCount();
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.client.annotator.comments.CommentTableModelInterface#setComments(java.util.List)
	 */
	@Override // CommentTableModel
	public void setComments(List<CommentEnvelope> comments) {
		delegate.setComments(comments);
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.client.annotator.comments.CommentTableModelInterface#getComment(int)
	 */
	@Override // CommentTableModel
	public CommentEnvelope getComment(int index) {
		return delegate.getComment(index);
	}
	
	/**
	 * Provide the data to be displayed in the extra column.
	 * <p>
	 * Setting this does not fire any events, so that replacing the table
	 * data can be done with this column first, and then the main table,
	 * which will call fireTableDataChanged().
	 * 
	 * @param column
	 */
	public void setColumn(List<T> column) {
		this.column = column;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override // TableModel
	public int getRowCount() {
		return delegate.getRowCount();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override // TableModel
	public int getColumnCount() {
		return delegateColumns + 1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
    @Override // TableModel
    public String getColumnName(int col) {
		if (col < delegateColumns) {
			return delegate.getColumnName(col);
		} else {
			return columnName;
		}
    }

    /**
     * Returns <code>String.class</code> regardless of column index.
     */
    @Override // TableModel
    public Class<?> getColumnClass(int col) {
		if (col < delegateColumns) {
			return delegate.getColumnClass(col);
		} else {
	        return clazz;
		}
    }

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override // TableModel
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex < delegateColumns) {
			return delegate.getValueAt(rowIndex, columnIndex);
		} else {
			if (rowIndex < column.size()) {
				return column.get(rowIndex);
			} else {
				return "-";
			}
		}
	}

	@Override // TableModel
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex < delegateColumns) {
			return delegate.isCellEditable(rowIndex, columnIndex);
		} else {
			return false;
		}
	}

	@Override // TableModel
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex < delegateColumns) {
			delegate.setValueAt(aValue, rowIndex, columnIndex);
		}
	}

	@Override // TableModel
	public void addTableModelListener(TableModelListener l) {
		delegate.addTableModelListener(l);
	}

	@Override // TableModel
	public void removeTableModelListener(TableModelListener l) {
		delegate.removeTableModelListener(l);
	}
}
