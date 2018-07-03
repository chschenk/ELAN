package mpi.eudico.client.annotator.linkedmedia;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.ClosableDialog;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * A dialog to add, remove or change media files that are linked to  the
 * transcription/document.
 *
 * @author Han Sloetjes
 * @version Aug 2005 Identity removed
 */
public class LinkedFilesDialog extends ClosableDialog implements ActionListener,
    ChangeListener {

	private static final long serialVersionUID = 393737775238780076L;
	// ui stuff
    private JLabel titleLabel;
    private JPanel titlePanel;
    
    private JPanel dialogButtonPanel;
    private JButton cancelButton;
    private JButton applyButton;
    private TranscriptionImpl transcription;

	private JTabbedPane tabPane;
	
    /**
     * Creates a modal new LinkedFilesDialog.
     *
     * @param transcription the transcription
     */
    public LinkedFilesDialog(Transcription transcription) {
        super(ELANCommandFactory.getRootFrame(transcription), true);
        this.transcription = (TranscriptionImpl) transcription;

        initComponents();
        postInit();
    }

    /**
     * Creates a new LinkedFilesDialog
     *
     * @param parent the parent Frame
     */
    public LinkedFilesDialog(Frame parent) {
        super(parent, true);
        initComponents();
        postInit();
    }

    /**
     * This method is called from within the constructor to initialize the
     * dialog.
     */
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

		tabPane = new JTabbedPane();

        titlePanel = new JPanel();
        titleLabel = new JLabel();
        
        dialogButtonPanel = new JPanel();
        applyButton = new JButton();
        cancelButton = new JButton();

        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                @Override
				public void windowClosing(WindowEvent evt) {
                    closeDialog();
                }
            });

        Insets insets = new Insets(2, 6, 2, 6);

        titleLabel.setFont(titleLabel.getFont().deriveFont((float) 16));
        titlePanel.add(titleLabel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = insets;
        getContentPane().add(titlePanel, gridBagConstraints);
        
        tabPane.addTab(ElanLocale.getString("LinkedFilesDialog.Label.LinkedMediaFiles"), 
        	new MediaFilesPanel(transcription, this));
		tabPane.addTab(ElanLocale.getString("LinkedFilesDialog.Label.LinkedSecFiles"), 
			new SecLinkedFilesPanel(transcription, this));
		tabPane.addChangeListener(this);
			
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = insets;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(tabPane, gridBagConstraints);
		
        dialogButtonPanel.setLayout(new GridLayout(1, 2, 6, 0));

        applyButton.addActionListener(this);
        dialogButtonPanel.add(applyButton);

        cancelButton.addActionListener(this);
        dialogButtonPanel.add(cancelButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = insets;
        getContentPane().add(dialogButtonPanel, gridBagConstraints);

        updateLocale();
    }

    /**
     * Applies localized strings to the ui elements.
     */
    private void updateLocale() {
        setTitle(ElanLocale.getString("LinkedFilesDialog.Title"));
        titleLabel.setText(ElanLocale.getString("LinkedFilesDialog.Title"));
        
        applyButton.setText(ElanLocale.getString(
                "LinkedFilesDialog.Button.Apply"));
        cancelButton.setText(ElanLocale.getString(
                "Button.Close"));
    }

    /**
     * Pack, size and set location.
     */
    private void postInit() {
        pack();

        /*
           int w = 550;
           int h = 450;
           setSize((getSize().width < w) ? w : getSize().width,
               (getSize().height < h) ? h : getSize().height);
         */
        setLocationRelativeTo(getParent());
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * Checks whether changes have been made to the set of linked media files
     * and, if any, creates a command that replaces the  media descriptors.
     * The dialog is then closed.
     */
    private void applyChanges() {
		if (tabPane.getSelectedIndex() == 0) {
			((MediaFilesPanel)tabPane.getComponentAt(0)).applyChanges();
		} else {
			((SecLinkedFilesPanel)tabPane.getComponentAt(1)).applyChanges();
		}		
		closeDialog();
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
     * Shows a yes-no option dialog with the specified question.
     *
     * @param question the question
     *
     * @return true if the user's answer is confirmative, false otherwise
     */
    private boolean showOptionDialog(String question) {
        int option = JOptionPane.showOptionDialog(this, question,
                ElanLocale.getString("LinkedFilesDialog.Title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[] {
                    ElanLocale.getString("Button.Yes"),
                    ElanLocale.getString("Button.No")
                }, ElanLocale.getString("Button.Yes"));

        return (option == JOptionPane.YES_OPTION);
    }

    /**
     * The action performed method.
     *
     * @param actionEvent DOCUMENT ME!
     */
    @Override
	public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if (source == cancelButton) {
            closeDialog();
        } else if (source == applyButton) {
            applyChanges();
        }
    }

    /**
     * Test main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new LinkedFilesDialog(new JFrame()).setVisible(true);
    }

	/**
	 * Listens to the selection of a tab in the tabpane.
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		// as long as there are only two tabs...
		if (tabPane.getSelectedIndex() == 1) {
			if (((MediaFilesPanel)tabPane.getComponentAt(0)).hasChanged()) {
				if (showOptionDialog(ElanLocale.getString("LinkedFilesDialog.Question.ApplyChangesMedia"))) {				
					((MediaFilesPanel)tabPane.getComponentAt(0)).applyChanges();
				}			
			}
		} else {
			if (((SecLinkedFilesPanel)tabPane.getComponentAt(1)).hasChanged()) {
				if (showOptionDialog(ElanLocale.getString("LinkedFilesDialog.Question.ApplyChangesNonAV"))) {				
					((SecLinkedFilesPanel)tabPane.getComponentAt(1)).applyChanges();
				}
			}			
		}
	}
	
	/**
	 * By default the dialog comes up with the Media Files Panel.
	 * Call this to show the Secondary Linked Files Panel instead.
	 */
	public void selectSecLinkedFilesPanel() {
		tabPane.setSelectedIndex(1);
	}
	
	public boolean urlAlreadyLinked(String url)
	{
		return ((MediaFilesPanel)tabPane.getComponentAt(0)).urlAlreadyLinked(url) ||
		       ((SecLinkedFilesPanel)tabPane.getComponentAt(1)).urlAlreadyLinked(url);
	}
}
