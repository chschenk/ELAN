/*
 * Created on Sep 22, 2003
 *
 *
 */
package mpi.eudico.client.annotator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.grid.GridViewer;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.NoPlayerException;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.client.annotator.recognizer.gui.RecognizerPanel;
import mpi.eudico.client.annotator.search.result.viewer.ElanResultViewer;
import mpi.eudico.client.annotator.transcriptionMode.TranscriptionViewer;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.AnnotationDensityViewer;
import mpi.eudico.client.annotator.viewer.CommentViewer;
import mpi.eudico.client.annotator.viewer.GestureDispatcher;
import mpi.eudico.client.annotator.viewer.GestureMacDispatcher;
import mpi.eudico.client.annotator.viewer.GesturesListener;
import mpi.eudico.client.annotator.viewer.InterlinearViewer;
import mpi.eudico.client.annotator.viewer.LexiconEntryViewer;
import mpi.eudico.client.annotator.viewer.MetadataViewer;
import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;
import mpi.eudico.client.annotator.viewer.MultiTierViewer;
import mpi.eudico.client.annotator.viewer.SegmentationViewer2;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SignalViewerControlPanel;
import mpi.eudico.client.annotator.viewer.SingleTierViewer;
import mpi.eudico.client.annotator.viewer.SingleTierViewerPanel;
import mpi.eudico.client.annotator.viewer.SubtitleViewer;
import mpi.eudico.client.annotator.viewer.TextViewer;
import mpi.eudico.client.annotator.viewer.TimeLineViewer;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;
import mpi.eudico.client.annotator.viewer.Viewer;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.client.mediacontrol.ControllerListener;
import mpi.eudico.client.mediacontrol.PeriodicUpdateController;
import mpi.eudico.client.mediacontrol.TimeEvent;
import mpi.eudico.client.mediacontrol.TimeLineController;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.server.corpora.util.ACMEditableDocument;

/**
 * A ViewerManager must manage the viewer world that is created around a
 * Transcription. It takes care of creating, destroying, enabling and
 * disabling viewers and media players ensuring that all connections between
 * controllers and listeners are as they should be.
 * @version Aug 2005 Identity removed
 */
public class ViewerManager2 {
	/** Holds value of property DOCUMENT ME! */
	private final static long SIGNAL_VIEWER_PERIOD = 50; // make these values setable?

	/** Holds value of property DOCUMENT ME! */
	private final static long TIME_LINE_VIEWER_PERIOD = 50;

	/** Holds value of property DOCUMENT ME! */
	private final static long INTERLINEAR_VIEWER_PERIOD = 100;

	/** Holds value of property DOCUMENT ME! */
	private final static long MEDIA_CONTROL_PANEL_PERIOD = 100;
	private ElanMediaPlayer masterMediaPlayer;
	private ElanMediaPlayer signalSourcePlayer;
	private SignalViewer signalViewer;
	private RecognizerPanel recognizerPanel;
	private TranscriptionImpl transcription;
	private Selection selection;
	private TimeScale timeScale;
	private ActiveAnnotation activeAnnotation;
	private TierOrder tierOrder;
	private ElanMediaPlayerController mediaPlayerController;
	private AnnotationDensityViewer annotationDensityViewer;
	private MediaPlayerControlSlider mediaPlayerControlSlider;
	private TimePanel timePanel;
	private MultiTierControlPanel multiTierControlPanel;
	private SignalViewerControlPanel signalViewerControlPanel;
	private List<ElanMediaPlayer> slaveMediaPlayers;
	private List<ElanMediaPlayer> disabledMediaPlayers;
	private Map<Object, Controller> controllers;
	private List<AbstractViewer> viewers;
	private List<AbstractViewer> enabledViewers;
	private List<AbstractViewer> disabledViewers;
	private MetadataViewer metadataViewer;
	private Map<AbstractViewer, GestureDispatcher> gestureMap;//use interface for dispatcher
	
	private GridViewer gridViewer;
	private TimeLineViewer timeLineViewer;
	private TextViewer textViewer;
	private LexiconEntryViewer lexiconViewer;
	private CommentViewer commentViewer;
	private TurnsAndSceneViewer turnsAndSceneViewer;
	private List<SubtitleViewer> subtitleViewers;	
	private InterlinearViewer interlinearViewer;	
	private TranscriptionViewer transcriptionViewer;	
	private String signalMediaURL;
	private List<String> audioPaths;
	private List<String> videoPaths;
	private List<String> otherMediaPaths;

	private VolumeManager volumeManager;
	
	/** The maximal number of video players in Elan */
	public static final int MAX_NUM_VIDEO_PLAYERS = 2; // will be 4
	/** The maximal number of audio players in Elan */
	public static final int MAX_NUM_AUDIO_PLAYERS = 1;

	/**
	 * Create a ViewerManager for a specific Transcription
	 *
	 * @param transcription the Transcription used in this ViewerManagers
	 *        universe
	 */
	public ViewerManager2(TranscriptionImpl transcription) {
		this.transcription = transcription;

		// as long as no real media player is set as master player use
		// an empty media player.
		masterMediaPlayer = new EmptyMediaPlayer(Integer.MAX_VALUE);

		// observables for this viewer universe
		selection = new Selection();
		timeScale = new TimeScale();
		activeAnnotation = new ActiveAnnotation();
		
		createTierOrderObject();
		//tierOrder = new TierOrder();

		// administration objects
		slaveMediaPlayers = new ArrayList<ElanMediaPlayer>();
		disabledMediaPlayers = new ArrayList<ElanMediaPlayer>();
		controllers = new HashMap<Object, Controller>();
		viewers = new ArrayList<AbstractViewer>();
		subtitleViewers = new ArrayList<SubtitleViewer>();
		enabledViewers = new ArrayList<AbstractViewer>();
		disabledViewers = new ArrayList<AbstractViewer>();
		gestureMap = new HashMap<AbstractViewer, GestureDispatcher>();
		
		audioPaths = new ArrayList<String>();
		videoPaths  = new ArrayList<String>();
		otherMediaPaths = new ArrayList<String>();
	}
	
	private void createTierOrderObject(){	
		tierOrder = new TierOrder(transcription);
		connectListener(tierOrder);
		List<TierImpl> tiers = transcription.getTiers();
		List<String> tierOrderList = Preferences.getListOfString("MultiTierViewer.TierOrder", 
			transcription);
		
		if (tierOrderList != null) {				
			// add (new) tiers, tiers that are not in the preferences
			for (int i = 0; i < tierOrderList.size(); i++) {
				Tier t = transcription.getTierWithId(tierOrderList.get(i));					
				if ( t == null ) {
					tierOrderList.remove(i);
					i--;
				}					
			}	
			
			for (Tier t : tiers) {
				if ( !tierOrderList.contains(t.getName()) ) {
					tierOrderList.add(t.getName()); 
				}					
			}				
		} else{
			tierOrderList = new ArrayList<String>();
			for (Tier t : tiers) {
				tierOrderList.add(t.getName());
			}				
		}
		
		if(tierOrderList instanceof ArrayList){
			tierOrder.setTierOrder(tierOrderList);
		} else {
			tierOrder.setTierOrder(new ArrayList<String>(tierOrderList));
		}			
	}

