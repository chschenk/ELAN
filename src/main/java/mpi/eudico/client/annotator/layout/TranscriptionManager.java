package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ResizeComponent;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.transcriptionMode.PossibleTypesExtractor;
import mpi.eudico.client.annotator.transcriptionMode.TranscriptionModePlayerController;
import mpi.eudico.client.annotator.transcriptionMode.TranscriptionModeSettingsDlg;
import mpi.eudico.client.annotator.transcriptionMode.TranscriptionViewer;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Creates transcription mode layout.
 *
 * @author Aarthy Somasundaram
 */
public class TranscriptionManager implements ModeLayoutManager, ActionListener {
						
	private ElanLayoutManager layoutManager;
    private Container container;
    private ViewerManager2 viewerManager; 
    private Transcription transcription;
    private SignalViewer signalViewer;
	private ResizeComponent horMediaResizer;
	private TranscriptionModePlayerController playerController;
	private TranscriptionViewer transViewer;	
    
    //settings panel
    private JScrollPane settingsScrollPane;
    private JCheckBox autoPlayBackCB;
    private JCheckBox autoCreateAnnCB;
    private JCheckBox showTierNamesCB;
    private JCheckBox showColorOnlyOnNoColumnCB;
    private JCheckBox moveViaColumnCB;
    private JCheckBox showActiveCellInCenterCB;
    private JButton changeTypesButton; 
    
   	//align media button
	private final String toopTipAlignLeft = ElanLocale.getString("TranscriptionManager.AlignVideoLeft");
	private final String toopTipAlignRight = ElanLocale.getString("TranscriptionManager.AlignVideoRight");	
    private Icon alignLeftIcon;
	private Icon alignRightIcon;	
    private JButton butAlignVideoToRight;
	
	private boolean videoInRight = false;
	private boolean initialized = false;
	private int numVisualPlayers;
	
	private static int DEF_SIGNAL_HEIGHT = 200;	
	private final static Dimension BUTTON_SIZE = new Dimension(30, 20);	
	
	public final static String COMBOBOX_DEFAULT_STRING = ElanLocale.getString("TranscriptionManager.ComboBoxDefaultString");
		
	private float zoomLevelSignalViewer = -1F;
	
	private MouseListener mouseListener;
	private MouseMotionListener motionListener;
	
