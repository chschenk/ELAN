package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ShoeboxMarkerDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.shoebox.MarkerRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo2;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxParser;

@SuppressWarnings("serial")
public class MFToolboxImportStep2 extends AbstractMFImportStep2 implements ActionListener, ItemListener{	

    /** the value property */
    public static final String VALUE_PROPERTY = "value";

    /** a constant for a text file */
    public static final String TEXT_KEY = "TextFile";

    /** a constant for a typ file */
    public static final String TYPE_KEY = "TypeFile";   
    
    private JRadioButton typeRB;
    private JLabel typeLabel;
    private JTextField typField;  
    private JButton typButton;
    
    private JRadioButton specRB;
    private JButton fieldSpecButton;
    private JLabel fieldSpecLabel;
    
    private JLabel intervalLabel;
    private JTextField intervalField;
    private JCheckBox timeInRefMarker;
    private JCheckBox allUnicodeCB;
    private JCheckBox calcCharBytesCB;
    private JCheckBox scrubOnImportCB;

    private final String INTERVAL_PREF = "ShoeboxChatBlockDuration";

  
    /** Used for the storage of the filenames and media files */
    //private Hashtable fileNames = new Hashtable();
    private List<MarkerRecord> markers = null;
    
	
	public MFToolboxImportStep2(MultiStepPane multiPane) {
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
	    
		typeRB = new JRadioButton();
		typeRB.setSelected(true);	
		
		
	   	typeLabel = new JLabel(ElanLocale.getString("ImportDialog.Label.TypeToolbox"));	   	
	   	typField = new JTextField("", 23);		
	   	typButton = new JButton("...");	    
	    specRB = new JRadioButton();		   
	    
	    fieldSpecButton = new JButton(ElanLocale.getString("ImportDialog.Button.FieldSpec"));		
		fieldSpecButton.setEnabled(false);
	    
	    fieldSpecLabel = new JLabel("-");
	    fieldSpecLabel.setFont(fieldSpecLabel.getFont().deriveFont(10f));
	    
	    allUnicodeCB = new JCheckBox(ElanLocale.getString("ImportDialog.Label.AllUnicode"));
	    allUnicodeCB.setSelected(true);
	    
	    calcCharBytesCB = new JCheckBox(ElanLocale.getString("ImportDialog.Label.CorrectForBytesPerChar"));
	    calcCharBytesCB.setSelected(true);
	    
	    ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(typeRB);
		buttonGroup.add(specRB);
		
		Insets insets = new Insets(2, 6, 2, 6);
	   	
	   	GridBagConstraints gbc = new GridBagConstraints();	   
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.insets = insets;
		gbc.gridx = 0;
	    gbc.gridy = 0;
	    add(typeRB, gbc);
	            
	    gbc.gridx = 1;
	    add(typeLabel, gbc);
	            
	    gbc.gridx = 2;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1.0;
	    add(typField, gbc);
	            
	    gbc.gridx = 3;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 0.0;
	    add(typButton, gbc);
	    
	    gbc.gridx = 0;
        gbc.gridy = gbc.gridy+1;
        add(specRB, gbc);
        
        gbc.gridx = 1;
        add(fieldSpecButton, gbc);    

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        add(fieldSpecLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = gbc.gridy+1;
        gbc.gridwidth = 3;
        add (allUnicodeCB, gbc);
  
    	gbc.gridy = gbc.gridy+1;
    	add (calcCharBytesCB, gbc);
		
		//import options panel
		intervalLabel = new JLabel(ElanLocale.getString(
				"ImportDialog.Label.BlockDuration"));
		
		intervalField = new JTextField();
		Integer intPref = Preferences.getInt(INTERVAL_PREF, null);
		if (intPref != null) {
			intervalField.setText(String.valueOf(intPref.intValue()));	
		} else {
			intervalField.setText(String.valueOf(ToolboxDecoderInfo.DEFAULT_BLOCK_DURATION));
		}
		
		timeInRefMarker = new JCheckBox(ElanLocale.getString("ImportDialog.Label.TimeInRefMarker")); 
	   
	    scrubOnImportCB = new JCheckBox(ElanLocale.getString("ImportDialog.Label.ScrubAnnotations"));
	            
	             
	    JPanel optionsPanel = new JPanel(new GridBagLayout());
					    
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(4,6,4,6);
		optionsPanel.add(new JLabel(ElanLocale.getString("ImportDialog.Label.Options")), gridBagConstraints);
		
		
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new Insets(4,10,4,6);
		optionsPanel.add(intervalLabel, gridBagConstraints);
				
		gridBagConstraints.gridx = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(intervalField, gridBagConstraints);
				
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		optionsPanel.add(timeInRefMarker, gridBagConstraints);
				
		gridBagConstraints.gridy = 3;
		optionsPanel.add(scrubOnImportCB, gridBagConstraints);
				
		gbc.gridx = 0;
		gbc.gridy = gbc.gridy+1;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(10,6,10,6);
		add(optionsPanel, gbc);
		
		gbc.gridy = gbc.gridy+1;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weighty =1.0;
	    add(new JPanel(), gbc);	  
	    
		setShoeboxMarkerRB();
	      
		loadPreferences();
		
		typeRB.addItemListener(this);
		specRB.addItemListener(this);		

	    typButton.addActionListener(this);
	    fieldSpecButton.addActionListener(this);
	}
	
	private void setShoeboxMarkerRB() {
	    Boolean boolPref = Preferences.getBool("LastUsedShoeboxImportWithType", null);
	    if (boolPref != null && !boolPref) {
	    	//markerfile
	        specRB.setSelected(true);
			typButton.setEnabled(false);
			fieldSpecButton.setEnabled(true);
			allUnicodeCB.setEnabled(true);			
			preloadMarkers();
	    } else {
	        typeRB.setSelected(true);
			String luTypFile = Preferences.getString("LastUsedShoeboxTypFile", null);
			if (luTypFile != null) {
			    typField.setText(luTypFile);
			}
			typButton.setEnabled(true);
			allUnicodeCB.setEnabled(true);
	    }
	}
	
	/**
     * Loads the last used marker file.
     */
    private void preloadMarkers() {
 		String markerFile = Preferences.getString("LastUsedShoeboxMarkerFile", null);
 	    if (markerFile != null) {
 	        File f = new File(markerFile);
 	       if (!f.exists()) {
 	    	   return;
 	       }
 	       // copied from ShoeboxMarkerDialog
 	      String line = null;
 	      markers = new ArrayList<MarkerRecord>();
    	  FileReader filereader = null;
    	  BufferedReader br = null;
    	  
 	      try {
 	    	  filereader = new FileReader(f);
 	    	  br = new BufferedReader(filereader);
				
 	    	  MarkerRecord newRecord = null;
				
				while ((line = br.readLine()) != null) {
					line = line.trim();
					String label = getLabelPart(line);
					if (label == null) {
						continue;
					}
					String value = getValuePart(line);
					
					if (label.equals("marker")) {
						newRecord = new MarkerRecord();
						if (!value.equals("null")) {
							newRecord.setMarker(value);
						}
					} else if (newRecord == null) {
						// Skip line; avoid Null pointer problems.
					} else if (label.equals("parent")){
						if (!value.equals("null")) {
							newRecord.setParentMarker(value);
						}
					} else if (label.equals("stereotype")) {
						if (!value.equals("null")) {
							newRecord.setStereoType(value);
						}
					} else if (label.equals("charset")) {
						if (!value.equals("null")) {
							newRecord.setCharset(value);
						}
					} else if (label.equals("exclude")) {
						if (!value.equals("null")) {
							if (value.equals("true")) {
								newRecord.setExcluded(true);
							} else {
								newRecord.setExcluded(false);
							}
						}
					} else if (label.equals("participant")) {
						if (!value.equals("null")) {
							if (value.equals("true")) {
								newRecord.setParticipantMarker(true);
							} else {
								newRecord.setParticipantMarker(false);
							}
						}
						if (!ToolboxParser.elanBeginLabel.equals(newRecord.getMarker()) &&
								!ToolboxParser.elanEndLabel.equals(newRecord.getMarker()) &&
								!ToolboxParser.elanParticipantLabel.equals(newRecord.getMarker())) {
							markers.add(newRecord);
						}
					}
				}
				// if succes, change the label to show the loaded file
				fieldSpecLabel.setText(markerFile);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (br != null) {
						br.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (filereader != null) {
						filereader.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 	    }
    }
    
    private String getLabelPart(String theLine) {
		String label = null;

		int index = theLine.indexOf(':');

		if (index > 0) {
			label = theLine.substring(0, index);
		}

		return label;
	}
    
    private String getValuePart(String theLine) {
		String value = null;

		int index = theLine.indexOf(':');

		if (index < (theLine.length() - 2)) {
			value = theLine.substring(index + 1).trim();
		}

		return value;
	}

	private void loadPreferences(){
		// restore some
        Boolean val = Preferences.getBool("ToolboxImport.AllUnicode", null);
        if (val != null) {
        	allUnicodeCB.setSelected(val.booleanValue());
        }
        val = Preferences.getBool("ToolboxImport.TimeInRefMarker", null);
        if (val != null) {
        	timeInRefMarker.setSelected(val.booleanValue());
        }
        val = Preferences.getBool("ToolboxImport.CalcForCharBytes", null);
        if (val != null) {
        	calcCharBytesCB.setSelected(val.booleanValue());
        }
        val = Preferences.getBool("ToolboxImport.ScrubAnnotations", null);
        if (val != null) {
        	scrubOnImportCB.setSelected(val.booleanValue());
        }        
	}
	
	private void savePreferences(){
		Preferences.set("ToolboxImport.AllUnicode", 
				Boolean.valueOf(allUnicodeCB.isSelected()), null);
		
		Preferences.set("ToolboxImport.TimeInRefMarker", 
				Boolean.valueOf(timeInRefMarker.isSelected()), null);
		
		Preferences.set("ToolboxImport.CalcForCharBytes", 
				Boolean.valueOf(calcCharBytesCB.isSelected()), null);
		
		Preferences.set("ToolboxImport.ScrubAnnotations", 
				Boolean.valueOf(scrubOnImportCB.isSelected()), null);
	}
	
   /**
    * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
    */
   @Override
public boolean leaveStepForward() { 
       String typPath = null;
       
       if (typField.getText() != null) {
           typPath = typField.getText().trim();
       }
       
       if (typeRB.isSelected()) {
		if( ((typPath == null) ||  	(typPath.length() == 0)) ){
			showError(ElanLocale.getString("ImportDialog.Message.SpecifyType"));
		           return false;
			}
			
	        if (!(new File(typPath).exists())) {
	            showError(ElanLocale.getString("ImportDialog.Message.NoType"));
	
	            return false;
	        } 
		} else {
		    if (markers == null || markers.size() == 0) {
		        showError(ElanLocale.getString("ImportDialog.Message.SpecifyMarkers"));
		        
		        return false;
		    }
		}

		int durVal = ToolboxDecoderInfo.DEFAULT_BLOCK_DURATION;
		if (intervalField != null) {
			String dur = intervalField.getText();			
			try {
				durVal = Integer.parseInt(dur);	
				Preferences.set(INTERVAL_PREF, durVal, null);
			} catch (NumberFormatException nfe) {
				// ignore
			}			
		}
		
		savePreferences();
		
		ToolboxDecoderInfo2 decInfo = new ToolboxDecoderInfo2(null);
		decInfo.setBlockDuration(durVal);
		if (typeRB.isSelected()) {
		    decInfo.setTypeFile(typPath);
		    decInfo.setAllUnicode(allUnicodeCB.isSelected());
		} else {
		    decInfo.setShoeboxMarkers(markers);
		    decInfo.setAllUnicode(allUnicodeCB.isSelected());
		}
		decInfo.setTimeInRefMarker(timeInRefMarker.isSelected());
		decInfo.setRecalculateForCharBytes(calcCharBytesCB.isSelected());
		decInfo.setScrubAnnotations(scrubOnImportCB.isSelected());
		
		multiPane.putStepProperty("ToolboxDecoderInfo", decInfo);		 
		
       return true;       
   }	
   
   private void chooseTyp() {
	   FileChooser chooser = new FileChooser(this);   
       chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("ImportDialog.Approve"), 
       		null, FileExtension.SHOEBOX_TYP_EXT, false, "LastUsedShoeboxTypDir", FileChooser.FILES_ONLY, null);

       File f = chooser.getSelectedFile();
       if (f != null) {
    	   typField.setText(f.getAbsolutePath());
           Preferences.set("LastUsedShoeboxTypFile", f.getAbsolutePath(), null);
           Preferences.set("LastUsedShoeboxImportWithType", Boolean.TRUE, null);
       }
   }
   
   private void specifyFieldSpecs() {	
	   ShoeboxMarkerDialog smd = new ShoeboxMarkerDialog(null, true, true);
	   smd.setVisible(true);
	   markers = smd.getValue();
	   Preferences.set("LastUsedShoeboxImportWithType", Boolean.FALSE, null);
	
	   String markerFile = Preferences.getString("LastUsedShoeboxMarkerFile", null);
	   if (markerFile != null) {
		   fieldSpecLabel.setText(markerFile);
	   }
	}
   
   /**
    * Shows an error dialog.
    *
    * @param message
    */
   private void showError(String message) {
       JOptionPane.showMessageDialog(this, message,
           ElanLocale.getString("Message.Error"), JOptionPane.ERROR_MESSAGE);
   }

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
        if (source == typButton) {
            chooseTyp();
        } else if (source == fieldSpecButton) {
        	specifyFieldSpecs();
        } 
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if ((e.getSource() == typeRB) &&
				(e.getStateChange() == ItemEvent.SELECTED)) {	
					
			typeLabel.setEnabled(true);
			typButton.setEnabled(true);	
			typField.setEnabled(true);
			allUnicodeCB.setEnabled(true);
			fieldSpecButton.setEnabled(false);
			markers = null;
			if (typField.getText() == null || typField.getText().length() == 0) {
				typButton.doClick(200);	
			}
		} else if ((e.getSource() == specRB) &&
				(e.getStateChange() == ItemEvent.SELECTED)) {					
			typeLabel.setEnabled(false);
			typButton.setEnabled(false);
			typField.setEnabled(false);
			fieldSpecButton.setEnabled(true);
			fieldSpecButton.doClick(200);			
		}
	}
}
