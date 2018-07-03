package mpi.eudico.client.annotator.tier;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.util.SelectEnableObject;
import mpi.eudico.server.corpora.clom.Tier;


/**
 * A simple table model that denotes cells with a Boolean or SelectEnableObject value as editable.
 * <p>
 * The typical use case is for tables that have rows with a checkmark and a string.
 * There are a few helper functions geared towards this use case, but using it this
 * way is not compulsory.
 * <p>
 * A good subclass of JTable to use is {@link TierExportTable}.
 *
 * @author Han Sloetjes
 * @author Olaf Seibert
 */
@SuppressWarnings("serial")
public class TierExportTableModel extends DefaultTableModel {
	/** The column number of the checkbox */
	public static final int CHECK_COL = 0;
	/** The column number of the tier name */
    public static final int NAME_COL = 1;

	/**
     * Returns true for the Boolean columns, false for tall other columns.
     *
     * @param row the row
     * @param column the column
     *
     * @return true if the value is of type Boolean, false otherwise
     *
     * @see #getValueAt
     */
    @Override
	public boolean isCellEditable(int row, int column) {
    	final Object value = getValueAt(row, column);
    	
    	return value instanceof SelectEnableObject || value instanceof Boolean;
    }
    
    /**
     * Convenience method to add an unselected tier name to the model.
     */
    public void addRow(String name) {
        addRow(new Object[] { Boolean.FALSE, name });
    }

    /**
     * Convenience method to add a tier name to the model.
     */
    public void addRow(Boolean selected, String name) {
        addRow(new Object[] { selected, name });
    }

    /**
     * Convenience method to add a tier name to the model.
     */
    public void addRow(boolean selected, String name) {
        addRow(new Object[] { Boolean.valueOf(selected), name });
    }
	
    /**
     * Convenience method to add a tier name and an extra column to the model.
     */
    public void addRow(Boolean selected, String name, Object col2) {
        addRow(new Object[] { selected, name, col2 });
    }

	/**
	 * Extract the tier names and place them in rows in the table.
	 * The first one is selected.
	 * 
	 * @param v
	 */
	public void extractTierNames(List<? extends Tier> v) {
        for (int i = 0; i < v.size(); i++) {
            Tier t = v.get(i);

            addRow(i == 0, t.getName());
        }
	}
	
    /**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    public List<String> getSelectedTiers() {
        List<String> tiers = new ArrayList<String>();
        Object selObj = null;
        Object nameObj = null;

        final int rowCount = this.getRowCount();
		for (int i = 0; i < rowCount; i++) {
            selObj = this.getValueAt(i, CHECK_COL);

            if (selObj == Boolean.TRUE) {
                nameObj = this.getValueAt(i, NAME_COL);

                if (nameObj instanceof String) {
                    tiers.add((String)nameObj);
                }
            }
        }

        return tiers;
    }    
}
