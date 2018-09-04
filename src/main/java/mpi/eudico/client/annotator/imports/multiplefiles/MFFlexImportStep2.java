package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ImportFLExDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.flex.FlexConstants;
import mpi.eudico.server.corpora.clomimpl.flex.FlexDecoderInfo;

/**
 * Step 2: Abstract Step pane for setting the import options
 * 
 * @author aarsom
 * @version April, 2013
 */
public class MFFlexImportStep2 extends AbstractMFImportStep2 implements ChangeListener{	
			
	    private JCheckBox includeITCB;
	    private JCheckBox includeParagrCB;
	    private JCheckBox importParticipantInfoCB;
	    private JComboBox unitsCombo;   
	    
	    //duration   
	    private JTextField unitTextField;
	    
	    //linguistic types
	    private JRadioButton typesPerElementRB;
	    private JRadioButton typesPerTypeRB;
	    private JCheckBox typesPerLanguageCB; 
	   
	    
	    private final String[] elements = new String[] {
	            FlexConstants.PHRASE, FlexConstants.WORD
	        };
	  
	    private FlexDecoderInfo decoderInfo = null;
	    
	
	public MFFlexImportStep2(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}
	 
	/**
	 * Initializes ui components.
	 */
	@Override
	public void initComponents() {
		setLayout(new GridBagLayout());
	    setBorder(new EmptyBorder(12, 12, 12, 12));	
	           
        includeITCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.IncludeIT"));
        includeITCB.setSelected(true);
        includeParagrCB = new JCheckBox(ElanLocale.getString(
                    "ImportDialog.Flex.IncludePara"));
        includeParagrCB.setSelected(true);
        
        importParticipantInfoCB = new JCheckBox(ElanLocale.getString(
                "ImportDialog.Flex.ImportParticipantInfo"));
        
        typesPerElementRB = new JRadioButton(ElanLocale.getString("ImportDialog.Flex.LinTypeForBasicElement"), true);
        typesPerElementRB.addChangeListener(this);
        typesPerTypeRB = new JRadioButton(ElanLocale.getString("ImportDialog.Flex.LinTypeForTypes"));
        typesPerTypeRB.addChangeListener(this);
        
        ButtonGroup group = new ButtonGroup();
        group.add(typesPerElementRB);
        group.add(typesPerTypeRB);
        
        typesPerLanguageCB = new JCheckBox(ElanLocale.getString("ImportDialog.Flex.LinTypeForLang"));      
        typesPerLanguageCB.setEnabled(false);        
        
        unitsCombo = new JComboBox(elements);
        unitsCombo.setSelectedItem(FlexConstants.PHRASE);      
        unitTextField = new JTextField("", 8);
        
        Insets insets = new Insets(6, 6, 2, 6);
       
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.gridwidth = 2;
        add(includeITCB, gbc);
        
        gbc.gridy = gbc.gridy+1;
        add(includeParagrCB, gbc);
        
        gbc.gridy = gbc.gridy +1;
        add(importParticipantInfoCB, gbc);      
        
        gbc.gridy = gbc.gridy+1;
        gbc.gridwidth = 1;          
        add(new JLabel(ElanLocale.getString(
        		"ImportDialog.Flex.SmallestTimeAlignable")), gbc);
       
        gbc.gridx = 1; 
        add(unitsCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = gbc.gridy+1;
        gbc.gridwidth = 2;     
        add(new JLabel(ElanLocale.getString("ExportTiersDialog.Tab2")), gbc);
        
        gbc.gridy = gbc.gridy+1;
        gbc.insets = new Insets(2, 10, 2, 6);
        add(typesPerElementRB, gbc);
        
        gbc.gridy = gbc.gridy+1;       
        add(typesPerTypeRB, gbc);
     
        gbc.gridy = gbc.gridy+1;
        gbc.insets = new Insets(2, 26, 2, 6);
        add(typesPerLanguageCB, gbc);
        
        gbc.gridy = gbc.gridy+1;
        gbc.insets = insets;
        gbc.gridwidth = 1;       
        add(new JLabel(ElanLocale.getString(
                "ImportDialog.Flex.UnitDuration")), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(unitTextField, gbc);     
        
        gbc.gridx = 0;
        gbc.gridy = gbc.gridy+1;
        gbc.gridwidth = 2;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weighty =1.0;
	    add(new JPanel(), gbc);	  
        
        loadPreferences();
	}		
	
	private void loadPreferences() {
		String stringPref;
		Boolean boolPref;
		
		boolPref = Preferences.getBool(ImportFLExDialog.INTERLINEAR_TEXT, null);
		if (boolPref != null) {
			includeITCB.setSelected(boolPref);
		}

		boolPref = Preferences.getBool(ImportFLExDialog.PARAGRAPH, null);
		if (boolPref != null) {
			includeParagrCB.setSelected(boolPref);
		}

		boolPref = Preferences.getBool(ImportFLExDialog.PARTICIPANT, null);
		if (boolPref != null) {
			importParticipantInfoCB.setSelected(boolPref);
		}		

		stringPref = Preferences.getString(ImportFLExDialog.SMALLEST_ALIGNABLE_ELEMENT, null);
		if (stringPref != null) {
			unitsCombo.setSelectedItem(stringPref);
		}

		boolPref = Preferences.getBool(ImportFLExDialog.TYPES_PER_ELEMENT, null);
		if (boolPref != null) {
			typesPerElementRB.setSelected(boolPref);
		}

		boolPref = Preferences.getBool(ImportFLExDialog.TYPES_PER_TYPE, null);
		if (boolPref != null) {
			typesPerTypeRB.setSelected(boolPref);
		}

		boolPref = Preferences.getBool(ImportFLExDialog.TYPES_PER_LANG, null);
		if (boolPref != null) {
			typesPerLanguageCB.setSelected(boolPref);
		}

		stringPref = Preferences.getString(ImportFLExDialog.DURATION, null);
		if (stringPref != null) {
			unitTextField.setText(stringPref);
		}
	}
	
	private void savePreferences(){
		Preferences.set(ImportFLExDialog.INTERLINEAR_TEXT, includeITCB.isSelected(), null);
	    Preferences.set(ImportFLExDialog.PARAGRAPH, includeParagrCB.isSelected(), null);
	    Preferences.set(ImportFLExDialog.PARTICIPANT, importParticipantInfoCB.isSelected(), null);	  
	    Preferences.set(ImportFLExDialog.SMALLEST_ALIGNABLE_ELEMENT, unitsCombo.getSelectedItem(), null);
	    Preferences.set(ImportFLExDialog.TYPES_PER_ELEMENT, typesPerElementRB.isSelected(), null);
	    Preferences.set(ImportFLExDialog.TYPES_PER_TYPE, typesPerTypeRB.isSelected(), null);
	    Preferences.set(ImportFLExDialog.TYPES_PER_LANG, typesPerLanguageCB.isSelected(), null);
	    Preferences.set(ImportFLExDialog.DURATION, unitTextField.getText(), null);
	}
	
   /**
    * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
    */
   @Override
public boolean leaveStepForward() { 
       long durationVal = -1;

       try {
       		durationVal = Long.parseLong(unitTextField.getText());
       } catch (NumberFormatException nfe) {
       		JOptionPane.showMessageDialog(this,
       				ElanLocale.getString("ImportDialog.Flex.Message.DurElement"),
       				ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

       		return false;
       }

       // if we get here create the decoder object  
       decoderInfo = new FlexDecoderInfo();
       decoderInfo.smallestWithTimeAlignment = (String) unitsCombo.getSelectedItem();
       decoderInfo.inclITElement = includeITCB.isSelected();
       decoderInfo.inclParagraphElement = includeParagrCB.isSelected();
       decoderInfo.importParticipantInfo = importParticipantInfoCB.isSelected();
       decoderInfo.perPhraseDuration = durationVal;
       if(typesPerTypeRB.isSelected()){
       	decoderInfo.createLingForNewType = typesPerTypeRB.isSelected();
           decoderInfo.createLingForNewLang = typesPerLanguageCB.isSelected();
       }   
       
       savePreferences();
       
       multiPane.putStepProperty("FlexDecoderInfo", decoderInfo);

       return true;	    
   }
   
	@Override
	public void stateChanged(ChangeEvent e) {
		typesPerLanguageCB.setEnabled(typesPerTypeRB.isSelected());
	}
}
