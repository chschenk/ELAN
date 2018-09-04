package mpi.eudico.client.annotator.multiplefilesedit;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.UpdateTranscriptionsWithTemplateCommand;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.gui.MFDomainDialog;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.search.viewer.EAFMultipleFileUtilities;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A dialog to configure a process of updating multiple files (a corpus) 
 * with new or updated items in a template file. 
 * The dialog has a pre-flight or dry run option that allows to check first
 * before applying changes to the corpus.
 * 
 * @author Han Sloetjes
 *
 * July, 2018
 */
@SuppressWarnings("serial")
public class UpdateTranscriptionsWithTemplateDialog extends ClosableDialog
		implements ActionListener, ProgressListener {
	private JButton loadDomainButton;
	private JButton selectTemplateButton;
	private JCheckBox checkOnlyCB;
	private JCheckBox replaceCVCB;
	private JButton startStopButton;
	private JButton closeButton;
	private JList fileList;
	private DefaultListModel fileListModel;
	private JTextField templatePathTF;
	private JProgressBar progressBar;
	
	private UpdateTranscriptionsWithTemplateCommand command;
	private File templateFile;
	private List<File> files;
	private boolean inProgress = false;

	/**
	 * Constructor.
	 * @param owner the parent window
	 * @throws HeadlessException
	 */
	public UpdateTranscriptionsWithTemplateDialog(Frame owner)
			throws HeadlessException {
		this(owner, true);
	}

	/**
	 * Constructor.
	 * @param owner the parent window
	 * @param modal the modal flag
	 * @throws HeadlessException
	 */
	public UpdateTranscriptionsWithTemplateDialog(Frame owner, boolean modal)
			throws HeadlessException {
		super(owner, modal);
		initComponents();

		posiInit();
	}
	
	private void posiInit() {
		pack();
		if (getParent() != null) {
			Dimension parentSize = getParent().getSize();
			Dimension curSize = getSize();
			int w = Math.max(curSize.width, parentSize.width / 2);
			int h = Math.max(curSize.height, (parentSize.height / 3) * 2);
			setSize(w, h);
		}
		setLocationRelativeTo(getParent());
	}
	
	private void initComponents() {
		setTitle(ElanLocale.getString(ELANCommandFactory.UPDATE_TRANSCRIPTIONS_WITH_TEMPLATE));
		getContentPane().setLayout(new GridBagLayout());
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridBagLayout());
		optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("TokenizeDialog.Label.Options")));//reuse
		JPanel progressPanel = new JPanel(new GridBagLayout());
		progressPanel.setBorder(new TitledBorder(ElanLocale.getString("MultipleFileSearch.FindReplace.Progress")));// reuse
		JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
		Insets insets = new Insets(4, 6, 0, 6);
		Insets vertIndent = new Insets(12, 6, 0, 6);
		
		loadDomainButton = new JButton(ElanLocale.getString("MFE.DomainDefKey"));// reuse FileAndTierSelectionStepPane.Button.Domain
		fileListModel = new DefaultListModel();
		fileListModel.addElement(ElanLocale.getString("MultiFileImport.Step1.NoFiles"));
		fileList = new JList(fileListModel);
		fileList.setEnabled(false);
		JScrollPane fileListScrollPane = new JScrollPane(fileList);
		fileListScrollPane.setPreferredSize(new Dimension(400, 60));
		
		selectTemplateButton = new JButton(ElanLocale.getString("UpdateMultipleTranscriptionsWithTemplate.SelectTemplate"));
		templatePathTF = new JTextField(ElanLocale.getString("CreateMultiEAFDialog.Warning.NoTemplate"));
		templatePathTF.setEnabled(false);
		templatePathTF.setEditable(false);
		
		checkOnlyCB = new JCheckBox(ElanLocale.getString("UpdateMultipleTranscriptionsWithTemplate.CheckAndReport"));
		replaceCVCB = new JCheckBox(ElanLocale.getString("UpdateMultipleTranscriptionsWithTemplate.ReplaceCV"));
		
		startStopButton = new JButton(ElanLocale.getString("Button.Start"));
		startStopButton.setActionCommand("start");
		startStopButton.setEnabled(false);
		closeButton = new JButton(ElanLocale.getString("Button.Close"));

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		
		// layout panels and content pane
		// options panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = insets;
		optionsPanel.add(loadDomainButton, gbc);
		
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		optionsPanel.add(fileListScrollPane, gbc);
		
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = vertIndent;
		optionsPanel.add(selectTemplateButton, gbc);
		
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = insets;
		optionsPanel.add(templatePathTF, gbc);
		
		gbc.gridy = 4;
