package mpi.eudico.client.annotator.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.FileExtension;

public class LogTextFrame extends ClosableFrame implements ActionListener {
	private JButton saveButton;
	private JButton closeButton;
	private JTextArea ta;
	
	/**
	 * @param title
	 * @param log the content of the log
	 * @throws HeadlessException
	 */
	public LogTextFrame(String title, String log) throws HeadlessException {
		super(title);
		setTitle(title);
		initComponents(log);
	}

	/**
	 * 
	 * @param log the contents of the log as a single String
	 */
	private void initComponents(String log) {
		ta = new JTextArea(log);
		ta.setLineWrap(false);
		JScrollPane pane = new JScrollPane(ta);
		pane.setPreferredSize(new Dimension(500, 500));
		
		Insets insets = new Insets(2, 6, 2, 6);
		getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = insets;
		gbc.fill = GridBagConstraints.BOTH;
		
		getContentPane().add(pane, gbc);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		saveButton = new JButton(ElanLocale.getString("Button.Save"));
		saveButton.addActionListener(this);
		
		closeButton = new JButton(ElanLocale.getString("Button.Close"));
		closeButton.addActionListener(this);
		
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);
		
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		getContentPane().add(buttonPanel, gbc);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
			dispose();
		} else if (e.getSource() == saveButton) {			
			FileChooser chooser = new FileChooser(this);
			chooser.createAndShowFileDialog(null, FileChooser.SAVE_DIALOG, FileExtension.TEXT_EXT, "LastUsedExportDir");
		
			File f = chooser.getSelectedFile();				
			if (f != null) {					
				try {
					FileWriter writer = new FileWriter(f);
					writer.write(ta.getText());
					writer.flush();
					writer.close();
				} catch (IOException ioe) {
					ClientLogger.LOG.warning("Could not save log file: " + ioe.getMessage());
				}
			}			
		}		
	}
}
