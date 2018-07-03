package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A dialog to apply the attributes of a tier to all the tiers 
 * with same type, participant or on all depending tiers. 
 * 
 * @version 1.0
 * @author aarsom
 *
 */
@SuppressWarnings("serial")
public class AdvancedAttributeSettingOptionDialog extends JDialog implements ActionListener, ChangeListener{
	
	private JCheckBox typeCB;
	private JCheckBox dependentTiersCB;
	private JCheckBox participantsCB;
	private JCheckBox tierColorCB;
	private JCheckBox tierHighLightColorCB;
	private JCheckBox tierFontCB;
	private JButton okButton;
	
	private Map<String, Object> tierProperties;	
	private String tierName;
    private TranscriptionImpl transcription;
    
    // flag which says whether it is called from tier dialog or
    // a direct call to apply the attributes of the current tier
    private boolean appleAttributesDlg = false;
        
    /**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param title the dialog title
     * @param tierProperties a map containing current properties key-value pairs 
     */
	public AdvancedAttributeSettingOptionDialog(Dialog owner, String title, Map<String, Object> tierProps) {
        super(owner, title, true);
        this.tierProperties = tierProps;
        initComponents();
        postInit();
	}
	
	/**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param title the dialog title
     * @param transcription trancription of the tier
     * @param tierName the name of the tier whose attributes have to be applied
     */
	public AdvancedAttributeSettingOptionDialog(Dialog owner, String title,TranscriptionImpl transcription, String tierName) {     
		super(owner, title, true);
		
		initialize(transcription, tierName);
	}
	
	/**
     * Creates a new Instance.
     *
     * @param owner the owner window
     * @param title the dialog title
     * @param transcription trancription of the tier
     * @param tierName the name of the tier whose attributes have to be applied
     */
	public AdvancedAttributeSettingOptionDialog(Frame owner, String title,TranscriptionImpl transcription, String tierName) {     
		super(owner, title, true);
		
		initialize(transcription, tierName);

	}
	
	/**
	 * Initialize 
	 * 
	 * @param transcription, the transcription of the tier
	 * @param tierName, the tier whose attributes have to be applied
	 */
	private void initialize(TranscriptionImpl transcription, String tierName){
		appleAttributesDlg = true;
		this.tierName = tierName;
		this.transcription = transcription;
        initComponents();
        postInit();
	}
	
	/**
	 * Initialize the components
	 */
	private void initComponents(){		
		typeCB = new JCheckBox(ElanLocale.getString("EditTierDialog.AdvancedSetting.Type"));
		typeCB.addChangeListener(this);
		
		dependentTiersCB = new JCheckBox(ElanLocale.getString("EditTierDialog.AdvancedSetting.DependentTiers"));
		dependentTiersCB.addChangeListener(this);
		
		participantsCB = new JCheckBox(ElanLocale.getString("EditTierDialog.AdvancedSetting.Participants"));
		participantsCB.addChangeListener(this);
		
		tierColorCB = new JCheckBox(ElanLocale.getString("EditTierDialog.Label.TierColor"),true);
		tierColorCB.addChangeListener(this);
		
		tierHighLightColorCB = new JCheckBox(ElanLocale.getString("EditTierDialog.Label.TierHighlightColor"), true);
		tierHighLightColorCB.addChangeListener(this);
		
		tierFontCB = new JCheckBox(ElanLocale.getString("EditTierDialog.Label.TierFont"), true);
		tierFontCB.addChangeListener(this);
						
		getContentPane().setLayout(new GridBagLayout());
        
        JPanel optionsPanel = new JPanel();  
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditTierDialog.AdvancedSetting.Label.Options")));
        optionsPanel.setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;        
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;      
        gbc.insets = insets;
        optionsPanel.add(typeCB, gbc);
        
        gbc.gridy = 1;      
        optionsPanel.add(dependentTiersCB, gbc);
        
        gbc.gridy = 2;      
        optionsPanel.add(participantsCB, gbc);
        
        JPanel settingsPanel = new JPanel();  
        settingsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditTierDialog.AdvancedSetting.Label.Setting")));
        settingsPanel.setLayout(new GridBagLayout());
        
        okButton = new JButton();
        okButton.setText(ElanLocale.getString("Button.Apply"));
        okButton.addActionListener(this);
        
        if(appleAttributesDlg){
        	okButton.setEnabled(false);
        }
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;        
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;      
        gbc.insets = insets;
        settingsPanel.add(tierColorCB, gbc);
        
        gbc.gridy = 1;      
        settingsPanel.add(tierHighLightColorCB, gbc);
        
        gbc.gridy = 2;      
        settingsPanel.add(tierFontCB, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(settingsPanel, gbc);        
        
        gbc.gridy = 1;
        getContentPane().add(optionsPanel, gbc);        

        gbc = new GridBagConstraints();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(okButton, gbc);   
	}
	
	 /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();        
        setResizable(false);
        setLocationRelativeTo(getParent());
    }
	
    /**
     * close the dialog
     */
	private void doClose() {
        setVisible(false);
        dispose();
    }
	
