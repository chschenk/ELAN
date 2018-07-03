package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * A table model for tier objects.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class TierTableModel extends AbstractTableModel {
    /** table column and label identifiers */
    public static final String LABEL_PREF = "EditTierDialog.Label.";

    /** name column */
    public static final String NAME = "TierName";

    /** parent column */
    public static final String PARENT = "Parent";

    /** participant */
    public static final String PARTICIPANT = "Participant";
    
    /** annotator */
    public static final String ANNOTATOR = "Annotator";

    /** linguistic type column */
    public static final String TYPE = "LinguisticType";

    /** input method (formerly language) */
    public static final String INPUT_METHOD = "Language";

    /** content language */
    public static final String CONTENT_LANGUAGE = "ContentLanguage";

    /** empty or not applicable value */
    public static final String N_A = "-";
    
    private List<TierImpl> tierList;
    private List<String> columnIds;
    private List<List<String>> tableData;
    private List<Class<String>> classes;

    /**
     * No-arg constructor
     */
    public TierTableModel() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param tiers a vector containing tier objects
     */
    public TierTableModel(List<TierImpl> tiers) {
        this(tiers, null);
    }

    /**
     * Constructor.
     *
     * @param tiers a vector containing tier objects
     * @param columns an array of column names that should be visible in the
     *        table
     */
    public TierTableModel(List<TierImpl> tiers, String[] columns) {
        if (tiers == null) {
            tierList = new ArrayList<TierImpl>(0);
        } else {
            tierList = new ArrayList<TierImpl>(tiers);
        }

        columnIds = new ArrayList<String>();
        classes = new ArrayList<Class<String>>();

        if (columns != null) {
            for (String column : columns) {
                if (column.equals(NAME)) {
                    columnIds.add(NAME);
                    classes.add(String.class);
                } else if (column.equals(PARENT)) {
                    columnIds.add(PARENT);
                    classes.add(String.class);
                } else if (column.equals(TYPE)) {
                    columnIds.add(TYPE);
                    classes.add(String.class);
                } else if (column.equals(PARTICIPANT)) {
                    columnIds.add(PARTICIPANT);
                    classes.add(String.class);
                } else if (column.equals(ANNOTATOR)) {
                    columnIds.add(ANNOTATOR);
                    classes.add(String.class);
                } else if (column.equals(INPUT_METHOD)) {
                    columnIds.add(INPUT_METHOD);
                    classes.add(String.class);
                } else if (column.equals(CONTENT_LANGUAGE)) {
                    columnIds.add(CONTENT_LANGUAGE);
                    classes.add(String.class);
                }
            }
        } else {
            columnIds.add(NAME);
            columnIds.add(PARENT);
            columnIds.add(TYPE);
            columnIds.add(PARTICIPANT);
            columnIds.add(ANNOTATOR);
            columnIds.add(INPUT_METHOD);
            columnIds.add(CONTENT_LANGUAGE);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
        }

        initData();
    }

    private void initData() {
        tableData = new ArrayList<List<String>>(tierList.size());

        TierImpl tier;

        for (int i = 0; i < tierList.size(); i++) {
            tier = tierList.get(i);
            addRowData(tier);
        }

        fireTableDataChanged();
    }

    private void addRowData(TierImpl tier) {
        if (tier == null) {
            return;
        }

        List<String> rowData = new ArrayList<String>(6);

        int index = columnIds.indexOf(NAME);

        if (index > -1) {
            rowData.add(index, tier.getName());
        }

        index = columnIds.indexOf(PARENT);

        if (index > -1) {
            if (tier.getParentTier() == null) {
                rowData.add(index, N_A);
            } else {
                rowData.add(index, tier.getParentTier().getName());
            }
        }

        index = columnIds.indexOf(TYPE);

        if (index > -1) {
            rowData.add(index, tier.getLinguisticType().getLinguisticTypeName());
        }

        index = columnIds.indexOf(PARTICIPANT);

        if (index > -1) {
            rowData.add(index, tier.getParticipant());
        }
        
        index = columnIds.indexOf(ANNOTATOR);

        if (index > -1) {
            rowData.add(index, tier.getAnnotator());
        }

        index = columnIds.indexOf(INPUT_METHOD);

        if (index > -1) {
        	Locale loc = tier.getDefaultLocale();
        	if (loc != null) {
        		rowData.add(index, loc.getDisplayName());
        	} else {
        		rowData.add(index, N_A);
        	}
        }

        tableData.add(rowData);

        index = columnIds.indexOf(CONTENT_LANGUAGE);

        if (index > -1) {
        	String lang = tier.getLangRef();
            rowData.add(index, lang == null ? N_A : lang);
        }

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
     * the number of columns = the size of column ids list
     *
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
	public int getColumnCount() {
        return columnIds.size();
    }

    /**
     * Finds the List of the specified row and returns the value at the
     * column index.
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
	public Class<?> getColumnClass(int columnIndex) {
        if ((columnIndex < 0) || (columnIndex >= classes.size())) {
            return null;
        }

        return classes.get(columnIndex);
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
     * Note: silently returns instead of throwing an
     * ArrayIndexOutOfBoundsException
     *
     * @param rowIndex the row to remove
     */
    public void removeRow(int rowIndex) {
        if ((rowIndex >= 0) && (rowIndex < tableData.size())) {
            tableData.remove(rowIndex);
            tierList.remove(rowIndex);
            fireTableDataChanged();
        }
    }

    /**
     * Removes all rows from the table.
     */
    public void removeAllRows() {
        tableData.clear();
        tierList.clear();
        fireTableDataChanged();
    }

    /**
     * Adds a row with the data from the tier.
     *
     * @param tier the tier to add to the model
     */
    public void addRow(TierImpl tier) {
        if ((tier == null) || tierList.contains(tier)) {
            return;
        }

        tierList.add(tier);
        addRowData(tier);
        fireTableDataChanged();
    }

    /**
     * Adds a row with the data from the tier
     *
     * @param tier the tier to add to the model
     */
    public void addTier(TierImpl tier) {
        addRow(tier);
    }

    /**
     * Notification that the data in some Tier has been changed so
     * the row value list should be updated.
     */
    public void rowDataChanged() {
        initData();
        fireTableDataChanged();
    }
}
