package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.util.FrameConstants;

public class CreateSignalViewerMA extends FrameMenuAction {	
    /**
     * Creates a new CreateSignalViewerMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public CreateSignalViewerMA(String name, ElanFrame2 frame) {
        super(name, frame);        
    }

    /**
     * Sets the preference setting when changed 
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	boolean value;
    	if( e.getSource() instanceof JCheckBoxMenuItem){    
    		value = ((JCheckBoxMenuItem)e.getSource()).isSelected();
    		frame.setMenuEnabled(FrameConstants.WAVE_FORM_VIEWER, value);
    		Preferences.set(commandId, value , null, false);
    		if(frame.isIntialized()){
    			frame.getLayoutManager().updateViewer(ELANCommandFactory.SIGNAL_VIEWER, value);
    			if(!value){
    				frame.getViewerManager().destroySignalViewer();
    			} else {
    				frame.getWaveFormViewerMenuManager().reinitializeWaveFormMenu();
    			}
    		}
    	}       
    }
    /** Adds or destroys the signal viewer from the
     *  Elan layout
     * 
     * @param value if true adds the signal view, if
     *        false, destroys the signal view
     */
    
 
}


