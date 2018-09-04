package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * The import document preferences command action.
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImportPrefsCA extends CommandAction {
    /**
     * Creates a new ImportPrefsCA instance
     *
     * @param theVM the viewermanager
     */
    public ImportPrefsCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.IMPORT_PREFS);
    }

    /**
     * Creates anew command
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.IMPORT_PREFS);
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
