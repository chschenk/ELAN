package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

@SuppressWarnings("serial")
public class MFPraatImportStep1 extends AbstractMFImportStep1 {
	
	private JComboBox encodingComboBox;
	
	private String[] encodings = null;

	public MFPraatImportStep1(MultiStepPane mp) {
		super(mp);
		
	}
	
	/**
	 * Initialize the ui components
	 */
	@Override
	protected void initComponents(){	
		super.initComponents();
		
		remove(removeFilesBtn);
		
		encodings = new String[]{ElanLocale.getString("Button.Default"), 
	    		FileChooser.UTF_8, FileChooser.UTF_16};
		
		encodingComboBox = new JComboBox();
		for(int i=0; i < encodings.length; i++){
			encodingComboBox.addItem(encodings[i]);
		}
		encodingComboBox.setSelectedItem(FileChooser.UTF_8);
		
		JPanel panel = new JPanel();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = globalInset;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		add(removeFilesBtn, gbc);

		gbc.gridy = 3;	
		gbc.anchor = GridBagConstraints.WEST;
		add(panel, gbc);		
		
		
		panel.setLayout(new GridBagLayout());
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(new JLabel(ElanLocale.getString("FileChooser.Mac.Label.Encoding")), gbc);
		
		gbc.gridx = 1;
		panel.add(encodingComboBox, gbc);		

	}
	
	@Override
	public boolean leaveStepForward(){	
			
		multiPane.putStepProperty("Encoding", encodingComboBox.getSelectedItem().toString());	
		
		return super.leaveStepForward();
	}
	
	@Override
	protected Object[] getMultipleFiles() {
    	Object[] files = getMultipleFiles(ElanLocale.getString("MultiFileImport.Praat.Select"),
		FileExtension.PRAAT_TEXTGRID_EXT, "LastUsedPraatDir", FileChooser.FILES_AND_DIRECTORIES);    	

    	if ((files == null) || (files.length == 0)) {
    		return null;
    	}

    	return getFilesFromFilesAndFolders(files, FileExtension.PRAAT_TEXTGRID_EXT);
	}

}
