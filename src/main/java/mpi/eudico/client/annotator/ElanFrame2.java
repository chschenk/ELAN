package mpi.eudico.client.annotator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import mpi.eudico.client.annotator.commands.BackupCA;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.CommandAction;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.PlayAroundSelectionCA;
import mpi.eudico.client.annotator.commands.PlaybackRateToggleCA;
import mpi.eudico.client.annotator.commands.PlaybackVolumeToggleCA;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.commands.StoreCommand;
import mpi.eudico.client.annotator.commands.global.AboutMA;
import mpi.eudico.client.annotator.commands.global.ActivityMonitoringMA;
import mpi.eudico.client.annotator.commands.global.AnnotatorCompareMA;
import mpi.eudico.client.annotator.commands.global.ClipMediaMultiMA;
import mpi.eudico.client.annotator.commands.global.CreateCommentViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateGridViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateInterlinearViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateLexiconViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateMetadataViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateRecognizerMA;
import mpi.eudico.client.annotator.commands.global.CreateSignalViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateSubtitleViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateTextViewerMA;
import mpi.eudico.client.annotator.commands.global.CreateTimeSeriesViewerMA;
import mpi.eudico.client.annotator.commands.global.EditLanguagesMA;
import mpi.eudico.client.annotator.commands.global.EditPreferencesMA;
import mpi.eudico.client.annotator.commands.global.EditShortcutsMA;
import mpi.eudico.client.annotator.commands.global.EditSpellCheckerMA;
import mpi.eudico.client.annotator.commands.global.EditTierSetMA;
import mpi.eudico.client.annotator.commands.global.ExitMA;
import mpi.eudico.client.annotator.commands.global.ExportAnnotationsMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportFlexMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportOverlapsMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportPraatMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportTabMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportThemeMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportTiersMA;
import mpi.eudico.client.annotator.commands.global.ExportToolBoxMultiMA;
import mpi.eudico.client.annotator.commands.global.ExportWordsMultiMA;
import mpi.eudico.client.annotator.commands.global.FASTSearchMA;
import mpi.eudico.client.annotator.commands.global.FontBrowserMA;
import mpi.eudico.client.annotator.commands.global.HelpMA;
import mpi.eudico.client.annotator.commands.global.ImportCHATMA;
import mpi.eudico.client.annotator.commands.global.ImportDelimitedTextMA;
import mpi.eudico.client.annotator.commands.global.ImportFlexMA;
import mpi.eudico.client.annotator.commands.global.ImportFlexMultiMA;
import mpi.eudico.client.annotator.commands.global.ImportPraatMA;
import mpi.eudico.client.annotator.commands.global.ImportPraatMultiMA;
import mpi.eudico.client.annotator.commands.global.ImportRecognizerTiersMA;
import mpi.eudico.client.annotator.commands.global.ImportShoeboxMA;
import mpi.eudico.client.annotator.commands.global.ImportToolboxMA;
import mpi.eudico.client.annotator.commands.global.ImportToolboxMultiMA;
import mpi.eudico.client.annotator.commands.global.ImportTranscriberMA;
import mpi.eudico.client.annotator.commands.global.MenuAction;
import mpi.eudico.client.annotator.commands.global.MergeTranscriptionsMA;
import mpi.eudico.client.annotator.commands.global.MultiEAFCreationMA;
import mpi.eudico.client.annotator.commands.global.MultiEAFScrubberMA;
import mpi.eudico.client.annotator.commands.global.MultiFileAnnotationsFromOverlapsMA;
import mpi.eudico.client.annotator.commands.global.MultiFileAnnotationsFromSubtractionMA;
import mpi.eudico.client.annotator.commands.global.MultiFindReplaceMA;
import mpi.eudico.client.annotator.commands.global.MultipleFileMergeTiersMA;
import mpi.eudico.client.annotator.commands.global.MultipleFilesEditMA;
import mpi.eudico.client.annotator.commands.global.NewMA;
import mpi.eudico.client.annotator.commands.global.NextWindowMA;
import mpi.eudico.client.annotator.commands.global.NgramStatisticsMA;
import mpi.eudico.client.annotator.commands.global.OpenMA;
import mpi.eudico.client.annotator.commands.global.PrevWindowMA;
import mpi.eudico.client.annotator.commands.global.SearchMultipleMA;
import mpi.eudico.client.annotator.commands.global.SetLocaleMA;
import mpi.eudico.client.annotator.commands.global.SetPlayAroundSelectionMA;
import mpi.eudico.client.annotator.commands.global.SetPlaybackToggleMA;
import mpi.eudico.client.annotator.commands.global.ShortcutsMA;
import mpi.eudico.client.annotator.commands.global.ShowLogMA;
import mpi.eudico.client.annotator.commands.global.StatisticsMultipleFilesMA;
import mpi.eudico.client.annotator.commands.global.StructuredSearchMultipleMA;
import mpi.eudico.client.annotator.commands.global.UpdateElanMA;
import mpi.eudico.client.annotator.commands.global.UpdateMultiForECVMA;
import mpi.eudico.client.annotator.commands.global.WebMA;
import mpi.eudico.client.annotator.gui.ElanMenuItem;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.linkedmedia.LinkedFileDescriptorUtil;
import mpi.eudico.client.annotator.linkedmedia.MediaDescriptorUtil;
import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesMenuItem;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.FrameConstants;
import mpi.eudico.client.annotator.util.MonitoringLogger;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.util.TranscriptionECVLoader;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clom.TranscriptionStore;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.ACMTranscriptionStore;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

/**
 *
 * The main ELAN window class.
 */
