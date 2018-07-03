package mpi.eudico.client.annotator.commands;

import java.util.Locale;

import mpi.eudico.client.annotator.tier.MergeTiers;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Undoable Command that merges the annotations on tiers and creates new
 * annotations on a third tier. Overlapping annotations are merged into one
 * new annotation. Non overlapping annotations are copied as is. The content
 * depends  on the user's choice
 *
 * @updatedBy aarsom
 * @version Feb, 2014
 */
public class MergeTiersUndoableCommand extends MergeTiers implements UndoableCommand {    
	
	private String tierParentTier;
	private String tierParticipant;
	private String tierAnnotator;
	private String tierLingTypeName;
	private Locale tierDefaultLocale; 
	private String tierLangRef;

	/**
     * Constructor.
     *
     * @param name the name of the command
     */
    public MergeTiersUndoableCommand(String name) {
       super(name);
    }

    /**
     * Recreates the new tier (if applicable) and the newly created
     * annotations.
     */
    @Override
	public void redo() {
        if (transcription != null) {
            TierImpl dt = transcription.getTierWithId(destTierName);

            if( dt == null ){
                TierImpl parent = null;
                if (tierParentTier != null) {
                	parent = transcription.getTierWithId(tierParentTier);
                }

                LinguisticType lType = transcription.getLinguisticTypeByName(tierLingTypeName);
                if (lType == null) {
                	ClientLogger.LOG.severe("Cannot find Linguistic Type '"+tierLingTypeName+"'to restore");
                	return;
                }

                TierImpl dTier = new TierImpl(parent, destTierName,
                		tierParticipant, transcription, lType);
                dTier.setAnnotator(tierAnnotator);
                dTier.setDefaultLocale(tierDefaultLocale);
                dTier.setLangRef(tierLangRef);
                transcription.addTier(dTier);
                dt = dTier;
            }

            if (createdAnnos == null || createdAnnos.size() == 0) {
                ClientLogger.LOG.info("No annotations to restore");

                return;
            }

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            transcription.setNotifying(false);

            AnnotationDataRecord record;
            Annotation ann;

            for (int i = 0; i < createdAnnos.size(); i++) {
                record = createdAnnos.get(i);
                ann = dt.createAnnotation(record.getBeginTime(),
                        record.getEndTime());

                if ((ann != null) && (record.getValue() != null)) {
                    ann.setValue(record.getValue());
                }
                // TODO: check where these records are created and that they
                // provide the appropriate Ids.
                if (record.getCvEntryId() != null) {
                	ann.setCVEntryId(record.getCvEntryId());
                }
            }

            transcription.setNotifying(true);
            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }
    }

    /**
     * Deletes the new annotations and/or the new tier.
     */
    @Override
	public void undo() {
        if (transcription != null) {
        	//if during the gap creation process, a new tier was added
        	if( destTierCreated ){
        		TierImpl dt = transcription.getTierWithId(destTierName);
        		
        		//remove that tier now, but remember some of its properties
                if (dt != null) {
                	if (dt.hasParentTier()) {
                		tierParentTier = dt.getParentTier().getName();
                	} else {
                    	tierParentTier = null;
                	}
                	tierParticipant = dt.getParticipant();
                	tierAnnotator = dt.getAnnotator();
                	tierLingTypeName = dt.getLinguisticType().getLinguisticTypeName();
                	tierLangRef = dt.getLangRef();
                	tierDefaultLocale = dt.getDefaultLocale(); 

                    transcription.removeTier(dt);
                }
            } else {
            	//annotations were added to an already existing tier, so remove annotations only
                TierImpl st = transcription.getTierWithId(destTierName);

                if (st != null) {
                    if ((createdAnnos != null) && (createdAnnos.size() > 0)) {
                        transcription.setNotifying(false);

                        AnnotationDataRecord record;
                        Annotation ann;

                        for (int i = 0; i < createdAnnos.size(); i++) {
                            record = createdAnnos.get(i);
                            ann = st.getAnnotationAtTime((record.getBeginTime() +
                                    record.getEndTime()) / 2);

                            if (ann != null) {
                                st.removeAnnotation(ann);
                            }
                        }
                        
                        transcription.setNotifying(true);
                    }
                }
            }
        }
    }
}

