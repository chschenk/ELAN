package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Action to zoom in one step (step size is determined by the receiving viewer).
 */
@SuppressWarnings("serial")
public class ZoomInCA extends ZoomCA {
	
	public ZoomInCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.ZOOM_IN);
		//maybe add icon for the button case
		arg = new Object[]{1};
	}
}
