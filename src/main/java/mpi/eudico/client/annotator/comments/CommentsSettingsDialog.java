package mpi.eudico.client.annotator.comments;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileUtility;
import mpi.eudico.client.annotator.viewer.CommentViewer;
import mpi.eudico.server.corpora.clom.Transcription;

@SuppressWarnings("serial")
public class CommentsSettingsDialog extends ClosableDialog implements ActionListener {
    private static final String NONE = "-";

    private String sharedDirectoryLocation = null;
    private Boolean useSharedDirectory = false;
    private String searchCommentsDirectory = null;
    private String searchEAFDirectory = null;
    private String threadID;
    private String senderEmailAddress;
    private String recipientEmailAddress;
    private String initials;
    private String serverURL;
    private String serverLoginName;

    private DirPanel sharedDirPanel;
	private JCheckBox useSharedDirBox;
	private DirPanel searchEAFPanel;
	private DirPanel searchCommentsPanel;

    private Transcription transcription;
    private JLabel senderLabel;
    private JTextField senderTextField;
    private JLabel recipientLabel;
    private JTextField recipientTextField;
    private JLabel initialsLabel;
    private JTextField initialsTextField;
	private JLabel threadIdLabel;
	private JTextField threadIdTextField;
    private JLabel serverURLLabel;
    private JTextField serverURLTextField;
    private JLabel serverLoginNameLabel;
    private JTextField serverLoginNameTextField;
	private JLabel updatetimeLabel;
	private JSlider updatetimeSlider;
	private int updatetimeValue = 10;
    private JButton applyButton;
    private JButton cancelButton;

    public CommentsSettingsDialog(Transcription transcription) {
        super((Frame)null, true);   // Make it  a modal dialog
        this.transcription = transcription;
        readPrefs();
        initComponents();
        postInit();
    }

