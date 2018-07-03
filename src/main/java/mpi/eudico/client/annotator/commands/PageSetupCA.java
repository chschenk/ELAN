package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to generate printout.
 *
 * @author Han Sloetjes
 */
public class PageSetupCA extends CommandAction {

    /**
     * Creates a new PrintCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public PageSetupCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PAGESETUP);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PAGESETUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return null;
    }
}
