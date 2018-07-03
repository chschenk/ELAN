package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.util.FileExtension;

public class DomainPane extends JPanel implements ActionListener {
	private static final long serialVersionUID = 7647117764716393078L;
    private List<String> searchDirs;
    private List<String> searchPaths;
    private File[] searchFiles;
    
    private JButton domainButton;
    private JButton startStopButton;
    private MFEFrame mfeParent;
//    private boolean reloadTierList=false;
    
	public DomainPane(MFEFrame mfeParent) {
		this.mfeParent=mfeParent;
		initComponents();
	}

	private void initComponents() {
		BorderLayout lm = new BorderLayout();
		setLayout(lm);
		
		domainButton = new JButton();
		domainButton.setActionCommand("OpenDomainDialog");
		domainButton.addActionListener(this);
		add(domainButton, BorderLayout.LINE_START);
		
		startStopButton = new JButton(ElanLocale.getString("MFE.Apply"));
		startStopButton.setActionCommand("ApplyMultiFiles");
		startStopButton.addActionListener(this);
		add(startStopButton, BorderLayout.LINE_END);
		
		startStopButton.setEnabled(false);
	}
	
	public void updateLocale() {
		setBorder(new TitledBorder(ElanLocale.getString(
			"MFE.Domain")));
		domainButton.setText(ElanLocale.getString(
			"MFE.DomainDefKey"));
	}
    /**
     * Shows a file chooser to specify the files and directories to process.
     */
    private boolean updateFileList() {
        List<File> files = getMultipleFiles(null,
                ElanLocale.getString("MultipleFileSearch.DomainDialogTitle"));

        if ((files != null) && (files.size() > 0)) {
//            reloadTierList = true; // the domain changed
            searchFiles=files.toArray(new File[files.size()]);
            //startStopButton.setEnabled(true); //?? check textfields??
            return true;
        } else {
        	return false;
        }
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
    			title, true);
    	mfDialog.setSearchDirs(searchDirs);
    	mfDialog.setSearchPaths(searchPaths);
    	mfDialog.setVisible(true);
    	searchDirs = mfDialog.getSearchDirs();
    	searchPaths = mfDialog.getSearchPaths();

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
     * @param dir the directory or folder
     * @param files the list to add the files to
     */
    protected void addFiles(File dir, List<File> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (File allSub : allSubs) {
            if (allSub.isDirectory() && allSub.canRead()) {
                addFiles(allSub, files);
            } else {
                if (allSub.canRead()) {
                    if (allSub.getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                        files.add(allSub);
                    }
                }
            }
        }
    }

    public File[] getSearchFiles() {
    	return searchFiles;
    }
    
    public void clearSearchFiles() {
    	searchFiles = new File[0];
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton)
		{
			if(e.getActionCommand().equals("OpenDomainDialog")) {
				if(updateFileList()) {
					mfeParent.loadFiles();
				}
			}
			if(e.getActionCommand().equals("ApplyMultiFiles")) {
				Object[] options = {ElanLocale.getString("Button.No"),
						ElanLocale.getString("Button.Yes")};
				int n = JOptionPane.showOptionDialog(this,
    					ElanLocale.getString("MFE.SaveMessage"),
    				    ElanLocale.getString("MFE.SaveMessageTitle"),
    				    JOptionPane.YES_NO_OPTION,
    				    JOptionPane.WARNING_MESSAGE,
    				    null,options,options[0]);
				if(n==1) {// not JOptionPane.YES because of the order of the options above
					mfeParent.writeChanges();
				}
			}
		}		
	}

	public void enableUI(boolean b) {
		domainButton.setEnabled(b);
		startStopButton.setEnabled(b && searchFiles.length > 0);
		if(b) {
			startStopButton.setText(ElanLocale.getString("MFE.Apply"));
		} else {
			startStopButton.setText(ElanLocale.getString("MFE.Busy"));
		}
	}
}
