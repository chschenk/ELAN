package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action that creates a dialog for flex  export. 
 *
 * @author Aarthy Somasundaram
 */
public class ExportFlexDlgCA extends CommandAction {
    /**
     * Creates a new ExportFlexDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportFlexDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_FLEX);
    }

    /**
     * Creates a new ExportToolboxDlgCommand.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_FLEX);
    }

    /**
     * Returns the transcription object.
     *
     * @return the transcription object
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription() };
    }
}