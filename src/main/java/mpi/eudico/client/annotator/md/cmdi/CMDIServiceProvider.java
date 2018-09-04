package mpi.eudico.client.annotator.md.cmdi;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import mpi.dcr.DCSmall;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.dcr.ELANLocalDCRConnector;
import mpi.eudico.client.annotator.md.imdi.MDKVData;
import mpi.eudico.client.annotator.md.spi.MDConfigurationPanel;
import mpi.eudico.client.annotator.md.spi.MDContentLanguageUser;
import mpi.eudico.client.annotator.md.spi.MDServiceProvider;
import mpi.eudico.client.annotator.md.spi.MDViewerComponent;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.type.ControlledVocabularyItem;
import nl.mpi.metadata.cmdi.api.CMDIApi;
import nl.mpi.metadata.cmdi.api.model.CMDIContainerMetadataElement;
import nl.mpi.metadata.cmdi.api.model.CMDIMetadataElement;
import nl.mpi.metadata.cmdi.api.type.impl.CMDIControlledVocabularyItemImpl;
import nl.mpi.metadata.cmdi.api.type.impl.ControlledVocabularyElementTypeImpl;

/**
 * A metadata service provider for visualization of CMDI metadata.
 * Uses the MPI-TLA CMDIApi.
 * Note: as a result of the current behavior of the MDServiceRegistry a provider is never 
 * re-used for another file of the same type. In principle this class is ready for re-use
 * but this might need checking if the registry behavior changes.  
 *  
 * @author Han Sloetjes
 */
public class CMDIServiceProvider implements MDServiceProvider, MDContentLanguageUser {
	private String sourcePath;
	private MetadataDocument cmdiDoc;
	/** for offline mode, loads the xml and creates a tree of the first Component */
	private CMDIDom simpleDom;
	private CMDIViewerPanel viewerPanel;
	private String twoLetterLanguageCode;
	private DefaultMutableTreeNode rootTreeNode;
	private boolean isLoading = false;
	private final ReentrantLock treeLock = new ReentrantLock();
	// concept registry related
	private final String HANDLE = "hdl.handle.net";
	private final Pattern HANDLE_PAT;
	
	/**
	 * Constructor
	 */
	public CMDIServiceProvider() {
		super();
		HANDLE_PAT = Pattern.compile("CCR_C-");
	}

	/**
	 * Checks the extension of the specified file or URL.
	 *  
	 * @see mpi.eudico.client.annotator.md.spi.MDServiceProvider#setMetadataFile(java.lang.String)
	 * 
	 * @param filePath a path to a local file or the URI of a metadata file 
	 * @return true if the specified file seems to be a CMDI file
	 */
	@Override
	public boolean setMetadataFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        File f = new File(filePath);

        if (!f.exists()) {

            ClientLogger.LOG.warning("The CMDI metadata file does not exist: " +
                filePath);
            return false;
        }
        
        if (!f.canRead()) {
            ClientLogger.LOG.warning("The CMDI metadata file cannot be read: " +
                filePath);

            return false;
        }

        if (f.isDirectory()) {
            ClientLogger.LOG.warning("The path is a directory not a CMDI file: " + filePath);

            return false;
        }

        if (f.length() == 0) {
            ClientLogger.LOG.warning("The CMDI metadata file has zero length: " +
                filePath);

            return false;
        }
        
        // check extension
        boolean extFound = false;
        String lowerPath = filePath.toLowerCase();       
        
        for (String ext : FileExtension.CMDI_EXT) {
        	if (lowerPath.endsWith(ext)) {
        		extFound = true;
        		break;
        	}
        }
        
        if (extFound) {
        	// perform a quick test for <CMD ?? to see if it really is cmdi?
        	sourcePath = filePath;
        	// set rootTreeNode to null so that a tree will be created when necessary
        	rootTreeNode = null;// or update the panel immediately?
        	return true;
        }
        
