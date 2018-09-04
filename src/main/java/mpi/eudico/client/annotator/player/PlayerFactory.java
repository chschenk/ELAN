package mpi.eudico.client.annotator.player;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

import java.io.File;


/**
 * The PlayerFactory creates ElanMediaPlayers for a specific URL
 * 
 * @version Dec 2012 removed the players that depend on the commercial JNIWrapper libraries.
 */
public class PlayerFactory {
    /** constant for Java Media Framework player */
    public final static String JMF_MEDIA_FRAMEWORK = "JMF";
    /** constant for QuickTime for Java based player */
    public final static String QT_MEDIA_FRAMEWORK = "QT";
    /** constant for Cocoa-QT framework */
    public static final String COCOA_QT = "CocoaQT";
    /** constant for an in-house JNI based Direct Show for Java player */
    public final static String JDS = "JDS";
    /** constant for an in-house JNI based Microsoft Media Foundation for Java player */
    public final static String JMMF = "JMMF";
    /** which types to load with Microsoft Media Foundation? First 3 also on Vista */
    public static final String[] MMF_EXTENSIONS = new String[]{"asf", "wma", "wmv", "m4a", "m4v", "mp4"};
    /** constant for a VLC for Java player */
    public final static String VLCJ = "VLCJ";
    /** constant for a javax.sound.sampled based audio player */
    public static final String JAVA_SOUND = "JavaSound"; 
    
