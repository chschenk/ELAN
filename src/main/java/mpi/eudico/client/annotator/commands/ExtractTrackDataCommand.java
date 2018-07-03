package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.timeseries.AbstractTSTrack;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A Command that extracts maximum, minimum or average from a timeseries track,
 * based on intervals/annotations on one tier and stores the information  in
 * annotations on a dependent tier.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ExtractTrackDataCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    private ArrayList<ProgressListener> listeners;
    private TranscriptionImpl transcription;
    private String sourceTierName;
    private String destTierName;
    private ArrayList<AnnotationDataRecord> newAnnos;
    private ArrayList<ValueRecord> changedAnnos;

    /**
     * Creates a new ExtractTrackDataCommand instance
     *
     * @param commandName the name
     */
    public ExtractTrackDataCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * The undo action; newly created annotations are deleted, changed
     * annotations are restored to their old state.
     */
    @Override
	public void undo() {
        if (transcription != null) {
            TierImpl tier = transcription.getTierWithId(destTierName);

            if (tier == null) {
                LOG.severe("Tier " + destTierName + " does no longer exist");

                return;
            }

            setWaitCursor(true);

            RefAnnotation ref;
            AnnotationDataRecord record;

            if (newAnnos != null) {
                for (int i = 0; i < newAnnos.size(); i++) {
                    record = newAnnos.get(i);
                    ref = (RefAnnotation) tier.getAnnotationAtTime(record.getBeginTime());

                    if (ref != null) {
                        tier.removeAnnotation(ref);
                    } else {
                        LOG.warning("Could not delete annotation: " +
                            record.getValue() + "bt: " + record.getBeginTime());
                    }
                }
            }

            ValueRecord valRec;

            if (changedAnnos != null) {
                for (int i = 0; i < changedAnnos.size(); i++) {
                    valRec = changedAnnos.get(i);
                    ref = (RefAnnotation) tier.getAnnotationAtTime(valRec.beginTime);

                    if (ref != null) {
                        ref.setValue(valRec.oldValue);
                    } else {
                        LOG.warning("Could not find annotation: " +
                            valRec.oldValue + "bt: " + valRec.beginTime);
                    }
                }
            }

            setWaitCursor(false);
        }
    }

    /**
     * The redo action.
     */
    @Override
	public void redo() {
        if (transcription != null) {
            TierImpl tier = transcription.getTierWithId(destTierName);

            if (tier == null) {
                LOG.severe("Tier " + destTierName + " does no longer exist");

                return;
            }

            setWaitCursor(true);

            RefAnnotation ref;
            AnnotationDataRecord record;
            long mid;

            if (newAnnos != null) {
                for (int i = 0; i < newAnnos.size(); i++) {
                    record = newAnnos.get(i);
                    ref = (RefAnnotation) tier.getAnnotationAtTime(record.getBeginTime());

                    if (ref == null) {
                        mid = (record.getBeginTime() +
                            record.getEndTime()) / 2;
                        ref = (RefAnnotation) tier.createAnnotation(mid, mid);

                        if (ref != null) {
                            ref.setValue(record.getValue());
                        } else {
                            LOG.warning(
                                "Could not create a reference annotation at time: " +
                                mid);
                        }
                    } else {
                        LOG.warning("Annotation was not deleted in undo: " +
                            record.getValue() + "bt: " + record.getBeginTime());
                    }
                }
            }

            ValueRecord valRec;

            if (changedAnnos != null) {
                for (int i = 0; i < changedAnnos.size(); i++) {
                    valRec = changedAnnos.get(i);
                    ref = (RefAnnotation) tier.getAnnotationAtTime(valRec.beginTime);

                    if (ref != null) {
                        ref.setValue(valRec.newValue);
                    } else {
                        LOG.warning("Could not find annotation: " +
                            valRec.newValue + "bt: " + valRec.beginTime);
                    }
                }
            }

            setWaitCursor(false);
        }
    }

    /**
     * Extracts values from a time series track based on intervals of
     * annotations on one tier and stores the values in annotations on a
     * dependent tier. <b>Note: </b>it is assumed the types and order of the
     * arguments are correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments: <ul><li>arg[0] the source tier
     *        (String)</li> <li>arg[1] the destination tier (String)</li>
     *        <li>arg[2] the track to extract the data from
     *        (AbstractTSTrack/Object)</li> <li>arg[3] what to extract
     *        maximum, minimum, average (String)</li> <li>arg[4] whether or
     *        not to overwrite existing annotation values (Boolean)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        sourceTierName = (String) arguments[0];
        destTierName = (String) arguments[1];

        AbstractTSTrack track = (AbstractTSTrack) arguments[2];
        String method = (String) arguments[3];
        boolean overwrite = ((Boolean) arguments[4]).booleanValue();

        TierImpl sourceTier = transcription.getTierWithId(sourceTierName);

        if (sourceTier == null) {
            progressInterrupt(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NotFound") + " " +
                sourceTierName);

            return;
        }

        TierImpl destTier = transcription.getTierWithId(destTierName);

        if (destTier == null) {
            progressInterrupt(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NotFound") + " " + destTierName);

            return;
        }

        // ?? test sourceTier.isAlignable, sourceTier is ancestorof destTier, 
        // destTier is symbolicassociation ??
        if (track == null) {
            progressInterrupt(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NotFound") + " " + "track");

            return;
        }

        if (method == null) {
            progressInterrupt(ElanLocale.getString(
                    "TimeSeriesViewer.Extract.NoMethod"));

            return;
        }

        newAnnos = new ArrayList<AnnotationDataRecord>();
        changedAnnos = new ArrayList<ValueRecord>();

        ExtractThread thread = new ExtractThread(sourceTier, destTier, track,
                method, overwrite);

        try {
            thread.start();
        } catch (Exception e) {
            LOG.warning("Error in extraction progress: " + e.getMessage());
            progressInterrupt("Extraction progress interrupted");
            transcription.setNotifying(true);
        }
    }

    /**
     * Returns the name of the command.
     *
     * @return the command name
     */
    @Override
	public String getName() {
        return commandName;
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
     * Class for minimal storage of a time value and an old and new textual
     * value.
     *
     * @author Han Sloetjes, MPI
     */
    private class ValueRecord {
        /** begin time */
        public final long beginTime;

        /** the old value */
        public final String oldValue;

        /** the new value */
        public final String newValue;

        /**
         * Creates a new ValueRecord instance
         *
         * @param beginTime begin time
         * @param oldValue the old value
         * @param newValue the new value
         */
        ValueRecord(long beginTime, String oldValue, String newValue) {
            this.beginTime = beginTime;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    /**
     * The actual extraction is executed in a separate thread; a listener  with
     * a user interface will be able to update itself (progress bar)
     *
     * @author Han Sloetjes, MPI
     */
    private class ExtractThread extends Thread {
        /** the source tier */
        private TierImpl sourceTier;

        /** the destination tier */
        private TierImpl destTier;

        /** the track */
        private AbstractTSTrack track;

        /** id for the calculation method; min, max or ave */
        private String method;

        /** the overwrite flag */
        private boolean overwrite = false;
        /** shorthands for min, max, average/ar. mean, sum, begin value, end value, median, range (max-min) */
        private final char[] modes = new char[] { '<', '>', '=', '+', 'b', 'e', 'm', 'r' };
        private char mode = modes[0];

        /**
         * Creates a new ExtractThread instance
         *
         * @param sourceTier the source tier
         * @param destTier the destination tier
         * @param track the time series track
         * @param method the calculation method
         * @param overwrite overwrite flag
         */
        ExtractThread(TierImpl sourceTier, TierImpl destTier,
            AbstractTSTrack track, String method, boolean overwrite) {
            super(ExtractThread.class.getName());
            this.sourceTier = sourceTier;
            this.destTier = destTier;
            this.track = track;
            this.method = method;

            String lMethod = method.toLowerCase();
            if ("max".equals(lMethod)) {
                mode = modes[1];
            } else if ("ave".equals(lMethod)) {
                mode = modes[2];
            } else if ("sum".equals(lMethod)) {
            	mode = modes[3];
            } else if ("atbegin".equals(lMethod)) {
            	mode = modes[4];
            } else if ("atend".equals(lMethod)) {
            	mode = modes[5];
            } else if ("median".equals(lMethod)) {
            	mode = modes[6];
            } else if ("range".equals(lMethod)) {
            	mode = modes[7];
            }

            this.overwrite = overwrite;
        }

        /**
         * The actual work
         *
         * @see Runnable#run()
         */
        @Override
		public void run() {
            transcription.setNotifying(false);

            List<AbstractAnnotation> annos = new ArrayList<AbstractAnnotation>(sourceTier.getAnnotations());
            float perAnn = 100f / Math.max(annos.size(), 1);

            DecimalFormat format = new DecimalFormat("#0.####",
                    new DecimalFormatSymbols(Locale.US));

            for (int i = 0; i < annos.size(); i++) {
            	AlignableAnnotation aa = (AlignableAnnotation) annos.get(i);
                long begin = aa.getBeginTimeBoundary();
                long end = aa.getEndTimeBoundary();
                float value = 0f;

                try {
                    switch (mode) {
                    case '<':
                        value = track.getMinimum(begin, end);

                        break;

                    case '>':
                        value = track.getMaximum(begin, end);

                        break;

                    case '=':
                        value = track.getAverage(begin, end);
                        
                        break;
                        
                    case '+':
                    	value = track.getSum(begin, end);

                    	break;

                    case 'b':
                    	value = track.getValueAtBegin(begin, end);

                    	break;

                    case 'e':
                    	value = track.getValueAtEnd(begin, end);

                    	break;

                    case 'm':
                    	value = track.getMedian(begin, end);

                    	break;

                    case 'r':
                    	value = track.getRange(begin, end);

                    	break;
                    } 
                } catch (Exception ex) {
                    transcription.setNotifying(true);
                    LOG.warning("Exception occured during calculation: " +
                        ex.getMessage());
                    progressInterrupt("Exception occured during calculation: " +
                        ex.getMessage());

                    return;
                }

                RefAnnotation ref = (RefAnnotation) destTier.getAnnotationAtTime(begin);

                String valString;

                if (ref == null) {
                    long mid = (begin + end) / 2;
                    ref = (RefAnnotation) destTier.createAnnotation(mid, mid);

                    if (ref != null) {
                        //ref.setValue(String.valueOf(value));
                    	if (value != Float.NaN) {
                    		ref.setValue(format.format(value));
                    	} // else leave empty
                        // store for undo
                        newAnnos.add(new AnnotationDataRecord(ref));
                    } else {
                        LOG.warning(
                            "Could not create a reference annotation at time: " +
                            mid);
                    }
                } else if (overwrite) {
                    // store for undo
                    // valString = String.valueOf(value);
                	if (value != Float.NaN) {
                		valString = format.format(value);
                	} else {
                		valString = "";
                	}

                    changedAnnos.add(new ValueRecord(begin, ref.getValue(),
                            valString));
                    ref.setValue(valString);
                }

                progressUpdate((int) ((i + 1) * perAnn), null);
            }

            transcription.setNotifying(true);
            progressComplete("Operation complete...");
        }

        /**
         * Interrupts the current merging process.
         */
        @Override
		public void interrupt() {
            progressInterrupt("Operation interrupted...");
            super.interrupt();
        }
    }
}
