package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportSubtitleDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A command that creates a dialog for export to subtitles text.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportSubtitlesCommand implements Command {
    private String commandName;

    /**
     * COnstructor
     *
     * @param commandName
     */
    public ExportSubtitlesCommand(String commandName) {
        this.commandName = commandName;
    }

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            null
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the Transcription object (TranscriptionImpl)</li>
	 *            <li>arg[1] = the Selection object (Selection)</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        new ExportSubtitleDialog(ELANCommandFactory.getRootFrame(
                (Transcription) arguments[0]), true,
            (TranscriptionImpl) arguments[0], (Selection) arguments[1]).setVisible(true);
    }

    /**
     * Returns the name.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
