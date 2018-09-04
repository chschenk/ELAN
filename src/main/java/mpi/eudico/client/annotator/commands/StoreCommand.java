package mpi.eudico.client.annotator.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.SaveAs27Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationEncoder;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Saves a transcription as an .eaf or .etf (template) file, either creating a new 
 * file (Save As) or overwriting an existing file (Save).<br/>
 * The command will save as 2.7 format, if the user preference indicates it.
 * There is no need to pass TranscriptionStore.EAF_2_7 for that purpose.<br/>
 * In fact, it must not be done: the logic to determine if the transcription's
 * name must be updated does so if the format is EAF, and not if it is EAF_2_7.
 * 
 * @version Nov 2007 added support for relative media paths
 * @author Hennie Brugman
 */
public class StoreCommand implements Command {
    private String commandName;

    /**
     * Creates a new StoreCommand instance
     *
     * @param name DOCUMENT ME!
     */
    public StoreCommand(String name) {
        commandName = name;
    }

    //arguments:
    //[0]: TranscriptionStore eafTranscriptionStore
    //[1]: Boolean saveAsTemplate
    //[2]: Boolean saveNewCopy
    //[3]: List<TierImpl> visibleTiers
    //[4]: optional: format (TranscriptionStore.EAF or TranscriptionStore.EAF_2_7)
    @Override
	public void execute(Object receiver, Object[] arguments) {
        TranscriptionImpl tr = (TranscriptionImpl) receiver;
        TranscriptionStore eafTranscriptionStore = (TranscriptionStore) arguments[0];
        boolean saveAsTemplate = ((Boolean) arguments[1]).booleanValue();
        boolean saveNewCopy = ((Boolean) arguments[2]).booleanValue();
        List<TierImpl> visibleTiers;

        if (arguments[3] != null) {
        	if (arguments[3] instanceof List<?>) {
        		visibleTiers = (List<TierImpl>) arguments[3];
        	} else {
        		visibleTiers = new ArrayList<TierImpl>(0);//just to be on the safe side, should be ignored in most cases
        	}
        } else {
        	if (ELANCommandFactory.getViewerManager(tr)
                                             .getMultiTierControlPanel() != null) {
	            visibleTiers = ELANCommandFactory.getViewerManager(tr)
	                                             .getMultiTierControlPanel()
	                                             .getVisibleTiers();
        	} else {
        		visibleTiers = new ArrayList<TierImpl>(0);//just to be on the safe side, should be ignored in most cases
        	}
        }
        int format = TranscriptionStore.EAF;
        if (arguments.length > 4) {
        	int f = (Integer)arguments[4];
        	if (f == TranscriptionStore.EAF || f == TranscriptionStore.EAF_2_7) {
        		format = f;
        	}
        }
        // Only if we save in the default format, we change the file name of the current
        // transcription. For an Save A Copy As EAF 2.7... we keep such things unchanged.
        boolean updateFileName = (format == TranscriptionStore.EAF);

        // If the format is default, it can still be overridden by the preference.
        if (format == TranscriptionStore.EAF) {
        	format = SaveAs27Preferences.saveAsType(tr);
        }

        if (saveNewCopy) {
            // prompt for new file name         
            // open dialog at directory of original eaf file
            FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(tr));

            if (saveAsTemplate) {              
                chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Template.Title"), FileChooser.SAVE_DIALOG, 
                		FileExtension.TEMPLATE_EXT, "LastUsedEAFDir"); 
            } else {            	
            	String fileName = tr.getName();
            	
            	if(!fileName.equals(TranscriptionImpl.UNDEFINED_FILE_NAME)){
            		String filePath = FileUtility.urlToAbsPath(tr.getFullPath());
            		chooser.setCurrentDirectory(filePath.substring(0, filePath.indexOf(fileName)));    
            		fileName = fileName.substring(0,fileName.lastIndexOf('.')) + ".eaf";
            		
            		chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, null,
                			FileExtension.EAF_EXT, null,fileName); 
            	}else {            	
            		chooser.createAndShowFileDialog(ElanLocale.getString("SaveDialog.Title"), FileChooser.SAVE_DIALOG, 
                			FileExtension.EAF_EXT, "LastUsedEAFDir"); 
            	}            	
            }
            
            File f = chooser.getSelectedFile();
            if (f != null) {
                // make sure pathname finishes with .eaf or .etf extension
                String pathName = f.getAbsolutePath();  
                if (saveAsTemplate) {
                    try {
                    	eafTranscriptionStore.storeTranscriptionAsTemplateIn(tr,
                    			visibleTiers, pathName);                    
                    
                    	//overwrite the currentmode preferences such that, a new file created from a template
                    	//should always open in annotation mode                   
                    	Integer currentMode = Preferences.getInt("LayoutManager.CurrentMode", tr); 
                    	Preferences.set("LayoutManager.CurrentMode", ElanLayoutManager.NORMAL_MODE,tr); 
                                        
                    	// HS Nov 2009: save a preferences file alongside the template
                    	storePreferences(tr, format, pathName);
                    
                    	// restore the currentmode for the current transcription preferences
                    	if(currentMode != null){
                    		Preferences.set("LayoutManager.CurrentMode",currentMode,tr); 
                    	} 
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                "Unable to save the template file: " +
                                "(" + ioe.getMessage() + ")",
                                ElanLocale.getString("Message.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                	storePreferences(tr, format, pathName);

                    String name = pathName;
                    int lastSlashPos = name.lastIndexOf(System.getProperty(
                                "file.separator"));

                    if (lastSlashPos >= 0) {
                        name = name.substring(lastSlashPos + 1);
                    }

                    if (updateFileName) {
	                    //System.out.println("nm " + name);
	                    tr.setName(name);
	
	                    //tr.setName(pathName);
	                    if (tr instanceof TranscriptionImpl) {
	                        tr.setPathName(pathName);
	                        ((ElanFrame2)ELANCommandFactory.getRootFrame(tr)).setFrameTitle();
	                        FrameManager.getInstance().updateFrameTitle(ELANCommandFactory.getRootFrame(tr), 
	                        		pathName);
	                    } else {
	                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
	                            name);
	                    }
                    }
                    
                    // check, copy and update linked files, configuration and svg files 
                    List<LinkedFileDescriptor> linkedFiles = tr.getLinkedFileDescriptors();
                    String svgExt = ".svg";
                    String confExt = "_tsconf.xml";
                    String curExt;
                    if (linkedFiles.size() > 0) {
                    	LinkedFileDescriptor lfd;
                    	for (int i = 0; i < linkedFiles.size(); i++) {
                    		curExt = null;
                    		lfd = linkedFiles.get(i);
                    		if (lfd.linkURL.toLowerCase().endsWith(confExt)) {
                    			curExt = confExt;
                    		} else if (lfd.linkURL.toLowerCase().endsWith(svgExt)) {
                    			curExt = svgExt;
                    		}
                    		if (curExt != null) {
                    			// ELAN generated configuration file, copy
                    			String url = pathName.substring(0, pathName.length() - 4) +
                    				curExt;
                    			System.out.println("New conf: " + url);                     			
                    			// copy conf or svg
                    			try {
                    				File source = null, dest = null;
                    				if (lfd.linkURL.startsWith("file:")) {
                    					source = new File(lfd.linkURL.substring(5));
                    				} else {
                    					source = new File(lfd.linkURL);
                    				}
                    				if (url.startsWith("file:")) {
                    					dest = new File(url.substring(5));
                    				} else {
                    					dest = new File(url);
                    				}
                    				if (source.exists() && source.compareTo(dest) != 0) {
                    					FileUtility.copyToFile(source, dest);
                    				} else {                       					
                    					TSConfigurationEncoder enc = new TSConfigurationEncoder();
                    					enc.encodeAndSave(tr, 
                    							ELANCommandFactory.getTrackManager(tr).getConfigs());
                    				}
                    			} catch (Exception ex) {
                    				System.out.println("Could not copy the configuration file.");
                    			}
                    			lfd.linkURL = FileUtility.pathToURLString(url);
                    			tr.setChanged();
                    		}
                    	}
                    }
                    
                    // update relative media paths
                    // make sure the eaf path is treated the same way as media files,
                    // i.e. it starts with file:/// or file://
                    String fullEAFURL = FileUtility.pathToURLString(pathName);
                    fixRelativePathsOfLinkedFiles(tr, fullEAFURL);
                    
                    // save
                    try {
                        eafTranscriptionStore.storeTranscriptionIn(tr, null,
                                visibleTiers, pathName, format);
                        if(MonitoringLogger.isInitiated()){
                        	MonitoringLogger.getLogger(tr).log(MonitoringLogger.SAVE_FILE);
                        }
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                                //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                                "Unable to save the transcription file: " +
                                "(" + ioe.getMessage() + ")",
                                ElanLocale.getString("Message.Error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
//                    String name = pathName;
//                    int lastSlashPos = name.lastIndexOf(System.getProperty(
//                                "file.separator"));
//
//                    if (lastSlashPos >= 0) {
//                        name = name.substring(lastSlashPos + 1);
//                    }
//
//                    //System.out.println("nm " + name);
//                    tr.setName(name);
//
//                    //tr.setName(pathName);
//                    if (tr instanceof TranscriptionImpl) {
//                        ((TranscriptionImpl) tr).setPathName(pathName);
//                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
//                            tr.getName());
//                        FrameManager.getInstance().updateFrameTitle(ELANCommandFactory.getRootFrame(tr), 
//                        		pathName);
//                    } else {
//                        ELANCommandFactory.getRootFrame(tr).setTitle("ELAN - " +
//                            name);
//                    }
                    
                   if (updateFileName) {
	                   tr.setUnchanged();
	
	                   // create a new backup timer
	                   if (tr instanceof TranscriptionImpl) {
	                       ((BackupCA) ELANCommandFactory.getCommandAction(tr,
	                           ELANCommandFactory.BACKUP)).setFilePath(pathName);
	                   }
                   }
                }
            }             
        } else if (tr.isChanged()) {
            // check if relative media paths have to be generated or updated
            // make sure the eaf path is treated the same way as media files,
            // i.e. it starts with file:/// or file://
            String fullEAFURL = FileUtility.pathToURLString(tr.getFullPath());
            fixRelativePathsOfLinkedFiles(tr, fullEAFURL);
            
            try {
                eafTranscriptionStore.storeTranscription(tr, null, visibleTiers,
                		format);
                if(MonitoringLogger.isInitiated()){
                	MonitoringLogger.getLogger(tr).log(MonitoringLogger.SAVE_FILE);
                }
                if (ELANCommandFactory.getTrackManager(tr) != null) {
                	ELANCommandFactory.getTrackManager(tr).saveIfChanged();
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(tr),
                        //ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                        "Unable to save the transcription file: " +
                        "(" + ioe.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);
            }

            tr.setUnchanged();
        }// hier.. check if there are sec. linked files and store the config file
        else {
        	
        }
    }

	private void fixRelativePathsOfLinkedFiles(Transcription tr, String fullEAFURL) {
		List<MediaDescriptor> mediaDescriptors = tr.getMediaDescriptors();
		String relUrl;
		
		for (MediaDescriptor md : mediaDescriptors) {
		    relUrl = FileUtility.getRelativePath(fullEAFURL, md.mediaURL);
		    md.relativeMediaURL = relUrl;
		}
		
		// linked other files 
		List<LinkedFileDescriptor> linkedFiles = tr.getLinkedFileDescriptors();

		for (LinkedFileDescriptor lfd : linkedFiles) {
			relUrl = FileUtility.getRelativePath(fullEAFURL, lfd.linkURL);
			lfd.relativeLinkURL = relUrl;
		}
	}
	
	/**
	 * Save the preferences, if need be converted for the format we're exporting as.
	 * It can be in the old format because of the preference, but also because of
	 * explicit command.
	 * 
	 * @param ts knows how to convert the preferences
	 * @param t the preferences belong to this
	 * @param format this format might or might not need conversion
	 * @param pathName the name of the eaf, to find the name of the preferences.
	 */
	private void storePreferences(Transcription t, int format, String pathName) {
    	String templatePrefPath = pathName.substring(0, pathName.length() - 3) + "pfsx";
    	// convert the preferences to the other format if needed...
    	Object orig = SaveAs27Preferences.adjustPreferencesForSavingFormat(t, format);
    	Preferences.exportPreferences(t, templatePrefPath);
    	// Restore our original preferences
    	SaveAs27Preferences.restoreAdjustedPreferences(t, orig);

	}

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	public String getName() {
        return commandName;
    }
}
