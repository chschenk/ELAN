package mpi.eudico.client.annotator.export.multiplefiles;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.interlinear.Interlinear;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxTypFile;

/**
 * Panel for step 2: Export/Toolbox options
 * 
 * Set toolbox options for the files
 * those are going to be exported
 * 
 * @author aarsom
 * @version Feb, 2012
 */
public class MultipleFileToolBoxExportStep2 extends StepPane implements ItemListener, ActionListener{

	private JCheckBox wrapBlocksCB;
	private JCheckBox correctTimesCB;
	private JCheckBox wrapLinesCB;
	private JCheckBox includeEmptyLinesCB;
	private JCheckBox mediaMarkerCB;	
	private JCheckBox appendFileNameCB;	
	
	private JTextField numCharTF;
	private JTextField typField;
	private JTextField dbTypField;
	private JTextField markerTF;
	private JTextField mediaMarkerNameTF;
	
	private JLabel mediaMarkerNameLabel;
	private JLabel mediaTypeLabel;
	private JLabel charPerLineLabel;
	private JLabel databaseErrorLabel;
	private JLabel recordMarkerErrorLabel;
	
	
	private JRadioButton ssMSFormatRB;
	private JRadioButton hhMMSSMSFormatRB;
	private JRadioButton wrapNextLineRB;
	private JRadioButton wrapAfterBlockRB;
	private JRadioButton typeRB;
	private JRadioButton specRB;
	private JRadioButton detectedRMRB;
	private JRadioButton defaultRMRB;
	private JRadioButton customRMRB;
	private JRadioButton videoRB;
	private JRadioButton audioRB;
	
	private JRadioButton absFilePathRB;
	private JRadioButton relFilePathRB;
	
	private JButton typButton;
	
	private JComboBox recordMarkerCB;
	
	private JPanel outputOptionsPanel;
	private JPanel toolboxOptionsPanel;
	
	private JScrollPane outerScrollPane;
	
	List<String> recordMarkerList = null;
	
	/** default line width */
    private final int NUM_CHARS = 80;
    
    Insets insets = new Insets(4, 6, 4, 6);
    Insets vertInsets = new Insets(0, 2, 2, 2);
    Insets leftVertIndent = new Insets(0, 16, 2, 2);
    Insets innerInsets = new Insets(4, 2, 4, 2);
    
    private boolean moveForward = true;
	
    /**
     * Constructor
     * 
     * @param multiPane
     */
	public MultipleFileToolBoxExportStep2(MultiStepPane multiPane) {
		super(multiPane);
		initComponents();		
	}
	
