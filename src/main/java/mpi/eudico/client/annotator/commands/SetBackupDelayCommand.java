package mpi.eudico.client.annotator.commands;

/**
 * A Command to change the frequency of automatic backups.
 *
 * @author Han Sloetjes
 */
public class SetBackupDelayCommand implements Command {
    private String commandName;

    /**
     * Creates a new SetBackupDelayCommand instance
     *
     * @param theName DOCUMENT ME!
     */
    public SetBackupDelayCommand(String theName) {
        commandName = theName;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the BackupCA
     * @param arguments the arguments:  <ul><li>arg[0] = the delay for
     *        backup(Integer)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ((BackupCA) receiver).setDelay(((Integer) arguments[0]).intValue());
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
