package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.annotator.imports.praat.ImportPraatTGStep1;
import mpi.eudico.client.annotator.imports.praat.ImportPraatTGStep2;
import mpi.eudico.client.annotator.imports.praat.ImportPraatTGStep3;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;


/**
 * Imports a Praat TextGrid file into an empty, just-in-time created
 * transcription.
 *
 * @author Han Sloetjes
 * @version 1.0
 *
 * @see {@link mpi.eudico.client.annotator.commands.ImportPraatGridDlgCommand}
 */
public class ImportPraatMA extends FrameMenuAction {
    /**
     * Creates a new ImportPraatMA instance
     *
     * @param name the name of the action
     * @param frame the parent frame
     */
    public ImportPraatMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a new transcription with one linguistic type and  shows the
     * import Praat wizard.
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Transcription transcription = new TranscriptionImpl();

        // create a default LinguisticType for the tiers
        if (transcription.getLinguisticTypes().size() == 0) {
            // time-alignable, no constraint type
            LinguisticType type = new LinguisticType("praat");
            transcription.addLinguisticType(type);
            transcription.setChanged();
        }

        // show import wizard
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        StepPane step1 = new ImportPraatTGStep1(pane);
        StepPane step2 = new ImportPraatTGStep2(pane,
                (TranscriptionImpl) transcription);
        StepPane step3 = new ImportPraatTGStep3(pane,
                (Transcription) transcription, true);

        pane.addStep(step1);
        pane.addStep(step2);
        pane.addStep(step3);

        JDialog dialog = pane.createDialog(frame,
                ElanLocale.getString("Menu.File.Import.PraatTiers"), true);

        dialog.setVisible(true);
        // TODO if the dialog is canceled an empty ELAN frame is still created.
        // implement a way to detect canceling of the dialog
        // open window
        FrameManager.getInstance().createFrame(transcription);
    }
}
