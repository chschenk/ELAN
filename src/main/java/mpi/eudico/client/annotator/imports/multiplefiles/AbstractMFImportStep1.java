package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * Step 1: Abstract Step pane for selecting multiple
 * files that are to be imported 
 * 
 * @author aarsom
 * @version May, 2012
 */
public abstract class AbstractMFImportStep1 extends StepPane implements ActionListener{	
	
	protected JLabel selectFilesLabel;
	protected JButton selectFilesBtn;
	protected JButton removeFilesBtn;
	
	protected JList fileList;
	protected DefaultListModel model;
	
	private List<Object> selectedFiles;
	
	protected Insets globalInset = new Insets(2, 4, 2, 4);
    
    
    protected FileChooser chooser;

	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * 
	 */
	public AbstractMFImportStep1(MultiStepPane mp){
		super(mp);		
		initComponents();
	}
		
	/**
	 * Initialize the ui components
	 */
	@Override
	protected void initComponents(){	
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();		
		
		selectFilesLabel = new JLabel(ElanLocale.getString("OverlapsDialog.Radio.FilesFromFileBrowser"));
		
		selectFilesBtn = new JButton(ElanLocale.getString("Button.Browse"));
		selectFilesBtn.addActionListener(this);		
		
		removeFilesBtn = new JButton(ElanLocale.getString("FileChooser.Button.Remove"));
		removeFilesBtn.setEnabled(false);
		removeFilesBtn.addActionListener(this);		
		
		model = new DefaultListModel();
        fileList = new JList(model);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);         
        
        JPanel selectedFilesPanel = new JPanel(new GridBagLayout());		
		selectedFilesPanel.setBorder(new TitledBorder(ElanLocale.getString("MultiFileImport.Step1.SelectedFiles")));	   
		
		 JScrollPane jsp = new JScrollPane(fileList);
         jsp.setPreferredSize(new Dimension(jsp.getPreferredSize().getSize().width -
        		 30, jsp.getPreferredSize().getSize().height));
		
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		selectedFilesPanel.add(jsp, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		add(selectFilesLabel, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		add(selectFilesBtn, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		add(selectedFilesPanel, gbc);
		
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		add(removeFilesBtn, gbc);
	}
	
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("MultiFileImport.Step1.Title");
	}
	
	@Override
	public void enterStepForward(){
		updateButtonStates();
	}
	
	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	@Override
	public boolean leaveStepForward(){			
		multiPane.putStepProperty("FilesToBeImported", getFilesToBeImported());	
		
		return true;
	}
	
	/**
	 * Get the files that are selected for import
	 * 
	 * @return array of files that are to be imported
	 */
	private Object[] getFilesToBeImported(){
		Object[] files = new Object[model.getSize()];
		
		List<String> selectedFileNames = new ArrayList<String>();
		for(int i=0; i<model.getSize(); i++){
			selectedFileNames.add((String) model.get(i));
		}
		
		for(int i=0; i < selectedFiles.size(); i++){
			if(selectedFileNames.contains(((File)selectedFiles.get(i)).getAbsolutePath())){
				files[i] = selectedFiles.get(i);
			}
		}		
		return files;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e != null ){
			JButton button = (JButton) e.getSource();
			
			if( button == selectFilesBtn ){
				Object[] array = getMultipleFiles();
				if(array != null){
				List<Object> files = Arrays.asList(array);
				if( files != null && files.size() != 0){
					selectedFiles =  files;
					initializeFileList();
				}
				}
			} 
			else if( button == removeFilesBtn ){
				removeFiles();
			}
		}
		
		updateButtonStates();
	}
	
	/**
	 * gets the files selected from the file chooser
	 * and does some processing if needed.
	 * 
	 * For eg: the selected files can have a directory selected
	 * This method should extract the required files from the selected directory
	 * 
	 * @return array of selected files for import
	 */
    protected abstract Object[] getMultipleFiles();
    
