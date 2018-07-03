package mpi.eudico.client.annotator.export;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.AbstractTierSortAndSelectPanel;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.Transcription2WordList;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Tier selection dialog for export of a word list, either from a single file
 * or from multiple files.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ExportWordListDialog extends AbstractExtTierExportDialog 
    implements ChangeListener {
   
    private JRadioButton customDelimRB;
    private JLabel tokenDelimLabel;
    private JRadioButton defaultDelimRB;
    private JTextField customDelimField;
    private ButtonGroup delimButtonGroup;
    private JCheckBox countTokensCB;
    
    public static int WORDS = 0;
    public static int ANNOTATIONS = 1;
    
    private int mode = WORDS;

    /**
     * Constructor for single file.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param transcription the (single) transcription
     */
    public ExportWordListDialog(Frame parent, boolean modal,
        TranscriptionImpl transcription) {
        super(parent, modal, transcription, null);
        makeLayout();
        extractTiers();
        postInit();
    }

    /**
     * Constructor for multiple files.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param files a list of eaf files
     */
    public ExportWordListDialog(Frame parent, boolean modal, List<File> files) {
    	    this(parent, modal, files, WORDS);
    }

    /**
     * Constructor for multiple files.
     *
     * @param parent the parent frame
     * @param modal the modal flag
     * @param files a list of eaf files
     * @param mode the mode, WORDS or ANNOTATION export
     */
    public ExportWordListDialog(Frame parent, boolean modal, List<File> files, int mode) {
        super(parent, modal, files);
        this.files = files;
        if (mode == ANNOTATIONS || mode == WORDS) {
        		this.mode = mode;
        }
        makeLayout();
        extractTiersFromFiles();
        postInit();
    }

	/**
     * Extract candidate tiers for export.
     */
    protected void extractTiers() {
    	
    	List<String> stringsPref = Preferences.getListOfString("ExportWordListDialog.TierOrder", transcription);
    	if (stringsPref != null) {
    		setTierOrder(stringsPref);        	
        } else {
        	super.extractTiers(false);
        }
    	
        stringsPref = Preferences.getListOfString("ExportWordListDialog.selectedTiers", transcription);
        if (stringsPref != null) {
        	//loadTierPreferences(useTyp);
        	setSelectedTiers(stringsPref);
        }
        
        String stringPref = Preferences.getString("ExportWordListDialog.SelectTiersMode", transcription);
        if (stringPref != null) {
        	//List list = (List) Preferences.get("ExportWordListDialog.HiddenTiers", transcription);
        	//setSelectedMode(string, list);   
        	setSelectionMode(stringPref);
        	
        	if (!AbstractTierSortAndSelectPanel.BY_TIER.equals(stringPref) ) {
            	// call this after! the mode has been set
            	List<String> selItems  = Preferences.getListOfString("ExportWordListDialog.LastSelectedItems", transcription);
            	
            	if (selItems != null) {
            		setSelectedItems(selItems);
            	}
        	}
        } 
        
        // use previous preference settings, if available
        if (stringPref == null) {        	
    		Boolean boolPref = Preferences.getBool("ExportWordListDialog.tierRB", null);
        	if (boolPref != null) {
        		if (boolPref) {
        			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TIER);
        		} else {
        			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
        		}
        	} else {
        		boolPref = Preferences.getBool("ExportWordListDialog.typeRB", null);
            	if (boolPref != null) {
            		if (boolPref) {
            			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
            		} else {
            			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TIER);
            		}
            	}
        	}        	
        }
    }
    
    /**
     * Restore some multiple file preferences.
     */
	@Override
	protected void extractTiersFromFiles() {
		super.extractTiersFromFiles();
        // in multiple file mode transcription is null and global setting will be used
        String stringPref = Preferences.getString("ExportWordListDialog.SelectTiersMode", null);
        if (stringPref != null) {
        	//List list = (List) Preferences.get("ExportWordListDialog.HiddenTiers", transcription);
        	//setSelectedMode((String)useTyp, list);   
        	setSelectionMode(stringPref);
        }
        
        // use previous preference settings, if available
        if (stringPref == null){        	
    		Boolean boolPref = Preferences.getBool("ExportWordListDialog.tierRB", null);
        	if (boolPref != null) {
        		if (boolPref) {
        			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TIER);
        		} else {
        			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
        		}
        	}else{
        		boolPref = Preferences.getBool("ExportWordListDialog.typeRB", null);
            	if (boolPref != null) {
            		if (boolPref) {
            			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TYPE);
            		} else {
            			setSelectionMode(AbstractTierSortAndSelectPanel.BY_TIER);
            		}
            	}
        	}        	
        }
	}

	/**
     * Calls the super implementation and sets some properties of the tier
     * table.
     */
    @Override
	protected void makeLayout() {
        super.makeLayout();       
        
        countTokensCB = new JCheckBox();
        
        // options
        if (mode == WORDS) {
            delimButtonGroup = new ButtonGroup();
            tokenDelimLabel = new JLabel();
            defaultDelimRB = new JRadioButton();
            customDelimRB = new JRadioButton();
            customDelimField = new JTextField();
            
            GridBagConstraints gridBagConstraints;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = insets;
            optionsPanel.add(tokenDelimLabel, gridBagConstraints);

            defaultDelimRB.setSelected(true);
            defaultDelimRB.addChangeListener(this);
            delimButtonGroup.add(defaultDelimRB);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(defaultDelimRB, gridBagConstraints);

            customDelimRB.addChangeListener(this);
            delimButtonGroup.add(customDelimRB);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(customDelimRB, gridBagConstraints);

            customDelimField.setEnabled(false);
            customDelimField.setColumns(6);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = insets;
            optionsPanel.add(customDelimField, gridBagConstraints);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(12, 6, 4, 6);
            optionsPanel.add(countTokensCB, gridBagConstraints);

        } else {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = insets;
            optionsPanel.add(countTokensCB, gridBagConstraints);
            
            //getContentPane().remove(optionsPanel);
        }
        
        setPreferredSetting();        
        updateLocale();
    }

    /**
     * Applies localized text values ui elements.
     */
    @Override
	protected void updateLocale() {
        super.updateLocale();
        titleLabel.setText(ElanLocale.getString("ExportDialog.WordList.Title"));
        
        if (mode == WORDS) {
        	    titleLabel.setText(ElanLocale.getString("ExportDialog.WordList.Title"));
	        	tokenDelimLabel.setText(ElanLocale.getString(
	            "TokenizeDialog.Label.TokenDelimiter"));
	        defaultDelimRB.setText(ElanLocale.getString(
	            "Button.Default") + "( . , ! ? \" \' )");
	        customDelimRB.setText(ElanLocale.getString(
	            "TokenizeDialog.RadioButton.Custom"));
        } else if (mode == ANNOTATIONS) {
            titleLabel.setText(ElanLocale.getString("ExportDialog.AnnotationList.Title"));
        }
        countTokensCB.setText(ElanLocale.getString("ExportDialog.WordList.CountOccur"));
    }

    /**
     * @see mpi.eudico.client.annotator.export.AbstractBasicExportDialog#startExport()
     */
    @Override
	protected boolean startExport() throws IOException {
        List<String> selectedTiers = getSelectedTiers();
        savePreferences();

        if (selectedTiers.size() == 0) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportTradTranscript.Message.NoTiers"),
                ElanLocale.getString("Message.Warning"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

        // prompt for file name and location
        File exportFile = null;
        if (mode == WORDS) {
	        	exportFile = promptForFile(ElanLocale.getString(
	            "ExportDialog.WordList.Title"), null, FileExtension.TEXT_EXT, true);      	
        } else if (mode == ANNOTATIONS) {
            exportFile = promptForFile(ElanLocale.getString(
                "ExportDialog.AnnotationList.Title"), null, FileExtension.TEXT_EXT, true);
        }

        if (exportFile == null) {
            return false;
        }
        String delimiters = null;
        if (mode == WORDS && customDelimRB.isSelected()) {
            delimiters = customDelimField.getText();
        }
        boolean countOccurrences = countTokensCB.isSelected();
        
        Transcription2WordList twl = new Transcription2WordList();

        try {
            if (transcription != null) {
                twl.exportWords(transcription, selectedTiers, exportFile,
                    encoding, delimiters, countOccurrences);
            } else {
            	   if (mode == ANNOTATIONS) {
            		   twl.exportWords(files, selectedTiers, exportFile, encoding, new String(""), countOccurrences);   
            	   } else if (mode == WORDS) {
            		   twl.exportWords(files, selectedTiers, exportFile, encoding, delimiters, countOccurrences);   
            	   }
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("ExportDialog.Message.Error"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);

            return false;
        }        
        
        return true;
    }    

	/**
     * The state changed event handling.
     *
     * @param ce the change event
     */
    @Override
	public void stateChanged(ChangeEvent ce) {
    	if (ce.getSource() == defaultDelimRB || ce.getSource() == customDelimField) {
	        if (defaultDelimRB.isSelected()) {
	            customDelimField.setEnabled(false);
	        } else {
	            customDelimField.setEnabled(true);
	            customDelimField.requestFocus();
	        }
    	}
    } 
  
    /**
     * Initializes the dialogBox with the last preferred/ used settings 
     *
     */
    private void setPreferredSetting()
    {
    	if(mode == WORDS){         
    		Boolean boolPref = Preferences.getBool("ExportWordListDialog.customDelimRB", null);
        	if (boolPref != null) {
        		customDelimRB.setSelected(boolPref); 
        	}
         
        	boolPref = Preferences.getBool("ExportWordListDialog.defaultDelimRB", null);
        	if (boolPref != null) {
        		defaultDelimRB.setSelected(boolPref); 
        	}
         
        	boolPref = Preferences.getBool("ExportWordListDialog.countTokensCB", null);
        	if (boolPref != null) {
        		countTokensCB.setSelected(boolPref); 
        	}
         
        	boolPref = Preferences.getBool("ExportWordListDialog.customDelimField", null);
        	if (boolPref != null) {
        		customDelimField.setText(boolPref.toString()); 
        	}    		
    	} else {
        	Boolean boolPref = Preferences.getBool("ExportAnnotationListDialog.countTokensCB", null);
        	if( boolPref != null) {
        		countTokensCB.setSelected(boolPref); 
        	}   
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferences(){    	
    	if(mode == WORDS){
        	Preferences.set("ExportWordListDialog.customDelimRB", customDelimRB.isSelected(), null);
        	Preferences.set("ExportWordListDialog.defaultDelimRB", defaultDelimRB.isSelected(), null);
        	Preferences.set("ExportWordListDialog.countTokensCB", countTokensCB.isSelected(), null);
        	
        	if (customDelimField.getText() != null) {
        		Preferences.set("ExportWordListDialog.customDelimField", customDelimField.getText(), null);
        	}
        	
        	if(!multipleFileExport){
        		Preferences.set("ExportWordListDialog.selectedTiers", getSelectedTiers(), transcription);        		
        		Preferences.set("ExportWordListDialog.SelectTiersMode", getSelectionMode(), transcription);    	
            	Preferences.set("ExportWordListDialog.HiddenTiers", getHiddenTiers(), transcription);
            	// save the selected list in case on non-tier tab
            	if (getSelectionMode() != AbstractTierSortAndSelectPanel.BY_TIER) {
            		Preferences.set("ExportWordListDialog.LastSelectedItems", getSelectedItems(), transcription);
            	}
            	List<String> tierOrder = getTierOrder();
            	Preferences.set("ExportWordListDialog.TierOrder", tierOrder, transcription);
            	/*
            	List currentTierOrder = getCurrentTierOrder();    	    	
            	for(int i=0; i< currentTierOrder.size(); i++){
            		if(currentTierOrder.get(i) != tierOrder.get(i)){
            			Preferences.set("ExportWordListDialog.TierOrder", currentTierOrder, transcription);
            			break;
            		}
            	} 
            	*/   	
        	} else {
        		Preferences.set("ExportWordListDialog.SelectTiersMode", getSelectionMode(), null);
        	}
    	} else {   
        	Preferences.set("ExportAnnotationListDialog.countTokensCB", countTokensCB.isSelected(), null);
    	}    	
    }
}
