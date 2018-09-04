package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.gui.EditTypeDialog2;


/**
 * Brings up a JDialog for changing a Linguistic Type.
 *
 * @author Han Sloetjes
 */
public class ChangeLingTypeDlgCA extends CommandAction {
    /**
     * Creates a new ChangeLingTypeDlgCA instance
     *
     * @param viewerManager the viewermanager
     */
    public ChangeLingTypeDlgCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.CHANGE_TYPE);
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
     * Returns the arguments, here the Change mode constant
     *
     * @return the args for the command
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Integer.valueOf(EditTypeDialog2.CHANGE) };
    }
}
