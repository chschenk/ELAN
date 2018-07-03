package mpi.eudico.client.util;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


public class SubHeaderTableCellRenderer extends DefaultTableCellRenderer {

    
    /**
     * Calls the super implementation unless the value is of type TableSubHeaderObject.
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
	public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (value instanceof TableSubHeaderObject) {

            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            	} else {
            	    super.setForeground(table.getForeground());
            	    super.setBackground(table.getBackground());
            	}
             setFont(table.getFont().deriveFont(Font.BOLD, table.getFont().getSize() + 2));

            setValue(value);
            
            return this;
        }
        
        return super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
    }
}