@SuppressWarnings("serial")
public class ElanFrame2 extends JFrame implements ActionListener,
    ElanLocaleListener, FrameConstants, PreferencesUser {
	/** a list of actions for menu containers and a few transcription independent 
	 * actions */
    protected Map<String, MenuAction> menuActions = new HashMap<String, MenuAction>();
  
    /** a map for actions that are added to the menubar's action map
     * this storage is needed for detaching and re-attaching actions to the menu bar */
    protected Map<Object, Action> registeredActions = new HashMap<Object, Action>();
    
    protected boolean initialized = false;
    public final int WINDOW_POS_MARGIN = 30;
    protected String applicationName = "ELAN " + ELAN.getVersionString();
    
    // load some keybindings for the Mac
    static {
        if (System.getProperty("os.name").indexOf("Mac") > -1) {
            JTextComponent.KeyBinding[] bind = new JTextComponent.KeyBinding[] {
                    new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
                            KeyEvent.VK_C,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                        DefaultEditorKit.copyAction),
                    new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
                            KeyEvent.VK_X,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                        DefaultEditorKit.cutAction),
                    new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
                            KeyEvent.VK_V,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                        DefaultEditorKit.pasteAction),
                    new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(
                            KeyEvent.VK_A,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                        DefaultEditorKit.selectAllAction)
                };

            JTextComponent comp = new JTextField();
            Keymap map = JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);
            JTextComponent.loadKeymap(map, bind, comp.getActions());
        }
    }

    protected Transcription transcriptionForThisFrame;
    protected JMenuBar menuBar;
    protected JMenu menuFile;
    protected JMenuItem menuItemFileNew;
    protected JMenuItem menuItemFileOpen;
    protected JMenu menuRecentFiles;
    protected JMenuItem menuItemFileExit;
    protected JMenu menuBackup;
    protected JMenu menuImport;
    protected JMenu menuExport;
    protected JMenu menuExportSMIL;
    protected JMenuItem menuItemShoeboxImport;
    protected JMenuItem menuItemCHATImport;
    protected JMenuItem menuItemTranscriberImport;
    protected JMenu menuEdit;
    private JMenu menuPreferences;
    private ElanMenuItem setAuthorMI;
    private JMenu menuAnnotation;
    private JMenu menuTier;
    private JMenu menuType;
    private JMenu menuWindow;
    private ButtonGroup windowsGroup;
    private ButtonGroup waveFormGroup;
    private JMenu menuSearch;
    private JMenu menuView;
    private JMenu menuViewer;
    private JMenu menuOptions;
    private JCheckBoxMenuItem menuItemNativeMedia;
    private JCheckBoxMenuItem menuMacNativeLF;
    private JRadioButtonMenuItem menuItemAnnoMode;
    private JRadioButtonMenuItem menuItemSyncMode;
    private JRadioButtonMenuItem menuItemTranscMode; 
    private JRadioButtonMenuItem menuItemSegmentMode; 
    private JRadioButtonMenuItem menuItemInterLinearMode;
    private JCheckBoxMenuItem menuItemKioskMode;
    private JMenuItem menuItemPlayAround;
    private JMenuItem menuItemRateVol;
    protected JMenu menuHelp;
    private JMenu menuFrameLength;
    private JMenu menuAppLanguage;
    private ButtonGroup languageBG;
    private JMenu menuChangeTimePropMode;
    private JMenuItem menuItemScrubTrans;
    private JMenu menuMediaPlayer;
    private JMenu menuWaveform;
    protected ElanLayoutManager layoutManager;
    protected ViewerManager2 viewerManager;
    private PlayerViewerMenuManager pvMenuManager;
    private WaveFormViewerMenuManager wfvMenuManager;
    protected boolean fullyInitialized = false;
    protected ElanMenuItem closeMI;
	protected ElanMenuItem saveMI;
	protected ElanMenuItem saveAsMI;
	private ElanMenuItem saveAsTemplateMI;
	private ElanMenuItem saveSelEafMI;
	private ElanMenuItem saveAs2_7MI;
	private ElanMenuItem mergeTransMI;
	private ElanMenuItem pageSetUpMI;
	private ElanMenuItem printPreviewMI;
	private ElanMenuItem printMI;
	protected ElanMenuItem undoMI;
	protected ElanMenuItem redoMI;
	private ElanMenuItem editCVMI;
	private ElanMenuItem editLangListMI;
	private ElanMenuItem editTierSetMI;
	private ElanMenuItem linkedFilesMI;
	private ElanMenuItem importPrefsMI;
	private ElanMenuItem exportPrefsMI;
	private ElanMenuItem searchMI;
	private ElanMenuItem goToMI;
	private ElanMenuItem tierDependenciesMI;
	private ElanMenuItem spreadsheetMI;
	private ElanMenuItem statisticsMI;
	private ElanMenuItem importPraatMI;
	private ElanMenuItem importRecogTiersMI;
	private ElanMenuItem editLexiconServiceMI;
	private ElanMenuItem copyCurrentTimeCodeMI;
	private ElanMenuItem compareAnnotatorsMI;
	
	private JCheckBoxMenuItem menuItemGridViewer;
	private JCheckBoxMenuItem menuItemTextViewer;
	private JCheckBoxMenuItem menuItemSubtitleViewer;
	private JCheckBoxMenuItem menuItemLexiconViewer;
	private JCheckBoxMenuItem menuItemCommentViewer;
	private JCheckBoxMenuItem menuItemRecognizer;
	private JCheckBoxMenuItem menuItemMetaDataViewer;
	private JCheckBoxMenuItem menuItemSignalViewer;
	private JCheckBoxMenuItem menuItemInterLinearViewer;
	private JCheckBoxMenuItem menuItemTimeSeriesViewer;
	private JCheckBoxMenuItem menuItemInterLinearizerViewer;
	private JMenu menuWebservices;

	private RecentLanguagesMenuItem recentLanguagesMenuItem;

	
    /**
     * The no arg constructor creates an empty elan frame containing a menubar
     * with a limited set of menu items.
     */
    public ElanFrame2() {
        // set the initial title
        setTitle(applicationName);
        setFrameIcon();
        initFrame();
        createDnDTarget();
    }

    /**
     * Constructor that accepts the path to an .eaf file. This first creates an
     * empty frame and then calls openEAF to open the specified .eaf file.
     *
     * @param path the location of the eaf file
     *
     * @see #openEAF(String)
     */
    public ElanFrame2(final String path) {
        this();
        
        if (path != null) {
	        SwingUtilities.invokeLater(new Runnable() {
	                @Override
					public void run() {
	                    openEAF(path);
	                }
	            });
        }
    }

    /**
     * Creates a new ElanFrame2 instance
     *
     * @param eafPath DOCUMENT ME!
     * @param mediaFiles DOCUMENT ME!
     */
    public ElanFrame2(final String eafPath, final List<String> mediaFiles) {
        this();
        
        if (eafPath != null) {
	        SwingUtilities.invokeLater(new Runnable() {
	                @Override
					public void run() {
	                    openEAF(eafPath, mediaFiles);
	                }
	            });
        }
    }

    /**
     * Constructor that accepts a Transcription object to use for the current
     * ElanFrame.
     *
     * @param transcription the transcription
     */
    public ElanFrame2(Transcription transcription) {
        this();
        transcriptionForThisFrame = transcription;

        // fill the frame if transcription != null
        if (transcriptionForThisFrame != null) {
            try {
                //new InitThread(transcriptionForThisFrame.getName()).start();
                initElan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
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
     * Open an .eaf file defined by a full path string
     * Nov 2006: changed access from private to package
     * @param fullPath
     * @param mediaFiles a vector containing associated media files
     */
    void openEAF(String fullPath, List<String> mediaFiles) {
        File fileTemp = new File(fullPath);

        //check if file exists and is a file
        if (!fileTemp.exists() || fileTemp.isDirectory()) {
            String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
            strMessage += fullPath;
            strMessage += ElanLocale.getString("Menu.Dialog.Message2");

            String strError = ElanLocale.getString("Message.Error");
            JOptionPane.showMessageDialog(this, strMessage, strError,
                JOptionPane.ERROR_MESSAGE);

            return;
        }
        
        //check if file is a media File
        String lowerPath = fileTemp.toString().toLowerCase(); 
        if(isMediaFile(lowerPath)){     
        	 String strMessage = fullPath;
             strMessage += ElanLocale.getString("Menu.Dialog.Message4");           

             String strWarning = ElanLocale.getString("Message.Warning");
             
        	int i = JOptionPane.showOptionDialog(this, strMessage, strWarning, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
       		if(i == JOptionPane.YES_OPTION){
       			if(menuItemFileNew.getAction() instanceof NewMA){
       				List<String> v = new ArrayList<String>();
       				v.add(fullPath);
       				((NewMA)menuItemFileNew.getAction()).createNewFile(v);
       			}
       			return;
       		} 
       	}
       
        //check if file is a '.eaf' file
        if (!lowerPath.endsWith(".eaf") && !lowerPath.endsWith(".etf")) {
            String strMessage = ElanLocale.getString("Menu.Dialog.Message1");
            strMessage += fullPath;
            strMessage += ElanLocale.getString("Menu.Dialog.Message3");

            String strError = ElanLocale.getString("Message.Error");
            JOptionPane.showMessageDialog(this, strMessage, strError,
                JOptionPane.ERROR_MESSAGE);

            return;
        }

        //open the eaf/etf and get the Transcription from it
        try {
            // When config files are possible check if eaf or configuration file
            //           String path = chooser.getSelectedFile().getAbsolutePath();
            //String path = fullPath;
			String path = fileTemp.getAbsolutePath();
            // replace all backslashes by forward slashes
            path = path.replace('\\', '/');

            //long before = System.currentTimeMillis();
            TranscriptionImpl transcription = new TranscriptionImpl(new File(path).getAbsolutePath());
            //long after = System.currentTimeMillis();
            //System.out.println("open eaf took " + (after - before) + " ms");
            transcription.setUnchanged();
            
	        if (lowerPath.endsWith(".etf")) {
                transcription.setName(TranscriptionImpl.UNDEFINED_FILE_NAME);

                // HS nov 2006: set the pathname to undefined too instead of
                // the path to the template
                transcription.setPathName(TranscriptionImpl.UNDEFINED_FILE_NAME);
                transcription.setChanged();
                // load template preferences
            	String prefPath = path.substring(0, path.length() - 3) + "pfsx";
            	try {
            		File pFile = new File(prefPath);
            		if (pFile.exists()) {
            			Preferences.importPreferences(transcription, prefPath);
            		}
            	} catch (Exception ex) {// catch any exception and continue
            		
            	}
            }
	        // Perhaps here set the CV language of the transcription?
	        // (Note that it is a global setting)
            String pref = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
            if (pref != null) {
                transcription.updateCVLanguage(pref, false);
            } 
            // in case of eaf and etf files load external controlled vocabularies
            if (transcription.getControlledVocabularies().size() > 0) {
            	new TranscriptionECVLoader().loadExternalCVs(transcription, this);// this can set the changed flag
            }
            // hier.. load a set of lexicon client factories? or delay until needed?
            
            if (mediaFiles != null) {
                List<MediaDescriptor> descriptors = MediaDescriptorUtil.createMediaDescriptors(mediaFiles);

                // improve this; check and compare with the medianames from the eaf
                transcription.setMediaDescriptors(descriptors);
                transcription.setChanged();
            }

            String eafPath = FileUtility.directoryFromPath(path);
            boolean validMedia = checkMedia(transcription, eafPath);

            if (!validMedia) {
                // ask if incomplete media session is ok, if not return
                int answer = JOptionPane.showConfirmDialog(this,
                        ElanLocale.getString(
                            "Frame.ElanFrame.IncompleteMediaQuestion"),
                        ElanLocale.getString(
                            "Frame.ElanFrame.IncompleteMediaAvailable"),
                        JOptionPane.YES_NO_OPTION);

                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            if (transcriptionForThisFrame != null) {
                // create a new ElanFrame for the Transcription
                //new ElanFrame2(transcription);
            	FrameManager.getInstance().createFrame(transcription);
            } else {
                transcriptionForThisFrame = transcription;

                //new InitThread(transcriptionForThisFrame.getName()).start();
                initElan();
                
                FrameManager.getInstance().updateFrameTitle(this, 
                		transcription.getPathName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fullPath DOCUMENT ME!
     */
    public void openEAF(String fullPath) {
        openEAF(fullPath, null);
    }
    
    /**
     * Sets the transcription (document) for this frame. Only effective if this 
     * frame is an empty frame, if there already is an transcription this 
     * method simply returns.
     * 
     * @param transcription the transcription object
     */
    protected void setTranscription(Transcription transcription) {
    	if (transcriptionForThisFrame != null || 
    			!(transcription instanceof TranscriptionImpl)) {
    		return;
    	}
    	transcriptionForThisFrame = transcription;
    	
    	initElan();
    }

    /**
     * Returns the viewermanager for this frame
     *
     * @return returns the viewer manager
     */
    public ViewerManager2 getViewerManager() {
        return viewerManager;
    }
    
    /**
     * Returns the Elan layout manager.
     * 
     * @return the layout manager
     */
    public ElanLayoutManager getLayoutManager() {
    	return layoutManager;
    }
    
	/**
	 * Set the "proxy icon" in the title bar, if we have a valid file name.
	 */
	private void setProxyIcon() {
        if (SystemReporting.isMacOS()) {
    		File f = null;
        	String fileName = transcriptionForThisFrame.getFullPath();
        	if (fileName != null && 
        		!TranscriptionImpl.UNDEFINED_FILE_NAME
        			.equals(transcriptionForThisFrame.getName())) {
    			fileName = FileUtility.urlToAbsPath(fileName);
    			f = new File(fileName);
    		}
    		getRootPane().putClientProperty("Window.documentFile", f);
		}
	}
	
	/**
	 * Sets the icon for the frame.
	 * Could move the retrieval of the icon to the main class or Constants.
	 */
	protected void setFrameIcon() {
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));
        setIconImage(icon.getImage());
	}

    /**
     * Check for existence of media files and fix URLs if needed and possible.
     * The directory from which the .eaf file came is used as an alternative
     * location for the media file if the absolute path does not exist. When
     * the file is not found the user is prompted with a file chooser to
     * locate the file.
     *
     * @param transcription
     * @param eafPath DOCUMENT ME!
     *
     * @return boolean that flags if the media descriptors are valid
     */
    public boolean checkMedia(Transcription transcription, String eafPath) {
        boolean validMedia = true;
        boolean saveChangedMedia = true;
        
        Boolean boolPref = Preferences.getBool("MediaLocation.AltLocationSetsChanged", null);
        
        if (boolPref != null) {
        	saveChangedMedia = boolPref.booleanValue();
        }
        
        // make sure the eaf path is treated the same way as media files,
        // i.e. it starts with file:/// or file://
        String fullEAFURL = FileUtility.pathToURLString(transcription.getFullPath());

        try {
            List<MediaDescriptor> mediaDescriptors = transcription.getMediaDescriptors();

            for (int i = 0; i < mediaDescriptors.size(); i++) {
                MediaDescriptor md = mediaDescriptors.get(i);

                // remove the file: part of the URL, leading slashes are no problem
                int colonPos = md.mediaURL.indexOf(':');
                // wwj: if the url begins with rtsp, bypass the file checking
                if (colonPos > 0) {
                	    String urlhead = md.mediaURL.substring(0,colonPos);
                	    if ( urlhead.trim().equalsIgnoreCase("rtsp")) {
                		   continue; 
                	    }
                 }
                String fileName = md.mediaURL.substring(colonPos + 1);

                // replace all back slashes by forward slashes
                fileName = fileName.replace('\\', '/');

                File file = new File(fileName);

                if (!file.exists()) {
                    // look for the file in the local directory
                    String localFileName = FileUtility.fileNameFromPath(fileName);
                    file = new File(eafPath + "/" + localFileName);

                    if (file.exists()) {
                        // adjust urls, check the user setting whether this should set the changed flag
                        adjustMediaDescriptors(mediaDescriptors, i,
                            file.getAbsolutePath());
                        ClientLogger.LOG.info("Updated file location from: \"" + fileName + 
                        		"\" to: \"" + file.getAbsolutePath() + "\", in same directory as transcription file");
                        if (saveChangedMedia) {
                        	transcription.setChanged();
                        }

                        continue;
                    }
                    
                    // look in the relative path stored in the mediadescriptor
                    if (md.relativeMediaURL != null) {
                    	String relUrl = md.relativeMediaURL;
                    	if (relUrl.startsWith("file:/")) {
                    		relUrl = relUrl.substring(6);
                    	}
                    	// resolve relative url and check location
                    	String absPath = FileUtility.getAbsolutePath(fullEAFURL, relUrl);
                    	if (absPath != null) {
                    		file = new File(absPath);
                    		if (file.exists()) {
                                adjustMediaDescriptors(mediaDescriptors, i,
                                    file.getAbsolutePath());
                                ClientLogger.LOG.info("Updated file location from: \"" + fileName + 
                                		"\" to: \"" + file.getAbsolutePath() + "\", by resolving relative path");
                                if (saveChangedMedia) {
                                	transcription.setChanged();
                                }
                                continue;
                    		}
                    	}
                    }
                    
                    // look in a relative path ../Media
                    file = new File(eafPath + "/../Media/" + localFileName);

                    if (file.exists()) {
                        // adjust urls
                        adjustMediaDescriptors(mediaDescriptors, i,
                            file.getAbsolutePath());
                        ClientLogger.LOG.info("Updated file location from: \"" + fileName + 
                        		"\" to: \"" + file.getAbsolutePath() + "\", in Media subdirectory");
                        if (saveChangedMedia) {
                        	transcription.setChanged();
                        }

                        continue;
                    }

                    // look in a relative path ../media
                    file = new File(eafPath + "/../media/" + localFileName);

                    if (file.exists()) {
                        // adjust urls
                        adjustMediaDescriptors(mediaDescriptors, i,
                            file.getAbsolutePath());
                        ClientLogger.LOG.info("Updated file location from: \"" + fileName + 
                        		"\" to: \"" + file.getAbsolutePath() + "\", in media subdirectory");
                        if (saveChangedMedia) {
                        	transcription.setChanged();
                        }
                        continue;
                    }
                    // Dec 2008 check a user definable preferred location
                    String stringPref = Preferences.getString("DefaultMediaLocation", null);
                    
                    if (stringPref != null) {
                    	file = new File(FileUtility.urlToAbsPath(stringPref) + "/" + localFileName);
                    	
                        if (file.exists()) {
                            // adjust urls
                            adjustMediaDescriptors(mediaDescriptors, i,
                                file.getAbsolutePath());
                            ClientLogger.LOG.info("Updated file location from: \"" + fileName + 
                            		"\" to: \"" + file.getAbsolutePath() + "\", in preferred media location");
                            if (saveChangedMedia) {
                            	transcription.setChanged();
                            }

                            continue;
                        }
                    }
                    
                    // no fallback worked, prompt the user to locate the file
                    FileChooser chooser = new FileChooser(this);
                    ArrayList<String[]> extensions = new ArrayList<String[]>();
                    String[] mainExt;

                    if (md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {                    	
                    	mainExt = FileExtension.WAV_EXT;
                    } else if (md.mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
                    	mainExt = FileExtension.MPEG_EXT;                    	
                    }else if (md.mimeType.equals(MediaDescriptor.MP4_MIME_TYPE)) {
                    	mainExt = FileExtension.MPEG4_EXT;                    	
                    } else if (md.mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE)) {
                    	mainExt = FileExtension.QT_EXT;                    	
                    }else {
                    	extensions.add(FileExtension.MEDIA_EXT);
                    	extensions.add(FileExtension.MPEG_EXT);
                    	extensions.add(FileExtension.WAV_EXT);
                    	extensions.add(FileExtension.MPEG4_EXT);
                    	extensions.add(FileExtension.QT_EXT);
                		mainExt = null;
                    }                   
                    
                    // Keep asking for a file until ok
                    for (;;) {
	                    // set directory to last used media file or to the location of the eaf
	                    chooser.createAndShowFileDialog(
	                    		ElanLocale.getString("Frame.ElanFrame.LocateMedia") + ": " + localFileName,
	                    		FileChooser.OPEN_DIALOG, null, 
	                    		extensions, mainExt, true, "MediaDir", FileChooser.FILES_ONLY, file.getName());
	
	                    if (chooser.getSelectedFile() == null) {
	                    	// the user did choose cancel and thereby gave up locating the file
	                    	md.isValid = false;
	                    	validMedia = false;
	                    	break;
	                    }
                    	String absolutePath = chooser.getSelectedFile().getAbsolutePath();
                    	
                    	if (linkingTheSameMediaTwice(mediaDescriptors, absolutePath)) {
                    		continue;	// ask again
                    	}

                    	// Check if the file looks like a plausible replacement
                    	if (!plausiblyTheSameFile(localFileName, absolutePath)) {
                    		continue;	// ask again
                    	}

                		// adjust urls
                		adjustMediaDescriptors(mediaDescriptors, i,	absolutePath);
                		//if (saveChangedMedia) {// in this case prompt for save
                		transcription.setChanged();
                		//}
                		break;
                	}             	   
                }
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return validMedia;
    }

   /**
    * Check if the replacement media happens to be linked already. 
    * @param mediaDescriptors of the existing media
    * @param absolutePath of the new media
    * @return true if already linked.
    */
   private boolean linkingTheSameMediaTwice(List<MediaDescriptor> mediaDescriptors, String absolutePath) {
       String newURL = FileUtility.pathToURLString(absolutePath);

       for (MediaDescriptor md : mediaDescriptors) {
    	   if (md.mediaURL.equals(newURL)) {
    			JOptionPane.showMessageDialog(this,
    					ElanLocale.getString("LinkedFilesDialog.Message.AlreadyLinked"),
    					ElanLocale.getString("Message.Warning"),
    					JOptionPane.ERROR_MESSAGE);
    		   return true;
    	   }
       }
       return false;
	}

    /**
     * Check if it is plausible that the replacement file corresponds to the original file.
     * If there is doubt, ask the user to confirm (s)he is sure.
     * Doubt is created when the file name differs and when the extension differs.
     */
    private boolean plausiblyTheSameFile(String original, String replacement) {
    	String fn1 = FileUtility.fileNameFromPath(original);
    	String fn2 = FileUtility.fileNameFromPath(replacement);

    	if (!FileUtility.sameNameIgnoreExtension(fn1, fn2)) {
    		String fmt = ElanLocale.getString("Frame.ElanFrame.FileNamesDiffer");
    		String text = String.format(fmt, fn1, fn2);
    		boolean ok = showConfirmDialog(text);
    		if (!ok) {
				return false;
			}
    	}
    	
    	String e1 = FileUtility.getExtension(fn1, "");
    	String e2 = FileUtility.getExtension(fn2, "");
    	
    	if (!e1.equals(e2)) {
    		String fmt = ElanLocale.getString("Frame.ElanFrame.FileExtensionsDiffer");
    		String text = String.format(fmt, e1, e2);
    		boolean ok = showConfirmDialog(text);
  
    	    return ok;
    	}

    	return true;
	}

    /**
     * Shows a yes-no option dialog with the specified question.
     *
     * @param question the question
     *
     * @return true if the user's answer is affirmative, false otherwise
     */
    private boolean showConfirmDialog(String question) {        
        int answer = JOptionPane.showConfirmDialog(this,
        		question,
                ElanLocale.getString(
                    "Frame.ElanFrame.IncompleteMediaAvailable"),
                JOptionPane.YES_NO_OPTION);

        return answer == JOptionPane.YES_OPTION;
    }

	/**
     * Replace the current path in a media descriptor and all the media
     * descriptors that are derived from it.
     *
     * @param mediaDescriptors
     * @param index position in the List of descriptors of the descriptor
     *        which media URL must be changed
     * @param newPath the new file path that must become the new media URL
     */
    private void adjustMediaDescriptors(List<MediaDescriptor> mediaDescriptors, int index,
        String newPath) {
        // remember the old URL
        String oldURL = mediaDescriptors.get(index).mediaURL;

        String newURL = FileUtility.pathToURLString(newPath);
        String newExt = null;

        if (newURL.indexOf('.') > -1) {
            newExt = newURL.substring(newURL.lastIndexOf('.') + 1);
        }

        // replace the old URL and mime type
        mediaDescriptors.get(index).mediaURL = newURL;
        mediaDescriptors.get(index).mimeType = MediaDescriptorUtil.mimeTypeForExtension(newExt);

        // replace the extracted from URL's
        for (int i = 0; i < mediaDescriptors.size(); i++) {
            String extractedFrom = mediaDescriptors.get(i).extractedFrom;

            if (oldURL.equals(extractedFrom)) {
                mediaDescriptors.get(i).extractedFrom = newURL;
            }
        }
    }

    /**
     * Init Elan for a Transcription mediaDecriptors should be contained in
     * Transcription Is this the place to create all the viewers, must there
     * be getters for these viewers who needs to know about them?
     */
    protected void initElan() {
    	if(MonitoringLogger.isInitiated()){            	
        	MonitoringLogger.getLogger(null).log(MonitoringLogger.OPEN_FILE, transcriptionForThisFrame.getName());	
        	MonitoringLogger.getLogger(transcriptionForThisFrame).log(MonitoringLogger.OPEN_FILE);
        }
    	
        setTitle("Initializing....");
        // before creating viewers apply preferences to controlled vocabularies
        loadCVPreferences();   
       
        viewerManager = new ViewerManager2((TranscriptionImpl)transcriptionForThisFrame);  
        layoutManager = new ElanLayoutManager(this, viewerManager);        
        ELANCommandFactory.addDocument(this, viewerManager, layoutManager); 
        
        pvMenuManager = new PlayerViewerMenuManager(this, transcriptionForThisFrame);
        //long time = System.currentTimeMillis();
        //System.out.println("B: " + 0);
        //MediaDescriptorUtil.createMediaPlayers((TranscriptionImpl) transcriptionForThisFrame,
        //    transcriptionForThisFrame.getMediaDescriptors());
        MediaDescriptorUtil.createMediaPlayers((TranscriptionImpl) transcriptionForThisFrame,
        		pvMenuManager.getStoredVisiblePlayers());
        
        wfvMenuManager = new WaveFormViewerMenuManager(this, transcriptionForThisFrame);

        // if there is a signal viewer its mediafile is the first in the list       
        ArrayList<String> audioPaths = new ArrayList<String>(4);   
       	if (layoutManager.getSignalViewer() != null) {
       		audioPaths.add(layoutManager.getSignalViewer().getMediaPath());
       	}        	
      
        if(!menuItemSignalViewer.isSelected()){
        	if (layoutManager.getSignalViewer() != null) {
        		layoutManager.remove(layoutManager.getSignalViewer());        		
        	} 
        	menuWaveform.setEnabled(false);
        }
        
        ArrayList<String> videoPaths = new ArrayList<String>(6);
        // there may be other video files associated with the transcription

        for (MediaDescriptor md : transcriptionForThisFrame.getMediaDescriptors()) {
    		String path = md.mediaURL;
    		path = FileUtility.urlToAbsPath(path);
        	if (md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) ||
        		md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
    			if (!audioPaths.contains(path)) {
        		    audioPaths.add(path);
            	}
			} else {
        		if (!videoPaths.contains(path)) {
        		    videoPaths.add(path);
        		}
			}
        }
        
        viewerManager.setAudioPaths(audioPaths); 
        viewerManager.setVideoPaths(videoPaths);    	
    	
        // initLinkedFiles now also updates the otherMediaPaths of the ViewerManager
        if (transcriptionForThisFrame.getLinkedFileDescriptors().size() > 0) {      
        	LinkedFileDescriptorUtil.initLinkedFiles(
        		(	TranscriptionImpl) transcriptionForThisFrame);
        }

        ElanLocale.addElanLocaleListener(transcriptionForThisFrame, this);
        
        setFrameTitle();
        
        initMenusAndCommands();
        
        pvMenuManager.initPlayerMenu();
        wfvMenuManager.initWaveFormViewerMenu();

        if (viewerManager.getMasterMediaPlayer().isFrameRateAutoDetected()) {
            // disable the menu to change the video standard, i.e. the frame length
            setMenuEnabled(FrameConstants.FRAME_LENGTH, false);
        }       
        
		int mode = ElanLayoutManager.NORMAL_MODE;
		Integer lastMode = Preferences.getInt("LayoutManager.CurrentMode", viewerManager.getTranscription()); 
		if(lastMode != null && mode != lastMode.intValue()){
			// try to switch to the lastMode
			layoutManager.changeMode(lastMode);	
			if(lastMode == layoutManager.getMode()){
				// update the changes in the menu, if the mode is switched to the new one
				setMenuSelected(layoutManager.getModeConstant(lastMode), FrameConstants.OPTION);
				mode = lastMode;
			}
		} 	
		
		if(mode == ElanLayoutManager.NORMAL_MODE){
			layoutManager.changeMode(mode);
		}		
				
		Preferences.addPreferencesListener(transcriptionForThisFrame, layoutManager);
	    Preferences.addPreferencesListener(transcriptionForThisFrame, this);
	    Preferences.notifyListeners(transcriptionForThisFrame);
		
        // a few prefs to load only on load
        loadPreferences();

        // instantiate Viewer components:
        //  use CommandActions for relevant toolbar buttons and combobox menu items
        //  position and size components using LayoutManager
        layoutManager.doLayout();

        //  this sucks but is needed to ensure visible video on the mac
        //viewerManager.getMasterMediaPlayer().setMediaTime(0);
        initialized = true;
    }

    public void setFrameTitle() {
        try {
            if (transcriptionForThisFrame != null) {
                if (transcriptionForThisFrame.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                    setTitle(applicationName + " - " +
                        ElanLocale.getString(
                            "Frame.ElanFrame.UndefinedFileName"));
                } else {
                    setTitle(applicationName + " - " + transcriptionForThisFrame.getName());
                }
                setProxyIcon();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs some general initialization for an empty frame.
     */
    protected void initFrame() {
    	Boolean boolPref = Preferences.getBool("ActivityMonitoring.AlwaysStartMonitoring", null);
    	if (boolPref != null) {
    		if (boolPref) {
    			MonitoringLogger.startMonitoring(true);
    		}
    	}
    	
    	//getRootPane().putClientProperty("Window.alpha", new Float(0.50));// on MacOS a window can be (semi) transparent
        Locale savedLocale = (Locale) Preferences.get("Locale", null);

        if (savedLocale != null) {
            ElanLocale.setLocale(savedLocale);
        }
        // create the initial menu items
        initMenuBar();

        // listen for WindowEvents events on the ElanFrame
        ElanFrameWindowListener windowList = new ElanFrameWindowListener();
        addWindowListener(windowList);
        addComponentListener(windowList);

        // require the program to handle the operation in the
        // windowClosing method of a registered WindowListener object.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // add this elanframe to locale listener
        //ElanLocale.addElanLocaleListener(this);
        pack();
        
        updateShortcutMap(null);

        /*
           Dimension dimSize = new Dimension(800, 650);
           setSize((int)dimSize.getWidth(), (int)dimSize.getHeight());
           Dimension dimLocation = Toolkit.getDefaultToolkit().getScreenSize();
           setLocation((int)(dimLocation.getWidth()/2-getWidth()/2),(int)(dimLocation.getHeight()/2-getHeight()/2));
         */
        // HS 2012 re/store dimension per transcription
        Dimension d = Preferences.getDimension("FrameSize", transcriptionForThisFrame);
        
        if (d == null) {
        	d = Preferences.getDimension("FrameSize", null);
        }

//        if (d != null) {
//            setSize(d);
//        } else {
//            Dimension dimSize = new Dimension(800, 650);
//            setSize((int) dimSize.getWidth(), (int) dimSize.getHeight());
//        }
//
//        Point p = Preferences.getPoint("FrameLocation", null);
//
//        if (p != null) {
//            setLocation(p);
//        } else {
//            Dimension dimLocation = Toolkit.getDefaultToolkit().getScreenSize();
//            setLocation((int) ((dimLocation.getWidth() / 2) - (getWidth() / 2)),
//                (int) ((dimLocation.getHeight() / 2) - (getHeight() / 2)));
//        }
        // HS June 2010 correct dimension and location if they don't fit on the screen?
        // or make this a user preference?
        Rectangle wRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        
        if (d != null) {      	
        	if (d.width > wRect.width) {
        		d.setSize(wRect.width, d.height);
        	}
        	if (d.height > wRect.getHeight()) {
        		d.setSize(d.width, wRect.height);
        	}
            setSize(d);
        } else {
        	setSize(800, 600);
        }
        Point p = Preferences.getPoint("FrameLocation", transcriptionForThisFrame);
        
        if (p == null) {
        	p = Preferences.getPoint("FrameLocation", null);
        }

        if (p != null) {
        	// HS 08-2012 only adjust the location if the upper left corner is outside of the 
        	// screen or within some screen inset
        	if (p.x < wRect.x) {
        		p.x = wRect.x;
        	} else if (p.x > wRect.width - WINDOW_POS_MARGIN) {
        		p.x = wRect.width - WINDOW_POS_MARGIN;// or position at left side of the screen?
        	}
//        	int mpx = wRect.width - getWidth();
//        	if (p.x > mpx && mpx > 0) {
//        		p.x = mpx;
//        	}
        	if (p.y < wRect.y) {
        		p.y = wRect.y;
        	} else if (p.y > wRect.height - WINDOW_POS_MARGIN) {
        		p.y = wRect.height - WINDOW_POS_MARGIN;
        	}
//        	int mph = wRect.height - getHeight();
//        	if (p.y > mph && mph > 0) {
//        		p.y = mph;
//        	}
            setLocation(p);
        } else {
        	setLocation((int) ((wRect.getWidth() / 2) - (getWidth() / 2)), 
        			(int) ((wRect.getHeight() / 2) - (getHeight() / 2)));
        }
        // the call to setVisible should be moved to (a new thread created in) 
        // the class that called a constructor....
        setVisible(true);
    }
    
    /**
     * Enables creation of new transcriptions and opening of existing transcriptions by
     * means of drag and drop of files onto the window.
     */
    protected void createDnDTarget() {
    	DropTarget dropTarget = new DropTarget(this.getContentPane(), new ELANDropTargetListener());
    	// the above works as it is but maybe the target could/should be configured appropriately 
    	dropTarget.setDefaultActions(DnDConstants.ACTION_REFERENCE);
    }
    
    protected void initMenuBar() {
        menuBar = new JMenuBar();

        setJMenuBar(menuBar);

        //make menu visible / appear above heavyweight video
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        // HS 07-2009 make a complete menu layout except for Annotation, Tier and Type menu.
        // add Actions for most menu items later in initMenusAndCommands()
        MenuAction ma;
        ma = new MenuAction("Menu.File");
        menuFile = new JMenu(ma);
        menuActions.put("Menu.File", ma);
        menuBar.add(menuFile);

        ma = new NewMA(ELANCommandFactory.NEW_DOC, this);
        menuActions.put(ELANCommandFactory.NEW_DOC,ma);
        menuItemFileNew = new JMenuItem(ma);
        menuFile.add(menuItemFileNew);

        ma = new OpenMA(ELANCommandFactory.OPEN_DOC, this);
        menuActions.put(ELANCommandFactory.OPEN_DOC,ma);
        menuItemFileOpen = new JMenuItem(ma);
        menuFile.add(menuItemFileOpen);

        ma = new MenuAction("Menu.File.OpenRecent");
        menuActions.put("Menu.File.OpenRecent",ma);
        menuRecentFiles = new JMenu(ma);
        menuFile.add(menuRecentFiles);
        
        ma = new MenuAction(ELANCommandFactory.CLOSE);
        closeMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.CLOSE, ma);
        menuFile.add(closeMI);
		menuFile.addSeparator();
       
		ma = new MenuAction(ELANCommandFactory.SAVE);
		saveMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.SAVE, ma);
		menuFile.add(saveMI);
		
		ma = new MenuAction(ELANCommandFactory.SAVE_AS);
		saveAsMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.SAVE_AS, ma);
		menuFile.add(saveAsMI);
        
		ma = new MenuAction(ELANCommandFactory.SAVE_AS_TEMPLATE);
        saveAsTemplateMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.SAVE_AS_TEMPLATE, ma);
        menuFile.add(saveAsTemplateMI);
		
        ma = new MenuAction(ELANCommandFactory.SAVE_SELECTION_AS_EAF);
		saveSelEafMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.SAVE_SELECTION_AS_EAF, ma);
        menuFile.add(saveSelEafMI);
        
        ma = new MenuAction(ELANCommandFactory.EXPORT_EAF_2_7);
		saveAs2_7MI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.EXPORT_EAF_2_7, ma);
        menuFile.add(saveAs2_7MI);

        menuFile.addSeparator();
        
        ma = new MergeTranscriptionsMA(ELANCommandFactory.MERGE_TRANSCRIPTIONS, this);
        menuActions.put(ELANCommandFactory.MERGE_TRANSCRIPTIONS,ma);
        mergeTransMI = new ElanMenuItem(ma);
		menuFile.add(mergeTransMI);
		
        ma = new MenuAction("Menu.File.Backup.Auto");
        menuActions.put("Menu.File.Backup.Auto",ma);
        menuBackup = new JMenu(ma);
        menuBackup.setEnabled(false);
        menuFile.add(menuBackup);
        menuFile.addSeparator();
        // hier met menu actions
        ma = new MenuAction(ELANCommandFactory.PAGESETUP);
        menuActions.put(ELANCommandFactory.PAGESETUP, ma);
        pageSetUpMI = new ElanMenuItem(ma, false);
		menuFile.add(pageSetUpMI);
		ma = new MenuAction(ELANCommandFactory.PREVIEW);
		printPreviewMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.PREVIEW, ma);
		menuFile.add(printPreviewMI);
		ma = new MenuAction(ELANCommandFactory.PRINT);
		printMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.PRINT, ma);
		menuFile.add(printMI);
		menuFile.addSeparator();
		
		ma = new MenuAction("Menu.File.ProcessMulti");
		menuActions.put("Menu.File.ProcessMulti",ma);
		JMenu mfProcessMenu = new JMenu(ma);
		menuFile.add(mfProcessMenu);

		ma = new MultiEAFCreationMA(ELANCommandFactory.CREATE_NEW_MULTI, this);
		menuActions.put(ELANCommandFactory.CREATE_NEW_MULTI, ma);
		mfProcessMenu.add(new JMenuItem(ma));
		
		ma = new MultipleFilesEditMA(ELANCommandFactory.EDIT_MULTIPLE_FILES, this);
		menuActions.put(ELANCommandFactory.EDIT_MULTIPLE_FILES, ma);
		mfProcessMenu.add(new JMenuItem(ma));
        
        ma = new MultiEAFScrubberMA(ELANCommandFactory.SCRUB_MULTIPLE_FILES, this);
        menuActions.put(ELANCommandFactory.SCRUB_MULTIPLE_FILES, ma);
        menuItemScrubTrans = new JMenuItem(ma);
        mfProcessMenu.add(menuItemScrubTrans);
        
        ma = new UpdateMultiForECVMA(ELANCommandFactory.UPDATE_TRANSCRIPTIONS_FOR_ECV, this);
		menuActions.put(ELANCommandFactory.UPDATE_TRANSCRIPTIONS_FOR_ECV, ma);
		mfProcessMenu.add(new JMenuItem(ma));
        
        ma = new ClipMediaMultiMA(ELANCommandFactory.CLIP_MEDIA_MULTI, this);
        menuActions.put(ELANCommandFactory.CLIP_MEDIA_MULTI, ma);
        mfProcessMenu.add(new JMenuItem(ma));
        mfProcessMenu.addSeparator();
        
        ma = new MultiFileAnnotationsFromOverlapsMA(ELANCommandFactory.ANNOTATION_OVERLAP_MULTI, this);
        menuActions.put(ELANCommandFactory.ANNOTATION_OVERLAP_MULTI, ma);
        JMenuItem menuItemMultipleFileAnnotationsFromOverlaps = new JMenuItem(ma);
        mfProcessMenu.add(menuItemMultipleFileAnnotationsFromOverlaps);
        
        ma = new MultiFileAnnotationsFromSubtractionMA(ELANCommandFactory.ANNOTATION_SUBTRACTION_MULTI, this);
        menuActions.put(ELANCommandFactory.ANNOTATION_SUBTRACTION_MULTI, ma);
       // JMenuItem menuItemMultipleFileAnnotationsFromOverlaps = new JMenuItem(ma);
        mfProcessMenu.add(new JMenuItem(ma));
        
        ma = new MultipleFileMergeTiersMA(ELANCommandFactory.MERGE_TIERS_MULTI, this);
        menuActions.put(ELANCommandFactory.MERGE_TIERS_MULTI, ma);
        mfProcessMenu.add(new JMenuItem(ma));
        mfProcessMenu.addSeparator();
        
        ma = new AnnotatorCompareMA(ELANCommandFactory.ANNOTATOR_COMPARE_MULTI, this);
        menuActions.put(ELANCommandFactory.ANNOTATOR_COMPARE_MULTI, ma);
        compareAnnotatorsMI = new ElanMenuItem(ma);
        mfProcessMenu.add(compareAnnotatorsMI);
        
        ma = new StatisticsMultipleFilesMA(ELANCommandFactory.STATISTICS_MULTI, this);
        menuActions.put(ELANCommandFactory.STATISTICS_MULTI, ma);
        mfProcessMenu.add(new JMenuItem(ma));
        
        ma = new NgramStatisticsMA(ELANCommandFactory.NGRAMSTATS_MULTI, this);
        menuActions.put(ELANCommandFactory.NGRAMSTATS_MULTI, ma);
        mfProcessMenu.add(new JMenuItem(ma));
		
		menuFile.addSeparator();
		
        ma = new MenuAction("Menu.File.Export");
        menuActions.put("Menu.File.Export", ma);
        menuExport = new JMenu(ma);
        menuExport.setEnabled(false);
        menuFile.add(menuExport);
        
        ma = new MenuAction("Menu.File.Export.MultipleFiles");
        menuActions.put("Menu.File.Export.MultipleFiles", ma);
        JMenu exportMenuMulti = new JMenu(ma);
        menuFile.add(exportMenuMulti);   
        
        ma = new ExportToolBoxMultiMA(ELANCommandFactory.EXPORT_TOOLBOX_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_TOOLBOX_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportFlexMultiMA(ELANCommandFactory.EXPORT_FLEX_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_FLEX_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportPraatMultiMA(ELANCommandFactory.EXPORT_PRAAT_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_PRAAT_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportTabMultiMA(ELANCommandFactory.EXPORT_TAB_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_TAB_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportAnnotationsMultiMA(ELANCommandFactory.EXPORT_ANNLIST_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_ANNLIST_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportWordsMultiMA(ELANCommandFactory.EXPORT_WORDLIST_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_WORDLIST_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportTiersMA(ELANCommandFactory.EXPORT_TIERS_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_TIERS_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportOverlapsMultiMA(ELANCommandFactory.EXPORT_OVERLAPS_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_OVERLAPS_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new ExportThemeMultiMA(ELANCommandFactory.EXPORT_THEME_MULTI, this);
        menuActions.put(ELANCommandFactory.EXPORT_THEME_MULTI, ma);
        exportMenuMulti.add(new JMenuItem(ma));
        
        ma = new MenuAction("Menu.File.Import");
        menuActions.put("Menu.File.Import", ma);
        menuImport = new JMenu(ma);
        menuFile.add(menuImport);

        ma = new ImportToolboxMA(ELANCommandFactory.IMPORT_TOOLBOX, this);
        menuActions.put(ELANCommandFactory.IMPORT_TOOLBOX, ma);
        JMenuItem toolboxImportMI = new JMenuItem(ma);
        menuImport.add(toolboxImportMI);

        ma = new ImportFlexMA(ELANCommandFactory.IMPORT_FLEX, this);
        menuActions.put(ELANCommandFactory.IMPORT_FLEX, ma);
        JMenuItem flexImportMI = new JMenuItem(ma);
        menuImport.add(flexImportMI);
        
        ma = new ImportCHATMA(ELANCommandFactory.IMPORT_CHAT, this);
        menuActions.put(ELANCommandFactory.IMPORT_CHAT, ma);
        menuItemCHATImport = new JMenuItem(ma);
        menuImport.add(menuItemCHATImport);

        ma = new ImportTranscriberMA(ELANCommandFactory.IMPORT_TRANS, this);
        menuActions.put(ELANCommandFactory.IMPORT_TRANS, ma);
        menuItemTranscriberImport = new JMenuItem(ma);
        menuImport.add(menuItemTranscriberImport);

        ma = new ImportDelimitedTextMA(ELANCommandFactory.IMPORT_TAB, this);
        menuActions.put(ELANCommandFactory.IMPORT_TAB, ma);
        menuImport.add(new JMenuItem(ma));
        
        ma = new ImportPraatMA(ELANCommandFactory.IMPORT_PRAAT_GRID, this);
        importPraatMI = new ElanMenuItem(ma);
        menuActions.put(ELANCommandFactory.IMPORT_PRAAT_GRID, ma);
        menuImport.add(importPraatMI);
        
        ma = new ImportRecognizerTiersMA(ELANCommandFactory.IMPORT_RECOG_TIERS, this);
        importRecogTiersMI = new ElanMenuItem(ma);
        menuActions.put(ELANCommandFactory.IMPORT_RECOG_TIERS, ma);
		menuImport.add(importRecogTiersMI);
		
        
        ma = new ImportShoeboxMA(ELANCommandFactory.IMPORT_SHOEBOX, this);
        menuActions.put(ELANCommandFactory.IMPORT_SHOEBOX, ma);
        menuItemShoeboxImport = new JMenuItem(ma);
        menuImport.add(menuItemShoeboxImport);
        
        ma = new MenuAction("Menu.File.Import.MultipleFiles");
        menuActions.put("Menu.File.Import.MultipleFiles", ma);
        JMenu importMenuMulti = new JMenu(ma);
        menuFile.add(importMenuMulti); 
        
        ma = new ImportToolboxMultiMA(ELANCommandFactory.IMPORT_TOOLBOX_MULTI, this);
        menuActions.put(ELANCommandFactory.IMPORT_TOOLBOX_MULTI, ma);
        importMenuMulti.add(new JMenuItem(ma));      
        
        ma = new ImportPraatMultiMA(ELANCommandFactory.IMPORT_PRAAT_GRID_MULTI, this);
        menuActions.put(ELANCommandFactory.IMPORT_PRAAT_GRID_MULTI, ma);
        importMenuMulti.add(new JMenuItem(ma));      
        
        ma = new ImportFlexMultiMA(ELANCommandFactory.IMPORT_FLEX_MULTI, this);
        menuActions.put(ELANCommandFactory.IMPORT_FLEX_MULTI, ma);
        importMenuMulti.add(new JMenuItem(ma));      
        
        menuFile.addSeparator();

        ma = new ExitMA(ELANCommandFactory.EXIT);
        menuActions.put(ELANCommandFactory.EXIT, ma);
        menuItemFileExit = new JMenuItem(ma);
        menuFile.add(menuItemFileExit);

        ma = new MenuAction("Menu.Edit");
        menuActions.put("Menu.Edit", ma);
        menuEdit = new JMenu(ma);
        menuBar.add(menuEdit); 
 
        ma = new MenuAction(ELANCommandFactory.UNDO);
        undoMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.UNDO, ma);
        menuEdit.add(undoMI);
        ma = new MenuAction(ELANCommandFactory.REDO);
        redoMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.REDO, ma);
		menuEdit.add(redoMI);
        menuEdit.addSeparator();
        ma = new MenuAction(ELANCommandFactory.COPY_CURRENT_TIME);
        copyCurrentTimeCodeMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.COPY_CURRENT_TIME, ma);
        menuEdit.add(copyCurrentTimeCodeMI);
        ma = new MenuAction(ELANCommandFactory.EDIT_CV_DLG);
        editCVMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.EDIT_CV_DLG, ma);
		menuEdit.add(editCVMI);	
		ma = new MenuAction(ELANCommandFactory.SET_AUTHOR);
		setAuthorMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.SET_AUTHOR, ma);
		menuEdit.add(setAuthorMI);
		menuEdit.addSeparator();
		ma = new MenuAction(ELANCommandFactory.LINKED_FILES_DLG);
		linkedFilesMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.LINKED_FILES_DLG, ma);
		menuEdit.add(linkedFilesMI);
        menuEdit.addSeparator();
        
		// For opening a Edit Lexicon Service Dialog: 
		ma = new MenuAction(ELANCommandFactory.EDIT_LEX_SRVC_DLG);
		editLexiconServiceMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.EDIT_LEX_SRVC_DLG, ma);
		menuEdit.add(editLexiconServiceMI);
		// Edit languages list (always enabled)
        ma = new EditLanguagesMA(ELANCommandFactory.EDIT_LANGUAGES_LIST);
        editLangListMI = new ElanMenuItem(ma, true);
        menuActions.put(ELANCommandFactory.EDIT_LANGUAGES_LIST, ma);
        menuEdit.add(editLangListMI);
        
        ma = new EditTierSetMA(ELANCommandFactory.EDIT_TIER_SET, this);
        editTierSetMI = new ElanMenuItem(ma, true);
        menuActions.put(ELANCommandFactory.EDIT_TIER_SET, ma);
        menuEdit.add(editTierSetMI);	
		ma = new EditSpellCheckerMA(ELANCommandFactory.EDIT_SPELL_CHECKER_DLG, this);
		menuActions.put(ELANCommandFactory.EDIT_SPELL_CHECKER_DLG, ma);
		menuEdit.add(new JMenuItem(ma));
		menuEdit.addSeparator();
		
        ma = new MenuAction("Menu.Edit.Preferences");
        menuActions.put("Menu.Edit.Preferences", ma);
        menuPreferences = new JMenu(ma);
        menuEdit.add(menuPreferences);
        
        ma = new EditPreferencesMA(ELANCommandFactory.EDIT_PREFS, this);
        menuActions.put(ELANCommandFactory.EDIT_PREFS, ma);
        menuPreferences.add(new JMenuItem(ma));
       
        ma = new EditShortcutsMA("Menu.Edit.Preferences.Shortcut", this);
        menuActions.put("Menu.Edit.Preferences.Shortcut", ma);
        menuPreferences.add(new JMenuItem(ma));
        menuPreferences.addSeparator();
        ma = new MenuAction(ELANCommandFactory.IMPORT_PREFS);
        importPrefsMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.IMPORT_PREFS, ma);
        menuPreferences.add(importPrefsMI);
        ma = new MenuAction(ELANCommandFactory.EXPORT_PREFS);
        exportPrefsMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.EXPORT_PREFS, ma);
		menuPreferences.add(exportPrefsMI);

		ma = new MenuAction("Menu.Annotation");
		menuActions.put("Menu.Annotation", ma);
        menuAnnotation = new JMenu(ma);
        menuBar.add(menuAnnotation);
        menuAnnotation.setEnabled(false);
        
        ma = new MenuAction("Menu.Tier");
        menuActions.put("Menu.Tier", ma);
        menuTier = new JMenu(ma);
        menuBar.add(menuTier);
        menuTier.setEnabled(false);
        
        ma = new MenuAction("Menu.Type");
        menuActions.put("Menu.Type", ma);
        menuType = new JMenu(ma);
        menuBar.add(menuType);
        menuType.setEnabled(false);
        
        ma = new MenuAction("Menu.Search");
        menuActions.put("Menu.Search", ma);
        menuSearch = new JMenu(ma);
        menuBar.add(menuSearch);
        //menuSearch.setEnabled(false);
        
        ma = new MenuAction(ELANCommandFactory.SEARCH_DLG);
        searchMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.SEARCH_DLG, ma);
		menuSearch.add(searchMI);
		ma = new MultiFindReplaceMA(ELANCommandFactory.REPLACE_MULTIPLE, this);
		menuActions.put(ELANCommandFactory.EDIT_PREFS, ma);
		menuSearch.add(ma);
		
		ma = new SearchMultipleMA(ELANCommandFactory.SEARCH_MULTIPLE_DLG, this);
		menuActions.put(ELANCommandFactory.SEARCH_MULTIPLE_DLG, ma);
		menuSearch.add(ma);
		
		ma = new FASTSearchMA(ELANCommandFactory.FASTSEARCH_DLG, this);
		menuActions.put(ELANCommandFactory.FASTSEARCH_DLG, ma);
		menuSearch.add(ma);
		
		ma = new StructuredSearchMultipleMA(ELANCommandFactory.STRUCTURED_SEARCH_MULTIPLE_DLG, this);
		menuActions.put(ELANCommandFactory.STRUCTURED_SEARCH_MULTIPLE_DLG, ma);
		menuSearch.add(ma);
		
		ma = new MenuAction(ELANCommandFactory.GOTO_DLG);
		goToMI = new ElanMenuItem(ma, false);
		menuActions.put(ELANCommandFactory.GOTO_DLG, ma);
		menuSearch.add(goToMI);
	
        ma = new MenuAction("Menu.View");
        menuActions.put("Menu.View", ma);
        menuView = new JMenu(ma);
        menuBar.add(menuView);

        ma = new MenuAction(ELANCommandFactory.MEDIA_PLAYERS);
        menuActions.put(ELANCommandFactory.MEDIA_PLAYERS, ma);        
        menuMediaPlayer = new JMenu(ma);
        menuView.add(menuMediaPlayer);
        
        ma = new MenuAction(ELANCommandFactory.WAVEFORMS);
        menuActions.put(ELANCommandFactory.WAVEFORMS, ma);
        menuWaveform = new JMenu(ma);
        waveFormGroup = new ButtonGroup();
        menuView.add(menuWaveform);
        
        ma = new MenuAction(ELANCommandFactory.VIEWERS);
        menuActions.put(ELANCommandFactory.VIEWERS, ma);
        menuViewer = new JMenu(ma);
        menuView.add(menuViewer);
        menuView.addSeparator();
        
        menuItemGridViewer = new JCheckBoxMenuItem(new CreateGridViewerMA(ELANCommandFactory.GRID_VIEWER,this));      
        menuItemGridViewer.setSelected(true);  
        menuViewer.add(menuItemGridViewer);   
        
        
        menuItemTextViewer = new JCheckBoxMenuItem(new CreateTextViewerMA(ELANCommandFactory.TEXT_VIEWER,this));      
        menuItemTextViewer.setSelected(true);
        menuViewer.add(menuItemTextViewer);   
        
        menuItemSubtitleViewer = new JCheckBoxMenuItem(new CreateSubtitleViewerMA(ELANCommandFactory.SUBTITLE_VIEWER,this));      
        menuItemSubtitleViewer.setSelected(true);
        menuViewer.add(menuItemSubtitleViewer);   
        
        menuItemLexiconViewer = new JCheckBoxMenuItem(new CreateLexiconViewerMA(ELANCommandFactory.LEXICON_VIEWER,this));      
        menuItemLexiconViewer.setSelected(true); 
        menuViewer.add(menuItemLexiconViewer);
        
        menuItemCommentViewer = new JCheckBoxMenuItem(new CreateCommentViewerMA(ELANCommandFactory.COMMENT_VIEWER,this));      
        menuItemCommentViewer.setSelected(true); 
        menuViewer.add(menuItemCommentViewer);
        
        menuItemRecognizer = new JCheckBoxMenuItem(new CreateRecognizerMA(ELANCommandFactory.RECOGNIZER,this));  
        menuItemRecognizer.setSelected(true); 
        menuViewer.add(menuItemRecognizer);   
        
        menuItemMetaDataViewer = new JCheckBoxMenuItem(new CreateMetadataViewerMA(ELANCommandFactory.METADATA_VIEWER,this));      
        menuItemMetaDataViewer.setSelected(true);
        menuViewer.add(menuItemMetaDataViewer); 
        
        menuItemSignalViewer = new JCheckBoxMenuItem(new CreateSignalViewerMA(ELANCommandFactory.SIGNAL_VIEWER,this));      
        menuItemSignalViewer.setSelected(true);        
        menuViewer.add(menuItemSignalViewer); 
        
        menuItemInterLinearViewer = new JCheckBoxMenuItem(new CreateInterlinearViewerMA(ELANCommandFactory.INTERLINEAR_VIEWER,this));      
        menuItemInterLinearViewer.setSelected(true);          
        menuViewer.add(menuItemInterLinearViewer); 
        
        menuItemTimeSeriesViewer = new JCheckBoxMenuItem(new CreateTimeSeriesViewerMA(ELANCommandFactory.TIMESERIES_VIEWER,this));      
        menuItemTimeSeriesViewer.setSelected(true);          
        menuViewer.add(menuItemTimeSeriesViewer); 
        
//        menuItemInterLinearizerViewer = new JCheckBoxMenuItem(new CreateInterlinearizerViewerMA(ELANCommandFactory.INTERLINEAR_LEXICON_VIEWER, this));
//        menuItemInterLinearizerViewer.setSelected(true);          
//        menuViewer.add(menuItemInterLinearizerViewer);
        
        loadViewerPreferences();
        
        ma = new MenuAction(ELANCommandFactory.TIER_DEPENDENCIES);
        tierDependenciesMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.TIER_DEPENDENCIES, ma);
		menuView.add(tierDependenciesMI);
		// replace by transcription independent actions
		ma = new ShortcutsMA(ELANCommandFactory.SHORTCUTS, this);
		menuActions.put(ELANCommandFactory.SHORTCUTS, ma);
        menuView.add(ma);
        
        ma = new FontBrowserMA(ELANCommandFactory.FONT_BROWSER, this);
        menuActions.put(ELANCommandFactory.FONT_BROWSER, ma);
        menuView.add(ma);
        
        ma = new ShowLogMA("Menu.View.LogView", this);
        menuActions.put("Menu.View.LogView", ma);
        menuView.add(ma);
        
        menuView.addSeparator();
        ma = new MenuAction(ELANCommandFactory.SPREADSHEET);
        spreadsheetMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.SPREADSHEET, ma);
        menuView.add(spreadsheetMI);
        
        ma = new MenuAction(ELANCommandFactory.STATISTICS);
        statisticsMI = new ElanMenuItem(ma, false);
        menuActions.put(ELANCommandFactory.STATISTICS, ma);
        menuView.add(statisticsMI);
        
        ma = new MenuAction("Menu.Options");
        menuActions.put("Menu.Options", ma);
        menuOptions = new JMenu(ma);
        menuBar.add(menuOptions);

        ma = new MenuAction("Menu.Options.TimeChangePropagationMode");
        menuActions.put("Menu.Options.TimeChangePropagationMode", ma);
        menuChangeTimePropMode = new JMenu(ma);
        menuChangeTimePropMode.setEnabled(false);
        menuOptions.add(menuChangeTimePropMode);
        menuOptions.addSeparator();
        
        ma = new MenuAction(ELANCommandFactory.ANNOTATION_MODE);
        menuItemAnnoMode = new JRadioButtonMenuItem(ma);
        menuActions.put(ELANCommandFactory.ANNOTATION_MODE, ma);
        menuItemAnnoMode.setEnabled(false);
        menuItemAnnoMode.setSelected(true);
        menuOptions.add(menuItemAnnoMode);

        ma = new MenuAction(ELANCommandFactory.SYNC_MODE);
        menuItemSyncMode = new JRadioButtonMenuItem(ma);
        menuActions.put(ELANCommandFactory.SYNC_MODE, ma);
        menuItemSyncMode.setEnabled(false);
        menuOptions.add(menuItemSyncMode);
        
        ma = new MenuAction(ELANCommandFactory.TRANSCRIPTION_MODE);
        menuItemTranscMode = new JRadioButtonMenuItem(ma);
        menuActions.put(ELANCommandFactory.TRANSCRIPTION_MODE, ma);
        menuItemTranscMode.setEnabled(false);
        menuOptions.add(menuItemTranscMode);
        
        ma = new MenuAction(ELANCommandFactory.SEGMENTATION_MODE);
        menuItemSegmentMode = new JRadioButtonMenuItem(ma);
        menuActions.put(ELANCommandFactory.SEGMENTATION_MODE, ma);
        menuItemSegmentMode.setEnabled(false);
        menuOptions.add(menuItemSegmentMode);       
        
        ma = new MenuAction(ELANCommandFactory.INTERLINEARIZATION_MODE);
        menuItemInterLinearMode = new JRadioButtonMenuItem(ma);
        menuActions.put(ELANCommandFactory.INTERLINEARIZATION_MODE, ma);
        menuItemInterLinearMode.setEnabled(false);
        menuOptions.add(menuItemInterLinearMode);
        menuOptions.addSeparator();
        
        ma = new ActivityMonitoringMA("Menu.Options.ActivityMonitoring", this);
        menuActions.put("Menu.Options.ActivityMonitoring", ma);
        menuOptions.add(new JMenuItem(ma));
        menuOptions.addSeparator();
        
        ma = new MenuAction(ELANCommandFactory.WEBSERVICES_DLG);
        menuWebservices = new JMenu(ma);
        menuWebservices.setEnabled(false);
        menuOptions.add(menuWebservices);
        menuOptions.addSeparator();
        
