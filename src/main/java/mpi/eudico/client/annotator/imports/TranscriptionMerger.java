package mpi.eudico.client.annotator.imports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.prefs.PreferencesWriter;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.util.ControlledVocabulary;


/**
 * A class that merges two source transcriptions to a new destination
 * transcription, in a separate thread. ProgressListeners can register to be
 * informed about the merging progress.
 *
 * @author Han Sloetjes
 */
public class TranscriptionMerger implements ClientLogger {
    private List<ProgressListener> listeners;
    private File source1File;
    private File source2File;
    private File destinationFile;
    private Transcription transcription;
    private TranscriptionImpl transcription2;
    private TranscriptionImpl destTranscription;
    private MergeThread mergeThread;

    /** an estimated percentage of processing time needed for loading */
    private final int LOAD_PERCENTAGE = 15;

    /**
     * Creates a new TranscriptionMerger instance.
     *
     * @param source1 the path to the first source file
     * @param source2 the path to the second source file
     * @param destination the path to the destination file
     *
     * @throws IOException thrown if we cannot write to the destination file
     * @throws NullPointerException thrown when any of the parameters is null
     * @throws IllegalArgumentException thrown if any of the source files does
     *         not exist
     */
    public TranscriptionMerger(String source1, String source2,
        String destination) throws IOException {
        if ((source1 == null) || (source2 == null)) {
            LOG.warning("Sources for merging cannot be null.");
            throw new NullPointerException(
                "Sources for merging cannot be null.");
        }

        if (destination == null) {
            LOG.warning("Destination for merging cannot be null.");
            throw new NullPointerException(
                "Destination for merging cannot be null.");
        }

        source1File = new File(source1);
        source2File = new File(source2);

        if (!source1File.exists() || !source2File.exists()) {
            LOG.warning("Sources for merging must be existing files.");
            throw new IllegalArgumentException(
                "Sources for merging must be existing files.");
        }

        destinationFile = new File(destination);

        if (!destinationFile.exists()) {
            destinationFile.createNewFile();
        }

        if (!destinationFile.canWrite() || destinationFile.isDirectory()) {
            LOG.warning("Cannot write to file: " +
                destinationFile.getAbsolutePath());
            throw new IOException("Cannot write to file: " +
                destinationFile.getAbsolutePath());
        }
    }

    /**
     * Creates a new TranscriptionMerger instance.<br>
     * Note: Merging with an existing transcription is not implemented yet.
     *
     * @param transcription the first source as a Transcription object
     * @param source2 the path to the second source file
     * @param destination the path to the destination file
     *
     * @throws IOException thrown if we cannot write to the destination file
     * @throws NullPointerException thrown when any of the parameters is null
     * @throws IllegalArgumentException thrown if any of the source files does
     *         not exist
     */
    public TranscriptionMerger(Transcription transcription, String source2,
        String destination) throws IOException {
        if ((transcription == null) || (source2 == null)) {
            LOG.warning("Sources for merging cannot be null.");
            throw new NullPointerException(
                "Sources for merging cannot be null.");
        }

        if (destination == null) {
            LOG.warning("Destination for merging cannot be null.");
            throw new NullPointerException(
                "Destination for merging cannot be null.");
        }

        source2File = new File(source2);

        if (!source1File.exists() || !source2File.exists()) {
            LOG.warning("Sources for merging must be existing files.");
            throw new IllegalArgumentException(
                "Sources for merging must be existing files.");
        }

        destinationFile = new File(destination);

        if (!destinationFile.exists()) {
            destinationFile.createNewFile();
        }

        if (!destinationFile.canWrite() || destinationFile.isDirectory()) {
            LOG.warning("Cannot write to file: " +
                destinationFile.getAbsolutePath());
            throw new IOException("Cannot write to file: " +
                destinationFile.getAbsolutePath());
        }
    }

    /**
     * Starts the merging process. This creates a separate thread that directs
     * the  merging steps.
     */
    public void startMerge() {
        mergeThread = new MergeThread(TranscriptionMerger.class.getName());
        mergeThread.start();
    }

