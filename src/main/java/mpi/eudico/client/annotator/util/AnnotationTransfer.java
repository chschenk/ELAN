package mpi.eudico.client.annotator.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;

/**
 * Utility class with methods to check the accessibility of the Clipboard and the 
 * type of contents on the clipboard.
 * 
 * @author Han Sloetjes, MPI
 */
public class AnnotationTransfer implements ClientLogger {

	/**
	 * Constructor.
	 */
	AnnotationTransfer() {
		super();
	}

    /**
     * Checks the contents of the clipboard. 
     * 
     * @return true if there is valid contents on the clipboard 
     * (TransferableAnnotation or TransferableAnnotationTree),
     * false otherwise
     */
    public static boolean validContentsOnClipboard() {
        if (canAccessSystemClipboard()) {
            try { 
                // get Clipboard contents.
                Transferable ta = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if (ta != null) {
                    try {
                        ta.getTransferData(AnnotationDataFlavor.getInstance()) ;
                        return true;
                    } catch (Exception ee) {
                        try {
                            ta.getTransferData(AnnotationTreeDataFlavor.getInstance());
                            return true;
                        } catch (Exception eee){}
                    }
                }
            } catch (Exception ex) {}
        }
        return false;
    }
    
    /**
     * Performs a check on the accessibility of the system clipboard.
     *
     * @return true if the system clipboard is accessible, false otherwise
     */
    public static boolean canAccessSystemClipboard() {

        if (System.getSecurityManager() != null) {
            try {
                System.getSecurityManager().checkSystemClipboardAccess();

                return true;
            } catch (SecurityException se) {
            	LOG.warning(se.getMessage());
                //se.printStackTrace();

                return false;
            }
        }

        return true;
    }
    
}
