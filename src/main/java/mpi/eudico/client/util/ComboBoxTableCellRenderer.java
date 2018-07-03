package mpi.eudico.client.util;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ComboBoxTableCellRenderer extends JComboBox implements
		TableCellRenderer {

	public ComboBoxTableCellRenderer() {
		super();
		setOpaque(true);
	}

	public ComboBoxTableCellRenderer(Object[] values) {
		super(values);
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            super.setBackground(table.getSelectionBackground());
        } else {
            super.setBackground(table.getBackground());
        }
        // assume that the value is in the list of values of the combobox
		setSelectedItem(value);
		return this;
	}

}
