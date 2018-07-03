package mpi.eudico.client.annotator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.gui.ExitStrategyPane;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SignalViewerControlPanel;
import mpi.eudico.client.util.SelectableObject;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

/**
 * WaveFormViewerMenuManager
 *
 * @author aarsom
 * @version 1.0
 * @version 2.0, April 2014 : Communicates with the SignalViewerControlPanel
 */
public class WaveFormViewerMenuManager {
    private ElanFrame2 frame;
    private Transcription transcription;

    // all wave forms that are present in the menu, stored as Selectable objects,
    // holding a MediaDescriptor and a selection flag
    private List<SelectableObject<MediaDescriptor>> menuWaveForm;
    
    //<mediarul, action>
    private HashMap<String, AbstractAction> actionMap;
    
    private SignalViewerControlPanel signalViewerControlPanel;
    
    private String currentMediaURL;
    
    private ExitStrategyPane masterMediaWarningPanel;
    /**
     * Creates a new WaveFormViewerMenuManager instance
     *
     * @param frame the frame containing the menu's, players and viewers
     * @param transcription the transcription loaded in the frame, containing
     *        the media descriptors
     */
    public WaveFormViewerMenuManager(ElanFrame2 frame, Transcription transcription) {
        super();
        this.frame = frame;
        this.transcription = transcription;
        menuWaveForm = new ArrayList<SelectableObject<MediaDescriptor>>();
        actionMap = new HashMap<String, AbstractAction>();
        
        masterMediaWarningPanel = new ExitStrategyPane();        
    }
    
    public List<SelectableObject<MediaDescriptor>> getWaveFormList(){
    	return menuWaveForm;
    }
    
    public void setSignalViewerControlPanel(SignalViewerControlPanel panel){
    	signalViewerControlPanel = panel;
    }
    
    public void loadPreferences(){
    	String mediaURL = Preferences.getString("WaveFormViewer.ActiveURL", transcription);
    	AbstractAction action = actionMap.get(mediaURL);
    	if(mediaURL != null && action != null){
    		frame.setMenuSelected(mediaURL, FrameConstants.WAVE_FORM_VIEWER);
    		playerActionPerformed(action, null, true);
    	}
    }

