package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 0, meaning no automatic backup.
 *
 * @author Han Sloetjes
 */
public class BackupNeverCA extends CommandAction {
    /** Holds value of property DOCUMENT ME! */
    private final Object[] arg = new Object[] { Constants.BACKUP_NEVER };

    /**
     * Creates a new BackupNeverCA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public BackupNeverCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_NEVER);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.BACKUP);
    }

    /**
     * The receiver is BackupCA.
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return ELANCommandFactory.getCommandAction(vm.getTranscription(),
            ELANCommandFactory.BACKUP);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        return arg;
    }
}
