package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A dialog to create a  new participant for a selected tier structure
 * or all for the tiers of the selected participant.
 * 
 * Created on Oct 20, 2010 
 * @author Aarthy Somasundaram
 * @version Oct 20, 2010
 */
@SuppressWarnings("serial")
public class AddParticipantDlg extends ClosableDialog implements ActionListener{
     private TranscriptionImpl transcription;

    // ui elements
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton startButton;
    private JLabel titleLabel;
   
    private JTable tierTable;
    private TierExportTableModel model;
    
    private JRadioButton selectTierStrucRB;
    private JRadioButton selectParticipantRB;
    
    private JLabel participantLabel;    
    private JTextField participantTextField;  
    
    private JLabel prefixSuffixLabel;        
    private JRadioButton prefixRB;
    private JRadioButton suffixRB;
    
    private JLabel oldValueLabel;    
    private JTextField oldValueTextField;    
    private JLabel newValueLabel;     
    private JTextField newValueTextField; 
    
    private boolean close = false;

    /**
     * Creates a new AddParticipantDlg instance
     *
     * @param transcription the transcription that hold the tiers
     */
    public AddParticipantDlg(TranscriptionImpl transcription, Frame frame) {
        super(frame);
        this.transcription = transcription;
        initComponents();        
        extractTiers();
        if(close){
        	closeDialog(null);
        }else{
        	postInit();
        	setVisible(true);
        }
        
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });
        
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();
        titleLabel = new JLabel();       
        oldValueLabel = new JLabel();
        oldValueTextField = new JTextField();
        newValueLabel = new JLabel();        
        newValueTextField = new JTextField();     
        participantLabel = new JLabel();   
        participantTextField = new JTextField();   
        prefixSuffixLabel = new JLabel();   
        prefixRB = new JRadioButton();
        suffixRB = new JRadioButton();
        selectTierStrucRB = new JRadioButton();
        selectParticipantRB = new JRadioButton();
        
        ButtonGroup group = new ButtonGroup();
        group.add(prefixRB);
        group.add(suffixRB);
        
        ButtonGroup group1 = new ButtonGroup();
        group1.add(selectTierStrucRB);
        group1.add(selectParticipantRB);
        
        model = new TierExportTableModel();
        tierTable = new TierExportTable(model);

        JScrollPane tierScroll = new JScrollPane(tierTable);
        tierScroll.setPreferredSize(new Dimension(100, 100));  
        
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        setModal(true);
        getContentPane().setLayout(new GridBagLayout());
        
        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gridBagConstraints;
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titleLabel, gridBagConstraints);
       
        tierPanel.setLayout(new GridBagLayout());
        
        JPanel panel = new JPanel(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        //gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 1.0;
        panel.add(selectTierStrucRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        //gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 1.0;
        panel.add(selectParticipantRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(2,22,2,6);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 1.0;
        tierPanel.add(panel, gridBagConstraints);

        Dimension tableDim = new Dimension(50, 100);  
        JScrollPane tierScrollPane = new JScrollPane(tierTable);
        tierScrollPane.setPreferredSize(tableDim);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        //gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tierPanel.add(tierScrollPane, gridBagConstraints);

        // add more elements to this panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(tierPanel, gridBagConstraints);
       
        optionsPanel.setLayout(new GridBagLayout());
        insets.bottom = 3;    
        
     // add elements to the optionspanel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;       
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(optionsPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);        
        
        // elements in optionspanel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;   
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weighty = 1.0;
        optionsPanel.add(participantLabel, gridBagConstraints);          

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;       
        gridBagConstraints.weightx = 0.5;     
        optionsPanel.add(participantTextField, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;        
        optionsPanel.add(prefixSuffixLabel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,30,4,6);        
        optionsPanel.add(prefixRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,30,4,6);        
        optionsPanel.add(suffixRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;   
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;       
        optionsPanel.add(oldValueLabel , gridBagConstraints);          

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;        
        optionsPanel.add(oldValueTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;       
        optionsPanel.add(newValueLabel , gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;      
        optionsPanel.add(newValueTextField, gridBagConstraints);

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));
        startButton.addActionListener(this);
        buttonPanel.add(startButton);
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
        
        setDefaultOrPreferredSettings();
        
        selectParticipantRB.addActionListener(this);
        selectTierStrucRB.addActionListener(this);
        updateLocale();
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();    
        setLocationRelativeTo(getParent());
        //setResizable(false);       
    }
    
    /**
     * Initializes the dialogBox with the last preferred/default settings 
     *
     */
    private void setDefaultOrPreferredSettings(){  
    	
    	
    	Boolean boolPref = Preferences.getBool("AddParticipantDlg.prefixRB", null);    
    	if (boolPref != null) {
    		prefixRB.setSelected(boolPref); 
    	} else {
    		prefixRB.setSelected(true);     	
    	}
    	
    	boolPref = Preferences.getBool("AddParticipantDlg.selectTierStrucRB", null);    
    	if (boolPref != null) {
    		selectTierStrucRB.setSelected(boolPref);     		
    	} else {
    		selectTierStrucRB.setSelected(true); 
    		
    	}    	
    	
    	suffixRB.setSelected(!prefixRB.isSelected());
    	selectParticipantRB.setSelected(!selectTierStrucRB.isSelected());
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferredSettings(){ 
    	Preferences.set("AddParticipantDlg.prefixRB", prefixRB.isSelected(), null);   
    	Preferences.set("AddParticipantDlg.selectTierStrucRB", selectTierStrucRB.isSelected(), null);  
    }
    
    private void updateLocale() {
        setTitle(ElanLocale.getString("AddParticipantDlg.Title"));
        titleLabel.setText(ElanLocale.getString("AddParticipantDlg.Title"));        
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString("AddParticipantDlg.Title.TierTable")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("AddParticipantDlg.Title.Options")));  
        participantLabel.setText(ElanLocale.getString("AddParticipantDlg.Label.Participant"));
        prefixSuffixLabel.setText(ElanLocale.getString("AddParticipantDlg.Label.PrefixSuffix"));        
        prefixRB.setText(ElanLocale.getString("AddParticipantDlg.RB.Prefix"));
        suffixRB.setText(ElanLocale.getString("AddParticipantDlg.RB.Suffix"));
        oldValueLabel.setText(ElanLocale.getString("AddParticipantDlg.OldValue"));
        newValueLabel.setText(ElanLocale.getString("AddParticipantDlg.NewValue"));        
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));        
        selectParticipantRB.setText(ElanLocale.getString("AddParticipantDlg.RB.Participant"));
        selectTierStrucRB.setText(ElanLocale.getString("AddParticipantDlg.RB.TierStructure"));
    }

    /**
     * Extract all tiers and fill the table.
     */
    private void extractTiers() {
    	close = false;
    	
        if (transcription != null) {
        	while(tierTable.getRowCount() > 0){
        		model.removeRow(0);
        	} 
        	        	
        	List<TierImpl> v = transcription.getTiers();
        	TierImpl t;
        	
        	if(selectTierStrucRB.isSelected() && selectTierStrucRB.isEnabled()){
        		for (int i = 0; i < v.size(); i++) {
        			t = v.get(i);
        			if(t.hasParentTier()){
        				continue;
        			}
                
        			List<TierImpl> dependentTiers = t.getDependentTiers();
        			if(dependentTiers == null || dependentTiers.size() == 0){
        				continue;
        			}

        			if (i == 0) {
        				model.addRow(Boolean.TRUE, t.getName());
        			} else {
        				model.addRow(Boolean.FALSE, t.getName());
        			}
        		}
        		
        		if(model.getRowCount() < 1){
        			selectTierStrucRB.setEnabled(false);
        			if(selectParticipantRB.isEnabled()){
        				selectParticipantRB.setSelected(true);
        			}
        		} else {
        			return;
        		}
        	} 
        	
        	if(selectParticipantRB.isSelected() && selectParticipantRB.isEnabled()){
        		List<String> participants = new ArrayList<String>();

        		for (int i = 0; i < v.size(); i++) {
        			t = v.get(i);
        			String partici = t.getParticipant();
//        			if(partici == null || partici.trim().length() == 0){
//        				partici = "Unknown";
//        			}
        			if(partici != null && partici.trim().length() != 0 && !participants.contains(partici)){
        				participants.add(partici);
        			}
        		}
        		
        		if(participants.size() > 0){
        			for(int i=0; i< participants.size(); i++){
        				if (i == 0) {
        					model.addRow(Boolean.FALSE, participants.get(i));
        				} else {
        					model.addRow(Boolean.FALSE, participants.get(i));
        				}
        			} 
        			return;
        		} else {        	
        			if(selectTierStrucRB.isEnabled()){
        				selectTierStrucRB.setSelected(true);
        			}
            		selectParticipantRB.setEnabled(false);            		
        		}
        	}
        		
      		if(!selectParticipantRB.isEnabled() && !selectTierStrucRB.isEnabled()){
       			JOptionPane.showMessageDialog(this, ElanLocale.getString("AddParticipantDlg.Message.NoTier/Participant"), ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
       			close = true;
       		} else {        			
       			extractTiers();
       		}
        }
    }     

	/**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    private void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }
    
    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) { 
    	Object source = ae.getSource();

        if (source == startButton) {
            startOperation();
        } else if (source == closeButton) {
            closeDialog(null);
        }  else if(source == selectTierStrucRB || source ==selectParticipantRB){
        	extractTiers();
        }
    }
	
	
	/**
     * Returns the tiers that have been selected in the table.
     *
     * @return a list of the selected tiers
     */
    private List<String> getSelectedTiers() {
    	return model.getSelectedTiers();
    }
    
    /**
     * Checks the current settings and creates a Command.
     */
    private void startOperation() {
    	savePreferredSettings();
    	
        List<String> selectedValues = getSelectedTiers();
        if (selectedValues.size() == 0 ) {
        	if(selectTierStrucRB.isSelected()){
        		JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("AddParticipantDlg.Warning.NoTier"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
               
            } else if(selectParticipantRB.isSelected()){
            	JOptionPane.showMessageDialog(this,
            			ElanLocale.getString("AddParticipantDlg.Warning.NoParticipant"),
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.WARNING_MESSAGE);
            }
        	       	    	  
            return;
        }         
        
        String participantName = participantTextField.getText();
        if(participantName == null || participantName.trim().length() <= 0){
        	JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("AddParticipantDlg.Warning.NewParticipant"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String newValue = newValueTextField.getText();
        if(newValue == null || newValue.trim().length() <= 0){
        	JOptionPane.showMessageDialog(this,
                    ElanLocale.getString("AddParticipantDlg.Warning.NewValue"),
                    ElanLocale.getString("Message.Error"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }     
        
        // validate the new tier names and check
        String oldValue = oldValueTextField.getText();
        TierImpl tier;
        List<String> tierNames = new ArrayList<String>();
        if(selectTierStrucRB.isSelected()){	     
	    	 for(int i= 0; i< selectedValues.size();i++){	    		
	    		 String newTier = getNewTierName(oldValue, selectedValues.get(i), newValue);
	    		 if(transcription.getTierWithId(newTier) != null){
	    			 tierNames.add(newTier);
	    		 } 
	    	}
	     }else {
	    	 List<TierImpl> tiers = transcription.getTiers();	    	
	    	 for(int i= 0; i< tiers.size();i++){
	    		 tier = tiers.get(i);
	    		 if(tier != null && !tier.hasParentTier()){
	    			if(selectedValues.contains(tier.getParticipant())) {	    				
	    				String newTier = getNewTierName(oldValue, tier.getName(), newValue);	    	
	   	    		 	if(transcription.getTierWithId(newTier) != null){
	   	    		 	 tierNames.add(newTier);
	   	    		 	} 
	    			}
	    		 }
	    	 }	
	     }
        
        if(tierNames.size() == selectedValues.size()){
        	JOptionPane.showMessageDialog(this, ElanLocale.getString("AddParticipantDlg.Warning.OutPut.NoTiers"),
                    ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
        	return;
        }
        
        if(tierNames.size() > 0 && tierNames.size() < selectedValues.size()){
        	 int response = JOptionPane.showConfirmDialog(this,
        			 ElanLocale.getString("AddParticipantDlg.Warning.OutPut.FewTiers.Part1")+ " " +
        					 ElanLocale.getString("AddParticipantDlg.Warning.OutPut.FewTiers.Part2"),
	                    ElanLocale.getString("Message.Warning"),
	                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	
	         if (response != JOptionPane.YES_OPTION) {
	        	 return;
	         } 
        } 
        
        
        Object[] args = new Object[] { selectedValues, participantName, oldValueTextField.getText(), newValue, 
   			 prefixRB.isSelected(), selectTierStrucRB.isSelected() };
   	 	Command command = ELANCommandFactory.createCommand(transcription,
   			 ELANCommandFactory.ADD_PARTICIPANT);
   	 	command.execute(transcription, args);
    }
        
    private String getNewTierName(String oldValue, String tierName, String newValue){
    	if(oldValue != null && oldValue.trim().length() > 0){        	
    	    if(prefixRB.isSelected()){
    	    	if(tierName.startsWith(oldValue)){
    	    		tierName= tierName.replaceFirst(oldValue, newValue);
    	    	} else {
    	    		tierName = newValue +"-" +tierName;
    	    	}
    	    } else {
    	    	if(tierName.endsWith(oldValue)){
    	    		tierName= tierName.substring(0, tierName.lastIndexOf(oldValue)) + newValue;
    	    	} else {
    	    		tierName = tierName +"-" +newValue;
    	    	}
    	    }   
         } else {
           	if(prefixRB.isSelected()){
           		tierName = newValue +"-" +tierName;
           	} else {
           		tierName = tierName +"-" +newValue;
           	}
    	 }        
        return tierName;
	 }
}

    

 