    private void initComponents() {
    	String title = ElanLocale.getString("CommentSettingsDialog.Title");
        setTitle(title);
        
        setLayout(new GridBagLayout());
        GridBagConstraints maingbc = new GridBagConstraints();
        maingbc.gridx = 0;
        maingbc.weightx = 1;
        maingbc.fill = GridBagConstraints.HORIZONTAL;
        maingbc.insets = new Insets(20, 10, 20, 20);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints settingsgbc = new GridBagConstraints();
        settingsgbc.insets = new Insets(10, 10, 0, 10);
        settingsgbc.fill = GridBagConstraints.HORIZONTAL;
        settingsgbc.weightx = 1.0;
        settingsgbc.gridx = 0;
        settingsgbc.gridy = 0;

        // create panel for set directory
        // "Location of shared directory"
        sharedDirPanel = new DirPanel(ElanLocale.getString(
                "CommentSettingsDialog.Sharedir.DefaultLoc"),
                sharedDirectoryLocation);
        settingsPanel.add(sharedDirPanel, settingsgbc);
        
        useSharedDirBox = new JCheckBox(ElanLocale.getString(
                "CommentSettingsDialog.Sharedir.UseShared"));
        useSharedDirBox.setSelected(useSharedDirectory);
        sharedDirectoryChanged(sharedDirectoryLocation);
        settingsgbc.gridy++;
        settingsPanel.add(useSharedDirBox, settingsgbc);

        settingsgbc.gridy++; // "Search Comments in"
        searchCommentsPanel = new DirPanel(ElanLocale.getString(
                "CommentSettingsDialog.SearchComments.DefaultLoc"),
                searchCommentsDirectory);
        settingsPanel.add(searchCommentsPanel, settingsgbc);

        settingsgbc.gridy++; // "Search EAF files in"
        searchEAFPanel = new DirPanel(ElanLocale.getString(
                "CommentSettingsDialog.SearchEAF.DefaultLoc"),
                searchEAFDirectory);
        settingsPanel.add(searchEAFPanel, settingsgbc);

        // Create the block with the default values for comments
        JPanel commentValuesPanel = new JPanel(new GridBagLayout());
        //"Default comment field values"
        commentValuesPanel.setBorder(BorderFactory.createTitledBorder(ElanLocale.getString(
                "CommentSettingsDialog.DefaultFieldValues")));

        // Create the email labels and textfields
        senderLabel = new JLabel(ElanLocale.getString(
                "CommentSettingsDialog.Sender"));
        senderTextField = new JTextField(senderEmailAddress);
        recipientLabel = new JLabel(ElanLocale.getString(
                "CommentSettingsDialog.Recipient"));
        recipientTextField = new JTextField(recipientEmailAddress);
        initialsLabel = new JLabel(ElanLocale.getString(
                "CommentSettingsDialog.Initials"));
        initialsTextField = new JTextField(initials);
        threadIdLabel = new JLabel(ElanLocale.getString(
                "CommentSettingsDialog.ThreadID"));
        threadIdTextField = new JTextField(threadID);
        serverURLLabel = new JLabel(ElanLocale.getString(
                "CommentSettingsDialog.ServiceURL"));
        serverURLTextField = new JTextField(serverURL);

        // left column
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        commentValuesPanel.add(senderLabel, gbc);
        gbc.gridy++;
        commentValuesPanel.add(recipientLabel, gbc);
        gbc.gridy++;
        commentValuesPanel.add(initialsLabel, gbc);
        gbc.gridy++;
        commentValuesPanel.add(threadIdLabel, gbc);

        // Right column
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        commentValuesPanel.add(senderTextField, gbc);
        gbc.gridy++;
        commentValuesPanel.add(recipientTextField, gbc);
        gbc.gridy++;
        commentValuesPanel.add(initialsTextField, gbc);
        gbc.gridy++;
        commentValuesPanel.add(threadIdTextField, gbc);
        
        settingsgbc.gridy++;
        settingsPanel.add(commentValuesPanel, settingsgbc);

        // Create the settings for the server and locally stored comments
        JPanel serverValuesPanel = new JPanel(new GridBagLayout());
        //"Stored Comments"
        serverValuesPanel.setBorder(BorderFactory.createTitledBorder(ElanLocale.getString(
                "CommentSettingsDialog.StoredComments")));
  
        if (CommentViewer.USE_WEB_SERVICE) {
	        serverLoginNameLabel = new JLabel(ElanLocale.getString(
	                "CommentSettingsDialog.ServerLoginName"));
	        serverLoginNameTextField = new JTextField(serverLoginName);
        }
        // Create the update time slider
        updatetimeLabel = new JLabel(ElanLocale.getString(
                "CommentSettingsDialog.UpdateTime"));
        updatetimeSlider = new JSlider(2, 60, updatetimeValue); // range: 2 - 60 minutes
        updatetimeSlider.setMajorTickSpacing(10);
        updatetimeSlider.setMinorTickSpacing(2);
        updatetimeSlider.setPaintTicks(true);
        updatetimeSlider.setPaintLabels(true);
        // The major ticks and labels look a bit weird: 2, 12, 22, .. 52.
        
        // Left column
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        if (CommentViewer.USE_WEB_SERVICE) {
            serverValuesPanel.add(serverURLLabel, gbc);
	        gbc.gridy++;
	        serverValuesPanel.add(serverLoginNameLabel, gbc);
            gbc.gridy++;
        }
        serverValuesPanel.add(updatetimeLabel, gbc);

        // Right column
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1;
        if (CommentViewer.USE_WEB_SERVICE) {
	        serverValuesPanel.add(serverURLTextField, gbc);
	        gbc.gridy++;
	        serverValuesPanel.add(serverLoginNameTextField, gbc);
	        gbc.gridy++;
        }
        serverValuesPanel.add(updatetimeSlider, gbc);

        settingsgbc.gridy++;
        settingsPanel.add(serverValuesPanel, settingsgbc);

        add(settingsPanel, maingbc);

        // Create a panel with APPLY and CANCEL buttons below the bordered area.
        JPanel okCancelPanel = new JPanel(new GridBagLayout());

        // Create the "Apply" button
        applyButton = new JButton(ElanLocale.getString(
                "CommentSettingsDialog.Apply"));
        applyButton.addActionListener(this);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        okCancelPanel.add(applyButton, gbc);

        // Create the "Cancel" button
        cancelButton = new JButton(ElanLocale.getString(
                "CommentSettingsDialog.Cancel"));
        cancelButton.addActionListener(this);
        okCancelPanel.add(cancelButton, gbc);

        maingbc.insets = new Insets(0, 10, 20, 20);
        add(okCancelPanel, maingbc);
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();
        setLocationRelativeTo(getParent());
    }