	/**
	 * Initialize the ui components
	 */
	@Override
	protected void initComponents(){
		initOutputOptionsPanel();		
		initToolboxOptionsPanel();
		
		 JPanel outerPanel = new JPanel();
	     outerPanel.setLayout(new GridBagLayout());
	     outerScrollPane = new JScrollPane(outerPanel);
	     outerScrollPane.setBorder(null);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		Insets globalInset = new Insets(5, 10, 5, 10);		
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = globalInset;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		outerPanel.add(outputOptionsPanel, gbc);		
	
		gbc.gridy = 1;
		outerPanel.add(toolboxOptionsPanel, gbc);
		
		gbc.gridy = 2;	
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		outerPanel.add(new JPanel(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
        add(outerScrollPane, gbc);           
	
		setDefaultNumOfChars();

	    setShoeboxMarkerRB();

	    loadPreferences();
	    
	    TextFieldHandler tfHandler = new TextFieldHandler();     
	    numCharTF.addKeyListener(tfHandler);
    	typField.addKeyListener(tfHandler);
    	dbTypField.addKeyListener(tfHandler);
    	markerTF.addKeyListener(tfHandler);
    	mediaMarkerNameTF.addKeyListener(tfHandler);
    	
    	mediaMarkerCB.addItemListener(this);
        customRMRB.addItemListener(this);
        defaultRMRB.addItemListener(this);
        detectedRMRB.addItemListener(this);
        specRB.addItemListener(this);
		typeRB.addItemListener(this);
	}
	
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("MultiFileExportToolbox.Title.Step2Title");
	}	
	
	
	
	@Override
	public void enterStepForward(){
		
		boolean repaint = false;
		
		detectedRMRB.setEnabled(false);
     	defaultRMRB.setSelected(true);
     	detectedRMRB.setText(ElanLocale.getString(
         		"ExportShoebox.Label.Detected"));
     	if(recordMarkerCB != null){
     		toolboxOptionsPanel.remove(recordMarkerCB); 
     		recordMarkerCB = null;
     		
     		repaint = true;
     	}
		
		recordMarkerList = (List<String>) multiPane.getStepProperty("RecordMarkersList");	
    	if(recordMarkerList != null && recordMarkerList.size() > 0){
    		updateDetectedRecordMarker();
    		
    		repaint = true;
        } 	
			
		mediaMarkerCB.setSelected(false);
		mediaMarkerCB.setEnabled(false);
		
		boolean mediaDetected = (Boolean) multiPane.getStepProperty("EnableMediaMarker");			
		if (mediaDetected){
			mediaMarkerCB.setEnabled(mediaDetected);
			mediaMarkerCB.setSelected(mediaDetected);
			
			Boolean bothMediaDetected = (Boolean) multiPane.getStepProperty("BothMediaDetected");	
			if(!bothMediaDetected){
				audioRB.setEnabled(false);
				videoRB.setEnabled(false);
				mediaTypeLabel.setEnabled(false);
			}		
			repaint = true;
		} 
		
		if(repaint){
			repaint();
		}
			
		moveForward = true;
	    updateButtonStates();		
	}			
	
	/**
	 * Updates the button states
	 */
	public void updateButtonStates(){
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, moveForward);		
	}	
	
