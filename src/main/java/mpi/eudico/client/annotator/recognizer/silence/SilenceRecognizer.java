package mpi.eudico.client.annotator.recognizer.silence;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.AudioSegment;
import mpi.eudico.client.annotator.recognizer.data.MediaDescriptor;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.util.WAVSampler;

/**
 * 
 * @author albertr
 * @updated aarsom, Sep 2012
 *
 */
public class SilenceRecognizer implements Recognizer {
	public final static String NAME = "Silence Recognizer MPI-PL";
	
	private final static int SILENCE = -1;
	private final static int NON_SILENCE = 1;
	private final static String SILENCE_LABEL = "s";
	private final static String NON_SILENCE_LABEL = "x";
	public final static int DEFAULT_SILENCE_DURATION = 400;
	public final static int DEFAULT_NON_SILENCE_DURATION  = 300;
	public final static float DEFAULT_NOISE_THRESHOLD_1 = 400f;// channel 1
	public final static float DEFAULT_NOISE_THRESHOLD_2 = 400f;// channel 2
	public final static double DEFAULT_NOISE_THRESHOLD = 0.05;
	private final static int stepDuration = 20;
	private int nSteps;
	private RecognizerHost host;
	private SilenceRecognizerPanel controlPanel;	
	private String currentMediaFilePath;
	private WAVSampler sampler;
	private int nrOfChannels;
	private int sampleFrequency;
	//private long nrOfSamples;
	private float duration;
	boolean canHandleMedia;
	private long sampleBufferBeginTime;
	private int sampleBufferDuration;
	private boolean keepRunning;
	private float[] averageEnergy1;
	private int[] samples1;
	private float[] averageEnergy2;
	private int[] samples2;
	
	private float noiseThreshold1 = DEFAULT_NOISE_THRESHOLD_1;
	private float noiseThreshold2 = DEFAULT_NOISE_THRESHOLD_2;
	//private boolean noiseThr1Set = false;
	private boolean noiseThr2Set = false;
	private int silenceDur = DEFAULT_SILENCE_DURATION;
	private int nonSilenceDur = DEFAULT_NON_SILENCE_DURATION;
	
	/**
	 * Lightweight constructor, try to do as little as possible here
	 *
	 */
	public SilenceRecognizer() {
		
	}

	/**
	 * Called by RecognizerHost to get a name for this recognizer in the ComboBox with available recognizers
	 * 
	 * @return the name of this recognizer
	 */
	@Override
	public String getName() {
		return NAME;
	}


	/**
	 * Sets the name of the recognizer, which is ignored.
	 * 
	 * @param name ignored.
	 */
	@Override
	public void setName(String name) {
	}
	
	/**
	 * Called by RecognizerHost to get a control panel for this recognizers parameters
	 * 
	 * @return a JPanel with the recognizers GUI controls or null if there are no controls
	 */
	@Override
	public JPanel getControlPanel() {		
		return getSilRecPanel();
	}
	
	@Override
	public void validateParameters() throws RecognizerConfigurationException {
		getSilRecPanel().validateParameters();		
	}
	
	private SilenceRecognizerPanel getSilRecPanel(){
		if (controlPanel == null) {			
			controlPanel= new SilenceRecognizerPanel(host.getSelectionPanel(null));
		}
		return controlPanel;
	}
	