    /**
     * Creates the first source transcrition.  Note: only the construction of a
     * transcription from a source file is implemented. Duplication or cloning
     * of an existing, in-memory, transcription is not yet supported.
     */
    private void firstTranscription() {
        if (transcription != null) {
            // copy structures from first transcription to dest transcription
            // to be implemented
        } else {
            progressUpdate(5, "Loading first source file...");

            try {
                destTranscription = new TranscriptionImpl(source1File.getAbsolutePath());
                destTranscription.setNotifying(false);
                destTranscription.setChanged();
                progressUpdate(LOAD_PERCENTAGE, "First transcription loaded...");
                LOG.info("First transcription loaded...");
            } catch (Exception rex) {
                progressInterrupt("Could not load the first source file...");
                LOG.warning("Could not load the first source file...");

                if (mergeThread != null) {
                    mergeThread.interrupt();
                }
            }
        }
    }

    /**
     * Creates the second transcription from a source file.
     */
    private void secondTranscription() {
        progressUpdate(LOAD_PERCENTAGE + 5, "Loading second source file...");

        try {
            transcription2 = new TranscriptionImpl(source2File.getAbsolutePath());
            progressUpdate(2 * LOAD_PERCENTAGE, "Second transcription loaded...");
            LOG.info("Second transcription loaded...");
        } catch (Exception rex) {
            progressInterrupt("Could not load the second source file...");
            LOG.warning("Could not load the second source file...");

            if (mergeThread != null) {
                mergeThread.interrupt();
            }
        }
    }

    /**
     * The actual merging process. Checks what tiers, types and CV's should be
     * added and then copies the annotations to the destination transcription.
     */
    private void mergeTranscriptions() {
        progressUpdate(2 * LOAD_PERCENTAGE,
            "Adding Tiers, LinguisticTypes and ControlledVocabularies...");

        try {
            // extract unique tiers
            List<TierImpl> firstTiers = destTranscription.getTiers();
            List<TierImpl> secondTiers = transcription2.getTiers();
            List<TierImpl> tiersToAdd = new ArrayList<TierImpl>();
            Map<String, TierImpl> firstTierTable = new HashMap<String, TierImpl>();
            TierImpl t = null;
            String name = null;

            for (int i = 0; i < firstTiers.size(); i++) {
                t = firstTiers.get(i);
                name = t.getName();
                firstTierTable.put(name, t);
            }

            for (int i = 0; i < secondTiers.size(); i++) {
                t = secondTiers.get(i);
                name = t.getName();

                if (!firstTierTable.containsKey(name)) {
                    tiersToAdd.add(t);
                    LOG.info("Adding tier to list of tiers: " + t.getName());
                }
            }

            //sort by hierarchy
            DefaultMutableTreeNode sortedRootNode = new DefaultMutableTreeNode(
                    "sortRoot");
            Map<TierImpl, DefaultMutableTreeNode> nodes = new HashMap<TierImpl, DefaultMutableTreeNode>();

            for (int i = 0; i < tiersToAdd.size(); i++) {
                t = tiersToAdd.get(i);

                DefaultMutableTreeNode node = new DefaultMutableTreeNode(t);
                nodes.put(t, node);
            }

            for (int i = 0; i < tiersToAdd.size(); i++) {
                t = tiersToAdd.get(i);

                if ((t.getParentTier() == null) ||
                        !tiersToAdd.contains(t.getParentTier())) {
                    sortedRootNode.add(nodes.get(t));
                } else {
                    nodes.get(t.getParentTier()).add(nodes.get(
                            t));
                }
            }

            // sort the tiers to add
            tiersToAdd.clear();

            Enumeration en = sortedRootNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();

                final Object userObject = node.getUserObject();
				if (userObject instanceof TierImpl) {
                    tiersToAdd.add((TierImpl)userObject);
                }
            }

            // first add tiers, linguistic types and CV's
            addTiersTypesAndCVs(tiersToAdd);

            // add the sorted tiers,
            int tierStartProgress = 3 * LOAD_PERCENTAGE;
            int numTopTiers = Math.max(sortedRootNode.getChildCount(), 1);
            progressUpdate(tierStartProgress, "Start adding annotations...");

            int progressPerIndepTier = (100 - (4 * LOAD_PERCENTAGE)) / numTopTiers;
            int tierNum = 1;
            String busy = "...";

            // loop over 'top' tiers and add annotations
            Enumeration topTierEnum = sortedRootNode.children();

            while (topTierEnum.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) topTierEnum.nextElement();
                Object o = node.getUserObject();

                if (o instanceof TierImpl) {
                    TierImpl tier = (TierImpl) o;
                    progressUpdate(tierStartProgress,
                        "Merging tier: " + tier.getName());
                    LOG.info("Merging tier: " + tier.getName());

                    List<AbstractAnnotation> annotations = tier.getAnnotations();
                    int numAnn = annotations.size();

                    if (numAnn > 0) {
                        int ppa = progressPerIndepTier / numAnn;
                        DefaultMutableTreeNode recordNode = null;

                        for (int i = 0; i < numAnn; i++) {
                        	AbstractAnnotation ann = annotations.get(i);
                            recordNode = AnnotationRecreator.createTreeForAnnotation(ann);
                            AnnotationRecreator.createAnnotationFromTree(destTranscription,
                                recordNode);

                            progressUpdate(tierStartProgress + (ppa * i), busy);
                        }

                        LOG.info("Added " + numAnn + " annotations to " +
                            tier.getName());
                    }

                    progressUpdate(tierStartProgress +
                        (tierNum * progressPerIndepTier),
                        "Done merging tier: " + tier.getName());
                    LOG.info("Done merging tier: " + tier.getName());
                    tierNum++;
                }
            }

