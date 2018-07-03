package mpi.eudico.client.annotator.viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

// Methods for hiding selected table columns.
// Adapted from Stackoverflow: 
// http://stackoverflow.com/questions/6793257/add-column-to-exiting-tablemodel/6798013#6798013
//
// I changed the method of hiding the columns from removing them from the
// TableColumnModel to setting their width to 0. That way, we avoid the
// problem of remembering the correct location for each column (which was
// insufficient in the example code anyway). The user can rearrange the order
// of the columns so keeping track of where a removed column would belong
// becomes somewhat complicated.
//
// Alternatively, a custom TableColumnModel could be made based on
// http://www.stephenkelvin.de/XTableColumnModel/ .

public class TableColumnHider extends MouseAdapter {
    	
	   private static class IndexedColumn {
	        private TableColumn column;
	        private int minWidth, preferredWidth, maxWidth;
	        private boolean resizable;

	        public IndexedColumn(Integer index, TableColumn column) {
	            this.column = column;
	            this.minWidth = column.getMinWidth();
	            this.preferredWidth = column.getPreferredWidth();
	            this.maxWidth = column.getMaxWidth();
	            this.resizable = column.getResizable();
	        }
	        
	        public void restore() {
	        	column.setMaxWidth(maxWidth);
	        	column.setMinWidth(minWidth);
	        	column.setPreferredWidth(preferredWidth);
	        	column.setResizable(resizable);
	        }
	    }
	   
		private TableColumnModel tcm;
	    private Map<String, TableColumnHider.IndexedColumn> hidden =
	        new HashMap<String, TableColumnHider.IndexedColumn>();
		
		public TableColumnHider(JTable table) {
			this.tcm = table.getColumnModel();
		}
		
	    public void hide(String columnName) {
	        // If it is already hidden, don't do anything.
	        if (hidden.containsKey(columnName)) {
	        	return;
	        }
	        try {
		        int index = tcm.getColumnIndex(columnName); // column number in the view
		        TableColumn column = tcm.getColumn(index);
		        TableColumnHider.IndexedColumn ic = new IndexedColumn(index, column);
		        hidden.put(columnName, ic); // ignore return value; the check above ensures it is null
		        column.setMinWidth(0);
		        column.setMaxWidth(0);
		        column.setResizable(false); // to prevent resizing it back into view
	        } catch (IllegalArgumentException ex) {
	        	// apparently the preferences contain an unknown column name
	        }
	    }

	    public void show(String columnName) {
	        TableColumnHider.IndexedColumn ic = hidden.remove(columnName);
	        if (ic != null) {
	        	ic.restore();
	        }
	    }
			
//		public JPanel newPanel(String [] columnNames) {
//			JPanel checkBoxes = new JPanel();
//	        for (int i = 0; i < columnNames.length; i++) {
//	            JCheckBox checkBox = new JCheckBox(columnNames[i]);
//	            checkBox.setSelected(true);
//	            checkBox.addActionListener(new ActionListener() {
//	
//	                @Override
//	                public void actionPerformed(ActionEvent evt) {
//	                    JCheckBox cb = (JCheckBox) evt.getSource();
//	                    String columnName = cb.getText();
//	
//	                    if (cb.isSelected()) {
//	                        show(columnName);
//	                    } else {
//	                        hide(columnName);
//	                    }
//	                }
//	            });
//	            checkBoxes.add(checkBox);
//	        }
//			return checkBoxes;
//		}

		JPopupMenu popup;
		
		/**
		 * Create a new popup menu to handle selecting the columns.
		 * <p>
		 * If you generate only a single such menu, you can use this
		 * TableColumnHider as a MouseListener to bring up the menu.
		 * 
		 * @param columnNames The names you wish to be able to hide.
		 * @return
		 */
		
		public JPopupMenu newPopupMenu(String[] columnNames) {
			popup = new JPopupMenu();
			
	        for (int i = 0; i < columnNames.length; i++) {
	            JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem(columnNames[i]);
	            checkBox.setSelected(true);
	            checkBox.addActionListener(new ActionListener() {
	
	                @Override
	                public void actionPerformed(ActionEvent evt) {
	                	JCheckBoxMenuItem cb = (JCheckBoxMenuItem) evt.getSource();
	                    String columnName = cb.getText();
	
	                    if (cb.isSelected()) {
	                        show(columnName);
	                    } else {
	                        hide(columnName);
	                    }
	                }
	            });
	            popup.add(checkBox);
	        }
			return popup;
		}
				
		/**
		 * Set the checked/selected state of a named item in the popup menu.
		 * 
		 * @param name
		 * @param value
		 */
		private void setSelected(String name, boolean value) {
			if (popup == null) {
				return;
			}
			
			int n = popup.getComponentCount();
			
			for (int i = 0; i < n; i++) {
				Component c = popup.getComponent(i);
				if (c instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem mi = (JCheckBoxMenuItem)c;
					if (mi.getText().equals(name)) {
						mi.setSelected(value);
						return;
					}
				}
			}
		}
		
		/**
		 * The mouse pressed event handler, for showing the popup menu.
		 * Try not to react on Command + LeftClick, because we want to keep that available
		 * for multiple (de)selection in the table.
		 * Unfortunately, Command also pretends to be the Right Mouse Button.
		 * Therefore this weird extra check.
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				if (!(SwingUtilities.isLeftMouseButton(e) && 
					  SwingUtilities.isRightMouseButton(e))) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
                return;
			}
		}
		
		/**
		 * Return a list of currently hidden columns, so it can be saved to preferences.
		 */
		public List<String> getPreferences() {
			List<String> pref = new ArrayList<String>();
			
			pref.addAll(hidden.keySet());
			
			return pref;
		}
		
		/**
		 * Apply the preferences object, which should be a List<String>.
		 * <p>
		 * Unhide hidden columns not given in the list.
		 * Hide columns given in the list.
		 * <p>
		 * If there is a popup menu, adjust checkmarks accordingly.
		 * If there is more than one popup menu, only the latest one is adjusted.
		 * 
		 * @param pref
		 */
		public void applyPreferences(List<String> hiddenList) {
			if (hiddenList != null) {
				// Unhide whatever is not in the given list
				for (String s : hidden.keySet()) {
					if (!hiddenList.contains(s)) {
						show(s);
						setSelected(s, true);
					}
				}
				// Hide everything from the list
				for (String s : hiddenList) {
					hide(s);
					setSelected(s, false);
				}
			}
		}
    }