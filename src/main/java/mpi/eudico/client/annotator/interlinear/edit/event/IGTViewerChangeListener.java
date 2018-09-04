package mpi.eudico.client.annotator.interlinear.edit.event;

/**
 * A listener for (size) changes of the viewer. 
 * 
 * @author Han Sloetjes
 */
public interface IGTViewerChangeListener {

	/**
	 * Notification of a change in the view size.
	 */
	public void viewChanged();
}
