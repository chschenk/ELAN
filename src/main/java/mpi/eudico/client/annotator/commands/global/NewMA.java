package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.client.util.TranscriptionECVLoader;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * Action that starts a New Document sequence.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class NewMA extends FrameMenuAction {
    /**
     * Creates a new NewMA instance.
     *
     * @param name the name of the action (command)
     * @param frame the associated frame
     */
    public NewMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows a multiple file chooser and creates a new transcription.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {       
        List<String> fileNames = null;        
        FileChooser chooser = new FileChooser(frame);
        chooser.createAndShowMultiFileDialog(ElanLocale.getString(
                "Frame.ElanFrame.NewDialog.Title"), FileChooser.MEDIA_TEMPLATE);
       Object[] files = chooser.getSelectedFiles();
       if(files != null){
    	   if (files.length > 0) {
               fileNames = new ArrayList<String>();

               for (Object file : files) {
                   if (fileNames.contains(file) == false) {
                       fileNames.add("" + file);
                   }
               }
           }
    	   createNewFile(fileNames);
        }  
    }
    
    public void createNewFile(List<String> fileNames){
    	 List<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();
    	 
    	 if (fileNames != null) {
             //check if non-existing filenames were entered
             int fileNames_size = fileNames.size();

             for (int i = 0; i < fileNames_size; i++) {
                 String strFile = fileNames.get(i);

                 if (strFile.startsWith("rtsp")) {
                     continue;
                 }

                 if ((new File(strFile)).exists() == false) {
                     String strMessage = ElanLocale.getString(
                             "Menu.Dialog.Message1");
                     strMessage += (new File(strFile)).getName();
                     strMessage += ElanLocale.getString("Menu.Dialog.Message2");

                     String strError = ElanLocale.getString("Message.Error");
                     JOptionPane.showMessageDialog(frame, strMessage, strError,
                         JOptionPane.ERROR_MESSAGE);

                     return;
                 }
             }

             //get first template file; if there are more template files, rest is not used
             //check whether a eaf file is selected
             String strTemplate = "";             
             
             for (int i = fileNames_size - 1; i >= 0; i--) {
                 String strFile = fileNames.get(i);

                 if (strFile.toLowerCase().endsWith(".etf")) {
                     // use the first etf file in the list
                     strTemplate = fileNames.get(i);

                     // remove template files
                     fileNames.remove(i);
                 } else if(strFile.toLowerCase().endsWith(".eaf")){
                	 // if only eaf file is selected, open the eaf file
                	 if(fileNames.size() == 1 && !strTemplate.toLowerCase().endsWith(".etf")){
                		 frame.openEAF(strFile);                		
                		 return;
                	 }else{
                		 // remove eaf files
                         fileNames.remove(i);
                	 }
                 }
             }

             mediaDescriptors = MediaDescriptorUtil.createMediaDescriptors(fileNames);

             // create the Transcription
             TranscriptionImpl nextTranscription;

             if (strTemplate.equals("")) {
                 nextTranscription = new TranscriptionImpl();
                 // HS Jan 2007 create a default LinguisticType and Tier
                 if (nextTranscription.getLinguisticTypes().size() == 0 && 
                         nextTranscription.getTiers().size() == 0) {
                	 addInitialTierAndType(nextTranscription);    
                 }
             } else {
                 nextTranscription = new TranscriptionImpl(new File(strTemplate).getAbsolutePath());
                 nextTranscription.setName(TranscriptionImpl.UNDEFINED_FILE_NAME);

                 // HS nov 2006: set the pathname to undefined too instead of
                 // the path to the template
                 nextTranscription.setPathName(TranscriptionImpl.UNDEFINED_FILE_NAME);
                 if (nextTranscription.getControlledVocabularies().size() > 0) {
                 	new TranscriptionECVLoader().loadExternalCVs(nextTranscription, frame);
                 }
             }

             nextTranscription.setMediaDescriptors(mediaDescriptors);

             nextTranscription.setChanged();
             
             // apply template preferences
             if (!strTemplate.equals("")) {
             	String prefPath = strTemplate.substring(0, strTemplate.length() - 3) + "pfsx";
             	try {
             		File pFile = new File(prefPath);
             		if (pFile.exists()) {
             			Preferences.importPreferences(nextTranscription, prefPath);
             		}
             	} catch (Exception ex) {// catch any exception and continue
             		
             	}
             }
             
             createFrame(nextTranscription);
         }
    }
    
    /**
     * Creates a frame for the new transcription.
     * 
     * @param nextTranscription the newly created transcription
     */
    
    protected void createFrame(Transcription nextTranscription) {
    	 ElanFrame2 frame = FrameManager.getInstance().createFrame(nextTranscription);
         
         if(MonitoringLogger.isInitiated()){
         	MonitoringLogger.getLogger(null).log(MonitoringLogger.NEW_FILE);
         	MonitoringLogger.getLogger(frame.getViewerManager().getTranscription()).log(MonitoringLogger.NEW_FILE);
         }
    }
    
    protected void addInitialTierAndType(TranscriptionImpl transcription) {
        // time-alignable, no constraint type
        LinguisticType type = new LinguisticType("default-lt");                     
        transcription.addLinguisticType(type); 
        
        TierImpl tier = new TierImpl("default", "", transcription, type);
        transcription.addTier(tier);
        tier.setDefaultLocale(null);
        
//        type = new LinguisticType("time-sub");     
//        type.addConstraint(new TimeSubdivision());
//        transcription.addLinguisticType(type);                            
//        
//        type = new LinguisticType("sym-sub");
//        type.addConstraint(new SymbolicSubdivision());
//        type.setTimeAlignable(false);                     
//        transcription.addLinguisticType(type);          
//        
//        type = new LinguisticType("sym-asso"); 
//        type.addConstraint(new SymbolicAssociation());
//        type.setTimeAlignable(false);        
//        transcription.addLinguisticType(type);          
//        
//        type = new LinguisticType("included-in"); 
//        type.addConstraint(new IncludedIn());
//        transcription.addLinguisticType(type);
    }
}
