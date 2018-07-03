package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.List;

import mpi.eudico.client.annotator.interlinear.IGTTierType;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelEvent;
import mpi.eudico.client.annotator.interlinear.edit.event.IGTDataModelListener;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTBlockRenderInfo;
import mpi.eudico.client.annotator.interlinear.edit.render.IGTRowHeaderRenderInfo;

/**
 * A data model for one group of annotations consisting of a root annotation
 * and all its depending annotations on depending tiers.
 * The model can also hold other types of information, e.g. time information,
 * speaker, silence duration to next root annotation
 * 
 * @author Han Sloetjes 
 *
 */
public interface IGTDataModel {

	/**
	 * THe number of rows (tiers) in the model
	 * @return
	 */
	public int getRowCount();
	
	/**
	 * Each row (tier) can have a different number of columns, defaults to 1
	 * 
	 * @param row the row
	 * @return the number of columns
	 */
	public int getColumnCountForRow(int row);
	
	/**
	 * Tries to insert a data row (tier) to the model at a certain position.
	 * 
	 * @param data the data
	 * @param insertionRow the index to insert
	 * @return the actual row index where the row is inserted
	 * 
	 * @throws ArrayIndexOutOfBoundsException if the row is greater than the current length or < 0
	 */
	public void insertRow(IGTTier data, int insertionRow) throws ArrayIndexOutOfBoundsException;
	
	/**
	 * Adds a row (tier) to the model
	 * 
	 * @param data
	 */
	public void addRow(IGTTier data);
	
	/**
	 * Returns the root (tier) row of the model.
	 * 
	 * @return the root row of the model, typically this will be the row at index 0
	 */
	public IGTTier getRootRow();
	
	/**
	 * Returns the begin time of the group of annotations (which now corresponds with the 
	 * boundaries of the root annotation). 
	 * 
	 * @return the begin time or -1
	 */
	public long getBeginTime();
	
	/**
	 * Returns the end time of the group of annotations (which now corresponds with the 
	 * boundaries of the root annotation). 
	 * 
	 * @return the end time or -1
	 */
	public long getEndTime();
	
	/**
	 * Returns a list of first level subdivision tiers, typically this will be "word" level
	 * subdivision tiers and normally the size of the list will be 1.
	 * 
	 * @return a list of first level subdivision tiers (typically "word" level tiers)
	 */
	public List<IGTTier> getFirstLevelSubdivisionRows();
	
	/**
	 * Returns the contents of the row (tier) at the specified index.
	 * 
	 * @param row the row index
	 * @return the data
	 */
	public IGTTier getRowData(int row)  throws ArrayIndexOutOfBoundsException;
	
	/**
	 * Returns the contents of the row for the specified tier name.
	 * 
	 * @param tierName the name of the tier
	 * @return the row data or null
	 */
	public IGTTier getRowDataForTier(String tierName);
	
	/**
	 * Returns the row index for a tier
	 *  
	 * @param tierName the tier name
	 * @return the row index
	 */
	public int getRowIndexForTier(String tierName);
	
	/**
	 * Returns the name of the tier at the specified row
	 * @param index the row index
	 * @return the name
	 */
	public String getTierNameForIndex(int index);
	
	/**
	 * Returns the short or abbreviated name of the tier at the specified row
	 * 
	 * @param index the row index
	 * @return the short, abbreviated name
	 */
	public String getShortTierNameForIndex(int index);
	
	/**
	 * Returns the type of the row (tier): root, subdivision, one-to-one (non-subdivided), non-annotation
	 * @param row the row index
	 * @return the content type of a row
	 */
	public IGTTierType getTypeForRow(int row);
	
	/**
	 * Sets the type of the row (tier): root, subdivision, one-to-one (non-subdivided), non-annotation
	 * @param row the row index
	 * @param type the type of ILT tier
	 */
	public void setTypeForRow(int row, IGTTierType type);
	
	/**
	 * Sets the shortened name for the tier at the specified row index.
	 *  
	 * @param row the row to set the short label for
	 * @param shortName the shortened name (can be the full name)
	 */
	public void setShortTierNameForIndex(int row, String shortName);
	
	/**
	 * Makes the specified special tier (in)visible. In practice this means 
	 * the tier is added or removed from the data model.
	 * 
	 * @param specialTier one of {@link IGTTierType#TIME_CODE}, {@link IGTTierType#SPEAKER_LABEL}
	 * or {@link IGTTierType#SILENCE_DURATION}
	 * @param visible true to set the tier visible (add), false to hide it (remove) 
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
	
	//public void fireModelChangedEvent();
	
	/**
	 * Returns an Annotation or a String (time information, speaker/participant, silence duration
	 * 
	 * @param row the row (tier) index
	 * @param column the column index, defaults to 0
	 * @return the value at the specified row
	 */
	public Object getValueAtRow(int row, int column);
	
	/**
	 * Returns the render information object.
	 * 
	 * @return the render information
	 */
	public IGTBlockRenderInfo getRenderInfo();
	
	/**
	 * @return the row header render information
	 */
	public IGTRowHeaderRenderInfo getRowHeaderRenderInfo();
	
	/**
	 * Adds a listener to the model.
	 * 
	 * @param listener
	 */
	public void addIGTDataModelListener(IGTDataModelListener listener);
	
	/**
	 * Removes a listener from the model
	 * 
	 * @param listener
	 */
	public void removeIGTDataModelListener(IGTDataModelListener listener);
	
	/**
	 * Allows to let the model post change events from external code.
	 * NB might need to be removed when all changes to data in the model is done by the model itself.  
	 * 
	 * @param event the event
	 */
	public void postEvent(IGTDataModelEvent event);
		
}
