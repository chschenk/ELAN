package mpi.eudico.client.annotator.recognizer.api;

import java.util.List;

import mpi.eudico.client.annotator.recognizer.data.Segmentation;

/**
 * An interface to be used by Recognizers. It allows them to provide progress
 * reports and results. It can also provide them with information about the
 * data they are supposed to process. 
 * 
 * @version 4 Jan/June 2015 
 * - added appendToReport
 * - introduction of AbstractTierSelectionPanel as return type of getSelectionPanel()
 */
public interface RecognizerHost {
	/**
	 * Called by a Recognizer to send a Segmentation object to the RecognizerHost
	 * More than one Segmentation can be sent.
	 * 
	 * @param segmentation the Segmentation produced by the Recognizer.
	 */
	public void addSegmentation(Segmentation segmentation);
	
	/**
	 * Periodically called by a Recognizer to inform the RecognizerHost 
	 * about the progress of the recognition task.
	 * 
	 * @param progress a float between 0.0 and 1.0 where 1.0 means that the recognizer has finished processing.
	 */
	public void setProgress(float progress);
	
	/**
	 * Periodically called by a Recognizer to inform the RecognizerHost 
	 * about the progress of the recognition task.
	 * 
	 * @param progress a float between 0.0 and 1.0 where 1.0 means that the recognizer has finished processing.
	 * @param message a progress message
	 */
	public void setProgress(float progress, String message);
	
	/**
	 * Can be called by a recognizer to signal that a fatal error occurred and the recognition task stopped
	 * 
	 * @param message a description of the error
	 */
	public void errorOccurred(String message);
	
	/**
	 * Can be called by a recognizer to report some debugging or progress status
	 * information.
	 * The RecognizerHost may collect and display this information to the user.
	 * 
	 * @version Added in version 2.
	 * @param message a message to display to the user
	 */
	public void appendToReport(String message);
	
	/**
	 * Returns a tier selection panel mainly used by the
	 * java plug-in recognizers
	 * 
	 * @param param name for which the tierSelectionpanel is used
	 * @return
	 */
	public AbstractSelectionPanel getSelectionPanel(String paramName);
	
	/**
	 * Returns a list of media files of the given type
	 * ({@link Recognizer.AUDIO_TYPE} etc).
	 * To be potentially called by recognizers.
	 * @version Added in version 2.
	 */
	 public List<String> getMediaFiles(int mode);
	 
	/**
	 * Called by ELAN to get the recognition result.
	 * 
	 * @version Version 2 changes the return type to List&lt;Segmentation>
	 * (was ArrayList&lt;Segmentation>).
	 * @return an List with Segmentation objects.
	 */
	public List<Segmentation> getSegmentations();
	
	/**
	 * Tells ELAN if there is one or more recognizer busy
	 * 
	 * @return true if there is some recognizing going on
	 */
	public boolean isBusy();
}