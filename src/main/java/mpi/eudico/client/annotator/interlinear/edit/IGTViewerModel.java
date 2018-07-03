package mpi.eudico.client.annotator.interlinear.edit;

import java.util.Collection;
import java.util.List;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTViewerModelListener;
import mpi.eudico.client.annotator.interlinear.edit.model.IGTDataModel;

/**
 * A data model for a viewer that handles visualization and editing of
 * annotations that are organised in an interlinear glossed text style.
 * The elements in the model are instances of IGTDataModel, i.e. are 
 * data models containing the interdependent annotations.
 *    
 * @author Han Sloetjes
 *
 */
public interface IGTViewerModel {
	/**
	 * Returns the number of rows.
	 * 
	 * @return the number of rows
	 */
	public int getRowCount();
	
	/**
	 * Returns the index of the row that is being edited.
	 * 
	 * @return the index of the editing row
	 */
	public int getEditingRow();
	
	/**
	 * Records that the given <code>row</code> is (shortly going to be) edited.
	 * <p>
	 * Implementation note:
	 * The documentation originally suggested that this method <em>causes</em>
	 * the editing. This has never been true, and it doesn't make sense for a model.
	 * <p>
	 * <s>Initiates the editing of the specified row.
	 * First the current editing row will be notified to stop editing.</s>
	 * 
	 * @param row the index of the row. If < 0 then no row will be edited.
	 */
	public void startEditingRow(int row);
	
	/**
	 * Returns the current setting for collapsed or expanded visualization of 
	 * the rows.
	 * 
	 * @return the current expanded state flag
	 */
	public boolean getExpandedState();//or use an int, to allow more state than expanded/collapse
	
	/**
	 * Sets the collapsed/expanded state for visualization of the rows.
	 * 
	 * @param expanded the new flag for the collapsed/expanded state
	 */
	public void setExpandedState(boolean expanded);
	
	/**
	 * Sets which tiers should be visible in collapsed state.
	 * 
	 * TODO Note: it might be good to have a method to set the visible types
	 *  
	 * @param tierNames the list of tier names
	 */
	public void setVisibleTiersInCollapsedState(List<String> tierNames);
	
	/**
	 * Returns the list of tiers visible in collapsed state.
	 * 
	 * TODO add a method to return the list of visible types?
	 * 
	 * @return the list of tiers visible in collapsed state.
	 */
	public List<String> getVisibleTiersInCollapsedState();
	
	/**
	 * Sets the list of hidden tiers.
	 * TODO: add setVisibleTypes?
	 * 
	 * @param tierNames the list of visible tiers
	 */
	public void setHiddenTiers(Collection<String> tierNames);
	
	/**
	 * Returns the hidden tiers.
	 * TODO add getVisibleTypes method?
	 * 
	 * @return the visible tiers
	 */
	public Collection<String> getHiddenTiers();
	
	/**
	 * Sets which participants are visible.
	 * 
	 * @param partNames a list of participant names
	 */
	public void setVisibleParticipants(List<String> partNames);
	
	/**
	 * Returns the current visible participants.
	 *  
	 * @return the current visible participants
	 */
	public List<String> getVisibleParticipants();
	
	/**
	 * Shows or hides one of the "special tiers" (lines that don't represent 
	 * actual tiers but are added to show special information).
	 * 
	 * @param specialTier one of {@link IGTTierType#TIME_CODE}, {@link IGTTierType#SPEAKER_LABEL}
	 * or {@link IGTTierType#SILENCE_DURATION}
	 * @param visible true to set the tier visible, false to hide it 
	 */
	public void setSpecialTierVisibility(IGTTierType specialTier, boolean visible);
	
	/**
	 * Returns the current visibility of one of the additional tiers/lines.
	 * 
	 * @param specialTier one of {@link IGTTierType#TIME_CODE}, {@link IGTTierType#SPEAKER_LABEL}
	 * or {@link IGTTierType#SILENCE_DURATION}
	 * @return the visibility flag of the specified tier
	 */
	public boolean getSpecialTierVisibility(IGTTierType specialTier);
	
	/**
	 * Add a row of data.
	 * 
	 * @param rowData the data to add, of type IGTDataModel
	 */
	public void addRow(IGTDataModel rowData);
	
	/**
	 * Inserts a row of data at the specified index.
	 * 
	 * @param rowData an IGTDataModel, i.e. an annotation with its dependent annotations.
	 * @param row the index of the row to add
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void insertRow(IGTDataModel rowData, int row) throws ArrayIndexOutOfBoundsException;
	
	/**
	 * Returns the data at the specified row.
	 * 
	 * @param row the index of the row
	 * @return the row data at that row
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public IGTDataModel getRowData(int row) throws ArrayIndexOutOfBoundsException;
	
	/**
	 * Removes the (data at the) specified row from the viewer model.
	 *  
	 * @param row the index of the row to remove
	 * @return the row data if it was deleted successfully, null otherwise
	 * 
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public IGTDataModel removeRow(int row) throws ArrayIndexOutOfBoundsException;
	
	/**
	 * Removes the specified object from the viewer model.
	 * 
	 * @param rowData the data to remove
	 * @return true if the object was in the model and was successfully removed, false otherwise
	 */
	public boolean removeRowData(IGTDataModel rowData);
	
	/**
	 * Removes all rows from the viewer model.
	 * 
	 * @return true if all rows were successfully removed
	 */
	public boolean removeAllRows();

	/** Adds a listener to the model.
	 * 
	 * @param listener
	 */
	public void addIGTViewerModelListener(IGTViewerModelListener listener);
	
	/**
	 * Removes the listener from the listeners list.
	 * 
	 * @param listener
	 */
	public void removeIGTViewerModelListener(IGTViewerModelListener listener);
	// row to pixel and vice versa methods?
}
