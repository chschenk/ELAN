package mpi.eudico.client.annotator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * A utility class that performs some Elan specific File operations.
 *
 * @author Han Sloetjes
 */
public class FileUtility {
    /**
     * Converts a path to a file URL string. Takes care of Samba related
     * problems file:///path works for all files except for samba file
     * systems, there we need file://machine/path, i.e. 2 slashes instead of 3
     * Does not support relative paths.
     *
     * @param path the path to convert
     *
     * @return a file url string
     */
    public static String pathToURLString(String path) {
        if (path == null) {
            return null;
        }

        // replace all back slashes by forward slashes
        path = path.replace('\\', '/');
        
        if (path.startsWith("rtsp:")) {
            // could check '//' on position 5-6
            return path;
        }

		if (path.startsWith("file:")) {
			path = path.substring(5);
		}
		
        // remove leading slashes and count them
        int n = 0;

        while (!path.isEmpty() && path.charAt(0) == '/') {
            path = path.substring(1);
            n++;
        }

        // add the file:// or file:/// prefix
        if (n == 2) {
            return "file://" + path;
        } else {
            return "file:///" + path;
        }
    }

    /**
     * Method to compare the file names in two file/url paths without their
     * path and extensions.
     *
     * @param path1 first file path
     * @param path2 seconds file path
     *
     * @return boolean true if the file names without path and extension are
     *         the same
     */
    public static boolean sameNameIgnoreExtension(String path1, String path2) {
        // no name gives false, two nulls are equal but have no name
        if ((path1 == null) || (path2 == null)) {
            return false;
        }

        String name1 = fileNameFromPath(path1);
        int extensionPos = name1.lastIndexOf('.');

        if (extensionPos >= 0) {
            name1 = name1.substring(0, extensionPos);
        }

        String name2 = fileNameFromPath(path2);
        extensionPos = name2.lastIndexOf('.');

        if (extensionPos >= 0) {
            name2 = name2.substring(0, extensionPos);
        }

        return name1.equals(name2);
    }

    /**
     * Returns the file name from a file path.
     *
     * @param path the path
     *
     * @return the filename part of the path
     */
    public static String fileNameFromPath(final String path) {
        if (path == null) {
            return null;
        }

        String name = path.replace('\\', '/');
        int delimiterPos = name.lastIndexOf('/');

        if (delimiterPos >= 0) {
            name = name.substring(delimiterPos + 1);
        }

        return name;
    }

    /**
     * Returns the directory name from a file path.
     *
     * @param path the directory
     *
     * @return the filename part of the path
     */
    public static String directoryFromPath(final String path) {
        if (path == null) {
            return null;
        }

        String name = path.replace('\\', '/');
        int delimiterPos = name.lastIndexOf('/');

        if (delimiterPos >= 0) {
            name = name.substring(0, delimiterPos);
        }

        return name;
    }

    /**
     * Tests whether a file exists.
     *
     * @param path the filepath to test
     *
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(final String path) {
        if (path == null) {
            return false;
        }

        // remove the file: part of the URL, leading slashes are no problem
        int colonPos = path.indexOf(':');
        String fileName = path;

        if (colonPos > -1) {
            fileName = path.substring(colonPos + 1);
        }

        // replace all back slashes by forward slashes
        fileName = fileName.replace('\\', '/');

        File file = new File(fileName);

        if (!file.exists()) {
            return false;
        } else {
            return true;
        }
    }

    //////////////////////////////////////
    // Copied from 'old' FileUtil  class
    //////////////////////////////////////

    /**
     * If file f is a file and has an extension, it is returned. Otherwise,
     * null is returned.
     *
     * @param f a File
     *
     * @return the file extension or null
     */
    public static final String getExtension(final File f) {
        String name = f.getName();
        
        return getExtension(name);
    }

    /**
     * If file has a name with an extension, it is returned. Otherwise,
     * null is returned.
     *
     * @param name a filename
     *
     * @return the file extension or null
     */
    public static final String getExtension(final String name) {
        int li = name.lastIndexOf(".");

        return (li == -1) ? null : name.substring(li + 1);
    }

    /**
     * If file has a name with an extension, it is returned. Otherwise,
     * the default, as supplied, is returned.
     *
     * @param name a filename
     * @param defaultExtension what to return if there is no extension
     *
     * @return the file extension or null
     */
    public static final String getExtension(final String name, final String defaultExtension) {
        int li = name.lastIndexOf(".");

        return (li == -1) ? defaultExtension : name.substring(li + 1);
    }

    /**
     * If file f is a filename with an extension, the name is returned without the extension.
     * Otherwise, the name is returned unmodified.
     *
     * @param name the name
     *
     * @return the file name without extension
     */
    public static final String dropExtension(final String name) {
        int dot = name.lastIndexOf(".");
        
        if (dot == -1) {
        	return name;
        }
        
        String basename = name.substring(0, dot);
        
        // Check if there may be a / following
        
        int slash = name.lastIndexOf('/');
        
        return (slash < dot) ? basename : name;
    }

    /**
     * Copies a file to a destination directory.
     *
     * @param sourceFile the source
     * @param destDir the destination directory
     *
     * @throws Exception DOCUMENT ME!
     */
    public void copyToDir(final File sourceFile, final File destDir)
        throws Exception {
        // this should better not be static!
        byte[] buffer = new byte[4096]; // You can change the size of this if you want.
        destDir.mkdirs(); // creates the directory if it doesn't already exist.

        File destFile = new File(destDir, sourceFile.getName());
        FileInputStream in = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(destFile);
        int len;

        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        out.close();
        in.close();
    }

