package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;

/**
 * A CommandAction to set the backup delay to 1 minutes.
 *
 * @author Han Sloetjes
 */
public class Backup1CA extends CommandAction {

    /**
     * Creates a new Backup1CA instance
     *
     * @param viewerManager the viewer manager
     */
    public Backup1CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_1);
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
     * @return BackupCA
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     * Returns the arguments, an array of size 1
     *
     * @return an array of size 1
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Constants.BACKUP_1 };
    }

}
