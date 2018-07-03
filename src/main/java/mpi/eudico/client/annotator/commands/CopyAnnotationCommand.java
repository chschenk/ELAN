package mpi.eudico.client.annotator.commands;

import java.awt.Toolkit;
import java.util.List;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.AnnotationDataRecord;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.TransferableAnnotation;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

/**
 * A Command to copy an annotation (i.e. a transferable AnnotationDataRecord) to the System's 
 * Clipboard.
 */
public class CopyAnnotationCommand implements Command, ClientLogger {
    private String commandName;
    
    /**
     * Creates a new CopyAnnotationCommand instance
     * 
     * @param name the name of the command
     */
    public CopyAnnotationCommand(String name) {
        commandName = name;
    }
    
    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver null
     * @param arguments the arguments:  <ul><li>arg[0] = the (active) annotation
     *        (Annotation)</li></ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        if (arguments[0] instanceof Annotation) {
            AnnotationDataRecord record = new AnnotationDataRecord((Annotation) arguments[0]);
            TransferableAnnotation ta = new TransferableAnnotation(record);
           
            /* HS March 2015, added code to allow citation that includes the media file rather than the transcription file */
            String copyOption = Preferences.getString("EditingPanel.CopyOption", null);        
            // string should be a non-locale string, a string from Constants        
    		if (copyOption != null) {
    		    if (copyOption.equals(Constants.CITE_STRING)) {
    		    	String mediaFile = getFirstMedia((Annotation) arguments[0]);
    		    	if (mediaFile != null) {
    		    		record.setFilePath(mediaFile);
    		    	}
    		    }
    		}
    		
            if (canAccessSystemClipboard()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ta, ta);
            }
        }

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
                LOG.warning("Cannot copy, cannot access the clipboard.");

                return false;
            }
        }

        return true;
    }
    
    /**
     * Returns the first media file linked to the transcription or null if there isn't one.
     * 
     * @param anno the annotation
     * @return the file path or url of the first media file
     */
    private String getFirstMedia(Annotation anno) {
    	List<MediaDescriptor> md = ((TierImpl) anno.getTier()).getTranscription().getMediaDescriptors();
    	if (md != null && md.size() > 0) {
    		return md.get(0).mediaURL;
    	}
    	
    	return null;
    }

}