//        ma = new WebServicesMA("Menu.Options.WebServices", this);
//        menuActions.put("Menu.Options.WebServices", ma);
//        menuOptions.add(new JMenuItem(ma));
//        menuOptions.addSeparator();
        
//        menuItemPlayAround = new ElanMenuItem(ElanLocale.getString(
//        		ELANCommandFactory.PLAY_AROUND_SELECTION_DLG), false);
//        menuOptions.add(menuItemPlayAround);
        ma = new SetPlayAroundSelectionMA(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG, this);
        menuActions.put(ELANCommandFactory.PLAY_AROUND_SELECTION_DLG, ma);
        menuOptions.add(new JMenuItem(ma));
        ma = new SetPlaybackToggleMA(ELANCommandFactory.PLAYBACK_TOGGLE_DLG, this);
        menuActions.put(ELANCommandFactory.PLAYBACK_TOGGLE_DLG, ma);
        menuOptions.add(new JMenuItem(ma));
//        menuItemRateVol = new ElanMenuItem(ElanLocale.getString(
//                ELANCommandFactory.PLAYBACK_TOGGLE_DLG), false);
//        menuOptions.add(menuItemRateVol);

        menuOptions.addSeparator();
        ma = new MenuAction("Menu.Options.FrameLength");
        menuActions.put("Menu.Options.FrameLength", ma);
        menuFrameLength = new JMenu(ma);
        menuFrameLength.setEnabled(false);
        menuOptions.add(menuFrameLength);
        
        menuOptions.addSeparator();
        ma = new MenuAction("Menu.Options.Language");
        menuActions.put("Menu.Options.Language", ma);
        menuAppLanguage = new JMenu(ma);
        languageBG = new ButtonGroup();
        JRadioButtonMenuItem langRBMI;
        // add languages
        ma = new SetLocaleMA(ELANCommandFactory.CATALAN, this, ElanLocale.CATALAN);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);

        ma = new SetLocaleMA(ELANCommandFactory.CHINESE_SIMPL, this, ElanLocale.CHINESE_SIMP);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.GERMAN, this, ElanLocale.GERMAN);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.ENGLISH, this, ElanLocale.ENGLISH);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.SPANISH, this, ElanLocale.SPANISH);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.FRENCH, this, ElanLocale.FRENCH);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.JAPANESE, this, ElanLocale.JAPANESE);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.KOREAN, this, ElanLocale.KOREAN);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.DUTCH, this, ElanLocale.DUTCH);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.PORTUGUESE, this, ElanLocale.PORTUGUESE);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.RUSSIAN, this, ElanLocale.RUSSIAN);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.SWEDISH, this, ElanLocale.SWEDISH);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);
        
        ma = new SetLocaleMA(ELANCommandFactory.CUSTOM_LANG, this, ElanLocale.CUSTOM);
        langRBMI = new JRadioButtonMenuItem(ma);
        languageBG.add(langRBMI);
        menuAppLanguage.add(langRBMI);

        menuOptions.add(menuAppLanguage);
        recentLanguagesMenuItem = new RecentLanguagesMenuItem();
        menuOptions.add(recentLanguagesMenuItem);
        
        ma = new MenuAction("Menu.Window");
        menuActions.put("Menu.Window", ma);
        menuWindow = new JMenu(ma);
        windowsGroup = new ButtonGroup();
        menuBar.add(menuWindow);
        
        ma = new MenuAction("Menu.Help");
        menuActions.put("Menu.Help", ma);
        menuHelp = new JMenu(ma);
        menuBar.add(menuHelp);
        //menuHelp.setVisible(false);
       
        ma = new HelpMA(ELANCommandFactory.HELP, this);
        menuActions.put(ELANCommandFactory.HELP, ma);
        menuHelp.add(new JMenuItem(ma));
        menuHelp.addSeparator();
        
        ma = new UpdateElanMA(ELANCommandFactory.UPDATE_ELAN, this);
        menuActions.put(ELANCommandFactory.UPDATE_ELAN, ma);
        menuHelp.add(new JMenuItem(ma));
        
        ma = new MenuAction("Menu.Help.Website");
        menuActions.put("Menu.Help.Website", ma);
        JMenu websiteMenu = new JMenu(ma);
        menuHelp.add(websiteMenu);
        
        ma = new WebMA("Menu.Help.Website.ReleaseNotes", this, "http://tla.mpi.nl/tools/tla-tools/elan/release-notes/");
        menuActions.put("Menu.Help.Website.ReleaseNotes", ma);
        websiteMenu.add(new JMenuItem(ma));
        
        ma = new WebMA("Menu.Help.Website.Download", this, "http://tla.mpi.nl/tools/tla-tools/elan/download/");
        menuActions.put("Menu.Help.Website.Download", ma);
        websiteMenu.add(new JMenuItem(ma));
   
        ma = new WebMA("Menu.Help.Website.Forum", this, "http://tla.mpi.nl/forums/software/elan/");
        menuActions.put("Menu.Help.Website.Forum", ma);
        websiteMenu.add(new JMenuItem(ma));
        
        websiteMenu.addSeparator();
        
        ma = new WebMA("Menu.Help.Website.Subscribe", this, "mailto:majordomo@mpi.nl?body=subscribe%20Elan&subject=subscribe%20to%20Elan");
        menuActions.put("Menu.Help.Website.Subscribe", ma);
        websiteMenu.add(new JMenuItem(ma));
        
        ma = new AboutMA(ELANCommandFactory.ABOUT, this);
        menuActions.put(ELANCommandFactory.ABOUT, ma);
        menuHelp.add(new JMenuItem(ma));
        
        // temporary and therefore old fashioned menu item to allow fallback to JMF media players
        // on windows machines. As soon as native media is succesfully used in a few releases
        // this menu item can be deleted. The only action performed while toggling this item is
        // setting the PreferredMediaFramework property that is used by the player factory
        /* use either with a -D startup option (-DPreferredMediaFramework=JMF) or add to a preference panel 
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
            menuItemNativeMedia = new JCheckBoxMenuItem();
            menuItemNativeMedia.setText("Use Native Media Platform");
            menuItemNativeMedia.addActionListener(this);

            // haal de state uit de property
            boolean nativePrefered = true;
            String preferredMF = System.getProperty("PreferredMediaFramework");

            // set here default choice if nothing defined
            if (preferredMF == null) {
                System.setProperty("PreferredMediaFramework", "NativeWindows");

                //System.setProperty("PreferredMediaFramework", "JMF");
            } else if (!preferredMF.equals("NativeWindows")) {
                nativePrefered = false;
            }

            menuItemNativeMedia.setState(nativePrefered);

            // optionally suppress jmf support for the standalone version
            String suppressJMF = System.getProperty("suppressJMF");

            if ((suppressJMF == null) ||
                    !suppressJMF.toLowerCase().equals("true")) {
                menuOptions.add(menuItemNativeMedia);
            }
        }
		*/

        /*
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            // inserted by AR to allow choice for permanent detached video at startup
            menuItemPermanentDetached = new JCheckBoxMenuItem();
            menuItemPermanentDetached.setText("Use detached media window");
            menuItemPermanentDetached.addActionListener(this);

            // haal de state uit de preferences
            Boolean permanentDetached = Preferences.getBool("PreferredMediaWindow",
                    null);

            if (permanentDetached == null) {
                permanentDetached = Boolean.FALSE; // default usage is attached media window
            }

            menuItemPermanentDetached.setState(permanentDetached.booleanValue());
            menuOptions.add(menuItemPermanentDetached);

            // end of insertion by AR
        }
        */

        updateLocale();
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Use Native Media Platform")) {
            boolean useNativeMedia = menuItemNativeMedia.getState();

            if (useNativeMedia) {
                System.out.println(
                    "Setting preferred media framework to Native");
                System.setProperty("PreferredMediaFramework", "NativeWindows");
            } else {
                System.out.println("Setting preferred media framework to JMF");
                System.setProperty("PreferredMediaFramework", "JMF");
            }
        } /*else if (command.equals("Use detached media window")) {
            Preferences.set("PreferredMediaWindow",
                Boolean.valueOf(menuItemPermanentDetached.getState()), null);
        } else if (command.equals("MacNativeLF")) {
            switchMacLF();
        }*/  
    }
    
    public void clearShortcutsMap(String modeConstant){
//    	if(modeConstant.equals(ELANCommandFactory.INTERLINEARIZATION_MODE)){
//    		return;
//    	}
    	
    	if(getViewerManager() == null || getViewerManager().getTranscription() == null){
    		return;
    	}
    	
    	Iterator<Entry<String, KeyStroke>> it = ShortcutsUtil.getInstance().getCurrentShortcuts(modeConstant).entrySet().iterator();
        while (it.hasNext())
        {
        	Map.Entry<String, KeyStroke> pairs = it.next();
        	String actionName = pairs.getKey();        	
        	Action ca = ELANCommandFactory.getCommandAction(getViewerManager().getTranscription(), actionName);
        	if(ca != null){
        		ca.putValue(Action.ACCELERATOR_KEY, null);      
            	String ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(pairs.getValue());
        		if(ksDescription != null && ksDescription.trim().length() > 0){
        			ca.putValue(Action.SHORT_DESCRIPTION,
        	                ElanLocale.getString(actionName + "ToolTip"));
        		}
        	} else{
        		ca = menuActions.get(actionName);
        		if(ca != null){
            		ca.putValue(Action.ACCELERATOR_KEY, null);
            	}
        	}
        } 
    }
    
    public void updateShortcutMap(String modeConstant){
    	if(modeConstant == null){
    		return;
    	}
    	
    	if(getViewerManager() == null || getViewerManager().getTranscription() == null){
    		return;
    	}
    	
    	// add actions with accelerator keys and without a menu item to the input
        // and action map
//    	InputMap inputMap = menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        ActionMap actionMap = menuBar.getActionMap();
//    	InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getRootPane().getActionMap();
    	InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        ActionMap actionMap = getRootPane().getActionMap();      
        inputMap.clear();
        actionMap.clear();
        
//        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
        if (inputMap != null) {//useless test here
            String id = "Act-";
            String nextId;
            
            int index = 0;
            Action act;
            KeyStroke ks;
            Map.Entry<String, KeyStroke> pairs ;
            Map<String, KeyStroke> shortMap = ShortcutsUtil.getInstance().getCurrentShortcuts(modeConstant);            
            Iterator<Entry<String, KeyStroke>> it = shortMap.entrySet().iterator();
            
            while (it.hasNext())
            {
            	act = null;
            	pairs = it.next();
            	String actionName = pairs.getKey();
            	ks =  pairs.getValue();
            	String ksDescription = ShortcutsUtil.getInstance().getDescriptionForKeyStroke(ks);
            	if(modeConstant != null){
            		act = ELANCommandFactory.getCommandAction(getViewerManager().getTranscription(), actionName);
            	}
            	if(act != null){        				
            		act.putValue(Action.ACCELERATOR_KEY, ks);
            		nextId = id + index++;
            		if (ks != null) {           		
            			inputMap.put(ks, nextId);
            			actionMap.put(nextId, act);
            		}
            		if(ksDescription != null && ksDescription.trim().length() > 0){
                		String shortValue = (String) act.getValue(Action.SHORT_DESCRIPTION);
                		if(shortValue != null){
                			act.putValue(Action.SHORT_DESCRIPTION, shortValue + " (" + ksDescription+")");
                		}
                	}
            	} else {
            		act = menuActions.get(actionName);
                	if(act != null){                      		
                		act.putValue(Action.ACCELERATOR_KEY, ks);
                		nextId = id + index++;
                		if (ks != null) {
                			inputMap.put(ks, nextId);
                			actionMap.put(nextId, act);
                		}
                	}else if(actionName.equals(ELANCommandFactory.UNDO)){                		
                		act = undoMI.getAction();
                		if(act != null){        				
                    		act.putValue(Action.ACCELERATOR_KEY, ks);
                    		nextId = id + index++;
                    		if (ks != null) {
                        		inputMap.put(ks, nextId);
                        		actionMap.put(nextId, act);
                    		}
                		}
                	}else if (actionName.equals(ELANCommandFactory.REDO)){         
                    	act = redoMI.getAction();
                    	if(act != null){        				
                        	act.putValue(Action.ACCELERATOR_KEY, ks);
                        	nextId = id + index++;
                    		if (ks != null) {
                    			inputMap.put(ks, nextId);
                    			actionMap.put(nextId, act);
                    		}
                        }                    	
                	}
            	}
            }  
            
            // add 2 more actions that are not in a menu and that are not transcription dependent
            act = new NextWindowMA(ELANCommandFactory.NEXT_WINDOW);
            ks = (KeyStroke) act.getValue(Action.ACCELERATOR_KEY);
            nextId = id + index++;
			inputMap.put(ks, nextId);
			actionMap.put(nextId, act);
           
			act = new PrevWindowMA(ELANCommandFactory.PREV_WINDOW);
            ks = (KeyStroke) act.getValue(Action.ACCELERATOR_KEY);
            nextId = id + index++;
			inputMap.put(ks, nextId);
			actionMap.put(nextId, act);
        }
    }


    protected void initMenusAndCommands() {
        // instantiate CommandActions, also UndoCA and RedoCA

        /* sample:
           CommandAction playSelectionCA = ELANCommandFactory.getCommandAction(ELANCommandFactory.PLAY_SELECTION);
         */

        // instantiate JMenuItems, where possible with CommandActions as args for constructor

        /* sample:
           playSelectionItem = new JMenuItem(playSelectionCA);
           playSelectionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
           menuInvisible.add(playSelectionItem);   // add to menu, if only for button then to invisible menu
         */
    	
    	// add actions with accelerator keys and without a menu item to the input
        // and action map
//    	InputMap inputMap = menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        ActionMap actionMap = menuBar.getActionMap();
//        inputMap.clear();
//        actionMap.clear();
//        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
//            String id = "Act-";
//            String nextId;
//            
//            Object key;
//            List<String> vals;
//            int index = 0;
//            Action act;
//            KeyStroke ks;
//            Map<String, KeyStroke> shortMap = ShortcutsUtil.getInstance().getCurrentShortcuts(null);
//            Iterator<String> keyIt = shortMap.keySet().iterator();
//            while (keyIt.hasNext()) {
//            	act = ELANCommandFactory.getCommandAction(transcriptionForThisFrame, keyIt.next());
//            	if (act != null) {
//            		ks = (KeyStroke) act.getValue(Action.ACCELERATOR_KEY);
//            		if (ks != null) {
//            			nextId = id + index++;
//            			inputMap.put(ks, nextId);
//            			actionMap.put(nextId, act);
//            		}
//            	}
//            }
//            
//            // add 2 more actions that are not in a menu and that are not transcription dependent
//            act = new NextWindowMA(ELANCommandFactory.NEXT_WINDOW);
//            ks = (KeyStroke) act.getValue(Action.ACCELERATOR_KEY);
//            nextId = id + index++;
//			inputMap.put(ks, nextId);
//			actionMap.put(nextId, act);
//           
//			act = new PrevWindowMA(ELANCommandFactory.PREV_WINDOW);
//            ks = (KeyStroke) act.getValue(Action.ACCELERATOR_KEY);
//            nextId = id + index++;
//			inputMap.put(ks, nextId);
//			actionMap.put(nextId, act);
//        }     

        MenuAction ma;
        //menuFile.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        //        transcriptionForThisFrame, ELANCommandFactory.CLOSE)), 3);
        menuActions.remove(ELANCommandFactory.CLOSE);
        closeMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.CLOSE), true);
        menuActions.remove(ELANCommandFactory.SAVE);
        saveMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SAVE), true);
        menuActions.remove(ELANCommandFactory.SAVE_AS);
        saveAsMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SAVE_AS), true);
        menuActions.remove(ELANCommandFactory.SAVE_AS_TEMPLATE);
        saveAsTemplateMI.setAction(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SAVE_AS_TEMPLATE), true);

        menuActions.remove(ELANCommandFactory.SAVE_SELECTION_AS_EAF);
        saveSelEafMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame,
                ELANCommandFactory.SAVE_SELECTION_AS_EAF), true);

        menuActions.remove(ELANCommandFactory.EXPORT_EAF_2_7);
        saveAs2_7MI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame,
                ELANCommandFactory.EXPORT_EAF_2_7), true);

        menuActions.remove(mergeTransMI.getAction());
        mergeTransMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.MERGE_TRANSCRIPTIONS), true);
        
        menuBackup.setEnabled(true);
        ButtonGroup backupGroup = new ButtonGroup();

        // retrieve the stored value for backup interval, reuses the main back up preference key
        Integer buDelay = Preferences.getInt("BackUpDelay", null);
        JRadioButtonMenuItem neverMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_NEVER));

        if ((buDelay == null) ||
                (buDelay.compareTo(Constants.BACKUP_NEVER) == 0)) {
            neverMI.setSelected(true);
        }

        JRadioButtonMenuItem backup1MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.BACKUP_1));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_1) == 0)) {
            backup1MI.setSelected(true);
        }
        
        JRadioButtonMenuItem backup5MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_5));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_5) == 0)) {
            backup5MI.setSelected(true);
        }

        JRadioButtonMenuItem backup10MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_10));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_10) == 0)) {
            backup10MI.setSelected(true);
        }

        JRadioButtonMenuItem backup20MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_20));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_20) == 0)) {
            backup20MI.setSelected(true);
        }

        JRadioButtonMenuItem backup30MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BACKUP_30));

        if ((buDelay != null) && (buDelay.compareTo(Constants.BACKUP_30) == 0)) {
            backup30MI.setSelected(true);
        }

        backupGroup.add(neverMI);
        backupGroup.add(backup1MI);
        backupGroup.add(backup5MI);
        backupGroup.add(backup10MI);
        backupGroup.add(backup20MI);
        backupGroup.add(backup30MI);
        menuBackup.add(neverMI);
        menuBackup.add(backup1MI);
        menuBackup.add(backup5MI);
        menuBackup.add(backup10MI);
        menuBackup.add(backup20MI);
        menuBackup.add(backup30MI);

        menuActions.remove(ELANCommandFactory.PAGESETUP);
        pageSetUpMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.PAGESETUP), true);
        menuActions.remove(ELANCommandFactory.PREVIEW);
        printPreviewMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.PREVIEW), true);
        menuActions.remove(ELANCommandFactory.PRINT);
        printMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.PRINT), true);

        menuActions.remove(ELANCommandFactory.ANNOTATOR_COMPARE_MULTI);
        compareAnnotatorsMI.setAction(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.ANNOTATOR_COMPARE_MULTI), true);
      
        menuExport.setEnabled(true);

        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_TOOLBOX)));
      
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_FLEX)));
        
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.EXPORT_CHAT)));

        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.EXPORT_TAB)));

        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_TIGER)));
        
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.EXPORT_INTERLINEAR)));

        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                ELANCommandFactory.EXPORT_HTML)));
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.EXPORT_TRAD_TRANSCRIPT)));
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.EXPORT_PRAAT_GRID)));
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_WORDS)));
        
        ma = new MenuAction("Menu.File.Export.Smil");
        menuActions.put("Menu.File.Export.Smil", ma);
        menuExportSMIL = new JMenu(ma);        
        menuExport.add(menuExportSMIL);
        menuExportSMIL.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                   transcriptionForThisFrame, ELANCommandFactory.EXPORT_SMIL_RT)));
        menuExportSMIL.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_SMIL_QT)));        

        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.EXPORT_QT_SUB)));
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_SUBTITLES)));
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_RECOG_TIER)));

        //		menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        //				 transcriptionForThisFrame, ELANCommandFactory.EXPORT_TEX)));
        // maybe test here if available and only show menu item if available:
        //
        //ExportMediaCA ca = (ExportMediaCA) ELANCommandFactory.getCommandAction(transcriptionForThisFrame, ELANCommandFactory.EXPORT_MEDIA);
        //if (ca.isAvailable()) {
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.EXPORT_MEDIA)));

        //}
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.CLIP_MEDIA)));
        
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.EXPORT_IMAGE_FROM_WINDOW)));
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.EXPORT_FILMSTRIP)));
        
        menuExport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.EXPORT_SHOEBOX)));
        
        //menuImport.addSeparator();
        //menuImport.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        //        transcriptionForThisFrame, ELANCommandFactory.IMPORT_PRAAT_GRID)));
        menuActions.remove(importPraatMI.getAction());
        importPraatMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.IMPORT_PRAAT_GRID), true);
        menuActions.remove(importRecogTiersMI.getAction());
        importRecogTiersMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.IMPORT_RECOG_TIERS), true);
        // menu items with command actions
        menuActions.remove(ELANCommandFactory.UNDO);
        CommandAction undoCA = ELANCommandFactory.getUndoCA(transcriptionForThisFrame);
        undoMI.setAction(undoCA);

        menuActions.remove(ELANCommandFactory.REDO);
        CommandAction redoCA = ELANCommandFactory.getRedoCA(transcriptionForThisFrame);
        redoMI.setAction(redoCA);
                
        menuActions.remove(ELANCommandFactory.COPY_CURRENT_TIME);
        copyCurrentTimeCodeMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.COPY_CURRENT_TIME), true);
        menuActions.remove(ELANCommandFactory.EDIT_CV_DLG);
        editCVMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.EDIT_CV_DLG), true);
        
		// For opening a Edit Lexicon Service Dialog
        menuActions.remove(ELANCommandFactory.EDIT_LEX_SRVC_DLG);
        editLexiconServiceMI.setAction(ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        		ELANCommandFactory.EDIT_LEX_SRVC_DLG), true);
        menuActions.remove(ELANCommandFactory.SET_AUTHOR);
        setAuthorMI.setAction(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SET_AUTHOR), true);
        menuActions.remove(ELANCommandFactory.LINKED_FILES_DLG);
        linkedFilesMI.setAction(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.LINKED_FILES_DLG), true);
        menuActions.remove(ELANCommandFactory.IMPORT_PREFS);
        importPrefsMI.setAction(ELANCommandFactory.getCommandAction(
    			transcriptionForThisFrame, ELANCommandFactory.IMPORT_PREFS), true);
        menuActions.remove(ELANCommandFactory.EXPORT_PREFS);
        exportPrefsMI.setAction(ELANCommandFactory.getCommandAction(
    			transcriptionForThisFrame, ELANCommandFactory.EXPORT_PREFS), true);

        menuAnnotation.setEnabled(true);
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.NEW_ANNOTATION))); 
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.NEW_ANNOTATION_BEFORE)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.NEW_ANNOTATION_AFTER)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.CREATE_DEPEND_ANN)));        
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.MODIFY_ANNOTATION)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.MODIFY_ANNOTATION_TIME_DLG)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.MODIFY_ANNOTATION_DC_DLG)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.MERGE_ANNOTATION_WN)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.MERGE_ANNOTATION_WB)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.REMOVE_ANNOTATION_VALUE)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.SPLIT_ANNOTATION)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.DELETE_ANNOTATION)));
        ma = new MenuAction("Menu.Annotation.Delete");
        menuActions.put("Menu.Annotation.Delete", ma);
        JMenu delMenu = new JMenu(ma);
        menuAnnotation.add(delMenu);
        delMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, 
        		ELANCommandFactory.DELETE_ANNOS_IN_SELECTION)));
        delMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, 
        		ELANCommandFactory.DELETE_ANNOS_LEFT_OF)));
        delMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, 
        		ELANCommandFactory.DELETE_ANNOS_RIGHT_OF)));
        delMenu.addSeparator();
        delMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, 
        		ELANCommandFactory.DELETE_ALL_ANNOS_LEFT_OF)));
        delMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, 
        		ELANCommandFactory.DELETE_ALL_ANNOS_RIGHT_OF)));
        
        menuAnnotation.addSeparator();
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
            transcriptionForThisFrame,
            ELANCommandFactory.COPY_ANNOTATION)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
            transcriptionForThisFrame,
            ELANCommandFactory.COPY_ANNOTATION_TREE)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
    		transcriptionForThisFrame, 
		    ELANCommandFactory.DUPLICATE_ANNOTATION)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
            transcriptionForThisFrame,
            ELANCommandFactory.PASTE_ANNOTATION)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
            transcriptionForThisFrame,
            ELANCommandFactory.PASTE_ANNOTATION_TREE)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
            transcriptionForThisFrame,
            ELANCommandFactory.PASTE_ANNOTATION_HERE)));
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
            transcriptionForThisFrame,
            ELANCommandFactory.PASTE_ANNOTATION_TREE_HERE)));
        menuAnnotation.addSeparator();
        ma = new MenuAction("Menu.Annotation.Shift");
        menuActions.put("Menu.Annotation.Shift", ma);
        JMenu shiftMenu = new JMenu(ma);
        menuAnnotation.add(shiftMenu);
        shiftMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SHIFT_ACTIVE_ANNOTATION)));
        shiftMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SHIFT_ANNOS_IN_SELECTION)));
        shiftMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SHIFT_ANNOS_LEFT_OF)));
        shiftMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SHIFT_ANNOS_RIGHT_OF)));
        shiftMenu.addSeparator();
        shiftMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SHIFT_ALL_ANNOS_LEFT_OF)));
        shiftMenu.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SHIFT_ALL_ANNOS_RIGHT_OF)));
        
        menuAnnotation.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SHIFT_ALL_ANNOTATIONS)));
    
        menuTier.setEnabled(true);
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.ADD_TIER)));
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.CHANGE_TIER)));
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
    		transcriptionForThisFrame, ELANCommandFactory.REPARENT_TIER)));
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.DELETE_TIER)));
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.IMPORT_TIERS)));
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.ADD_PARTICIPANT)));
        menuTier.addSeparator();
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.TOKENIZE_DLG)));

        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.FILTER_TIER_DLG)));

        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
				transcriptionForThisFrame,
				ELANCommandFactory.COPY_TIER_DLG)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
				transcriptionForThisFrame,
				ELANCommandFactory.COPY_ANN_OF_TIER)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
				transcriptionForThisFrame,
				ELANCommandFactory.MERGE_TIERS)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
				transcriptionForThisFrame,
				ELANCommandFactory.MERGE_TIERS_CLAS)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
				transcriptionForThisFrame,
				ELANCommandFactory.MERGE_TIER_GROUP)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.ANN_ON_DEPENDENT_TIER)));

        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.ANN_FROM_OVERLAP)));
        // temp
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.ANN_FROM_OVERLAP_CLAS)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.ANN_FROM_SUBTRACTION)));        

        // temp
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.ANN_FROM_GAPS)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.REGULAR_ANNOTATION_DLG)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.REMOVE_ANNOTATIONS_OR_VALUES)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, 
                ELANCommandFactory.ANNOTATIONS_TO_TIERS)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.LABEL_AND_NUMBER)));
        
        menuTier.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.CHANGE_CASE)));
    
        menuType.setEnabled(true);
        
        menuType.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.ADD_TYPE)));
        menuType.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.CHANGE_TYPE)));
        menuType.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.DELETE_TYPE)));
        menuType.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame,
                ELANCommandFactory.IMPORT_TYPES)));
        
        //menuSearch.setEnabled(true);
        menuActions.remove(ELANCommandFactory.SEARCH_DLG);
        searchMI.setAction(ELANCommandFactory.getCommandAction(
        		transcriptionForThisFrame, ELANCommandFactory.SEARCH_DLG), true);
        menuActions.remove(ELANCommandFactory.GOTO_DLG);
        goToMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.GOTO_DLG), true);

        //menuView.setEnabled(true);
        menuActions.remove(ELANCommandFactory.TIER_DEPENDENCIES);
        tierDependenciesMI.setAction(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.TIER_DEPENDENCIES), true);
        menuActions.remove(ELANCommandFactory.SPREADSHEET);
        spreadsheetMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SPREADSHEET), true);
        menuActions.remove(ELANCommandFactory.STATISTICS);
        statisticsMI.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.STATISTICS), true);
        
