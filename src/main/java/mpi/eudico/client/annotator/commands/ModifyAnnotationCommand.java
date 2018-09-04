package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceGroup;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A command for modifying an annotation value.
 *
 * @author Han Sloetjes
 */
public class ModifyAnnotationCommand implements UndoableCommand {
    private String commandName;
    private Transcription transcription;
    private AnnotationDataRecord annotationRecord;
    private String oldValue;
    private String newValue;
    private ExternalReference oldExtRef;
    private ExternalReference newExtRef;
    private String newCveId;

    /**
     * Creates a new ModifyAnnotationCommand instance
     *
     * @param name the name of the command
     */
    public ModifyAnnotationCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. We can not just use an object reference to the
     * annotation because the annotation might have been deleted and recreated
     * between the  calls to the execute and undo methods. The reference would
     * then be invalid.
     */
    @Override
	public void undo() {
        if ((annotationRecord != null) && (transcription != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            Annotation annotation = tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // doublecheck to see if we have the right annotation
            if ((annotation != null) &&
                    (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
                annotation.setCVEntryId(annotationRecord.getCvEntryId());                
                ((AbstractAnnotation) annotation).setExtRef(oldExtRef);
                annotation.setValue(oldValue);
                
                if (MonitoringLogger.isInitiated()) {        	
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.UNDO, MonitoringLogger.CHANGE_ANNOTATION_VALUE);        				
                }
            }
        }
    }

    /**
     * The redo action.
     *
     * @see #undo
     */
    @Override
	public void redo() {
        if ((annotationRecord != null) && (transcription != null)) {
            TierImpl tier = (TierImpl) transcription.getTierWithId(annotationRecord.getTierName());
            Annotation annotation = tier.getAnnotationAtTime(annotationRecord.getBeginTime());

            // doublecheck to see if we have the right annotation
            if ((annotation != null) &&
                    (annotation.getEndTimeBoundary() == annotationRecord.getEndTime())) {
                annotation.setCVEntryId(newCveId);
                //((AbstractAnnotation) annotation).setExtRef(newExtRef);
                replaceExternalReference((AbstractAnnotation)annotation, newExtRef);
                annotation.setValue(newValue);

                if (MonitoringLogger.isInitiated()) {        	
                	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.REDO, MonitoringLogger.CHANGE_ANNOTATION_VALUE);        				
                }
            }                
        }
    }

	/**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the Annotation (AbstractAnnotation)
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the old value of the annotation (String)</li>
	 *            <li>arg[1] = the new value of the annotation (String)</li>
	 *            <li>arg[2] = the (new) external reference object that can be
	 *            set by a CV entry</li>
	 *            <li>arg[3] = the (new) id of the controlled vocabulary entry
	 *            associated with this annotation</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        AbstractAnnotation annotation = (AbstractAnnotation) receiver;

        if (annotation != null) {
            transcription = annotation.getTier().getTranscription();
        }

        annotationRecord = new AnnotationDataRecord(annotation);

        oldValue = (String) arguments[0];
        newValue = (String) arguments[1];
        
        // if an ext ref argument is passed, remove the current ext ref of the annotation 
        // even if the new ext ref is null
        if (arguments.length >= 3) {
        	ExternalReferenceImpl eri = (ExternalReferenceImpl) arguments[2];// can be null
        	try {
        		if (annotation.getExtRef() instanceof ExternalReference) {
        			oldExtRef = annotation.getExtRef().clone();
        		}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
        	newExtRef = eri;
        	
        	replaceExternalReference(annotation, eri);
        }
        
        if (arguments.length >= 3) {
            newCveId = (String) arguments[3];        	
        } else {
            newCveId = null;        	        	
        }
        annotation.setCVEntryId(newCveId);
        // Set the text value last: it may trigger update events.
        annotation.setValue(newValue);

        if (MonitoringLogger.isInitiated()) {        	
        	MonitoringLogger.getLogger(transcription).log(MonitoringLogger.CHANGE_ANNOTATION_VALUE);        				
        }
    }

    /*
     * This method is also used in class ModifyOrAddDependentAnnotationsCommand.
     */
	public static void replaceExternalReference(AbstractAnnotation annotation, ExternalReference eri) {
		// List all Ext. Refs that are in the third argument 
		List<ExternalReference> eriList = new ArrayList<ExternalReference>();
		int listIndex = 0;
		if (eri != null) {
			eriList.add(eri);
		}
		
		while (listIndex < eriList.size()) {
			final ExternalReference externalReference = eriList.get(listIndex);
			if (externalReference instanceof ExternalReferenceGroup) {
				eriList.addAll(((ExternalReferenceGroup) externalReference).getAllReferences());
			}
			listIndex++;
		}

		// Remove all Ext. Refs of the types that are in the list
		for (ExternalReference er : eriList) {
			if (er != null && !(er instanceof ExternalReferenceGroup)) {
				String value = annotation.getExtRefValue(er.getReferenceType());
				annotation.removeExtRef(new ExternalReferenceImpl(value, er.getReferenceType()));
				if (er.getValue() != null) {
		    		annotation.addExtRef(er);
		    	}
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