    /**
     * Action event handling
     *
     * @param e the event
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == applyButton) {
            savePrefs();
            dispose();
        } else if (source == cancelButton) {
            dispose();
        }
    }
    
    public void sharedDirectoryChanged(String value) {
    	if (value != null && !value.isEmpty()) {
    		useSharedDirBox.setEnabled(true);
    	} else {
    		useSharedDirectory = false;
    		useSharedDirBox.setSelected(false);
    		useSharedDirBox.setEnabled(false);
    	}
    }

    private void readPrefs() {
        String stringPref = Preferences.getString(CommentManager.SHARED_DIRECTORY_LOCATION, null);
        if (stringPref != null) {
            sharedDirectoryLocation = stringPref;
        }

        // Per-transcription preference
        Boolean boolPref = Preferences.getBool(CommentManager.USE_SHARED_DIRECTORY, transcription);
        if (boolPref != null) {
            useSharedDirectory = boolPref;
        }

        stringPref = Preferences.getString(CommentManager.SEARCH_EAF_DIRECTORY, null);
        if (stringPref != null) {
            searchEAFDirectory = stringPref;
        }

        stringPref = Preferences.getString(CommentManager.SEARCH_COMMENTS_DIRECTORY, null);
        if (stringPref != null) {
            searchCommentsDirectory = stringPref;
        }

        stringPref = Preferences.getString(CommentManager.SENDER_EMAIL_ADDRESS, null);
        if (stringPref != null) {
            senderEmailAddress = stringPref;
        }

        stringPref = Preferences.getString(CommentManager.RECIPIENT_EMAIL_ADDRESS, null);
        if (stringPref != null) {
            recipientEmailAddress = stringPref;
        }
        
        stringPref = Preferences.getString(CommentManager.INITIALS, null);
        if (stringPref != null) {
            initials = stringPref;
        }

        stringPref = Preferences.getString(CommentManager.THREAD_ID, null);
        if (stringPref != null) {
            threadID = stringPref;
        }

        stringPref = Preferences.getString(CommentManager.SERVER_URL, null);
        if (stringPref != null) {
            serverURL = stringPref;
        } else {
        	serverURL = CommentManager.SERVER_URL_DEFAULT;
        }
        
        stringPref = Preferences.getString(CommentManager.SERVER_LOGIN_NAME, null);
        if (stringPref != null) {
            serverLoginName = stringPref;
        }

        Integer intPref = Preferences.getInt(CommentManager.UPDATE_CHECK_TIME, null);
        if (intPref != null) {
        	updatetimeValue = intPref;
        }
    }

    private void savePrefs() {
    	sharedDirectoryLocation = sharedDirPanel.getDirectory();
        Preferences.set(CommentManager.SHARED_DIRECTORY_LOCATION, sharedDirectoryLocation, null, false, false);

        useSharedDirectory = useSharedDirBox.isSelected();
        Preferences.set(CommentManager.USE_SHARED_DIRECTORY, Boolean.valueOf(useSharedDirectory), transcription, false, false);

        searchCommentsDirectory = searchCommentsPanel.getDirectory();
        Preferences.set(CommentManager.SEARCH_COMMENTS_DIRECTORY, searchCommentsDirectory, null, false, false);

    	searchEAFDirectory = searchEAFPanel.getDirectory();
        Preferences.set(CommentManager.SEARCH_EAF_DIRECTORY, searchEAFDirectory, null, false, false);

        senderEmailAddress = senderTextField.getText();
        Preferences.set(CommentManager.SENDER_EMAIL_ADDRESS, senderEmailAddress, null, false, false);

        recipientEmailAddress = recipientTextField.getText();
        Preferences.set(CommentManager.RECIPIENT_EMAIL_ADDRESS, recipientEmailAddress, null, false, false);

        initials = initialsTextField.getText();
        Preferences.set(CommentManager.INITIALS, initials, null, false, false);

        threadID = threadIdTextField.getText();
        Preferences.set(CommentManager.THREAD_ID, threadID, null, false, false);

        serverURL = serverURLTextField.getText();
        Preferences.set(CommentManager.SERVER_URL, serverURL, null, false, false);

        serverLoginName = serverLoginNameTextField.getText();
        Preferences.set(CommentManager.SERVER_LOGIN_NAME, serverLoginName, null);

        updatetimeValue = updatetimeSlider.getValue(); // notify listeners
        Preferences.set(CommentManager.UPDATE_CHECK_TIME, Integer.valueOf(updatetimeValue), null, true, false);        
    }
    
    /**
     * A small panel for choosing a directory, or resetting the choice.
     * We need 3 of them here so a class makes sense.
     *  
     * @author olasei
     */
    private class DirPanel extends JPanel implements ActionListener {
    	private String textlabel;

