package mpi.eudico.client.annotator;

import java.awt.EventQueue;
import java.io.File;

import mpi.eudico.client.annotator.turnsandscenemode.TaSFrame;
import mpi.eudico.client.annotator.util.ClientLogger;

/**
 * Main class for Simple-ELAN.
 * 
 * @author Han Sloetjes
 */
public class SIMPLELAN {
    /** the major version value */
    public static int major = 1;

    /** the minor/bug-fix version value */
    public static int minor = 2;
    
    /** the application name */
    public final static String appName = "Simple-ELAN";
    /**
     * Private constructor
     */
	private SIMPLELAN() {
	}

	public static void main(final String[] args) {
		System.out.println("SIMPLE-ELAN Launch");
		System.setProperty("ELANApplicationMain", SIMPLELAN.class.getName());
		
    	ClientLogger.LOG.info(String.format("%s %s\n", appName, getVersionString()) + 
    			ELAN.getSystemAndUserInfo());
    	
		// initialize platform dependent settings, preferences
		ELAN.initPlatformPreferences();
        ELAN.readProperties();
		ELAN.updateUIDefaults();
		ELAN.detectUILabelFont();
		
        //FrameManager.getInstance().setExitAllowed(true);
        // create the frame on the event dispatch thread
        
        EventQueue.invokeLater(new Runnable() {
                @Override
				public void run() {
                	TaSFrame slFrame = null;
                	if ((args != null) && (args.length > 0) && (args[0].length() != 0)) {
                		File argFile = new File(args[0]); 
                		//System.out.println("F " + argFile.getAbsolutePath());
                		//System.out.println("A " + argFile.isAbsolute());
                    	// HS July 2008: check if the argument (filepath to eaf) is a relative
                    	// path. If so let the jvm resolve it relative to the current directory 
                    	// (where ELAN is launched from)
                    	if (!argFile.isAbsolute()) { 
                    		//System.out.println("F " + argFile.getAbsolutePath());
                    		//FrameManager.getInstance().createFrame(argFile.getAbsolutePath());
                    		slFrame = new TaSFrame(argFile.getAbsolutePath());
                    		//FrameManager.getInstance().addFrame(slFrame);
                    	} else {
                    		//FrameManager.getInstance().createFrame(args[0]);
                    		slFrame = new TaSFrame(args[0]);
                    		//FrameManager.getInstance().addFrame(slFrame);
                    	}	                    	
                	} else {
                		slFrame = new TaSFrame();
                	}
                	
                	if (slFrame != null) {
                		slFrame.setVisible(true);
                		
                		if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
                			try {
                            	Class<?> macHandler = null;
                            	try {
                            		macHandler = Class.forName("mpi.eudico.client.mac.MacAppHandler2");
                            	} catch (Throwable anyCause) {
                            		System.out.println("Loading Apple integration 2 failed");
                            		//anyCause.printStackTrace();
                            	}
                                Class<?> macList = Class.forName(
                                        "mpi.eudico.client.annotator.turnsandscenemode.TaSFrame$SimpleELANMacApplication");
                                Class<?> superList = macList;
                                if (macList.getInterfaces().length > 0) {
                                	superList = macList.getInterfaces()[0];
                                }
                                java.lang.reflect.Constructor<?> con = macHandler.getConstructor(superList);
                                con.newInstance(slFrame.new SimpleELANMacApplication());
                            } catch (Throwable ex) {
                                System.out.println("Could not load Mac application handler.");
                                //ex.printStackTrace();
                            }
                		}
                	}
                }
            });
           
	}
	
	/**
	 * @return the name of the application as shown to the users
	 */
	public static String getApplicationName() {
		return appName;
	}
	
    /**
     * Returns the current version information as a string.
     *
     * @return the current version
     */
    public static String getVersionString() {
        return major + "." + minor;
    }
    
}
