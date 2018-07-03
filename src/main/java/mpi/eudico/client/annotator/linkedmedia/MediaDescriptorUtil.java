package mpi.eudico.client.annotator.linkedmedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.CocoaQTMediaPlayer;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.client.annotator.player.EmptyMediaPlayer;
import mpi.eudico.client.annotator.player.NoPlayerException;
import mpi.eudico.client.annotator.player.PlayerFactory;
import mpi.eudico.client.annotator.recognizer.gui.AbstractRecognizerPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A utility class for creating, checking and updating mediadescriptors.
 *
 * @author Han Sloetjes
 */
public class MediaDescriptorUtil {
    /**
     * Checks the existance of the file denoted by the mediadescriptor.
     *
     * @param md the media descriptor
     *
     * @return true when the file exists, false otherwise
     */
    public static boolean checkLinkStatus(MediaDescriptor md) {
        if ((md == null) || (md.mediaURL == null) ||
                (md.mediaURL.length() < 5)) {
            return false;
        }

        //wwj: return true if rtsp presents
        if (md.mediaURL.startsWith("rtsp")) {
            return true;
        }

        // remove the file: part of the URL, leading slashes are no problem
        int colonPos = md.mediaURL.indexOf(':');
        String fileName = md.mediaURL.substring(colonPos + 1);

        // replace all back slashes by forward slashes
        fileName = fileName.replace('\\', '/');

        File file = new File(fileName);

        if (!file.exists()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates a mediadescriptor for the specified file.
     *
     * @param filePath the full path of the file
     *
     * @return a MediaDescriptor
     */
    public static MediaDescriptor createMediaDescriptor(String filePath) {
        if ((filePath == null) || (filePath.length() == 0)) {
            return null;
        }

        String mediaURL = FileUtility.pathToURLString(filePath);

        if (mediaURL == null) {
            return null;
        }

        String mimeType = null;
        String mediaExtension;

        if (mediaURL.indexOf('.') > -1) {
            mediaExtension = mediaURL.substring(mediaURL.lastIndexOf('.') + 1);
        } else {
            mediaExtension = mediaURL.substring(mediaURL.length() - 3); // of no use, at least with JMF
        }

        mimeType = MediaDescriptorUtil.mimeTypeForExtension(mediaExtension);

        MediaDescriptor md = new MediaDescriptor(mediaURL, mimeType);

        return md;
    }

    /**
     * Creates a Vector of mediadescriptors for the specified files.
     *
     * @param fileNames a collection of files
     *
     * @return a Vector of MediaDescriptors
     */
    public static List<MediaDescriptor> createMediaDescriptors(List<String> fileNames) {
        List<MediaDescriptor> mediaDescriptors = new ArrayList<MediaDescriptor>();

        if (fileNames == null) {
            return mediaDescriptors;
        }

mdloop: 
        for (int i = 0; i < fileNames.size(); i++) {
            String path = fileNames.get(i);
            MediaDescriptor nextMD = MediaDescriptorUtil.createMediaDescriptor(path);

            if (nextMD == null) {
                continue;
            }

            for (int j = 0; j < mediaDescriptors.size(); j++) {
                MediaDescriptor otherMD = mediaDescriptors.get(j);

                if (otherMD.mediaURL.equals(nextMD.mediaURL)) {
                    // don't add the same file twice?
                    continue mdloop;
                }

                // should this automatic detection of extracted_from remain??
                if (nextMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
                        MediaDescriptorUtil.isVideoType(otherMD)) {
                    if (FileUtility.sameNameIgnoreExtension(nextMD.mediaURL,
                                otherMD.mediaURL)) {
                        nextMD.extractedFrom = otherMD.mediaURL;
                    }
                }

                if (otherMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
                        MediaDescriptorUtil.isVideoType(nextMD)) {
                    if (FileUtility.sameNameIgnoreExtension(nextMD.mediaURL,
                                otherMD.mediaURL)) {
                        otherMD.extractedFrom = nextMD.mediaURL;
                    }
                }
            }

            mediaDescriptors.add(nextMD);
        }

        return mediaDescriptors;
    }

    /**
     * Returns a mime-type for a given file extension.  Works only for a very
     * limited set of known file types.
     *
     * @param fileExtension the file extension
     *
     * @return a Mime-Type String from the <code>MediaDescriptor</code> class
     */
    public static String mimeTypeForExtension(String fileExtension) {
        if ((fileExtension == null) || (fileExtension.length() < 2)) {
            return MediaDescriptor.UNKNOWN_MIME_TYPE;
        }

        String lowExt = fileExtension.toLowerCase();

        for (String element : FileExtension.MPEG_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.MPG_MIME_TYPE;
            }
        }
        
        for (String element : FileExtension.MPEG4_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.MP4_MIME_TYPE;
            }
        }        
      
        for (String element : FileExtension.WAV_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.WAV_MIME_TYPE;
            }
        }

