package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.gui.SegmentsToTiersDialog;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.util.List;


/**
 * Creates a dialog to configure options for the recognizer segmentation to
 * tiers and annotations process.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class SegmentsToTiersDlgCommand implements Command {
    private String commandName;

    /**
     * Creates a new SegmentsToTiersDlgCommand instance
     *
     * @param name name of the command
     */
    public SegmentsToTiersDlgCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.<br>
     *
     * @param receiver the TranscriptionImpl
     * @param arguments the arguments: <ul><li>arg[0] = the list of
     *        segmentation  objects (ArrayList)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof TranscriptionImpl && (arguments != null) &&
                arguments[0] instanceof List) {
            new SegmentsToTiersDialog(ELANCommandFactory.getRootFrame(
                    (Transcription) receiver),
                (Transcription) receiver, (List) arguments[0]).setVisible(true);
        }
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
     */
    @Override
	public String getName() {
        return commandName;
    }
}
