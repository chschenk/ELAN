package mpi.eudico.client.annotator.linkedmedia;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.recognizer.gui.AbstractRecognizerPanel;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.client.annotator.timeseries.TimeSeriesConstants;
import mpi.eudico.client.annotator.timeseries.config.TSSourceConfiguration;
import mpi.eudico.client.annotator.timeseries.io.TSConfigurationParser;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceProvider;
import mpi.eudico.client.annotator.timeseries.spi.TSServiceRegistry;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.viewer.MetadataViewer;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;
import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A utility class for creating, checking and updating linked file descriptors.
 *
 * @author Han Sloetjes
 */
public class LinkedFileDescriptorUtil implements ClientLogger {
    /**
     * Creates a LinkedFileDescriptor for a file, given the path to the file.
     *
     * @param filePath the path to the file
     *
     * @return a LinkedFileDescriptor for the file
     */
    public static LinkedFileDescriptor createLFDescriptor(String filePath) {
        if ((filePath == null) || (filePath.length() == 0)) {
            return null;
        }

        String linkURL = FileUtility.pathToURLString(filePath);

        if (linkURL == null) {
            return null;
        }

        String mimeType = null;
        String extension = "";

        if (linkURL.indexOf('.') > -1) {
            extension = linkURL.substring(linkURL.lastIndexOf('.') + 1);
        }

        mimeType = mimeTypeForExtension(extension);

        LinkedFileDescriptor md = new LinkedFileDescriptor(linkURL, mimeType);

        return md;
    }

    /**
     * Returns the mime-type of a file based on its extension.
     *
     * @param extension the file extension
     *
     * @return a mime-type string, returns "unknown" when the file is not part
     *         of a  limited set of known file types in this context
     */
    public static String mimeTypeForExtension(String extension) {
        if ((extension == null) || (extension.length() < 3)) {
            return LinkedFileDescriptor.UNKNOWN_MIME_TYPE;
        }

        String lowExt = extension.toLowerCase();

        for (int i = 0; i < FileExtension.TEXT_EXT.length; i++) {
            if (lowExt.equals(FileExtension.TEXT_EXT[i])) {
                return LinkedFileDescriptor.TEXT_TYPE;
            }
        }

        for (int i = 0; i < FileExtension.LOG_EXT.length; i++) {
            if (lowExt.equals(FileExtension.LOG_EXT[i])) {
                return LinkedFileDescriptor.TEXT_TYPE;
            }
        }

        for (int i = 0; i < FileExtension.XML_EXT.length; i++) {
            if (lowExt.equals(FileExtension.XML_EXT[i])) {
                return LinkedFileDescriptor.XML_TYPE;
            }
        }

        for (int i = 0; i < FileExtension.SVG_EXT.length; i++) {
            if (lowExt.equals(FileExtension.SVG_EXT[i])) {
                return LinkedFileDescriptor.SVG_TYPE;
            }
        }

        for (int i = 0; i < FileExtension.CMDI_EXT.length; i++) {
            if (lowExt.equals(FileExtension.CMDI_EXT[i])) {
                return LinkedFileDescriptor.CMDI_TYPE;
            }
        }

        // Must be after CMDI_EXT because they overlap
        for (int i = 0; i < FileExtension.IMDI_EXT.length; i++) {
            if (lowExt.equals(FileExtension.IMDI_EXT[i])) {
                return LinkedFileDescriptor.IMDI_TYPE;
            }
        }

        return LinkedFileDescriptor.UNKNOWN_MIME_TYPE;
    }

