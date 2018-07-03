package mpi.eudico.client.annotator.smfsearch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableFrame;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.prefs.MultipleFileDomains;
import mpi.eudico.client.annotator.search.viewer.EAFMultipleFileUtilities;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.search.SearchLocale;
import nl.mpi.annot.search.mfsearch.SearchApplication;
import nl.mpi.annot.search.mfsearch.SearchApplicationMediator;


/**
 * A frame for structured search in multiple local annotation (eaf) files.
 * This is the ELAN version of Annex search. This class implements Annex'
 * SearchApplication interface to communicate with Annex functionality.
 * A separate class that implements SearchApplication instead of this Frame 
 * might be created later.
 * 
 * @author HS
 * @version 1.0
 */
@SuppressWarnings("serial")
public class StructuredMultipleFileSearchFrame extends ClosableFrame
    implements SearchApplication {
    /** prefs key for directories */
    protected static final String PREFERENCES_DIRS_KEY = "MultipleFileSearchDirs";

    /** prefs key for annotation files */
    protected static final String PREFERENCES_PATHS_KEY = "MultipleFileSearchPaths";
    protected static final String PREFERENCES_LAST_DOMAIN = "LastUsedMFSearchDomain";
    private List<String> searchDirs;
    private List<String> searchPaths;
    private File[] searchFiles;
    private JComponent defPanel;
    //private JPanel resPanel;

    /**
     * Creates a new StructuredMultipleFileSearchFrame instance
     *
     * @param elanFrame the parent frame
     */
    public StructuredMultipleFileSearchFrame(ElanFrame2 elanFrame) {
        super(SearchLocale.getString("MultipleFileSearch.Title"));

        ImageIcon icon = new ImageIcon(this.getClass()
                                           .getResource("/mpi/eudico/client/annotator/resources/ELAN16.png"));

        if (icon != null) {
            setIconImage(icon.getImage());
        }

        ArrayList<File> curDomain = loadDomain();

        if (curDomain == null) {
            return;
        }

        // initialize mediator and panels
        SearchApplicationMediator mediator = new SearchApplicationMediator(this,
                curDomain);
        defPanel = mediator.getSearchComponent();
        //resPanel = mediator.getSearchResultPanel();

        initComponents();
        pack();
        postInit();
        setLocationRelativeTo(elanFrame);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //setVisible(true);
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 2, 2, 2);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        getContentPane().add(defPanel, gbc);
    }
    
    /**
     * Adjust the size of the frame if necessary.
     */
    private void postInit() {
    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((getSize().width > dim.width - 40) ? dim.width - 40 : getSize().width,
            (getSize().height > dim.height - 40) ? dim.height - 40 : getSize().height);
    }

    /**
     * Loads the stored files and folders. If no domain has been specified
     * before the user will  be prompted to add files to the domain. The ui
     * will not be instantiated if there are no files to be searched.
     *
     * @return a list of eaf files
     * 
     * Because getDomain must return an ArrayList due to inheritance,
     * and new SearchApplicationMediator() requires it,
     * it is easier to do that here as well.
     */
    private ArrayList<File> loadDomain() {
    	// check the last used domain
    	String domainName = Preferences.getString(PREFERENCES_LAST_DOMAIN, null);
    	
    	if (domainName != null) {
            Map<String, List<String>> domain = MultipleFileDomains.getInstance()
            		.getDomain(domainName);

			if (domain != null) {
				List<String> dirs = domain.get(domainName +
						MultipleFileDomains.DIR_SUF);
				
				if (dirs != null) {
					searchDirs = dirs;
					//dirs = new ArrayList<String>(0);
				}
				
				List<String> paths = domain.get(domainName +
						MultipleFileDomains.PATH_SUF);
				
				if (paths != null) {
					searchPaths = paths;
					//paths = new ArrayList<String>(0);
				}
			}
    	}
    	
    	if (searchDirs == null && searchPaths == null) {
	        // initialize lists for directories and files from preferences
    		List<String> listPref = Preferences.getListOfString(PREFERENCES_DIRS_KEY, null);
	        searchDirs = listPref != null ? listPref
	        		                      : new ArrayList<String>(0);
    		listPref = Preferences.getListOfString(PREFERENCES_PATHS_KEY, null);
	        searchPaths = listPref != null ? listPref 
	        		                       : new ArrayList<String>(0);
    	} else {
    		if (searchDirs == null) {
    			searchDirs = new ArrayList<String>(0);
    		}
    		if (searchPaths == null) {
    			searchPaths = new ArrayList<String>(0);
    		}
    	}

        searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs,
                searchPaths);

        if (searchFiles.length == 0) {
            EAFMultipleFileUtilities.specifyDomain(this, searchDirs, searchPaths);
            searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs,
                    searchPaths);

            if (searchFiles.length == 0) {
                return null;
            }
        }

        ArrayList<File> domain = new ArrayList<File>(searchFiles.length);

        for (File searchFile : searchFiles) {
            domain.add(searchFile); // or add the path?
        }

        return domain;
    }

    /**
     * Opens the specified file in the viewer application (i.e. ELAN)
     * activating the annotation at the specified time, on the specified tier.
     *
     * @param filePath the file path
     * @param tierName the name of the tier
     * @param beginTime begin time of the annotation
     * @param endTime end time of the annotation
     */
    @Override
	public void showInViewer(String filePath, final String tierName,
        final long beginTime, final long endTime) {
        if (filePath != null) {
            final ElanFrame2 newElanFrame = FrameManager.getInstance()
                                                        .getFrameFor(filePath);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (newElanFrame != null) {
                if (newElanFrame.getViewerManager() != null && newElanFrame.isFullyInitialized()) {
                    newElanFrame.getViewerManager().getSelection()
                                .setSelection(beginTime, endTime);
                    newElanFrame.getViewerManager().getMasterMediaPlayer()
                                .setMediaTime(beginTime);

                    TierImpl t = (TierImpl) newElanFrame.getViewerManager()
                                                        .getTranscription()
                                                        .getTierWithId(tierName);

                    if (t != null) {
                        Annotation ann = t.getAnnotationAtTime(beginTime);

                        if (ann != null) {
                            newElanFrame.getViewerManager().getActiveAnnotation()
                                        .setAnnotation(ann);
                        }
                    }
                } else {

                    new Thread(new Runnable() {// new thread, this doesn't work on the eventqueue with invokeLater
                            @Override
							public void run() {        
                            	  // check initialization of frame, use a time out period
                            	  long timeOut = System.currentTimeMillis() + 30000;
                            	  while (!newElanFrame.isFullyInitialized() && System.currentTimeMillis() < timeOut) {                         		  
                            		  try {
                            			  Thread.sleep(200);
                            		  } catch (InterruptedException ie) {
                            			  
                            		  }
                            	  }

                                newElanFrame.getViewerManager().getSelection()
                                            .setSelection(beginTime, endTime);
                                newElanFrame.getViewerManager()
                                            .getMasterMediaPlayer()
                                            .setMediaTime(beginTime);

                                TierImpl t = (TierImpl) newElanFrame.getViewerManager()
                                                                    .getTranscription()
                                                                    .getTierWithId(tierName);

                                if (t != null) {
                                    Annotation ann = t.getAnnotationAtTime(beginTime);

                                    if (ann != null) {
                                        newElanFrame.getViewerManager()
                                                    .getActiveAnnotation()
                                                    .setAnnotation(ann);
                                    }
                                }
                            }
                        }).start();
                }

                newElanFrame.toFront();
                this.toFront();
            }
        }
    }

    /**
     * Returns a list of annotation files (File objects).
     * 
     * @version June 2009 if there are stored domains first prompt whether to 
     * load an existing domain or create a new one.
     *   
     * @return a list of file objects or null if no annotation file is in the
     *         domain. When null is returned this is interpreted as no change
     *         to the search domain
     */
    @Override
	public ArrayList<File> getDomain() {
    	// prompt with a list of domains
    	// if one is picked load that domain, otherwise continue with 
    	// "new domain prompt"
    	MFDomainDialog mfDialog = new MFDomainDialog(this, 
    			ElanLocale.getString("MultipleFileSearch.SearchDomain"), true);
    	mfDialog.setSearchDirs(searchDirs);
    	mfDialog.setSearchPaths(searchPaths);
    	mfDialog.setVisible(true);
    	searchDirs = mfDialog.getSearchDirs();
    	searchPaths = mfDialog.getSearchPaths();
    	
        //EAFMultipleFileUtilities.specifyDomain(this, searchDirs, searchPaths);
        searchFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs,
                searchPaths);

        if (searchFiles.length == 0) {
            return null;
        }
        
        ArrayList<File> domain = new ArrayList<File>(searchFiles.length);

        for (File searchFile : searchFiles) {
        	if (searchFile != null) {
        		domain.add(searchFile);
        	}          
        }

        if (domain.size() == 0) {
        	return null;
        }
        
        return domain;
    }

    /**
     * Return the default background color.
     *
     * @return the default background color
     */
    public Color getBackgroundColor() {
        return UIManager.getColor("Panel.background");
    }
    
    /**
     * Store a key/value preference pair in a persistent manner
     * 
     * @param key the key
     * @param value the value
     * @see #getPersistent(String)
     */
    @Override
	public void putPersistent(String key, String value) {
    	// maybe these preferences should be added to a map that in
    	// turn is added to the preferences
    	Preferences.set(key, value, null);
    }
    
    /**
     * Get a value String for a key from the persistent store used by putPersistent
     * 
     * @param key the key
     * @return the value String, null if it does not exist
     * @see #putPersistent(String, String)
     */
    @Override
	public String getPersistent(String key) {
    	// maybe the string should be retrieved from a map (that is retrieved
    	// from the preferences) rather than from the preferences directly
    	return Preferences.getString(key, null);
    }
    
    /**
     * Delete a key/value pair from the persistent store used by putPersistent
     * 
     * @param key the key
     */
    @Override
	public void deletePersistent(String key) {
    	Preferences.set(key, null, null);//??
    }
    
    /**
     * Requests the search application to show a file chooser and return the selected or the specified file path. 
     *  
     * @param parent parent component
     * @param title title for the dialog
     * @param dialogType OPEN or SAVE
     * @param extensions file extensions
     * @param prefKey a key for saving and retrieving last used folder
     * 
     * @return the absolute path to the selected or specified file.
     */
    public String getFilePathForIO(Component parent, String title, String dialogType, 
    		String[] extensions, String prefKey) {
    	FileChooser fc = new FileChooser(parent);
		if (prefKey == null) {
			prefKey = "";
		}
		File selectedFile = null;
		
    	if ("OPEN".equals(dialogType)) {
    		fc.createAndShowFileDialog(title, FileChooser.OPEN_DIALOG, FileExtension.XML_EXT, prefKey);
    		selectedFile = fc.getSelectedFile();
    	} else if ("SAVE".equals(dialogType)) {
    		fc.createAndShowFileDialog(title, FileChooser.SAVE_DIALOG, FileExtension.XML_EXT, prefKey);
    		selectedFile = fc.getSelectedFile();
    	}
    	
		if (selectedFile != null) {
			return selectedFile.toString();
		} // else return null
		
    	return null;
    }
}
