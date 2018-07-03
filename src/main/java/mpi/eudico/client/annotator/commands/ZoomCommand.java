package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.Zoomable;

/**
 * A command that passes zoom actions to one of the viewers of the current mode layout manager. 
 * 
 * @author Han Sloetjes
 */
public class ZoomCommand implements Command {
	private String name;
	
	public ZoomCommand(String name) {
		super();
		this.name = name;
	}

	/**
	 * @param receiver the layout manager
	 * @param arguments arg[0] = -1, 0, 1, or any>=10 (Integer). 
	 * -1 = zoom out, 0 = default zoom level, 1 = zoom in, >=10 = a zoom level
	 * Depending on the current "mode" the actual receiver will be a specific 
	 * time scale based viewer.
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		ElanLayoutManager elm = (ElanLayoutManager) receiver;
		if (elm == null) {
			return;// log
		}
		
		List<Zoomable> zoomViewers = elm.getZoomableViewers();
		
		if (zoomViewers != null && !zoomViewers.isEmpty()) {
			Zoomable zoomViewer = zoomViewers.get(0);		
			int zoom = (Integer) arguments[0];
		
			if (zoomViewer != null) {
				if (zoom == -1) {
					zoomViewer.zoomOutStep();
				} else if (zoom == 0) {
					zoomViewer.zoomToDefault();
				} else if (zoom == 1) {
					zoomViewer.zoomInStep();
				}
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

}