    /**
     * Creates objects like viewers and updates viewer manager and layout
     * manager etc for the files linked to the transcription.
     * Only to be called when initializing an existing transcription, 
     * makes sure that the transcription is not set to "changed" and that 
     * no preferences are stored.
     *
     * @param transcription the Transcription
     */
    public static void initLinkedFiles(TranscriptionImpl transcription) {
        if ((transcription == null) ||
                (transcription.getLinkedFileDescriptors() == null)) {
            return;
        }
        boolean isChanged = transcription.isChanged();
        List<LinkedFileDescriptor> lfDescs = transcription.getLinkedFileDescriptors();
        LinkedFileDescriptor lfd;
        TSServiceRegistry registry = TSServiceRegistry.getInstance();
        TSTrackManager trackManager = null;
        List<String> handledSources = new ArrayList<String>();

        // first check existence of linked files
        // linked files that are not found or have not been located are in the map as a url-null mapping
        Map<String, String> urlmap = checkLinkedFiles(transcription);
        if (isChanged != transcription.isChanged()) {
        	// the location of a linked file may have been updated
        	isChanged = true;
        }

        // next try to recreate tracks from configuration
        for (int i = 0; i < lfDescs.size(); i++) {
            lfd = lfDescs.get(i);

            if (lfd.linkURL.endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
                String path = lfd.linkURL;

                if (path.startsWith("file:")) {
                    path = path.substring(5);
                }

                TSConfigurationParser parser = new TSConfigurationParser();
                List<TSSourceConfiguration> confs = parser.parseSourceConfigs(path);

                if ((confs != null) && (confs.size() > 0) &&
                        (trackManager == null)) {
                    trackManager = new TSTrackManager(transcription);
                    ELANCommandFactory.addTrackManager(transcription,
                        trackManager);

                    // get viewer manager, create viewer
                    TimeSeriesViewer tsViewer = ELANCommandFactory.getViewerManager(transcription)
                                                                  .createTimeSeriesViewer();
                    tsViewer.setTrackManager(trackManager);
                    // get layout manager, add viewer
                    ELANCommandFactory.getLayoutManager(transcription)
                                      .add(tsViewer);
                } else {
                	continue;
                }
                
              //get the list of linked files
                List<String> linkedFiles = new ArrayList<String>();                
                for (int z = 0; z < lfDescs.size(); z++) {                	
                	String linkUrl = lfDescs.get(z).linkURL;
                	if (linkUrl == lfd.linkURL) {
                		handledSources.add(linkUrl);
                		continue;
                	}
                	if (urlmap.containsKey(linkUrl)) {
                		String mappedLink = urlmap.get(linkUrl);
                		if (mappedLink != null) {
                 			// add the updated link?
                        	if(!linkedFiles.contains(mappedLink)){
                        		linkedFiles.add(mappedLink);
                        	}
                		} else {
                			// link not found, don't add
                			handledSources.add(linkUrl);
                		}
                		continue;
                	}
                	if(!linkedFiles.contains(linkUrl)){
                		linkedFiles.add(linkUrl);
                	}
                }     
                
                // in rare occasions(?) there can be sources in the config file that are not amongst 
                // the linked files of the transcription (should not be possible, actually). Check.
                // HS 08-2013 if the linked files in the transcription have been updated e.g. based on relative
                // url the source links are out of sync
                for (int j = 0; j < confs.size(); j++) {
                    TSSourceConfiguration sc = confs.get(j);

                    if (urlmap.containsKey(sc.getSource()) && urlmap.get(sc.getSource()) != null) {
                        sc.setSource(urlmap.get(sc.getSource()));
                        if (trackManager != null) {
                        	trackManager.setChanged(true);
                        }
                    } 	
                    
                    // if the source file is not linked 
                    if(!linkedFiles.contains(sc.getSource())){
                    	continue;
                    }

                    TSServiceProvider provider = null;

                    if (sc.getProviderClassName() != null) {
                        provider = registry.getProviderByClassName(sc.getProviderClassName());
                    } 

                    if (provider == null) {
                        provider = registry.getProviderForFile(sc.getSource());
                    }

                    if (provider != null) {
                        if (!provider.isConfigurable()) {
                            provider.autoCreateTracks(sc);
                        } else {
                            provider.createTracksFromConfiguration(sc);
                        }

                        trackManager.addTrackSource(sc, false);
                    }

                    handledSources.add(sc.getSource());
                }
            }
        }

        for (int i = 0; i < lfDescs.size(); i++) {
            lfd = lfDescs.get(i);

            if (handledSources.contains(lfd.linkURL)) {
                continue;
            }

            if (lfd.mimeType.equals(LinkedFileDescriptor.SVG_TYPE)) {
                continue;
            }

            TSServiceProvider provider = registry.getProviderForFile(lfd.linkURL);

            if (provider != null) {
                if (trackManager == null) {
                    trackManager = new TSTrackManager(transcription);
                    ELANCommandFactory.addTrackManager(transcription,
                        trackManager);

                    // get viewer manager, create viewer
                    TimeSeriesViewer tsViewer = ELANCommandFactory.getViewerManager(transcription)
                                                                  .createTimeSeriesViewer();
                    tsViewer.setTrackManager(trackManager);
                    // get layout manager, add viewer
                    ELANCommandFactory.getLayoutManager(transcription)
                                      .add(tsViewer);
                }

                TSSourceConfiguration config = new TSSourceConfiguration(lfd.linkURL);
                config.setProviderClassName(provider.getClass().getName());

                if (!provider.isConfigurable()) {
                    provider.autoCreateTracks(config);
                }

                trackManager.addTrackSource(config, false);
            }
        }
        if (!isChanged) {
        	transcription.setUnchanged();
        }
        // update the list of the viewer manager
        updateOtherMediaPaths(transcription, true);
    }

