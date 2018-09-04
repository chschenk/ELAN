package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 5 minutes.
 *
 * @author Han Sloetjes
 */
public class Backup5CA extends CommandAction {
    /**
     * Creates a new Backup5CA instance
     *
     * @param viewerManager the viewermanager
     */
    public Backup5CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_5);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BACKUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     * The arguments.
     *
     * @return the arguments
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Constants.BACKUP_5 };
    }
}
