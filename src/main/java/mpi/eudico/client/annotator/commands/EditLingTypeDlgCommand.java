package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.EditTypeDialog2;

import mpi.eudico.server.corpora.clom.Transcription;

import javax.swing.JFrame;


/**
 * Creates a JDialog for defining, changing or deleting a Linguistic Type.
 *
 * @author Han Sloetjes
 */
public class EditLingTypeDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new EditLingTypeDlgCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public EditLingTypeDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the dialog mode
     *        (Integer)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        // receiver is a transcription
        // arguments[0] is the edit mode, ADD, CHANGE or DELETE
        Transcription transcription = (Transcription) receiver;
        JFrame frame = ELANCommandFactory.getRootFrame(transcription);
        Integer mode = (Integer) arguments[0];
        new EditTypeDialog2(frame, true, transcription, mode.intValue()).setVisible(true);
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
