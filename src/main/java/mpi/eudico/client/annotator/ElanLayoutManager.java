package mpi.eudico.client.annotator;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.layout.AnnotationManager;
import mpi.eudico.client.annotator.layout.InterlinearizationManager;
import mpi.eudico.client.annotator.layout.ModeLayoutManager;
import mpi.eudico.client.annotator.layout.PlayerLayoutModel;
import mpi.eudico.client.annotator.layout.TurnsAndSceneManager;
import mpi.eudico.client.annotator.layout.SegmentationManager;
import mpi.eudico.client.annotator.layout.SyncManager;
import mpi.eudico.client.annotator.layout.TranscriptionManager;
import mpi.eudico.client.annotator.player.*;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.viewer.*;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * Takes care of layout in an ElanFrame
 */
public class ElanLayoutManager implements ElanLocaleListener, 
     PreferencesUser {
    /* Note: when more modi are added, SyncModeCA should be adapted */

    /** sync layout mode */
    public final static int SYNC_MODE = 0;

    /** Annotation layout mode */
    public final static int NORMAL_MODE = 1;
    
    /** transcription layout mode */
    public final static int TRANSC_MODE = 2;
    
    /** segmentation layout mode */
    public final static int SEGMENT_MODE = 3;
    
    /** interlinearization layout mode */
    public final static int INTERLINEAR_MODE = 4;
    
    /** turns and scene annotation layout mode */
    public final static int TURN_SCENE_MODE = 5;
    
    /** the simplified mode is (initially) implemented as a separate application,
     *  no switching between modes in that case **/
    //public final static int FULL_APPLICATION_MODE = 100;//??
    //public final static int REDUCED_APPLICATION_MODE = 110;//??
    

    /** the default width for the first video */
    public final static int MASTER_MEDIA_WIDTH = 352;
    private final int MIN_MEDIA_WIDTH = 80;
    
    /** the minimum height for the first video and the tabpane */
    public final static int MASTER_MEDIA_HEIGHT = 250;
    private final int MIN_MEDIA_HEIGHT = 40;
    public static final int CONTROL_PANEL_WIDTH = 120;

    /** outer content pane margin */
    public final static int CONTAINER_MARGIN = 3;
    
    /** outer content pane margin */
    public final static int BELOW_BUTTONS_MARGIN = 8;
    
    /** the default height of the signalviewer */
    public final static int DEF_SIGNAL_HEIGHT = 70;
    
    private ElanFrame2 elanFrame;
    private Container container;
    private ViewerManager2 viewerManager;
    private int mode = -1;
    private int mediaAreaWidth = MASTER_MEDIA_WIDTH;
    private int mediaAreaHeight = MASTER_MEDIA_HEIGHT;
    private int multiTierControlPanelWidth = CONTROL_PANEL_WIDTH;


    // temp permanent detached flag
    private boolean permanentDetached;

    // media players
//    private ElanMediaPlayer masterMediaPlayer;
    //private ElanMediaPlayer mediaPlayer2;
    // a list of player layout info objects
    private List<PlayerLayoutModel> playerList; 
    private SignalViewer signalViewer;       
    private TimeSeriesViewer timeseriesViewer;
    
    private ModeLayoutManager activeManager;

	private boolean initialized;

    /**
     * DOCUMENT ME!
     *
     * @param elanFrame the content pane of the ElanFrame
     * @param viewerManager DOCUMENT ME!
     */
    public ElanLayoutManager(ElanFrame2 elanFrame, ViewerManager2 viewerManager) {
        this.elanFrame = elanFrame;
        this.container = elanFrame.getContentPane();
        this.viewerManager = viewerManager;
		playerList = new ArrayList<PlayerLayoutModel>(8);
		
        // listen to locale changes
        ElanLocale.addElanLocaleListener(viewerManager.getTranscription(), this);

        // no java LayoutManager, we take care of it ourselves
        container.setLayout(null);

        // listen for ComponentEvents on the Container
        container.addComponentListener(new ContainerComponentListener());
        
        
        // initially attached, temp from here
		// temporary code to allow permanent detached mode until swapping is possible on the mac
		Boolean permDetached = Preferences.getBool("PreferredMediaWindow", null);
		if (permDetached == null) {
			permDetached = Boolean.FALSE; // default usage is attached media window
		}
		permanentDetached = permDetached.booleanValue();
		if (!System.getProperty("os.name").startsWith("Mac OS")) {
			permanentDetached = false;
		}
		// end temp
		
//		mode = NORMAL_MODE;
//		Integer lastMode = (Integer) Preferences.get("LayoutManager.CurrentMode", viewerManager.getTranscription()); 
//		if(lastMode != null && mode != lastMode.intValue()){
//			mode = lastMode;
//		}
        //activeManager = new AnnotationManager(viewerManager, this);
//		activeManager = createManagerInstance();
//		activeManager.initComponents();
    }

    /**
     * 
     */
    @Override
	public void updateLocale() {       
        if (activeManager != null) {
        	activeManager.updateLocale();
        	if (mode != NORMAL_MODE) { ////////// to be checked
        		doLayout();
        	}
        } 
    }

    /**
     * Make an object visible in the layout.
     *
     * @param object
     */
    public void add(Object object) {
        if (object instanceof ElanMediaPlayer) {
            addMediaPlayer((ElanMediaPlayer) object);
        }  else if (object instanceof SignalViewer ) {
            signalViewer = ((SignalViewer) object);
            if(activeManager !=null){
        		activeManager.add(signalViewer);
        	}  
        } else if (object instanceof TimeSeriesViewer) {
        	if(timeseriesViewer == null){
        		timeseriesViewer = ((TimeSeriesViewer) object);
        	}
        	if(activeManager != null){
        		activeManager.createAndAddViewer(ELANCommandFactory.TIMESERIES_VIEWER);
        	}        	
        } else {
        	if(activeManager !=null){
        		activeManager.add(object);
        	}
        }
    }

	/**
	 * Remove an object from the layout.
	 *
	 * @param object
	 */
	public void remove(Object object) {
		if (object instanceof ElanMediaPlayer) {
			removeMediaPlayer((ElanMediaPlayer) object);
		} else if (object instanceof SignalViewer) {
			if(	activeManager !=null){
				activeManager.remove(object);
			}
			signalViewer = null;
			doLayout();
		} else {
			if(	activeManager !=null){
				activeManager.remove(object);
			}
		}
	}

	public void updateViewer(String viewerName, boolean createViewer) {        
		if(viewerName == null){
			return;
		}		
		if(	activeManager !=null){			
			if(createViewer){
				activeManager.createAndAddViewer(viewerName);
				if(viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)){
					signalViewer = viewerManager.getSignalViewer();			
				} 
			}else{
				boolean doLayout = activeManager.destroyAndRemoveViewer(viewerName);
				if(viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)){
					signalViewer = null;			
				}
				if(doLayout){
					doLayout();
				}
				
			}	
		}
	}
	
	/**
	 * Make an object invisible in the layout.
	 *
	 * @param object
	 */
	public void hide(Object object) {
	}
    
    /**
     * Add a mediaplayer to the layout- and syncmanager.
     * Before adding/removing a player while in sync mode be sure that all players 
     * are connected by calling connectAllPlayers.
     * 
     * @see #connectAllPlayers()
     * @param player the player to add
     */
    private void addMediaPlayer(ElanMediaPlayer player) {   
    	if (player == null) {
    		return; //cannot happen now
    	}
    	/*
    	if (player instanceof SyncPlayer) {
    		player.setLayoutManager(this); //??
        	PlayerLayoutModel plModel = new PlayerLayoutModel(player);
        	plModel.syncOnly = true;
        	playerList.add(plModel);
        	
        	if (mode == SYNC_MODE) {
        		activeManager.add(player);
        		container.add(((SyncManager)activeManager).getPlayerLabel(player));                   
        		doLayout();
        	}        	
        	return;    	
        }
    	*/
    	player.setLayoutManager(this);
    	PlayerLayoutModel plModel = new PlayerLayoutModel(player, this);
    	playerList.add(plModel);
    	// should we check whether the player already has been added before??
    	if (permanentDetached) {
    		plModel.detach();
    	}
    	
//    	int nextIndex = playerList.size();  
    	//activeManager.add(player); // hier... add the player model instead   	
//    	if (mode == SYNC_MODE) {    		
//    		container.add(((SyncManager)activeManager).getPlayerLabel(player)); 
//    	}         	
    	
    	if (plModel.isVisual() && plModel.isAttached()) {
    		// this also starts to initialize CocoaQT players
    		container.add(plModel.visualComponent);
    	}
    	
//    	if (nextIndex == 1) {
//    		masterMediaPlayer = player;
//    	}
    	// read aspect ratio from preferences
    	if (player.getMediaDescriptor() != null) {
	    	Float ar = Preferences.getFloat("AspectRatio(" + player.getMediaDescriptor().mediaURL + ")", 
	    			viewerManager.getTranscription());
	    	if (ar != null) {
	    		player.setAspectRatio(ar.floatValue());
	    	}
    	}
    	// fix for CocoaQT audio only players. there should be component on the screen otherwise it won't work
    	if (player.getAspectRatio() == -1) {
    		plModel.visualComponent.setBounds(0, 0, 1, 1);
    		plModel.visualComponent = null;
    	}
    	
    	doLayout();// maybe call from the outside
    }
    
    /**
     * Removes a player from the layout and syncmanager.
     * Before adding/removing a player while in sync mode be sure that all players 
     * are connected by calling connectAllPlayers. 
     * 
     * @see #connectAllPlayers()
     * @param player the player to remove
     */
    private void removeMediaPlayer(ElanMediaPlayer player) {
    	if (player == null) {
    		return;
    	}

		PlayerLayoutModel plModel = null;
		//int index = -1;
		for (int i = 0; i < playerList.size(); i++) {
			plModel = playerList.get(i);
			if (plModel.player == player) {
				//index = i;
				break;
			}
		}
		if (plModel == null) {
			return;
		}
		/*
    	if (player instanceof SyncPlayer) {
    		if (playerList.remove(plModel)) {        		
        		if (plModel.player.getVisualComponent() != null) {
        			container.remove(plModel.player.getVisualComponent()); 
        		}  
            	
            	if (mode == SYNC_MODE) {
            		activeManager.remove(player);            		                 
            		doLayout();
            	}
    		}

        	return;
    	}
    	*/
		
		if (plModel.isVisual()) {
			if (plModel.isAttached()) {
				container.remove(plModel.visualComponent);
			} else {
				// attach destroys a detached frame...
				plModel.attach();
			}
		}
		
		playerList.remove(plModel);	
		//activeManager.remove(plModel.player);
		if(activeManager != null){
			activeManager.remove(plModel);
		}		
		
//		if (index == 0 && playerList.size() > 0) {
//			//the master media has been removed
//			PlayerLayoutModel model = (PlayerLayoutModel)playerList.get(0);
//			masterMediaPlayer = model.player;
//		}
		
		doLayout();
    }

    /**
     * Detaches the specified viewer or player.
     * 
     * @param object the viewer or player to remove from the main application frame
     */
    public void detach(Object object) {
    	if (object instanceof AbstractViewer) {
    		if (mode == NORMAL_MODE && activeManager instanceof AnnotationManager) {
    	    	   activeManager.detach(object);
    		} else if (mode == SYNC_MODE && activeManager instanceof SyncManager) {
    			activeManager.detach(object);
    		}
    	}
        else if (object instanceof Component) {
        	// might be a visual component of a player
        	PlayerLayoutModel model;
        	for (int i = 0; i < playerList.size(); i++) {
        		model = playerList.get(i);
        		if (model.visualComponent == object) {
        			if (model.isVisual() && model.isAttached()) {
        				if (model.isSyncOnly() && mode == SYNC_MODE) {
        					break;
        				}
        				container.remove(model.visualComponent);
						model.detach();
						
						//if (mode == SYNC_MODE && activeManager instanceof SyncManager) {
						if(	activeManager !=null){
							activeManager.detach(model);
						}
							
						//}
						
						doLayout();
        			}
        			break;       			
        		}
        	}
        }
    }

    /**
     * Attaches the specified viewer or player. 
     *
     * @param object the viewer or player to attach
     */
    public void attach(Object object) {
    	
		if (object instanceof AbstractViewer) {
			if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
    	    	   activeManager.attach(object);
    		}
		} else if (object instanceof Component) {
			// might be a visual component of a player
			PlayerLayoutModel model;
			for (int i = 0; i < playerList.size(); i++) {
				model = playerList.get(i);
				if (model.visualComponent == object) {
					if (model.isVisual() && !model.isAttached()) {
						
						model.attach();						
						container.add(model.visualComponent);
						
						if (mode == SYNC_MODE && activeManager instanceof SyncManager) {
							activeManager.attach(model);
						}
						
						doLayout();
						// test for QT
						if (model.player instanceof QTMediaPlayer) {

						    // force movie to resize					
							try{
							    Thread.sleep(100);
							} catch (Exception e){}
							Rectangle rec = model.visualComponent.getBounds();
							model.visualComponent.setBounds(new Rectangle(rec.x, rec.y, rec.width - 1, rec.height - 1));
							container.validate();
						}
			    		
			    		if ((mode == TRANSC_MODE && activeManager instanceof TranscriptionManager) ||
			    				(mode == TURN_SCENE_MODE && activeManager instanceof TurnsAndSceneManager)) {
							activeManager.attach(model);
						}
					}
					break;        			
				}
			}
        }
    }
    
    /**
     * When more than one video players are present and attached, this will make 
     * the specified video the one that is displayed as the first (and largest) 
     * video. 
     * @param player the player to display as the first (attached) video
     */
    public void setFirstPlayer(ElanMediaPlayer player) {
    	if (player == null) {
    		return;
    	}
		PlayerLayoutModel model;
		for (int i = 0; i < playerList.size(); i++) {
			model = playerList.get(i);
			if (model.player == player) {
				model.setDisplayedFirst(true);       			
			} else {
				model.setDisplayedFirst(false);
			}
		}
		doLayout();
    }
    
	/**
	 * Returns the SignalViewer.
	 * 
	 * @return the SignalViewer, can be null
	 */
	public SignalViewer getSignalViewer() {
		return signalViewer;
	}
	
	/**
	 * Returns the main container, the content pane of the frame
	 * 
	 * @return the main container
	 */
	public Container getContainer() {
		return container;
	}
	
	/**
	 * Returns the TimeLineViewer.
	 * 
	 * @return the TimeLineViewer, can be null
	 */
	public TimeLineViewer getTimeLineViewer() {
		if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
	    	   return ((AnnotationManager)activeManager).getTimeLineViewer();
		} else {
			return null;
		}		
	}
	
	/**
	 * Returns the InterlinearViewer.
	 * 
	 * @return the InterlinearViewer, can be null
	 */
	public InterlinearViewer getInterlinearViewer() {
		if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
	    	   return ((AnnotationManager)activeManager).getInterlinearViewer();
		} else {
			return null;
		}	
	}
	
	/**
	 * Returns the TimeSeries Viewer or null.
	 * 
	 * @return the TimeSeries Viewer
	 */
	public TimeSeriesViewer getTimeSeriesViewer() {		
		return timeseriesViewer;
	}
	
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public MultiTierControlPanel getMultiTierControlPanel() {
    	if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
	    	   return ((AnnotationManager)activeManager).getMultiTierControlPanel();
		} else {
			return null;
		}	
    }

    /**
     * Returns the current visible MultiTierViewer.<br>
     * Note: maybe should return null if in synchronization mode.
     * 
     * @return the timeline viewer or interlinear viewer
     */
    public MultiTierViewer getVisibleMultiTierViewer() {
    	if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
	    	   return ((AnnotationManager)activeManager).getVisibleMultiTierViewer();
		} else {
			return null;
		}	
    }
    
    /**
     * DOCUMENT ME!
     */
    public void showTimeLineViewer() {
       if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
    	   ((AnnotationManager)activeManager).showTimeLineViewer();
       }
    }

    /**
     * DOCUMENT ME!
     */
    public void showInterlinearViewer() {
    	if(mode == NORMAL_MODE && activeManager instanceof AnnotationManager){
     	   ((AnnotationManager)activeManager).showInterlinearViewer();
        }
    }
    
    /**
     * Switches to between the modes
     */
    public void changeMode(int mode){
    	if(this.mode == mode || this.mode == TURN_SCENE_MODE){
    		return;
    	}
    	
    	if(!initialized){
    		loadMediaSizePreferences();
    	}
    	
    	 if(viewerManager.getMasterMediaPlayer() != null && 
    			 viewerManager.getMasterMediaPlayer().isPlaying()){
         	viewerManager.getMasterMediaPlayer().stop();
         }
    	
    	if(activeManager !=null){  
    		elanFrame.clearShortcutsMap(getModeConstant(this.mode));
    		//removeShortcutKeys();
    		activeManager.enableOrDisableMenus(true);
    		activeManager.isClosing();
    		activeManager.clearLayout(); 
    	}
    	
    	activeManager = createManagerInstance(mode);
    
    	if(activeManager != null){
    		this.mode = mode; 
    		elanFrame.updateShortcutMap(getModeConstant(mode));
    		activeManager.initComponents();    
    		activeManager.enableOrDisableMenus(false);
    		activeManager.shortcutsChanged();
    		updateDetachedWindowsShortcuts();
    		
        	if (mode != TURN_SCENE_MODE) {
        		setPreference("LayoutManager.CurrentMode", 
        				Integer.valueOf(this.mode), viewerManager.getTranscription()); 
        	}

    		doLayout();
    	}
    }
    
    public void shortcutsChanged(){    	    
    	elanFrame.updateShortcutMap(getModeConstant(mode));
    	if(activeManager != null){
    		activeManager.shortcutsChanged();
    	}
    	// update possible detached player or viewer windows
    	updateDetachedWindowsShortcuts();	
    }
    
    /**
     * Notifies all detached windows to update the input and action map.
     */
    private void updateDetachedWindowsShortcuts() {
    	PlayerLayoutModel model = null;

    	for (int i = 0; i < playerList.size(); i++) {
    		model = playerList.get(i);
    		if (model.isVisual() && !model.isAttached()) {
    			model.detachedFrame.updateShortcuts();
    		}
    	}
    }
    
    public String getModeConstant(int modeId){
    	String modeName = null;
    	switch(modeId){ 
    	case SYNC_MODE:
    		modeName = ELANCommandFactory.SYNC_MODE;	
    		break;
			
		case TRANSC_MODE:
			modeName = ELANCommandFactory.TRANSCRIPTION_MODE;			
			break;
			
		case SEGMENT_MODE:
			modeName = ELANCommandFactory.SEGMENTATION_MODE;
			break;
			
		case INTERLINEAR_MODE:
			modeName = ELANCommandFactory.INTERLINEARIZATION_MODE;			
			break;
			
		case TURN_SCENE_MODE:
			modeName = ELANCommandFactory.TURNS_SCENE_MODE;
			break;
			
		default:
			modeName = ELANCommandFactory.ANNOTATION_MODE;		
    	}      	
    	
    	return modeName;
    }
    
    private ModeLayoutManager createManagerInstance(int mode){    	
    	activeManager = null;
    	switch(mode){    	
    		case NORMAL_MODE:
    			activeManager = new AnnotationManager(viewerManager, this);
    			break;
    			
    		case SYNC_MODE:
    			activeManager = new SyncManager(viewerManager, this);
    			break;
    			
    		case TRANSC_MODE:
    			activeManager = new TranscriptionManager(viewerManager, this);
    			break;
    			
    		case SEGMENT_MODE:
    			activeManager = new SegmentationManager(viewerManager, this);
    			break;
    			
    		case INTERLINEAR_MODE:
    			activeManager = new InterlinearizationManager(viewerManager, this);
    			break;
    			
    		case TURN_SCENE_MODE:
    			activeManager = new TurnsAndSceneManager(viewerManager, this);
    			break;
    	}      	
    	return activeManager;
    }

    /**
     * Returns the current layout mode.
     *
     * @return the current layout mode
     */
    public int getMode() {
        return mode;
    }
    
    /**
     * Makes sure all players are connected. Current offsets in the 
     * sync mode are ignored accept for the current active player.
     */
    public void connectAllPlayers() {
    	if (mode == SYNC_MODE && activeManager instanceof SyncManager) {
    		((SyncManager)activeManager).reconnect();
    	}
    } 

    /**
     * Checks the layout Mode flag and calls either doNormalLayout() or
     * doSyncLayout().
     */
    public void doLayout() {
    	if(isIntialized() && activeManager!= null){
    		activeManager.doLayout();  
    	}
    }
    
    /**
     * Helper class to detect if a Component is added to the content pane of
     * the Elan frame.
     *
     * @param component the component that is looked for in the content pane
     *
     * @return boolean to tell if the component was found
     */
    public boolean containsComponent(Component component) {
        Component[] components = container.getComponents();

        for (int i = 0; i < components.length; i++) {
            if (components[i] == component) {
                return true;
            }
        }

        return false;
    }

	/**
	 * Returns the media descriptors of the players with a visual component.
	 * 
	 * @return the media descriptors of the players with a visual component
	 */
	public List<MediaDescriptor> getVisualPlayers() {
		ArrayList<MediaDescriptor> visuals = new ArrayList<MediaDescriptor>(playerList.size());
    	PlayerLayoutModel model = null;

    	for (int i = 0; i < playerList.size(); i++) {
    		model = playerList.get(i);
    		if (model.isVisual()) {
    			visuals.add(model.player.getMediaDescriptor());   			  			
    		}
    	}
		return visuals;
	}
	
    /**
	 * Remove references to the ElanFrame etc. for garbage collection.
	 *
	 */
	public void cleanUpOnClose() {
		container.removeAll();
		if (activeManager != null){
			activeManager.cleanUpOnClose();
		}
		
		setPreference("LayoutManager.CurrentMode", 
				Integer.valueOf(this.mode), viewerManager.getTranscription()); 
		
		// close all detached frames if any		
		PlayerLayoutModel model;
		for (int i = 0; i < playerList.size(); i++) {
			model = playerList.get(i);
			if (model.isVisual() && !model.isAttached() && model.detachedFrame != null) {
				model.detachedFrame.getContentPane().remove(model.visualComponent);
				model.detachedFrame.resetShortcutMaps();
				model.detachedFrame.setVisible(false);
				model.detachedFrame.dispose();
				model.detachedFrame = null;
			}
		}
		
		//glasspane = null;
		container = null;
		viewerManager = null;
		elanFrame = null;
	}
	
	/**
	 * Called when the file is going to be closed, and the  
	 * required things like unsaved changes, preferences, etc 
	 * should be done here
	 *
	 */
	public void isClosing() {
		if (activeManager != null){
			activeManager.isClosing();
		}	
	}
	
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JFrame getElanFrame() {
        return elanFrame;
    }
   
    /**
     * Give access to the viewermanager.
     * 
     * @return the viewermanager
     */
    public ViewerManager2 getViewerManager() {
    	return viewerManager;
    }
    
    /**
     * Enables / Disables the command actions
     * 
     * @param actionList, list of all actions
     * @param enabled if true, enables all the actions, 
     * 				if false, disables all the actions
     */
	public void enableOrDisableActions(List<String> actionList, boolean enabled){
		for(int i=0; i< actionList.size(); i++){
			Action ca = ELANCommandFactory.getCommandAction(viewerManager.getTranscription(), actionList.get(i));
			if(ca != null){
				ca.setEnabled(enabled);
			}
		}
	}    

    /**
     * Returns the current height of the area reserved for the media players.
     * 
     * @return the current height of the area reserved the media players
     */
    public int getMediaAreaHeight() {
        return mediaAreaHeight;
    }
    
    /**
     * Returns the current width of the area reserved for the media players.
     * (Mainly used for the layout of the transcription mode)
     * 
     * @return the current width of the area reserved the media players
     */
	public int getMediaAreaWidth() {		
		return mediaAreaWidth;
	}
	
	public boolean isIntialized(){
		return initialized;
	}
	
	/**
	 * Returns whether the specified player is attached. 
	 * @param player the player
	 * @return true if attached, false if detached
	 */
	public boolean isAttached(ElanMediaPlayer player) {
    	PlayerLayoutModel model = null;

    	for (int i = 0; i < playerList.size(); i++) {
    		model = playerList.get(i);
    		if (model.player == player) {
    			return model.isAttached();
    		}
    	}
    	return false;
	}
	
	 /**
     * Returns the players that have a visual component and are attached.
     * The player that is denoted to be displayed first (largest) is inserted 
     * as the first element in the array. 
     * 
     * @return an array of PlayerLayoutModel objects.
     */
    public PlayerLayoutModel[] getAttachedVisualPlayers() {
    	List<PlayerLayoutModel> plList = new ArrayList<PlayerLayoutModel>(playerList.size());
    	PlayerLayoutModel model = null;

    	for (int i = 0; i < playerList.size(); i++) {
    		model = playerList.get(i);
    		if (model.isVisual() && model.isAttached()) {
    			if (model.isDisplayedFirst()) {
    				plList.add(0, model);
    			} else {
					plList.add(model);
    			}   			
    		}
    	}    	
    	return (PlayerLayoutModel[]) plList.toArray(new PlayerLayoutModel[]{});
    }
	
	/**
     * Sets the width of the area reserved for media players.
     * Currently the max width is limited to the container width minus 
     * a fixed number of pixels, 100.
     * 
     * @param mediaAreaWidth the new width
     */
	public void setMediaAreaWidth(int mediaAreaWidth) {
		if (mediaAreaWidth >= MIN_MEDIA_WIDTH) {
            this.mediaAreaWidth = (mediaAreaWidth < container.getWidth() - 100) ? 
            		mediaAreaWidth : container.getWidth() - 100; 
        } else {
            this.mediaAreaWidth = MIN_MEDIA_WIDTH;
        }
        setPreference("LayoutManager.MediaAreaWidth", 
        		Integer.valueOf(this.mediaAreaWidth), viewerManager.getTranscription());
        doLayout();
	}
    
    /**
     * Sets the height of the area reserved for media players (and tabpane).
     * Currently the max height is limited to the container height minus 
     * a fixed number of pixels, 50.
     * 
     * @param mediaAreaHeight the new height
     */
    public void setMediaAreaHeight(int mediaAreaHeight) {
        if (mediaAreaHeight >= MIN_MEDIA_HEIGHT) {
            this.mediaAreaHeight = (mediaAreaHeight < container.getHeight() -100) ? 
            		mediaAreaHeight : container.getHeight() -100; 
        } else {
            this.mediaAreaHeight = MIN_MEDIA_HEIGHT;
        }
        setPreference("LayoutManager.MediaAreaHeight", 
        		Integer.valueOf(this.mediaAreaHeight), viewerManager.getTranscription());      
        doLayout();
    }
    
	public List<PlayerLayoutModel> getPlayerList() {
		return playerList;
	}

    /**
     * Returns the width of the multiple tier control panel.
     * 
     * @return the width
     */
    public int getMultiTierControlPanelWidth() {
		return multiTierControlPanelWidth;
	}

    /**
     * Sets the width for the multiple tier control panel. Also determines the size of the 
     * Signalviewer etc.
     * 
     * @param multiTierControlPanelWidth the new width
     */
	public void setMultiTierControlPanelWidth(int multiTierControlPanelWidth) {
		if (multiTierControlPanelWidth >= MIN_MEDIA_HEIGHT) {//reuse
			this.multiTierControlPanelWidth = (multiTierControlPanelWidth < container.getWidth() - 50) ? 
					multiTierControlPanelWidth : container.getWidth() - 50;
		} else {
			this.multiTierControlPanelWidth = MIN_MEDIA_HEIGHT;
		}
        setPreference("LayoutManager.ControlPanelWidth", 
        		Integer.valueOf(this.multiTierControlPanelWidth), viewerManager.getTranscription());
		
		doLayout();
	}

	/**
     * The PreferencesUser methods replace the getState and setState methods.
     * 
     * @see PreferencesUser#setPreference(String, Object, Object)
     */
	@Override
	public void setPreference(String key, Object value, Object document) {
		if (document instanceof Transcription) {
			Preferences.set(key, value, (Transcription)document, false, false);
		} else {
			Preferences.set(key, value, null, false, false);
		}
	}
	
	private void loadMediaSizePreferences(){
		Rectangle wRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		//Dimension d = (Dimension) Preferences.get("FrameSize", null);	
		
		Integer medHeight = Preferences.getInt("LayoutManager.MediaAreaHeight", 
				viewerManager.getTranscription());
		if (medHeight != null && medHeight.intValue() >= MIN_MEDIA_HEIGHT) {
			mediaAreaHeight = medHeight.intValue();
			
			if (mediaAreaHeight > wRect.height - 120) {
				mediaAreaHeight = wRect.height - 120;
			}
		}
		
		Integer medWidth = Preferences.getInt("LayoutManager.MediaAreaWidth", viewerManager.getTranscription());
		if (medWidth != null && medWidth.intValue() >= MIN_MEDIA_WIDTH) {
			mediaAreaWidth = medWidth.intValue();
			
			if (mediaAreaWidth > wRect.width - 120) {
				mediaAreaWidth = wRect.width - 120;
			}
		}
	}

	/**
	 * @see PreferencesListener#preferencesChanged()
	 */
	@Override
	public void preferencesChanged() {
		
		loadMediaSizePreferences();
		
		Integer cpWidth = Preferences.getInt("LayoutManager.ControlPanelWidth", 
				viewerManager.getTranscription());
		if (cpWidth != null && cpWidth.intValue() >= MIN_MEDIA_HEIGHT) {
			multiTierControlPanelWidth = cpWidth.intValue();			
		}		
		
		Boolean ttShown = Preferences.getBool("UI.ToolTips.Enabled", null);
		
		Integer lastMode = Preferences.getInt("LayoutManager.CurrentMode", viewerManager.getTranscription()); 
		if(lastMode != null && mode != lastMode.intValue()){
			changeMode(lastMode);	
			if(lastMode == mode){
				((ElanFrame2)getElanFrame()).setMenuSelected(ElanLocale.getString(getModeConstant(lastMode)), FrameConstants.OPTION);
			}
		} 	
		
		if (ttShown != null) {
			ToolTipManager.sharedInstance().setEnabled(ttShown);
		}
		if (activeManager != null) {
			activeManager.preferencesChanged();
		}
		initialized = true;
		doLayout();
	}
	
	/**
	 * 
	 * @return a list of zoomable viewers, provided by the current mode layout manager.  
	 */
	public List<Zoomable> getZoomableViewers() {
		if (activeManager != null) {
			return activeManager.getZoomableViewers();
		}
		
		return null;
	}
	
    /**
     * Listener for  ElanFrame ComponentEvents
     */
    class ContainerComponentListener extends ComponentAdapter {    	
    	
        /**
         * Calls doLayout() after a change in the size of the content pane 
         * of the ElanFrame.
         *
         * @param e component event
         */
        @Override
		public void componentResized(ComponentEvent e) {        	
            doLayout();
        }
    }
    
    /**
     * A special component listener for the splitpane that only lays out the 
     * viewers and panels in the splitpane when the divider has been relocated
     * (dragged). The componentResized() method does not simply call doLayout()
     * because the double call to doLayout() after a resize of the main container
     * (the contentpane of the Elan frame) messed up the layout of some components.
     * This listener is only needed when there is a top component as well as a bottom
     * component in the splitpane (i.e. when the divider can be dragged).
     * 
     * Oct 2008: this listener can now also be used by other splitpanes than the first
     * signalviewer/timelineviewer splitpane, e.g. the nested 
     * timeseriesviewer/signalviewer splitpane
     *  
     * @author Han Sloetjes
     */
    public class SignalSplitPaneListener extends ComponentAdapter {  	
    	
    	/**
		 * No-arg constructor.
		 */
		public SignalSplitPaneListener() {
			super();
		}


		/**
    	 * Sets the bounds of the components in the splitpane, when and only 
    	 * when their height is not equal to the height of their parent 
    	 * in the enclosing splitpane area.
    	 * 
    	 * @param e the component event
    	 */
		@Override
		public void componentResized(ComponentEvent e) {
			if (e == null || e.getComponent() == null) {
				return;
			}

			JSplitPane splitPane = null;
			if (e.getComponent().getParent() instanceof JSplitPane) {
				splitPane = (JSplitPane) e.getComponent().getParent();
			}
			
			if (splitPane == null) {
				return;
			}
			
			if (splitPane != null) {
				Component top = splitPane.getTopComponent();
				// special case if topcomponent is a nested splitpane
				if (top instanceof JSplitPane) {
					top = ((JSplitPane) top).getBottomComponent();
				}
				
				if (top != null && top instanceof Container) { 
					//int height = splitPane.getDividerLocation();
					int height = top.getHeight();
					Component[] cc = ((Container)top).getComponents();
					for (int i = 0; i < cc.length; i++) {
						if (cc[i].getHeight() != height) {
							cc[i].setSize(cc[i].getWidth(), height);
							/*
							if (cc[i] instanceof Container) {// 2 levels deep
								Component[] cc2 = ((Container)cc[i]).getComponents();
								for (int j = 0; j < cc2.length; j++) {
									// this is only correct if the subcomponents span the whole height
									cc2[j].setSize(cc2[j].getWidth(), height);
								}
							}*/
						}
					}
				}
				Component bottom = splitPane.getBottomComponent();
				if (bottom != null && bottom instanceof Container) {
					int height = splitPane.getHeight() - splitPane.getDividerLocation() - 
						splitPane.getDividerSize();
					Component[] cc = ((Container)bottom).getComponents();
					for (int i = 0; i < cc.length; i++) {
						if (cc[i].getHeight() != height) {
							cc[i].setSize(cc[i].getWidth(), height);
							/*
							if (cc[i] instanceof Container) {// 2 levels deep
								Component[] cc2 = ((Container)cc[i]).getComponents();
								for (int j = 0; j < cc2.length; j++) {
									// this is only correct if the subcomponents span the whole height
									cc2[j].setSize(cc2[j].getWidth(), height);
								}
							}*/
						}
					}
				}
			}
		}
    }
}