//        CommandAction syntaxViewerAction = ELANCommandFactory.getCommandAction(
//        		transcriptionForThisFrame, ELANCommandFactory.SYNTAX_VIEWER);
//        if(syntaxViewerAction != null){
//        	menuView.add(new ElanMenuItem(syntaxViewerAction));
//        }
        
        menuChangeTimePropMode.setEnabled(true);

        ButtonGroup timePropGroup = new ButtonGroup();
        JRadioButtonMenuItem normalModeMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.TIMEPROP_NORMAL));
        JRadioButtonMenuItem bulldozerModeMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.BULLDOZER_MODE));
        JRadioButtonMenuItem shiftModeMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.SHIFT_MODE));
        timePropGroup.add(normalModeMI);
        normalModeMI.setSelected(true);
        timePropGroup.add(bulldozerModeMI);
        timePropGroup.add(shiftModeMI);
        menuChangeTimePropMode.add(normalModeMI);
        menuChangeTimePropMode.add(bulldozerModeMI);
        menuChangeTimePropMode.add(shiftModeMI);

        ButtonGroup modeGroup = new ButtonGroup();
        menuActions.remove(ELANCommandFactory.ANNOTATION_MODE);
        menuItemAnnoMode.setAction(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame,
                    ELANCommandFactory.ANNOTATION_MODE));
        menuItemAnnoMode.setEnabled(true);
        menuActions.remove(ELANCommandFactory.SYNC_MODE);
        menuItemSyncMode.setAction(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.SYNC_MODE));
        menuItemSyncMode.setEnabled(true);
        menuActions.remove(ELANCommandFactory.TRANSCRIPTION_MODE);
        menuItemTranscMode.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.TRANSCRIPTION_MODE));
        menuItemTranscMode.setEnabled(true);
        menuActions.remove(ELANCommandFactory.SEGMENTATION_MODE);
        menuItemSegmentMode.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SEGMENTATION_MODE));
        menuItemSegmentMode.setEnabled(true);
        
        menuActions.remove(ELANCommandFactory.INTERLINEARIZATION_MODE);
        menuItemInterLinearMode.setAction(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.INTERLINEARIZATION_MODE));
        menuItemInterLinearMode.setEnabled(true);
        
        modeGroup.add(menuItemAnnoMode);
        modeGroup.add(menuItemSyncMode);
        modeGroup.add(menuItemTranscMode);
        modeGroup.add(menuItemSegmentMode);
        modeGroup.add(menuItemInterLinearMode);
        