            progressUpdate(100 - LOAD_PERCENTAGE, "Saving transcription...");

            // save the new transcription
            TranscriptionStore transcriptionStore = ACMTranscriptionStore.getCurrentTranscriptionStore();
			int saveAsType = SaveAs27Preferences.saveAsType(destTranscription);
            transcriptionStore.storeTranscription(destTranscription, null,
                new ArrayList<TierImpl>(0), destinationFile.getAbsolutePath(),
                saveAsType);

            LOG.info("Transcription saved to: " +
                destinationFile.getAbsolutePath());
                
            //preference file
            //copy the first source file pfsx file
            PreferencesWriter xmlPrefsWriter = new PreferencesWriter();    
            String fileName = destinationFile.getAbsolutePath();
        	String prefName = fileName.substring(0, fileName.lastIndexOf('.'));
        	prefName = prefName + ".pfsx";
        	xmlPrefsWriter.encodeAndSave(Preferences.loadPreferencesForFile(destTranscription.getFullPath()), prefName); 
        	
            progressComplete("Completed merging transcription...");
        } catch (Exception rex) {
            progressInterrupt("Error while merging: " + rex.getMessage());

            if (mergeThread != null) {
                mergeThread.interrupt();
            }
        }
    }

    /**
     * Adds the tiers in the specified List to the destination transcription,
     * after performing some checks.  If Linguistic types and/or CV's should
     * be copied these are copied first.
     *
     * @param tiersToAdd a list of tiers to add to the destination
     */
    private void addTiersTypesAndCVs(List<TierImpl> tiersToAdd) {
        if ((tiersToAdd == null) || (destTranscription == null) ||
                (transcription2 == null)) {
            LOG.warning("Transcription or tiers null");

            return;
        }

        Map<String, ControlledVocabulary> renamedCVS = new HashMap<String, ControlledVocabulary>();
        Map<String, LinguisticType> renamedTypes = new HashMap<String, LinguisticType>();
        List<LinguisticType> typesToAdd = new ArrayList<LinguisticType>();
        List<ControlledVocabulary> cvsToAdd = new ArrayList<ControlledVocabulary>();
        List<LinguisticType> destTypes;

        for (TierImpl t : tiersToAdd) {
        	LinguisticType lt = t.getLinguisticType();

            if (!typesToAdd.contains(lt)) {
                typesToAdd.add(lt);
            }

            //typeName = lt.getLinguisticTypeName();

            if (lt.isUsingControlledVocabulary()) {
            	ControlledVocabulary cv = transcription2.getControlledVocabulary(lt.getControlledVocabularyName());

                if (!cvsToAdd.contains(cv)) {
                    cvsToAdd.add(cv);
                }
            }
        }

        // add CV's, renaming when necessary
        for (ControlledVocabulary cv : cvsToAdd) {
        	ControlledVocabulary cv2 = destTranscription.getControlledVocabulary(cv.getName());

            if (cv2 == null) {
                destTranscription.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            } else if (!cv.equals(cv2)) {
                // rename
                String newCVName = cv.getName() + "-copy";

                renamedCVS = new HashMap<String, ControlledVocabulary>();

                LOG.info("Renamed Controlled Vocabulary: " + cv.getName() +
                    " to " + newCVName);
                renamedCVS.put(cv.getName(), cv);
                cv.setName(newCVName);
                destTranscription.addControlledVocabulary(cv);
                LOG.info("Added Controlled Vocabulary: " + cv.getName());
            }
        }

        // end cv's
        for (LinguisticType lt : typesToAdd) {

            //typeName = lt.getLinguisticTypeName();

            if (lt.isUsingControlledVocabulary() &&
                    renamedCVS.containsKey(lt.getControlledVocabularyName())) {
            	ControlledVocabulary cv2 = renamedCVS.get(lt.getControlledVocabularyName());
                lt.setControlledVocabularyName(cv2.getName());
            }

            destTypes = destTranscription.getLinguisticTypes();

            if (!destTypes.contains(lt)) {
            	LinguisticType lt2 = null;

                boolean typeNameExists = false;

                for (int j = 0; j < destTypes.size(); j++) {
                    lt2 = destTypes.get(j);

                    if (lt.getLinguisticTypeName()
                              .equals(lt2.getLinguisticTypeName())) {
                        typeNameExists = true;

                        break;
                    }
                }

                if (!typeNameExists) {
                    destTranscription.addLinguisticType(lt);
                    LOG.info("Added Linguistic Type: " +
                        lt.getLinguisticTypeName());
                } else {
                    // if they are equal the existing will be used by its name
                    if (!lt.equals(lt2)) {
                        String newLTName = lt.getLinguisticTypeName() +
                            "-copy";

                        renamedTypes = new HashMap<String, LinguisticType>();

                        LOG.info("Renamed Linguistic Type: " +
                            lt.getLinguisticTypeName() + " to " + newLTName);
                        renamedTypes.put(lt.getLinguisticTypeName(), lt);
                        lt.setLinguisticTypeName(newLTName);
                        destTranscription.addLinguisticType(lt);
                        LOG.info("Added Linguistic Type: " +
                            lt.getLinguisticTypeName());
                    }
                }
            }
        }

        //end linguistic types
        // create new tiers on the destination transcription
        for (TierImpl t : tiersToAdd) {
            TierImpl t2 = t.getParentTier();

            String parentTierName = null;

            if (t2 != null) {
                parentTierName = t2.getName();
            }

            TierImpl newTier = null;

            if (parentTierName == null) {
                newTier = new TierImpl(t.getName(), t.getParticipant(),
                        destTranscription, null);
            } else {
                t2 = destTranscription.getTierWithId(parentTierName);

                if (t2 != null) {
                    newTier = new TierImpl(t2, t.getName(), t.getParticipant(),
                            destTranscription, null);
                } else {
                    LOG.warning("The parent tier: " + parentTierName +
                        " for tier: " + t.getName() +
                        " was not found in the destination transcription");
                }
            }

            if (newTier != null) {
            	LinguisticType lt = t.getLinguisticType();
                destTypes = destTranscription.getLinguisticTypes();

                if (destTypes.contains(lt)) {
                    // transcription does not perform any checks..
                    if (destTranscription.getTierWithId(newTier.getName()) == null) {
                        destTranscription.addTier(newTier);
                        LOG.info("Created and added tier to destination: " +
                            newTier.getName());
                    }
                    
                    newTier.setLinguisticType(lt);
                } else {
                    LOG.warning("Could not add tier: " + newTier.getName() +
                        " because the Linguistic Type was not found in the destination transcription.");
                }

                newTier.setDefaultLocale(t.getDefaultLocale());
                newTier.setAnnotator(t.getAnnotator());
                newTier.setLangRef(t.getLangRef());
            }
        }

        // end add/create tiers		
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
     * A thread that directs the main steps in the merging process. Merging is
     * done in a separate thread in order to be able to update ui elements
     * that visualize the progress of the process.
     *
     * @author HS
     * @version 1.0, april 2005
     */
    class MergeThread extends Thread {
        /**
         * Creates a new MergeThread instance.
         */
        public MergeThread() {
            super();
        }

        /**
         * Creates a new MergeThread instance.
         *
         * @param name the name of the thread
         */
        public MergeThread(String name) {
            super(name);
        }

        /**
         * The actual action of this thread.
         */
        @Override
		public void run() {
            TranscriptionMerger.this.firstTranscription();

            TranscriptionMerger.this.secondTranscription();

            TranscriptionMerger.this.mergeTranscriptions();
        }

        /**
         * Interrupts the current merging process.
         */
        @Override
		public void interrupt() {
            TranscriptionMerger.LOG.warning("Merge thread interrupted.");
            TranscriptionMerger.this.progressInterrupt("Merging interrupted...");
            super.interrupt();
        }
    }
}
