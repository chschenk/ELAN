package mpi.eudico.client.annotator;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.gui.ChangedTranscriptionsPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.util.FrameInfo;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A singleton manager class that keeps track of open windows and that
 * maintains the "recent files" list.
 *
 * @author Han Sloetjes, MPI
 */
@SuppressWarnings("serial")
public class FrameManager implements PreferencesListener{
    // the manager

    /** the single manager */
    private static final FrameManager manager = new FrameManager();
    private boolean exitAllowed = false;

    /** contains  mappings from abbreviated file path to full path */
    private Map<String, String> recentFilesMap;

    /** contains a list of the recently opened files, the most recent at index 0 */     
    private List<String> recentFilesList;

    /** a List containing (Elan)FrameInfo objects */
    private List<FrameInfo> openFrameList;

    /** the max length of the file path */
    private final int maxFilePathLength = 40;

    /** a counter for frame id's */
    private int frameCounter = 0;

    /** a prefix for frame id's */
    private final String FR_NAME = "Frame-";

    /** the preferences key for the recent file list */
    private final String RECENTS_PREF = "FrameManager.RecentFiles";

    /** exit message preference */
    private final String EXIT_PREF = "FrameManager.ExitMes";
    
    /* Mod by Mark */
    /** a list with all recent items up to a maximum of 30 */
    private List<String> completeRecentItems;
	
    /* --- END --- */
    /** the preferred number of recent files in the list */
    private int nrRecentItemsPreference = FrameConstants.MAX_NUM_RECENTS;
    
    private static List<ShutdownListener> listenerList;

    
    /**
     * Private constructor, initializes some lists and maps.
     */
    private FrameManager() {
    /* Mod by Mark */
        Integer recentItems = Preferences.getInt("UI.RecentItems", null);
        if (recentItems != null) {
        	nrRecentItemsPreference = recentItems.intValue();
        }
        
		recentFilesMap = new HashMap<String, String>(nrRecentItemsPreference);
        recentFilesList = new ArrayList<String>(nrRecentItemsPreference);
        openFrameList = new ArrayList<FrameInfo>(10);
        listenerList = new ArrayList<ShutdownListener>(1);

        // get the stored recent files list
        completeRecentItems = Preferences.getListOfString(RECENTS_PREF, null);

        if (completeRecentItems != null) {
            String url;
            String shUrl;    
            
            for (int i = 0; i < completeRecentItems.size() && i < nrRecentItemsPreference; i++) {
                url = completeRecentItems.get(i);

                if (url != null) {
                    recentFilesList.add(url);
                    shUrl = fullPathToDisplayPath(url);

                    if (shUrl != null) {
                        recentFilesMap.put(url, shUrl);
                    }
                }
            }
        } else {
        	completeRecentItems = new ArrayList<String>();
        }
    /* --- END --- */
        Preferences.addPreferencesListener(null, this);
    }
    
    /* Mod by Mark */
    /**
     * Called after a change in the preferred size of the recent files list. 
     */
    private void updateRecentFileMenu() {
        //int nrRecentItemsPreference = FrameConstants.MAX_NUM_RECENTS;
        Integer recentItems = Preferences.getInt("UI.RecentItems", null);
        if (recentItems != null) {
        	nrRecentItemsPreference = recentItems.intValue();
        }
    	List<String> old_recentFilesList = new ArrayList<String>(
    			recentFilesList.subList(0, recentFilesList.size()));
    	
    	int nrTotalRecentFiles = completeRecentItems.size();
    	if(nrRecentItemsPreference <= nrTotalRecentFiles) {
    		recentFilesList.clear();
    		recentFilesList.addAll(completeRecentItems.subList(0, nrRecentItemsPreference));
    	} else {
    		recentFilesList.clear();
    		recentFilesList.addAll(completeRecentItems);
    	}
    	updateRecentFilesMap();
    	
    	for (FrameInfo frame_info:openFrameList) {
    		if ((frame_info).getFrame() instanceof ElanFrame2) {
    			ElanFrame2 elan_frame = (ElanFrame2)(frame_info).getFrame();
    			
    			for(String url:old_recentFilesList) {
					elan_frame.removeActionFromMenu(url, FrameConstants.RECENT);
				}
    			
                for (int i = 0; i < recentFilesList.size(); i++) {
                    String lon = recentFilesList.get(i);
                    String sho = recentFilesMap.get(lon);
                    elan_frame.addActionToMenu(new RecentAction(elan_frame, sho, lon),
                        FrameConstants.RECENT, i);
                }
    		}
    	}
    }
    /* --- END --- */

    /**
     * Returns the single instance of the FrameManager.
     *
     * @return the framemanager
     */
    public static FrameManager getInstance() {
        return manager;
    }

