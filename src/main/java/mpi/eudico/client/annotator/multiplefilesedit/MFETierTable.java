package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesBox;

@SuppressWarnings("serial")
public class MFETierTable extends MFETable {
	private RowModel rm;
	MyRecentLanguagesEditor langEditor;
	MyRecentLanguagesRenderer langRenderer;
	
	public MFETierTable(MFEModel model) {
		super(model);
		setModel(new TableByTierModel(model));
		this.setRowEditorModel(new RowModel());
	}
	

	
	public void initCombobox() {
		// The model may change behind our backs due to the EAFLoadThread.
		synchronized (model) {
			int row_count = model.getTierRowCount(); // same as getModel()/* a TableByTierModel */.getRowCount();
			for(int i=0;i<row_count;i++) {
				String[] linguistic_types = model.getLinguisticTypeNamesByTier(i);
				this.rm.addEditorForRow(i, new MyComboBoxEditor(linguistic_types));
				this.rm.addRendererForRow(i, new MyComboBoxRenderer(linguistic_types));
			}
			repaint();
		}
	}
	
	public void newRow(int new_row) {
		String[] linguistic_types = model.getLinguisticTypeNamesByTier(new_row);
		this.rm.addEditorForRow(new_row, new MyComboBoxEditor(linguistic_types));
		this.rm.addRendererForRow(new_row, new MyComboBoxRenderer(linguistic_types));
	}
	
	public void setRowEditorModel(RowModel rm)
	{
		this.rm = rm;
	}

	public RowModel getRowEditorModel()
	{
		return rm;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int col) {
		row = this.convertRowIndexToModel(row);
		if (col == MFEModel.TIER_TYPECOLUMN) {
			TableCellEditor tmpEditor = null;
			if (rm != null) {
				tmpEditor = rm.getEditor(row);
			}
			if (tmpEditor != null) {
				return tmpEditor;
			}
		}
		if (col == MFEModel.TIER_LANGUAGECOLUMN) {
			if (langEditor == null) {
				langEditor = new MyRecentLanguagesEditor();
			}
			return langEditor;
		}
		return super.getCellEditor(row, col);
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int col) {
		row = this.convertRowIndexToModel(row);
		if (col == MFEModel.TIER_TYPECOLUMN) {
			TableCellRenderer tmpRenderer = null;
			if (rm != null) {
				tmpRenderer = rm.getRenderer(row);
			}
			if (tmpRenderer != null) {
				return tmpRenderer;
			}
		}
		if (col == MFEModel.TIER_LANGUAGECOLUMN) {
			if (langRenderer == null) {
				langRenderer = new MyRecentLanguagesRenderer();
			}
			return langRenderer;
		}
		return super.getCellRenderer(row, col);
	}
	
	/**
	 * Custom cell renderers to render a JComboBox inside a table cell
	 */
	private class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {

		public MyComboBoxRenderer(String[] items) {
            super(items);
        }
    
        @Override
		public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
    
            if(!model.isTypeConsistentTier(row)) {
            	setSelectedItem(ElanLocale.getString("MFE.Multiple"));
            	setEnabled(false);
            } else {
            	// Select the current value
            	setSelectedItem(value);
            	setEnabled(true);
            }

            return this;
        }
    }
    
	private class MyComboBoxEditor extends DefaultCellEditor {
		public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }

	/**
	 * I made an attempt at combining a CellEditor and a TableCellRenderer,
	 * sharing the same RecentLanguagesBox (JComboBox) for editing and rendering.
	 * 
	 * However, at least on MacOS, the result is that the renderer doesn't
	 * render the typical button-like look associated with the combobox.
	 * 
	 * Using two RecentLanguagesBoxes seems wasteful but at least it works. And
	 * fortunately these editors and renderers can be shared amongst all rows,
	 * contrary to the Linguistic Type column, where they could be all
	 * different.
	 * 
	 * @author olasei
	 */
	private class MyRecentLanguagesEditor extends DefaultCellEditor
	                                      /*implements TableCellRenderer*/ {
		private RecentLanguagesBox languagebox;
		
		// DefaultCellEditor
		public MyRecentLanguagesEditor() {
            super(new RecentLanguagesBox(null));
            languagebox = (RecentLanguagesBox)super.getComponent(); // the RecentLanguagesBox.
            languagebox.addNoLanguageItem();
        }
		
//		@Override // TableCellRenderer
//        public Component getTableCellRendererComponent(JTable table, Object value,
//                boolean isSelected, boolean hasFocus, int row, int column) {
//            if (isSelected) {
//            	languagebox.setForeground(table.getSelectionForeground());
//            	languagebox.setBackground(table.getSelectionBackground());
//            } else {
//            	languagebox.setForeground(table.getForeground());
//            	languagebox.setBackground(table.getBackground());
//            }
//    
//        	// Select the current value
//        	languagebox.setSelectedItem((String)value);
//        	languagebox.setEnabled(true);
//
//            return languagebox;
//        }
    }
	
	private class MyRecentLanguagesRenderer extends RecentLanguagesBox implements TableCellRenderer {
		public MyRecentLanguagesRenderer() {
			super(null);
			addNoLanguageItem();
		}

		@Override // TableCellRenderer
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			// Select the current value.
			setSelectedItem((String)value);
			setEnabled(true);

			return this;
		}
	}
}
