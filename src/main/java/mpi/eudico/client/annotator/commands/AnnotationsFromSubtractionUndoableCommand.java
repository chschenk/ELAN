package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.tier.AnnotationFromSubtraction;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A command that creates annotations based on subtracting the annotations on the  
 * the selected tiers. The new annotations are created on a new tier.
 *
 * @author Han Sloetjes
 * @author aarsom 
 * @version November, 2011
 */
public class AnnotationsFromSubtractionUndoableCommand extends AnnotationFromSubtraction implements UndoableCommand {   
	/**
     * Constructor.
    *
    * @param name the name of the command
    */
   public AnnotationsFromSubtractionUndoableCommand(String name) {
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
           	dt = transcription.getTierWithId(sourceTiers[0]);
               LinguisticType lType = dt.getLinguisticType();

               if (lType.getConstraints() != null) {
                   // the source tier should be a toplevel tier
                   ClientLogger.LOG.severe("The source tier is not a root tier.");

                   return;
               }

               TierImpl dTier = new TierImpl(null, destTierName,
                       dt.getParticipant(), transcription, lType);
               dTier.setAnnotator(dt.getAnnotator());
               dTier.setDefaultLocale(dt.getDefaultLocale());
               dTier.setLangRef(dt.getLangRef());
               transcription.addTier(dTier);
               dt = dTier;
           }

           if (dt == null) {
               ClientLogger.LOG.severe(
                   "Could not find the destination tier for redo");

               return;
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
       		
       		//remove that tier now
               if (dt != null) {
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
