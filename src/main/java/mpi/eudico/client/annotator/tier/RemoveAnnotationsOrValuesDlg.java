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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * A dialog to remove all the annotations or to remove the annotations with specific values from one or more tiers
 * 
 * Created on Oct 20, 2010 
 * @author Aarthy Somasundaram
 * @version Oct 20, 2010
 */
@SuppressWarnings("serial")
public class RemoveAnnotationsOrValuesDlg extends ClosableDialog implements ActionListener,
    ChangeListener {
    private TranscriptionImpl transcription;

    // ui elements
    //private JPanel titlePanel;
    private JPanel tierPanel;
    private JPanel optionsPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JButton startButton;
    private JLabel titleLabel;
    private JLabel optionsTitleLabel;
   
    private JTable tierTable;
    private TierExportTableModel model;
    
    private JRadioButton annotationsRadioButton;    
    private JRadioButton annotationValuesRadioButton;
    
    private JRadioButton allAnnotationsRadioButton;
    private JRadioButton emptyAnnotationsRadioButton;
    private JRadioButton annotationsWithValRadioButton;
    private JTextField annotationsWithValTextField;    
    
    private JRadioButton allAnnotationsRadioButton1;
    private JRadioButton annotationsWithValRadioButton1;
    private JTextField annotationsWithValTextField1; 

    /**
     * Creates a new RemoveAnnotationsOrValuesDlg instance
     *
     * @param transcription the transcription that hold the tiers
     */
    public RemoveAnnotationsOrValuesDlg(TranscriptionImpl transcription, Frame frame) {
        super(frame);
        this.transcription = transcription;
        initComponents();
        extractTiers();
        postInit();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });
        //titlePanel = new JPanel();
        tierPanel = new JPanel();
        optionsPanel = new JPanel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();
        titleLabel = new JLabel(); 
        optionsTitleLabel = new JLabel();
        
        model = new TierExportTableModel();
        tierTable = new TierExportTable(model);

        JScrollPane tierScroll = new JScrollPane(tierTable);
        tierScroll.setPreferredSize(new Dimension(100, 100));
               
        annotationsRadioButton = new JRadioButton();
        emptyAnnotationsRadioButton = new JRadioButton();
        annotationValuesRadioButton = new JRadioButton();
        
        annotationsWithValTextField = new JTextField();
        allAnnotationsRadioButton = new JRadioButton();
        annotationsWithValRadioButton = new JRadioButton();     
        
        annotationsWithValTextField1 = new JTextField();
        allAnnotationsRadioButton1 = new JRadioButton();
        annotationsWithValRadioButton1 = new JRadioButton();   
       

        ButtonGroup optionsGroup = new ButtonGroup();
        optionsGroup.add(annotationsRadioButton);
        optionsGroup.add(annotationValuesRadioButton);  

        ButtonGroup subOptionsGroup = new ButtonGroup();
        subOptionsGroup.add( allAnnotationsRadioButton);
        subOptionsGroup.add( emptyAnnotationsRadioButton);
        subOptionsGroup.add(annotationsWithValRadioButton);
        
        ButtonGroup subOptionsGroup1 = new ButtonGroup();
        subOptionsGroup1.add( allAnnotationsRadioButton1);
        subOptionsGroup1.add(annotationsWithValRadioButton1);     
        
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
        gridBagConstraints.weightx = 1.0;
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
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;   
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(optionsTitleLabel, gridBagConstraints); 
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;   
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(10,6,4,6);
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(annotationsRadioButton, gridBagConstraints);          

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(allAnnotationsRadioButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(emptyAnnotationsRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(annotationsWithValRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;       
        gridBagConstraints.insets = new Insets(4,2,4,6);
        //gridBagConstraints.insets = new Insets(4,250,4,6);
        optionsPanel.add(annotationsWithValTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        optionsPanel.add(annotationValuesRadioButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(allAnnotationsRadioButton1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,22,4,6);
        optionsPanel.add(annotationsWithValRadioButton1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4,2,4,6);
        //gridBagConstraints.insets = new Insets(4,250,4,6);
        optionsPanel.add(annotationsWithValTextField1, gridBagConstraints);

        

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));
        startButton.addActionListener(this);
        buttonPanel.add(startButton);
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
        
        setDefaultOrPreferredSettings();
        updateLocale();
       
        annotationsRadioButton.addChangeListener(this);
        annotationValuesRadioButton.addChangeListener(this);
        annotationsWithValRadioButton.addChangeListener(this);
        annotationsWithValRadioButton1.addChangeListener(this);       
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
     * Intializes the dialogBox with the last preferred/default settings 
     *
     */
    private void setDefaultOrPreferredSettings(){    	
        
        // defaults
        annotationsRadioButton.setSelected(true);
        annotationValuesRadioButton.setSelected(false);
        
        allAnnotationsRadioButton.setSelected(true);
        emptyAnnotationsRadioButton.setSelected(false);
        annotationsWithValRadioButton.setSelected(false);     
        annotationsWithValTextField.setEnabled(false); 
        
        allAnnotationsRadioButton1.setSelected(true);
        allAnnotationsRadioButton1.setEnabled(false);
        annotationsWithValRadioButton1.setSelected(false);     
        annotationsWithValRadioButton1.setEnabled(false);   
        annotationsWithValTextField1.setEnabled(false); 
        
       
    	Boolean boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.annotationsRadioButton", null);
    
    	if(boolPref != null){
    		annotationsRadioButton.setSelected(boolPref); 
    	}	
     
    	boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.annotationValuesRadioButton", null);
    	if(boolPref != null){
    		annotationValuesRadioButton.setSelected(boolPref); 
    	}
     
    	boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.allAnnotationsRadioButton", null);
    	if(boolPref != null){
    		allAnnotationsRadioButton.setEnabled(annotationsRadioButton.isSelected());
    		allAnnotationsRadioButton.setSelected(boolPref);     		
    	}
     
    	boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.emptyAnnotationsRadioButton", null);
    	// this radio button was added later, set enabled state regardless of existing preference
    	emptyAnnotationsRadioButton.setEnabled(annotationsRadioButton.isSelected());
    	if(boolPref != null){
    		emptyAnnotationsRadioButton.setSelected(boolPref);     		
    	}
    	
    	boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.annotationsWithValRadioButton", null);
    	if(boolPref != null){
    		annotationsWithValRadioButton.setEnabled(annotationsRadioButton.isSelected());
    		annotationsWithValRadioButton.setSelected(boolPref); 
    		annotationsWithValTextField.setEnabled(annotationsWithValRadioButton.isSelected());
    	}
     
    	boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.allAnnotationsRadioButton1", null);
    	if(boolPref != null){
    		allAnnotationsRadioButton1.setSelected(boolPref); 
    		allAnnotationsRadioButton1.setEnabled(annotationValuesRadioButton.isSelected());
    	}
     
    	boolPref = Preferences.getBool("RemoveAnnotationsOrValuesDlg.annotationsWithValRadioButton1", null);
    	if(boolPref != null){
    		annotationsWithValRadioButton1.setSelected(boolPref); 
    		annotationsWithValRadioButton1.setEnabled(annotationValuesRadioButton.isSelected());
    		annotationsWithValTextField1.setEnabled(annotationsWithValRadioButton1.isSelected()); 
    	}
    }
    
    /**
     * Saves the preferred settings Used. 
     *
     */
    private void savePreferredSettings(){
    	Preferences.set("RemoveAnnotationsOrValuesDlg.annotationsRadioButton", annotationsRadioButton.isSelected(), null);    
    	Preferences.set("RemoveAnnotationsOrValuesDlg.annotationValuesRadioButton", annotationValuesRadioButton.isSelected(), null);
    	Preferences.set("RemoveAnnotationsOrValuesDlg.allAnnotationsRadioButton", allAnnotationsRadioButton.isSelected(), null);
    	Preferences.set("RemoveAnnotationsOrValuesDlg.emptyAnnotationsRadioButton", emptyAnnotationsRadioButton.isSelected(), null);
    	Preferences.set("RemoveAnnotationsOrValuesDlg.annotationsWithValRadioButton", annotationsWithValRadioButton.isSelected(), null);
    	Preferences.set("RemoveAnnotationsOrValuesDlg.allAnnotationsRadioButton1", allAnnotationsRadioButton1.isSelected(), null);
    	Preferences.set("RemoveAnnotationsOrValuesDlg.annotationsWithValRadioButton1", annotationsWithValRadioButton1.isSelected(), null);
    }
    
    private void updateLocale() {
        setTitle(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Title"));
        titleLabel.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Title"));        
        tierPanel.setBorder(new TitledBorder(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Label.Tier")));
        optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Label.Options")));  
        optionsTitleLabel.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Label.Options.Title"));
        annotationsRadioButton.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.Annotations"));
        annotationValuesRadioButton.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.AnnotationValues"));
        allAnnotationsRadioButton.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.AllAnnotations"));
        emptyAnnotationsRadioButton.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.EmptyAnnotations"));
        annotationsWithValRadioButton.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.AnnotationsWithValues"));
        allAnnotationsRadioButton1.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.AllAnnotations"));
        annotationsWithValRadioButton1.setText(ElanLocale.getString("RemoveAnnotationsOrValuesDlg.RadioButton.AnnotationsWithValues"));
        
        startButton.setText(ElanLocale.getString("Button.OK"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
       
    }

    /**
     * Extract all tiers and fill the table.
     */
    private void extractTiers() {
        if (transcription != null) {
            List<TierImpl> v = transcription.getTiers();

            model.extractTierNames(v);
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
        }       
    }
    
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == annotationsRadioButton){
			allAnnotationsRadioButton.setEnabled(annotationsRadioButton.isSelected());
			emptyAnnotationsRadioButton.setEnabled(annotationsRadioButton.isSelected());
			annotationsWithValRadioButton.setEnabled(annotationsRadioButton.isSelected());			
		}else if(e.getSource() == annotationValuesRadioButton){
			allAnnotationsRadioButton1.setEnabled(annotationValuesRadioButton.isSelected());
			annotationsWithValRadioButton1.setEnabled(annotationValuesRadioButton.isSelected());			
		}else if(e.getSource() == annotationsWithValRadioButton){			
			annotationsWithValTextField.setEnabled(annotationsRadioButton.isSelected());
		}else if(e.getSource() == annotationsWithValRadioButton1){			
			annotationsWithValTextField1.setEnabled(annotationValuesRadioButton.isSelected());
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
    	
        List<String> tierNames = null;
        boolean annotations = false;
        boolean annotationValues = false;
        boolean allAnnotations = false;
        boolean annotationWithVal = false;
        String value = null;
        
        tierNames = getSelectedTiers();

        if (tierNames.size() == 0 ) {
            JOptionPane.showMessageDialog(this,
                ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Warning.NoTier"),
                ElanLocale.getString("Message.Error"),
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (annotationsRadioButton.isSelected()) {
        	annotations = true;
                   
            if (allAnnotationsRadioButton.isSelected()) {
            	allAnnotations = true;
            } else if (emptyAnnotationsRadioButton.isSelected()) {
            	annotationWithVal = true;
            	value = "";
            } else if(annotationsWithValRadioButton.isSelected()){
            	annotationWithVal = true;
            	if(annotationsWithValTextField.getText() != null && annotationsWithValTextField.getText().length() > 0 ){
            		value = annotationsWithValTextField.getText().trim();
            	} else {
//            		 JOptionPane.showMessageDialog(this,
//            	                ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Warning.EmptyValue"),
//            	                ElanLocale.getString("Message.Error"),
//            	                JOptionPane.WARNING_MESSAGE);
//            	            return;
            		// HS Aug 2013 treat an empty text field the same as the empty annotation option
            		value = "";
            	}
            }
        } else if (annotationValuesRadioButton.isSelected()) {
        	annotationValues = true;
                   
            if (allAnnotationsRadioButton1.isSelected()) {
            	allAnnotations = true;
            } else if(annotationsWithValRadioButton1.isSelected()){
            	annotationWithVal = true;
            	if(annotationsWithValTextField1.getText() != null && annotationsWithValTextField1.getText().length() > 0){
            		value = annotationsWithValTextField1.getText().trim();
            	} else {
            		JOptionPane.showMessageDialog(this,
     	                ElanLocale.getString("RemoveAnnotationsOrValuesDlg.Warning.EmptyValue"),
     	                ElanLocale.getString("Message.Error"),
     	                JOptionPane.WARNING_MESSAGE);
     	            return;
            	}
            } 
        }

        Object[] args = new Object[] { tierNames, annotations, annotationValues, allAnnotations, annotationWithVal, value };
        Command command = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.REMOVE_ANNOTATIONS_OR_VALUES);

        command.execute(transcription, args);
    }
}
