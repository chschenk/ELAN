package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.MergeMultiTiersStep1;
import mpi.eudico.client.annotator.tier.MergeMultiTiersStep2;
import mpi.eudico.client.annotator.tier.MergeMultiTiersStep3;
import mpi.eudico.client.annotator.tier.MergeMultiTiersStep4;
import mpi.eudico.client.annotator.tier.MergeMultiTiersStep5;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Commands creates the "merge tiers wizard".
 */
public class MergeTiersDlgCommand implements Command {
    private String commandName;
    private boolean subtraction;
    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public MergeTiersDlgCommand(String name) {
    	commandName = name;
    }

    /**
     * Creates the "merge tiers wizard".
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;       

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
		StepPane step1 = new MergeMultiTiersStep1(pane, trans);
	    StepPane step2 = new MergeMultiTiersStep2(pane, ELANCommandFactory.getRootFrame(trans));
	    StepPane step3 = new MergeMultiTiersStep3(pane, trans);
	    StepPane step4 = new MergeMultiTiersStep4(pane);
	    StepPane step5 = new MergeMultiTiersStep5(pane, trans);
	     
	    pane.addStep(step1);
	    pane.addStep(step2);
	    pane.addStep(step3);
	    pane.addStep(step4);
	    pane.addStep(step5);
	     
	     JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(trans), ElanLocale.getString("MergeTiers.Title"), true);
	     dialog.setPreferredSize(new Dimension(600, 680));
	     dialog.pack();
	     dialog.setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
