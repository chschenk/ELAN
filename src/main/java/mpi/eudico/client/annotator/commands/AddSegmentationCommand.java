package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeIntervalComparator;
import mpi.eudico.util.TimeInterval;


/**
 * A Command that adds a group of segments/annotations to a tier in one,
 * undoable, action.
 *
 * @author Han Sloetjes
 */
public class AddSegmentationCommand implements UndoableCommand {
    private String commandName;

    // receiver
    private TranscriptionImpl transcription;
    private String tierName;
    private TierImpl tier;
    private ArrayList<TimeInterval> segments;

    // undo/redo

    /** Holds value of property DOCUMENT ME! */
    ArrayList<DefaultMutableTreeNode> changedAnnotations;

    /** Holds value of property DOCUMENT ME! */
    ArrayList<DefaultMutableTreeNode> removedAnnotations;

    /**
     * Creates a new AddSegmentationCommand instance
     *
     * @param name the name of the command
     */
    public AddSegmentationCommand(String name) {
        commandName = name;
    }

    /**
     * The undo action. Deletes the added annotations from the Tier.
     */
    @Override
	public void undo() {
        if (transcription != null) {
            restoreAnnotations();
        }
    }

    /**
     * The redo action. Adds the segments/annotations to the Tier.
     */
    @Override
	public void redo() {
        if (transcription != null) {
            createNewAnnotations();
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the name of the Tier,
     *        which should be a root tier  (TierImpl)</li> <li>arg[1] = a list
     *        of time intervals (ArrayList containing  TimeInterval
     *        objects)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        tierName = (String) arguments[0];
        segments = (ArrayList<TimeInterval>) arguments[1];

        if (transcription != null) {
            tier = transcription.getTierWithId(tierName);
        }

        if (tier != null) {
            setWaitCursor(true);

            preProcessIntervals();
            changedAnnotations = new ArrayList<DefaultMutableTreeNode>();
            removedAnnotations = new ArrayList<DefaultMutableTreeNode>();

            storeAnnotations();

            createNewAnnotations();

            setWaitCursor(false);
        }
    }

    /**
     * Sort the intervals and correct overlapping intervals.<br>
     * This is done by changing the end time of one annotation to  the begin
     * time of the next annotation, in case the old end time  is greater than
     * the next begin time.
     */
    private void preProcessIntervals() {
        if (segments != null) {
            Collections.sort(segments, new TimeIntervalComparator());

            TimeInterval t1 = null;
            TimeInterval t2 = null;

            for (int i = segments.size() - 1; i > 0; i--) {
                t1 = segments.get(i - 1);
                t2 = segments.get(i);

                if (t2.getBeginTime() < t1.getEndTime()) {
                	if (t1 instanceof AnnotationDataRecord) {
                		((AnnotationDataRecord) t1).setEndTime(t2.getBeginTime());
                		
                		if (((AnnotationDataRecord) t1).getDuration() <= 0) {
                			segments.remove(t1);
                		}
                	} else {
	                    TimeInterval ti = new TimeInterval(t1.getBeginTime(),
	                            t2.getBeginTime());
	                    segments.remove(t1);
	
	                    if (ti.getDuration() > 0) {
	                        segments.add(i - 1, ti);
	                    }
                	}
                }
            }
        }
    }

    /**
     * Makes a backup of the effected annotations of this tier, including child
     * annotations (for undo).
     */
    private void storeAnnotations() {
        if ((segments != null) && (segments.size() > 0)) {
            ArrayList<AbstractAnnotation> changedTemp = new ArrayList<AbstractAnnotation>();
            TimeInterval curInterval = null;
            AbstractAnnotation aa = null;

            for (int i = 0; i < segments.size(); i++) {
                curInterval = segments.get(i);

                List<Annotation> effectedAnn = tier.getOverlappingAnnotations(curInterval.getBeginTime(),
                        curInterval.getEndTime());

                for (int j = 0; j < effectedAnn.size(); j++) {
                    aa = (AbstractAnnotation) effectedAnn.get(j);

                    if (aa.getBeginTimeBoundary() < curInterval.getBeginTime()) {
                        if (!changedTemp.contains(aa)) {
                            changedTemp.add(aa);
                            changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    aa));
                        }
                    } else if (aa.getEndTimeBoundary() > curInterval.getEndTime()) {
                        if (!changedTemp.contains(aa)) {
                            changedTemp.add(aa);
                            changedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                    aa));
                        }
                    } else {
                        removedAnnotations.add(AnnotationRecreator.createTreeForAnnotation(
                                aa));
                    }
                }
            }
        }
    }

    /**
     * Creates annotations of the provided time interval objects.
     */
    private void createNewAnnotations() {
        if ((segments == null) || (tier == null)) {
            return;
        }

        if (!tier.isTimeAlignable()) {
            return;
        }

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        transcription.setNotifying(false);
        setWaitCursor(true);

        TimeInterval ti = null;
        AnnotationDataRecord adr = null;
        AbstractAnnotation aa;
        Object segment;

        for (int i = 0; i < segments.size(); i++) {
        	segment = segments.get(i);
        	if (segment instanceof AnnotationDataRecord) {
        		adr = (AnnotationDataRecord) segment;
        		
        		aa = (AbstractAnnotation) tier.createAnnotation(adr.getBeginTime(), adr.getEndTime());
        		if (aa != null) {
        			AnnotationRecreator.restoreValueEtc(aa, adr, false);
        		}
        	} else if (segment instanceof TimeInterval) {
        		ti = (TimeInterval) segment;

        		tier.createAnnotation(ti.getBeginTime(), ti.getEndTime());
        	}
        }

        transcription.setNotifying(true);
        setWaitCursor(false);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);
    }

    private void restoreAnnotations() {
        if ((tier == null) || (segments == null) || (segments.size() == 0)) {
            return;
        }

        setWaitCursor(true);

        transcription.setNotifying(false);

        int curPropMode = 0;

        curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        TimeInterval ti = null;
        AnnotationDataRecord adr = null;
        Object segment;
        AbstractAnnotation aa = null;
        DefaultMutableTreeNode node = null;
        AnnotationDataRecord annRecord = null;

        // first remove the created annotations
        for (int i = 0; i < segments.size(); i++) {
            ti = segments.get(i);
            aa = (AbstractAnnotation) tier.getAnnotationAtTime(ti.getBeginTime());

            if (aa != null) {
                tier.removeAnnotation(aa);
            } else {
                // log..
                System.out.println(
                    "Could not delete a previously created annotation");
            }
        }

        // then remove the changed annotations
        for (int i = 0; i < changedAnnotations.size(); i++) {
            node = changedAnnotations.get(i);
            annRecord = (AnnotationDataRecord) node.getUserObject();

            // because we don't know whether the annotation's begin time, 
            // end time or both have been changed, just find any annotation 
            // within the time interval
            // the annotation might have been removed by two successive new annotations
            List<Annotation> v = tier.getOverlappingAnnotations(annRecord.getBeginTime(),
                    annRecord.getEndTime());

            if (v.size() > 1) {
                // log...
                System.out.println("Found more than one annotation in interval");
            }

            for (int j = 0; j < v.size(); j++) {
                aa = (AbstractAnnotation) v.get(j);
                tier.removeAnnotation(aa);
            }
        }

        // the tier should be cleared from all new and all changed annotations
        // recreate the changed and the removed annotations
        if (changedAnnotations.size() > 0) {
            for (int i = 0; i < changedAnnotations.size(); i++) {
                node = changedAnnotations.get(i);
                AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
            }
        }

        if (removedAnnotations.size() > 0) {
            for (int i = 0; i < removedAnnotations.size(); i++) {
                node = removedAnnotations.get(i);
                AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
            }
        }

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);

        transcription.setNotifying(true);
        setWaitCursor(false);
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

    private void printSegments() {
        if (segments == null) {
            return;
        }

        if (segments.size() > 0) {
            for (int i = 0; i < segments.size(); i++) {
                TimeInterval ti = segments.get(i);
                System.out.println("Segment: " + ti.getBeginTime() + " - " +
                    ti.getEndTime());
            }
        }
    }
}
