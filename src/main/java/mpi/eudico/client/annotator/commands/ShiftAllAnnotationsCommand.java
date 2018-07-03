package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * SHifts all time slots in a Transcription.
 *
 * @author Han Sloetjes
 */
public class ShiftAllAnnotationsCommand implements UndoableCommand {
    private String commandName;

    // receiver; the transcription 
    private TranscriptionImpl transcription;
    private long shiftValue = 0;

    /**
     * Creates a new shift all command.
     *
     * @param name the name of the command
     */
    public ShiftAllAnnotationsCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the amount of
     *        milliseconds that should be added to all time slot values
     *        (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        if ((arguments != null) && (arguments.length >= 1)) {
            shiftValue = ((Long) arguments[0]).longValue();
        }

        shiftAll(shiftValue);
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * The undo action. All Time Slots are shifted back the same amount.
     */
    @Override
	public void undo() {
        shiftAll(-shiftValue);
    }

    /**
     * The redo action. All Time Slots are shifted again the same amount.
     */
    @Override
	public void redo() {
        shiftAll(shiftValue);
    }

    /**
     * Issues the actual shifting. 
     *
     * @param value the shift value in ms
     */
    private void shiftAll(long value) {
        if (transcription != null) {
            try {
				transcription.shiftAllAnnotations(value);
            } catch (IllegalArgumentException iae){
            	ClientLogger.LOG.warning(iae.getMessage());
            }
        }
    }
}
