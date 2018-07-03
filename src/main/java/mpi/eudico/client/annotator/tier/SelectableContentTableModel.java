package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

/**
 * Table model to display a table with items that can be selected (and can have a selected state
 * independent of row selection in the table). The first column, at index 0, always contains the
 * boolean value of the selected state. Next to it can be any number of columns holding Object
 * values (usually String objects).
 *  
 * @author Jeffrey Lemein
 * @author Han Sloetjes
 * @version March, 2011
 * @version July, 2014 Removed reference to the JTable, the data model should preferably be 
 * unaware of a view object; views will receive events if they are registered as listener. 
 * @version Nov 2017 replaced the arrays by lists, added documentation, added setColumnName() 
 * and findColumn()  
 */
@SuppressWarnings("serial")
public class SelectableContentTableModel extends AbstractTableModel {
	public static final String SELECT_ID = "Select_ID";
	private List<String> columnNameList;
	private List<List<Object>> rowDataList;
	
	/**
	 * Constructor with the data of one column as parameter
	 * @param content the elements of a single (additional) column as a Set 
	 * 		(the selected column will always be there)
	 */
	public SelectableContentTableModel( Set<? extends Object> content){
		this(content, null);
	}
	
	/**
	 * Constructor with the data of one column as parameter
	 * @param content the elements of a single (additional) column as a Set 
	 * 		(the selected column will always be there)
	 * @param columnName the name of the one column
	 */
	public SelectableContentTableModel( Set<? extends Object> content, String columnName){
		columnNameList = new ArrayList<String>(2);
		columnNameList.add(SELECT_ID);// identifier instead of name
		columnNameList.add(columnName);
		rowDataList = new ArrayList<List<Object>>(content.size());
		for (Object obj : content) {
			List<Object> row = new ArrayList<Object>(2);
			row.add(Boolean.FALSE);
			row.add(obj);
			rowDataList.add(row);
		}
	}
	
	/**
	 * Creates a selectable content table model in which the data for two columns can be specified.
	 * Both arrays of data should be of the same length
	 * @param column1Data data for all columns after the first (the first index in the array 
	 * is the column index, so the length of columnData corresponds to the number of columns!!)
	 * @param columnNames the names for the columns, in the same order, or null
	 */
	public SelectableContentTableModel( Object[][] columnData, String[] columnNames ){
		int numCols = columnData.length + 1;
		int numRows = 0;
		columnNameList = new ArrayList<String>(numCols);
		columnNameList.add(SELECT_ID);
		for (int i = 0; i < columnData.length; i++) {
			Object[] obja = columnData[i];
			numRows = Math.max(numRows, obja.length);
			if (columnNames != null && i < columnNames.length) {
				columnNameList.add(columnNames[i]);
			} else {
				columnNameList.add(null);
			}
		}
		
		rowDataList = new ArrayList<List<Object>>(numRows);
		for (int i = 0; i < numRows; i++) {
			List<Object> row = new ArrayList<Object>(numCols);
			row.add(Boolean.FALSE);
			for (Object[] obja : columnData) {
				if (i < obja.length) {
					row.add(obja[i]);
				} else {
					row.add(null);//
				}
			}
			rowDataList.add(row);
		}
	}
	
	/**
	 * @return the number of rows in the model
	 */
	@Override
	public int getRowCount(){
		return rowDataList.size();
	}
	
	/**
	 * @return the number of columns, including the Select column
	 */
	@Override
	public int getColumnCount(){
		return columnNameList.size();
	}
	
	/**
	 * @return the given name for the column. For the Select column
	 * and for columns with no name set, the empty string is returned
	 */
	@Override
	public String getColumnName(int column) {
		if (column > 0 && column < columnNameList.size()) {
			String name = columnNameList.get(column);
			return name != null ? name : "";
		}
		return "";
	}
	
	/**
	 * Allows to set the column name after construction of the model 
	 * (currently the constructors don't allow to pass the column names.
	 * 
	 * @param column the column to set the name of. The Select column's name 
	 * can not be changed, so the first array passed to the constructor is at index 1
	 * @param name the new name of the column
	 */
	public void setColumnName(int column, String name) {
		if (column > 0 && column < columnNameList.size()) {
			columnNameList.set(column, name);
		}
	}

