package mpi.eudico.client.annotator.md;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.gui.ClosableDialog;

import mpi.eudico.client.annotator.md.spi.MDConfigurationPanel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * Shows a configuration panel for metadata configuration.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class MDConfigurationDialog extends ClosableDialog
    implements ActionListener {
    private MDConfigurationPanel panel;
    private JButton applyButton;
    private JButton cancelButton;

    /**
     * Creates a new MDConfigurationDialog instance
     *
     *@param owner the parent
     * @param panel the config panel
     *
     * @throws HeadlessException
     */
    public MDConfigurationDialog(Frame owner, MDConfigurationPanel panel)
        throws HeadlessException {
        super(owner);
        this.panel = panel;
        setModal(true);
        setTitle(ElanLocale.getString("MetadataViewer.Configure"));
        initComponents();
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 4));

        applyButton = new JButton(ElanLocale.getString("Button.Apply"));
        applyButton.addActionListener(this);
        buttonPanel.add(applyButton);
        cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = insets;
        getContentPane().add(panel, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridy = 1;
        getContentPane().add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == cancelButton) {
            setVisible(false);
            dispose();
        } else if (ae.getSource() == applyButton) {
            panel.applyChanges();
            setVisible(false);
            dispose();
        }
    }
}
