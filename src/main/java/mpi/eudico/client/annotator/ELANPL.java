package mpi.eudico.client.annotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.util.SystemReporting;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * The main class for ELAN. Main performs some initialization and creates the
 * first frame. Holds version information, major, minor and micro.
 */
public class ELANPL implements ClientLogger {
    /** the major version value */
    public static int major = 3;

    /** the minor version value */
    public static int minor = 9;

    /** the micro (bug fix) version value */
    public static int micro = 1;

    /**
     * Creates a new ELAN instance
     */
    private ELANPL() {
    }

    /**
     * Main method, initialization and first frame.
     *
     * @param args the arguments, path to an eaf file
     */
    public static void main(final String[] args) {
    	LOG.info("ELAN Playlist Player" + getVersionString());
    	LOG.info("Java version: " +
                System.getProperty("java.version"));
    	LOG.info("Runtime version: " +
                System.getProperty("java.runtime.version"));
    	LOG.info("OS name: " + System.getProperty("os.name"));
    	LOG.info("OS version: " + System.getProperty("os.version"));
    	LOG.info("User language: " + System.getProperty("user.language"));
    	LOG.info("User home: " + System.getProperty("user.home"));
    	LOG.info("User dir: " + System.getProperty("user.dir"));
    	LOG.info("Classpath: " + System.getProperty("java.class.path"));

        // make sure the directory for Elan data exists, could move to preferences?
        try {
            /* HS May 2008: copy files to the new ELAN data folder. Do this only once. */
            if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
            	File dataFolder = new File(Constants.ELAN_DATA_DIR);
            	if (!dataFolder.exists()) {
            		dataFolder.mkdir();
            		File oldDataFolder = new File(Constants.USERHOME + Constants.FILESEPARATOR + ".elan_data");
            		if (oldDataFolder.exists()) {
            			// copy files
            			File[] files = oldDataFolder.listFiles();
            			File inFile = null;
            			File outFile = null;
            			for (int i = 0; i < files.length; i++) {
            				inFile = files[i];
            				if (inFile.isFile()) {
            					outFile = new File(dataFolder.getAbsolutePath() + Constants.FILESEPARATOR + inFile.getName());
            					FileUtility.copyToFile(inFile, outFile);
            				}
            			}
            		}
            	}
            	boolean screenBar = false;//default
            	Object val = Preferences.get("OS.Mac.useScreenMenuBar", null);
            	if (val instanceof Boolean) {
            		screenBar = ((Boolean) val).booleanValue();
            		System.setProperty("apple.laf.useScreenMenuBar", String.valueOf(screenBar));
            	}
            	// using the screen menu bar implies the default Mac OS L&F
            	if (!screenBar) {
            		val = Preferences.get("UseMacLF", null);
            		
            		if (val instanceof Boolean) {
            			boolean macLF = ((Boolean) val).booleanValue();
            			if (!macLF) {
                            try {
                                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                                JPopupMenu.setDefaultLightWeightPopupEnabled(false);//??
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
            			}
            		}
            	}
            	//System.setProperty("apple.awt.brushMetalLook", "true"); 
            	// media framework: if no framework specified, check the user's stored preference
            	if (System.getProperty("PreferredMediaFramework") == null) {
            		val = Preferences.get("Mac.PrefMediaFramework", null);
            		if (val instanceof String) {
            			System.setProperty("PreferredMediaFramework", (String) val);
            		}
            	}

            }// end mac initialization
            else if (SystemReporting.isWindows()) {// windows user preferred media framework
            	if (System.getProperty("PreferredMediaFramework") == null) {
            		Object val = Preferences.get("Windows.PrefMediaFramework", null);
            		if (val instanceof String) {
            			System.setProperty("PreferredMediaFramework", (String) val);
            		}
            	}
            }
            
            File dataDir = new File(Constants.ELAN_DATA_DIR);

            if (!dataDir.exists()) {
                dataDir.mkdir();
            }

            // temporary, clean up old crap
            File oldCrap = new File(Constants.STRPROPERTIESFILE);
            oldCrap.delete();
            oldCrap = new File(Constants.USERHOME + Constants.FILESEPARATOR +
                    ".elan.pfs");
            oldCrap.delete();
            
        } catch (Exception ex) {
            // catch any
        	LOG.warning("Could not create ELAN's data directory: " + ex.getMessage());
        }
        
        FrameManager.getInstance().setExitAllowed(true);
        
        // the path to the playlist file
        String plFileName = System.getProperty("user.dir") + File.separator + "ELAN_Playlist.txt";
        // create the frame on the event dispatch thread
        
        if ((args != null) && (args.length > 0) && (args[0].length() != 0)) {
        	plFileName = args[0];
        	/*
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                		File argFile = new File(args[0]); 
                		//System.out.println("F " + argFile.getAbsolutePath());
                		//System.out.println("A " + argFile.isAbsolute());
                    	// HS July 2008: check if the argument (filepath to eaf) is a relative
                    	// path. If so let the jvm resolve it relative to the current directory 
                    	// (where ELAN is launched from)
                    	if (!argFile.isAbsolute()) { 
                    		//System.out.println("F " + argFile.getAbsolutePath());
                    		FrameManager.getInstance().createFrame(argFile.getAbsolutePath());
                    	} else {
                    		FrameManager.getInstance().createFrame(args[0]);
                    	}
                    }
                });
                */
        } else {
        	/*
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        FrameManager.getInstance().createEmptyFrame();
                    }
                });
                */
        }
        
        // external launcher, currently only accepts imdi files to open an eaf
        // from another application/VM
        // mpi.eudico.client.annotator.integration.ExternalLauncher.start();
        
        // create one Mac Application handler per jvm
        if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
            try {
                Class macHandler = Class.forName(
                        "mpi.eudico.client.mac.MacAppHandler");
                Class macList = Class.forName(
                        "mpi.eudico.client.mac.MacApplicationListener");
                java.lang.reflect.Constructor con = macHandler.getConstructor(new Class[] {
                            macList
                        });
                ElanMacApplication elanMacApp = new ElanMacApplication();
                con.newInstance(new Object[] { elanMacApp });
            } catch (Exception ex) {
                System.out.println("Could not load Mac application handler.");
            }
        }
        
        
        List<File> files = loadPlaylist(plFileName);
        if (files == null || files.size() == 0) {
        	LOG.severe("No playlist or no files in the playlist, exiting");
        	System.exit(1);
        }
        startLoop(files);
    	LOG.info("Ended play loop, exiting...");
    }
    
    private static List<File> loadPlaylist(String file) {
    	if (file == null) {
    		return null;
    	}
    	List<File> files = new ArrayList<File>(10);
    	
    	BufferedReader bufRead = null;
    	try {
    		File pl = new File(file);
    		
    		if (pl.exists() && pl.isFile() && pl.canRead()) {
    			 bufRead = new BufferedReader(new FileReader(pl));
    			 String line = null;
    			 while ((line = bufRead.readLine()) != null) {
    				 if (line.length() > 0) {
    					 if (line.startsWith("#")) {
    						 continue;
    					 }
    					 try {
    						 File f = new File(line);
    						 if (f.exists() && f.canRead()) {
    							 files.add(f);
    						 } else {
    							 LOG.warning("The eaf file does not exist or cannot be read: " + line); 
    						 }
    					 } catch (Exception ee) {
    						 LOG.warning("Could not find or load eaf file: " + line);
    					 }
    				 }
    			 }

    		} else {
    			LOG.severe("Could not find or load the playlist file: " + file);
    		}
    	} catch (Exception ioe) {
    		LOG.severe("Could not find or load the playlist file: " + file);
    	} finally {
    		try {
				bufRead.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	return files;
    }
    
    private static void startLoop(List<File> files) {
    	if (files == null || files.size() == 0) {
    		return; // no more logging
    	}
    	int index = 0;
    	File cur;
    	TranscriptionImpl curTranscription;
    	ElanFrame2 oldActive = null;
    	ElanFrame2 curActive = null;
    	
    	while (true) {
    		cur = files.get(index);
    		
    		curTranscription = new TranscriptionImpl(cur.getAbsolutePath());
    		curTranscription.setUnchanged();    		

    		curActive = new ElanFrame2(cur.getAbsolutePath());
    		
    		if (oldActive != null) {
    			if (oldActive.getViewerManager().getTranscription() != null) {
    				// surpress messages
    				oldActive.getViewerManager().getTranscription().setUnchanged();
    			}
    			
    			oldActive.doClose(false);
    		}
    		
    		//while (active.getViewerManager().getMasterMediaPlayer() == null) {
    		while (!curActive.isFullyInitialized()) {
    			try {
    				// give it some time to start
    				Thread.sleep(500);
    			} catch (InterruptedException ie) {}
    		}
    		
			// not null anymore
    		curActive.getViewerManager().getMasterMediaPlayer().start();
			
			try {
				// give it some time to start
				Thread.sleep(5000);
			} catch (InterruptedException ie) {}
			
			while (true) {
				if (curActive.getViewerManager().getMasterMediaPlayer().isPlaying() || 
						curActive.getViewerManager().getMasterMediaPlayer().getMediaDuration() -
						curActive.getViewerManager().getMasterMediaPlayer().getMediaTime() > 500) {
					// the second condition allows the user to temporarily stop the playback, there is some margin in the exact stop time
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						
					}	
				} else {
					break;
				}

			}
			
			index++;
			if (index == files.size()) {
				index = 0;
			}
			oldActive = curActive;
		}
    	
    }

    /**
     * Returns the current version information as a string.
     *
     * @return the current version
     */
    public static String getVersionString() {
        return major + "." + minor + "." + micro;
    }
}
