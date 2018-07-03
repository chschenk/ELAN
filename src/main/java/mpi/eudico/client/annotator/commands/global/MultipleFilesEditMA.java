package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.multiplefilesedit.MFEFrame;

/**
 * An action that creates the multiple files edit window.
 * 
 * @author Han Sloetjes
 *
 */
public class MultipleFilesEditMA extends FrameMenuAction {
	
	/**
	 * Constructor.
	 * @param name name of the action
	 * @param frame the parent frame
	 */
	public MultipleFilesEditMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	@Override
	/**
	 * Creates the edit window
	 */
	public void actionPerformed(ActionEvent e) {
        int option = JOptionPane.showConfirmDialog(frame,
                ElanLocale.getString("MFE.LaunchWarning"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
        	new MFEFrame(ElanLocale.getString("MFE.FrameTitle")).setVisible(true);
        }
	}

	
}
