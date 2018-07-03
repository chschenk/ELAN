package mpi.eudico.client.annotator.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.clomimpl.shoebox.MarkerRecord;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxDecoderInfo2;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxParser;


/**
 * Displays a dialog to either import a WAC file or a Shoebox file. Uses a
 * JOptionPane style mechanism to display a JDialog and return an Object as a
 * user value.<br>
 * <b>Note: </b>localization of the file choosers is not implemented (yet).<br>
 * 
 * @version Jan 2009 This dialog is also used for import of Toolbox files. (WAC 
 * hasn't been supported for a long time).
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ImportShoeboxWAC extends JComponent implements ActionListener, ItemListener {
    /** a constant for Shoebox mode */
    public static final int SHOEBOX = 0;

    /** a constant for Word Annotation Converter mode */
    public static final int WAC = 1;
    
    /** a constant for Toolbox mode */
    public static final int TOOLBOX = 2;
    
    private static int importType;

    /** the value property */
    public static final String VALUE_PROPERTY = "value";

    /** a constant for a text file */
    public static final String TEXT_KEY = "TextFile";

    /** a constant for a typ file */
    public static final String TYPE_KEY = "TypeFile";

//    public static final String MEDIA_KEY = "MediaFile";
    private JTextField sbxField = new JTextField("", 23);
    private JTextField typField = new JTextField("", 23);
//    private JTextField auField = new JTextField("", 23);
    private GridBagLayout gridbag = new GridBagLayout();
    private JButton txtButton;
    private JButton typButton;
//    private JButton medButton;
    private JButton fieldSpecButton;
    private JLabel fieldSpecLabel;
    private JButton okButton;
    private JButton cancelButton;
    
    private JRadioButton typeRB;
    private JRadioButton specRB;
    
    private JLabel typeLabel;
    
    private JLabel intervalLabel;
    private JTextField intervalField;
    private JCheckBox timeInRefMarker;
    private JCheckBox allUnicodeCB;
    private JCheckBox calcCharBytesCB;
    private JCheckBox scrubOnImportCB;

    private final String INTERVAL_PREF = "ShoeboxChatBlockDuration";

    //	private File lastUsedDir;//used for elan properties file

    /** Used for the storage of the filenames and media files */
    //private Hashtable fileNames = new Hashtable();
    private List markers = null;
