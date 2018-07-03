package mpi.eudico.client.annotator.interlinear.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelListener;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTViewerModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTViewerModelListener;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel;

/**
 * Implementation of a viewer model for interlinear blocks in the form of 
 * IGTDataModel objects.
 * Each IGTGroup represents one root annotation plus children.
 * <p>
 * Since the individual rows (groups, root annotations plus children)
 * are displayed in a JTable, this also implements a TableModel.
 * <p>
 * Creating a wrapper that just implements the TableModel
 * is difficult/impossible because the modification events need to be fired.
 * 
 * @author Han Sloetjes
 * @author olasei
 */
public class IGTViewerModelImpl implements IGTViewerModel, IGTDataModelListener, TableModel {
	/** Each row is an IGTGroup */
	private List<IGTDataModel> rowData;
	private int editingRow = -1;
	private boolean isExpanded = false;
	private List<IGTViewerModelListener> listeners;
	
	private Collection<String> hiddenTiers;
	private List<String> visibleParticipants;
	private List<String> visibleTiersCollapsed;
	
	private boolean timeCodesVisible = true;
	private boolean speakerLabelsVisible = true;
	//private boolean silenceDurationVisible = false; // not supported 
	
	/**
	 * No-arg constructor.
	 */
	public IGTViewerModelImpl() {
		rowData = new ArrayList<IGTDataModel>();
		listeners = new ArrayList<IGTViewerModelListener>(4);
	}

	/**
	 * How many rows = IGTGroups we have.
	 * Each IGTGroup represents one root annotation plus children.
	 */
	@Override
	public int getRowCount() {
		return rowData.size();
	}

	@Override
	public int getEditingRow() {
		return editingRow;
	}
	
	@Override
	public void startEditingRow(int row) {
		editingRow = row;
	}

	@Override
	public boolean getExpandedState() {
		return isExpanded;
	}

	@Override
	public void setExpandedState(boolean expanded) {
		isExpanded = expanded;
	}
	
	@Override
	public void setVisibleTiersInCollapsedState(List<String> tierNames) {
		// TODO copy the names to the list instead?
		visibleTiersCollapsed = tierNames;

	}

	@Override
	public List<String> getVisibleTiersInCollapsedState() {
		// TODO don't return a reference to the list itself, but copy values?
		return visibleTiersCollapsed;
	}

	@Override
	public void setHiddenTiers(Collection<String> tierNames) {
		// TODO copy the values to the local list instead?
		hiddenTiers = tierNames;
	}
	
	@Override
	public Collection<String> getHiddenTiers() {
		// TODO return a copy of the list instead of a reference?
		return hiddenTiers;
	}

	@Override
	public void setVisibleParticipants(List<String> partNames) {
		// TODO copy the values instead of setting the reference?
		this.visibleParticipants = partNames;
	}
	
	@Override
	public List<String> getVisibleParticipants() {
		// TODO return a copy of the list instead of a reference?
		return visibleParticipants;
	}

	
	/**
	 * Changes the visibility of one of the extra lines with time codes, speaker label etc.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.IGTViewerModel#setSpecialTierVisibility(mpi.eudico.client.annotator.interlinear.IGTTierType, boolean)
	 */
	@Override
	public void setSpecialTierVisibility(IGTTierType specialTier,
			boolean visible) {
		switch (specialTier) {
		case TIME_CODE:
			if (visible != timeCodesVisible) {			
				for (IGTDataModel dataModel : rowData) {
					dataModel.setSpecialTierVisibility(specialTier, visible);
				}
				timeCodesVisible = visible;
				if (rowData.size() > 0) {
					fireTableRowsUpdated(0, rowData.size() -1);
				}
			}
			break;
		case SPEAKER_LABEL:
			if (visible != speakerLabelsVisible) {
				for (IGTDataModel dataModel : rowData) {
					dataModel.setSpecialTierVisibility(specialTier, visible);
				}
				speakerLabelsVisible = visible;
				if (rowData.size() > 0) {
					fireTableRowsUpdated(0, rowData.size() -1);
				}
			}
			break;
			default:
				;
		}		
	}

	/**
	 * 
	 * @return the visibility of the specified extra "tier"
	 * @see mpi.eudico.client.annotator.interlinear.edit.IGTViewerModel#getSpecialTierVisibility(mpi.eudico.client.annotator.interlinear.IGTTierType)
	 */
	@Override
	public boolean getSpecialTierVisibility(IGTTierType specialTier) {
		switch (specialTier) {
		case TIME_CODE:
			return timeCodesVisible;
		case SPEAKER_LABEL:
			return speakerLabelsVisible;
			default:
				return false;
		}
	}

	/**
	 * Add a row = IGTGroup.
	 * Each IGTGroup represents one root annotation plus children.
	 */
	@Override
	public void addRow(IGTDataModel nextRowData) {
		if (nextRowData != null) {
			rowData.add(nextRowData);
			nextRowData.addIGTDataModelListener(this);
			int rowNr = rowData.size() - 1;
			fireTableRowsInserted(rowNr, rowNr);
		}
	}