    /**
     * Adds an action for each video media descriptor to the View -> Media
     * Player menu and sets the selected and enabled state if possible.
     */
    public void initWaveFormViewerMenu() {       
        List<MediaDescriptor> descriptors = transcription.getMediaDescriptors();
        MediaDescriptor md;
        String fileName;
        int visibles = 0;
        
        actionMap.clear();

        for (int i = 0; i < descriptors.size(); i++) {
            md = descriptors.get(i);
            
            if( md.mimeType != null && (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) ||
            		md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE))){  
            	 boolean curValid = MediaDescriptorUtil.checkLinkStatus(md);

                 // if we get here create an action and menuitem
                 fileName = FileUtility.fileNameFromPath(md.mediaURL);

                 PlayerAction action = new PlayerAction(md.mediaURL, fileName);

                 if (!curValid) {
                     action.setEnabled(false);
                 }

                 frame.addActionToMenu(action, FrameConstants.WAVE_FORM_VIEWER, -1);
                 actionMap.put(md.mediaURL, action);

                 if (visibles == 0 ) {
                     frame.setMenuSelected(md.mediaURL, FrameConstants.WAVE_FORM_VIEWER);
                     menuWaveForm.add(new SelectableObject<MediaDescriptor>(md, true));
                     visibles++;
                 } else {
                 	menuWaveForm.add(new SelectableObject<MediaDescriptor>(md, false));
                 }
            }
        }
        
        loadPreferences();
        
        if(signalViewerControlPanel != null){
        	signalViewerControlPanel.initViewerPopUPMenu();
        }
    }

    /**
     * Called after a change in the linked media files.<br>
     * - removes all player menu items <br>
     * - adds new menu items for new players 
     */
    public void reinitializeWaveFormMenu() {
        //remove current menu items

        for (int i = 0; i < menuWaveForm.size(); i++) {
            SelectableObject<MediaDescriptor> sob = menuWaveForm.get(i);
            MediaDescriptor md = sob.getValue();            

            frame.removeActionFromMenu(md.mediaURL, FrameConstants.WAVE_FORM_VIEWER);
        }

        // new players have been created, populate the players menu again 
        menuWaveForm.clear();
        initWaveFormViewerMenu();
    }
    
    /**
     * Performs the player action for the given mediaURL and
     * updates the waveform viewer menu of the frame
     *
     * @param mediaURL the url for which the action shud be performed
     * @param e the event
     */
    public void performActionFor(String mediaURL, ActionEvent e){
    	frame.setMenuSelected(mediaURL, FrameConstants.WAVE_FORM_VIEWER);
    	playerActionPerformed(actionMap.get(mediaURL), e);
    }

    /**
     * Updates the wav file in the the signal viewer. Also
     * checks and updates the master media player if necessary
     *
     * @param action the action that received the event
     * @param e the event
     */
    void playerActionPerformed(AbstractAction action, ActionEvent e) {
    	Boolean boolPref = Preferences.getBool("WaveFormViewer.SupressMasterMediaWarning", null);
    	boolean supressWarning = false;
    	if (boolPref != null) {
    		supressWarning = boolPref.booleanValue();
    	}
    	
    	playerActionPerformed(action, e, supressWarning);
    	
    }
    
    /**
     * Updates the wav file in the the signal viewer. Also
     * checks and updates the master media player if necessary
     *
     * @param action the action that received the event
     * @param e the event
     * @param supressWarning
     */
    void playerActionPerformed(AbstractAction action, ActionEvent e, boolean supressWarning) {
        if (action != null) {
        	
        	String url = (String) action.getValue(Action.LONG_DESCRIPTION);
        	
        	//check if master media is a wave file
        	if(!supressWarning && isMasterMediaWavFile()){
        		// check if the current Url is the mastermedia
        		if(isMasterMediaPlayer(currentMediaURL) || !isMasterMediaPlayer(url)){
        			masterMediaWarningPanel.setMessage(
        					"<html>" +
        					ElanLocale.getString("WaveFormViewer.MasterMedia.Warn1") + "<br>" +
        			        ElanLocale.getString("WaveFormViewer.MasterMedia.Warn2") + "<br>"+
        					"<br>"+ "</html>");
            		int s = JOptionPane.showConfirmDialog(frame, 
            				masterMediaWarningPanel, 
            				"Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            		if(s == JOptionPane.NO_OPTION){
            			return;
            		}
            		
            		Preferences.set("WaveFormViewer.SupressMasterMediaWarning", 
            				masterMediaWarningPanel.getDontShowAgain(), null);
        		} 
        	}
        	
        	// update the wave form viewer list
            MediaDescriptor newMD = null;
            MediaDescriptor md = null;
            SelectableObject<MediaDescriptor> sob = null;

            for (int i = 0; i < menuWaveForm.size(); i++) {
                 sob = menuWaveForm.get(i);
                 md = sob.getValue();
                   
                 if(sob.isSelected() && !md.mediaURL.equals(url)){
              	   sob.setSelected(false);
                 }

                 if (md.mediaURL.equals(url) && newMD == null ) {
                     sob.setSelected(true);
                     newMD = md;
                 }
            }

            // item is selected
            if (newMD != null) {
            	if(signalViewerControlPanel != null){
                	signalViewerControlPanel.updateWaveFormPanel(newMD.mediaURL);
                }
            	
            	SignalViewer viewer = frame.getViewerManager().getSignalViewer();
            	if(viewer != null){
            		viewer.setMedia(newMD.mediaURL);
            		viewer.setOffset(newMD.timeOrigin);
                	frame.getViewerManager().updateSignalViewerMedia(newMD.mediaURL);     
                	
                	//store preference
                	Preferences.set("WaveFormViewer.ActiveURL", newMD.mediaURL, transcription);
            	}
            }
        }
    }

    /**
     * Checks whether the given mediaUrl is the mastermedia
     *
     * @param mediaURL the media descriptor
     */
    private boolean isMasterMediaPlayer(String mediaURL) {
        if (mediaURL != null) {
        	MediaDescriptor otherMd = frame.getViewerManager().getMasterMediaPlayer().getMediaDescriptor();

            if ((otherMd != null) && mediaURL.equals(otherMd.mediaURL)) {
            	return true;
            }
        }        
        return false;
    }
    
    /**
     * Checks if the master media is a wav file
     *
     */
    private boolean isMasterMediaWavFile() {
        MediaDescriptor otherMd = frame.getViewerManager().getMasterMediaPlayer().getMediaDescriptor();

        if ((otherMd != null) && otherMd.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
        	return true;
        }	
        return false;
    }

    /**
     * An action class for menu items in the media players menu
     *
     * @author Han Sloetjes
     */
    class PlayerAction extends AbstractAction {
        /**
         * Creates a new PlayerAction instance
         *
         * @param fileUrl the full url of the media file
         * @param fileName the file name of the media file
         */
        PlayerAction(String fileUrl, String fileName) {
            putValue(Action.NAME, fileName);
            // use LONG_DESCRIPTION or DEFAULT ?
            putValue(Action.LONG_DESCRIPTION, fileUrl);
        }

        /**
         * Handles selection and deselection of a player.  Delegates to this
         * manager.
         *
         * @param e action event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            WaveFormViewerMenuManager.this.playerActionPerformed(this, e);
        }
    }
}
