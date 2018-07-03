package mpi.eudico.client.annotator.viewer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.prefs.PreferencesReader;
import mpi.eudico.client.annotator.prefs.PreferencesWriter;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;


/**
 * A class to start the Praat executable and open the .wav file that  has been
 * loaded into Elan.<br>
 * Opening a file and (possibly) selecting a part from that file is a two step
 * process. First we have to make sure that Praat is/has been started, next we
 * have to use the sendpraat executable to let Praat execute a script with the
 * right arguments. At least on Windows it is not possible to start Praat
 * with  the script etc. as arguments. The Windows praatcon.exe Praat console
 * executable  can start Praat with a script etc., but it can not load a 'long
 * sound' and create  a view and editor for it.<br>
 * Since elan only loads sound files that are on a local file system it is
 * save to expect that a provided url is on the local file system.
 *
 * @author Han Sloetjes
 * @version jul 2004
 * @version oct 2007 new praat script file with an additional argument for the 
 * Praat Longsound object name (also for the create clip script).
 * @version Jan 2014 added a native method to call a "sendpraat" function via JNI.
 * If this works the user no longer has to install the sendpraat executable.
 */
public class PraatConnection {
    /** constant for the name of the praat script, march 2011 version 3 */
    private static final String PRAAT_SCRIPT = "openpraat-v3.praat";
    
    /** constant for a script to create a clip from a sound file, march 2011 version 3 */
    public static final String PRAAT_CLIP_SCRIPT = "createsoundclip-v3.praat";
    
    private static String scriptFileName;

    /** constant for the Praat application path property */
    private static final String PRAAT_APP = "Praat app";

    /** constant for the sendpraat application path property */
    private static final String SENDPRAAT_APP = "Sendpraat app";

    /** constant for a binary preference file */
    private static final String PRAAT_PREFS_FILE = Constants.ELAN_DATA_DIR +
        Constants.FILESEPARATOR + "praat.pfs";
    
    private static final String PRAAT_PREFS_XML_FILE = Constants.ELAN_DATA_DIR +
    Constants.FILESEPARATOR + "praat.pfsx";

    /**
     * a non-thread-safe way of temporarily storing the result of a check on
     * the  running state of Praat (Unix systems)
     */
    static boolean isPraatRunning = false;

    /** the logger */
    private static final Logger LOG = Logger.getLogger(PraatConnection.class.getName());
    private static Map<String, String> preferences;
    
    /** constant for the name of the sendpraat native library and executable */
    private static final String SENDPRAAT_LIB = "sendpraat";
    private static boolean nativeLibLoaded = false;

    static {
    	try {
    		System.loadLibrary(SENDPRAAT_LIB);
    		nativeLibLoaded = true;
    	} catch (UnsatisfiedLinkError ue) {
    		LOG.warning("Error loading the sendpraat native library: " + ue.getMessage());
    		//ue.printStackTrace();
    	} catch (Throwable t) {// catch as much as possible
    		LOG.warning("Error loading the sendpraat native library: " + t.getMessage());
    	}
    }
    /**
     * Creates a new PraatConnection instance
     */
    private PraatConnection() {
    }
    
    /**
     * Call the native sendpraat routine. 
     * @param program the name of the program to call, Praat
     * @param timeOut time out period (not used for some OS's
     * @param command the command to send to Praat
     * 
     * @return an error message or null in case of success
     */
    private static native String sendpraatNative(String program, long timeOut, String command);

