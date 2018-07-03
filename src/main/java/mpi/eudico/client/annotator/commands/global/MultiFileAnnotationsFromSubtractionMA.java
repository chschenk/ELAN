package mpi.eudico.client.annotator.commands.global;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep1;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep3;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep4;
import mpi.eudico.client.annotator.tier.OverlapsOrSubtractionStep5;
import mpi.eudico.client.annotator.tier.SubtractionStep2;
import mpi.eudico.client.annotator.util.ClientLogger;

public class MultiFileAnnotationsFromSubtractionMA extends FrameMenuAction implements ClientLogger {
	
	public MultiFileAnnotationsFromSubtractionMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }
	
	@Override
	public void actionPerformed(ActionEvent e) {		
		 MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
		 StepPane step1 = new OverlapsOrSubtractionStep1(pane, null, true);
	     StepPane step2 = new SubtractionStep2(pane);
	     StepPane step3 = new OverlapsOrSubtractionStep3(pane, null, true);
	     StepPane step4 = new OverlapsOrSubtractionStep4(pane, true);
	     StepPane step5 = new OverlapsOrSubtractionStep5(pane, null, true);
	     
	     pane.addStep(step1);
	     pane.addStep(step2);
	     pane.addStep(step3);
	     pane.addStep(step4);
	     pane.addStep(step5);
	     
	     JDialog dialog = pane.createDialog(frame, ElanLocale.getString("SubtractAnnotationDialog.Title"), true);
	     dialog.setPreferredSize(new Dimension(600, 600));
	     dialog.pack();
	     dialog.setVisible(true);
	}
}