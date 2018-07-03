package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.lexicon.LexiconLink;

public class AddLexLinkCommand implements UndoableCommand {

    private String commandName;

    // receiver
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    private LexiconLink link;

    /**
     * Creates a new AddLexLinkCommand instance
     *
     * @param name the name of the command
     */
    public AddLexLinkCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Deletes the added Lexicon Link from the Transcription.
     */
    @Override
	public void undo() {
    	if (transcription != null) {
    		transcription.removeLexiconLink(link);
    	}
    }

    /**
     * The redo action. Adds the created Lexicon Link to the Transcription.
     */
    @Override
	public void redo() {
		if (transcription != null) {
			if (transcription.getControlledVocabulary(link.getName()) == null) {
				transcription.addLexiconLink(link);
			}
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: 
     * 		  arg[0] = the Lexicon Link object (LexiconLink)
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        
        if (arguments[0] instanceof LexiconLink) {
			link = (LexiconLink) arguments[0];
        	if (transcription.getLexiconLink(link.getName()) == null) {
        		transcription.addLexiconLink(link);
        	}
        }
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
