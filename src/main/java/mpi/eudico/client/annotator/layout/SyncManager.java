package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.MultiSourcePlayer;
import mpi.eudico.client.annotator.player.SyncPlayer;
import mpi.eudico.client.annotator.timeseries.TSViewerPlayer;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.SignalPlayerView;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;
import mpi.eudico.client.mediacontrol.Controller;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.util.TimeFormatter;

/**
 * A class for the synchronization of media players, i.e. setting relative or absolute 
 * offsets for each media player.
 * 
 */
public class SyncManager implements ActionListener, ItemListener, ChangeListener, ModeLayoutManager {
    private ViewerManager2 viewerManager;
    private ElanLayoutManager layoutManager;
    private ElanMediaPlayer playerInFocus;
    private Map<ElanMediaPlayer, String> nameForPlayer;
    private Map<String, ElanMediaPlayer> playerForName;
    private Map<ElanMediaPlayer, JPanel> labelForPlayer;
    private Map<ElanMediaPlayer, JRadioButton> playerButtons;
    private Map<ElanMediaPlayer, SignalPlayerView> viewForSyncPlayer;
    private Map<ElanMediaPlayer, Controller> controllerForView;
    private Map<ElanMediaPlayer, Long> lastSyncPositionForPlayer;
    private ButtonGroup buttonGroup;
    //private JRadioButton connectedButton;
    private JRadioButton absOffsetRB;
    private JRadioButton relOffsetRB;
    private JRadioButton allPlayersRB;
    private JPanel contentPanel;
    private JPanel playerPanel;
    private JPanel offsetPanel;
    private ButtonGroup playerGroup;
    private JButton applyButton;
    private JButton resetButton;
    private boolean connected;
    private boolean relative;
    private boolean allPlayerMode = true;
    
    private List<PlayerLayoutModel> playerList;
    private Container container;
    private JPanel volRatePanel;
    private JLabel volNameLabel;
    private JLabel volValueLabel;
    private JLabel rateNameLabel;
    private JLabel rateValueLabel;
    private JSlider volSlider;
    private JSlider rateSlider;
    private ElanMediaPlayerController mediaPlayerController;
	private int COMP_MARGIN = 5;
	private static int CONTROLS_MARGIN = 20;
    

    /**
     * Creates a new SyncManager instance
     *
     * @param viewerManager the viewer manager
     * @param layoutManager the layout manager
     */
    public SyncManager(ViewerManager2 viewerManager,
        ElanLayoutManager layoutManager) {
        this.viewerManager = viewerManager;
        this.layoutManager = layoutManager;
        
        playerList = new ArrayList<PlayerLayoutModel>(layoutManager.getPlayerList());
        container = layoutManager.getElanFrame().getContentPane();

        nameForPlayer = new HashMap<ElanMediaPlayer, String>();
        playerForName = new HashMap<String, ElanMediaPlayer>();
        labelForPlayer = new HashMap<ElanMediaPlayer, JPanel>();
        playerButtons = new HashMap<ElanMediaPlayer, JRadioButton>();
        viewForSyncPlayer = new HashMap<ElanMediaPlayer, SignalPlayerView>(6);
        controllerForView = new HashMap<ElanMediaPlayer, Controller>(4);
        lastSyncPositionForPlayer = new HashMap<ElanMediaPlayer, Long>(8);

        buttonGroup = new ButtonGroup();
        contentPanel = new JPanel(new GridLayout(1, 2));
        playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        offsetPanel = new JPanel();
        offsetPanel.setLayout(new BoxLayout(offsetPanel, BoxLayout.Y_AXIS));
		
		absOffsetRB = new JRadioButton();
		absOffsetRB.setFont(absOffsetRB.getFont().deriveFont(Font.PLAIN, 10));
		absOffsetRB.setSelected(true);
		absOffsetRB.addItemListener(this);
		buttonGroup.add(absOffsetRB);
		offsetPanel.add(absOffsetRB);
        //connectedButton = absOffsetRB;
        
		relOffsetRB = new JRadioButton();
		relOffsetRB.setFont(relOffsetRB.getFont().deriveFont(Font.PLAIN, 10));
		relOffsetRB.setSelected(false);
		relOffsetRB.addItemListener(this);
	    buttonGroup.add(relOffsetRB);
	    offsetPanel.add(Box.createRigidArea(new Dimension(0, 2)));
	    offsetPanel.add(relOffsetRB);
		applyButton = new JButton();
		applyButton.setFont(applyButton.getFont().deriveFont(Font.PLAIN, 10));
		applyButton.addActionListener(this);
		
		offsetPanel.add(Box.createRigidArea(new Dimension(0, 12)));
		offsetPanel.add(applyButton);
        connected = true;
        
        resetButton = new JButton();
        resetButton.setFont(resetButton.getFont().deriveFont(Font.PLAIN, 10));
        resetButton.addActionListener(this);
        offsetPanel.add(resetButton);
        
        playerGroup = new ButtonGroup();
        allPlayersRB = new JRadioButton();
        allPlayersRB.setFont(allPlayersRB.getFont().deriveFont(Font.PLAIN, 10));
		allPlayersRB.setSelected(true);
        allPlayersRB.addItemListener(this);
        playerGroup.add(allPlayersRB);
        playerPanel.add(allPlayersRB);
        
        contentPanel.add(offsetPanel);
		contentPanel.add(playerPanel);
		
		container.add(getPlayerSelectionPanel());
		
    }

