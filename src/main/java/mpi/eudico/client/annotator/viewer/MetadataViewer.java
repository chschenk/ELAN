package mpi.eudico.client.annotator.viewer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesUser;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.dcr.ISOCATLanguageCodeMapping;
import mpi.eudico.client.annotator.linkedmedia.LinkedFileDescriptorUtil;
import mpi.eudico.client.annotator.linkedmedia.LinkedFilesDialog;
import mpi.eudico.client.annotator.md.DefaultMDViewerComponent;
import mpi.eudico.client.annotator.md.MDConfigurationDialog;
import mpi.eudico.client.annotator.md.spi.MDContentLanguageUser;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDServiceRegistry;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;


/**
 * A viewer that is able to display metadata information relevant for the
 * current transcription document.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MetadataViewer extends JPanel implements PreferencesUser,
    ElanLocaleListener, Viewer, ActionListener {
    private ViewerManager2 viewerManager;
    private String metadataPath;
    private MDServiceProvider mdProvider;
    private MDViewerComponent viewerPanel;

    // ui elements
    private JButton selectMDButton;
    private JButton configureMDButton;
    private JLabel mdPathLabel;
    private JPanel viewerContainer;

    /** constant for metadata source */
    public final String MD_SOURCE = "MetadataSource";

    /** constant for metadata keys */
    public final String MD_KEYS = "MetadataKeys";
    
    private String prefContentLanguage;
	private boolean preferenceIsOutdated;

    /**
     * Creates a new MetadataViewer instance
     *
     * @param viewerManager the viewer manager
     */
    public MetadataViewer(ViewerManager2 viewerManager) {
        super();
        this.viewerManager = viewerManager;
        preferenceIsOutdated = false;
        initComponents();
        linkedFilesChanged();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        selectMDButton = new JButton();
        mdPathLabel = new JLabel();
        mdPathLabel.setFont(mdPathLabel.getFont().deriveFont(10f));
        configureMDButton = new JButton();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        add(selectMDButton, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 6, 0, 0);
        add(mdPathLabel, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        add(configureMDButton, gbc);

        updateLocale();
        selectMDButton.addActionListener(this);
        configureMDButton.addActionListener(this);
        configureMDButton.setEnabled(false);
        mdPathLabel.setText(ElanLocale.getString(
                "MetadataViewer.NoMetadataSource"));

        viewerContainer = new JPanel(new GridBagLayout());
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 0, 0, 0);
        add(viewerContainer, gbc);
    }
    
    private void popupLinkedFilesDialog() {
    	LinkedFilesDialog dialog = new LinkedFilesDialog(viewerManager.getTranscription());
    	dialog.selectSecLinkedFilesPanel();
    	dialog.setVisible(true);
    }
    
    /**
     * Removes the current link to a metadata source and resets the UI.
     */
    private void removeMDSource() {
    	if (metadataPath == null) {
    		return;
    	}
    	
    	metadataPath = null;
        setPreference(MD_SOURCE, metadataPath, viewerManager.getTranscription());// is this enough to remove the pref?
        
        if (viewerPanel != null) {
        	viewerContainer.remove((Component) viewerPanel);
        	viewerContainer.repaint();
        	viewerContainer.revalidate();
        	viewerPanel = null;
        }
        mdProvider = null;
        mdPathLabel.setText(ElanLocale.getString(
                "MetadataViewer.NoMetadataSource"));
        configureMDButton.setEnabled(false);
    }

    /**
     * Allows selection of metadata keys and values to display.
     */
    private void configureMD() {
        if (mdProvider == null) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("MetadataViewer.NoMetadataSource"),
                ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
        }

        // show configure dialog
        MDConfigurationDialog dialog = new MDConfigurationDialog(ELANCommandFactory.getRootFrame(viewerManager.getTranscription()), 
        		mdProvider.getConfigurationPanel());
        dialog.setVisible(true);

        List<String> selKeys = mdProvider.getSelectedKeys();

        if ((selKeys == null) || (selKeys.size() == 0)) {
            return;
        }

        // store preferences
        setPreference(MD_KEYS, selKeys, viewerManager.getTranscription());

        /*
           int rowCount = model.getRowCount();
           for (int i = rowCount - 1; i >= 0; i--) {
               model.removeRow(i);
           }
         */
        Map<String, String> allSelKeysVals = mdProvider.getSelectedKeysAndValues();

        if (allSelKeysVals != null) {
            if (viewerPanel != null) {
                viewerPanel.setSelectedKeysAndValues(allSelKeysVals);
            }
        }

        /*
           Iterator keyIt = allSelKeysVals.keySet().iterator();
           Object key, val;
           while (keyIt.hasNext()) {
               key = keyIt.next();
               // hier... store key in preferences
               val = allSelKeysVals.get(key);
               //System.out.println("K: " + key + "\n\tV: " + val);
               model.addRow(new Object[]{key, val});
           }
         */

        /*
           List allVals;
           String key;
           for (int i = 0; i < selKeys.size(); i++) {
               key = (String) selKeys.get(i);
               System.out.println("K: " + key);
               allVals = mdProvider.getValues(key);
               if (allVals != null) {
                   for (int j = 0; j < allVals.size(); j++) {
                       System.out.println("\tV: " + allVals.get(j));
                   }
               }
           }
         */
    }
    
    /**
     * If the current provider supports change in content language it is informed of 
     * the current preferred language. 
     */
    private void updateProviderLanguage() {
    	if (mdProvider instanceof MDContentLanguageUser) {
    		String lang3L = null;
        	String lang2L = null;
        	
        	if (prefContentLanguage != null) {
        		if (prefContentLanguage.length() == 2) {// can this happen?
        			lang2L = prefContentLanguage;
        		} else {
	        		// get language information from the collection
	        		LangInfo info = LanguageCollection.getLanguageInfo(prefContentLanguage);
	        		
	        		if (info != null) {
	    				lang3L = info.getId();
	    			}
	        		
	        		if (lang3L != null) {
	        			lang2L = ISOCATLanguageCodeMapping.get2LetterLanguageCode(lang3L);
	        		}
        		}
        	}
    		
    		((MDContentLanguageUser) mdProvider).setContentLanguage(prefContentLanguage, lang3L, lang2L);
    	}
    }

    /**
     * Stores preferences, such as which metadata fields and values to display.
     *
     * @param key the preference key
     * @param value the value
     * @param document the transcription
     */
    @Override
	public void setPreference(String key, Object value, Object document) {
        if (document instanceof Transcription) {
            Preferences.set(key, value, (Transcription) document, false, false);
        } else {
            Preferences.set(key, value, null, false, false);
        }
    }
    
    /**
     * Prepend the given path to the linked files, with the mimetype for CMDI or IMDI files.
     * 
     * @param path
     * @param document
     */
    private void prependLinkedMetaDataPath(String path, Transcription document) {
    	LinkedFileDescriptor lfd = LinkedFileDescriptorUtil.createLFDescriptor(path);
    	List<LinkedFileDescriptor> lfds = document.getLinkedFileDescriptors();
    	lfds.add(0, lfd);
    	document.setChanged();
    	// Don't use LinkedFilesUtil.updateLinkedFiles() here:
    	// it would call us back to happily let us know about the update,
    	// even though we know already because we instigated it.
    }

    /**
     * Get the first linked file which has the correct mimetime for a CMDI or IMDI file.
     * 
     * @param document
     * @return
     */
    private String getFirstLinkedMetaDataPath(Transcription document) {
    	List<LinkedFileDescriptor> lfds = document.getLinkedFileDescriptors();

    	for (LinkedFileDescriptor lfd : lfds) {
    		if (LinkedFileDescriptor.CMDI_TYPE.equals(lfd.mimeType) ||
    				LinkedFileDescriptor.IMDI_TYPE.equals(lfd.mimeType)) {
	    		preferenceIsOutdated = true;
    			return lfd.linkURL;    			
    		}
    	}
    	
    	// Compatibility: check if there is an old preferences setting for it.
    	// If so, put it in the linked files (and remove the preference?).
    	// Do this only once per session, to avoid removed files coming back.
    	// Don't do it at all if we ever used a linked file, for the same reason.
    	if (!preferenceIsOutdated) {
	    	String path = Preferences.getString(MD_SOURCE, document);
	
	    	if (path != null) {
	    		prependLinkedMetaDataPath(path, document);
	    		//Preferences.set(MD_SOURCE, null, document, false, false);
	    		preferenceIsOutdated = true;
	
	    		return path;
	    	}
		}
    	return null;
    }
    
    /**
     * Get the first linked file which has the correct mimetime for a CMDI or IMDI file.
     * 
     * @param document
     * @return
     */
    public void removeFirstLinkedMetaDataPath(Transcription document) {
    	List<LinkedFileDescriptor> lfds = document.getLinkedFileDescriptors();
    	Iterator<LinkedFileDescriptor> iter = lfds.iterator();
    	
    	while (iter.hasNext()) {
    		LinkedFileDescriptor lfd = iter.next();
    		
    		if (LinkedFileDescriptor.CMDI_TYPE.equals(lfd.mimeType) ||
    				LinkedFileDescriptor.IMDI_TYPE.equals(lfd.mimeType)) {
    			iter.remove();
    			return;    			
    		}
    	}
    	
    }
    
    /**
     * Method to call when the list of linked files changes.
     * We look at the list and see if we now have a different metadata file.
     */
    public void linkedFilesChanged() {
    	String newFile = getFirstLinkedMetaDataPath(viewerManager.getTranscription());
    	metadataFileChangedMaybe(newFile);
    }

    /**
     * Notification of a change in the preferences. Also called after opening a
     * file to restore state.
     */
    @Override
	public void preferencesChanged() {
        String lang = Preferences.getString(Preferences.PREF_ML_LANGUAGE, null);
        
        if (lang != null) {// iso URL id
        	if (prefContentLanguage == null || !prefContentLanguage.equals(lang)) {
        		prefContentLanguage = lang;
        		updateProviderLanguage();
        	}
        } else if (prefContentLanguage != null) {
        	updateProviderLanguage();
        }        
    }
    
    /**
     * Common point where to go if the metadata file name has been changed or removed. 
     */
    
    private void metadataFileChangedMaybe(String newFile) {
    	if (newFile == null) {
			removeMDSource();
			return;
    	}

    	newFile = FileUtility.urlToAbsPath(newFile);

        if (newFile.startsWith(".")) {
        	newFile = newFile.replace("\\", "/");
        	String eafPath = ((TranscriptionImpl)viewerManager.getTranscription()).getFullPath();
        	eafPath = FileUtility.urlToAbsPath(eafPath);
        	eafPath = eafPath.replace("\\", "/");
        	ClientLogger.LOG.info(eafPath);
        	ClientLogger.LOG.info(metadataPath);
        	newFile = FileUtility.getAbsolutePath(eafPath, newFile);
        }
        /* --- END --- */

        if (newFile.equals(metadataPath)) {
    		return;
    	}
    	
    	metadataPath = newFile;
    	
        if ((mdProvider == null) ||
                !metadataPath.equals(mdProvider.getMetadataFile())) {
            mdProvider = MDServiceRegistry.getInstance()
                                          .getProviderForMDFile(metadataPath);

            if (mdProvider == null) {
                configureMDButton.setEnabled(false);
                return;
            }

            MDViewerComponent oldPanel = viewerPanel;
            configureMDButton.setEnabled(mdProvider.isConfigurable());
            mdPathLabel.setText(metadataPath);
            viewerPanel = mdProvider.getMDViewerComponent();

            if (viewerPanel == null) {
                viewerPanel = new DefaultMDViewerComponent(mdProvider);
            }
            // remove the old one
            if (oldPanel instanceof Component) {
            	viewerContainer.remove((Component) oldPanel);
            }
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            viewerContainer.add((Component) viewerPanel, gbc);
        }

        // only look for defined keys if there is a provider
        List<String> pref = null;

        // Let IMDI settings from the Preferences panel win, if they exist.
        if ("IMDI".equals(mdProvider.getMDFormatDescription())) {
            // try globally specified keys, this has a different key in the preferences
            pref = Preferences.getListOfString("Metadata.IMDI.Defaults", null);
        } 
        if (pref == null) {
            pref = Preferences.getListOfString(MD_KEYS, viewerManager.getTranscription());
        }
        
        if (pref != null) {
            mdProvider.setSelectedKeys(new ArrayList<String>(pref));
            
            Map<String, String> allSelKeysVals = mdProvider.getSelectedKeysAndValues();

            if (allSelKeysVals != null) {
                if (viewerPanel != null) {
                    viewerPanel.setSelectedKeysAndValues(allSelKeysVals);
                }
            }
        }   
        
        updateProviderLanguage();
    }

    /**
     * Notification of a change in ui language.
     */
    @Override
	public void updateLocale() {
        selectMDButton.setText(ElanLocale.getString(
                "MetadataViewer.SelectSource"));
        configureMDButton.setText(ElanLocale.getString(
                "MetadataViewer.Configure"));
        if (viewerPanel != null) {
        	viewerPanel.setResourceBundle(ElanLocale.getResourceBundle());
        }
    }

    /**
     * Returns the viewer manager.
     *
     * @return the viewer manager
     */
    @Override
	public ViewerManager2 getViewerManager() {
        return viewerManager;
    }

    /**
     * Sets the viewer manager.
     *
     * @param viewerManager the viewer manager
     */
    @Override
	public void setViewerManager(ViewerManager2 viewerManager) {
        this.viewerManager = viewerManager;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectMDButton) {
        	popupLinkedFilesDialog();
        } else if (e.getSource() == configureMDButton) {
            configureMD();
        }
    }
}