	private void checkInputFields(){
		moveForward = true;
		if(wrapBlocksCB.isSelected()){
			if(numCharTF.getText() == null || 
				numCharTF.getText().trim().length() <= 0){
				charPerLineLabel.setForeground(Color.RED);
				moveForward = false;
			} else {
				charPerLineLabel.setForeground(Color.BLACK);
			}
		} else {
			charPerLineLabel.setForeground(Color.BLACK);
		}
		
		databaseErrorLabel.setText("");
		
		if(typeRB.isSelected()){
			if(typField.getText() == null || 
					typField.getText().trim().length() <= 0){
				databaseErrorLabel.setText("- " + ElanLocale.getString("ImportDialog.Message.SpecifyType"));
				moveForward = false;
			}
		} else if(specRB.isSelected()){
			if(dbTypField.getText() == null || 
					dbTypField.getText().trim().length() <= 0){
				databaseErrorLabel.setText("- " + ElanLocale.getString("ExportShoebox.Message.NoType"));
				moveForward = false;
			} 
		} 
		
		recordMarkerErrorLabel.setText("");
		if(customRMRB.isSelected()){
			if(markerTF.getText() == null || 
				markerTF.getText().trim().length() <= 0){
				recordMarkerErrorLabel.setText("- "+ ElanLocale.getString("ExportShoebox.Message.NoRecordMarker"));
				moveForward = false;
			}
		}
		
		if(mediaMarkerCB.isSelected()){
			if(mediaMarkerNameTF.getText() == null || 
				mediaMarkerNameTF.getText().trim().length() <= 0){
				if(recordMarkerErrorLabel.getText().trim().length() > 0){
					recordMarkerErrorLabel.setText(recordMarkerErrorLabel.getText() + " "+ ElanLocale.getString("ExportShoebox.Message.NoMediaMarker"));
				} else {
					recordMarkerErrorLabel.setText("- "+ ElanLocale.getString("ExportShoebox.Message.NoMediaMarker"));
				}
				moveForward = false;
			}
		}
	}
		
	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {      	
    	// validate inputs	
    	checkInputFields();
    	
    	if(!moveForward){
    		updateButtonStates();
    		return false;
    	}
    	
    	
    	int charsPerLine = Integer.MAX_VALUE;
    	
    	if (wrapBlocksCB.isSelected()) {
            String textValue = numCharTF.getText().trim();
            try {
                charsPerLine = Integer.parseInt(textValue);
            } catch (NumberFormatException nfe) {
            	JOptionPane.showMessageDialog(this, ElanLocale.getString("ExportShoebox.Message.InvalidNumber"),
                        ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
                numCharTF.selectAll();
                numCharTF.requestFocus();

                return false;
            }
        }    
    	
    	String databaseType = null;
    	
    	if (typeRB.isSelected()) {
            File tf = new File(typField.getText());

            if (!tf.exists()) {
            	JOptionPane.showMessageDialog(this, ElanLocale.getString("ImportDialog.Message.NoType"),
                        ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);

                return false;
            } else {
                try {
                    ShoeboxTypFile typFile = new ShoeboxTypFile(tf);
                    databaseType = typFile.getDatabaseType();
                } catch (Exception e) {
                }
            }
        } else {
            databaseType = dbTypField.getText();
        }
    	
    	int timeFormat = Interlinear.SSMS;

        if (hhMMSSMSFormatRB.isSelected()) {
            timeFormat = Interlinear.HHMMSSMS;
        }
        
        String recordMarker = null;
        
        // record marker test
        if (customRMRB.isSelected()) {
        	recordMarker = markerTF.getText().trim();
        } else if (defaultRMRB.isSelected()) {
        	recordMarker = "block"; 
        } else {
        	if(recordMarkerList.size() == 1){
        		recordMarker = recordMarkerList.get(0);
        	}else {
        		recordMarker = recordMarkerCB.getSelectedItem().toString();
        	}
        }
    	
    	//store the values 
    	multiPane.putStepProperty("CharsPerLine", charsPerLine);		
    	multiPane.putStepProperty("TimeFormat", timeFormat);    	
    	multiPane.putStepProperty("CorrectTimes", correctTimesCB.isSelected());
    	multiPane.putStepProperty("TypeFileSelected", typeRB.isSelected());
    	multiPane.putStepProperty("DatabaseType", databaseType);
    	multiPane.putStepProperty("WrapLines", wrapLinesCB.isSelected());
    	multiPane.putStepProperty("WrapNextLine", wrapNextLineRB.isSelected());
    	multiPane.putStepProperty("IncludeEmptyLines", includeEmptyLinesCB.isSelected());
    	multiPane.putStepProperty("UseDetectedRecordMarker", detectedRMRB.isSelected());
    	multiPane.putStepProperty("AppendFileNameWithRecordMarker", appendFileNameCB.isSelected());
    	multiPane.putStepProperty("RecordMarker", recordMarker);
    	multiPane.putStepProperty("IncludeMediaMarkerCB", mediaMarkerCB.isSelected());
    	String mediaMarkerName = mediaMarkerNameTF.getText();
    	if(mediaMarkerName != null){
    		multiPane.putStepProperty("MediaMarkerName", mediaMarkerName.trim());
    	} 	
    	multiPane.putStepProperty("AudiofileType", audioRB.isSelected());
    	multiPane.putStepProperty("UseRelFilePath", relFilePathRB.isSelected());
    	
    	savePreferences();         
   		return true;       
    }    
    
    /**
     * Updates the detected record marker label
     */
    private void updateDetectedRecordMarker(){
    	detectedRMRB.setEnabled(true);
   		detectedRMRB.setSelected(true);
    	if(recordMarkerList.size() == 1){
       		detectedRMRB.setText(detectedRMRB.getText() + 
         		" (\\" + recordMarkerList.get(0) + ")");         		
       	 } else if(recordMarkerList.size() > 1){
       		 recordMarkerCB = new JComboBox();
       		 for(String marker: recordMarkerList){
       			 recordMarkerCB.addItem(marker);
       		 }        		 
       		 recordMarkerCB.setSelectedIndex(0);
       		 
       		GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 2, 2, 2);
            toolboxOptionsPanel.add(recordMarkerCB, gridBagConstraints);            
       	 }    	
    }
	
