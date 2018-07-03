package mpi.eudico.client.annotator.util;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.File;
import java.util.Map;

public class SystemReporting {
	public static final String OS_NAME;
	public static final String USER_HOME;
	public static boolean antiAliasedText = false;
	public static boolean useBufferedPainting = false;
	public static boolean isBufferedPaintingPropertySet = false;
	private static boolean isMacOS;
	private static boolean isMacSierraOrHigher = false;
	private static boolean isWindows;
	private static boolean isVista = false;
	private static boolean isWin7 = false;	
	private static boolean isLinux;
	
	static {
		OS_NAME = System.getProperty("os.name");
		USER_HOME = System.getProperty("user.home");
		
		String lowerOS = OS_NAME.toLowerCase();
		
		if (lowerOS.indexOf("win") > -1) {
			isWindows = true;
		} else if (lowerOS.indexOf("mac") > -1) {
			isMacOS = true;
		} else if (lowerOS.indexOf("lin") > -1) {
			isLinux = true;
		}
		
		// check Windows versions and macOS
		String version = System.getProperty("os.version");// 6.0 = Vista, 6.1 = Win 7

		try {
			if (isWindows) {
				if (version.indexOf('.') > -1) {
					String[] verTokens = version.split("\\.");
					int major = Integer.parseInt(verTokens[0]);
					if (verTokens.length > 1) {
						int minor = Integer.parseInt(verTokens[1]);
						if (major > 6) {
							// treat as win 7 for now
							isWin7 = true;
						} else if (major == 6) {
							if (minor > 0) {
								isWin7 = true;
							} else {
								isVista = true;
							}
						}
					}
				} else {
					int major = Integer.parseInt(version);
					if (major > 6) {
						isWin7 = true;
					} else if (major == 6){
						isVista = true;// arbitrary assumption
					}
				}
			} else if (isMacOS) {
				String[] verTokens = version.split("\\.");
				if (verTokens.length >= 2) {
					int major = Integer.parseInt(verTokens[0]);
					if (major == 10) {
						int minor = Integer.parseInt(verTokens[1]);
						if (minor >= 11) {
							isMacSierraOrHigher = true;
						}
					} else if (major > 10) {
						isMacSierraOrHigher = true;
					}
				}
			}
		} catch (NumberFormatException nfe) {
			ClientLogger.LOG.warning("Unable to parse the Windows version.");
		}
		
		String atp = System.getProperty("swing.aatext");
		if ("true".equals(atp)) {
			antiAliasedText = true;
		}
		// for now under J 1.6 only apply the text anti aliasing property
		Map map = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"));

		if (map != null) {
			Object aaHint = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);

			if (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF != aaHint /*|| 
					RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT.equals(aaHint)*/) {
				// treat default as anti-aliasing on??
				antiAliasedText = true;
			}
			//Iterator mapIt = map.keySet().iterator();
		}
		
		String awtRH = System.getProperty("awt.useSystemAAFontSettings");
		if ("on".equals(awtRH)) {
			antiAliasedText = true;
		} else if (map != null) {
			// a desktop setting is overridden by a -D argument
			// should do more specialized testing on the value of awtRH
			if ("off".equals(awtRH) || "false".equals(awtRH) || "default".equals(awtRH)) {
			    antiAliasedText = false;
			}
		}
		
        String bufImg = System.getProperty("useBufferedImage");
        if (bufImg != null) {
        	isBufferedPaintingPropertySet = true;
        	if (bufImg.toLowerCase().equals("true")) {
        		useBufferedPainting = true;
        	}
        }
	}

	public static boolean isMacOS() {
		return isMacOS;
	}
	
	public static boolean isMacOSSierraOrHigher() {
		return isMacSierraOrHigher;
	}
	
	public static boolean isWindows() {
		return isWindows;
	}
	
	public static boolean isWindowsVista() {
		return isVista;
	}

	public static boolean isWindows7OrHigher() {
		return isWin7;
	}
	
	public static boolean isLinux() {
		return isLinux;
	}
	
	public static void printProperty(String prop) {
		System.out.println(prop + " = " + System.getProperty(prop));
	}

	/**
	 *  @return lib/ext directory.
	 */
	public static File getLibExtDir() {
		if (OS_NAME.startsWith("Mac OS X")) {
			return verifyMacUserLibExt();
		} else {
			return new File (System.getProperty("java.home")
								 + File.separator
								 + "lib"
								 + File.separator
								 + "ext");
		}
	}

	/**
	   @return files from lib/ext. May be null.
	*/
	public static File[] getLibExt() {
		File ext = SystemReporting.getLibExtDir();
		if (ext != null && ext.exists()) {
			return SystemReporting.getLibExtDir().listFiles();
		} else {
			return null;
		}
	}
	
    private static File verifyMacUserLibExt() {
		// im jars will be stored in the user home library ext dir
		String userLibJavaExt = USER_HOME + "/Library/Java/Extensions";

		File userLibExt = new File(userLibJavaExt);
		//System.out.println("Home lib ext: " + userLibJavaExt);
		if (!userLibExt.exists()) {
			try {
				boolean success = userLibExt.mkdirs();
				if (!success) {
					ClientLogger.LOG.warning("Unable to create folder: " + userLibExt);
					return null;
				}
			} catch (SecurityException se) {
				ClientLogger.LOG.warning("Unable to create folder: " + userLibExt);
				ClientLogger.LOG.warning("Cause: " + se.getMessage());
				return null;
			}
		}
		return userLibExt;
	}

	/**
	   report files from lib/ext
	*/
	public static void printLibExt() {
		File potext[] = getLibExt();
		int NOFfiles = potext==null?0:potext.length;
		System.out.println("Found " + NOFfiles+ " potential extension(s)");
		for (int i=0; i<NOFfiles; i++) {
			System.out.println("\t" + potext[i]);
		}
	}


	/**
	   testing
	 */
    public static void main(String args[]) throws Exception {
		printProperty("java.home");
		printLibExt();
    }
}
