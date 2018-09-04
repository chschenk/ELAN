package mpi.eudico.client.annotator.util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A transerable annotation tree, using a DefaultMutableTreeNode as the transferable object.
 * An annotation tree is an annotation with its depending or descendant annotations.
 * 
 * Note June 2006 transferal of application/x-java-serialized-object objects between jvm's 
 * doesn't seem to work on Mac OS X. The Transferable sun.awt.datatransfer.ClipboardTransferable 
 * doesn't support any flavor with human presentable name "application/x-java-serialized-object" 
 */
public class TransferableAnnotationTree implements Transferable, ClipboardOwner {
    private DefaultMutableTreeNode node;
    private final static DataFlavor[] flavors = new DataFlavor[]{AnnotationTreeDataFlavor.getInstance()};
    
    /**
     * Creates a new TransferableAnnotationTree
     * 
     * @param node the object for transfer
     */
    public TransferableAnnotationTree(DefaultMutableTreeNode node) {
        if (node == null) {
            throw new NullPointerException("Annotation TreeNode is null.");
        }
        
        this.node = node;
    }
    
    /**
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    @Override
	public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    @Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor == null) {
            return false; // could throw NullPointer Exc.
        }
        
        return flavor.equals(flavors[0]);
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    @Override
	public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        /* as long as there is one supported flavor the test below is always true if no exception 
         has been thrown above
        if (flavor.equals(flavors[0])) {
            return node;
        }
        */
        return node;
    }

    /**
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    @Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
        node = null;
    }

}
