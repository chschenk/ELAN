package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
/**
 * Action to zoom out one step (step size is determined by the receiving viewer).
 */
@SuppressWarnings("serial")
public class ZoomOutCA extends ZoomCA {
	
	public ZoomOutCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.ZOOM_OUT);
		 arg = new Object[]{-1};
	}

}
