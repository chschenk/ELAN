package mpi.eudico.client.annotator.commands;

import java.awt.Cursor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.tier.TierCopier;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;


/**
 * A Command that copies a tier or changes the parent of a Tier by making a copy of 
 * the Tier 'under' the new parent. Optionally dependent Tiers will also be copied.
 *
 * @author Han Sloetjes
 */
public class CopyTierCommand implements UndoableCommand, ClientLogger {
    private ArrayList<ProgressListener> listeners;
    private String commandName;
    private TranscriptionImpl transcription;
    private String tierName;
    private String newParentName;
    private String lingType;
    private TierImpl tier;
    private TierImpl parent;
    private LinguisticType destType;
    private Map<String, String> oldNameToNewName;
    private ArrayList<TierImpl> newTiers;
    private List<DefaultMutableTreeNode> copiedAnnotationsNodes;
    private int transitionType;
    private boolean groupWiseCopying = false;
    private boolean includeDepTiers = true;
    private boolean renameOriginalTiers = false;
    private Map<String, String> renamedSourceTierName; // if original tiers are renamed, instead of the new tiers having new names

    /**
     * Creates a new CopyTierCommand instance
     *
     * @param theName the name of the command
     */
    public CopyTierCommand(String theName) {
        commandName = theName;
    }

    /**
	 * Creates copies of the tier and sub-tiers as a dependent tier of the new
	 * parent. <b>Note: </b>it is assumed the types and order of the arguments
	 * are correct.
	 *
	 * @param receiver
	 *            the transcription
	 * @param arguments
	 *            the arguments:
	 *            <ul>
	 *            <li>arg[0] = the tier to copy (String)</li>
	 *            <li>arg[1] the new parent tier, if null the tier will become a
	 *            root tier (String)</li>
	 *            <li>arg[2] the (new) LinguisticType for the tier (String)</li>
	 *            <li>arg[3] whether or not to copy depending tiers (Boolean)</li>
	 *            <li>arg[4] whether or not to rename the original tiers (Boolean)</li>
	 *            </ul>
	 */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        tierName = (String) arguments[0];
        newParentName = (String) arguments[1];
        lingType = (String) arguments[2];

        if (arguments.length >= 4) {
            includeDepTiers = ((Boolean) arguments[3]).booleanValue();
        }
        
        if (arguments.length >= 5) {
            renameOriginalTiers = ((Boolean) arguments[4]).booleanValue();
        }

        tier = transcription.getTierWithId(tierName);

        if ((newParentName == null) || "-".equals(newParentName)) {
            parent = null;
        } else {
            parent = transcription.getTierWithId(newParentName);
        }

        destType = transcription.getLinguisticTypeByName(lingType);

        if (tier == null) {
            progressInterrupt("No valid tier found");

            return;
        }

        if (destType == null) {
            progressInterrupt("No valid LinguisticType found");

            return;
        }

        CopyThread copyThread = new CopyThread(CopyTierCommand.class.getName());

