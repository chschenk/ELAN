package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.UpdateTranscriptionsForECVCommand;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.prefs.gui.RecentLanguagesBox;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;
import mpi.eudico.util.multilangcv.LangInfo;
import mpi.eudico.util.multilangcv.LanguageCollection;

/**
 * A dialog to configure the Corpus ECV updater.
 * 
 * @author Micha Hulsbosch, after CreateTranscriptionsDialog by Han Sloetjes
 */
@SuppressWarnings("serial")
public class UpdateTranscriptionsForECVDialog extends ClosableDialog implements 
    ActionListener, ChangeListener, ProgressListener {
//	private List<File> videoFolders;
//	private List<File> audioFolders;
	private JButton sourceButton;
	private JRadioButton sameFolderRB;
	private JRadioButton otherFolderRB;
	private JButton destFolderButton;
	private JButton startButton;
	private JButton closeButton;
	private JTextField sourceTF;
	private JTextField destFolderTF;
	private JTextField languageTF;
	private JComboBox newLanguageComboBox;	// contains all available languages
	private JCheckBox recursiveCB;
	private JProgressBar progressBar;
	private Command command;
	private SwingWorker<Void, Void> task;
	private JCheckBox annoValuePrecedenceCB;
	/**
	 * Constructor with a parent frame
	 * 
	 * @param owner parent 
	 * @throws HeadlessException
	 */
	public UpdateTranscriptionsForECVDialog(Frame owner) throws HeadlessException {
		super(owner);
		initComponents();
	}

	/**
	 * Constructor with parent and modal flag.
	 * @param owner parent frame
	 * @param modal modal flag
	 * @throws HeadlessException
	 */
	public UpdateTranscriptionsForECVDialog(Frame owner, boolean modal)
			throws HeadlessException {
		super(owner, modal);
		initComponents();
		postInit();
	}

	private void postInit() {
		updateNewLanguageComboBox();
		loadPreferences();
		pack();
		setSize(getWidth() + 60, getHeight());
		setLocationRelativeTo(getParent());	
	}
	
	private void initComponents() {
		setTitle(ElanLocale.getString("UpdateMTranscriptionsForECVDialog.Title"));
		getContentPane().setLayout(new GridBagLayout());
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		pane.setBorder(new TitledBorder(ElanLocale.getString("TokenizeDialog.Label.Options")));//reuse
		Insets insets = new Insets(4, 6, 0, 6);
		Insets indent = new Insets(4, 26, 0, 6);
		
		JLabel sourceLabel = new JLabel(ElanLocale.getString("UpdateMTranscriptionsForECVDialog.SourceFolder"));
		sourceTF = new JTextField();
		// allow or disallow editing of the textfields?
		sourceTF.setEnabled(false);
		sourceButton = new JButton(ElanLocale.getString("Button.Browse"));
		recursiveCB = new JCheckBox(ElanLocale.getString("CreateMultiEAFDialog.Button.Recursive"));//reuse
		
		JLabel transLocationLabel = new JLabel(ElanLocale.getString("CreateMultiEAFDialog.Label.EAFLocation"));//reuse
		sameFolderRB = new JRadioButton(ElanLocale.getString("UpdateMTranscriptionsForECVDialog.Button.Overwrite"));
		sameFolderRB.setSelected(true);
		otherFolderRB = new JRadioButton(ElanLocale.getString("CreateMultiEAFDialog.Button.OtherFolder"));//reuse
		ButtonGroup fGroup = new ButtonGroup();
		fGroup.add(sameFolderRB);
		fGroup.add(otherFolderRB);
		destFolderButton = new JButton(ElanLocale.getString("Button.Browse"));
		destFolderButton.setEnabled(false);
		destFolderTF = new JTextField();
		destFolderTF.setEnabled(false);
		
		JLabel languageLabel = new JLabel(ElanLocale.getString("UpdateMTranscriptionsForECVDialog.Language"));
		newLanguageComboBox = getNewLanguageComboBox();
		
		annoValuePrecedenceCB = new JCheckBox(ElanLocale.getString("UpdateMTranscriptionsForECVDialog.AnnotationValuePrecedence"));
		
		JLabel progressLabel = new JLabel(ElanLocale.getString("MultipleFileSearch.FindReplace.Progress"));//reuse
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 6, 2));
		startButton = new JButton(ElanLocale.getString("Button.Start"));
		closeButton = new JButton(ElanLocale.getString("Button.Close"));
		buttonPanel.add(startButton);
		buttonPanel.add(closeButton);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 3;
		pane.add(sourceLabel, gbc);
		
		gbc.gridwidth = 2;
		gbc.gridy = 1;
		pane.add(sourceTF, gbc);
		
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		pane.add(sourceButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 26, 6, 6);
		pane.add(recursiveCB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = insets;
		pane.add(transLocationLabel, gbc);
		
		gbc.gridy = 4;
		gbc.insets = indent;
		pane.add(sameFolderRB, gbc);
		
		gbc.gridy = 5;
		pane.add(otherFolderRB, gbc);
		
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(4, 26, 6, 6);
		pane.add(destFolderTF, gbc);
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.insets = new Insets(4, 6, 6, 6);
		pane.add(destFolderButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 3;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		pane.add(languageLabel, gbc);
		
		gbc.gridy = 8;
		pane.add(newLanguageComboBox, gbc);
		
		gbc.gridy = 9;
		gbc.insets = new Insets(8, 6, 0, 6);
		pane.add(annoValuePrecedenceCB, gbc);
		
		gbc.gridy = 10;
		gbc.insets = new Insets(12, 6, 0, 6);
		pane.add(progressLabel, gbc);
		
		gbc.gridy = 11;
		gbc.insets = insets;
		pane.add(progressBar, gbc);
		
		gbc = new GridBagConstraints();
		gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(pane, gbc);
        
		gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 6, 2, 6);
		getContentPane().add(buttonPanel, gbc);
		
		sourceButton.addActionListener(this);
		destFolderButton.addActionListener(this);
		startButton.addActionListener(this);
		closeButton.addActionListener(this);
		sameFolderRB.addChangeListener(this);
		otherFolderRB.addChangeListener(this);
	}
	
	private void loadPreferences() {
		String stringPref = Preferences.getString("UpdateTranscriptionsForECV.SourcePath", null);
		if (stringPref != null) {
			sourceTF.setText(stringPref); 
		}
		Boolean boolPref = Preferences.getBool("UpdateTranscriptionsForECV.Recursive", null);
		if (boolPref != null) {
			recursiveCB.setSelected(boolPref);
		}
		boolPref = Preferences.getBool("UpdateTranscriptionsForECV.OtherDestination", null);
		if (boolPref != null) {
			otherFolderRB.setSelected(boolPref);// this sets the state for sameFolderRB as well
			destFolderButton.setEnabled(otherFolderRB.isSelected());
			//destFolderTF.setEnabled(otherFolderRB.isSelected());
		}
		stringPref = Preferences.getString("UpdateTranscriptionsForECV.DestinationPath", null);
		if (stringPref != null) {
			destFolderTF.setText(stringPref);
		}
		
		stringPref = Preferences.getString("UpdateTranscriptionsForECV.LanguageRef", null);
		if(stringPref != null) {
			LangInfo langInfo = LanguageCollection.getLanguageInfo(stringPref);
			newLanguageComboBox.getModel().setSelectedItem(langInfo);
		}
		
		boolPref = Preferences.getBool("UpdateTranscriptionsForECV.AnnotationValuePrecedence", null);
		if (boolPref != null) {
			annoValuePrecedenceCB.setSelected(boolPref);
		}
		
	}
	
	private void savePreferences() {
		String sourcePath = sourceTF.getText();
		if (sourcePath != null && sourcePath.length() > 0) {
			Preferences.set("UpdateTranscriptionsForECV.SourcePath", sourcePath, null, false, false);
		}

		Preferences.set("UpdateTranscriptionsForECV.Recursive", recursiveCB.isSelected(), null, false, false);

		boolean otherDest = otherFolderRB.isSelected();
		Preferences.set("UpdateTranscriptionsForECV.OtherDestination", otherDest, null, false, false);
		
		String destPath = destFolderTF.getText();
		if (destPath != null && destPath.length() > 0) {
			Preferences.set("UpdateTranscriptionsForECV.DestinationPath", destPath, null, false, false);
		}
		if(newLanguageComboBox.getSelectedIndex() > -1) {
			String langRef = ((LangInfo) newLanguageComboBox.getSelectedItem()).getId();
			if(langRef != null && langRef.length() > 0) {
				Preferences.set("UpdateTranscriptionsForECV.LanguageRef", langRef, null, false, false);
			}
		}
		Preferences.set("UpdateTranscriptionsForECV.AnnotationValuePrecedence", 
				annoValuePrecedenceCB.isSelected(), null, false, false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sourceButton) {
			// get source folder
			getSourceFolder();
		} else if (e.getSource() == destFolderButton) {
			// destination folder
			getDestinationFolder();
		} else if (e.getSource() == startButton) {
			startButton.setEnabled(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			task = new SwingWorker<Void, Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					// create command
					if (create() && !((UpdateTranscriptionsForECVCommand) command).isCanceled()) {
						savePreferences();
						// show report if any.
						if (command instanceof ProcessReporter) {
							ProcessReport report = ((ProcessReporter) command).getProcessReport();
							if (report != null) {
								ReportDialog rd = new ReportDialog(UpdateTranscriptionsForECVDialog.this, report);
								rd.setVisible(true);
							}
						} else {
							setVisible(false);
							dispose();
						}
					}
					return null;
				}
				@Override
				public void done() {
					progressBar.setValue(100);
					progressBar.setIndeterminate(false);
					startButton.setEnabled(true);
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			};
	        task.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress" == evt.getPropertyName()) {
			            int progress = (Integer) evt.getNewValue();
			            progressBar.setValue(progress);
			        } 
				}
			});
	        progressBar.setIndeterminate(true);
	        task.execute();
			
			
		} else if (e.getSource() == closeButton) {
			savePreferences();
			
			// Canceling
			if (command != null && command instanceof UpdateTranscriptionsForECVCommand) {
				((UpdateTranscriptionsForECVCommand) command).cancel();
			}
			if (task != null) {
				task.cancel(true);
			}			
			
			setVisible(false);
			dispose();
		}
		
	}

	
	/**
	 * Change events of radio buttons and checkboxes, update UI.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sameFolderRB) {
			destFolderButton.setEnabled(false);
			//destFolderTF.setEnabled(false);
		} else if (e.getSource() == otherFolderRB) {
			destFolderButton.setEnabled(true);
			//destFolderTF.setEnabled(true);
		}
	}
	
	/**
	 * Prompts for a folder containing transcription files
	 */
	private void getSourceFolder() {
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("CreateMultiEAFDialog.Label.SelectSourceFolder"), FileChooser.OPEN_DIALOG, 
				ElanLocale.getString("Button.Select"), null, null, true, "LastUsedEAFDir", 
				FileChooser.DIRECTORIES_ONLY, null);

		File selDir = chooser.getSelectedFile();
		if (selDir != null) {
			sourceTF.setText(selDir.getAbsolutePath());
		}
	}
	
	/**
	 * Prompts the user for a destination folder for the eaf files to create.
	 */
	private void getDestinationFolder() {

		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("CreateMultiEAFDialog.Label.EAFLocation"), FileChooser.OPEN_DIALOG, 
				ElanLocale.getString("Button.Select"), null, null, true, 
				"LastUsedEAFDir", FileChooser.DIRECTORIES_ONLY, null);

		File selDir = chooser.getSelectedFile();
		if (selDir != null) {				
			destFolderTF.setText(selDir.getAbsolutePath());
		}
	}
	
	/**
	 * Performs checks and creates a command.
	 * Minimal required is a source directory. Above that it depends on the selected checkboxes etc.
	 * 
	 * @return true if all requirements were met and the command has been executed, false otherwise
	 */
	private boolean create() {
		String sourceFol = sourceTF.getText();
		if (sourceFol == null || sourceFol.length() == 0) {
			showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoSource"));//reuse
			sourceButton.requestFocus();
			return false;
		} else {
			// check if it exists
			File f = new File(sourceFol);
			try {
				if (!f.exists() || !f.isDirectory()) {
					showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NonExistSource"));//reuse
					sourceButton.requestFocus();
					return false;
				}
			} catch (SecurityException ex) {
				showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoAccessSource"));//reuse
				return false;
			}
		}
		String destFol = null;
		if (otherFolderRB.isSelected()) {
			destFol = destFolderTF.getText();
			if (destFol == null || destFol.length() == 0) {				
				showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoDestination"));//reuse
				destFolderButton.requestFocus();
				return false;
			} else {
				File f = new File(destFol);
				try {
					if (!f.exists() || !f.isDirectory()) {
						showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NonExistDestination"));//reuse
						destFolderButton.requestFocus();
						return false;
					}
				} catch (SecurityException se) {
					showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoAccessDestination"));//reuse
					return false;
				}
			}
		}
		
		boolean recursive = recursiveCB.isSelected();
		boolean annoPrecedence = annoValuePrecedenceCB.isSelected();
		
		//String language = languageTF.getText();
		String language = ((LangInfo) newLanguageComboBox.getSelectedItem()).getId();
		
		//TODO change the command
		command = new UpdateTranscriptionsForECVCommand(ELANCommandFactory.UPDATE_TRANSCRIPTIONS_FOR_ECV);
		if (command instanceof ProcessReporter) {
			((ProcessReporter) command).setProcessReport(new SimpleReport(ElanLocale.getString("UpdateMTranscriptionsForECVDialog.Title")));
		}
		command.execute(this, new Object[]{sourceFol, destFol, 
				Boolean.valueOf(recursive), language, Boolean.valueOf(annoPrecedence)});
		return true;
	}
	
	private void showWarning(String message) {
		JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
				JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Create the combobox for the list of languages to choose from.
	 * Can be overridden if you don't want a box with all languages
	 * from the LanguageCollection.
	 */
	protected JComboBox getNewLanguageComboBox() {
		JComboBox box = new JComboBox();
	    box.setEditable(false);
	    return box;
	}
	
	/**
	 * Update the "new language" combobox with a list of "all possible"
	 * languages.
	 * Since filling a combobox with a few thousand entries is rather slow,
	 * and we have the entries in a List already anyway, just create a model
	 * that adapts the List.
	 */
	protected void updateNewLanguageComboBox() {
		if(!(newLanguageComboBox instanceof RecentLanguagesBox)) {
			final List<LangInfo> languages = LanguageCollection.getLanguages();
			
			ComboBoxModel m = new LanguagesListModel(languages);
			 // speeds up initial display drastically:
			newLanguageComboBox.setPrototypeDisplayValue(languages.get(0));
			newLanguageComboBox.setModel(m);
		}
	}

	/**
	 * Use a custom model for the NewLanguageDialog to avoid duplicating the whole list
	 * (about 7700 elements) element by element when adding it to the combo box.
	 * We keep one extra element for a user-edited string.
	 * 
	 * @author olasei
	 */
	private static class LanguagesListModel extends AbstractListModel
											implements ComboBoxModel {
		final List<LangInfo> languages;
		final int size;
		Object selectedItem;
		
		LanguagesListModel(List<LangInfo> languages) {
			this.languages = languages;
			this.size = languages.size();
		}
		
		@Override // AbstractListModel
		public int getSize() {
			return size;
		}
	
		@Override // AbstractListModel
		public Object getElementAt(int index) {
			if (index < 0) {
				return "";
			}
			if (index < size) {
				return languages.get(index);
			} else {
				return "";
			}
		}
	
		@Override // ComboBoxModel; copied from DefaultComboBoxModel
		public void setSelectedItem(Object anItem) {
	        if ((selectedItem != null && !selectedItem.equals(anItem)) ||
	                selectedItem == null && anItem != null) {
	            selectedItem = anItem;
	            fireContentsChanged(this, -1, -1);
	        }
		}
	
		@Override // ComboBoxModel
		public Object getSelectedItem() {
			return selectedItem;
		}
	}

	@Override
	public void progressUpdated(Object source, int percent, String message) {
		progressBar.setString(message);
	}

	@Override
	public void progressCompleted(Object source, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void progressInterrupted(Object source, String message) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		new UpdateTranscriptionsForECVDialog(null, false).setVisible(true);	
	}
}
