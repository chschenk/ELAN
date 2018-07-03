package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.FrameManager;


/**
 * Action that starts the Exit Application sequence.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class ExitMA extends MenuAction {
    /**
     * Creates a new ExitMA instance.
     *
     * @param name the name of the action (command)
     */
    public ExitMA(String name) {
        super(name);
    }

    /**
     * Invokes exit on the FrameManager.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        FrameManager.getInstance().exit();
    }
}
