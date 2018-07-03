package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SelectionPanel;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;

/**
 * MediaPlayerControlPanel A collection of buttons, sliders, etc to controls
 * the media, e.g. playing, setting current time.
 */
public class TranscriptionModePlayerController implements ChangeListener, ActionListener {
    /** Holds value of property DOCUMENT ME! */
    final private static Dimension BUTTON_SIZE = new Dimension(30, 20);
    private long userTimeBetweenLoops = 500; //used when playing selection in loop mode, default 0.5 seconds
    
	// volume rate panel
    private JPanel volRatePanel;
    private JLabel volNameLabel;    
    private JLabel volValueLabel;
    private JLabel rateNameLabel;
    private JLabel rateValueLabel;
    private JSlider volSlider;
    private JSlider rateSlider;
    
    private ViewerManager2 vm;   
    private TranscriptionViewer viewer;    
    
    // play buttons panel    
    private Icon playIcon;
    private Icon pauseIcon;	
	private JButton butPlay;   
	private JPanel playButtonsPanel;
	
	// mode panel	
    private JCheckBox loopModeCB;    
    private JPanel modePanel;
    
    private JButton butPlaySelection;
    private JButton butClearSelection;
	private JPanel selectionButtonsPanel;    
    
    private boolean loopMode = false;    
    
    private LoopThread loopThread;
      
	private SelectionPanel selectionPanel;

    /**
     * Constructor
     *
     * @param theVM DOCUMENT ME!
     */
    public TranscriptionModePlayerController(ViewerManager2 theVM, TranscriptionViewer viewer) {
        vm = theVM;       
        this.viewer = viewer;
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
    
    /**
	 * Returns the volume and the rate sliders panel
	 * 
	 * @return
	 */
    public JPanel getVolumeRatePanel() {
    	if (volRatePanel != null) {
    		return volRatePanel;
    	}
    	volRatePanel = new JPanel(new GridBagLayout());
    	volNameLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Volume"));
    	int volume = (int)(vm.getVolumeManager().getMasterVolume() * 100);
    	Float value = Preferences.getFloat("MediaControlVolume", vm.getTranscription());
    	if (value != null) {
    		volume = (int) (value.floatValue() * 100);
    	}
    	volValueLabel = new JLabel(String.valueOf(volume));
    	volValueLabel.setFont(volValueLabel.getFont().deriveFont(Font.PLAIN, 10));
    	volSlider = new JSlider(0, 100, volume);
    	volSlider.addChangeListener(this);    	
    	
    	rateNameLabel = new JLabel(ElanLocale.getString("MediaPlayerControlPanel.ElanSlider.Rate"));
    	int rate = (int)(vm.getMasterMediaPlayer().getRate() * 100);
    	value = Preferences.getFloat("MediaControlRate", vm.getTranscription());
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
    	
    	gbc.gridy = 2;
    	gbc.gridwidth = 1;
    	gbc.fill = GridBagConstraints.NONE;    	
    	gbc.insets = new Insets(1, 1, 1, 1);
    	volRatePanel.add(rateNameLabel, gbc);
    	
    	gbc.gridx = 1;
    	gbc.gridy = 0;
    	gbc.insets = new Insets(1, 6, 1, 1);
    	gbc.weighty = 0.5;	    
    	volRatePanel.add(volValueLabel, gbc);
    	
    	gbc.gridy = 2;    	
    	volRatePanel.add(rateValueLabel, gbc);
    	
    	gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.gridwidth = 2;
    	gbc.insets = insets;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.weightx = 1.0;
    	volRatePanel.add(volSlider, gbc);  
    	
    	gbc.gridy = 3;
    	volRatePanel.add(rateSlider, gbc);
    	
    	volRatePanel.setBorder(new TitledBorder(""));    	
    	return volRatePanel;
    }
    
    /**
     * Returns the loop mode panel
     * 
     * @return
     */
    public JPanel getModePanel(){
    	if (modePanel != null) {
    		return modePanel;
    	}
    	modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    	loopModeCB = new JCheckBox(ElanLocale.getString(ELANCommandFactory.LOOP_MODE));
 	    loopModeCB.setFont(Constants.deriveSmallFont(loopModeCB.getFont()));
 	    loopModeCB.addActionListener(this);	 	   
 	    String ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
 	    		getKeyStrokeForAction(ELANCommandFactory.LOOP_MODE, ELANCommandFactory.TRANSCRIPTION_MODE));
 	    if(ksDescription != null && ksDescription.trim().length() > 0){
 	    	loopModeCB.setToolTipText(ElanLocale.getString(ELANCommandFactory.LOOP_MODE+"ToolTip") + " (" + ksDescription+")");
 	    } else {
 	    	loopModeCB.setToolTipText(ElanLocale.getString(ELANCommandFactory.LOOP_MODE+"ToolTip"));
 	    }
 	    
 	    modePanel.add(loopModeCB);
 	    
 	    return modePanel;
 	    
    }
    
