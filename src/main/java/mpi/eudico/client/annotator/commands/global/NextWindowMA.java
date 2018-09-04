package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.FrameManager;


/**
 * Action that activates the next window.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class NextWindowMA extends MenuAction {
    /**
     * Constructor.
     *
     * @param name the name of the action
     */
    public NextWindowMA(String name) {
        super(name);
    }

    /**
     * Tells the FrameManager to activate the next window in the menu.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        FrameManager.getInstance().activateNextFrame(true);
    }
}