    /**
     * Shows a message dialog.
     * 
     * @param message the message text
     */
    private static void showMessage(String message) {
    	JOptionPane.showMessageDialog(null, message, ElanLocale.getString(
                "PraatConnection.Message.SendpraatError"), JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Asynchronously opens a file in Praat.
     *
     * @param fileName the media file
     * @param begintime the selection begintime
     * @param endtime the selection end time
     */
    public static void openInPraat(final String fileName, final long begintime,
        final long endtime) {
        new Thread() {
                @Override
				public void run() {
                    if (!checkScript()) {
                        JOptionPane.showMessageDialog(new JFrame(),
                            ElanLocale.getString(
                                "PraatConnection.Message.NoScript"),
                            ElanLocale.getString("Message.Warning"),
                            JOptionPane.WARNING_MESSAGE);
                        LOG.warning("Praat script could not be created");

                        return;
                    }

                    if (SystemReporting.isWindows()) {
                        openWindowsPraat(fileName, begintime, endtime);
                    } else {
                        openOtherPraat(fileName, begintime, endtime);
                    }
                }
            }.start();
    }

    /**
     * Asynchronously creates clip from a file with Praat and opens the new file as a Sound object.
     * Reuses the openWindowsPraat() and openOtherPraat() methods
     *
     * @param fileName the media file
     * @param begintime the selection begintime
     * @param endtime the selection end time
     */
    public static void clipWithPraat(final String fileName, final long begintime, 
        final long endtime) {
        new Thread() {
            @Override
			public void run() {
                if (!checkClipScript()) {
                    JOptionPane.showMessageDialog(new JFrame(),
                        ElanLocale.getString(
                            "PraatConnection.Message.NoScript"),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
                    LOG.warning("Praat script could not be created");

                    return;
                }

                if (SystemReporting.isWindows()) {
                    openWindowsPraat(fileName, begintime, endtime);
                } else {
                    openOtherPraat(fileName, begintime, endtime);
                }
            }
        }.start();        
    }
    /**
     * For windows flavors some processing of path names should be done etc.
     *
     * @param fileName the media file
     * @param begintime the begintime of the selection
     * @param endtime the end time of the selection
     */
    private static void openWindowsPraat(String fileName, long begintime,
        long endtime) {
        if (fileName != null) {
            // Praat can handle files on samba shares too
            if (fileName.startsWith("///")) {
                fileName = fileName.substring(3);
            }

            fileName = fileName.replace('/', '\\');

            // sendpraat has problems with filepaths containing spaces
            // single or double quotes don't seem to help
            // if the file name itself contains spaces there seems to be no solution
            // but with the built-in native library (based on sendpraat) using double
            // quotes seems to work
            if (!nativeLibLoaded && fileName.indexOf(' ') > 0) {
                fileName = spacelessWindowsPath(fileName);
            }

            //System.out.println("file: " + fileName);
        } else {
            LOG.warning("Praat: media file is null");

            return;
        }

        // first make sure praat is running
        String praatExe = getPreference(PRAAT_APP);

        if ((praatExe == null) || (praatExe.length() == 0)) {
            praatExe = "Praat.exe";
        }

        String[] praatCom = new String[] { praatExe };

        try {
            Runtime.getRuntime().exec(praatCom);

            // give praat a moment to start
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                LOG.info("Thread interrupted in sleep: " + ie.getMessage());
            }

            //
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(new JFrame(),
                ElanLocale.getString("PraatConnection.Message.Security"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);
            LOG.warning("Cannot execute Praat (security): " + se.getMessage());

            return;
        } catch (IOException ioe) {
            LOG.warning("Cannot execute Praat (io): " + ioe.getMessage());

            // not found, prompt
            String path = locatePraat();

            if (path == null) {
                return;
            } else {
                // retry
                openWindowsPraat(fileName, begintime, endtime);

                return;
            }
        }

        // next execute sendpraat
        // (on windows we cannot use praatcon.exe for this task)
        String sendpraatExe = SENDPRAAT_LIB;
        
        String executeCom = "execute \"" + scriptFileName + "\" \"" + fileName + "\" " + 
                //" " + praatFileName + " " +
                String.valueOf(begintime) + " " + String.valueOf(endtime);
        
        String[] sendpraatCom = new String[3];
        sendpraatCom[0] = sendpraatExe;
        sendpraatCom[1] = "Praat";
        sendpraatCom[2] = executeCom;
        
        if (nativeLibLoaded) {
        	String errorMessage = sendpraatNative("Praat", 0, executeCom);
        	
            if (errorMessage == null) {
            	return;
            } else {
            	ClientLogger.LOG.warning("Error occurred when using native sendpraat library: " + errorMessage);
            }
        }

        // try with the original sendpraat executable
        sendpraatExe = getPreference(SENDPRAAT_APP);

        if ((sendpraatExe == null) || (sendpraatExe.length() == 0)) {
            sendpraatExe = "sendpraat.exe";
        }
        
        sendpraatCom[0] = sendpraatExe;

        // "execute C:\Users\hasloe\.elan_data\openpraat-v3.praat 'C:\tmp\with spaces\elan-example1.wav' 9350 9810" //does not work
        // "execute C:\Users\hasloe\.elan_data\openpraat-v3.praat C:\tmp\with spaces\elan-example1.wav 9350 9810" //does not work
        // "execute C:\Users\hasloe\.elan_data\openpraat-v3.praat "C:\tmp\with spaces\elan-example1.wav" 9350 9810" //does not work
        /*
        String executeCom = "execute " + scriptFileName + " " + fileName + " " +
            String.valueOf(begintime) + " " + String.valueOf(endtime);
*/

        try {
            Runtime.getRuntime().exec(sendpraatCom);
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(new JFrame(),
                ElanLocale.getString("PraatConnection.Message.Security"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);
            LOG.warning("Cannot execute Praat (security):" + se.getMessage());

            return;
        } catch (IOException ioe) {
            LOG.warning("Cannot execute Praat (io):" + ioe.getMessage());

            // not found, prompt
            String path = locateSendpraat();

            if (path == null) {
                return;
            } else {
                // retry
                openWindowsPraat(fileName, begintime, endtime);
            }
        }
    }

    /**
     * Open Praat on non-windows systems; MacOS X, Unix, Linux. <br>
     * Note: on Mac OS X every call to runtime.exec to start Praat opens a new
     * instance of Praat: it is not detected that it is already open (this is
     * a result  of the fact that you can not use the Praat.app "folder" to
     * start Praat).  Maybe we should not try to start Praat but let the user
     * be responsible to  start Praat.
     *
     * @param fileName the path to the media file
     * @param begintime the begintime of the selection
     * @param endtime the end time of the selection
     */
    private static void openOtherPraat(String fileName, long begintime,
        long endtime) {
        if (fileName != null) {
            // Praat can handle files on samba shares too
            // on Mac: when the url starts with 3 slashes, remove 2 slashes
            // TO DO: this needs testing on other platforms
            if (fileName.startsWith("///")) {
                fileName = fileName.substring(2);
            }
            // Problems opening files with space characters in the path, on Mac
            // replacing spaces by "-" does not solve the problem
            //fileName = fileName.replaceAll(" ", "_");
        } else {
            LOG.warning("Praat: media file is null");

            return;
        }
        if (SystemReporting.isMacOS()) {
	        // first make sure praat is running
	        String praatExe = getPreference(PRAAT_APP);
	        
	        if (praatExe == null) {
	        	praatExe = locatePraat();
	        	if (praatExe.length() == 0) {
	        		praatExe = "praat";
	        	}
	        } else if (praatExe.length() == 0) {
	        	praatExe = "praat";
	        }
	
	        // check whether praat is running already; this is not a thread safe way to check
	        isPraatRunning = false;
	        checkUnixPraatProcess();
	
	        try {
	            Thread.sleep(700);
	        } catch (InterruptedException ie) {
	        }
	
	        if (!isPraatRunning) {
	            String[] praatCom = new String[] { praatExe };
	
	            try {
	                Runtime.getRuntime().exec(praatCom);
	
	                // give praat a moment to start
	                try {
	                    Thread.sleep(1500);
	                } catch (InterruptedException ie) {
	                    LOG.info("Interrupted in sleep: " + ie.getMessage());
	                }
	            } catch (SecurityException se) {
	                JOptionPane.showMessageDialog(new JFrame(),
	                    ElanLocale.getString("PraatConnection.Message.Security"),
	                    ElanLocale.getString("Message.Warning"),
	                    JOptionPane.WARNING_MESSAGE);
	                LOG.warning("Cannot execute Praat (security): " + se.getMessage());
	
	                return;
	            } catch (IOException ioe) {
	                LOG.warning("Cannot execute Praat (io): " + ioe.getMessage());
	                
	                //if (!SystemReporting.isLinux()) {// superfluous now
		                // not found, prompt
		                String path = locatePraat();
		
		                if (path == null) {
		                    return;
		                } else {
		                    // retry, don't do this or otherwise do it a limited number of times,
		                    //openOtherPraat(fileName, begintime, endtime);
		
		                    return;
		                }
	                //}
	            }
	        }
        }
        // next execute sendpraat
        String sendpraatExe = SENDPRAAT_LIB;
        
        String executeCom = "execute " + scriptFileName + " \"" + fileName + "\" " + 
        //    " " + praatFileName + " " +
            String.valueOf(begintime) + " " + String.valueOf(endtime);
        String[] sendpraatCom = new String[3];
        sendpraatCom[0] = sendpraatExe;
        sendpraatCom[1] = "praat";
        sendpraatCom[2] = executeCom;
        
        // first try native library

        if (nativeLibLoaded) {
        	String errorMes = sendpraatNative("Praat", 0, executeCom);
        	
        	if (errorMes == null) {
        		return;
        	} else {
        		ClientLogger.LOG.warning("Error occurred when using native sendpraat library: " + errorMes);
        		//showMessage(errorMes);//??
        	}
        }
        // if failed, try the sendpraat executable
        sendpraatExe = getPreference(SENDPRAAT_APP);
        if ((sendpraatExe == null)) {
        	sendpraatExe = locateSendpraat();
        	if (sendpraatExe.length() == 0) {
        		sendpraatExe = SENDPRAAT_LIB;
        	}
        } else if (sendpraatExe.length() == 0) {
        	sendpraatExe = SENDPRAAT_LIB;
        }

        sendpraatCom[0] = sendpraatExe;
        // test this on other platforms...

        try {
            Runtime.getRuntime().exec(sendpraatCom);
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(new JFrame(),
                ElanLocale.getString("PraatConnection.Message.Security"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);
            LOG.warning("Cannot execute sendpraat (security): " + se.getMessage());

            return;
        } catch (IOException ioe) {
            LOG.warning("Cannot execute sendraat (io): " + ioe.getMessage());

            // not found, prompt
            String path = locateSendpraat();

            if (path == null) {
                return;
            } else {
                // retry?
                openOtherPraat(fileName, begintime, endtime);
            }
        }

    }

    /**
     * Checks whether or not the praatscript file already exists.  When not it
     * is created in the elan directory in the user's home directory.
     *
     * @return true if the file already existed or could be created, false
     *         otherwise
     */
    private static boolean checkScript() {
        if (!checkHome()) {
            return false;
        }

        scriptFileName = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR +
            PRAAT_SCRIPT;
        
        if (!nativeLibLoaded && SystemReporting.isWindows()) {
            // Praat/sendpraat on Windows have problems with white spaces in the path
        	// even when surrounded by double quotes
            if (scriptFileName.indexOf(' ') > -1) {
                String dir = System.getProperty("java.io.tmpdir");

                if (dir != null) {
                    scriptFileName = dir + Constants.FILESEPARATOR +
                        PRAAT_SCRIPT;
                }
                // or
				// scriptFileName = spacelessWindowsPath(scriptFileName);
            }
        }
		
 
        File file = new File(scriptFileName);

        if (file.exists()) {
            return true;
        } else {
            // first try to copy the file from the .jar
            if (copyScriptFromJar(file, "/mpi/eudico/client/annotator/resources/openpraat-v3.praat")) {
                return true;
            } else {
                // fallback: create the file programmatically
                return createScriptFile(file);
            }
        }
    }

    /**
     * Checks whether or not the praat clip script file already exists.  When not it
     * is created in the elan directory in the user's home directory.
     *
     * @return true if the file already existed or could be created, false
     *         otherwise
     */
    private static boolean checkClipScript() {
        if (!checkHome()) {
            return false;
        }

        scriptFileName = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR +
            PRAAT_CLIP_SCRIPT;

        if (!nativeLibLoaded && SystemReporting.isWindows()) {
        	// Praat/sendpraat on Windows have problems with white spaces in the path
        	// even when surrounded by double quotes
            if (scriptFileName.indexOf(' ') > -1) {
                String dir = System.getProperty("java.io.tmpdir");

                if (dir != null) {
                    scriptFileName = dir + Constants.FILESEPARATOR +
                    	PRAAT_CLIP_SCRIPT; //HS 22-04-2010 changed PRAAT_SCRIPT to PRAAT_CLIP_SCRIPT!
                }
                // or
				// scriptFileName = spacelessWindowsPath(scriptFileName);
            }
        }

 
        File file = new File(scriptFileName);

        if (file.exists()) {
            return true;
        } else {
            // first try to copy the file from the .jar
            if (copyScriptFromJar(file, "/mpi/eudico/client/annotator/resources/createsoundclip-v3.praat")) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Checks whether the elan home dir exists. When not create it.
     *
     * @return true if the dir already existed or could be created, false
     *         otherwise
     */
    private static boolean checkHome() {
        File dataDir = new File(Constants.ELAN_DATA_DIR);

        if (!dataDir.exists()) {
            try {
                dataDir.mkdir();
            } catch (Exception e) {
                LOG.warning("Unable to create the Data directory: " + e.getMessage());

                return false;
            }
        }

        return true;
    }

    /**
     * Tries to copy the script file from the jar.
     *
     * @param copyFile the copy of the script
     * @param resource the fully qualified name of the resource
     *
     * @return true if the operation succeeded, false otherwise
     */
    private static boolean copyScriptFromJar(File copyFile, String resource) {
        BufferedInputStream inStream = null;
        FileOutputStream out = null;

        try {
            copyFile.createNewFile();

            URL scriptSrc = PraatConnection.class.getResource(resource);

            if (scriptSrc == null) {
                LOG.warning("No script source file found");

                return false;
            }

            inStream = new BufferedInputStream(scriptSrc.openStream());

            byte[] buf = new byte[512];
            int n;
            out = new FileOutputStream(copyFile);

            while ((n = inStream.read(buf)) > -1) {
                out.write(buf, 0, n);
            }

            out.flush();

            return true;
        } catch (IOException ioe) {
            LOG.warning("Unable to write the Praat script file: " + ioe.getMessage());

            return false;
        } finally {
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
				}
        	}
        	if (inStream != null) {
        		try {
        			inStream.close();
				} catch (IOException e) {
				}
        	}
        }
    }

    /**
     * Writes the script file to the elan home dir.
     *
     * @param scriptFile the file to write to
     *
     * @return true if everything went allright, false otherwise
     */
    private static boolean createScriptFile(File scriptFile) {
        try {
            if (!scriptFile.exists()) {
                scriptFile.createNewFile();
            }

            String contents = createScriptContents();
            FileWriter writer = new FileWriter(scriptFile);
            writer.write(contents);
            writer.close();
        } catch (IOException ioe) {
            LOG.warning("Unable to write the Praat script file: " + ioe.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Create the script. Temp. could be copied from the jar...
     *
     * @return the scriptcontents
     * @version oct 2007 changed script contents (extra argument, simplified script)
     */
    private static String createScriptContents() {
        StringBuilder scriptBuffer = new StringBuilder();
        scriptBuffer.append("form Segment info\n");
        scriptBuffer.append("\ttext Filepath \"\"\n");
        scriptBuffer.append("\ttext Filename \"\"\n");
        scriptBuffer.append("\tpositive Start 0\n");
        scriptBuffer.append("\tpositive End 10\n");
        scriptBuffer.append("endform\n");
        scriptBuffer.append("Open long sound file... 'filepath$'\n");
        scriptBuffer.append("s = start / 1000\n");
        scriptBuffer.append("en = end / 1000\n");
        scriptBuffer.append("View\n");
        scriptBuffer.append("editor LongSound 'filename$'\n");
        scriptBuffer.append("\tSelect... 's' 'en'\n");
        scriptBuffer.append("\tZoom to selection\n");
        scriptBuffer.append("endeditor");

        return scriptBuffer.toString();
    }

    /**
     * Promt the user for the location of the Praat executable.
     *
     * @return the path to the Praat executable
     */
    private static String locatePraat() {
        String praatPath = null;
        FileChooser chooser = new FileChooser(new JFrame());
        chooser.createAndShowFileDialog(ElanLocale.getString("PraatConnection.LocateDialog.Title1"), FileChooser.OPEN_DIALOG, ElanLocale.getString("PraatConnection.LocateDialog.Select"), 
        		null, null, true, "PraatLocation.Dir", FileChooser.FILES_ONLY, null);
        
        File path = chooser.getSelectedFile();

        if (path == null) {
            // cannot remove a preference yet
            setPreference(PRAAT_APP, "");
        } else {           
            praatPath = path.getAbsolutePath();
            
            // Mac specific addition
            if (path.isDirectory() && praatPath.endsWith(".app")) {
                // append path to the actual executable
                praatPath += "/Contents/MacOS/Praat";
            }
            // trust the user, cannot possibly check the value
            setPreference(PRAAT_APP, praatPath);
        }
        return praatPath;
    }

    /**
     * Promt the user for the location of the sendpraat executable.
     *
     * @return the path to the sendpraat executable
     */
    private static String locateSendpraat() {      
        FileChooser chooser = new FileChooser(new JFrame());
        chooser.createAndShowFileDialog(ElanLocale.getString("PraatConnection.LocateDialog.Title2"), FileChooser.OPEN_DIALOG, ElanLocale.getString(
        	"PraatConnection.LocateDialog.Select"), null, null, true, "PraatLocation.Dir", FileChooser.FILES_ONLY, null);
        
        File path = chooser.getSelectedFile();

        if (path == null) {
            // cannot remove a preference yet
            setPreference(SENDPRAAT_APP, "");
            return null;
        } else {
        	setPreference(SENDPRAAT_APP, path.getAbsolutePath());
        	return path.getAbsolutePath();
        }
    }

    /**
     * Returns the pref for the specified key like the Praat and sendpraat
     * executable paths from the praat pref file.
     *
     * @param key the key for the pref
     *
     * @return the pref
     */
    private static String getPreference(String key) {
        if (key == null) {
            return null;
        }

        if (preferences == null) {
            preferences = loadPreferences();
            // if it is from an old pfs file (Hashtable) force write of xml
            if (preferences instanceof Hashtable) {
            	savePreferences();
            }
        }

        return (String) preferences.get(key);
    }

    /**
     * Loads the praat preferences from the praat pref file.
     *
     * @return a map with the pref mappings
     */
    private static Map<String, String> loadPreferences() {
    	// first try to load an xml version of the preferences
    	PreferencesReader reader = new PreferencesReader();
    	Map<String, String> prefMap = reader.parse(PRAAT_PREFS_XML_FILE);
    	if (prefMap.size() > 0) {
    		return prefMap;
    	}

    	Hashtable<String, String> hashtable = new Hashtable<String, String>();

        File inFile = new File(PRAAT_PREFS_FILE);

        try {
            //if (inFile.exists()) {
                FileInputStream inStream = new FileInputStream(inFile);
                ObjectInputStream objectIn = new ObjectInputStream(inStream);
                hashtable = (Hashtable<String, String>) objectIn.readObject();
                objectIn.close();
                inStream.close();
            //}
        } catch (FileNotFoundException fnfe) {
            LOG.warning("Could not load Praat preferences: " + fnfe.getMessage());
        } catch (IOException ioe) {
            LOG.warning("Could not load Praat preferences: " + ioe.getMessage());
        } catch (ClassNotFoundException cnfe) {
            LOG.warning("Could not load Praat preferences: " + cnfe.getMessage());
        }

        return hashtable;
    }

    /**
     * Sets the specified pref to the new value.
     *
     * @param key the pref key
     * @param value the pref value
     */
    private static void setPreference(String key, String value) {
        if ((key == null) || (value == null)) {
            return;
        }

        if (preferences == null) {
            preferences = new HashMap<String, String>();
        }

        preferences.put(key, value);

        savePreferences();
    }

    /**
     * Wrties the praat preferences file to disc.
     */
    private static void savePreferences() {
        if (preferences == null) {
            return;
        }
        PreferencesWriter writer = new PreferencesWriter();
        writer.encodeAndSave(preferences, PRAAT_PREFS_XML_FILE);
        /*
        try {
            FileOutputStream outStream = new FileOutputStream(PRAAT_PREFS_FILE);
            ObjectOutputStream objectOut = new ObjectOutputStream(outStream);
            objectOut.writeObject(preferences);
            objectOut.close();
            outStream.close();
        } catch (FileNotFoundException fnfe) {
            LOG.warning("Could not save Praat preferences: " + fnfe.getMessage());
        } catch (IOException ioe) {
            LOG.warning("Could not save Praat preferences: " + ioe.getMessage());
        }
        */
    }

    /**
     * Tries to check whether or not Praat is already running using the  Unix
     * utility <code>top</code>.
     */
    private static void checkUnixPraatProcess() {
        try {
            final Process p = Runtime.getRuntime().exec(new String[] {
                        "top", "-l1"
                    });

            new Thread(new Runnable() {
                    @Override
					public void run() {
                        try {
                            InputStreamReader isr = new InputStreamReader(p.getInputStream());
                            BufferedReader br = new BufferedReader(isr);
                            String line = null;

                            while ((line = br.readLine()) != null) {
                                if (line.indexOf(" Praat ") > 0) {
                                    br.close();
                                    isPraatRunning = true;

                                    break;
                                }
                            }
                        } catch (IOException ioe) {
                            LOG.warning("Cannot determine the running state of Praat: " + ioe.getMessage());
                        }
                    }
                }).start();
        } catch (SecurityException sex) {
            LOG.warning("Cannot run the \"top\" utility: " + sex.getMessage());
        } catch (IOException ioe) {
        	LOG.warning("Cannot run the \"top\" utility: " + ioe.getMessage());
        }
    }

    /**
     * Converts a file path containing spaces in directory names to a path with
     * "DIRECT~x" elements. The filename itself will not be converted.
     *
     * @param fileName the file path with spaces
     *
     * @return a converted path
     */
    private static String spacelessWindowsPath(String fileName) {
        if (fileName == null) {
            return null;
        }

        //String path = fileName;
        int firstSpace = fileName.indexOf(' ');

        if (firstSpace < 0) {
            return fileName;
        }

        int prevSep = fileName.lastIndexOf(File.separator, firstSpace);
        int nextSep = fileName.indexOf(File.separator, firstSpace);
        String fileOrDirName = null;

        if (nextSep > 0) {
            fileOrDirName = fileName.substring(prevSep + 1, nextSep);
        } else {
            //fileOrDirName = fileName.substring(prevSep + 1);
            return fileName;
        }

        // if an tilde already has been added, prevSep + 1 does not include the file separator ?? 
        try {
            File dir = new File(fileName.substring(0, prevSep + 1));

            if (!dir.isDirectory()) {
                return fileName;
            }

            String start = fileOrDirName.substring(0, fileOrDirName.indexOf(' '));
            String[] fNames = dir.list();

            int sufNum = 0;

            for (String fName : fNames) {
                if (fName.startsWith(fileOrDirName)) {
                    sufNum++;

                    if (fName.equals(fileOrDirName)) {
                        break;
                    }
                }
            }

            StringBuilder pathBuf = new StringBuilder(dir.getPath());

            // see comment above
            if (pathBuf.charAt(pathBuf.length() - 1) != File.separatorChar) {
                pathBuf.append(File.separator);
            }

            if (start.length() <= 6) {
                String tmp = fileOrDirName.replaceAll(" ", "");

                if (tmp.length() <= 6) {
                    pathBuf.append(tmp.toUpperCase()).append("~").append(sufNum);
                } else {
                    pathBuf.append(tmp.substring(0, 6).toUpperCase()).append("~")
                           .append(sufNum);
                }
            } else {
                pathBuf.append(start.substring(0, 6).toUpperCase()).append("~")
                       .append(sufNum);
            }

            if (nextSep > 0) {
                pathBuf.append(fileName.substring(nextSep));
            }

            if (pathBuf.indexOf(" ") < 0) {
                return pathBuf.toString();
            } else {
                return spacelessWindowsPath(pathBuf.toString());
            }
        } catch (Exception ex) {
            LOG.warning("Invalid directory: " + ex.getMessage());

            return fileName;
        }
    }
    
    /**
     * Creates a Praat-like object name; a file name, without the path part, without extension
     * and with spaces replaced by underscores
     *  
     * @param filePath
     * @return
     */
    private static String praatFriendlyFileName(String filePath) {
    	    String pffn = filePath.replace('\\', '/');
    	    int b = pffn.lastIndexOf('/');
    	    int e = pffn.lastIndexOf('.');
    	    
    	    if (b > -1) {
    	    		if (e > b + 1) {
    	    			pffn = pffn.substring(b + 1, e);
    	    		} else {
    	    			pffn = pffn.substring(b + 1);
    	    		}
    	    } else if (e > -1) {
    	    		pffn = pffn.substring(0, e);
    	    }
    	    
    	    pffn = pffn.replace(' ', '_');
    	    pffn = pffn .replace('.', '_');
    		
    	    return pffn;
    }
}