    /**
     * Finds the frame with the specified id and calls removeFrame(frame)
     *
     * @param frameId the id of the frame
     */
    public void closeFrame(String frameId) {
        if (frameId == null) {
            return;
        }

        FrameInfo fin = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrameId().equals(frameId)) {
                break;
            } else {
                fin = null;
            }
        }

        if (fin != null) {
            closeFrame(fin.getFrame());
        }
    }

    /**
     * Removes the specified frame, updates the open windows menus  of the
     * remaining frames and creates a new empty frame if this was the  last
     * open frame.
     *
     * @param frame the frame to remove from the lists
     */
    public void closeFrame(JFrame frame) {
        if (frame == null) {
            return;
        }

        FrameInfo fin = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrame() == frame) {
                break;
            } else {
                fin = null;
            }
        }

        if (fin == null) {
            return; // the frame is not in the list
        }

        String id = fin.getFrameId();
        openFrameList.remove(fin);

        if (id != null) {
            // notify remaining frames that the corresponding menuitem 
            // should be removed
            FrameInfo info;

            for (int i = 0; i < openFrameList.size(); i++) {
                info = openFrameList.get(i);

                if (info.getFrame() instanceof ElanFrame2) {
                    ((ElanFrame2) info.getFrame()).removeActionFromMenu(id,
                        FrameConstants.WINDOW);
                }
            }
        }

        // check which window is now active
        FrameInfo actInfo = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            actInfo = openFrameList.get(i);

            if (actInfo.getFrame().isActive()) {
                break;
            } else {
                actInfo = null;
            }
        }

        if (actInfo != null) {
            FrameInfo other;

            for (int i = 0; i < openFrameList.size(); i++) {
                other = openFrameList.get(i);

                if (other.getFrame() instanceof ElanFrame2) {
                    ((ElanFrame2) other.getFrame()).setMenuSelected(actInfo.getFrameId(),
                        FrameConstants.WINDOW);
                }
            }
        }

        // 07-2007 changed behaviour: exit the application if there is only
        // one empty window
        if (openFrameList.size() == 0) {
            if (frame instanceof ElanFrame2) {
                if (((ElanFrame2) frame).getViewerManager() != null) {
                	//createEmptyFrame();
                  
                	// creating the empty frame immediately does not activate the empty frame in all
                	// situations, as a result the shortcuts does not work on a empty frame created
                	// when a frame is closed 
                	EventQueue.invokeLater(new Runnable() {
                        @Override
						public void run() {
                            FrameManager.getInstance().createEmptyFrame();
                        }
                    });
                } else {
                    exit();
                }
            } else {
                //createEmptyFrame();
            	
            	// creating the empty frame immediately does not activate the empty frame in all
            	// situations, as a result the shortcuts does not work on a empty frame created
            	// when a frame is closed 
                EventQueue.invokeLater(new Runnable() {
                    @Override
					public void run() {
                        FrameManager.getInstance().createEmptyFrame();
                    }
                });
            }
            //createEmptyFrame();
        } 
    }

    /**
     * Called from an action that has been added to the open window menu.
     *
     * @param frameId the internal id of the window
     */
    public void setToFront(String frameId) {    	
        FrameInfo fin = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrameId().equals(frameId)) {
                fin.getFrame().toFront();

                break;

                // updating the menu's should then follow a call from 
                // the frame??
            }
        }
    }

    /**
     * Activate next or previous frame as listed in the menu, through keyboard
     * shortcuts.
     *
     * @param forward if true the next frame in the menu list will be activated,
     * if it is the last the first element in the list will become active
     */
    public void activateNextFrame(boolean forward) {
        if (openFrameList.size() <= 1) {
            return;
        }

        FrameInfo loopInfo;
        ElanFrame2 ef2;
        int current = -1;

        // find current active window
        for (int i = 0; i < openFrameList.size(); i++) {
            loopInfo = openFrameList.get(i);

            if (loopInfo.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) loopInfo.getFrame();

                if (ef2.isActive()) {
                    current = i;

                    break;
                }
            }
        }

        int next = -1;

        if (current == (openFrameList.size() - 1)) {
            if (forward) {
                next = 0;
            } else {
                next = current - 1;
            }
        } else if (current == 0) {
            if (forward) {
                next = 1;
            } else {
                next = openFrameList.size() - 1;
            }
        } else {
            if (forward) {
                next = current + 1;
            } else {
                next = current - 1;
            }
        }

        if ((next > -1) && (next < openFrameList.size())) {
            loopInfo = openFrameList.get(next);
            loopInfo.getFrame().toFront();

            // update of menus follows a call from the frame
        }
    }

    /**
     * After a frame has been activated this method should be called to  update
     * the window menu.
     *
     * @param frame the new active frame
     */
    public void frameActivated(JFrame frame) {
        if (frame == null) {
            return;
        }

        FrameInfo fin = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrame() == frame) {
                frameActivated(fin.getFrameId());                
            }
        }
    }
    
    public void updateShortcuts(){
    	FrameInfo fin = null;
    	ElanFrame2 frame = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);
            if (fin.getFrame() instanceof ElanFrame2) {    
            	frame = (ElanFrame2) fin.getFrame();
            	ElanLayoutManager layoutManager = frame.getLayoutManager();
                if(layoutManager != null){
                	layoutManager.shortcutsChanged();
//                	String modeName =layoutManager.getModeName(layoutManager.getMode());  
//                	frame.updateShortcutMap(modeName);
                }  
        	}
        }
    }

    /**
     * After a frame has been activated this method should be called to  update
     * the window menu.
     *
     * @param frameId the internal id of the new active frame
     */
    public void frameActivated(String frameId) {
        if (frameId != null) {
            // update menus
            FrameInfo fin = null;

            for (int i = 0; i < openFrameList.size(); i++) {
                fin = openFrameList.get(i);

                if (fin.getFrame() instanceof ElanFrame2) {
                    ((ElanFrame2) fin.getFrame()).setMenuSelected(frameId,
                        FrameConstants.WINDOW);     
                }
            }
        }
    }

    /**
     * Returns the current active frame.
     * 
     * @return the currently active frame
     */
    public JFrame getActiveFrame() {
        if (openFrameList.size() == 0) {
            return null;
        }

        FrameInfo loopInfo;
        ElanFrame2 ef2;
        
        // find current active window
        for (int i = 0; i < openFrameList.size(); i++) {
            loopInfo = openFrameList.get(i);

            if (loopInfo.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) loopInfo.getFrame();

                if (ef2.isActive()) {
                    return ef2;
                }
            }
        }
        
    	return null;
    }
    /**
     * Find the frame for the specified file.
     *
     * @param filePath the file path
     *
     * @return the frame
     */
    public ElanFrame2 getFrameFor(String filePath) {
        return getFrameFor(filePath, true);
    }
    
    /**
     * Find the frame for the specified file.
     *
     * @param filePath the file path
     *
     * @return the frame
     */
    public ElanFrame2 getFrameFor(String filePath, boolean createNewFrame) {
        if (filePath == null) {
            return null;
        }

        //String url = FileUtility.pathToURLString(filePath);
        FrameInfo fin = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);
            if (fin.getFilePath() != null && fin.getFilePath().equals(filePath)) {
                if (fin.getFrame() instanceof ElanFrame2) {
                    return (ElanFrame2) fin.getFrame();
                }
            }
        }
        if(createNewFrame){
        	// not found, create a new frame 
        	return createFrame(filePath);
        } 

        return null;
    }

    /**
     * Find the frame for the specified Transcription.
     *
     * @param transcription the transcription
     *
     * @return the frame, or null if not found
     */
    public ElanFrame2 getFrameFor(Transcription transcription) {
        if (transcription == null) {
            return null;
        }

        for (int i = 0; i < openFrameList.size(); i++) {
            FrameInfo fin = openFrameList.get(i);
            
            if (fin.getFrame() instanceof ElanFrame2) {
                ElanFrame2 frame = (ElanFrame2) fin.getFrame();
    			ViewerManager2 vm = frame.getViewerManager();
    			
    			if (vm != null) {
	    			Transcription t = vm.getTranscription();
	    			
	    			if (t == transcription) {
	    				return frame;
	    			}
    			}
            }
        }

        return null;
    }

    /**
     * To be used after a Save As action as well as after loading an existing
     * file  in an empty frame
     *
     * @param frame the frame
     * @param newPath the new path (possibly as URL)
     */
    public void updateFrameTitle(JFrame frame, String newPath) {
        if ((frame == null) || (newPath == null)) {
            return;
        }

    	newPath = FileUtility.urlToAbsPath(newPath);

        // get the frameinfo object
        FrameInfo fin = null;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrame() == frame) {
                break;
            } else {
                fin = null;
            }
        }

        if (fin == null) {
            return;
        }

        //String oldUrl = fin.getFilePath();
        //String oldName = fin.getFrameName();
        String nextName = FileUtility.fileNameFromPath(newPath);

        // update the open frames menu
        FrameInfo loopInfo;
        ElanFrame2 ef2;

        for (int i = 0; i < openFrameList.size(); i++) {
            loopInfo = openFrameList.get(i);

            if (loopInfo.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) loopInfo.getFrame();
                ef2.removeActionFromMenu(fin.getFrameId(), FrameConstants.WINDOW);
                ef2.addActionToMenu(new ActiveWindowAction(fin.getFrameId(),
                        nextName), FrameConstants.WINDOW, -1);

                if (frame.isActive()) {
                    ef2.setMenuSelected(fin.getFrameId(), FrameConstants.WINDOW);
                }
            }
        }

        // adjust the frame info object
        fin.setFilePath(newPath);
        fin.setFrameName(nextName);

        // update recent file list
        addToRecentFiles(newPath);
    }

    /**
     * Prepares and executes the shutdown of the application. Checks if there
     * are  any transcription that need to be saved.
     */
    public void exit() {
        // show a warning message about the changed exit behavior
    	/* HS July 2012 Don't show this message anymore
        Boolean again = (Boolean) Preferences.get(EXIT_PREF, null);

        if ((again == null) || !again.booleanValue()) {
            ExitStrategyPane pane = new ExitStrategyPane();
            int option = JOptionPane.showConfirmDialog(getActiveFrame(), pane,
                    ElanLocale.getString("Menu.File.Exit"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            boolean sel = pane.getDontShowAgain();

            if (sel) {
                Preferences.set(EXIT_PREF, Boolean.valueOf(sel), null);
            }
        }
		*/

    	fireWindowClosing(this, ShutdownListener.Event.ELAN_EXITS_EARLY);

        // check openframes with changed transcriptions and ask save etc.
        List<String> changedTrans = new ArrayList<String>(openFrameList.size());
        FrameInfo loopInfo;
        ElanFrame2 ef2;

        for (int i = 0; i < openFrameList.size(); i++) {
            loopInfo = openFrameList.get(i);

            if (loopInfo.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) loopInfo.getFrame();

                if (ef2.getViewerManager() != null) {
                	ViewerManager2 vm = ef2.getViewerManager();
                	Transcription transcription = vm.getTranscription();

                    if (transcription.isChanged()) {
                        changedTrans.add(loopInfo.getFrameName());
                    }
                } else {
                    ef2.doClose(false);
                }
            }
        }

        List<String> vals = null;

        if (changedTrans.size() > 0) {
            // prompt with a list of dirty transcriptions
            ChangedTranscriptionsPane pane = new ChangedTranscriptionsPane(changedTrans);
            int option = JOptionPane.showConfirmDialog(getActiveFrame(), pane,
                    ElanLocale.getString("Menu.File.Save"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            vals = pane.getSelectedValues();
        }

        // close the frames, save the selected transcriptions
        for (int j = 0; j < openFrameList.size(); j++) {
            loopInfo = openFrameList.get(j);

            if ((vals != null) && vals.contains(loopInfo.getFrameName())) {
                // doublechecking maybe not necessary
                if (loopInfo.getFrame() instanceof ElanFrame2) {
                    ef2 = (ElanFrame2) loopInfo.getFrame();
                    ef2.saveAndClose(false);
                }
            } else {
                if (loopInfo.getFrame() instanceof ElanFrame2) {
                    ef2 = (ElanFrame2) loopInfo.getFrame();
                    ef2.doClose(false);
                }
            }
        }

        // store the recent files list in preferences
        /* Mod by Mark */
        Preferences.set(RECENTS_PREF, completeRecentItems, null);
		/* --- END --- */
        // check if all frames are closed; the save as dialog of an unsaved, 
        // new document could have been cancelled: don't quit
        ArrayList<FrameInfo> closed = new ArrayList<FrameInfo>(openFrameList.size());

        for (int i = openFrameList.size() - 1; i >= 0; i--) {
            loopInfo = openFrameList.get(i);

            if (loopInfo.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) loopInfo.getFrame();

                if (!ef2.isShowing()) {
                    closed.add(loopInfo);
                    openFrameList.remove(loopInfo);
                }
            }
        }

        if (openFrameList.size() > 0) {
            // there is still some frame open
            FrameInfo ofi;

            for (int i = 0; i < closed.size(); i++) {
                loopInfo = closed.get(i);

                for (int j = 0; j < openFrameList.size(); j++) {
                    ofi = openFrameList.get(j);

                    if (ofi.getFrame() instanceof ElanFrame2) {
                        ef2 = (ElanFrame2) ofi.getFrame();
                        ef2.removeActionFromMenu(loopInfo.getFrameId(),
                            FrameConstants.WINDOW);
                    }
                }
            }

            // check which frame is active and update menu??
            //don't exit
            return;
        }

        if (exitAllowed) {
        	fireWindowClosing(this, ShutdownListener.Event.ELAN_EXITS_LATE);
        	MonitoringLogger.exitElan();
        	ClientLogger.LOG.info("ELAN stopped.");
            System.exit(0);
        }
    }

    /**
     * Creates a new empty frame and adds it to the lists.
     *
     * @return the new Elan window
     */
    ElanFrame2 createEmptyFrame() {
        ElanFrame2 ef2 = new ElanFrame2();
        addFrame(ef2);
                
        return ef2;
    }

    /**
     * Creates a new ELAN window, loading the specified eaf file.
     *
     * @param eafPath the path of the .eaf file
     *
     * @return the (new) ELAN window
     */
    public ElanFrame2 createFrame(String eafPath) {
        if (eafPath == null) {
            return null;
        }

        ElanFrame2 ef2 = null;

        if (openFrameList.size() == 1) {
            FrameInfo fin = openFrameList.get(0);

            if (fin.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) fin.getFrame();

                if (ef2.getViewerManager() == null) {
                    // single empty frame
                    ef2.openEAF(eafPath);
                    updateFrameTitle(ef2, eafPath);
                } else {
                    ef2 = new ElanFrame2(eafPath);
                    addFrame(ef2);
                }
            }
        } else {
            ef2 = new ElanFrame2(eafPath);
            addFrame(ef2);
        }
        return ef2;
    }

    /**
     * Creates a new ELAN window, loading the specified eaf file and using the
     * mediafiles in the specified list as the associated media files.
     *
     * @param eafPath the path of the .eaf file
     * @param files a list of media file paths
     *
     * @return the (new) ELAN window
     */
    public ElanFrame2 createFrame(String eafPath, List<String> files) {
        if (eafPath == null) {
            return null;
        }

        if (files == null) {
            return createFrame(eafPath);
        }

//        Vector<String> fileVector = null;
//
//        if (files instanceof Vector) {
//            fileVector = (Vector<String>) files;
//        } else {
//            fileVector = new Vector<String>(files);
//        }

        ElanFrame2 ef2 = null;

        if (openFrameList.size() == 1) {
            FrameInfo fin = openFrameList.get(0);

            if (fin.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) fin.getFrame();

                if (ef2.getViewerManager() == null) {
                    // single empty frame
                    ef2.openEAF(eafPath, files);
                    updateFrameTitle(ef2, eafPath);
                } else {
                    ef2 = new ElanFrame2(eafPath, files);
                    addFrame(ef2);
                }
            }
        } else {
            ef2 = new ElanFrame2(eafPath, files);
            addFrame(ef2);
        }
        
        return ef2;
    }

    /**
     * Creates a new ELAN window with the specified transcription as the document.
     *
     * @param transcription the document object for the (new) frame
     *
     * @return the (new) ELAN window
     */
    public ElanFrame2 createFrame(Transcription transcription) {
        if (transcription == null) {
            return null;
        }

        ElanFrame2 ef2 = null;

        if (openFrameList.size() == 1) {
            FrameInfo fin = openFrameList.get(0);

            if (fin.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) fin.getFrame();

                if (ef2.getViewerManager() == null) {
                    // single empty frame
                    ef2.setTranscription(transcription);                    

                    String fullPath = transcription.getName();

                    if (!transcription.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                        fullPath = transcription.getFullPath();

                        if (fullPath.startsWith("file")) {
                            fullPath = fullPath.substring(5);
                        }

                        updateFrameTitle(ef2, fullPath);
                    }
                } else {
                    ef2 = new ElanFrame2(transcription);
                    addFrame(ef2);
                }
            }
        } else {
            ef2 = new ElanFrame2(transcription);
            addFrame(ef2);
        }

        return ef2;
    }

    /**
     * A new ELAN Frame has been created and should be added to the list. Menus
     * have to be updated.
     *
     * @param frame the new Elan Frame
     */
    public void addFrame(JFrame frame) {
        if (frame == null) {
            return;
        }

        String id = FR_NAME + frameCounter++;
        String fullPath = null;
        String fileName = ElanLocale.getString("Frame.ElanFrame.Untitled") +
            "-" + frameCounter;
        FrameInfo frInfo = new FrameInfo(frame, id);
        frInfo.setFrameName(fileName);
        openFrameList.add(frInfo);

        if (frame instanceof ElanFrame2) {
            ElanFrame2 ef2 = (ElanFrame2) frame;

            if (ef2.getViewerManager() != null) {
                TranscriptionImpl tr = (TranscriptionImpl) ef2.getViewerManager()
                                                              .getTranscription();

                if (!tr.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                    fullPath = tr.getFullPath();

                    fullPath = FileUtility.urlToAbsPath(fullPath);

                    fileName = tr.getName();
                    frInfo.setFilePath(fullPath);
                    frInfo.setFrameName(fileName);
                }
            }

            // add current recent files list to the new window
            String sho;
            String lon;

            for (int i = 0; i < recentFilesList.size(); i++) {
                lon = recentFilesList.get(i);
                sho = recentFilesMap.get(lon);
                ef2.addActionToMenu(new RecentAction(ef2, sho, lon),
                    FrameConstants.RECENT, i);
            }
        }

        // add a menu item for the new frame to all window menu's
        // add actions for all existing frames to the new frame 
        FrameInfo fin;
        ElanFrame2 ef2;
        Action ac;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) fin.getFrame();
                ac = new ActiveWindowAction(id, fileName);
                ef2.addActionToMenu(ac, FrameConstants.WINDOW, -1);

                // set selected ?
                ef2.setMenuSelected(id, FrameConstants.WINDOW);
            }

            if (fin != frInfo) {
                if (frInfo.getFrame() instanceof ElanFrame2) {
                    ef2 = (ElanFrame2) frInfo.getFrame();
                    ac = new ActiveWindowAction(fin.getFrameId(),
                            fin.getFrameName());
                    ef2.addActionToMenu(ac, FrameConstants.WINDOW, i);
                }
            }
        }

        if (fullPath != null) {
            addToRecentFiles(fullPath);
        }
    }

    /**
     * Adds the filePath to the recent files list, if it is an .eaf path
     * and if it is not already in the list.
     *
     * @param fullPath the eaf file path
     */
    private void addToRecentFiles(final String fullPath) {
        if (fullPath == null) {
            return;
        }

        if (!fullPath.toLowerCase().endsWith(".eaf")) {
            return;
        }

        String shortUrl = fullPathToDisplayPath(fullPath);

        boolean move = false;
		/* Mod by Mark */
        if (completeRecentItems.contains(fullPath)) {
            if (completeRecentItems.indexOf(fullPath) == 0) {
                return; // change nothing
            }

            move = true;
            completeRecentItems.remove(fullPath);
            completeRecentItems.add(0, fullPath);
        } else {
        	completeRecentItems.add(0, fullPath);
        	// changed from FrameConstants.MAX_NUM_RECENTS to FrameConstants.MAX_NUM_STORED_RECENTS
            if (completeRecentItems.size() > FrameConstants.MAX_NUM_STORED_RECENTS) {
            	completeRecentItems.remove(completeRecentItems.size() - 1);
                // could remove from the recentFilesMap
            }

            if (shortUrl != null) {
                recentFilesMap.put(fullPath, shortUrl);
            }
        }
        /* --- END --- */
        // if we  get here we have to update the recentFilesList
        recentFilesList.clear();
        if (completeRecentItems.size() > nrRecentItemsPreference) {
        	recentFilesList.addAll(completeRecentItems.subList(0, nrRecentItemsPreference));
        } else {
        	recentFilesList.addAll(completeRecentItems);
        }
        
        updateRecentFilesMap();

        FrameInfo fin;
        ElanFrame2 ef2;
        Action ac;

        for (int i = 0; i < openFrameList.size(); i++) {
            fin = openFrameList.get(i);

            if (fin.getFrame() instanceof ElanFrame2) {
                ef2 = (ElanFrame2) fin.getFrame();

                if (move) {
                    ac = ef2.removeActionFromMenu(fullPath,
                            FrameConstants.RECENT);
                    if(ac == null){
                    	ac = new RecentAction(ef2, shortUrl, fullPath);                    	
                    }
                    ef2.addActionToMenu(ac, FrameConstants.RECENT, 0);
                } else {
                    ac = new RecentAction(ef2, shortUrl, fullPath);
                    ef2.addActionToMenu(ac, FrameConstants.RECENT, 0);
                }
            }
        }
        Preferences.set(RECENTS_PREF, completeRecentItems, null);
    }
    
    private void updateRecentFilesMap(){
    	recentFilesMap.clear();
    	
    	String url;
        String shUrl;    
            
        for (int i = 0; i < recentFilesList.size(); i++ ) {
        	url = completeRecentItems.get(i);
            if (url != null) {
                shUrl = fullPathToDisplayPath(url);
                if (shUrl != null) {
                	recentFilesMap.put(url, shUrl);                   
                }
            }
        } 
    }

    /**
     * Removes the specified file (path) from the recent files list, if it is in the list
     * e.g. when a file has been deleted or moved.
     *
     * @param fullPath the path
     */
    private void removeFromRecentFiles(final String fullPath) {
        if (fullPath == null) {
            return;
        }
		/* Mod by Mark */
        if (completeRecentItems.contains(fullPath)) {
        	completeRecentItems.remove(fullPath);
        	// update the complete list??
        	recentFilesList.remove(fullPath);
        	recentFilesMap.remove(fullPath);
		/* --- END --- */
            // remove from menus
            FrameInfo fin;
            ElanFrame2 ef2;

            for (int i = 0; i < openFrameList.size(); i++) {
                fin = openFrameList.get(i);

                if (fin.getFrame() instanceof ElanFrame2) {
                    ef2 = (ElanFrame2) fin.getFrame();
                    ef2.removeActionFromMenu(fullPath, FrameConstants.RECENT);
                }
            }
			/* Mod by Mark */
            Preferences.set(RECENTS_PREF, completeRecentItems, null);
            /* --- END --- */
        }
    }

    /**
     * Converts a full eaf file path to a shortened display path for a  menu
     * item.
     *
     * @param fullPath the full eaf file path
     *
     * @return a shortened file path
     */
    public String fullPathToDisplayPath(String fullPath) {
        if (fullPath == null) {
            return "";
        }

        int start = 0;
        int lastSep = fullPath.lastIndexOf(File.separatorChar);

        if (lastSep < 0) {
            if (fullPath.length() <= maxFilePathLength) {
                return fullPath;
            } else {
                return fullPath.substring(0, maxFilePathLength - 3) + "...";
            }
        }

        if (fullPath.startsWith("file:")) {
            start = 5;
        }

        int colon = fullPath.indexOf(':', start);

        if (((colon - start) > 0) && ((colon - start) < 5)) {
            // assume windows path ?
            start = colon - 1;
        } else {
            int ad = 0;

            for (int i = 0; i < 3; i++) {
                if (fullPath.charAt(start + i) == File.separatorChar) {
                    ad++;
                }
            }

            if (ad == 3) {
                start += 2;
            }
        }

        if ((fullPath.length() - start) < maxFilePathLength) {
            return fullPath.substring(start);
        } else {
            int fl = fullPath.length() - lastSep;

            if (fl > maxFilePathLength) {
                return fullPath.substring(lastSep, lastSep + maxFilePathLength);
            }

            int fd = fullPath.indexOf(File.separatorChar, start + 3);

            if (fd == lastSep) {
                if (((fl + fd) - start) <= maxFilePathLength) {
                    return fullPath.substring(start);
                } else {
                    return fullPath.substring(start,
                        (start + maxFilePathLength) - fl - 3) + "..." +
                    fullPath.substring(lastSep);
                }
            } else if ((fd >= 0) &&
                    ((fl + (fd - start)) < (maxFilePathLength - 3))) {
                int nextSep = fd;

                while (true) {
                    nextSep = fullPath.indexOf(File.separatorChar, fd + 1);

                    if ((nextSep > 0) && (nextSep != lastSep)) {
                        if ((fl + (nextSep - start)) < (maxFilePathLength - 3)) {
                            fd = nextSep;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                return fullPath.substring(start, fd + 1) + "..." +
                fullPath.substring(lastSep);
            } else if (fd >= 0) {
                int rm = Math.max(maxFilePathLength - fl - 4, 1);

                if ((fd - start) > rm) {
                    fd = start + rm;

                    return fullPath.substring(start, fd) + "." +
                    File.separator + "..." + fullPath.substring(lastSep);
                } else {
                    return fullPath.substring(start, fd + 1) + "..." +
                    fullPath.substring(lastSep);
                }
            } else {
                return fullPath.substring(start);
            }
        }
    }

    /**
     * Returns whether or not the manager is allowed to call System.exit().
     * ELAN or an ELAN window may have been created by another tool, in such
     * case frames should just be closed without exiting the VM.
     * Default is false.
     *
     * @return whether or not the manager may call System.exit()
     */
    public boolean isExitAllowed() {
        return exitAllowed;
    }

    /**
     * Sets whther or not the application may call System.exit().
     * In ELAN's main method this flag is set to true.
     *
     * @param exitAllowed if true, ELAN can exit the VM
     */
    public void setExitAllowed(boolean exitAllowed) {
        this.exitAllowed = exitAllowed;
    }

    /**
     * Updates frames after a change in number of recent files preference. 
     *
     */
	@Override
	public void preferencesChanged() {
        Integer recentItems = Preferences.getInt("UI.RecentItems", null);
        if (recentItems != null) {
        	nrRecentItemsPreference = recentItems.intValue();
        }

		if (nrRecentItemsPreference != recentFilesList.size()) {
			updateRecentFileMenu();
		}
	}
	
	/**
	 * Delivers a List of the currently open Transcriptions.
	 */
	public List<Transcription> getOpenTranscriptions() {
		List<Transcription> res = new ArrayList<Transcription>();

		for (FrameInfo frame_info : openFrameList) {
    		if (frame_info.getFrame() instanceof ElanFrame2) {
    			ElanFrame2 elan_frame = (ElanFrame2)frame_info.getFrame();
    			
    			ViewerManager2 vm = elan_frame.getViewerManager();
    			if (vm != null) {
	    			Transcription t = vm.getTranscription();
	    			res.add(t);
    			}
    		}
    	}
    	return res;
	}
	
	/**
	 * Delivers a read-only version of the recent files.
	 * @return
	 */
	public List<String> getRecentFiles() {
		return Collections.unmodifiableList(recentFilesList);
	}

    /**
     * Adds a listener to the list that's notified when a frame or Elan closes.
     *
     * @param l the <code>ListDataListener</code> to be added
     */
    public void addWindowCloseListener(ShutdownListener l) {
        listenerList.add(l);
    }


    /**
     * Removes a listener from the list that's notified each time a
     * frame or Elan shuts down.
     *
     * @param l the <code>ListDataListener</code> to be removed
     */
    public void removeWindowCloseListener(ShutdownListener l) {
        listenerList.remove(l);
    }

    protected void fireWindowClosing(Object source, int type)
    {
        ShutdownListener.Event e = null;

        for (ShutdownListener l : listenerList) {
            if (e == null) {
                e = new ShutdownListener.Event(source, type);
            }
            l.somethingIsClosing(e);
        }
    }
	
    /**
     * An action object that stores a full path and a short path of
     * a recently used file. The actionPerformed() opens the file in
     * a (new) ELAN window.
     *
     * @author Han Sloetjes, MPI
     * @version 1.0
      */
	class RecentAction extends AbstractAction {
    	private JFrame fr;
        /**
         * Creates a new RecentAction instance
         *
         * @param shortUrl a shortened path for display in menu
         * @param fullUrl the full path to the file
         */
        RecentAction(JFrame fr, String shortUrl, String fullUrl) {
        	this.fr = fr;
            putValue(Action.NAME, shortUrl);

            // use LONG_DESCRIPTION or DEFAULT ?
            putValue(Action.LONG_DESCRIPTION, fullUrl);
        }

        /**
         * Lets the FrameManager open the corresponding file.
         *
         * @param e event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            //check if file still exists, if file does not exist warn and remove from lists
            try {
                String path = (String) getValue(Action.LONG_DESCRIPTION);
                File fileTemp = new File(path);

                //check if file exists and is a file
                if (!fileTemp.exists() || fileTemp.isDirectory()) {
                    String strMessage = ElanLocale.getString(
                            "Menu.Dialog.Message1");
                    strMessage += path;
                    strMessage += ElanLocale.getString("Menu.Dialog.Message2");

                    String strError = ElanLocale.getString("Message.Error");
                    JOptionPane.showMessageDialog(fr, strMessage, strError,
                        JOptionPane.ERROR_MESSAGE);

                    FrameManager.this.removeFromRecentFiles(path);

                    return;
                }
            } catch (Exception exc) {
            }

            ElanFrame2 frame = FrameManager.getInstance().createFrame((String) getValue(
                    Action.LONG_DESCRIPTION));
            
//            if(MonitoringLogger.isInitiated()){            	
//            	MonitoringLogger.getLogger(null).log(MonitoringLogger.OPEN_FILE, frame.getViewerManager().getTranscription().getName());	
//            	MonitoringLogger.getLogger(frame.getViewerManager().getTranscription()).log(MonitoringLogger.OPEN_FILE);
//            }
        }
    }

    /**
     * An Action that activates (brings to front) the frame identified
     * by the internal frame id. Th action stores a filename/framename for
     * displayal in a menu.
     *
     * @author Han Sloetjes, MPI
     * @version 1.0
      */
	class ActiveWindowAction extends AbstractAction {
        /**
         * Creates a new ActiveWindowAction instance
         *
         * @param frameId the internal frame id
         * @param fileName the file or frame name
         */
        ActiveWindowAction(String frameId, String fileName) {
            putValue(Action.NAME, fileName);

            // use LONG_DESCRIPTION or DEFAULT ?
            putValue(Action.LONG_DESCRIPTION, frameId);
        }

        /**
         * Lets the FrameManager activate the corresponding frame and update
         * the Window menu in all open frames.
         *
         * @param e the event
         */
        @Override
		public void actionPerformed(ActionEvent e) {
            FrameManager.getInstance().setToFront((String) getValue(
                    Action.LONG_DESCRIPTION));
        }
    }

}
