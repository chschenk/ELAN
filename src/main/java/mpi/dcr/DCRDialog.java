package mpi.dcr;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import mpi.dcr.isocat.RestDCRConnector;


/**
 * A dialog to interact with a data category registry.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DCRDialog extends JDialog implements ActionListener {
    /**
     * constant for local mode; interaction with a locally stored dc selection
     */
    public final static int LOCALE_MODE = 1;

    /** constant for remote mode; interaction with a remote registry */
    public final static int REMOTE_MODE = 2;
    private int mode = LOCALE_MODE;
    private ResourceBundle bundle = null;
    private String dcrName;
    private String dcrLocation;
    private JPanel titlePanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel subtitleLabel2;
    private AbstractDCSelectPanel2 dcPanel;
    private JPanel buttonPanel;
    private JButton applyButton;
    private JButton cancelButton;
    private Object value = null; // return value

    /**
     * Creates a new DCRDialog instance
     *
     * @param owner the owner of the dialog
     * @param modal the modal flag
     * @param mode remote or local mode
     *
     * @throws HeadlessException headless exception
     */
    public DCRDialog(Frame owner, boolean modal, int mode)
        throws HeadlessException {
        super(owner, modal);

        if (mode == REMOTE_MODE) {
            this.mode = mode;
        }

        initComponents();
    }

    /**
     * Creates a new DCRDialog instance
     *
     * @param owner the owner of the dialog
     * @param modal the modal flag
     * @param mode remote or local mode
     * @param bundle a resource bundle for localized strings
     *
     * @throws HeadlessException headless exception
     */
    public DCRDialog(Frame owner, boolean modal, int mode, ResourceBundle bundle)
        throws HeadlessException {
        super(owner, modal);

        if (mode == REMOTE_MODE) {
            this.mode = mode;
        }

        this.bundle = bundle;

        initComponents();
    }

    /**
     * Creates a new DCRDialog instance
     *
     * @param owner the owner of the dialog
     * @param modal the modal flag
     * @param mode remote or local mode!
     *
     * @throws HeadlessException headless exception
     */
    public DCRDialog(Dialog owner, boolean modal, int mode)
        throws HeadlessException {
        super(owner, modal);

        if (mode == REMOTE_MODE) {
            this.mode = mode;
        }

        initComponents();
    }

    /**
     * Creates a new DCRDialog instance
     *
     * @param owner the owner of the dialog
     * @param modal the modal flag
     * @param mode remote or local mode
     * @param bundle a resource bundle for localized strings
     *
     * @throws HeadlessException headless exception!
     */
    public DCRDialog(Dialog owner, boolean modal, int mode,
        ResourceBundle bundle) throws HeadlessException {
        super(owner, modal);

        if (mode == REMOTE_MODE) {
            this.mode = mode;
        }

        this.bundle = bundle;

        initComponents();
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titlePanel = new JPanel(new GridBagLayout());
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel = new JLabel();
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont((float) 10));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel2 = new JLabel();
        subtitleLabel2.setFont(subtitleLabel2.getFont().deriveFont((float) 10));
        subtitleLabel2.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        titlePanel.add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = insets;
        titlePanel.add(subtitleLabel, gbc);
        gbc.gridy = 2;
        titlePanel.add(subtitleLabel2, gbc);

        gbc.gridy = 0;
        getContentPane().add(titlePanel, gbc);

        if (mode == REMOTE_MODE) {
            RestDCRConnector conn = new RestDCRConnector();
            dcrName = conn.getName();
            dcrLocation = conn.getDCRLocation();
            dcPanel = new RemoteDCSelectPanel(conn, bundle);
        } else {
            dcPanel = new LocalDCSelectPanel(new LocalDCRConnector(), bundle);
        }

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 1;
        gbc.insets = insets;
        getContentPane().add(dcPanel, gbc);

        // buttons
        buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
        applyButton = new JButton();
        applyButton.addActionListener(this);
        cancelButton = new JButton();
        cancelButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        gbc = new GridBagConstraints();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = insets;
        getContentPane().add(buttonPanel, gbc);
        updateLocale();
    }

    private void updateLocale() {
        if (mode == REMOTE_MODE) {
            if (bundle != null) {
                titleLabel.setText(bundle.getString("DCR.Label.Remote") + " " +
                    dcrName);
                subtitleLabel.setText("(" + dcrLocation + "");
                subtitleLabel2.setText(bundle.getString("DCR.Label.RemoteHelp"));
            } else {
                titleLabel.setText("Remote DCR: " + dcrName);
                subtitleLabel.setText("(" + dcrLocation + "");
                subtitleLabel2.setText(
                    "First add and select a profile, next select one or more data categories.");
            }
        } else {
            if (bundle != null) {
                titleLabel.setText(bundle.getString("DCR.Label.LocalDCS"));
                subtitleLabel2.setText(bundle.getString(
                        "DCR.Label.LocalDCSHelp"));
            } else {
                titleLabel.setText("Local Data Category Selection");
                subtitleLabel2.setText(
                    "Select one or all profiles, then select a data category.");
            }
        }

        if (bundle != null) {
            applyButton.setText(bundle.getString("Button.Apply"));
            cancelButton.setText(bundle.getString("Button.Cancel"));
        } else {
            applyButton.setText("Apply");
            cancelButton.setText("Cancel");
        }
    }

    /**
     * Returns the selected value
     *
     * @return the selected value or null
     */
    public Object getValue() {
        return value;
    }

    /**
     * Button action event handling
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            value = dcPanel.getSelectedCategories();
        } else if (e.getSource() == cancelButton) {
            value = null;
        }

        setVisible(false);
        dispose();
    }
}
