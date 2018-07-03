package mpi.eudico.client.annotator.imports.praat;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ImportPraatGridCommand;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.clom.Transcription;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;


/**
 * Final step of the import Praat TextGrid process. Parsing of the file and
 * creation of new tiers and annotations.
 */
public class ImportPraatTGStep3 extends StepPane implements ProgressListener {
    private Transcription curTranscription;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private ImportPraatGridCommand com;
    private boolean inNewTranscription = false;

    /**
     * Creates a new instance of the final step of the wizard.
     *
     * @param multiPane
     * @param curTranscriptionthe transcription
     */
    public ImportPraatTGStep3(MultiStepPane multiPane,
        Transcription curTranscription) {
        super(multiPane);
        this.curTranscription = curTranscription;

        initComponents();
    }
    
    /**
     * Creates a new instance of the final step of the wizard.
     *
     * @param multiPane
     * @param curTranscriptionthe transcription
     */
    public ImportPraatTGStep3(MultiStepPane multiPane,
        Transcription curTranscription, boolean inNewTranscription) {
        super(multiPane);
        this.curTranscription = curTranscription;
        this.inNewTranscription = inNewTranscription;
        
        initComponents();
    }

    /**
     * Shows a progress bar.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	protected void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        progressLabel = new JLabel("...");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(progressLabel, gbc);

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        progressBar.setIndeterminate(true);

        gbc.gridy = 1;
        add(progressBar, gbc);
    }

    /**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("ImportDialog.Praat.Title3");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        multiPane.setButtonEnabled(MultiStepPane.ALL_BUTTONS, false);

        String sourcePath = (String) multiPane.getStepProperty("Source");
        final String typeName = (String) multiPane.getStepProperty("Type");
        final Boolean includePT = (Boolean) multiPane.getStepProperty("PointTier");
        final Integer duration = (Integer) multiPane.getStepProperty("PointDuration");
        String tempEncoding = (String) multiPane.getStepProperty("Encoding");
        final Boolean skipEmpty = (Boolean) multiPane.getStepProperty("SkipEmpty");

        if (tempEncoding != null && tempEncoding.equals(ElanLocale.getString("Button.Default"))) {
        	// use the system default, set encoding to null
        	tempEncoding = null;
        }
        final String encoding = tempEncoding;
        
        if (sourcePath != null) {
            final File impFile = new File(sourcePath);

            if ((impFile == null) || !impFile.exists()) {
                progressInterrupted(null, "The TextGrid file does not exist");
            }

            try {
                new Thread(new Runnable() {
                        @Override
						public void run() {
                            try {
                                boolean ipt = false;
                                int dur = 40;
                                
                                if (includePT != null) {
                                    ipt = includePT.booleanValue();
                                    
                                    if (duration != null) {
                                        dur = duration.intValue();
                                    }
                                }
                                
                                PraatTextGrid ptg = new PraatTextGrid(impFile, ipt, dur, encoding);
                                if (inNewTranscription) {
                                	com = new ImportPraatGridCommand(ElanLocale.getString(ELANCommandFactory.IMPORT_PRAAT_GRID));
                                } else {
                                	com = (ImportPraatGridCommand) ELANCommandFactory.createCommand(curTranscription,
                                            ELANCommandFactory.IMPORT_PRAAT_GRID);
                                }
                                com.addProgressListener(ImportPraatTGStep3.this);
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(0);
                                com.execute(curTranscription,
                                    new Object[] { ptg, typeName, skipEmpty });
                            } catch (IOException ioe) {
                                progressInterrupted(null, ioe.getMessage());
                            }
                        }
                    }).start();
            } catch (Exception e) {
                progressInterrupted(null, e.getMessage());
            }
        } else {
            progressInterrupted(null, "No TextGrid file selected");
        }

        return false;
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        doFinish();
    }

    /**
     * @see mpi.eudico.client.annotator.util.ProgressListener#progressUpdated(java.lang.Object,
     *      int, java.lang.String)
     */
    @Override
	public void progressUpdated(Object source, int percent, String message) {
        if ((progressLabel != null) && (message != null)) {
            progressLabel.setText(message);
        }

        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }

        progressBar.setValue(percent);

        if (percent >= 100) {
            if (com != null) {
                com.removeProgressListener(this);
            }

            showMessageDialog("Operation completed");

            multiPane.close();
        }
    }

    /**
     * @see mpi.eudico.client.annotator.util.ProgressListener#progressCompleted(java.lang.Object,
     *      java.lang.String)
     */
    @Override
	public void progressCompleted(Object source, String message) {
        if (progressLabel != null) {
            progressLabel.setText(message);
        }

        if (com != null) {
            com.removeProgressListener(this);
        }

        showMessageDialog("Operation completed");

        multiPane.close();
    }

    /**
     * @see mpi.eudico.client.annotator.util.ProgressListener#progressInterrupted(java.lang.Object,
     *      java.lang.String)
     */
    @Override
	public void progressInterrupted(Object source, String message) {
        if (progressLabel != null) {
            progressLabel.setText(message);
        }

        // message dialog
        showWarningDialog("Operation interrupted: " + message);

        if (com != null) {
            com.removeProgressListener(this);
        }

        multiPane.close();
    }

    /**
     * Shows a warning/error dialog with the specified message string.
     *
     * @param message the message to display
     */
    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message,
            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows a message dialog with the specified message string.
     *
     * @param message the message to display
     */
    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message, null,
            JOptionPane.INFORMATION_MESSAGE);
    }
}
