package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.EditTypeDialog2;


/**
 * Brings up a JDialog for defining a new Linguistic Type.
 *
 * @author Han Sloetjes
 */
public class AddLingTypeDlgCA extends CommandAction {
    /**
     * Creates a new AddLingTypeDlgCA instance
     *
     * @param viewerManager the viewer manager
     */
    public AddLingTypeDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.ADD_TYPE);
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
     * Returns the arguments, here the Add mode constant
     *
     * @return the args for the command
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Integer.valueOf(EditTypeDialog2.ADD) };
    }
}