    /**
     * Copies a source file to a destination file.
     *
     * @param sourceFile the source file
     * @param destFile the destination file
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void copyToFile(final File sourceFile, final File destFile)
        throws Exception {
        // this should better not be static!
        byte[] buffer = new byte[4096]; // You can change the size of this if you want.
        FileInputStream in = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(destFile);
        int len;

        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        out.close();
        in.close();
    }
 
    //########## methods copied from mpi.bcarchive.repaircheck.Util ###################
    //########## modifications HS 09-2011 ############
    
    /**
     * translates URL to absolute path (removes "file:" prefix from URL)
     * @param filename name of file
     * @return returns filename without url prefixes
     */
    public static String urlToAbsPath(String filename) {
    	if (filename == null) {
    		return filename;
    	}
        if (filename.startsWith("file:")) {
            filename = filename.substring("file:".length());
        }
        return filename;
    }
    
    /**
     * getRelativePath gets path of resource relative to a reference file
     * Forward slashes are assumed, loose attempts are made to not calculate a relative path
     * for files that are on different drives (problems differ per platform). 
     * 
     * @param referenceFilename path+filename of the reference file
     * @param resourceFilename path+filename of resource
     * @return relative path or null
     */
    public static String getRelativePath(String referenceFilename, String resourceFilename) {
    	if (referenceFilename == null || resourceFilename == null) {
    		return resourceFilename;
    	}
        if (resourceFilename.startsWith("../") || resourceFilename.startsWith("./")) {
            return resourceFilename;
        }
        String refNoProt = FileUtility.urlToAbsPath(referenceFilename);
        String resourceNoProt = FileUtility.urlToAbsPath(resourceFilename);
        
        int numSlashesRef = 0;
        while (numSlashesRef < refNoProt.length() && refNoProt.charAt(numSlashesRef) == '/') {
        	numSlashesRef++;
        }
        
        int numSlashesRes = 0;
        while (numSlashesRes < resourceNoProt.length() && resourceNoProt.charAt(numSlashesRes) == '/') {
        	numSlashesRes++;
        }
        
        if (numSlashesRef != numSlashesRes) {// probably not on the same drive
        	return null;
        }
        
        String  refFields2[] = refNoProt.split("/");
        String resourceFields2[] = resourceNoProt.split("/");
        // remove the empty elements
        String  refFields[] = new String[refFields2.length - numSlashesRef];
        for (int i = 0; i < refFields.length; i++) {
        	refFields[i] = refFields2[i + numSlashesRef];
        }
        String resourceFields[] = new String[resourceFields2.length - numSlashesRes];
        for (int i = 0; i < resourceFields.length; i++) {
        	resourceFields[i] = resourceFields2[i + numSlashesRes];
        }
        // stop if the first element of both arrays is not the same, assuming they are not on the same drive
        // this is sloppy: if they are the same this does not guarantee that the file are on the same drive 
        if (refFields.length > 0 && resourceFields.length > 0 && !refFields[0].equals(resourceFields[0])) {
        	return null;
        }
        
        int refFieldsLen = refFields.length;
        int resourceFieldsLen = resourceFields.length;
        
        int minFieldsLen = Math.min(refFieldsLen, resourceFieldsLen);
        
        int i = 0;
        while (i < minFieldsLen && refFields[i].equals(resourceFields[i])) {
            i++;
        }
        
        int restIndex = i;
        StringBuilder relPathBuf = new StringBuilder();
        
        if (i == refFieldsLen - 1) {
            relPathBuf.append("./");
        } else if (i == refFieldsLen) {
        	// Exceptional case: both file names are the same! Create "./filename".
            relPathBuf.append("./");
            restIndex--;
        } else {
	        while (i < refFieldsLen - 1) {
	            relPathBuf.append("../");
	            i++;
	        }
        }
        
        i = restIndex;
        while (i < resourceFieldsLen - 1) {
            relPathBuf.append(resourceFields[i] + "/");
            i++;
        }
        
        relPathBuf.append(resourceFields[i]);
        
        return relPathBuf.toString();
    }
    
    /**
     * getAbsolutePath returns the absolute path of a resource of which a relative path is known
     * @param referenceFilename path+filename of reference file
     * @param resourceFilename relative path of resource to reference
     * @return absolute path
     */
    public static String getAbsolutePath(String referenceFilename, String resourceFilename) {
    	if (referenceFilename == null || resourceFilename == null) {
    		return resourceFilename;
    	}
        if (resourceFilename.startsWith("../") || resourceFilename.startsWith("./")) {
            
            String  refFields[] = FileUtility.urlToAbsPath(referenceFilename).split("/");
            String resourceFields[] = FileUtility.urlToAbsPath(resourceFilename).split("/");
            
            int refFieldsLen = refFields.length;
            int resourceFieldsLen = resourceFields.length;
            
            int minFieldsLen = refFieldsLen;
            if (resourceFieldsLen < refFieldsLen) {
                minFieldsLen =  resourceFieldsLen;
            }
            
            int i = 0;
            
            while (i < resourceFieldsLen && resourceFields[i].equalsIgnoreCase("..")) {
                i++;
            }
            
            int refBaseIndex = i;
            StringBuilder absPathBuf = new StringBuilder();
            
            int s = 0;
            while (s < refFieldsLen - refBaseIndex - 1 ) {
                absPathBuf.append(refFields[s] + "/");
                s++;
            }
            
            if (resourceFilename.startsWith("./")) {
                i = 1;
            }
            
            while (i < resourceFieldsLen - 1) {
                absPathBuf.append(resourceFields[i] + "/");
                i++;
            }
            
            absPathBuf.append(resourceFields[i]);
            
            
            return absPathBuf.toString();
            
        } else {
            return resourceFilename;
        }
    }
}
