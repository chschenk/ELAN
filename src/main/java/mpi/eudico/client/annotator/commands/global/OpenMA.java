package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;


/**
 * Action that starts a Open Document sequence.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class OpenMA extends FrameMenuAction {
    /**
     * Creates a new OpenMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public OpenMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }
    
    /**
     * Checks if the file is a media file
     * 
     * @param file, file to be checked
     * @return true, if file is a media file else false
     */
    private boolean isMediaFile(String file){
   	 for (String element : FileExtension.MISC_VIDEO_EXT) {
            if (file.endsWith(element)) {
              return true;
            }
        }
   	 
   	 for (String element : FileExtension.MPEG_EXT) {
            if (file.endsWith(element)) {
              return true;
            }
        }
   	 
   	 for (String element : FileExtension.WAV_EXT) {
            if (file.endsWith(element)) {
              return true;
            }
        }
   	 
   	 for (String element : FileExtension.MISC_AUDIO_EXT) {
            if (file.endsWith(element)) {
              return true;
            }
        }   	 
   	 return false;
   }

    /**
     * Shows an eaf file chooser and creates a new transcription.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
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
            
           	//check if file is a media File
            fullPath = fileTemp.getAbsolutePath();
           	lowerPath = fullPath.toLowerCase(); 
           	if(isMediaFile(lowerPath)){  
           		String strMessage = fullPath;
                strMessage += ElanLocale.getString("Menu.Dialog.Message4"); 
                String strWarning = ElanLocale.getString("Message.Warning");
                int i = JOptionPane.showOptionDialog(frame, strMessage, strWarning, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
                if(i == JOptionPane.YES_OPTION){      			
          				List<String> v= new ArrayList<String>();
          				v.add(fullPath);
          				(new NewMA(ELANCommandFactory.NEW_DOC, frame)).createNewFile(v);
          		} else {
          			continue;
          		}
               return;
          	}
          
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
    
    /**
     * Creates a new frame for the specified file path.
     * 
     * @param filePath the full path to an eaf or etf file
     */
    public void createFrameForPath(String filePath) {
    	FrameManager.getInstance().createFrame(filePath);
    }
}
