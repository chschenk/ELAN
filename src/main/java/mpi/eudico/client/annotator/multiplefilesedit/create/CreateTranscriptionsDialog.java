package mpi.eudico.client.annotator.multiplefilesedit.create;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.CreateTranscriptionsCommand;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.ProcessReporter;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A dialog to configure the batch wise creation of eaf files for all media files in a domain.
 * 
 * @author Han Sloetjes
 */
public class CreateTranscriptionsDialog extends ClosableDialog implements 
    ActionListener, ChangeListener {
//	private List<File> videoFolders;
//	private List<File> audioFolders;
	private JButton sourceButton;
	private JButton templateButton;
	private JRadioButton sameFolderRB;
	private JRadioButton otherFolderRB;
	private JButton destFolderButton;
	private JCheckBox combineVideoCB;
	private JRadioButton suffixRB;
	private JRadioButton prefixRB;
	private JButton startButton;
	private JButton closeButton;
	private JTextField sourceTF;
	private JTextField templateTF;
	private JTextField destFolderTF;
	private JCheckBox recursiveCB;
	private JCheckBox templateCB;
	private JCheckBox separatorCB;
	private JTextField separatorTF;
	private Command command;

	/**
	 * Constructor with a parent frame
	 * 
	 * @param owner parent 
	 * @throws HeadlessException
	 */
	public CreateTranscriptionsDialog(Frame owner) throws HeadlessException {
		super(owner);
		initComponents();
	}

	/**
	 * Constructor with parent and modal flag.
	 * @param owner parent frame
	 * @param modal modal flag
	 * @throws HeadlessException
	 */
	public CreateTranscriptionsDialog(Frame owner, boolean modal)
			throws HeadlessException {
		super(owner, modal);
		initComponents();
		postInit();
	}

	private void postInit() {
		pack();
		setSize(getWidth() + 60, getHeight());
		setLocationRelativeTo(getParent());	
	}
	
	private void initComponents() {
		setTitle(ElanLocale.getString("Menu.File.MultiEAFCreationToolTip"));
		getContentPane().setLayout(new GridBagLayout());
		JPanel pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		pane.setBorder(new TitledBorder(ElanLocale.getString("TokenizeDialog.Label.Options")));//reuse
		Insets insets = new Insets(4, 6, 0, 6);
		Insets indent = new Insets(4, 26, 0, 6);
		
		JLabel sourceLabel = new JLabel(ElanLocale.getString("CreateMultiEAFDialog.Label.SelectSourceFolder"));
		sourceTF = new JTextField();
		// allow or disallow editing of the textfields?
		sourceTF.setEnabled(false);
		sourceButton = new JButton(ElanLocale.getString("Button.Browse"));
		recursiveCB = new JCheckBox(ElanLocale.getString("CreateMultiEAFDialog.Button.Recursive"));
		templateCB = new JCheckBox(ElanLocale.getString("CreateMultiEAFDialog.Label.SelectTemplate"));
		templateCB.setSelected(true);
		templateTF = new JTextField();
		templateTF.setEnabled(false);
		templateButton = new JButton(ElanLocale.getString("Button.Browse"));
		JLabel transLocationLabel = new JLabel(ElanLocale.getString("CreateMultiEAFDialog.Label.EAFLocation"));
		sameFolderRB = new JRadioButton(ElanLocale.getString("CreateMultiEAFDialog.Button.SameFolder"));
		sameFolderRB.setSelected(true);
		otherFolderRB = new JRadioButton(ElanLocale.getString("CreateMultiEAFDialog.Button.OtherFolder"));
		ButtonGroup fGroup = new ButtonGroup();
		fGroup.add(sameFolderRB);
		fGroup.add(otherFolderRB);
		destFolderButton = new JButton(ElanLocale.getString("Button.Browse"));
		destFolderButton.setEnabled(false);
		destFolderTF = new JTextField();
		destFolderTF.setEnabled(false);
		
		combineVideoCB = new JCheckBox(ElanLocale.getString("CreateMultiEAFDialog.Button.CombineVideos"));
		suffixRB = new JRadioButton(ElanLocale.getString("CreateMultiEAFDialog.Label.Suffix"));
		suffixRB.setSelected(true);
		suffixRB.setEnabled(false);
		prefixRB = new JRadioButton(ElanLocale.getString("CreateMultiEAFDialog.Label.Prefix"));
		prefixRB.setEnabled(false);
		ButtonGroup combGroup = new ButtonGroup();
		combGroup.add(suffixRB);
		combGroup.add(prefixRB);
		separatorCB = new JCheckBox(ElanLocale.getString("CreateMultiEAFDialog.Button.Separator"));
		separatorCB.setEnabled(false);
		separatorTF = new JTextField(4);
		separatorTF.setEnabled(false);
		
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
		gbc.insets = new Insets(4, 26, 6, 6);;
		pane.add(recursiveCB, gbc);
		
		gbc.insets = insets;
		gbc.gridy = 3;
		pane.add(templateCB, gbc);
		
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(4, 6, 6, 6);
		pane.add(templateTF, gbc);
		
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		pane.add(templateButton, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		pane.add(transLocationLabel, gbc);
		
		gbc.gridy = 6;
		gbc.insets = indent;
		pane.add(sameFolderRB, gbc);
		
		gbc.gridy = 7;
		pane.add(otherFolderRB, gbc);
		
		gbc.gridy = 8;
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
		gbc.gridy = 9;
		gbc.gridwidth = 2;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		pane.add(combineVideoCB, gbc);
		
		gbc.gridy = 10;
		gbc.insets = indent;
		pane.add(suffixRB, gbc);
		
		gbc.gridy = 11;
		pane.add(prefixRB, gbc);
		
		gbc.gridy = 12;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		pane.add(separatorCB, gbc);
		
		gbc.gridx = 1;
		gbc.gridwidth = 1;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		pane.add(separatorTF, gbc);
		
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
		
		loadPreferences();
		sourceButton.addActionListener(this);
		templateButton.addActionListener(this);
		destFolderButton.addActionListener(this);
		startButton.addActionListener(this);
		closeButton.addActionListener(this);
		combineVideoCB.addChangeListener(this);
		sameFolderRB.addChangeListener(this);
		otherFolderRB.addChangeListener(this);
		templateCB.addChangeListener(this);
		separatorCB.addChangeListener(this);
	}
	
	private void loadPreferences() {
		String stringPref = Preferences.getString("CreateMultipleEAF.SourcePath", null);
		if (stringPref != null) {
			sourceTF.setText(stringPref); 
		}
		Boolean boolPref = Preferences.getBool("CreateMultipleEAF.Recursive", null);
		if (boolPref != null) {
			recursiveCB.setSelected(boolPref);
		}
		boolPref = Preferences.getBool("CreateMultipleEAF.UseTemplate", null);
		if (boolPref != null) {
			templateCB.setSelected(boolPref);
			templateButton.setEnabled(templateCB.isSelected());
			//templateTF.setEnabled(templateCB.isSelected());
		}
		stringPref = Preferences.getString("CreateMultipleEAF.TemplatePath", null);
		if (stringPref != null) {
			templateTF.setText(stringPref);
		}
		boolPref = Preferences.getBool("CreateMultipleEAF.OtherDestination", null);
		if (boolPref != null) {
			otherFolderRB.setSelected(boolPref);// this sets the state for sameFolderRB as well
			destFolderButton.setEnabled(otherFolderRB.isSelected());
			//destFolderTF.setEnabled(otherFolderRB.isSelected());
		}
		stringPref = Preferences.getString("CreateMultipleEAF.DestinationPath", null);
		if (stringPref != null) {
			destFolderTF.setText(stringPref);
		}
		boolPref = Preferences.getBool("CreateMultipleEAF.CombineVideos", null);
		if (boolPref != null) {
			combineVideoCB.setSelected(boolPref);
			suffixRB.setEnabled(combineVideoCB.isSelected());
			prefixRB.setEnabled(combineVideoCB.isSelected());
			separatorCB.setEnabled(combineVideoCB.isSelected());
			separatorTF.setEnabled(separatorCB.isEnabled() && separatorCB.isSelected());
		}
		boolPref = Preferences.getBool("CreateMultipleEAF.PrefixBased", null);
		if (boolPref != null) {
			if (boolPref) {
				prefixRB.setSelected(true);// this sets the state of suffixRB as well
			} else {
				suffixRB.setSelected(true);// this sets suffixRB as well
			}
		}
		boolPref = Preferences.getBool("CreateMultipleEAF.AffixSeparatorSpecified", null);
		if (boolPref != null) {
			separatorCB.setSelected(boolPref);
			separatorTF.setEnabled(separatorCB.isEnabled() && separatorCB.isSelected());
		}
		stringPref = Preferences.getString("CreateMultipleEAF.AffixSeparator", null);
		if (stringPref != null) {
			separatorTF.setText(stringPref);
		}
	}
	
	private void savePreferences() {
		String sourcePath = sourceTF.getText();
		if (sourcePath != null && sourcePath.length() > 0) {
			Preferences.set("CreateMultipleEAF.SourcePath", sourcePath, null, false, false);
		}

		Preferences.set("CreateMultipleEAF.Recursive", recursiveCB.isSelected(), null, false, false);

		boolean useTemplate = templateCB.isSelected();
		Preferences.set("CreateMultipleEAF.UseTemplate", useTemplate, null, false, false);
		//if (useTemplate) {
			String tempPath = templateTF.getText();
			if (tempPath != null && tempPath.length() > 0) {
				Preferences.set("CreateMultipleEAF.TemplatePath", tempPath, null, false, false);
			}
		//}
		boolean otherDest = otherFolderRB.isSelected();
		Preferences.set("CreateMultipleEAF.OtherDestination", otherDest, null, false, false);
		String destPath = destFolderTF.getText();
		if (destPath != null && destPath.length() > 0) {
			Preferences.set("CreateMultipleEAF.DestinationPath", destPath, null, false, false);
		}
		
		boolean combine = combineVideoCB.isSelected();
		Preferences.set("CreateMultipleEAF.CombineVideos", combine, null, false, false);
		Preferences.set("CreateMultipleEAF.PrefixBased", prefixRB.isSelected(), null, false, false);
		
		String sep = separatorTF.getText();
		if (sep != null && sep.length() > 0) {
			Preferences.set("CreateMultipleEAF.AffixSeparator", sep, null, false, false);
		}
		Preferences.set("CreateMultipleEAF.AffixSeparatorSpecified", separatorCB.isSelected(), null);//saves preferences
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sourceButton) {
			// get source folder
			getSourceFolder();
		} else if (e.getSource() == templateButton) {
			// get template
			getTemplate();
		} else if (e.getSource() == destFolderButton) {
			// destination folder
			getDestinationFolder();
		} else if (e.getSource() == startButton) {
			// create command
			if (create()) {
				savePreferences();
				// show report if any.
				if (command instanceof ProcessReporter) {
					ProcessReport report = ((ProcessReporter) command).getProcessReport();
					if (report != null) {
						ReportDialog rd = new ReportDialog(this, report);
						rd.setVisible(true);
					}
				} else {
					setVisible(false);
					dispose();
				}
			}
		} else if (e.getSource() == closeButton) {
			savePreferences();
			setVisible(false);
			dispose();
		}
		
	}

	
	/**
	 * Change events of radio buttons and checkboxes, update UI.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == combineVideoCB) {
			boolean enable = combineVideoCB.isSelected();
			suffixRB.setEnabled(enable);
			prefixRB.setEnabled(enable);
			separatorCB.setEnabled(enable);
			separatorTF.setEnabled(enable && separatorCB.isSelected());
		} else if (e.getSource() == sameFolderRB) {
			destFolderButton.setEnabled(false);
			//destFolderTF.setEnabled(false);
		} else if (e.getSource() == otherFolderRB) {
			destFolderButton.setEnabled(true);
			//destFolderTF.setEnabled(true);
		} else if (e.getSource() == templateCB) {
			templateButton.setEnabled(templateCB.isSelected());
			//templateTF.setEnabled(templateCB.isSelected());
		} else if (e.getSource() == separatorCB) {
			separatorTF.setEnabled(separatorCB.isSelected());
		}
	}
	
	/**
	 * Prompts for a folder containing media files
	 */
	private void getSourceFolder() {
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("CreateMultiEAFDialog.Label.SelectSourceFolder"), FileChooser.OPEN_DIALOG, 
				ElanLocale.getString("Button.Select"), null, null, true, "MediaDir", 
				FileChooser.DIRECTORIES_ONLY, null);

		File selDir = chooser.getSelectedFile();
		if (selDir != null) {
			sourceTF.setText(selDir.getAbsolutePath());
		}
	}
	
	/**
	 * Prompts the user for a template file
	 */
	private void getTemplate() {
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("CreateMultiEAFDialog.Label.SelectTemplate"), FileChooser.OPEN_DIALOG, FileExtension.TEMPLATE_EXT, "TemplateDir");
		//chooser.setMultiSelectionEnabled(false);
		File selFile = chooser.getSelectedFile();
		if (selFile != null) {
			templateTF.setText(selFile.getAbsolutePath());
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
			showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoSource"));
			sourceButton.requestFocus();
			return false;
		} else {
			// check if it exists
			File f = new File(sourceFol);
			try {
				if (!f.exists() || !f.isDirectory()) {
					showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NonExistSource"));
					sourceButton.requestFocus();
					return false;
				}
			} catch (SecurityException ex) {
				showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoAccessSource"));
				return false;
			}
		}
		String templateFile = templateTF.getText();
		if (templateCB.isSelected()) {
			if (templateFile == null || templateFile.length() == 0) {
				showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoTemplate"));
				templateButton.requestFocus();
				return false;
			} else {
				File f = new File(templateFile);
				try {
					if (!f.exists() || f.isDirectory()) {
						showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NonExistTemplate"));
						templateButton.requestFocus();
						return false;
					}
				} catch (SecurityException ex) {
					showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoAccessTemplate"));
					return false;
				}
			}
		} else {
			templateFile = null;
		}
		String destFol = null;
		if (otherFolderRB.isSelected()) {
			destFol = destFolderTF.getText();
			if (destFol == null || destFol.length() == 0) {				
				showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoDestination"));
				destFolderButton.requestFocus();
				return false;
			} else {
				File f = new File(destFol);
				try {
					if (!f.exists() || !f.isDirectory()) {
						showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NonExistDestination"));
						destFolderButton.requestFocus();
						return false;
					}
				} catch (SecurityException se) {
					showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoAccessDestination"));
					return false;
				}
			}
		}
		String separator = separatorTF.getText();
		if (combineVideoCB.isSelected() && separatorCB.isSelected()) {		
			if (separator == null || separator.length() == 0) {
				showWarning(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoSeparator"));
				separatorTF.requestFocus();
				return false;
			}
		} else {
			separator = null;
		}
		
		boolean recursive = recursiveCB.isSelected();
		boolean combineVideos = combineVideoCB.isSelected();
		boolean prefixBased = prefixRB.isSelected();
		
		command = new CreateTranscriptionsCommand(ELANCommandFactory.CREATE_NEW_MULTI);
		if (command instanceof ProcessReporter) {
			((ProcessReporter) command).setProcessReport(new SimpleReport(ElanLocale.getString("Menu.File.MultiEAFCreationToolTip")));
		}
		command.execute(null, new Object[]{sourceFol, templateFile, destFol, 
				Boolean.valueOf(recursive), Boolean.valueOf(combineVideos), Boolean.valueOf(prefixBased), separator});
		return true;
	}
	
	private void showWarning(String message) {
		JOptionPane.showMessageDialog(this, message, ElanLocale.getString("Message.Warning"), 
				JOptionPane.WARNING_MESSAGE);
	}
}
