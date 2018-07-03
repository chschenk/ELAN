package mpi.eudico.client.annotator.search.viewer;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * @author Albert Russel
 * @version Oct 27, 2004
 */
public class EAFMultipleFileUtilities {
	final static public String extension = ".eaf";

	/**
	 * GUI to change content of searchDirs and searchPaths
	 * @param parent parent component
	 * @param searchDirs List of directories
	 * @param searchPaths List of paths
	 * 
	 * @return true if a domain has been specified, false otherwise
	 */
	static public boolean specifyDomain(
		Component parent,
		final List<String> searchDirs,
		final List<String> searchPaths) {		
		
		// put the current search domain in the file chooser
		File[] currentFiles = null;
		int nDirs = searchDirs.size();
		int nFiles = searchPaths.size();
		if (nDirs + nFiles > 0) {
			currentFiles = new File[nDirs + nFiles];
			for (int i = 0; i < searchDirs.size(); i++) {
				currentFiles[i] = new File(searchDirs.get(i));
			}
			
			for (int i = 0; i < searchPaths.size(); i++) {
				currentFiles[i + nDirs] = new File(searchPaths.get(i));
			}				
		}
		
		FileChooser chooser = new FileChooser(parent);
		chooser.createAndShowMultiFileDialog(ElanLocale.getString("MultipleFileSearch.DomainDialogTitle"), FileChooser.GENERIC,
				ElanLocale.getString("Button.OK"), null, FileExtension.EAF_EXT, false, 
				EAFMultipleFileSearchPanel.LAST_DIR_KEY, FileChooser.FILES_AND_DIRECTORIES, currentFiles);

		
		// let the user choose
		Object[] names = chooser.getSelectedFiles();
		if (names != null) {		

			// extract search dirs and paths from chooser
			searchDirs.clear();
			searchPaths.clear();
			
			for (Object name2 : names) {
				String name = name2.toString();
				File f = new File(name);
				if (f.isFile()) {
					searchPaths.add(f.getPath());
				}
				else if (f.isDirectory()) {
					searchDirs.add(f.getPath());
				}
			}

			// make the dirs and paths persistent
			Preferences.set(EAFMultipleFileSearchPanel.PREFERENCES_DIRS_KEY, searchDirs, null);
			Preferences.set(EAFMultipleFileSearchPanel.PREFERENCES_PATHS_KEY, searchPaths, null);
			
			return true;
		}
		
		return false;
	}

	/**
	 * returns sorted list of existing and readable eaf-files
	 * each file occurs only once in the list
	 * @param dirs directories
	 * @param paths paths
	 * @return List sorted list of java.io.File's
	 */
	static public File[] getUniqueEAFFilesIn(List<String> dirs, List<String> paths) {
		TreeSet<File> sortedUniqueFiles = new TreeSet<File>();

		// get the .eaf files from the directories
		for (int i = 0; i < dirs.size(); i++) {
			File dir = new File(dirs.get(i));
			//System.out.println(dir);
			if (dir.exists() && dir.isDirectory() && dir.canRead()) {
				sortedUniqueFiles.addAll(getAllEafFilesUnder(dir));
			}
		}

		// get the files from the paths
		for (int i = 0; i < paths.size(); i++) {
			String path = paths.get(i);
			if (path.toLowerCase().endsWith(extension)) {
				File file = new File(path);
				if (file.exists() && file.canRead()) {
					sortedUniqueFiles.add(file);
				}
			}
		}

		return sortedUniqueFiles.toArray(new File[0]);
	}

	/**
	 * returns all eaf-files (extension .eaf) in the directory
	 * @param directory
	 * @return List
	 */
	static public List<File> getAllEafFilesUnder(File directory) {
		List<File> eafFiles = new ArrayList<File>();

		File[] filesAndDirs = directory.listFiles();
		
		if (filesAndDirs == null) {
			return eafFiles;
		}
		//System.out.println(filesAndDirs);
		for (File fileOrDir : filesAndDirs) {
			if (fileOrDir.isFile() && fileOrDir.canRead()) {
				if (fileOrDir.getName().toLowerCase().endsWith(extension)) {
					eafFiles.add(fileOrDir);
				}
			}
			else if (fileOrDir.isDirectory() && fileOrDir.canRead()) {
				eafFiles.addAll(getAllEafFilesUnder(fileOrDir));
			}
		}

		return eafFiles;
	}

}
