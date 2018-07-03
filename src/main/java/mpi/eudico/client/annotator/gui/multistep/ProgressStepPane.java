package mpi.eudico.client.annotator.gui.multistep;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.util.ProgressListener;

/**
 * A base class for a step pane showing the progress of an operation.
 * 
 * @author Han Sloetjes
 */
public class ProgressStepPane extends StepPane implements ProgressListener {
    protected JProgressBar progressBar;
    protected JTextArea progressLabel;
    protected boolean completed = false;
    
	public ProgressStepPane(MultiStepPane multiPane) {
		super(multiPane);
		// classes that extend this pane should call initComponents
//		initComponents();
	}

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	protected void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();

        progressLabel = new JTextArea("...");// setFont?
        progressLabel.setEditable(false);
        progressLabel.setBackground(this.getBackground());
        JLabel lab = new JLabel();
        progressLabel.setFont(lab.getFont());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(progressLabel, gbc);

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        progressBar.setValue(0);

        gbc.gridy = 1;
        add(progressBar, gbc);
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

        if (percent >= 100 && !completed) {
        	
            showMessageDialog(ElanLocale.getString("MultiStep.Progress.Complete"));
            completed = true;
            
            endOfProcess();
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

        progressBar.setValue(100);
        if (!completed) {
	        showMessageDialog(ElanLocale.getString("MultiStep.Progress.Complete"));
	        completed = true;
	        
	        endOfProcess();
        }
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
        showWarningDialog(ElanLocale.getString("MultiStep.Progress.Interrupted") + " " + message);

        endOfProcess();
    }
    
    /**
     * Classes extending this pane can implement appropriate actions here. 
     */
    protected void endOfProcess() {
    	multiPane.close();
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
     * Shows a message dialog with the specified message string.
     *
     * @param message the message to display
     */
    protected void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message, null,
            JOptionPane.INFORMATION_MESSAGE);
    }

}
