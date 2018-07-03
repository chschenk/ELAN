package mpi.eudico.client.annotator.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Converts segmentations produced by a recognizer to tiers and annotations. In
 * fact this command receives a map with tiername to annotation records, so it
 * could be used by other operations as well. As long as all annotations are
 * time-aligned and there is no assumption of tier dependencies.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class SegmentsToTiersCommand implements UndoableCommand {
    private List<ProgressListener> listeners;
    private String commandName;
    private TranscriptionImpl transcription;
    private Map<String, List<AnnotationDataRecord>> resolvedMap;
    private String lingTypeName = "";
    private boolean newLTCreated = false;

    /**
     * Creates a new SegmentsToTiersCommand instance
     *
     * @param commandName the name of the command
     */
    public SegmentsToTiersCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Re-creates the tiers and annotations using the same code.
     */
    @Override
	public void redo() {
        createTiers();
    }

    /**
     * Delete all created tiers and if needed the created linguistic type.
     */
    @Override
	public void undo() {
        TierImpl tier = null;

        for (String name : resolvedMap.keySet()) {

            tier = transcription.getTierWithId(name);

            if (tier != null) {
                transcription.removeTier(tier);
            }
        }

        if (newLTCreated) {
            LinguisticType lt = transcription.getLinguisticTypeByName(lingTypeName);

            if (lt != null) {
                transcription.removeLinguisticType(lt);
            }
        }
    }

    /**
	 * <b>Note: </b>it is assumed the types and order of the arguments are
	 * correct.
	 *
	 * @param receiver
	 *            the Transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = a map containing segmentation or tier name to
	 *            annotation records (in a List):
     *            Map&lt;String, List&lt;AnnotationDataRecord>></li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        Map<String, List<AnnotationDataRecord>> segMap = (Map<String, List<AnnotationDataRecord>>) arguments[0];

        // check tier names
        boolean changeNames = false;

        //TierImpl tier;

        for (String name : segMap.keySet()) {
            Tier tier = transcription.getTierWithId(name);

            if (tier != null) {
                changeNames = true;

                break;
            }
        }

        if (!changeNames) {
            resolvedMap = new HashMap<String, List<AnnotationDataRecord>>(segMap);

            //resolvedMap = segMap;//??
        } else {
            resolvedMap = new HashMap<String, List<AnnotationDataRecord>>(segMap.size());

            List<AnnotationDataRecord> segments;

            for (String name : segMap.keySet()) {
                segments = segMap.get(name);
                Tier tier = transcription.getTierWithId(name);

                if (tier != null) {
                    int count = 1;

                    while (count < 30) {
                        tier = transcription.getTierWithId(name +
                                "-" + count);

                        if (tier == null) {
                            resolvedMap.put(name + "-" + count, segments);

                            break;
                        }

                        count++;
                    }
                } else {
                    resolvedMap.put(name, segments);
                }
            }
        }

        LinguisticType lt = transcription.getLinguisticTypeByName(
                "default-lt");

        if ((lt != null) && (lt.getConstraints() == null)) {
            lingTypeName = "default-lt";
        } else {
            String ltName = "segmentation";
            lt = transcription.getLinguisticTypeByName(ltName);

            if ((lt != null) && (lt.getConstraints() == null)) {
                lingTypeName = ltName;
            } else if (lt == null) {
                lingTypeName = ltName;
                newLTCreated = true;
            } else {
                // create new
                int count = 1;

                while (count < 30) {
                    lt = transcription.getLinguisticTypeByName(ltName +
                            "-" + count);

                    if (lt == null) {
                        lingTypeName = ltName + "-" + count;
                        newLTCreated = true;

                        break;
                    }

                    count++;
                }
            }
        }

        ConvertThread ct = new ConvertThread();

        try {
            ct.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            transcription.setNotifying(true);
            progressInterrupt("An exception occurred: " + ex.getMessage());
        }

        //createTiers();
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
     * Creates a new LinguisticType if needed and then creates new tiers and
     * annotations.
     */
    private void createTiers() {
        LinguisticType lt = transcription.getLinguisticTypeByName(lingTypeName);

        if (newLTCreated || (lt == null)) { // double check?
            lt = new LinguisticType(lingTypeName);
            lt.setTimeAlignable(true);
            transcription.addLinguisticType(lt);
        }

        int curPropMode = transcription.getTimeChangePropagationMode();

        if (curPropMode != Transcription.NORMAL) {
            transcription.setTimeChangePropagationMode(Transcription.NORMAL);
        }

        int numTiers = resolvedMap.size();
        float progPerTier = (numTiers == 0) ? 100 : (100 / numTiers);

        int curProg = 0;
        int curTierIndex = 0;

        for (String name : resolvedMap.keySet()) {
            curTierIndex++;

            if (transcription.getTierWithId(name) != null) {
                curProg = (int) (curTierIndex * progPerTier);
                progressUpdate(curProg, "");

                continue; // don't try to add to the tier
            }

            TierImpl tier = new TierImpl(name, null, transcription, lt);
            transcription.addTier(tier);

            transcription.setNotifying(false);

            List<AnnotationDataRecord> segments = resolvedMap.get(name);

            if ((segments != null) && (segments.size() > 0)) {
                float perSeg = progPerTier / segments.size();

                for (int i = 0; i < segments.size(); i++) {
                    AnnotationDataRecord record = segments.get(i);

                    if (record != null) {
                        AlignableAnnotation aa;
                        aa = (AlignableAnnotation) tier.createAnnotation(record.getBeginTime(),
                                record.getEndTime());

                        if ((aa != null) && (record.getValue() != null)) {
                            aa.setValue(record.getValue());
                        }
                    }

                    //curProg += (int) perSeg;
                    progressUpdate((int) (curProg + (i * perSeg)), "");
                }
            }

            transcription.setNotifying(true);

            curProg = (int) (curTierIndex * progPerTier);
            progressUpdate(curProg, "");
        }

        transcription.setNotifying(true);

        // restore the time propagation mode
        transcription.setTimeChangePropagationMode(curPropMode);

        progressComplete("");
    }

    /**
     * Run on separate thread to enable progress monitoring.
     *
     * @author Han Sloetjes
     */
    private class ConvertThread extends Thread {
        /**
         * The actual action of this thread.
         */
        @Override
		public void run() {
            createTiers();
        }

        /**
         * Interrupts the current merging process.
         */
        @Override
		public void interrupt() {
            super.interrupt();
            progressInterrupt("Operation interrupted...");
        }
    }
}
