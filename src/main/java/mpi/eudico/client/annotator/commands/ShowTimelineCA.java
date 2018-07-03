package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action for making the Timeline viewer visible in the Elan Frame.
 *
 * @author Han Sloetjes
 */
public class ShowTimelineCA extends CommandAction {
    private ElanLayoutManager layoutManager;
    private Object[] args;

    /**
     * Creates a new ShowTimelineCA instance
     *
     * @param theVM DOCUMENT ME!
     * @param layoutManager DOCUMENT ME!
     */
    public ShowTimelineCA(ViewerManager2 theVM, ElanLayoutManager layoutManager) {
        super(theVM, ELANCommandFactory.SHOW_TIMELINE);
        this.layoutManager = layoutManager;
        args = new Object[] { ELANCommandFactory.SHOW_TIMELINE };
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SHOW_MULTITIER_VIEWER);
    }

    /**
     * The receiver of this CommandAction is the ViewerManager.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return layoutManager;
    }

    /**
     * Argument[0] is ELANCommandFactory.SHOW_TIMELINE.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return args;
    }
}
