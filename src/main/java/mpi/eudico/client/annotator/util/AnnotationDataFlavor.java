package mpi.eudico.client.annotator.util;

import java.awt.datatransfer.DataFlavor;

/**
 * A DataFlavor for AnnotationDataRecords (Singleton).
 */
public class AnnotationDataFlavor extends DataFlavor implements ClientLogger {
    private static AnnotationDataFlavor flavor = null;
    
    private AnnotationDataFlavor() throws ClassNotFoundException {
        super(DataFlavor.javaSerializedObjectMimeType + ";class=" + 
                    AnnotationDataRecord.class.getName());
    }
    
    public static AnnotationDataFlavor getInstance() {
        if (flavor == null) {
            createFlavor();
        }
        
        return flavor;
    }
    
    private static void createFlavor() {
        try {
            flavor = new AnnotationDataFlavor();
        } catch (ClassNotFoundException cnfe) {
            LOG.warning("Flavor class not found: " + cnfe.getMessage());
        }
    }
}