    /**
     * The preferred call for client code to ask for an ElanMediaPlayer. Only
     * if the client code has a special reason to do so it should ask for a
     * specific media framework like QT or JMF. When there is more than one
     * media framework available the returned framework will be chosen as
     * follows: If the property "PreferredMediaFramework" is defined and if the preferred
     * framework is available it will be chosen to construct a player. The
     * property is set by using java -DPreferredMediaFramework="JMF" or
     * -DPreferredMediaFramework="QT" Otherwise JMF is returned as the default preferred
     * framework. Of course if there is only one media framework available it
     * is chosen to construct the player. If the creation of the media players
     * fails a NoPlayerException is thrown
     *
     * @param mediaDescriptor 
     *
     * @return an appropriate player
     *
     * @throws NoPlayerException
     */
    public static ElanMediaPlayer createElanMediaPlayer(MediaDescriptor mediaDescriptor) throws NoPlayerException { 
    	// check if the media descriptor contains a valid media file reference
        String mediaURL = mediaDescriptor.mediaURL;


        if (mediaURL.startsWith("file")) {
            if (!new File(mediaURL.substring(5)).exists()) { // remove file: part of url string
                throw new NoPlayerException("Media File not found: " +
                    mediaURL);
            }
        } else if (mediaURL.startsWith("rtsp")) {// new test for rtsp media

            //return createNativeMediaPlayerQT(mediaDescriptor);//old
        	if (SystemReporting.isMacOS()) {
        		//return createQTMediaPlayer(mediaDescriptor);
        		// still some problems with the Movie based streaming player on the Mac
        		// like disappearing sound, loosing connection, VM crashes
        		return createQTStreamingPlayer(mediaDescriptor);
        	} else {//wwj: currently windows only
        	     return createQTStreamingPlayer(mediaDescriptor);
        	}
        }// hier... add http for images?
        
        // if it is an image, create an image viewer here
        // hier... have a constant for image mime type
        if (mediaDescriptor.mimeType != null && mediaDescriptor.mimeType.startsWith("image")) {
        	return new ImagePlayer(mediaDescriptor);
        }
        
        String preferredMF = System.getProperty("PreferredMediaFramework");
        
        // windows specific flags and variables
        boolean jmmfTried = false;
        boolean jdsTried = false;
        String tempPreferredMF = preferredMF;
        StringBuilder sb = new StringBuilder();
        
        try {
	        // simplified version of the above
	        if (preferredMF != null) {
	            // Both audio and video are ok here, an exception will be thrown
	            // if a video URL is used on an audio only JMF
	            if (preferredMF.equals(JMF_MEDIA_FRAMEWORK)) {
	                // try to create a JMF player
	                return createJMFMediaPlayer(mediaDescriptor);
	            } else if (preferredMF.equals(QT_MEDIA_FRAMEWORK)) {
	                // try to create a QT player
	                return createQTMediaPlayer(mediaDescriptor);
	            } else if (preferredMF.equals(COCOA_QT)) {
	            	try {// cocoa player as first option//??
	            		return createCocoaQTMediaPlayer(mediaDescriptor);
	            	} catch (NoPlayerException npe) {
	            		System.out.println("Could not create a Cocoa based player...");
	            		sb.append("Could not create a Cocoa QT based player for: " + mediaDescriptor.mediaURL + "\n");
	            		return createQTMediaPlayer(mediaDescriptor);// do this here or in the following MacOS part?
	            	}
	            } else if (preferredMF.equals(JDS) || preferredMF.equals(JMMF)) {
	            	String playerType = checkLoadJdsOrJmmf(mediaDescriptor);
	            	if (playerType == JMMF) {
	            		preferredMF = JMMF;// set the preferred fm for the message in case of an exception
	            		jmmfTried = true;
	            		return createJMMFPlayer(mediaDescriptor);
	            	}
	            	jdsTried = true;
	            	return createJDSPlayer(mediaDescriptor);
	            } else if (preferredMF.equals(VLCJ)) {
	            	return createVLCJPlayer(mediaDescriptor);
	            } else if (preferredMF.equals(JAVA_SOUND)) {
	            	return createJavaSoundPlayer(mediaDescriptor);
	            }
	        }
        } catch (NoPlayerException npe) {
        	System.out.println("Preferred media framework \'" + preferredMF + 
				"\' can not handle: " + mediaDescriptor.mediaURL);
        	sb.append("Preferred media framework \'" + preferredMF + 
    				"\' can not handle: " + mediaDescriptor.mediaURL + "\n");
        	if (jmmfTried) {// reset preferred mf
        		preferredMF = tempPreferredMF;
        	}
        }

        if (SystemReporting.isWindows()) {
        	// at this point the preferred framework (if any) has already been tried
        	ElanMediaPlayer player = null;
        	// jds / jmmf section
	        if (!JDS.equals(preferredMF)) {
	        	String playerType = checkLoadJdsOrJmmf(mediaDescriptor);
	        	if (playerType == JMMF && !jmmfTried) {
	        		try {
	        			player = createJMMFPlayer(mediaDescriptor);
	        		} catch (NoPlayerException npe) {
		        		System.out.println(npe.getMessage());
		        		sb.append(npe.getMessage() + "\n");
		        		// make sure that jds is tested separately as well
			        	try {
				        	player = createJDSPlayer(mediaDescriptor);
				        } catch (NoPlayerException nnpe) {
				        	System.out.println(nnpe.getMessage());
				        	sb.append(nnpe.getMessage() + "\n");
				        }
	        		}
	        	} else {
		        	try {
			        	player = createJDSPlayer(mediaDescriptor);
			        } catch (NoPlayerException npe) {
			        	System.out.println(npe.getMessage());
			        	sb.append(npe.getMessage() + "\n");
			        }
	        	}
	        }
	         
	        if (player == null && !QT_MEDIA_FRAMEWORK.equals(preferredMF)) {
	        	try {
					player = createQTMediaPlayer(mediaDescriptor);
	        	} catch (NoPlayerException npe){
					System.out.println("No QT: " + npe.getMessage());
					sb.append(npe.getMessage() + "\n");
	        	}	        	
	        } 
	        if (player == null && !JMF_MEDIA_FRAMEWORK.equals(preferredMF)) {
	        	try {
					player = createJMFMediaPlayer(mediaDescriptor);
	        	} catch (NoPlayerException npe) {
					System.out.println("No JMF: " + npe.getMessage());
					sb.append(npe.getMessage() + "\n");
	        	}	        	
	        }
	        if (player == null && !JAVA_SOUND.equals(preferredMF) && 
	        		mediaDescriptor.mimeType.indexOf("audio") > -1) {
	        	try {
	        		player = createJavaSoundPlayer(mediaDescriptor);
	        	} catch(NoPlayerException npe) {
					System.out.println("JavaSound cannot play the file: " + npe.getMessage());
					sb.append(npe.getMessage() + "\n");
	        	}
	        }
			
			if (player != null) {
				return player;
			} else {
				throw new NoPlayerException("Could not create any media player for: " + 
				    mediaDescriptor.mediaURL + "\n" + sb.toString());
			}
        	//return createNativeMediaPlayerDS(mediaDescriptor);
        	//return createJMFMediaPlayer(mediaDescriptor);
        } else if (SystemReporting.isMacOS()) {
        	ElanMediaPlayer player = null;
        	
        	if (!COCOA_QT.equals(preferredMF)) {// the following has already been tried if COCOA is the preferred framework
	        	try {// cocoa player as first option//??
	        		player = createCocoaQTMediaPlayer(mediaDescriptor);
	        	} catch (NoPlayerException npe) {
	        		System.out.println("Could not create a Cocoa based player... " + npe.getMessage());
	        		sb.append(npe.getMessage() + "\n");
	        		try {
	        			player = createQTMediaPlayer(mediaDescriptor);
	        		} catch (NoPlayerException np) {
	        			sb.append(np.getMessage());
	        		}
	        	}
        	}
	        if (player == null && !JAVA_SOUND.equals(preferredMF) && 
	        		mediaDescriptor.mimeType.indexOf("audio") > -1) {
	        	try {
	        		player = createJavaSoundPlayer(mediaDescriptor);
	        	} catch(NoPlayerException npe) {
					System.out.println("JavaSound cannot play the file: " + npe.getMessage());
					sb.append(npe.getMessage() + "\n");
	        	}
	        }
	        
        	if (player != null) {
        		return player;
        	} else {
        		throw new NoPlayerException("Could not create any media player for: " + 
    				    mediaDescriptor.mediaURL + "\n" + sb.toString());
        	}
        } else if (SystemReporting.isLinux()) {
        	ElanMediaPlayer player = null;
        	if (!VLCJ.equals(preferredMF)) {
	        	try {
	        		player = createVLCJPlayer(mediaDescriptor);
	        	} catch (NoPlayerException npe) {
					System.out.println("Could not create a VLCJ player for the file: " + npe.getMessage());
					sb.append(npe.getMessage() + "\n");
	        	}
        	}
        	 
	        if (player == null && !JAVA_SOUND.equals(preferredMF) && 
	        		mediaDescriptor.mimeType.indexOf("audio") > -1) {
	        	try {
	        		player = createJavaSoundPlayer(mediaDescriptor);
	        	} catch(NoPlayerException npe) {
					System.out.println("JavaSound cannot play the file: " + npe.getMessage());
					sb.append(npe.getMessage() + "\n");
	        	}
	        }
	        
        	if (player != null) {
        		return player;
        	} else {
        		throw new NoPlayerException("Could not create any media player for: " + 
    				    mediaDescriptor.mediaURL + "\n" + sb.toString());
        	}
        }
        
        // always try as last possibility jmf, although it is very unlikely that this point is ever reached
        // unix gets a JMFPlayer automatically
        return createJMFMediaPlayer(mediaDescriptor);
    }
   
