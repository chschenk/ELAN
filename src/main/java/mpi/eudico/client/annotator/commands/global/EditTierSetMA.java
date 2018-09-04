package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.tiersets.ManageTierSetDlg;

/**
 * A menu action that creates and shows the Edit Tier Sets dialog.
 *
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EditTierSetMA extends FrameMenuAction {
   
	/**
     * Creates a new EditTierSetMA instance
     *
     * @param name the name of the command
     * @param frame the associated frame
     */
    public EditTierSetMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows the dialog.
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        new ManageTierSetDlg(frame).setVisible(true);
    }
}


	
	
	
	
	
	
	
	
	
	