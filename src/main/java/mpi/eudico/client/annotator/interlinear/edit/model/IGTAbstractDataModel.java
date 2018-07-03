package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelListener;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTBlockRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTRowHeaderRenderInfo;

/**
 * An abstract class providing an implementation for some basic functionality of the 
 * data model.
 *  
 * @author Han Sloetjes
 */
public abstract class IGTAbstractDataModel implements IGTDataModel {
	protected List<IGTTier> rowData;
	protected IGTRowHeader rowHeader;
	protected IGTRowHeaderRenderInfo rowHeaderRenderInfo;
	protected IGTBlockRenderInfo renderInfo;
	protected List<IGTDataModelListener> listeners;
	
	/**
	 * Constructor. 
	 */
	public IGTAbstractDataModel() {
		super();
		rowData = new ArrayList<IGTTier>();
		rowHeader = new IGTRowHeader();
		rowHeaderRenderInfo = new IGTRowHeaderRenderInfo();
		renderInfo = new IGTBlockRenderInfo();
		listeners = new ArrayList<IGTDataModelListener>(4);
	}

	/**
	 * Returns the number of rows (tiers)
	 */
	@Override
	public int getRowCount() {
		return rowData.size();
	}

	/**
	 * The basic implementation returns the number of annotations on the specified tier.
	 * 
	 * @param row the row (tier) index
	 */
	@Override
	public int getColumnCountForRow(int row) {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException("Row index is less than zero: " +  row);
		} else if (row > rowData.size() - 1) {
			throw new ArrayIndexOutOfBoundsException("Row index is greater than model size: " +  
					row + " > " + rowData.size());
		}
		