    /**
     * 
     * @param mediaDescriptor
     * @return
     * @throws NoPlayerException
     */
    public static ElanMediaPlayer createJMFMediaPlayer(
        MediaDescriptor mediaDescriptor) throws NoPlayerException {
        System.out.println("Using JMF Media Player for " + mediaDescriptor.mediaURL);

        return new JMFMediaPlayer(mediaDescriptor);
    }

    /**
     * Create a QT version of an ElanMediaPlayer for a certain URL
     *
     * @param mediaDescriptor DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoPlayerException DOCUMENT ME!
     */
    public static ElanMediaPlayer createQTMediaPlayer(
        MediaDescriptor mediaDescriptor) throws NoPlayerException {
        // Look into Greg's PlayerFactory code for creating a QT player
        // JNLP needs to have the QTPlayer class explicitly loaded for some reason?
        System.out.println("Using QT Media Player for " + mediaDescriptor.mediaURL);
        // loading the QTJava libraries can already throw an exception before the 
        // native library loading code in QTMediaPlayer.
        try {
        	return new QTMediaPlayer(mediaDescriptor);
        } catch (Throwable tr) {
        	throw new NoPlayerException(tr.getMessage());
        }
        //		throw new NoPlayerException("Unable to create a QT player");
    }
    
    /**
     * Creates a Cocoa QT version of an ElanMediaPlayer for a certain URL
     *
     * @param mediaDescriptor the media descriptor
     *
     * @return the player
     *
     * @throws NoPlayerException if the player could not be created
     */
    public static ElanMediaPlayer createCocoaQTMediaPlayer(
        MediaDescriptor mediaDescriptor) throws NoPlayerException {
        System.out.println("Using Cocoa QT Media Player for " + mediaDescriptor.mediaURL);

        return new CocoaQTMediaPlayer(mediaDescriptor);
    }
    
    /**
     * Create a streaming player to handle rtsp stream
     */
    public static ElanMediaPlayer createQTStreamingPlayer(
            MediaDescriptor mediaDescriptor) throws NoPlayerException {
    	System.out.println("Using QT Streaming Player for " + mediaDescriptor.mediaURL);
    	// on windows we should go with Movie (QTMediaPlayer); on mac 
    	// we should go with MoviePlayer (QTStreamingPlayer)
    	return new QTStreamingPlayer(mediaDescriptor);
    }
    
    /**
     * Creates a DirectShow for Java player.
     * @param mediaDescriptor the media descriptor
     * @return a JDSMediaPlayer
     * @throws NoPlayerException thrown when the player could not be created e.g. 
     * when the file is not supported or when there is a problem initializing the
     * native Direct Show framework
     */
    public static ElanMediaPlayer createJDSPlayer(
    		MediaDescriptor mediaDescriptor) throws NoPlayerException {
    	System.out.println("Using JDS Player for " + mediaDescriptor.mediaURL);
    	return new JDSMediaPlayer(mediaDescriptor);
    }
    
