package mpi.eudico.client.annotator.player;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.UnsupportedAudioFileException;

import nl.mpi.jsound.NavigableAudioPlayer;
import nl.mpi.jsound.StreamingPlayer;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.ControllerManager;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import static mpi.eudico.client.annotator.util.ClientLogger.LOG;
/* E.g.
 Audio Format:
Number of channels: 2
Sample rate: 48000.0
Frame rate: 48000.0
Frame size: 4
Sample size in bits: 16
Format encoding: PCM_SIGNED

Clip:
Duration in Frames: 70005121
Duration in Microseconds: 1458440020
Microseconds per Frame: 20
Buffer size: 192000

DataLine:
Number available bytes: 192000
Get buffer size: 192000
Frame position (int): 0
Frame position (long): 0
Micro-second position: 0
Level: -1.0

Controls: 
Control: Master Gain
	Value: 0.0
	Max-Min: 6.0206--80.0
	Precision 0.625
	Units: dB
Control: Mute
	Label: True
	Value: false
Control: Balance
	Value: 0.0
	Max-Min: 1.0--1.0
	Precision 0.0078125
	Units: 
Control: Pan
	Value: 0.0
	Max-Min: 1.0--1.0
	Precision 0.0078125
	Units: 
 */
/**
 * An audio-only media player based on the javax.sound.sampled packages.
 * Supports WAVE files (and AU, SND, AIFF)
 * 
 * @author Han Sloetjes
 */
public class JavaSoundPlayer extends ControllerManager implements ElanMediaPlayer, ControllerListener {
	private Clip playerClip;
	private AudioInputStream audioInStream;
	private FloatControl gainControl;// volume control
	private FloatControl rateControl;// play back rate control
	private BooleanControl muteControl;// mute control
	// private FloatControl balanceControl, panControl;
	private LineListener jspLineListener;
	private PeriodicUpdateController intervalEndController;
	
	private MediaDescriptor mediaDescriptor;
	private long offset = 0L;
	private boolean playing;
	private boolean playingInterval;
	private float curVolume = 1.0f;
	private float curSubVolume = 1.0f;
	private float curRate = 1.0f;
	private boolean muted = false;
	//private float balance = 0.0f;
	private final long MIC_TO_MIL = 1000L;
	private double milliSecondsPerSample = 0.0d;
	private double microsecondsPerFrame; // as calculated from the player properties
	private long stopTime = 0;
	
	// "stream" here means any situation where not all data is loaded into memory,
	// so it could just be a local file. The default Clip implementation loads all data into memory
	private boolean streamFile = true;
	// currently always true
	private boolean localFile = true;
	
	/**
	 * Constructor, currently only the file:/ variant is anticipated.
	 * @param mediaDescriptor the media descriptor containing the media file path
	 * @throws NoPlayerException when creating a Clip fails, for whatever reason 
	 * (e.g. IO or UnsupportedAudio)
	 */
	public JavaSoundPlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException {
		if (mediaDescriptor == null) {
			throw new NoPlayerException(ElanLocale.getString("MediaPlayer.Message.NoMediaDescriptor"));
		}
		this.mediaDescriptor = mediaDescriptor;
		offset = mediaDescriptor.timeOrigin;
		String mediaPath = mediaDescriptor.mediaURL;
		if (mediaPath.startsWith("file:")) {
			mediaPath = mediaPath.substring(5);
		}
		
		if (System.getProperty("JavaSoundUseDefaultClip") != null) {
			streamFile = false;
		}
		
		try {			
			// if the descriptor has the file protocol
			File f = new File (mediaPath);
			InputStream is = new BufferedInputStream(new FileInputStream(f));
			// else if it is a http(s) protocol
			//...
			audioInStream = AudioSystem.getAudioInputStream(is);
			AudioFormat format = audioInStream.getFormat();
			if (!streamFile) {
				// two ways to initialize a Clip, both load the entire file into memory
				playerClip = AudioSystem.getClip();
				playerClip.open(audioInStream);
				// or
				    //DataLine.Info info = new DataLine.Info(Clip.class, format);
				    //playerClip = (Clip) AudioSystem.getLine(info);
				    //playerClip.open(audioInStream);
				
				try {
					audioInStream.close();
				} catch(Throwable t) {
//					t.printStackTrace();// ignore
				}
			} else {
				if (!localFile) {
					playerClip = new StreamingPlayer(audioInStream);
				} else {
					playerClip = new NavigableAudioPlayer(audioInStream, f);
					try {
						audioInStream.close();
					} catch(Throwable t) {
//						t.printStackTrace();//ignore
					}
				}
				
			}
			
			printClip(playerClip);
			printFormat(format);
			detectControls(playerClip);
			microsecondsPerFrame = playerClip.getMicrosecondLength() / 
					(double) playerClip.getFrameLength();
			jspLineListener = new JSPLineListener();
			playerClip.addLineListener(jspLineListener);
		} catch (IOException ioe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create sound player, IO error: " + ioe.getMessage());
			}
			throw new NoPlayerException(String.format("MediaPlayer.Message.CannotPlay", "JavaSound") + 
					String.format(": \n", ioe.getMessage()));
		} catch (UnsupportedAudioFileException uafe) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create sound player, unsupported file: " + uafe.getMessage());
			}
			throw new NoPlayerException(String.format("MediaPlayer.Message.UnsupportedFile", "JavaSound") + 
					String.format(": \n", uafe.getMessage()));
		} catch (Throwable t) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.warning("Cannot create sound player, error: " + t.getMessage());
			}
			throw new NoPlayerException(String.format("MediaPlayer.Message.CannotPlay", "JavaSound") + 
					String.format(": \n", t.getMessage()));
		}
	}
	
