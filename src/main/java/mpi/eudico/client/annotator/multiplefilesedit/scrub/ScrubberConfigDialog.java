/**
 * 
 */
package mpi.eudico.client.annotator.multiplefilesedit.scrub;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.ReportDialog;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.util.ProcessReport;
import mpi.eudico.server.corpora.util.SimpleReport;

/**
 * A dialog to configure the transcription scrubber.
 * 
 * @author Han Sloetjes
 */
public class ScrubberConfigDialog extends ClosableDialog implements
		ActionListener, ChangeListener, ProgressListener {
	private JPanel optionsPanel;
	// new lines
	private JCheckBox newLineCB;
	private JCheckBox leadNewLineCB;
	private JCheckBox trailNewLineCB;
	private JCheckBox anyNewLineCB;
	// space
	private JCheckBox spaceCB;
	private JCheckBox leadSpacesCB;
	private JCheckBox trailSpacesCB;
	private JCheckBox multipleSpacesCB;
	// 
	private JCheckBox tabCB;
	private JCheckBox leadTabCB;
	private JCheckBox trailTabCB;
	private JCheckBox anyTabCB;
	
	private JPanel progressPanel;
	private JButton startStopButton;
	private JProgressBar bar;
	private JButton closeButton;
	
	private boolean isRunning = false;
	private List<File> files;
	private TranscriptionScrubber scrubber;
	// options for whitespaces, newline chars, tabs, what else? ASCII control characters?
	
	/**
	 * @param owner the parent frame
	 * @param modal the modal flag
	 * @throws HeadlessException he
	 */
	public ScrubberConfigDialog(Frame owner, boolean modal, List<File> files)
			throws HeadlessException {
		super(owner, modal);
		this.files = files;
		
		initComponents();
		postInit();
	}
	
	private void postInit() {
		pack();
		setSize(getWidth() + 50, getHeight());
		setLocationRelativeTo(getParent());	
	}

	private void initComponents() {
		setTitle(ElanLocale.getString("MFE.Scrubber.Title"));		
		getContentPane().setLayout(new GridBagLayout());
		Insets insets = new Insets(4, 6, 0, 6);
		Insets indent = new Insets(0, 26, 10, 6);
		Insets ins2 = new Insets(0, 6, 10, 0);
		
		optionsPanel = new JPanel(new GridBagLayout());
		optionsPanel.setBorder(new TitledBorder(ElanLocale.getString("TokenizeDialog.Label.Options")));//reuse
		JLabel infoLabel = new JLabel(ElanLocale.getString("MFE.Scrubber.Info"));
		newLineCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.NewLines"));
		newLineCB.setSelected(true);
		newLineCB.addChangeListener(this);
		leadNewLineCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Leading"));
		trailNewLineCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Trailing"));
		anyNewLineCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.All"));
		anyNewLineCB.setSelected(true);
		
		spaceCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Whitespaces"));
		spaceCB.setSelected(true);
		spaceCB.addChangeListener(this);
		leadSpacesCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Leading"));
		trailSpacesCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Trailing"));
		multipleSpacesCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Multiple"));
		multipleSpacesCB.setSelected(true);
		tabCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Tabs"));
		tabCB.addChangeListener(this);
		leadTabCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Leading"));
		leadTabCB.setEnabled(false);
		trailTabCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.Trailing"));
		trailTabCB.setEnabled(false);
		anyTabCB = new JCheckBox(ElanLocale.getString("MFE.Scrubber.All"));
		anyTabCB.setEnabled(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(4, 6, 16, 6);
		optionsPanel.add(infoLabel, gbc);
		
		gbc.insets = insets;
		gbc.gridy = 1;
		optionsPanel.add(newLineCB, gbc);
		gbc.gridy = 3;
		optionsPanel.add(tabCB, gbc);		
		gbc.gridy = 5;
		optionsPanel.add(spaceCB, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridy = 2;
		gbc.insets = indent;
		gbc.gridwidth = 1;
		optionsPanel.add(leadNewLineCB, gbc);
		gbc.gridx = 1;
		gbc.insets = ins2;
		optionsPanel.add(trailNewLineCB, gbc);
		gbc.gridx = 2;
		optionsPanel.add(anyNewLineCB, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.insets = indent;
		optionsPanel.add(leadTabCB, gbc);
		gbc.gridx = 1;
		gbc.insets = ins2;
		optionsPanel.add(trailTabCB, gbc);
		gbc.gridx = 2;
		optionsPanel.add(anyTabCB, gbc);
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.insets = indent;
		optionsPanel.add(leadSpacesCB, gbc);
		gbc.gridx = 1;
		gbc.insets = ins2;
		optionsPanel.add(trailSpacesCB, gbc);
		gbc.gridx = 2;
		optionsPanel.add(multipleSpacesCB, gbc);

		
		progressPanel = new JPanel(new GridBagLayout());
		progressPanel.setBorder(new TitledBorder(ElanLocale.getString("MultipleFileSearch.FindReplace.Progress")));
		bar = new JProgressBar(0, 100);
		bar.setStringPainted(true);
		startStopButton = new JButton(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));// reuse
		startStopButton.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		progressPanel.add(bar, gbc);
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		progressPanel.add(startStopButton, gbc);
		
		closeButton = new JButton(ElanLocale.getString("Button.Close"));
		closeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1, 6, 2));
        buttonPanel.add(closeButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        //gbc.weighty = 1.0;
        gbc.insets = insets;
        getContentPane().add(optionsPanel, gbc);
        
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(progressPanel, gbc);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 2;
        gbc.insets = new Insets(2, 6, 2, 6);
        getContentPane().add(buttonPanel, gbc);
	}
	
	/**
	 * Shows a report of the last scrub run and enables ui elements.
	 */
	private void reset() {
		bar.setValue(0);
		// show report
		if (scrubber != null) {
			ProcessReport report = scrubber.getProcessReport();
			if (report != null) {
				ReportDialog dialog = new ReportDialog(this, report);
				dialog.setVisible(true);
			}
			scrubber.removeProgressListener(this);
		}
		startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
		closeButton.setEnabled(true);
		isRunning = false;
	}
	
	/**
	 * Close button and start/stop button actions.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			if (!isRunning) {
				setVisible(false);
				dispose();
			}
		} else if (e.getSource() == startStopButton) {
			if (isRunning) {
				//stop the process
				if (scrubber != null) {
					scrubber.interrupt();
					// wait for a progress event
				} else {
					startStopButton.setText(ElanLocale.getString("Recognizer.RecognizerPanel.Start"));
					closeButton.setEnabled(true);
					isRunning = false;
				}
			} else {
				closeButton.setEnabled(false);
				startStopButton.setText(ElanLocale.getString("Button.Cancel"));
				// start the process
				isRunning = true;
				scrubber = new TranscriptionScrubber();
				scrubber.addProgressListener(this);
				SimpleReport report = new SimpleReport();
				scrubber.setProcessReport(report);
				Map<Character, boolean[]> filters = new HashMap<Character, boolean[]>(3);
				boolean[] nlFlags = new boolean[3];
				if (newLineCB.isSelected()) {
					nlFlags[0] = leadNewLineCB.isSelected();
					nlFlags[1] = trailNewLineCB.isSelected();
					nlFlags[2] = anyNewLineCB.isSelected();
				} else {
					nlFlags[0] = false;
					nlFlags[1] = false;
					nlFlags[2] = false;
				}
				filters.put('\n', nlFlags);
				boolean[] tabFlags = new boolean[3];
				if (tabCB.isSelected()) {
					tabFlags[0] = leadTabCB.isSelected();
					tabFlags[1] = trailTabCB.isSelected();
					tabFlags[2] = anyTabCB.isSelected();
				} else {
					tabFlags[0] = false;
					tabFlags[1] = false;
					tabFlags[2] = false;
				}
				filters.put('\t', tabFlags);
				boolean[] wspFlags = new boolean[3];
				if (spaceCB.isSelected()) {
					wspFlags[0] = leadSpacesCB.isSelected();
					wspFlags[1] = trailSpacesCB.isSelected();
					wspFlags[2] = multipleSpacesCB.isSelected();
				} else {
					wspFlags[0] = false;
					wspFlags[1] = false;
					wspFlags[2] = false;
				}
				filters.put('\u0020', wspFlags);
				scrubber.scrubAndSave(files, filters);
			}
		}

	}

	/**
	 * Enable/disable relevant ui elements.
	 * 
	 * @param e the change event
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == newLineCB) {
			leadNewLineCB.setEnabled(newLineCB.isSelected());
			trailNewLineCB.setEnabled(newLineCB.isSelected());
			anyNewLineCB.setEnabled(newLineCB.isSelected());
		} else if (e.getSource() == spaceCB) {
			leadSpacesCB.setEnabled(spaceCB.isSelected());
			trailSpacesCB.setEnabled(spaceCB.isSelected());
			multipleSpacesCB.setEnabled(spaceCB.isSelected());
		} else if (e.getSource() == tabCB) {
			leadTabCB.setEnabled(tabCB.isSelected());
			trailTabCB.setEnabled(tabCB.isSelected());
			anyTabCB.setEnabled(tabCB.isSelected());
		}		
	}

	/**
	 * Notification that the process completed.
	 * 
	 * @param source the source of the message
	 * @param message the message
	 */
	@Override
	public void progressCompleted(Object source, String message) {
		reset();
	}

	/**
	 * Notification that the process was interrupted.
	 * 
	 * @param source the source of the message
	 * @param message the message
	 */
	@Override
	public void progressInterrupted(Object source, String message) {
		reset();
	}

	/**
	 * Process update notification.
	 * 
	 * @param source the source of the message
	 * @param message the message
	 */
	@Override
	public void progressUpdated(Object source, int percent, String message) {
		bar.setValue(percent);
		if (message != null) {
			bar.setString(message);
		}
		if (percent >= 100) {
			progressCompleted(source, message);
		}
		
	}

}
