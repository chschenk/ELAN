package mpi.eudico.client.annotator.tier;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import mpi.eudico.client.util.CheckBoxTableCellRenderer;

/**
 * Implement a common tier table that goes nicely with {@link TierExportTableModel}.
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class TierExportTable extends JTable {
	protected DefaultTableModel model;
	int selectionMode;

	/**
	 * Constructor for a JTable with checkboxes and tier names.
	 * As a model, preferably pass a TierExportTableModel.
	 * <p>
	 * Some users define IDs for the columns, and some define more than 2 columns,
	 * but in all cases	column 0 is the checkbox column
	 * and column 1 is the String column (usually a tier name).
	 * By default the table header is not shown; use
	 * {@code TierExportTable(model, true)} to show it. 
	 * <p>
	 * The column identifiers must be set before creating the table,
	 * since doing so erases any cell editors and renderers this
	 * table sets.
	 * <p>
	 * Each row of data in the model consists of a Boolean and a String
	 * (and for some users, more Objects).
	 * 
	 * @param model a TierExportTableModel, typically.
	 */
	public TierExportTable(DefaultTableModel model) {
		this(model, ListSelectionModel.SINGLE_INTERVAL_SELECTION, false);
	}
	
	/**
	 * @param model
	 * @param showTableHeader By default, the table header is not shown.
	 *                        Use this constructor with {@code true} to show it.
	 * @see #TierExportTable(DefaultTableModel)
	 */
	public TierExportTable(DefaultTableModel model, boolean showTableHeader) {
		this(model, ListSelectionModel.SINGLE_INTERVAL_SELECTION, showTableHeader);
	}

	/**
	 * @param model
	 * @param selectionMode One of the ListSelectionModel.*_SELECTION values
	 * @see #TierExportTable(DefaultTableModel)
	 * @see #setSelectionMode(int mode)
	 */
	public TierExportTable(DefaultTableModel model, int selectionMode) {
		super(model);
		this.model = model;
		init(selectionMode, false);
	}
	
	public TierExportTable(DefaultTableModel model, int selectionMode,
			boolean showTableHeader) {
		super(model);
		this.model = model;
		init(selectionMode, showTableHeader);
	}
	
	public void init(int selectionMode, boolean showTableHeader) {
		if (model.getColumnCount() < 2) {
			model.setColumnCount(2);
		}

        DefaultCellEditor cellEd = new DefaultCellEditor(new JCheckBox());
        final TableColumn column0 = this.getColumnModel().getColumn(TierExportTableModel.CHECK_COL);
		column0.setCellEditor(cellEd);
        column0.setCellRenderer(new CheckBoxTableCellRenderer());
        column0.setMaxWidth(30);
        this.setSelectionMode(selectionMode);
        this.getSelectionModel().setSelectionMode(selectionMode);
        this.setShowVerticalLines(false);
        if (!showTableHeader) {
        	this.setTableHeader(null);
        }

        // Set up listeners to select checkboxes when their row is selected.
        // Actually, since JTable is already a listener to these events,
        // we don't need to add ourselves again.
        //this.getSelectionModel().addListSelectionListener(this);
        //this.getColumnModel().getColumn(0).getCellEditor()
        //         .addCellEditorListener(this);
	}
	
	/**
	 * Set the table to allow either a single checked tier
	 * (ListSelectionModel.SINGLE_SELECTION)
	 * or multiple
	 * (ListSelectionModel.*_INTERVAL_SELECTION).
	 * The GUI selection of rows of the actual table is not affected:
	 * call table.getSelectionModel().setSelectionMode(selectionMode)
	 * for that, as usual.
	 */
    @Override
	public void setSelectionMode(int mode) {
    	selectionMode = mode;
    }
    
    public int getSelectionMode() {
    	return selectionMode;
    }

	/**
	 * Forward some calls to the selection model.
	 */
    public void setSelectionInterval(int from, int to) {
        this.getSelectionModel().setSelectionInterval(from, to);
	}

	/**
	 * Add a listener to the selection model.
	 * 
	 * @param listener
	 */
	public void addListSelectionListener(ListSelectionListener listener) {
        this.getSelectionModel().addListSelectionListener(listener);
	}
	
    
    /**
     * Updates the checked state of the export checkboxes.
     * All checkboxes in range of the selection are checked
     * (none are unchecked).
     * Except for SINGLE_SELECTION, then only the single selected one is
     * checked and all others are unchecked.
     *
     * @param lse the list selection event
     */
    @Override // ListSelectionListener
	public void valueChanged(ListSelectionEvent lse) {
    	super.valueChanged(lse);
    	
        if ((model != null) && lse.getValueIsAdjusting()) {
            if (selectionMode == ListSelectionModel.SINGLE_SELECTION) {
                int i = this.getSelectedRow();

                if (i > -1) {
                    for (int j = 0; j < this.getRowCount(); j++) {
                        if (j == i) {
                        	model.setValueAt(Boolean.TRUE, j, TierExportTableModel.CHECK_COL);
                        } else {
                        	model.setValueAt(Boolean.FALSE, j, TierExportTableModel.CHECK_COL);
                        }
                    }
                    this.revalidate();
                }
            } else {
                int b = lse.getFirstIndex();
                int e = lse.getLastIndex();
                
                // IF we could check here if the user clicked in the checkbox
                // or elsewhere in the row, we could avoid setting the checkbox
                // if the user is just clicking it to unset it.

                for (int i = b; i <= e; i++) {
                    if (this.isRowSelected(i)) {
                        model.setValueAt(Boolean.TRUE, i, TierExportTableModel.CHECK_COL);
                    }
                }
            }
        }
    }

    /**
     * Ensures that only one checkbox is selected in 'single tier' mode.
     *
     * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
     */
    @Override // CellEditorListener
	public void editingStopped(ChangeEvent e) {
    	super.editingStopped(e);
    	
        if (selectionMode == ListSelectionModel.SINGLE_SELECTION) {
            int i = this.getSelectedRow();

            if (i >= 0) {
                final int rowCount = model.getRowCount();
				for (int j = 0; j < rowCount; j++) {
                    if (j != i) {
                    	model.setValueAt(Boolean.FALSE, j, TierExportTableModel.CHECK_COL);
                    }
                }
            }

        }
    }

    /**
     * Ignored (for the editor is a checkbox)
     *
     * @see javax.swing.event.CellEditorListener#editingCanceled(javax.swing.event.ChangeEvent)
     */
    @Override // CellEditorListener
	public void editingCanceled(ChangeEvent e) {
    	super.editingCanceled(e);
    }
}
