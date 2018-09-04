package mpi.eudico.client.annotator.layout;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ModePanel;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SelectionButtonsPanel;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.gui.FontSizePanel;
import mpi.eudico.client.annotator.turnsandscenemode.TaSViewerControlPanel;
import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;
import mpi.eudico.client.annotator.turnsandscenemode.commands.CancelEditAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSActivateCellAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSContinuousPlayAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSDeleteAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSMergeAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSModifyTimeAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSPostCutAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSPostPasteAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSSplitAction;
import mpi.eudico.client.annotator.turnsandscenemode.commands.TaSMergeAction.MERGE_ROW;
import mpi.eudico.client.annotator.viewer.SignalViewer;
//import mpi.eudico.client.annotator.viewer.SingleTierViewer;
//import mpi.eudico.client.annotator.viewer.SingleTierViewerPanel;
import mpi.eudico.server.corpora.clom.Transcription;

/**
 * Layout manager for a simplified layout of a transcription window,
 * with at most one video, with (at most) one waveform viewer and one 
 * area that is an annotation viewer/editor for (initially) one tier. 
 * A segmentation is added by hitting the Enter key.
 * 
 * Eventually it would be nice if the video area can be either WEST, EAST, NORTH or SOUTH
 * and the waveform viewer either NORTH or SOUTH.
 */
public class TurnsAndSceneManager implements ModeLayoutManager {
	private ElanLayoutManager layoutManager;
    private Container container;
    private ViewerManager2 viewerManager; 
    private Transcription transcription;
    private JScrollPane signalViewerScrollPane;
    private SignalViewer signalViewer;
    private TurnsAndSceneViewer turnsAndSceneViewer;
    private TaSViewerControlPanel tasControlPanel;
    /* Use a single tier viewer panel for this?
     * Using a single tier viewer panel has several undesired side effects:
     * - the viewer receives less time events (only at start and end of annotations)
     * - it currently has no "mode" to only show root tiers
     * - the layout of combo box and viewer are not very flexible
    */
    //private SingleTierViewerPanel singleTierViewerPanel;
	private JSplitPane vertSplitPane;
	private JSplitPane horiSplitPane;
	private JPanel videoAndControlPanel;
	private VideoLayoutPanel videoPanel;
	private JPanel controllerPanel;
	private JPanel viewerPanel;
	// could add this to a more generic panel, similar to the Loop Mode and SelectionMode panels
	private JCheckBox continuousPlayCB;
	private TaSContinuousPlayAction continuousPlayAction;
	
	private ElanMediaPlayerController mediaPlayerController;
	
	private FontSizePanel fontSizePanel;
	
//	private JButton butAlignVideoToRight;
//	private boolean videoInRight = false;
	private boolean initialized = false;
//	private int numVisualPlayers;
	
	private final int MIN_SIGNAL_HEIGHT = 100;
//	private final int MIN_VIEWER_HEIGHT = 120;
	// preferences
	final String prefKeyFontSize = "TurnsAndSceneMode.View.FontSize";
	final String prefKeyVertSplitLoc = "TurnsAndSceneMode.VertSplit.Location";
	final String prefKeyHoriSplitLoc = "TurnsAndSceneMode.HoriSplit.Location";
	final String prefKeyContinuousPlay = "TurnsAndSceneMode.ContinuousPlayMode";
	// a list of actions that are currently not created via the CommandFactory
	// but (re-)use the name/identifier constant 
	public static final List<String> REDEFINED_ACTIONS = new ArrayList<String>();
	static {
		REDEFINED_ACTIONS.add(ELANCommandFactory.SPLIT_ANNOTATION);
		REDEFINED_ACTIONS.add(ELANCommandFactory.MERGE_ANNOTATION_WB);
		REDEFINED_ACTIONS.add(ELANCommandFactory.MERGE_ANNOTATION_WN);
		REDEFINED_ACTIONS.add(ELANCommandFactory.ANNOTATION_DOWN);
		REDEFINED_ACTIONS.add(ELANCommandFactory.ANNOTATION_UP);
		REDEFINED_ACTIONS.add(ELANCommandFactory.DELETE_ANNOTATION);
		REDEFINED_ACTIONS.add(ELANCommandFactory.REMOVE_ANNOTATION_VALUE);
		REDEFINED_ACTIONS.add(ELANCommandFactory.MODIFY_ANNOTATION_TIME);
		REDEFINED_ACTIONS.add(ELANCommandFactory.CANCEL_ANNOTATION_EDIT);
		REDEFINED_ACTIONS.add(ELANCommandFactory.ACTIVE_ANNOTATION_CURRENT_TIME);	
	}
	
