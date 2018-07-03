package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.global.OpenMA;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.turnsandscenemode.TaSFrame;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * A (temporary?) action that extends the ELAN Open action by not
 * interacting with the FrameManager. This way there is no interference 
 * with the ELAN recent files list and other preferences. This has to be worked out...
 * 
 * The simplified transcription application is initially a single window / single document program.
 */
@SuppressWarnings("serial")
public class OpenTaSMA extends OpenMA {

	public OpenTaSMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	/**
	 * Performs a simplified test on the selected file and creates a new transcription
	 * for the current window.
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// first get the next file to open 
		// TODO copied from OpenMA, merge later
    	FileChooser chooser = new FileChooser(frame);
    	File fileTemp = null;
    	String fullPath;
       	String lowerPath;
    	while(true){    
    		chooser.createAndShowFileDialog(ElanLocale.getString("Frame.ElanFrame.OpenDialog.Title"), FileChooser.OPEN_DIALOG,
    				FileExtension.EAF_EXT, "LastUsedEAFDir");
    		
            
            fileTemp = chooser.getSelectedFile();
            
            if (fileTemp == null) {
        	   	return; 
            }
            fullPath = fileTemp.getAbsolutePath();
           	lowerPath = fullPath.toLowerCase(); 
            //check if file is a '.eaf' file
            if (!lowerPath.endsWith(".eaf") && !lowerPath.endsWith(".etf")) {
                String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
                strMessage += fullPath;
                strMessage += ElanLocale.getString("Menu.Dialog.Message3");
                String strError = ElanLocale.getString("Message.Error");
                JOptionPane.showMessageDialog(frame, strMessage, strError, JOptionPane.ERROR_MESSAGE);
                actionPerformed(e);
                return;
            }
            break;
    	}
		
    	createFrameForPath(fullPath);
	}

	@Override
	public void createFrameForPath(String filePath) {
		// check if there is something open that has to be saved
    	// clean up the window or create a new one?
		if (frame.getViewerManager() != null && frame.getViewerManager().getTranscription() != null) {
			frame.checkSaveAndClose();			
		}
			
		// set the new transcription
		if (frame instanceof TaSFrame) {
			frame.openEAF(filePath);
		}
	}

	
}