		IGTTier igtTier = rowData.get(row);
		return igtTier.getAnnotations().size();
	}

	/**
	 * Inserts a tier/row at the specified index. If needed connections to a parent
	 * are established.
	 * @param the tier data
	 * @param the row index for insertion
	 */
	@Override
	public void insertRow(IGTTier data, int insertionRow)
			throws ArrayIndexOutOfBoundsException {
		if (insertionRow < 0) {
			throw new ArrayIndexOutOfBoundsException("Row index is less than zero: " +  insertionRow);
		} else if (insertionRow > rowData.size()){// if insertion point == size, then add 
			throw new ArrayIndexOutOfBoundsException("Row index is greater than model size: " +  insertionRow);
		}
		// establish connections ??
		IGTTier igtTier = (IGTTier) data;
	
		if (insertionRow == rowData.size()) {
			rowData.add(igtTier);
			rowHeader.addHeader(igtTier.getTierName());
		} else {
			rowData.add(insertionRow, igtTier);
			rowHeader.addHeader(insertionRow, igtTier.getTierName());
		}
		
		notifyRowAdded();
	}
	
	/**
	 * Add a row (tier) to the end.
	 * TODO: test if the data is already in the model (if a tier with the same name is already there)
	 * 
	 * @param igtTier the new row
	 */
	@Override
	public void addRow(IGTTier igtTier) {
		if (igtTier != null) {
			rowData.add(igtTier);
			rowHeader.addHeader(igtTier.getTierName());
		
			notifyRowAdded();
		}
	}
	

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getRootRow()
	 */
	@Override
	public IGTTier getRootRow() {
		IGTTier t;
		for (int i = 0; i < rowData.size(); i++) {
			t = rowData.get(i);
			if (t.getType() == IGTTierType.ROOT) {
				return t;
			}
		}
		// default return the tier at index 0
		return rowData.get(0);
	}
	
	

	/**
	 * Returns the begin time of the data in this model.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getBeginTime()
	 */
	@Override
	public abstract long getBeginTime();

	/**
	 * Returns the end time of the data in this model. 
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getEndTime()
	 */
	@Override
	public abstract long getEndTime();

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getFirstLevelSubdivisionRows()
	 */
	@Override
	public List<IGTTier> getFirstLevelSubdivisionRows() {
		List<IGTTier> firstLevels = new ArrayList<IGTTier>(5);
		
		IGTTier t;
		for (int i = 0; i < rowData.size(); i++) {
			t = rowData.get(i);
			if (t.getType() == IGTTierType.WORD_LEVEL_ROOT) {
				firstLevels.add(t);
			}
		}
		
		return firstLevels;
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getRowData(int)
	 */
	@Override
	public IGTTier getRowData(int row) throws ArrayIndexOutOfBoundsException {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException("Row index is less than zero: " +  row);
		} else if (row > rowData.size()){// if insertion point == size, then add 
			throw new ArrayIndexOutOfBoundsException("Row index is greater than model size: " +  row);
		}
		return rowData.get(row);
	}
	

	/**
	 * Returns the contents of the row (tier) for the specified tier name.
	 * 
	 * @param tierName the name of the tier
	 * @return the row data or null
	 */
	@Override
	public IGTTier getRowDataForTier(String tierName) {
		int index = rowHeader.getIndexForLabel(tierName);
		
		if (index > -1 && index < rowData.size()) {
			return rowData.get(index);
		}
		
		return null;
	}

	/**
	 * Returns the index in the model of the specified tier.
	 * 
	 * @return the index in the model of the specified tier or -1 if the tier is not in the model
	 */
	@Override
	public int getRowIndexForTier(String tierName) {
		if (tierName == null) {
			return -1;
		}
		
		return rowHeader.getIndexForLabel(tierName);
	}

	/**
	 * Returns the tier name (or label) for the given row or null.
	 * 
	 * @return the (full) tier name (or label) for the given row or null.
	 */
	@Override
	public String getTierNameForIndex(int index) {
		try {
			return rowHeader.getLabelForIndex(index);
		} catch (IndexOutOfBoundsException iobe) {
			return null;
		}
	}
	
	
	/**
	 * Returns the short or abbreviated name of the tier at the specified row
	 * 
	 * @param index the row index
	 * @return the short, abbreviated name
	 */
	@Override
	public String getShortTierNameForIndex(int index) {
		try {
			return rowHeader.getHeaderText(index);
		} catch (IndexOutOfBoundsException iobe) {
			return null;
		}
	}

	/**
	 * Sets the shortened name for the tier at the specified row index.
	 *  
	 * @param row the row to set the short label for
	 * @param shortName the shortened name (can be the full name)
	 */
	@Override
	public void setShortTierNameForIndex(int row, String shortName) {
		try {
			rowHeader.setHeaderText(row, shortName);
		} catch (IndexOutOfBoundsException iobe) {
			// log failure
		}		
	}

	/**
	 * Default implementation does nothing.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#setSpecialTierVisibility(mpi.eudico.client.annotator.interlinear.IGTTierType, boolean)
	 */
	@Override
	public void setSpecialTierVisibility(IGTTierType specialTier,
			boolean visible) {
		// stub, ignored		
	}

	/**
	 * Default implementation returns true.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getSpecialTierVisibility(mpi.eudico.client.annotator.interlinear.IGTTierType)
	 */
	@Override
	public boolean getSpecialTierVisibility(IGTTierType specialTier) {
		return true;
	}

	/**
	 * Returns the type of the tier in the specified row.
	 * 
	 * @param row the row index
	 * @return the IGTTierType of the row, or NONE
	 */
	@Override
	public IGTTierType getTypeForRow(int row) {
		try {
			IGTTier t = rowData.get(row);
			return t.getType();
		} catch (IndexOutOfBoundsException iobe) {
			return IGTTierType.NONE;
		}
	}
	
	/**
	 * Sets or changes the type of a tier, should rarely be needed. A change
	 * might have to have an effect on other, depending tiers.
	 * 
	 * @param row the row index of the tier to change
	 * @param type the new tier type for the tier in the row
	 */
	@Override
	public void setTypeForRow(int row, IGTTierType type) {
		try {
			IGTTier t = rowData.get(row);
			t.setType(type);
		} catch (IndexOutOfBoundsException iobe) {
			// log failure
		}
		
	}

	/**
	 * Returns the annotations at position column from the tier at row.
	 * 
	 * @param row the row index of a tier
	 * @param column the index into the annotations of the tier
	 * 
	 * @return an IGTAnnotation or null
	 */
	@Override
	public Object getValueAtRow(int row, int column) {

		try {
			IGTTier t = rowData.get(row);
			return t.getAnnotations().get(column);
		} catch (IndexOutOfBoundsException iobe) {
			// log
		}
		return null;
	}	
	
	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getRenderInfo()
	 */
	@Override
	public IGTBlockRenderInfo getRenderInfo() {
		return renderInfo;
	}

	/**
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#getRowHeaderRenderInfo()
	 */
	@Override
	public IGTRowHeaderRenderInfo getRowHeaderRenderInfo() {
		return rowHeaderRenderInfo;
	}

	/**
	 * Add a listener.
	 */
	@Override
	public synchronized void addIGTDataModelListener(IGTDataModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener
	 */
	@Override
	public synchronized void removeIGTDataModelListener(IGTDataModelListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notify listeners of the change event.
	 * 
	 * @see mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel#postEvent(mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelEvent)
	 */
	@Override
	public void postEvent(IGTDataModelEvent event) {
		if (event != null) {
			final int size = listeners.size();
			for (int i = 0; i < size; i++) {
				listeners.get(i).dataModelChanged(event);
			}
		}
	}
	
	/**
	 * Creates an event and notifies the listeners.
	 */
	protected void notifyRowAdded() {
		IGTDataModelEvent event = new IGTDataModelEvent(this);
		final int size = listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).dataModelChanged(event);
		}
	}
	
	/**
	 * Returns a list of visible tiers in the order of the model.
	 * Special tiers are not included.
	 * 
	 * @return a list of visible tiers in the order of the model 
	 * (this is in most cases the order they appear on screen)
	 */
	public List<String> getVisibleTierOrder() {
		List<String> tierNames = new ArrayList<String>();
		
		for (IGTTier igtt : rowData) {
			if (!igtt.isSpecial()) {
				tierNames.add(igtt.getTierName());
			}
		}
		
		return tierNames;
	}
	
}
