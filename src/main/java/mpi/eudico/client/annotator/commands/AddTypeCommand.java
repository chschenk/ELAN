package mpi.eudico.client.annotator.commands;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;


/**
 * Adds a new Linguistic Type to a Transcription.
 *
 * @author Han Sloetjes
 * @version jun 04 added the name of a linked Controlled Vocabulary
 * @version apr 08 added the id of a data category (ISO 12620, DCR); this value is added as last object 
 * in the arguments array
 * @version dec 10 added lexicon query bundle; this value is added as last object
 * in the arguments array
 */
public class AddTypeCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private Transcription transcription;

    // store the arguments for undo /redo, not used actually
    private String typeName;
    private Constraint constraint;
    private String cvName;
    private String dcId;
    private boolean timeAlignable;
    private LexiconQueryBundle2 queryBundle;

    // the new LinguisticType
    private LinguisticType newLT;

    /**
     * Creates a new AddTypeCommand instance
     *
     * @param name the name of the command
     */
    public AddTypeCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action.
     */
    @Override
	public void undo() {
        if (newLT != null) {
            transcription.removeLinguisticType(newLT);
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if (newLT != null) {
            transcription.addLinguisticType(newLT);
        }
    }

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the Transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the name of the type (String)</li>
	 *            <li>arg[1] = the the stereotype name for the type
	 *            (ConstraintImpl)</li>
	 *            <li>arg[2] = the name of the ControlledVocabulary (String)</li>
	 *            <li>arg[3] = time alignable value (Boolean)</li>
	 *            <li>arg[4] = graphics reference allowed value (Boolean) (IGNORED NOW)</li>
	 *            <li>arg[5] = data category ID (String)</li>
	 *            <li>arg[6] = lexicon query bundle</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (Transcription) receiver;
        typeName = (String) arguments[0];
        constraint = (Constraint) arguments[1];
        cvName = (String) arguments[2];
        timeAlignable = ((Boolean) arguments[3]).booleanValue();
        // Ignore argument[4]
        if (arguments.length >= 6) {
        	dcId = (String) arguments[5];
        }
		if (arguments.length >= 7) {
        	queryBundle = (LexiconQueryBundle2) arguments[6];
        }
        newLT = new LinguisticType(typeName);
        newLT.setControlledVocabularyName(cvName);
        newLT.setTimeAlignable(timeAlignable);

        if (constraint != null) {
            newLT.addConstraint(constraint);
        }
        if (dcId != null) {
        	newLT.setDataCategory(dcId);
        }
		if (queryBundle != null) {
        	newLT.setLexiconQueryBundle(queryBundle);
        }
        transcription.addLinguisticType(newLT);
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
