package mpi.eudico.client.annotator.commands;

import java.awt.Rectangle;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.CopyAnnotationsOfTierStep1;
import mpi.eudico.client.annotator.tier.CopyAnnotationsOfTierStep2;
import mpi.eudico.client.annotator.tier.CopyAnnotationsOfTierStep3;
import mpi.eudico.client.annotator.tier.CopyAnnotationsOfTierStep4;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates the dialog to copy annotations of one tier to another tier, 
 * with filtering.
 * 
 * @author Han Sloetjes
 */
public class CopyAnnotationsOfTierDlgCommand implements Command {
	private String commandName;
	
	/**
	 * Constructor
	 * @param commandName name of the command
	 */
	public CopyAnnotationsOfTierDlgCommand(String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Creates and configures the dialog to initiate the copy process
	 * @param receiver the Transcription object containing source and target tier
	 * @param arguments null, not used
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		TranscriptionImpl trans = (TranscriptionImpl) receiver;

	    MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
	    // add step panes
	    StepPane step1 = new CopyAnnotationsOfTierStep1(pane, trans);
	    StepPane step2 = new CopyAnnotationsOfTierStep2(pane, trans);
	    StepPane step3 = new CopyAnnotationsOfTierStep3(pane);
	    StepPane step4 = new CopyAnnotationsOfTierStep4(pane, trans);
	    pane.addStep(step1);
	    pane.addStep(step2);
	    pane.addStep(step3);
	    pane.addStep(step4);
	    
        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(
                trans),
            ElanLocale.getString("Menu.Tier.CopyAnnotationsOfTierDialog"), true);
        // make the window a bit bigger
        Rectangle bounds = dialog.getBounds();
        final int RES = 60;
        dialog.setBounds(Math.max(bounds.x - RES,  0), Math.max(bounds.y - RES, 0),
        		bounds.width + 2 * RES, bounds.height + 2 * RES);
        dialog.setVisible(true);
	}

	@Override
	public String getName() {
		return commandName;
	}

}