//        Integer val = Preferences.getInt("LayoutManager.CurrentMode", viewerManager.getTranscription()); 
//        if(val != null){
//        	switch(val){
//        	case ElanLayoutManager.NORMAL_MODE:
//        		menuItemAnnoMode.setSelected(true);
//        		break;
//        	case ElanLayoutManager.SYNC_MODE:
//        		menuItemSyncMode.setSelected(true);
//        		break;
//        	case ElanLayoutManager.TRANSC_MODE:
//        		menuItemTranscMode.setSelected(true);
//        		
//        		break;
//        	case ElanLayoutManager.SEGMENT_MODE:
//        		menuItemSegmentMode.setSelected(true);
//        		break;
//        	}        	
//        }  
        
        menuWebservices.setEnabled(true);
        menuWebservices.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.WEBLICHT_DLG)));
        menuWebservices.add(new ElanMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.TYPECRAFT_DLG)));
//        menuOptions.add(menuItemAnnoMode);
//        menuOptions.add(menuItemSyncMode);
        //menuOptions.addSeparator();
        //menuItemKioskMode = new JCheckBoxMenuItem(ELANCommandFactory.getCommandAction(
        //        transcriptionForThisFrame,
        //        ELANCommandFactory.KIOSK_MODE));
        //menuOptions.add(menuItemKioskMode);
        //menuOptions.addSeparator();

