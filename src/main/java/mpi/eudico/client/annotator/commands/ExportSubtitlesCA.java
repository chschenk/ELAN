package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Action that creates an export as subtitles dialog.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportSubtitlesCA extends CommandAction {
    /**
     * Creates a new ExportSubtitlesCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ExportSubtitlesCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.EXPORT_SUBTITLES);
    }

    /**
     * Creates a new export subtitles command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_SUBTITLES);
    }

    /**
     * Returns the arguments, the transcription and the selection
     *
     * @return the transcription and the selection
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getTranscription(), vm.getSelection() };
    }
}
