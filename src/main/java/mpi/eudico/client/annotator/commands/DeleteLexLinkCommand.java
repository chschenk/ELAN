package mpi.eudico.client.annotator.commands;

import java.util.HashMap;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconLink;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

public class DeleteLexLinkCommand implements UndoableCommand {
    private String commandName;

    // receiver; the transcription 
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    private LexiconLink link;
    
    //
	private HashMap<LinguisticType, LexiconQueryBundle2> effectedTypes;

    /**
     * Creates a new DeleteLexLinkCommand instance
     *
     * @param name the name of the command
     */
    public DeleteLexLinkCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Adds the removed Lexicon Link to the Transcription.
     */
    @Override
	public void undo() {
    	if (transcription != null && link != null) {
			transcription.addLexiconLink(link);
    		// restore the linguistic types that were using this Lexicon Link before deletion
    		for (LinguisticType type : effectedTypes.keySet()) {
    			type.setLexiconQueryBundle(effectedTypes.get(type));
    		}   		
    	}
    }

    /**
     * The redo action. Removes the Lexicon Link from the Transcription.
     */
    @Override
	public void redo() {
    	if (transcription != null) {
    		transcription.removeLexiconLink(link);
    	}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments: <ul><li>arg[0] = the  Lexicon Link</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        link = (LexiconLink) arguments[0];
		
		if (transcription != null) {
			effectedTypes = new HashMap<LinguisticType, LexiconQueryBundle2>();
			for(LinguisticType type : transcription.getLinguisticTypesWithLexLink(
					link.getName())) {
				effectedTypes.put(type, type.getLexiconQueryBundle());
			}
			transcription.removeLexiconLink(link);
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