	/**
	 * Currently the returned Transcription is often used as a
	 * TranscriptionImpl.
	 *
	 * @return the Transcription object for this viewer universe
	 */
	public Transcription getTranscription() {
		return transcription;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the Selection object for this viewer universe
	 */
	public Selection getSelection() {
		return selection;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the TimeScale object for this viewer universe
	 */
	public TimeScale getTimeScale() {
		return timeScale;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the ActiveAnnotation object for this viewer universe
	 */
	public ActiveAnnotation getActiveAnnotation() {
		return activeAnnotation;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return the ActiveAnnotation object for this viewer universe
	 */
	public TierOrder getTierOrder() {
		return tierOrder;
	}


	/**
	 * Makes an ElanMediaPlayer master media player. The current master media
	 * player becomes a slave from the new master. The old master media player
	 * should be destroyed separately if it is no longer needed.
	 *
	 * @param player the ElanMediaPlayer that must become master player
	 */
	public void setMasterMediaPlayer(ElanMediaPlayer player) {
		if (player == masterMediaPlayer) {
			return;
		}

		// remember the rate of the current master
		float rate = masterMediaPlayer.getRate();

		// make sure all current master media player connections are removed
		// disconnect slave players
		for (ElanMediaPlayer mp : slaveMediaPlayers) {
			masterMediaPlayer.removeController(mp);
		}

		// disconnect the non-player controllers, TimeLine and PeriodicUpdate
		for (Controller c : controllers.values()) {
			masterMediaPlayer.removeController(c);
		}

		// remove the new master player from the slave or disabled list
		// and add the current master player to the slave list
		slaveMediaPlayers.remove(player);
		disabledMediaPlayers.remove(player);
		slaveMediaPlayers.add(masterMediaPlayer);

		// set the master
		masterMediaPlayer = player;

		// connect the new master media player to the viewer universe
		// reconnect slave players
		for (ElanMediaPlayer mp : slaveMediaPlayers) {
			masterMediaPlayer.addController(mp);
		}

		// reconnect the non-player controllers, TimeLine and PeriodicUpdate
		for (Controller c : controllers.values()) {
			masterMediaPlayer.addController(c);
		}

		// set the player in all existing viewers
		for (AbstractViewer v : viewers) {
			v.setPlayer(masterMediaPlayer);
		}

		// set the rate
		masterMediaPlayer.setRate(rate);

		// Make sure the user hears the new master media.
		// This may be overridden again in the next paragraph
		// (but this is set to get something sensible in any case).
		getVolumeManager().setSimpleVolumes();
		
		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	/**
	 * Creates an ElanMediaPlayer and connects it to the master media player
	 *
	 * @param mediaDescriptor a string representation of the media URL
	 *
	 * @return an ElanMediaPlayer that is connected to the master media player
	 *
	 * @throws NoPlayerException
	 */
	public ElanMediaPlayer createMediaPlayer(MediaDescriptor mediaDescriptor)
		throws NoPlayerException {
		// ask the player factory to create a player
		ElanMediaPlayer player = PlayerFactory.createElanMediaPlayer(mediaDescriptor);

		if (player == null) {
			return null;
		}

		addMediaPlayer(player);
		return player;
	}
	
	/**
	 * Creates an ElanMediaPlayer and connects it to the master media player.
	 * It first tries to create a player of the preferred type; if this fails 
	 * it will try to create a player the default way. 
	 *
	 * @param mediaDescriptor a string representation of the media URL
	 * @param preferredMediaFramework the preferred media framework
	 *
	 * @return an ElanMediaPlayer that is connected to the master media player
	 *
	 * @throws NoPlayerException
	 */
	public ElanMediaPlayer createMediaPlayer(MediaDescriptor mediaDescriptor, 
		String preferredMediaFramework) throws NoPlayerException {
		if (preferredMediaFramework == null) {
			return createMediaPlayer(mediaDescriptor);
		}
		// ask the player factory to create a player
		ElanMediaPlayer player = null;
		StringBuilder errors = new StringBuilder();
		try {
			if (preferredMediaFramework.equals(PlayerFactory.QT_MEDIA_FRAMEWORK)) {
				player = PlayerFactory.createQTMediaPlayer(mediaDescriptor);
			} else if (preferredMediaFramework.equals(PlayerFactory.JMF_MEDIA_FRAMEWORK)) {
				player = PlayerFactory.createJMFMediaPlayer(mediaDescriptor);
			} else if (preferredMediaFramework.equals(PlayerFactory.COCOA_QT)) {
				try {
					player = PlayerFactory.createCocoaQTMediaPlayer(mediaDescriptor);
				} catch (Exception ex) {
					errors.append(ex.getMessage() + "\n");
					player = PlayerFactory.createQTMediaPlayer(mediaDescriptor);
				}
			} else {
				try {
					return createMediaPlayer(mediaDescriptor); //default
				} catch (NoPlayerException np) {
					// then player is null
					errors.append(np.getMessage() + "\n");
				}
			}
		} catch (NoPlayerException npe) {
			errors.append(npe.getMessage() + "\n");
			try {
				return createMediaPlayer(mediaDescriptor);
			} catch (NoPlayerException np) {
				errors.append(np.getMessage() + "\n");
			}
		}
		

		if (player == null) {
			throw new NoPlayerException(errors.toString());
			//return null;
		}

		addMediaPlayer(player);
		return player;
	}

	/**
	 * Adds a custom made Elan media player to the list of slave media players 
	 * and connects it to the master media player.
	 * 
	 * @see #destroyMediaPlayer(ElanMediaPlayer)
	 * @param player the palyer to add
	 */
	public void addMediaPlayer(ElanMediaPlayer player) {
		if (player == null || slaveMediaPlayers.contains(player) ||
				player == masterMediaPlayer) {
			return;
		}

		ElanLocale.addElanLocaleListener(transcription, player);
		connectListener(player);
		player.setRate(masterMediaPlayer.getRate());
		getVolumeManager().setSubVolume(player, 0);

		// connect it to the master media player
		masterMediaPlayer.addController(player);

		// update the administration
		slaveMediaPlayers.add(player);	

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}
	
	/**
	 * Removes an ElanMediaPlayer from this viewer universe. Nothing will be
	 * done if an attempt is made to remove the master media player
	 *
	 * @param player the ElanMediaPlayer that must be destroyed
	 */
	public void destroyMediaPlayer(ElanMediaPlayer player) {
		if (player == masterMediaPlayer) {
			return;
		}

		ElanLocale.removeElanLocaleListener(player);
		disconnectListener(player);
		// disconnect the player from the master player
		masterMediaPlayer.removeController(player);

		// update the administration, the player is in one of two vectors
		slaveMediaPlayers.remove(player);
		disabledMediaPlayers.remove(player);
		
		player.cleanUpOnClose(); // sometimes crashed with NullPointerException
		player = null;

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	/**
	 * Enables an ElanMediaPlayer that was previously disabled
	 *
	 * @param player the ElanMediaPlayer that must be enabled.
	 */
	public void enableMediaPlayer(ElanMediaPlayer player) {
		// only enable a player that is a disabled player
		if (disabledMediaPlayers.contains(player)) {
			// reconnect the player to the master player
			masterMediaPlayer.addController(player);

			// update the administration
			slaveMediaPlayers.add(player);
			disabledMediaPlayers.remove(player);

			if (mediaPlayerController != null) {
				mediaPlayerController.updatePlayersVolumePanel();
			}
		}
	}

	/**
	 * Temporarily disconnects the player from the master media player. It can
	 * be reconnected by calling enableElanMediaPlayer The master media player
	 * will not be disabled.
	 *
	 * @param player the ElanMediaPlayer that must be disabled.
	 */
	public void disableMediaPlayer(ElanMediaPlayer player) {
		// only disable a player that is a slave player
		if (slaveMediaPlayers.contains(player)) {
			// disconnect the player from the master player
			masterMediaPlayer.removeController(player);

			// update the administration
			slaveMediaPlayers.remove(player);
			disabledMediaPlayers.add(player);

			if (mediaPlayerController != null) {
				mediaPlayerController.updatePlayersVolumePanel();
			}
		}
	}

	/**
	 * Enable all players except the master player.
	 */
	public void enableDisabledMediaPlayers() {
		for (ElanMediaPlayer mp : disabledMediaPlayers) {
			// reconnect the player to the master player
			masterMediaPlayer.addController(mp);
		}

		// update the administration
		slaveMediaPlayers.addAll(disabledMediaPlayers);
		disabledMediaPlayers.clear();

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	/**
	 * Disable all players except the master player.
	 */
	public void disableSlaveMediaPlayers() {
		for (ElanMediaPlayer mp : slaveMediaPlayers) {
			// disconnect the player from the master player
			masterMediaPlayer.removeController(mp);
		}

		// update the administration
		disabledMediaPlayers.addAll(slaveMediaPlayers);
		slaveMediaPlayers.clear();

		if (mediaPlayerController != null) {
			mediaPlayerController.updatePlayersVolumePanel();
		}
	}

	// this must be called from the outside, maybe viewer manager can derive
	// the signalSourcePlayer implicitly. The signal source player is an mpeg
	// or wav player that renders the audio for the wav data that is used in the signal viewer
	public void setSignalSourcePlayer(ElanMediaPlayer player) {
		signalSourcePlayer = player;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public long getSignalViewerOffset() {
		long offset = 0;

		if (signalSourcePlayer != null) {
			offset = signalSourcePlayer.getOffset();
		}

		return offset;
	}

	/* ALBERT
	 */
	public SignalViewer getSignalViewer() {
		return signalViewer;
	}
	
	/* END ALBERT
	 */
	/**
	 * DOCUMENT ME!
	 *
	 * @param player DOCUMENT ME!
	 * @param offset DOCUMENT ME!
	 */
	public void setOffset(ElanMediaPlayer player, long offset) {
		player.setOffset(offset);

		if ((player == signalSourcePlayer) && (signalViewer != null)) {
			signalViewer.setOffset(offset);
		}
		transcription.setChanged();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the ElanMediaPlayer that is the current master media player.
	 */
	public ElanMediaPlayer getMasterMediaPlayer() {
		return masterMediaPlayer;
	}
	
	/**
	 * Returns the collection of slave mediaplayers.
	 * 
	 * @return the collection of slave mediaplayers
	 */
	public List<ElanMediaPlayer> getSlaveMediaPlayers() {
		return slaveMediaPlayers;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return the controlpanel for the master media player
	 */
	public ElanMediaPlayerController getMediaPlayerController() {
		if (mediaPlayerController == null) {
			mediaPlayerController = new ElanMediaPlayerController(this);

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(mediaPlayerController, controller);
			connect(mediaPlayerController);
			viewers.add(mediaPlayerController);
			enabledViewers.add(mediaPlayerController);
		}

		return mediaPlayerController;
	}
	
	/**
	 * Remove the transcription Viewer
	 */
	public void destroyElanMediaPlayerController() {
		if(mediaPlayerController != null){
			destroyViewer(mediaPlayerController);
			mediaPlayerController = null;
		}
	}

	public VolumeManager getVolumeManager() {
		if (volumeManager == null) {
			volumeManager = new VolumeManager(this);
		}
		
		return volumeManager;
	}
	
	public void destroyVolumeManager() {
		if (volumeManager != null) {
			volumeManager = null;
		}
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public MediaPlayerControlSlider getMediaPlayerControlSlider() {
		if (mediaPlayerControlSlider == null) {
			mediaPlayerControlSlider = new MediaPlayerControlSlider();

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(mediaPlayerControlSlider, controller);
			connect(mediaPlayerControlSlider);
			viewers.add(mediaPlayerControlSlider);
			enabledViewers.add(mediaPlayerControlSlider);
		}

		return mediaPlayerControlSlider;
	}

	/**
	 * This creates a annotation density viewer if it does not exist.
	 *
	 * @return the AnnotationDensityViewer
	 */
	public AnnotationDensityViewer createAnnotationDensityViewer() {
		if (annotationDensityViewer == null) {
			annotationDensityViewer = new AnnotationDensityViewer(transcription);
			
			annotationDensityViewer.setTierOrderObject(tierOrder);
			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(annotationDensityViewer, controller);
			connect(annotationDensityViewer);
			viewers.add(annotationDensityViewer);
			enabledViewers.add(annotationDensityViewer);
		}

		return annotationDensityViewer;
	}
	
	/**
	 * HS Sep 2013 the getter doesn't create the viewer anymore. There's a separate
	 * create method (like for most other viewers).
	 *
	 * @return the density viewer or null
	 */
	public AnnotationDensityViewer getAnnotationDensityViewer() {
		return annotationDensityViewer;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public TimePanel getTimePanel() {
		if (timePanel == null) {
			timePanel = new TimePanel();

			PeriodicUpdateController controller =
				getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD);
			controllers.put(timePanel, controller);
			connect(timePanel);
			viewers.add(timePanel);
			enabledViewers.add(timePanel);
		}

		return timePanel;
	}
	
	/**
	 * Creates a Viewer for the specified fully qualified class name.
	 * 
	 * @param className the class name
	 * @param controllerPeriod the requested period for controller updates
	 * 
	 * @return the viewer
	 */
	public Viewer createViewer(String className, long controllerPeriod) {
	    Viewer viewer = null; 
	    
	    try {
	        viewer = (Viewer) Class.forName(className).newInstance();
	        viewer.setViewerManager(this);
	        if (viewer instanceof AbstractViewer) {
		    		PeriodicUpdateController controller = getControllerForPeriod(controllerPeriod);
		    		controllers.put(viewer, controller);
		    		connect((AbstractViewer) viewer);
	
		    		viewers.add((AbstractViewer)viewer);
		    		enabledViewers.add((AbstractViewer)viewer);
	        } else if (viewer instanceof ControllerListener) {
	            getControllerForPeriod(controllerPeriod).addControllerListener(
	                    (ControllerListener) viewer);
	        } else {
	            // special case for syntax viewer?
	            /*
	            try {
	                Method method = viewer.getClass().getDeclaredMethod("getControllerListener", null);
	                ControllerListener listener = (ControllerListener) method.invoke(viewer, null);
	                getControllerForPeriod(controllerPeriod).addControllerListener(
	                        listener);	                
	            } catch (Exception e){
	                System.out.println("Could not connect controller: " + e.getMessage());
	            }
	            */
	        }
	    } catch (Exception e) {
	        System.out.println("Could not create viewer: " + className + ": " + e.getMessage());
	    }
	    
	    return viewer;
	}
	
	/**
	 * Connection method to be used by external objects that want to connect a
	 * listener
	 *
	 * @param listener
	 */
	public void connectListener(Object listener) {
		if (listener instanceof ControllerListener) {
			getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD).addControllerListener(
				(ControllerListener) listener);
		}

		if (listener instanceof SelectionListener) {
			selection.addSelectionListener((SelectionListener) listener);
		}

		if (listener instanceof ActiveAnnotationListener) {
			activeAnnotation.addActiveAnnotationListener((ActiveAnnotationListener) listener);
		}

		if (listener instanceof TimeScaleListener) {
			timeScale.addTimeScaleListener((TimeScaleListener) listener);
		}

		if (listener instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).addACMEditListener(
					(ACMEditListener) listener);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (listener instanceof PreferencesListener) {
			Preferences.addPreferencesListener(transcription, (PreferencesListener) listener);
		}
	}

	/**
	 * Connection method to be used by external objects that want to connect a
	 * listener
	 *
	 * @param listener
	 */
	public void disconnectListener(Object listener) {
		if (listener instanceof ControllerListener) {
			getControllerForPeriod(MEDIA_CONTROL_PANEL_PERIOD).removeControllerListener(
				(ControllerListener) listener);
		}

		if (listener instanceof SelectionListener) {
			selection.removeSelectionListener((SelectionListener) listener);
		}

		if (listener instanceof ActiveAnnotationListener) {
			activeAnnotation.removeActiveAnnotationListener((ActiveAnnotationListener) listener);
		}

		if (listener instanceof TimeScaleListener) {
			timeScale.removeTimeScaleListener((TimeScaleListener) listener);
		}

		if (listener instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).removeACMEditListener(
					(ACMEditListener) listener);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (listener instanceof PreferencesListener) {
			Preferences.removePreferencesListener(transcription, (PreferencesListener) listener);
		}
	}
	
	/**
	 * DOCUMENT ME! 
	 *
	 * @return the controlpanel for the wav files
	 */
	public SignalViewerControlPanel getSignalViewerControlPanel() {
		if(signalViewerControlPanel == null){
			return createSignalViewerControlPanel();
		}
		return signalViewerControlPanel;
	}
	
	public SignalViewerControlPanel createSignalViewerControlPanel(){
		if(signalViewerControlPanel == null){
			signalViewerControlPanel = new SignalViewerControlPanel(
				((ElanFrame2)ELANCommandFactory.getRootFrame(transcription)).getWaveFormViewerMenuManager());
		}
		
		return signalViewerControlPanel;
	}
	
	public void destroySignalViewerControlPanel(){
		if(signalViewerControlPanel != null){
			signalViewerControlPanel = null;
		}
	}

	/**
	 * DOCUMENT ME! 
	 *
	 * @return the controlpanel for the Transcriptions tiers
	 */
	public MultiTierControlPanel getMultiTierControlPanel() {
		if(multiTierControlPanel == null){
			return createMultiTierControlPanel();
		}
		return multiTierControlPanel;
	}
	
	public MultiTierControlPanel createMultiTierControlPanel(){
		if(multiTierControlPanel == null){
			multiTierControlPanel = new MultiTierControlPanel(transcription, tierOrder);
			//multiTierControlPanel.setTierOrderObject(tierOrder);
			ElanLocale.addElanLocaleListener(transcription, multiTierControlPanel);
			Preferences.addPreferencesListener(transcription, multiTierControlPanel);
		}
		
		return multiTierControlPanel;
	}
	
	public void destroyMultiTierControlPanel(){
		if(multiTierControlPanel != null){
			Preferences.removePreferencesListener(transcription,multiTierControlPanel);
			ElanLocale.removeElanLocaleListener(multiTierControlPanel);
			multiTierControlPanel = null;
		}
	}

	/**
	 * Creates a TimeLineViewer that is connected to the Viewer universe.
	 *
	 * @return the TimeLineViewer for the Transcription in this ViewerManager
	 */
	public TimeLineViewer createTimeLineViewer() {
		timeLineViewer = new TimeLineViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(timeLineViewer, controller);
		connect(timeLineViewer);

		viewers.add(timeLineViewer);
		enabledViewers.add(timeLineViewer);

		return timeLineViewer;
	}
	
	/**
	 * Returns the TimeLineViewer.
	 *
	 * @return the TimeLineViewer for the Transcription in this ViewerManager or null
	 */
	public TimeLineViewer getTimeLineViewer() {
		return timeLineViewer;
	}

	/**
	 * Creates a InterlinearViewer that is connected to the Viewer universe.
	 *
	 * @return the InterlinearViewer for the Transcrption in this ViewerManager, can be null
	 */
	public InterlinearViewer createInterlinearViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.INTERLINEAR_VIEWER, null);
	    if (val == null || val) {
	    	if(interlinearViewer == null){				
	    		interlinearViewer = new InterlinearViewer(transcription);
	    		PeriodicUpdateController controller = getControllerForPeriod(INTERLINEAR_VIEWER_PERIOD);
	    		controllers.put(interlinearViewer, controller);
	    		connect(interlinearViewer);

	    		viewers.add(interlinearViewer);
	    		enabledViewers.add(interlinearViewer);
			}
	    	return interlinearViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 *Returns the interlinear viewer, can be null
	 *
	 * @return the InterlinearViewer, can be null
	 */
	public InterlinearViewer getInterlinearViewer() {		
		return interlinearViewer;
	}	
	
	
	public String getSignalMediaURL(){
		return signalMediaURL;
	}
	
	/**Set the paths the audio, if linked
	 * 
	 * @return audioPath the path of the audio
	 */
	public void setAudioPaths(List<String> audioPath) {
		audioPaths.clear();
		if (audioPath != null) {
			for (String path : audioPath) {
				path = FileUtility.urlToAbsPath(path);
				if(!audioPaths.contains(path)){
					audioPaths.add(path);
				}				
			}
		}
	}
	
	/**Returns the paths the audio, if linked. Can be null
	 * 
	 * @return audioPaths the path of the audio, can be null
	 */
	public List<String> getAudioPaths(){
		return audioPaths;
	}
	
	/**
	 * Set the paths the video, if linked
	 * 
	 * @param videoPath
	 */	 
	public void setVideoPaths(List<String> videoPath) {
		videoPaths.clear();
		if (videoPath != null) {
			for (String path : videoPath) {
				path = FileUtility.urlToAbsPath(path);
				if (!videoPaths.contains(path)) {
					videoPaths.add(path);
				}				
			}
		}
	}
	
	/**Returns the paths the video, if linked. Can be null
	 * 
	 * @return the paths of the video files, can be null
	 */
	public List<String> getVideoPaths(){
		return videoPaths;
	}

	/**
	 * Set the paths of secondary linked media files
	 * 
	 * @param the list of paths to other types of media files
	 */	 
	public void setOtherMediaPaths(List<String> otherPath) {
		otherMediaPaths.clear();
		if (otherPath != null) {
			for (String path : otherPath) {
				path = FileUtility.urlToAbsPath(path);
				if (!otherMediaPaths.contains(path)) {
					otherMediaPaths.add(path);
				}				
			}
		}
	}
	
	/**Returns the paths of secondary linked media files. Can be null
	 * 
	 * @return the paths of secondary linked media files
	 */
	public List<String> getOtherMediaPaths(){
		return otherMediaPaths;
	}
	
	public SignalViewer createSignalViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.SIGNAL_VIEWER, null);
	    if (val == null || val) {
	    	if(signalViewer == null){
	    		if(signalMediaURL != null){
	    			createSignalViewer(signalMediaURL);
	    			signalViewer.setOffset(getSignalViewerOffset());
	    			signalViewer.preferencesChanged();
	    		}
	    	}	    	
	    	return signalViewer;
	    }else {
	    	return null;
	    }
	}
	

	/**
	 * Creates a SignalViewer that is connected to the Viewer universe.
	 *
	 * @param mediaURL String that represents the signal media URL
	 *
	 * @return the SignalViewer for the media URL
	 */
	public SignalViewer createSignalViewer(String mediaURL) { // throw exception ?
		SignalViewer viewer = null;
		if(mediaURL != null){
			signalMediaURL = mediaURL;
		}
	
		// URL or String  problem to be solved, is a problem for rtsp://
		// the SignalViewer dows not work with streaming (rtsp://)
		if (mediaURL.startsWith("rtsp://")) {
			return viewer; // == null
		}
		
		 Boolean val = Preferences.getBool(ELANCommandFactory.SIGNAL_VIEWER, null);
		 if (val != null && !val) {
			 return viewer;
		 }

		viewer = new SignalViewer(mediaURL);	
		
		PeriodicUpdateController controller = getControllerForPeriod(SIGNAL_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		// something to set the offset
		viewers.add(viewer);
		enabledViewers.add(viewer);
		signalViewer = viewer; // a problem when there is more than one signal viewer		

		return viewer;
	}	
	
	public void updateSignalViewerMedia(String mediaURL){
		if(mediaURL != null){
			signalMediaURL = mediaURL;
		}
	}
	
	public TranscriptionViewer createTranscriptionViewer() {
		transcriptionViewer = new TranscriptionViewer(this);
		PeriodicUpdateController controller = getControllerForPeriod(ViewerManager2.MEDIA_CONTROL_PANEL_PERIOD);
		controllers.put(transcriptionViewer, controller);
		connect(transcriptionViewer);

		viewers.add(transcriptionViewer);
		enabledViewers.add(transcriptionViewer);

		return transcriptionViewer;
	}
	
	/**
	 *Returns the transcription viewer, can be null
	 *
	 * @return the transcriptionViewer, can be null
	 */
	public TranscriptionViewer getTranscriptionViewer() {		
		return transcriptionViewer;
	}	

	/**
	 * Creates a GridViewer that is connected to the Viewer universe but not
	 * yet connected to a certain Tier.
	 *
	 * @return the GridViewer, can return null
	 */
	public GridViewer createGridViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.GRID_VIEWER, null);
	    if (val == null || val) {
	    	if(gridViewer == null){				
	    		gridViewer = new GridViewer();
				connect(gridViewer);
				viewers.add(gridViewer);
				enabledViewers.add(gridViewer);
			}
	    	return gridViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 *Returns the grid viewer, can be null
	 *
	 * @return the GridViewer, can be null
	 */
	public GridViewer getGridViewer() {		
		return gridViewer;
	}	
	

	/**
	* Creates a GridViewer that is connected to the Viewer universe but not
	* yet connected to a certain Tier.
	*
	* @return the GridViewer
	*/
	public ElanResultViewer createSearchResultViewer() {
		ElanResultViewer viewer = new ElanResultViewer();
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	    * Creates a SubtitleViewer that is connected to the Viewer universe but
	    * not yet connected to a certain Tier.
	    *
	    * @return the SubtitleViewer
	    */
	public SubtitleViewer createSubtitleViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.SUBTITLE_VIEWER, null);
	    if (val == null || val) {
	    	SubtitleViewer subtitleViewer = new SubtitleViewer();
			connect(subtitleViewer);

			viewers.add(subtitleViewer);
			enabledViewers.add(subtitleViewer);
			
			subtitleViewers.add(subtitleViewer);
			
	    	return subtitleViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the subtitle viewer, can be null
	 *
	 * @return the SubtitleViewer, can be null
	 */
	public List<SubtitleViewer> getSubtitleViewers() {		
		return subtitleViewers;
	}

	/**
	 * Creates a TextViewer that is connected to the Viewer universe but not
	 * yet connected to a certain Tier.
	 *
	 * @return the TextViewer, can be null
	 */
	public TextViewer createTextViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.TEXT_VIEWER, null);
	    if (val == null || val) {
	    	if(textViewer == null){				
	    		textViewer =  new TextViewer();
				connect(textViewer);
				viewers.add(textViewer);
				enabledViewers.add(textViewer);
			}
	    	return textViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the text viewer, can be null.
	 * 
	 * @return the textViewer, can be null
	 */
	public TextViewer getTextViewer() {		
		return textViewer;
	}

	/**
	 * Creates a SegmentationViewer that is connected to the Viewer universe.
	 *
	 * @return a SegmentationViewer for the Transcription in this ViewerManager
	 */
	public SegmentationViewer2 createSegmentationViewer() {
		SegmentationViewer2 viewer = new SegmentationViewer2(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);

		return viewer;
	}

	/**
	 * Creates a TimeSeriesViewer that is connected to the Viewer universe.
	 * 
	 * @return a TimeSeriesViewer for the Transcription in this ViewerManager
	 */
	public TimeSeriesViewer createTimeSeriesViewer() {
		TimeSeriesViewer viewer = new TimeSeriesViewer(transcription);
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(viewer, controller);
		connect(viewer);

		viewers.add(viewer);
		enabledViewers.add(viewer);
		return viewer;
	}	
	
	public void connectViewer(AbstractViewer viewer, boolean connect){
		if(viewer == null){
			return;
		}
		if(connect){
			if(viewer instanceof TimeSeriesViewer){
				controllers.put(viewer, getControllerForPeriod(TIME_LINE_VIEWER_PERIOD));
			}else if(viewer instanceof SignalViewer){
				controllers.put(viewer, getControllerForPeriod(SIGNAL_VIEWER_PERIOD));
			}
			connect(viewer);
			viewers.add(viewer);
			enabledViewers.add(viewer);
			disabledViewers.remove(viewer);
		} else {
			disconnect(viewer , false);
			enabledViewers.remove(viewer);
			disabledViewers.add(viewer);
		}
	}
	
	/**
	 * Creates and connects a metadata viewer.
	 * 
	 * @return the metadata viewer
	 */
	public MetadataViewer createMetadataViewer() {
		Boolean val = Preferences.getBool(ELANCommandFactory.METADATA_VIEWER, null);
	    if (val == null || val) {
	    	if(metadataViewer == null){				
	    		metadataViewer = new MetadataViewer(this);
	    		ElanLocale.addElanLocaleListener(transcription, metadataViewer);
	    		Preferences.addPreferencesListener(transcription, metadataViewer);
				}
	    	return metadataViewer;
	    }else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the metadata viewer, can be null
	 * 
	 * @return the metadata viewer, can be null
	 */
	public MetadataViewer getMetadataViewer() {
		return metadataViewer;
	}
	
	/**
	 * Removes an MetaData from this viewer universe. 
	 * @param metadataViewer 
	  
	 */
	public void destroyMetaDataViewer() {
		if(metadataViewer != null){
			ElanLocale.removeElanLocaleListener(metadataViewer);
			Preferences.removePreferencesListener(transcription, metadataViewer);
			metadataViewer = null;
		}
	}
	
	/**
	 * Note: use media descriptor instead of path?
	 * Note: if there is nothing to connect this could also be done in e.g. ElanFrame
	 * 
	 * @return a panel for selection and configuration of an audio based recognizer 
	 */
	public RecognizerPanel createRecognizerPanel() {		
		Boolean val = Preferences.getBool(ELANCommandFactory.RECOGNIZER, null);
	    if (val == null || val) {
	    	if(recognizerPanel == null){
	    		boolean a = audioPaths != null && !audioPaths.isEmpty();
	    		boolean v = videoPaths != null && !videoPaths.isEmpty();
	    		boolean o = otherMediaPaths != null && !otherMediaPaths.isEmpty();
				if (a || v || o) {
					recognizerPanel = new RecognizerPanel(this);
					if (a) {
						recognizerPanel.setAudioFilePaths(audioPaths);
					}
					if (v) {
						recognizerPanel.setVideoFilePaths(videoPaths);
					}
					if (o) {
						recognizerPanel.setOtherFilePaths(otherMediaPaths);
					}
					
					// connect to anything??
					ElanLocale.addElanLocaleListener(transcription, recognizerPanel);
				}
	    	}
	    	return recognizerPanel;
	    } else {
	    	return null;
	    }
	}
	
	/**
	 * Returns the recognizer panel, can be null.
	 * 
	 * @return the recognizer panel, can be null
	 */
	public RecognizerPanel getRecognizerPanel() {
		return recognizerPanel;
	}	
	
	/**
	 * Creates and connects a LexiconEntryViewer.
	 * 
	 * @return a lexicon entry viewer
	 */
	public LexiconEntryViewer createLexiconEntryViewer() {
		lexiconViewer = new LexiconEntryViewer();
		
		connect(lexiconViewer);
		
		viewers.add(lexiconViewer);
		enabledViewers.add(lexiconViewer);

		return lexiconViewer;
	}
	
	/**
	 * Returns the lexicon viewer, can be null.
	 * 
	 * @return the lexicon viewer, can be null
	 */
	public LexiconEntryViewer getLexiconViewer() {
		return lexiconViewer;
	}
	
	/**
	 * Creates and connects a CommentViewer.
	 * 
	 * @return a comment viewer
	 */
	public CommentViewer createCommentViewer(Transcription transcription) {
		commentViewer = new CommentViewer((TranscriptionImpl) transcription);
		// We want to know when the crosshair changes its location
		PeriodicUpdateController controller = getControllerForPeriod(TIME_LINE_VIEWER_PERIOD);
		controllers.put(commentViewer, controller);
		connect(commentViewer);
		
		viewers.add(commentViewer);
		enabledViewers.add(commentViewer);
		
		// We want to know when the active tier changes
		if (multiTierControlPanel != null) {
			multiTierControlPanel.addViewer(commentViewer);
		}

		return commentViewer;
	}
	
	/**
	 * Returns the comment viewer, can be null.
	 * 
	 * @return the comment viewer, can be null
	 */
	public CommentViewer getCommentViewer() {
		return commentViewer;
	}
	
	
	/**
	 * Creates and connects the single layer viewer
	 *  
	 * @return the single layer viewer
	 */
	public TurnsAndSceneViewer createTurnsAndSceneViewer() {
		turnsAndSceneViewer = new TurnsAndSceneViewer(transcription);
		
		PeriodicUpdateController controller = getControllerForPeriod(INTERLINEAR_VIEWER_PERIOD);
		controllers.put(turnsAndSceneViewer, controller);
		connect(turnsAndSceneViewer);
		viewers.add(turnsAndSceneViewer);
		enabledViewers.add(turnsAndSceneViewer);
		
		return turnsAndSceneViewer;
	}
	
	/**
	 * @return the turnsAndSceneViewer, can be null
	 */
	public TurnsAndSceneViewer getTurnsAndSceneViewer() {
		return turnsAndSceneViewer;
	}
	
	public void destroyPanel(String panelName){
		if(panelName == null){
			return;
		}
		if (panelName.equals(ELANCommandFactory.RECOGNIZER)) {
			if(recognizerPanel != null){
				ElanLocale.removeElanLocaleListener(recognizerPanel);
				recognizerPanel = null;
			}
        }
	}

	
	/**
	 * Registers (Tier-) time line controllers to viewer 
	 * @param viewer the viewer for which the controllers must be set
	 * @param tierNames array of Tier Names 
	 */
	public void setControllersForViewer(AbstractViewer viewer, String[] tierNames){
	    try{
	        Tier[] tiers = new Tier[tierNames.length];
	        for(int i=0; i<tierNames.length; i++){
	            tiers[i] = transcription.getTierWithId(tierNames[i]);
	        }
	        setControllersForViewer(viewer, tiers);
	    }
	    catch(Exception e){}
	}
	
	public void setTierForViewer(SingleTierViewer viewer, Tier tier){
	    if(viewer instanceof AbstractViewer) {
			setControllersForViewer((AbstractViewer) viewer, tier == null ? new Tier[0] : new Tier[]{tier});
		}
	    viewer.setTier(tier);
	}
	
	/**
	 * Registers (Tier-) time line controllers to viewer
	 * @param viewer the viewer for which the controllers must be set
	 * @param tiers array of tiers that must be set
	 */
	public void setControllersForViewer(AbstractViewer viewer, Tier[] tiers) {
		if (viewer == null) {
			return;
		}

	    // disconnect an old controller if it exists
        disconnectController(viewer, true);

	    //TODO: connect to all tiers, not just first (-> change storage of controllers)
	    // connect the viewer to the right controller
	    if (tiers != null && tiers.length > 0) {
	        TimeLineController controller = getControllerForTier(tiers[0]);
	        controller.addControllerListener(viewer);
	        controllers.put(viewer, controller);

            // set the controller in the started state if player is playing
            if (masterMediaPlayer.isPlaying()) {
                controller.start();
            }
        }
	}

	/**
	 * Creates a SingleTierViewerPanel and connects it to the Transcription as
	 * an ACMEditListener.
	 *
	 * @return the new SingleTierViewerPanel
	 */
	public SingleTierViewerPanel createSingleTierViewerPanel() {
		SingleTierViewerPanel panel = new SingleTierViewerPanel(this);
		tierOrder.addTierOrderListener(panel);

		try {
			((ACMEditableDocument) transcription).addACMEditListener(panel);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ElanLocale.addElanLocaleListener(transcription, panel);

		return panel;
	}

	/**
	 * Destroys (disconnects) the SingleTierViewerPanel as ACMEditListener from
	 * the Transcription.
	 *
	 * @param panel the SingleTierViewerPanel to be destroyed (disconnected).
	 */
	public void destroySingleTierViewerPanel(SingleTierViewerPanel panel) {
		if(panel == null){
			return;
		}
		tierOrder.removeTierorderListener(panel);
		try {
			((ACMEditableDocument) transcription).removeACMEditListener(panel);
			
			Preferences.removePreferencesListener(transcription, panel);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ElanLocale.removeElanLocaleListener(panel);
	}

	/**
	 * Removes an AbstractViewer completely from the viewer universe
	 *
	 * @param viewer the AbstractViewer that must be destroyed
	 */
	public void destroyViewer(AbstractViewer viewer) {
		if (enabledViewers.contains(viewer)) {
			enabledViewers.remove(viewer);
		}
		if (disabledViewers.contains(viewer)){
			disabledViewers.remove(viewer);
		}
	
		disconnect(viewer, true);
		viewers.remove(viewer);	
	}
	
	/**
	 * Remove the transcription Viewer
	 */
	public void destroyTranscriptionViewer() {
		if(transcriptionViewer != null){
			destroyViewer(transcriptionViewer);
			transcriptionViewer = null;
		}
	}
	
	/**
	 * Removes an Grid Viewer
	 */
	public void destroyGridViewer() {
		if(gridViewer != null){
			destroyViewer(gridViewer);
			gridViewer = null;
		}
	}
	
	/**
	 * Removes an Text Viewer	   
	 */
	public void destroyTextViewer() {
		if(textViewer != null){
			destroyViewer(textViewer);
			textViewer = null;
		}
	}
	
	/**
	 * Removes an subtitle Viewers	 
	 */
	public void destroySubtitleViewers() {
		if(subtitleViewers != null){
			for (int i = 0; i < subtitleViewers.size(); i++) {
				destroyViewer(subtitleViewers.get(i));
			}
			subtitleViewers.clear();
			//subtitleViewers = null;
		}
	}
	
	/**
	 * Removes an Lexicon Viewer	  
	 */
	public void destroyLexiconViewer() {
		if(lexiconViewer != null){
			destroyViewer(lexiconViewer);
			lexiconViewer = null;
		}
	}
		
	/**
	 * Removes a Comment Viewer	  
	 */
	public void destroyCommentViewer() {
		if (commentViewer != null) {
			destroyViewer(commentViewer);
			commentViewer = null;
		}
	}
	
	/**
	 * Removes an Signal Viewer	
	 */
	public void destroySignalViewer() {
		if(signalViewer != null){
			destroyViewer(signalViewer);
			signalViewer = null;
			
			destroySignalViewerControlPanel();
		}
	}
	
	/**
	 * Removes an Interlinear Viewer	
	 */
	public void destroyInterlinearViewer() {
		if(interlinearViewer != null){
			destroyViewer(interlinearViewer);
			interlinearViewer = null;
		}		
	}
	
	/**
	 * Removes an Timeline Viewer	
	 */
	public void destroyTimeLineViewer() {
		if(timeLineViewer != null){
			destroyViewer(timeLineViewer);
			timeLineViewer = null;
		}		
	}		
	
	/**
	 * Removes an Annotation Density Viewer	
	 */
	public void destroyAnnotationDensityViewer() {
		if(annotationDensityViewer != null){
			destroyViewer(annotationDensityViewer);
			annotationDensityViewer = null;
		}		
	}

	/**
	 * Removes an Media Player Control Slider
	 */
	public void destroyMediaPlayerControlSlider() {
		if(mediaPlayerControlSlider != null){
			destroyViewer(mediaPlayerControlSlider);
			mediaPlayerControlSlider = null;
		}		
	}
	
	/**
	 * Removes an Time Panel
	 */
	public void destroyTimePanel() {
		if(timePanel != null){
			destroyViewer(timePanel);
			timePanel = null;
		}		
	}
	/**
	 * Disconnects an AbstractViewer from the viewer universe in such a manner
	 * that it can be reconnected.
	 *
	 * @param viewer the AbstractViewer that must be disabled
	 */
	public void disableViewer(AbstractViewer viewer) {
		if (enabledViewers.contains(viewer)) {
			enabledViewers.remove(viewer);
			disconnect(viewer, false);
			disabledViewers.add(viewer);
		}
	}

	/**
	 * Reconnects an AbstractViewer to the viewer universe from which it was
	 * temporarily disconnected.
	 *
	 * @param viewer the AbstractViewer that must be enabled
	 */
	public void enableViewer(AbstractViewer viewer) {
		if (disabledViewers.contains(viewer)) {
			disabledViewers.remove(viewer);
			connect(viewer);
			enabledViewers.add(viewer);
		}
	}
	
	/**
	 * Sets a flag on existing players whether frame forward/backward always jumps
	 * to the beginning of the next/previous frame or jumps with the ms per frame value.
	 * 
	 * @param stepsToBegin if true frame forward/backward jumps to begin of next/previous
	 * frame
	 */
	public void setFrameStepsToBeginOfFrame(boolean stepsToBegin) {
		if (masterMediaPlayer != null) {
			masterMediaPlayer.setFrameStepsToFrameBegin(stepsToBegin);
		}
		for (ElanMediaPlayer player : slaveMediaPlayers) {
			player.setFrameStepsToFrameBegin(stepsToBegin);
		}
		for (ElanMediaPlayer player : disabledMediaPlayers) {
			player.setFrameStepsToFrameBegin(stepsToBegin);
		}
	}

	/**
	 * Tries to make sure resources are freed, especially the created 
	 * media players might have to release resources.
	 * Preliminary....
	 */
	public void cleanUpOnClose() {
		if(masterMediaPlayer != null && 
				masterMediaPlayer.isPlaying()){
			masterMediaPlayer.stop();
	    }
		
		for (ElanMediaPlayer player : slaveMediaPlayers) {
			player.cleanUpOnClose();
		}
		for (ElanMediaPlayer player : disabledMediaPlayers) {
			player.cleanUpOnClose();
		}
		if (masterMediaPlayer != null) {
			masterMediaPlayer.cleanUpOnClose();
		}
		for (int i = 0; i < viewers.size(); i++) {
			AbstractViewer viewer = viewers.get(i);
			disconnect(viewer, true);
			if (viewer instanceof ACMEditListener && transcription != null) {
				((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) viewer);
			}
			if (viewer instanceof TimeLineViewer) {
				((TimeLineViewer)viewer).setTranscription(null);
			}
			if (viewer instanceof InterlinearViewer) {
				((InterlinearViewer)viewer).setTranscription(null);
			}
		}
		if (recognizerPanel != null) {
			// it will be removed as locale listener when the transcription is removed
		}
		enabledViewers.clear();
		viewers.clear();
		disabledViewers.clear();
	}
	
	/**
	 * Connect an AbstractViewer to the Viewer Universe.
	 *
	 * @param viewer the viewer that must be connected;
	 */
	private void connect(AbstractViewer viewer) {
		// observables for all viewers
		viewer.setPlayer(masterMediaPlayer);
		viewer.setSelectionObject(selection);
		selection.addSelectionListener(viewer);
		viewer.setActiveAnnotationObject(activeAnnotation);
		activeAnnotation.addActiveAnnotationListener(viewer);
		ElanLocale.addElanLocaleListener(transcription, viewer);

		viewer.setViewerManager(this);

		// only for viewers that show trancription data
		if (viewer instanceof ACMEditListener) {
			try {
				((ACMEditableDocument) transcription).addACMEditListener((ACMEditListener) viewer);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// only for viewers that share the time scale
		if (viewer instanceof TimeScaleUser) {
			((TimeScaleUser) viewer).setGlobalTimeScale(timeScale);
			timeScale.addTimeScaleListener((TimeScaleListener) viewer);
		}
		
		if (viewer instanceof GesturesListener) {
			if (SystemReporting.isMacOS()) {
				GestureMacDispatcher disp = new GestureMacDispatcher((JComponent) viewer, (GesturesListener) viewer);
				gestureMap.put(viewer, disp);
				disp.connect();
			}
		}

		// only multi tier viewers are connected to the multi tier control panel
		if (viewer instanceof MultiTierViewer && multiTierControlPanel != null) {
			multiTierControlPanel.addViewer((MultiTierViewer) viewer);
		}

		if (viewer instanceof PreferencesListener) {
			Preferences.addPreferencesListener(transcription, viewer);
		}
		// if there is a controller associated with this viewer connect them
		Controller controller = controllers.get(viewer);

		if (controller != null) {
			controller.addControllerListener(viewer);

			// make sure the viewer is in sync
			viewer.controllerUpdate(new TimeEvent(controller));
		}		
	}

	/**
	 * Disconnect an AbstractViewer from the Viewer Universe.
	 *
	 * @param viewer the viewer that must be disconnected;
	 * @param finalDisconnection flag that tells if the viewer might need to be
	 *        reconnected
	 */
	private void disconnect(AbstractViewer viewer, boolean finalDisconnection) {
		// observables for all viewers
		viewer.setPlayer(null);
		viewer.setSelectionObject(null);
		selection.removeSelectionListener(viewer);
		viewer.setActiveAnnotationObject(null);
		activeAnnotation.removeActiveAnnotationListener(viewer);
		ElanLocale.removeElanLocaleListener(viewer);
		
		viewer.setViewerManager(null);
		
		// only for viewers that show trancription data
		// TEMPRORARY? disabled because disconnected viewers are not aware of changes in teh edited
		// document after they wake up. Keeoing them connected looks like the easiest solution.
		// DO NOT disable the same block in the connect method because that takes care of the
		// first time connection.

		if (viewer instanceof ACMEditListener) {
			try {
		       ((ACMEditableDocument) transcription).removeACMEditListener((ACMEditListener) viewer);
			} catch (Exception e) {
		       e.printStackTrace();
			}
		}		

		// only for viewers that share the time scale
		if (viewer instanceof TimeScaleUser) {
			timeScale.removeTimeScaleListener((TimeScaleUser) viewer);
		}

		if (viewer instanceof GesturesListener) {
			if (SystemReporting.isMacOS()) {
				GestureDispatcher disp = gestureMap.remove(viewer);
				if (disp != null) {
					disp.disconnect();
				}

			}
		}
		
		// only multi tier viewers are disconnected to the multi tier control panel
		if (viewer instanceof MultiTierViewer && multiTierControlPanel != null) {
			multiTierControlPanel.removeViewer((MultiTierViewer) viewer);
		}

		if (viewer instanceof PreferencesListener) {
			Preferences.removePreferencesListener(transcription, viewer);
		}
		
		disconnectController(viewer, finalDisconnection);
	}

	/**
	 * Break the connection between a viewer and its controller. Removes the
	 * associated controller if it has no connected Viewers left after this
	 * operation.
	 *
	 * @param viewer the viewer that must be disconnected from its controller
	 * @param finalDisconnection flag that tells if the viewer might need to be
	 *        reconnected
	 */
	private void disconnectController(AbstractViewer viewer, boolean finalDisconnection) {
		//	get the controller for this viewer and remove the viewer as listener
		Controller controller = controllers.get(viewer);

		//searchResultViewer might be created yet not connected -> controller == null
		if (controller != null) {
			controller.removeControllerListener(viewer);

			// remove the viewer key from the controllers hashtable if the disconnection is final
			if (finalDisconnection) {
				controllers.remove(viewer);

				// if there are no more listeners for the controller clean it up
				if (controller.getNrOfConnectedListeners() == 0) {
					removeFromHashTable(controller, controllers);
					masterMediaPlayer.removeController(controller);
					controller = null;
				}
			}
		}
	}

	/**
	 * Gets a TimeLineController for a Tier. If the Controller already exists
	 * it is reused otherwise it is created
	 *
	 * @param tier the Tier for which the TimeLineController must be created
	 *
	 * @return the TimeLineController for the Tier
	 */
	private TimeLineController getControllerForTier(Tier tier) {
		if (tier == null) {
			return null;
		}

		TimeLineController controller = null;

		// first see if the controller already exists
		if (controllers.containsKey(tier)) {
			controller = (TimeLineController) controllers.get(tier);
		}
		else {
			// The controller does not exist, create it
			controller = new TimeLineController(tier, masterMediaPlayer);

			// connect the controller to the master media player
			masterMediaPlayer.addController(controller);

			// add the controller to the existing controller list
			controllers.put(tier, controller);
		}

		return controller;
	}

	/**
	 * Gets a PeriodicUpdateController for a period. If the Controller already
	 * exists it is reused otherwise it is created
	 *
	 * @param period the period in milliseconds for which the
	 *        PeriodicUpdateController must be created
	 *
	 * @return the PeriodicUpdateController for the period
	 */
	private PeriodicUpdateController getControllerForPeriod(long period) {
		PeriodicUpdateController controller = null;
		Long periodKey = new Long(period);

		// first see if the controller already exists
		if (controllers.containsKey(periodKey)) {
			controller = (PeriodicUpdateController) controllers.get(periodKey);
		}
		else {
			// The controller does not exist, create it
			controller = new PeriodicUpdateController(period);

			// connect the controller to the master media player
			masterMediaPlayer.addController(controller);

			// add the controller to the existing controller list
			controllers.put(periodKey, controller);
		}

		return controller;
	}

	/**
	 * Utility to remove all occurrences of an object from a Hashtable. There is
	 * no direct method for this in the Java API if you do not know the key
	 *
	 * @param object the Object to be removed
	 * @param hashtable the Hashtable that contains the Object
	 *
	 * @return boolean to flag if the Object was actually removed
	 */
	private boolean removeFromHashTable(Object object, Map<Object, Controller> hashtable) {
		Object key;
		boolean objectRemoved = false;

		Iterator<Map.Entry<Object, Controller>> it = hashtable.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Controller> e = it.next();
			if (e.getValue() == object) {
				it.remove();
				objectRemoved = true;
			}
		}

		return objectRemoved;
	}
}
