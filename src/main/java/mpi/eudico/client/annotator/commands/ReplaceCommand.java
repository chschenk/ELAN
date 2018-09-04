package mpi.eudico.client.annotator.commands;

import java.util.logging.Logger;

import mpi.eudico.client.annotator.search.result.model.ElanMatch;
import mpi.eudico.client.annotator.search.result.model.Replace;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.search.content.result.model.ContentResult;

/**
 * A Command that tokenizes the contents of the annotations a source tier new
 * annotations on a destination tier.
 * 
 * @author Han Sloetjes
 */
public class ReplaceCommand implements UndoableCommand {
    private String commandName;

    private static final Logger LOG = Logger.getLogger(ReplaceCommand.class.getName());

    // store state
    private TranscriptionImpl transcription;

    private String replaceString;

    private Annotation[] modifiedAnnotations;

    private String[] oldValues;

    /**
     * Creates a new TokenizeCommand instance.
     * 
     * @param name
     *            the name of the command
     */
    public ReplaceCommand(String name) {
        commandName = name;
    }

    /**
     * Undo the changes made by this command.
     */
    @Override
	public void undo() {
        transcription.setNotifying(false);
        for (int i = 0; i < modifiedAnnotations.length; i++) {
            String currentValue = modifiedAnnotations[i].getValue();
            modifiedAnnotations[i].setValue(oldValues[i]);
            oldValues[i] = currentValue;
        }
        transcription.setNotifying(true);
    }

    /**
     * Redo the changes made by this command.
     */
    @Override
	public void redo() {
        undo();
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     * 
     * @param receiver
     *            the TranscriptionImpl
     * @param arguments
     *            the arguments:
     *            <ul>
     *            <li>arg[0] = Result
     *            <li>arg[1] = Replace String</li>
     *            </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        ContentResult result = (ContentResult) arguments[0];
        replaceString = (String) arguments[1];
        oldValues = new String[result.getRealSize()];
        modifiedAnnotations = new Annotation[result.getRealSize()];
        for (int i = 1; i <= result.getRealSize(); i++) {
            ElanMatch match = (ElanMatch) result.getMatch(i);
            modifiedAnnotations[i - 1] = match.getAnnotation();
            oldValues[i - 1] = modifiedAnnotations[i - 1].getValue();
        }
        Replace.execute(result, replaceString, transcription);
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

}
