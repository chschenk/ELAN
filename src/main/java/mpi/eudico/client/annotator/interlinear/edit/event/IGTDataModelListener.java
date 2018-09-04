package mpi.eudico.client.annotator.interlinear.edit.event;

import java.util.EventListener;


/**
 * A listener interface for events produced by a IGTDataModel.
 * 
 */
public interface IGTDataModelListener extends EventListener {
	
	/**
	 * Notification of a change in the IGTDataModel
	 * 
	 * @param event object containing information about the event
	 */
	public void dataModelChanged(IGTDataModelEvent event);

}