    /**
     * Shows a multiple file chooser to select multiple files and or
     * folders.
     *
     * @param title, the title for the dialog
     * @param mainFilterExt, the main file filter extension array for the file dialog
     * @param prefStringToLoadtheCurrentPath, the preference string to store the
     * 										  last used path
     *
     * @return a list of File objects (files and folders)
     */
    protected Object[] getMultipleFiles(String title, String[] mainFilterExt, String prefStringToLoadtheCurrentPath) {  
        return getMultipleFiles(title,mainFilterExt, prefStringToLoadtheCurrentPath, FileChooser.FILES_ONLY);
    }
    
    /**
     * Shows a multiple file chooser to select multiple files and or
     * folders.
     *
     * @param title, the title for the dialog
     * @param mainFilterExt, the main file filter extension array for the file dialog
     * @param prefStringToLoadtheCurrentPath, the preference string to store the
     * 										  last used path
     * @param fileType, FileChooser.FILES_ONLY or FileChooser.FILES_AND_DIRECTORIES or
     * 					FileChooser.DIRECTORIES_ONLY.   
     *                  The default value is FileChooser.FILES_ONLY.
     *
     * @return a list of File objects (files and folders)
     */
    protected Object[] getMultipleFiles(String title, String[] mainFilterExt, String prefStringToLoadtheCurrentPath, int fileType) {       
        chooser = new FileChooser(this);
        chooser.createAndShowMultiFileDialog(title, FileChooser.GENERIC, null, null, mainFilterExt, false, prefStringToLoadtheCurrentPath, fileType, null);           
        return chooser.getSelectedFiles();
    }
    
    /**
     * Creates and returns an array of non-directory File elements, where each file has one of the requested
     * extensions and where (sub-)folders have been scanned for files of the specified type(s).
     *   
     * @param files an array of input File objects, can contain files and/or directories, not null
     * @param fileExtensions a list of acceptable file extensions, not null
     * @return an array of (non-directory) files of the requested type 
     */
    protected Object[] getFilesFromFilesAndFolders(Object[] files, String[] fileExtensions) {
    	ArrayList<String> extensions = new ArrayList<String>();
    	for(String ext: fileExtensions){
    		if(!extensions.contains(ext.toLowerCase())){
    			extensions.add(ext.toLowerCase());
    		}
    	}

    	ArrayList<File> validFiles = new ArrayList<File>();
    	File file;
    	for(Object f: files){
    		file = (File)f;
    		if(file.isFile()){
    			if(file.canRead() && !validFiles.contains(file))
    				validFiles.add(file);
    		} else if(file.isDirectory()){
    			chooser.addFiles(file, validFiles, extensions);
    		}
    	}
    	
		return validFiles.toArray();
    }

	/**
	 * Updates the button states according to some constraints 
	 * (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){		
			if(model.size() > 0){
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
				removeFilesBtn.setEnabled(true);
			} else{
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
				removeFilesBtn.setEnabled(false);
			}
			
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
		
	}
	
	/**
     * Removes the selected files from the selected files list.
     */
    public void removeFiles() {
    	int[] selIndices = fileList.getSelectedIndices();
    	if (selIndices.length == 0) {
           	return;
        }
    	for (int i = selIndices.length - 1; i >= 0; i--) {    		
           	model.removeElementAt(selIndices[i]);
    	}
    	
    	if (model.getSize() > 0) {
           	fileList.setSelectedIndex(model.getSize()-1);
            fileList.ensureIndexIsVisible(model.getSize()-1);
    	}
    }
	
	/**
	 * Initialize the selected file list
	 */
	protected void initializeFileList(){		
		while(model.size() != 0){
			model.removeElementAt(0);
		}
		
		if(selectedFiles != null){
			for(Object f : selectedFiles){
				if (!model.contains(f)) {
		    		int curIndex = fileList.getSelectedIndex();
					model.add(curIndex + 1, ((File)f).getAbsolutePath());
					fileList.setSelectedIndex(curIndex + 1);
		 		}   
			}
		}	
	}  
}

