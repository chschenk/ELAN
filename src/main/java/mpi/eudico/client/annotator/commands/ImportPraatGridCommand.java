package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.imports.praat.PraatTextGrid;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A Command that adds tiers and annotations that have been extracted from a
 * Praat TextGrid file.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class ImportPraatGridCommand implements UndoableCommand, ClientLogger {
    private String commandName;
    private List<ProgressListener> listeners;

    // receiver
    private TranscriptionImpl transcription;
    private PraatTextGrid ptg;
    private String typeName;
    private LinguisticType lt;
    private boolean skipEmptyIntervals;

    // for undo/redo
    private List<String> tierNames;
    private List<AnnotationDataRecord> annRecords;

    /**
     * Creates a new ImportPraatGridCommand instance
     *
     * @param name the name of the command
     */
    public ImportPraatGridCommand(String name) {
        commandName = name;
    }

    /**
     * If any tier has been added, it will be removed here.
     *
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        if ((transcription != null) && (tierNames != null) &&
                (tierNames.size() > 0)) {
            setWaitCursor(true);

            TierImpl t;

            for (int i = 0; i < tierNames.size(); i++) {
                t = (TierImpl) transcription.getTierWithId(tierNames.get(
                            i));
                transcription.removeTier(t);
            }

            setWaitCursor(false);
        }
    }

    /**
     * The tier(s) will be added here again, with the annotations.
     *
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if ((transcription != null) && (tierNames != null) &&
                (tierNames.size() > 0)) {
            setWaitCursor(true);

            if (typeName != null) {
                lt = transcription.getLinguisticTypeByName(typeName);

                if (lt != null) {
                    TierImpl t = null;

                    for (int i = 0; i < tierNames.size(); i++) {
                        String name = tierNames.get(i);
                        t = new TierImpl(name, "", transcription, lt);

                        if (transcription.getTierWithId(name) == null) {
                            transcription.addTier(t);
                        }
                    }

                    transcription.setNotifying(false);

                    int curPropMode = 0;

                    curPropMode = transcription.getTimeChangePropagationMode();

                    if (curPropMode != Transcription.NORMAL) {
                        transcription.setTimeChangePropagationMode(Transcription.NORMAL);
                    }

                    // add annotations from records
                    AnnotationDataRecord record;
                    Annotation ann;

                    for (int i = 0; i < annRecords.size(); i++) {
                        record = annRecords.get(i);

                        if ((t == null) ||
                                !t.getName().equals(record.getTierName())) {
                            t = (TierImpl) transcription.getTierWithId(record.getTierName());
                        }

                        if (t != null) {
                            ann = t.createAnnotation(record.getBeginTime(),
                                    record.getEndTime());

                            if ((ann != null) && (record.getValue() != null)) {
                                ann.setValue(record.getValue());
                            }
                        }
                    }

                    transcription.setTimeChangePropagationMode(curPropMode);

                    transcription.setNotifying(true);
                }
            }

            setWaitCursor(false);
        }
    }

    /**
     * Creates new tiers and annotations based on information in IntervalTiers
     * in the  TextGrid file. <b>Note: </b>it is assumed the types and order
     * of the arguments are correct.
     *
     * @param receiver the Transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the Praat TextGrid
     *        file (PraatTextGrid)</li> <li>arg[1] = the name of the
     *        Linguistic Type (String)</li> <li>arg[2] = a flag whether to ommit
     *        empty intervals (Boolean)</li></ul>
     *
     * @see mpi.eudico.client.annotator.commands.Command#execute(java.lang.Object,
     *      java.lang.Object[])
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        ptg = (PraatTextGrid) arguments[0];

        if (ptg == null) {
            progressInterrupt("No Praat TextGrid object specified.");
        }

        if (arguments.length > 1) {
        	if (arguments[2] instanceof Boolean) {
        		skipEmptyIntervals = ((Boolean) arguments[2]).booleanValue();
        	}
        }
        
        typeName = (String) arguments[1];

        // get the lin. type object
        if (typeName != null) {
            lt = transcription.getLinguisticTypeByName(typeName);

            if (lt != null) {
                // initialise some fields
                tierNames = new ArrayList<String>();
                annRecords = new ArrayList<AnnotationDataRecord>();

                PraatTGThread ptgThread = new PraatTGThread(ImportPraatGridCommand.class.getName());

                try {
                    ptgThread.start();
                } catch (Exception exc) {
                    transcription.setNotifying(true);
                    LOG.severe("Exception in calculation of overlaps: " +
                        exc.getMessage());
                    progressInterrupt("An exception occurred: " +
                        exc.getMessage());
                }
            } else {
                progressInterrupt("The Linguistic Type does not exist.");
            }
        } else {
            progressInterrupt("No Linguistic Type specified for new tiers.");
        }
    }

    /**
     * Returns the name of the command
     *
     * @return the name of the command
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
     * A thread that creates new tiers and annotations based on information
     * extracted from the  Praat TextGrid file.
     */
    class PraatTGThread extends Thread {
        /**
         * No-arg constructor.
         */
        PraatTGThread() {
            super();
        }

        /**
         * Creates a new thread with the specified name.
         *
         * @param name the name of the thread
         */
        PraatTGThread(String name) {
            super(name);
        }

        /**
         * Interrupts the current process.
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
            int tierCount = ptg.getTierNames().size();

            if (tierCount == 0) {
                progressInterrupt("No tiers detected in TextGrid file");

                return;
            }
            HashMap<String, String> nameMap = new HashMap<String, String>(tierCount);
            float perTier = 10f / tierCount;

            for (int i = 0; i < tierCount; i++) {
                String name = (String) ptg.getTierNames().get(i);
                // prevent using an existing tier
                if (transcription.getTierWithId(name) != null) {
                	int count = 40;
                	String nextNm;
                	for (int z = 1; z < count; z++) {
                		nextNm = name + "-" + z;
                		if (transcription.getTierWithId(nextNm) == null) {
                			nameMap.put(nextNm, name);
                			name = nextNm;
                			break;
                		}
                	}
                } else {
                	nameMap.put(name, name);
                }
                
                TierImpl t = new TierImpl(name, "", transcription, lt);

                if (transcription.getTierWithId(name) == null) {
                    transcription.addTier(t);
                    tierNames.add(name);
                    progressUpdate((int) ((i + 1) * perTier), "Added tier: " +
                            name);
                } else {               
	                progressUpdate((int) ((i + 1) * perTier), "Could not add tier: " +
	                    name);
                }
            }

            // disable notification after creation of the tiers
            transcription.setNotifying(false);

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            tierCount = tierNames.size();
            perTier = 90f / tierCount;

            for (int i = 0; i < tierCount; i++) {
                //String name = (String) ptg.getTierNames().get(i);
            	String name = tierNames.get(i);
                TierImpl t = (TierImpl) transcription.getTierWithId(name);
                String orgName = nameMap.get(name);
                List<AnnotationDataRecord> anns = ptg.getAnnotationRecords(orgName);

                if ((anns.size() == 0) || (t == null)) {
                    progressUpdate(10 + (int) ((i + 1) * perTier),
                        "Added annotations of tier: " + name);

                    continue;
                }

                float perAnn = perTier / anns.size();
                AnnotationDataRecord record = null;
                Annotation ann;
                progressUpdate(10 + (int) (i * perTier),
                    "Creating annotations...");

                for (int j = 0; j < anns.size(); j++) {
                    record = (AnnotationDataRecord) anns.get(j);
                    if (skipEmptyIntervals && (record.getValue() == null ||
                    		record.getValue().length() == 0)) {
                    	// progress update??
                    	continue;
                    }
                    ann = t.createAnnotation(record.getBeginTime(),
                            record.getEndTime());
                    if (ann != null) {
                    	ann.setValue(record.getValue());
                    	annRecords.add(new AnnotationDataRecord(ann));
                    }
                    progressUpdate((int) (10 + (i * perTier) +
                        ((j + 1) * perAnn)), null);
                }

                progressUpdate(10 + (int) ((i + 1) * perTier),
                    "Added annotations of tier: " + name);
            }

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);

            transcription.setNotifying(true);

            progressComplete("Operation complete...");
        }
    }
}
