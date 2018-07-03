package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.update.ElanUpdateDialog;

/**
 * Menu action that checks for new updates of ELAN
 *
 * @author aarsom
 */
public class UpdateElanMA extends FrameMenuAction{

	 /**
     * Creates a new UpdateElanMA instance
     *
     * @param name the name of the action
     */
	public UpdateElanMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}	

	/**
     * Creates a updater and checks for update.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {       
    	ElanUpdateDialog updater = new ElanUpdateDialog(frame);
    	updater.checkForUpdates();
    }

}