	/**
	 * @param viewerManager the viewer manager
	 * @param elanLayoutManager the layout manager
	 */
	public TurnsAndSceneManager(ViewerManager2 viewerManager, ElanLayoutManager elanLayoutManager) {
		this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;
        transcription = viewerManager.getTranscription();        
        if(viewerManager.getMasterMediaPlayer() != null){
        	viewerManager.getMasterMediaPlayer().setMediaTime(0);
        }
        
        container = layoutManager.getContainer();
	}

	@Override
	public void add(Object object) {
		if (object instanceof SignalViewer) {
	    	setSignalViewer((SignalViewer) object);
		} else if (object instanceof TurnsAndSceneViewer) {
			setSingleLayerViewer((TurnsAndSceneViewer) object);
		} else if (object instanceof ElanMediaPlayerController) {
			setMediaPlayerController((ElanMediaPlayerController) object);
		} else if (object instanceof FontSizePanel) {
			setFontSizePanel((FontSizePanel) object);
		}
		/*else if (object instanceof SingleTierViewerPanel) {
			container.remove(turnsAndSceneViewer);
			setSingleTierViewerPanel((SingleTierViewerPanel) object);
			
		}*/
	}

	/**
	 * This has to be implemented once in a situation this 
	 * method is actually called.
	 */
	@Override
	public void remove(Object object) {
		//System.out.println("Remove Object");
	}
	
	private void setSignalViewer(SignalViewer sigViewer){
		this.signalViewer = sigViewer;	
		signalViewer.setMediaTime(0);
		signalViewer.setSelection(0, 0);
		signalViewerScrollPane = new JScrollPane((signalViewer), 
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		signalViewerScrollPane.setPreferredSize(new Dimension(MIN_SIGNAL_HEIGHT, MIN_SIGNAL_HEIGHT));
		signalViewerScrollPane.setMinimumSize(signalViewerScrollPane.getPreferredSize());
		// add at final initialization
        if (initialized) { 
        	doLayout();
        } 
	}
	
	private void setSingleLayerViewer(TurnsAndSceneViewer slViewer) {
		this.turnsAndSceneViewer = slViewer;
		tasControlPanel = new TaSViewerControlPanel(slViewer);
		viewerManager.connectListener(tasControlPanel);
		viewerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(ElanLayoutManager.CONTAINER_MARGIN, ElanLayoutManager.CONTAINER_MARGIN,
				ElanLayoutManager.CONTAINER_MARGIN, ElanLayoutManager.CONTAINER_MARGIN);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		viewerPanel.add(tasControlPanel, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, ElanLayoutManager.CONTAINER_MARGIN,
				ElanLayoutManager.CONTAINER_MARGIN, ElanLayoutManager.CONTAINER_MARGIN);
		viewerPanel.add(turnsAndSceneViewer, gbc);
		// set the viewer of this action in case it has been created before the viewer
		if (continuousPlayAction != null) {
			continuousPlayAction.setViewer(turnsAndSceneViewer);
		}
		
		if (initialized) { 
        	doLayout();
        }
	}
	
	/**
	 * Add the font size panel to the viewer/editor panel.
	 * 
	 * @param fontSizePane
	 */
	private void setFontSizePanel(FontSizePanel fontSizePane) {
		this.fontSizePanel = fontSizePane;
		if (viewerPanel != null) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = new Insets(ElanLayoutManager.CONTAINER_MARGIN,ElanLayoutManager.CONTAINER_MARGIN,
					ElanLayoutManager.CONTAINER_MARGIN,ElanLayoutManager.CONTAINER_MARGIN);
			viewerPanel.add(fontSizePanel, gbc);
		}
	}
	