		return false;
	}

	/**
	 * @return the path to CMDI metadata file or null if not specified
	 */
	@Override
	public String getMetadataFile() {
		return sourcePath;
	}

	/**
	 * @return the description of the format, CMDI
	 */
	@Override
	public String getMDFormatDescription() {
		return "CMDI";
	}

	/**
	 * @return the value for the specified key or null if key was null or was not in the document
	 */
	@Override
	public String getValue(String key) {
		// TODO 
		return null;
	}

	/**
	 * @return a list of values for the specified key or null if key is null or was not found
	 */
	@Override
	public List<String> getValues(String key) {
		// TODO 
		return null;
	}

	/**
	 * 
	 * @return a list of all metadata keys or null if no metadata were found or recognized.
	 */
	@Override
	public List<String> getKeys() {
		// TODO 
		return null;
	}

	/**
	 * 
	 * @return a list of selected keys or null if no keys are selected (i.e. all keys are used)
	 */
	@Override
	public List<String> getSelectedKeys() {
		// TODO 
		return null;
	}

	/**
	 * Sets the selected keys. Only if the provider is configurable.
	 * 
	 * @param selectedKeys a list of selected keys
	 */
	@Override
	public void setSelectedKeys(List<String> selectedKeys) {
		// TODO 

	}

	/**
	 * Returns a map of selected keys and their values. If nothing can be configured it returns all keys.
	 * 
	 * @return a map of selected keys and their values
	 */
	@Override
	public Map<String, String> getSelectedKeysAndValues() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 *  
	 * @return the root of a tree representation of the CMDI
	 */
	public DefaultMutableTreeNode getAsTree() {
		if (isLoading) {
			return new DefaultMutableTreeNode(ElanLocale.getString("MetadataViewer.Loading.CMDI"));
		}
		
		if (cmdiDoc != null) {
			if (rootTreeNode != null) {
				return rootTreeNode;
			}
			//long now = System.currentTimeMillis();
			treeLock.lock();
			try {
				DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
				MDKVData mdkvData = getDataObject(cmdiDoc.getName(), cmdiDoc.getDisplayValue(), null, null);
				rootNode.setUserObject(mdkvData);
				
				List<CMDIMetadataElement> children = cmdiDoc.getChildren();
				Iterator<CMDIMetadataElement> chIt = children.iterator();
				
				while (chIt.hasNext()) {
					CMDIMetadataElement nextElem = chIt.next();
					addChildElement(nextElem, rootNode);
				}
				
				rootTreeNode = rootNode;
			} finally {
				treeLock.unlock();
			}
			//System.out.println("Creating tree took: " + (System.currentTimeMillis() - now) + " ms");
			return rootTreeNode;
		} else if (simpleDom != null) {
			if (rootTreeNode != null) {
				return rootTreeNode;
			}
			treeLock.lock();
			try {
				rootTreeNode = simpleDom.getAsTree();
			} finally {
				treeLock.unlock();
			}
			
			return rootTreeNode;
		}
		
		return null;
	}

	/**
	 * Returns false for the time being.
	 * 
	 * @return false for now
	 */
	@Override
	public boolean isConfigurable() {
		return false;
	}

	/**
	 * @return null for the time being
	 */
	@Override
	public MDConfigurationPanel getConfigurationPanel() {
		return null;
	}

	/**
	 * @return a {@link #CMDIViewerPanel} with the default representation of the metadata as a tree
	 */
	@Override
	public MDViewerComponent getMDViewerComponent() {
		if (viewerPanel == null) {
			viewerPanel = new CMDIViewerPanel(this);
		}
		return viewerPanel;
	}

	/**
	 * Initializes the CMDI API.
	 * Currently the API throws an exception when used offline (the schema cannot be accessed).
	 * In that case the XML is read "as is" and switching language has no effect.
	 * 
	 *  Since loading can take a considerable time try to do it in a separate thread
	 */
	@Override
	public void initialize() {
		if (sourcePath == null) {
			return;
		}
		
		isLoading = true;
		Thread loadThread = new Thread(new CMDILoader());
		loadThread.setPriority(Thread.MIN_PRIORITY);
		loadThread.start();
	}
	
	/**
	 * Notification of background loading of CMDI document. 
	 */
	private void delayedLoadingComplete() {
		isLoading = false;
		if (viewerPanel != null) {
			EventQueue.invokeLater(new PanelUpdater());
		}
	}
	
	// copied from ImdiDoc
    private MDKVData getDataObject(String key, String value, String datCatID, String valueDatCatID) {
    	if (value != null) {
    		value = value.trim();
    		if (value.length() == 1 && value.charAt(0) == '\n') {
    			value = null;
    		}
    	}
//    	if (valueDatCatID != null) {
//    		System.out.println("Value: " + valueDatCatID + " - " + value);
//    	}
    	datCatID = handleToDCId(datCatID);
    	valueDatCatID = handleToDCId(valueDatCatID);
    	
    	return new CMDIKVData(key, value, datCatID, valueDatCatID);
    }
    
    /**
     * The CLARIN Component Registry switched from ISOcat DC id's to Concept Registry handle uri's.
     * The ID of converted data categories is part of the handle production. 
     * This method tries to extract the ISOcat ID (until there is support for the OpenSKOS handles.
     * Maybe more check are needed?
     * @param ccrHandle the handle URL (http://hdl.handle.net/11459/CCR_C-4146_5ccc45c8-d729-c180-2bf1-fccc56dde24d)
     * @return the ISOcat 4 letter code if present
     */
    private String handleToDCId(String ccrHandle) {
    	if (ccrHandle != null && ccrHandle.indexOf(HANDLE) > -1) {
    		String[] parts = HANDLE_PAT.split(ccrHandle);
    		if (parts.length >= 2) {
    			if (parts[1].length() >= 5) {
    				return parts[1].substring(0, 4);
    			}
    		}
    	}
    	
    	return ccrHandle;
    }

    /**
     * Adds the child elements as nodes to the specified parent node. Recursive.
     * 
     * @param mdElement the cmdi element
     * @param parentNode the tree node to add child nodes to
     */
	private void addChildElement(CMDIMetadataElement mdElement, DefaultMutableTreeNode parentNode) {
		if (mdElement == null) {
			return;
		}
		if (parentNode == null) {
			return;
		}
		URI datCatURI = null;
		URI valueDatCatURI = null;
		int numTypeExceptions = 0;
		try {
			if (mdElement.getType().getDataCategory() != null) {
				datCatURI = mdElement.getType().getDataCategory().getIdentifier();	
			}
			// check data category of the value if it is taken from a controlled vocabulary
			if (mdElement.getType() instanceof ControlledVocabularyElementTypeImpl && mdElement.getDisplayValue() != null && 
					!mdElement.getDisplayValue().isEmpty()) {
				ControlledVocabularyElementTypeImpl cvElemType = (ControlledVocabularyElementTypeImpl) mdElement.getType();
				
				List<ControlledVocabularyItem> cvItems = cvElemType.getItems();
				for (ControlledVocabularyItem item : cvItems) {
					CMDIControlledVocabularyItemImpl cmdiItem = (CMDIControlledVocabularyItemImpl) item;
					if (mdElement.getDisplayValue().equals(cmdiItem.getValue())) {
						// HS Sept 2014 this always returns null, with snapshot metadata-api-1.2-20140617.115217-2.jar
						if (cmdiItem.getDataCategory() != null) {
							valueDatCatURI = cmdiItem.getDataCategory().getIdentifier();
						}
						break;
					}
				}
			}
		} catch (Throwable t) {// TODO check if and how to catch more specific exceptions
			if (numTypeExceptions <= 1) {
				ClientLogger.LOG.warning("Unable to retrieve data category information: " + t.getMessage());
			}
			numTypeExceptions++;
		}
		
		DefaultMutableTreeNode nextNode = new DefaultMutableTreeNode(getDataObject(mdElement.getName(), 
				mdElement.getDisplayValue(), (datCatURI != null ? datCatURI.toString() : null),
				(valueDatCatURI != null ? valueDatCatURI.toString() : null) ));
		parentNode.add(nextNode);
		
		if (mdElement instanceof CMDIContainerMetadataElement) {
			CMDIContainerMetadataElement cmdElement = (CMDIContainerMetadataElement) mdElement;
			if (cmdElement.getChildrenCount() > 0) {
				List<MetadataElement> children = cmdElement.getChildren();
				for (MetadataElement child : children) {
					addChildElement((CMDIMetadataElement)child, nextNode);
				}
			}
			
		}
		
	}
	
	/**
	 * Loops over all nodes in the tree and updates the keys, if possible.
	 */
	void updateTreeForLanguage() {
		if (rootTreeNode != null && cmdiDoc != null) {
			//long now = System.currentTimeMillis();
			// loop over all nodes and check if keys have a translation
			DefaultMutableTreeNode node;
			CMDIKVData dataObject;
			Map<String, String> loadedDatCats = new HashMap<String, String>();
			/*
			 * The connector is a preference listener as well and updates its preferred language
			 * but there is no guarantee it will receive an event before the Metadata Viewer ?   
			 */
			ELANLocalDCRConnector dcrConnector = ELANLocalDCRConnector.getInstance();
			DCSmall curCategory;
			
			Enumeration<DefaultMutableTreeNode> breadthEnum = 
					rootTreeNode.breadthFirstEnumeration();
			while (breadthEnum.hasMoreElements()) {
				node = breadthEnum.nextElement();
				if (node == rootTreeNode) {
					continue;
				}
				dataObject = (CMDIKVData) node.getUserObject();
				if (dataObject.getKeyDatCatID() != null) {
					String locKey = null;
					String locValue = null;
					
					if (twoLetterLanguageCode != null) {
						// check key
						if (loadedDatCats.containsKey(dataObject.getKeyDatCatID())) {
							locKey = loadedDatCats.get(dataObject.getKeyDatCatID());
						} else {
							curCategory = dcrConnector.getDCSmallLoaded(dataObject.getKeyDatCatID());
							
							if (curCategory != null) {
								locKey = curCategory.getName(twoLetterLanguageCode);
							}
							loadedDatCats.put(dataObject.getKeyDatCatID(), locKey);
						}
						// check value
						if (loadedDatCats.containsKey(dataObject.getValueDatCatID())) {
							locValue = loadedDatCats.get(dataObject.getValueDatCatID());
						} else {
							curCategory = dcrConnector.getDCSmallLoaded(dataObject.getValueDatCatID());
							
							if (curCategory != null) {
								locValue = curCategory.getName(twoLetterLanguageCode);
							}
							loadedDatCats.put(dataObject.getValueDatCatID(), locValue);
						}
					}
					dataObject.setLocalizedKey(locKey);
					dataObject.setLocalizedValue(locValue);
				}
			}
			//System.out.println("Updating tree for language took: " + (System.currentTimeMillis() - now) + " ms");
			// notify the ui of the update
			if (viewerPanel != null) {
				viewerPanel.dataModelUpdated();
			}
		}
	}

	/**
	 * Performs some checks on the new language, this class only considers the iso-639-2 code. 
	 * If changed, calls the method to update the nodes in the tree.
	 * 
	 * @param isoUrlId ignored
	 * @param iso3L ignored
	 * @param lang2L the new preferred language 
	 */
	@Override
	public void setContentLanguage(String isoUrlId, String iso3L, String lang2L) {
		if (twoLetterLanguageCode != null && twoLetterLanguageCode.equals(lang2L)) {
			return; // don't update anything
		}
		String curTwoLetterCode = twoLetterLanguageCode;
		twoLetterLanguageCode = lang2L;
		
		if (twoLetterLanguageCode == null && curTwoLetterCode != null) {
			updateTreeForLanguage();// will load the default keys
		} else if (twoLetterLanguageCode != null && !twoLetterLanguageCode.equals(curTwoLetterCode)) {
			updateTreeForLanguage();
		}
		
	}
	
	/**
	 * A Runnable for background loading of the metadata file. 
	 * 
	 * @author Han Sloetjes
	 */
	private class CMDILoader implements Runnable {

		@Override
		public void run() {
			URL cmdiUrl = null;

			try {							
				File f = new File(sourcePath);
				if (f.exists()) {
					// a local file
					URI fUri = f.toURI();
					cmdiUrl = fUri.toURL();
				} else {// not very useful at the moment although a user might edit an eaf to point to an online cmdi?
					cmdiUrl = new URL(sourcePath);
				}

			} catch(MalformedURLException mue) {
				// log
	            ClientLogger.LOG.warning("Not a valid cmdi url: " +
	                    sourcePath);
	            CMDIServiceProvider.this.delayedLoadingComplete();
	            return;
			} catch (Throwable th) {
	            ClientLogger.LOG.warning("Could not create an url: " +
	                    sourcePath);
	            CMDIServiceProvider.this.delayedLoadingComplete();
	            return;
			}
			treeLock.lock();
			
			try {
				CMDIApi anAPI = new CMDIApi();
				try {
					cmdiDoc = anAPI.getMetadataDocument(cmdiUrl);
				} catch (MetadataException me) {
		            ClientLogger.LOG.warning("Metadata exception: " + me.getMessage());
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Metadata io exception: " + ioe.getMessage());			
				} catch (Throwable th) {
					ClientLogger.LOG.warning("Metadata loading exception: " + th.getMessage());
				}
				
				//System.out.println("Creating Metadata doc took: " + (System.currentTimeMillis() - now) + " ms");
				if (cmdiDoc == null) {
					try {
						simpleDom = new CMDIDom(cmdiUrl);
					} catch (IOException ioe) {
						ClientLogger.LOG.warning("CMDI DOM exception: " + ioe.getMessage());
					} catch (Throwable t) {
						ClientLogger.LOG.warning("CMDI DOM throwable: " + t.getMessage());
					}
				}
			} finally {
				treeLock.unlock();
			}
			
			CMDIServiceProvider.this.delayedLoadingComplete();
		}		
	}
	
	/**
	 * A Runnable to update the tree UI on the EventQueue.
	 */
	private class PanelUpdater implements Runnable {

		@Override
		public void run() {
			if (CMDIServiceProvider.this.viewerPanel != null) {
				CMDIServiceProvider.this.viewerPanel.reinitializeTree();
			}			
		}
	}
}
