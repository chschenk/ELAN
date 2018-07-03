package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.util.AnnotationDataRecordComparator;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationValuesRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.TimeFormatter;


/**
 * Undoable Command that merges the annotations on 2 tiers and creates new
 * annotations on a third tier. Overlapping annotations are merged into one
 * new annotation. Non overlapping annotations are copied as is. The content
 * depends  on the user's choice: either concatenated annotation values or the
 * duration of  the new annotation.
 *
 * @author Han Sloetjes
 * @version 1.0 July 2008
 */
public class MergeTiersClasCommand implements UndoableCommand, ClientLogger {
    private List<ProgressListener> listeners;
    private String commandName;
    private TranscriptionImpl transcription;
    private TierImpl sourceTier1;
    private TierImpl sourceTier2;
    private TierImpl destTier;
    private boolean concatenate = true;
    private int timeFormat = 0;    
    private boolean matchingValuesOnly = false;
    private boolean specificValueOnly = false;
    private String specificValue;
    private List<AnnotationValuesRecord> overlaps;
    private List<AnnotationDataRecord> annRecords;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public MergeTiersClasCommand(String name) {
        commandName = name;
    }

	/**
	 * Oct 2008: a flag is added that determines to only create an annotation
	 * for overlapping annotations with the same value. <b>Note: </b>it is
	 * assumed the types and order of the arguments are correct.
	 *
	 * @param receiver
	 *            the transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the first source tier (String)</li>
	 *            <li>arg[1] the second source tier (String)</li>
	 *            <li>arg[2] the destination tier name (new tier) (String)</li>
	 *            <li>arg[3] the name of the LinguisticType for the new tier
	 *            (String)</li>
	 *            <li>arg[4] whether to use and concatenate the values of
	 *            annotations or to use the duration as values (Boolean)</li>
	 *            <li>arg[5] the format of the time value (in case arg[4] is
	 *            false), a constant for ms, ssms or hhmmssms (Integer)</li>
	 *            <li>arg[6] a flag that determines that a new annotation only
	 *            has to be created if the two compared annotations have the
	 *            same value (Boolean)</li>
	 *            <li>arg[7] a flag that determines that only one specific
	 *            annotation value is considered (Boolean)</li>
	 *            <li>arg[8] the specific value to consider (if 6 and 7 are
	 *            true) (String)</li>
	 *            </ul>
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
            concatenate = ac.booleanValue();
        }

        if (arguments.length > 5) {
            Integer tf = (Integer) arguments[5];

            if (tf != null) {
                timeFormat = tf.intValue();
            }
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
        
        sourceTier1 = transcription.getTierWithId(sourceName1);
        sourceTier2 = transcription.getTierWithId(sourceName2);

        if ((sourceTier1 == null) || (sourceTier2 == null)) {
            progressInterrupt("One of the sourcetiers could not be found");

            return;
        }

        if (destName == null) {
            destName = "Overlap";
            LOG.warning("Name of destination tier is null, changed to Overlap");
        }

        destTier = transcription.getTierWithId(destName);

        if (destTier != null) {
            // it already exists
            int count = 1;
            String cName = destName + "-";

            while (destTier != null) {
                cName = cName + count;
                destTier = transcription.getTierWithId(cName);
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
                countType = transcription.getLinguisticTypes()
                                                          .get(i);

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
        overlaps = new ArrayList<AnnotationValuesRecord>();

        progressUpdate(8, "Created tier: " + destName);

        Thread calcThread = new MergeThread(MergeTiersClasCommand.class.getName());

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
     * ovelaps and  creates new annotations on a third tier.
     */
    private class MergeThread extends Thread {
        /**
         * Creates a new thread to merge tiers.
         */
        public MergeThread() {
            super();
        }

        /**
         * Creates a new thread to merge tiers.
         *
         * @param name the name of the thread
         */
        public MergeThread(String name) {
            super(name);
        }