        try {
            copyThread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            transcription.setNotifying(true);
            progressInterrupt("An exception occurred: " + ex.getMessage());
        }
    }

    /**
     * Undo the changes made by this command, i.e. removes the created/copied
     * tier(s).
     */
    @Override
	public void undo() {
        if ((transcription != null) && (newTiers != null)) {
            // It is inconsistent whether an ACMEditEvent.REMOVE_TIER event properly
            // updates listeners to consider the dependent tiers also removed.
        	// (For instance, the MultiTierControlPanel remembers traces of the dependent tiers,
        	// and the TierOrder doesn't remove them at all.)
            // To be on the safe side, just remove all tiers, bottom-up.

            for (int i = newTiers.size() - 1; i >= 0; i--) {
            	transcription.removeTier(newTiers.get(i));
            }
        }
        
        // Rename source tier(s) back if needed
        if (renameOriginalTiers) {
        	for (Map.Entry<String, String> e : renamedSourceTierName.entrySet()) {
        		String origName = e.getKey();
        		String renamed = e.getValue();
        		TierImpl tier = transcription.getTierWithId(renamed);
        		if (tier != null) {
        			tier.setName(origName);
        		}
        	}
        }
    }

    /**
     * Redo the changes made by this command.
     */
    @Override
	public void redo() {
        if ((transcription != null) && (newTiers != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);
            
            // Rename source tier(s) again if needed
            if (renameOriginalTiers) {
            	for (Map.Entry<String, String> e : renamedSourceTierName.entrySet()) {
            		String origName = e.getKey();
            		String renamed = e.getValue();
            		TierImpl tier = transcription.getTierWithId(origName);
            		if (tier != null) {
            			tier.setName(renamed);
            		}
            	}
            }

            String newName = getNameOfCopy();
            Tier tierCopy = null;

            for (TierImpl coptier : newTiers) {
                final String name = coptier.getName();
                
				if (transcription.getTierWithId(name) == null) {
                    transcription.addTier(coptier);
                }

                if (name.equals(newName)) {
                    tierCopy = coptier;
                }
            }

            if (tierCopy != null && !copiedAnnotationsNodes.isEmpty()) {
                transcription.setNotifying(false);

                if (tier.hasParentTier()) {
                    AnnotationRecreator.createAnnotationsSequentially(transcription,
                        copiedAnnotationsNodes, true);
                } else {
                    for (DefaultMutableTreeNode node : copiedAnnotationsNodes) {
                        AnnotationRecreator.createAnnotationFromTree(transcription,
                            node, true);
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
     * Returns the name of the command
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
	 * @return the new name of the tier that was originally copied
	 */
	private String getNameOfCopy() {
		return renameOriginalTiers ? tierName // or oldNameToNewName.get(renamedSourceTierName.get(tierName))
		                         : oldNameToNewName.get(tierName);
	}

	/**
     * Creates a copy of the tier and its depending tiers and tries to
     * re-create  the annotations on the copied tiers. Since the
     * LinguisticType might have been changed, success is not guaranteed.
     *
     * @author Han Sloetjes
     * @version 1.1
     */
    class CopyThread extends Thread {
    	/**
    	 * Depending on the copying mode ('groupwise' or not), this ArrayList
    	 * contains either DefaultMutableTreeNode(with AnnotationDataRecord)s, or
    	 * List&lt;DefaultMutableTreeNode>s.
    	 * 
    	 */
        private ArrayList<Serializable> annotationsNodes;

        /**
         * Creates a new CopyThread instance
         */
        public CopyThread() {
            super();
        }

        /**
         * Creates a new CopyThread instance
         *
         * @param name the name of the thread
         */
        public CopyThread(String name) {
            super(name);
        }

        /**
         * The actual action of this thread.
         */
        @Override
		public void run() {
        	try {
	            checkTransitionType();
	            copyTiers();
	            storeAnnotations();
	            createCopiesOfAnnotations();
	            storeCopiedAnnotations();
	            progressComplete("Operation complete...");
        	} catch (Exception ex) {
        		// This might happen:
        		// java.lang.ClassCastException: mpi.eudico.server.corpora.clomimpl.abstr.RefAnnotation cannot be cast to mpi.eudico.server.corpora.clomimpl.abstr.AlignableAnnotation
        		// if dependent tiers aren't properly adjusted too.
        		// The GUI prevents this case, but there is no other protection.
        		ex.printStackTrace();
	            progressComplete("Operation failed...");
        	}
        }

        /**
         * Interrupts the current merging process.
         */
        @Override
		public void interrupt() {
            super.interrupt();
            progressInterrupt("Operation interrupted...");
        }

        private void checkTransitionType() {
            progressUpdate(5, "Checking LinguisticType transition...");

            int oldStereo = -1;
            int destStereo = -1;

            if (tier.getLinguisticType().getConstraints() != null) {
                oldStereo = tier.getLinguisticType().getConstraints()
                                .getStereoType();
            }

            if (destType.getConstraints() != null) {
                destStereo = destType.getConstraints().getStereoType();
            }

            if (oldStereo == destStereo) {
                transitionType = TierCopier.SAME;
            }

            switch (oldStereo) {
            case -1:

                switch (destStereo) {
                case -1:
                    transitionType = TierCopier.SAME;

                    break;

                case Constraint.TIME_SUBDIVISION:
                    transitionType = TierCopier.ROOT_TO_TIMESUB;

                    break;

                case Constraint.SYMBOLIC_SUBDIVISION:
                    transitionType = TierCopier.ROOT_TO_SYMSUB;

                    break;

                case Constraint.SYMBOLIC_ASSOCIATION:
                    transitionType = TierCopier.ROOT_TO_ASSOC;
                
                	break;
                	
                	case Constraint.INCLUDED_IN:
                	    transitionType = TierCopier.ROOT_TO_INCLUDED_IN;
                }

                break;

            case Constraint.TIME_SUBDIVISION:

                switch (destStereo) {
                case -1:
                    transitionType = TierCopier.ANY_TO_ROOT;

                    break;

                case Constraint.TIME_SUBDIVISION:
                    transitionType = TierCopier.SAME;

                    break;

                case Constraint.SYMBOLIC_SUBDIVISION:
                    transitionType = TierCopier.TIMESUB_TO_SYMSUB;

                    break;

                case Constraint.SYMBOLIC_ASSOCIATION:
                    transitionType = TierCopier.TIMESUB_TO_ASSOC;
                
                break;
                
                case Constraint.INCLUDED_IN:
                    transitionType = TierCopier.TIMESUB_TO_INCLUDED_IN;
                }

                break;

            case Constraint.SYMBOLIC_SUBDIVISION:

                switch (destStereo) {
                case -1:
                    transitionType = TierCopier.ANY_TO_ROOT;

                    break;

                case Constraint.TIME_SUBDIVISION:
                    transitionType = TierCopier.SYMSUB_TO_TIMESUB;

                    break;

                case Constraint.SYMBOLIC_SUBDIVISION:
                    transitionType = TierCopier.SAME;

                    break;

                case Constraint.SYMBOLIC_ASSOCIATION:
                    transitionType = TierCopier.SYMSUB_TO_ASSOC;
                
                break;
                
                case Constraint.INCLUDED_IN:
                    transitionType = TierCopier.SYMSUB_TO_INCLUDED_IN;
                }

                break;

            case Constraint.SYMBOLIC_ASSOCIATION:

                switch (destStereo) {
                case -1:
                    transitionType = TierCopier.ANY_TO_ROOT;

                    break;

                case Constraint.TIME_SUBDIVISION:
                    transitionType = TierCopier.ASSOC_TO_TIMESUB;

                    break;

                case Constraint.SYMBOLIC_SUBDIVISION:
                    transitionType = TierCopier.ASSOC_TO_SYMSUB;

                    break;

                case Constraint.SYMBOLIC_ASSOCIATION:
                    transitionType = TierCopier.SAME;
                
                    break;
                
                case Constraint.INCLUDED_IN:
                    transitionType = TierCopier.ASSOC_TO_INCLUDED_IN;
                }
            
                break;
                
            case Constraint.INCLUDED_IN:
                
                switch (destStereo) {
                case -1:
                    transitionType = TierCopier.ANY_TO_ROOT;
                
                    break;
                    
                case Constraint.TIME_SUBDIVISION:
                    transitionType = TierCopier.INCLUDED_IN_TO_TIMESUB;
                
                    break;
                    
                case Constraint.SYMBOLIC_SUBDIVISION:
                    transitionType = TierCopier.INCLUDED_IN_TO_SYMSUB;
                
                    break;
                    
                case Constraint.SYMBOLIC_ASSOCIATION:
                    transitionType = TierCopier.ASSOC_TO_INCLUDED_IN;
                
                    break;
                
                case Constraint.INCLUDED_IN:
                    transitionType = TierCopier.SAME;
                }
            
            }

            if (parent != null && parent != tier) {
                int stereo = destType.getConstraints().getStereoType();

                if (((transitionType == TierCopier.SAME) &&
                        ((stereo == Constraint.TIME_SUBDIVISION) ||
                        (stereo == Constraint.SYMBOLIC_SUBDIVISION))) ||
                        ((transitionType != TierCopier.ANY_TO_ROOT) &&
                        (transitionType != TierCopier.ROOT_TO_ASSOC) &&
                        (transitionType != TierCopier.ROOT_TO_SYMSUB) &&
                        (transitionType != TierCopier.ROOT_TO_TIMESUB) &&
                        (transitionType != TierCopier.ROOT_TO_INCLUDED_IN))) {
                    groupWiseCopying = true;
                }
            }
            //System.out.println("group copying: " + groupWiseCopying);
        }

        /**
         * Create copies of the tier(s) involved. A unique name is generated
         * and  'proper' Linguistic Types are applied to the new tiers.
         */
        private void copyTiers() {
            progressUpdate(10, "Copying tier(s)");

            List<TierImpl> depTiers = tier.getDependentTiers();

            if (!includeDepTiers) {
                depTiers = new ArrayList<TierImpl>();
            }

            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(tier);
            final int numDepTiers = depTiers.size();
			DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[numDepTiers];
            HashMap<TierImpl, DefaultMutableTreeNode> nodeMap = new HashMap<TierImpl, DefaultMutableTreeNode>();

            for (int i = 0; i < numDepTiers; i++) {
                TierImpl ti = depTiers.get(i);
                nodes[i] = new DefaultMutableTreeNode(ti);
                nodeMap.put(ti, nodes[i]);
            }

            for (int i = 0; i < numDepTiers; i++) {
                TierImpl ti = depTiers.get(i);

                if (ti.getParentTier() == tier) {
                    rootNode.add(nodes[i]);
                } else {
                    DefaultMutableTreeNode dn = nodeMap.get(ti.getParentTier());

                    if (dn != null) {
                        dn.add(nodes[i]);
                    }
                }
            }

            // a subtree has been made, now create copies
            oldNameToNewName = new HashMap<String, String>();
            newTiers = new ArrayList<TierImpl>();
            if (renameOriginalTiers) {
            	renamedSourceTierName = new HashMap<String, String>();
            }

            Enumeration en = rootNode.breadthFirstEnumeration();

            while (en.hasMoreElements()) {
            	DefaultMutableTreeNode dn = (DefaultMutableTreeNode) en.nextElement();
            	TierImpl to = (TierImpl) dn.getUserObject();

                String newName;
                if (renameOriginalTiers) {
                	String origTierNewName = getUniqueName(to.getName(), "orig");
                	newName = to.getName();
                	to.setName(origTierNewName);
                	renamedSourceTierName.put(newName, origTierNewName);
                } else {
                	newName = getUniqueName(to.getName(), "cp");
                }
                oldNameToNewName.put(to.getName(), newName);

                LinguisticType nextType = to.getLinguisticType();

                if (to == tier) {
                    nextType = destType;
                } else {
                    // only check the transition types that imply a change of type for dep tiers. 
                	LinguisticType curType = to.getLinguisticType();

                    int stereo = -1;
                    if (curType.getConstraints() != null) {
                        stereo = curType.getConstraints().getStereoType();
                    }

                    switch (transitionType) {
                    case TierCopier.ROOT_TO_SYMSUB:
                    case TierCopier.ROOT_TO_ASSOC:
                    case TierCopier.TIMESUB_TO_SYMSUB:
                    case TierCopier.TIMESUB_TO_ASSOC:
                    case TierCopier.INCLUDED_IN_TO_SYMSUB:
                    case TierCopier.INCLUDED_IN_TO_ASSOC:

                        if (stereo == Constraint.TIME_SUBDIVISION) {
                            // same type as the tier that is reparented. in case of 
                            // ROOT_TO_ASSOC Symbolic Subdivision would be better
                            nextType = destType;
                        }

                        break;

                    case TierCopier.SYMSUB_TO_ASSOC:

                        if (stereo != Constraint.SYMBOLIC_ASSOCIATION) {
                            nextType = destType;
                        }
                    }
                }

                TierImpl pt = null;

                if (dn.getParent() != null) {
                    String oldParentName = ((TierImpl) ((DefaultMutableTreeNode) dn.getParent()).getUserObject()).getName();
                    String newPName = oldNameToNewName.get(oldParentName);
                    pt = transcription.getTierWithId(newPName);
                } else {
                    // the tier to reparent
                    if (to == tier) {
                        pt = parent;
                    }
                }

                TierImpl tn;
                
                if (pt != null) {
                    tn = new TierImpl(pt, newName, to.getParticipant(),
                            transcription, nextType);
                } else {
                    tn = new TierImpl(newName, to.getParticipant(),
                            transcription, nextType);
                }

                tn.setDefaultLocale(to.getDefaultLocale());
                tn.setAnnotator(to.getAnnotator());
                tn.setLangRef(to.getLangRef());

                newTiers.add(tn);
                transcription.addTier(tn);
                //System.out.println("OT: " + to.getName() + " LT: " +
                //    to.getLinguisticType().getLinguisticTypeName());
                //System.out.println("NT: " + tn.getName() + " LT: " +
                //    tn.getLinguisticType().getLinguisticTypeName());
            }

            progressUpdate(20, "Tier(s) copied");
        }

        /**
         * Store information on the annotations of the tier to reparent (and
         * depending tiers).
         */
        private void storeAnnotations() {
            progressUpdate(20, "Copying annotations...");

            int progForThis = 30; // (100 - 20 - 20) / 2
            annotationsNodes = new ArrayList<Serializable>();

            List<AbstractAnnotation> annos = tier.getAnnotations();
            float perAnn = 0f;

            if (annos.size() > 0) {
                perAnn = progForThis / (float) annos.size();
            }

            int count = 0;

            if (!groupWiseCopying) {
            	for (AbstractAnnotation ann : annos) {
                    DefaultMutableTreeNode savedNode;
                    
                    if (includeDepTiers) {
                    	savedNode = AnnotationRecreator.createTreeForAnnotation(ann);
                    } else {
                    	savedNode = AnnotationRecreator.createNodeForAnnotation(ann);
                    }
                    annotationsNodes.add(savedNode);

                    count++;
                    progressUpdate((int) (20 + (count * perAnn)), null);
                }
            } else {
                ArrayList<DefaultMutableTreeNode> group = new ArrayList<DefaultMutableTreeNode>();
                TierImpl rootTier = tier.getRootTier();
                Annotation curParent = null;

            	for (AbstractAnnotation ann : annos) {
                    DefaultMutableTreeNode savedNode;

                    if (includeDepTiers) {
                    	savedNode = AnnotationRecreator.createTreeForAnnotation(ann);
                    } else {
                    	savedNode = AnnotationRecreator.createNodeForAnnotation(ann);
                    }
                    
                    // Where do we save this node?
                    if (curParent == null) {
                        // add to first group
                    	group.add(savedNode);

                        curParent = rootTier.getAnnotationAtTime(ann.getBeginTimeBoundary());
                    } else if (rootTier.getAnnotationAtTime(
                                ann.getBeginTimeBoundary()) == curParent) {
                        // add to current group
                    	group.add(savedNode);
                    } else {
                        annotationsNodes.add(group); // finish group
                        group = new ArrayList<DefaultMutableTreeNode>();
                        curParent = rootTier.getAnnotationAtTime(ann.getBeginTimeBoundary());

                        // add to new group
                    	group.add(savedNode);
                    }

                    count++;
                    progressUpdate((int) (20 + (count * perAnn)), null);
                }

                // add the last group...
                if (!group.isEmpty()) {
                    annotationsNodes.add(group);
                }
            }
        }

        /**
         * Creates copies of the annotation: success might depend on the new
         * LinguisticType  and on the annotations on the (new) parent tier.
         */
        private void createCopiesOfAnnotations() {
            progressUpdate(60, "Creating new annotations...");

            int progForThis = 30; // (100 - 20 - 20) / 2
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            final int numAnnotationsNodes = annotationsNodes.size();
			if (numAnnotationsNodes > 0) {
                float perAnn = progForThis / (float) numAnnotationsNodes;
                int count = 0;

                transcription.setNotifying(false);

                TierCopier copier = new TierCopier();
                DefaultMutableTreeNode node;

                if (parent != null) {
                    // int stereo = destType.getConstraints().getStereoType();
                    if (groupWiseCopying) {
                        // annotationNodes contains ArrayList<DefaultMutableTreeNode>s
                        ArrayList<DefaultMutableTreeNode> group;

                        for (int i = 0; i < numAnnotationsNodes; i++) {
                            group = (ArrayList<DefaultMutableTreeNode>) annotationsNodes.get(i);
                            copier.createAnnotationsSequentially(transcription,
                                group, oldNameToNewName);
                            count++;
                            progressUpdate((int) (50 + (count * perAnn)), null);
                        }
                    } else {
                        for (int i = 0; i < numAnnotationsNodes; i++) {
                            node = (DefaultMutableTreeNode) annotationsNodes.get(i);
                            copier.createAnnotationFromTree(transcription,
                                node, oldNameToNewName);
                            count++;
                            progressUpdate((int) (50 + (count * perAnn)), null);
                        }
                    }
                } else {
                    for (int i = 0; i < numAnnotationsNodes; i++) {
                        node = (DefaultMutableTreeNode) annotationsNodes.get(i);
                        copier.createRootAnnotationFromTree(transcription,
                            node, oldNameToNewName);
                        count++;
                        progressUpdate((int) (50 + (count * perAnn)), null);
                    }
                }

                transcription.setNotifying(true);
            }

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
        }

        /**
         * Store datarecords for the new annotations for undo/redo
         */
        private void storeCopiedAnnotations() {
             progressUpdate(80, "Storing information for undo/redo...");

            int progForThis = 20; // leftover
            String newName = getNameOfCopy();
            TierImpl tierCopy = null;

            for (TierImpl t : newTiers) {
                if (t.getName().equals(newName)) {
                    tierCopy = t; // should be == newTiers.get(0)
                    break;
                }
            }

            if (tierCopy != null) {
                List<AbstractAnnotation> annos = tierCopy.getAnnotations();
                copiedAnnotationsNodes = new ArrayList<DefaultMutableTreeNode>(annos.size());

                float perAnn = progForThis / (float) annos.size();
                int count = 0;

                for (AbstractAnnotation ann : annos) {
                    copiedAnnotationsNodes.add(AnnotationRecreator.createTreeForAnnotation(ann));
                    count++;
                    progressUpdate((int) (80 + (count * perAnn)), null);
                }
            }
        }

        /**
         * Creates a unique name for a copy of an existing tier.
         *
         * @param name the name of the original tier
         *
         * @return the name for a copy, unique within the transcription
         */
        private String getUniqueName(String name, String suffix) {
            String nName = name + "-" + suffix;

            if (transcription.getTierWithId(nName) != null) {
                for (int i = 1; i < 50; i++) {
                    nName = nName + "-" + i;

                    if (transcription.getTierWithId(nName) == null) {
                        return nName;
                    }
                }
            }

            return nName;
        }
    }
}
