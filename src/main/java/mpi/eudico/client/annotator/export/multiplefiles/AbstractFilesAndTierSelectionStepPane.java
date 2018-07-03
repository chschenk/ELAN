package mpi.eudico.client.annotator.export.multiplefiles;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.FrameManager;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel.Modes;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.FilesTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.TranscriptionTierSortAndSelectPanel;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * Abstract Step pane for selecting multiple tiers 
 * from single or multiple files
 *  
 * Uses the advanced tier selection panel
 * 
 * @author aarsom
 * @version Feb, 2012
 */
@SuppressWarnings("serial")
public abstract class AbstractFilesAndTierSelectionStepPane
		extends StepPane implements TableModelListener {	
	
	protected TranscriptionImpl transcription;
	
	protected JRadioButton currentlyOpenedFileRB, selectedFilesFromDiskRB, filesFromDomainRB;
	protected JPanel fileSelectionPanel,tierSelectionPanel;
	protected ButtonGroup buttonGroup;
	protected JButton selectFilesBtn, selectDomainBtn;
	
	protected AbstractTierSortAndSelectPanel tierSelectPanel;
	protected JTextArea textArea;	
	
	private List<String> openedFileList;
	
	protected Insets globalInset = new Insets(2, 4, 2, 4);

	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param transcription, the transcription
	 */
	public AbstractFilesAndTierSelectionStepPane(MultiStepPane mp, TranscriptionImpl transcription){
		super(mp);
		this.transcription = transcription;		
		
		initComponents();
	}
		
	/**
	 * Initialize the ui components
	 */
	@Override
	protected void initComponents(){		
		initFileSelectionPanel();
		initTierSelectionPanel();		
	
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		add(fileSelectionPanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		add(tierSelectionPanel, gbc);
	}		
	
	/**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
	@Override
	public abstract String getStepTitle();

	/**
	 * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#enterStepForward()
	 */
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
		
		//retrieve selected tier names	
		multiPane.putStepProperty("SelectedTiers", tierSelectPanel.getSelectedTiers());		
		multiPane.putStepProperty("OpenedFiles", openedFileList);		
		
		return true;
	}
    
	/**
	 * Initializes the upper part containing file selection
	 */
	protected void initFileSelectionPanel(){			
		//panel
		fileSelectionPanel = new JPanel(new GridBagLayout());
		fileSelectionPanel.setBorder(new TitledBorder( ElanLocale.getString("MultiFileExport.Panel.Title.FileSelection")));
		
		//create all radio buttons
		RadioButtonHandler radioButtonListener = new RadioButtonHandler();
		currentlyOpenedFileRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.CurrentlyOpenedFile"));
		currentlyOpenedFileRB.addActionListener(radioButtonListener);
		selectedFilesFromDiskRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromFileBrowser"));
		selectedFilesFromDiskRB.addActionListener(radioButtonListener);
		filesFromDomainRB = new JRadioButton(ElanLocale.getString("FileAndTierSelectionStepPane.Radio.FilesFromDomain"));;
		filesFromDomainRB.addActionListener(radioButtonListener);
		
		//add radio buttons to button group
		buttonGroup = new ButtonGroup();
		buttonGroup.add(currentlyOpenedFileRB);
		buttonGroup.add(selectedFilesFromDiskRB);
		buttonGroup.add(filesFromDomainRB);
		
		//create all buttons
		ButtonHandler buttonHandler = new ButtonHandler();
		
		selectFilesBtn = new JButton(ElanLocale.getString("Button.Browse"));
		selectFilesBtn.addActionListener(buttonHandler);
		
		selectDomainBtn = new JButton(ElanLocale.getString("FileAndTierSelectionStepPane.Button.Domain"));
		selectDomainBtn.addActionListener(buttonHandler);
		selectDomainBtn.setEnabled(false);
		
		//handle multiple file case vs. single file case
		if( transcription == null ){
			//MULTIPLE_FILES: disable all radio buttons dealing with single file
			currentlyOpenedFileRB.setEnabled(false);
			selectedFilesFromDiskRB.setEnabled(true);
			selectedFilesFromDiskRB.setSelected(true);
			selectFilesBtn.setEnabled(true);
		}else{
			//SINGLE FILES: disable all multiple file functionality
			selectedFilesFromDiskRB.setEnabled(false);
			filesFromDomainRB.setEnabled(false);
			selectFilesBtn.setEnabled(false);
			selectDomainBtn.setEnabled(false);
			
			currentlyOpenedFileRB.setSelected(true);
		}
		
		//add buttons to panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = globalInset;
		fileSelectionPanel.add(currentlyOpenedFileRB, gbc);
		
		//files from disk
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		fileSelectionPanel.add(selectedFilesFromDiskRB, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectFilesBtn, gbc);
		
		//files from domain
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		fileSelectionPanel.add(filesFromDomainRB, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		fileSelectionPanel.add(selectDomainBtn, gbc);
	}
	
	/**
	 * Initializes tier table pane
	 */
	protected void initTierSelectionPanel(){
		//panel		
		tierSelectionPanel = new JPanel(new GridBagLayout());		
		tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("MultiFileExport.Panel.Title.TierSelection")));		
		
		//first menu depends on multiple files or single files
		if( transcription != null ){
			tierSelectPanel = new TranscriptionTierSortAndSelectPanel(transcription, getTierMode());
		}else{
			textArea = new JTextArea(ElanLocale.getString("FileAndTierSelectionStepPane.Message1"));
			textArea.setEditable(false);
			textArea.setBorder(new LineBorder(Color.LIGHT_GRAY));
			
			selectFilesBtn.setEnabled(true);
			selectDomainBtn.setEnabled(false);
		}
	
		//add table
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInset;
		tierSelectionPanel.add(textArea, gbc);		
	}
	
	 /**
     * To be overridden by the actual export classes.
     *  
     * @return one of the tier modes, the default is Modes.ALL_TIERS
     */
    protected Modes getTierMode() {
    	return Modes.ALL_TIERS;
    }
	
	/**
	 * Shows a multiple file chooser dialog, checks if every selected file exists
	 * and stores the selected files in private variable eafFiles
	 * 
	 * @return 
	 */
    protected List<String> showMultiFileChooser(){
		List<String> fileNames = null;	
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowMultiFileDialog(ElanLocale.getString("ExportDialog.Multi"), FileChooser.GENERIC,
        		FileExtension.EAF_EXT, "LastUsedEAFDir");
        
        Object[] objects = chooser.getSelectedFiles();     

		if (objects != null) {	  
			if (objects.length > 0) {           
				fileNames = new ArrayList<String>();
                for (Object object : objects) {
                    if (fileNames.contains(object) == false) {
                        fileNames.add("" + object);
                    }
                }
            }
		}		
		return fileNames;
	}

	/**
	 * Updates the button states according to some constraints 
	 * (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){		
			if(tierSelectPanel != null && tierSelectPanel.getSelectedTiers().size() > 0){
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
			} else{
				multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
			}
			
			multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, false);
		
	}
	
	/**
	 * Initialize the advanced tier select panel
	 * 
	 * @param files, the files to be used for export
	 */
	protected void initializeTierSelectPanel(ArrayList<File> files){
		
		if(tierSelectPanel != null){
			tierSelectionPanel.remove(tierSelectPanel);
		}else {
			tierSelectionPanel.remove(textArea);
		}
		
		tierSelectPanel = new FilesTierSortAndSelectPanel(files, getTierMode()){
			@Override
			protected void initTables(){
				super.initTables();
				model.addTableModelListener(this);
				typeModel.addTableModelListener(this);
				partModel.addTableModelListener(this);
				annotModel.addTableModelListener(this);
				langModel.addTableModelListener(this);
			}
		};
		
		//add table
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInset;
		tierSelectionPanel.add(tierSelectPanel, gbc);	
		
		updateButtonStates();
		
		revalidate();
		repaint();
	}
		
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			updateButtonStates();
		}
	}

	protected ArrayList<File> getMultipleFiles(List<String> fileNames){
		if(fileNames == null){
			return null;
		}
		
		
		if (fileNames.size() > 0) {
			ArrayList<File> files = new ArrayList<File>();
	        File f;
	        for (int i = 0; i < fileNames.size(); i++) {
	        	f = new File(fileNames.get(i));
	        	if (f.isFile() && f.canRead()) {
	        		files.add(f);
	        	} else if (f.isDirectory() && f.canRead()) {
	        		addFiles(f, files);
	        	}
	        }
	        
	        if(files.size() > 0){
	        	return files;
	        }
		}		
		return null;
	}
	
	
    /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the  or folder
     * @param files the list to add the files to
     */
    protected void addFiles(File dir, ArrayList<File> files) {
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
                    	
                    	if(!files.contains(allSubs[i])) {
							files.add(allSubs[i]);
						}
                    }
                }
            }
        }
    }
    
    /**
     * Scans the folders for eaf files and adds them to files list,
     * recursively.
     *
     * @param dir the  or folder
     * @param files the list to add the files to
     */
    protected void addFileNames(File dir, List<String> files) {
        if ((dir == null) && (files == null)) {
            return;
        }

        File[] allSubs = dir.listFiles();

        for (int i = 0; i < allSubs.length; i++) {
            if (allSubs[i].isDirectory() && allSubs[i].canRead()) {
            	addFileNames(allSubs[i], files);
            } else {
                if (allSubs[i].canRead()) {
                    if (allSubs[i].getName().toLowerCase()
                                      .endsWith(FileExtension.EAF_EXT[0])) {
                        // test if the file is already there??
                    	
                    	if(!files.contains(allSubs[i].getAbsolutePath())) {
							files.add(allSubs[i].getAbsolutePath());
						}
                    }
                }
            }
        }
    }

	private class ButtonHandler implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			updateButtonStates();
			if( e != null ){
				JButton button = (JButton) e.getSource();
				
				if( button == selectFilesBtn ){
					List<String> filenames = showMultiFileChooser();
					if( filenames != null && !filenames.isEmpty() ){
						initializeTierSelectPanel(getMultipleFiles(filenames));
						openedFileList = filenames;
						
						if(filenames.size() > 0){
							checkForOpenedFiles(filenames);
						}
					}
				}else if( button == selectDomainBtn ){
					//create domain dialog
					MFDomainDialog domainDialog = new MFDomainDialog(ELANCommandFactory.getRootFrame(null), ElanLocale.getString("ExportDialog.Multi"), true);
					domainDialog.setVisible(true);
					
					//when domain is selected, get the search paths
					List<String> searchPaths = domainDialog.getSearchPaths();
					List<String> searchDirs = domainDialog.getSearchDirs();
					
					
					List<String> filenames = new ArrayList<String>();
					ArrayList<File> files = null;
					
					//check if domain contains files
					if( !searchPaths.isEmpty() ){
						//load the files in the selected domain		
						if(files == null){
							files = getMultipleFiles(searchPaths);
						}else {
							files.addAll(getMultipleFiles(searchPaths));
						}						
						filenames.addAll(searchPaths);
					}
				
					if( !searchDirs.isEmpty() ){
						//load the files in the selected domain		
						if(files == null){
							files = getMultipleFiles(searchDirs);
						}else {
							files.addAll(getMultipleFiles(searchDirs));
						}
						
					    File f;
					    for (int i = 0; i < searchDirs.size(); i++) {
					      	f = new File(searchDirs.get(i));
					       	if (f.isFile() && f.canRead()) {
					       		if(!filenames.contains(searchDirs.get(i))){
					       			filenames.add(searchDirs.get(i));	
					       		}
					       	} else if (f.isDirectory() && f.canRead()) {
					       		addFileNames(f, filenames);
					       	}
					    }	
					}
					
					if(files != null){
						initializeTierSelectPanel(files);						
					}
					
					if(filenames.size() > 0){
						checkForOpenedFiles(filenames);
						openedFileList = filenames;
					}
				}
			}				
		}		
	}
	
	protected void checkForOpenedFiles(List<String> fileNames){
		ElanFrame2 frame;
		FrameManager manager = FrameManager.getInstance();
		List<String> openedFileNames = new ArrayList<String>();
		for(String fileName : fileNames){
			frame = manager.getFrameFor(fileName, false);
			if(frame != null){
				openedFileNames.add(fileName);
			}
		}
		
		if(openedFileNames.size() > 0){
			String message = null;
			String lineSep = System.getProperty("line.separator");
			for(int i=0; i< openedFileNames.size(); i++){
				if(i == 0){
					message = openedFileNames.get(i) + lineSep;
				}else{
					message += openedFileNames.get(i) +lineSep;
				}
			}
			
			message += lineSep + ElanLocale.getString("MultiFileExport.FilesAndTierSelectionPane.Message.Part1") +lineSep
					+ lineSep +  ElanLocale.getString("MultiFileExport.FilesAndTierSelectionPane.Message.Part2") +lineSep
					+ lineSep + ElanLocale.getString("FileAndTierSelectionStepPane.Message3.Part3");
			
			JOptionPane.showMessageDialog(AbstractFilesAndTierSelectionStepPane.this, message, ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE, null);
					
		}
	}
	
	private class RadioButtonHandler implements ActionListener{
		private JRadioButton previouslySelectedRadioButton;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton rb = (JRadioButton) e.getSource();
			
			if( rb == currentlyOpenedFileRB ){
				if( previouslySelectedRadioButton != rb ){
					openedFileList = null;
					previouslySelectedRadioButton = rb;
				}
				
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(false);
			}else if( rb == selectedFilesFromDiskRB ){
				if( previouslySelectedRadioButton != rb ){
					openedFileList = null;
					previouslySelectedRadioButton = rb;
				}
				
				selectFilesBtn.setEnabled(true);
				selectDomainBtn.setEnabled(false);
			}else if( rb == filesFromDomainRB ){
				if( previouslySelectedRadioButton != rb ){
					openedFileList = null;
					previouslySelectedRadioButton = rb;
				}				
				selectFilesBtn.setEnabled(false);
				selectDomainBtn.setEnabled(true);
			}			
			updateButtonStates();
		}
	}
}
