package mpi.eudico.client.annotator.commands;


import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.client.annotator.util.TierNameCompare;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.TierRecord;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * Undoable Command that merges 2 tiers with depending tiers, if possible. It creates new
 * annotations on a third tier group. Overlapping annotations are not merged, instead 
 * annotations of the second tier are create on top of those of the first tier.
 * Non overlapping annotations are copied as is. The mapping of tier names in the 
 * second group to tier names in the first, is based on prefix or suffix.
 *
 * @author Han Sloetjes
 * @version 1.0 October 2009
 */
public class MergeTierGroupCommand implements UndoableCommand {
    private ArrayList<ProgressListener> listeners;
    private String commandName;
    private TranscriptionImpl transcription;
    private String sourceTierName1;
    private String sourceTierName2;
    private String destTierName;
    private ArrayList<TierRecord> tierRecords;

    private ArrayList<DefaultMutableTreeNode> annRecords;
    private String copySuffix;

    /**
     * Constructor.
     *
     * @param name the name of the command
     */
    public MergeTierGroupCommand(String name) {
        commandName = name;
    }

    /**
     * Oct 2009: creates a new tier group based on tier 1 plus depending tiers.
     * Copies annotations form tier 1 onto the new group and then copies 
     * the annotations of tier 2 to the new group (whereby existing annotations 
     * can be changed).
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments: <ul><li>arg[0] = the first source tier
     *        (String)</li> <li>arg[1] the second source tier (String)</li>
     *        <li>arg[2] the destination tier suffix (for new tier) (String)</li>
     *        </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;

        sourceTierName1 = (String) arguments[0];
        sourceTierName2 = (String) arguments[1];
        copySuffix = (String) arguments[2];
        
        Tier sourceTier1 = transcription.getTierWithId(sourceTierName1);
        Tier sourceTier2 = transcription.getTierWithId(sourceTierName2);

        if ((sourceTier1 == null) || (sourceTier2 == null)) {
            progressInterrupt("One of the sourcetiers could not be found");

            return;
        }

        if (copySuffix == null) {
        	copySuffix = "MergedGroup";
            ClientLogger.LOG.warning("Name of destination tier is null, changed to MergedGroup");
        }

        Tier destTier = transcription.getTierWithId(sourceTierName1 + copySuffix);

        if (destTier != null) {
            // it already exists
            int count = 1;
            String cName = copySuffix + "-";

            while (destTier != null) {
                cName = cName + count;
                destTier = transcription.getTierWithId(sourceTierName1 + cName);
                count++;
            }

            ClientLogger.LOG.warning("Tier " + (sourceTierName1 + copySuffix) +
                " already exists, changed name to " + (sourceTierName1 + cName));
            copySuffix = cName;
        }

        destTierName = sourceTierName1 + copySuffix;
        
        progressUpdate(5, "Creating tier group: " + destTierName);

        Thread mergeThread = new MergeThread(MergeTierGroupCommand.class.getName());

        try {
        	mergeThread.start();
        } catch (Exception exc) {
            transcription.setNotifying(true);
            ClientLogger.LOG.severe("Exception in calculation of overlaps: " +
                exc.getMessage());
            progressInterrupt("An exception occurred: " + exc.getMessage());
        }
    }

    /**
     * Removes the tier group.
     *
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        if ((transcription != null) && (destTierName != null)) {
            setWaitCursor(true);
            
            TierImpl destTier = transcription.getTierWithId(destTierName);
            if (destTier != null) {
            	transcription.removeTier(destTier);
            }

            setWaitCursor(false);
        }
    }

    /**
     * Adds the tiers again and the annotations.
     *
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if ((transcription != null) && (destTierName != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);
            // create the tiers
            TierRecord tr = null;
            TierImpl pt, tt;
            LinguisticType lt;
            Locale l;
            for (int i = 0; i < tierRecords.size(); i++) {
            	pt = null;
            	tr = tierRecords.get(i);
            	
            	if (transcription.getTierWithId(tr.getName()) == null) {
            		if (tr.getParentTier() != null) {
            			pt = transcription.getTierWithId(tr.getParentTier());
            			if (pt == null) {
            				// error situation
            				ClientLogger.LOG.severe("Cannot recreate tier: " + tr.getName() + 
            						" because the parent tier was not found");
            				continue;
            			} 
            		}
            		lt = transcription.getLinguisticTypeByName(tr.getLinguisticType());
            		if (lt == null) {
            			ClientLogger.LOG.severe("Cannot recreate tier: " + tr.getName() + 
						" because the linguistic type was not found");
            			continue;
            		}
            		tt = new TierImpl(pt, tr.getName(), tr.getParticipant(), transcription, lt);
            		transcription.addTier(tt);
            		tt.setAnnotator(tr.getAnnotator());
            		if (tr.getDefaultLocale() != null) {
            			l = new Locale(tr.getDefaultLocale());
            			tt.setDefaultLocale(l);
            		}
            		tt.setLangRef(tr.getLangRef());
            	} else {
            		ClientLogger.LOG.warning("Tier already exists!");
            	}
            }

            if (transcription.getTierWithId(destTierName) == null) {
                //transcription.addTier(destTier);
            	ClientLogger.LOG.severe("Could not recreate tier group, exiting...");
                setWaitCursor(false);
                // restore the time propagation mode
                transcription.setTimeChangePropagationMode(curPropMode);
                return;
            }
            
            if ((annRecords != null) && (annRecords.size() > 0)) {
                transcription.setNotifying(false);

                DefaultMutableTreeNode node;
                Annotation ann;

                for (int i = 0; i < annRecords.size(); i++) {
                    node = annRecords.get(i);
                    ann = AnnotationRecreator.createAnnotationFromTree(transcription, node, true);
                    if (ann == null) {
                    	ClientLogger.LOG.warning("Could not recreate annotation: " + 
                    			((AnnotationDataRecord) node.getUserObject()).getValue());
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
     * Returns a TierRecord for the specified tier.
     * 
     * @param t the tier
     * @return the tier record
     */
    private TierRecord recordForTier(TierImpl t) {
        TierRecord tr = new TierRecord();
        tr.setAnnotator(t.getAnnotator());
        Locale defLoc = t.getDefaultLocale();
        if (defLoc != null) {
        	tr.setDefaultLocale(defLoc.getLanguage());
        }
        tr.setLinguisticType(t.getLinguisticType().getLinguisticTypeName());
        tr.setName(t.getName());
        tr.setParticipant(t.getParticipant());
        if (t.hasParentTier()) {
        	tr.setParentTier(t.getParentTier().getName());
        }
        tr.setLangRef(t.getLangRef());
        tr.setExtRef(t.getExtRef());
        
        return tr;
    }
    
