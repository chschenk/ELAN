package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Table model used to display rows of data. This data is read only.
 * HS: Mainly used to show a message in the table area, it seems. 
 * Needs revision and documentation.
 * 
 * @author Jeffrey Lemein
 * @version March, 2011
 */
@SuppressWarnings("serial")
public class DisplayableContentTableModel extends AbstractTableModel{
	private String[] columnNames;
	private String[][] rowData;
	private List<JTable> connectedTables;
	
	private DisplayableContentTableModel(){
		connectedTables = new ArrayList<JTable>();
	}
	
	public DisplayableContentTableModel( int nrColumns ){
		this();
		columnNames = new String[nrColumns];
		rowData = null;//new String[1][nrColumns];
	}
	
	public DisplayableContentTableModel( String message ){
		this();
		columnNames = new String[1];
		rowData = new String[1][1];
		rowData[0][0] = message;
	}
	
	public DisplayableContentTableModel( String message, JTable table ){
		this(message);

		connectedTables.add(table);
	}
	
	public DisplayableContentTableModel( String[] messages ){
		this();
		columnNames = new String[1];
		rowData = new String[messages.length][1];
		
		for( int i = 0; i<messages.length; i++ ){
			rowData[i][0] = messages[i];
		}
	}
	
	public DisplayableContentTableModel( String[] colValues, Collection<String> rowValues ){
		this();
		
		columnNames = new String[colValues.length];
		rowData = new String[rowValues.size()][colValues.length];
		
		for( int c=0; c<colValues.length; c++ )
			columnNames[c] = colValues[c];
		
		Object[] o = rowValues.toArray();
		for( int r = 0; r<rowValues.size(); r++ ){
			rowData[r][0] = o[r].toString();
		}
	}
	
	public DisplayableContentTableModel( String[] messages, JTable table ){
		this(messages);
		
		connectedTables.add(table);
	}
	
	public DisplayableContentTableModel( String[] values, int nRows ){
		this();
		
		int nColumns = values.length;
		columnNames = new String[nColumns];
		rowData = new String[nRows][nColumns];
		
		for( int c=0; c<nColumns; c++ )
			columnNames[c] = values[c];
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	public void addRow(String[] data){
		int nrRows = getRowCount();
		int nrCols = getColumnCount();
		
		String[][] oldRowData = rowData;
		rowData = new String[nrRows+1][nrCols];
		
		for( int r=0; r<nrRows; r++ )
			for( int c=0; c<nrCols; c++ ){
				rowData[r][c] = oldRowData[r][c];
			}
		
		//add new values
		for( int c=0; c<data.length; c++ )
			rowData[nrRows][c] = data[c];
		
		updateTables();
	}
	
	public void removeRow(int rowNr){
		int nrRows = getRowCount();
		int nrCols = getColumnCount();
		
		String[][] oldRowData = rowData;
		rowData = new String[nrRows-1][nrCols];
		
		for( int r=0; r<nrRows-1; r++ )
			for( int c=0; c<nrCols; c++ ){
				if( r >= rowNr )
					rowData[r][c] = oldRowData[r+1][c];
				else
					rowData[r][c] = oldRowData[r][c];
			}
		
		updateTables();
	}
	
	public void updateMessage( int row, String newMessage ){
		rowData[row][0] = newMessage;
		updateTables();
	}

	@Override
	public int getRowCount() {
		if( rowData == null )
			return 0;
		else
			return rowData.length;
	}

	/*
	 * Nov 2017 added some array index tests
	 */
	@Override
	public Object getValueAt(int row, int column) {
		if (row < rowData.length && column < getColumnCount()) {
			return rowData[row][column];
		}
		return null;
	}

	/*
	 * Nov 2017 added some array index tests
	 */
	public void setValueAt(String value, int rowIndex, int colIndex){
		if (rowIndex < rowData.length && colIndex < getColumnCount()) {
			rowData[rowIndex][colIndex] = value;
		}
	}
	
	/*
	 * Nov 2017 added some array index tests
	 */
	public void setValues( String[] values, int columnNr ){
		if (columnNr >= getColumnCount()) {
			return;
		}
		
		for( int r=0; r<values.length; r++ ) {
			if (r < rowData.length) {
				rowData[r][columnNr] = values[r];
			}
		}
	}
	
	// fireTableDataChanged(); would be a better way?
	public void updateTables(){
		for( JTable t : connectedTables ){
			t.repaint();
			t.revalidate();
		}
	}
	
	/**
	 * Convenience method to connect a table with its table model.
	 * Changes in the table model causes the connected tables to be redrawn automatically. 
	 * @param table the table to which the table model should be connected
	 */
	public void connectTable( JTable table ){
		connectedTables.add(table);
	}

	public void addRowAt(String[] data, int rowIndex) {
		int nrRows = getRowCount();
		int nrCols = getColumnCount();
		
		String[][] oldRowData = rowData;
		rowData = new String[nrRows+1][nrCols];
		
		//copy old data
		for( int r=0; r<nrRows; r++ ){
			for( int c=0; c<nrCols; c++ ){
				if( r >= rowIndex )
					rowData[r+1][c] = oldRowData[r][c];
				else
					rowData[r][c] = oldRowData[r][c];
			}
		}
		
		//add new values
		for( int c=0; c<data.length; c++ )
			rowData[rowIndex][c] = data[c];
		
		updateTables();	
	}

	public boolean contains(String tierName, int column) {
		for( int i=0; i<getRowCount(); i++ )
			if( ((String)getValueAt(i, column)).equals(tierName) )
				return true;
		
		return false;
	}
}
