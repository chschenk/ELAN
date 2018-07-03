package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.util.TimeFormatter;


/**
 * A dialog that lets the user type a shift value for annotations within a certain
 * time interval, on a certain tier.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class ShiftAnnotationsDialog extends ClosableDialog implements ActionListener {
    private Transcription transcription;
    private JLabel label;
    private JLabel minMaxLabel;
    private JTextField textField;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;
    
    private long value = 0L;
    private long minValue = 0L;
    private long maxValue = 0L;

    /**
     * Creates the dialog.
     *
     * @param transcription the Transcription
     * @param min the minimum value
     * @param max the maximum value
     */
    public ShiftAnnotationsDialog(Transcription transcription, long min, long max) {
        super(ELANCommandFactory.getRootFrame(transcription), true);
        this.transcription = transcription;
        minValue = min;
        maxValue = max;

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the
     * dialog.
     */
    private void initComponents() {
        label = new JLabel();
        minMaxLabel = new JLabel();
        textField = new JTextField();
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        okButton = new JButton();
        okButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.addActionListener(this);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getContentPane().setLayout(new GridBagLayout());

        Insets inset = new Insets(2, 6, 2, 6);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = inset;
        gbc.weightx = 1.0;

        getContentPane().add(label, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = inset;
        gbc.weightx = 1.0;
        getContentPane().add(minMaxLabel, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = inset;
        gbc.weightx = 1.0;
        getContentPane().add(textField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = inset;
        getContentPane().add(buttonPanel, gbc);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog();
                }
            });

        updateLocale();

        pack();

        int w = 260;
        int h = 180;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);

        setLocationRelativeTo(getParent());
        textField.grabFocus();
        getRootPane().setDefaultButton(okButton);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    private void updateLocale() {
        setTitle(ElanLocale.getString("CommandActions.ShiftAnnotations"));
        label.setText(ElanLocale.getString("ShiftAllDialog.Label"));
        minMaxLabel.setText(minValue + "(ms)  &  " + maxValue +"(ms)");
        okButton.setText(ElanLocale.getString("Button.OK"));
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * Returns the specified value.
     * 
     * @return the value
     */
    public long getValue() {
    	return value;
    }
    
    /**
     * The action performed method.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            closeDialog();
        } else if (e.getSource() == okButton) {
            String textValue = textField.getText().trim();
            long longValue = 0;

            try {
                //longValue = Long.parseLong(textValue);
                longValue = TimeFormatter.toMilliSeconds(textValue);
            } catch (NumberFormatException nfe) {
                textField.setText("");
                Toolkit.getDefaultToolkit().beep();

                return;
            }

            if (longValue == 0) {
            	value = 0;
                closeDialog();

                return;
            }

            if (longValue < minValue) {
                JOptionPane.showMessageDialog(this, 
                		ElanLocale.getString("ShiftAllDialog.Warn") + " " + (minValue - 1),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
                textField.setText("" + minValue);

                    return;
            }
            if (longValue > maxValue) {
                JOptionPane.showMessageDialog(this, 
                		ElanLocale.getString("ShiftAllDialog.Warn2") + " " + (maxValue + 1),
                        ElanLocale.getString("Message.Warning"),
                        JOptionPane.WARNING_MESSAGE);
                textField.setText("" + maxValue);

                    return;
            }
            value = longValue;
            
            closeDialog();
        }
    }
}
