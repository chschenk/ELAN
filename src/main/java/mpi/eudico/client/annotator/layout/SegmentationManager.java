package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.VolumeIconPanel;
import mpi.eudico.client.annotator.Zoomable;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ResizeComponent;
import mpi.eudico.client.annotator.gui.SegmentationPanel;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.viewer.SegmentationControlPanel;
import mpi.eudico.client.annotator.viewer.SegmentationViewer2;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SignalViewerControlPanel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class SegmentationManager implements ModeLayoutManager {
    private ViewerManager2 viewerManager;
    private ElanLayoutManager layoutManager;
    private Container container;   
    private ElanMediaPlayerController mediaPlayerController;

    private SignalViewerControlPanel signalControlPanel;
    private SignalViewer signalViewer;
    private JComponent signalComponent;
    private SegmentationViewer2 segmentationViewer;
    private JComponent segmentationComponent;
    private SegmentationPanel segmentationPanel;
    private JScrollPane segmentationPanelScroll;
    private SegmentationControlPanel segmentationControlPanel;
    private JSplitPane timeLineSplitPane;
    private JTabbedPane tabPane;
    private JTabbedPane leftTabPane;
    private JPanel controlPanel;
    private ResizeComponent vertMediaResizer;
    private VolumeIconPanel volumePanel;
    // 
    private boolean mediaInCentre = false;
    private boolean oneRowForVisuals = false;
    private boolean preferenceChanged = false;
    private int minTabWidth = 150;       
    private int numOfPlayers;
    private int tabIndex = 0;
    
    /**
     * 
     * @param viewerManager
     * @param elanLayoutManager
     */
	public SegmentationManager(ViewerManager2 viewerManager,
			ElanLayoutManager elanLayoutManager) {
        this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;
        // the media players are there, media player buttons, the V/R controls
        // signalviewer, segmentation viewer, segmentation configuration panel,
        // walker mode playback controls 
        
		controlPanel = new JPanel();
		controlPanel.setName(ElanLocale.getString("Tab.Controls"));
		controlPanel.setLayout(new GridBagLayout());
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        container = layoutManager.getContainer();        
	}

	@Override
	public void add(Object object) {
		if (object instanceof ElanMediaPlayerController) {
			setMediaPlayerController((ElanMediaPlayerController) object);
		} else if (object instanceof SignalViewer) {
			setSignalViewer((SignalViewer) object);
		} else if (object instanceof SegmentationViewer2) {
			setSegmentationViewer((SegmentationViewer2) object);
		} /*else if (object instanceof SegmentationPanel) {
			addToTabPane(ElanLocale.getString("SegmentationDialog.Title"),
					(SegmentationPanel) object);
		}*/
	}

	/**
	 * All objects are removed in the clearLayout() method when leaving the segmentation mode. 
	 * Separate removal is only supported for the signalviewer.
	 */
	@Override
	public void remove(Object object) {
		if (object instanceof SignalViewer) {
			if (object == signalViewer) {
				signalComponent.remove(signalViewer);
				signalComponent.remove(signalControlPanel);
				timeLineSplitPane.remove(signalComponent);
				signalViewer = null;
				signalControlPanel = null;
				if (segmentationViewer != null) {
					segmentationViewer.setTimeScaleConnected(false);
				}
			}
		}
	}

	/**
     * DOCUMENT ME!
     *
     * @param mediaPlayerController
     */
    private void setMediaPlayerController(ElanMediaPlayerController mediaPlayerController) {    	
   
        this.mediaPlayerController = mediaPlayerController;
        
        mediaPlayerController.getSliderPanel().addMouseListener(
        		mediaPlayerController.getAnnotationDensityViewer());
        
     // add the control components to the container
        container.add(mediaPlayerController.getPlayButtonsPanel());
        container.add(mediaPlayerController.getTimePanel());

        //container.add(mediaPlayerController.getModePanel());//??
        container.add(mediaPlayerController.getSelectionPanel());
        container.add(mediaPlayerController.getSelectionButtonsPanel());
        container.add(mediaPlayerController.getSliderPanel());
        container.add(mediaPlayerController.getAnnotationDensityViewer());//??
        mediaPlayerController.getStepAndRepeatPanel().showEnableCheckBox(false);
        container.add(mediaPlayerController.getStepAndRepeatPanel());
        volumePanel = mediaPlayerController.getVolumeIconPanel();
        container.add(volumePanel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(mediaPlayerController.getVolumePanel(), gbc);
        controlPanel.add(mediaPlayerController.getPlayersVolumePanel(), gbc);
        JPanel filler = new JPanel();
        gbc.weighty = 3;
        controlPanel.add(filler, gbc);      
        gbc.weighty = 1;
        controlPanel.add(mediaPlayerController.getRatePanel(), gbc);          
      
        addToTabPane(ElanLocale.getString("Tab.Controls"), new JScrollPane(controlPanel));
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param mediaPlayerController
     */
    private void removetMediaPlayerController() {    	
        mediaPlayerController.getSliderPanel().removeMouseListener(
        		mediaPlayerController.getAnnotationDensityViewer());        
        
        container.remove(mediaPlayerController.getPlayButtonsPanel());
        container.remove(mediaPlayerController.getTimePanel());
        viewerManager.destroyTimePanel();
        //container.add(mediaPlayerController.getModePanel());//??
        container.remove(mediaPlayerController.getSelectionPanel());
        container.remove(mediaPlayerController.getSelectionButtonsPanel());
        container.remove(mediaPlayerController.getSliderPanel());
        viewerManager.destroyMediaPlayerControlSlider();
        container.remove(mediaPlayerController.getAnnotationDensityViewer());//??    
        viewerManager.destroyAnnotationDensityViewer();
        
        container.remove(volumePanel);
        container.remove(mediaPlayerController.getStepAndRepeatPanel());
        viewerManager.destroyElanMediaPlayerController();
		mediaPlayerController = null;
       
    }
    
    private void addToTabPane(String tabName, Component component) {    	
    	
    	getTabPane().insertTab(tabName, null, component, tabName, tabIndex++);   	
    	
    	//doLayout();
    }
    
    private void setSegmentationViewer(SegmentationViewer2 segmentationViewer) {
    	this.segmentationViewer = segmentationViewer;
    	
    	if (segmentationComponent == null) {
    		segmentationComponent = new JPanel();
    		segmentationComponent.setLayout(null);
    		//container.add(segmentationComponent);
    	}
    	segmentationComponent.add(segmentationViewer); 	
    	
        if (segmentationControlPanel == null) {
        	segmentationControlPanel = new SegmentationControlPanel((TranscriptionImpl)viewerManager.getTranscription());
            segmentationControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
            ResizeComponent mcpResize = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL, ResizeComponent.CONTROL_PANEL);
            mcpResize.setSize(8, 16);
            segmentationControlPanel.setResizeComponent(mcpResize);
            segmentationControlPanel.setTierOrderObject(viewerManager.getTierOrder());
            segmentationComponent.add(segmentationControlPanel);
            segmentationViewer.setSegmentationControlPanel(segmentationControlPanel);
            segmentationControlPanel.setViewer(segmentationViewer);
        }
    	// get multitier panel
    	// add to splitpane
    	getTimeLineSplitPane().setBottomComponent(segmentationComponent);
    	
    	if (signalViewer != null) {
			Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				timeLineSplitPane.setDividerLocation(sigHeight.intValue());
			} else {
				timeLineSplitPane.setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
			}
			segmentationViewer.setTimeScaleConnected(true);
    	}
    }
    
    /**
     * Adds a signalviewer.
     *
     * @param signalViewer
     */
    private void setSignalViewer(SignalViewer signalViewer) {
        this.signalViewer = signalViewer;
        
        if(signalControlPanel == null){
        	signalControlPanel = viewerManager.getSignalViewerControlPanel();
        	//signalControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
        }
        
        if (signalComponent == null) {
            signalComponent = new JPanel();
            signalComponent.setLayout(null);
			//signalComponent.addComponentListener(layoutManager.new SignalSplitPaneListener("LayoutManager.SplitPaneDividerLocation"));
        }
        signalComponent.add(signalViewer);
        signalComponent.add(signalControlPanel);
        getTimeLineSplitPane().setTopComponent(signalComponent);
        
        if (segmentationViewer != null) {
			Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				timeLineSplitPane.setDividerLocation(sigHeight.intValue());
			} else {
				timeLineSplitPane.setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
			}
			segmentationViewer.setTimeScaleConnected(true);
        }
        if (layoutManager.isIntialized()) {
        	doLayout();
        }
    }
    
    private JSplitPane getTimeLineSplitPane() {
        if (timeLineSplitPane == null) {
            timeLineSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            timeLineSplitPane.setOneTouchExpandable(true);

            // HS 24 nov set the divider location when a top component is added
            timeLineSplitPane.setDividerLocation(0);
            timeLineSplitPane.setContinuousLayout(true);
            container.add(timeLineSplitPane);
        }

        return timeLineSplitPane;
    }
    
	@Override
	public void doLayout() {
		if(!layoutManager.isIntialized()){
			return;
		}
		
		// as in normal mode place video's, media controls, tab pane or a single panel with options
		// a resize slider, the waveform (if there) and the segmentation viewer
		 // get the width and height of the usable area
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();
        int containerMargin = 3;
        int componentMargin = 5;

        PlayerLayoutModel[] visualPlayers = layoutManager.getAttachedVisualPlayers();
		int numVisualPlayers = visualPlayers.length;	
		
		// first layout the player components, next the tabpane
		int mediaAreaHeight = layoutManager.getMediaAreaHeight();
		int visibleMediaX = containerMargin;
		int visibleMediaY = containerMargin;
		int visibleMediaWidth = 0;
		int visibleMediaHeight = mediaAreaHeight;

		int firstMediaWidth = visibleMediaWidth;
		int firstMediaHeight = mediaAreaHeight;
					
		if (oneRowForVisuals) {
			if (numVisualPlayers >= 1) {
				int maxPerMedia = (containerWidth - minTabWidth) / numVisualPlayers;
				int maxUsedHeight = 0;
				float aspectRatio;
				Component visComp;
				for (int i = 0; i < numVisualPlayers && i < 4; i++) {
					visComp = visualPlayers[i].visualComponent;
					aspectRatio = visualPlayers[i].player.getAspectRatio();
					int curWidth = 0, curHeight = 0;
					if (mediaAreaHeight * aspectRatio > maxPerMedia) {
						curWidth = maxPerMedia;
						curHeight = (int) (maxPerMedia / aspectRatio);
						maxUsedHeight = curHeight > maxUsedHeight ? curHeight : maxUsedHeight;
					} else {
						curWidth = (int) (mediaAreaHeight * aspectRatio);
						curHeight = mediaAreaHeight;
					}					
					if (i == 0) {		
						visibleMediaWidth = visibleMediaX + curWidth + componentMargin;
						 visComp.setBounds(visibleMediaX, visibleMediaY, curWidth, curHeight);
						 firstMediaWidth = curWidth;// used by the time panel
					} else {
						visComp.setBounds(visibleMediaX + visibleMediaWidth, visibleMediaY, curWidth, curHeight);
						visibleMediaWidth = visibleMediaWidth + curWidth + componentMargin;
					}
				}
				// recalculate X coordinates now that the total width of the videos is known
				if (mediaInCentre) {
					visibleMediaX = (containerWidth - visibleMediaWidth) / 2;
					int shiftX = 0;
					for (int i = 0; i < numVisualPlayers && i < 4; i++) {
						if (i == 0) {
							shiftX = visibleMediaX - visualPlayers[i].visualComponent.getX();
						}
						Point p = visualPlayers[i].visualComponent.getLocation();
						visualPlayers[i].visualComponent.setLocation(p.x + shiftX, p.y);
					}
				}
			}
		} else {
			//if (numVisualPlayers == 0) {
			//	visibleMediaHeight = mediaAreaHeight;
			//}
			int maxWidthForMedia = containerWidth - minTabWidth;
			if (numVisualPlayers >= 1) {
				// layout the first video
				Component firstVisualComp = visualPlayers[0].visualComponent;
				float aspectRatio = visualPlayers[0].player.getAspectRatio();
				firstMediaWidth = ElanLayoutManager.MASTER_MEDIA_WIDTH;
				// jan 2007 if the source- or encoded-width of the video is more than twice the MASTER_
				// MEDIA_WIDTH constant, then divide the real source width by 2 for optimal rendering
				if (visualPlayers[0].player.getSourceWidth() > 2 * ElanLayoutManager.MASTER_MEDIA_WIDTH && 
				        mediaAreaHeight == ElanLayoutManager.MASTER_MEDIA_HEIGHT) {
				    firstMediaWidth = visualPlayers[0].player.getSourceWidth() / 2;
				    //System.out.println("adj. width: " + firstMediaWidth);
				} else {
				    firstMediaWidth = (int) (firstMediaHeight * aspectRatio);
				}
				// force inside media area
				firstMediaWidth = firstMediaWidth > maxWidthForMedia ? maxWidthForMedia : firstMediaWidth;
			    firstMediaHeight = (int) (firstMediaWidth / aspectRatio);
			    // revert if the height > media area height
			    if (firstMediaHeight > mediaAreaHeight) {
			    	firstMediaHeight = mediaAreaHeight;
			    	firstMediaWidth = (int) (firstMediaHeight * aspectRatio);
			    }
				
				visibleMediaWidth = firstMediaWidth + componentMargin;	
				visibleMediaHeight = firstMediaHeight;
				if(numVisualPlayers == 1){
					if(mediaInCentre){	
						visibleMediaX = (containerWidth - visibleMediaWidth)/2;
					}
					firstVisualComp.setBounds(containerMargin+visibleMediaX, visibleMediaY, firstMediaWidth,
							firstMediaHeight);				
				}
				//System.out.println("width: " + firstMediaWidth + " height: " + firstMediaHeight);
			}
			if (numVisualPlayers == 2) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
							
				int secondMediaWidth = (int) (visibleMediaHeight * secondAR);
				int secondMediaHeight = visibleMediaHeight;
				// try to use exactly half of the width in some cases
				if (visualPlayers[1].player.getSourceWidth() > 2 * ElanLayoutManager.MASTER_MEDIA_WIDTH && 
						visualPlayers[1].player.getSourceWidth() > visualPlayers[0].player.getSourceWidth()) {
					secondMediaWidth = visualPlayers[1].player.getSourceWidth() / 2;
					secondMediaHeight = (int) (secondMediaWidth / secondAR);
					// revert if the height > mediaAreaHeight
					if (secondMediaHeight > mediaAreaHeight) {
						secondMediaHeight = mediaAreaHeight;
						secondMediaWidth = (int) (secondMediaHeight * secondAR);
					}
				}
				// force the two video's inside the available area, try to maintain same height, possibly different widths
				if (firstMediaWidth + secondMediaWidth + componentMargin > maxWidthForMedia) {
					// opt. 1: calculate the ratio to fit the two video's in the available width
					float sizeRatio = (maxWidthForMedia - componentMargin) / (float)(firstMediaWidth + secondMediaWidth);
					firstMediaWidth = (int) (sizeRatio * firstMediaWidth);
					firstMediaHeight = (int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio());
					
					visibleMediaWidth = firstMediaWidth + componentMargin;					
					secondMediaWidth = (int) (sizeRatio * secondMediaWidth);
					secondMediaHeight = (int) (secondMediaWidth / secondAR);
					// due to rounding effects the two heights might not be equal
					secondMediaHeight = firstMediaHeight;					
					// opt. 2: equal width for both videos, possibly different heights, doesn't make much sense
					/*
					firstMediaWidth = (maxWidthForMedia - componentMargin) / 2;
					visibleMediaWidth = firstMediaWidth + componentMargin;
					firstMediaHeight = (int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio());
					if (firstMediaHeight > visibleMediaHeight) {
						firstMediaHeight = visibleMediaHeight;
						firstMediaWidth = (int) (firstMediaHeight * visualPlayers[0].player.getAspectRatio());
					}
					
					secondMediaWidth = firstMediaWidth;
					secondMediaHeight = (int) (secondMediaWidth / secondAR);
					if (secondMediaHeight > visibleMediaHeight) {
						secondMediaHeight = visibleMediaHeight;
						secondMediaWidth = (int) (secondMediaHeight * secondAR);
					}
					*/
				}
				
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth + secondMediaWidth))/2;
				}				
				visualPlayers[0].visualComponent.setBounds(containerMargin+visibleMediaX, visibleMediaY, firstMediaWidth,
						firstMediaHeight);
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth,
					visibleMediaY, secondMediaWidth, secondMediaHeight);
				visibleMediaWidth += (secondMediaWidth + componentMargin);
				//System.out.println("sec width: " + secondMediaWidth + " sec height: " + secondMediaHeight);
			}
			else if (numVisualPlayers == 3) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
				Component thirdVisualComp = visualPlayers[2].visualComponent;
				float thirdAR = visualPlayers[2].player.getAspectRatio();
				int heightPerPlayer = (visibleMediaHeight - componentMargin) / 2;
				int secondWidth = (int)(secondAR * heightPerPlayer);
				int thirdWidth = (int) (thirdAR * heightPerPlayer);
				int widthPerPlayer = Math.max(secondWidth, thirdWidth);
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth+widthPerPlayer))/2;
				}
				visualPlayers[0].visualComponent.setBounds(visibleMediaX, visibleMediaY, firstMediaWidth,
							(int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio()));						
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - secondWidth) / 2, visibleMediaY, 
					secondWidth, heightPerPlayer);
				thirdVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - thirdWidth) / 2, 
					visibleMediaY + heightPerPlayer + componentMargin, 
					thirdWidth, heightPerPlayer);
				visibleMediaWidth += widthPerPlayer + componentMargin;
			}
			else if (numVisualPlayers >= 4) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
				Component thirdVisualComp = visualPlayers[2].visualComponent;
				float thirdAR = visualPlayers[2].player.getAspectRatio();
				Component fourthVisualComp = visualPlayers[3].visualComponent;
				float fourthAR = visualPlayers[3].player.getAspectRatio();
				int heightPerPlayer = (visibleMediaHeight - 2 * componentMargin) / 3;
				int secondWidth = (int)(secondAR * heightPerPlayer);
				int thirdWidth = (int) (thirdAR * heightPerPlayer);
				int fourthWidth = (int) (fourthAR * heightPerPlayer);
				int widthPerPlayer = Math.max(secondWidth, thirdWidth);
				widthPerPlayer = Math.max(widthPerPlayer, fourthWidth);
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth+widthPerPlayer))/2;
				}
				visualPlayers[0].visualComponent.setBounds(visibleMediaX, visibleMediaY, firstMediaWidth,
							(int) (firstMediaWidth / visualPlayers[0].player.getAspectRatio()));	
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - secondWidth) / 2, visibleMediaY, 
					secondWidth, heightPerPlayer);
				thirdVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - thirdWidth) / 2, 
					visibleMediaY + heightPerPlayer + componentMargin, 
					thirdWidth, heightPerPlayer);
				fourthVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - fourthWidth) / 2, 
					visibleMediaY + 2 * heightPerPlayer + 2 * componentMargin, 
					fourthWidth, heightPerPlayer);
				visibleMediaWidth += widthPerPlayer + componentMargin;
			}
	    }
        // layout the tab panel
		
		int tabPaneX = visibleMediaX + visibleMediaWidth;
        int tabPaneY = visibleMediaY;
        int tabPaneWidth = containerWidth - tabPaneX ;
        int tabPaneHeight = visibleMediaHeight;
        
        if(mediaInCentre){
        	if(numVisualPlayers > 0){
        		tabPaneWidth = visibleMediaX;
        	} else {
        		tabPaneWidth = tabPaneWidth/2;
        		tabPaneX = tabPaneWidth;
        	}    
        	tabPaneX = tabPaneX - containerMargin;
        
        	getLeftTabPane().setBounds(containerMargin, containerMargin, tabPaneWidth, tabPaneHeight);
        } else {
        	destroyLeftPane();
        }
        
        if (tabPane != null) {
            tabPane.setBounds(tabPaneX, tabPaneY, tabPaneWidth, tabPaneHeight);

           if (mediaPlayerController != null && controlPanel != null) {
               controlPanel.setSize(tabPaneWidth, tabPaneHeight);
           }
        }
        
        if(numOfPlayers != numVisualPlayers && numOfPlayers == 0){
        	preferenceChanged = true;
        }
        
        if(preferenceChanged || numVisualPlayers == 0){ 
        	preferenceChanged = false;
        }

        int timePanelX = 0;
        int timePanelY = visibleMediaY + visibleMediaHeight + 2;
        int timePanelWidth = 0;
        int timePanelHeight = 0;
        

        if (mediaPlayerController != null) {
            timePanelWidth = mediaPlayerController.getTimePanel()
                                                  .getPreferredSize().width;
            timePanelHeight = mediaPlayerController.getTimePanel()
                                                   .getPreferredSize().height;
			if (numVisualPlayers == 0) {
				timePanelX = containerMargin;
			} else {				
				if(mediaInCentre){		        	
		        	timePanelX = visibleMediaX;
		        } else{
		        	timePanelX = (containerMargin + (firstMediaWidth / 2)) -
		        			(timePanelWidth / 2);
		        }
			}
	        
            mediaPlayerController.getTimePanel().setBounds(timePanelX,
                timePanelY, timePanelWidth, timePanelHeight);
        }

        int playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
        int playButtonsY = timePanelY + timePanelHeight + 4;
        int playButtonsWidth = 0;
        int playButtonsHeight = 0;

        if (mediaPlayerController != null) {
            playButtonsWidth = mediaPlayerController.getPlayButtonsPanel()
                                                    .getPreferredSize().width;
            playButtonsHeight = mediaPlayerController.getPlayButtonsPanel()
                                                     .getPreferredSize().height;

			if (numVisualPlayers > 0) {
				if(mediaInCentre){	
					playButtonsX = (visibleMediaX );
				}else{
					playButtonsX = (containerMargin + (firstMediaWidth / 2)) -
						(playButtonsWidth / 2);				
					if (playButtonsX < ElanLayoutManager.CONTAINER_MARGIN) {
						playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
					}
				}
			}

            mediaPlayerController.getPlayButtonsPanel().setBounds(playButtonsX,
                playButtonsY, playButtonsWidth, playButtonsHeight);
        }

        int selectionPanelX = playButtonsX + playButtonsWidth + 20;
        int selectionPanelY = visibleMediaY + visibleMediaHeight + 2;
        int selectionPanelWidth = 0;
        int selectionPanelHeight = 0;

        if (mediaPlayerController != null) {
            selectionPanelWidth = 100 +
                mediaPlayerController.getSelectionPanel().getPreferredSize().width;
            selectionPanelHeight = mediaPlayerController.getSelectionPanel()
                                                        .getPreferredSize().height;
            mediaPlayerController.getSelectionPanel().setBounds(selectionPanelX,
                selectionPanelY, selectionPanelWidth, selectionPanelHeight);
        }

        int selectionButtonsX = selectionPanelX;
        int selectionButtonsY = selectionPanelY + selectionPanelHeight + 4;
        int selectionButtonsWidth = 0;
        int selectionButtonsHeight = 0;

        if (mediaPlayerController != null) {
            selectionButtonsWidth = mediaPlayerController.getSelectionButtonsPanel()
                                                         .getPreferredSize().width;
            selectionButtonsHeight = mediaPlayerController.getSelectionButtonsPanel()
                                                          .getPreferredSize().height;
            mediaPlayerController.getSelectionButtonsPanel().setBounds(selectionButtonsX,
                selectionButtonsY, selectionButtonsWidth, selectionButtonsHeight);
            
            int stepAndRepeatX = selectionButtonsX + selectionButtonsWidth + 20;
            int stepAndRepeatY = selectionButtonsY;
            int stepAndRepeatW = mediaPlayerController.getStepAndRepeatPanel().getPreferredSize().width;
            int stepAndRepeatH = mediaPlayerController.getStepAndRepeatPanel().getPreferredSize().height;
            
            mediaPlayerController.getStepAndRepeatPanel().setBounds(stepAndRepeatX, stepAndRepeatY, 
            		stepAndRepeatW, stepAndRepeatH);
            
            int volumePanelX = stepAndRepeatX + stepAndRepeatW + 8;
            int volumePanelY = selectionButtonsY;
            int volumePanelW = volumePanel.getPreferredSize().width;
            int volumePanelH = volumePanel.getPreferredSize().height;
            
            volumePanel.setBounds(volumePanelX, volumePanelY, volumePanelW, volumePanelH);
        }
        
        // resize divider
        int divX = 0 ; 
        int divY = playButtonsY + playButtonsHeight +4; 
        int divHeight = vertMediaResizer.getPreferredSize().height;
        vertMediaResizer.setBounds(divX, divY, containerWidth, divHeight);

        
        int sliderPanelX = ElanLayoutManager.CONTAINER_MARGIN;
        int sliderPanelY = divY + divHeight +4;
        int sliderPanelWidth = 0;
        int sliderPanelHeight = 0;

        if (mediaPlayerController != null) {
        	sliderPanelWidth = containerWidth - (2 * ElanLayoutManager.CONTAINER_MARGIN);
        	sliderPanelHeight = mediaPlayerController.getSliderPanel()
                                               .getPreferredSize().height;
        	mediaPlayerController.getSliderPanel().setBounds(sliderPanelX,
        			sliderPanelY, sliderPanelWidth, sliderPanelHeight);
        }

        int densityPanelX = ElanLayoutManager.CONTAINER_MARGIN;
        int densityPanelY = sliderPanelY + componentMargin - ElanLayoutManager.BELOW_BUTTONS_MARGIN; //sliderPanelHeight;
        int densityPanelWidth = sliderPanelWidth;
        int densityPanelHeight = 0;

        if (mediaPlayerController != null) {
            densityPanelHeight = mediaPlayerController.getAnnotationDensityViewer()
                                                      .getPreferredSize().height;
            mediaPlayerController.getAnnotationDensityViewer().setBounds(densityPanelX,
                densityPanelY, densityPanelWidth, densityPanelHeight);
        }
		
        // set signal viewer and segmentation viewer
        // layout time line split pane
        int splitPaneX = ElanLayoutManager.CONTAINER_MARGIN;
        int splitPaneXPaneY = densityPanelY + densityPanelHeight + 4;
        int splitPaneXPaneWidth = 0;
        int splitPaneXPaneHeight = 0;

        if (timeLineSplitPane != null) {
        	splitPaneXPaneWidth = containerWidth - (2 * ElanLayoutManager.CONTAINER_MARGIN);
        	splitPaneXPaneHeight = containerHeight - splitPaneXPaneY;
        	timeLineSplitPane.setBounds(splitPaneX, splitPaneXPaneY,
        			splitPaneXPaneWidth, splitPaneXPaneHeight);
        }
        
        // layout time line pane
        int multiTierControlX = 0;
        int multiTierControlY = 0;
        int multiTierControlWidth = 0;
        int multiTierControlHeight = 0;
        int timeLineX = 0;
        int timeLineY = 0;
        int timeLineWidth = 0;
        int timeLineHeight = 0;
        
        if (segmentationComponent != null) {
            int bottomHeight = timeLineSplitPane.getHeight() -
                timeLineSplitPane.getDividerLocation() -
                timeLineSplitPane.getDividerSize();
            Insets insets = timeLineSplitPane.getInsets();
            segmentationComponent.setSize(timeLineSplitPane.getWidth() - insets.left - insets.top, 
            		bottomHeight - insets.bottom);
            segmentationComponent.setPreferredSize(segmentationComponent.getSize());
            multiTierControlWidth = layoutManager.getMultiTierControlPanelWidth();
            multiTierControlHeight = bottomHeight;
            segmentationControlPanel.setSize(multiTierControlWidth, multiTierControlHeight);
            segmentationControlPanel.setBounds(multiTierControlX,
                multiTierControlY, multiTierControlWidth, multiTierControlHeight);
            
            timeLineX = multiTierControlWidth;

            //timeLineWidth = timeLineComponent.getWidth() - multiTierControlWidth;
            timeLineWidth = timeLineSplitPane.getWidth() -
                    multiTierControlWidth;
            timeLineHeight = bottomHeight; //timeLineComponent.getHeight();

            segmentationViewer.setBounds(timeLineX, timeLineY, timeLineWidth,
                timeLineHeight);
            segmentationViewer.setPreferredSize(
				new Dimension(timeLineWidth, timeLineHeight));
            // force a component event on the viewer, does not happen automatically apparently
            segmentationViewer.componentResized(null);
        }
        
        int signalX = multiTierControlWidth;
        int signalY = 0;
        int signalWidth = 0;
        int signalHeight = 0;
        
        if ((signalComponent != null) && (signalViewer != null)) {
            int rMargin = 0;

            if (segmentationViewer != null) {
                rMargin = segmentationViewer.getRightMargin();
            } 

			//		 signalWidth = signalComponent.getWidth() - multiTierControlWidth - rMargin;
			signalWidth = timeLineSplitPane.getWidth() - multiTierControlWidth -
				rMargin;
			signalHeight = signalComponent.getHeight();

			signalViewer.setBounds(signalX, signalY, signalWidth, signalHeight);			
            Insets insets = timeLineSplitPane.getInsets();
            signalComponent.setSize(timeLineSplitPane.getWidth() - insets.left - insets.top, 
            		signalHeight - insets.top);
            signalComponent.setPreferredSize(signalComponent.getSize());
            signalControlPanel.setBounds(0, 0, multiTierControlWidth, signalHeight);
        }
        
        container.validate();
	}

	@Override
	public void updateLocale() {
		 if (tabPane != null) {
	            int nTabs = tabPane.getTabCount();

	            for (int i = 0; i < nTabs; i++) {
	                Component component = tabPane.getComponentAt(i);

	                if (component == segmentationPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("SegmentationDialog.Title"));
	                } else if (component == controlPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Controls"));
	                }
	            }
		 }
		 if (segmentationPanel != null) {
			 segmentationPanel.updateLocale();
		 }
	}

	@Override
	public void clearLayout() {		
		segmentationViewer.setTimeScaleConnected(false);
		viewerManager.destroyViewer(segmentationViewer);
		segmentationViewer.setSegmentationControlPanel(null);//??
		segmentationComponent.remove(segmentationViewer);
		segmentationComponent.remove(segmentationControlPanel);
		
		if(timeLineSplitPane != null) {
			timeLineSplitPane.remove(segmentationComponent);
		}
		if (signalViewer != null) {
			signalComponent.remove(signalViewer);
			signalComponent.remove(signalControlPanel);
			signalControlPanel = null;
			timeLineSplitPane.remove(signalComponent);
		}
		if(timeLineSplitPane != null) {
			container.remove(timeLineSplitPane);
		}
		if (tabPane != null) {
			tabPane.removeAll();
			container.remove(tabPane);
		}
		if (leftTabPane != null) {
			leftTabPane.removeAll();
			container.remove(leftTabPane);
		}
		
		removetMediaPlayerController();

        container.remove(vertMediaResizer);
        container.repaint();
	}		
	
	@Override
	public void cleanUpOnClose() {
		
	}

	@Override
	public void initComponents() {		
    	vertMediaResizer = new ResizeComponent(layoutManager, SwingConstants.VERTICAL);
    	vertMediaResizer.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    	vertMediaResizer.setPreferredSize(new Dimension(container.getWidth(), 7));
   	
    	Component n = vertMediaResizer.getComponent(0);
        vertMediaResizer.remove(n);
        vertMediaResizer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0; 
        vertMediaResizer.add(n, gbc);
        
        container.add(vertMediaResizer);
        
		add(viewerManager.createSegmentationViewer());
		segmentationViewer.preferencesChanged();
		segmentationPanel = new SegmentationPanel(segmentationViewer);
		segmentationPanelScroll = new JScrollPane(segmentationPanel);
		// first add the segmentation panel
		addToTabPane(ElanLocale.getString("SegmentationDialog.Title"),
				segmentationPanelScroll);
		createAndAddViewer(ELANCommandFactory.SIGNAL_VIEWER);
		segmentationViewer.setTimeScaleConnected(true);
		
		add(viewerManager.getMediaPlayerController());
        viewerManager.getMediaPlayerController().preferencesChanged();
		
		viewerManager.getActiveAnnotation().setAnnotation(null);	
		// add the component listener at the end of initialization
		// always have the component in order to only attach a listener once?
        if (signalComponent != null) {
			signalComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
        } else {
        	signalComponent = new JPanel();
        	signalComponent.setLayout(null);
        	signalComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
        }
        preferencesChanged();
		container.repaint();
		doLayout();
		container.transferFocusUpCycle();
	}	

    private JTabbedPane getTabPane() {
        if (tabPane == null) {
            tabPane = new JTabbedPane();
            //tabPane.addChangeListener(this);
            container.add(tabPane);
        } 
        return tabPane;
    }
    
    private JTabbedPane getLeftTabPane() {
        if (leftTabPane == null) {
        	leftTabPane = new JTabbedPane();           	
            container.add(leftTabPane);
        }

        return leftTabPane;
    }
    
    /**
     * destroys the left tabpane
     */
    private void destroyLeftPane(){
    	if(leftTabPane != null){
    		container.remove(leftTabPane);
    		leftTabPane = null;
    	}    	
    }

	@Override
	public void attach(Object object) {
	}

	@Override
	public void detach(Object object) {
	}
	
	@Override
	public void preferencesChanged() {
		Integer sigHeight = Preferences.getInt("LayoutManager.SplitPaneDividerLocation", 
				viewerManager.getTranscription());
		if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
			if (signalViewer != null && timeLineSplitPane != null) {
				timeLineSplitPane.setDividerLocation(sigHeight.intValue());
			}
		}
		
		Boolean sameSize = Preferences.getBool("Media.VideosSameSize", null);
		
	    if (sameSize != null) {
	        oneRowForVisuals = sameSize.booleanValue();
	    }
	    
	    Boolean val = Preferences.getBool("Media.VideosCentre", null);
	    boolean oldInCentre = mediaInCentre;
	    if (val != null) {
	    	mediaInCentre = val.booleanValue();
	    	
	    	if (oldInCentre != mediaInCentre) {
	    		// if the new situation is in centre create left tab pane
	    		if (mediaInCentre) {
	    			getTabPane().remove(segmentationPanelScroll);
	    			getLeftTabPane().addTab(ElanLocale.getString("SegmentationDialog.Title"), 
	    					null, segmentationPanelScroll, ElanLocale.getString("SegmentationDialog.Title"));
	    		}
	    		// else destroy left tab pane
	    		else {
	    			getLeftTabPane().remove(segmentationPanelScroll);
	    			destroyLeftPane();
	    			getTabPane().insertTab(ElanLocale.getString("SegmentationDialog.Title"), null, segmentationPanelScroll, 
	    					ElanLocale.getString("SegmentationDialog.Title"), 0);
	    		}
	    	}
	    }
	    
	    Integer selTabIndex = Preferences.getInt("SegmentationManager.SelectedTabIndex", viewerManager.getTranscription());
	    if (selTabIndex != null) {
	    	int tab = selTabIndex.intValue();
	    	if (tab >= 0 && tab < getTabPane().getTabCount()) {
	    		getTabPane().setSelectedIndex(tab);
	    	} else {
	    		getTabPane().setSelectedIndex(0);
	    	}
	    }

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

		layoutManager.enableOrDisableActions(actions, enabled);
	}

	@Override
	public void shortcutsChanged() {
		segmentationViewer.shortcutsChanged();	
		segmentationPanel.updateSegmentkeyLabel();
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
		boolean doLayout = false;
		if(viewerName == null){
			return doLayout;
		}
		
		if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)){
			if(signalViewer != null) {		
				remove(signalViewer);
				doLayout = true;
			}
			viewerManager.destroySignalViewer();			
		}
		return doLayout;
	}

	/**
	 * This method only stores some preferences that
	 *  haven't been stored yet and also store any 
	 *  unsaved changes if needed. 
	 */
	@Override
	public void isClosing() {
		if (signalViewer != null && timeLineSplitPane != null) {
			int location = timeLineSplitPane.getDividerLocation();
			if (location != ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", Integer.valueOf(location), 
						viewerManager.getTranscription());
			}
		}
		// if there is a left tab pane, this is now always 0
		layoutManager.setPreference("SegmentationManager.SelectedTabIndex", 
				getTabPane().getSelectedIndex(), viewerManager.getTranscription());
	}

	/**
	 * @return list of zoomable viewers, first the segmentation viewer, then the signal viewer
	 */
	@Override
	public List<Zoomable> getZoomableViewers() {
		List<Zoomable> zoomList = new ArrayList<Zoomable>(2);

		if (segmentationViewer != null) {
			zoomList.add(segmentationViewer);
		}
		if (signalViewer != null) {
			zoomList.add(signalViewer);
		}
		
		return zoomList;
	}
	
}