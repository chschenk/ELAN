package mpi.eudico.client.annotator.timeseries;

import java.awt.Rectangle;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class ExtractDataMultiStep implements ClientLogger {

	public ExtractDataMultiStep(TranscriptionImpl transcription,
			TSTrackManager manager) {
		show(transcription, manager);
	}
	
	private void show(TranscriptionImpl transcription,
			TSTrackManager manager) {
		if (transcription == null) {
			LOG.warning("Could not create multistep dialog: transcription is null");
			return;
		}
		if (manager == null) {
			LOG.warning("Could not create multistep dialog: manager is null");
			return;
		}
		MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
		
		StepPane step1 = new ExtractStep1(pane, transcription);
		StepPane step2 = new ExtractStep2(pane, transcription, manager);
		StepPane step3 = new ExtractStep3(pane, transcription, manager);
        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);
        
        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(
                transcription),
            ElanLocale.getString("TimeSeriesViewer.Extract"), true);
        Rectangle bounds = dialog.getBounds();
        dialog.setBounds(bounds.x - 70, bounds.y - 20, bounds.width + 140, bounds.height + 40);
        
        dialog.setVisible(true);
	}

}
