package mpi.eudico.client.annotator.commands;

import java.util.List;
import java.util.Locale;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * A Command to add a tier to a transcription.
 *
 * @author Hennie Brugman
 * @version 1.2
 */
public class AddTierCommand implements UndoableCommand {
    private String commandName;
    private TierImpl tier = null;

    // receiver
    private TranscriptionImpl transcription;

    /**
     * Creates a new AddTierCommand instance
     *
     * @param theName the name of the command
     */
    public AddTierCommand(String theName) {
        commandName = theName;
    }

	/**
	 * Adds a tier to the transcription.
	 *
	 * @param receiver
	 *            the transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the tier name (String)</li>
	 *            <li>arg[1] = the parent tier (TierImpl)</li>
	 *            <li>arg[2] = the linguistic type name (String)</li>
	 *            <li>arg[3] = the participant name (String)</li>
	 *            <li>arg[4] = the annotator (String)</li>
	 *            <li>arg[5] = the default language (Locale)</li>
	 *            <li>arg[6] = the langRef to set</li>
	 *            </ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		// receiver is Transcription
		transcription = (TranscriptionImpl) receiver;

        // arguments
        String tierName = (String) arguments[0];
        TierImpl parentTier = (TierImpl) arguments[1];
        String lingType = (String) arguments[2];
        String participant = (String) arguments[3];
        String annotator = (String) arguments[4];
        Locale locale = (Locale) arguments[5];
		String langRef = (String) arguments[6];

        if (transcription != null) {
            tier = new TierImpl(parentTier, tierName, null, transcription, null);

            List<LinguisticType> types = transcription.getLinguisticTypes();

            for (LinguisticType t : types) {
                if (t.getLinguisticTypeName().equals(lingType)) {
                    tier.setLinguisticType(t);
                    break;
                }
            }

            tier.setParticipant(participant);
            tier.setAnnotator(annotator);
            tier.setDefaultLocale(locale);
			tier.setLangRef(langRef);

            // transcription does not perform any checks..
            if (transcription.getTierWithId(tierName) == null) {
                transcription.addTier(tier);
            }
        }
    }

    /**
     * The undo action.
     */
    @Override
	public void undo() {
        if (tier != null) {
            transcription.removeTier(tier);
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if (tier != null) {
            // transcription does not perform any checks..
            if (transcription.getTierWithId(tier.getName()) == null) {
                transcription.addTier(tier);
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