// some methods to print some information to the log or system out	
	private void printClip(Clip clip) {
		if (LOG.isLoggable(Level.INFO)) {
			StringBuilder sb = new StringBuilder("Java Sound Player:\n");
			sb.append(String.format("\tFile: %s\n", mediaDescriptor.mediaURL));
			sb.append(String.format("\tDuration in microseconds: %d\n", clip.getMicrosecondLength()));
			sb.append(String.format("\tDuration in seconds: %f\n", (clip.getMicrosecondLength() / (float)MIC_TO_MIL) / MIC_TO_MIL));
			sb.append(String.format("\tDuration in frames: %d\n", clip.getFrameLength()));
			sb.append(String.format("\tMicroseconds per frame: %d\n", (clip.getMicrosecondLength() / clip.getFrameLength())));
			sb.append(String.format("\tBuffer Size (bytes): %d", clip.getBufferSize()));
			LOG.info(sb.toString());
		}
	}
	
	private void printFormat(AudioFormat format) {
		if (LOG.isLoggable(Level.INFO)) {
			StringBuilder sb = new StringBuilder("Audio Format:\n");
			sb.append(String.format("\tNumber of channels: %d\n", format.getChannels()));
			sb.append(String.format("\tSample rate: %f\n", format.getSampleRate()));
			sb.append(String.format("\tFrame rate: %f\n", format.getFrameRate()));
			sb.append(String.format("\tFrame size: %d\n", format.getFrameSize()));
			sb.append(String.format("\tSample size in bits: %d\n", format.getSampleSizeInBits()));
			sb.append(String.format("\tEncoding: %s\n", format.getEncoding().toString()));
			sb.append(String.format("\tProperties: %s", format.properties().toString()));
			LOG.info(sb.toString());
		}
	}
	/*
	private void printLineInfo(DataLine.Info info) {
		System.out.println("Data Line Info:");
		System.out.println("Data line class: " + info.getLineClass().getName());
		System.out.println("Minimum buffer size: " + info.getMinBufferSize());
		System.out.println("Maximum buffer size: " + info.getMaxBufferSize());
		AudioFormat[] formats = info.getFormats();
		for (AudioFormat af: formats) {
			printFormat(af);
		}
		System.out.println();
	}
	
	private void printDataLine(DataLine line) {
		System.out.println("DataLine:");
		System.out.println("Number available bytes: " + line.available());
		System.out.println("Get buffer size: " + line.getBufferSize());
		System.out.println("Frame position (int): " + line.getFramePosition());
		System.out.println("Frame position (long): " + line.getLongFramePosition());
		System.out.println("Micro-second position: " + line.getMicrosecondPosition());
		System.out.println("Level: " + line.getLevel());
		System.out.println();
		Control[] controls = line.getControls();
		System.out.println("Controls: ");
		for (Control c : controls) {
			System.out.println("Control: " + c.getType().toString());
			if (c instanceof BooleanControl) {
				BooleanControl bc = (BooleanControl) c;
				System.out.println("\tLabel: " + bc.getStateLabel(true));
				System.out.println("\tValue: " + bc.getValue());
			} else if (c instanceof CompoundControl) {
				CompoundControl cc = (CompoundControl) c;
				System.out.println("\tNumber of members: " + cc.getMemberControls().length);
			} else if (c instanceof EnumControl) {
				EnumControl ec = (EnumControl) c;
				System.out.println("\tValue: " + ec.getValue());
				System.out.println("\tValues: " + ec.getValues());
			} else if (c instanceof FloatControl) {
				FloatControl fc = (FloatControl) c;
				System.out.println("\tValue: " + fc.getValue());
				System.out.println("\tMax-Min: " + fc.getMaximum() + "-" + fc.getMinimum());
				System.out.println("\tPrecision " + fc.getPrecision());
				System.out.println("\tUnits: " + fc.getUnits());
			}
		}
	}
	*/
	/**
	 * Detects available Controls. The clip line must be open for successful discovery.
	 * @param clip the player to query the Controls of
	 */
	private void detectControls(Clip clip) {
		Control[] controls = clip.getControls();
		StringBuilder sb = new StringBuilder("Sound Controls:\n");
		for (Control c : controls) {
			if (c instanceof BooleanControl) {
				BooleanControl bc = (BooleanControl) c;
				if (bc.getType().equals(BooleanControl.Type.MUTE)) {
					muteControl = bc;
					sb.append("Mute Control found\n");
				}
			} /* else if (c instanceof CompoundControl) {
				CompoundControl cc = (CompoundControl) c;
				System.out.println("\tNumber of members: " + cc.getMemberControls().length);
			} else if (c instanceof EnumControl) {
				EnumControl ec = (EnumControl) c;
				System.out.println("\tValue: " + ec.getValue());
				System.out.println("\tValues: " + ec.getValues());
			}*/ else if (c instanceof FloatControl) {
				FloatControl fc = (FloatControl) c;
				if (fc.getType().equals(FloatControl.Type.MASTER_GAIN)) {
					gainControl = fc;
					sb.append("Gain Control found:\n");
					sb.append(String.format("\tMin-Max: %f - %f, Precision: %f, Units: %s\n", 
							fc.getMinimum(), fc.getMaximum(), fc.getPrecision(), fc.getUnits()));
				} else if (fc.getType().equals(FloatControl.Type.SAMPLE_RATE)) {
					rateControl = fc;
					sb.append("Sample Rate Control found:\n");
					sb.append(String.format("\tMin-Max: %f - %f, Precision: %f, Units: %s\n", 
							fc.getMinimum(), fc.getMaximum(), fc.getPrecision(), fc.getUnits()));
				} else if (fc.getType().equals(FloatControl.Type.BALANCE)) {
					sb.append("Balance Control found, not used\n");
				} else if (fc.getType().equals(FloatControl.Type.PAN)) {
					sb.append("Pan Control found, not used\n");	
				}
			}
		}
		if (LOG.isLoggable(Level.INFO)) {
			LOG.info(sb.toString());
		}
	}

	@Override
	public void preferencesChanged() {
		// stub
	}

	@Override
	public MediaDescriptor getMediaDescriptor() {
		return mediaDescriptor;
	}

	@Override
	public void start() {
		if (playerClip != null) {
			if (playing) {
				return;
			}
	        // play at start of media if at end of media
	        if ((getMediaDuration() - getMediaTime()) < 40) {
	            setMediaTime(0);
	        }

	        playing = true;
			playerClip.start();
			startControllers();
		}

	}

	@Override
	public void stop() {
		if (playerClip != null) {
			playerClip.stop();
			stopControllers();
			playing = false;

			if (playingInterval) {
				stopPlayingInterval();
			}
			setControllersMediaTime(getMediaTime());
		}
	}

	/**
	 * Returns whether the player is playing, delegates directly to
	 * the clip.
	 * @return Clip.isRunning()
	 * @see {@link Clip#isRunning()}
	 */
	@Override
	public boolean isPlaying() {
		if (playerClip != null) {
			return playerClip.isRunning();
		}
		
		return false;
	}

	@Override
	public void playInterval(long startTime, long stopTime) {
		if (playerClip != null) {
			if (playerClip.isRunning()) {
				stop();
			}
			if (intervalEndController != null) {
				stopPlayingInterval();
			}
			intervalEndController = new PeriodicUpdateController(25);
			intervalEndController.addControllerListener(this);

			setMediaTime(startTime);
			setStopTime(stopTime);
			// loop plays the interval n times and then continues to the end
			// while looping, the getMicrosecondPosition method returns values like it played
			// continuously, without setting the cursor back to the start of the loop
//			playerClip.setLoopPoints(microsecPositionToFrame((startTime + offset) * MIC_TO_MIL), 
//					microsecPositionToFrame((stopTime + offset) * MIC_TO_MIL));
//			playerClip.loop(10);
			
			playing = true;
			playingInterval = true;
			playerClip.start();
			startControllers();
			intervalEndController.start();
		}
	}
	
	/**
	 * Stops and cleans up the play interval controller.
	 */
	private void stopPlayingInterval() {
		if (intervalEndController != null) {
			intervalEndController.removeControllerListener(this);
			intervalEndController.stop();
			intervalEndController = null;
		}
		playingInterval = false;
	}