    /**
     * Creates a Microsoft Media Foundation for Java Player.
     * Only available on Vista and Windows 7 and only for certain media types.
     * Is currently an alternative player within the JDS framework.
     * 
     * @param mediaDescriptor the media descriptor
     * @return a JMMFPlayer 
     * @throws NoPlayerException thrown when a player cannot be created
     */
    public static ElanMediaPlayer createJMMFPlayer(
    		MediaDescriptor mediaDescriptor) throws NoPlayerException {
    	System.out.println("Using JMMF Player for " + mediaDescriptor.mediaURL);
    	return new JMMFMediaPlayer(mediaDescriptor);
    }
    
    /**
     * Checks whether a JDS or a JMMF player should be created.
     * The decision depends on file type, OS version and user preferences.
     * 
     * @param mediaDescriptor contains the media url
     * @return JDS or JMMF constant
     */
    private static String checkLoadJdsOrJmmf(MediaDescriptor mediaDescriptor) {
    	// check OS version and media type (loosely, based on extension)
    	String lower = mediaDescriptor.mediaURL.toLowerCase();
    	int extIndex = lower.lastIndexOf('.');
    	if (extIndex > -1 && extIndex < lower.length() - 1) {
    		String ext = lower.substring(extIndex + 1);
    		int fileTypeIndex = -1;
    		for (int i = 0; i < MMF_EXTENSIONS.length; i++) {
    			if (MMF_EXTENSIONS[i].equals(ext)) {
    				fileTypeIndex = i;
    				break;
    			}
    		}
    		if (fileTypeIndex > -1) {
    			// if JMMF is disabled return a JDS
    			String jmmfPref = System.getProperty("JMMFEnabled");

    			if (jmmfPref != null && "false".equals(jmmfPref.toLowerCase())) {
    		    	//System.out.println("Using JDS Player for " + mediaDescriptor.mediaURL);
    		    	return JDS;
    			}
    			
    			Boolean jmmfUserPref = Preferences.getBool("Windows.JMMFEnabled", null);
    			if (jmmfUserPref != null && !jmmfUserPref) {
    		    	//System.out.println("Using JDS Player for " + mediaDescriptor.mediaURL);
    		    	return JDS;
    			}
    			
    			// check OS version
    			boolean isVista = SystemReporting.isWindowsVista();
    			boolean isWin7 = SystemReporting.isWindows7OrHigher();
    			
    			if (isVista && fileTypeIndex <= 2) {
    				//System.out.println("Using JMMF Player for " + mediaDescriptor.mediaURL);
    				return JMMF;
    			} else if (isWin7) {
    				//System.out.println("Using JMMF Player for " + mediaDescriptor.mediaURL);
    				return JMMF;
    			}
    		}
    	}
    	
    	return JDS;
    }
    
    /**
     * Creates a VLC for Java Player.
     * Only available on Linux for now. 
     * 
     * @param mediaDescriptor the media descriptor
     * @return a VLCJPlayer 
     * @throws NoPlayerException thrown when a player cannot be created
     */
    public static ElanMediaPlayer createVLCJPlayer(
    		MediaDescriptor mediaDescriptor) throws NoPlayerException {
    	try {
        	System.out.println("Trying VLCJ Player for " + mediaDescriptor.mediaURL);
        	return new VLCJMediaPlayer(mediaDescriptor);
    	} catch (UnsatisfiedLinkError le) {
        	System.out.println("Failing to load VLCJ Player.");
        	System.out.println("Is VLC properly installed?");
        	throw new NoPlayerException("Failing to load VLCJ Player.\nIs VLC properly installed?\n"
        								+ le.toString());
    	} catch (Throwable t) {
        	throw new NoPlayerException(t.toString());
    	}
    }
    
    /**
     * Creates a JavaSound (javax.sound.sampled) based media player. 
     * This player only supports WAV, AU, AIFF and SND files. This player
     * is independent of any non-JRE libraries, so it should only throw an
     * exception when the file is not supported.
     *  
     * @param mediaDescriptor the media descriptor
     * @return a JavaSoundPlayer
     * @throws NoPlayerException if the file format is not supported
     */
    public static ElanMediaPlayer createJavaSoundPlayer(
    		MediaDescriptor mediaDescriptor) throws NoPlayerException {
    	try {
    		return new JavaSoundPlayer(mediaDescriptor);
    	} catch (Throwable t) {
    		throw new NoPlayerException(t.toString());
    	}
    }
}