    /**
     * Creates copies of dependent tiers using the specified suffix.
     * 
     * @param parent the parent tier for the tiers to create
     * @param children the original depending tiers
     * @param copySuffix the suffix
     */
    private void createChildTiers(TierImpl parent, List<TierImpl> children, String copySuffix) {
    	if (parent == null || children == null || children.size() == 0) {
    		return;
    	}
    	TierImpl ch;
    	for (int i = 0; i < children.size(); i++) {
    		ch = children.get(i);
    		String nch = ch.getName() + copySuffix;
    		TierImpl ti = new TierImpl(parent, nch, ch.getParticipant(), parent.getTranscription(), ch.getLinguisticType());
    		ti.setAnnotator(ch.getAnnotator());
    		ti.setDefaultLocale(ch.getDefaultLocale());
    		ti.setLangRef(ch.getLangRef());
    		parent.getTranscription().addTier(ti);
    		tierRecords.add(recordForTier(ti));
    		
    		createChildTiers(ti, ch.getChildTiers(), copySuffix);
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
            TierImpl sourceTier1 = transcription.getTierWithId(sourceTierName1);
            TierImpl sourceTier2 = transcription.getTierWithId(sourceTierName2);
        	
            TierImpl destTier = new TierImpl(null, destTierName, null, transcription, sourceTier1.getLinguisticType());
            destTier.setDefaultLocale(sourceTier1.getDefaultLocale());
            destTier.setParticipant(sourceTier1.getParticipant());
            destTier.setAnnotator(sourceTier1.getAnnotator());
            destTier.setLangRef(sourceTier1.getLangRef());
            transcription.addTier(destTier);
            
            tierRecords = new ArrayList<TierRecord>(10);
            tierRecords.add(recordForTier(destTier));

            createChildTiers(destTier, sourceTier1.getChildTiers(), copySuffix);
            // add dep. tiers of sourcetier 2 that are not in sourcetier1 ?
            
            progressUpdate(10, "Created tier group: " + destTierName);
            
            transcription.setNotifying(false);

            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }
            TierNameCompare tnCompare = new TierNameCompare();
            int[] indices = tnCompare.findCorrespondingAffix(sourceTier1.getName(), sourceTier2.getName());
            char del = ' ';
        	String affix = "";
        	if (indices != null && indices[0] > -1) {
        		del = sourceTier1.getName().charAt(indices[0]);
        		if (indices[1] <= TierNameCompare.PREFIX_MODE) {//maintain left side 
        			int di = sourceTier1.getName().lastIndexOf(del);
        			if (di > -1) {
        				affix = sourceTier1.getName().substring(di);
        			}
        		} else {// maintain right side
        			int di = sourceTier1.getName().indexOf(del);
        			if (di > -1) {
        				affix = sourceTier1.getName().substring(0, di);
        			}
        		}
        	}
        	annRecords = new ArrayList<DefaultMutableTreeNode>();

            progressUpdate(10, "Creating copies of annotations...");
            
            // two counters or two loops: one for both source tiers
            List<AlignableAnnotation> sourceAnns1 = new ArrayList<AlignableAnnotation>(sourceTier1.getAlignableAnnotations());
            int numAnns1 = sourceAnns1.size();
            List<AlignableAnnotation> sourceAnns2 = new ArrayList<AlignableAnnotation>(sourceTier2.getAlignableAnnotations());
            int numAnns2 = sourceAnns2.size();
            int numAnn = numAnns1 + numAnns2;
            float perAnn = 90f / (numAnn > 0 ? numAnn : 1);
            
            AlignableAnnotation ann1 = null, ann2 = null;
            DefaultMutableTreeNode node;           

            for (int i = 0; i < numAnns1; i++) {
            	ann1 = sourceAnns1.get(i);
            	node = AnnotationRecreator.createTreeForAnnotation(ann1);
            	tnCompare.addAffixToTierNames(node, copySuffix, TierNameCompare.KEEP_LEFT_PART);
            	ann2 = (AlignableAnnotation) AnnotationRecreator.createAnnotationFromTree(transcription, node);
            	annRecords.add(AnnotationRecreator.createTreeForAnnotation(ann2));
            	
            	progressUpdate((int) (10 + (i * perAnn)), null);
            }
            
            int numSkippedAnnos = 0;
            for (int i = 0; i < numAnns2; i++) {
            	ann1 = sourceAnns2.get(i);
            	node = AnnotationRecreator.createTreeForAnnotation(ann1);
            	// first translate to the correct tier name
            	tnCompare.adjustTierNames(node, affix, del, indices[1]);
            	// add the suffix
            	tnCompare.addAffixToTierNames(node, copySuffix, TierNameCompare.KEEP_LEFT_PART);
            	ann2 = (AlignableAnnotation) AnnotationRecreator.createAnnotationFromTree(transcription, node);
            	// if the mapping to the new tier names was not successful, the annotation could not be created
            	if (ann2 != null) {
            		annRecords.add(AnnotationRecreator.createTreeForAnnotation(ann2));
            	} else {
            		// could maybe break here? or sometimes one annotation succeeds but the next maybe not,
            		// depending on child annotations present?
            		if (numSkippedAnnos == 0) {
            			ClientLogger.LOG.warning("Could not recreate annotation on new tier group.");
            		}
            		numSkippedAnnos++;
            	}
            	
            	progressUpdate((int) (10 + (numAnns1 * perAnn) + (i * perAnn)), null);
            }
            
            if (numSkippedAnnos > 1) {
            	ClientLogger.LOG.warning(numSkippedAnnos + " annotations could not be recreated.");
            }
            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);

            transcription.setNotifying(true);

            progressComplete("Operation complete...");
        }
    }
}
