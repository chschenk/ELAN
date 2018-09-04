package mpi.eudico.client.annotator;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JPopupMenu;
import javax.swing.UIDefaults;
//import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import mpi.eudico.client.annotator.update.ExternalUpdaterThread;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.SystemReporting;


/**
 * The main class for ELAN. Main performs some initialization and creates the
 * first frame. Holds version information, major, minor and micro.
 * 
 * @version May 2017 some methods changed from private to package private so that 
 * they are visible in Simple-ELAN
 */
public class ELAN {
    /** the major version value */
    public static int major = 5;

    /** the minor version value */
    public static int minor = 3;

    /** the micro (bug fix) version value
     *  No longer used as of ELAN 5.0, Oct 2017 */
    public static int micro = 0;
    
    /** a string version suffix, e.g. alpha or beta or whatever */
    public static String versionSuffix = "";
    
    public static final String appName = "ELAN";
    /** the default name of a properties file to be read when ELAN is launched */
    public static final String propertiesFileName = "elan.properties";

    /**
     * Creates a new ELAN instance
     */
    private ELAN() {
    }

    /**
     * Main method, initialization and first frame.
     *
     * @param args the arguments, path to an eaf file
     */
    public static void main(final String[] args) {
    	ClientLogger.LOG.info("");
    	System.out.println("\n@ELAN Launched\n");
    	System.setProperty("ELANApplicationMain", ELAN.class.getName());

    	ClientLogger.LOG.info(String.format("%s %s\n", appName, getVersionString()) + 
    			getSystemAndUserInfo());
    	 
    	/*
    	if (args != null && args.length > 0) {
    		for (String s : args) {
    			System.out.println("arg: " + s);
    		}
    	} else {
    		System.out.println("No args!");
    	}
    	*/
    	initPlatformPreferences();
        readProperties();
        updateUIDefaults();
        detectUILabelFont();
        
        FrameManager.getInstance().setExitAllowed(true);
        
        // create the frame on the event dispatch thread
        
        if ((args != null) && (args.length > 0) && (args[0].length() != 0)
        		// 2015-11-18 robert.fromont@canterbury.ac.nz
        	    && (!args[0].startsWith("-")) // not a command-line switch
        	    ) {
            EventQueue.invokeLater(new Runnable() {
                    @Override
					public void run() {
                		File argFile = new File(args[0]); 
                		//System.out.println("F " + argFile.getAbsolutePath());
                		//System.out.println("A " + argFile.isAbsolute());
                    	// HS July 2008: check if the argument (filepath to eaf) is a relative
                    	// path. If so let the jvm resolve it relative to the current directory 
                    	// (where ELAN is launched from)
                		// HS June 2016 restore support for passing media files along with an etf or eaf file
                		List<String> mediaFiles = null;
                		if (args.length > 1) {
                			mediaFiles = new ArrayList<String>();
                			for (int i = 1; i < args.length; i++) {
                				File mediaFile = new File(args[i]);
                				if (!mediaFile.isAbsolute()) {
                					mediaFiles.add(mediaFile.getAbsolutePath());
                				} else {
                					mediaFiles.add(args[i]);
                				}
                			}
                		}
                		
                    	if (!argFile.isAbsolute()) { 
                    		//System.out.println("F " + argFile.getAbsolutePath());
                    		FrameManager.getInstance().createFrame(argFile.getAbsolutePath(), mediaFiles);
                    	} else {
                    		FrameManager.getInstance().createFrame(args[0], mediaFiles);
                    	}
                    }
                });
        } else {
            EventQueue.invokeLater(new Runnable() {
                    @Override
					public void run() {
                        FrameManager.getInstance().createEmptyFrame();
                    }
                });
        }

        // external launcher, currently only accepts imdi files to open an eaf
        // from another application/VM
        // HS 02-2012 has not been used for years
        //mpi.eudico.client.annotator.integration.ExternalLauncher.start();
        
        // automatic check for version update
        Boolean boolPref = Preferences.getBool("AutomaticUpdate", null);		
    	if (boolPref == null || boolPref) {
    		ExternalUpdaterThread updater = new ExternalUpdaterThread();
            updater.start();
    	}           
        
        // create one Mac Application handler per jvm
        if (System.getProperty("os.name").indexOf("Mac OS") > -1) {
            try {
            	Class<?> macHandler = null;
            	try {
            		macHandler = Class.forName("mpi.eudico.client.mac.MacAppHandler2");
            		System.out.println("Loading new Apple integration 2");
            	} catch (Throwable anyCause) {
            		macHandler = Class.forName(
                    "mpi.eudico.client.mac.MacAppHandler");	
            		System.out.println("Loading Apple integration 1");
            		//anyCause.printStackTrace();
            	}
                
                Class<?> macList = Class.forName(
                        "mpi.eudico.client.mac.MacApplicationListener");
                java.lang.reflect.Constructor<?> con = macHandler.getConstructor(macList);
                // the Handler's constructor registers itself to Application etc.
                con.newInstance(new ElanMacApplication());
            } catch (Throwable ex) {
                System.out.println("Could not load Mac application handler.");
                //ex.printStackTrace();
            }
        }
        /*
		printSystemProperties();
        */
    }

