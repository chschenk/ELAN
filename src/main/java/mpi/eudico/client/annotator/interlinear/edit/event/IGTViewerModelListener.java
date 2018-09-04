package mpi.eudico.client.annotator.interlinear.edit.event;

import java.util.EventListener;


/**
 * An interface for listeners to events produced after a change in a IGT viewer model.
 *  
 * @author Han Sloetjes
 *
 */
public interface IGTViewerModelListener extends EventListener {

	/**
	 * Notification of changes in the viewer model
	 * 
	 * @param event the event
	 */
	public void viewerModelChanged(IGTViewerModelEvent event);
	
}
