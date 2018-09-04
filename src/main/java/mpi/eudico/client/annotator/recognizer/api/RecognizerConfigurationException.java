package mpi.eudico.client.annotator.recognizer.api;

/**
 * Recognizer configuration exception. Thrown when a recognizer is about to be started but
 * has not been properly configured by the user. 
 * 
 * @author aarsom
 *
 */
@SuppressWarnings("serial")
public class RecognizerConfigurationException extends Exception{
	
	 public RecognizerConfigurationException(String message) {
			super(message);
	 }

}
