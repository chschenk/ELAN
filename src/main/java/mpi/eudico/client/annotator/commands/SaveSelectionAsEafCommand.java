package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.export.ExportSelectionAsEAF;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A Command that creates a new Transcription, copies CV's, LinguisticTypes,
 * Tiers and thw annotations within/overlapping the selected time interval.
 * All copied annotations are forced in to the interval.  
 *
 * @author Han Sloetjes
 */
public class SaveSelectionAsEafCommand implements Command {
    private String commandName;
    
    /**
     * Creates a new CopyTierCommand instance
     *
     * @param theName the name of the command
     */
    public SaveSelectionAsEafCommand(String theName) {
        commandName = theName;
    }
    
    /**
     * Creates a new Transcription and adds copies of CV's, Linguistic Types, Tiers 
     * and the annotations within the selection. Overlapping annotations will be
     * forced into the selectioninterval.   
     * <b>Note: </b>it is assumed the types and order of the arguments
     * are correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments: <ul><li>arg[0] = the selection begin time
     *        (Long)</li> <li>arg[1] the selection end time (Long)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl transcription = (TranscriptionImpl) receiver;
        long beginTime = ((Long) arguments[0]).longValue();
        long endTime = ((Long) arguments[1]).longValue();
        // double check
        if (beginTime == endTime) {
            return;
        }
        
        new ExportSelectionAsEAF(transcription, beginTime, endTime);
    }
    
    /**
     * Returns the name of the command
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }
}