//    private Vector mediaFileNames;
    private Object value;

    /**
     * Creates a new ImportShoeboxWAC instance
     *
     * @param type either <code>WAC</code> or <code>SHOEBOX</code>
     */
    private ImportShoeboxWAC(int type) {
        if (type >= SHOEBOX && type <= TOOLBOX) {
            importType = type;
        }

        createPane();

    }

    private void createPane() {
        setLayout(gridbag);
        Insets insets = new Insets(2, 6, 2, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        int y = 0;
        
        if (importType == SHOEBOX || importType == TOOLBOX) {
	    		ButtonGroup buttonGroup = new ButtonGroup();
	    		typeRB = new JRadioButton();
	    		typeRB.setSelected(true);
	    		specRB = new JRadioButton();
	    		buttonGroup.add(typeRB);
	    		buttonGroup.add(specRB);
	    		
            fieldSpecLabel = new JLabel("-");
            fieldSpecLabel.setFont(fieldSpecLabel.getFont().deriveFont(10f));
                
            gbc.gridx = 1;
            gbc.gridy = y;
            gbc.weightx = 0.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = insets;
            if (importType == SHOEBOX) {
            	add(new JLabel(ElanLocale.getString("ImportDialog.Label.Shoebox")),
                    gbc);
            } else {
            	add(new JLabel(ElanLocale.getString("ImportDialog.Label.Toolbox")),
                        gbc);            	
            }
            
            gbc.gridx = 2;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(sbxField, gbc);
            
            txtButton = new JButton("...");
            txtButton.addActionListener(this);
            gbc.gridx = 3;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            add(txtButton, gbc);
            y++;
            
            gbc.gridx = 0;
            gbc.gridy = y;
            add(typeRB, gbc);
            
            if (importType == SHOEBOX) {
            	typeLabel = new JLabel(ElanLocale.getString("ImportDialog.Label.Type"));
            } else {
            	typeLabel = new JLabel(ElanLocale.getString("ImportDialog.Label.TypeToolbox"));
            }
            gbc.gridx = 1;
            add(typeLabel, gbc);
            
            gbc.gridx = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            add(typField, gbc);
            
            typButton = new JButton("...");
            typButton.addActionListener(this);
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            add(typButton, gbc);
            y++;
            
            allUnicodeCB = new JCheckBox(ElanLocale.getString("ImportDialog.Label.AllUnicode"));
            calcCharBytesCB = new JCheckBox(ElanLocale.getString("ImportDialog.Label.CorrectForBytesPerChar"));
            calcCharBytesCB.setSelected(true);
            scrubOnImportCB = new JCheckBox(ElanLocale.getString("ImportDialog.Label.ScrubAnnotations"));
            
            if (importType == SHOEBOX) {
                gbc.gridx = 1;
                gbc.gridy = y;
                gbc.gridwidth = 2;
                add (allUnicodeCB, gbc);
                y++;
                
                gbc.gridx = 0;
                gbc.gridy = y;
                add(specRB, gbc);
                
    			fieldSpecButton = new JButton(ElanLocale.getString("ImportDialog.Button.FieldSpec"));
    			fieldSpecButton.addActionListener(this);
    			fieldSpecButton.setEnabled(false);
                gbc.gridx = 1;
                gbc.gridwidth = 1;
                add(fieldSpecButton, gbc); 
                
                gbc.gridx = 2;
                gbc.gridwidth = 2;
                add(fieldSpecLabel, gbc);
                y++;
            } else
            if (importType == TOOLBOX) {
            	allUnicodeCB.setSelected(true);

                gbc.gridx = 0;
                gbc.gridy = y;
                add(specRB, gbc);
                
    			fieldSpecButton = new JButton(ElanLocale.getString("ImportDialog.Button.FieldSpec"));
    			fieldSpecButton.addActionListener(this);
    			fieldSpecButton.setEnabled(false);
                gbc.gridx = 1;
                add(fieldSpecButton, gbc);    

                gbc.gridx = 2;
                gbc.gridwidth = 2;
                add(fieldSpecLabel, gbc);
                y++;
                
                gbc.gridx = 1;
                gbc.gridy = y;
                gbc.gridwidth = 3;
                add (allUnicodeCB, gbc);
                y++;
                
            	gbc.gridy = y;
            	add (calcCharBytesCB, gbc);
            	y++;
            }
            /*
            gbc.gridx = 1;
            gbc.gridy = y;
            gbc.gridwidth = 2;
            add (allUnicodeCB, gbc);
            y++;
            

            if (importType == TOOLBOX) {
            	gbc.gridy = y;
            	add (calcCharBytesCB, gbc);
            	y++;
            }
            
            gbc.gridx = 0;
            gbc.gridy = y;
            add(specRB, gbc);
            
			fieldSpecButton = new JButton(ElanLocale.getString("ImportDialog.Button.FieldSpec"));
			fieldSpecButton.addActionListener(this);
			fieldSpecButton.setEnabled(false);
            gbc.gridx = 1;
            add(fieldSpecButton, gbc);            
            y++;
            */
		    JPanel optionsPanel = new JPanel(new GridBagLayout());
		    optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("ImportDialog.Label.Options")));
		    
			intervalLabel = new JLabel(ElanLocale.getString(
				"ImportDialog.Label.BlockDuration"));
			intervalField = new JTextField();
			Integer intPref = Preferences.getInt(INTERVAL_PREF, null);
			if (intPref != null) {
				intervalField.setText(String.valueOf(intPref.intValue()));	
			} else {
				intervalField.setText(String.valueOf(ToolboxDecoderInfo.DEFAULT_BLOCK_DURATION));
			}
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.insets = gbc.insets;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			optionsPanel.add(intervalLabel, gridBagConstraints);
			
			gridBagConstraints.gridx = 1;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			optionsPanel.add(intervalField, gridBagConstraints);
			
			timeInRefMarker = new JCheckBox(ElanLocale.getString("ImportDialog.Label.TimeInRefMarker"));
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			optionsPanel.add(timeInRefMarker, gridBagConstraints);
			
			gridBagConstraints.gridy = 2;
			optionsPanel.add(scrubOnImportCB, gridBagConstraints);
			
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = y;
			gridBagConstraints.gridwidth = 4;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			add(optionsPanel, gridBagConstraints);
			
	  		setShoeboxMarkerRB();
	  		
			typeRB.addItemListener(this);
			specRB.addItemListener(this);
			y++;
        } else {
            gbc.gridx = 0;
            gbc.gridy = y;
            gbc.weightx = 0.0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = insets;
            add(new JLabel(ElanLocale.getString("ImportDialog.Label.WAC")), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(sbxField, gbc);
            
            txtButton = new JButton("...");
            txtButton.addActionListener(this);
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            add(txtButton, gbc);
            y++;
            
            typeLabel = new JLabel(ElanLocale.getString("ImportDialog.Label.Type"));
            gbc.gridx = 0;
            gbc.gridy = y;
            add(typeLabel, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            add(typField, gbc);
            
            typButton = new JButton("...");
            typButton.addActionListener(this);
            gbc.gridx = 2;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            add(typButton, gbc);
            y++;
        }
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

        // ok - cancel buttons //
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton(ElanLocale.getString("Button.OK"));
        okButton.addActionListener(this);

        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = y;;
        add(buttonPanel, gbc);
    }

	private void setShoeboxMarkerRB() {
	    Boolean boolPref = Preferences.getBool("LastUsedShoeboxImportWithType", null);
	    if (boolPref != null && !boolPref.booleanValue()) {
	    	//markerfile
	        specRB.setSelected(true);
			typButton.setEnabled(false);
			fieldSpecButton.setEnabled(true);
			if (importType == SHOEBOX) {
			    allUnicodeCB.setEnabled(false);
			} else {
				allUnicodeCB.setEnabled(true);
			}
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
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object showDialog() {
        return showDialog(SHOEBOX);
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object showDialog(int type) {
        return showDialog(null, type);
    }

    /**
     * Creates a Dialog to select some files for import.
     *
     * @param parent the parent Frame, can be null
     * @param type the type of import, either WAC or Shoebox
     *
     * @return a Hashtable with file names, or null
     */
    public static Object showDialog(Frame parent, int type) {
        ImportShoeboxWAC pane = new ImportShoeboxWAC(type);
        JDialog dialog = pane.createDialog(parent, type);
        dialog.setVisible(true);
        dialog.dispose();

        Object o = pane.getValue();

        //System.out.println("Return Value: " + o);
        return o;
    }

    /**
     * Creates the dialog with <code>this</code> as content pane.
     *
     * @param parent the parent Frame or null
     * @param type the type of import, either WAC, Shoebox or Toolbox
     *
     * @return a modal JDialog
     */
    private JDialog createDialog(Frame parent, int type) {
        final JDialog dialog = new ClosableDialog(parent);

        if (type == WAC) {
            dialog.setTitle(ElanLocale.getString("ImportDialog.Title.WAC"));
        } else if (type == SHOEBOX){
            dialog.setTitle(ElanLocale.getString("ImportDialog.Title.Shoebox"));
        } else {
        	dialog.setTitle(ElanLocale.getString("ImportDialog.Title.Toolbox"));
        }

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(this, BorderLayout.CENTER);
        dialog.setModal(true);
        dialog.pack();

        if (parent != null) {
            dialog.setLocationRelativeTo(parent);
        }

        dialog.addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent we) {
                    setValue(null);
                }
            });

        // use the java.beans PropertyChangeSupport mechanism
        addPropertyChangeListener(new PropertyChangeListener() {
                @Override
				public void propertyChange(PropertyChangeEvent event) {
                    if (dialog.isVisible() &&
                            (event.getSource() == ImportShoeboxWAC.this) &&
                            event.getPropertyName().equals(VALUE_PROPERTY)) {
                        dialog.setVisible(false);
                    }
                }
            });

        return dialog;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == txtButton) {
            chooseSbxWAC();
        } else if (source == typButton) {
            chooseTyp();
        } else if (source == fieldSpecButton) {
        	specifyFieldSpecs();
  //    }
  //      else if (source == medButton) {
  //          chooseMedia();
        } else if (source == okButton) {
            checkFields();
            /*
            if (checkFields()) {
                setValue(fileNames);
            }
            */
        } else if (source == cancelButton) {
            setValue(null);
        }
    }

    private void chooseSbxWAC() {
  
        FileChooser chooser = new FileChooser(this);       
        String[] mainExtension = null;
        boolean acceptAllFilesFilter = true;
        
        if (importType == WAC) {        	
        	mainExtension = FileExtension.WAC_EXT;		
        } else {
        	acceptAllFilesFilter =false;
        	if (importType == SHOEBOX) {        		
        		mainExtension = FileExtension.SHOEBOX_TEXT_EXT;		
        	} else if (importType == TOOLBOX) {        	
        		mainExtension = FileExtension.TOOLBOX_TEXT_EXT;		
        	}
        }
        
        chooser.createAndShowFileDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, ElanLocale.getString("ImportDialog.Approve"), 
        		null, mainExtension, acceptAllFilesFilter, "LastUsedShoeboxDir", FileChooser.FILES_ONLY, null);
        
        File f = chooser.getSelectedFile();
        if (f != null) {
            sbxField.setText(f.getAbsolutePath());
        }
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
 	      markers = new ArrayList();
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
	
	private void specifyFieldSpecs() {
		if (importType == SHOEBOX) {
			ShoeboxMarkerDialog smd = new ShoeboxMarkerDialog(null, true);
			smd.setVisible(true);
			markers = (List) smd.getValue();
			Preferences.set("LastUsedShoeboxImportWithType", Boolean.FALSE, null);
		} else {
			ShoeboxMarkerDialog smd = new ShoeboxMarkerDialog(null, true, true);
			smd.setVisible(true);
			markers = (List) smd.getValue();
			Preferences.set("LastUsedShoeboxImportWithType", Boolean.FALSE, null);
		}
		//Preferences.set("LastUsedShoeboxMarkers", markers, null);
 		String markerFile = Preferences.getString("LastUsedShoeboxMarkerFile", null);
 	    if (markerFile != null) {
 	    	fieldSpecLabel.setText(markerFile);
 	    }
	}

    /**
     * Checks the contents of all input fields and next the existence of the
     * designated files.
     *
     * @return true if the files exist, false otherwise
     */
    private boolean checkFields() {
        String sbxPath = null;
        String typPath = null;
        
        if (sbxField.getText() != null) {
            sbxPath = sbxField.getText().trim();
        }
        
        if ((sbxPath == null) || (sbxPath.length() == 0)) {
            if (importType == WAC) {
                showError(ElanLocale.getString(
                        "ImportDialog.Message.SpecifyWAC"));
            } else {
                showError(ElanLocale.getString(
                        "ImportDialog.Message.SpecifyShoebox"));
            }

            return false;
        }
        
        if (typField.getText() != null) {
            typPath = typField.getText().trim();
        }
        
        if (	typeRB.isSelected() &&
        		((typPath == null) || 
        		(typPath.length() == 0))	) {
            showError(ElanLocale.getString("ImportDialog.Message.SpecifyType"));

            return false;
        }

        if (!(new File(sbxPath).exists())) {
            if (importType == WAC) {
                showError(ElanLocale.getString("ImportDialog.Message.NoWAC"));
            } else {
                showError(ElanLocale.getString("ImportDialog.Message.NoShoebox"));
            }

            return false;
        } 

		if (typeRB.isSelected()) {
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
		
		Preferences.set("ToolboxImport.AllUnicode", 
				Boolean.valueOf(allUnicodeCB.isSelected()), null);
		Preferences.set("ToolboxImport.TimeInRefMarker", 
				Boolean.valueOf(timeInRefMarker.isSelected()), null);
		
		if (importType == TOOLBOX) {
			ToolboxDecoderInfo2 decInfo = new ToolboxDecoderInfo2(sbxPath);
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
			Preferences.set("ToolboxImport.CalcForCharBytes", 
					Boolean.valueOf(calcCharBytesCB.isSelected()), null);
			Preferences.set("ToolboxImport.ScrubAnnotations", 
					Boolean.valueOf(scrubOnImportCB.isSelected()), null);
			
			setValue(decInfo);
		} else {
			ToolboxDecoderInfo decInfo = new ToolboxDecoderInfo(sbxPath);
			decInfo.setBlockDuration(durVal);
			if (typeRB.isSelected()) {
			    decInfo.setTypeFile(typPath);
			    decInfo.setAllUnicode(allUnicodeCB.isSelected());
			} else {
			    decInfo.setShoeboxMarkers(markers);
			}
			decInfo.setTimeInRefMarker(timeInRefMarker.isSelected());
	
			setValue(decInfo);	
		}
		
        return true;
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

    /**
     * Returns a decoder info object, or null
     *
     * @return the user object or null
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the new value the user has chosen.
     *
     * @param newValue the new value
     */
    public void setValue(Object newValue) {
        Object oldValue = value;
        value = newValue;
        firePropertyChange(VALUE_PROPERTY, oldValue, value);
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
			if (importType == SHOEBOX) {
			    allUnicodeCB.setEnabled(false);
			}
	
			fieldSpecButton.setEnabled(true);
			fieldSpecButton.doClick(200);			
		}
	}
}