    /**
     * Adds a object to the layout.
     *
     * @param object 
     */
    @Override
	public void add(Object object) {
    	// check whether the object is already in the layout
    	if (object instanceof PlayerLayoutModel) {
    		PlayerLayoutModel plm = (PlayerLayoutModel) object;
 
    		PlayerLayoutModel model = null;
    		for (int i = 0; i < playerList.size(); i++) {
    			model = playerList.get(i);
    			if (model == plm || model.player == plm.player) {
    				return;
    			}
    			if (model.player.getMediaDescriptor() != null &&
    					plm.player.getMediaDescriptor() != null &&
    					plm.player.getMediaDescriptor().mediaURL.equals(model.player.getMediaDescriptor().mediaURL)) {
    				return;
    			}
    		}
    	
    		// not yet in the list
    		playerList.add(plm);
    		addInternal(plm);
    		doLayout();
    	} else if (object instanceof ElanMediaPlayer) {
    		ElanMediaPlayer player = ((ElanMediaPlayer) object);
    		
    		PlayerLayoutModel model = null;
    		for (int i = 0; i < playerList.size(); i++) {
    			model = playerList.get(i);
    			if (model.player == player) {
    				return;
    			}
    			if (model.player.getMediaDescriptor() != null &&
    					player.getMediaDescriptor() != null &&
    					player.getMediaDescriptor().mediaURL.equals(model.player.getMediaDescriptor().mediaURL)) {
    				return;
    			}
    		}
    		
        	player.setLayoutManager(layoutManager);// just to be sure
        	PlayerLayoutModel plModel = new PlayerLayoutModel(player, layoutManager);
        	
        	playerList.add(plModel);
        	addInternal(plModel);
        	doLayout();
    	} else if (object instanceof ElanMediaPlayerController) {
            this.mediaPlayerController = ((ElanMediaPlayerController) object);
            
            // add the control components to the container
            container.add(mediaPlayerController.getPlayButtonsPanel());
            container.add(mediaPlayerController.getSliderPanel());
            container.add(mediaPlayerController.getTimePanel());
            container.add(mediaPlayerController.getSelectionPanel());
            container.add(mediaPlayerController.getSelectionButtonsPanel()); 
            container.add(getVolumeRatePanel());
            
        } else if (object instanceof TimeSeriesViewer) {
			TimeSeriesViewer tsViewer = (TimeSeriesViewer) object;
			tsViewer.setTimeScaleConnected(false);
			
			TSViewerPlayer tsSyncPlayer = new TSViewerPlayer(tsViewer, 
        			viewerManager.getMasterMediaPlayer().getMediaDuration());
			tsSyncPlayer.setTrackManager(tsViewer.getTrackManager());
			tsSyncPlayer.setMediaTime(viewerManager.getMasterMediaPlayer().getMediaTime());
			//tsViewer.setViewerManager(viewerManager);
			tsViewer.setSyncModeViewer(true);
			
			//
			//viewerManager.disconnectListener(tsSyncPlayer.getViewer());
	    	////PeriodicUpdateController controller = new PeriodicUpdateController(25);
	    	////tsSyncPlayer.addController(controller);
	    	////controllerForView.put(tsSyncPlayer, controller);
	    	//viewerManager.connectListener(controller);
	    	//
			//tsSyncPlayer.setLayoutManager(layoutManager);
			viewerManager.addMediaPlayer(tsSyncPlayer);
			
        	PlayerLayoutModel plModel = new PlayerLayoutModel(tsSyncPlayer, layoutManager);
        	plModel.setSyncOnly(true);
        	playerList.add(plModel);
        	addInternal(plModel);
			//viewerManager.destroyMediaPlayer(tsSyncPlayer);
        }
    }
    
    /**
     * Internal add method for objects that already have been handled after 
     * adding from the main layout manager.
     * 
     * @param object
     */
    private void addInternal(Object object) {
    	if (object instanceof PlayerLayoutModel) {
    		PlayerLayoutModel plm = (PlayerLayoutModel) object;
    		
			// this is for the case there is already a media player for the wav files	
			if (plm.player.getMediaDescriptor() != null && 
					MediaDescriptor.WAV_MIME_TYPE.equals(plm.player.getMediaDescriptor().mimeType)) {
				
				// separately store the viewer component for the wav players
				SignalPlayerView spv = new SignalPlayerView(plm.player);
		    	////PeriodicUpdateController controller = new PeriodicUpdateController(25);
		    	////plm.player.addController(controller);
		    	////plm.player.addControllerListener(spv);
		    	viewerManager.connectListener(spv);

				spv.setSelectionObject(viewerManager.getSelection());
				viewerManager.getSelection().addSelectionListener(spv);
				spv.setViewerManager(viewerManager);
				container.add(spv);
				
				viewForSyncPlayer.put(plm.player, spv);
				////controllerForView.put(plm.player, controller);
			}
			
			addInternal(plm.player);	
			container.add(getPlayerLabel(plm.player)); 
			
			if (plm.isSyncOnly()) {	
				viewerManager.addMediaPlayer(plm.player);
	    		if (plm.player.getVisualComponent() != null) {
	    			container.add(plm.player.getVisualComponent()); 
	    		}
	    		if (plm.player instanceof SyncPlayer) {
	    			((SyncPlayer) plm.player).setSyncConnected(true);
	    			plm.player.setMediaTime(viewerManager.getMasterMediaPlayer().getMediaTime());
	    		}
			}
			
    	} else if (object instanceof ElanMediaPlayer) {
    		ElanMediaPlayer player = ((ElanMediaPlayer) object);    		
    		int size = 1;
        	while (playerForName.containsKey(String.valueOf(size))) {
        		size++;
        	}
        	String name = String.valueOf(size);
            nameForPlayer.put(player, name);
            playerForName.put(name, player);
            labelForPlayer.put(player, createLabelPanel(player));
            lastSyncPositionForPlayer.put(player, -1L);

            JRadioButton button = new JRadioButton(
            	ElanLocale.getString("SyncMode.Label.Player") + " " + name);
            button.setFont(Constants.deriveSmallFont(button.getFont()));
            button.addItemListener(this);
            playerButtons.put(player, button);
            playerGroup.add(button);
            
            playerPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            playerPanel.add(button);
    	} else if (object instanceof ElanMediaPlayerController) {
            this.mediaPlayerController = ((ElanMediaPlayerController) object);
            
            // add the control components to the container
            container.add(mediaPlayerController.getPlayButtonsPanel());
            container.add(mediaPlayerController.getSliderPanel());
            container.add(mediaPlayerController.getTimePanel());
            container.add(mediaPlayerController.getSelectionPanel());
            container.add(mediaPlayerController.getSelectionButtonsPanel()); 
            container.add(getVolumeRatePanel());
        }
    }

    /**
     * Removes a media player.
     *
     * @param player media player
     */
    @Override
	public void remove(Object object) {
    	
    	if (object instanceof ElanMediaPlayer) {
    		ElanMediaPlayer player = ((ElanMediaPlayer) object);
    		
    		container.remove(getPlayerLabel(player));
    		
    		if (nameForPlayer.containsKey(player)) {
                playerForName.remove(nameForPlayer.get(player));
                nameForPlayer.remove(player);
                labelForPlayer.remove(player);            
    			playerButtons.get(player).removeItemListener(this);
                buttonGroup.remove(playerButtons.get(player));
                playerPanel.remove(playerButtons.get(player));
                playerButtons.remove(player);
                
                if (viewForSyncPlayer.containsKey(player)) {
                	SignalPlayerView spv = viewForSyncPlayer.get(player);
                	container.remove(spv);
                	viewForSyncPlayer.remove(player);
                	Controller c = controllerForView.get(player);
                	if (c != null) {
	                	c.removeControllerListener(spv);
	                	player.removeController(c);
                	}
                	viewerManager.disconnectListener(spv);
                	spv.setSelectionObject(null);
                	viewerManager.getSelection().removeSelectionListener(spv);
                	spv.setViewerManager(null);
                } else if (player instanceof SyncPlayer) {
                	if (player.getVisualComponent() != null) {
                		container.remove(player.getVisualComponent());
                	}
                	Controller c = controllerForView.get(player);
                	if (c != null) {
                		player.removeController(c);
                	}
                	viewerManager.destroyMediaPlayer(player);
                }
    		}
    		
        } else if (object instanceof PlayerLayoutModel) {
        	PlayerLayoutModel plModel = (PlayerLayoutModel) object;
        	playerList.remove(plModel);
        	remove(plModel.player);
        }
    }

