package mpi.search.gui;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class DescriptedObjectListCellRenderer extends BasicComboBoxRenderer {
    /**
     * Adds description of Object as tooltip to the list
     *
     */
    @Override
	public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean celHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, celHasFocus);
        
        if (isSelected && value instanceof DescriptedObject) {
            list.setToolTipText(((DescriptedObject)value).getDescription());            
        }
        
        return this;
    }

}