	/**
	 * Insert a row = IGTGroup.
	 * Each IGTGroup represents one root annotation plus children.
	 */
	@Override
	public void insertRow(IGTDataModel nextRowData, int row) throws ArrayIndexOutOfBoundsException {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException("Insert row, index is less than zero: " +  row);
		} else if (row > rowData.size()){// if insertion point == size, then add 
			throw new ArrayIndexOutOfBoundsException("Insert row, index is greater than model size: " +  row);
		}
		if (rowData != null) {
			rowData.add(row, nextRowData);
			nextRowData.addIGTDataModelListener(this);
			fireTableRowsInserted(row, row);
		}
	}

	/**
	 * Get a row = IGTGroup.
	 * Each IGTGroup represents one root annotation plus children.
	 */
	@Override
	public IGTDataModel getRowData(int row) throws ArrayIndexOutOfBoundsException {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException("Get row, index is less than zero: " +  row);
		} else if (row > rowData.size() - 1){
			throw new ArrayIndexOutOfBoundsException("Get row, index is greater than model size: " +  row);
		}
		
		return rowData.get(row);
	}	
	
	/**
	 * Remove a row = IGTGroup.
	 * Each IGTGroup represents one root annotation plus children.
	 */
	@Override
	public IGTDataModel removeRow(int row) throws ArrayIndexOutOfBoundsException {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException("Remove row, index is less than zero: " +  row);
		} else if (row > rowData.size() - 1){
			throw new ArrayIndexOutOfBoundsException("Remove row, index is greater than model size: " +  row);
		}
		
		IGTDataModel remRowData = rowData.remove(row);
		if (remRowData != null) {
			remRowData.removeIGTDataModelListener(this);
		}
		fireTableRowsDeleted(row, row);
		
		return remRowData;
	}

	/**
	 * Remove a row = IGTGroup.
	 * Each IGTGroup represents one root annotation plus children.
	 */
	@Override
	public boolean removeRowData(IGTDataModel remRowData) {
		if (remRowData == null) {
			return false;
		}
		
		int rowNr = rowData.indexOf(remRowData);
		if (rowNr >= 0) {
			rowData.remove(rowNr);
			remRowData.removeIGTDataModelListener(this);
			fireTableRowsDeleted(rowNr, rowNr);
			
			return true;
		}
		
		return false;
	}

	
	/**
	 * Removes all rows from the model, clearing the viewer model.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.IGTViewerModel#removeAllRows()
	 */
	@Override
	public boolean removeAllRows() {
		int numRows = rowData.size();
		
		if (numRows == 0) {
			return true;
		}
		
		for (int i = numRows - 1; i >= 0; i--) {
			IGTDataModel remRowData = rowData.remove(i);
			remRowData.removeIGTDataModelListener(this);
		}
		fireTableRowsDeleted(0, numRows - 1);
		
		return rowData.isEmpty();
	}

	/**
	 * Adds a listener to the model.
	 * 
	 * @param listener
	 */
	@Override
	public synchronized void addIGTViewerModelListener(IGTViewerModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes the listener from the listeners list.
	 * 
	 * @param listener
	 */
	@Override
	public synchronized void removeIGTViewerModelListener(IGTViewerModelListener listener) {
		listeners.remove(listener);
	}

	// ######## IGTDataModelListener implementation ###################
	/**
	 * If data changed in the one or more of the rows, the viewer model can notify its listeners.
	 * 
	 * @param event the IGT data model event
	 */
	@Override
	public void dataModelChanged(IGTDataModelEvent event) {
		if (event != null) {
			IGTDataModel rowModel = (IGTDataModel) event.getSource();
			int rowIndex = rowData.indexOf(rowModel);
			if (rowIndex > -1) {
				IGTViewerModelEvent vmEvent = new IGTViewerModelEvent(this, rowIndex, event);
				// post event, could have method for that
				for (IGTViewerModelListener l : listeners) {
					l.viewerModelChanged(vmEvent);
				}
				fireTableRowsUpdated(rowIndex, rowIndex);
			} else {
				// not in the model (yet)?
			}
		}
	}

	//// ######## TableModel implementation ###################
	////
	//// Oh I wish we could use multiple inheritance and re-use stuff from
	//// AbstractTableModel...
	
    /** List of listeners */
    protected EventListenerList listenerList = new EventListenerList();

	@Override
	public int getColumnCount() {
		return 1; // TODO: separate the labels on the left into their own column.
	}

	@Override
	public String getColumnName(int columnIndex) {
		return "IGTGroup";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return IGTDataModel.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getRowData(rowIndex); // IGTDataModel
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// The editor modifies the edited object directly
	}

	// The JTable won't hear about model changes if we don't implement this.
	// The IGTViewerModel has listeners of its own, though.
	@Override
    public void addTableModelListener(TableModelListener l) {
		listenerList.add(TableModelListener.class, l);
    }

	@Override
    public void removeTableModelListener(TableModelListener l) {
		listenerList.remove(TableModelListener.class, l);
    }
	
    public TableModelListener[] getTableModelListeners() {
        return (TableModelListener[])listenerList.getListeners(
                TableModelListener.class);
    }
    
    public void fireTableChanged(TableModelEvent e) {
    	Object[] listeners = listenerList.getListenerList();
    	for (int i = listeners.length-2; i>=0; i-=2) {
    		if (listeners[i]==TableModelListener.class) {
    			((TableModelListener)listeners[i+1]).tableChanged(e);
    		}
    	}
    }
    
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }


    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }

    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }
}
