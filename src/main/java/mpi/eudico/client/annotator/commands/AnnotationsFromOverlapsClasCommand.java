package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Constants;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import mpi.eudico.util.TimeFormatter;

import java.awt.Cursor;

import java.util.ArrayList;


/**
 * Undoable Command that calculates overlaps of annotations on 2 tiers and
 * creates a new annotation for each overlap found. Position and duration of
 * the new annotation corresponds to begin time and length of the overlap.
 * Optionally the annotation gets the duration of the overlap as value.
 *
 * @author Han Sloetjes
 * @version 1.0 Jan 2007, 2.0 Oct 2008
 */
public class AnnotationsFromOverlapsClasCommand implements UndoableCommand,
    ClientLogger {
    private ArrayList<ProgressListener> listeners;
    private String commandName;
    private TranscriptionImpl transcription;
    private TierImpl sourceTier1;
    private TierImpl sourceTier2;
    private TierImpl destTier;
    private boolean addContent = false;
    private int timeFormat = 0;
    private boolean matchingValuesOnly = false;
    private boolean specificValueOnly = false;
    private String specificValue;
    private ArrayList<long[]> overlaps;
    private ArrayList<String> overlapValues;
    private ArrayList<AnnotationDataRecord> annRecords;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public AnnotationsFromOverlapsClasCommand(String name) {
        commandName = name;
    }

    /**
     * Calculates the overlaps of the annotations on 2 source tiers and creates
     * new annotation on the destination tier of the duration of the overlaps
     * and optionally adds the duration as the annotations' values.   <br>
     * Oct 2008: a flag is added that determines to only create annotation for
     * overlapping annotations with the same value.
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments: <ul><li>arg[0] = the first source tier
     *        (String)</li> <li>arg[1] the second source tier (String)</li>
     *        <li>arg[2] the destination tier name (new tier) (String)</li>
     *        <li>arg[3] the name of the LinguisticType for the new tier
     *        (String)</li> <li>arg[4] whether or not to make the duration of
     *        the overlap the value of  each annotation (Boolean)</li>
     *        <li>arg[5] the format of the time value  (in case arg[4] is
     *        true), a constant for ms, ssms or hhmmssms (Integer)</li>
     *        <li>arg[6] a flag that determines that a new annotation only has to be created if the
     *        two compared annotations have the same value (Boolean)</li>
     *        <li>arg[7] a flag that determines that only one specific annotation value is considered (Boolean)</li>
     *        <li>arg[8] the specific value to consider (if 6 and 7 are true) (String) </li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        String sourceName1 = (String) arguments[0];
        String sourceName2 = (String) arguments[1];
        String destName = (String) arguments[2];
        String typeName = (String) arguments[3];
        Boolean ac = (Boolean) arguments[4];

        if (ac != null) {
            addContent = ac.booleanValue();
        }

        Integer tf = (Integer) arguments[5];

        if (tf != null) {
            timeFormat = tf.intValue();
        }
        if (arguments.length > 5 && arguments[6] instanceof Boolean) {
        	matchingValuesOnly = ((Boolean) arguments[6]).booleanValue();
        	if (matchingValuesOnly && arguments.length > 7) {
        		specificValueOnly = ((Boolean) arguments[7]).booleanValue();
        		specificValue = (String) arguments[8];
        		if (specificValue == null) {
        			specificValue = "";
        		}
        	}
        }

        sourceTier1 = (TierImpl) transcription.getTierWithId(sourceName1);
        sourceTier2 = (TierImpl) transcription.getTierWithId(sourceName2);

        if ((sourceTier1 == null) || (sourceTier2 == null)) {
            progressInterrupt("One of the sourcetiers could not be found");

            return;
        }

        if (destName == null) {
            destName = "Overlap";
            LOG.warning("Name of destination tier is null, changed to Overlap");
        }

        destTier = (TierImpl) transcription.getTierWithId(destName);

        if (destTier != null) {
            // it already exists
            int count = 1;
            String cName = destName + "-";

            while (destTier != null) {
                cName = cName + count;
                destTier = (TierImpl) transcription.getTierWithId(cName);
                count++;
            }

            LOG.warning("Tier " + destName +
                " already exists, changed name to " + cName);
            destName = cName;
        }

        LinguisticType type = transcription.getLinguisticTypeByName(typeName);

        if (type == null) {
            // get the first suitable type
            LinguisticType countType;

            for (int i = 0; i < transcription.getLinguisticTypes().size();
                    i++) {
                countType = transcription.getLinguisticTypes().get(i);

                if (countType.getConstraints() == null) {
                    LOG.warning("LinguisticType " + typeName +
                        " could not be found, using " +
                        countType.getLinguisticTypeName() + " instead.");
                    type = countType;
                    typeName = type.getLinguisticTypeName();

                    break;
                }
            }
        }

        destTier = new TierImpl(null, destName, null, transcription, type);
        transcription.addTier(destTier);
        overlaps = new ArrayList<long[]>();
        overlapValues = new ArrayList<String>();

        progressUpdate(8, "Created tier: " + destName);

        Thread calcThread = new CalcOverLapsThread(AnnotationsFromOverlapsClasCommand.class.getName());

        try {
            calcThread.start();
        } catch (Exception exc) {
            transcription.setNotifying(true);
            LOG.severe("Exception in calculation of overlaps: " +
                exc.getMessage());
            progressInterrupt("An exception occurred: " + exc.getMessage());
        }
    }

    /**
     * Removes the tier.
     *
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        if ((transcription != null) && (destTier != null)) {
            setWaitCursor(true);

            transcription.removeTier(destTier);

            setWaitCursor(false);
        }
    }

    /**
     * Adds the tier again and the annotations.
     *
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if ((transcription != null) && (destTier != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);

            if (transcription.getTierWithId(destTier.getName()) == null) {
                transcription.addTier(destTier);
            }

            if ((annRecords != null) && (annRecords.size() > 0)) {
                transcription.setNotifying(false);

                AnnotationDataRecord record;
                Annotation ann;

                for (int i = 0; i < annRecords.size(); i++) {
                    record = annRecords.get(i);
                    ann = destTier.createAnnotation(record.getBeginTime(),
                            record.getEndTime());

                    if ((ann != null) && (record.getValue() != null)) {
                        ann.setValue(record.getValue());
                    }
                }

                transcription.setNotifying(true);
            }

            setWaitCursor(false);
            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }
    }

    /**
     * @see mpi.eudico.client.annotator.commands.Command#getName()
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

    /**
     * Adds a ProgressListener to the list of ProgressListeners.
     *
     * @param pl the new ProgressListener
     */
    public synchronized void addProgressListener(ProgressListener pl) {
        if (listeners == null) {
            listeners = new ArrayList<ProgressListener>(2);
        }

        listeners.add(pl);
    }

    /**
     * Removes the specified ProgressListener from the list of listeners.
     *
     * @param pl the ProgressListener to remove
     */
    public synchronized void removeProgressListener(ProgressListener pl) {
        if ((pl != null) && (listeners != null)) {
            listeners.remove(pl);
        }
    }

    /**
     * Notifies any listeners of a progress update.
     *
     * @param percent the new progress percentage, [0 - 100]
     * @param message a descriptive message
     */
    private void progressUpdate(int percent, String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressUpdated(this,
                    percent, message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has completed.
     *
     * @param message a descriptive message
     */
    private void progressComplete(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressCompleted(this,
                    message);
            }
        }
    }

    /**
     * Notifies any listeners that the process has been interrupted.
     *
     * @param message a descriptive message
     */
    private void progressInterrupt(String message) {
        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressInterrupted(this,
                    message);
            }
        }
    }

    /**
     * A thread class that compares the annotations of two tiers, detects the
     * ovelaps and  creates a new annotation on a third tier, with the
     * duration of the overlap.
     */
    private class CalcOverLapsThread extends Thread {
        /**
         * Creates a new thread to calculate the overlaps.
         */
        public CalcOverLapsThread() {
            super();
        }

        /**
         * Creates a new thread to calculate the overlaps.
         *
         * @param name the name of the thread
         */
        public CalcOverLapsThread(String name) {
            super(name);
        }

        /**
         * Interrupts the current calculation process.
         */
        @Override
		public void interrupt() {
            super.interrupt();
            progressInterrupt("Operation interrupted...");
        }

        /**
         * The actual action of this thread.
         *
         * @see java.lang.Runnable#run()
         */
        @Override
		public void run() {
            transcription.setNotifying(false);

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            // take the number of annotations on the first source tier as the indicator for
            // progress updates
            int annCount = sourceTier1.getNumberOfAnnotations();
            float perAnn = 60f;
            if (annCount > 0) {
            	perAnn = 60 / (float) annCount;
            }

            // two counters or two loops: one for both source tiers
            int j = 0;
            ArrayList<AbstractAnnotation> sourceAnns1 = new ArrayList<AbstractAnnotation>(sourceTier1.getAnnotations());
            int numAnns1 = sourceAnns1.size();
            ArrayList<AbstractAnnotation> sourceAnns2 = new ArrayList<AbstractAnnotation>(sourceTier2.getAnnotations());
            int numAnns2 = sourceAnns2.size();
            AbstractAnnotation ann1 = null;
            AbstractAnnotation ann2 = null;
            long bt1;
            long bt2;
            long et1;
            long et2;
            long obt;
            long oet;
            progressUpdate(10, "Calculating overlaps...");

            // loop over the annotations of the first tier
            for (int i = 0; i < numAnns1; i++) {
                ann1 = sourceAnns1.get(i);
                bt1 = ann1.getBeginTimeBoundary();
                et1 = ann1.getEndTimeBoundary();

                // find overlapping annotations on second tier
                for (; j < numAnns2; j++) {
                    ann2 = sourceAnns2.get(j);
                    bt2 = ann2.getBeginTimeBoundary();
                    et2 = ann2.getEndTimeBoundary();

                    if (et2 <= bt1) {
                        continue;
                    } else if (bt2 >= et1) {
                        if (j > 0) {
                            j--;
                        }

                        break;
                    } else {
                        if (bt1 <= bt2) {
                            obt = bt2;
                        } else {
                            obt = bt1;
                        }

                        if (et1 <= et2) {
                            oet = et1;
                        } else {
                            oet = et2;
                        }

                        if (matchingValuesOnly) {
                        	if (!specificValueOnly) {
                        		if ((ann1.getValue() == null && ann2.getValue() == null) ||
                        				((ann1.getValue() != null && ann2.getValue() != null) &&
                                				(ann1.getValue().length() == 0 && ann2.getValue().length() == 0) ||
                                				(ann1.getValue().equals(ann2.getValue()) ))) {
                        			overlaps.add(new long[] { obt, oet });
                        			if (!addContent) {
                        				overlapValues.add(ann1.getValue());
                        			}
                        		}
                        	} else {// compare one value only
                        		if (ann1.getValue() != null && ann2.getValue() != null &&
                        				ann1.getValue().equals(specificValue) && ann2.getValue().equals(specificValue)) {
                        			overlaps.add(new long[] { obt, oet });
                        			if (!addContent) {
                        				overlapValues.add(specificValue);
                        			}
                        		}
                        	}

                        } else {
                        	overlaps.add(new long[] { obt, oet });
                			if (!addContent) {
                				overlapValues.add(ann1.getValue() + " " + ann2.getValue());
                			}
                        }
                    }
                }

                progressUpdate((int) (10 + (i * perAnn)), null);
            }

            perAnn = 25f;
            if (overlaps.size() > 0) {
            	perAnn = 25 / (float) overlaps.size();
            }
            annRecords = new ArrayList<AnnotationDataRecord>(overlaps.size());

            long[] ol;
            Annotation ann;
            progressUpdate(70, "Creating annotations...");

            for (int i = 0; i < overlaps.size(); i++) {
                ol = overlaps.get(i);
                ann = destTier.createAnnotation(ol[0], ol[1]);

                if ((ann != null) && addContent) {
                    switch (timeFormat) {
                    case Constants.MS:
                        ann.setValue(String.valueOf(ol[1] - ol[0]));

                        break;

                    case Constants.SSMS:
                        ann.setValue(TimeFormatter.toSSMSString(ol[1] - ol[0]));

                        break;

                    case Constants.HHMMSSMS:
                        ann.setValue(TimeFormatter.toString(ol[1] - ol[0]));

                    default:
                    }
                } else if((ann != null) && !addContent){
                	if (i < overlapValues.size()) {
                		ann.setValue(overlapValues.get(i));
                	}
                	/*
                	Annotation ann1A = sourceTier1.getAnnotationAtTime(ann.getBeginTimeBoundary());
                	Annotation ann1B = sourceTier1.getAnnotationAtTime(ann.getEndTimeBoundary());

                	Annotation ann2A = sourceTier2.getAnnotationAtTime(ann.getBeginTimeBoundary());
                	Annotation ann2B = sourceTier2.getAnnotationAtTime(ann.getEndTimeBoundary());
                	String value = null;

                	Annotation annOne = null;

                	if(ann1A != null && ann1B != null){
                		if (ann1A.getValue().equals(ann1B.getValue())){
                			annOne = ann1A;
                		}else{

                		}
                	}else{
                		if(ann1A != null){
                			annOne = ann1A;
                		}else if (ann2A != null){
                			annOne = ann2A;
                		}
                	}

                	Annotation annTwo = null;

                	if(ann2A != null && ann2B != null){
                		if (ann2A.getValue().equals(ann2B.getValue())){
                			annTwo = ann2A;
                		}else{

                		}
                	}else{
                		if(ann2A != null){
                			annTwo = ann2A;
                		}else if (ann2A != null){
                			annTwo = ann2A;
                		}
                	}

                	if(annOne!= null && annTwo!= null){
                		value = annOne.getValue() + " " + annTwo.getValue();
//                		if(annOne.getBeginTimeBoundary() < annTwo.getBeginTimeBoundary()){
//                			value = annOne.getValue() + annTwo.getValue();
//                		}else {
//                			value = annTwo.getValue() + annOne.getValue();
//                		}
                	}

                	if(value != null){
                 	   ann.setValue(value);
                 	}
                 	*/
                }

                if (ann != null) {
                    annRecords.add(new AnnotationDataRecord(ann));
                }

                progressUpdate((int) (70 + (i * perAnn)), null);
            }

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);

            transcription.setNotifying(true);

            progressComplete("Operation complete...");
        }
    }
}
