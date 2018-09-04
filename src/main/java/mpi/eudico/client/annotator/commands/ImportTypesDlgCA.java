package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.EditTypeDialog2;

/**
 * Creates a JDialog for importing Linguistic Types.
 *
 * @author Han Sloetjes
 */
public class ImportTypesDlgCA extends CommandAction {
    /**
     * Creates a new ImportTypesDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ImportTypesDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.IMPORT_TYPES);
    }
    
    /**
     * Creates a new edit type dialog command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.EDIT_TYPE);
    }
    
    /**
     * Returns the transcription
     *
     * @return the transcription
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Returns the arguments, here the Import mode constant
     *
     * @return the args for the command
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Integer.valueOf(EditTypeDialog2.IMPORT) };
    }
}
