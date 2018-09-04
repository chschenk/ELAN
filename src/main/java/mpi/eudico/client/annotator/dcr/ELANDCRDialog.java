package mpi.eudico.client.annotator.dcr;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import mpi.dcr.AbstractDCSelectPanel2;
import mpi.dcr.DCSmall;
import mpi.dcr.RemoteDCSelectPanel;
import mpi.dcr.isocat.RestDCRConnector;
import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;

/**
 * A dialog to interact with a local or remote DCR server or cache.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ELANDCRDialog extends ClosableDialog implements ActionListener {
    /** constant for local mode */
    public final static int LOCAL_MODE = 1;

    /** constant for remote mode */
    public final static int REMOTE_MODE = 2;
    private int mode = LOCAL_MODE;

    /** a resource bundle with localized strings */
    protected ResourceBundle bundle = null;

    /** the name of the dcr */
    protected String dcrName;

    /** the location of the dcr */
    protected String dcrLocation;

    /** the title panel */
    protected JPanel titlePanel;

    /** the title label */
    protected JLabel titleLabel;

    /** the subtitle label */
    protected JLabel subtitleLabel;

    /** the second subtitle label */
    protected JLabel subtitleLabel2;

    /** a panel for selection of a (number of) data category */
    protected AbstractDCSelectPanel2 dcPanel;

    /** button panel */
    protected JPanel buttonPanel;

    /** apply button */
    protected JButton applyButton;

    /** the cancel button */
    protected JButton cancelButton;
    private List<DCSmall> value = null; // return value
    
    private final Cursor BUSY_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
   	private final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

    /**
     * Creates a new ELANDCRDialog instance
     *
     * @param owner the owner of the dialog
     * @param modal the modal flag
     * @param mode the mode, local or remote
     *
     * @throws HeadlessException headless exception
     */
    public ELANDCRDialog(Frame owner, boolean modal, int mode)
        throws HeadlessException {
        super(owner, modal);

        if (mode == REMOTE_MODE) {
            this.mode = mode;
        }

        initComponents();
    }

    /**
     * Creates a new ELANDCRDialog instance
     *
     * @param owner the owner of the dialog
     * @param modal the modal flag
     * @param mode the mode, local or remote
     *
     * @throws HeadlessException headless exception
     */
    public ELANDCRDialog(Dialog owner, boolean modal, int mode)
        throws HeadlessException {
        super(owner, modal);

        if (mode == REMOTE_MODE) {
            this.mode = mode;
        }

        initComponents();
    }

    /**
     * Initializes ui components.
     */
    protected void initComponents() {
        bundle = ElanLocale.getResourceBundle();
        getContentPane().setLayout(new GridBagLayout());

        Insets insets = new Insets(2, 6, 2, 6);

        titlePanel = new JPanel(new GridBagLayout());
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel = new JLabel();
        subtitleLabel.setFont(Constants.deriveSmallFont(subtitleLabel.getFont()));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel2 = new JLabel();
        subtitleLabel2.setFont(subtitleLabel.getFont());
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

            // read and apply profiles from preferences, or read all profiles??
        } else {
        	this.getOwner().setCursor(BUSY_CURSOR);
            dcPanel = new LocalDCSPanel(ELANLocalDCRConnector.getInstance(),
                    bundle);
            this.getOwner().setCursor(DEFAULT_CURSOR);
           // ((LocalDCSPanel)dcPanel).setSingleSelection(false);

            // reads stored, local dcs
        }
        dcPanel.setPreferredLanguage(ELANLocalDCRConnector.getInstance().getPreferedLanguage());

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

    /**
     * Applies localized texts.
     */
    protected void updateLocale() {
        if (mode == REMOTE_MODE) {
            titleLabel.setText(ElanLocale.getString("DCR.Label.Remote") + " " +
                dcrName);
            subtitleLabel.setText("(" + dcrLocation + ")");
            subtitleLabel2.setText(ElanLocale.getString("DCR.Label.RemoteHelp"));
        } else {
            titleLabel.setText(ElanLocale.getString("DCR.Label.LocalDCS"));
            subtitleLabel2.setText(ElanLocale.getString(
                    "DCR.Label.LocalDCSHelp"));
        }

        applyButton.setText(ElanLocale.getString("Button.Apply"));
        cancelButton.setText(ElanLocale.getString("Button.Cancel"));
    }

    /**
     * Returns the selected value.
     *
     * @return the selected value, can be null
     */
    public List<DCSmall> getValue() {
        return value;
    }

    /**
     * The action event handling.
     *
     * @param e the action event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyButton) {
            value = dcPanel.getSelectedCategories();
            
            // if local mode- allow only one selection
            // warning
        } else if (e.getSource() == cancelButton) {
            value = null;
        }

        setVisible(false);
        dispose();
    }
}
