package mpi.eudico.client.annotator.type;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A TableModel for a table displaying linguistic type information.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class LinguisticTypeTableModel extends AbstractTableModel {
    /** table column and label identifiers */
    public static final String LABEL_PREF = "EditTypeDialog.Label.";

    /** name of the type */
    public static final String NAME = "Type";

    /** the stereotype */
    public static final String STEREOTYPE = "Stereotype";

    /** time-alignable */
    public static final String TIME_ALIGNABLE = "TimeAlignable";

    /** the controlled vocabulary name */
    public static final String CV_NAME = "CV";

    /** the ISO DCR data category id */
    public static final String DC_ID = "DCR";
    
    /** selected state */
    public static final String SELECT = "Selected";

    /** empty or not applicable value */
    public static final String N_A = "-";
    private ArrayList<LinguisticType> types;
    private int[] currentShownStereoTypes;
    private List<String> columnIds;
    private List<List<Object>> data;
    private List<Class> classes;

    /**
     * Creates a new LinguisticTypeTableModel instance
     */
    public LinguisticTypeTableModel() {
        this(null);
    }

    /**
     * Creates a new LinguisticTypeTableModel instance
     *
     * @param allTypes the Vector of Linguistic Types
     */
    public LinguisticTypeTableModel(List<LinguisticType> allTypes) {
        this.types = (allTypes != null) ? new ArrayList<LinguisticType>(allTypes)
                                        : new ArrayList<LinguisticType>(0);

        columnIds = new ArrayList<String>();

        //replaced
        //columnIds.add(ElanLocale.getString(LABEL_PREF + NAME));
        columnIds.add(SELECT);
        columnIds.add(NAME);
        columnIds.add(STEREOTYPE);
        columnIds.add(CV_NAME);
        columnIds.add(DC_ID);
        columnIds.add(TIME_ALIGNABLE);

        classes = new ArrayList<Class>(columnIds.size());
        classes.add(Boolean.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(String.class);
        classes.add(Boolean.class);

        initData();
    }

    /**
     * Creates a new LinguisticTypeTableModel instance
     *
     * @param allTypes the Vector of Linguistic Types
     * @param columns the column identifiers
     */
    public LinguisticTypeTableModel(List<LinguisticType> allTypes, String[] columns) {
        this.types = (allTypes != null) ? new ArrayList<LinguisticType>(allTypes)
                                        : new ArrayList<LinguisticType>(0);

        columnIds = new ArrayList<String>();
        classes = new ArrayList<Class>();

        if (columns != null) {
            for (String column : columns) {
                if (column.equals(SELECT)) {
                    columnIds.add(SELECT);
                    classes.add(Boolean.class);
                } else if (column.equals(NAME)) {
                    // replaced
                    // columnIds.add(ElanLocale.getString(LABEL_PREF + NAME));
                    columnIds.add(NAME);
                    classes.add(String.class);
                } else if (column.equals(STEREOTYPE)) {
                    columnIds.add(STEREOTYPE);
                    classes.add(String.class);
                } else if (column.equals(CV_NAME)) {
                    columnIds.add(CV_NAME);
                    classes.add(String.class);
                } else if (column.equals(DC_ID)) {
                    columnIds.add(DC_ID);
                    classes.add(String.class);
                } else if (column.equals(TIME_ALIGNABLE)) {
                    columnIds.add(TIME_ALIGNABLE);
                    classes.add(Boolean.class);
                }
            }
        } else {
            columnIds.add(SELECT);
            columnIds.add(NAME);
            columnIds.add(STEREOTYPE);
            columnIds.add(CV_NAME);
            columnIds.add(DC_ID);
            columnIds.add(TIME_ALIGNABLE);

            classes.add(Boolean.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(String.class);
            classes.add(Boolean.class);
        }

        initData();
    }

    /**
     * @see #initData(int[])
     */
    private void initData() {
        //data = new ArrayList(types.size());
        initData(currentShownStereoTypes);
    }

    /**
     * Add rows to the model's data list, eventually limiting the list to the
     * Linguistic Types  of the stereotypes that are specified by argument
     * <code>theseTypes</code>.
     *
     * @param theseTypes the types as int's (as specified in
     *        <code>mpi.eudico.server.corpora.clomimpl.type.Constraint</code>)
     */
    private void initData(int[] theseTypes) {
        data = new ArrayList<List<Object>>(types.size());

        LinguisticType type;

        for (int i = 0; i < types.size(); i++) {
            type = types.get(i);

            boolean add = (theseTypes == null); // add if theseTypes == null

            if (!add) {
                int stereotype = -1; //no constraints

                if (type.hasConstraints()) {
                    stereotype = type.getConstraints().getStereoType();
                }

                for (int theseType : theseTypes) {
                    if (stereotype == theseType) {
                        add = true;

                        break;
                    }
                }
            }

            if (add) {
                addRowData(type);
            }
        }

        fireTableDataChanged();
    }

    /**
     * Adds information taken from a LinguisticType object to the model. Only
     * information for the visible columns is used.
     *
     * @param type the LinguisticType to add
     */
    private void addRowData(LinguisticType type) {
        if (type == null) {
            return;
        }

        List<Object> rowData = new ArrayList<Object>(getColumnCount());

        if (columnIds.indexOf(SELECT) > -1) {
            rowData.add(columnIds.indexOf(SELECT), Boolean.FALSE);
        }

        if (columnIds.indexOf(NAME) > -1) {
            rowData.add(columnIds.indexOf(NAME), type.getLinguisticTypeName());
        }

        if (columnIds.indexOf(STEREOTYPE) > -1) {
            String stereoTypeName = N_A;

            if (type.hasConstraints()) {
                stereoTypeName = Constraint.stereoTypes[type.getConstraints()
                                                            .getStereoType()];
            }

            rowData.add(columnIds.indexOf(STEREOTYPE), stereoTypeName);
        }

        if (columnIds.indexOf(CV_NAME) > -1) {
            String cvName = N_A;

            if (type.isUsingControlledVocabulary()) {
                cvName = type.getControlledVocabularyName();
            }

            rowData.add(columnIds.indexOf(CV_NAME), cvName);
        }
        
        if (columnIds.indexOf(DC_ID) > -1) {
            String dcId = N_A;

            if (type.getDataCategory() != null) {
                dcId = type.getDataCategory();
            }

            rowData.add(columnIds.indexOf(DC_ID), dcId);
        }

        if (columnIds.indexOf(TIME_ALIGNABLE) > -1) {
            rowData.add(columnIds.indexOf(TIME_ALIGNABLE),
            	Boolean.valueOf(type.isTimeAlignable()));
        }

        data.add(rowData);
    }

    /**
     * Sets which linguistic types to add to the table model.
     *
     * @param theseTypes an array of stereotypes identifiers
     */
    public void showOnlyStereoTypes(int[] theseTypes) {
        currentShownStereoTypes = theseTypes;
        initData();
    }

    /**
     * Returns the number of rows (== the size of the data list).
     *
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
	public int getRowCount() {
        return data.size();
    }

    /**
     * Returns the number of columns (== the size of the list of column id's).
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
	public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= data.size()) || (columnIndex < 0) ||
                (columnIndex >= columnIds.size())) {
            return null;
        }

        List<Object> row = data.get(rowIndex);

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
	public Class getColumnClass(int columnIndex) {
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
        if ((rowIndex >= 0) && (rowIndex < data.size())) {
            data.remove(rowIndex);
            types.remove(rowIndex);
            fireTableDataChanged();
        }
    }

    /**
     * Removes all rows from the table.
     */
    public void removeAllRows() {
        data.clear();
        types.clear();
        fireTableDataChanged();
    }
    
    /**
     * Adds a row with the data of the LinguisticType to the model, if it's
     * stereotype is in the list of 'stereotypes to show'
     *
     * @param type the new LinguisticType
     *
     * @see #addLinguisticType(LinguisticType)
     */
    public void addRow(LinguisticType type) {
        if ((type == null) || types.contains(type)) {
            return;
        }

        types.add(type);

        if (currentShownStereoTypes == null) {
            addRowData(type);
            fireTableDataChanged();
        } else {
            int stereotype = -1; //no constraints

            if (type.hasConstraints()) {
                stereotype = type.getConstraints().getStereoType();
            }

            for (int currentShownStereoType : currentShownStereoTypes) {
                if (stereotype == currentShownStereoType) {
                    addRowData(type);
                    fireTableDataChanged();

                    break;
                }
            }
        }
    }

    /**
     * Adds a Linguistic Type to the Vector of Linguistic Types.
     *
     * @param type the new Linguistic Type
     *
     * @see #addRow(Linguistic Type)
     */
    public void addLinguisticType(LinguisticType type) {
        addRow(type);
    }

    /**
     * Notification that the data in some Linguistic Type has been changed so
     * the row value list should be updated.
     */
    public void rowDataChanged() {
        initData();
        fireTableDataChanged();
    }
}
