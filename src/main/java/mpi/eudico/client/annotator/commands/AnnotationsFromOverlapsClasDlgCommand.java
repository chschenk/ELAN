package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.annotator.tier.CalcOverlapsStep1;
import mpi.eudico.client.annotator.tier.CalcOverlapsStep2;
import mpi.eudico.client.annotator.tier.CalcOverlapsStep3;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JDialog;


/**
 * Creates the "annotations from overlaps wizard".
 */
public class AnnotationsFromOverlapsClasDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromOverlapsClasDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the "annotations from overlaps wizard".
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
        StepPane step1 = new CalcOverlapsStep1(pane, trans);
        StepPane step2 = new CalcOverlapsStep2(pane, trans);
        StepPane step3 = new CalcOverlapsStep3(pane, trans);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(
                    trans),
                ElanLocale.getString("Menu.Tier.AnnotationsFromOverlaps"), true);
        dialog.pack();
        dialog.setSize(new Dimension(dialog.getSize().width, dialog.getSize().height + 100));
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
