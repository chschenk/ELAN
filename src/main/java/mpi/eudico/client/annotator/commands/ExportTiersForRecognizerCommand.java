package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.export.ExportRecogTiersDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A command that creates a dialog for exporting tiers to the AVATecH 
 * TIER xml or csv file format.
 * 
 * @author Han Sloetjes
 *
 */
public class ExportTiersForRecognizerCommand implements Command {
	private String commandName;
	
	/**
	 * Constructor
	 * @param name name of the command
	 */
	public ExportTiersForRecognizerCommand(String name) {
		commandName = name;
	}
	
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the Transcription
     *        object (TranscriptionImpl)</li> <li>arg[1] = the Selection
     *        object (Selection)</li> </ul>
     */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (arguments != null && arguments.length >= 2) {
			new ExportRecogTiersDialog(ELANCommandFactory.getRootFrame(
	                (Transcription) arguments[0]), true,
	                (TranscriptionImpl) arguments[0], (Selection) arguments[1]).setVisible(true);
		}
	}

	/**
	 * Returns the name.
	 */
	@Override
	public String getName() {
		return commandName;
	}

}