    /**
     * Tries to update any object (in the viewermanager, the layoutmanager etc)
     * and finally sets the linked file descriptors in the transcription. The
     * kind of objects that have to be updated can be very diverse.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new linked file descriptors
     */
    public static void updateLinkedFiles(TranscriptionImpl transcription,
        List<LinkedFileDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        List<LinkedFileDescriptor> oldDescriptors = transcription.getLinkedFileDescriptors();
        List<LinkedFileDescriptor> removedSources = new ArrayList<LinkedFileDescriptor>(4);
        List<LinkedFileDescriptor> addedSources = new ArrayList<LinkedFileDescriptor>(4);

        LinkedFileDescriptor lfd;
        LinkedFileDescriptor olfd;
outerloop: 
        for (int i = 0; i < oldDescriptors.size(); i++) {
            olfd = oldDescriptors.get(i);

            if (descriptors.size() == 0) {
                removedSources.add(olfd);
            } else {
                for (int j = 0; j < descriptors.size(); j++) {
                    lfd = descriptors.get(j);

                    if (lfd.linkURL.equals(olfd.linkURL)) {
                        // check for changes??
                        continue outerloop;
                    }

                    if (j == (descriptors.size() - 1)) {
                        removedSources.add(olfd);
                    }
                }
            }
        }

outerloop: 
        for (int i = 0; i < descriptors.size(); i++) {
            lfd = descriptors.get(i);

            if (oldDescriptors.size() == 0) {
                addedSources.add(lfd);
            } else {
                for (int j = 0; j < oldDescriptors.size(); j++) {
                    olfd = oldDescriptors.get(j);

                    if (olfd.linkURL.equals(lfd.linkURL)) {
                        // check for changes??
                        continue outerloop;
                    }

                    if (j == (oldDescriptors.size() - 1)) {
                        addedSources.add(lfd);
                    }
                }
            }
        }

        // if there is any time series source detected...
        TSTrackManager trackManager = ELANCommandFactory.getTrackManager(transcription);
        TSServiceRegistry registry = TSServiceRegistry.getInstance();

        if (removedSources.size() > 0) {
            for (int i = 0; i < removedSources.size(); i++) {
                lfd = removedSources.get(i);

                // check track manager
                if (trackManager != null) {
                    trackManager.removeTrackSource(lfd.linkURL);
                }

                // check other ??
            }
            // check if there is a dangling tsconf.xml file and remove?
            if (descriptors.size() == 1) {
            	lfd = descriptors.get(0);
            	if (lfd.linkURL.endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
            		descriptors.remove(0);
            	}
            }
        }

        if (addedSources.size() > 0) {
        	boolean configFound = false;
            for (int i = 0; i < addedSources.size(); i++) {
                lfd = addedSources.get(i);

                TSServiceProvider provider = registry.getProviderForFile(lfd.linkURL);

                if (provider != null) {
                    if (trackManager == null) {
                        trackManager = new TSTrackManager(transcription);
                        ELANCommandFactory.addTrackManager(transcription,
                            trackManager);

                        // get viewer manager, create viewer
                        TimeSeriesViewer tsViewer = ELANCommandFactory.getViewerManager(transcription)
                                                                      .createTimeSeriesViewer();
                        tsViewer.setTrackManager(trackManager);
                        // get layout manager, add viewer
                        ELANCommandFactory.getLayoutManager(transcription)
                                          .add(tsViewer);
                    }

                    TSSourceConfiguration config = new TSSourceConfiguration(lfd.linkURL);
                    config.setProviderClassName(provider.getClass().getName());

                    if (!provider.isConfigurable()) {
                        provider.autoCreateTracks(config);
                    }
                    
                    trackManager.addTrackSource(config, true);
                    // if there isn't a tsconfig.xml yet in the descriptors create one?
                    if (!configFound) {
	                    LinkedFileDescriptor confLfd;
	                    
	                    for (int j = 0; j < descriptors.size(); j++) {
	                    	confLfd = descriptors.get(j);
	                    	if (confLfd == lfd) {
	                    		continue;
	                    	}
	                    	
	                    	if (confLfd.linkURL.endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
	                    		configFound = true;
	                    		break;
	                    	}
	                    }
	                    
	                    if (!configFound) {
	                    	String transPath = transcription.getFullPath();
	                    	if (transPath.toLowerCase().endsWith(".eaf")) {
	                    		transPath = transPath.substring(0, transPath.length() - 4);
	                    	}
	                    	transPath = transPath + TimeSeriesConstants.CONF_SUFFIX;
	                    	//System.out.println("Path: " + transPath);
	                    	// create a path...
	                    	LinkedFileDescriptor configDesc = LinkedFileDescriptorUtil.createLFDescriptor(transPath);
	                    	if (configDesc != null) {
	                    		descriptors.add(configDesc);
	                    		configFound = true;
	                    		//break;
	                    	}
	                    }
                    }
                }
            }
        }

        // check if there are any sources left in the trackmanager? 
        
        // Destroy time series viewer?
        transcription.setLinkedFileDescriptors(descriptors);
        transcription.setChanged();
        
        // Metadata may be changed: notify the metadata viewer
        ViewerManager2 vm = ELANCommandFactory.getViewerManager(transcription);
        if (vm != null) {
        	MetadataViewer mdv = vm.getMetadataViewer();
        	if (mdv != null) {
        		mdv.linkedFilesChanged();
        	}
        }
        // update recognizer panel and the ViewerManager's otherMediaPaths list 
        updateOtherMediaPaths(transcription, true);
    }
    