/*
	private int microsecPositionToFrame(long microsecondPos) {
		return (int) (microsecondPos / microsecondsPerFrame);
	}
*/	
	@Override
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
		setControllersStopTime(this.stopTime);
	}

	/**
	 * Updates the offset of the player and of the media descriptor and
	 * updates the current media time
	 * 
	 * @param offset the new offset
	 */
	@Override
	public void setOffset(long offset) {
		long curTime = getMediaTime();
		long diff = this.offset - offset;
        this.offset = offset;
        mediaDescriptor.timeOrigin = offset;

        curTime += diff;
        setMediaTime(curTime < 0 ? 0 : curTime);
	}

	@Override
	public long getOffset() {
		return offset;
	}

	/**
	 * Moves the media time one frame forward. Uses the amount of microseconds per frame
	 * derived from the clip properties, unless a different amount has been set explicitly
	 * via {@link #setMilliSecondsPerSample(long)} 
	 */
	@Override
	public void nextFrame() {
		if (playerClip != null) {
			if (playerClip.isRunning()) {
				stop();
			}
			long curTime = getMediaTime();
			if (milliSecondsPerSample > 0) {
				setMediaTime(curTime + (long)(milliSecondsPerSample));
			} else {
				setMediaTime(curTime + (long)(microsecondsPerFrame));
			}
		}
	}

	/**
	 * Moves the media time one frame backward. Uses the amount of microseconds per frame
	 * derived from the clip properties, unless a different amount has been set explicitly
	 * via {@link #setMilliSecondsPerSample(long)} 
	 */
	@Override
	public void previousFrame() {
		if (playerClip != null) {
			if (playerClip.isRunning()) {
				stop();
			}
			long curTime = getMediaTime();
			if (milliSecondsPerSample > 0) {
				setMediaTime(Math.max(0, curTime - (long)(milliSecondsPerSample)));
			} else {
				setMediaTime(Math.max(0, curTime - (long)(microsecondsPerFrame)));
			}
		}
	}

	@Override
	public void setFrameStepsToFrameBegin(boolean stepsToFrameBegin) {
		// stub, video only
	}

	@Override
	public void setMediaTime(long time) {
		if (playerClip != null) {
			if (playerClip.isRunning()) {
				stop();
			}
			if (time < 0) {
				time = 0;
			}
			if (time > getMediaDuration()) {
				time = getMediaDuration();
			}
			playerClip.setMicrosecondPosition((time + offset) * MIC_TO_MIL);
			setControllersMediaTime(time);
		}
	}

	/**
	 * 
	 * @return the current media time of the player (taking the offset into account)
	 */
	@Override
	public long getMediaTime() {
		if (playerClip != null) {
			return (playerClip.getMicrosecondPosition() / MIC_TO_MIL) - offset;
			//return (long)((playerClip.getLongFramePosition() * microsecondsPerFrame) / MIC_TO_MIL) - offset;
		}
		return 0;
	}

	/**
	 * A Rate Control usually doesn't seem to be part of the controls of a clip.
	 * @param rate the new play back rate
	 */
	@Override
	public void setRate(float rate) {
		curRate = rate;
		if (rateControl != null) {
			rateControl.setValue(rate);
		}
	}

	@Override
	public float getRate() {
		return curRate;
	}

	/**
	 * For audio files the encoded sample rate or frame rate is always known. 
	 * This method is intended for video players (that don't always provide 
	 * access to that property).
	 * 
	 * @return true
	 */
	@Override
	public boolean isFrameRateAutoDetected() {
		return true;
	}

	@Override
	public long getMediaDuration() {
		if (playerClip != null) {
			return playerClip.getMicrosecondLength() / MIC_TO_MIL - offset;
		}
		return 0;
	}

	@Override
	public float getVolume() {
		return curVolume;
	}

	/**
	 * Sets the volume level to a value between 0 and 1. This will be recalculated to a 
	 * dB value within the range of the Gain Control.
	 * 
	 * @param level a level between 0 and 1
	 */
	@Override
	public void setVolume(float level) {
		curVolume = level;
		if (gainControl != null) {
			if (level == 0.0) {
				gainControl.setValue(gainControl.getMinimum());
			} else {			
				float extend = gainControl.getMaximum() - gainControl.getMinimum();
				float nValue = (float) ((extend / 2) * Math.log10(level));// does /2  make sense here?
				//float nValue = (float) (extend * Math.log10(level));
				nValue = gainControl.getMaximum() + nValue;
				nValue = Math.min(nValue, gainControl.getMaximum());
				nValue = Math.max(nValue, gainControl.getMinimum());
				//System.out.println("dB: " + nValue);
				gainControl.setValue(nValue);
			}
		}
	}

	/**
	 * Only stored here, not directly,internally applied.
	 * 
	 * @param level
	 */
	@Override
	public void setSubVolume(float level) {
		this.curSubVolume = level;
	}

	@Override
	public float getSubVolume() {
		return curSubVolume;
	}

	@Override
	public void setMute(boolean mute) {
		muted = mute;
		if (muteControl != null) {
			muteControl.setValue(mute);
			if (!mute) {
				playerClip.flush();
			}
		}
	}

	@Override
	public boolean getMute() {
		return muted;
	}

	@Override
	public void setLayoutManager(ElanLayoutManager layoutManager) {
		// stub

	}

	@Override
	public Component getVisualComponent() {
		return null;
	}

	@Override
	public int getSourceWidth() {
		return 0;
	}

	@Override
	public int getSourceHeight() {
		return 0;
	}

	@Override
	public float getAspectRatio() {
		return 0;
	}

	@Override
	public void setAspectRatio(float aspectRatio) {
		// stub
	}

	/**
	 * 
	 * @return if set, the preferred duration per sample in milliseconds, otherwise
	 * the encoded frame duration in milliseconds
	 */
	@Override
	public double getMilliSecondsPerSample() {
		if (milliSecondsPerSample > 0.0d) {
			return milliSecondsPerSample;
		}
		if (playerClip != null) {
			return (playerClip.getMicrosecondLength() / playerClip.getFrameLength()) / MIC_TO_MIL;
		}
		
		return 0;
	}

	/**
	 * Allows to set a duration per sample or frame for practical reasons. The duration of 
	 * a sound sample often is less than a millisecond, which is not a practical value for
	 * frame stepping purposes. 
	 * @param milliSeconds the desired duration per sample or frame
	 */
	@Override
	public void setMilliSecondsPerSample(long milliSeconds) {
		if (milliSeconds > 0) {
			milliSecondsPerSample = (double) milliSeconds;
		} else {
			milliSecondsPerSample = 0.0d;
		}
	}

	@Override
	public void updateLocale() {
		// stub

	}

	@Override
	public String getFrameworkDescription() {
		return "JavaSound Media Player";
	}

	@Override
	public void cleanUpOnClose() {
		if (playerClip != null) {
			if (playerClip.isRunning()) {
				playerClip.stop();
			}
			playerClip.flush();
			playerClip.close();
			playerClip.removeLineListener(jspLineListener);
		}
		// not sure if closing the clip also closes the input stream
		if (audioInStream != null) {
			try {
				audioInStream.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * A listener that stops connected controllers when an end-of-media stop event
	 * is received.
	 */
	private class JSPLineListener implements LineListener {

		@Override
		public void update(LineEvent event) {
			//System.out.println("LE: " + event.getType() + " At frame: " + event.getFramePosition());
			if (event.getType() == LineEvent.Type.STOP && 
					event.getFramePosition() == playerClip.getFrameLength()) {
				stopControllers();
				playing = false;
			}
		}
		
	}

	/**
	 * Implementation based on the existence of intervalEndController. Stops the play back
	 * if the media time is (close) at or past the interval stop time
	 * @param event the time event
	 */
	@Override
	public void controllerUpdate(ControllerEvent event) {
		if (event instanceof TimeEvent) {
			if (playingInterval) {
				if (getMediaTime() >= stopTime - 25) {
					stop();
					//stopPlayingInterval(); called from stop()
					// correct if the overshoot is too large
					if (getMediaTime() - stopTime >= 10) {
						setMediaTime(stopTime);
					}
				}
			}
		}
		
	}
	
}