	/**
	 * Platform dependent handling of a few preferences that need to be set before
	 * a window (or in general a UI) is created and of the default data/preferences 
	 * folder. It creates the ELAN data folder if it doesn't exist already
	 * and moves old data/preferences.
	 */
	static void initPlatformPreferences() {
		// make sure the directory for ELAN data exists, could move to preferences?
        try {
            /* HS May 2008: copy files to the new ELAN data folder. Do this only once. */
            if (SystemReporting.isMacOS()) {
            	/* May 2016 removed copying of old data files, now the generic folder creation below
            	 * is used on Mac OS as well */
//            	File dataFolder = new File(Constants.ELAN_DATA_DIR);
//            	if (!dataFolder.exists()) {
//            		dataFolder.mkdir();
//            	}
            	boolean screenBar = false;//default
            	Boolean boolPref = Preferences.getBool("OS.Mac.useScreenMenuBar", null);
            	if (boolPref != null) {
            		screenBar = boolPref.booleanValue();
            		System.setProperty("apple.laf.useScreenMenuBar", String.valueOf(screenBar));
            	}
            	// using the screen menu bar implies the default Mac OS L&F
            	if (!screenBar) {
            		boolPref = Preferences.getBool("UseMacLF", null);
            		
            		if (boolPref != null) {
            			boolean macLF = boolPref.booleanValue();
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
            		String stringPref = Preferences.getString("Mac.PrefMediaFramework", null);
            		if (stringPref != null) {
            			System.setProperty("PreferredMediaFramework", stringPref);
            		}
            	}

            }// end Mac OS initialization
            else if (SystemReporting.isWindows()) {// windows user preferred media framework
            	if (System.getProperty("PreferredMediaFramework") == null) {
            		String stringPref = Preferences.getString("Windows.PrefMediaFramework", null);
            		if (stringPref != null) {
            			System.setProperty("PreferredMediaFramework", stringPref);
            		}
            	}

            	Boolean boolPref = Preferences.getBool("UseWinLF", null);
            	if (boolPref != null) {
            		if (boolPref) {
                    	try {
                        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception ex) {
                        	ClientLogger.LOG.warning("Could not set the Look and Feel");
                        }
            		}
            	}
            }// end Windows initialization
            else if (SystemReporting.isLinux()) {// Linux user preferred media framework
            	if (System.getProperty("PreferredMediaFramework") == null) {
            		String stringPref = Preferences.getString("Linux.PrefMediaFramework", null);
            		if (stringPref != null) {
            			System.setProperty("PreferredMediaFramework", stringPref);
            		}
            	}
            }// end Linux initialization
            // generic data folder creation
            File dataDir = new File(Constants.ELAN_DATA_DIR);

            if (!dataDir.exists()) {
                dataDir.mkdir();
            }
            
        } catch (Throwable ex) {
            // catch any
        	ClientLogger.LOG.warning("Could not create the application's data directory or apply some preferences: " + ex.getMessage());
        }
	}

	/**
	 * Read "elan.properties", and set everything therein as a system property.
	 * In fact, the file name "elan.properties" can be changed via a system property
	 * with the name "elan.properties".
	 * Example from  https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
	 */
	static void readProperties() {
    	FileInputStream propFile = null;
    	String propFileName = System.getProperty(propertiesFileName, propertiesFileName);
    	
        try {
        	propFile = new FileInputStream(propFileName);
            Properties p = new Properties(System.getProperties());
            p.load(propFile);

            // set the system properties
            System.setProperties(p);
            // display new properties
            //System.getProperties().list(System.out);
        } catch (FileNotFoundException e) {
        	// Don't care
		} catch (IOException e) {
        	// Let the user know
			ClientLogger.LOG.info("IOException while reading '"+propFileName+"' file: " +
					e.toString());
		} finally {
			if (propFile != null) {
				try {
					propFile.close();
				} catch (IOException e) {
				}
			}
        }
	}
	
	/**
	 * Update default UI values, especially font sizes, if preferences or properties
	 * have been set.
	 */
	static void updateUIDefaults() {
		// the default font for tiers/annotations property
		String defFontName = Constants.DEFAULTFONT.getFontName();
		int defFontSize = Constants.DEFAULTFONT.getSize();
		
		String defFontNameProp = System.getProperty("ELAN.Tiers.DefaultFontName");
		String defFontSizeProp = System.getProperty("ELAN.Tiers.DefaultFontSize");
		if (defFontNameProp != null && !defFontNameProp.isEmpty()) {
			defFontName = defFontNameProp;
		}
		if (defFontSizeProp != null && !defFontSizeProp.isEmpty()) {
			try {
				defFontSize = Integer.parseInt(defFontSizeProp);
			} catch (NumberFormatException nfe) {
				
			}
		}
		if (!defFontName.equals(Constants.DEFAULTFONT.getFontName()) || 
				defFontSize != Constants.DEFAULTFONT.getSize()) {
			Constants.setDefaultFont(defFontName, defFontSize);
		}
		// on high resolution displays a font scaling factor might be estimated automatically
		boolean allowAutoScaling = true;
		String autoScalingProp = System.getProperty("ELAN.UI.AutoDetectFontScaleFactor");
		if (autoScalingProp != null) {
			allowAutoScaling = Boolean.valueOf(autoScalingProp);
		}
		// first try the "launch properties"  file
		String fontScaleFactorProp = System.getProperty("ELAN.UI.FontScaleFactor");
		float scaleFactor = 1.0f;// -> load from preference or properties
		
		if (fontScaleFactorProp != null) {
			fontScaleFactorProp.replace(',', '.');// just to be sure
			try {
				scaleFactor = Float.parseFloat(fontScaleFactorProp);
			} catch (NumberFormatException nfe) {
				ClientLogger.LOG.warning("The value for the property 'ELAN.UI.FontScaleFactor' is not a valid float number");
			}
		}
        
        if (scaleFactor == 1) {
        	// next try the font scale preference setting
        	Float prefScaleFactor = Preferences.getFloat("UI.FontScaleFactor", null);
        	if (prefScaleFactor != null) {
        		scaleFactor = prefScaleFactor.floatValue();
        		if (scaleFactor == 1) {
        			// check the screen resolution and calculate the scale factor
        			int screenRes = SystemReporting.getScreenResolution();
        			if (screenRes > Constants.LOW_RES_SCREEN_DPI && allowAutoScaling) {
        				scaleFactor = screenRes / (float) Constants.LOW_RES_SCREEN_DPI;
        			} else {
        				// don't change anything
        				return; 
        			}
        		}
        	} else {
    			// check the screen resolution and calculate the scale factor
    			int screenRes = SystemReporting.getScreenResolution();
    			if (screenRes > Constants.LOW_RES_SCREEN_DPI && allowAutoScaling) {
    				scaleFactor = screenRes / (float) Constants.LOW_RES_SCREEN_DPI;
    			} else {
    				// don't change anything
    				return; 
    			}
        	}
        }	
		
        //printUIDefaults();
        
        // all font resource related key - value pairs
        Map<String, Object> fontResourcesMap = new HashMap<String, Object>();
        // the same font resources is (or is possibly) used for multiple keys, replicate that approach
        List<FontUIResource> fontResources = new ArrayList<FontUIResource>();
        
        UIDefaults curLFDefaults = UIManager.getLookAndFeelDefaults();
        Map<Object, Object> uid = new HashMap<Object, Object>(curLFDefaults);
        Iterator<Object> keyIt = uid.keySet().iterator();
        while (keyIt.hasNext()) {
        	Object key = keyIt.next();
        	String keyString = key.toString();
        	Object value = curLFDefaults.get(keyString);
        	if (value instanceof FontUIResource) {
        		fontResourcesMap.put(keyString, value);
        		if (!fontResources.contains(value)) {
        			fontResources.add((FontUIResource)value);
        		}
        	}
        }
        
        for (FontUIResource fur : fontResources) {
        	FontUIResource updateFUR = new FontUIResource(fur.getFontName(), fur.getStyle(), 
        			(int) Math.ceil(scaleFactor * fur.getSize()));
        	Iterator<String> fontIt = fontResourcesMap.keySet().iterator();
        	while(fontIt.hasNext()) {
            	String key = fontIt.next();
            	Object value = fontResourcesMap.get(key);
            	if (value.equals(fur)) {
            		//fontResourcesMap.put(key, updateFUR);
            		UIManager.getLookAndFeelDefaults().put(key, updateFUR);
            	}
        	}
        }
      //UIManager.getLookAndFeelDefaults().putAll(fontResourcesMap);
        
        Object treeRowHeight = curLFDefaults.get("Tree.rowHeight");
        if (treeRowHeight instanceof Integer) {
        	int nextHeight = (int) (scaleFactor * (Integer) treeRowHeight);
        	UIManager.getLookAndFeelDefaults().put("Tree.rowHeight", nextHeight);
        }
        
        Object tableRowHeight = curLFDefaults.get("Table.rowHeight");
        if (tableRowHeight instanceof Integer) {
        	int nextHeight = (int) (scaleFactor * (Integer) tableRowHeight);
        	UIManager.getLookAndFeelDefaults().put("Table.rowHeight", nextHeight);
        }
        
        Constants.setFontScaling(scaleFactor);       
	}
	
	/**
	 * Stores the look and feel font for Labels and stores it in the
	 * Constants class. 
	 */
	static void detectUILabelFont() {
		Object labelFont = UIManager.getLookAndFeelDefaults().get("Label.font");
		if (labelFont instanceof FontUIResource) {
			Constants.setLookAndFeelLabelFont((FontUIResource) labelFont);
		} else {
			ClientLogger.LOG.info("Unable to detect the default Font for Labels");
		}
	}
    
    /**
     * @return the name of the application as shown to users
     */
    public static String getApplicationName() {
    	return appName;
    }

    /**
     * Returns the current version information as a string.
     * @version Oct 2017, ELAN 5.0, no longer using the micro version
     * 
     * @return the current version
     */
    public static String getVersionString() {
    	return major + "." + minor + versionSuffix;
        //return major + "." + minor + "." + micro + versionSuffix;
    }
    
    /**
     * @return a multiple line string containing system and 
     * user information
     */
    public static String getSystemAndUserInfo() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(String.format("Java home: \t%s\n", System.getProperty("java.home")));
    	sb.append(String.format("Java version: \t%s\n", System.getProperty("java.version")));
    	sb.append(String.format("Runtime: \t%s\n", System.getProperty("java.runtime.version")));
    	sb.append(String.format("OS name: \t%s\n", System.getProperty("os.name")));
    	sb.append(String.format("OS version: \t%s\n", System.getProperty("os.version")));
    	sb.append(String.format("OS arch.: \t%s\n", System.getProperty("os.arch")));
    	sb.append(String.format("User language: \t%s\n", System.getProperty("user.language")));
    	sb.append(String.format("User home: \t%s\n", System.getProperty("user.home")));
    	sb.append(String.format("User dir: \t%s\n", System.getProperty("user.dir")));
    	sb.append(String.format("Classpath: \t%s\n", System.getProperty("java.class.path")));
    	sb.append(String.format("Library path: \t%s\n", System.getProperty("java.library.path")));
    	List<String> screenInfo = SystemReporting.getScreenInfo();
    	if (screenInfo != null && !screenInfo.isEmpty()) {
    		sb.append("Display info:\n");
    		for (String s : screenInfo) {
    			sb.append(String.format("\t%s\n", s));
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * Prints the UI defaults of the current platform to System.out.
     */
    /*
    private static void printUIDefaults() {
    	System.out.println("Current L&F: " + UIManager.getLookAndFeel().getName());
    	UIDefaults curLFDefaults = UIManager.getLookAndFeelDefaults();
        Map<Object, Object> uid = new HashMap<Object, Object>(curLFDefaults);
        Iterator keyIt = uid.keySet().iterator();
        Object uiKey, val;
        while (keyIt.hasNext()) {
        	uiKey = keyIt.next();
        	val = uid.get(uiKey);
        	System.out.println("Key: " + uiKey + "\n\tValue: " + val);
        }
    }
    */
    /*
    public static void printSystemProperties() {
        Properties props = System.getProperties();
        Iterator<String> prIt = props.stringPropertyNames().iterator();
        System.out.println("- property name = property value - ");
        while (prIt.hasNext()) {
        	String key = prIt.next();
        	String value = props.getProperty(key);
        	System.out.println(key + " = " + value);
        }
    }
    */
}