	private void setMediaPlayer() {
		PlayerLayoutModel[] visualPlayers = layoutManager.getAttachedVisualPlayers();

		PlayerLayoutModel playerModelToAdd = null;
	    for (PlayerLayoutModel plModel : visualPlayers) {
	    	if (playerModelToAdd == null) {
	    		//if (plModel.player == viewerManager.getMasterMediaPlayer()) {
	    			playerModelToAdd = plModel; // just take the first one
	    		//}
	    	}
	    	
	    	// this should be revised if this becomes a mode in ELAN
	    	if (plModel != playerModelToAdd) {
	    		viewerManager.destroyMediaPlayer(plModel.player);
	    	}
	    	container.remove(plModel.visualComponent);
	    }
	    
	    if (playerModelToAdd != null) {
	    	// create and add the video panel
	    	if (videoPanel == null) {
	    		videoPanel = new VideoLayoutPanel();
	    		videoPanel.setPlayerLayoutModel(playerModelToAdd);
//	    		GridBagConstraints gbc = new GridBagConstraints();
//	    		gbc.fill = GridBagConstraints.BOTH;
//	    		gbc.weightx = 1.0;
//	    		gbc.weighty = 1.0;
//	    		videoPanel.add(playerModelToAdd.visualComponent, gbc);
	    		
//	    		float aspectRatio = visualPlayers[0].player.getAspectRatio();
//	    		int vidWidth = playerModelToAdd.player.getSourceWidth();
//	    		int vidHeight = (int) (vidWidth / aspectRatio);
//	    		Dimension prefSize = new Dimension(vidWidth, vidHeight);
//	    		videoPanel.setPreferredSize(prefSize);
//	    		playerModelToAdd.visualComponent.setPreferredSize(prefSize);
	    		if (videoAndControlPanel == null) {
	    			videoAndControlPanel = new JPanel(new GridBagLayout());	    			
	    		}
	    		
	    		GridBagConstraints gbc = new GridBagConstraints();
	    		int margin = ElanLayoutManager.CONTAINER_MARGIN;
	    		gbc.anchor = GridBagConstraints.NORTH;
	    		gbc.fill = GridBagConstraints.BOTH;
	    		gbc.weightx = 1.0;
	    		gbc.weighty = 0.01;
	    		gbc.insets = new Insets(margin, margin, 0, margin);
	    		videoAndControlPanel.add(videoPanel, gbc);
	    		
	    		gbc.gridy = 3;
	    		gbc.fill = GridBagConstraints.BOTH;
	    		gbc.weightx = 1.0;
	    		gbc.weighty = 1.0;
	    		gbc.insets = new Insets(0, 0, 0, 0);
	    		JPanel filler = new JPanel();
	    		filler.setPreferredSize(new Dimension(2, 2));
	    		videoAndControlPanel.add(filler, gbc);
	    		
	    		videoAndControlPanel.addComponentListener(videoPanel);
	    	} else {
	    		// replace video?
	    	}
	    }
	}
	
