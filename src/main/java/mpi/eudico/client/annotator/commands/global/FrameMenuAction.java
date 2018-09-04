package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;


/**
 * A menu action associated to a particular Elan frame.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public abstract class FrameMenuAction extends MenuAction {
    /** the ELAN frame */
    protected ElanFrame2 frame;

    /**
     * Creates a new FrameMenuAction
     *
     * @param name the name of the action (command id)
     * @param frame the associated frame
     */
    public FrameMenuAction(String name, ElanFrame2 frame) {
        super(name);
        this.frame = frame;
    }
}
