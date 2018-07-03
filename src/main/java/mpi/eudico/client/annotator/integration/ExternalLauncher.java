package mpi.eudico.client.annotator.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.FrameManager;

import org.xml.sax.SAXException;


/**
 * This class acts as a listener to a file in the elan home dir and opens a new
 * ElanFrame once a new open request has been written to it. Follows the
 * Singleton pattern.
 *
 * @author Han Sloetjes
 */
public class ExternalLauncher {
    /** a Logger */
    private static final Logger LOG = Logger.getLogger(ExternalLauncher.class.getName());
    private static String exchangeFile = "externallaunch";

    /** the open command */

    //private static final String OPEN = "open";

    /** the executed command */

    //private static final String EXECUTED = "executed";

    /** the IMDI session command */
    private static final String IMDI_SESSION = "IMDI_Session: ";
    private static File launchFile;
    private static FileReader reader;
    private static ExternalLauncher externalLauncher;
    private static LaunchThread launchThread;
    private static long lastModified = 0L;
    private static boolean running = false;

    /**
     * Creates a new ExternalLauncher instance
     */
    private ExternalLauncher() {
        init();
    }

    /**
     * Starts the listener/polling thread.
     */
    public static void start() {
        if (externalLauncher == null) {
            externalLauncher = new ExternalLauncher();
            LOG.info("External launch thread started...");
        }
    }

    /**
     * Stops the listener/polling thread and deletes the external launch file.
     * Note: maybe change the return type to boolean, to confirm the deletion
     * of the file succeeded.
     */
    public static void stop() {
        running = false;

        if ((launchThread != null) && launchThread.isAlive()) {
            try {
                launchThread.interrupt();
            } catch (SecurityException se) {
                LOG.warning("Could not stop the launch thread: " + se.getMessage());
            }
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ioe) {
                LOG.warning("Could not close the file: " +
                    launchFile.getAbsolutePath() + " : " +
                    ioe.getMessage());
            }
        }

        if (launchFile != null) {
            try {
                launchFile.delete();
            } catch (SecurityException se) {
                LOG.warning("Could not delete the file: " +
                    launchFile.getAbsolutePath() + " : " +
                    se.getMessage());
            }
        }