	/**
	 * @return the column index based on the name of the column,
	 * for the Select column the constant {@link #SELECT_ID} can be used 
	 * (which is always at index 0)
	 */
	@Override
	public int findColumn(String columnName) {
		return columnNameList.indexOf(columnName); 
	}

	/**
	 * @return the value of the specified cell, or null if it does not exist
	 * (no index out of bounds exception).
	 */
	@Override
	public Object getValueAt(int row, int column){
		if (row < rowDataList.size() && column < columnNameList.size()) {
			return rowDataList.get(row).get(column);
		}
		return null;
	}
	
	/**
	 * Sets the value in the specified cell, if it exists.
	 * The first column only allows Booleans.
	 * Index out of bounds is ignored (no exception) 
	 */
	@Override
	public void setValueAt( Object object, int row, int column ){
		if (row < rowDataList.size() && column < columnNameList.size()) {
			if (column == 0 && object.getClass() != Boolean.class) {
				return;
			}
			rowDataList.get(row).set(column, object);
			fireTableRowsUpdated(row, row);
		}
	}
	
	/**
	 * Sets the selected value in column 0 in all rows to true.
	 * 
	 * @see #selectNone()
	 */
	public void selectAll(){
		if( getColumnCount() > 0 ) {
			for( int i = 0; i < getRowCount(); i++ ) {
				setValueAt( Boolean.TRUE, i, 0 );
			}
		}
		fireTableDataChanged();
	}
	
	
	/**
	 * Sets all rows to not selected by changing the value in 
	 * the Select column at index 0
	 * 
	 * @see #selectAll()
	 */
	public void selectNone(){
		if( getColumnCount() > 0 ) {
			for( int i = 0; i < getRowCount(); i++ ) {
				setValueAt( Boolean.FALSE, i, 0 );
			}
		}
		fireTableDataChanged();
	}
	
	/**
	 * Returns a list of selected values based on the value in column 0.
	 * The returned List then only contains the elements from the first column
	 * after the Select column (normally the tier name)!! 
	 * This should be taken into account when creating the table. 
	 * 
	 * @return a list of selected objects from the first(!) data column after the Select column
	 */
	public List<Object> getSelectedValues(){
		List<Object> selectedValues = new ArrayList<Object>();
		if (getColumnCount() > 1) {
			for (List<Object> rowData : rowDataList) {
				if ((Boolean)rowData.get(0)) {
					Object val = rowData.get(1); 
					if (val != null) {
						selectedValues.add(val);
					}
				}
			}
		}

		return selectedValues;
	}	
	
	/**
	 * Moves an element up in the list.
	 * 
	 * @param row the data at this row should move to row - 1
	 * @see #moveDown(int)
	 */
    public void moveUp(int row) {
    	// could throw ArrayIndexOutOfBoundsException here 
    	if(row > 0 && row < rowDataList.size()){
    		List<Object> rowData = rowDataList.remove(row);
    		rowDataList.add(row - 1, rowData);
    		fireTableRowsUpdated(row - 1, row);
    	}
    }
    
    /**
     * Moves an element down in the list.
     * 
     * @param row the row to move down
     * @see #moveUp(int)
     */
    public void moveDown(int row) { 
    	// could throw ArrayIndexOutOfBoundsException here
    	if(row < rowDataList.size() - 1 && row >= 0){
    		List<Object> rowData = rowDataList.remove(row);
    		rowDataList.add(row + 1, rowData);
    		fireTableRowsUpdated(row, row + 1);
    	}

    }
	
    /**
     * 
     * @return true if no row has the Selected flag set, false if there is at least one
     * row selected
     */
	public boolean nothingSelected(){
		for (List<Object> rowData : rowDataList) {
			if ((Boolean)rowData.get(0)) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Only the first, Selected, column is editable.
	 * @return true for the Select column at index 0, false otherwise
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 0;
    }
	
	/**
	 * @param col the column index
	 * @return Boolean.class for the Select column, the actual class from the 
	 * first row for all other columns
	 */
	@Override
	public Class<? extends Object> getColumnClass(int col) {
		// could throw ArrayIndexOutOfBoundsException here
		if (col == 0) {
			return Boolean.class;
		} else if (col < columnNameList.size() && rowDataList.size() > 0) {
			// the number of columns (items in the row list) should be the same as in the 
			// column name/id list
			Object val = rowDataList.get(0).get(col);
			if (val != null) {
				return val.getClass();
			}
		}
		
		return Object.class;
    }
}
