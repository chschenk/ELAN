package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.linkedmedia.LinkedFilesDialog;

import mpi.eudico.server.corpora.clom.Transcription;


/**
 * A Command that brings up a JDialog for viewing and editing files linked to
 * the transcription.
 *
 * @author Han Sloetjes
 */
public class LinkedFilesDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new linked files command.
     *
     * @param name the name of the command
     */
    public LinkedFilesDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the dialog.
     *
     * @param receiver the transcription holding the media descriptors
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) receiver;
        new LinkedFilesDialog(transcription).setVisible(true);
    }

    /**
     * Returns the name of the command
     *
     * @return the name
     */
    @Override
	public String getName() {
        return null;
    }
}