//        menuItemPlayAround.setAction(ELANCommandFactory.getCommandAction(
//                transcriptionForThisFrame,
//                ELANCommandFactory.PLAY_AROUND_SELECTION_DLG));
//        menuItemPlayAround.setEnabled(true);

//        menuItemRateVol.setAction(ELANCommandFactory.getCommandAction(
//                transcriptionForThisFrame,
//                ELANCommandFactory.PLAYBACK_TOGGLE_DLG));
//        menuItemRateVol.setEnabled(true);

        menuFrameLength.setEnabled(true);
        ButtonGroup videoGroup = new ButtonGroup();
        JRadioButtonMenuItem palMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.SET_PAL));
        JRadioButtonMenuItem pal50MI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                transcriptionForThisFrame, ELANCommandFactory.SET_PAL_50));
        JRadioButtonMenuItem ntscMI = new JRadioButtonMenuItem(ELANCommandFactory.getCommandAction(
                    transcriptionForThisFrame, ELANCommandFactory.SET_NTSC));
        videoGroup.add(palMI);
        palMI.setSelected(true);
        videoGroup.add(pal50MI);
        videoGroup.add(ntscMI);
        menuFrameLength.add(palMI);
        menuFrameLength.add(pal50MI);
        menuFrameLength.add(ntscMI);

        updateLocale();
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void updateLocale() {
        setFrameTitle();

        // update the language menu items
        if (languageBG != null) {
            if (languageBG.getButtonCount() > 0) {
                Enumeration<AbstractButton> en = languageBG.getElements();
                Object item;
                JRadioButtonMenuItem rbItem;

                while (en.hasMoreElements()) {
                    item = en.nextElement();

                    if (item instanceof JRadioButtonMenuItem) {
                        rbItem = (JRadioButtonMenuItem) item;

                        if (rbItem.getAction() instanceof SetLocaleMA) {
                        	if ( ((SetLocaleMA)rbItem.getAction()).getLocale().equals(ElanLocale.getLocale())) {
	                            rbItem.setSelected(true);                            
	
	                            break;
                        	}
                        }
                    }
                }
            }
        }

        // update the special, document independent actions
        MenuAction ma;
//        for (int i = 0; i < menuActions.size(); i++) {
//        	ma = (MenuAction) menuActions.get(i);
//        	ma.updateLocale();
//        }
        
        Iterator<MenuAction> it = menuActions.values().iterator();
		while(it.hasNext()){
			ma = it.next();
			if (ma !=null) {				
	        	ma.updateLocale();
			}
		}
    }

    protected void savePreferences() {
        // save some transcription specific preference values
    	/*
        if (layoutManager != null) {
            if (layoutManager.getMultiTierControlPanel() != null) {
                List tierOrder = layoutManager.getMultiTierControlPanel()
                                                .getTierOrder();

                if (tierOrder != null) {
                    Preferences.set("TierOrder", tierOrder,
                        transcriptionForThisFrame);
                }

                String activeTierName = layoutManager.getMultiTierControlPanel()
                                                     .getActiveTierName();

                if (activeTierName != null) {
                    Preferences.set("ActiveTierName", activeTierName,
                        transcriptionForThisFrame);
                }
            }

            Preferences.set("LayoutManagerState", layoutManager.getState(),
                transcriptionForThisFrame);
        }
        */
        if (viewerManager != null) {
            Preferences.set("MediaTime",
                viewerManager.getMasterMediaPlayer().getMediaTime(),
                transcriptionForThisFrame, false, false);
            Preferences.set("SelectionBeginTime",
                viewerManager.getSelection().getBeginTime(),
                transcriptionForThisFrame, false, false);
            Preferences.set("SelectionEndTime",
                viewerManager.getSelection().getEndTime(),
                transcriptionForThisFrame, false, false);
            Preferences.set("TimeScaleBeginTime",
                viewerManager.getTimeScale().getBeginTime(),
                transcriptionForThisFrame, false, true);// forces writing
        }
        // replace by setPreference();
        setPreference("Locale", ElanLocale.getLocale(), null);
        // 10-2012 store per transcription. or global if transcription is null
        setPreference("FrameSize", getSize(), transcriptionForThisFrame);
        //setPreference("FrameSize", getSize(), null);
        //setPreference("FrameLocation", getLocation(), null);
        //System.out.println("Location: " + getLocation());
        // 10-2012 store either for the transcription or global if transcription is null
        Preferences.set("FrameLocation", getLocation(), transcriptionForThisFrame, false, true);// forces writing
        //Preferences.set("FrameLocation", getLocation(), null, false, true);// forces writing
    }
    
    public WaveFormViewerMenuManager getWaveFormViewerMenuManager(){
    	return wfvMenuManager;
    }
    
    private void loadViewerPreferences(){
    	
    	Boolean boolPref = Preferences.getBool(ELANCommandFactory.GRID_VIEWER, null);
    	if (boolPref != null) {
    		menuItemGridViewer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.TEXT_VIEWER, null);
    	if (boolPref != null) {
    		menuItemTextViewer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.SUBTITLE_VIEWER, null);
    	if (boolPref != null) {
    		menuItemSubtitleViewer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.LEXICON_VIEWER, null);
    	if (boolPref != null) {
    		menuItemLexiconViewer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.COMMENT_VIEWER, null);
    	if (boolPref != null) {
    		menuItemCommentViewer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.RECOGNIZER, null);
    	if (boolPref != null) {
    		menuItemRecognizer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.METADATA_VIEWER, null);
    	if (boolPref != null) {
    		menuItemMetaDataViewer.setSelected(boolPref);    	
    	}
    	
    	boolPref = Preferences.getBool(ELANCommandFactory.SIGNAL_VIEWER, null);
    	if (boolPref != null) {
    		menuItemSignalViewer.setSelected(boolPref); 
    	}    
    	boolPref = Preferences.getBool(ELANCommandFactory.INTERLINEAR_VIEWER, null);
    	if (boolPref != null) {
    		menuItemInterLinearViewer.setSelected(boolPref);    	
    	} 
    	boolPref = Preferences.getBool(ELANCommandFactory.TIMESERIES_VIEWER, null);
    	if (boolPref != null) {
    		menuItemTimeSeriesViewer.setSelected(boolPref);    	
    	} 
//    	val = Preferences.getBool(ELANCommandFactory.INTERLINEAR_LEXICON_VIEWER, null);
//    	if (val != null) {
//    		menuItemInterLinearizerViewer.setSelected((Boolean)val);    	
//    	}
    }

    protected void loadPreferences() {
        // initialize some transcription specific values
        // invokeLater ensures the viewers are initialized properly before setting values
        SwingUtilities.invokeLater(new Runnable() {
                @Override
				public void run() {
                	/*
                    Locale savedLocale = (Locale) Preferences.get("Locale", null);

                    if (savedLocale != null) {
                        ElanLocale.setLocale(savedLocale);
                    }
                    Object toObject = Preferences.get("TierOrder",
                            transcriptionForThisFrame);
                    Vector tierOrder;
                    if (toObject instanceof ArrayList) {
                    	tierOrder = new Vector((ArrayList) toObject);
                    } else {
                    	tierOrder = (Vector) toObject;
                    }

                    if (tierOrder != null) {
                        if (tierOrder.size() > 0) {
                            layoutManager.getMultiTierControlPanel()
                                         .setTierOrder(tierOrder);
                        } else if (transcriptionForThisFrame.getTiers()
                                                                .size() > 0) {
                            Vector tiers = transcriptionForThisFrame.getTiers();
                            Vector tierNames = new Vector(tiers.size());

                            for (int i = 0; i < tiers.size(); i++) {
                                Tier t = (Tier) tiers.get(i);
                                tierNames.add(t.getName());
                            }

                            layoutManager.getMultiTierControlPanel()
                                         .setTierOrder(tierNames);
                        }
                    }

                    String activeTierName = Preferences.getString("ActiveTierName",
                            transcriptionForThisFrame);

                    if (activeTierName != null) {
                        layoutManager.getMultiTierControlPanel()
                                     .setActiveTierForName(activeTierName);
                    }
                    */
                    Long beginTime = Preferences.getLong("SelectionBeginTime",
                            transcriptionForThisFrame);
                    Long endTime = Preferences.getLong("SelectionEndTime",
                            transcriptionForThisFrame);

                    if ((beginTime != null) && (endTime != null)) {
                        viewerManager.getSelection().setSelection(beginTime.longValue(),
                            endTime.longValue());
                    }

                    Long mediaTime = Preferences.getLong("MediaTime",
                            transcriptionForThisFrame);

                    if (mediaTime != null) {
                        viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime.longValue());
                    }

                    Long timeScaleBeginTime = Preferences.getLong("TimeScaleBeginTime",
                            transcriptionForThisFrame);

                    if (timeScaleBeginTime != null) {
                        viewerManager.getTimeScale().setBeginTime(timeScaleBeginTime.longValue());
                    }
                    
        	            Boolean val = Preferences.getBool("MediaNavigation.FrameStepToFrameBegin",
        	                null);
        	
        	            if (val != null) {
        	                viewerManager.setFrameStepsToBeginOfFrame(val);
        	            }
                    /*
                    Map layoutMap = Preferences.getMap("LayoutManagerState",
                           transcriptionForThisFrame);

                    if (layoutMap != null) {
                        layoutManager.setState(layoutMap);
                    }
					*/
                    // start the backup thread
                    Integer backupDelay = Preferences.getInt("BackUpDelay",
                            null);

                    if ((backupDelay != null) && (backupDelay.intValue() > 0)) {
                        Command c = ELANCommandFactory.createCommand(transcriptionForThisFrame,
                                ELANCommandFactory.BACKUP);
                        c.execute(ELANCommandFactory.getCommandAction(
                                transcriptionForThisFrame,
                                ELANCommandFactory.BACKUP),
                            new Object[] { backupDelay });
                    }
                    fullyInitialized = true;
                    
                    // Sometimes layoutManager is still null at this time
                    if (layoutManager != null) {
                    	layoutManager.doLayout();
                    }
                    Toolkit.getDefaultToolkit().sync();
                }
            });

        //layoutManager.doLayout();
    }

    /**
     * A separate treatment for preferences for controlled vocabulary entries.
     * The preferences are (currently) stored in the CVEntry themselves, in contrast
     * to most other preferences that are fetched from the Preferences each time when needed.
     */
    private void loadCVPreferences() {
    	if (transcriptionForThisFrame == null) {
    		return;
    	}
    	Map/*<String, Map<String, Map<String, ?>>>*/ cvPrefMap = Preferences.getMap(Preferences.CV_PREFS, transcriptionForThisFrame);
    	if (cvPrefMap != null) {
    		List<ControlledVocabulary> allCV = ((TranscriptionImpl) transcriptionForThisFrame).getControlledVocabularies();
    		if (allCV == null) {
    			return;
    		}
			String color = "Color";
			String keyCode = "KeyCode";

			for (ControlledVocabulary cv : allCV) {
    			Object cvMapObj = cvPrefMap.get(cv.getName());
    			if (cvMapObj instanceof Map) {
    				Map<String, Map<String, ?>> cvMap = (Map<String, Map<String, ?>>) cvMapObj;
    				
    				for (CVEntry cve : cv) {
    					String id = cve.getId();
    					if (cvMap.containsKey(id)) {
    						Map<String, ?> cveMap = cvMap.get(id);
    						if (cveMap.containsKey(color)) {
    							cve.setPrefColor((Color)cveMap.get(color));
    						}
    						if (cveMap.containsKey(keyCode)) {
    							cve.setShortcutKeyCode(((Integer)cveMap.get(keyCode)).intValue());
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    /**
     * @see PreferencesUser#setPreference(String, Object, Object)
     */
    @Override
	public void setPreference(String key, Object value, Object document) {
		if (document instanceof Transcription) {
			Preferences.set(key, value, (Transcription)document, false, false);
		} else {
			Preferences.set(key, value, null, false, false);
		}
	}

	/**
	 * Should these preferences be part of the loading of doc. preferences??
	 * @see PreferencesListener#preferencesChanged()
	 */
    @Override
	public void preferencesChanged() {
        Dimension d = Preferences.getDimension("FrameSize", transcriptionForThisFrame);
        
        if (d == null) {
        	d = Preferences.getDimension("FrameSize", null);
        }
        
        // HS June 2010 correct dimension and location if they don't fit on the screen?
        // or make this a user preference?
        Rectangle wRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        
        if (d != null) {      	
        	if (d.width > wRect.width) {
        		d.setSize(wRect.width, d.height);
        	}
        	if (d.height > wRect.getHeight()) {
        		d.setSize(d.width, wRect.height);
        	}
            setSize(d);
        }
        Point p = Preferences.getPoint("FrameLocation", transcriptionForThisFrame);
        
        if (p == null) {
        	p = Preferences.getPoint("FrameLocation", null);
        }

        if (p != null) {
        	if (p.x < wRect.x) {
        		p.x = wRect.x;
        	} else if (p.x > wRect.width - WINDOW_POS_MARGIN) {
        		p.x = wRect.width - WINDOW_POS_MARGIN;
        	}
//        	int mpx = wRect.width - getWidth();
//        	if (p.x > mpx && mpx > wRect.x) {
//        		p.x = mpx;
//        	}
        	if (p.y < wRect.y) {
        		p.y = wRect.y;
        	} else if (p.y > wRect.height - WINDOW_POS_MARGIN) {
        		p.y = wRect.height - WINDOW_POS_MARGIN;
        	}
//        	int mph = wRect.height - getHeight();
//        	if (p.y > mph && mph > wRect.y) {
//        		p.y = mph;
//        	}
            setLocation(p);
        }
        // ?? 
        Locale savedLocale = (Locale) Preferences.get("Locale", null);

        if (savedLocale != null) {
            ElanLocale.setLocale(savedLocale);
        }
        
        if (viewerManager != null) {
        	Boolean val = Preferences.getBool("MediaNavigation.FrameStepToFrameBegin",
	                null);
	
	        if (val != null) {
	            viewerManager.setFrameStepsToBeginOfFrame(val.booleanValue());
	        }
        }
        // update values in playbacktoggle and play around selection actions, not really elegant
        String stringPref = Preferences.getString("PlayAroundSelection.Mode", null);
        boolean msMode = true;
        if (stringPref != null) {
        	if ("frames".equals(stringPref)) {
        		msMode = false;
        	}
        }
        Integer intPref = Preferences.getInt("PlayAroundSelection.Value", null);
        if (intPref != null && transcriptionForThisFrame != null) {
        	int playaroundVal = intPref.intValue();
        	if (!msMode) {
        		playaroundVal = (int) (playaroundVal * viewerManager.getMasterMediaPlayer().getMilliSecondsPerSample());
        		((PlayAroundSelectionCA) ELANCommandFactory.getCommandAction(transcriptionForThisFrame, 
        				ELANCommandFactory.PLAY_AROUND_SELECTION)).setPlayAroundSelectionValue(playaroundVal);
        	}
        }
        Float floatPref = Preferences.getFloat("PlaybackRateToggleValue", null);
        if (floatPref != null && transcriptionForThisFrame != null) {
        	float rate = floatPref.floatValue();
        	((PlaybackRateToggleCA)ELANCommandFactory.getCommandAction(
                	transcriptionForThisFrame, 
                	ELANCommandFactory.PLAYBACK_RATE_TOGGLE)).setToggleValue(rate);
        }
        floatPref = Preferences.getFloat("PlaybackVolumeToggleValue", null);
        if (floatPref != null && transcriptionForThisFrame != null) {
        	float vol = floatPref.floatValue();
        	((PlaybackVolumeToggleCA)ELANCommandFactory.getCommandAction(
                	transcriptionForThisFrame, 
                	ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE)).setToggleValue(vol);
        }
        intPref = Preferences.getInt("NumberOfBackUpFiles", null);
        if (intPref != null && transcriptionForThisFrame != null) {
        	int nf = intPref.intValue();
        	((BackupCA) ELANCommandFactory.getCommandAction(
        			transcriptionForThisFrame, 
        			ELANCommandFactory.BACKUP)).setNumBuFiles(nf);
        } 
	}
	
    /**
     * Checks whether there are any changes to save and starts the saving and or 
     * closing sequence.
     * 
     * @see #saveAndClose(boolean)
     * @see #doClose(boolean)
     */
    public void checkSaveAndClose() {
    	if (transcriptionForThisFrame != null) {
    		// HS temporarily? activated again for 4.7.2
    		// needs reconsideration: is now called twice, here and in doClose()
    		layoutManager.isClosing(); // moved to later in doClose() so it will be called in more cases, in particular from FrameManager.exit()
    		if (transcriptionForThisFrame.isChanged()) {

	            int response = JOptionPane.showConfirmDialog(null,
	                    ElanLocale.getString("Frame.ElanFrame.UnsavedData"),
	                    ElanLocale.getString("Message.Warning"),
	                    JOptionPane.YES_NO_CANCEL_OPTION);
	
	            if (response == JOptionPane.YES_OPTION) {
	            	saveAndClose(true);
	            } else if ((response == JOptionPane.CANCEL_OPTION) ||
	                    (response == JOptionPane.CLOSED_OPTION)) {
	                // the user does not want to close
	                return;
	            } else {
	            	doClose(true);
	            }
    		} else {
            	doClose(true);
            }
    	}
    }
    
    /**
     * Saves the document and starts the window closing sequence. 
     * <b>Caution: </b>does not ask the user whether or not to save changes; 
     * it is assumed that this has been done elsewhere.
     * Called from the Close Command or from the FrameManager.
     * 
     * @param unregister if true this frame should be unregistered with the 
     * FrameManager
     * @see #checkSaveAndClose()
     * @see #doClose(boolean)
     */
    public void saveAndClose(boolean unregister) {
    	if (unregister) {
    		FrameManager.getInstance().closeFrame(this);	
    	}
    	
        if ((transcriptionForThisFrame != null) &&
                transcriptionForThisFrame.isChanged()) {
            boolean saveNewCopy = false;

            if (transcriptionForThisFrame.getName().equals(TranscriptionImpl.UNDEFINED_FILE_NAME)) {
                // save as dialog
                saveNewCopy = true;
            }

            TranscriptionStore ets = ACMTranscriptionStore.getCurrentTranscriptionStore();
            StoreCommand storeComm = new StoreCommand(ELANCommandFactory.STORE);
            if (viewerManager.getMultiTierControlPanel() != null) {
	            storeComm.execute(transcriptionForThisFrame,
	                new Object[] {
	                    ets, Boolean.FALSE, Boolean.valueOf(saveNewCopy),
	                    viewerManager.getMultiTierControlPanel()
	                                 .getVisibleTiers(),
	                    Integer.valueOf(TranscriptionStore.EAF)
	                });
            } else {
	            storeComm.execute(transcriptionForThisFrame,
		                new Object[] {
		                    ets, Boolean.FALSE, Boolean.valueOf(saveNewCopy),
		                    new ArrayList()
		                });
            }
            // HS nov 2006: check if the file actually has been saved
            // if not e.g. a Save As dialog has been canceled, return
            if (transcriptionForThisFrame.isChanged()) {
                System.out.println("Save (as) cancelled");
                return;
            }
        }        
        doClose(unregister);
    }
    
    /**
     * Unregisteres with the FrameManager if necessary and closes 
     * the frame (without saving). Performs clean up and disposes the frame.<br>
     * Called from from Close Command or from the FrameManager.<br>
     * <b>Caution: </b>does not check whether the document should be saved or not; 
     * it is assumed that this has been done elsewhere.
     * @param unregister if true this frame should be unregistered with the 
     * FrameManager
     * @see #checkSaveAndClose()
     * @see #saveAndClose(boolean)
     */
    public void doClose(boolean unregister) {
    	FrameManager.getInstance().fireWindowClosing(this, ShutdownListener.Event.WINDOW_CLOSES_EARLY);

    	if (transcriptionForThisFrame != null) {
  		    layoutManager.isClosing();	// save various preferences
    	}
    	
    	if (unregister) {
    		FrameManager.getInstance().closeFrame(this);	
    	}
    	
    	if (MonitoringLogger.isInitiated() && viewerManager != null 
    			&& viewerManager.getTranscription() != null){
    		MonitoringLogger.getLogger(viewerManager.getTranscription()).log(MonitoringLogger.CLOSE_FILE);
        }    
    	
    	savePreferences();
   	
    	//remove document from ELANCommandFactory, unregister as listener etc.
        if (transcriptionForThisFrame != null) {
            //stop the backup task, just to be sure
        	BackupCA ca = ((BackupCA) ELANCommandFactory.getCommandAction(transcriptionForThisFrame,
            ELANCommandFactory.BACKUP));
            if (ca != null) {
               ca.stopBackUp();
            }               	 
            if (viewerManager != null) {
                viewerManager.cleanUpOnClose();
            } 
        	
        	if (layoutManager != null) {
            	layoutManager.cleanUpOnClose();	
        	}  
        	
            ELANCommandFactory.removeDocument(viewerManager);
            Preferences.removeDocument(transcriptionForThisFrame);
            // remove this elan frame as locale listener
            ElanLocale.removeElanLocaleListener(transcriptionForThisFrame);            
            
            transcriptionForThisFrame = null;
            viewerManager = null;
            layoutManager = null;
        }
        setJMenuBar(null);
        menuBar = null;
    	if (recentLanguagesMenuItem != null) {
	    	recentLanguagesMenuItem.isClosing();
    	}

    	FrameManager.getInstance().fireWindowClosing(this, ShutdownListener.Event.WINDOW_CLOSES_LATE);
        dispose();
        System.gc();
        System.runFinalization();
    }

    /**
     * Adds an action to a menu. The menu is specified by an id, the position in 
     * the menu can be specified by an index. The action is supposed to handle its
     * own events.
     * 
     * @param action the action to add to a Menu (inside a JMenuItem)
     * @param menuId the identifier of the menu, a constant from #FrameConstants
     * @param index the index to insert the action, -1 means to add at the end
     */
    public void addActionToMenu(Action action, int menuId, int index) {
    	if (action == null) {
    		return;
    	}
    	Object mm = getMenuById(menuId);
    	if (mm instanceof JMenu) {
    		JMenu menu = (JMenu) mm;
    		if (menu == menuWindow) {
    			JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
    			menu.add(item, index);
    			windowsGroup.add(item);
    		} else if (menu == menuMediaPlayer) {
    			JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
    			menu.add(item, index);
    		} else if(menu == menuWaveform){
    			JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
    			menu.add(item, index);
    			waveFormGroup.add(item);
    		} else if(menu == menuRecentFiles){
        		JMenuItem item = new JMenuItem(action);
        		if (action.getValue(Action.LONG_DESCRIPTION) != null) {
        			if (((String) action.getValue(Action.NAME)).length() != 
        					((String) action.getValue(Action.LONG_DESCRIPTION)).length()) {
        				item.setToolTipText((String) action.getValue(Action.LONG_DESCRIPTION));
            		}
        		}
        		menu.add(item, index);
    		} else {
        		JMenuItem item = new JMenuItem(action);
        		menu.add(item, index);
    		}
    	}
    }
    
    /**
     * Looks for an Action with the specified id (== the LONG_DESCRIPTION) under 
     * the menu identified by the menu id. If it is found it is removed from the menu 
     * and returned.
     * @param actionId the id of the Action, currently stored in the LONG_DESCRIPTION value
     * @param menuId the identifier of the menu, a constant from #FrameConstants
     * @return the action that has been removed from the menu
     */
    public Action removeActionFromMenu(String actionId, int menuId) {
    	if (actionId == null) {
    		return null;
    	}
    	Object mm = getMenuById(menuId);
    	if (mm instanceof JMenu) {
    		JMenu menu = (JMenu) mm;
    		JMenuItem item;
    		Action ac;
    		Object mi;
    		for (int i = 0; i < menu.getMenuComponentCount(); i++) {
    			mi = menu.getMenuComponent(i);
    			if (mi instanceof JMenuItem) {
    				item = (JMenuItem) mi;
    				ac = item.getAction();
    				if (ac != null) {
    					if (actionId.equals(ac.getValue(Action.LONG_DESCRIPTION))) {
    						menu.remove(item);
    						if (menu == menuWindow) {
    							windowsGroup.remove(item);
    						} else if (menu == menuWaveform) {
    							waveFormGroup.remove(item);
    						}
    						return ac;
    					}
    				}
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Sets the action / menuitem identified by actionId selected. 
     * Note: this method might need to be extended with a boolean
     * argument for the selected state.
     * 
     * @param actionId the id of the action
     * @param menuId the id of the menu, a constant from #FrameConstants
     */
    public void setMenuSelected(String actionId, int menuId) {
    	if (actionId == null) {
    		return;
    	}
    	
    	Object mm = getMenuById(menuId);
    	if (mm instanceof JMenu) {
    		JMenu menu = (JMenu) mm;
    		JRadioButtonMenuItem item;
    		Action ac;
    		Object mi;
    		for (int i = 0; i < menu.getMenuComponentCount(); i++) {
    			mi = menu.getMenuComponent(i);
    			if (mi instanceof JRadioButtonMenuItem) {
    				item = (JRadioButtonMenuItem) mi;
    				ac = item.getAction();
    				if (ac != null) {
    					if (actionId.equals(ac.getValue(Action.LONG_DESCRIPTION))) {
    						item.setSelected(true);
    					}
    				}
    			} else if (mi instanceof JCheckBoxMenuItem) {
    				JCheckBoxMenuItem chItem = (JCheckBoxMenuItem) mi;
    				ac = chItem.getAction();
    				if (ac != null) {
    					if (actionId.equals(ac.getValue(Action.LONG_DESCRIPTION))) {
    						chItem.setSelected(true);
    					}
    				}
    			}
    		}
    	}
    }

    /**
     * Enables or disables a menu or menuitem, identified by meniId.
     * Most menu items can also be enabled/disabled through their 
     * ActionCommands.
     * 
     * @param menuId the identifier of the menu, a constant defined 
     * in FrameConstants
     * @param enabled the enabled state
     */
    public void setMenuEnabled(int menuId, boolean enabled) {
	    	JMenuItem menu = getMenuById(menuId);
	    	if (menu != null) {
	    		menu.setEnabled(enabled);
	    	}
    }

    /**
     * Update or re-initialize the menu with the specified id.
     * 
     * @param menuId the menu Id
     */
    public void updateMenu(int menuId) {
    	if (menuId == FrameConstants.MEDIA_PLAYER) {
    		// delagate to the responsible manager
    		pvMenuManager.reinitializePlayerMenu();
    	} else if (menuId == FrameConstants.WAVE_FORM_VIEWER) {
    		// delagate to the responsible manager
    		wfvMenuManager.reinitializeWaveFormMenu();
    	}
    }
    
    /**
     * Looks for an Action with the specified id (== the LONG_DESCRIPTION) under 
     * the menu identified by the menu id. If it is found it is removed from the menu 
     * and returned.
     * @param actionId the id of the Action, currently stored in the LONG_DESCRIPTION value
     * @param menuId the identifier of the menu, a constant from #FrameConstants
     * @return the action that has been removed from the menu
     */
    public void enableOrDisableMenus(List<String> actionIdList, int menuId, boolean enabled) {    	    	
    	Object mm = getMenuById(menuId);

    	if (mm instanceof JMenu) {
			JMenu menu = (JMenu) mm;			
			if(actionIdList == null){
				menu.setEnabled(enabled);
				return;
			}			

    		for (int i = 0; i < menu.getMenuComponentCount(); i++) {
    			Object mi = menu.getMenuComponent(i);
    			if (mi instanceof JMenuItem) {
    				JMenuItem item = (JMenuItem) mi;
    				Action ac = item.getAction();
    				String ca = item.getActionCommand();
    				if (ac != null & ca != null) {
    					if (actionIdList.contains(ca)) {
    						item.setEnabled(enabled);
    						ac.setEnabled(enabled);
    					}
    				}
    			}
    		}
    	}    			
    }
    
    /**
     * Enables or disables all commands that are not in a menu but are instead 
     * added to the menubar's action map (and are accessible through an accelerator
     * key). 
     *  
     * @param enable
     */
    public void enableCommands(boolean enable) {
    	ActionMap menuMap = menuBar.getActionMap();
    	if (menuMap != null) {
			Iterator<Object> keyIt = registeredActions.keySet().iterator();
			Object key;
			while (keyIt.hasNext()) {
				key = keyIt.next();
				registeredActions.get(key).setEnabled(enable);
			}
    		/*
    		if (!enable) {
    			menuMap.clear();
    		} else {
    			Iterator keyIt = registeredActions.keySet().iterator();
    			Object key;
    			while (keyIt.hasNext()) {
    				key = keyIt.next();
    				menuMap.put(key, (Action) registeredActions.get(key));
    			}
    		}
    		*/
    	}
    }
    
    /**
     * Returns the menu item identified by one of the menu constants in 
     * FrameConstants. This could be extended eventually to give access to 
     * any menu (item).<br>
     * Note: the mapping from frame constant to menu or menuitem might need 
     * to be reconsidered...
     * 
     * @param id the id of the menu, as defined in FrameConstants
     * @return the corresponding menu or menuitem
     */
    protected JMenuItem getMenuById(int id) {
    	switch(id) {
    	case FrameConstants.FILE:
    		return menuFile;
    	case FrameConstants.EDIT:
    		return menuEdit;
    	case FrameConstants.ANNOTATION:
    		return menuAnnotation;
    	case FrameConstants.TIER:
    		return menuTier;
    	case FrameConstants.TYPE:
    		return menuType;
    	case FrameConstants.SEARCH:
    		return menuSearch;
    	case FrameConstants.VIEW:
    		return menuView;
    	case FrameConstants.OPTION:
    		return menuOptions;
    	case FrameConstants.WINDOW:
    		return menuWindow;
    	case FrameConstants.HELP:
    		return menuHelp;
    	case FrameConstants.RECENT:
    		return menuRecentFiles;
    	case FrameConstants.EXPORT:
    		return menuExport;
    	case FrameConstants.IMPORT:
    		return menuImport;
    	case FrameConstants.LANG:
    		return menuAppLanguage;
    	case FrameConstants.PROPAGATION:
    		return menuChangeTimePropMode;
    	case FrameConstants.FRAME_LENGTH:
    	    return menuFrameLength;
    	case FrameConstants.ANNOTATION_MODE:
    		return menuItemAnnoMode;
    	case FrameConstants.SYNC_MODE:
    		return menuItemSyncMode;
    	case FrameConstants.KIOSK_MODE:
    		return menuItemKioskMode;
    	case FrameConstants.PLAY_AROUND_SEL:
    		return menuItemPlayAround;
    	case FrameConstants.RATE_VOL_TOGGLE:
    		return menuItemRateVol;
    	case FrameConstants.MEDIA_PLAYER:
    		return menuMediaPlayer;    	
    	case FrameConstants.WAVE_FORM_VIEWER:
    		return menuWaveform;    	
    	}
    	return null;
    }
    
    /**
     * This flag is set to true after loading of preferences, which runs on a separate
     * thread. Other classes can wait for this flag to be true before performing their
     * own actions, e.g. setting the media time.
     * 
	 * @return Returns the fullyInitialized flag.
	 */
	public boolean isFullyInitialized() {
		return fullyInitialized;
	}

    /**
     * Switches between "Metal" and the Mac native Look and Feel. "Metal" on
     * MacOS isn't always behaving well; menus and popup menus  often are not
     * updated correctly etc.
     */
    private void switchMacLF() {
        if (menuMacNativeLF != null) {
            if (menuMacNativeLF.getState()) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    Preferences.set("UseMacLF", Boolean.TRUE, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    Preferences.set("UseMacLF", Boolean.FALSE, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Mac OS X specific handling of the main (screen) menu Quit application
     * event. Implementation of MacApplicationListener.
     */
    public void macHandleQuit() {
    	FrameManager.getInstance().exit();
    }

    /**
     * Mac OS X specific handling of the main (screen) menu About application
     * event. Implementation of MacApplicationListener.
     */
    public void macHandleAbout() {
    	if (menuActions != null) {
//    		MenuAction ma = null;    		
//    		for (int i = 0; i < menuActions.size(); i++) {
//    			ma = (MenuAction) menuActions.get(i);
//    			if (ma instanceof AboutMA) {
//    				ma.actionPerformed(null);
//    				break;
//    			}
//    		}
    		
    		MenuAction ma = null;  
    		Iterator<MenuAction> it = menuActions.values().iterator();
    		while(it.hasNext()){
    			ma = it.next();
    			if (ma instanceof AboutMA) {
    				ma.actionPerformed(null);
    				break;
    			}
    		}
    	}
    }
    
    /**
     * Mac specific handling of Preferences from the main menu bar.
     */
    public void macHandlePreferences() {
    	MenuAction ma = null;
	    	if (menuActions != null) {
	    		
//	    		for (int i = 0; i < menuActions.size(); i++) {
//	    			ma = (MenuAction) menuActions.get(i);
//	    			if (ma instanceof EditPreferencesMA) {
//	    				ma.actionPerformed(null);
//	    				break;
//	    			}
//	    		}
	    		Iterator<MenuAction> it = menuActions.values().iterator();
	    		while(it.hasNext()){
	    			ma = it.next();
	    			if (ma instanceof EditPreferencesMA) {
	    				ma.actionPerformed(null);
	    				break;
	    			}
	    		}
	    	} 
	    	if (!(ma instanceof EditPreferencesMA)) {
	    		MenuAction ma2 = new EditPreferencesMA("Menu.Edit.Preferences.Edit", this);
	    		ma2.actionPerformed(null);
	    	}
    }

    /**
     * Listener for ElanFrame WindowEvents
     */
    private class ElanFrameWindowListener extends WindowAdapter implements ComponentListener {
        // triggered when the window is closed to quit the application
        // handle warnings and save operations in doExit();
        // EXIT WIL BE REPLACED BY CLOSE
        @Override
		public void windowClosing(WindowEvent e) {
            //doExit();
        	if (transcriptionForThisFrame != null) {
        		// do nothing if this is an empty frame because a new
        		// empty frame would be created
        		checkSaveAndClose();	
        	} else {
        		// 07-2007 changed behaviour: with one empty window, exit the 
        		// application when the close button is pressed
        		doClose(true);
        	}
        	
        }
        
        /**
         * Notifies the FrameManager that this frame has been activated.
         * Menus will be updated. 
         */
		@Override
		public void windowActivated(WindowEvent e) {
			FrameManager.getInstance().frameActivated(ElanFrame2.this);
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// ignore
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// store location
			if (transcriptionForThisFrame != null) {
				// for the time being store every change in location, not only when closing
				// (until there is a more elaborate preferences changed event mechanism 
				Preferences.set("FrameLocation", getLocation(), transcriptionForThisFrame, false, false);
			} else {
				// store a global preference, or not here?
				// if so, the if-else block can be collapsed into a single statement 
				Preferences.set("FrameLocation", getLocation(), null, false, false);
			}
		}

		@Override
		public void componentResized(ComponentEvent e) {
			// store size
			if (transcriptionForThisFrame != null) {
				// for the time being store every change in size, not only when closing
				// (until there is a more elaborate preferences changed event mechanism 
				Preferences.set("FrameSize", getSize(), transcriptionForThisFrame, false, false);
			} else {
				// store a global preference, or not here?
				// if so, the if-else block can be collapsed into a single statement 
				Preferences.set("FrameSize", getSize(), null, false, false);
			}
		}

		@Override
		public void componentShown(ComponentEvent e) {
			// ignore
		}
		
    }

    @Override
    public void finalize() throws Throwable {
    	    System.out.println("Finalize ELAN window...");
    	    super.finalize();
    }
    
    public boolean isIntialized(){
    	return initialized;
    }
    
    /*
       private class InitThread extends Thread {
           final String path;
           InitThread(String eafPath) {
               path = eafPath;
           }
           public void run() {
               final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(
                   ElanFrame2.this,
                   true,
                   ElanLocale.getString("Frame.ElanFrame.Progress.Open") + path,
                   false,
                   null);
               new Thread(new Runnable(){
                   public void run() {
                       monitor.show();
                   }
               }).start();
               //monitor.show();
               ElanFrame2.this.initElan();
               monitor.close();
           }
       }
     */
}