	/**
     * Returns the, possibly modified, properties.
     *
     * @return the properties
     */
    public Map<String, Object> getTierProperties() {
        return tierProperties;
    }

	@Override
	public void actionPerformed(ActionEvent e) {	
		// if can one of the options are selected then applychanges
		if(tierColorCB.isSelected() || tierHighLightColorCB.isSelected() || tierFontCB.isSelected()){
			if(typeCB.isSelected() || dependentTiersCB.isSelected() || participantsCB.isSelected()){
				if(tierProperties != null){
					applyNewPropertyChanges(); 
				}else {
					applyAttributeSettings();
				}
			}
		}
		
		doClose();
	}
	
	/**
	 * Store the properties for which the attributes and the tiers 
	 * have to be changed
	 */
	private void applyNewPropertyChanges(){		
		tierProperties.put("SameType", typeCB.isSelected());   
		tierProperties.put("DependingTiers", dependentTiersCB.isSelected());  
		tierProperties.put("SameParticipants", participantsCB.isSelected());  
		tierProperties.put("Color", tierColorCB.isSelected());   
		tierProperties.put("HighLightColor", tierHighLightColorCB.isSelected());  
		tierProperties.put("Font", tierFontCB.isSelected());
	}
	
	/**
	 * Applies the selected attributes on the selected tiers
	 */
	private void applyAttributeSettings(){
		Map<String, Color> colors = Preferences.getMapOfColor("TierColors", transcription);
		if (colors == null) {
			colors = new HashMap<String, Color>();
			Preferences.set("TierColors", colors, transcription);
		}
        
		Map<String, Color> highlightColors = Preferences.getMapOfColor("TierHighlightColors", transcription);
        if(highlightColors == null) {
        	highlightColors = new HashMap<String, Color>();
        	Preferences.set("TierHighlightColors", highlightColors, transcription);
        }        

        Map<String, Font> fonts = Preferences.getMapOfFont("TierFonts", transcription);
		if (fonts == null) {
			fonts = new HashMap<String, Font>();
			Preferences.set("TierFonts", fonts, transcription);
		}
    	
    	TierImpl tier = transcription.getTierWithId(tierName);	
		
    	List<TierImpl> tierList = new ArrayList<TierImpl>();
		if(typeCB.isSelected()){
			if(tier.getLinguisticType() != null){
				if(transcription.getTiersWithLinguisticType(tier.getLinguisticType().getLinguisticTypeName()) !=null){
					tierList.addAll(transcription.getTiersWithLinguisticType(tier.getLinguisticType().getLinguisticTypeName()));
				}
			}
		}
		
		if(dependentTiersCB.isSelected()){
			if(tier.getDependentTiers() != null){
				tierList.addAll(tier.getDependentTiers());
			}
		}  
		
		if(participantsCB.isSelected() ){
			if(tier.getParticipant() != null){
				List<TierImpl> allTiers = transcription.getTiers();
				for(int i= 0; i<allTiers.size(); i++ ){
					TierImpl t = allTiers.get(i);
					if(t.getParticipant()!= null){
						if(t.getParticipant().equals(tier.getParticipant())){
							if(!tierList.contains(t)){
								tierList.add(t);
							}
						}
					}
				}
			}				
		}
		
		Color nextColor = colors.get(tierName);
        Color nextHighlightColor = highlightColors.get(tierName);       
		Font fo = fonts.get(tierName);
		
		for(int i=0; i< tierList.size(); i++){
			TierImpl t = tierList.get(i);
			if (tierColorCB.isSelected()){
				if(nextColor != null && !nextColor.equals(Color.WHITE) ) {
					((Map<String, Color>) colors).put(t.getName(), nextColor); 
				}
        	}
			
			if (tierHighLightColorCB.isSelected()) {
				if(nextHighlightColor != null && !nextHighlightColor.equals(Color.WHITE)) {
					((Map<String, Color>) highlightColors).put(t.getName(), nextHighlightColor);
				} 
            } 
		
			if (tierFontCB.isSelected()) {
				if( fo != null) {
					((Map<String, Font>) fonts).put(t.getName(), fo);
				} else {
					((Map<String, Font>) fonts).remove(t.getName());
				}
        	}        			
		}
		
		Preferences.set("TierHighlightColors", highlightColors, transcription);
		Preferences.set("TierColors", colors, transcription);
		Preferences.set("TierFonts", fonts, transcription, true);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {		
		if(tierColorCB.isSelected() || tierHighLightColorCB.isSelected() || tierFontCB.isSelected()){
			typeCB.setEnabled(true);
			dependentTiersCB.setEnabled(true);
			participantsCB.setEnabled(true);
			 if(appleAttributesDlg){
				 if(typeCB.isSelected()|| dependentTiersCB.isSelected() || participantsCB.isSelected()){
		        	okButton.setEnabled(true);
				 } else {
		        	okButton.setEnabled(false);
		         }
			 }
		}else {
			typeCB.setEnabled(false);
			dependentTiersCB.setEnabled(false);
			participantsCB.setEnabled(false);
			if(appleAttributesDlg){
		       	okButton.setEnabled(false);
		    }
		}	
	}
}
