package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.ShiftAllDialog;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * A Command that brings up a JDialog where the user can enter a shift value 
 * for all annotations (i.e. all time slots).
 *
 * @author Han Sloetjes
 */
public class ShiftAllAnnotationsDlgCommand implements Command {
	private String commandName;
	
	/**
	 * Creates a new shift all command.
	 *
	 * @param name the name of the command
	 */
	public ShiftAllAnnotationsDlgCommand(String name) {
		commandName = name;
	}

	/**
	 * Creates the dialog.
	 *
	 * @param receiver the transcription 
	 * @param arguments null
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		Transcription transcription = (Transcription) receiver;
		new ShiftAllDialog(transcription).setVisible(true);
	}

	/**
	 * Returns the name of the command
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return commandName;
	}

}
