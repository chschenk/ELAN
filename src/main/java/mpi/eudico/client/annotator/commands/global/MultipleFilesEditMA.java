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
@SuppressWarnings("serial")
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
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
        	new MFEFrame(ElanLocale.getString("MFE.FrameTitle")).setVisible(true);
        }
	}

	
}
