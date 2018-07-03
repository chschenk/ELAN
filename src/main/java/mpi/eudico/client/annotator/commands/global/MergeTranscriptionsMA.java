package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import mpi.eudico.client.annotator.imports.MergeStep1;
import mpi.eudico.client.annotator.imports.MergeStep2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;


/**
 * Creates a merge transcriptions menu action. Opens the wizard without a
 * preloaded transcription.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class MergeTranscriptionsMA extends FrameMenuAction {
    /**
     * Creates a new MergeTranscriptionsMA instance
     *
     * @param name action name
     * @param frame the parent frame
     */
    public MergeTranscriptionsMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a wizard without a "current" transcription.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());

        StepPane step1 = new MergeStep1(pane, null);
        StepPane step2 = new MergeStep2(pane);
        pane.addStep(step1);
        pane.addStep(step2);

        pane.setPreferredSize(new Dimension(650,400));
        JDialog jd = pane.createDialog(frame,
            ElanLocale.getString("MergeTranscriptionDialog.Title"), true);
        jd.setBounds(jd.getX(), jd.getY() - 50, jd.getWidth(), jd.getHeight() + 100);
        jd.setVisible(true);
    }
}
