package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A command action for making the Interlinear viewer visible in the Elan
 * Frame.
 *
 * @author Han Sloetjes
 */
public class ShowInterlinearCA extends CommandAction {
    private ElanLayoutManager layoutManager;
    private Object[] args;

    /**
     * Creates a new ShowInterlinearCA instance
     *
     * @param theVM DOCUMENT ME!
     * @param layoutManager DOCUMENT ME!
     */
    public ShowInterlinearCA(ViewerManager2 theVM,
        ElanLayoutManager layoutManager) {
        super(theVM, ELANCommandFactory.SHOW_INTERLINEAR);
        this.layoutManager = layoutManager;
        args = new Object[] { ELANCommandFactory.SHOW_INTERLINEAR };
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
     * The receiver of this CommandAction is the layoutManager.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return layoutManager;
    }

    /**
     * Argument[0] is ELANCommandFactory.SHOW_INTERLINEAR.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return args;
    }
}