    /**
     * Updates the time offset of existing linked files.
     * 
     * @param transcription the transcription holding the linked files
     *        descriptors 
     * @param newLinkOffsets mapping from link url's to time offset values
     */
    public static void updateLinkedFilesOffsets(TranscriptionImpl transcription, Map<String, 
    		Long> newLinkOffsets) {
    	if (transcription == null || newLinkOffsets == null) {
    		return;
    	}
    	
    	List<LinkedFileDescriptor> changedDesc = new ArrayList<LinkedFileDescriptor>(10);
    	LinkedFileDescriptor lfd;
    	
    	for (int i = 0; i < transcription.getLinkedFileDescriptors().size(); i++) {
    		lfd = (LinkedFileDescriptor) transcription.getLinkedFileDescriptors().get(i);
    		
    		Long nextOffset = newLinkOffsets.get(lfd.linkURL);
    		if (nextOffset != null && nextOffset != lfd.timeOrigin) {
    			changedDesc.add(lfd);
    			lfd.timeOrigin = nextOffset;
    		}
    	}
    	
    	if (changedDesc.size() == 0) {
    		return; // log...
    	}
    	transcription.setChanged();
    	// update existing objects, currently only timeseries objects
        TSTrackManager trackManager = ELANCommandFactory.getTrackManager(transcription);
        if (trackManager == null) {
        	return;
        }
    	
        for (int i = 0; i < changedDesc.size(); i++) {
        	lfd = changedDesc.get(i);
        	trackManager.setOffset(lfd.linkURL, (int) lfd.timeOrigin);// already changed in the descriptor
        }
        TimeSeriesViewer tsViewer = ELANCommandFactory.getLayoutManager(transcription).getTimeSeriesViewer();
        if (tsViewer != null) {
        	tsViewer.repaint();
        }
    }

