package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.annotator.tier.CopyTierStep1;
import mpi.eudico.client.annotator.tier.CopyTierStep2;
import mpi.eudico.client.annotator.tier.CopyTierStep3;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JDialog;


/**
 * A Command that creates a dialog to copy a Tier.
 *
 * @author Han Sloetjes
 */
public class CopyTierDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new CopyTierDlgCommand instance
     *
     * @param theName the name of the command
     */
    public CopyTierDlgCommand(String theName) {
        commandName = theName;
    }

    /**
     * Shows a multiple step dialog (wizard) to copy a tier. <b>Note: </b>it is
     * assumed the types and order of the arguments are correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments: <ul><li>arg[0] = </li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        pane.putStepProperty("CopyMode", "true");

        StepPane step1 = new CopyTierStep1(pane, trans);
        StepPane step2 = new CopyTierStep2(pane, trans);
        StepPane step3 = new CopyTierStep3(pane, trans);
        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(
                    trans),
                ElanLocale.getString("Menu.Tier.CopyTierDialog"), true);

        dialog.setVisible(true);
    }

    /**
     * Returns the name of the command
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
