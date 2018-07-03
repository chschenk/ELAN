package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.ElanLocale;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * An info panel that informs the user that ELAN's close/exit behavior  has
 * been changed after version 2.6.3. Up to that version "Exit" would  only
 * close the window. If it was the last window System.exit() was called. The
 * new behavior is that there is a separate Close item and Exit closes all
 * windows and quits the application.  There is a checkbox that lets the user
 * choose not to be warned again.
 *
 * @author Han Sloetjes, MPI
 */
public class ExitStrategyPane extends JPanel {
    private JLabel messageLabel;
    private JCheckBox showAgainCB;

    /**
     * Creates a new ExitStrategyPane instance
     */
    public ExitStrategyPane() {
        super();
        initComponents();
    }

    private void initComponents() {
        messageLabel = new JLabel("<html>" +
                ElanLocale.getString("Frame.ElanFrame.Exit.Warn1") + "<br>" +
                ElanLocale.getString("Frame.ElanFrame.Exit.Warn2") + "</html>");
        showAgainCB = new JCheckBox(ElanLocale.getString("Message.DontShow"));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(messageLabel, gbc);

        gbc.gridy = 1;
        add(showAgainCB, gbc);
    }
    
    public void setMessage(String text){
    	messageLabel.setText(text);
    }

    /**
     * Returns whether the user wishes to be warned again or not.
     *
     * @return true if the "don't show again" checkbox is checked
     */
    public boolean getDontShowAgain() {
        return showAgainCB.isSelected();
    }
}
