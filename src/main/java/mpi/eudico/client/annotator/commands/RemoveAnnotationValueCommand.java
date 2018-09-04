/*
 * File:     RemoveAnnotationValueCommand.java
 * Project:  MPI Linguistic Application
 * Date:     25 January 2007
 *
 * Feature added by Ouriel Grynszpan, European contract MATHESIS IST-027574
 * CNRS UMR 7593, Paris, France
 *
 * Copyright (C) 2001-2005  Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * A command for removing an annotation value.
 *
 * @author Ouriel Grynszpan
 */
public class RemoveAnnotationValueCommand implements UndoableCommand {
    private String commandName;
    private Transcription transcription;
    private AnnotationDataRecord annotationRecord;

    /**
     * Creates a new RemoveAnnotationValueCommand instance
     *
     * @param name the name of the command
     */
    public RemoveAnnotationValueCommand(String name) {
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
                if (annotation instanceof AbstractAnnotation) {
                	((AbstractAnnotation)annotation).setExtRef(annotationRecord.getExtRef());
                }
                annotation.setValue(annotationRecord.getValue());
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
                annotation.setCVEntryId(null);
                if (annotation instanceof AbstractAnnotation) {
                	((AbstractAnnotation)annotation).setExtRef(null);
                }
                annotation.setValue("");
            }
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Annotation
     * @param arguments the arguments:  <ul><li>null</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        Annotation annotation = (Annotation) receiver;

        if (annotation != null) {
            transcription = annotation.getTier().getTranscription();
        }

        annotationRecord = new AnnotationDataRecord(annotation);

        annotation.setCVEntryId(null);
        if (annotation instanceof AbstractAnnotation) {
        	((AbstractAnnotation)annotation).setExtRef(null);
        }
        // do setValue() last: it triggers the GUI update.
        annotation.setValue("");
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
