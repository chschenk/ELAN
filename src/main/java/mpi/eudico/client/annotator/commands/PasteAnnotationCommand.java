package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import mpi.eudico.client.annotator.util.AnnotationDataFlavor;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.AnnotationRecreator;
import mpi.eudico.client.annotator.util.AnnotationTreeDataFlavor;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A Command to paste an annotation (i.e. a transferable AnnotationDataRecord) from the System's 
 * Clipboard.
 * 
 * Note June 2006 transferal of application/x-java-serialized-object objects between jvm's 
 * doesn't seem to work on Mac OS X. The Transferable sun.awt.datatransfer.ClipboardTransferable@8b4cb8 
 * doesn't support any flavor with human presentable name "application/x-java-serialized-object" 
 */
public class PasteAnnotationCommand extends NewAnnotationCommand implements ClientLogger {
    private AnnotationDataRecord record;
    private String destTierName;
    private Long newBeginTime;
    
    /**
     * Creates a new PasteAnnotationCommand instance
     * 
     * @param name the name of the command
     */
    public PasteAnnotationCommand(String name) {
        super(name);
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#undo()
     */
    @Override
	public void undo() {
        super.undo();
    }
    
    /**
     * @see mpi.eudico.client.annotator.commands.UndoableCommand#redo()
     */
    @Override
	public void redo() {
        super.redo();
        
        if (newAnnotation != null && record != null) {
            AnnotationRecreator.restoreValueEtc((AbstractAnnotation) newAnnotation, record, false);
        }
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct. The arguments parameter can be null, of length 1 or of length 2<br>
     * If the arguments parameter is null the following order is used to decide on which tier the 
     * annotation has te be pasted:<br>
     * - the tier of the same name as in the copied annotation record, if present in the destination transcription<br>
     * - the current active tier
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
        TranscriptionImpl tr = (TranscriptionImpl) receiver;
        if (canAccessSystemClipboard()) {
            Object contents = null;
            Transferable ta;
            
            // get Clipboard contents.
            ta = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            try {
                contents = ta.getTransferData(AnnotationDataFlavor.getInstance());
            } catch (UnsupportedFlavorException nfe) {
                // check if there is an annotation tree on the clipboard 
                try {
                    contents = ta.getTransferData(AnnotationTreeDataFlavor.getInstance());
                } catch (Exception e) {
                    LOG.warning("Unsupported flavor on the clipboord: " + nfe.getMessage());
                    return;
                }
                /* could use a String representation as alternative?
                try {
                    contents = ta.getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    LOG.warning("No text on clipbaard: " + e.getMessage());
                    return;
                }
                */
                //return;
            } catch (IOException ioe) {
                LOG.warning("I/O error: " + ioe.getMessage());
                return;
            }
            
            if (contents instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) contents;
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
                }
            }
            TierImpl tier = null;
            if (destTierName != null) {
                tier = (TierImpl) tr.getTierWithId(destTierName);
            }
            // if the tier is not found try the active tier
            if (tier == null) {
                tier = (TierImpl) ELANCommandFactory.getViewerManager(tr).getMultiTierControlPanel().getActiveTier();
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
                if (tier.isTimeAlignable()) {
                    args[0] = newBeginTime;
                	   args[1] = new Long(newBeginTime.longValue() + (record.getEndTime() - record.getBeginTime()));
                } else {
                    args[0] = newBeginTime;
                    args[1] = newBeginTime;
                }
            } else {
                if (tier.isTimeAlignable()) {
                    args[0] = new Long(record.getBeginTime());
                    args[1] = new Long(record.getEndTime());
                } else {
                    long mid = (record.getBeginTime() + record.getEndTime()) / 2;
                    args[0] = new Long(mid);
                    args[1] = args[0];
                }

            }
            super.execute(tier, args);
            
            // HS July 2012: after the actions above the focus is completely lost, 
            // none of the keyboard shortcuts seem to function
            // try to correct this by giving the frame the focus
    		// see also DuplicateAnnotation, MoveAnnotation
            SwingUtilities.invokeLater(new Runnable(){
                @Override
				public void run() {
                    ELANCommandFactory.getRootFrame(transcription).requestFocus();
                }
            });
        }

    }
 
    /**
     * The creation of the new annotation in a separate method to allow overriding.
     */
    @Override
	void newAnnotation() {
        super.newAnnotation();
        
        if (newAnnotation != null) {
            AnnotationRecreator.restoreValueEtc((AbstractAnnotation) newAnnotation, record, false);
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
                //se.printStackTrace();

                return false;
            }
        }

        return true;
    }

}
