package mpi.eudico.client.annotator.commands;

import java.util.List;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.client.annotator.timeseries.TimeSeriesTrack;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;

public class AddTSTrackAndPanelCommand implements UndoableCommand {
	private String name;
	private List<Object> tracks;
	
	public AddTSTrackAndPanelCommand(String name) {
		this.name = name;
	}
	
	@Override
	public void redo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param receiver the viewer manager
	 * @param arguments args[0] is a List of Track objects
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		ViewerManager2 vm = (ViewerManager2) receiver;
		tracks = (List<Object>) arguments[0];
		
		if (tracks != null && tracks.size() > 0) {
			TSTrackManager trackManager = ELANCommandFactory.getTrackManager(vm.getTranscription());
			
			if (trackManager == null) {
                trackManager = new TSTrackManager(vm.getTranscription());
                ELANCommandFactory.addTrackManager(vm.getTranscription(),
                    trackManager);

                // get viewer manager, create viewer
                TimeSeriesViewer tsViewer = ELANCommandFactory.getViewerManager(vm.getTranscription())
                                                              .createTimeSeriesViewer();
                tsViewer.setTrackManager(trackManager);
                // get layout manager, add viewer
                ELANCommandFactory.getLayoutManager(vm.getTranscription())
                                  .add(tsViewer);
			}
			
			TimeSeriesTrack track;
			for (int i = 0; i < tracks.size(); i++) {
				track = (TimeSeriesTrack) tracks.get(i);
				trackManager.addTrack(track);
			}
		}

	}

	@Override
	public String getName() {
		return name;
	}

}
