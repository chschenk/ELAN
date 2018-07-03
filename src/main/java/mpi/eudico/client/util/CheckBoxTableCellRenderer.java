package mpi.eudico.client.util;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


/**
 * A table cell renderer that uses a JCheckBox to render a boolean  value in a
 * table cell. 
 *
 * @author Han Sloetjes
 */
public class CheckBoxTableCellRenderer extends JCheckBox
    implements TableCellRenderer {
    /**
     * Constructor. Calls super().
     */
    public CheckBoxTableCellRenderer() {
        super();
        setOpaque(true);
    }

    /**
     * Returns the component.
     *
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *      java.lang.Object, int, boolean, boolean)
     */
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setBackground(table.getBackground());
        }

        if (value instanceof Boolean) {
            super.setSelected(((Boolean) value).booleanValue());
        } else if (value instanceof String) {
            super.setSelected(((String) value).equalsIgnoreCase("true"));
        } else if(value instanceof SelectEnableObject){
        	super.setSelected(((SelectEnableObject)value).isSelected());
        	super.setText(((SelectEnableObject)value).getValue().toString());
        	super.setEnabled(((SelectEnableObject)value).isEnabled());
        } else if(value == null){
        	return null;
        }

        return this;
    }
}
