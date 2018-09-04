package mpi.eudico.client.annotator.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;


/**
 * A ProgressMonitor that displays an indeterminate JProgressBar. It can
 * display a message above the progressbar and a cancel button below the
 * progressbar (both optional).  The monitor can be modal or non-modal, and
 * cancellable or non-cancellable. It is the
 * caller's responsibility to make sure the monitor is closed at the end of
 * the process or when an exception occurs during the process. Cancelling only sets
 * the boolean <code>cancelled</code> to true. It
 * does not stop the process it is monitoring; the caller should use
 * <code>isCancelled</code> to check the state of the monitor. <br>
 * Note: this class uses Java 1.4 features.
 *
 * @author HS
 * @version 1.0 May, 2004
 */
public class IndeterminateProgressMonitor {
    private String message;
    private boolean cancellable;
    private boolean cancelled;
    private boolean modal;

    /** estimated minimal width for the dialog */
    private final int MIN_WIDTH = 240;
    private JDialog dialog;
    private JLabel messageLabel;
    private JButton cancelButton;
    private String cancelText;
    private Component parent;
    private JProgressBar progressBar;

    /**
     * Creates a modal or non-modal progres monitor positioned relative to  the
     * frame containing <code>parent</code> or parent itself when it is a frame.<br>
     * The constructor does not make the monitor visible. <code>show</code>
     * should  be called to make it visible.
     *
     * @param parent the parent Component
     * @param modal when true the monitor blocks user interaction
     * @param message a message <code>String</code> that will be displayed
     * @param cancellable when true a cancel button is added to the monitor
     * @param cancelText the text for the cancel button
     */
    public IndeterminateProgressMonitor(Component parent, boolean modal,
        String message, boolean cancellable, String cancelText) {
        this.parent = parent;
        this.modal = modal;
        this.message = message;
        this.cancellable = cancellable;
        this.cancelText = cancelText;
        cancelled = false;
    }

    /**
     * Closes the monitor. The dialog is made invisible and disposed.
     */
    public void close() {
    	// sometimes the monitor is being closed before it has the change to
    	// show itself, so set cancelled to true
    	cancelled = true;
		if (dialog != null) {
			dialog.setVisible(false);
			dialog.dispose();
		}        
    }

    /**
     * Creates the dialog, adds the proper components and makes the dialog
     * visible. It may be necessary to use a separate thread for the process
     * and/or the monitor to make it visible.
     */
    public void show() {
    	if (cancelled) {
    		return;
    	}
    	
        Frame frame = null;

        if (parent instanceof Frame) {
            frame = (Frame) parent;
        } else if (parent != null) {
            Window owner = SwingUtilities.windowForComponent(parent);

            if (owner instanceof Frame) {
                frame = (Frame) owner;
            }
        }

        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBorder(new EmptyBorder(6, 6, 6, 6));
        messageLabel = new JLabel();

        if (message != null) {
            messageLabel.setText(message);
        }

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        GridBagConstraints gbc = new GridBagConstraints();
        Insets inset = new Insets(4, 2, 4, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = inset;
        contentPane.add(messageLabel, gbc);
        gbc.gridy = 1;
        contentPane.add(progressBar, gbc);

        if (cancellable) {
            cancelButton = new JButton();
            cancelButton.setAction(new AbstractAction() {
                    @Override
					public void actionPerformed(ActionEvent ae) {
                        cancelled = true;
                        cancelButton.setEnabled(false);
                        //close();
                    }
                });

            if (cancelText != null) {
                cancelButton.setText(cancelText);
            } else {
                //default text
                cancelButton.setText("Cancel");
            }

            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.0;
            contentPane.add(cancelButton, gbc);
        }

		dialog = new JDialog(frame, modal);
	    dialog.setContentPane(contentPane);
	    dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    dialog.setUndecorated(true);
	    dialog.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
	    dialog.pack();
	
	    if (dialog.getWidth() < MIN_WIDTH) {
	    	dialog.setSize(MIN_WIDTH, dialog.getHeight());
	    }
	
	    dialog.setLocationRelativeTo(frame);
	    if (cancelled) {
	    	dialog.dispose();
	    } else {
			dialog.setVisible(true);
	    }
    }

    /**
     * Sets the message that is displayed along the progress bar.
     *
     * @param message the message to display
     */
    public void setMessage(String message) {
        this.message = message;

        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    /**
     * Returns the message String.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns whether or not the monitor has been cancelled
     *
     * @return true if the monitor has been cancelled, false otherwise
     */
    public synchronized boolean isCancelled() {
        return cancelled;
    }
}
