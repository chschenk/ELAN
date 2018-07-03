package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.EditLexSrvcDialog;
import mpi.eudico.server.corpora.clom.Transcription;

public class EditLexSrvcDlgCommand implements Command {
	private String commandName;

    /**
     * Creates a new edit lexicon service command.
     *
     * @param name the name of the command
     */
	public EditLexSrvcDlgCommand (String name) {
		commandName = name;
	}
	
	/**
     * Creates the edit lexicon service dialog.
     *
     * @param receiver the transcription holding the lexicon services
     * @param arguments null
     */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		Transcription transcription = (Transcription) receiver;
        new EditLexSrvcDialog(transcription).setVisible(true);
	}

	@Override
	public String getName() {
		return commandName;
	}

}