//		gbc.fill = GridBagConstraints.HORIZONTAL;
//		gbc.weightx = 1.0;
		gbc.insets = vertIndent;
		optionsPanel.add(checkOnlyCB, gbc);
		
		gbc.gridy = 5;
		gbc.insets = insets;
		optionsPanel.add(replaceCVCB, gbc);
		
		// progress panel
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		progressPanel.add(progressBar, gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		progressPanel.add(startStopButton, gbc);
		
		// button panel
		buttonPanel.add(closeButton);
		// layout contents pane
		GridBagConstraints cbc = new GridBagConstraints();
		cbc.anchor = GridBagConstraints.NORTHWEST;
		cbc.fill = GridBagConstraints.BOTH;
		cbc.weightx = 1.0;
		cbc.weighty = 1.0;
		cbc.insets = insets;//new Insets(2, 4, 2, 4);
		getContentPane().add(optionsPanel, cbc);
		
		cbc.gridy = 1;
		cbc.fill = GridBagConstraints.HORIZONTAL;
		cbc.weighty = 0.0;
		getContentPane().add(progressPanel, cbc);
		
		cbc.gridy = 2;
		cbc.insets  = new Insets(4, 6, 4, 6);
		cbc.anchor = GridBagConstraints.CENTER;
		cbc.fill = GridBagConstraints.NONE;
		cbc.weightx = 0.0;
		getContentPane().add(buttonPanel, cbc);
		
		loadDomainButton.addActionListener(this);
		selectTemplateButton.addActionListener(this);
		startStopButton.addActionListener(this);
		closeButton.addActionListener(this);
		
		loadPreferences();
	}

	/**
	 * Implementation of the button actions.
	 * @param e the action event
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startStopButton) {
			if ("start".equals(e.getActionCommand())) {
				if (!inProgress) {
					start();
				}
			} else {
				if (inProgress) {
					stop();
				}
			}
		} else if (e.getSource() == loadDomainButton) {
			// load domain
			loadDomain();
			if (templateFile != null && files != null && !files.isEmpty()) {
				startStopButton.setEnabled(true);
			}
		} else if (e.getSource() == selectTemplateButton) {
			// select template file
			getTemplate();
			if (templateFile != null && files != null && !files.isEmpty()) {
				startStopButton.setEnabled(true);
			}
		} else if (e.getSource() == closeButton) {
			// check if running?
			if (!inProgress) {
				savePreferences();
				setVisible(false);
				dispose();
			} else {
				// warn
			}
		}

	}
	
	private void start() {
		if (templateFile != null && files != null && !files.isEmpty()) {
		
			closeButton.setEnabled(false);
			startStopButton.setText(ElanLocale.getString("Button.Stop"));
			startStopButton.setActionCommand("stop");
			command = new UpdateTranscriptionsWithTemplateCommand(
					ELANCommandFactory.UPDATE_TRANSCRIPTIONS_WITH_TEMPLATE);
			command.setProcessReport(new SimpleReport());
			command.setProgressListener(this);
			command.execute(null, new Object[]{files, templateFile, 
					Boolean.valueOf(checkOnlyCB.isSelected()), 
					Boolean.valueOf(replaceCVCB.isSelected())});
			inProgress = true;
		}
	}
	
	/**
	 * Stops the ongoing process. This should result in a call to 
	 * {@link #progressInterrupted(Object, String)}, which resets the UI etc.
	 */
	private void stop() {
		if (command != null) {
			command.setCancelled(true);
			inProgress = false;
		}
	}
	
	private void finished() {
		showReport();
		closeButton.setEnabled(true);
		startStopButton.setText(ElanLocale.getString("Button.Start"));
		startStopButton.setActionCommand("start");
		progressBar.setValue(progressBar.getMinimum());
		progressBar.setString("");
		inProgress = false;
	}
	
	/**
	 * Prompts the user for a template file
	 */
	private void getTemplate() {
		FileChooser chooser = new FileChooser(this);
		chooser.createAndShowFileDialog(ElanLocale.getString("CreateMultiEAFDialog.Label.SelectTemplate"), 
				FileChooser.OPEN_DIALOG, FileExtension.TEMPLATE_EXT, "TemplateDir");
		//chooser.setMultiSelectionEnabled(false);
		File selFile = chooser.getSelectedFile();
		if (selFile != null && selFile.exists()) {
			templateFile = selFile;
			String path = selFile.getAbsolutePath();
			try {
				String name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
				templatePathTF.setText(String.format("%s  [%s]", name, 
						path.substring(0, path.lastIndexOf(File.separatorChar))));				
			} catch (Throwable t) {
				templatePathTF.setText(path);
			}
			templatePathTF.setEnabled(true);
		}
	}
	
	/**
	 * Prompts to load a domain (multiple files, a corpus) and shows the list
	 * of files.
	 */
	private void loadDomain() {        
    	MFDomainDialog mfDialog = new MFDomainDialog(this, 
    			ElanLocale.getString("MultipleFileSearch.SearchDomain"), true);
        mfDialog.setVisible(true);
        List<String> searchDirs = mfDialog.getSearchDirs();
        List<String> searchPaths = mfDialog.getSearchPaths();
        File[] uniqueFiles = EAFMultipleFileUtilities.getUniqueEAFFilesIn(searchDirs, searchPaths);
        if (files != null) {
        	files.clear();
        } else {
        	files = new ArrayList<File>();
        }
        fileListModel.clear();
        
        for (File f : uniqueFiles) {
        	files.add(f);
        	fileListModel.addElement(String.format("<html><b>%s</b> - %s</html>", f.getName(), f.getAbsolutePath()));
        	//fileListModel.addElement(f);
        }
        
	}
	
	/**
	 * Shows the report in a new window. The output can be saved to file.
	 */
	private void showReport() {
		if (command != null) {
			SimpleReport report = (SimpleReport) command.getProcessReport();
			if (report != null) {
				// show dialog
				new ReportDialog(this, report).setVisible(true);
			}
		}
	}
	
	private void loadPreferences() {
		String tempPath = Preferences.getString("UpdateTranscriptionsWithTemplate.TemplatePath", null);
		if (tempPath != null && !tempPath.isEmpty()) {
			templateFile = new File(tempPath);
			if (templateFile.exists()) {
				try {
					String name = tempPath.substring(tempPath.lastIndexOf(File.separatorChar) + 1);
					templatePathTF.setText(String.format("%s  [%s]", name, 
							tempPath.substring(0, tempPath.lastIndexOf(File.separatorChar))));				
				} catch (Throwable t) {
					templatePathTF.setText(tempPath);
				}
				templatePathTF.setEnabled(true);
			}
		}
		
		Boolean dryRunFlag = Preferences.getBool("UpdateTranscriptionsWithTemplate.DryRun", null);
		if (dryRunFlag != null) {
			checkOnlyCB.setSelected(dryRunFlag.booleanValue());
		}
		
		Boolean replaceCVFlag = Preferences.getBool(
				"UpdateTranscriptionsWithTemplate.ForceReplaceCVs", null);
		if (replaceCVFlag != null) {
			replaceCVCB.setSelected(replaceCVFlag.booleanValue());
		}
	}
	
	private void savePreferences() {
		if (templateFile != null) {
			String tp = templateFile.getAbsolutePath();
			Preferences.set("UpdateTranscriptionsWithTemplate.TemplatePath", tp, null, false, false);
		}
		Preferences.set("UpdateTranscriptionsWithTemplate.DryRun", 
				Boolean.valueOf(checkOnlyCB.isSelected()), null, false, false);
		Preferences.set("UpdateTranscriptionsWithTemplate.ForceReplaceCVs", 
				Boolean.valueOf(replaceCVCB.isSelected()), null, false, false);
	}
	
	@Override
	public void progressUpdated(Object source, int percent, String message) {
		progressBar.setValue(percent);
		progressBar.setString(message);
	}

	@Override
	public void progressCompleted(Object source, String message) {
		progressBar.setValue(progressBar.getMaximum());
		progressBar.setString(message);
		finished();
	}

	@Override
	public void progressInterrupted(Object source, String message) {
		// reset to minimum?
		progressBar.setString(message);
		finished();
	}

}
