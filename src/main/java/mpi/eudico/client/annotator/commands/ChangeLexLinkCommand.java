package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.lexicon.LexiconLink;

public class ChangeLexLinkCommand implements UndoableCommand {

    private String commandName;

    // receiver
    private Transcription transcription;

    // store the arguments for undo /redo
    
    private LexiconLink oldLink;
    private LexiconLink newLink;

    /**
     * Creates a new AddLexLinkCommand instance
     *
     * @param name the name of the command
     */
    public ChangeLexLinkCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Deletes the added Lexicon Link from the Transcription.
     */
    @Override
	public void undo() {
    	if (transcription != null) {
    		//transcription.changeLexiconLink(newLink, oldLink);
    	}
    }

    /**
     * The redo action. Adds the created Lexicon Link to the Transcription.
     */
    @Override
	public void redo() {
		if (transcription != null) {
			//transcription.changeLexiconLink(oldLink, newLink);
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: 
     * 		  arg[0] = the Lexicon Link object 
     * 		(Lexicon Link)
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;
        
        if (arguments[0] instanceof LexiconLink && arguments[1] instanceof LexiconLink) {
			oldLink = (LexiconLink) arguments[0];
			newLink = (LexiconLink) arguments[1];
			//transcription.changeLexiconLink(oldLink, newLink);
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
