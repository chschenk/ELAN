package mpi.eudico.client.annotator.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.recognizer.data.Segment;


/**
 * A table model for recognizer segment objects.
 *
 * @author Han Sloetjes
 * @version 1.0, July 2008
 */
@SuppressWarnings("serial")
public class SegmentTableModel extends AbstractTableModel {
    /** table column and label identifiers */
    public static final String LABEL_PREF = "SegmentsToTierDialog.Table.";

    /** name column */
    public static final String LABEL = "CurrentLabel";

    /** new name column */
    public static final String LABEL_NEW = "NewLabel";

    /** include column */
    public static final String INCLUDE = "Include";

    /** index number column */
    public static final String NUMBER = "NumberSegments";
    
    /** the "null" label */
    public static final String NULL = "<null>";
    
    private final int NUM_COLS = 4;
    private List<Segment> segmentList;
    private List<String> columnIds;
    private List<List<Object>> tableData;
    private List<Class> classes;

    /**
     * Creates a new SegmentTableModel instance
     */
    public SegmentTableModel() {
        segmentList = new ArrayList<Segment>();
        columnIds = new ArrayList<String>(NUM_COLS);
        tableData = new ArrayList<List<Object>>();
        classes = new ArrayList<Class>(NUM_COLS);
        initLists();
    }

    private void initLists() {
        columnIds.add(INCLUDE);
        classes.add(Boolean.class);
        columnIds.add(LABEL);
        classes.add(String.class);
        columnIds.add(LABEL_NEW);
        classes.add(String.class);
        columnIds.add(NUMBER);
        classes.add(Boolean.class);
    }

    /**
     * Add the contents of a Segment to the table model.
     *
     * @param segment the segment to add
     */
    public void addSegment(Segment segment) {
        if (segment != null) {
            segmentList.add(segment);

            List<Object> rdata = new ArrayList<Object>(NUM_COLS);
            int index = columnIds.indexOf(INCLUDE);

            if (index > -1) {
                rdata.add(index, Boolean.TRUE);
            }

            index = columnIds.indexOf(LABEL);

            if (index > -1) {
                if (segment.label != null) {
                    rdata.add(index, segment.label);
                } else {
                    rdata.add(index, NULL);
                }
            }

            index = columnIds.indexOf(LABEL_NEW);

            if (index > -1) {
                rdata.add(index, "");
            }

            index = columnIds.indexOf(NUMBER);

            if (index > -1) {
                rdata.add(index, Boolean.FALSE);
            }

            tableData.add(rdata);
        }
    }

    /**
     * The number of columns.
     *
     * @return the number of columns
     */
    @Override
	public int getColumnCount() {
        return columnIds.size();
    }

    /**
     * The number of rows = size of table data list
     *
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
	public int getRowCount() {
        return tableData.size();
    }

    /**
     * Finds the List of the specified row and returns the value at the
     * column index.
     *
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
	public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= tableData.size()) ||
                (columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return null;
        }

        List<Object> row = tableData.get(rowIndex);

        return row.get(columnIndex);
    }

    /**
     * Sets the value for the specified cell.
     *
     * @param aValue the new value
     * @param rowIndex the row
     * @param columnIndex the column
     */
    @Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= tableData.size()) ||
                (columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return;
        }
        if (isCellEditable(rowIndex, columnIndex)) {
	        List<Object> row = tableData.get(rowIndex);
	        row.set(columnIndex, aValue);
	        fireTableDataChanged();
        }
    }

    /**
     * Returns false for the column with the segment label.
     *
     * @param row the row
     * @param column the column
     *
     * @return true for the include, new label and number column
     */
    @Override
	public boolean isCellEditable(int row, int column) {
        int col = findColumn(LABEL);

        if (column == col) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Finds the column index for the specified identifier.
     *
     * @param columnName the name/identifier of the column
     *
     * @return the index, or -1 if not found
     */
    @Override
	public int findColumn(String columnName) {
        return columnIds.indexOf(columnName);
    }

    /**
     * Returns the column index given the column name instead of the indentifier
     * constant.
     * 
     * @param columnName the display name, as returned by getColumnName
     * @return the index or -1
     */
    public int findColumnByName(String columnName) {
    	if (columnName == null) {
    		return -1;
    	}
    	for (int i= 0; i < columnIds.size(); i++) {
    		if (columnName.equals(ElanLocale.getString(LABEL_PREF +
            columnIds.get(i)))) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    /**
     * Returns the identifier of the column. Note: returns empty String when
     * the column cannot be found
     *
     * @param columnIndex the column
     *
     * @return the id of the column or empty stringl
     */
    @Override
	public String getColumnName(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return "";
        }

        //return (String) columnIds.get(columnIndex);
        return ElanLocale.getString(LABEL_PREF +
            columnIds.get(columnIndex));
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
	public Class getColumnClass(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= classes.size())) {
            return null;
        }

        return classes.get(columnIndex);
    }

    /**
     * Note: silently returns instead of throwing an
     * ArrayIndexOutOfBoundsException
     *
     * @param rowIndex the row to remove
     */
    public void removeRow(int rowIndex) {
        if ((rowIndex >= 0) && (rowIndex < tableData.size())) {
            tableData.remove(rowIndex);
            segmentList.remove(rowIndex);
            fireTableDataChanged();
        }
    }

    /**
     * Removes all rows from the table.
     */
    public void removeAllRows() {
        tableData.clear();
        segmentList.clear();
        fireTableDataChanged();
    }
}
