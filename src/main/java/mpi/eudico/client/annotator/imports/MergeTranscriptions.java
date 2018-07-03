package mpi.eudico.client.annotator.imports;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.FileChooser;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * A dialog that offers the opportunity to merge two existing transcriptions/
 * transcription files (.eaf) to a new, destination transcription file. Can be
 * created from within ELAN or can be run as a separate application.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class MergeTranscriptions extends ClosableDialog implements ActionListener,
    ProgressListener {
    private static boolean exitOnClose = false;
    private TranscriptionImpl transcription;
    private JButton browseSource1;
    private JButton browseSource2;
    private JButton browseDest;
    private JLabel select1Label;
    private JLabel select2Label;
    private JLabel selectDestLabel;
    private JTextField source1Field;
    private JTextField source2Field;
    private JTextField destField;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JPanel transPanel;
    private JPanel progressPanel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JPanel buttonPanel;
    private JButton startButton;
    private JButton closeButton;

    /**
     * Creates a new MergeTranscriptions instance.<br>
     *
     * @param transcription the first source transcription, or null
     */
    public MergeTranscriptions(Transcription transcription) {
        //super(ELANCommandFactory.getRootFrame(transcription), true);
        super(new JFrame(), true);
        this.transcription = (TranscriptionImpl) transcription;
        initComponents();
        postInit();
    }

    /**
     * Initializes UI elements.
     */
    protected void initComponents() {
        GridBagConstraints gridBagConstraints;
        titlePanel = new JPanel();
        titleLabel = new JLabel();
        transPanel = new JPanel();
        browseSource1 = new JButton();
        browseSource2 = new JButton();
        browseDest = new JButton();
        source1Field = new JTextField();
        source2Field = new JTextField();
        destField = new JTextField();
        select1Label = new JLabel();
        select2Label = new JLabel();
        selectDestLabel = new JLabel();
        buttonPanel = new JPanel();
        startButton = new JButton();
        closeButton = new JButton();
        progressPanel = new JPanel();
        progressLabel = new JLabel();
        progressBar = new JProgressBar();

        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        Insets insets = new Insets(2, 6, 2, 6);

        titlePanel.setLayout(new BorderLayout(0, 4));
        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titleLabelPanel = new JPanel();
        titleLabelPanel.add(titleLabel);
        titlePanel.add(titleLabelPanel, BorderLayout.NORTH);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);

        transPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        transPanel.add(select1Label, gridBagConstraints);

        if (transcription != null) {
            source1Field.setEnabled(false);
            source1Field.setText(transcription.getFullPath());
        }

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        transPanel.add(source1Field, gridBagConstraints);

        if (transcription != null) {
            browseSource1.setEnabled(false);
        } else {
            browseSource1.addActionListener(this);
        }

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        transPanel.add(browseSource1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        transPanel.add(select2Label, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        transPanel.add(source2Field, gridBagConstraints);

        browseSource2.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        transPanel.add(browseSource2, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        transPanel.add(selectDestLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        transPanel.add(destField, gridBagConstraints);

        browseDest.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        transPanel.add(browseDest, gridBagConstraints);

        JPanel filler = new JPanel();
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weighty = 1.0;
        transPanel.add(filler, gridBagConstraints);

        progressPanel.setLayout(new GridBagLayout());
        progressPanel.setPreferredSize(new Dimension(50, 80));
        progressLabel.setFont(Constants.deriveSmallFont(progressLabel.getFont()));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        progressPanel.add(progressLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        progressPanel.add(progressBar, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = insets;
        gridBagConstraints.weightx = 1.0;
        transPanel.add(progressPanel, gridBagConstraints);
        progressPanel.setVisible(false);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        getContentPane().add(transPanel, gridBagConstraints);

        buttonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        startButton.addActionListener(this);
        buttonPanel.add(startButton);

        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(buttonPanel, gridBagConstraints);

        updateLocale();
    }

    /**
     * Enable/disable ui elements.
     *
     * @param enable true to enable all buttons etc., false to disable
     */
    private void activateUI(boolean enable) {
        startButton.setEnabled(enable);
        closeButton.setEnabled(enable);

        if (transcription == null) {
            browseSource1.setEnabled(enable);
            source1Field.setEnabled(enable);
        }

        browseSource2.setEnabled(enable);
        browseDest.setEnabled(enable);
        source2Field.setEnabled(enable);
        destField.setEnabled(enable);
    }

    /**
     * Applies localized strings to the ui elements.
     */
    protected void updateLocale() {
        setTitle(ElanLocale.getString("MergeTranscriptionDialog.Title"));
        transPanel.setBorder(new TitledBorder(ElanLocale.getString(
                    "MergeTranscriptionDialog.Title")));
        titleLabel.setText(ElanLocale.getString(
                "MergeTranscriptionDialog.Title"));
        select1Label.setText(ElanLocale.getString(
                "MergeTranscriptionDialog.Label.Source1"));
        select2Label.setText(ElanLocale.getString(
                "MergeTranscriptionDialog.Label.Source2"));
        selectDestLabel.setText(ElanLocale.getString(
                "MergeTranscriptionDialog.Label.Destination"));
        browseSource1.setText(ElanLocale.getString("Button.Browse"));
        browseSource2.setText(ElanLocale.getString("Button.Browse"));
        browseDest.setText(ElanLocale.getString("Button.Browse"));
        startButton.setText(ElanLocale.getString(
                "MergeTranscriptionDialog.Button.Merge"));
        closeButton.setText(ElanLocale.getString("Button.Close"));
    }

    /**
     * Pack, size and set location.
     */
    protected void postInit() {
        pack();

        int w = 550;
        int h = 400;
        setSize((getSize().width < w) ? w : getSize().width,
            (getSize().height < h) ? h : getSize().height);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    /**
     * Closes the dialog
     *
     * @param evt the window closing event
     */
    protected void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();

        if (exitOnClose) {
            System.exit(0);
        }
    }

    /**
     * Start the merging process. First some checks are performed on the
     * contents of the fields for the source and destination files, next a
     * TranscriptionMerger object is  created, then we register as a
     * progresslistener so that a progressbar can visualize  the progress of
     * the merging process, and finally the merging process is started.
     */
    private void startMerge() {
        if (transcription == null) {
            // check first source
            if (!checkValidEAFFile(source1Field.getText())) {
                showWarningDialog(ElanLocale.getString(
                        "MergeTranscriptionDialog.Warning.Source1"));

                return;
            }
        }

        if (!checkValidEAFFile(source2Field.getText())) {
            showWarningDialog(ElanLocale.getString(
                    "MergeTranscriptionDialog.Warning.Source2"));

            return;
        }

        String dest = destField.getText();

        if ((dest == null) || (dest.length() < 4)) {
            showWarningDialog(ElanLocale.getString(
                    "MergeTranscriptionDialog.Warning.NoDestination"));

            return;
        }

        if (checkValidEAFFile(destField.getText())) {
            int answer = JOptionPane.showConfirmDialog(null,
                    ElanLocale.getString(
                        "MergeTranscriptionDialog.Warning.DestinationExists"),
                    ElanLocale.getString("Message.Warning"),
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.NO_OPTION) {
                return;
            }
        }

        // start the merging process				
        TranscriptionMerger merger = null;

        try {
            if (transcription != null) {
                merger = new TranscriptionMerger(transcription,
                        source2Field.getText(), destField.getText());
            } else {
                merger = new TranscriptionMerger(source1Field.getText(),
                        source2Field.getText(), destField.getText());
            }
        } catch (IOException ioe) {
            showWarningDialog(ioe.getMessage());

            return;
        } catch (Exception e) {
            showWarningDialog(e.getMessage());

            return;
        }

        if (merger != null) {
            activateUI(false);
            progressPanel.setVisible(true);
            merger.addProgressListener(this);
            merger.startMerge();
        }
    }

    /**
     * Let the user browse to the first source eaf.
     */
    private void browseFirstSource() {
        String name = promptForFileName("MergeFirstEafDir");

        if (name != null) {
            source1Field.setText(name);

            if (progressPanel.isVisible()) {
                progressLabel.setText("");
                progressBar.setValue(0);
            }
        }
    }

    /**
     * Let the user browse to the second source eaf.
     */
    private void browseSecondSource() {
        String name = promptForFileName("MergeSecondEafDir");

        if (name != null) {
            source2Field.setText(name);

            if (progressPanel.isVisible()) {
                progressLabel.setText("");
                progressBar.setValue(0);
            }
        }
    }

    /**
     * Let the user browse to and/or typ the destination eaf.
     */
    private void browseDestination() {
        String name = getSaveFileName("MergeDestEafDir");

        if (name != null) {
            destField.setText(name);

            if (progressPanel.isVisible()) {
                progressLabel.setText("");
                progressBar.setValue(0);
            }
        }
    }

    /**
     * Prompts the user for a file name and location.
     *
     * @return a file path
     */
    private String promptForFileName(String prefLoc) {
        FileChooser chooser = new FileChooser(null);
        chooser.createAndShowFileDialog(ElanLocale.getString("MergeTranscriptionDialog.SelectEAF"), FileChooser.OPEN_DIALOG, ElanLocale.getString("Button.Select"), 
        		null, FileExtension.EAF_EXT, false, prefLoc, FileChooser.FILES_ONLY, null);


        File eafFile = chooser.getSelectedFile();
        if (eafFile != null) {
             return eafFile.getAbsolutePath();
        }
        return null;        
    }
    
    /**
     * Prompts the user to provide a filepath for a new .eaf file.
     *
     * @return a String representation of a file
     */
    private String getSaveFileName(String locPrefString) {
        FileChooser chooser = new FileChooser(this);
        chooser.createAndShowFileDialog(ElanLocale.getString("MergeTranscriptionDialog.SelectEAF"), FileChooser.SAVE_DIALOG, 
        		 FileExtension.EAF_EXT,  locPrefString);

        File eafFile = chooser.getSelectedFile();
        
        if (eafFile != null) {
              return eafFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Checks whether the file exists and has the extension eaf.
     *
     * @param path the file path
     *
     * @return true if the file exists and is an eaf file, false otherwise
     */
    private boolean checkValidEAFFile(String path) {
        if ((path == null) || (path.length() == 0)) {
            return false;
        }

        if (new File(path).exists()) {
            String lowerPathName = path.toLowerCase();

            String[] exts = FileExtension.EAF_EXT;
            boolean validExt = false;

            for (int i = 0; i < exts.length; i++) {
                if (lowerPathName.endsWith("." + exts[i])) {
                    validExt = true;

                    break;
                }
            }

            if (validExt) {
                return true;
            }
        }

        return false;
    }

    /**
     * The action performed event handling.
     *
     * @param ae the action event
     */
    @Override
	public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == startButton) {
            startMerge();
        } else if (source == closeButton) {
            closeDialog(null);
        } else if (source == browseSource1) {
            browseFirstSource();
        } else if (source == browseSource2) {
            browseSecondSource();
        } else if (source == browseDest) {
            browseDestination();
        }
    }

    /**
     * Creates the dialog as a standalone application.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        exitOnClose = true;
        new MergeTranscriptions(null).setVisible(true);
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Receives progress update notifications.
     *
     * @param source the source of the notification
     * @param percent the progress value. [0 - 100]
     * @param message a description of the progress
     */
    @Override
	public void progressUpdated(Object source, int percent, String message) {
        if (progressPanel.isVisible()) {
            progressLabel.setText(message);

            if (percent < 0) {
                percent = 0;
            } else if (percent > 100) {
                percent = 100;
            }

            progressBar.setValue(percent);
        }

        if (percent >= 100) {
            activateUI(true);
        }
    }

    /**
     * Notification that the process has been completed.
     *
     * @param source the source of the notification
     * @param message a description of the progress
     */
    @Override
	public void progressCompleted(Object source, String message) {
        if (progressPanel.isVisible()) {
            progressLabel.setText(message);
            progressBar.setValue(100);
        }

        activateUI(true);
    }

    /**
     * Notification that the process has been interrupted.
     *
     * @param source the source of the notification
     * @param message a description of the progress
     */
    @Override
	public void progressInterrupted(Object source, String message) {
        if (progressPanel.isVisible()) {
            progressLabel.setText(message);
        }

        activateUI(true);
    }
}