    /**
     * Returns the player selection panel.
     *
     * @return the panel
     */
    public JPanel getPlayerSelectionPanel() {
		return contentPanel;
    }

    /**
     * Updates the localized Label for the player.
     *
     * @param player the player 
     *
     * @return a localized JLabel
     */
    public JPanel getPlayerLabel(ElanMediaPlayer player) {
        if (!nameForPlayer.containsKey(player)) {
            return null;
        }

        JPanel panel = labelForPlayer.get(player);
        if (panel == null) {
        	panel = createLabelPanel(player);
        	labelForPlayer.put(player, panel);
        	return panel;
        }
        
        Container playPanel = (Container) panel.getComponent(0);
        Component nameComp = playPanel.getComponent(0);
        if (nameComp instanceof JLabel) {
        	JLabel nameLabel = (JLabel)nameComp;
        	// the name (number) could have been changed
    		String name = ElanLocale.getString("SyncMode.Label.Player") + " " +
			nameForPlayer.get(player) + " " +
			ElanLocale.getString("SyncMode.Label.Offset") + ": ";
        	nameLabel.setText(name);
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(1, 1, 1, 1);
        
        if (playPanel.getComponentCount() > 1) {
        	playPanel.remove(1);
        }
        
        // the current master gets the master time label that is alive
        // the others get a static label that shows their offset
        if (player == playerInFocus) {
            playPanel.add(viewerManager.getTimePanel(), gbc);
        } else {
            JLabel offsetLabel = new JLabel();

            offsetLabel.setText(TimeFormatter.toString(player.getOffset()));
            playPanel.add(offsetLabel, gbc);
        }
        
		// update the combo box
        if (player instanceof MultiSourcePlayer) {
			JComboBox box = null;
			JLabel fileLabel = null;
			
			Component[] comps = panel.getComponents();
			for (int i = 0; i < comps.length; i++) {
				if (comps[i] instanceof JComboBox) {
					box = (JComboBox) comps[i];
				}
				if (comps[i] instanceof JLabel) {
					fileLabel = (JLabel) comps[i];
				}
			}
			
			String[] sources = ((MultiSourcePlayer) player).getDescriptorStrings();
			if (box != null) {
				sourceloop:
				for (int i = 0; i < sources.length; i++) {
					for (int j = 0; j < box.getItemCount(); j++) {
						if (box.getItemAt(j).equals(sources[i])) {
							continue sourceloop;
						}
					}
					box.addItem(sources[i]);
				}
			
				boxloop:
				for (int j = 0; j < box.getItemCount(); j++) {
					for (int i = 0; i < sources.length; i++) {
						if (box.getItemAt(j).equals(sources[i])) {
							continue boxloop;
						}
					}
					box.removeItemAt(j);
				}
				
				if (fileLabel != null) {
					fileLabel.setText((String) box.getSelectedItem());
				}
			}

        }
        
        return panel;
    }

    /**
     * Creates the panel with name and time labels etc.
     * @param player the player
     * @return the label panel
     */
    private JPanel createLabelPanel(ElanMediaPlayer player) {
    	JPanel panel = new JPanel(new GridBagLayout());
    	
        if (!nameForPlayer.containsKey(player)) {
            return panel;
        }
        
        JLabel nameLabel = new JLabel();
        JPanel playPanel = new JPanel(new GridBagLayout());
		String name = ElanLocale.getString("SyncMode.Label.Player") + " " +
			nameForPlayer.get(player) + " " +
			ElanLocale.getString("SyncMode.Label.Offset") + ": ";
        //nameLabel.setFont(Constants.SMALLFONT);
        //nameLabel.setText(nameForPlayer.get(player) + " offset: ");
        nameLabel.setText(name);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(1, 1, 1, 1);
        playPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.SOUTHWEST;

        // the current master gets the master time label that is alive
        // the others get a static label that shows their offset
        if (player == playerInFocus) {
            playPanel.add(viewerManager.getTimePanel(), gbc);
        } else {
            JLabel offsetLabel = new JLabel();

            offsetLabel.setText(TimeFormatter.toString(player.getOffset()));
            playPanel.add(offsetLabel, gbc);
        }
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(playPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		
		JLabel fileLabel = new JLabel();
		fileLabel.setFont(Constants.deriveSmallFont(fileLabel.getFont()));
		if (player.getMediaDescriptor() != null) {			
			fileLabel.setText(FileUtility.fileNameFromPath(
				player.getMediaDescriptor().mediaURL));
		} else {
			fileLabel.setText("-");
		}
		panel.add(fileLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		
		if (player instanceof MultiSourcePlayer) {
			String[] sources = ((MultiSourcePlayer) player).getDescriptorStrings();
			if (sources.length > 1) {
				JComboBox box = new JComboBox();
				
				for (int i = 0; i < sources.length; i++) {
					box.addItem(sources[i]);
					if ( i == 0) {
						fileLabel.setText(sources[i]);
					}
				}
				box.addItemListener(this);
				panel.add(box, gbc);
			} else {
				fileLabel.setText(sources[0]);
			}
		}
		
    	return panel;
    }
    
    private JPanel getVolumeRatePanel() {
    	if (volRatePanel != null) {
    		return volRatePanel;
    	}
    	volRatePanel = new JPanel(new GridBagLayout());
    	volNameLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Volume"));
    	int volume = (int)(viewerManager.getVolumeManager().getMasterVolume() * 100);
    	Float value = Preferences.getFloat("MediaControlVolume", viewerManager.getTranscription());
    	if (value != null) {
    		volume = (int) (value.floatValue() * 100);
    	}
    	volValueLabel = new JLabel(String.valueOf(volume));
    	volValueLabel.setFont(volValueLabel.getFont().deriveFont(Font.PLAIN, 10));
    	volSlider = new JSlider(0, 100, volume);
    	volSlider.addChangeListener(this);
    	
    	rateNameLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Rate"));
    	int rate = (int)(viewerManager.getMasterMediaPlayer().getRate() * 100);
    	value = Preferences.getFloat("MediaControlRate", viewerManager.getTranscription());
    	if (value != null) {
    		rate = (int) (value.floatValue() * 100);
    	}
    	rateValueLabel = new JLabel(String.valueOf(rate));
    	rateValueLabel.setFont(rateValueLabel.getFont().deriveFont(Font.PLAIN, 10));
    	rateSlider = new JSlider(0, 200, rate);
    	rateSlider.addChangeListener(this);
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	Insets insets = new Insets(1, 1, 1, 1);
    	gbc.insets = insets;
    	gbc.anchor = GridBagConstraints.SOUTHWEST;
    	volRatePanel.add(volNameLabel, gbc);
    	gbc.gridx = 1;
    	gbc.insets = new Insets(1, 6, 1, 1);
    	volRatePanel.add(volValueLabel, gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.gridwidth = 2;
    	gbc.insets = insets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	volRatePanel.add(volSlider, gbc);
    	
    	gbc.gridy = 2;
    	gbc.gridwidth = 1;
    	gbc.fill = GridBagConstraints.NONE;
    	gbc.weightx = 0.0;
    	gbc.insets = new Insets(10, 1, 1, 1);
    	volRatePanel.add(rateNameLabel, gbc);
    	
    	gbc.gridx = 1;
    	gbc.insets = new Insets(1, 6, 1, 1);
    	volRatePanel.add(rateValueLabel, gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 3;
    	gbc.gridwidth = 2;
    	gbc.insets = insets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	volRatePanel.add(rateSlider, gbc);
    	
    	gbc.gridy = 4;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weighty = 1.0;
    	volRatePanel.add(new JPanel(), gbc);
    	volRatePanel.setBorder(new TitledBorder(""));
    	
    	return volRatePanel;
    }
    
    /**
     * Returns whether or not the media players currently are connected 
     * to the controllers.
     *
     * @return the connected state
     */
    public boolean connectedPlayers() {
        return connected;
    }

    /**
     * Update the offset for the current player in focus
     */
    private void setOffset() {
        if (playerInFocus != null) {
            if (viewForSyncPlayer.containsKey(playerInFocus)) {
            	viewForSyncPlayer.get(playerInFocus).setOffset(playerInFocus.getMediaTime());
            	viewForSyncPlayer.get(playerInFocus).setSyncConnected(false);
            }

            lastSyncPositionForPlayer.put(playerInFocus, playerInFocus.getMediaTime());
        	viewerManager.setOffset(playerInFocus, playerInFocus.getMediaTime());
    		if (playerInFocus instanceof SyncPlayer) { 			
    			//playerInFocus.setMediaTime(0);// or only in absolute mode
    			if (playerInFocus instanceof TSViewerPlayer) {
    				((TSViewerPlayer) playerInFocus).getViewer().setPlayer(viewerManager.getMasterMediaPlayer());
    			}
    			((SyncPlayer) playerInFocus).setSyncConnected(false);
    		}
        }
    }
    
    /**
     * only keep the relative offsets
     *
     */
    private void setRelativeOffsets() {	
    	// find smallest offset
    	long minOffset = Long.MAX_VALUE;
    	Iterator<ElanMediaPlayer> playIt = playerForName.values().iterator();
    	while(playIt.hasNext()) {
    		ElanMediaPlayer player = playIt.next();
    		if (player.getOffset() < minOffset) {
    			minOffset = player.getOffset();
    		}
    	}

    	// correct all offsets
    	playIt = playerForName.values().iterator();
    	while(playIt.hasNext()) {
    		ElanMediaPlayer player = playIt.next();
    		long offset = player.getOffset();
	   		viewerManager.setOffset(player, offset - minOffset);
	   		if (viewForSyncPlayer.containsKey(player)) {
	   			viewForSyncPlayer.get(player).setOffset(offset - minOffset);
	   		}
    	}
    	
    	// keep the image as it is
    	viewerManager.getMasterMediaPlayer().setMediaTime(minOffset);
    	
    	relative = true;
    }
    
   /**
    * Use absolute offsets
    *
    */
   private void setAbsoluteOffsets() {
	   	// correct all offsets
	   Iterator<ElanMediaPlayer> playIt = playerForName.values().iterator();
	   	while(playIt.hasNext()) {
	   		ElanMediaPlayer player = playIt.next();
	   		long offset = player.getOffset();
	   		viewerManager.setOffset(player, offset);
	   		if (viewForSyncPlayer.containsKey(player)) {
	   			viewForSyncPlayer.get(player).setOffset(offset);
	   		}
	   	}
	   	
	   	// keep the image as it is
	   	viewerManager.getMasterMediaPlayer().setMediaTime(0);
	   	
	   	relative = false;
   }
   
   private void resetOffsets() {
 	   long curTime = 0;
	   if (playerInFocus != null) {
		   curTime = playerInFocus.getMediaTime();
	   }
       allPlayersRB.setSelected(true);
	   reconnect();//sets offset of focused player
	   Iterator<ElanMediaPlayer> playIt = playerForName.values().iterator();
	   
	   while(playIt.hasNext()) {
		   ElanMediaPlayer player = playIt.next();
		   viewerManager.setOffset(player, 0);
	   }
	   
	   Iterator <SignalPlayerView> playIt2 = viewForSyncPlayer.values().iterator();
	   SignalPlayerView spv;
	   while (playIt2.hasNext()) {
		   spv = playIt2.next();
		   spv.setOffset(0);
	   }
	   // don't change the image of the current active player? Or set to 0?
	   viewerManager.getMasterMediaPlayer().setMediaTime(curTime);
	   doLayout();
   }
    
    /**
     * Connects all players.
     */
    public void reconnect() {
        setOffset();
        playerInFocus = null;
       // container.add(mediaPlayerController.getTimePanel());
        if (playerList.size() > 0) {
			viewerManager.setMasterMediaPlayer(playerList.get(0).player);
			viewerManager.enableDisabledMediaPlayers();
        }

        connected = true;
        allPlayersRB.removeItemListener(this);
        allPlayersRB.setSelected(true);
        allPlayersRB.addItemListener(this);
        // ts sync test
	   Iterator<ElanMediaPlayer> playIt = playerForName.values().iterator();
	   
	   while(playIt.hasNext()) {
		   ElanMediaPlayer player = playIt.next();
		   if (player instanceof TSViewerPlayer) {
			   ((TSViewerPlayer) player).getViewer().setPlayer(viewerManager.getMasterMediaPlayer());
		   }
	   }
    }
    
    private void stop() {
    	if (viewerManager.getMasterMediaPlayer().isPlaying()) {
    		viewerManager.getMasterMediaPlayer().stop();
    	}
    }
    
    /**
     * Sets the player that has the focus, i.e. the player of which the offset 
     * can be manipulated with the player control buttons.
     * @param elanPlayer the focused player
     */
    private void setFocusedPlayer(ElanMediaPlayer elanPlayer) {
    	if (elanPlayer == null) {
    		return;
    	}
    	setOffset();
		playerInFocus = elanPlayer;
		viewerManager.enableDisabledMediaPlayers();
		viewerManager.setMasterMediaPlayer(playerInFocus);
		viewerManager.disableSlaveMediaPlayers();

		// ts sync test
		Iterator<ElanMediaPlayer> playIt = playerForName.values().iterator();

		while(playIt.hasNext()) {
			ElanMediaPlayer player = playIt.next();
			if (player instanceof TSViewerPlayer && player != playerInFocus) {
				((TSViewerPlayer) player).getViewer().setPlayer(player);
			}
		}
		if (playerInFocus instanceof SyncPlayer) {
			((SyncPlayer) playerInFocus).setSyncConnected(true);
		}
		//long offset = playerInFocus.getOffset();
		long offset = lastSyncPositionForPlayer.get(playerInFocus);
		if (offset < 0) {
			offset = playerInFocus.getOffset();
		}
		long mediaTime = playerInFocus.getMediaTime();
		playerInFocus.setOffset(0);
		playerInFocus.setMediaTime(offset != 0 ? offset : mediaTime);
		//playerInFocus.setMediaTime((offset != 0  && mediaTime == 0) ? offset : mediaTime);
		if (viewForSyncPlayer.containsKey(playerInFocus)) {
			SignalPlayerView spv = viewForSyncPlayer.get(playerInFocus);
			spv.setOffset(0);
			spv.setMediaTime(offset != 0 ? offset : mediaTime);
			//spv.setMediaTime((offset != 0  && mediaTime == 0) ? offset : mediaTime);

			spv.setSyncConnected(true);
		}


		connected = false;
		doLayout();
    }

    /**
     * Sets the source for a MultiSourcePlayer, the offset for that player 
     * and ui elements of the player label panel.
     *  
     * @param sourceComponent the source selection ComboBox connected to the player
     */
    private void updateMultiPlayer(JComboBox sourceComponent) {
    	Iterator<ElanMediaPlayer> playIt = labelForPlayer.keySet().iterator();
    	Object playerObj = null;
    	JPanel panel = null;
    	
    	labelloop:
    	while (playIt.hasNext()) {
    		playerObj = playIt.next();
    		panel = labelForPlayer.get(playerObj);
    		Component[] comps = panel.getComponents();
    		for (int i = 0; i < comps.length; i++) {
    			if (comps[i] == sourceComponent) {
    				break labelloop;
    			}
    		}
    		panel = null;
    		playerObj = null;
    	}
    	
    	if (panel != null && playerObj instanceof MultiSourcePlayer) {
    		MultiSourcePlayer player = (MultiSourcePlayer) playerObj;
    		String source = (String) sourceComponent.getSelectedItem();
    		
    		if (source != null) {
    			// this sets the offset of the player
    			player.setCurrentSource(source);
    			layoutManager.doLayout();
    		}
    	}
    }
    
    /**
     * Calculates the optimal combination of number of columns and number of rows
     * for the specified width, height, labelheight and number of players. 
     * Optimal means that the players are displayed as large as possible in the given 
     * area.
     *  
     * @param availableWidth the width available for the players
     * @param availableHeight the height available for the players
     * @param labelHeight the height of the label that is displayed beneath each player
     * @param numPlayers the total number of players
     * @return an int array of length 2; the first int is the number of columns, 
     *   the second int is the number of rows
     */
    private int[] getNumberOfColumnsAndRows(int availableWidth, 
    	int availableHeight, int labelHeight) {
    	// calculate the average aspect ratio of the players with a visual component
    	float averageAspectRatio = 0.0f;
    	int visualCount = 0;
    	for (int i = 0; i < playerList.size(); i++) {
    		PlayerLayoutModel plModel = playerList.get(i);
    		if (plModel.isVisual() && plModel.isAttached()) {
    			averageAspectRatio += plModel.player.getAspectRatio();
    			visualCount++;
    		}
    	}
    	// add the wav player views
    	visualCount += viewForSyncPlayer.size();
    	averageAspectRatio += (2 * viewForSyncPlayer.size());
    	if (visualCount > 0) {
    		averageAspectRatio /= visualCount;
    	} else {
    		averageAspectRatio = 1.3f; //default
    	}
    	
    	if (visualCount == 0 || visualCount == 1) {
    		return new int[]{visualCount, visualCount};
    	}
    	if (availableWidth <= 0 || availableHeight <= 0 || 
    		availableHeight <= labelHeight) {
    		return new int[]{0, 0};		
    	}

    	int maxArea = 0;
    	int maxWidth = 0;
    	int maxHeight = 0;
    	int numColumns = 0;
    	int numRows = 0;
    	for (int i = 1; i <= visualCount; i++) {   		
    		for (int j = visualCount; j >= 1 && i * j >= visualCount; j--) {
				maxWidth = availableWidth / i;
				maxHeight = (availableHeight - j * labelHeight) / j;
				if (maxHeight > maxWidth * (1 / averageAspectRatio)) {
					maxHeight = (int)(maxWidth * (1 / averageAspectRatio)); 
				}
				if (maxWidth > maxHeight * averageAspectRatio) {
					maxWidth = (int)(maxHeight * averageAspectRatio); 
				}
				if (maxWidth <=0 || maxHeight <= 0) {
					continue;
				}
				int area = maxWidth * maxHeight;

				if (area > maxArea) {
					maxArea = area;
					numColumns = i;
					numRows = j;
				}
    		}
    	}
    	return new int[]{numColumns, numRows};
    }
    
    /**
	 * Tests the media descriptors for video files and extracted audio files
	 * with different offsets. When found with different offset while the video 
	 * is not the master media a warning message is generated.
	 */  
	private void checkUnequalOffsets() {
		if (playerList.size() < 2) {
			return;
		}
		StringBuilder mesBuf = null;

		MediaDescriptor amd, vmd;
		PlayerLayoutModel amodel, vmodel;
		for (int i = 0; i < playerList.size(); i++) {
			amodel = playerList.get(i);
			amd = amodel.player.getMediaDescriptor();
			if (amd == null) {
				continue;
			}
			if (amd.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
				
				for (int j = 0; j < playerList.size(); j++) {
					vmodel = playerList.get(j);
					vmd = vmodel.player.getMediaDescriptor();
					if (vmd == null) {
						continue;
					}
					if (MediaDescriptorUtil.isVideoType(vmd)) {
						
						if (vmd.mediaURL.equals(amd.extractedFrom) &&	
							j != 0 && vmd.timeOrigin != amd.timeOrigin) {
							// add to the message
							if (mesBuf == null) {
								mesBuf = new StringBuilder(ElanLocale.getString(
									"LinkedFilesDialog.Message.OffsetNotEqual") + "\n\n");
							}
							mesBuf.append("- " + vmd.mediaURL + "\n");
							mesBuf.append("- " + amd.mediaURL + "\n\n");

							break;
						}
					}
				}
			}
		}
		
		if (mesBuf != null) {
			JOptionPane.showMessageDialog(container, mesBuf.toString(),
				ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
		}
	}
    
    /**
     * Handles the action events
     *
     * @param event the action event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (event.getSource() == applyButton) {
        	if (!allPlayersRB.isSelected()) {
	        	// this will cause an item event on the "all players radio button" 
	        	allPlayersRB.setSelected(true);
        	} else {
        		if(relative) {
        			setRelativeOffsets();
            	} else {
            		setAbsoluteOffsets();
        		}
        		doLayout();//updates the offset labels
        	}      	
        } else if (event.getSource() == resetButton) {
        	resetOffsets();       	
        }
    }
    
	/**
	 * Handling of changes in radio buttons selection states.
	 * 
	 * @param e the item selection event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		// ignore deselect events
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() instanceof JComboBox) {
				updateMultiPlayer((JComboBox) e.getSource());
			} else if (e.getSource() == absOffsetRB) {
				stop();
				relative = false;
				if (!allPlayersRB.isSelected()) {
					allPlayersRB.setSelected(true);
				} else {
					//setAbsoluteOffsets();
				}				
			} else if (e.getSource() == relOffsetRB) {
				stop();
				relative = true;
				if (!allPlayersRB.isSelected()) {
					allPlayersRB.setEnabled(true);
				} else {
					//setRelativeOffsets();
				}
				
			} else if (e.getSource() == allPlayersRB) {
				stop();
				reconnect();
				allPlayerMode = true;
				if (relative) {
					setRelativeOffsets();
				} else {
					setAbsoluteOffsets();
				}
				doLayout();
			} else {
				// it is a player's radio button
				// check if we are switching from all players to single player mode
				boolean oldAllMode = allPlayerMode;
				if (playerButtons.containsValue(e.getSource())) {
					stop();
					Iterator<ElanMediaPlayer> playIt = playerButtons.keySet().iterator();
					ElanMediaPlayer player;
					while (playIt.hasNext()) {
						player = playIt.next();
						if (playerButtons.get(player) == e.getSource()) {
							setFocusedPlayer((ElanMediaPlayer)player);
							allPlayerMode = false;
							break;
						}
					}
				}

				doLayout();
			}
		}
	}
	
	/**
	 * Event handling for the sliders.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == volSlider) {
			int volume = volSlider.getValue();
			volValueLabel.setText(String.valueOf(volume));
			viewerManager.getVolumeManager().setMasterVolume((float) volume / 100);
		} else if (e.getSource() == rateSlider) {
			int rate = rateSlider.getValue();
			rateValueLabel.setText(String.valueOf(rate));
			viewerManager.getMasterMediaPlayer().setRate(((float) rate / 100));
		}
	}
    
    /**
     * Update the labels, border titles and button texts.
     * Called by the ElanLayoutManager.
     */
    @Override
	public void updateLocale() {
    	contentPanel.setBorder(new TitledBorder(""));
    	offsetPanel.setBorder(new TitledBorder(
			ElanLocale.getString("SyncMode.Label.Offset")));
		absOffsetRB.setText(ElanLocale.getString("SyncMode.Label.Absolute"));
		relOffsetRB.setText(ElanLocale.getString("SyncMode.Label.Relative"));
		applyButton.setText(ElanLocale.getString("SyncMode.Button.Apply"));
		resetButton.setText(ElanLocale.getString("SyncMode.Button.Reset"));

		playerPanel.setBorder(new TitledBorder(
			ElanLocale.getString("SyncMode.Label.Player")));
		allPlayersRB.setText(ElanLocale.getString("SyncMode.Label.AllPlayers"));
		volNameLabel.setText(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Volume"));
		rateNameLabel.setText(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Rate"));
		
		//update player radiobuttons and player labels
		Iterator<ElanMediaPlayer> playIt = nameForPlayer.keySet().iterator();
		ElanMediaPlayer player;
		JRadioButton b;
		JPanel       p;
		JLabel       l;
		while (playIt.hasNext()) {
			player = playIt.next();
			b = playerButtons.get(player);
			b.setText(ElanLocale.getString("SyncMode.Label.Player") + " " + 
				nameForPlayer.get(player));
			p = labelForPlayer.get(player);
			try {
				l = (JLabel)p.getComponent(0);
				String name = ElanLocale.getString("SyncMode.Label.Player") + " " + 
					nameForPlayer.get(player) + " " +
					ElanLocale.getString("SyncMode.Label.Offset") + ": ";
				l.setText(name);
			} catch (Exception e) {
				// just catch any exception, NullPointer, ArrayIndexOutOfBounds or
				// ClassCast
			}
		}
    }
    
    /**
     * Detaches the specified viewer or player.
     * The detaching of the player itself is handled in the main ElanLayoutManager.
     * 
     * @param object the viewer or player to remove from the main application frame
     */
    @Override
	public void detach(Object object) {
    	if (object instanceof PlayerLayoutModel) {
    		PlayerLayoutModel m = (PlayerLayoutModel) object;
    		
    		container.remove(getPlayerLabel(m.player));
    	} else if (object instanceof AbstractViewer) {
    		
    	}
    }
    
    /**
     * Attaches the specified viewer or player. 
     *
     * @param object the viewer or player to attach
     */
    @Override
	public void attach(Object object) {
    	if (object instanceof PlayerLayoutModel) {
    		PlayerLayoutModel m = (PlayerLayoutModel) object;
    		
    		container.add(getPlayerLabel(m.player));
    	}
    }
    
    @Override
	public void preferencesChanged() {
    	
    }

	/**
     * Show the layout for synchronizing two or more video and audio players.
     */
    @Override
	public void doLayout() {
    	if(!layoutManager.isIntialized()){
			return;
		}  
    	
        // get the width and height of the usable area
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();

		int labelHeight = 20;
		int numVisible = 0;
		if (playerList.size() > 0) {
			for (int i = 0; i < playerList.size(); i++) {		
				PlayerLayoutModel m = playerList.get(i);
				
				if (m.isAttached()) {
					numVisible++;
				}
				Dimension prefSize = getPlayerLabel(m.player).getPreferredSize();
	
				if (prefSize.height > labelHeight) {
					labelHeight = prefSize.height;	
				}
			}
		}
		numVisible += viewForSyncPlayer.size();
		labelHeight += ElanLayoutManager.CONTAINER_MARGIN;
		// initialize some fields
		int playerSelectionWidth = 130;
		int playerSelectionHeight = 80;
		Dimension playerSelectionSize = getPlayerSelectionPanel().getPreferredSize();
		playerSelectionWidth = Math.max(playerSelectionWidth, playerSelectionSize.width);
		playerSelectionHeight = Math.max(playerSelectionHeight, playerSelectionSize.height);
		int controlsHeight = 2 * COMP_MARGIN + mediaPlayerController.getSliderPanel()
			.getPreferredSize().height + playerSelectionHeight;
		int totalPlayerWidth = containerWidth - (2 * ElanLayoutManager.CONTAINER_MARGIN);
		int totalPlayerHeight = containerHeight - ElanLayoutManager.CONTAINER_MARGIN - controlsHeight;
		
		
		int[] colrow = getNumberOfColumnsAndRows(totalPlayerWidth, 
						totalPlayerHeight, labelHeight);
						
		// conditionally layout players	
		int mediaY = ElanLayoutManager.CONTAINER_MARGIN;
		int mediaX = ElanLayoutManager.CONTAINER_MARGIN;	
		if (colrow[0] > 0 && colrow[1] > 0) {
			int maxMediaWidth = totalPlayerWidth / colrow[0] - COMP_MARGIN;
			int maxMediaHeight = (totalPlayerHeight / colrow[1]) - labelHeight - COMP_MARGIN;

			if (maxMediaWidth > 0 && maxMediaHeight > 0) {
				int playerIndex = 0;				
				// layout per row
				for (int i = 0; i < colrow[1]; i++) {
					mediaY += i * (maxMediaHeight + labelHeight);
					for (int j = 0; j < colrow[0]; j++) {
						PlayerLayoutModel model = null;
						ElanMediaPlayer player = null;

						do {
							model = playerList.get(playerIndex);
							if (model.isAttached()) {
								player = model.player;
								break;
							}
							playerIndex++;
						} while (playerIndex < playerList.size());
						
						Component label = getPlayerLabel(player);
						int curX = mediaX + j * (maxMediaWidth + COMP_MARGIN);
						int curY = mediaY;
						int curW = maxMediaWidth;
						int curH = maxMediaHeight;
						if (label != null) {
							Dimension prefLabelSize = label.getPreferredSize();
							if (prefLabelSize.width <= maxMediaWidth) {
								label.setBounds(curX, curY + curH,
									curW, labelHeight);
							} else {
								label.setBounds(curX, curY + curH,
										maxMediaWidth, labelHeight);
							}
						}
						
						Component videoComp = null;
						float aspectRatio = 1.0f;
						if (model.isSyncOnly()) {
							videoComp = model.player.getVisualComponent();
							//aspectRatio = model.player.getAspectRatio();
						} else if (model.isVisual() && model.isAttached()) {
							videoComp = model.visualComponent;
							aspectRatio = model.player.getAspectRatio();
						} else if (viewForSyncPlayer.containsKey(model.player)) {
							videoComp = viewForSyncPlayer.get(model.player);
							//aspectRatio = 1.33f;
						}
						if (videoComp != null) {
							//Component videoComp = model.visualComponent;
							//float aspectRatio = model.player.getAspectRatio();
							curH = (int) ((float) curW / aspectRatio);
							if (curH > maxMediaHeight) {
								curH = maxMediaHeight;
								curW = (int) (curH * aspectRatio);
								if (model.isSyncOnly() || viewForSyncPlayer.containsKey(model.player)) {
									curW = maxMediaWidth;
									//curH = maxMediaHeight;
								}
								
								curX += (maxMediaWidth - curW) / 2;
							}

							videoComp.setBounds(curX, curY, curW, curH);
							// make sure the label is positioned close to the component
							if(label.getY() > curY + curH + ElanLayoutManager.CONTAINER_MARGIN) {
								label.setBounds(label.getX(), curY + curH + ElanLayoutManager.CONTAINER_MARGIN, 
										label.getWidth(), label.getHeight());
							}
						}
						playerIndex++;
						if (playerIndex >= playerList.size() || playerIndex >= numVisible) {
							break;
						}
					}
					if (playerIndex >= playerList.size() || playerIndex >= numVisible) {
						mediaY += maxMediaHeight + labelHeight + COMP_MARGIN;
						break;
					}
				}				
			}
		}
		
		int controlsY = mediaY;

		
        int sliderPanelX = ElanLayoutManager.CONTAINER_MARGIN + CONTROLS_MARGIN;
		int sliderPanelY = controlsY;
        int sliderPanelWidth = 0;
        int sliderPanelHeight = 0;

        if (mediaPlayerController != null) {
            sliderPanelWidth = containerWidth - (2 * ElanLayoutManager.CONTAINER_MARGIN) - (2 * CONTROLS_MARGIN);
            sliderPanelHeight = mediaPlayerController.getSliderPanel()
                                                     .getPreferredSize().height;
            mediaPlayerController.getSliderPanel().setBounds(sliderPanelX,
                sliderPanelY, sliderPanelWidth, sliderPanelHeight);
        }

        int timePanelX = 0;
        int timePanelY = sliderPanelY + sliderPanelHeight + COMP_MARGIN;
        int timePanelWidth = 0;
        int timePanelHeight = 0;
        
        int playButtonsX = 0;

        if (mediaPlayerController != null) {// HS removed check: connectedPlayers()
            timePanelX = (containerWidth / 2) -
                (mediaPlayerController.getTimePanel().getPreferredSize().width / 2);
            timePanelWidth = mediaPlayerController.getTimePanel()
                                                  .getPreferredSize().width;
            timePanelHeight = mediaPlayerController.getTimePanel()
                                                   .getPreferredSize().height;
            mediaPlayerController.getTimePanel().setBounds(timePanelX,
                timePanelY, timePanelWidth, timePanelHeight);
        
	        
	        int playButtonsY = timePanelY + timePanelHeight + 4;
	        int playButtonsWidth = 0;
	        int playButtonsHeight = 0;

            playButtonsX = (containerWidth / 2) -
                (mediaPlayerController.getPlayButtonsPanel().getPreferredSize().width / 2);
            playButtonsWidth = mediaPlayerController.getPlayButtonsPanel()
                                                    .getPreferredSize().width;
            playButtonsHeight = mediaPlayerController.getPlayButtonsPanel()
                                                     .getPreferredSize().height;
            // adjust the position to prevent overlap with the player selection panel
			if (playButtonsX < sliderPanelX + playerSelectionWidth + COMP_MARGIN) {
				playButtonsX = sliderPanelX + playerSelectionWidth + COMP_MARGIN;

				int adjTimePanelX = playButtonsX + ((playButtonsWidth - timePanelWidth) / 2);
				mediaPlayerController.getTimePanel().setBounds(adjTimePanelX, 
					timePanelY, timePanelWidth, timePanelHeight);
			}
            mediaPlayerController.getPlayButtonsPanel().setBounds(playButtonsX,
                playButtonsY, playButtonsWidth, playButtonsHeight);
            
            // add selection panels
            int selPanelY = playButtonsY + playButtonsHeight + 8;
            int selPanelWidth = mediaPlayerController.getSelectionPanel().getPreferredSize().width;
            int selPanelX = playButtonsX + (playButtonsWidth / 2) - (selPanelWidth / 2);
            int selPanelHeight = mediaPlayerController.getSelectionPanel().getPreferredSize().height;
            
            mediaPlayerController.getSelectionPanel().setBounds(selPanelX, selPanelY, selPanelWidth, 
            		selPanelHeight);
            
            int selButtonY = selPanelY + selPanelHeight + 4;
            int selButtonWidth = mediaPlayerController.getSelectionButtonsPanel().getPreferredSize().width;
            int selButtonX = playButtonsX + (playButtonsWidth / 2) - (selButtonWidth / 2);
            int selButtonHeight = mediaPlayerController.getSelectionButtonsPanel().getPreferredSize().height;
            
            mediaPlayerController.getSelectionButtonsPanel().setBounds(selButtonX, selButtonY, 
            		selButtonWidth, selButtonHeight);
        }
        
        //int playerSelectionX = Math.max(layoutManager.CONTAINER_MARGIN, playButtonsX - playerSelectionWidth - COMP_MARGIN);
        int	playerSelectionX = sliderPanelX;
        int playerSelectionY = timePanelY;

        getPlayerSelectionPanel().setBounds(playerSelectionX,
            playerSelectionY, 
            playButtonsX - COMP_MARGIN - ElanLayoutManager.CONTAINER_MARGIN - playerSelectionX,
            playerSelectionHeight);

        // volume, rate
        getVolumeRatePanel().setBounds(
        		playButtonsX + mediaPlayerController.getPlayButtonsPanel().getWidth() + COMP_MARGIN + ElanLayoutManager.CONTAINER_MARGIN, 
        		sliderPanelY + sliderPanelHeight + COMP_MARGIN, 
        		sliderPanelX + sliderPanelWidth - (playButtonsX + mediaPlayerController.getPlayButtonsPanel().getWidth() + COMP_MARGIN), 
        		playerSelectionHeight);

        container.validate();
        container.repaint();
    }

	@Override
	public void clearLayout() {
	   	// master media video and wav file of  the same name
		checkUnequalOffsets();
		
		for (int i=0; i < playerList.size(); i++) {
			PlayerLayoutModel plm = playerList.get(i);
			
            if (viewForSyncPlayer.containsKey(plm.player)) {
            	SignalPlayerView spv = viewForSyncPlayer.get(plm.player);
            	container.remove(spv);
            	viewForSyncPlayer.remove(plm.player);
            	Controller c = controllerForView.get(spv);
            	if (c != null) {
	            	c.removeControllerListener(spv);
	            	plm.player.removeController(c);
            	}
            	
            	viewerManager.disconnectListener(spv);
            	spv.setSelectionObject(null);
            	viewerManager.getSelection().removeSelectionListener(spv);
            	spv.setViewerManager(null);
            } else if (plm.isSyncOnly()) {
            	if (plm.player instanceof TSViewerPlayer) {
            		((TSViewerPlayer) plm.player).getViewer().setSyncModeViewer(false);
            		((TSViewerPlayer) plm.player).getViewer().setPlayer(viewerManager.getMasterMediaPlayer());
            	}
        		if (plm.player.getVisualComponent() != null) {
        			container.remove(plm.player.getVisualComponent());        			
        		}
            	Controller c = controllerForView.get(plm.player);
            	if (c != null) {
            		plm.player.removeController(c);
            	}
				viewerManager.destroyMediaPlayer(plm.player);
            	
			}
			
			container.remove(getPlayerLabel(plm.player)); 
		}
		
		container.remove(mediaPlayerController.getPlayButtonsPanel());
		container.remove(mediaPlayerController.getSliderPanel());
		viewerManager.destroyMediaPlayerControlSlider();
		container.remove(mediaPlayerController.getTimePanel());
		viewerManager.destroyTimePanel();
		container.remove(mediaPlayerController.getSelectionPanel());
		container.remove(mediaPlayerController.getSelectionButtonsPanel());
		viewerManager.destroyViewer(mediaPlayerController);
		mediaPlayerController = null;
		container.remove(getPlayerSelectionPanel());
		container.remove(getVolumeRatePanel());
		reconnect();
		// empty maps and lists?
		playerButtons.clear();
		playerForName.clear();
		nameForPlayer.clear();
		labelForPlayer.clear();
		playerButtons.clear();
		// disconnect separate viewers
		viewForSyncPlayer.clear();		
		
		container.repaint();
		
		if(layoutManager.getSignalViewer() != null){
    		viewerManager.connectViewer(layoutManager.getSignalViewer(), true);
    	}
	}

	@Override
	public void initComponents() {
		layoutManager.add(viewerManager.getMediaPlayerController());
		
		// add visual component of sync_only_players
		for (int i = 0; i < playerList.size(); i++) {
			PlayerLayoutModel plm = playerList.get(i);
			addInternal(plm);
		}
		// add sync player for timeseries viewer
		if (layoutManager.getTimeSeriesViewer() != null) {
			TimeSeriesViewer tsViewer = layoutManager.getTimeSeriesViewer();
			tsViewer.setTimeScaleConnected(false);
			tsViewer.setTrackManager(layoutManager.getTimeSeriesViewer().getTrackManager());
			
			TSViewerPlayer tsSyncPlayer = new TSViewerPlayer(tsViewer, 
        			viewerManager.getMasterMediaPlayer().getMediaDuration());
			tsSyncPlayer.setTrackManager(tsViewer.getTrackManager());
			tsSyncPlayer.setMediaTime(viewerManager.getMasterMediaPlayer().getMediaTime());
			
			//
			//viewerManager.disconnectListener(tsSyncPlayer.getViewer());
	    	////PeriodicUpdateController controller = new PeriodicUpdateController(25);
	    	///tsSyncPlayer.addController(controller);
	    	///controllerForView.put(tsSyncPlayer, controller);
	    	//viewerManager.connectListener(controller);
	    	//
			//tsSyncPlayer.setLayoutManager(layoutManager);
			viewerManager.addMediaPlayer(tsSyncPlayer);
			
        	PlayerLayoutModel plModel = new PlayerLayoutModel(tsSyncPlayer, layoutManager);
        	plModel.setSyncOnly(true);
        	playerList.add(plModel);
        	addInternal(plModel);
        	
        	if(layoutManager.getSignalViewer() != null){
        		viewerManager.connectViewer(layoutManager.getSignalViewer(), false);
        	}
		}
		
		updateLocale();		
		viewerManager.getActiveAnnotation().setAnnotation(null);
	}

	@Override
	public void enableOrDisableMenus(boolean enabled) {
		((ElanFrame2)layoutManager.getElanFrame()).enableOrDisableMenus(null, FrameConstants.ANNOTATION, enabled);
		List<String> actions = new ArrayList<String>(8);
		actions.add(ELANCommandFactory.NEXT_ACTIVE_TIER);
		actions.add(ELANCommandFactory.PREVIOUS_ACTIVE_TIER);
		actions.add(ELANCommandFactory.NEXT_ANNOTATION);
		actions.add(ELANCommandFactory.NEXT_ANNOTATION_EDIT);
		actions.add(ELANCommandFactory.PREVIOUS_ANNOTATION);
		actions.add(ELANCommandFactory.PREVIOUS_ANNOTATION_EDIT);
		actions.add(ELANCommandFactory.ANNOTATION_UP);
		actions.add(ELANCommandFactory.ANNOTATION_DOWN);
		actions.add(ELANCommandFactory.LINKED_FILES_DLG);
		layoutManager.enableOrDisableActions(actions, enabled);	
	}

	@Override
	public void cleanUpOnClose() {
	}

	@Override
	public void shortcutsChanged() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void createAndAddViewer(String viewerName) {
	
	}

	@Override
	public boolean destroyAndRemoveViewer(String viewerName) {
		return false;
	}

    /**
     * called before closing the synchronization mode
     */
	@Override
    public void isClosing() {
		Preferences.set("MediaControlRate", new Float((float)  rateSlider.getValue() / 100), 
				viewerManager.getTranscription());

		Preferences.set("MediaControlVolume", new Float((float)  volSlider.getValue() / 100), 
				viewerManager.getTranscription());
    }

	@Override
	public List<Zoomable> getZoomableViewers() {
		return null;
	}
}
