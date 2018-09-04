package mpi.eudico.client.annotator.comments;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.viewer.CommentViewer;

/**
 * A dialog for the user to specify a filter string.
 * The filter is applied to all columns of the table.
 * Only rows which match (i.e. at least one of the columns matches the expression)
 * are shown.
 * The string is in principle a regular expression, but if it fails to parse as such,
 * is used as a literal string (by fully escaping it).
 * 
 * @author olasei
 */
@SuppressWarnings("serial")
public class CommentsFilterDialog extends ClosableDialog implements ActionListener {

	private CommentViewer viewer;
	private JLabel filterLabel;
	private JTextField filterTextField;
	private String filterString;
	private JLabel caseLabel;
	private JCheckBox caseCheckBox;
	private boolean caseSensitive;
	private JButton applyButton;
	private JButton cancelButton;
	
	public CommentsFilterDialog(CommentViewer commentViewer, String initialFilter, boolean caseSensitive) {
        super((Frame)null, true);   // Make it  a modal dialog
		this.viewer = commentViewer;
		this.filterString = initialFilter;
		this.caseSensitive = caseSensitive;
		initComponents();
		postInit();
	}

	private void initComponents() {
		String title = ElanLocale.getString("CommentFilterDialog.Title");
        setTitle(title);
        
        setLayout(new GridBagLayout());
        GridBagConstraints maingbc = new GridBagConstraints();
        maingbc.gridx = 0;
        maingbc.weightx = 1;
        maingbc.fill = GridBagConstraints.HORIZONTAL;
        maingbc.insets = new Insets(20, 20, 20, 20);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints settingsgbc = new GridBagConstraints();
        settingsgbc.insets = new Insets(10, 10, 10, 10);
        settingsgbc.fill = GridBagConstraints.HORIZONTAL;
        settingsgbc.weightx = 1.0;
        settingsgbc.gridx = 0;
        settingsgbc.gridy = 0;

        // Create the block with the text fields
        JPanel textfieldsPanel = new JPanel(new GridBagLayout());

        // Create the filter label and textfield
        // "Filter with regular expression"
        filterLabel = new JLabel(ElanLocale.getString("CommentFilterDialog.FilterWithRegex"));
        filterTextField = new JTextField(filterString, 30);
        filterTextField.addActionListener(this);
        
        // "Case sensitive"
        caseLabel = new JLabel(ElanLocale.getString("CommentFilterDialog.CaseSensitive"));
        caseCheckBox = new JCheckBox("", caseSensitive);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        textfieldsPanel.add(filterLabel, gbc);
        gbc.gridy++;
        textfieldsPanel.add(caseLabel, gbc);
        gbc.gridy++;

        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        textfieldsPanel.add(filterTextField, gbc);
        gbc.gridy++;
        textfieldsPanel.add(caseCheckBox, gbc);
        gbc.gridy++;

        settingsgbc.gridy++;
        settingsPanel.add(textfieldsPanel, settingsgbc);

        add(settingsPanel, maingbc);

        // Create a panel with APPLY and CANCEL buttons below the bordered area.
        JPanel okCancelPanel = new JPanel(new GridBagLayout());

        // Create the "Apply" button
        applyButton = new JButton(ElanLocale.getString("CommentFilterDialog.Apply"));
        applyButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        okCancelPanel.add(applyButton, gbc);

        // Create the "Cancel" button
        cancelButton = new JButton(ElanLocale.getString("CommentFilterDialog.Cancel"));
        cancelButton.addActionListener(this);
        okCancelPanel.add(cancelButton, gbc);

        add(okCancelPanel, maingbc);
	}

	/**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
    	boolean caseSensitive = caseCheckBox.isSelected();
	    if (source == applyButton || source == filterTextField) {
	        viewer.setRegexFilter(filterTextField.getText(), caseSensitive);
	        dispose();
	    } else if (source == cancelButton) {
	    	// Should canceling the dialog remove the filtering?
	        // viewer.setRegexFilter(null, caseSensitive);
	        dispose();
	    }
	}
}
