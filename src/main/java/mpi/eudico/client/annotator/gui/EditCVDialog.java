package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.util.TranscriptionECVLoader;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextReader;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;
import mpi.eudico.server.corpora.clomimpl.dobes.ECV02Encoder;
import mpi.eudico.server.corpora.clomimpl.dobes.ECV02Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.ECVStore;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.ExternalCVEntry;


/**
 * $Id: EditCVDialog.java 46494 2018-07-31 11:16:35Z hasloe $
 *
 * The Edit Controlled Vocabulary dialog is a dialog for defining and changing
 * controlled vocabularies and their entries.<br>
 *
 * @author Han Sloetjes, Alexander Klassmann
 * @version jun 04
 * @version Aug 2005 Identity removed
 * @version July 2006 refactored
 *
 */
@SuppressWarnings("serial")
public class EditCVDialog extends AbstractEditCVDialog implements ActionListener,
    ItemListener {
	/** a logger */
    private static final Logger LOG = Logger.getLogger(EditCVDialog.class.getName());
    private JButton importButton;
    private JButton exportButton; 
    private JButton externalCVButton;
    private TranscriptionImpl transcription;
    private final int SKIP = 0;
    private final int REPLACE = 1;
    private final int RENAME = 2;
    private final int MERGE = 3;
    private boolean cvPrefsImported = false;

    /**
     * Creates a new EditCVDialog.
     *
     * @param transcription the transcription containing the controlled
     *        vocabularies
     */
    public EditCVDialog(Transcription transcription) {
        super(ELANCommandFactory.getRootFrame(transcription), true, true,
            new ElanEditCVPanel());
        this.transcription = (TranscriptionImpl) transcription;
        addCloseActions();
        updateLabels();
        setPosition();
        updateComboBox();
        cvNameTextField.requestFocus();
        postInit();
    }
    
    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();
        
        setLocationRelativeTo(getParent());
        //setResizable(false);
    }

    /**
     * The button actions.
     *
     * @param actionEvent the actionEvent
     */
    @Override
	public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        // check source equality
        if (source == importButton) {
            importCV();
        } else if (source == externalCVButton) {
        	connectExternalCV();
        } else if (source == exportButton){ 
        	exportECV();
        }else {
            super.actionPerformed(actionEvent);
        }
    }

    public void updateLocale() {
        ((ElanEditCVPanel) cvEditorPanel).updateLabels();
        updateLabels();
    }

    @Override
	protected List<ControlledVocabulary> getCVList() {
        return transcription.getControlledVocabularies();
    }
    
    @Override
	protected void updateCVButtons() {
        super.updateCVButtons();
        exportButton.setEnabled(cvComboBox.getItemCount() > 0);
    }

    /**
     * Calls command to add a CV to the transcription. Re-initializes gui afterwards.
     */
    @Override
    protected void addCV(String name) {
        //create a new CV and add it to the Transcription
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ADD_CV);
        Object[] args = new Object[2];
        args[0] = name;
        args[1] = cvDescArea.getText();

        com.execute(transcription, args);
        updateComboBox();
        cvComboBox.setSelectedIndex(cvComboBox.getItemCount() - 1);
    }

    /**
     * Calls command to change the CV in the transcription.
     * The name and description apply to the currently selected language
     * in the cvLanguageComboBox.
     */
    @SuppressWarnings("unchecked")
	@Override
    protected void changeCV(ControlledVocabulary cv, String name,
        String description) {
		//update preferences
		Map<String, Map<String, Map<String, Object>>> cvPrefs = 
			(Map<String, Map<String, Map<String, Object>>>) Preferences.getMap(Preferences.CV_PREFS, transcription);
		if (cvPrefs != null) {
			Map<String, Map<String, Object>> curCVPref = cvPrefs.remove(cv.getName());
			if (curCVPref != null) {
				cvPrefs.put(name, curCVPref);
				Preferences.set(Preferences.CV_PREFS, cvPrefs, transcription, true, false);
			}
		}
		
		int languageIndex = cvLanguageComboBox.getSelectedIndex();
		
        // create a change CV command
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.CHANGE_CV);
        Object[] args = new Object[5];
        args[0] = oldCVName;
        args[1] = (description != null) ? description : oldCVDesc;
        args[2] = (name != null) ? name : oldCVName;
        args[3] = description;
        args[4] = Integer.valueOf(languageIndex);

        com.execute(transcription, args);

        updateComboBox();
        cvComboBox.setSelectedItem(cv);
        // Preserve the currently selected language (was reset by updateComboBox()).
		cvLanguageComboBox.setSelectedIndex(languageIndex);
    }

    /**
     * If there are any tiers using this
     * cv, a message will ask the user for confirmation.
     * (Overrides condition of super class)
     */
    @Override
    protected void deleteCV() {
        ControlledVocabulary conVoc = (ControlledVocabulary) cvComboBox.getSelectedItem();
		
        // warn if there are tiers using lin. types using this cv
        if (transcription.getTiersWithCV(conVoc.getName()).size() > 0) {
            String mes = ElanLocale.getString("EditCVDialog.Message.CVInUse") +
                "\n" +
                ElanLocale.getString("EditCVDialog.Message.CVConfirmDelete");

            if (!showConfirmDialog(mes)) {
                return;
            }
        }

        deleteCV(conVoc);
    }

    /**
     * Calls command to delete CV in the transcription. Re-initializes gui afterwards.
     */
    @Override
    protected void deleteCV(ControlledVocabulary cv) {
		//update preferences or not? problems in case of undo/redo or not saving
    	/*
		HashMap<String, Map<String, Map<String, Object>>> cvPrefs = 
			(HashMap<String, Map<String, Map<String, Object>>>) Preferences.getMap(CV_PREFS, transcription);
		if (cvPrefs != null) {
			Map curCVPref = cvPrefs.remove(cv.getName());
			if (curCVPref != null) {
				Preferences.set(CV_PREFS, cvPrefs, transcription, true, false);
			}
		}
		*/
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.DELETE_CV);
        com.execute(transcription, new Object[] { cv });
        updateComboBox();

        if (cvComboBox.getItemCount() == 0) {
            cvEditorPanel.setControlledVocabulary(null);
        }
    }

    /**
     * This method is called from within the constructor to initialize the
     * dialog's components.
     */
    @Override
    protected void makeLayout() {
        super.makeLayout();
        importButton = new JButton();
        importButton.addActionListener(this);
        cvButtonPanel.add(importButton);
		externalCVButton = new JButton();
        externalCVButton.addActionListener(this);
        cvButtonPanel.add(externalCVButton);
        
        exportButton = new JButton();
        exportButton.addActionListener(this);
        cvButtonPanel.add(exportButton);
    }

    /**
     * Shows a confirm (yes/no) dialog with the specified message string.
     *
     * @param message the message to display
     *
     * @return true if the user clicked OK, false otherwise
     */
    @Override
    protected boolean showConfirmDialog(String message) {
        int confirm = JOptionPane.showConfirmDialog(this, message,
                ElanLocale.getString("Message.Warning"),
                JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    @Override
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Since this dialog is meant to be modal a Locale change while this dialog
     * is open is not supposed to happen. This will set the labels etc. using
     * the current locale strings.
     */
    @Override
    protected void updateLabels() {
        closeDialogButton.setText(ElanLocale.getString(
                "EditCVDialog.Button.Close"));
        deleteCVButton.setText(ElanLocale.getString("Button.Delete"));
        changeCVButton.setText(ElanLocale.getString("Button.Change"));
        addCVButton.setText(ElanLocale.getString("Button.Add"));
        importButton.setText(ElanLocale.getString("Button.Import"));
        exportButton.setText(ElanLocale.getString("EditCVDialog.Button.ExportEcv"));
        cvDescLabel.setText(ElanLocale.getString(
                "EditCVDialog.Label.CVDescription"));
        cvNameLabel.setText(ElanLocale.getString("EditCVDialog.Label.Name"));
        cvPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "EditCVDialog.Label.CV")));
        currentCVLabel.setText(ElanLocale.getString(
                "EditCVDialog.Label.Current"));
        titleLabel.setText(ElanLocale.getString("EditCVDialog.Title"));
        setTitle(ElanLocale.getString("EditCVDialog.Title"));

        cvNameExistsMessage = ElanLocale.getString(
                "EditCVDialog.Message.CVExists");
        cvInvalidNameMessage = ElanLocale.getString(
                "EditCVDialog.Message.CVValidName");
        cvContainsEntriesMessage = ElanLocale.getString(
                "EditCVDialog.Message.CVInUse");
        deleteQuestion = ElanLocale.getString(
                "EditCVDialog.Message.CVConfirmDelete");
        externalCVButton.setText(
        		ElanLocale.getString("Button.externalCV"));
    }

    @SuppressWarnings("unchecked")
	@Override
	protected void closeDialog() {
		if (cvEditorPanel instanceof ElanEditCVPanel) {
			if (((ElanEditCVPanel) cvEditorPanel).isPrefsChanged() || cvPrefsImported) {
				//save preferences, update listeners
				HashMap<String, Map<String, Map<String, Object>>> cvPrefs = 
					(HashMap<String, Map<String, Map<String, Object>>>) Preferences.getMap(Preferences.CV_PREFS, transcription);
				if (cvPrefs == null) {
					cvPrefs = new HashMap<String, Map<String, Map<String, Object>>>();
				}
				String color = "Color";
				String keyCode = "KeyCode";
				// iterate over the CV's and their entries
				Map<String, Map<String, Object>> curCV;
				Map<String, Object> curEnt;
				
				// Save preferences for words in the vocabularies.
				for (int i = 0; i < transcription.getControlledVocabularies().size(); i++) {
					curCV = null;
					ControlledVocabulary cv = transcription.getControlledVocabularies().get(i);
					curCV = cvPrefs.get(cv.getName());
					boolean anyPref = false;// make sure that there is at least one pref set, otherwise remove
					
					for (CVEntry cve : cv) {
						curEnt = null;
						if (cve.getPrefColor() != null || cve.getShortcutKeyCode() > -1) {
							anyPref = true;							
							if (curCV == null) {
								curCV = new HashMap<String, Map<String, Object>>();
								cvPrefs.put(cv.getName(), curCV);
							}
							curEnt = curCV.get(cve.getId());
							if (curEnt == null) {
								curEnt = new HashMap<String, Object>(3);
								curCV.put(cve.getId(), curEnt);
							}
							if (cve.getPrefColor() != null) {
								curEnt.put(color, cve.getPrefColor());
							} else {
								curEnt.remove(color);
							}
							if (cve.getShortcutKeyCode() > -1) {
								curEnt.put(keyCode, cve.getShortcutKeyCode());
							} else {
								curEnt.remove(keyCode);
							}
						} else {
							if (curCV != null) {
								curCV.remove(cve.getId());
							}
						}
					}
					// remove the CV if necessary
					if (!anyPref && curCV != null) {
						cvPrefs.remove(curCV);
					}
				}
				
				if (cvPrefs.size() == 0) {
					Preferences.set(Preferences.CV_PREFS, null, transcription, true);
				} else {// adds or replaces
					Preferences.set(Preferences.CV_PREFS, cvPrefs, transcription, true, false);
				}
			}
		}
		super.closeDialog();
	}

	/**
     * Prompts the user to select an import file, which can be a template file
     * (.etf), an .eaf file or a .csv file with a specific format.
     * .ecv could be added here, although that might cause confusion because 
     * the difference between linking an .ecv and importing an .ecv (which would
     * turn the external CV's into internal CV's) could be unclear.
     *
     * @return the template file, or null when no valid file was selected
     */
    private File getImportFile() {
    	
    	ArrayList<String[]> extensions = new ArrayList<String[]>();
        extensions.add(FileExtension.EAF_EXT);
        extensions.add(FileExtension.TEMPLATE_EXT);
        //extensions.add(FileExtension.ECV_EXT);
        
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("EditCVDialog.Import.Title"), 
        		FileChooser.OPEN_DIALOG, extensions, FileExtension.CSV_EXT, "LastUsedEAFDir", null);
        File impFile = chooser.getSelectedFile();
        
        if (impFile != null) { 
            return impFile;
        }
        
        return null;
    }

    /**
     * Adds an existing, imported CV, probably already containing entries to
     * the  Transcription.
     *
     * @param conVoc the CV to add
     *
     * @see #importCV()
     */
    private void addCV(ControlledVocabulary conVoc) {
        if (conVoc == null) {
            return;
        }

        //create a new CV and add it to the Transcription
        Command com = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.ADD_CV);
        Object[] args = new Object[1];
        args[0] = conVoc;

        com.execute(transcription, args);

        //reextractCVs() is done somewhere else
    }

    /**
     * Imports one or several CV's from a template file, selected by the user.
     * When there are CV's with the same identifier as existing CV's the user
     * is prompted to either replace an existing CV, to merge the two CV's, to 
     * rename the imported CV or to just skip the CV.
     */
    @SuppressWarnings("unchecked")
	private void importCV() {
        File importFile = getImportFile();

        if (importFile == null) {
            return;
        }
        List<ControlledVocabulary> allCVs = null;
        
        String fileName = importFile.getAbsolutePath();
                
        if(fileName.toLowerCase().endsWith(".txt") || fileName.toLowerCase().endsWith(".csv")){        	
        	try {
    			DelimitedTextReader reader = new DelimitedTextReader(importFile);
    			allCVs = parseTextFile(importFile,reader.getDelimiter());    			
    		} 
        	catch (FileNotFoundException e) {
    			showWarningDialog(e.getMessage() +":"+ importFile.getAbsolutePath());
    			LOG.warning( e.getMessage() +":"+ importFile.getAbsolutePath());
    		} 
        	catch(IOException e){
        		showWarningDialog(ElanLocale.getString(
        	   	"EditCVDialog.Message.ReadError") + importFile.getAbsolutePath());
        		
    			LOG.warning("Error while reading the file: " + e.getMessage());
    			return;
    		}
        
        } else {

        	// use an eaf skeleton parser, can handle etf as well as eaf
        	EAFSkeletonParser skelParser = null;
        	try {
        		skelParser = new EAFSkeletonParser(importFile.getAbsolutePath());
        		skelParser.parse();
        		allCVs = skelParser.getControlledVocabularies();
        	} catch (ParseException pe) {
        		showWarningDialog(ElanLocale.getString(
            	   	"EditCVDialog.Message.ReadError") + importFile.getAbsolutePath());
            
        		LOG.warning("Could not parse the file: " + pe.getMessage());
        		return;
        	}        
        }
        
        if (allCVs == null || allCVs.size() == 0) {
            showWarningDialog(ElanLocale.getString(
                    "EditCVDialog.Message.NoCVFound"));

            return;
        }
        // for external CV's
        List<ExternalCV> extCVForImport = new ArrayList<ExternalCV>();
        List<ExternalReference> ecvExtRef = new ArrayList<ExternalReference>();
        // load preferences
        Map<String, Object> preferences = Preferences.loadPreferencesForFile(importFile.getAbsolutePath());
        Map<String, Object> importPrefs = null;
        boolean oldPrefs = false;
        if (preferences != null) {
        	Object cvPrefObj = preferences.get(Preferences.CV_PREFS);
        	if (cvPrefObj instanceof Map) {
        		importPrefs = (Map<String, Object>) cvPrefObj;
        	} else {        		
            	cvPrefObj = preferences.get(Preferences.CV_PREFS_OLD_2_7);
            	if (cvPrefObj instanceof Map) {
            		importPrefs = (Map<String, Object>) cvPrefObj;
            		oldPrefs = true;
            	}        		
        	}
        }
		
        ControlledVocabulary cv = null;
        // now add them to the transcription, ensuring that all CV-names are unique
        for (int i = 0; i < allCVs.size(); i++) {
            cv = allCVs.get(i);
    		// By Micha: if cv is external, do not try to add it 
            // as a normal cv (perhaps add it as external cv later)
    		if (!(cv instanceof ExternalCV)) {
	            // apply preferences to the entries
    			importPreferencesFor(importPrefs, cv, oldPrefs);
            	
	            if (transcription.getControlledVocabulary(cv.getName()) != null) {
	                // cv with that name already exists: prompt user:
	                // replace, rename, merge or skip
	                int option = showCVQuestionDialog(cv.getName());
	
	                if (option == REPLACE) {
	                    replaceCV(cv);
	                } else if (option == RENAME) {
	                    String newName;
	
	                    while (true) {
	                        newName = showAskNameDialog(cv.getName());
	
	                        if (transcription.getControlledVocabulary(newName) != null) {
	                            showWarningDialog(ElanLocale.getString(
	                                    "EditCVDialog.Message.CVExists"));
	
	                            continue;
	                        }
	
	                        break;
	                    }
	
	                    if ((newName == null) || (newName.length() == 0)) {
	                        continue; //means skipping
	                    }
	
	                    cv.setName(newName);
	                    addCV(cv);
	                } else if (option == MERGE) {
	                    mergeCVs(cv);
	                }
	
	                // else continue...
	            } else {
	                // the transcription does not contain a cv with the same name, add it
	                addCV(cv);
	            }

    		} else {
    			extCVForImport.add((ExternalCV) cv);
    			if (!ecvExtRef.contains(((ExternalCV)cv).getExternalRef())) {
    				ecvExtRef.add(((ExternalCV)cv).getExternalRef());
    			}
    		}
        }
        // here check if external CV's plus associated external references can be imported
        if (!extCVForImport.isEmpty()) {
        	for (ExternalCV ecv : extCVForImport) {
	        	if (transcription.getControlledVocabulary(ecv.getName()) == null) {
	        		// check colors etc.
	        		importPreferencesFor(importPrefs, ecv, oldPrefs);
	        		// add the cv
	        		addCV(ecv);
	        	}
        	}
        	TranscriptionECVLoader ecvLoader = new TranscriptionECVLoader();
        	ecvLoader.loadExternalCVs(transcription, null);
        }
        
        updateComboBox();
    }
    
    /**
     * Export a CV as an External CV. 
     */
    private void exportECV() {
    	List<String> cvs = new ArrayList<String>();
    	for(int i =0; i < cvComboBox.getItemCount(); i ++){
    		cvs.add(cvComboBox.getItemAt(i).toString());
    	}
    	ExportExternalCVDialog dialog = new ExportExternalCVDialog(this, cvs);
    	dialog.setVisible(true);
    	
    	List<String> cvList = dialog.getCVList();
    	String exportFilePath = dialog.getExportFilePath();
    	
    	if(cvList.size() > 0 && exportFilePath != null){
    		List<ExternalCV> ecvList = new ArrayList<ExternalCV>();
        	       	
        	for (int i = 0; i < cvList.size(); i++) {
        		String cvName = cvList.get(i).toString(); 
        		ControlledVocabulary cv = transcription.getControlledVocabulary(cvName);
        		
        		if (cv != null) {
        			ExternalCV ecv = new ExternalCV("dummy name");
            		ecv.cloneStructure(cv);

            		for (CVEntry cvEntry : cv) {
           				ExternalCVEntry ecvEntry = new ExternalCVEntry(ecv, cvEntry);
           				ecv.addEntry(ecvEntry);
           			} 
            		ecvList.add(ecv);
           		}
           	}
        	
        	if (ecvList.size() > 0) {
        		try {
    				new ECV02Encoder().encodeAndSave(ecvList, exportFilePath);
    			} catch (IOException e) {
    				if (LOG.isLoggable(Level.WARNING)) {
    					LOG.warning("Could not save ECVs: " +e.getMessage());
    				}
    				//e.printStackTrace();
    			}
        	}
    	}
    }

    /**
     * Parse the .txt / .csv files to get all the cv entries
     * 
     * 1st column is the value of the cv entry
     * 2nd column is the description of the cv entry
     * 
     * This allows for only a single language.
     *
     * @throws IoException any io exception
     * @textFile the input file with CVs
     * @delimiter the delimiter used in the file
     * 
     * @return the new CVList
     */
    private ArrayList<ControlledVocabulary> parseTextFile(File textFile, String delimiter ) throws IOException{
    	
    	String fileName = textFile.getName();
    	int index = fileName.lastIndexOf('.');
    	String name = fileName.substring(0, index);   
    	int lineNumber = 0;
    	
    	ControlledVocabulary newCV = new ControlledVocabulary(name);  
    
    	InputStreamReader fileReader = new InputStreamReader(new FileInputStream(textFile), "UTF-8");        	
    	Scanner	scanner = new Scanner(fileReader);
    	
    	final int langId = 0;

    	//first use a Scanner to get each line
        while ( scanner.hasNextLine() ){  
        	String aLine = scanner.nextLine();
        	Scanner line = new Scanner(aLine);   
        	String value = null;
			String description = null; 
        	
        	lineNumber++;        		
        	
        	line.useDelimiter(delimiter);
        		 
    		if ( line.hasNext() ){
        		value = line.next();  
        		
        		// to remove the BOM in the value
        		if (lineNumber == 1) {            			
        			if(value.contains("\ufffd")){
        				value = value.replace("\ufffd", "");   
                    }else if(value.startsWith("\ufeff")){
        				value = value.replace("\ufeff", "");  
                    } else 	if(value.startsWith("\uefbbbf")){
        				value = value.replace("\uefbbbf", "");   
                    }
        		}
        		
        		if(line.hasNext()){
        			description = line.next(); 
        		}
        		else{
        			description = value;
        		} 
        		
        		CVEntry entry = new CVEntry(newCV);
        		entry.setValue(langId, value.trim());
        		entry.setDescription(langId, description.trim());
        		newCV.addEntry(entry); 
    		} 
        }
        
        fileReader.close();
       	scanner.close();
        
        ArrayList<ControlledVocabulary> list = new ArrayList<ControlledVocabulary>();
        list.add(newCV);
        
    	return list;    	
    }    
   
    /**
     * Deletes an existing CV with the name of the specified CV and adds  the
     * specified CV. The user is not prompted or warned.
     *
     * @param conVoc the new ControlledVocabulary
     */
    private void replaceCV(ControlledVocabulary conVoc) {
        String name = conVoc.getName();
        ControlledVocabulary oldCv = transcription.getControlledVocabulary(name);

        if (oldCv != null) {
            Command com = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.REPLACE_CV);
            com.execute(transcription, new Object[] { oldCv, conVoc });
        }
    }
    
    /**
     * Merges two CV's with the same name; entries present in the second cv that are not in the first cv 
     * are added to the first cv.
     * 
     * @param conVoc the second cv
     */
    private void mergeCVs(ControlledVocabulary conVoc) {
        String name = conVoc.getName();
        ControlledVocabulary oldCv = transcription.getControlledVocabulary(name);

        if (oldCv != null) {
            Command com = ELANCommandFactory.createCommand(transcription,
                  ELANCommandFactory.MERGE_CVS);
            com.execute(transcription, new Object[] { oldCv, conVoc });
        }        
    }

    /**
     * Prompts the user to enter a new name for a CV.
     *
     * @param name the old name
     *
     * @return the new name, or null when the user has cancelled the dialog
     */
    private String showAskNameDialog(String name) {
        String message = ElanLocale.getString("EditCVDialog.Message.NewName") +
            "\n\n- " + name;
        String newName = JOptionPane.showInputDialog(this, message,
                ElanLocale.getString("EditCVDialog.Message.Rename"),
                JOptionPane.QUESTION_MESSAGE);

        return newName;
    }
    
    /**
     * 
     * @param cvName name of the cv
     * 
     * @return 0 means skip, 1 means replace and 2 means rename
     */
	private int showCVQuestionDialog(String cvName) {
		return showCVQuestionDialog(cvName, false);
	}

    /**
     * Ask the user to skip, replace existing cv or rename importing cv.
     *
     * @param cvName name of the cv
     *
     * @return 0 means skip, 1 means replace and 2 means rename
     */
	private int showCVQuestionDialog(String cvName, Boolean forExternalCV) {
		// If it is a external CV only show Skip and Replace
		// Otherwise also show Rename and Merge
		ArrayList<String> optionsArray = new ArrayList<String>();
		optionsArray.add(ElanLocale.getString("EditCVDialog.Message.Skip"));
		optionsArray.add(ElanLocale.getString("EditCVDialog.Message.Replace"));
		if(!forExternalCV) {
			optionsArray.add(ElanLocale.getString("EditCVDialog.Message.Rename"));
			optionsArray.add(ElanLocale.getString("EditCVDialog.Message.Merge"));
		}
		String[] options = new String[optionsArray.size()];
		options = optionsArray.toArray(options);
//        options[0] = ElanLocale.getString("EditCVDialog.Message.Skip");
//        options[1] = ElanLocale.getString("EditCVDialog.Message.Replace");
//        options[2] = ElanLocale.getString("EditCVDialog.Message.Rename");
//        options[3] = ElanLocale.getString("EditCVDialog.Message.Merge");

        String message = ElanLocale.getString("EditCVDialog.Message.CVExists") +
            "\n\n- " + cvName + "\n";

        JOptionPane pane = new JOptionPane(message,
                JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                options);
        pane.createDialog(this, "").setVisible(true);

        Object selValue = pane.getValue();

        for (int i = 0; i < options.length; i++) {
            if (selValue == options[i]) {
                return i;
            }
        }

        return SKIP;
    }
    
    /**
     * Add the Escape and Ctrl-W close actions.
     */
    protected void addCloseActions() {
        EscCloseAction escAction = new EscCloseAction(this);
        CtrlWCloseAction wAction = new CtrlWCloseAction(this);
        
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        if (inputMap instanceof ComponentInputMap && (actionMap != null)) {
            String esc = "esc";
            inputMap.put((KeyStroke) escAction.getValue(
                    Action.ACCELERATOR_KEY), esc);
            actionMap.put(esc, escAction);
            String wcl = "cw";
            inputMap.put((KeyStroke) wAction.getValue(
                    Action.ACCELERATOR_KEY), wcl);
            actionMap.put(wcl, wAction);
        }
    }
    
    /**
	 * Prompt the user for a remote CV URL
	 * 
	 * @author Micha Hulsbosch
	 * 
	 * @param
	 * @return the url of the external CV 
	 */
	private URL getExternalCVURL() {
		URL remoteURL;
		String urlString = new String();
		if (this instanceof JDialog) {
			GetExternalCVURLDialog dialog = new GetExternalCVURLDialog(this);
			dialog.setVisible(true);//blocks
			urlString = dialog.getExternalCVURLString();
		}
		
		if (urlString == null) {
			return null;
		}
		
		try {
			remoteURL = new URL(urlString);
			return remoteURL;
		} catch (MalformedURLException me) {
			if (urlString.length() == 0) {
				JOptionPane.showMessageDialog(this, "No url was entered. No CV will be loaded.",
						"Url format error",	JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Url has an incorrect format. No CV will be loaded.",
						"Url format error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}
	
	/**
	 * Read an external CV and adds it to the transcription
	 * 
	 * @author Micha Hulsbosch
	 * 
	 * @param 
	 * @return
	 */
	private void connectExternalCV() {
		URL externalCVURL = getExternalCVURL();
		if(externalCVURL == null ) {
			return;
		}
		
		ECV02Parser ecvParser = null;
		try {
			// version tasting!
			// HS 2018 not so clear why this is important now since ECV20Parser
			// handles 0.1 too
			ECVStore eStore = new ECVStore();
			String version = eStore.ecvFileFormatTest(externalCVURL.toString());
			if (LOG.isLoggable(Level.INFO)) {
				LOG.info(String.format("The .ecv file \"%s\" is a file of version %s", externalCVURL, version));
			}
			ecvParser = new ECV02Parser(externalCVURL.toString());
			ecvParser.parse(null);
		} catch (ParseException pe) {
			showWarningDialog(ElanLocale.getString(
				   "EditCVDialog.Message.ReadError") + externalCVURL.toString());
			
			LOG.warning("Could not parse the file: " + pe.getMessage());
			return;
		}
		
		List<ControlledVocabulary> allCVs = ecvParser.getControlledVocabularies();
		
		if (allCVs.size() == 0) {
			showWarningDialog(ElanLocale.getString(
					"EditCVDialog.Message.NoCVFound"));

			return;
		}
		// create a cache file immediately
		// optionally record which cv's have been added and only cache them??
		ArrayList<ExternalCV> extCVs = new ArrayList<ExternalCV>(allCVs.size());
		ExternalReferenceImpl eri = new ExternalReferenceImpl(externalCVURL.toString(),
				ExternalReference.EXTERNAL_CV);
		ExternalCV cv = null;
		//now add them to the transcription, ensuring that all CV-names are unique
		for (int i = 0; i < allCVs.size(); i++) {
			cv = new ExternalCV(allCVs.get(i));
			if (eri != null) {
				try {
					cv.setExternalRef(eri.clone());
				} catch (CloneNotSupportedException cnse) {
					LOG.severe("Could not set the external reference: " + cnse.getMessage());
				}
			}
			extCVs.add(cv);
			if (transcription.getControlledVocabulary(cv.getName()) != null) {
				// cv with that name already exists: prompt user:
				// replace, rename, merge or skip
				int option = showCVQuestionDialog(cv.getName(), true);

				if (option == REPLACE) {
					replaceCV(cv);
				} 

				// else continue...
			} else {
				// the transcription does not contain a cv with the same name, add it
				addCV(cv);
			}
		}
		
		// save the ext. CV's to a cache 
		String saveDir = Constants.ELAN_DATA_DIR + Constants.FILESEPARATOR
		+ "CVCACHE";
		try {
			new ECV02Encoder().encodeAndSave(extCVs, saveDir, eri);
		} catch (IOException ioe) {
			// message??
			showWarningDialog(ElanLocale.getString(
				"LoadExternalCV.Message.WriteError"));
		}
		
		
		updateComboBox();
	}
	
	@SuppressWarnings("unchecked")
	private void importPreferencesFor(Map<String, Object> importPrefs, ControlledVocabulary cv, boolean oldStylePrefs) {
        if (importPrefs != null && cv != null) {
    		final String color = "Color";
    		final String keyCode = "KeyCode";
    		
        	Map<String, Object> hm = (Map<String, Object>) importPrefs.get(cv.getName());
        	Map<String, Object> entMap;
        	if (hm != null) {
        		for (CVEntry cve : cv) {
        			String key = oldStylePrefs ? cve.getValue(0) : cve.getId();
        			entMap = (Map<String, Object>) hm.get(key);
        			if (entMap != null) {
        				Object c = entMap.get(color);
        				if (c instanceof Color) {	
        					cve.setPrefColor((Color) c);
        				}
        				Object k = entMap.get(keyCode);
        				if (k instanceof Integer) {
        					cve.setShortcutKeyCode((Integer) k);
        				}
        			}
        		}
        		cvPrefsImported = true;
        	}
        }
	}
}


