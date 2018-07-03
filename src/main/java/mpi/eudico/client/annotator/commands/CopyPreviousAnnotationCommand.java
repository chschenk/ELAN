/*
 * File:     CopyPreviousAnnotationCommand.java
 * Project:  MPI Linguistic Application
 * Date:     27 December 2006
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Copies values of currently active annotation and every simultaneous
 * annotations to next adjacent annotations. Sets the annotation next to
 * currently active  as the new active annotation. The copying takes place if
 * and only if a simultaneous annotation matches the time boundary of current
 * annotation and next annotation is adjacent and of same length.
 *
 * @author Ouriel Grynszpan
 */
public class CopyPreviousAnnotationCommand implements UndoableCommand,
    ClientLogger {
    private String commandName;
    private TranscriptionImpl transcription;
    private ArrayList<AnnotationValuesRecord> records = new ArrayList<AnnotationValuesRecord>();

    /**
     * Creates a new CopyPreviousAnnotationCommand instance
     *
     * @param theName the name of the command
     */
    public CopyPreviousAnnotationCommand(String theName) {
        commandName = theName;
    }

    /**
     * Undo the changes made by this command.
     */
    @Override
	public void undo() {
    	if (transcription != null) {
	        Iterator<AnnotationValuesRecord> annIt = records.iterator();
	        AnnotationValuesRecord record;
	        TierImpl tier;
	        Annotation ann;
	        
	        while (annIt.hasNext()) {
	        		record = annIt.next();
	        		tier = transcription.getTierWithId(record.getTierName());
	        		if (tier != null) {
	        			ann = tier.getAnnotationAtTime(record.getBeginTime());
	        			if (ann != null) {
	        				ann.setValue(record.getValue());
	        			}
	        		}
	        }
    		}
    }

    /**
     * Redo the changes made by this command.
     */
    @Override
	public void redo() {
        if (transcription != null) {
	        Iterator<AnnotationValuesRecord> annIt = records.iterator();
	        AnnotationValuesRecord record;
	        TierImpl tier;
	        Annotation ann;
	        
	        while (annIt.hasNext()) {
	        		record = annIt.next();
	        		tier = transcription.getTierWithId(record.getTierName());
	        		if (tier != null) {
	        			ann = tier.getAnnotationAtTime(record.getBeginTime());
	        			if (ann != null) {
	        				ann.setValue(record.getNewLabelValue());
	        			}
	        		}
	        }
    		}
    }

    /**
     * Copies values of annotations as current time to next adjacent and sets
     * new active annotation
     *
     * @param receiver the viewer manager
     * @param arguments arg[0] = new active annotation arg[1] = currently
     *        active annotation arg[2] = annotations simultaneous to currently
     *        active
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        ViewerManager2 vm = (ViewerManager2) receiver;
        transcription = (TranscriptionImpl)vm.getTranscription();
        Annotation newActiveAnnot = (Annotation) arguments[0];
        Annotation currentActiveAnnot = (Annotation) arguments[1];
        List<Annotation> simultaneousAnnots = (List<Annotation>) arguments[2];

        if ((null == newActiveAnnot) || (null == currentActiveAnnot)) {
            LOG.severe("Annotations on active tier not valid for copying.");

            return;
        }

        if (!((newActiveAnnot instanceof AlignableAnnotation) &&
                (currentActiveAnnot instanceof AlignableAnnotation))) {
            LOG.severe(
                "Error in constraints of retrieved annotation on the active tier.");

            return;
        }

        //Time boundaries of currently active annotation
        long curBeginTime = currentActiveAnnot.getBeginTimeBoundary();
        long curEndTime = currentActiveAnnot.getEndTimeBoundary();

        //Duration of currently active annotation
        long curLength = curEndTime - curBeginTime;

        //Time boundaries of next active annotation
        long newBeginTime = newActiveAnnot.getBeginTimeBoundary();
        long newEndTime = newActiveAnnot.getEndTimeBoundary();

        //current and new active annotations must be contiguous and of the same length
        if ((curEndTime == newBeginTime) &&
                (curLength == (newEndTime - newBeginTime))) {
            //Copy value to next annotation if next annotation has no value
            if (newActiveAnnot.getValue().trim().length() < 1) {
            		AnnotationValuesRecord record = new AnnotationValuesRecord(newActiveAnnot);
            		record.setNewLabelValue(currentActiveAnnot.getValue());
            		records.add(record);
                newActiveAnnot.setValue(currentActiveAnnot.getValue());
            }

            //Copy values of simultaneous annotations
            if ((simultaneousAnnots != null) && !simultaneousAnnots.isEmpty()) {
                Iterator<Annotation> annotsIter = simultaneousAnnots.iterator();

                while (annotsIter.hasNext()) {
                    Annotation ann = annotsIter.next();
                    copyToNextAnnotation(ann, curEndTime, curLength);
                }
            }

            setActiveAnnotation(vm, newActiveAnnot);
        }
    }

    /**
     * Sets active annotation.
     *
     * @param vm the viewermanager
     * @param newActiveAnnot the new active annotation
     */
    private void setActiveAnnotation(ViewerManager2 vm,
        Annotation newActiveAnnot) {
        vm.getActiveAnnotation().setAnnotation(newActiveAnnot);

        vm.getSelection()
          .setSelection(newActiveAnnot.getBeginTimeBoundary(),
            newActiveAnnot.getEndTimeBoundary());

        if (!vm.getMediaPlayerController().isBeginBoundaryActive()) {
            vm.getMediaPlayerController().toggleActiveSelectionBoundary();
        }

        vm.getMasterMediaPlayer()
          .setMediaTime(newActiveAnnot.getBeginTimeBoundary());
    }

    /**
     * Copies annotation to next adjacent annotation on same tier, if its time
     * boundaries match arguments and  if its length is equal to the length of
     * next adjacent annotation
     *
     * @param ann the current anotation
     * @param endTime the current end time
     * @param length the annotation duration
     */
    private void copyToNextAnnotation(Annotation ann, long endTime, long length) {
        if (ann != null) {
            Annotation nextAnn = ((TierImpl) ann.getTier()).getAnnotationAfter(ann);

            if (nextAnn != null) {
                long annBT = ann.getBeginTimeBoundary();
                long annET = ann.getEndTimeBoundary();
                long nextBT = nextAnn.getBeginTimeBoundary();
                long nextET = nextAnn.getEndTimeBoundary();

                //The annotations must be aligned with the annotations on the active tier
                if (((annET - annBT) == length) &&
                        ((nextET - nextBT) == length) && (annET == nextBT)) {
                    //Copy value to next annotation if next annotation has no value
                    if (nextAnn.getValue().trim().length() < 1) {
	                    	AnnotationValuesRecord record = new AnnotationValuesRecord(nextAnn);
	                    	record.setNewLabelValue(ann.getValue());
	                    	records.add(record);
                        nextAnn.setValue(ann.getValue());
                    }
                }
            }
        }
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }
}
