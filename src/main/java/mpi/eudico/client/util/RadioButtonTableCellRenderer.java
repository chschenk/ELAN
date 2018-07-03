package mpi.eudico.client.util;

import java.awt.Component;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


public class RadioButtonTableCellRenderer extends JRadioButton implements TableCellRenderer {

	/**
	 * Constructor
	 */
	public RadioButtonTableCellRenderer() {
		super();
		 setOpaque(true);
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof SelectEnableObject) {
			SelectEnableObject seo = (SelectEnableObject) value;
			setText(seo.getValue().toString());
			setSelected(seo.isSelected());
			setEnabled(seo.isEnabled());
		}
		
		return this;
	}
}
	
	
	

