package mpi.eudico.client.annotator.linkedmedia;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;


/**
 * An abstract TableModel for a non editable table displaying information
 * about linked files.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public abstract class LinkedFilesTableModel extends AbstractTableModel {
    /** table column and label identifiers */
    public static final String LABEL_PREF = "LinkedFilesDialog.Label.";

    /** name of the file */
    public static final String NAME = "MediaName";

    /** url of the file */
    public static final String URL = "MediaURL";

    /** mime type of the file */
    public static final String MIME_TYPE = "MimeType";

    /** extracted from field for audio files */
    public static final String EXTRACTED_FROM = "ExtractedFrom";

    /** the offset or time origin */
    public static final String OFFSET = "MediaOffset";

    /** the master media field */
    public static final String MASTER_MEDIA = "MasterMedia";

    /** the status, linked or missing */
    public static final String LINK_STATUS = "LinkStatus";

    /** the missing status */
    public static final String MISSING = "StatusMissing";

    /** the linked status */
    public static final String LINKED = "StatusLinked";
    
    /** the associated with field  */
	public static final String ASSOCIATED_WITH = "AssociatedWith";

    /** not applicable string */
    public static final String N_A = "-";

    /** a list of column id's */
    List<String> columnIds;

    /** a list for the data of the model */
    List<List<Object>> data;

    /** a list of column class types */
    List<Class<?>> types;
    
    boolean globalCellEditFlag = true;

    /**
     * Returns the number of columns.
     *
     * @return the number of columns
     */
    @Override
	public int getColumnCount() {
        return columnIds.size();
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows
     */
    @Override
	public int getRowCount() {
        return data.size();
    }

    /**
     * Returns the value at the given row and column. Note: returns null
     * instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param rowIndex the row
     * @param columnIndex the column
     *
     * @return the value at the given row and column
     */
    @Override
	public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= data.size()) || (columnIndex < 0) ||
                (columnIndex >= columnIds.size())) {
            return null;
        }

        List<Object> row = data.get(rowIndex);

        return row.get(columnIndex);
    }

    /**
     * Sep 2013: added implementation for making the offset column editable
     *
     * @param row the row
     * @param column the column
     *
     * @return false in most cases, true for the offset column in the edit-offset-only mode
     */
  	@Override
	public boolean isCellEditable(int row, int column) {
  		if (!globalCellEditFlag) {
  			return false;
  		}
  		
  		String columnName = getColumnName(column);
  		if (columnName == null) {		
  			return false;
  		}
  		
  		if (columnName.equals(ElanLocale.getString(LABEL_PREF + OFFSET))) {
  			return true;
  		}
  		
  		return super.isCellEditable(row, column);
  	}

    /**
     * Returns the class of the data in the specified column. Note: returns
     * null instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param columnIndex the column
     *
     * @return the <code>class</code> of the objects in column
     *         <code>columnIndex</code>
     */
    @Override
	public Class<?> getColumnClass(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= types.size())) {
            return null;
        }

        return types.get(columnIndex);
    }

    /**
     * Returns the (internal) identifier of the column. Note: returns null
     * instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param columnIndex the column
     *
     * @return the id of the column or null
     */
    @Override
	public String getColumnName(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return null;
        }

        return columnIds.get(columnIndex);
    }
    
    
    
	/**
	 * Allows to disable editing of single cells altogether.
	 * 
	 * @param editable if false no cell will be editable, otherwise the normal behaviour of 
	 * isCellEditable applies
	 */
	public void setGlobalCellEditable(boolean editable) {
		globalCellEditFlag = editable;
	}
}
