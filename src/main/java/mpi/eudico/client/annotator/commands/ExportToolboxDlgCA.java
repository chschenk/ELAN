package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action that creates an export toolbox dialog. The export differs from the
 * Shoebox export in the following way:<br>
 * - a larger number of user options <br>
 * - it only supports utf-8 export
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportToolboxDlgCA extends CommandAction {
    /**
     * Creates a new ExportToolboxCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportToolboxDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_TOOLBOX);
    }

    /**
     * Creates a new ExportToolboxDlgCommand.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_TOOLBOX);
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
