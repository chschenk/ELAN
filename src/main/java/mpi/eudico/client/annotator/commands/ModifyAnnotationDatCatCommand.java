package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Modifies the reference to an ISO data category. Note: does not yet handle
 * compound references!
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationDatCatCommand implements UndoableCommand {
    private String commandName;
    private Transcription transcription;
    private AnnotationDataRecord annotationRecord;
    private String oldValue;
    private String newValue;

    /**
     * Constructor.
     *
     * @param commandName the name of the command
     */
    public ModifyAnnotationDatCatCommand(String commandName) {
        super();
        this.commandName = commandName;
    }

    /**
     * The redo action
     */
    @Override
	public void redo() {
        if ((annotationRecord != null) && (transcription != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            AbstractAnnotation annotation = (AbstractAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // doublecheck to see if we have the right annotation
            if ((annotation != null) &&
                    (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
            	annotation.removeExtRef(new ExternalReferenceImpl(oldValue, ExternalReference.ISO12620_DC_ID));
                if (newValue == null) {
                	// Do nothing
                } else {
                    annotation.addExtRef(new ExternalReferenceImpl(newValue,
                            ExternalReference.ISO12620_DC_ID));
                }
            }
        }
    }

    /**
     * The undo action
     */
    @Override
	public void undo() {
        if ((annotationRecord != null) && (transcription != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            AbstractAnnotation annotation = (AbstractAnnotation) tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // doublecheck to see if we have the right annotation
    		if ((annotation != null) &&
                    (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
            	annotation.removeExtRef(new ExternalReferenceImpl(newValue, ExternalReference.ISO12620_DC_ID));
                if (oldValue == null) {
                    // Do nothing
                } else {
                    annotation.addExtRef(new ExternalReferenceImpl(oldValue,
                            ExternalReference.ISO12620_DC_ID));
                }
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Annotation (AbstractAnnotation)
     * @param arguments the arguments:  <ul><li>arg[0] = the new value of the
     *        annotation data category reference (String)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        AbstractAnnotation annotation = (AbstractAnnotation) receiver;

        if (annotation != null) {
            transcription = annotation.getTier().getTranscription();
        }

        annotationRecord = new AnnotationDataRecord(annotation);

        newValue = (String) arguments[0];

        oldValue = annotation.getExtRefValue(ExternalReference.ISO12620_DC_ID);
        
        if (oldValue != null) {
			annotation.removeExtRef(new ExternalReferenceImpl(oldValue,
					ExternalReference.ISO12620_DC_ID));
		}
        
        if (newValue != null) {
        	annotation.addExtRef(new ExternalReferenceImpl(newValue,
					ExternalReference.ISO12620_DC_ID));
        }
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
