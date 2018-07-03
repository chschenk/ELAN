package mpi.eudico.client.annotator.util;

import java.awt.datatransfer.DataFlavor;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A DataFlavor for annotation tree DefaultMutableTreeNodes  (Singleton).
 */
public class AnnotationTreeDataFlavor extends DataFlavor implements
        ClientLogger {
    private static AnnotationTreeDataFlavor flavor = null;
    
    private AnnotationTreeDataFlavor() throws ClassNotFoundException {
        super(DataFlavor.javaSerializedObjectMimeType + ";class=" + 
                DefaultMutableTreeNode.class.getName());
    }
    
    /**
     * Returns the single instance of AnnotationTreeDataFlavor.
     * 
     * @return the single instance of AnnotationTreeDataFlavor
     */
    public static AnnotationTreeDataFlavor getInstance() {
        if (flavor == null) {
            createFlavor();
        }
        
        return flavor;
    }
    
    private static void createFlavor() {
        try {
            flavor = new AnnotationTreeDataFlavor();
        } catch (ClassNotFoundException cnfe) {
            LOG.warning("Flavor class not found: " + cnfe.getMessage());
        }
    }

}