	private void setMediaPlayerController(ElanMediaPlayerController mediaPlayerControl) {
		this.mediaPlayerController = mediaPlayerControl;
		if (videoAndControlPanel == null) {
			videoAndControlPanel = new JPanel(new GridBagLayout());
		}		

		int sliderHeight = mediaPlayerController.getSliderPanel().getPreferredSize().height;
		mediaPlayerController.getSliderPanel().setPreferredSize(new Dimension(200, sliderHeight));
		mediaPlayerController.getSliderPanel().setMinimumSize(new Dimension(200, sliderHeight));
		mediaPlayerController.getAnnotationDensityViewer().setPreferredSize(new Dimension(200, 
				mediaPlayerController.getAnnotationDensityViewer().getPreferredSize().height));
		mediaPlayerController.getAnnotationDensityViewer().setMinimumSize(new Dimension(200, 
				mediaPlayerController.getAnnotationDensityViewer().getPreferredSize().height));
		
		continuousPlayAction = new TaSContinuousPlayAction(turnsAndSceneViewer);
		continuousPlayAction.putValue(Action.NAME, ElanLocale.getString("CommandActions.ContinuousPlaybackMode"));
		continuousPlayAction.putValue(Action.SHORT_DESCRIPTION, 
				ElanLocale.getString("CommandActions.ContinuousPlaybackModeToolTip"));
		//continuousPlayAction.putValue(Action.SELECTED_KEY, Boolean.FALSE);
		continuousPlayCB = new JCheckBox(continuousPlayAction);
		
		int margin = ElanLayoutManager.CONTAINER_MARGIN;
		controllerPanel = new JPanel(new GridBagLayout());
		// add density viewer, time label, player buttons, selection buttons, loop mode checkbox
		// density and slider on a separate panel?
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 4;
		gbc.insets = new Insets(margin, margin, 0, margin);
		controllerPanel.add(mediaPlayerController.getSliderPanel(), gbc);
		
		gbc.gridy = 1;
		gbc.insets = new Insets(-sliderHeight - 3, margin, margin, margin);// the required, strange overlapping layout
		controllerPanel.add(mediaPlayerController.getAnnotationDensityViewer(), gbc);
		
		// the elements below the slider panel are embedded between vertical panels to enforce 
		// vertical alignment of button panels
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 4;
		JPanel fill1 = new JPanel();
		fill1.setPreferredSize(new Dimension(2, 10));
		fill1.setMinimumSize(new Dimension(2, 10));
		controllerPanel.add(fill1, gbc);//side filler

		// add the button panels
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, margin, margin, margin);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		controllerPanel.add(mediaPlayerController.getTimePanel(), gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		controllerPanel.add(mediaPlayerController.getPlayButtonsPanel(), gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		SelectionButtonsPanel selectionBP = mediaPlayerController.getSelectionButtonsPanel();
		selectionBP.setSeparateLeftRightMode(true);
		controllerPanel.add(selectionBP, gbc);
		
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		((ModePanel) mediaPlayerController.getModePanel()).setModeVisible(ELANCommandFactory.SELECTION_MODE, false);
		controllerPanel.add(mediaPlayerController.getModePanel(), gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		controllerPanel.add(continuousPlayCB, gbc);
		//
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 4;
		JPanel fill2 = new JPanel();
		fill2.setPreferredSize(new Dimension(2, 10));
		fill2.setMinimumSize(new Dimension(2, 10));
		controllerPanel.add(fill2, gbc);// side filler
		
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridy = 1;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.weightx = 1.0;
		gbc2.insets = new Insets(margin, margin, 0, margin);
		gbc.anchor = GridBagConstraints.NORTH;
		videoAndControlPanel.add(controllerPanel, gbc2);
	}
	
	/**
	 * When everything has been added and initialized by this manager and the 
	 * ElanLayoutManager the layout can finally be completely initialized, 
	 * based on the players and viewers that are present.  
	 */
	private  void completeInitialization() {
		// first check the attached visual players, remove them and add the first one to
		// the video panel
		setMediaPlayer();
		container.add(vertSplitPane);
		if (signalViewerScrollPane != null) {
			vertSplitPane.setBottomComponent(signalViewerScrollPane);
		}
		
		if (videoPanel == null) {
			// can do without a second split-pane 
			// add the controller panel to the top component below the main viewer or
			// add it to the main container itself, below the splitpane, below the signal viewer
//			viewerPanel.add(controllerPanel, constraints);
//			GridBagConstraints gbc = new GridBagConstraints();
//			gbc.anchor = GridBagConstraints.NORTH;
//			gbc.fill = GridBagConstraints.HORIZONTAL;
//			gbc.weightx = 1.0;
//			gbc.weighty = 0.0;
//			gbc.gridwidth = 2;
//			gbc.gridy = 3;
//			gbc.insets = new Insets(0, ElanLayoutManager.CONTAINER_MARGIN,
//					ElanLayoutManager.CONTAINER_MARGIN, ElanLayoutManager.CONTAINER_MARGIN);
			//viewerPanel.add(videoAndControlPanel, gbc);
			vertSplitPane.setTopComponent(viewerPanel);
			
			container.add(videoAndControlPanel);
		} else {
			// create and add a second split-pane and have the video in the left component
			horiSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			horiSplitPane.setResizeWeight(0);
			vertSplitPane.setTopComponent(horiSplitPane);
			horiSplitPane.setOneTouchExpandable(true);
			
			horiSplitPane.setTopComponent(videoAndControlPanel);
			horiSplitPane.setBottomComponent(viewerPanel);
		}
		//vertSplitPane.resetToPreferredSizes();
		vertSplitPane.revalidate();
	}
/*
	private void setSingleTierViewerPanel(SingleTierViewerPanel singleTierViewerPanel) {
		this.singleTierViewerPanel = singleTierViewerPanel;
		singleTierViewerPanel.setViewer(turnsAndSceneViewer);
		// update the list of tiers by removing all depending tiers from the list
		List<String> rootTiers = new ArrayList<String>();
		for (Tier t : transcription.getTiers()) {
			if (t.getParentTier() == null) {
				rootTiers.add(t.getName());
			}
		}
		singleTierViewerPanel.updateTierOrder(rootTiers);
		if (rootTiers.size() > 0) {
			// do that here?
			singleTierViewerPanel.selectTier(rootTiers.get(0));
		}
		container.add(singleTierViewerPanel);
	}
*/
	
	/**
	 * The first time this is called by the ElanLayoutManager the layout for this mode
	 * is finalized.
	 */
	@Override
	public void doLayout() {
		if (!initialized) {
			completeInitialization();
			initialized = true;
			//return;
		}
		int containerMargin = ElanLayoutManager.CONTAINER_MARGIN;
		if (videoPanel != null) {
		vertSplitPane.setBounds(containerMargin, containerMargin, 
				container.getWidth() - 2 * containerMargin, container.getHeight() - 2 * containerMargin);
		} else {
			int videoAndControlPanelHeight = videoAndControlPanel.getPreferredSize().height;
			vertSplitPane.setBounds(containerMargin, containerMargin, 
					container.getWidth() - 2 * containerMargin, container.getHeight() - 
					2 * containerMargin - videoAndControlPanelHeight);
			videoAndControlPanel.setBounds(containerMargin, 
					container.getHeight() - containerMargin - videoAndControlPanelHeight, 
					container.getWidth() - 2 * containerMargin, videoAndControlPanelHeight);
			videoAndControlPanel.revalidate();
		}
	}
	
	/**
	 * Not called in the Simple-ELAN context
	 */
	@Override
	public void updateLocale() {
		// call updateLocale on all items that are not directly connected as locale listeners
		if (fontSizePanel != null) {
			fontSizePanel.updateLocale();
		}
		if (continuousPlayAction != null) {
			continuousPlayAction.putValue(Action.NAME, 
					ElanLocale.getString("CommandActions.ContinuousPlaybackMode"));
			continuousPlayAction.putValue(Action.SHORT_DESCRIPTION, 
					ElanLocale.getString("CommandActions.ContinuousPlaybackModeToolTip"));
		}
	}

	/**
	 * To be implemented once or if this becomes one of the modes in ELAN and it should 
	 * be possible to switch between modes.
	 */
	@Override
	public void clearLayout() {
		// System.out.println("Clear Layout");
	}
	
	@Override
	public void initComponents() {
		vertSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		vertSplitPane.setOneTouchExpandable(true);
		vertSplitPane.setResizeWeight(1);
		// the SignalViewer is added by ElanLayoutManager
		// the players are added after initComponents is called
	    //add(viewerManager.getMasterMediaPlayer());
	    add(viewerManager.getMediaPlayerController());
	    add(viewerManager.createTurnsAndSceneViewer());
		if (turnsAndSceneViewer != null) {
			add(new FontSizePanel(turnsAndSceneViewer));
		}
	}
	
	/**
	 * Maybe this can/should be implemented. 
	 * It should be implemented in case this mode should ever
	 * become one of the modes in ELAN (rather than the single mode of a different application).
	 */
	@Override
	public void enableOrDisableMenus(boolean enabled) {
		//System.out.println("En/Disable Menus " + enabled);
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.ANNOTATION, enabled);
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.TIER, enabled);
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.TYPE, enabled);
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.SEARCH, enabled);
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.VIEW, enabled);
//		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.OPTION, enabled);

	}

	/**
	 * This method is also called and used at load time.
	 */
	@Override
	public void shortcutsChanged() {
		//System.out.println("Shortcut changed..");
		/* this is not used (yet), relevant actions are added to the text area
		List<KeyStroke> keyStrokesNotToBeConsumed = new ArrayList<KeyStroke>();
    	KeyStroke ks = null;
    	ks = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, 
    			ELANCommandFactory.TURNS_SCENE_MODE);
    	if (ks != null){
    		keyStrokesNotToBeConsumed.add(ks);
    	}
    	ks = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.LOOP_MODE, 
    			ELANCommandFactory.TURNS_SCENE_MODE);
    	if (ks != null){
    		keyStrokesNotToBeConsumed.add(ks);
    	}
    	ks = ShortcutsUtil.getInstance().getKeyStrokeForAction(ELANCommandFactory.PLAY_AROUND_SELECTION, 
    			ELANCommandFactory.TURNS_SCENE_MODE);
    	if (ks != null) {
    		keyStrokesNotToBeConsumed.add(ks);
    	}
    	// update viewer
    	if (turnsAndSceneViewer != null) {
    		turnsAndSceneViewer.getEditor().setKeyStrokesNotToBeConsumed(keyStrokesNotToBeConsumed);
    	}
		*/
		// load special actions for the editor and/or update these actions
    	if (turnsAndSceneViewer != null) {
    		InputMap textInputMap = turnsAndSceneViewer.getEditor().getTextArea().getInputMap();
    		ActionMap textActionMap = turnsAndSceneViewer.getEditor().getTextArea().getActionMap();
    		// the main action, the split/create action
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.SPLIT_ANNOTATION, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.SPLIT_ANNOTATION);
    		textActionMap.put(ELANCommandFactory.SPLIT_ANNOTATION, new TaSSplitAction(turnsAndSceneViewer));
    		// merge with previous cell
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.MERGE_ANNOTATION_WB, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.MERGE_ANNOTATION_WB);
    		textActionMap.put(ELANCommandFactory.MERGE_ANNOTATION_WB, 
    				new TaSMergeAction(turnsAndSceneViewer, MERGE_ROW.PREVIOUS));
    		// merge with next cell
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.MERGE_ANNOTATION_WN, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.MERGE_ANNOTATION_WN);
    		textActionMap.put(ELANCommandFactory.MERGE_ANNOTATION_WN, 
    				new TaSMergeAction(turnsAndSceneViewer, MERGE_ROW.NEXT));
    		// activate next cell, one cell down in the table
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.ANNOTATION_DOWN, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.ANNOTATION_DOWN);
    		textActionMap.put(ELANCommandFactory.ANNOTATION_DOWN, 
    				new TaSActivateCellAction(turnsAndSceneViewer, SwingConstants.NEXT));
    		// activate previous cell, one cell up in the table
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.ANNOTATION_UP, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.ANNOTATION_UP);
    		textActionMap.put(ELANCommandFactory.ANNOTATION_UP, 
    				new TaSActivateCellAction(turnsAndSceneViewer, SwingConstants.PREVIOUS));
    		// delete current annotation / cell
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.DELETE_ANNOTATION, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.DELETE_ANNOTATION);
    		textActionMap.put(ELANCommandFactory.DELETE_ANNOTATION, new TaSDeleteAction(turnsAndSceneViewer));
    		// delete current annotation value
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.REMOVE_ANNOTATION_VALUE, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.REMOVE_ANNOTATION_VALUE);
    		textActionMap.put(ELANCommandFactory.REMOVE_ANNOTATION_VALUE, 
    				ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.REMOVE_ANNOTATION_VALUE));
    		// modify annotation time
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.MODIFY_ANNOTATION_TIME, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.MODIFY_ANNOTATION_TIME);
    		textActionMap.put(ELANCommandFactory.MODIFY_ANNOTATION_TIME, new TaSModifyTimeAction(turnsAndSceneViewer));
    		// actions after cut or paste in a cell
    		textActionMap.put("PostCut", new TaSPostCutAction(turnsAndSceneViewer));
    		textActionMap.put("PostPaste", new TaSPostPasteAction(turnsAndSceneViewer));
    		// cancel cell editing, Escape
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.CANCEL_ANNOTATION_EDIT, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.CANCEL_ANNOTATION_EDIT);
    		textActionMap.put(ELANCommandFactory.CANCEL_ANNOTATION_EDIT, new CancelEditAction(turnsAndSceneViewer));
    		// play selection
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.PLAY_SELECTION, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.PLAY_SELECTION);
    		textActionMap.put(ELANCommandFactory.PLAY_SELECTION, 
    				ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PLAY_SELECTION)); 		
    		textInputMap.put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    				ELANCommandFactory.PLAY_AROUND_SELECTION, ELANCommandFactory.TURNS_SCENE_MODE), 
    				ELANCommandFactory.PLAY_AROUND_SELECTION);
    		textActionMap.put(ELANCommandFactory.PLAY_AROUND_SELECTION, 
    				ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PLAY_AROUND_SELECTION));
    		// play selection with tab and/or shift tab
    		KeyStroke TAB_KS = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false);// hardcoded, not via ShortcutUtils
    		String tabPS = "TAB_PLAY_SEL";
    		textInputMap.put(TAB_KS, tabPS);
    		textActionMap.put(tabPS, ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PLAY_SELECTION));
    	
    		// a command to add to the root pane
    		JRootPane rootPane = SwingUtilities.getRootPane(container);
    		if (rootPane != null) {
    			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
    					ELANCommandFactory.ACTIVE_ANNOTATION_CURRENT_TIME, ELANCommandFactory.TURNS_SCENE_MODE), 
    					ELANCommandFactory.ACTIVE_ANNOTATION_CURRENT_TIME);
    			rootPane.getActionMap().put(ELANCommandFactory.ACTIVE_ANNOTATION_CURRENT_TIME, 
    					new TaSActivateCellAction(turnsAndSceneViewer, SwingConstants.CENTER));
    			// TODO could add some more commands to the root pane to be less dependent on editing in the text area going on
