package mpi.eudico.client.annotator;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.gui.ElanSlider;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.HasAsynchronousNativePlayer;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.AnnotationDensityViewer;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.client.mediacontrol.StartEvent;
import mpi.eudico.client.mediacontrol.StopEvent;
import mpi.eudico.client.mediacontrol.TimeEvent;


/**
 * MediaPlayerControlPanel A collection of buttons, sliders, etc to controls
 * the media, e.g. playing, setting current time.
 */
@SuppressWarnings("serial")
public class ElanMediaPlayerController extends AbstractViewer
                                       implements PreferencesListener {
	/** Holds value of property DOCUMENT ME! */
    final private static Dimension BUTTON_SIZE = new Dimension(30, 20);
    private long userTimeBetweenLoops = 500; //used when playing selection in loop mode, default 0.5 seconds
    private ViewerManager2 vm;
    private ElanSlider rateslider;
    private ElanSlider volumeslider;
    private SelectionPanel selectionpanel;
    private VolumeIconPanel volumeIconPanel;
    private StepAndRepeatPanel stepAndRepeatPanel;
    private JPanel volumesPanel;
    
    //private MediaPlayerControlSlider mpcs;
    private DurationPanel durationPanel;
    private AnnotationDensityViewer annotationDensityViewer;
    
    //private TimePanel timePanel;
    private PlayButtonsPanel playButtonsPanel;
    private AnnotationNavigationPanel annotationPanel;
    private SelectionButtonsPanel selectionButtonsPanel;
    private ModePanel modePanel;
    private long stopTime = 0;
    private boolean playingSelection = false;
    private boolean bLoopMode = false;
    private boolean bSelectionMode = false;
    private boolean bBeginBoundaryActive = false;
    private boolean stepAndRepeatMode = false;
    private boolean prevHaveSliders = false;
    
    // loopthread moved from PlaySelectionCommand to here to be able to stop it 
    // actively (instead of passively with a boolean, which has all kind of side effects)
    private LoopThread loopThread;
    
    private StepAndRepeatThread stepThread;

    /**
     * Constructor
     *
     * @param theVM DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
	public ElanMediaPlayerController(ViewerManager2 theVM) {
        vm = theVM;

        rateslider = new ElanSlider("ELANSLIDERRATE", 0, 200, 100, vm);
        volumeslider = new ElanSlider("ELANSLIDERVOLUME", 0, 100, 100, vm);
        selectionpanel = new SelectionPanel(vm);
        
        //	mpcs = new MediaPlayerControlSlider();
        //	timePanel = new TimePanel();
        durationPanel = new DurationPanel(vm.getMasterMediaPlayer()
                                            .getMediaDuration());
        playButtonsPanel = new PlayButtonsPanel(getButtonSize(), vm);
        annotationPanel = new AnnotationNavigationPanel(getButtonSize(), vm);
        selectionButtonsPanel = new SelectionButtonsPanel(getButtonSize(), vm);
        modePanel = new ModePanel(vm, this);        
        volumeIconPanel = new VolumeIconPanel(vm, SwingConstants.VERTICAL, getButtonSize());
        stepAndRepeatPanel = new StepAndRepeatPanel(vm);
        
		Map prefs = Preferences.getMap(INDIVIDUAL_VOLUMES_PREFS, vm.getTranscription());
		if (prefs == null) {
			playerVolumes = new HashMap<String, Float>();
		} else {
			playerVolumes = (Map<String, Float>) prefs;
		}
		prefs = Preferences.getMap(INDIVIDUAL_PLAYER_MUTE_SOLO_PREF, vm.getTranscription());
		if (prefs == null) {
			playerMutedStates = new HashMap<String, String>();
		} else {
			playerMutedStates = (Map<String, String>) prefs;
		}
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getButtonSize() {
        return BUTTON_SIZE;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public long getUserTimeBetweenLoops() {
        return userTimeBetweenLoops;
    }

    /**
     * DOCUMENT ME!
     *
     * @param loopTime DOCUMENT ME!
     */
    public void setUserTimeBetweenLoops(long loopTime) {
        userTimeBetweenLoops = loopTime;
    }

    // getters for subpanels
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public MediaPlayerControlSlider getSliderPanel() {
        return vm.getMediaPlayerControlSlider();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AnnotationDensityViewer getAnnotationDensityViewer() {
    	if (annotationDensityViewer == null) {
    		annotationDensityViewer = vm.createAnnotationDensityViewer();
    	}
        return annotationDensityViewer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getRatePanel() {
        return rateslider; //.getSlider();
    }

    /**
     * Sets the play rate and updates the ui.
     *
     * @param rate the play rate
     */
    @Override
    public void setRate(float rate) {
        super.setRate(rate);
        // multiply by 100; the slider uses ints
        rateslider.setValue((int) (100 * rate));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getVolumePanel() {
        return volumeslider;
    }

    /**
     * Return the panel that can be used to adjust the individual volumes 
     * of each media player: the Master and all slaves.
     *
     * @return a JPanel
     */
    public JComponent getPlayersVolumePanel() {
    	if (volumesPanel == null) {
            makePlayersVolumePanel();
    	}
    	
        return volumesPanel;
    }

    /**
     * Sets the master volume and updates the ui.
     * This overrides the superclass method, which changes the subvolume.
     *
     * @param volume the volume
     */
    @Override
    public void setVolume(float volume) {
        // Don't call super.setVolume(volume): the slider will propagate its change anyway.
        // multiply by 100; the slider uses ints.
    	// The slider will propagate its new value to setMasterVolume().
        volumeslider.setValue((int) (100 * volume));
    }
    
    /**
     * @returns the master volume.
     * In currently known cases the volume slider would exist already,
     * but there is a fallback to the Preferences just in case.
     */
    @Override 
    public float getVolume() {
    	if (volumeslider != null) {
    		return (float)volumeslider.getValue() / 100.0f;
    	} else {
    		Float volume = Preferences.getFloat("MediaControlVolume", 
    				vm.getTranscription());
    		if (volume != null) {
    			return volume.floatValue();
    		}
    		return 1.0f;
    	}
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getModePanel() {
        return modePanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public SelectionButtonsPanel getSelectionButtonsPanel() {
        return selectionButtonsPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public PlayButtonsPanel getPlayButtonsPanel() {
        return playButtonsPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AnnotationNavigationPanel getAnnotationNavigationPanel() {
        return annotationPanel;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public VolumeIconPanel getVolumeIconPanel() {
        return volumeIconPanel;
    }


    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getDurationPanel() {
        return durationPanel;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getTimePanel() {
        return vm.getTimePanel();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getSelectionPanel() {
        return selectionpanel;
    }
    
    /**
     * Returns the step-and-repeat mode panel.
     * 
     * @return
     */
    public StepAndRepeatPanel getStepAndRepeatPanel() {
    	return stepAndRepeatPanel;
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    @Override
	public void updateActiveAnnotation() {
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	public void updateLocale() {
    	if (muteButtons != null) {
    		String muteStr = ElanLocale.getString(
                    "MediaPlayerControlPanel.VolumeSlider.Mute");
    		for (ECheckBox box : muteButtons) {
    			box.setText(muteStr);
    		}
    	}
    	if (soloButtons != null) {
    		String soloStr = ElanLocale.getString(
                    "MediaPlayerControlPanel.VolumeSlider.Solo");
    		for (ERadioButton box : soloButtons) {
    			box.setText(soloStr);
    		}
    	}
    }

    /**
     * AR notification that the selection has changed method from SelectionUser
     * not implemented in AbstractViewer
     */
    @Override
	public void updateSelection() {
    }

    private void adjustSelection() {
        // set active boundary to current media time
        long currTime = getMediaTime();
        long beginTime = getSelectionBeginTime();
        long endTime = getSelectionEndTime();

        if (bBeginBoundaryActive) {
            beginTime = currTime;
        } else {
            endTime = currTime;
        }

        if (beginTime > endTime) { // begin and end change place
            setSelection(endTime, beginTime);
            toggleActiveSelectionBoundary();
        } else {
            setSelection(beginTime, endTime);
        }
    }

    /**
     * AR notification that some media related event happened method from
     * ControllerListener not implemented in AbstractViewer
     *
     * @param event DOCUMENT ME!
     */
    @Override
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof StartEvent) {
            //playingSelection = true;
            return;
        }

        // ignore time events within a certain time span after a stop event
        // that happened while playing a selection. This is needed to keep the
        // current selection after play selection is done in selection mode
        if (event instanceof TimeEvent &&
                ((System.currentTimeMillis() - stopTime) < 700)) {
            return;
        }

        // remember the stop time if the stop happened while playing a selection
        // time events will be ignored for a certain period after this stop time
        if (event instanceof StopEvent) {
            if (!bLoopMode) {
                playingSelection = false;
            }

            stopTime = System.currentTimeMillis();

            // change active annotation boundary if at end of selection and active edge was on the left
            // added for practical reasons, users got confused and inadvertently destroyed the selection
            //    		long halfTime = getSelectionBeginTime() + (getSelectionEndTime() - getSelectionBeginTime()) / 2;
            if (isBeginBoundaryActive() &&
                    (getMediaTime() == getSelectionEndTime())) {
                toggleActiveSelectionBoundary();
            }
            
            // HS Aug 2008: make sure that in selection mode the selection is updated
            // the selection is always a bit behind the media playhead 
            //return;
            
            // HS 2016/7 after a stop event in selection mode, check if the (native) media player
            // already finished playing a selection in case of an asynchronous native player
            if (bSelectionMode && vm.getMasterMediaPlayer() instanceof HasAsynchronousNativePlayer) {
            	HasAsynchronousNativePlayer asynPl = (HasAsynchronousNativePlayer) vm.getMasterMediaPlayer();
            	while (asynPl.isPlayingInterval()) {
            		try {
            			Thread.sleep(10);
            		} catch (InterruptedException ie){}
            	}
            }
        }

        //in some cases set a new selection 
        if (!playingSelection && (bSelectionMode == true)) {
            adjustSelection();
        }
    }

    /**
     * Switches the controller to the playing-selection mode.
     *
     * @param b the mode, on or off
     */
    public void setPlaySelectionMode(boolean b) {
        playingSelection = b;
    }

    /**
     * Returns whether the controller is in play selection mode.
     *
     * @return whether the controller is in play selection mode
     */
    public boolean isPlaySelectionMode() {
        return playingSelection;
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setLoopMode(boolean b) {
        bLoopMode = b;
        modePanel.updateLoopMode(bLoopMode);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getLoopMode() {
        return bLoopMode;
    }

    /**
     * Toggles the loop mode
     */
    public void doToggleLoopMode() {
        if (bLoopMode == true) {
            bLoopMode = false;
        } else {
            bLoopMode = true;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getSelectionMode() {
        return bSelectionMode;
    }

    /**
     * Toggles the selection mode
     */
    public void doToggleSelectionMode() {
        // bSelectionMode = !bSelectionMode
        if (bSelectionMode == true) {
            bSelectionMode = false;
        } else {
            bSelectionMode = true;
        }

        // generate a time event to make sure the image on the button toggles
        // this sometimes sets the selection begin time to 0
        //setMediaTime(getMediaTime());
        modePanel.updateSelectionMode(bSelectionMode);//??
        getModePanel().revalidate();
    }

    /**
     * When main time is begintime, main time is set to endtime (of selection)
     * When main time is endtime, main time is set to begintime (of selection)
     */
    public void toggleActiveSelectionBoundary() {
        // bBeginBoundaryActive = !bBeginBoundaryActive
        if (bBeginBoundaryActive == true) {
            bBeginBoundaryActive = false;
        } else {
            bBeginBoundaryActive = true;
        }

        // otherwise the button image is not always updated immediately
        if (!playerIsPlaying()) {
            setMediaTime(getMediaTime());
        }
    }

    /**
     * Returns whether the selection begin boundary is active.
     *
     * @return whether the selection begin boundary is active
     */
    public boolean isBeginBoundaryActive() {
        return bBeginBoundaryActive;
    }

    /**
     * Starts a new play selection in a loop thread, after stopping the current
     * one  (if necessary)
     *
     * @param begin selection begintime
     * @param end selection endtime
     */
    public void startLoop(long begin, long end) {
        // stop current loop if necessary
        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
            //??
            /*
            try {
            	loopThread.join(500);
            } catch (InterruptedException ie) {
            	
            }*/
        }

        loopThread = new LoopThread(begin, end);
        loopThread.start();
    }

    /**
     * Stops the current loop thread, if active.
     */
    public void stopLoop() {
        setPlaySelectionMode(false);

        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
        }
    }
    
    // step and repeat mode methods
    public void setStepAndRepeatMode(boolean mode) {
    	if (stepAndRepeatMode == mode) {
    		return;
    	} else if (stepAndRepeatMode) {
    		stepAndRepeatMode = false;
    		if (stepThread != null) {
    			try {
    				stepThread.interrupt();
    			} catch (Exception ie) {
    				ie.printStackTrace();
    			}
    		}
    		playButtonsPanel.setEnabled(true);
    		selectionButtonsPanel.setEnabled(true);
    		stepAndRepeatPanel.setPlayIcon(true);
    	} else {
    		playButtonsPanel.setEnabled(false);
    		selectionButtonsPanel.setEnabled(false);
    		stepAndRepeatPanel.setPlayIcon(false);
	    	this.stepAndRepeatMode = mode;

	    	// stop player, stop play selection, play selectionmode = false
	    	playingSelection = false;
	    	bLoopMode = false;
	    	stepThread = new StepAndRepeatThread();
	    	stepThread.start();
    	}
    }
    
    public boolean isStepAndRepeatMode() {
    	return stepAndRepeatMode;
    }

    /**
     * Adjust volume and rate.
     */
    @Override
	public void preferencesChanged() {
		Float volume = Preferences.getFloat("MediaControlVolume", 
				vm.getTranscription());
		if (volume != null) {
			setVolume(volume.floatValue());
		}
		Float rate = (Float) Preferences.getFloat("MediaControlRate", 
				vm.getTranscription());
		if (rate != null) {
			setRate(rate.floatValue());
		}
		if (volumesPanel != null) {
			// Prevent doing a lot of work if only some other preference changed...
			boolean newHaveSliders = haveSliders();
			if (newHaveSliders != prevHaveSliders) {
				if (!newHaveSliders) {
					/*
					 * If the user doesn't want sliders any more, having
					 * multiple sounds at once is weird. Make life simple again.
					 * These values are deliberately not stored in the
					 * preferences, so that later, when the sliders are back in
					 * view, they can be set to their previous values again.
					 */
					vm.getVolumeManager().setSimpleVolumes();
				}
				// Creating the sliders will also get the desired subvolumes from
				// the preferences and apply them to the players.
				updatePlayersVolumePanel();
			}
		}
	}
	
    /**
     * Starts a new playing thread when loopmode is true
     */
    private class LoopThread extends Thread {
        private long beginTime;
        private long endTime;
        private boolean stopLoop = false;

        /**
         * Creates a new LoopThread instance
         *
         * @param begin the interval begin time
         * @param end the interval endtime
         */
        LoopThread(long begin, long end) {
            this.beginTime = begin;
            this.endTime = end;
        }

        /**
         * Sets the flag that indicates that the loop thread should stop to
         * true.
         */
        public void stopLoop() {
            stopLoop = true;
        }

        /**
         * Restarts the player to play the interval as long as the controller
         * is in  loop mode and the loop is not explicitly stopped.
         */
        @Override
		public void run() {
            while (!stopLoop && getLoopMode()) {
                if (!playerIsPlaying()) {
                    playInterval(beginTime, endTime);
    				// wait until playing is started
    				while (!playerIsPlaying()) {
    					try {
    						Thread.sleep(10);
    					} catch (InterruptedException ie) {
    						return;
    					}
    				}
                }

                while (playerIsPlaying() == true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                    if (stopLoop) {
                    	return;
                    }
                }

                try {
                    Thread.sleep(getUserTimeBetweenLoops());
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Make a panel with volume sliders to adjust the volume of the individual media players.
     */
    
    /**
     * A private helper class: a JCheckBox that remembers sufficient information to
     * avoid feedback loops between components, if they are set up to influence
     * each other mutually.
     * This is done by overriding the setSelected() method: it will remember what
     * value was set, so that in an event handler it can be checked if the change
     * event was expected (from the program) or not (from the user).
     * @author olasei
     */
    private static class ECheckBox extends JCheckBox {
		private boolean expectedValue = false;
    	ECheckBox() {
    		super();
    	}
    	@Override
		public void setSelected(boolean val) {
    		expectedValue = val;
    		super.setSelected(val);
    	}
    	public boolean wasNotExpected(boolean selected) {
    		boolean was = selected != expectedValue;
    		expectedValue = selected;

    		return was;
    	}
    }
    
    /**
     * And the same for JRadioButton.
     * @author olasei
     */
    private static class ERadioButton extends JRadioButton {
		private boolean expectedValue = false;
		ERadioButton() {
    		super();
    	}
    	@Override
		public void setSelected(boolean val) {
    		expectedValue = val;
    		super.setSelected(val);
    	}
    	public boolean wasNotExpected(boolean selected) {
    		boolean was = selected != expectedValue;
    		expectedValue = selected;

    		return was;
    	}
    }
    
    static final String INDIVIDUAL_VOLUMES_PREFS = "IndividualPlayerVolumes";
    public static final String HAVE_INDIVIDUAL_VOLUME_CONTROLS_PREF = "Media.HaveIndividualVolumeControls";
    public static final String INDIVIDUAL_PLAYER_MUTE_SOLO_PREF = "IndividualPlayerMuteSoloSettings";
    private final String MUTE = "mute";
    private final String SOLO = "solo";
    
    private Map<String, Float> playerVolumes;
    private Map<String, String> playerMutedStates;
    private int deferringUpdates;
    private boolean updateWasDeferred;
    private List<ECheckBox> muteButtons;
    private List<ERadioButton> soloButtons;
    
    private void makePlayersVolumePanel()
    {
    	volumesPanel = new JPanel();
    	volumesPanel.setLayout(new GridBagLayout());
    	// Indent the subvolume sliders a bit.
        Border inner = BorderFactory.createEmptyBorder(0, 40, 0, 0);
        volumesPanel.setBorder(inner);

    	deferringUpdates = 0;
    	updateWasDeferred = false;

    	updatePlayersVolumePanel();
    }
    
    /**
     * If it is foreseeable that there will be many updates to the list of
     * slave players, then call this function with 'true' before,
     * and with 'false' after. The updates will then not be done until
     * the latter call.
     * 
     * Calls nest and should therefore be properly balanced.
     * 
     * @param defer true or false.
     */
    public void deferUpdatePlayersVolumePanel(boolean defer) {
    	if (defer) {
    		++deferringUpdates;
    	} else {
    		--deferringUpdates;
    		if (deferringUpdates <= 0 && updateWasDeferred) {
    			updatePlayersVolumePanel();
    		}
    	}
    }
    
    /**
     * Call this method if the list of slave media players
     * (or the master player) has been changed.
     * New sliders will be placed on the volumes panel.
     * This is needed in an unfortunate number of locations...
     */
    public void updatePlayersVolumePanel()
    {
    	if (deferringUpdates > 0) {
    		updateWasDeferred = true;
    		return;
    	}
		updateWasDeferred = false;

    	if (volumesPanel == null) {
    		return;
    	}

    	// Start out with an empty panel.
		volumesPanel.removeAll();
		
		// If preferences say we don't want these volume controls,
		// we're done at this point.
		prevHaveSliders = haveSliders();
		if (!prevHaveSliders) {
			return;
		}
    	    	
		// Then add back controls for each media player.   	
		
		muteButtons = new ArrayList<ECheckBox>();
		soloButtons = new ArrayList<ERadioButton>();
		
    	// Create a vertical orientation, flexible in horizontal direction.
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.anchor = GridBagConstraints.FIRST_LINE_START;
    	
		// Make one slider for the Master and each slave media player.
		addOneVolumeSlider(vm.getMasterMediaPlayer(), gbc);
    	for (ElanMediaPlayer mp : vm.getSlaveMediaPlayers()) {
    		addOneVolumeSlider(mp, gbc);
    	}   	
    	
    	updateLocale();
    }

    private void addOneVolumeSlider(final ElanMediaPlayer mp, GridBagConstraints gbc) {
		if (mp instanceof EmptyMediaPlayer) {
			return;
		}
		String name = mp.getMediaDescriptor().mediaURL;
		final String fileName = FileUtility.fileNameFromPath(name);
		//name = FileUtility.dropExtension(fileName);
		JLabel label = new JLabel(fileName);
		
		gbc.gridx = 0;
    	gbc.weightx = 0;
    	gbc.gridwidth = 2;
    	gbc.gridheight = 1;
		volumesPanel.add(label, gbc);
		
		ECheckBox muteButton = new ECheckBox();
		ERadioButton soloButton = new ERadioButton();

		muteButton.setSelected(mp.getMute());
		muteButton.setFont(Constants.deriveSmallFont(muteButton.getFont()));
		soloButton.setFont(muteButton.getFont());

    	gbc.gridy++;
    	gbc.gridwidth = 1;
		volumesPanel.add(muteButton, gbc);
		gbc.gridx = 1;
		volumesPanel.add(soloButton, gbc);
		
		muteButtons.add(muteButton);
		soloButtons.add(soloButton);

		// Find the previously set volume from Preferences, if any.
		// If not, make sure it gets set in Preferences, so that
		// Preferences, slider, and player all have the same volume.
		float volume;
		if (playerVolumes.containsKey(fileName)) {
			volume = playerVolumes.get(fileName);
			vm.getVolumeManager().setSubVolume(mp, volume);
		} else {
    		volume = mp.getSubVolume();
			playerVolumes.put(fileName, new Float(volume));
		}
		
		final JSlider slider  = new JSlider(0, 100, (int) (100 * volume));
		slider.putClientProperty("JComponent.sizeVariant", "mini"); //Mac Aqua look & feel only
		slider.setMajorTickSpacing(25);
		slider.setMinorTickSpacing(5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {					
				JSlider s = (JSlider)event.getSource();					
				final float newVolume = (float) s.getValue() / 100;
		        vm.getVolumeManager().setSubVolume(mp, newVolume);

		        if (!s.getValueIsAdjusting()) {
					// store it in preferences, keyed to the name of the media.
					playerVolumes.put(fileName, new Float(newVolume));
					// There is no need to save immediately, or to broadcast the new value.
					// (which means that unless the preferences make a copy, setting the
					// same Map every time probably isn't even necessary.)
					Preferences.set(INDIVIDUAL_VOLUMES_PREFS, playerVolumes, vm.getTranscription(), false, false);
		        }
			}});
		
		gbc.gridx = 2;
    	gbc.weightx = 1;
    	gbc.gridheight = 2;
    	gbc.gridy--;
		volumesPanel.add(slider, gbc);
		
		gbc.gridy += 2;
		
		// apply mute / solo preferences
		if (playerMutedStates.containsKey(fileName)) {
			String value = playerMutedStates.get(fileName);
			if (SOLO.equals(value)) {
				soloButton.setSelected(true);
			} else if (MUTE.equals(value)) {
				muteButton.setSelected(true);
				vm.getVolumeManager().setMute(mp, true);
				slider.setEnabled(false);
			}
		}
		
		/*
		 * Checking the boxes makes other boxes change.
		 * Checking a "Solo" box sets all Mute buttons to mute all players except this one.
		 * There can be at most one "Solo" button checked so it also de-selects all other Solo boxes.
		 * De-selecting the Solo box will un-Mute all.
		 * If the user manipulates a Mute box, all Solo boxes get deselected.
		 * 
		 * HOWEVER, with standard checkboxes we can't see if the user manipulated a checkbox, or if we did.
		 * Hence the derived class, which remembers the expected value, so that propagation
		 * of changes can be avoided when the change was not user-initiated.
		 */
		muteButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ECheckBox source = (ECheckBox)e.getSource();
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				vm.getVolumeManager().setMute(mp, selected);
				slider.setEnabled(!selected);
				if (selected) {
					playerMutedStates.put(fileName, MUTE);
				} else {
					if (!SOLO.equals(playerMutedStates.get(fileName))) {
						playerMutedStates.remove(fileName);
					}
				}
				// superfluous except for the first time 
				// we are working directly with the map in the preferences, not a copy, if it already existed
				Preferences.set(INDIVIDUAL_PLAYER_MUTE_SOLO_PREF, playerMutedStates, 
						vm.getTranscription(), false, false);
				
				if (source.wasNotExpected(selected)) {
					for (ERadioButton box: soloButtons) {
						box.setSelected(false);
					}
				}
			}
		});
		
		soloButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ERadioButton source = (ERadioButton)e.getSource();
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				// store preferences
				if (selected) {
					playerMutedStates.put(fileName, SOLO);
				} else {
					if (!MUTE.equals(playerMutedStates.get(fileName))) {
						playerMutedStates.remove(fileName);
					}
				}
				// superfluous except for the first time 
				// we are working directly with the map in the preferences, not a copy, if it already existed
				Preferences.set(INDIVIDUAL_PLAYER_MUTE_SOLO_PREF, playerMutedStates, 
						vm.getTranscription(), false, false);
				
				if (source.wasNotExpected(selected)) {
					if (selected) {
						// Deselect other solo checkboxes, and check all mutes that are not "ours".
						for (int i = 0; i < soloButtons.size(); i++) {
							ERadioButton solobox = soloButtons.get(i);
							if (source == solobox) {
								muteButtons.get(i).setSelected(false);
							} else {
								muteButtons.get(i).setSelected(true);
								solobox.setSelected(false);
							}
						}
					} else {
						// un-Mute all
						for (ECheckBox box: muteButtons) {
							box.setSelected(false);
						}				
					}
				}
			}
		});
    }
    
    /**
     * Check the preferences to see if the user wants volume control sliders for individual media players.
     * Default to <code>true</code> if no preference is set.
     * See {@link mpi.eudico.client.annotator.prefs.gui.MediaNavPanel#origShowVolumeControls} for default value
     * in the preferences panel.
     */
	private boolean haveSliders() {
		Boolean boolPref = Preferences.getBool(HAVE_INDIVIDUAL_VOLUME_CONTROLS_PREF, null);
    	boolean haveVolumeControls = true;
    	if (boolPref != null) {
    		haveVolumeControls = boolPref;
    	}
		return haveVolumeControls;
	}
    
	/**
     * A thread that plays an interval of duration t n times and then shifts the interval forward with
     * a step size s.
     * 
     * @author Han Sloetjes
     */
    class StepAndRepeatThread extends Thread {
    	private long interval = 2000;
    	private long repeats = 3;// number of repeat, or total number of times each interval is played
    	private long step = 1000;
    	private long pauseBetweenLoops = 500;
    	private long begin, end;
    	private long ultimateEnd;
    	private long count = 0;// count from 0 or 1?
    	
    	/**
		 * Constructor, initializes fields based on settings stored in the step-and-repeat panel.
		 */
		public StepAndRepeatThread() {
			super();
			
			if (stepAndRepeatPanel.getBeginTime() < 0) {
				begin = getMediaTime();
			} else {
				begin = stepAndRepeatPanel.getBeginTime();
			}
			if (begin == getMediaDuration()) {
				begin = 0;//?? restart from begin?
			}
			interval = stepAndRepeatPanel.getIntervalDuration();
			end = begin + interval;
			repeats = stepAndRepeatPanel.getNumRepeats();
			step = stepAndRepeatPanel.getStepSize();
			pauseBetweenLoops = stepAndRepeatPanel.getPauseDuration();
			
			if (stepAndRepeatPanel.getEndTime() <= 0) {
				ultimateEnd = getMediaDuration();
			} else {
				ultimateEnd = stepAndRepeatPanel.getEndTime();
			    if (ultimateEnd < begin + interval) {
			    	ultimateEnd = begin + interval;// or change the interval?
			    	if (ultimateEnd > getMediaDuration()) {
			    		ultimateEnd = getMediaDuration();
			    		interval = ultimateEnd - begin;
			    	}
			    }
			}
		}


		@Override
		public void run() {
			
			if (!playerIsPlaying()) {
				playInterval(begin, end);
				// wait until playing is started
				while (!playerIsPlaying()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						return;
					}
				}
			}
			//System.out.println("Start playing at " + begin);
			
    		while (!isInterrupted()) {
    			if (!playerIsPlaying()) {
    				if (isInterrupted()) {
    					return;
    				}
    				//System.out.println("Playing interval at " + begin + " count: " + count);
    				playInterval(begin, end);
    				// wait until playing is started
    				while (!playerIsPlaying()) {
    					try {
    						Thread.sleep(10);
    					} catch (InterruptedException ie) {
    						return;
    					}
    				}
    			}
    			
                while (playerIsPlaying()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    	try {
                    		vm.getMasterMediaPlayer().stop();
                    		return;
                    	} catch (Exception eex) {
                    		
                    	}
                    }
                    if (isInterrupted()) {
                    	try {
                    		vm.getMasterMediaPlayer().stop();
                    		return;
                    	} catch (Exception eex) {
                    		
                    	}
                    }
                }
                
                //System.out.println("Playing at end of interval " + end + " count: " + count);
                try {
                    Thread.sleep(pauseBetweenLoops);
                } catch (Exception ex) {
                	break;
                }
                
                count++;
                if (count == repeats) {
                	begin += step;// check media duration
                	if (begin >= ultimateEnd) {
                		break;
                	}
                	end += step;// check media duration
                	if (end > ultimateEnd) {
                		end = ultimateEnd;
                	} else if (ultimateEnd - end < step) {
                		end = ultimateEnd;
                	}
                	// if the remaining interval is too short, break
                	if (end - begin < 100) {
                		break;
                	}
                	count = 0;                	
                }
    		}
    		
    		ElanMediaPlayerController.this.setStepAndRepeatMode(false);
    	}// end run
    		
    }// end StepAndRepeatThread class
    
}
//end of ElanMediaPlayerController