        /**
         * Interrupts the current merging process.
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

            float perAnn = 50f;

            // two counters or two loops: one for both source tiers
            ArrayList sourceAnns1 = new ArrayList(sourceTier1.getAnnotations());
            int numAnns1 = sourceAnns1.size();
            ArrayList sourceAnns2 = new ArrayList(sourceTier2.getAnnotations());
            int numAnns2 = sourceAnns2.size();
            AbstractAnnotation ann1 = null;
            AnnotationValuesRecord curRecord = null;

            progressUpdate(10, "Merging annotations...");

            if (matchingValuesOnly) {
            	// only process annotations with the same value
                AbstractAnnotation ann2 = null;
                long bt1;
                long bt2;
                long et1;
                long et2;
                long obt;
                long oet;
                int j = 0;
                
                if (numAnns1 > 0) {
                	perAnn = perAnn / numAnns1;
                }
                
                // loop over the annotations of the first tier
                for (int i = 0; i < numAnns1; i++) {
                    ann1 = (AbstractAnnotation) sourceAnns1.get(i);
                    bt1 = ann1.getBeginTimeBoundary();
                    et1 = ann1.getEndTimeBoundary();

                    // find overlapping annotations on second tier
                    for (; j < numAnns2; j++) {
                        ann2 = (AbstractAnnotation) sourceAnns2.get(j);
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
                                obt = bt1;
                            } else {
                                obt = bt2;
                            }

                            if (et1 <= et2) {
                                oet = et2;
                            } else {
                                oet = et1;
                            }
                            
                            if (!specificValueOnly) {
	                            if ( (ann1.getValue() == null && ann2.getValue() == null) || 
	                            				((ann1.getValue() != null && ann2.getValue() != null) &&
	                            				(ann1.getValue().length() == 0 && ann2.getValue().length() == 0) || 
	                            				(ann1.getValue().equals(ann2.getValue()) )) ) {
	                            	overlaps.add(new AnnotationValuesRecord(sourceTier1.getName(), ann1.getValue(), obt, oet));
	                            }
                            } else {
                        		if (ann1.getValue() != null && ann2.getValue() != null && 
                        				ann1.getValue().equals(specificValue) && ann2.getValue().equals(specificValue)) {
                        			overlaps.add(new AnnotationValuesRecord(sourceTier1.getName(), specificValue, obt, oet));
                        		}
                            }
                        }
                    }

                    progressUpdate((int) (10 + (i * perAnn)), null);
                }
	            Collections.sort(overlaps, new AnnotationDataRecordComparator());
	        	
	            perAnn = 18;
                
	            if (overlaps.size() > 0) {
	                perAnn = 18 / (float) overlaps.size();
	            }
	
	            // merge records
	            AnnotationValuesRecord prevRecord = null;
	
	            for (int i = 0; i < overlaps.size(); i++) {
	                curRecord = overlaps.get(i);
	
	                if (prevRecord == null) {
	                    prevRecord = curRecord;
	
	                    continue;
	                }
	
	                if (curRecord.getBeginTime() < prevRecord.getEndTime()) {
	                    //as a result of the sorting cur bt >= prev bt
	                	if (prevRecord.getValue() != null && prevRecord.getValue().equals(prevRecord.getValue())) {
	                        prevRecord.setEndTime(Math.max(prevRecord.getEndTime(),
	                            curRecord.getEndTime()));
	
	                        overlaps.remove(i);
	                        i--; // should be save
	                	} else {
	                		prevRecord.setEndTime(curRecord.getBeginTime());
	                		if (prevRecord.getBeginTime() >= prevRecord.getEndTime()) {
	                			overlaps.remove(prevRecord);
	                			i--;
	                		}
	                		prevRecord = curRecord;
	                	}
	                } else {
	                    prevRecord = curRecord;
	                }
	
	                progressUpdate((int) (60 + (i * perAnn)), null);
	            }
            } else {
                // take the number of annotations on both source tiers as the indicator for 
                // progress updates
                int annCount = 2 * (sourceTier1.getNumberOfAnnotations() +
                    sourceTier2.getNumberOfAnnotations());
                if (annCount > 0) {
                    perAnn = 50 / (float) annCount;
                }
	            // loop over the annotations of the first tier, process all overlapping annos
	            int count = 0;
	
	            for (int i = 0; i < numAnns1; i++) {
	                ann1 = (AbstractAnnotation) sourceAnns1.get(i);
	                curRecord = new AnnotationValuesRecord(ann1);
	                overlaps.add(curRecord);
	                progressUpdate((int) (10 + (count++ * perAnn)), null);
	            }
	
	            for (int i = 0; i < numAnns2; i++) {
	                ann1 = (AbstractAnnotation) sourceAnns2.get(i);
	                curRecord = new AnnotationValuesRecord(ann1);
	                overlaps.add(curRecord);
	                progressUpdate((int) (10 + (count++ * perAnn)), null);
	            }
	
	            Collections.sort(overlaps, new AnnotationDataRecordComparator());
	
	            perAnn = 18;
	
	            if (overlaps.size() > 0) {
	                perAnn = 18 / (float) overlaps.size();
	            }
	
	            // merge records
	            AnnotationValuesRecord prevRecord = null;
	
	            for (int i = 0; i < overlaps.size(); i++) {
	                curRecord = overlaps.get(i);
	
	                if (prevRecord == null) {
	                    prevRecord = curRecord;
	
	                    continue;
	                }
	
	                if (curRecord.getBeginTime() < prevRecord.getEndTime()) {
	                    //as a result of the sorting cur bt >= prev bt
	                    prevRecord.setEndTime(Math.max(prevRecord.getEndTime(),
	                            curRecord.getEndTime()));
	
	                    if (prevRecord.getNewLabelValue() == null) {
	                        prevRecord.setNewLabelValue(prevRecord.getValue() +
	                            " " + curRecord.getValue());
	                    } else {
	                        prevRecord.setNewLabelValue(prevRecord.getNewLabelValue() +
	                            " " + curRecord.getValue());
	                    }
	
	                    overlaps.remove(i);
	                    i--; // should be save
	                } else {
	                    prevRecord = curRecord;
	                }
	
	                progressUpdate((int) (60 + (i * perAnn)), null);
	            }
            }
            progressUpdate(78, "Creating annotations...");
            annRecords = new ArrayList<AnnotationDataRecord>(overlaps.size());

            Annotation ann;

            for (int i = 0; i < overlaps.size(); i++) {
                curRecord = overlaps.get(i);
                ann = destTier.createAnnotation(curRecord.getBeginTime(),
                        curRecord.getEndTime());

                if (ann != null) {
                    if (concatenate) {
                        ann.setValue((curRecord.getNewLabelValue() != null)
                            ? curRecord.getNewLabelValue() : curRecord.getValue());
                    } else {
                        switch (timeFormat) {
                        case Constants.MS:
                            ann.setValue(String.valueOf(curRecord.getEndTime() -
                                    curRecord.getBeginTime()));

                            break;

                        case Constants.SSMS:
                            ann.setValue(TimeFormatter.toSSMSString(curRecord.getEndTime() -
                                    curRecord.getBeginTime()));

                            break;

                        case Constants.HHMMSSMS:
                            ann.setValue(TimeFormatter.toString(curRecord.getEndTime() -
                                    curRecord.getBeginTime()));

                        default:
                        }
                    }

                    annRecords.add(new AnnotationDataRecord(ann));
                }

                progressUpdate((int) (78 + (i * perAnn)), null);
            }

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);

            transcription.setNotifying(true);

            progressComplete("Operation complete...");
        }
    }
}
