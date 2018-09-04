package nl.mpi.recognizer.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.MediaDescriptor;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;

public class DemoRecognizer implements Recognizer { 
	private static String EVEN_LABEL = "E";
	private static String UNEVEN_LABEL = "U";
	private RecognizerHost host;
	private DemoRecognizerPanel controlPanel;
	private boolean keepRunning;
	private String currentMediaFilePath;
	float duration = 12000f;
	private String name = "Demo Recognizer";
	
	/**
	 * Lightweight constructor, try to do as little as possible here
	 *
	 */
	public DemoRecognizer() {
		
	}

	/**
	 * Called by RecognizerHost to get a name for this recognizer in the ComboBox with available recognizers
	 * 
	 * @return the name of this recognizer
	 */
	@Override
	public String getName() {
		// make sure this name is unique among the other recognizers!
		return name;
	}

	/**
	 * Called by RecognizerHost to get a control panel for this recognizers parameters
	 * 
	 * @return a JPanel with the recognizers GUI controls or null if there are no controls
	 */
	@Override
	public JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new DemoRecognizerPanel(host.getSelectionPanel(null), host);
		}
		return controlPanel;
	}
	
	/**
	 * Called by RecognizerHost to set the media files for this recognizer.
	 * This would be the proper moment to retrieve media files of other types
	 * than the main media type from the host
	 * 
	 * @param mediaFilePaths list of full path of media files 
	 * @return true 
	 */
	@Override
	public boolean setMedia(List<String> mediaFilePaths) {
		((DemoRecognizerPanel)getControlPanel()).updateMediaFiles(mediaFilePaths);
		return true;
	}
	
	/**
	 * Called by RecognizerHost to give this recognizer an object for callbacks
	 * 
	 * @param host the RecognizerHost that talks with this recognizer
	 */
	@Override
	public void setRecognizerHost(RecognizerHost host) {
		this.host = host;
	}

		
	/**
	 * Called by RecognizerHost to start the recognizer
	 *
	 */
	@Override
	public void start() {
		keepRunning = true;
		host.appendToReport("Recognizer: " + name + " starting...\n");
		recog();
	}

	/**
	 * Called by RecognizerHost to stop the recognizer, MUST BE OBEYED AT ALL TIMES
	 *
	 */
	@Override
	public void stop() {
		keepRunning = false;
		host.appendToReport("Recognizer: " + name + " stopped...\n");
	}
	
	/**
	 * Code that implements the actual recognition task
	 * This is only a demo that sets a segment at the interval
	 * as defined by the slider on the DemoRecognizerPanel
	 *
	 */
	protected void recog() {
		long start = -1;
		long end = -1;
		// a real recognizer could get the example selections and process them
		// look for a real example at the ELAN source file
		// mpi.eudico.client.annotator.recognizer.silence.SilenceRecognizer.java	

		currentMediaFilePath = controlPanel.getSelectedMediaFile();
		
		ArrayList<RSelection> selections = controlPanel.getSelections();
		if (selections != null) {
			for (int i = 0; i < selections.size(); i++) {
				RSelection selection = (RSelection) selections.get(i);
				// do something interesting with the selection to inform the 
				// recognition algorithm about the patterns it is supposed to find
				
				// here get the lowest time value and create segments from there?
				if (start == -1) {
					start = selection.beginTime;
				} else if (selection.beginTime < start){
					start = selection.beginTime;
				}
				if (selection.endTime > end) {
					end = selection.endTime;
				}
			}
		}
		
		start = start < 0 ? 0 : start;
		end = end < start + 12000 ? start + 12000 : end;
		host.appendToReport("Creating segments in interval: " + start + " - " + end + "\n");
		duration = end - start;
		ArrayList<RSelection> segments = new ArrayList<RSelection>();
		int stepDuration = 1000 * controlPanel.getStepDuration(); // time in milliseconds
		host.appendToReport("Segment size in ms.: " + stepDuration + "\n");
		int nSteps = (int) (duration / stepDuration);
		if (nSteps < 0) {
			nSteps = 1;
		}
		float perStep = 1f / nSteps;
		for (int step = 0; step < nSteps; step++) {
			long time = start + step * stepDuration;
			// inform the host about the progress we are making
			host.setProgress(time / duration);
			
			// add a dummy segment
			Segment segment = new Segment();
			segment.beginTime = time;
			segment.endTime = time + stepDuration;
			segment.label = step % 2 == 0 ? EVEN_LABEL : UNEVEN_LABEL;
			segments.add(segment);
			
			// sleep a while to make it look more interesting
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			
			}
			host.setProgress(step * perStep);
			if (!keepRunning) {
				break;
			}
		}
		host.appendToReport("Number of segments created: " + segments.size() + "\n");
		// if keepRunning is still true make sure the progress is set to 1
		if (keepRunning) {
			host.setProgress(1);
		}
		
		// give the resulting segmentation to the host
		MediaDescriptor descriptor = new MediaDescriptor(currentMediaFilePath, 1);
		Segmentation seg = new Segmentation("DEMO", segments, descriptor);
		host.addSegmentation(seg);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public void updateLocale(Locale locale) {
		// optional to implement, usually english GUI elements are ok.
	}

	/**
	 * A change in the application's language is now advertised by
	 * providing the language resource strings of the application (ELAN)
	 */
	@Override
	public void updateLocaleBundle(ResourceBundle bundle) {
		// could localize the panel for this recognizer
	}
	
	@Override
	public boolean canCombineMultipleFiles() {
		return false;
	}

	/**
	 * Always returns true, because the media file is not actually used in this demo
	 */
	@Override
	public boolean canHandleMedia(String mediaFilePath) {
		return true;
	}

	@Override
	public void dispose() {
		controlPanel = null;
	}

	@Override
	public Object getParameterValue(String param) {
		// will only be called if the control panel is not an instance of ParamPreferences
		if ("StepDuration".equals(param)) {
			if (controlPanel != null) {
				return Integer.valueOf(controlPanel.getStepDuration());
			}
		}
		
		return null;
	}

	/**
	 * This demo recognizer doesn't do anything really with the media file, 
	 * but acts like it is an Audio type.
	 * @return Recognizer.AUDIO_TYPE
	 */
	@Override
	public int getRecognizerType() {
		return Recognizer.AUDIO_TYPE;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		
	}

	@Override
	public void setParameterValue(String param, String value) {
		
	}

	@Override
	public void setParameterValue(String param, float value) {
		if ("StepDuration".equals(param)) {
			if (controlPanel != null) {
				controlPanel.setStepDuration((int) value);
			}
		}
	}
	
	@Override
	public void validateParameters() throws RecognizerConfigurationException {
		// Here a check can be performed if all necessary parameters have been set and have
		// valid values. This method is called before the process is actually started.
	}

}