        for (String element : FileExtension.QT_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.QUICKTIME_MIME_TYPE;
            }
        }

        for (String element : FileExtension.MISC_AUDIO_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.GENERIC_AUDIO_TYPE;
            }
        }

        for (String element : FileExtension.MISC_VIDEO_EXT) {
            if (lowExt.equals(element)) {
                return MediaDescriptor.GENERIC_VIDEO_TYPE;
            }
        }

        // 2010 HS: add for images
        for (String element : FileExtension.IMAGE_MEDIA_EXT) {
            if (lowExt.equals(element)) {
            	if (lowExt.equals("jpg") || lowExt.equals("jpeg")) {
            		return MediaDescriptor.JPEG_TYPE;
            	}
            	return "image/" + lowExt;// add all to MediaDescriptor?
            }
        }
        
        return MediaDescriptor.UNKNOWN_MIME_TYPE;
    }
    
    /**
     * Returns a array of file extensions for the given mimetype.  Works only for a very
     * limited set of known file types.
     *
     * @param mimeType the mimeType
     *
     * @return a file extension String[] from the <code>FileExtension</code> class
     */
    public static String[] extensionForMimeType(String mimeType) {
        if ((mimeType == null) ) {
            return null;
        }

        if (mimeType.equals(MediaDescriptor.MPG_MIME_TYPE)) {
            return FileExtension.MPEG_EXT;
        }
        
        if (mimeType.equals(MediaDescriptor.MP4_MIME_TYPE)) {
            return FileExtension.MPEG4_EXT;
        }
        
        if (mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {
            return FileExtension.WAV_EXT;
        }
        
        if (mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE)) {
            return FileExtension.QT_EXT;
        } 
        
        if (mimeType.equals(MediaDescriptor.JPEG_TYPE) || mimeType.startsWith("image")) {
            return FileExtension.IMAGE_MEDIA_EXT;
        } 
        
        return null;
    }


    /**
     * Returns whether the specified mime type is a known video type.
     *
     * @param mimeType the mime type string
     *
     * @return true if the specified mimetype is known to be a video type
     *
     * @see #isVideoType(MediaDescriptor)
     */
    public static boolean isVideoType(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        return (mimeType.equals(MediaDescriptor.GENERIC_VIDEO_TYPE) ||
        mimeType.equals(MediaDescriptor.MPG_MIME_TYPE) ||
        mimeType.equals(MediaDescriptor.QUICKTIME_MIME_TYPE)||
        mimeType.equals(MediaDescriptor.MP4_MIME_TYPE));
    }

    /**
     * Returns whether the specified MediaDescriptor is of a known video type.
     *
     * @param descriptor the mediadescriptor
     *
     * @return true if the specified mediadescriptor is of a known video type
     *
     * @see #isVideoType(String)
     */
    public static boolean isVideoType(MediaDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }

        return MediaDescriptorUtil.isVideoType(descriptor.mimeType);
    }

    /**
     * Tries to update the mediaplayers in the viewermanager as well as the
     * layoutmanager and finally sets the mediadescriptors in the
     * transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new media descriptors
     */
    public static void updateMediaPlayers(TranscriptionImpl transcription,
        List<MediaDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        long mediaTime = 0L;        

        ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);
        ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(transcription);
        SignalViewer signalViewer = layoutManager.getSignalViewer();
        
        // stop the player before destroying
        if(viewerManager.getMasterMediaPlayer() != null && 
        		viewerManager.getMasterMediaPlayer().isPlaying()){
        	viewerManager.getMasterMediaPlayer().stop();
	    }
        
        mediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
        
        ElanMediaPlayerController empc = viewerManager.getMediaPlayerController();

       	empc.deferUpdatePlayersVolumePanel(true);
       	
        // make sure all players are connected
        if (layoutManager.getMode() == ElanLayoutManager.SYNC_MODE) {
            layoutManager.connectAllPlayers();
        }

        // the master media player cannot be removed directly
        // replace the master; the master is added to the slaves
        viewerManager.setMasterMediaPlayer(new EmptyMediaPlayer(
                Integer.MAX_VALUE));

        // remove the slaves
        List<ElanMediaPlayer> slavePlayers = viewerManager.getSlaveMediaPlayers();
        List<ElanMediaPlayer> remPlayers = new ArrayList<ElanMediaPlayer>(slavePlayers.size());

        remPlayers.addAll(slavePlayers);

        // Doing the destroyMediaPlayer() and the remove() separately helps the
        // CocoaQTMediaPlayer not to crash in cleanupOnClose().
        // Possibly because
        // the re-layout affects the remaining players asynchronously (calling
        // JavaQTMoviePlayer.componentResized()) and destroying them at that time
        // might be a bad idea. No, that probably isn't it, since the callbacks
        // come much later and the removed players aren't called.
        for (ElanMediaPlayer slave : remPlayers) {
            viewerManager.destroyMediaPlayer(slave);
        }
        for (ElanMediaPlayer slave : remPlayers) {
            layoutManager.remove(slave);
        }
        // The players are not actually finalized yet:
        // at least the remPlayers vector still refers to them.

        if (signalViewer != null) {
            viewerManager.destroyViewer(signalViewer);
            layoutManager.remove(signalViewer);
        }
        
        // create new players from the descriptors
        MediaDescriptorUtil.createMediaPlayers(transcription, descriptors);

        // After all these changes, we can now do all the updates to the slave volume sliders.
       	empc.deferUpdatePlayersVolumePanel(false);
        
        // check recognizer panel
        ArrayList<String> newAudioPaths = new ArrayList<String>(6);
        
        if (layoutManager.getSignalViewer() != null) {
        	newAudioPaths.add(layoutManager.getSignalViewer().getMediaPath());
        }
        
    	// there may be other audio files associated with the transcription
    	MediaDescriptor md;
    	for (int i = 0; i < descriptors.size(); i++) {  		
    		md = descriptors.get(i);
    		if (md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) || md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) ) {    			
    			if (!newAudioPaths.contains(md.mediaURL)) {
    			    newAudioPaths.add(md.mediaURL);
    			}
    		}
    	}
    	
    	
    	final AbstractRecognizerPanel recognizerPanel = viewerManager.getRecognizerPanel();
		if (recognizerPanel != null) {
    		// could check here for changes compared to the old setup
    		recognizerPanel.setAudioFilePaths(newAudioPaths);     			
    	}
    	
    	viewerManager.setAudioPaths(newAudioPaths);    	
    		
    	
    	 //check video recognizer panel
        ArrayList<String> newVideoPaths = new ArrayList<String>(6);
                 	
    	for (int i = 0; i < descriptors.size(); i++) {  		
    		md = descriptors.get(i);
    		if (!md.mimeType.equals(MediaDescriptor.GENERIC_AUDIO_TYPE) && !md.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) ) {    			
    			if (!newVideoPaths.contains(md.mediaURL)) {
    				newVideoPaths.add(md.mediaURL);
    			}
    		}
    	}
    	
    	if (recognizerPanel != null) {
    			recognizerPanel.setVideoFilePaths(newVideoPaths);     			
    	} 
    	viewerManager.setVideoPaths(newVideoPaths);
    	
        viewerManager.getMasterMediaPlayer().setMediaTime(mediaTime); 
        transcription.setMediaDescriptors(descriptors);
        transcription.setChanged();
        
        layoutManager.doLayout();
    }
    
    /**
     * Updates the time offsets of players and connected viewers in case the time offset has changed.
     * 
     * @param transcription the transcription to be updated
     * @param newPlayerOffsets the new offsets, mappings of url to offset
     */
    public static void updateMediaPlayerOffsets(TranscriptionImpl transcription, Map<String, Long> newPlayerOffsets) {
    	if (transcription == null || newPlayerOffsets == null) {
    		return; // log
    	}
    	
    	List<MediaDescriptor> changedDescs = new ArrayList<MediaDescriptor>(10);
    	MediaDescriptor md;
    	
    	for (int i = 0; i < transcription.getMediaDescriptors().size(); i++) {
    		md = transcription.getMediaDescriptors().get(i);
    		Long newOffset = newPlayerOffsets.get(md.mediaURL);
    		if (newOffset != null && newOffset != md.timeOrigin) {
    			changedDescs.add(md);
    			md.timeOrigin = newOffset;
    		}
    	}
    	
    	if (changedDescs.size() == 0) {
    		return; // log...
    	}
    	transcription.setChanged();
    	
        ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);
        ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(transcription);
        
        if (viewerManager.getMasterMediaPlayer().isPlaying()) {
        	viewerManager.getMasterMediaPlayer().stop();
        }
        boolean masterOffsetChanged = changedDescs.contains(viewerManager.getMasterMediaPlayer().getMediaDescriptor());
    	long curMediaTime = viewerManager.getMasterMediaPlayer().getMediaTime();
    	
    	ElanMediaPlayer player;
    	for (int i = 0; i < viewerManager.getSlaveMediaPlayers().size(); i++) {
    		player = viewerManager.getSlaveMediaPlayers().get(i);
    		
    		if (changedDescs.contains(player.getMediaDescriptor())) {
    			player.setOffset(player.getMediaDescriptor().timeOrigin);// media descriptor has already been updated
    		}
    	}
    	// TODO the SignalViewer should accept MediaDescriptors as well
    	if (layoutManager.getSignalViewer() != null) {
    		String wavUrl = FileUtility.pathToURLString(layoutManager.getSignalViewer().getMediaPath());
    		Long nextOffset = newPlayerOffsets.get(wavUrl);
    		
    		if (nextOffset != null) {
    			layoutManager.getSignalViewer().setOffset(nextOffset); // could be unchanged
    		}
    	}
    	
    	if (masterOffsetChanged) {
    		long masterOffset = viewerManager.getMasterMediaPlayer().getMediaDescriptor().timeOrigin;
    		viewerManager.getMasterMediaPlayer().setOffset(masterOffset);
    		// notify viewers? duration changed as a result of change of offset
    		AbstractViewer viewer;
    		viewer = viewerManager.getAnnotationDensityViewer();
    		if (viewer != null) {
    			viewer.mediaOffsetChanged();
    		}

    		viewerManager.getMasterMediaPlayer().setMediaTime(curMediaTime);
    	}
    }
    

    /**
     * Tries to create the mediaplayers in the viewermanager as well as the
     * layoutmanager and finally sets the mediadescriptors in the
     * transcription.
     *
     * @param transcription the Transcription with the old descriptors
     * @param descriptors the new media descriptors
     */
    public static void createMediaPlayers(TranscriptionImpl transcription,
        List<MediaDescriptor> descriptors) {
        if ((transcription == null) || (descriptors == null)) {
            return;
        }

        int numDesc = descriptors.size();

        try {
            ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);
            ElanLayoutManager layoutManager = ELANCommandFactory.getLayoutManager(transcription);         
          
            int nrOfPlayers = 0;
            int nrVisualPlayers = 0;
            String signalSource = null;
            long signalOffset = 0;

            viewerManager.getMediaPlayerController().deferUpdatePlayersVolumePanel(true);

            MediaDescriptor curMD;
            ArrayList<MediaDescriptor> failedPlayers = null;
            StringBuilder errors = new StringBuilder();
            
            for (int i = 0; i < numDesc; i++) {
                curMD = descriptors.get(i);

                if (!MediaDescriptorUtil.checkLinkStatus(curMD)) {
                    continue;
                } 
                                
                if(!curMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE) &&
                		nrVisualPlayers == Constants.MAX_VISIBLE_PLAYERS){
                	continue;
                }
               

                try {
                    ElanMediaPlayer player = null;
                    if (curMD.mimeType.equals(
                                MediaDescriptor.QUICKTIME_MIME_TYPE)) {
                    	if (SystemReporting.isMacOS()) {
                    		player = viewerManager.createMediaPlayer(curMD,
                                PlayerFactory.COCOA_QT);	
                    	} else {
                    		player = viewerManager.createMediaPlayer(curMD,
	                                PlayerFactory.QT_MEDIA_FRAMEWORK);                    		
                    	}
                    } else {	
            			player = viewerManager.createMediaPlayer(curMD);                    			
                    }
                    
                    if(player == null){
                    	continue;
                    }
                    
                    nrOfPlayers++;

                    if (nrOfPlayers == 1) {
                        // here comes the mastermedia player
                        viewerManager.setMasterMediaPlayer(player);
                    }
                    if (signalSource == null && curMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE)) {                    	
                            signalSource = curMD.mediaURL;
                            signalOffset = curMD.timeOrigin;
                            // HS Aug 2008: pass this player to the viewermanager; important for synchronisation mode
                            viewerManager.setSignalSourcePlayer(player);
                    }
                    
                    if (player.getVisualComponent() != null && !(curMD.mimeType.equals(MediaDescriptor.WAV_MIME_TYPE))) {
                    	nrVisualPlayers++;
                    }
                    // only add layoutable players to the layout
                    // add no more than 4 visual players
                    if (nrVisualPlayers <= Constants.MAX_VISIBLE_PLAYERS || player.getVisualComponent() == null) {
                    	layoutManager.add(player);
                    	//System.out.println("Player Added... " + System.currentTimeMillis());
                    	// work around for Cocoa player, the constructor cannot (currently) throw
                    	// an exception if a file is not supported. Can only be done after it has been 
                    	// added to a frame
                    	if (player instanceof CocoaQTMediaPlayer) {
                    		try {
                    			Thread.sleep(100);
                    		} catch (InterruptedException ie) {	
                    		}
                    		if (!((CocoaQTMediaPlayer)player).isPlayerCreated()) {
                    			// delayed exception
                    			errors.append("The Cocoa QT Player can not handle the file.\n");
                    			player = PlayerFactory.createQTMediaPlayer(curMD);//only alternative on Mac
                    		}
                    	}
                    }

                } catch (NoPlayerException npe) {
                    if (failedPlayers == null) {
                        failedPlayers = new ArrayList<MediaDescriptor>();
                    }
                    errors.append(npe.getMessage() + "\n");
                    failedPlayers.add(curMD);
                }
            }

            if (nrOfPlayers == 0) {
            	if (viewerManager.getMasterMediaPlayer() instanceof EmptyMediaPlayer) {
            		((EmptyMediaPlayer) viewerManager.getMasterMediaPlayer()).setMediaDuration(
            				transcription.getLatestTime());
            	} else {
	                viewerManager.setMasterMediaPlayer(new EmptyMediaPlayer(
	                        transcription.getLatestTime()));
                }
                layoutManager.add(viewerManager.getMasterMediaPlayer());
            }

            // Create a signal viewer
            if (signalSource != null) {
                SignalViewer newSignalViewer = viewerManager.createSignalViewer(signalSource);
                if(newSignalViewer != null){
                	newSignalViewer.setOffset(signalOffset);
                	newSignalViewer.preferencesChanged();
                
                	layoutManager.add(newSignalViewer);
                }
            }

           	viewerManager.getMediaPlayerController().deferUpdatePlayersVolumePanel(false);

            layoutManager.doLayout();

            // inform the user of failures...
            if ((failedPlayers != null) && (failedPlayers.size() > 0)) {
                StringBuilder sb = new StringBuilder(
                        "No player could be created for:\n");

                for (int i = 0; i < failedPlayers.size(); i++) {
                    sb.append("- " +
                        failedPlayers.get(i).mediaURL +
                        "\n");
                }
                sb.append(errors.toString());

                JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), sb.toString(),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception rex) {
            rex.printStackTrace();
            JOptionPane.showMessageDialog(ELANCommandFactory.getRootFrame(transcription), 
            		"An error occurred while creating media players: " + rex.getMessage(),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Creates a single media player for a media descriptor. For customized
     * player  creates, e.g. when showing (recreating) a player that has
     * previously been destroyed.
     *
     * @param transcription the transcription
     * @param curMD the media descriptor
     *
     * @return a player or null
     */
    public static ElanMediaPlayer createMediaPlayer(
        TranscriptionImpl transcription, MediaDescriptor curMD) {
        if ((transcription == null) || (curMD == null)) {
            return null;
        }

        if (!MediaDescriptorUtil.checkLinkStatus(curMD)) {
            return null;
        }

        ElanMediaPlayer player = null;

        try {
            ViewerManager2 viewerManager = ELANCommandFactory.getViewerManager(transcription);

            if (curMD.mimeType.equals(
                        MediaDescriptor.QUICKTIME_MIME_TYPE) && !SystemReporting.isMacOS()) {
	            player = viewerManager.createMediaPlayer(curMD,
	                        PlayerFactory.QT_MEDIA_FRAMEWORK);
            } else {
                player = viewerManager.createMediaPlayer(curMD);
            }
            /*
        	if (player instanceof CocoaQTMediaPlayer) {
        		try {
        			Thread.sleep(100);
        		} catch (InterruptedException ie) {	
        		}
        		if (!((CocoaQTMediaPlayer)player).isPlayerCreated()) {
        			player = PlayerFactory.createQTMediaPlayer(curMD);//only alternative on Mac, only on older systems, (10.4)
        		}
        	}
        	*/
        } catch (NoPlayerException npe) {
        }

        return player;
    }
}
