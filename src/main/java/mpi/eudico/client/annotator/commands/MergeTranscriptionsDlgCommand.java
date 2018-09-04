package mpi.eudico.client.annotator.commands;

import java.awt.Dimension;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.imports.MergeStep1;
import mpi.eudico.client.annotator.imports.MergeStep2;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that shows a merge transcriptions dialog.
 *
 * @author Han Sloetjes
 */
public class MergeTranscriptionsDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new linked files command.
     *
     * @param name the name of the command
     */
    public MergeTranscriptionsDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Shows a multiple step dialog (wizard) to merge two transcriptions.
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments: <ul><li>arg[0] = </li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());

        StepPane step1 = new MergeStep1(pane, transcription);
        StepPane step2 = new MergeStep2(pane);
        pane.addStep(step1);
        pane.addStep(step2);

        pane.setPreferredSize(new Dimension(650,550));
        
        JDialog jd = pane.createDialog(ELANCommandFactory.getRootFrame(transcription),
            ElanLocale.getString("MergeTranscriptionDialog.Title"), true);
        jd.pack();
        jd.setVisible(true);
    }

    /**
     * Returns the name of the command
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