	@Override
	public boolean setMedia(List<String> mediaFilePaths) {
		getSilRecPanel().updateMediaFiles(mediaFilePaths);
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
	 * Fills an array with sample values for a certain time interval and a certain channel.
	 * This method was needed to optimize the WAVReader that is rather slow for small time steps
	 * 
	 * Pad with zeros if you read more samples than available in the media file
	 * 
	 * @param from interval begin time in milliseconds
	 * @param to interval end time in milliseconds
	 * @param channel the audio channel from which the samples must be read
	 * @param samples the int[] array that must be filled with the samples
	 */
	private void getSamples(long from, long to, int channel, int[] samples) {
		try {
			// check if the requested samples are in the buffer
			long sampleBufferEndTime = sampleBufferBeginTime + sampleBufferDuration;
			if (from < sampleBufferBeginTime || from >= sampleBufferEndTime || 
			                                    to < sampleBufferBeginTime || to >= sampleBufferEndTime) {
				sampleBufferDuration = 10000;
				while (to - from > sampleBufferDuration) {
					sampleBufferDuration += 1000;
				}
				int nSamples = (sampleBufferDuration * sampleFrequency) / 1000;
				sampleBufferBeginTime = from;
				sampler.seekTime(sampleBufferBeginTime);
				sampler.readInterval(nSamples, nrOfChannels);
			}
			
			Arrays.fill(samples, 0);
			int srcPos = (int) (((from - sampleBufferBeginTime) * sampleFrequency) / 1000);
			int length = (int) (((to - from) * sampleFrequency) / 1000);
			if (channel == 1) {
				System.arraycopy(sampler.getFirstChannelArray(), srcPos, samples, 0, length);
			} else {
				System.arraycopy(sampler.getSecondChannelArray(), srcPos, samples, 0, length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param samples
	 * @return
	 */
	private float max(int[] samples) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < samples.length; i++) {
			if (max < samples[i]) {
				max = samples[i];
			}
		}
		
		return max;
	}

	/**
	 * 
	 * @param samples
	 * @return
	 */
	private int zeroCrossings(int[] samples) {
		int crossings = 0;
		for (int i = 0; i + 1 < samples.length; i++) {
			if (samples[i] * samples[i + 1] < 0) {
				crossings++;
			}
		}
		
		return crossings;
	}
	
	/**
	 * 
	 * @param samples
	 * @return the root mean square (RMS) of the samples
	 */
	private float averageEnergy(int[] samples) {
		if (samples.length == 0) return 0;
		
		double average = 0;
		for (int i = 0; i < samples.length; i++) {
			int sample = samples[i];
			average += sample * sample;
		}
		
		return (float) Math.sqrt(average / samples.length);
	}
	
	private float averageEnergy(int channel, int step) {
		if (channel == 1) {
			if (averageEnergy1 == null) {
				averageEnergy1 = new float[nSteps];
				for (int i = 0; i < nSteps; i++) {
					averageEnergy1[i] = -1;
				}
				samples1 = new int[(sampleFrequency * stepDuration) / 1000]; 
			}
			if (averageEnergy1[step] < 0) {
				long time = step * stepDuration;
				getSamples(time, time + stepDuration, 1, samples1);
				averageEnergy1[step] = averageEnergy(samples1);
			}
			return averageEnergy1[step];
		} else if (channel == 2) {
			if (averageEnergy2 == null) {
				averageEnergy2 = new float[nSteps];
				for (int i = 0; i < nSteps; i++) {
					averageEnergy2[i] = -1;
				}
				samples2 = new int[(sampleFrequency * stepDuration) / 1000]; 
			}
			if (averageEnergy2[step] < 0) {
				long time = step * stepDuration;
				getSamples(time, time + stepDuration, 2, samples2);
				averageEnergy2[step] = averageEnergy(samples2);
			}
			return averageEnergy2[step];
		} else {
			return 0;
		}
	}
	
	/**
	 * Called by RecognizerHost to start the recognizer
	 *
	 */
	@Override
	public void start() {
		keepRunning = true;
		recog();
	}

	/**
	 * Called by RecognizerHost to stop the recognizer, MUST BE OBEYED AT ALL TIMES
	 *
	 */
	@Override
	public void stop() {
		keepRunning = false;
	}
	
	/**
	 * 
	 * Progress: 10% threshold calculation, 80% stepping frames, calculating averages, 10% pruning and creating segmentations
	 */
	public void recog() {
		currentMediaFilePath = getSilRecPanel().getSelectedMediaFile();
		if(currentMediaFilePath == null){
			System.out.println("No media available");
			stop();
			return;
		}
		averageEnergy1 = null;
		averageEnergy2 = null;
		
		try {
			sampler = new WAVSampler(currentMediaFilePath);
			nrOfChannels = sampler.getWavHeader().getNumberOfChannels();
			//System.out.println("Nr. of channels: " + nrOfChannels);
			sampleFrequency = sampler.getSampleFrequency();
			//nrOfSamples = sampler.getNrOfSamples();
			duration = sampler.getDuration();
			nSteps = (int) (duration / stepDuration);
			sampleBufferBeginTime = -1;
			sampleBufferDuration = 0;
			canHandleMedia = true;
		} catch (Exception e) {
			//e.printStackTrace();
		}		
		
		long curTime = System.currentTimeMillis();
		host.setProgress(0.01f, "Retrieving noise thresholds...");
		
		float maxEnergyChannel1 = Integer.MIN_VALUE;
		float maxEnergyChannel2 = Integer.MIN_VALUE;
		boolean lookAtChannel1 = false;
		boolean lookAtChannel2 = false;
		int[] samples = new int[(sampleFrequency * stepDuration) / 1000]; 
		
		host.setProgress(0.01f, "Loading selection/tier objects...");		
	
		ArrayList<RSelection> selections = null;
		
		if (getSilRecPanel().isNoiseThresholdSetManually()) {
			// If the noise threshold is set manually, calculate the maximum energy over the whole audio track.
			selections = new ArrayList<RSelection>();
			RSelection r = new AudioSegment(0, (int)duration, null, 1);
			selections.add(r);
			if (nrOfChannels > 1) {
				r = new AudioSegment(0, (int)duration, null, 2);
				selections.add(r);
			}
		} else {
			selections = getSilRecPanel().getSelections();
		}
		
		if(selections == null){
			host.appendToReport("No selections available\n");
			//stop();
			host.errorOccurred("No selections available");
			return;
		}
		
		if (selections != null && selections.size() > 0) {
			for (int i = 0; i < selections.size(); i++) {
				RSelection selection = (RSelection) selections.get(i);
				for (long time = selection.beginTime; time < selection.endTime; time += stepDuration) {
					long to = time + stepDuration;
					if (to > selection.endTime) {
						to = selection.endTime;
					}
					if ((selection instanceof AudioSegment) && ((AudioSegment) selection).channel == 2) {
						getSamples(time, to, 2, samples);
						float energy = averageEnergy(samples);
						if (energy > maxEnergyChannel2) {
							maxEnergyChannel2 = energy;
						}
					} else {// all other cases in channel 1
						getSamples(time, to, 1, samples);
						float energy = averageEnergy(samples);
						if (energy > maxEnergyChannel1) {
							maxEnergyChannel1 = energy;
						}
					}
				}
			}
			noiseThreshold1 = maxEnergyChannel1;
			if (maxEnergyChannel2 > Integer.MIN_VALUE) {
				noiseThreshold2 = maxEnergyChannel2;
			}
			lookAtChannel1 = maxEnergyChannel1 > Integer.MIN_VALUE;
			lookAtChannel2 = maxEnergyChannel2 > Integer.MIN_VALUE;
		} else {
			lookAtChannel1 = true;// always process one channel, use the default value
			if (nrOfChannels > 1 && noiseThr2Set) {
				lookAtChannel2 = true;
			}
		}
		
		if (getSilRecPanel().isNoiseThresholdSetManually()) {
			noiseThreshold1 = (float) (maxEnergyChannel1 * getSilRecPanel().getNoiseThreshold());
			noiseThreshold2 = (float) (maxEnergyChannel2 * getSilRecPanel().getNoiseThreshold());	
			
			host.appendToReport("Max energy level 1:\t" + maxEnergyChannel1 + '\n');
			if (lookAtChannel2) {
				host.appendToReport("Max energy level 2:\t" + maxEnergyChannel2 + '\n');
			}
		}
		
		//System.out.println("Threshold calc: " + (System.currentTimeMillis() - curTime));

		host.appendToReport("Noise level 1:\t" + noiseThreshold1 + '\n');
		if (lookAtChannel2) {
			host.appendToReport("Noise level 2:\t" + noiseThreshold2 + "\n\n");
		}
		
		host.appendToReport("Minimal silence duration :\t" + getSilRecPanel().getMinimalSilenceDuration() + " ms\n");
		host.appendToReport("Minimal non-silence duration :\t" + getSilRecPanel().getMinimalNonSilenceDuration() + " ms\n");
//		host.appendToReport("Step duration :\t" + stepDuration + " ms\n");
				
		host.setProgress(0.1f, "Calculating averages per frame....");
		
		int nSteps = (int) (duration / stepDuration);
		int[] steps1 = new int[nSteps];
		int[] steps2 = new int[nSteps];
		for (int step = 0; step < nSteps; step++) {
			long time = step * stepDuration;
			host.setProgress(0.1f + (0.8f * (time / duration)));

			if (lookAtChannel1) {
				//getSamples(time, time + stepDuration, 1, samples);
				//steps1[step] = averageEnergy(samples) < maxEnergyChannel1 ? SILENCE : NON_SILENCE;
				steps1[step] = averageEnergy(1, step) < noiseThreshold1 ? SILENCE : NON_SILENCE;
			}
			if (lookAtChannel2) {
				//getSamples(time, time + stepDuration, 2, samples);
				//steps2[step] = averageEnergy(samples) < maxEnergyChannel2 ? SILENCE : NON_SILENCE;
				steps2[step] = averageEnergy(2, step) < noiseThreshold2 ? SILENCE : NON_SILENCE;
			}
			
			if (!keepRunning) {
				break;
			}
		}
		//System.out.println("Stepping frames: " + (System.currentTimeMillis() - curTime));
		host.setProgress(0.9f, "Pruning segments...");
		// prune 
		if (lookAtChannel1) {
			prune(steps1, stepDuration);
		}
		if (lookAtChannel2) {
			prune(steps2, stepDuration);
		}
		// if keepRunning is still true make sure the progress is set to 1
//		if (keepRunning) {
//			host.setProgress(1);
//		}
		host.setProgress(0.97f, "Creating segmentations...");
		//System.out.println("Pruning: " + (System.currentTimeMillis() - curTime));
		// create the segments
		if (lookAtChannel1) {
			ArrayList<RSelection> segments = createSegmentation(steps1, stepDuration);
			MediaDescriptor descriptor = new MediaDescriptor(currentMediaFilePath, 1);
			Segmentation seg = new Segmentation("Channel1", segments, descriptor);
			host.addSegmentation(seg);
			host.appendToReport("Number of segments channel 1:\t" + segments.size() + '\n');
		}
		if (lookAtChannel2) {
			ArrayList<RSelection> segments = createSegmentation(steps2, stepDuration);
			MediaDescriptor descriptor = new MediaDescriptor(currentMediaFilePath, 2);
			Segmentation seg = new Segmentation("Channel2", segments, descriptor);
			host.addSegmentation(seg);
			host.appendToReport("Number of segments channel 2:\t" + segments.size() + '\n');
		}
		host.appendToReport("\nProcessing took:\t" + (System.currentTimeMillis() - curTime) + " ms\n");
		
		// if keepRunning is still true make sure the progress is set to 1
		if (keepRunning) {
			host.setProgress(1);
		}
		//System.out.println("Recognizing: " + (System.currentTimeMillis() - curTime));
	}

	private void prune(int[] steps, int stepDuration) {
		//     S  NS  S  ->  S  S  S
		for (int step = 1; step + 1 < steps.length; step++) {
			if (steps[step] > 0 && steps[step - 1] < 0 && steps[step + 1] < 0) {
				steps[step] = -1;
			}
		}
		
		//    S  S  NS  NS  S  S  ->  S  S  S  S  S  S
		for (int step = 2; step + 3 < steps.length; step++) {
			if (steps[step] > 0 && steps[step + 1] > 0 &&
					steps[step - 1] < 0 && steps[step - 2] < 0 &&
					steps[step + 2] < 0 && steps[step + 3] < 0) {
				steps[step] = -1;
				steps[step + 1] = -1;
			}
		}
		
		//  NS  S  NS  ->  NS  NS  NS
		for (int step = 1; step + 1 < steps.length; step++) {
			if (steps[step] < 0 && steps[step - 1] > 0 && steps[step + 1] > 0) {
				steps[step] = 1;
			}
		}
		
		//  NS  NS  S  S  NS  NS  ->  NS  NS  NS  NS  NS  NS
		for (int step = 2; step + 3 < steps.length; step++) {
			if (steps[step] < 0 && steps[step + 1] < 0 &&
					steps[step - 1] > 0 && steps[step - 2] > 0 &&
					steps[step + 2] > 0 && steps[step + 3] > 0) {
				steps[step] = 1;
				steps[step + 1] = 1;
			}
		}
		
		// remove NON_SILENCE patterns that are too short
		int minimalNonSilenceSteps = 1 + nonSilenceDur / stepDuration;
		minimalNonSilenceSteps = 1 + getSilRecPanel().getMinimalNonSilenceDuration() / stepDuration;
		
		for (int step = 0; step < steps.length; step++) {
			if (steps[step] >= NON_SILENCE) {
				int to = step + 1;
				while (to < steps.length && steps[to] >= NON_SILENCE) {
					to++;
				}
				if (to - step < minimalNonSilenceSteps) {
					for (int j = step; j < to; j++) {
						steps[j] = SILENCE;
					}
				}
				step = to - 1;
			}
		}
		
		// remove SILENCE patterns that are too short
		int minimalSilenceSteps = 1 + silenceDur / stepDuration;
		minimalSilenceSteps = 1 + getSilRecPanel().getMinimalSilenceDuration() / stepDuration;
		
		for (int step = 0; step < steps.length; step++) {
			if (steps[step] <= SILENCE) {
				int to = step + 1;
				while (to < steps.length && steps[to] <= SILENCE) {
					to++;
				}
				if (to - step < minimalSilenceSteps) {
					for (int j = step; j < to; j++) {
						steps[j] = NON_SILENCE;
					}
				}
				step = to - 1;
			}
		}
	}

	private ArrayList<RSelection> createSegmentation(int[] steps, int stepDuration) {
		ArrayList<RSelection> segments = new ArrayList<RSelection>();
		Segment segment = new Segment();
		segment.beginTime = 0;
		int current = steps[0];
		if (current <= SILENCE) {
			segment.label = SILENCE_LABEL;
		} else {
			segment.label = NON_SILENCE_LABEL;
		}
		for (int step = 1; step < steps.length; step++) {
			if (steps[step] != current) {
				segment.endTime = step * stepDuration;
				segments.add(segment);
				segment = new Segment();
				segment.beginTime = step * stepDuration;
				current = steps[step];
				if (current <= SILENCE) {
					segment.label = SILENCE_LABEL;
				} else {
					segment.label = NON_SILENCE_LABEL;
				}
			}
		}
		segment.endTime = (long) duration;
		segments.add(segment);	
		return segments;
	}
	

	@Override
	public void updateLocale(Locale locale) {		
		getSilRecPanel().updateLocale(locale);
	}
	

	@Override
	public void updateLocaleBundle(ResourceBundle bundle) {
		getSilRecPanel().updateLocaleBundle(bundle);
	}

	@Override
	public boolean canCombineMultipleFiles() {
		return false;
	}

	@Override
	public boolean canHandleMedia(String mediaFilePath) {
		if (mediaFilePath == null) {
			return false;
		}
		
		try {
			WAVSampler wavs = new WAVSampler(mediaFilePath);
			int nc = wavs.getWavHeader().getNumberOfChannels();
			if (nc == 0) {
				return false;
			}
		} catch(IOException ioe) {
			return false;
		} catch (Exception exc) {
			return false;
		}
		return true;
	}

	@Override
	public int getRecognizerType() {
		return Recognizer.AUDIO_TYPE;
	}

	/**
	 * For text and file parameters.
	 */
	@Override
	public void setParameterValue(String param, String value) {
		// stub
		
	}

	@Override
	public void setParameterValue(String param, float value) {
		if (param == null) {
			return;
		}
		if (param.equals("threshold_1")) {
			noiseThreshold1 = value;
			//noiseThr1Set = true;
		} else if (param.equals("threshold_2")) {
			noiseThreshold2 = value;
			noiseThr2Set = true;
		} else if (param.equals("silence_dur")) {
			silenceDur = (int) value;
		} else if (param.equals("non_silence_dur")) {
			nonSilenceDur = (int) value;
		}
	}
	
	@Override
	public Object getParameterValue(String param) {
		if (param == null) {
			return null;
		}
		if (param.equals("threshold_1")) {
			return new Float(noiseThreshold1);
		} else if (param.equals("threshold_2")) {
			return new Float(noiseThreshold2);
		} else if (param.equals("silence_dur")) {
			return new Float(silenceDur);
		} else if (param.equals("non_silence_dur")) {
			return new Float(nonSilenceDur);
		}
		
		return null;
	}

	@Override
	public void dispose() {
		controlPanel = null;
		averageEnergy1 = null;
		samples1 = null;
		averageEnergy2 = null;
		samples2 = null;
		host = null;
	}

}