package mpi.eudico.client.annotator.tier;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.UnsupportedCharsetException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;

/**
 * A dialog showing a table with the annotation comparisons.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CompareAnnotatorResultsDlg extends ClosableDialog implements ActionListener{
	private TableModel model;
	private JButton saveButton;
	private JButton closeButton;
	private JLabel averageLabel;

	/**
	 * @param parent the parent frame
	 * @param model the table model
	 * @throws HeadlessException
	 */
	public CompareAnnotatorResultsDlg(Frame parent, TableModel model)
			throws HeadlessException {
		super(parent, false);
		this.model = model;
		initComponents();
		pack();
		setLocationRelativeTo(parent);
	}
	
	/**
	 * @param parent the parent dialog
	 * @param model the table model 
	 * @throws HeadlessException
	 */
	public CompareAnnotatorResultsDlg(Dialog parent, TableModel model)
			throws HeadlessException {
		super(parent, false);
		this.model = model;
		initComponents();
		pack();
		setLocationRelativeTo(parent);
	}
	
	private void initComponents() {
		JPanel infoPanel = new JPanel(new GridBagLayout());
		JLabel t1Label = new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.Label.FirstTier"));
		JLabel t2Label = new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.Label.SecondTier"));
		JLabel avLabel = new JLabel(ElanLocale.getString("CompareAnnotatorsDialog.Label.Average"));
		averageLabel = new JLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(2, 6, 2, 6);
		infoPanel.add(t1Label, gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		infoPanel.add(new JLabel(model.getColumnName(0)), gbc);// hardcoded for now
		gbc.gridy = 1;
		infoPanel.add(new JLabel(model.getColumnName(3)), gbc);// hardcoded for now
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		infoPanel.add(t2Label, gbc);
		gbc.gridy = 2;
		infoPanel.add(avLabel, gbc);
		gbc.gridx = 1;
		infoPanel.add(averageLabel, gbc);
		
		getContentPane().setLayout(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		getContentPane().add(infoPanel, gbc);
		
		JTable table = new JTable(model);
		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(580, 300));
		table.getTableHeader().setReorderingAllowed(false);
		table.setEnabled(false);
		gbc.insets = new Insets(12, 6, 2, 6);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		getContentPane().add(scroll, gbc);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        saveButton = new JButton(ElanLocale.getString("Button.Save"));
        closeButton = new JButton(ElanLocale.getString("Button.Close"));
        
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
        
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
        gbc.weighty = 0.0;
        getContentPane().add(buttonPanel, gbc);
        
        calcAverage();
	}

	/**
	 * Calculates an average for the values of the last column and 
	 * updates the corresponding label.
	 */
	private void calcAverage() {
		if (model.getRowCount() == 0) {
			averageLabel.setText("0.00");
			return;
		}
		int column = model.getColumnCount() - 1;
		float totalQ = 0f;
		float curVal;
		String val;
		for (int i = 0; i < model.getRowCount(); i++) {
			val = (String) model.getValueAt(i, column);
			try {
				curVal = Float.parseFloat(val);
				totalQ += curVal;
			} catch (NumberFormatException nfe) {
				
			}
		}
		float average = totalQ / model.getRowCount();
		averageLabel.setText(String.valueOf(average));
	}
	
	/**
	 * Saves the table to a tab delimited text file.
	 * 
	 * @throws IOException
	 */
	private void saveModel() throws IOException {
		//prompt for file
		FileChooser chooser = new FileChooser(this);		
		chooser.createAndShowFileAndEncodingDialog(null,  FileChooser.SAVE_DIALOG, FileExtension.TEXT_EXT, "LastUsedExportDir", FileChooser.UTF_8);
		File exportFile = chooser.getSelectedFile();
		String encoding = chooser.getSelectedEncoding();
		if(exportFile != null){
            BufferedWriter writer = null;

            try {
                FileOutputStream out = new FileOutputStream(exportFile);
                OutputStreamWriter osw = null;

                try {
                    osw = new OutputStreamWriter(out, encoding);
                } catch (UnsupportedCharsetException uce) {
                    osw = new OutputStreamWriter(out, "UTF-8");
                }

                writer = new BufferedWriter(osw);
            } catch (Exception ex) {
                // FileNotFound, Security or UnsupportedEncoding exceptions
            	if (writer != null) {
            		writer.close();
            	}
                throw new IOException("Cannot write to file: " + ex.getMessage());
            }
            // write BOM?
            String TAB = "\t";
            String NL = "\n";
            
            for (int i = 0; i < model.getColumnCount(); i++) {
            	writer.write(model.getColumnName(i));
            	if (i < model.getColumnCount() - 1) {
            		writer.write(TAB);
            	}
            }
            writer.write(NL);
            writer.write(NL);
            
            for (int i = 0; i < model.getRowCount(); i++) {
            	for (int j = 0; j < model.getColumnCount(); j++) {
            		writer.write((String) model.getValueAt(i, j));
            		if (j < model.getColumnCount() - 1) {
            			writer.write(TAB);
            		} else {
            			writer.write(NL);
            		}
            	}
            }
            
            writer.write(NL);
            writer.write(ElanLocale.getString("CompareAnnotatorsDialog.Label.Average"));
            for (int i = 0; i < model.getColumnCount() - 1; i++) {
            	writer.write(TAB);
            }
            writer.write(averageLabel.getText());
            
            try {
                writer.close();
            } catch (IOException iioo) {
                iioo.printStackTrace();
            }
		}
	}
	
	/**
	 * The action event handling. Save the table or close the dialog.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeButton) {
			setVisible(false);
			dispose();
		} else if (e.getSource() == saveButton) {
			try {
				saveModel();
			} catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        ElanLocale.getString("ExportDialog.Message.Error") + "\n" +
                        "(" + ioe.getMessage() + ")",
                        ElanLocale.getString("Message.Error"),
                        JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
