package mpi.eudico.client.util;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class RadioButtonCellEditor  extends  DefaultCellEditor
		implements ItemListener {
	
	private JRadioButton button;
	 
	  public RadioButtonCellEditor(JCheckBox checkBox) {
	    super(checkBox);
	  }
	 
	  @Override
	public Component getTableCellEditorComponent(JTable table, Object value,
	                   boolean isSelected, int row, int column) {
	    
	    if (value instanceof SelectEnableObject) {
	    	if (button == null) {
	    		button = new JRadioButton(value.toString());
	            button.addItemListener(this);
	    	} else {
	    		button.setText(value.toString());
	    	}
            button.setSelected(((SelectEnableObject)value).isSelected());
            button.setEnabled(((SelectEnableObject)value).isEnabled());
        }
        
        return button;
	  }
	  
	  @Override
	public Object getCellEditorValue() {
		 // button.removeItemListener(this);
		  SelectEnableObject<String> seo = new SelectEnableObject<String>(button.getText(), button.isSelected(), button.isEnabled());	
	      return seo;
	    }
 

	  @Override
	public void itemStateChanged(ItemEvent e) {		  
		  super.fireEditingStopped();
	  }
	}