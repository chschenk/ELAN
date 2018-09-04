package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportTradTranscript;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Show a dialog for export in a traditional transcript style.
 *
 * @author Han Sloetjes
 */
public class ExportTradTranscriptDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ExportTabDelDlgCommand instance
     *
     * @param theName the name of the command
     */
    public ExportTradTranscriptDlgCommand(String theName) {
        commandName = theName;
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
	 *            <li>arg[0] = the Transcription object(Transcription)</li>
	 *            <li>arg[1] = the Selection object (Selection)</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        new ExportTradTranscript(ELANCommandFactory.getRootFrame(
                (Transcription) arguments[0]), true,
            (TranscriptionImpl) arguments[0], (Selection) arguments[1]).setVisible(true);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
