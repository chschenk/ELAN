package mpi.eudico.client.annotator.tier;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;


/**
 * Second step of the merge tier group wizard: entering of a  name for the new tier.
 *
 * @author Han Sloetjes
 * @version 1.0 Nov 2009
 */
@SuppressWarnings("serial")
public class MergeTierGroupStep2 extends StepPane implements 
    ActionListener, CaretListener {
	private TranscriptionImpl transcription;
	private JTextField suffixField;
	private JLabel nameLabel;
	private JLabel sufLabel;
	private String orgName;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public MergeTierGroupStep2(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;
        initComponents();
    }

    /**
     * Adds a textfield for a new tiername
     *
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	protected void initComponents() {
    	//super.initComponents();
        suffixField = new JTextField();
        suffixField.addActionListener(this);
        suffixField.addCaretListener(this);

        sufLabel = new JLabel(ElanLocale.getString(
                    "OverlapsDialog.Label.DestNameSuffix"));
        nameLabel = new JLabel();// shows the new name
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.ITALIC));
        nameLabel.setForeground(nameLabel.getForeground().brighter());
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        add(sufLabel, gbc);
        gbc.gridy = 1;
        add(suffixField, gbc);
        gbc.gridy = 2;
        add(nameLabel, gbc);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.DefineDest");
    }


    /**
	 * @see mpi.eudico.client.annotator.gui.multistep.StepPane#enterStepBackward()
	 */
	@Override
	public void enterStepBackward() {
		enterStepForward();
	}

	/**
	 * @see mpi.eudico.client.annotator.gui.multistep.StepPane#enterStepForward()
	 */
	@Override
	public void enterStepForward() {
		orgName = (String) multiPane.getStepProperty("Source-1");
		if (orgName == null) {
			orgName = "-";
		}
		updateNameLabel();
	}

	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        if (!validTierName()) {
            return false;
        }
        
        multiPane.putStepProperty("Suffix", suffixField.getText());
        
        return true;
    }

    /**
     * Calls leave forward. 
	 * @see mpi.eudico.client.annotator.gui.multistep.StepPane#doFinish()
	 */
	@Override
	public boolean doFinish() {
        multiPane.nextStep();

        return false;
	}

	/**
     * Checks whether the tier name field contains a valid tiername.
     *
     * @return true if a valid tier name has been entered,false otherwise
     */
    private boolean validTierName() {
        String name = suffixField.getText();

        if ((orgName == null) || (name == null) || (name.length() == 0)) {
            return false;
        }

        // this loose check is obsolete, other elements should be taken into account
        if (transcription.getTierWithId(orgName + name) == null) {
            return true;
        }

        return false;
    }
    
    /**
     * Receives events from the textfield.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	caretUpdate(null);
    }

    /**
     * Checks whether the entered tier name is unique and activates the Finish button
     * if so.
     */
	@Override
	public void caretUpdate(CaretEvent e) {
        if (validTierName()) {
            //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
        } else {
            //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
        updateNameLabel();
	}
	
	private void updateNameLabel() {
		if (orgName != null) {
			String suf  = suffixField.getText();
			if (suf != null) {
				nameLabel.setText(orgName + suf);
			} else {
				nameLabel.setText(orgName);
			}
		}
	}

}