        LOG.info("External launch thread stopped...");
    }

    /**
     * Starts the listener thread.
     *
     * @param exchangeFileName the file to read from
     */
    public static void start(String exchangeFileName) {
        if (externalLauncher == null) {
            if ((exchangeFileName != null) && (exchangeFileName.length() > 0)) {
                exchangeFile = exchangeFileName;
            }

            externalLauncher = new ExternalLauncher();
        }
    }

    private static void init() {
        launchFile = new File(Constants.ELAN_DATA_DIR, exchangeFile);

        if (launchFile.exists()) {
            launchFile.deleteOnExit(); //??
        }

        launchThread = new LaunchThread(1000);
        launchThread.setPriority(Thread.MIN_PRIORITY);
        running = true;
        launchThread.start();

        // for testing
        // new TestLauncher().start();		
    }

    private static void checkChange() {
        if (!running) {
            return;
        }

        if ((launchFile == null) || !launchFile.exists()) {
            return;
        }

        if ((lastModified == 0) || (lastModified < launchFile.lastModified())) {
            try {
                reader = new FileReader(launchFile);

                char[] ch = new char[(int) launchFile.length()];
                reader.read(ch);

                String line = new String(ch);

                if (line != null) {
                    //System.out.println("Read: " + line);
                    openElanFrame(line);

                    lastModified = launchFile.lastModified();
                }

                reader.close();
            } catch (FileNotFoundException fnfe) {
                LOG.severe("Could not read file: " +
                    launchFile.getAbsolutePath() + " : " +
                    fnfe.getMessage());
            } catch (IOException ioe) {
                LOG.severe("Error reading file: " +
                    launchFile.getAbsolutePath() + " : " +
                    ioe.getMessage());
            }
        }
    }

    private static void openElanFrame(String line) {
        if ((line == null)) {
            return;
        }

        //String directive = line.substring(OPEN.length() + 1);
        if (line.startsWith(IMDI_SESSION)) {
            // parse imdi file
            String IMDIString = line.substring(IMDI_SESSION.length());

            if (IMDIString.length() > 0) {
            	IMDIString = pathToURLString(IMDIString);
            	
                IMDISessionParser parser = new IMDISessionParser();
                HashMap filesMap = null;

                try {
                    parser.parse(IMDIString);
                    filesMap = parser.getFilesMap();
                } catch (IOException ioe) {
                    LOG.warning("Could not access the .imdi file: " +
                        ioe.getMessage());
					showWarningDialog("Elan launcher:\nCould not access the imdi file.");
                    return;
                } catch (SAXException sax) {
                    LOG.warning("Could not parse the .imdi file: " +
                        sax.getMessage());
					showWarningDialog("Elan launcher:\nCould not parse the imdi file.");
                    return;
                }

                if (filesMap != null) {
                    String eafString = (String) filesMap.get(IMDISessionParser.EAF);

                    if ((eafString == null) ||
                            !eafString.toLowerCase().endsWith(".eaf")) {
                        LOG.warning("No .eaf file found");
						showWarningDialog("Elan launcher:\nNo eaf file found in the imdi file.");
                        return;
                    } else {
                    	// create an absolute path...
                    	eafString = absolutePath(IMDIString, eafString);
                    	LOG.info("Launching: " + eafString);
                    }

                    String videoString = (String) filesMap.get("video");
                    String audioString = (String) filesMap.get("audio");
                    List<String> mediaFiles = new ArrayList<String>();

                    if (videoString != null) {
                        mediaFiles.add(absolutePath(IMDIString, videoString));
                    }

                    if (audioString != null) {
                        mediaFiles.add(absolutePath(IMDIString, audioString));
                    }

                    //new ElanFrame2(eafString, mediaFiles);
                    FrameManager.getInstance().createFrame(eafString, mediaFiles);
                }
            }
        }
    }
    
   /**
	* Convert a path to a file URL string. Takes care of Samba related problems
	* file:///path works for all files except for samba file systems, there we need file://machine/path,
	* i.e. 2 slashes instead of 3
	*
	* What's with relative paths?
	*/
    private static String pathToURLString(String path) {
		// replace all back slashes by forward slashes
		String pathURL = path.replace('\\', '/');
		
		boolean isFileURL = pathURL.startsWith("file:");
		
		if (isFileURL) {
			pathURL = pathURL.substring(5);			
		
			// remove leading slashes and count them
			int n = 0;
		
			while (pathURL.charAt(0) == '/') {
				pathURL = pathURL.substring(1);
				n++;
			}
		
			// add the file:// or file:/// prefix
			if (n == 2) {
				if (pathURL.charAt(1) == ':') {
					// local drive
					return "file:///" + pathURL;
				} else {
					// samba share
					return "file://" + pathURL;
				}
			} else {
				return "file:///" + pathURL;
			}
		} else {
			// this can still be a local file...
			return pathURL;
		}
	}
	
	/**
	 * Creates an absolute path for a file.
	 * If the file name is not an absolute path, an absolute path is created 
	 * by resolving from the base document's path.
	 * 
	 * @param fromDoc the base file 
	 * @param fileName the file to create an absolute path for
	 * @return an absolute path as a String, or null
	 */
	private static String absolutePath(String fromDoc, String fileName) {
		if (fileName == null) {
			return null;
		}
		fileName = fileName.replace('\\', '/');
		URI fileUri = null;
		try {
			fileUri = new URI(fileName);
			if (fileUri.isAbsolute()) {
				String resString = fileUri.toString();
				
				if (resString.startsWith("file:") || resString.startsWith("//")) {
					return fileUri.getSchemeSpecificPart();
				} else {
					return resString;
				}
			} else {
				if (fromDoc == null) {
					// give it a try
					return fileUri.getSchemeSpecificPart();
				} else {
					URI baseUri = null;
					try {
						baseUri = new URI(fromDoc);
						fileUri = new URI(stripLeadingSlashes(fileName));
						URI resolved = baseUri.resolve(fileUri.getSchemeSpecificPart());
							//stripLeadingSlashes(fileUri.getSchemeSpecificPart()));
						
						String resString = resolved.toString();
						
						if (resString.startsWith("file:") || resString.startsWith("//")) {
							return resolved.getSchemeSpecificPart();
						} else {
							return resString;
						}		
					} catch (URISyntaxException ue) {
						LOG.warning("URI: no context for relative file path: " + 
							ue.getMessage());
						return fileUri.getSchemeSpecificPart();
					}
				}
			}
		} catch (URISyntaxException ue) {
			LOG.warning("URI: invalid file path: " + 
				ue.getMessage());
		}
		return null;
	}
	
	/**
	 * Strip leading forward slashes from relative paths.
	 * @param in the path
	 * @return the stripped path, or null
	 */
	private static String stripLeadingSlashes(String in) {
		if (in == null) {
			return null;
		}
		String strip = in;
		while (strip.charAt(0) == '/') {
			strip = strip.substring(1);
		}
		return strip;
	}
	
	/**
	 * Shows a warning/error dialog with the specified message string.
	 *
	 * @param message the message to display
	 */
	private static void showWarningDialog(String message) {
		JOptionPane.showMessageDialog(null, message,
			"Warning", JOptionPane.WARNING_MESSAGE);
	}

    /**
     * Makes sure the listener thread is stopped. Clean up by calling stop()
     *
     * @see #stop()
     */
    @Override
	public void finalize() {
        ExternalLauncher.stop();
    }

    /**
     * A thread that periodically checks for changes in an exchange file.
     *
     * @author Han Sloetjes
     * @version 1.0
     */
    private static class LaunchThread extends Thread {
        private int delay;

        /**
         * Creates a new LaunchThread instance
         *
         * @param delay the delay period
         */
        LaunchThread(int delay) {
            this.delay = delay;
        }

        /**
         * Implements Runnable run method.
         */
        @Override
		public void run() {
            while (running) {
                ExternalLauncher.checkChange();

                try {
                    sleep(delay);
                } catch (InterruptedException ie) {
                    LOG.info("Launch thread interrupted...");
                }
            }
        }
    }

    /**
     * Test thread.
     *
     * @author Han Sloetjes
     * @version 1.0
     */
    private static class TestLauncher extends Thread {
        /** Holds value of property number of test runs */
        final int num = 1;

        /**
         * Implements Runnable run method.
         */
        @Override
		public void run() {
            int i = 0;

            while (i < num) {
                try {
                    sleep(5000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

                try {
                    File launchFl = new File(Constants.ELAN_DATA_DIR,
                            exchangeFile);

                    if (!launchFl.exists()) {
                        boolean success = launchFl.createNewFile();

                        if (!success) {
                            LOG.severe("Could not create file: " +
                                launchFl.getAbsolutePath());

                            return;
                        }
                    }

                    FileWriter writer = new FileWriter(launchFl);
                    writer.write(
                        "IMDI_Session: file:/D:/Dev_MPI/resources/testdata/elan/test.imdi");
					//writer.write(
					 //   "IMDI_Session: //Nt04/users1/hasloe/test.imdi");
					//writer.write( "IMDI_Session: C:/tmp/test.imdi");
                    writer.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                i++;
            }
        }
    }
}
