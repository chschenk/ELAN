package mpi.eudico.client.annotator.layout;

import mpi.eudico.client.annotator.DetachedFrame;
import mpi.eudico.client.annotator.DetachedViewerFrame;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.viewer.AbstractViewer;

/**
 * A class for convenient storage of viewer layout related attributes,
 * to avoid we have to administer a number of lists of attributes for viewers.
 * 
 * There should be an interface Detachable or something.
 * 
 * @author Han Sloetjes
 */
public class ViewerLayoutModel {
	public AbstractViewer viewer;
	private ElanLayoutManager layoutManager;
	private boolean attached;
	public DetachedFrame detachedFrame;
	
	public ViewerLayoutModel(AbstractViewer viewer, ElanLayoutManager layoutManager) {
		this.viewer = viewer;
		this.layoutManager = layoutManager;
		attached = true;
		detachedFrame = null;
	}


	/**
	 * Returns the attached/detached state.
	 * @return the attached/detached state
	 */
	public boolean isAttached() {
		return attached;
	}
	
	
	/**
	 * Detaches the viewer. The visual component is added to the content pane 
	 * of its own Frame.
	 */
	public void detach() {
		if (!attached) {
			return;
		}
		
		String title = viewer.getClass().getName();
		int index = title.lastIndexOf('.');
		if (index > 0) {
			title = title.substring(index + 1, title.length());
		}
		detachedFrame = new DetachedViewerFrame(layoutManager, viewer, 
			title);
		detachedFrame.setSize(500, 300);
		detachedFrame.setVisible(true);
		viewer.preferencesChanged();// or read the preferences here and set the bounds?
		attached = false;
		// viewer.setAttached(false);
	}
	
	/**
	 * Removes the viewer from the content pane of a detached frame
	 * and disposes the frame.
	 * Then the viewer can be added to the content pane of the main 
	 * application frame. This is not done here.
	 */
	public void attach() {
		if (attached || detachedFrame == null) {
			return;
		}
		detachedFrame.getContentPane().remove(viewer);
		detachedFrame.setVisible(false);
		detachedFrame.dispose();
		detachedFrame = null;
		attached = true;
	}
	
}
