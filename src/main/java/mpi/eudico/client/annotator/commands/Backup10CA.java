package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction to set the backup delay to 10 minutes.
 *
 * @author Han Sloetjes
 */
public class Backup10CA extends CommandAction {
    /** Holds value of property DOCUMENT ME! */
    private final Object[] arg = new Object[] { Constants.BACKUP_10 };

    /**
     * Creates a new Backup10CA instance
     *
     * @param viewerManager DOCUMENT ME!
     */
    public Backup10CA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.BACKUP_10);
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
