package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.FilterTierDialog;

import mpi.eudico.server.corpora.clom.Transcription;


/**
 * A Command that brings up a JDialog for filtering/copying a tier.
 *
 * @author Han Sloetjes
 */
public class FilterTierDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new tokenize dialog command.
     *
     * @param name the name of the command
     */
    public FilterTierDlgCommand(String name) {
        commandName = name;
    }

    /**
     * Creates the tokenize tier dialog.
     *
     * @param receiver the transcription holding the tiers
     * @param arguments null
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Transcription transcription = (Transcription) receiver;
        new FilterTierDialog(transcription).setVisible(true);
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
