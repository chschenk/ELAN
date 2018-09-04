package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.multiplefilesedit.UpdateTranscriptionsWithTemplateDialog;

/**
 * A menu action that initiates a process to update existing files with 
 * items from a template.
 */
@SuppressWarnings("serial")
public class MultipleFileUpdateWithTemplateMA extends FrameMenuAction {

	/**
	 * Constructor.
	 * @param name the name of the action
	 * @param frame the frame for this menu action
	 */
	public MultipleFileUpdateWithTemplateMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	/**
	 * Shows a "no undo" warning and creates a dialog to configure the process,
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// warn for no undo
        int option = JOptionPane.showConfirmDialog(frame,
                ElanLocale.getString("MultipleFileEdit.Warning.BackUp"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
        	// create and show dialog to select domain and template
        	new UpdateTranscriptionsWithTemplateDialog(frame, true).setVisible(true);
        }
	}
	
}
