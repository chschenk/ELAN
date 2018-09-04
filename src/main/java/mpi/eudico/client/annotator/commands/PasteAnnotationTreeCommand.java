package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataFlavor;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.AnnotationTreeDataFlavor;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.TierNameCompare;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A Command to paste an annotation with its depending annotations (i.e. a transferable AnnotationDataRecord or 
 * DefaultMutabletreeNode) from the System's Clipboard.
 * 
 * Note June 2006 transferal of application/x-java-serialized-object objects between jvm's 
 * doesn't seem to work on Mac OS X. The Transferable sun.awt.datatransfer.ClipboardTransferable@8b4cb8 
 * doesn't support any flavor with human presentable name "application/x-java-serialized-object" 
 */
public class PasteAnnotationTreeCommand extends NewAnnotationCommand implements ClientLogger {
    protected DefaultMutableTreeNode node;
    protected AnnotationDataRecord record;
    protected String destTierName;
    protected Long newBeginTime;
    
    /**
     * Creates a new PasteAnnotationTreeCommand instance
     * @param name the name of the command
     */
    public PasteAnnotationTreeCommand(String name) {
        super(name);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
    	transcription.setNotifying(false);// is this necessary?
        super.undo();
        transcription.setNotifying(true);
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        if (transcription != null) {
            setWaitCursor(true);
            transcription.setNotifying(false);
            
            newAnnotation();
            
            transcription.setNotifying(true);
            setWaitCursor(false);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct. The arguments parameter can be null, of length 1 or of length 2<br>
     * If the arguments parameter is null the following order is used to decide on which tier the 
     * annotation has te be pasted:<br>
     * - the tier of the same name as of the tier of the top level annotation in the tree, 
     * if it is present in the destination transcription<br>
     * - the current active tier
     * 
     * If there isn't an annotation tree (in the form of a DefualtMutableTreeNode) on the clipboard but 
     * an AnnotationDataRecord instead this single toplevel annoation is pasted.
     * 
     * @param receiver a transcription
     * @param arguments the arguments:  <ul><li>null</li></ul>
     * or 
     * <ul><li>arg[0] = destination tier name if the annotation should be pasted to a tier with another 
     * name (String)</li> <li>arg[1] = destination begin time; if the annotation should be pasted at 
     * another location (Long)</li>
     * </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        transcription = (TranscriptionImpl) receiver;
        if (canAccessSystemClipboard()) {
            Object contents = null;
            Transferable ta;
            
            // get Clipboard contents.
            ta = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            try {
                contents = ta.getTransferData(AnnotationTreeDataFlavor.getInstance());
            } catch (UnsupportedFlavorException nfe) {
                //LOG.warning("Unsupported flavor on the clipboord: " + nfe.getMessage());
                // check if there is a single annotation on the clipboard
                try {
                    contents = ta.getTransferData(AnnotationDataFlavor.getInstance());
                } catch (Exception e) {
                    LOG.warning("Unsupported flavor on the clipboord: " + nfe.getMessage());
                    return;
                }
                
                //return;
            } catch (IOException ioe) {
                LOG.warning("I/O error: " + ioe.getMessage());
                return;
            }
           
            if (contents instanceof DefaultMutableTreeNode) {
                node = (DefaultMutableTreeNode) contents;
                record = (AnnotationDataRecord) node.getUserObject();
            } else if (contents instanceof AnnotationDataRecord) {
                record = (AnnotationDataRecord) contents;
            } else {
                return;
            }
                
            destTierName = record.getTierName();
            //LOG.info("Record: " + record.toString() + " " + record.getBeginTime() +
            //        " " + record.getEndTime());
            if (arguments != null && arguments.length > 0) {
                if (arguments[0] != null) {
                    destTierName = (String) arguments[0]; 
                }
                    
                if (arguments.length > 1) {
                    newBeginTime = (Long) arguments[1];
                    
                    if (node != null && record != null) {
                        if (newBeginTime.longValue() != record.getBeginTime()) {
                            // adjust times if necessary
                            adjustTimes(node, newBeginTime.longValue() - record.getBeginTime());
                        }
                    }
                }
            }
            TierImpl tier = null;
            if (destTierName != null) {
                tier = (TierImpl) transcription.getTierWithId(destTierName);
                // if the destination tier is not the same as the source and there are child tiers
                // check if the child annotations can be mapped to the new dependent tiers
                if (tier != null && node != null && !destTierName.equals(record.getTierName())) {
                	// if source and destination tier contains same affix, update tier names
                	TierNameCompare tnc = new TierNameCompare();
                	int[] indices = tnc.findCorrespondingAffix(record.getTierName(), destTierName);
                	if (indices != null && indices[0] > -1) {
                		char del = record.getTierName().charAt(indices[0]);
                		String affix = "";
                		if (indices[1] <= TierNameCompare.PREFIX_MODE) {
                			int di = destTierName.lastIndexOf(del);
                			if (di > -1) {
                				affix = destTierName.substring(di);
                			}
                		} else {
                			int di = destTierName.indexOf(del);
                			if (di > -1) {
                				affix = destTierName.substring(0, di);
                			}
                		}
                		tnc.adjustTierNames(node, affix, del, indices[1]);
                	}
                }

            }
            // if the tier is not found try the active tier ??
            if (tier == null) {
                tier = (TierImpl) ELANCommandFactory.getViewerManager(transcription).getMultiTierControlPanel().getActiveTier();
                if (tier != null) {
                    destTierName = tier.getName();
                }
            }
                
            if (tier == null) {
                LOG.warning("Cannot paste annotation: tier not found: " + destTierName);
                return;
            }
            Long[] args = new Long[2];
            if (newBeginTime != null) {
                args[0] = newBeginTime;
                args[1] = new Long(newBeginTime.longValue() + (record.getEndTime() - record.getBeginTime()));
            } else {
                args[0] = new Long(record.getBeginTime());
                args[1] = new Long(record.getEndTime());
            }
            transcription.setNotifying(false);
            super.execute(tier, args);
            transcription.setNotifying(true);
        }          
    }

    /**
     * Create the new annotation tree.
     */
    @Override
	void newAnnotation() {
        if (node != null) { // annotation tree
            // if the destination (root) tier is not of the same name as the copied root tier name,
            // only paste the root annotation to this new tier, without depending annotations??
            if (record != null) { // record is the root node object
                if (!record.getTierName().equals(destTierName)) {
                    super.newAnnotation();
                    
                    if (newAnnotation != null) {
                        AnnotationRecreator.restoreValueEtc((AbstractAnnotation) newAnnotation, record, false);

                    }
                } else {
                    newAnnotation = AnnotationRecreator.createAnnotationFromTree(transcription, node);
                    
                    if (newAnnotation != null) {
                        newAnnBegin = newAnnotation.getBeginTimeBoundary();
                        newAnnEnd = newAnnotation.getEndTimeBoundary();
                    }
                }
            }
            
        } else if (record != null) { // single annotation
            super.newAnnotation();
            
            if (newAnnotation != null) {
                AnnotationRecreator.restoreValueEtc((AbstractAnnotation) newAnnotation, record, false);
            }
        }
    }
    
    /**
     * Adjusts all aligned begin and end times of the annotations in the tree.
     *  
     * @param node the root of the tree
     * @param shift the amount of milliseconds to shift the annotations with
     */
    private void adjustTimes(DefaultMutableTreeNode root, long shift) {
        if (root == null) {
            return;
        }
        DefaultMutableTreeNode n;
        AnnotationDataRecord adr;
        Enumeration nodeIt = root.breadthFirstEnumeration();
        while (nodeIt.hasMoreElements()) {
            n = (DefaultMutableTreeNode) nodeIt.nextElement();
            adr = (AnnotationDataRecord) n.getUserObject();
            if (adr.getBeginTime() != -1) {
                adr.setBeginTime(adr.getBeginTime() + shift);
            }
            if (adr.getEndTime() != -1) {
                adr.setEndTime(adr.getEndTime() + shift);
            }
        }
    }
 
    /**
     * Performs a check on the accessibility of the system clipboard.
     *
     * @return true if the system clipboard is accessible, false otherwise
     */
    protected boolean canAccessSystemClipboard() {

        if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();

                return true;
            } catch (SecurityException se) {
                LOG.warning("Cannot access the clipboard");

                return false;
            }
        }

        return true;
    }
    
}
