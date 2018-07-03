package mpi.eudico.client.annotator.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.TimeSlot;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TimeSlotImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.TimeFormatter;


/**
 * A dialog that lets the user type a shift value for all annotations  (time
 * slots).
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
@SuppressWarnings("serial")
public class ShiftAllDialog extends ClosableDialog implements ActionListener {
    private TranscriptionImpl transcription;
    private JLabel label;
    private JTextField textField;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    /**
     * Creates the dialog.
     *
     * @param transcription DOCUMENT ME!
     */
    public ShiftAllDialog(Transcription transcription) {
        super(ELANCommandFactory.getRootFrame(transcription), true);
        this.transcription = (TranscriptionImpl) transcription;

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the
     * dialog.
     */
    private void initComponents() {
        label = new JLabel();
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
        getContentPane().add(textField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
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
        int h = 130;
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
        setTitle(ElanLocale.getString("ShiftAllDialog.Title"));
        label.setText(ElanLocale.getString("ShiftAllDialog.Label"));
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

            //try {
                //longValue = Long.parseLong(textValue);
                longValue = TimeFormatter.toMilliSeconds(textValue);
            /*} catch (NumberFormatException nfe) {
                textField.setText("");
                Toolkit.getDefaultToolkit().beep();

                return;
            }*/

            if (longValue == 0) {
                closeDialog();

                return;
            }

            if (longValue < 0) {
                if (transcription.getTimeOrder().size() > 0) {
                    Iterator<TimeSlot> en = transcription.getTimeOrder().iterator();
                    long firstAlignedTime = 0;

                    while (en.hasNext()) {
                        TimeSlotImpl ts = (TimeSlotImpl) en.next();

                        if (ts.isTimeAligned()) {
                            firstAlignedTime = ts.getTime();

                            break;
                        }
                    }

                    if (Math.abs(longValue) > firstAlignedTime) {
                        String message = ElanLocale.getString(
                                "ShiftAllDialog.Warn");

                        if (firstAlignedTime == 0) {
                            message += (" " + firstAlignedTime);
                        } else {
                            message += (" -" + firstAlignedTime);
                        }

                        JOptionPane.showMessageDialog(this, message,
                            ElanLocale.getString("Message.Warning"),
                            JOptionPane.WARNING_MESSAGE);

                        return;
                    }
                } else {
                    closeDialog();

                    return;
                }
            }

            closeDialog();

            Command c = ELANCommandFactory.createCommand(transcription,
                    ELANCommandFactory.SHIFT_ALL_ANNOTATIONS);
            c.execute(transcription, new Object[] { new Long(longValue) });
        }
    }
}
