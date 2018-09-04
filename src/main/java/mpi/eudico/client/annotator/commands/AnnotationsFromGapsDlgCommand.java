package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.AnnotationsFromGapsDlg;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Creates the annotations from gaps dialog.
 */
public class AnnotationsFromGapsDlgCommand implements Command {
    private String commandName;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromGapsDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the annotations from gaps dialog.
     *
     * @param receiver the transcription
     * @param arguments null
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl trans = (TranscriptionImpl) receiver;
        long mediaDuration = ELANCommandFactory.getViewerManager(trans)
                                               .getMasterMediaPlayer()
                                               .getMediaDuration();

        AnnotationsFromGapsDlg dialog = new AnnotationsFromGapsDlg(ELANCommandFactory.getRootFrame(
                    trans), trans, mediaDuration);
        dialog.setVisible(true);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
