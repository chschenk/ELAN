package mpi.eudico.client.annotator.commands;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A Command to change tier attributes.
 *
 * @author HB, HS
 * @version 1.0
 */
public class ChangeTierAttributesCommand implements UndoableCommand {
    private String commandName;

    // old state
    private String oldTierName;
    private String oldParticipant;
    private String oldAnnotator;
    private Locale oldLocale;
    private LinguisticType oldLingType;
    private TierImpl oldParentTier;
    private String oldLangRef;

    // new state
    private String tierName;
    private TierImpl parentTier;
    private String lingTypeName;
    private LinguisticType lingType;
    private String participant;
    private String annotator;
    private Locale locale;
    private String langRef;

    // receiver
    private TierImpl tier;
    private TranscriptionImpl transcription;

    //private ArrayList annotationsNodes;
    /**
     * Creates a new ChangeTierAttributesCommand instance
     *
     * @param theName the name of the command
     */
    public ChangeTierAttributesCommand(String theName) {
        commandName = theName;
    }

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the Tier
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the tier name (String)</li>
	 *            <li>arg[1] = the parent tier (Tier)</li>
	 *            <li>arg[2] = the linguistic type (String)</li>
	 *            <li>arg[3] = the participant (String)</li>
	 *            <li>arg[4] = the annotator (String)</li>
	 *            <li>arg[5] = the default language (Locale)</li>
	 *            <li>arg[6] = the langRef to set</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        // receiver is Tier
        tier = (TierImpl) receiver;

        transcription = tier.getTranscription();

        // arguments, store for redo
        tierName = (String) arguments[0];
        parentTier = (TierImpl) arguments[1];
        lingTypeName = (String) arguments[2];
        participant = (String) arguments[3];
        annotator = (String) arguments[4];
        locale = (Locale) arguments[5];
		langRef = (String) arguments[6];

        if (tier != null) {
            setWaitCursor(true);

            try {
                oldTierName = tier.getName();
                oldParticipant = tier.getParticipant();
                oldLocale = tier.getDefaultLocale();
                oldLingType = tier.getLinguisticType();
                oldParentTier = tier.getParentTier();
                oldAnnotator = tier.getAnnotator();
                oldLangRef = tier.getLangRef();

                // first back up the annotations if necessary
                // HS sep-04 
                // as long as there is no proper mechanism of updating  
                // existing annotations, changes that can effect data integrity 
                // are prevented in the change tier dialog

                /*
                   if ((parentTier != oldParentTier) ||
                           !oldLingType.getLinguisticTypeName().equals(lingTypeName)) {
                
                       annotationsNodes = new ArrayList();
                       Vector annos = tier.getAnnotations(null);
                       Iterator anIter = annos.iterator();
                       AbstractAnnotation ann;
                       while (anIter.hasNext()) {
                           ann = (AbstractAnnotation) anIter.next();
                           annotationsNodes.add(AnnotationRecreator.createTreeForAnnotation(
                                   ann));
                       }
                
                   }
                 */
                if (!tierName.equals(oldTierName)) {
                    // assumes there has been a check on the uniqueness of the name 
                    tier.setName(tierName);
                }

                if (parentTier != oldParentTier) {
                    tier.setParentTier(parentTier);
                }

                if (!participant.equals(oldParticipant)) {
                    tier.setParticipant(participant);
                }

                if (!annotator.equals(oldAnnotator)) {
                    tier.setAnnotator(annotator);
                }
                
                if (locale == null || locale != oldLocale) {
                    tier.setDefaultLocale(locale);
                }

                if ((oldLingType == null) ||
                        (lingTypeName != oldLingType.getLinguisticTypeName())) {
                    List<LinguisticType> types = tier.getTranscription().getLinguisticTypes();
                    LinguisticType t = null;
                    Iterator typeIter = types.iterator();

                    while (typeIter.hasNext()) {
                        t = (LinguisticType) typeIter.next();

                        if (t.getLinguisticTypeName().equals(lingTypeName)) {
                            break;
                        }
                    }

                    if (t != null) {
                        lingType = t;
                        tier.setLinguisticType(lingType);
                    }
                }
                
                if (!tierName.equals(oldTierName)) {
                	updatePreferences(oldTierName, tierName);
                }
                
                if ((langRef != null) &&
                        (!langRef.equals(oldLangRef))) {
                   	tier.setLangRef(langRef);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                setWaitCursor(false);
            }

            setWaitCursor(false);
        }
    }

