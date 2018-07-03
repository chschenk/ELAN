package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.export.ExportShoebox;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A Command that brings up a dialog for shoebox style export.
 *
 * @author Han Sloetjes
 */
public class ExportShoeboxCommand implements Command {
    private String commandName;

    /**
     * Creates a new ExportShoeboxCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public ExportShoeboxCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object(ShoeboxTranscription)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) arguments[0];

		new ExportShoebox(ELANCommandFactory.getRootFrame(
				(Transcription) arguments[0]), true,
			(TranscriptionImpl) arguments[0]).setVisible(true);
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
