package mpi.eudico.client.annotator.timeseries;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.timeseries.config.TSTrackConfiguration;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;


/**
 * A table model for time series track objects.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class TrackTableModel extends AbstractTableModel {
    /** the track name column identifier */
    public static final String NAME = "TimeSeriesViewer.Config.TrackName";

    /** the tracks description column identifier */
    public static final String DESCRIPTION = "TimeSeriesViewer.Config.TrackDesc";

    /** empty or not applicable value */
    private final String N_A = "-";
    private List<TSTrackConfiguration> trackList;
    private List<String> columnIds;
    private List<List<String>> tableData;

    /**
     * Creates an empty table model.
     */
    public TrackTableModel() {
        this(null);
    }

    /**
     * Creates a track table model and fills it with the information from  the
     * list. It is assumed that the objects in the list are of the  proper
     * type.
     *
     * @param tracks the tracks (in TSTrackConfiguration objects)
     */
    public TrackTableModel(List<TSTrackConfiguration> tracks) {
        super();

        if (tracks == null) {
            trackList = new ArrayList<TSTrackConfiguration>(0);
        } else {
            trackList = new ArrayList<TSTrackConfiguration>(tracks);
        }

        columnIds = new ArrayList<String>(2);
        columnIds.add(NAME);
        columnIds.add(DESCRIPTION);
        initData();
    }

    private void initData() {
        tableData = new ArrayList<List<String>>(trackList.size());

        TSTrackConfiguration track;

        for (int i = 0; i < trackList.size(); i++) {
            track = trackList.get(i);
            addRowData(track);
        }

        fireTableDataChanged();
    }

    /**
     * Adds a row to the table.
     *
     * @param track the track configuration object to add to the table model
     */
    private void addRowData(TSTrackConfiguration track) {
        if (track == null) {
            return;
        }

        ArrayList<String> rowData = new ArrayList<String>(2);
        rowData.add(track.getTrackName());
        AbstractTSTrack tr = (AbstractTSTrack) track.getObject(track.getTrackName());
        if (tr != null) {
            rowData.add(tr.getDescription());
        } else {
            rowData.add(N_A);
        }
        
        tableData.add(rowData);
    }

    /**
     * the number of columns = the size of column ids list
     *
     * @see javax.swing.table.TableModel#getColumnCount()
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
     * column index. Returns null rather than throwing an AIOOBE.
     *
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
	public String getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= tableData.size()) ||
                (columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return null;
        }

        List<String> row = tableData.get(rowIndex);

        return row.get(columnIndex);
    }

    /**
     * Returns false regardless of parameter values. The values are not  to be
     * edited directly in the table.
     *
     * @param row the row
     * @param column the column
     *
     * @return false
     */
    @Override
	public boolean isCellEditable(int row, int column) {
        return false;
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
     * Returns the class of the data in the specified column. Note: returns
     * null instead of throwing an ArrayIndexOutOfBoundsException
     *
     * @param columnIndex the column
     *
     * @return the <code>class</code> of the objects in column
     *         <code>columnIndex</code>
     */
    @Override
	public Class<String> getColumnClass(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= columnIds.size())) {
            return null;
        }

        // for the time being only String objects are in the model
        return String.class;

        //return (Class) classes.get(columnIndex);
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

        return ElanLocale.getString(columnIds.get(columnIndex));
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
            trackList.remove(rowIndex);
            fireTableDataChanged();
        }
    }

    /**
     * Removes all rows from the table.
     */
    public void removeAllRows() {
        tableData.clear();
        trackList.clear();
        fireTableDataChanged();
    }

    /**
     * Adds a row with the data from the track.
     *
     * @param track the track to add to the model
     */
    public void addRow(TSTrackConfiguration track) {
        if ((track == null) || trackList.contains(track)) {
            return;
        }

        trackList.add(track);
        addRowData(track);
        fireTableDataChanged();
    }

    /**
     * Notification that the data in some Track has been changed so the row
     * value list should be updated.
     */
    public void rowDataChanged() {
        initData();
        fireTableDataChanged();
    }
}
