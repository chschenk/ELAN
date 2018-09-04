package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * The export document preferences command action.
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExportPrefsCA extends CommandAction {
    /**
     * Constructor
     *
     * @param theVM the viewermanager
     */
    public ExportPrefsCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.EXPORT_PREFS);
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EXPORT_PREFS);
    }

    /**
     * The Transcription is the "receiver".
     *
     * @return the receiver
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }
}
