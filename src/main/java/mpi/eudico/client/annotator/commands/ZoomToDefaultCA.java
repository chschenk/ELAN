package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Action to change the zoom level to the default value, the "actual size". 
 */
@SuppressWarnings("serial")
public class ZoomToDefaultCA extends ZoomCA {

	public ZoomToDefaultCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.ZOOM_DEFAULT);
		arg = new Object[]{0};
	}
}
