package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.util.FileExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;


/**
 * Base class for processing multiple eaf files. This part deals with
 * selecting multiple files and directories and creating a list of unique,
 * existing files.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractProcessMultiMA extends FrameMenuAction {
    private List<String> searchDirs;
    private List<String> searchPaths;
    
    /**
     * Creates a new AbstractProcessMultiMA instance
     *
     * @param name name of the action
     * @param frame the containing frame
     */
    public AbstractProcessMultiMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Shows a multiple file chooser to select multiple eaf files and or
     * folders.
     *
     * @param parent the parent frame
     * @param title the title for the dialog
     *
     * @return a list of File objects (files and folders)
     */
    protected List<File> getMultipleFiles(JFrame parent, String title) {
        List<File> files = new ArrayList<File>();
        if (searchDirs == null){
        	searchDirs = new ArrayList<String>();
        }
        if (searchPaths == null) {
        	searchPaths = new ArrayList<String>();
        }
    	// prompt with a list of domains
    	// if one is picked load that domain, otherwise continue with 
    	// "new domain prompt"
    	MFDomainDialog mfDialog = new MFDomainDialog(parent, 
    			ElanLocale.getString("ExportDialog.Multi"), true);
    	mfDialog.setSearchDirs(searchDirs);
    	mfDialog.setSearchPaths(searchPaths);
    	mfDialog.setVisible(true);
    	searchDirs = (ArrayList<String>) mfDialog.getSearchDirs();
    	searchPaths = (ArrayList<String>) mfDialog.getSearchPaths();
    	
        /*
        MultiFileChooser chooser = new MultiFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(ElanFileFilter.createFileFilter(
                ElanFileFilter.EAF_TYPE));
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        String dirPath = (String) Preferences.get("LastUsedEAFDir", null);

        if (dirPath == null) {
            // user.dir is probably a better choice than home.dir?
            dirPath = System.getProperty("user.dir");
        }

        chooser.setCurrentDirectory(new File(dirPath));

        // let the user choose
        int option = chooser.showDialog(parent, null);

        if (option == JFileChooser.APPROVE_OPTION) {
            String lastDirPath = chooser.getCurrentDirectory().getAbsolutePath();
            Preferences.set("LastUsedEAFDir", lastDirPath, null);

            Object[] names = chooser.getFiles();

            for (int i = 0; i < names.length; i++) {
                String name = "" + names[i];
                File f = new File(name);

                if (f.isFile() && f.canRead()) {
                    files.add(f);
                } else if (f.isDirectory() && f.canRead()) {
                    addFiles(f, files);
                }
            }
        }
        */
        if (searchPaths.size() > 0) {
        	String name;
        	File f;
        	for (int i = 0; i < searchPaths.size(); i++) {
        		name = searchPaths.get(i);
        		f = new File(name);
        		if (f.isFile() && f.canRead()) {
        			files.add(f);
        		} else if (f.isDirectory() && f.canRead()) {
        			addFiles(f, files);// should not occur
        		}
        	}
        }
        if (searchDirs.size() > 0) {
        	String name;
        	File f;
        	for (int i = 0; i < searchDirs.size(); i++) {
        		name = searchDirs.get(i);
        		f = new File(name);
        		if (f.isFile() && f.canRead()) {
        			files.add(f);//should not occur
        		} else if (f.isDirectory() && f.canRead()) {
        			addFiles(f, files);
        		}
        	}
        }
        
        return files;
    }

    /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the  or folder
     * @param files the list to add the files to
     */
    protected void addFiles(File dir, List<File> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (int i = 0; i < allSubs.length; i++) {
            if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
                addFiles(allSubs[i], files);
            } else {
                if (allSubs[i].canRead()) {
                    if (allSubs[i].getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                        files.add(allSubs[i]);
                    }
                }
            }
        }
    }
}
