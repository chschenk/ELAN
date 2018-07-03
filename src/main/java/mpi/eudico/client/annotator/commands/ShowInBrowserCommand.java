package mpi.eudico.client.annotator.commands;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.UrlOpener;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;

/**
 * A command to open a URL in the default browser. 
 * The URL is (or should be) retrieved from the external references of an annotation.
 * 
 * Note: the current implementation is based on very specific assumptions concerning
 * external CV's and the format of an entry's value and id.  
 */
public class ShowInBrowserCommand implements Command {
	private String commandName;
	
	/**
     * Creates a new ShowInBrowserCommand instance
     * 
     * The idea is that each ECV entry has a link to an external reference which is a base url. 
     * 
     * By concatenating the entry ID to the end of the base url, one becomes the direct url to an entry. 
     * 
     * @param name the name of the command
     */
    public ShowInBrowserCommand(String name) {
        commandName = name;
    }

	@Override
	public void execute(Object receiver, Object[] arguments) {
		if (arguments[0] instanceof Annotation) {
			Annotation annotation = (Annotation) arguments[0];
			if (hasBrowserLinkInECV(annotation)) {
				String entryId = annotation.getCVEntryId();
				
				// The following bit is for removing the 'gloss' prefix
		    	// TODO Remove this
		    	if(entryId.startsWith("gloss")) {
		    		entryId = entryId.substring(5);
		    	}
				
				String cvName = annotation.getTier().getLinguisticType().getControlledVocabularyName();
				ExternalCV ecv = (ExternalCV) annotation.getTier().getTranscription().getControlledVocabulary(cvName);
				
				CVEntry cvEntry = ecv.getEntrybyId(entryId);
				ExternalReference extRef = cvEntry.getExternalRef();
				if (extRef.getReferenceType() == ExternalReference.RESOURCE_URL) {
					String baseUrlString = extRef.getValue();
					if(!baseUrlString.endsWith("/")) {
						baseUrlString = baseUrlString + "/";
					}
					String urlString = baseUrlString + entryId;
					ClientLogger.LOG.info(urlString);
					
					try {
						new URL(urlString); //For testing whether the url string is a correct url
						UrlOpener.openUrl(urlString);
					} catch (MalformedURLException e) {
						if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
			            	ClientLogger.LOG.warning("The url " + urlString + " is malformed (" + e.getMessage() + ")");
			            }
					} catch (Exception e) {
						if(ClientLogger.LOG.isLoggable(Level.WARNING)) {
			            	ClientLogger.LOG.warning("The url " + urlString + " could not be opened (" + e.getMessage() + ")");
			            }
					}
				}
			}
		}

	}

	@Override
	public String getName() {
		return commandName;
	}

	/**
	 * Checks whether the ECV for this annotation has an ECV entry with a url
	 * @param annotation
	 * @return
	 */
	public static Boolean hasBrowserLinkInECV(Annotation annotation) {
		String cvName = annotation.getTier().getLinguisticType().getControlledVocabularyName();
		if(cvName != null) {
			ControlledVocabulary cv = annotation.getTier().getTranscription().getControlledVocabulary(cvName);
			if(cv instanceof ExternalCV) {
				ExternalCV ecv = (ExternalCV) cv;
				
				CVEntry cvEntry = ecv.getEntries()[0];
				ExternalReference extRef = cvEntry.getExternalRef();
				if (extRef != null && extRef.getReferenceType() == ExternalReference.RESOURCE_URL) {
					String baseUrlString = extRef.getValue();
					if (baseUrlString != null && !baseUrlString.equals("")) {
						return true;
					} 
				}
			}
		}
		return false;
	}
}