    /**
     * Checks whether linked files can be found on the system: first check the
     * specified path/url then look in the same directory as the eaf.
     *
     * @param transcription the transcription holding the linked files
     *        descriptors.
     *
     * @return a map containing old url to new url mappings
     */
    private static Map<String, String> checkLinkedFiles(TranscriptionImpl transcription) {
        Map<String, String> linkMap = new HashMap<String, String>(2);

        if (transcription == null) {
            return linkMap;
        }

        List<LinkedFileDescriptor> lfDescs = transcription.getLinkedFileDescriptors();

        if ((lfDescs == null) || lfDescs.isEmpty()) {
            return linkMap;
        }

        LinkedFileDescriptor lfd;
        String oldLinkURL;

        for (int i = 0; i < lfDescs.size(); i++) {
            lfd = lfDescs.get(i);

            if (lfd.linkURL == null) {
                LOG.warning("Link url is null");

                continue;
            }

            oldLinkURL = lfd.linkURL;

            // remove protocol
            int colonIndex = oldLinkURL.indexOf(':');
            String path = null;

            if ((colonIndex > 0) && (colonIndex < (lfd.linkURL.length() - 1))) {
                path = lfd.linkURL.substring(colonIndex + 1);
            } else {
                path = lfd.linkURL;
            }

            path = path.replace('\\', '/');

            File file = new File(path);

            if (file.exists()) {
                continue;
            }

            // not found, look in the same directory as the eaf
            int lastSlash = path.lastIndexOf('/');
            String linkName = path;

            if ((lastSlash >= 0) && (lastSlash < (path.length() - 1))) {
                linkName = path.substring(lastSlash + 1);
            }

            // get the dir of the transcription and look in the dir
            String linkDir = null;
            String eafFileName = transcription.getFullPath();
            colonIndex = eafFileName.indexOf(':');

            if (colonIndex > -1) {
                eafFileName = eafFileName.substring(colonIndex + 1);
            }

            File eafFile = new File(eafFileName);

            if (eafFile.exists() && (eafFile.getParentFile() != null)) {
                linkDir = eafFile.getParentFile().getAbsolutePath();
            }

            File searchFile = null;

            if (linkDir != null) {
                searchFile = new File(linkDir + "/" + linkName);
            } else {
                searchFile = new File(linkName);
            }

            if (searchFile.exists()) {
                // update de url
                lfd.linkURL = FileUtility.pathToURLString(searchFile.getAbsolutePath());
                LOG.info("Updating url from: " + oldLinkURL + " to: " +
                    lfd.linkURL);
                transcription.setChanged();
                linkMap.put(oldLinkURL, lfd.linkURL);

                continue;
            }
            // try relative path
            // make sure the eaf path is treated the same way as media files,
            // i.e. it starts with file:/// or file://      
            if (lfd.relativeLinkURL != null) {
            	String fullEAFURL = FileUtility.pathToURLString(transcription.getFullPath());
            	String relUrl = lfd.relativeLinkURL;
            	
            	if (relUrl.startsWith("file:/")) {
            		relUrl = relUrl.substring(6);
            	}
            	// resolve relative url and check location
            	String absPath = FileUtility.getAbsolutePath(fullEAFURL, relUrl);
            	if (absPath != null) {
            		File relFile = new File(absPath);
            		if (relFile.exists()) {
                        // update de url
                        lfd.linkURL = FileUtility.pathToURLString(relFile.getAbsolutePath());
                        LOG.info("Updating url from: " + oldLinkURL + " to: " +
                            lfd.linkURL);
                        transcription.setChanged();
                        linkMap.put(oldLinkURL, lfd.linkURL);

                        continue;
            		}
            	}
            }
            
            // last resort, prompt
            FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
            String[] extensions = null;

            if (lfd.mimeType.equals(LinkedFileDescriptor.XML_TYPE)) {
            	extensions = FileExtension.XML_EXT;                
            } else if (oldLinkURL.toLowerCase()
                                     .endsWith(FileExtension.LOG_EXT[0])) {
            	extensions = FileExtension.LOG_EXT; 
            } else if (lfd.mimeType.equals(LinkedFileDescriptor.TEXT_TYPE)) {
            	extensions = FileExtension.TEXT_EXT; 
            } else if (lfd.mimeType.equals(LinkedFileDescriptor.SVG_TYPE)) {
            	extensions = FileExtension.SVG_EXT; 
            }

            chooser.setCurrentDirectory(searchFile.getParentFile().getAbsolutePath());
            chooser.createAndShowFileDialog(ElanLocale.getString("LinkedFilesDialog.Message.Locate") + ": " + linkName, FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
            		null, extensions, true, null, FileChooser.FILES_ONLY, linkName);
            
            File selectedFile = chooser.getSelectedFile();
            if(selectedFile != null){
            	String nextUrl = FileUtility.pathToURLString(chooser.getSelectedFile()
                        .getAbsolutePath());
            	lfd.linkURL = nextUrl;
            	LOG.info("Updating url from: " + oldLinkURL + " to: " + 	nextUrl);
            	transcription.setChanged();
            	linkMap.put(oldLinkURL, nextUrl);	
            } else { // if the chooser is canceled there won't be a mapping, the link is missing
            	linkMap.put(oldLinkURL, null);
            }
        }
        return linkMap;
    }
    
