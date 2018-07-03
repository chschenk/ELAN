package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;


/**
 * Changes a Linguistic Type in a Transcription.
 *
 * @author Han Sloetjes
 * @version jun 04 added the name of a linked Controlled Vocabulary
 * @version apr 08 added the id of a Data Category
 */
public class ChangeTypeCommand implements UndoableCommand {
    /** Holds value of property DOCUMENT ME! */
    //private static final Logger LOG = Logger.getLogger(ChangeTypeCommand.class.getName());
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;

    // store the arguments for undo /redo
    // old values
    private String oldTypeName;
    private Constraint oldConstraint;
    private String oldCVName;
    private boolean oldTimeAlignable;
    private String oldDcId;
	private LexiconQueryBundle2 oldQueryBundle;

    // new values
    private String typeName;
    private Constraint constraint;
    private String cvName;
    private boolean timeAlignable;
    private String dcId;
    private LexiconQueryBundle2 queryBundle;

    // the LinguisticType
    private LinguisticType linType;

    /**
     * Creates a new ChangeTypeCommand instance
     *
     * @param name the name of the command
     */
    public ChangeTypeCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action.
     */
    @Override
	public void undo() {
        if (linType != null) {
            List<Constraint> constraints = new ArrayList<Constraint>();
            constraints.add(oldConstraint);

            transcription.changeLinguisticType(linType, oldTypeName,
                    constraints, oldCVName, oldTimeAlignable, oldDcId, oldQueryBundle);
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if (linType != null) {
        	List<Constraint> constraints = new ArrayList<Constraint>();
            constraints.add(constraint);

            transcription.changeLinguisticType(linType, typeName, constraints,
                    cvName, timeAlignable, dcId, queryBundle);
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
	 *            <li>arg[0] = the new name of the type (String)</li>
	 *            <li>arg[1] = the new Constraint for the type (Constraint)</li>
	 *            <li>arg[2] = the name of the new ControlledVocabulary (String)
	 *            </li>
	 *            <li>arg[3] = the new time alignable value (Boolean)</li>
	 *            <li>arg[4] = the new graphics reference allowed value
	 *            (Boolean) (IGNORED NOW)</li>
	 *            <li>arg[5] = the Linguistic Type to change</li>
	 *            <li>arg[6] = the id of a referenced data category</li>
	 *            <li>arg[7] = the name of the new lexicon service</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        // new
        typeName = (String) arguments[0];
        constraint = (Constraint) arguments[1];
        cvName = (String) arguments[2];
        timeAlignable = ((Boolean) arguments[3]).booleanValue();
        // Ignore arguments[4]
        linType = (LinguisticType) arguments[5];
        if (arguments.length >= 7) {
        	dcId = (String) arguments[6];
        }
        if (arguments.length >= 8) {
    		queryBundle = (LexiconQueryBundle2) arguments[7];
    	}
        // old
        oldTypeName = linType.getLinguisticTypeName();
        oldConstraint = linType.getConstraints();
        oldCVName = linType.getControlledVocabularyName();
        oldTimeAlignable = linType.isTimeAlignable();
        oldDcId = linType.getDataCategory();
        oldQueryBundle = linType.getLexiconQueryBundle();
        
        List<Constraint> constraints = new ArrayList<Constraint>();
        constraints.add(constraint);

        transcription.changeLinguisticType(linType, typeName, constraints,
                cvName, timeAlignable, dcId, queryBundle);
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