    	private JLabel setDirLabel;
        private JLabel curDirLabel;
        private JButton defaultDirButton;
        private JButton resetDirButton;
		private Font plainFont;
		private String directoryLocation;

        DirPanel(String textlabel, String dirlabel) {
        	super(new GridBagLayout());
        	
        	if (dirlabel == null || dirlabel.isEmpty()) {
        		dirlabel = NONE;
        	}
        	
            // create panel for set directory
        	this.textlabel = textlabel;
        	directoryLocation = dirlabel;
            setDirLabel = new JLabel(textlabel);
            
            plainFont = setDirLabel.getFont().deriveFont(Font.PLAIN);
            setDirLabel.setFont(plainFont);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            add(setDirLabel, gbc);

            curDirLabel = new JLabel(directoryLocation);
            curDirLabel.setFont(Constants.deriveSmallFont(curDirLabel.getFont()));
            gbc.gridy = 1;
            add(curDirLabel, gbc);

            defaultDirButton = new JButton(ElanLocale.getString("Button.Browse"));
            gbc.gridy = 0;
            gbc.gridx = 1;
            gbc.gridheight = 2;
            //gbc.insets = leftInset;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            add(defaultDirButton, gbc);
            defaultDirButton.addActionListener(this);

            resetDirButton = new JButton();
            ImageIcon resetIcon = null;
            // add reset icon
            try {
                resetIcon = new ImageIcon(this.getClass()
                                              .getResource("/mpi/eudico/client/annotator/resources/Remove.gif"));
                resetDirButton.setIcon(resetIcon);
            } catch (Exception ex) {
                resetDirButton.setText("X");
            }

            resetDirButton.setToolTipText(ElanLocale.getString(
                    "PreferencesDialog.Reset"));
            resetDirButton.setPreferredSize(new Dimension(
                    resetDirButton.getPreferredSize().width,
                    defaultDirButton.getPreferredSize().height));
            gbc.gridx = 2;
            add(resetDirButton, gbc);
            resetDirButton.addActionListener(this);
    	}

		/**
		 * @return the sharedDirectoryLocation
		 */
		public String getDirectory() {
			if (NONE.equals(directoryLocation)) {
				return null;
			}
			return directoryLocation;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
	        Object source = e.getSource();

	        if (source == defaultDirButton) {
	            // show a folder file chooser, set the current def. location
	            FileChooser chooser = new FileChooser(this);

	            File startDir = new File(System.getProperty("user.home"));
	            if (directoryLocation.length() > 0) {
	                File dir = new File(FileUtility.urlToAbsPath(
	                            directoryLocation));

	                if (dir.exists() && dir.isDirectory()) {
	                    startDir = dir;
	                }
	            }

	            chooser.setCurrentDirectory(startDir.getAbsolutePath());
	            chooser.createAndShowFileDialog(textlabel, FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"),
	                    null, null, true, null, FileChooser.DIRECTORIES_ONLY, null);
	            File selFile = chooser.getSelectedFile();
	            if (selFile != null) {
	                directoryLocation = FileUtility.pathToURLString(
	                        selFile.getAbsolutePath());
	                curDirLabel.setText(directoryLocation);
		            sharedDirectoryChanged(directoryLocation);
	            }
	        } else if (source == resetDirButton) {
	            directoryLocation = NONE;
	            curDirLabel.setText(directoryLocation);
	            sharedDirectoryChanged(null);
	        }
		}
    }
}
