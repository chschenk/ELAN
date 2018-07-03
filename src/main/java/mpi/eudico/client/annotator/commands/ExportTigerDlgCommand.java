package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportTigerDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that brings up a dialog for tab delimited export.
 *
 * @author Han Sloetjes
 */
public class ExportTigerDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new ExportTabDelDlgCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ExportTigerDlgCommand(String theName) {
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
	 *            <li>arg[0] = the Transcription object (TranscriptionImpl)</li>
	 *            <li>arg[1] = the Selection object (Selection)</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        new ExportTigerDialog(ELANCommandFactory.getRootFrame(
                (Transcription) arguments[0]), true,
            (TranscriptionImpl) arguments[0], (Selection) arguments[1]).setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
