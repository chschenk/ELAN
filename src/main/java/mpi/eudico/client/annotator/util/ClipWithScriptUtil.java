package mpi.eudico.client.annotator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import mpi.eudico.client.annotator.Constants;

/**
 * A utility class that handles discovery and reading of the text file ("script") containing 
 * the command with the "place holders" to be filled in by ELAN.
 *  
 * @author Han Sloetjes
 */
public class ClipWithScriptUtil {

	/**
	 * Constructor.
	 */
	public ClipWithScriptUtil() {
	}

	 /**
	  * Checks whether the clipping script file can be found in any known place.
	  * @version 04-2012 changed the order of file discovery; first in the 
	  * ELAN data dir in the user's home, then in user.dir (ELAN's install dir)
	  * 
	  * @param the name of the script file
	  * @return the File object of the script
	  */
	 public File getScriptFile(String scriptFileName) {
			// check if there is script file in the user dir or in the ELAN data dir
			// do this in the constructor or every time an attempt is made to call the script 
//			File f = new File(System.getProperty("user.dir") + File.separator + scriptFileName);
		 	File f = new File(Constants.ELAN_DATA_DIR + File.separator + scriptFileName);
			if (f.exists() && f.isFile() && f.canRead()) {
				return f;
			} else {
				// alternative location
				f = new File(System.getProperty("user.dir") + File.separator + scriptFileName);
//				f = new File(Constants.ELAN_DATA_DIR + File.separator + scriptFileName);
				
				if (f.exists() && f.isFile() && f.canRead()) {
					return f;
				}
			}
			
		 return null;
	 }
	
	 /**
	  * Tries to discover and parse the actual script line/command line.
	  * Ignores all lines starting with a # sign
	  *
	  * @param the file object containing the script line
	  * @return a String array of size 2 (at index 0 is the executable, index 1 the command part) or null
	  */
	 public String[] parseScriptLine(File scriptFile) {
		 if (scriptFile != null) {
			 String executable = null;
			 String paramLine = null;
			 FileReader fileRead = null;
			 BufferedReader bufRead = null;
			 // read the file, extract the command
			try {
				fileRead = new FileReader(scriptFile);
				bufRead = new BufferedReader(fileRead);
				String line = null;
				
				while ((line = bufRead.readLine()) != null) {
					if (line.length() == 0) {
						continue;
					}
					if (line.startsWith("#")) {
						continue;
					}
					// first real line is parsed
					 line = line.trim();
					 if (line.startsWith("\"")) {// check if the executable is within double quotes
						 int index = line.indexOf("\"", 1);
						 if (index > -1) {
							 executable = line.substring(1, index);
							 if (index < line.length() - 2) {
								 paramLine = line.substring(index + 2);
							 }
						 } else {// no closing double quote
							 executable = null;
						 }
					 } else {
						 int index = line.indexOf(' ');
						 if (index > 0) {
							 executable = line.substring(0, index);
							 if (index < line.length() - 1) {
								 paramLine = line.substring(index + 1);
							 }
						 }
					 }
				}
				if (executable != null && paramLine != null) {
					return new String[]{executable, paramLine};
				}
				
			} catch (FileNotFoundException fnfe) {
				ClientLogger.LOG.warning("The script file can not be found");
				executable = null;
				paramLine = null;
			} catch (IOException ioe) {
				ClientLogger.LOG.warning("Error while reading the script file " + ioe.getMessage());
				executable = null;
				paramLine = null;
			} finally {
				try {
					if (fileRead != null) {
						fileRead.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (bufRead != null) {
						bufRead.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		 }
		 
		 return null;
	 }
}