    /**
     * Checks if the file exists, tries to update the path when not.
     * 
     * @param filePath the path to the file 
     * @param transcription the transcription
     * @return the old or the updated filePath
     */
    private static String checkLinkedFile(String filePath, TranscriptionImpl transcription) {
    	if (filePath == null || transcription == null) {
    		return null;
    	}
    	String transPath = transcription.getFullPath();
        // remove protocol
        int colonIndex = filePath.indexOf(':');
        String path = null;

        if ((colonIndex > 0) && (colonIndex < (filePath.length() - 1))) {
            path = filePath.substring(colonIndex + 1);
        } else {
            path = filePath;
        }

        path = path.replace('\\', '/');

        File file = new File(path);
    	
    	if(file.exists()) {
    		return filePath;
    	}
    	
    	// not found, look in the same directory as the transcription
        int lastSlash = path.lastIndexOf('/');
        String linkName = path;

        if ((lastSlash >= 0) && (lastSlash < (path.length() - 1))) {
            linkName = path.substring(lastSlash + 1);
        }

        // get the dir of the transcription and look in the dir
        String linkDir = null;
        String eafFileName = transPath;
        colonIndex = eafFileName.indexOf(':');

        if (colonIndex > -1) {
            eafFileName = eafFileName.substring(colonIndex + 1);
        }

        File eafFile = new File(eafFileName);

        if (eafFile.exists() && (eafFile.getParentFile() != null)) {
            linkDir = eafFile.getParentFile().getAbsolutePath();
        }

        File searchFile = null;

        if (linkDir != null) {
            searchFile = new File(linkDir + "/" + linkName);
        } else {
            searchFile = new File(linkName);
        }

        if (searchFile.exists()) {
        	String nextUrl = FileUtility.pathToURLString(searchFile.getAbsolutePath());
            LOG.info("Updating url from: " + filePath + " to: " +
            		nextUrl);
            // return the new path
        	return nextUrl;
        }
        
        // last resort, prompt       
        FileChooser chooser = new FileChooser(ELANCommandFactory.getRootFrame(transcription));
        chooser.setCurrentDirectory(searchFile.getParentFile().getAbsolutePath());
        chooser.createAndShowFileDialog(ElanLocale.getString("LinkedFilesDialog.Message.Locate") + ": " + linkName, FileChooser.OPEN_DIALOG,
        		ElanLocale.getString("Button.Select"), null, null, true, null, FileChooser.FILES_ONLY, linkName);

        if(chooser.getSelectedFile() != null){
        	String nextUrl = FileUtility.pathToURLString(chooser.getSelectedFile().getAbsolutePath());
            LOG.info("Updating url from: " + filePath + " to: " + nextUrl);
            return  nextUrl;
       } 
       return filePath;
    }
    
    /**
     * Extracts a list of (possible) other media files (i.e. timeseries data) for usage in the 
     * RecognizerPanel.
     * 
     * @param transcription the transcription containing the linked files descriptor
     * @param isUpdate if true the recognizer control panel is updated as well
     */
    private static void updateOtherMediaPaths(TranscriptionImpl transcription, boolean isUpdate) {
        ArrayList<String> otherMediaPaths = new ArrayList<String>(6);
        
        for (LinkedFileDescriptor lfds : transcription.getLinkedFileDescriptors()) {
        	String path = lfds.linkURL;
    		path = FileUtility.urlToAbsPath(path);
        	if (lfds.mimeType.equals(LinkedFileDescriptor.TEXT_TYPE) ||
        			lfds.mimeType.equals(LinkedFileDescriptor.UNKNOWN_MIME_TYPE) ||
        			lfds.mimeType.equals(LinkedFileDescriptor.XML_TYPE)) {
        		if (!path.endsWith(TimeSeriesConstants.CONF_SUFFIX)) {
        			if (!otherMediaPaths.contains(path)) {
        				otherMediaPaths.add(path);
        			}
        		}
        	}
        }
        // update the otherMediaPaths list of the ViewerManager
        ELANCommandFactory.getViewerManager(transcription).setOtherMediaPaths(otherMediaPaths);
        
        if (isUpdate) {
        	final AbstractRecognizerPanel recognizerPanel = 
        			ELANCommandFactory.getViewerManager(transcription).getRecognizerPanel();
    		if (recognizerPanel != null) {
        		// could check here for changes compared to the old setup
        		recognizerPanel.setOtherFilePaths(otherMediaPaths);     			
        	}
        }
    }
}
