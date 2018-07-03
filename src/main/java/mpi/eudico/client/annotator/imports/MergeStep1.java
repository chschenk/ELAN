package mpi.eudico.client.annotator.imports;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;

/**
 * The first step of a merge process. Two sources can be specified and a destination
 * (file name) for the merged transcriptions. The first source (the transcription 
 * tiers are going to be added to) can either be a file name or a transcription loaded
 * in ELAN.
 * 
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MergeStep1 extends StepPane implements ActionListener,
    ItemListener {
    private TranscriptionImpl curTranscription;
    private JButton browseSource1;
    private JButton browseSource2;
    private JButton browseDest;    
    
    private JTextField source1Field;
    private JTextField source2Field;
    private JTextField destField;
    private JTextField timeFrameField;
    private JCheckBox curTransCB;
    private JCheckBox appendAnnCB;    
    private JRadioButton appendWitMediaDurRB;
    private JRadioButton appendWithLastAnnRB;
    private JRadioButton appendWithGivenTimeRB;   
    
    private JPanel firstSourcePanel;
    private JPanel secSourcePanel;  
    private JPanel appendOptionsPanel;  
    private JPanel desSourcePanel;
    
    private JCheckBox addLinkedFilesCB;    
    
    private boolean src1Ready = false;
    private boolean src2Ready = false;
    private boolean destReady = false;
    
    private long timeFrame = 0L;
   
    /**
     * Constructor.
     *
     * @param multiPane the enclosing MultiStepPane
     */
    public MergeStep1(MultiStepPane multiPane) {
        super(multiPane);

        initComponents();
    }

    /**
     * Constructor.
     *
     * @param multiPane the enclosing MultiStepPane
     * @param curTranscription a loaded transcription
     */
    public MergeStep1(MultiStepPane multiPane,
        TranscriptionImpl curTranscription) {
        super(multiPane);
        this.curTranscription = curTranscription;

        initComponents();
    }

    /**
     * Initializes ui components.
     */
    @Override
	public void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        browseSource1 = new JButton(ElanLocale.getString("Button.Browse"));
        browseSource2 = new JButton(ElanLocale.getString("Button.Browse"));
        browseDest = new JButton(ElanLocale.getString("Button.Browse"));
        source1Field = new JTextField();
        source2Field = new JTextField();
        destField = new JTextField();
        timeFrameField = new JTextField();
        timeFrameField.setEnabled(false);
        timeFrameField.setEditable(true);
        
        source1Field.setEditable(false);
        source1Field.setEnabled(false);
        source2Field.setEditable(false);
        source2Field.setEnabled(false);
        destField.setEditable(false);
        destField.setEnabled(false);
        
        curTransCB = new JCheckBox(ElanLocale.getString(
        		    		"MergeTranscriptionDialog.Label.UseCurrent"));
        appendAnnCB = new JCheckBox(ElanLocale.getString(
	    					"MergeTranscriptionDialog.Label.AppendAnnotation"));    
        appendAnnCB.addItemListener(this);
        appendWitMediaDurRB = new JRadioButton(ElanLocale.getString(
									"MergeTranscriptionDialog.Label.AppendWithCurMediaDur"));
        appendWitMediaDurRB.setEnabled(false);
        appendWithLastAnnRB = new JRadioButton(ElanLocale.getString(
									"MergeTranscriptionDialog.Label.AppendWithLastAnn"));
        appendWithLastAnnRB.setSelected(true);
        appendWithLastAnnRB.setEnabled(false);
        appendWithGivenTimeRB = new JRadioButton(ElanLocale.getString(
									"MergeTranscriptionDialog.Label.AppendWithGivenTime"));
        appendWithGivenTimeRB.setEnabled(false);
        appendWithGivenTimeRB.addItemListener(this);        
      
        addLinkedFilesCB = new JCheckBox(ElanLocale.getString(
				"MergeTranscriptionDialog.Label.AddLinkedFiles"));    

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        
        firstSourcePanel = new JPanel();
        firstSourcePanel.setLayout(new GridBagLayout());
        firstSourcePanel.setBorder(new TitledBorder(ElanLocale.getString("MergeTranscriptionDialog.Label.Source1")));
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = insets;  
        
        if (curTranscription != null) {
            curTransCB.setSelected(true);
            source1Field.setText(curTranscription.getFullPath());
            browseSource1.setEnabled(false);
            src1Ready = true;

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            firstSourcePanel.add(curTransCB, gbc);
            
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.WEST;
            firstSourcePanel.add(source1Field, gbc);

            gbc.gridx = 2;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0;
            firstSourcePanel.add(browseSource1, gbc);

            curTransCB.addItemListener(this);
        } else {
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            firstSourcePanel.add(source1Field, gbc);

            gbc.gridx = 2;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0;
            firstSourcePanel.add(browseSource1, gbc);
        }
        
        appendOptionsPanel = new JPanel();
        appendOptionsPanel.setLayout(new GridBagLayout());    
                       
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;   
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        if (curTranscription != null) {          	 
            appendOptionsPanel.add(appendWitMediaDurRB, gbc);   
            
            gbc.gridy = 1; 
            appendOptionsPanel.add(appendWithLastAnnRB, gbc);
            
            gbc.gridy = 2; 
            appendOptionsPanel.add(appendWithGivenTimeRB, gbc);   
            
            gbc.gridx = 1;
            appendOptionsPanel.add(timeFrameField, gbc);  
        } else {
        	appendOptionsPanel.add(appendWithLastAnnRB, gbc);
        	
        	gbc.gridy = 1; 
            appendOptionsPanel.add(appendWithGivenTimeRB, gbc);   
            
            gbc.gridx = 1;           
            appendOptionsPanel.add(timeFrameField, gbc);  
        }
        
        
        secSourcePanel = new JPanel();
        secSourcePanel.setLayout(new GridBagLayout());
        secSourcePanel.setBorder(new TitledBorder(ElanLocale.getString("MergeTranscriptionDialog.Label.Source2")));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = insets;  
        secSourcePanel.add(source2Field, gbc);        
        
    	gbc.gridx = 2;    	
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        secSourcePanel.add(browseSource2, gbc);
        
        gbc.gridx = 0;   
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        secSourcePanel.add(appendAnnCB, gbc);
        
        gbc.gridy = 3;
        secSourcePanel.add(addLinkedFilesCB, gbc);
        
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2,22,2,4); 
        secSourcePanel.add(appendOptionsPanel, gbc);
        
        ButtonGroup group = new ButtonGroup();
        group.add(appendWitMediaDurRB);
        group.add(appendWithGivenTimeRB);
        group.add(appendWithLastAnnRB);
        
        desSourcePanel = new JPanel();
        desSourcePanel.setLayout(new GridBagLayout());
        desSourcePanel.setBorder(new TitledBorder(ElanLocale.getString("MergeTranscriptionDialog.Label.Destination")));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;      
        gbc.insets = insets;
        gbc.gridwidth = 2;
        desSourcePanel.add(destField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        desSourcePanel.add(browseDest, gbc);
        

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = insets;
        add(firstSourcePanel, gbc);    
        
        gbc.gridy = 1;        
        add(secSourcePanel, gbc);  
        
        gbc.gridy = 2;        
        add(desSourcePanel, gbc); 

        browseSource1.addActionListener(this);
        browseSource2.addActionListener(this);
        browseDest.addActionListener(this);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("MergeTranscriptionDialog.Title");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        // the next button is already disabled
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
    }

    /**
     * Before we can leave this step we have to have two valid Transcription
     * objects or paths and the path to the destination file. Store all of
     * these in the properties map.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        //store
    	
    	if(appendWithGivenTimeRB.isSelected()){    
    		multiPane.putStepProperty("givenTimeFrame", timeFrame); 
    		}  	
    	
        if (curTransCB.isSelected()) {
            multiPane.putStepProperty("Source1", curTranscription);          
        } else {
            multiPane.putStepProperty("Source1", source1Field.getText());            
        }
        
        if(appendAnnCB.isSelected()){
        	multiPane.putStepProperty("appendAnnsWithMedia", appendWitMediaDurRB.isSelected());
        	multiPane.putStepProperty("appendAnnsWithGivenTime", appendWithGivenTimeRB.isSelected());
        	multiPane.putStepProperty("appendAnnsLastAnns", appendWithLastAnnRB.isSelected());
        } else {
        	multiPane.putStepProperty("appendAnnsWithMedia", false);
        	multiPane.putStepProperty("appendAnnsWithGivenTime", false);
        	multiPane.putStepProperty("appendAnnsLastAnns", false);
        }
        
        multiPane.putStepProperty("AddLinkedFiles", addLinkedFilesCB.isSelected());
        
        multiPane.putStepProperty("Source2", source2Field.getText());
        multiPane.putStepProperty("Destination", destField.getText());

        return true;
    }

    /**
     * Prompts the user to browse to a valid .eaf file.
     *
     * @return a String representation of a file
     */
    private String getOpenFileName(String locPrefString) { 
        FileChooser chooser = new FileChooser(this);   
        chooser.createAndShowFileDialog(ElanLocale.getString("MergeTranscriptionDialog.SelectEAF"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, FileExtension.EAF_EXT, false, locPrefString, FileChooser.FILES_ONLY, null);

        File eafFile = chooser.getSelectedFile();
        if (eafFile != null) {
        	return eafFile.getAbsolutePath();
        }           
        
        return null;
    }
        
    	

    /**
     * Prompts the user to provide a filepath for a new .eaf file.
     *
     * @return a String representation of a file
     */
    private String getSaveFileName(String locPrefString) {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("MergeTranscriptionDialog.SelectEAF"), FileChooser.SAVE_DIALOG, 
        		 FileExtension.EAF_EXT,  locPrefString);

        File eafFile = chooser.getSelectedFile();
        
        if (eafFile != null) {
              return eafFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Checks if a filename points to an exisitng .eaf file.
     *
     * @param fileName a String representation of a file
     *
     * @return true if the file exists and is an .eaf, false otherwise
     */
    private boolean isExistingEAF(String fileName) {
        if (fileName == null) {
            return false;
        }

        File f = new File(fileName);

        if (!f.exists()) {
            return false;
        }

        return validEAFName(fileName);
    }

    /**
     * Checks the extension of the filename.
     *
     * @param name the filename
     *
     * @return true if it ends with .eaf (case insensitive)
     */
    private boolean validEAFName(String name) {
        if (name == null) {
            return false;
        }

        String lowerPathName = name.toLowerCase();

        String[] exts = FileExtension.EAF_EXT;
        boolean validExt = false;

        for (String ext : exts) {
            if (lowerPathName.endsWith("." + ext)) {
                validExt = true;

                break;
            }
        }

        return validExt;
    }

    /**
     * If there are valid sources and a valid destination the next button can
     * be enabled, after storing the file paths to the properties map.
     */
    private void checkCondition() {   	    	
        if (src1Ready && src2Ready && destReady) {
            if (source1Field.getText().equals(source2Field.getText())) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString(
                        "MergeTranscriptionDialog.Warning.SameSources"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);

                return;
            }

            if (source1Field.getText().equals(destField.getText()) ||
                    source2Field.getText().equals(destField.getText())) {
                JOptionPane.showMessageDialog(this,
                    ElanLocale.getString(
                        "MergeTranscriptionDialog.Warning.DestinationIsSource"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.WARNING_MESSAGE);
                multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);

                return;
            }
            
            if(appendWithGivenTimeRB.isSelected()){    
        		String textValue = timeFrameField.getText().trim();
                try {
                	 timeFrame = TimeFormatter.toMilliSeconds(textValue);
                 } catch (NumberFormatException nfe) {       
                	 JOptionPane.showMessageDialog(this,
                                ElanLocale.getString("MergeTranscriptionDialog.Message.InvalidNumber"),
                                 ElanLocale.getString("Message.Warning"),
                                 JOptionPane.WARNING_MESSAGE);                
                	 timeFrameField.selectAll();
                	 timeFrameField.requestFocus();
                     return ;                
                 }
            }  	

            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        } else {
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == browseSource1) {
            String name = getOpenFileName("MergeFirstEafDir");

            if (name != null) {
                source1Field.setText(name);
                src1Ready = true;
            } else {
                if (isExistingEAF(source1Field.getText())) {
                    src1Ready = true;
                } else {
                    src1Ready = false;
                }
            }
        } else if (e.getSource() == browseSource2) {
            String name = getOpenFileName("MergeSecondEafDir");

            if (name != null) {
                source2Field.setText(name);
                src2Ready = true;
            } else {
                if (isExistingEAF(source2Field.getText())) {
                    src2Ready = true;
                } else {
                    src2Ready = false;
                }
            }
        } else {
            String name = getSaveFileName("MergeDestEafDir");

            if (name != null) {
                destField.setText(name);
                destReady = true;
            } else {
                if ((destField.getText() != null) &&
                        (destField.getText().length() > 4)) {
                    destReady = true;
                } else {
                    destReady = false;
                }
            }
        }

        checkCondition();
    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        // only one possible source
        if (e.getSource() == curTransCB) {            
            if(curTransCB.isSelected()){
            	browseSource1.setEnabled(false);
            	source1Field.setText(curTranscription.getFullPath());
            	if(appendAnnCB.isSelected()){
            		appendWitMediaDurRB.setEnabled(true);
            	}
            	src1Ready = true;      
            	
            } else {
            	browseSource1.setEnabled(true);
            	source1Field.setText("");
            	appendWitMediaDurRB.setEnabled(false);
            	src1Ready = false;            
            }
        } if(e.getSource() == appendWithGivenTimeRB){
        	if(appendWithGivenTimeRB.isSelected()){
        		timeFrameField.setEnabled(true);
        		timeFrameField.requestFocus();
			}else {
				timeFrameField.setEnabled(false);
			}
        }else if(e.getSource() == appendAnnCB){        	
        	if(appendAnnCB.isSelected()){
        		if(curTransCB.isSelected()){
        			appendWitMediaDurRB.setEnabled(true);
        		}else{
        			appendWitMediaDurRB.setEnabled(false);
        		}
        		if(appendWithGivenTimeRB.isSelected()){
        			timeFrameField.setEnabled(true);
            		timeFrameField.requestFocus();
        		}
        	} else {
        		appendWitMediaDurRB.setEnabled(false);
        	}
        	
        	appendWithLastAnnRB.setEnabled(appendAnnCB.isSelected());
        	appendWithGivenTimeRB.setEnabled(appendAnnCB.isSelected());

        }
    
        checkCondition();
    }

}
