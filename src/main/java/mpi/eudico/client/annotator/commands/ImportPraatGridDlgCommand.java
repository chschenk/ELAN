package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.annotator.imports.praat.ImportPraatTGStep1;
import mpi.eudico.client.annotator.imports.praat.ImportPraatTGStep2;
import mpi.eudico.client.annotator.imports.praat.ImportPraatTGStep3;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import javax.swing.JDialog;


/**
 * A command that creates a import Praat grid dialog, parses the file and adds
 * (interval) tiers and annotations to the transcription.
 */
public class ImportPraatGridDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a ImportPraat dialog
     *
     * @param name the name of the command
     */
    public ImportPraatGridDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the import Praat dialog.
     *
     * @param receiver the Transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;

        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        StepPane step1 = new ImportPraatTGStep1(pane);
        StepPane step2 = new ImportPraatTGStep2(pane, trans);
        StepPane step3 = new ImportPraatTGStep3(pane, trans);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(ELANCommandFactory.getRootFrame(
                    trans),
                ElanLocale.getString("Menu.File.Import.PraatTiers"), true);

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
