package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that brings up a dialog for TIGER export.
 *
 * @author Han Sloetjes
 */
public class ExportTigerDlgCA extends CommandAction {
    /**
     * Creates a new ExportTigerDlgCA instance
     *
     * @param viewerManager the vm
     */
    public ExportTigerDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_TIGER);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_TIGER);
    }

    /**
     * There's no logical receiver for this CommandAction.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription(), vm.getSelection() };
    }
}