	/**
	 * Creates an instance of the TranscriptionManager
	 * 
	 * @param viewerManager
	 * @param elanLayoutManager
	 */
    public TranscriptionManager(ViewerManager2 viewerManager, ElanLayoutManager elanLayoutManager) {
        this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;
        transcription = viewerManager.getTranscription();        
        if(viewerManager.getMasterMediaPlayer() != null){
        	viewerManager.getMasterMediaPlayer().setMediaTime(0);
        }
        
        container = layoutManager.getContainer();    
        
        // divider component
        horMediaResizer = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL);
        horMediaResizer.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED)); 
        horMediaResizer.setPreferredSize(new Dimension(7,container.getHeight()));
        Component n = horMediaResizer.getComponent(0);
        horMediaResizer.remove(n);
        horMediaResizer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.weighty = 1.0; 
        horMediaResizer.add(n, gbc);
        
    	container.add(horMediaResizer);
    }
	
    /**
     * Adds the object to the layout
     */
	@Override
	public void add(Object object) {
		if (object instanceof SignalViewer) {
	    	setSignalViewer((SignalViewer) object);
	    } else if(object instanceof TranscriptionModePlayerController){
	    	setMediaPlayerController ((TranscriptionModePlayerController) object);
	    } 
	}	
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param signalViewer
	 */
	private void setSignalViewer(SignalViewer sigViewer){
		this.signalViewer  = sigViewer;	
		signalViewer.removeMouseListener(signalViewer);
		signalViewer.removeMouseMotionListener(signalViewer);		
		signalViewer.setEnabled(false);
		signalViewer.setMediaTime(0);
		signalViewer.setSelection(0,0);
		container.add(signalViewer);
        signalViewer.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        if(initialized){ 
        	doLayout();
        	transViewer.updateSignalViewer(signalViewer);        
        }   
        
        initializeMouseListener();       
        
        signalViewer.addMouseListener(mouseListener);
		signalViewer.addMouseMotionListener(motionListener);		
	}	
	
	private void initializeMouseListener(){
		if(mouseListener == null){
			mouseListener = new MouseListener(){
				@Override
				public void mouseClicked(MouseEvent e) {
					if (playerIsPlaying()) {
			            stopPlayer();
			        }
					signalViewer.mouseClicked(e);
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					if (playerIsPlaying()) {
			            stopPlayer();
			        }
					signalViewer.mousePressed(e);					
				}
				
				@Override
				public void mouseReleased(MouseEvent e) {
					signalViewer.mouseReleased(e);
				}	
				
				@Override
				public void mouseEntered(MouseEvent e) {	
					signalViewer.mouseEntered(e);
				}	
				
				@Override
				public void mouseExited(MouseEvent e) {
					signalViewer.mouseExited(e);
				}
			};
		}
		
		if(motionListener == null){
			motionListener = new MouseMotionListener(){
				@Override
				public void mouseDragged(MouseEvent e) {
					if (playerIsPlaying()) {
			            stopPlayer();
			        }
					signalViewer.mouseDragged(e);					
				}
				
				@Override
				public void mouseMoved(MouseEvent e) {
					signalViewer.mouseMoved(e);
				}
			};
		}
	}
	
	private boolean playerIsPlaying() {		
		return viewerManager.getMasterMediaPlayer().isPlaying();
	}
	
	private void stopPlayer(){
		if(playerController != null){
			playerController.stopLoop();
		}
		viewerManager.getMasterMediaPlayer().stop();
	}

	 
	/**
     * DOCUMENT ME!
     *
     * @param mediaPlayerController
     */
    private void setMediaPlayerController(TranscriptionModePlayerController mediaPlayerController) {     	
    	if(mediaPlayerController == null){
    		return;
    	}
    	playerController = mediaPlayerController;
    	container.add(playerController.getVolumeRatePanel());
		container.add(playerController.getPlayButtonsPanel()); 
		container.add(playerController.getModePanel());  
		container.add(playerController.getSelectionButtonsPanel()); 
		container.add(playerController.getSelectionPanel()); 
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param mediaPlayerController
     */
    private void destroyMediaPlayerController() { 
    	if(playerController == null){
    		return;
    	}
    	container.remove(playerController.getVolumeRatePanel());
		container.remove(playerController.getPlayButtonsPanel()); 
		container.remove(playerController.getSelectionButtonsPanel());  
		container.remove(playerController.getSelectionPanel());  
		container.remove(playerController.getModePanel());  		
		
		playerController = null;
    }
    
    /**
     * Removes the object from the layout
     */
	@Override
	public void remove(Object object) {
		if (object instanceof SignalViewer && signalViewer != null) {
			container.remove(signalViewer);
			signalViewer = null;
		}
	}

	/**
	 * makes the layout for this mode
	 */
	@Override
	public void doLayout() {				
		int containerWidth 		= container.getWidth();
	    int containerHeight 	= container.getHeight();
	    int containerMargin 	= 3;
	    int componentMargin 	= 5;
	    
	    int mediaAreaWidth 		= layoutManager.getMediaAreaWidth();
	    int visibleMediaX 		= containerMargin;
	    int visibleMediaY 		= containerMargin;	
	    
	    PlayerLayoutModel[] visualPlayers = layoutManager.getAttachedVisualPlayers();
		numVisualPlayers = visualPlayers.length;
		
		// if there is no media
		if(numVisualPlayers == 0){
			if(videoInRight){
				visibleMediaX = containerWidth - (mediaAreaWidth+ containerMargin);
			} 
		} 
		
		if (numVisualPlayers >= 1) {
			// layout the first video
			Component firstVisualComp = visualPlayers[0].visualComponent;
			float aspectRatio = visualPlayers[0].player.getAspectRatio();
			int firstMediaHeight = (int) (mediaAreaWidth / aspectRatio);				
			if(videoInRight){	
				visibleMediaX = (containerWidth - (mediaAreaWidth+containerMargin));
			}			
			firstVisualComp.setBounds(visibleMediaX, visibleMediaY, mediaAreaWidth,
						firstMediaHeight);	
			visibleMediaY = visibleMediaY+ firstMediaHeight;
		}
		
		if (numVisualPlayers == 2) {			
			Component secondVisualComp = visualPlayers[1].visualComponent;
			float aspectRatio = visualPlayers[1].player.getAspectRatio();	
			int secondMediaHeight = (int) (mediaAreaWidth / aspectRatio);			
			visibleMediaY = visibleMediaY+ componentMargin;			
			secondVisualComp.setBounds(visibleMediaX, visibleMediaY, mediaAreaWidth, secondMediaHeight);			
			visibleMediaY = visibleMediaY+secondMediaHeight;
		}
		else if (numVisualPlayers == 3) {			
			Component secondVisualComp = visualPlayers[1].visualComponent;
			float secondAR = visualPlayers[1].player.getAspectRatio();
			Component thirdVisualComp = visualPlayers[2].visualComponent;
			float thirdAR = visualPlayers[2].player.getAspectRatio();
			
			int widthPerPlayer = (mediaAreaWidth - componentMargin)/2;
			int secondHeight = (int)( widthPerPlayer/secondAR);
			int thirdHeight = (int) ( widthPerPlayer/thirdAR);
			int heightPerPlayer = Math.max(secondHeight, thirdHeight);
			
			visibleMediaY = visibleMediaY+ componentMargin;		
			
			secondVisualComp.setBounds(visibleMediaX, visibleMediaY, widthPerPlayer, secondHeight);		
			
			thirdVisualComp.setBounds(visibleMediaX + widthPerPlayer+componentMargin, 
					visibleMediaY,	widthPerPlayer, thirdHeight);		
			
			visibleMediaY = visibleMediaY+heightPerPlayer;
		}
		else if (numVisualPlayers >= 4) {
			Component secondVisualComp = visualPlayers[1].visualComponent;
			float secondAR = visualPlayers[1].player.getAspectRatio();
			Component thirdVisualComp = visualPlayers[2].visualComponent;
			float thirdAR = visualPlayers[2].player.getAspectRatio();
			Component fourthVisualComp = visualPlayers[3].visualComponent;
			float fourthAR = visualPlayers[3].player.getAspectRatio();
			
			int widthPerPlayer = (mediaAreaWidth - 2 *componentMargin)/3;
			int secondHeight = (int)( widthPerPlayer/secondAR);
			int thirdHeight = (int) ( widthPerPlayer/thirdAR);
			int fourthHeight = (int) ( widthPerPlayer/fourthAR);
			int heightPerPlayer = Math.max(secondHeight, thirdHeight);
			heightPerPlayer = Math.max(heightPerPlayer, fourthHeight);
			
			visibleMediaY = visibleMediaY+ componentMargin;					
		
			secondVisualComp.setBounds(visibleMediaX, visibleMediaY, 
					widthPerPlayer, secondHeight);
			thirdVisualComp.setBounds(visibleMediaX  + widthPerPlayer + componentMargin, visibleMediaY, 
					widthPerPlayer, thirdHeight);
			fourthVisualComp.setBounds(visibleMediaX + 2*widthPerPlayer + 2*componentMargin, visibleMediaY, 
					widthPerPlayer, fourthHeight);
			
			visibleMediaY = visibleMediaY+heightPerPlayer;
		}
		
		// layout for the time panel
		int timePanelX = visibleMediaX;
		int timePanelY = visibleMediaY + 2;
		int timePanelWidth = 0;
		int timePanelHeight = 0;        
		if (viewerManager != null) {
			timePanelWidth = viewerManager.getTimePanel().getPreferredSize().width;
			timePanelHeight = viewerManager.getTimePanel().getPreferredSize().height;
			if(numVisualPlayers > 0){	
				timePanelX = (visibleMediaX + (mediaAreaWidth / 2)) - (timePanelWidth / 2);
			} 					
			viewerManager.getTimePanel().setBounds(timePanelX, timePanelY, timePanelWidth, timePanelHeight);
		}
        
		//layout for the video Alignment button
		int butAlignVideoX = visibleMediaX;
		int butAlignVideoY = timePanelY + timePanelHeight + 4;
		int butAlignVideoWidth = butAlignVideoToRight.getPreferredSize().width;
		int butAlignVideoHeight = butAlignVideoToRight.getPreferredSize().height;       
		butAlignVideoToRight.setBounds(butAlignVideoX, butAlignVideoY, butAlignVideoWidth, butAlignVideoHeight);	

		// layout for the buttons
		int playButtonX = butAlignVideoX + butAlignVideoWidth;
		int playButtonY = butAlignVideoY;
		int playButtonWidth = 0;
		int playButtonHeight = 0;
		if(playerController != null){
			playButtonWidth = playerController.getPlayButtonsPanel().getPreferredSize().width;
			playButtonHeight = playerController.getPlayButtonsPanel().getPreferredSize().height;
	        if(numVisualPlayers > 0 ){		
			// playButtonWidth*4 === width of all 4 buttons			
				playButtonX = (visibleMediaX + (mediaAreaWidth / 2)) -
									((playButtonWidth*4) / 2);	
			}
			if (playButtonX < (butAlignVideoX + butAlignVideoWidth)) {
				playButtonX = butAlignVideoX + butAlignVideoWidth;
			}
			playerController.getPlayButtonsPanel().setBounds(playButtonX, playButtonY, playButtonWidth, playButtonHeight);   
		}
		
		//layout for the selection buttons panel
		int selButtonPanelX = playButtonX+playButtonWidth;;
		int selButtonPanelY = playButtonY;
		int selButtonPanelWidth = 0;
		int selButtonPanelHeight = 0;
        if (playerController != null) {
			selButtonPanelWidth = playerController.getSelectionButtonsPanel().getPreferredSize().width;
			selButtonPanelHeight = playerController.getSelectionButtonsPanel().getPreferredSize().height;			
			playerController.getSelectionButtonsPanel().setBounds(selButtonPanelX, selButtonPanelY, 
					selButtonPanelWidth, selButtonPanelHeight);
		}	
			
		//layout for the loop mode check box
		int  loopModeCBX = selButtonPanelX + selButtonPanelWidth;
		int  loopModeCBY = selButtonPanelY;
		int  loopModeCBWidth = 0;
		int  loopModeCBHeight = 0;
		if(playerController != null){
			 loopModeCBWidth = playerController.getModePanel().getPreferredSize().width;
			 loopModeCBHeight = playerController.getModePanel().getPreferredSize().height;
	        if(numVisualPlayers > 0 || signalViewer !=null) {
				loopModeCBX = (visibleMediaX + mediaAreaWidth) -loopModeCBWidth ;        	
			}        
			if(loopModeCBX < (selButtonPanelX + selButtonPanelHeight)){
				loopModeCBX = visibleMediaX;
				loopModeCBY = selButtonPanelY + selButtonPanelHeight + 5;
			}
			playerController.getModePanel().setBounds(loopModeCBX, loopModeCBY, loopModeCBWidth, loopModeCBHeight);	
		}
	
		//layout for the time interval panel
		int selPanelX = visibleMediaX;
		int selPanelY = loopModeCBY + loopModeCBHeight + 10;// was 25
		int selPanelWidth = 0;
		int selPanelHeight = 0;
        if (playerController!= null) {
			selPanelWidth = playerController.getSelectionPanel().getPreferredSize().width;
			selPanelHeight = playerController.getSelectionPanel().getPreferredSize().height;        	
			if(signalViewer != null){
    			selPanelX = visibleMediaX + (mediaAreaWidth / 2) - (selPanelWidth / 2);
			}
			playerController.getSelectionPanel().setBounds(selPanelX, selPanelY, selPanelWidth, selPanelHeight);
		}
        
		// layout signal Viewer
		int signalX = visibleMediaX;
		int signalY = selPanelY + selPanelHeight + 10;
		int signalWidth = mediaAreaWidth;
        int signalHeight = 0;
        
        if (signalViewer != null) {   
        	signalHeight = DEF_SIGNAL_HEIGHT/2;
        	signalViewer.setBounds(signalX, signalY, signalWidth, signalHeight);
        } 
        
        //	layout for volume, rate panel  
        int volX = visibleMediaX;
		int volY = signalY + signalHeight + 10;
		int volWidth = mediaAreaWidth;
		int volHeight = DEF_SIGNAL_HEIGHT/2;	
		if(playerController != null){
			playerController.getVolumeRatePanel().setBounds(volX, volY, volWidth, volHeight);
		}		
		
		int settingsPanelX = visibleMediaX;
		int settingsPanelY = volY + volHeight + 10;
		int settingsPanelWidth = mediaAreaWidth;
		int settingsPanelHeight = getSettingsScrollPane().getPreferredSize().height + getSettingsScrollPane().getHorizontalScrollBar().getPreferredSize().height;
		
		int availableSpace = containerHeight - containerMargin - settingsPanelY;
		
		if (settingsPanelHeight > availableSpace) {
			settingsPanelHeight = availableSpace;
		} 		
		
		getSettingsScrollPane().setBounds(settingsPanelX, settingsPanelY, settingsPanelWidth, settingsPanelHeight);
       
		
		// resize divider       
        int divX = visibleMediaX + mediaAreaWidth + containerMargin; // always keep visible
        int divY = 0; 
        int divWidth = horMediaResizer.getPreferredSize().width;     
        if(videoInRight){
        	divX = visibleMediaX - containerMargin- divWidth;
        }          
        horMediaResizer.setBounds(divX, divY, divWidth, containerHeight);     
		
        if(viewerManager.getMasterMediaPlayer() instanceof EmptyMediaPlayer && signalViewer == null){
        	if (viewerManager.getTimePanel() != null) {				
        		viewerManager.getTimePanel().setBounds(0, 0, 0, 0);
        	}	
        	
        	if(playerController != null){
        		playerController.getPlayButtonsPanel().setBounds(0, 0, 0, 0);
        		playerController.getVolumeRatePanel().setBounds(0,0,0,0);
        		playerController.getModePanel().setBounds(0, 0, 0, 0);
        		playerController.getSelectionButtonsPanel().setBounds(0, 0, 0, 0);
        		playerController.getSelectionPanel().setBounds(0, 0, 0, 0);
        	}
        	
    		settingsPanelY = butAlignVideoY + butAlignVideoHeight + 4;
    		getSettingsScrollPane().setBounds(settingsPanelX, settingsPanelY, settingsPanelWidth, settingsPanelHeight);
        } 
		
        // layout for the viewer
	    int transPanelX = divX + divWidth+ containerMargin;
        int transPanelY = containerMargin;
        int transPanelWidth = containerWidth - transPanelX - containerMargin;
        int transPanelHeight = containerHeight - transPanelY - containerMargin;  
        
        if(videoInRight){
        	transPanelX = containerMargin;
        	transPanelWidth = containerWidth - mediaAreaWidth - (3 * containerMargin)  - divWidth;
    	}        
        
        transViewer.setBounds(transPanelX, transPanelY, transPanelWidth, transPanelHeight);
        transViewer.setScrollerSize(transPanelWidth,transPanelHeight);        
		
        container.validate();
        //container.repaint();
	}
	
	@Override
	public void detach(Object object) {
		transViewer.focusTable();
	}

	@Override
	public void attach(Object object) {
		transViewer.focusTable();
	}	

	@Override
	public void updateLocale() {
		if (settingsScrollPane != null) {
			changeTypesButton.setText(ElanLocale.getString("TranscriptionManager.ChangeSettings"));
			autoPlayBackCB.setText(ElanLocale.getString("TranscriptionManager.AutoPlayBack"));
			autoCreateAnnCB.setText(ElanLocale.getString("TranscriptionManager.AutoCreateAnnotations"));
			showActiveCellInCenterCB.setText(ElanLocale.getString("TranscriptionManager.ShowActiveCellInCenter"));
			showTierNamesCB.setText(ElanLocale.getString("TranscriptionManager.ShowTierNames"));
			showColorOnlyOnNoColumnCB.setText(ElanLocale.getString("TranscriptionManager.ShowColorOnlyOnNoColumn"));
			moveViaColumnCB.setText(ElanLocale.getString("TranscriptionManager.MoveViaColumn"));
		}
	}

	/**
	 * Removes all the components in this layout
	 */
	@Override
	public void clearLayout() {		
		container.remove(horMediaResizer);		
		container.remove(transViewer);	
		container.remove(butAlignVideoToRight);
		container.remove(getSettingsScrollPane());	
		container.remove(viewerManager.getTimePanel());  		
		viewerManager.destroyTimePanel();
		viewerManager.destroyTranscriptionViewer();
		destroyMediaPlayerController();		
		transViewer= null;
		
		if(signalViewer != null){		
			container.remove(signalViewer);						
		}		
		container.repaint();
	}
	
	@Override
	public void cleanUpOnClose() {	
	}

	/**
	 * Initialize the required components for this mode
	 */
	@Override
	public void initComponents() {
		
		Float zoom = Preferences.getFloat("SignalViewer.ZoomLevel",transcription);
		if (zoom != null) {
			zoomLevelSignalViewer = zoom;
		}else {
			zoomLevelSignalViewer = -1F;
		}
		
		transViewer = viewerManager.createTranscriptionViewer();	
		transViewer.intializeViewer(this);	
		container.add(transViewer);		
		
		playerController = new TranscriptionModePlayerController(viewerManager, transViewer);
		add(playerController);
		
		container.add(viewerManager.getTimePanel());
		
		container.add(getSettingsScrollPane());
	    
	    alignLeftIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/AlignLeft16.gif"));
        alignRightIcon = new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/AlignRight16.gif"));
        
        butAlignVideoToRight = new JButton();
	    butAlignVideoToRight.setIcon(alignRightIcon);
	    butAlignVideoToRight.setPreferredSize(BUTTON_SIZE);	   
	    butAlignVideoToRight.addActionListener(this);	 
	    butAlignVideoToRight.setToolTipText(toopTipAlignRight);
	    container.add(butAlignVideoToRight);   
	    
	    // adding a signal viewer
	    createAndAddViewer(ELANCommandFactory.SIGNAL_VIEWER);	    
	    Boolean val = Preferences.getBool("TranscriptionManager.PlaceVideoInRight", transcription);
	    if (val != null && val) {
    		alignVideo();
	    }
	    
	    doLayout();
	    
		loadPreferences();
		initialized = true;	
	}
	
	public ElanLayoutManager getElanLayoutManager(){
		return layoutManager;
	}
		 
	 public TranscriptionModePlayerController getTranscriptionModePlayerController() {
		  return playerController;
	 }
	
	private JScrollPane getSettingsScrollPane() {
		if (settingsScrollPane != null) {
			return settingsScrollPane;
		}
		
		JPanel settingsPanel = new JPanel(new GridBagLayout());
		
		changeTypesButton = new JButton(ElanLocale.getString("TranscriptionManager.ChangeSettings"));
		changeTypesButton.addActionListener(this);
        
        autoPlayBackCB = new JCheckBox(ElanLocale.getString("TranscriptionManager.AutoPlayBack"));
        autoPlayBackCB.setSelected(true);
        autoPlayBackCB.addActionListener(this);  
        
        autoCreateAnnCB = new JCheckBox(ElanLocale.getString("TranscriptionManager.AutoCreateAnnotations"));
        autoCreateAnnCB.setSelected(true);
        autoCreateAnnCB.addActionListener(this);     
	    
	    showActiveCellInCenterCB = new JCheckBox(ElanLocale.getString("TranscriptionManager.ShowActiveCellInCenter"));
	    showActiveCellInCenterCB.setSelected(false);
	    showActiveCellInCenterCB.addActionListener(this);	  
        
	    showTierNamesCB = new JCheckBox(ElanLocale.getString("TranscriptionManager.ShowTierNames"));
	    showTierNamesCB.setSelected(true);	    
	    showTierNamesCB.addActionListener(this);	
	    
	    showColorOnlyOnNoColumnCB = new JCheckBox(ElanLocale.getString("TranscriptionManager.ShowColorOnlyOnNoColumn"));
	    showColorOnlyOnNoColumnCB.setSelected(false);	 
	    showColorOnlyOnNoColumnCB.setEnabled(false);
	    showColorOnlyOnNoColumnCB.addActionListener(this);
	    
	    moveViaColumnCB = new JCheckBox(ElanLocale.getString("TranscriptionManager.MoveViaColumn"));		
		moveViaColumnCB.addActionListener(this);	    
		
		Insets insets = new Insets(1, 1, 1, 1);  	
    	
    	GridBagConstraints gbc = new GridBagConstraints();    	
    	gbc.insets = insets;
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	gbc.fill = GridBagConstraints.NONE;    	
    	gbc.gridy = 0;
    	settingsPanel.add(autoPlayBackCB, gbc);
    	
    	gbc.gridy++;
    	settingsPanel.add(autoCreateAnnCB, gbc);
    	
    	gbc.gridy++;
    	settingsPanel.add(showTierNamesCB, gbc);
    	
    	gbc.gridy++;
    	gbc.insets = new Insets(1, 22, 1, 1);
    	settingsPanel.add(showColorOnlyOnNoColumnCB, gbc);
    	
    	gbc.gridy++;
    	gbc.insets = insets;
    	settingsPanel.add(moveViaColumnCB, gbc);
    	
    	gbc.gridy++;
    	settingsPanel.add(showActiveCellInCenterCB, gbc);
    	
    	gbc.gridy++;
    	//gbc.weighty = 1.0;	
    	gbc.weightx= 1.0;	
    	gbc.anchor = GridBagConstraints.WEST;
    	settingsPanel.add(changeTypesButton, gbc);

    	settingsPanel.setBorder(null);  
		
		settingsScrollPane = new JScrollPane(settingsPanel);
		settingsScrollPane.setBorder(new TitledBorder(ElanLocale.getString("TranscriptionManager.ChangeSettingsDlg.Title")));	
		settingsScrollPane.setBackground(settingsPanel.getBackground());		
		
		
		return settingsScrollPane;
	}
	
    /**
     * Loads the user preferences
     */
    private void loadPreferences(){		
		transViewer.loadPreferences();
		
		Boolean val = Preferences.getBool("TranscriptionManager.MoveViaColumn", null);
		if(val != null){
			moveViaColumnCB.setSelected(val);	
			transViewer.moveViaColumn(val);
		}	
		
		val = Preferences.getBool("TranscriptionManager.AutoPlayBack", null);
		if(val != null){
			autoPlayBackCB.setSelected(val);
			transViewer.setAutoPlayBack(val);
		}	
		
		val = Preferences.getBool("TranscriptionManager.AutoCreateAnnotation", null);
		if(val != null){
			autoCreateAnnCB.setSelected(val);
			transViewer.autoCreateAnnotations(val);
		}
		
		val = Preferences.getBool("TranscriptionManager.ScrollToCenter", null);
		if(val != null){
			showActiveCellInCenterCB.setSelected(val);
			transViewer.scrollActiveCellInCenter(val);
		}
		
		val = Preferences.getBool("TranscriptionManager.ShowTierNames", null);
		if(val != null){
			showTierNamesCB.setSelected(val);
			transViewer.showTierNames(val);
			if(showTierNamesCB.isSelected()){
				showColorOnlyOnNoColumnCB.setEnabled(false);
			} else {
				showColorOnlyOnNoColumnCB.setEnabled(true);
			}
		}
		
		val = Preferences.getBool("TranscriptionManager.ShowColorOnlyOnNoColumn", null);
		if(val != null){
			showColorOnlyOnNoColumnCB.setSelected(val);
			transViewer.showColorOnlyOnNoColumn(val);
		}				
		 
		List<String> types = null;
		types = Preferences.getListOfString("TranscriptionTable.ColumnTypes", transcription);
		if(types == null){
			List<String> colTypes = new ArrayList<String>();
			String stringPref = Preferences.getString("TranscriptionTable.Column1Type", transcription);
			if(stringPref != null){
				colTypes.add(stringPref);
				stringPref = Preferences.getString("TranscriptionTable.Column2Type", transcription);
				if(stringPref != null){
					colTypes.add(stringPref);
					stringPref = Preferences.getString("TranscriptionTable.Column3Type", transcription);
					if(stringPref != null){
						colTypes.add(stringPref);	
					}
				}
			}
			
			if (!colTypes.isEmpty()) {
				types = colTypes; 
			}
		}		
		
		// validate the type list
		List<String> validTypes = new ArrayList<String>();
		List<Integer> deletedTypeIndexList = new ArrayList<Integer>();
		if(types != null && types.size() > 0){
			PossibleTypesExtractor computeTypes = new PossibleTypesExtractor((TranscriptionImpl)viewerManager.getTranscription());
	    	
	    	List<String> possibleTypes = computeTypes.getPossibleTypesForColumn(1, validTypes);	    	
	    	if(possibleTypes.contains(types.get(0))){
				validTypes.add(types.get(0));
				for(int i = 1; i < types.size(); i++){
					possibleTypes = computeTypes.getPossibleTypesForColumn(validTypes.size()+1, validTypes);
					if(possibleTypes.contains(types.get(i))){
						validTypes.add(types.get(i));
					} else {
						deletedTypeIndexList.add(i);
					}
				}
			}
		}
		
		Integer intPref = Preferences.getInt("TranscriptionTable.FontSize", null);	
		
		if(validTypes == null || validTypes.size() <= 0){
			TranscriptionModeSettingsDlg settingsDialog = new TranscriptionModeSettingsDlg(layoutManager, null,null, null, intPref);
			settingsDialog.setVisible(true);						
			applyChanges(settingsDialog.getTierMap(), settingsDialog.getHiddenTiersList(), settingsDialog.getColumnTypeList(), settingsDialog.getFontSize(), settingsDialog.isValueChanged());
		} else {
			
			Map<TierImpl, List<TierImpl>> tierMap = null;
			List<String> hiddenTiersList = null;
			Map<String, List<String>> map = (Map<String, List<String>>) Preferences.getMap("TranscriptionTable.TierMap", viewerManager.getTranscription());
			if(map != null){			
				tierMap = getValidatedTierMap(map, deletedTypeIndexList);	
			}
			
			if(tierMap != null){
				List<String> valList = Preferences.getListOfString("TranscriptionTable.HiddenTiers", viewerManager.getTranscription());
				if(valList != null){
					hiddenTiersList = getValidatedTierList(valList);			
				}
				
				valList = Preferences.getListOfString("TranscriptionTable.NonEditableTiers", viewerManager.getTranscription());
				if(valList != null){			
					transViewer.setNoneditableTier(getValidatedTierList(valList));	
				}
			}
			
			
			
			applyChanges(tierMap, hiddenTiersList, validTypes, intPref, true);
			transViewer.updateTable();
		}
    }
    
    private Map<TierImpl, List<TierImpl>> getValidatedTierMap(Map<String, List<String>> map, List<Integer> deletedTypeIndexList){
    	Map<TierImpl, List<TierImpl>> validMap = new HashMap<TierImpl, List<TierImpl>>();
		List<String> tierNamesList = null;
		List<TierImpl> tierList = null;
		String keyObj;
		Iterator<String> keyIt = map.keySet().iterator();
		
		//List<String> validTierNameList;
    	
		TierImpl referenceTier = null;
    	while (keyIt.hasNext()) {
    		keyObj = keyIt.next();	
    		referenceTier = null;
    		if(map.get(keyObj) instanceof List){
    			tierNamesList =  map.get(keyObj);
    			referenceTier = (TierImpl)viewerManager.getTranscription().getTierWithId(keyObj);
    			if(referenceTier != null){
    				tierList = new ArrayList<TierImpl>();
    				
    				for(int i=0; i< tierNamesList.size(); i++){
    					if(deletedTypeIndexList.contains(i)){
    						continue;
    					}    					
    					String tierName = tierNamesList.get(i);
    					if(tierName.equals("No tier")){
    						tierList.add(null);    					
    					} else if(tierName.equals(keyObj)){
    						tierList.add(referenceTier);    					
    					} else {
    						tierList.add((TierImpl)viewerManager.getTranscription().getTierWithId(tierName));    	
    					}
    				}    
    				validMap.put(referenceTier, tierList);
    			}
    		}
    	}
    	
    	if(validMap.keySet().size() <= 0){
    		return null;
    	}
    	return validMap;
    		
    }
    
    private List<String> getValidatedTierList(List<String> tierNames){
		List<String> tierList = new ArrayList<String>();
		String tierName;
		for(int i = 0; i < tierNames.size(); i++){
			tierName = tierNames.get(i);
			if(viewerManager.getTranscription().getTierWithId(tierName) != null){
				tierList.add(tierNames.get(i));
			}
		}		
		return tierList;
	}
    
    /**
     * Returns whether the all the components fro this mode
     * is initialized
     * 
     * @return initialized, if true initialized else
     * 						false - not initialized
     */
	public boolean isInitialized(){
		return initialized;
	}
	
	/**
	 * Switches to annotation mode for editing the
	 * active annotation
	 */
	public void editInAnnotationMode(){
		((ElanFrame2)layoutManager.getElanFrame()).setMenuSelected(
				ELANCommandFactory.ANNOTATION_MODE, FrameConstants.OPTION);
		layoutManager.changeMode(ElanLayoutManager.NORMAL_MODE);
	}
	
	/**
	 *  Set the preferences
	 */
	public void setPreference(String key, Object value, Object document) {
		if (document instanceof Transcription) {
			Preferences.set(key, value, (Transcription)document, false, false);
		} else {
			Preferences.set(key, value, null, false, false);
		}
	}
	
	/**
	 * Applies the new type/size changes to the transcription 
	 * table
	 * @param hashMap 
	 * @param b 
	 * @param hashMap 
	 * 
	 * @param type1, type for column 1
	 * @param type2, type for column 2
	 * @param type3, type for column 3
	 * @param size, font size for the table
	 */
	private void applyChanges(Map<TierImpl, List<TierImpl>> hashMap, List<String> hiddenTiers, List<String> types, Integer size, boolean valueChanged){				
		if(types == null ){
			transViewer.focusTable();
			return;
		}
		
		if(valueChanged){
			transViewer.setColumnTypeList(types);			
			transViewer.setTierMap(hashMap);
			transViewer.setHiddenTiersList(hiddenTiers);
		}				
				
		if(size != null){
			if(size != transViewer.getFontSize()){				
				transViewer.setFontSize(size);
			}
			setPreference("TranscriptionTable.FontSize", transViewer.getFontSize(), null);	
		}
		
		if(valueChanged){			
			transViewer.loadTable();
			transViewer.checkForMerge();
		} 	
		
		transViewer.reValidateTable();
		transViewer.focusTable();
	}
	
	@Override
	public void preferencesChanged() {
	}
	
	/**
	 * Enables or disables the menu's related to the current layout
	 * 
	 * @param enabled
	 */
	@Override
	public void enableOrDisableMenus(boolean enabled){
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.ANNOTATION, enabled);
		
		//List<String> searchMenu = new ArrayList<String>();
		//searchMenu.add(ElanLocale.getString(ELANCommandFactory.SEARCH_DLG));
		//((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(searchMenu, FrameConstants.SEARCH, enabled);
		
		// command ACTIONS
		Action act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.DELETE_ANNOTATION);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.MERGE_ANNOTATION_WN);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.MERGE_ANNOTATION_WB);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.CLEAR_SELECTION);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.SELECTION_BOUNDARY);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.SELECTION_CENTER);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PLAY_PAUSE);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PLAY_SELECTION);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PLAY_AROUND_SELECTION);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PIXEL_LEFT);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PIXEL_RIGHT);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.PREVIOUS_FRAME);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.NEXT_FRAME);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.SECOND_LEFT);
		if(act != null){
			act.setEnabled(enabled);
		}
		
		act = ELANCommandFactory.getCommandAction(transcription, ELANCommandFactory.SECOND_RIGHT);
		if(act != null){
			act.setEnabled(enabled);
		}	
	}	    
  
	
	private void alignVideo(){
		videoInRight = !videoInRight;
		horMediaResizer.changeBehaviourToDecrease(videoInRight) ;
		if(videoInRight){
			butAlignVideoToRight.setToolTipText(toopTipAlignLeft);
			butAlignVideoToRight.setIcon(alignLeftIcon);				
		}else{
			butAlignVideoToRight.setToolTipText(toopTipAlignRight);
			butAlignVideoToRight.setIcon(alignRightIcon);	
		}
		doLayout();
		setPreference("TranscriptionManager.PlaceVideoInRight", videoInRight, transcription);
		transViewer.focusTable();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {			
		if (e.getSource() == butAlignVideoToRight){
			alignVideo();
		} 
		else if(e.getSource() == changeTypesButton){
			viewerManager.getMasterMediaPlayer().stop();
			TranscriptionModeSettingsDlg settingsDialog = new TranscriptionModeSettingsDlg(layoutManager, transViewer.getColumnTypes(), transViewer.getTierMap(), transViewer.getHiddenTiers(), transViewer.getFontSize());
			settingsDialog.setVisible(true);
			applyChanges(settingsDialog.getTierMap(), settingsDialog.getHiddenTiersList(), settingsDialog.getColumnTypeList(), settingsDialog.getFontSize(), settingsDialog.isValueChanged());
		} 
		else {			
			if(viewerManager.getMasterMediaPlayer().isPlaying()){
				viewerManager.getMasterMediaPlayer().start();
			}					
			if(e.getSource() == showTierNamesCB){
				transViewer.showTierNames(showTierNamesCB.isSelected());
				setPreference("TranscriptionManager.ShowTierNames", showTierNamesCB.isSelected(), null);
				if(showTierNamesCB.isSelected()){
					showColorOnlyOnNoColumnCB.setEnabled(false);
				} else {
					showColorOnlyOnNoColumnCB.setEnabled(true);
				}
			}
			else if(e.getSource() == showColorOnlyOnNoColumnCB){
				transViewer.showColorOnlyOnNoColumn(showColorOnlyOnNoColumnCB.isSelected());
				setPreference("TranscriptionManager.ShowColorOnlyOnNoColumn", showColorOnlyOnNoColumnCB.isSelected(), null);
			}
			else if(e.getSource() == moveViaColumnCB){
				transViewer.moveViaColumn(moveViaColumnCB.isSelected());		
				setPreference("TranscriptionManager.MoveViaColumn", moveViaColumnCB.isSelected(), null);
			} 
			else if(e.getSource() == autoPlayBackCB){	
				transViewer.setAutoPlayBack(autoPlayBackCB.isSelected());		
				setPreference("TranscriptionManager.AutoPlayBack", autoPlayBackCB.isSelected(), null);    
			} else if(e.getSource() == autoCreateAnnCB){
				transViewer.autoCreateAnnotations(autoCreateAnnCB.isSelected());
				setPreference("TranscriptionManager.AutoCreateAnnotation", autoCreateAnnCB.isSelected(), null);    
			}
			else if(e.getSource() == showActiveCellInCenterCB){
				transViewer.scrollActiveCellInCenter(showActiveCellInCenterCB.isSelected());	
				setPreference("TranscriptionManager.ScrollToCenter", showActiveCellInCenterCB.isSelected(), null);
			}		
			transViewer.focusTable();
		}
	}
	
	@Override
	public void shortcutsChanged() {
		transViewer.shortcutsChanged();
		playerController.updateLocale();
	}

	@Override
	public void createAndAddViewer(String viewerName) {
		if(viewerName == null){
			return;
		}
		if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)) {
			layoutManager.add(viewerManager.createSignalViewer());
		}
	}

	@Override
	public boolean destroyAndRemoveViewer(String viewerName) {
		if(viewerName == null){
			return false;
		}
		if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER) && signalViewer != null) {
			container.remove(signalViewer);
			signalViewer = null;
			viewerManager.destroySignalViewer();	
			return true;
		}		
		return false;		
	}
	
	@Override
	public void isClosing() {	
		playerController.isClosing();
		transViewer.isClosing();	
		
		if(signalViewer != null){	
			signalViewer.removeMouseListener(mouseListener);
			signalViewer.removeMouseMotionListener(motionListener);	
	        signalViewer.addMouseListener(signalViewer);
			signalViewer.addMouseMotionListener(signalViewer);	
			
			signalViewer.setRecalculateInterval(true);
			signalViewer.setBorder(null);
			signalViewer.updateInterval(0, viewerManager.getMasterMediaPlayer().getMediaDuration());
			signalViewer.setMediaTime(viewerManager.getMasterMediaPlayer().getMediaTime());
			signalViewer.setEnabled(true);			
			
			Float zoom = Preferences.getFloat("SignalViewer.ZoomLevel", transcription);
			if (zoom != null) {
				setPreference("TM.SignalViewer.ZoomLevel", new Float(zoom), transcription);
			}
			
			if(zoomLevelSignalViewer > -1F){
				setPreference("SignalViewer.ZoomLevel", zoomLevelSignalViewer, transcription);
			} else {
				setPreference("SignalViewer.ZoomLevel", new Float(100.0f), transcription);
			}
			signalViewer.preferencesChanged();					
		}		
		transViewer.setActiveAnnotation();
	}

	@Override
	public List<Zoomable> getZoomableViewers() {
		List<Zoomable> zoomList = new ArrayList<Zoomable>(2);
		
		if (transViewer != null) {
			zoomList.add(transViewer);
		}
		if (signalViewer != null) {
//			zoomList.add(signalViewer);
		}

		return zoomList;
	}
}