//    			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ShortcutsUtil.getInstance().getKeyStrokeForAction(
//    					ELANCommandFactory.DELETE_ANNOTATION, ELANCommandFactory.TURNS_SCENE_MODE), 
//    					ELANCommandFactory.DELETE_ANNOTATION);
//    			rootPane.getActionMap().put(ELANCommandFactory.DELETE_ANNOTATION, 
//    					new TaSDeleteAction(turnsAndSceneViewer));
    			if (continuousPlayAction != null) {
    				KeyStroke ks = ShortcutsUtil.getInstance().getKeyStrokeForAction(
	    					ELANCommandFactory.CONTINUOUS_PLAYBACK_MODE, ELANCommandFactory.TURNS_SCENE_MODE);
	    			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, 
	    					ELANCommandFactory.CONTINUOUS_PLAYBACK_MODE);
	    			rootPane.getActionMap().put(ELANCommandFactory.CONTINUOUS_PLAYBACK_MODE, 
	    					continuousPlayAction);
	    			if (ks != null) {
	    				String ksDesc = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ks);	  
	    				String tooltip = ElanLocale.getString("CommandActions.ContinuousPlaybackModeToolTip");
	    				tooltip  = tooltip + " (" + ksDesc + ")";
	    				continuousPlayAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
	    				continuousPlayAction.putValue(Action.ACCELERATOR_KEY, ks);
	    			}
    			}
    		}
    	}
	}

	/**
	 * This is not supported or used at this moment. It is not possible to dynamically
	 * add or remove annotation viewers.
	 */
	@Override
	public void createAndAddViewer(String viewerName) {
		// maybe use this?		
	}

	/**
	 * This is not supported or used at this moment. It is not possible to dynamically
	 * add or remove annotation viewers.
	 * 
	 * @return false
	 */
	@Override
	public boolean destroyAndRemoveViewer(String viewerName) {
		return false;
	}

	/**
	 * When the video is detached the video panel could be removed from the video & control panel,
	 * the controller panel could move to the position below the viewer containing the annotations,
	 * but for now this has not been implemented. 
	 * Instead the video panel is notified and it sets its size to 0x0, it is removed as listener
	 */
	@Override
	public void detach(Object object) {
		if (object instanceof PlayerLayoutModel) {
			if (videoPanel != null) {
				videoPanel.detach((PlayerLayoutModel) object);
				videoAndControlPanel.removeComponentListener(videoPanel);
				// if the videoPanel is not removed the image of the video remains in the main
				// window, unresponsive
				videoAndControlPanel.remove(videoPanel);
				doLayout();
			}
		}
	}

	/**
	 * Adds the video to the video panel once more which thereupon resizes.
	 */
	@Override
	public void attach(Object object) {
		if (object instanceof PlayerLayoutModel) {
			if (videoPanel != null) {
				// the video might have been added to the container
				container.remove(((PlayerLayoutModel) object).visualComponent);
				videoPanel.attach((PlayerLayoutModel) object);
				videoAndControlPanel.addComponentListener(videoPanel);
				
	    		GridBagConstraints gbc = new GridBagConstraints();
	    		int margin = ElanLayoutManager.CONTAINER_MARGIN;
	    		gbc.anchor = GridBagConstraints.NORTH;
	    		gbc.fill = GridBagConstraints.BOTH;
	    		gbc.weightx = 1.0;
	    		gbc.weighty = 0.01;
	    		gbc.insets = new Insets(margin, margin, 0, margin);
	    		videoAndControlPanel.add(videoPanel, gbc);
				
				doLayout();
			}
		}
	}

	@Override
	public void preferencesChanged() {
		if (!initialized) {
			doLayout();// initializes
		}
		Integer fontSize = Preferences.getInt(prefKeyFontSize, transcription);
		if (fontSize != null && fontSizePanel != null) {
			fontSizePanel.setFontSize(fontSize);// updates the viewer as well
		}
				
		Float vertDivLocation = Preferences.getFloat(prefKeyVertSplitLoc, transcription);
		if (vertDivLocation != null && vertSplitPane != null) {
			int paneHeight = vertSplitPane.getHeight();
			if (paneHeight != 0) {// not fully initialized yet
				int divLoc = (int) (paneHeight * vertDivLocation);
				vertSplitPane.setDividerLocation(divLoc);
			}
		}
		
		Float horiDivLocation = Preferences.getFloat(prefKeyHoriSplitLoc, transcription);
		if (horiDivLocation != null && horiSplitPane != null) {
			int paneWidth = horiSplitPane.getWidth();
			if (paneWidth == 0 && vertSplitPane != null) {
				paneWidth = vertSplitPane.getWidth();// sometimes the size of the internal splitpane is initialized later
			}
			if (paneWidth != 0) {// not fully initialized yet
				int divLoc = (int) (paneWidth * horiDivLocation);
				horiSplitPane.setDividerLocation(divLoc);
			}
		}
		
		Boolean contPlayMode = Preferences.getBool(prefKeyContinuousPlay, transcription);
		if (contPlayMode != null && contPlayMode.booleanValue()) {
			// default is false, only need to change when true
			turnsAndSceneViewer.setContinuousPlayMode(contPlayMode.booleanValue());
			if (continuousPlayAction != null) {
				continuousPlayAction.putValue(Action.SELECTED_KEY, contPlayMode);
			}
		}
		
		doLayout();
	}

	@Override
	public void cleanUpOnClose() {
		// all components have already been removed from the content pane by ElanLayoutManager
		if (tasControlPanel != null) {
			viewerManager.disconnectListener(tasControlPanel);
		}
		if (videoAndControlPanel != null && videoPanel != null) {
			videoAndControlPanel.removeComponentListener(videoPanel);
		}
		
		//System.out.println("Clean Close");
	}

	@Override
	public void isClosing() {
		if (vertSplitPane != null) {
			float paneHeight = (float) vertSplitPane.getHeight();
			int vdivLoc = vertSplitPane.getDividerLocation();// can be 0
			float relLoc = vdivLoc / (float) paneHeight;
			layoutManager.setPreference(prefKeyVertSplitLoc, relLoc , transcription);
		}
		if (horiSplitPane != null) {
			float paneWidth = (float) horiSplitPane.getWidth();
			int hdivLoc = horiSplitPane.getDividerLocation();// can be 0
			float relLoc = hdivLoc / paneWidth;
			layoutManager.setPreference(prefKeyHoriSplitLoc, relLoc, transcription);
		}
		layoutManager.setPreference(prefKeyFontSize, turnsAndSceneViewer.getFontSize(), transcription);
		layoutManager.setPreference(prefKeyContinuousPlay, 
				turnsAndSceneViewer.isContinuousPlayMode(), transcription);
	}

	/**
	 * Returns a list of possible receivers of zoom in, zoom out commands.
	 * First candidate is the signal viewer, next the turns-and-scene viewer (via the font size panel). 
	 */
	@Override
	public List<Zoomable> getZoomableViewers() {
		List<Zoomable> zoomList = new ArrayList<Zoomable>(2);

		if (signalViewer != null) {
			zoomList.add(signalViewer);
		}
		if (fontSizePanel != null) {
			zoomList.add(fontSizePanel);
		}
		return zoomList;
	}

}
