package mpi.eudico.client.annotator.imports;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

import javax.swing.JDialog;
import javax.swing.JFrame;


/**
 * A standalone version of the merge transcriptions wizard.
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
public class EAFMerger {
    /**
     * Creates a new EAFMerger instance
     */
    public EAFMerger() {
        init();
    }

	/**
	 * Initializes two step panes, sets some properties and creates a dialog.
	 *
	 */
    private void init() {
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        pane.putStepProperty("Standalone", Boolean.TRUE);

        StepPane step1 = new MergeStep1(pane);
        pane.addStep(step1);

        StepPane step2 = new MergeStep2(pane);
        pane.addStep(step2);

        JDialog dialog = pane.createDialog(new JFrame(),
                ElanLocale.getString("MergeTranscriptionDialog.Title"), true);

        dialog.setVisible(true);

        // check if we can exit the jvm
        Object quit = pane.getStepProperty("CanQuit");

        if (quit != null) {
            if (quit instanceof Boolean) {
                if (((Boolean) quit).booleanValue()) {
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    /**
     * Main, creates a new EAFMerger.
     *
     * @param args application arguments are ignored
     */
    public static void main(String[] args) {
        new EAFMerger();
    }
}
