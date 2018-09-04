package mpi.eudico.client.annotator.webserviceclient.weblicht;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * A step in the interaction with WebLicht that allows typing or pasting text.
 *
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class WebLichtStep2 extends StepPane implements DocumentListener {
	private JTextArea textArea;
	private JLabel textLabel;

	public WebLichtStep2(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}

    /**
     * Initialize the panel.
     * 
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	public void initComponents() {
    	setLayout(new GridBagLayout());
    	setBorder(new EmptyBorder(5, 10, 5, 10));
    	
    	textLabel = new JLabel(ElanLocale.getString("WebServicesDialog.WebLicht.TypeText"));
    	textArea = new JTextArea();
    	textArea.setWrapStyleWord(true);
    	textArea.setLineWrap(true);
    	
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = new Insets(6, 0, 6, 0);
    	gbc.anchor = GridBagConstraints.NORTHWEST;
    	add(textLabel, gbc);
    	
    	gbc.gridy = 1;
    	gbc.insets = new Insets(2, 0, 2, 0);
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1.0;
    	gbc.weighty = 1.0;
    	add(new JScrollPane(textArea), gbc);
    	
    	textArea.getDocument().addDocumentListener(this);
    }

	@Override
	public void enterStepForward() {
		textArea.requestFocus();
		checkText();
	}

	/**
	 * Stores the text in the pane's properties.
	 */
	@Override
	public boolean leaveStepForward() {
		String text = textArea.getText(); 
		if (text != null && text.length() > 0) {
			multiPane.putStepProperty("InputText", text);
			return true;
		} else {
			// popup a info message
			return false;
		}
	}

	
	@Override
	public void enterStepBackward() {
		checkText();
		textArea.requestFocus();
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("WebServicesDialog.WebLicht.StepTitle2a");
	}

	/**
	 * Document listener
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		checkText();
		
	}

	/**
	 * Document listener
	 */
	@Override
	public void insertUpdate(DocumentEvent arg0) {
		checkText();
		
	}

	/**
	 * Document listener
	 */
	@Override
	public void removeUpdate(DocumentEvent arg0) {
		checkText();
		
	}
	
	/**
	 * Checks whether there is any text at all and updates the next button status. 
	 */
	private void checkText() {
		String t = textArea.getText();
		if (t != null && t.length() > 0) {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		} else {
			multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		}
	}
}
