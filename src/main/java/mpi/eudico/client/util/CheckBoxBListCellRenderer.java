package mpi.eudico.client.util;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A specialized renderer based on a check box that supports both selected and 
 * enabled state. Designed to be used in a JComboBox (or JList)
 * 
 * @author Han Sloetjes
 */
public class CheckBoxBListCellRenderer extends JCheckBox implements ListCellRenderer {

	/**
	 * Constructor
	 */
	public CheckBoxBListCellRenderer() {
		super();
	}

	/**
	 * Configures the JCheckBox and returns it.
	 * 
	 * @param list the list
	 * @param value the value to render
	 * @param index index in the list, -1 in the case of a combobox without popup
	 * @param isSelected the selected state, in the combo box mouse over
	 * @param cellHasFocus ignored for now
	 * 
	 * @return the JCheckbox
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof SelectableObject) {
			SelectableObject seo = (SelectableObject) value;
			setText(seo.getValue().toString());
			setSelected(seo.isSelected());
			
			if (seo instanceof SelectEnableObject) {
				setEnabled(((SelectEnableObject) seo).isEnabled());
				
				if (isEnabled()) {
					if (isSelected) {
						setBackground(list.getSelectionBackground());
						setForeground(list.getSelectionForeground());
					} else {
						setBackground(list.getBackground());
						setForeground(list.getForeground());
					}
				} else {
					// else the default checkbox disabled foreground color is used
					setBackground(list.getBackground());
				}

			} else {
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
			}
		} else {
			setText(value.toString());
			//setSelected(isSelected);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
		}

		return this;
	}

}