    /**
     * called before closing the transcription mode
     */
    public void isClosing(){
    	stopLoop();
    	Preferences.set("MediaControlRate", new Float((float)  rateSlider.getValue() / 100), 
                		vm.getTranscription());
     
        Preferences.set("MediaControlVolume", new Float((float)  volSlider.getValue() / 100), 
               		vm.getTranscription());
    }
    
    @Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == volSlider) {
			int volume = volSlider.getValue();
			volValueLabel.setText(String.valueOf(volume));			
			vm.getVolumeManager().setMasterVolume((float) volume / 100);
		} 
		else if (e.getSource() == rateSlider) {
			int rate = rateSlider.getValue();
			rateValueLabel.setText(String.valueOf(rate));
			vm.getMasterMediaPlayer().setRate(((float) rate / 100));			
		}
		viewer.focusTable();
	}   
   
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getPlayButtonsPanel() { 
    	if (playButtonsPanel != null) {
    		return playButtonsPanel;
    	}    	
    	
    	playButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));    	
        playIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PlayButton.gif"));
        pauseIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PauseButton.gif"));       	    
 	    butPlay = new JButton(playIcon);
 	    butPlay.setPreferredSize(BUTTON_SIZE);
 	    butPlay.setEnabled(false);
 	    butPlay.addActionListener(this);
 	    
 	    String ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
	    		getKeyStrokeForAction(ELANCommandFactory.PLAY_PAUSE, ELANCommandFactory.TRANSCRIPTION_MODE));
	    if(ksDescription != null && ksDescription.trim().length() > 0){
	    	butPlay.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_PAUSE+"ToolTip") + " (" + ksDescription+")");
	    } else {
	    	butPlay.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_PAUSE+"ToolTip"));
	    }
 	    
 	    playButtonsPanel.add(butPlay);    	
    	
        return playButtonsPanel;
    }  
    
    /**
     * Sets the icon of the play button 
     *     
     * @param play
     */
    public void setPlayPauseButton(boolean play) {
        if (play) {
            butPlay.setIcon(playIcon);
        } else {
        	 butPlay.setIcon(pauseIcon);
        }
       viewer.focusTable();
    }
  
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JPanel getSelectionButtonsPanel() {
    	if(selectionButtonsPanel != null){
    		return selectionButtonsPanel;
    	}
    	
    	selectionButtonsPanel = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0));
    	butPlaySelection = new JButton(new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PlaySelectionButton.gif")));
        butPlaySelection.setPreferredSize(BUTTON_SIZE);
        butPlaySelection.addActionListener(this);
        String ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
	    		getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE));
	    if(ksDescription != null && ksDescription.trim().length() > 0){
	    	butPlaySelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_SELECTION+"ToolTip") + " (" + ksDescription+")");
	    } else {
	    	butPlaySelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_SELECTION+"ToolTip"));
	    }
        
        selectionButtonsPanel.add(butPlaySelection);

        butClearSelection = new JButton(new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/ClearSelectionButton.gif")));
        butClearSelection.setPreferredSize(BUTTON_SIZE);
        butClearSelection.addActionListener(this);       
        ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
	    		getKeyStrokeForAction(ELANCommandFactory.CLEAR_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE));
	    if(ksDescription != null && ksDescription.trim().length() > 0){
	    	butClearSelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.CLEAR_SELECTION+"ToolTip") + " (" + ksDescription+")");
	    } else {
	    	butClearSelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.CLEAR_SELECTION+"ToolTip"));
	    }
	    selectionButtonsPanel.add(butClearSelection);  
	    
        return selectionButtonsPanel;
    }    

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getLoopMode() {
        return loopMode;
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

        if ((loopThread != null) && loopThread.isAlive()) {
            loopThread.stopLoop();
        }
    }   
    
    private boolean playerIsPlaying(){
    	return vm.getMasterMediaPlayer().isPlaying();
    }
    
    @Override
	public void actionPerformed(ActionEvent e) {		
    	if(e.getSource()== butPlay){    	
			viewer.playMedia();
		} 
		else if(e.getSource() == loopModeCB){		
			changeLoopMode();
		}
		else if(e.getSource() == butClearSelection){
			viewer.clearSelection();
		}
		else if(e.getSource() == butPlaySelection){	
			viewer.playSelection();
		}
    	
    	viewer.focusTable();    	
	}
    
    /**
     * Enables/Disables the Play button
     * 
     * @param b
     */
    public void enableButtons(boolean b) {
		butPlay.setEnabled(b);	
		butPlaySelection.setEnabled(b);
		butClearSelection.setEnabled(b);
		loopModeCB.setEnabled(b);	
	} 
    
    /**
     * Changes the loop mode
     */
    private void changeLoopMode(){
    	loopMode = loopModeCB.isSelected();
    	viewer.focusTable();
    	Preferences.set("TranscriptionManager.LoopMode", loopModeCB.isSelected(), null);	
    }    
    
    /**
     * Switch on/off the loop mode
     */
    public void toggleLoopMode() {
		loopModeCB.setSelected(!loopModeCB.isSelected());		
	}
    
    public SelectionPanel getSelectionPanel() {
		if(selectionPanel != null){
			return selectionPanel;
		}
			
		selectionPanel = new SelectionPanel(vm);
		selectionPanel.setNameLabel(ElanLocale.getString("TranscriptionManager.TimeInterval"));
		
		 vm.getSelection().removeSelectionListener(selectionPanel);
		
		return selectionPanel;
	} 
    
    public void updateLocale(){
    	String ksDescription;    	
    	
    	ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
 	    		getKeyStrokeForAction(ELANCommandFactory.LOOP_MODE, ELANCommandFactory.TRANSCRIPTION_MODE));
 	    if(ksDescription != null && ksDescription.trim().length() > 0){
 	    	loopModeCB.setToolTipText(ElanLocale.getString(ELANCommandFactory.LOOP_MODE+"ToolTip") + " (" + ksDescription+")");
 	    } else {
 	    	loopModeCB.setToolTipText(ElanLocale.getString(ELANCommandFactory.LOOP_MODE+"ToolTip"));
 	    }
 	    
 	    ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
	    		getKeyStrokeForAction(ELANCommandFactory.PLAY_PAUSE, ELANCommandFactory.TRANSCRIPTION_MODE));
	    if(ksDescription != null && ksDescription.trim().length() > 0){
	    	butPlay.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_PAUSE+"ToolTip") + " (" + ksDescription+")");
	    } else {
	    	butPlay.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_PAUSE+"ToolTip"));
	    }
	    
	    ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
	    		getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE));
	    if(ksDescription != null && ksDescription.trim().length() > 0){
	    	butPlaySelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_SELECTION+"ToolTip") + " (" + ksDescription+")");
	    } else {
	    	butPlaySelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.PLAY_SELECTION+"ToolTip"));
	    }
	    
	    ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ShortcutsUtil.getInstance().
	    		getKeyStrokeForAction(ELANCommandFactory.CLEAR_SELECTION, ELANCommandFactory.TRANSCRIPTION_MODE));
	    if(ksDescription != null && ksDescription.trim().length() > 0){
	    	butClearSelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.CLEAR_SELECTION+"ToolTip") + " (" + ksDescription+")");
	    } else {
	    	butClearSelection.setToolTipText(ElanLocale.getString(ELANCommandFactory.CLEAR_SELECTION+"ToolTip"));
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
            setName("Loop Thread");
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
            while (!stopLoop && loopMode) {
                if (!playerIsPlaying()) {
                    vm.getMasterMediaPlayer().playInterval(beginTime, endTime);
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
}