	/**
	 * Initializes the output options
	 */
	private void initOutputOptionsPanel(){			
		//panel
		outputOptionsPanel = new JPanel(new GridBagLayout());
		outputOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                "ExportDialog.Label.Options")));
		
		//create all components
		wrapBlocksCB = new JCheckBox(ElanLocale.getString(
                "ExportShoebox.Label.WrapBlocks"));	
		wrapBlocksCB.setSelected(true);
		wrapBlocksCB.addItemListener(this);
		
		charPerLineLabel = new JLabel(ElanLocale.getString(
                "ExportShoebox.Label.NumberChars"));    
		
		numCharTF = new JTextField(4);
		numCharTF.setEnabled(false);
        numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
       
        JLabel timeFormatLabel = new JLabel(ElanLocale.getString(
                "ExportShoebox.Label.Format"));
		
		ssMSFormatRB = new JRadioButton(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.Seconds"));
		ssMSFormatRB.setSelected(true);
		
		hhMMSSMSFormatRB = new JRadioButton(ElanLocale.getString(
                "InterlinearizerOptionsDlg.TimeCodeFormat.TimeCode"));
       
		correctTimesCB = new JCheckBox(ElanLocale.getString("ExportDialog.CorrectTimes"));
       
		wrapLinesCB = new JCheckBox(ElanLocale.getString(
                "ExportShoebox.Label.WrapLines"));
		wrapLinesCB.setSelected(true);    
		wrapLinesCB.addItemListener(this);
       
		wrapNextLineRB = new JRadioButton(ElanLocale.getString(
        		"ExportShoebox.Label.WrapNextLine"));
		wrapNextLineRB.setSelected(true);
		
		wrapAfterBlockRB = new JRadioButton(ElanLocale.getString(
                "ExportShoebox.Label.WrapEndOfBlock"));
		
		includeEmptyLinesCB = new JCheckBox(ElanLocale.getString(
        		"ExportShoebox.Label.IncludeEmpty"));
		includeEmptyLinesCB.setSelected(true);
		
		//add radio buttons to button group
		ButtonGroup timeGroup = new ButtonGroup();
		timeGroup.add(ssMSFormatRB);
	    timeGroup.add(hhMMSSMSFormatRB);
	        
	    ButtonGroup wrapGroup = new ButtonGroup();
	    wrapGroup.add(wrapNextLineRB);
	    wrapGroup.add(wrapAfterBlockRB);	
	    
	    GridBagConstraints gridBagConstraints;
	    //wrap panel
	    JPanel wrapPanel = new JPanel(new GridBagLayout());
      
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        wrapPanel.add(wrapBlocksCB, gridBagConstraints);
     
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(numCharTF, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = vertInsets;
        wrapPanel.add(charPerLineLabel, gridBagConstraints);
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        wrapPanel.add(wrapLinesCB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        wrapPanel.add(wrapNextLineRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        gridBagConstraints.gridwidth = 2;
        wrapPanel.add(wrapAfterBlockRB, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        wrapPanel.add(includeEmptyLinesCB, gridBagConstraints);
        
        //time panel
        JPanel timePanel = new JPanel(new GridBagLayout());
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        timePanel.add(timeFormatLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        timePanel.add(hhMMSSMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = leftVertIndent;
        timePanel.add(ssMSFormatRB, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = innerInsets;
        timePanel.add(correctTimesCB, gridBagConstraints);
        
        // add to options panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        outputOptionsPanel.add(wrapPanel, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        outputOptionsPanel.add(new JPanel(), gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        outputOptionsPanel.add(timePanel, gridBagConstraints);
	}	
	
	/**
	 * Initializes the toolbox options
	 * @param mediaMarkerCB 
	 */
	private void initToolboxOptionsPanel(){
		//panel
		toolboxOptionsPanel = new JPanel(new GridBagLayout());
		toolboxOptionsPanel.setBorder(new TitledBorder(ElanLocale.getString(
                "ExportShoebox.Label.ToolboxOptions")));
		
		//initialize components
		 JLabel toolboxDBTypeLabel = new JLabel(ElanLocale.getString(
	        		"ExportShoebox.Label.ToolboxBDName"));
		 toolboxDBTypeLabel.setToolTipText("e.g. \\_sh v3.0  400 Text");
		 
		 databaseErrorLabel = new JLabel();
		 databaseErrorLabel.setForeground(Color.RED);
		 
		 typeRB = new JRadioButton(ElanLocale.getString("ExportShoebox.Label.Type"));
	     typeRB.setSelected(true);	    
	     
	     typField = new JTextField("", 23);
	     
		 typButton = new JButton("...");
		 typButton.addActionListener(this);
		 
	     specRB = new JRadioButton(ElanLocale.getString(
	                "ExportShoebox.Label.SpecifyType"));
	     
         dbTypField = new JTextField("", 14);
         
         JLabel recordMarkerLabel = new JLabel(ElanLocale.getString(
         		"ExportShoebox.Label.RecordMarker"));
         
         recordMarkerErrorLabel = new JLabel();
         recordMarkerErrorLabel.setForeground(Color.RED);
         
         detectedRMRB = new JRadioButton(ElanLocale.getString(
         		"ExportShoebox.Label.Detected"));
         detectedRMRB.setSelected(true);
        
         
         defaultRMRB = new JRadioButton(ElanLocale.getString(
         		"ExportShoebox.Label.DefaultMarker") + " (\\block)");
        
         
         customRMRB = new JRadioButton(ElanLocale.getString(
         		"ExportShoebox.Label.CustomMarker"));
         
         
         markerTF = new JTextField("", 6);
         markerTF.setEnabled(false);
         
         appendFileNameCB = new JCheckBox(ElanLocale.getString("MultiFileExportToolbox.AppendFileName"));
         
         mediaMarkerCB = new JCheckBox(ElanLocale.getString("ExportShoebox.Label.IncludeMediaMarker"));
         
         audioRB = new JRadioButton(ElanLocale.getString("MultiFileExportToolbox.useAudioFile"));
  		 audioRB.setSelected(true);
  		 audioRB.addItemListener(this);
  		
  		 videoRB = new JRadioButton(ElanLocale.getString("MultiFileExportToolbox.useVideoFile"));
  		 videoRB.addItemListener(this);
         
         absFilePathRB = new JRadioButton(ElanLocale.getString("ExportShoebox.Label.AbsoluteMediaFile"));
         absFilePathRB.setSelected(true);
         
         relFilePathRB = new JRadioButton(ElanLocale.getString("ExportShoebox.Label.RelMediaFile"));
         
         mediaMarkerNameLabel = new JLabel(ElanLocale.getString("ExportShoebox.Label.MediaMarkerName"));
         
         mediaTypeLabel = new JLabel(ElanLocale.getString("MultiFileExportToolbox.SelectMediaType"));
         
         mediaMarkerNameTF = new JTextField("", 6);
         mediaMarkerNameTF.setEnabled(false);   

         ButtonGroup mediaGroup = new ButtonGroup();
  		 mediaGroup.add(videoRB);
  		 mediaGroup.add(audioRB);
  		
         ButtonGroup buttonGroup = new ButtonGroup();
         buttonGroup.add(typeRB);
         buttonGroup.add(specRB);       
        
         ButtonGroup rmGroup = new ButtonGroup();
         rmGroup.add(detectedRMRB);
         rmGroup.add(defaultRMRB);
         rmGroup.add(customRMRB);
         
         ButtonGroup fileGroup = new ButtonGroup();
         fileGroup.add(absFilePathRB);
         fileGroup.add(relFilePathRB);         
        
         int y = 0;
         GridBagConstraints gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;        
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = insets;
         toolboxOptionsPanel.add(toolboxDBTypeLabel, gridBagConstraints);
                 
         gridBagConstraints.gridx = 1;  
         gridBagConstraints.insets = vertInsets;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;       
         toolboxOptionsPanel.add(databaseErrorLabel, gridBagConstraints);         
        
         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;      
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(typeRB, gridBagConstraints);

         gridBagConstraints.gridx = 1;        
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(typField, gridBagConstraints);

         gridBagConstraints.gridx = 2;        
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.weightx = 0.0;
         toolboxOptionsPanel.add(typButton, gridBagConstraints);

         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(specRB, gridBagConstraints);

         gridBagConstraints.gridx = 1;         
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;         
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(dbTypField, gridBagConstraints);
         
         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = insets;
         toolboxOptionsPanel.add(recordMarkerLabel, gridBagConstraints);
         
         gridBagConstraints.gridx = 1;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(recordMarkerErrorLabel, gridBagConstraints);  
         
         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;         
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(detectedRMRB, gridBagConstraints);
         
         y++;
         gridBagConstraints.gridy = y;         
         toolboxOptionsPanel.add(defaultRMRB, gridBagConstraints);
        
         y++;
         gridBagConstraints.gridy = y;      
         toolboxOptionsPanel.add(customRMRB, gridBagConstraints);
         
         gridBagConstraints.gridx = 1;
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(markerTF, gridBagConstraints);
         
         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;    
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(appendFileNameCB, gridBagConstraints);            
        
         // add media marker elements
         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;   
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(mediaMarkerCB, gridBagConstraints);            
         
         y++;
         gridBagConstraints.gridy = y;         
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(mediaMarkerNameLabel, gridBagConstraints);
         
         gridBagConstraints.gridx = 1;  
         gridBagConstraints.insets = vertInsets;
         toolboxOptionsPanel.add(mediaMarkerNameTF, gridBagConstraints);
         
         y++;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = y;
         gridBagConstraints.fill = GridBagConstraints.NONE;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.insets = leftVertIndent;
         toolboxOptionsPanel.add(mediaTypeLabel, gridBagConstraints);  
         
         JPanel mediaPanel = new JPanel(new GridLayout(1, 2));
         mediaPanel.add(audioRB);
         mediaPanel.add(videoRB);
         
         y++;
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = y;        
         gridBagConstraints.insets = innerInsets;
         toolboxOptionsPanel.add(mediaPanel, gridBagConstraints);  	
         
         JPanel fileNamePanel = new JPanel(new GridLayout(1, 2));
         fileNamePanel.add(absFilePathRB);
         fileNamePanel.add(relFilePathRB);         

         y++;         
         gridBagConstraints.gridy = y;        
         toolboxOptionsPanel.add(fileNamePanel, gridBagConstraints); 
         
//         mediaMarkerCB.addItemListener(this);
//         customRMRB.addItemListener(this);
//         defaultRMRB.addItemListener(this);
//         detectedRMRB.addItemListener(this);
//         specRB.addItemListener(this);
// 		   typeRB.addItemListener(this);
	}
	
	/**
	 * sets the value in the
	 * numCharTF 
	 */
	private void setDefaultNumOfChars() {
        numCharTF.setEnabled(true);
        numCharTF.setBackground(Constants.SHAREDCOLOR4);

        if ((numCharTF.getText() != null) ||
                (numCharTF.getText().length() == 0)) {
            numCharTF.setText("" + NUM_CHARS);
        }
    }
	
	private void setShoeboxMarkerRB() {
        String stringPref = Preferences.getString("LastUsedShoeboxExport", null);

        if (stringPref == null || stringPref.equalsIgnoreCase("typ")) {
            typeRB.setSelected(true);

            String luTypFile = Preferences.getString("LastUsedShoeboxTypFile", null);

            if (luTypFile != null) {
                typField.setText((String) luTypFile);
            }
            enableTypComponents(true);
        } else {
        	specRB.setSelected(true);
        	enableTypComponents(false);
        }
    }
	
	 private void enableTypComponents(boolean enable) { 
	    	typField.setEnabled(enable);
	    	typButton.setEnabled(enable);
	    	dbTypField.setEnabled(!enable);
	    }
	
	private void chooseTyp() {      
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("ImportDialog.Approve"), 
        		null, FileExtension.SHOEBOX_TYP_EXT, false, "LastUsedShoeboxTypDir", FileChooser.FILES_ONLY, null);
        File f = chooser.getSelectedFile();
        if (f != null) {
            typField.setText(f.getAbsolutePath());
        }
    }

	/**
     * The item state changed handling.
     *
     * @param ie the ItemEvent
     */
    @Override
	public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == wrapBlocksCB) {
            if (wrapBlocksCB.isSelected()) {
                setDefaultNumOfChars();
                numCharTF.requestFocus();
                wrapLinesCB.setEnabled(true);
            	wrapNextLineRB.setEnabled(wrapLinesCB.isSelected());
            	wrapAfterBlockRB.setEnabled(wrapLinesCB.isSelected());
            } else {
                numCharTF.setEnabled(false);
                numCharTF.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
                wrapLinesCB.setEnabled(false);
            	wrapNextLineRB.setEnabled(false);
            	wrapAfterBlockRB.setEnabled(false);
            }
        } else if (ie.getSource() == wrapLinesCB) {
        	wrapNextLineRB.setEnabled(wrapLinesCB.isSelected());
        	wrapAfterBlockRB.setEnabled(wrapLinesCB.isSelected());
        } 
        else if (ie.getSource() == typeRB) {
        	enableTypComponents(true);
        	checkInputFields();
			updateButtonStates();
        } else if (ie.getSource() == specRB) {
        	enableTypComponents(false);
        	checkInputFields();
			updateButtonStates();
        	dbTypField.requestFocus();
        } else if (ie.getSource() == detectedRMRB || ie.getSource() == defaultRMRB) {
        	markerTF.setEnabled(false);
        } else if (ie.getSource() == customRMRB) {
        	markerTF.setEnabled(true);
        	markerTF.requestFocus();
        } else if (ie.getSource() == mediaMarkerCB) {
        	mediaMarkerNameTF.setEnabled(mediaMarkerCB.isSelected());
        	absFilePathRB.setEnabled(mediaMarkerCB.isSelected());
        	relFilePathRB.setEnabled(mediaMarkerCB.isSelected());
        	audioRB.setEnabled(mediaMarkerCB.isSelected());
    		videoRB.setEnabled(mediaMarkerCB.isSelected());
    		mediaTypeLabel.setEnabled(mediaMarkerCB.isSelected());  
         	mediaMarkerNameLabel.setEnabled(mediaMarkerCB.isSelected());  
        }
        
        updateButtonStates();
    }
    
    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == typButton) {
            chooseTyp();
            typeRB.setSelected(true);
        } 
    }
    
    private void loadPreferences(){
    	Boolean boolPref;
    	String stringPref;
    	
    	boolPref = Preferences.getBool("ExportToolbox.WrapBlocks", null);
    	if(boolPref != null){
    		wrapBlocksCB.setSelected(boolPref);
    	}
    	
    	stringPref = Preferences.getString("ExportShoebox.numCharTF", null);
    	if(stringPref != null){
    		numCharTF.setText(stringPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportShoebox.wrapLinesCB", null);
    	if(boolPref != null){
    		wrapLinesCB.setSelected(boolPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.wrapNextLineRB", null);
    	if(boolPref != null){
    		wrapNextLineRB.setSelected(boolPref);
    		wrapAfterBlockRB.setSelected(!boolPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.includeEmptyLinesCB", null);
    	if(boolPref != null){
    		includeEmptyLinesCB.setSelected(boolPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.ssMSFormatRB", null);
    	if(boolPref != null){
    		ssMSFormatRB.setSelected(boolPref);
    		hhMMSSMSFormatRB.setSelected(!boolPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.correctTimesCB", null);
    	if(boolPref != null){
    		correctTimesCB.setSelected(boolPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.detectedRMRB", null);
    	if(boolPref != null){
    		detectedRMRB.setSelected(boolPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.defaultRMRB", null);
    	if(boolPref != null){
    		defaultRMRB.setSelected(boolPref);
    	}
    	
    	if(defaultRMRB.isSelected() || detectedRMRB.isSelected()){
    		customRMRB.setSelected(false);
    	}else {
    		customRMRB.setSelected(true);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.AppendFileNameToRecordMarker", null);
    	if (boolPref != null) {
    		appendFileNameCB.setSelected(boolPref);
    	}
    	
    	stringPref = Preferences.getString("ExportToolbox.markerTF", null);
    	if(stringPref != null){
    		markerTF.setText(stringPref);
    	}    	
    	
    	stringPref = Preferences.getString("ExportToolbox.ManualDBName", null);
    	if (stringPref != null) {
    		dbTypField.setText(stringPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.exportMediaMarker", null);
    	if (boolPref != null) {
    		mediaMarkerCB.setSelected(boolPref);// will this fire an event?
    		mediaMarkerNameTF.setEnabled(mediaMarkerCB.isSelected());
    		absFilePathRB.setEnabled(mediaMarkerCB.isSelected());
    		relFilePathRB.setEnabled(mediaMarkerCB.isSelected());   
    		audioRB.setEnabled(mediaMarkerCB.isSelected());
         	videoRB.setEnabled(mediaMarkerCB.isSelected());  
         	mediaTypeLabel.setEnabled(mediaMarkerCB.isSelected());  
         	mediaMarkerNameLabel.setEnabled(mediaMarkerCB.isSelected());  
    	}
    	
    	stringPref = Preferences.getString("ExportToolbox.mediaMarkerName", null);
    	if(stringPref != null){
    		mediaMarkerNameTF.setText(stringPref);
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.absoluteMediaFileName", null);
    	if (boolPref != null) {
    		absFilePathRB.setSelected(boolPref);
    		relFilePathRB.setSelected(!((Boolean) boolPref));
    	}
    	
    	boolPref = Preferences.getBool("ExportToolbox.useAudioFile", null);
 		if (boolPref != null) { 			
 			videoRB.setSelected(boolPref);
 			videoRB.setSelected(!((Boolean) boolPref)); 			
 		} 		
    }
    
    private void savePreferences(){
    	Preferences.set("ExportToolbox.WrapBlocks", wrapBlocksCB.isSelected(), null);
    	Preferences.set("ExportToolbox.CharacterPerBlocks", numCharTF.getText(), null);
    	Preferences.set("ExportToolbox.wrapLinesCB", wrapLinesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.wrapNextLineRB", wrapNextLineRB.isSelected(), null);
    	Preferences.set("ExportToolbox.includeEmptyLinesCB", includeEmptyLinesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.ssMSFormatRB", ssMSFormatRB.isSelected(), null);
    	Preferences.set("ExportToolbox.correctTimesCB", correctTimesCB.isSelected(), null);
    	Preferences.set("ExportToolbox.detectedRMRB", detectedRMRB.isSelected(), null);
    	Preferences.set("ExportToolbox.defaultRMRB", defaultRMRB.isSelected(), null);
    	Preferences.set("ExportToolbox.markerTF", markerTF.getText(), null); 
    	Preferences.set("ExportToolbox.AppendFileNameToRecordMarker", appendFileNameCB.isSelected(), null);  
   
    	if (specRB.isSelected()) {
    		Preferences.set("ExportToolbox.ManualDBName", dbTypField.getText(), null);
    	}
    	Preferences.set("ExportToolbox.exportMediaMarker", mediaMarkerCB.isSelected(), null);
    	
    	if (mediaMarkerCB.isSelected()) {
    		Preferences.set("ExportToolbox.mediaMarkerName", mediaMarkerNameTF.getText(), null);    		
    		Preferences.set("ExportToolbox.absoluteMediaFileName", absFilePathRB.isSelected(), null);  
    		if(audioRB != null){
    			Preferences.set("ExportToolbox.useAudioFile", audioRB.isSelected(), null);
    		}    		
    	}
    	
    	if (typeRB.isSelected()) {
            Preferences.set("LastUsedShoeboxExport", "typ", null);
            Preferences.set("LastUsedShoeboxTypFile", typField.getText(), null);
        } else {
            Preferences.set("LastUsedShoeboxExport", "", null);
        }
    }
    
	private class TextFieldHandler implements KeyListener{
		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			checkInputFields();
			updateButtonStates();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			checkInputFields();
			updateButtonStates();
		}
		
	}
}
