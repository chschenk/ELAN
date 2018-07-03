package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that brings up a dialog for tab delimited export.
 *
 * @author Han Sloetjes
 */
public class ExportTeXDlgCA extends CommandAction {
    /**
     * Creates a new ExportTabDelDlgCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public ExportTeXDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_TEX);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_TEX);
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
