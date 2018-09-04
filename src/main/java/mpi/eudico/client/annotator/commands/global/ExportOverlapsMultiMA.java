package mpi.eudico.client.annotator.commands.global;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.tier.ExportOverlapsStep1;
import mpi.eudico.client.annotator.tier.ExportOverlapsStep2;

/**
 * A action that creates a dialog for exporting annotation overlap information 
 * for multiple files.
 * 
 * @author Han Sloetjes
 *
 */
@SuppressWarnings("serial")
public class ExportOverlapsMultiMA extends AbstractProcessMultiMA {

	/**
	 * Constructor.
	 * 
	 * @param name name of the action
	 * @param frame parent frame
	 */
	public ExportOverlapsMultiMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	/**
	 * Prompts for files and directories and creates a dialog.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
        List<File> files = getMultipleFiles(frame,
                ElanLocale.getString("ExportDialog.Multi"));

        if ((files == null) || files.isEmpty()) {
            return;
        }
        
        MultiStepPane pane = new MultiStepPane(ElanLocale.getResourceBundle());
        pane.putStepProperty("files", files);
        StepPane step1 = new ExportOverlapsStep1(pane);
        StepPane step2 = new ExportOverlapsStep2(pane);

        pane.addStep(step1);
        pane.addStep(step2);

        JDialog dialog = pane.createDialog(frame,
                ElanLocale.getString("ExportOverlapsDialog.Title"), true);
        dialog.pack();
        dialog.setSize(new Dimension(dialog.getSize().width, dialog.getSize().height + 100));
        dialog.setVisible(true);
	}

}
