package mpi.eudico.client.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
 

public class ButtonCellEditor extends DefaultCellEditor {
	
	protected JButton button;
	  
	public ButtonCellEditor(JCheckBox checkBox) {
		super(checkBox);
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
		                   boolean isSelected, int row, int column) {
		if (value == null) {
		   	return null;
		}
		
		button = (JButton)value;
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});		
		
		return (Component)value;
	}
	
	@Override
	public Object getCellEditorValue() {	
		return button;
	}
		 
	@Override
	public void fireEditingStopped() {
		super.fireEditingStopped();
	}
}

	 