    /**
     * The undo action.
     */
    @Override
	public void undo() {
        if (tier != null) {
            try {
                if ((tierName != null) && (!tierName.equals(oldTierName))) {
                    tier.setName(oldTierName);
                }

                if (parentTier != oldParentTier) {
                    tier.setParentTier(oldParentTier);
                }

                tier.setLinguisticType(oldLingType);

                if ((participant != null) &&
                        (!participant.equals(oldParticipant))) {
                    tier.setParticipant(oldParticipant);
                }

                if ((annotator != null) &&
                        (!annotator.equals(oldAnnotator))) {
                    tier.setAnnotator(oldAnnotator);
                }
                
                if (locale != oldLocale) {
                    tier.setDefaultLocale(oldLocale);
                }

                // finally recreate annotations if necessary
                //HS sep-04 see execute

                /*
                   if ((parentTier != oldParentTier) ||
                           !oldLingType.getLinguisticTypeName().equals(lingTypeName)) {
                       if ((transcription != null) && (annotationsNodes != null) &&
                               (annotationsNodes.size() > 0)) {
                           setWaitCursor(true);
                
                           tier.removeAllAnnotations();
                           DefaultMutableTreeNode node;
                           if (tier.hasParentTier()) {
                               AnnotationRecreator.createAnnotationsSequentially(transcription,
                                   annotationsNodes);
                           } else {
                               for (int i = 0; i < annotationsNodes.size(); i++) {
                                   node = (DefaultMutableTreeNode) annotationsNodes.get(i);
                                   AnnotationRecreator.createAnnotationFromTree(transcription,
                                       node);
                               }
                           }
                           setWaitCursor(false);
                       }
                   }
                 */
                if (!tierName.equals(oldTierName)) {
                	updatePreferences(tierName, oldTierName);
                }
                if ((langRef != null) &&
                        (!langRef.equals(oldLangRef))) {
                   	tier.setLangRef(oldLangRef);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                setWaitCursor(false);
            }
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if (tier != null) {
            setWaitCursor(true);

            try {
                if ((tierName != null) && (!tierName.equals(oldTierName))) {
                    tier.setName(tierName);
                }

                if (parentTier != oldParentTier) {
                    tier.setParentTier(parentTier);
                }

                if (lingType != null) {
                    tier.setLinguisticType(lingType);
                }

                /*
                   if ((oldLingType == null) ||
                           (lingTypeName != oldLingType.getLinguisticTypeName())) {
                       Vector types = ((Transcription) (tier.getParent())).getLinguisticTypes();
                       LinguisticType t = null;
                       Iterator typeIter = types.iterator();
                       while (typeIter.hasNext()) {
                           t = (LinguisticType) typeIter.next();
                           if (t.getLinguisticTypeName().equals(lingTypeName)) {
                               break;
                           }
                       }
                       tier.setLinguisticType(t);
                   }
                 */
                if ((participant != null) &&
                        (!participant.equals(oldParticipant))) {
                    tier.setParticipant(participant);
                }

                if (!annotator.equals(oldAnnotator)) {
                    tier.setAnnotator(annotator);
                }
                
                if (locale != oldLocale) {
                    tier.setDefaultLocale(locale);
                }

                if (!tierName.equals(oldTierName)) {
                	updatePreferences(oldTierName, tierName);
                }
                
                if ((langRef != null) &&
                        (!langRef.equals(oldLangRef))) {
                   	tier.setLangRef(langRef);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                setWaitCursor(false);
            }

            setWaitCursor(false);
        }
    }

    /**
     * Updates a few user preferences for the tier, if any.
     * 
     * @param oldTierName old name in the preferences maps
     * @param tierName the new name in the preferences maps
     */
    private void updatePreferences(String oldTierName, String tierName) {
    	Map<String, Color> colorMap = Preferences.getMapOfColor("TierColors", transcription);
    	if (colorMap != null) {
    		if (colorMap.containsKey(oldTierName)) {
    			Color col = colorMap.remove(oldTierName);
    			colorMap.put(tierName, col);
    			Preferences.set("TierColors", colorMap, transcription, true);
    		}
    	}
    	
		Map<String, Font> fontsMap = Preferences.getMapOfFont("TierFonts", transcription);
		if (fontsMap != null) {
			if (fontsMap.containsKey(oldTierName)) {
				Font font = fontsMap.remove(oldTierName);
				fontsMap.put(tierName, font);
				Preferences.set("TierFonts", fontsMap, transcription, true);
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

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }
}
