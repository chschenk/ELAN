package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.type.LinguisticTypeTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Second step of this wizard: entering of a  name for the new tier, selection
 * of it's linguistic type and optional selection of the format of the
 * duration  as annotation values.
 *
 * @author Han Sloetjes
 * @version 1.0 Jan 2007
 */
public class CalcOverlapsStep2 extends StepPane implements ListSelectionListener,
    ActionListener, CaretListener {
    private TranscriptionImpl transcription;
    private LinguisticTypeTableModel model;
    private JTable table;
    private JTextField nameField;
    JCheckBox contentCB;
	JRadioButton concatValuesRB;
	JRadioButton durationRB;
    JRadioButton msRB;
    JRadioButton secRB;
    JRadioButton hourRB;
    JLabel nameLabel;
    JLabel typeLabel;
    JCheckBox matchedValuesCB;
    JCheckBox specificValueCB;
    JTextField matchedValueTF;

    /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param transcription the transcription
     */
    public CalcOverlapsStep2(MultiStepPane multiPane,
        TranscriptionImpl transcription) {
        super(multiPane);
        this.transcription = transcription;

        initComponents();
    }

    /**
     * Adds a textfield for a new tiername, a list of valid linguistic types
     * and a checkbox  denoting whether or not th overlap duration should be
     * the annotation value.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.StepPane#initComponents()
     */
    @Override
	protected void initComponents() {
        // fill linguistic type table
        model = new LinguisticTypeTableModel(null,
                new String[] {
                    LinguisticTypeTableModel.NAME,
                    LinguisticTypeTableModel.STEREOTYPE
                });

        LinguisticType lt;

        for (int i = 0; i < transcription.getLinguisticTypes().size(); i++) {
            lt = (LinguisticType) transcription.getLinguisticTypes().get(i);

            if (lt.getConstraints() == null) {
                model.addRow(lt);
            }
        }

        table = new JTable(model);
        table.getSelectionModel()
             .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        table.getSelectionModel().addListSelectionListener(this);

        Dimension prdim = new Dimension(120, 80);
        JScrollPane p1 = new JScrollPane(table);
        p1.setPreferredSize(prdim);

        nameField = new JTextField();
        nameField.addActionListener(this);
        nameField.addCaretListener(this);

        nameLabel = new JLabel(ElanLocale.getString(
                    "OverlapsDialog.Label.DestName"));
        typeLabel = new JLabel(ElanLocale.getString(
                    "OverlapsDialog.Label.Type"));
        //contentCB = new JCheckBox(ElanLocale.getString(
        //            "OverlapsDialog.Label.Content"));
        
    	concatValuesRB = new JRadioButton(ElanLocale.getString("OverlapsDialog.Label.Content2"));
    	concatValuesRB.setSelected(true);
    	concatValuesRB.addActionListener(this);
    	
    	durationRB = new JRadioButton(ElanLocale.getString("OverlapsDialog.Label.Content"));
    	durationRB.addActionListener(this);
    	
    	
    	ButtonGroup contentBG = new ButtonGroup();
    	contentBG.add(durationRB);
    	contentBG.add(concatValuesRB);
    	
//        contentCB.setSelected(true);
//        contentCB.addActionListener(this);
    	
        msRB = new JRadioButton(ElanLocale.getString("TimeCodeFormat.MilliSec"));
        msRB.setSelected(true);
        secRB = new JRadioButton(ElanLocale.getString("TimeCodeFormat.Seconds"));
        hourRB = new JRadioButton(ElanLocale.getString(
                    "TimeCodeFormat.TimeCode"));
        msRB.setEnabled(false);
        secRB.setEnabled(false);
        hourRB.setEnabled(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(msRB);
        bg.add(secRB);
        bg.add(hourRB);
        
        matchedValuesCB = new JCheckBox(ElanLocale.getString("OverlapsDialog.Label.MatchingValues"));
        matchedValuesCB.addActionListener(this);
        specificValueCB = new JCheckBox(ElanLocale.getString("OverlapsDialog.Label.SpecificValue"));
        specificValueCB.setEnabled(false);
        specificValueCB.addActionListener(this);
        matchedValueTF = new JTextField();
        matchedValueTF.setEnabled(false);

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        add(nameLabel, gbc);
        gbc.gridy = 1;
        add(nameField, gbc);
        gbc.gridy = 2;
        add(typeLabel, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        add(p1, gbc);
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        add(durationRB, gbc);

        JPanel filler = new JPanel();
        filler.setPreferredSize(new Dimension(24, 16));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        add(filler, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridheight = 1;
        add(msRB, gbc);
        gbc.gridy = 6;
        add(secRB, gbc);
        gbc.gridy = 7;
        add(hourRB, gbc);
        
        gbc.insets = new Insets(10, 6, 4, 6);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(concatValuesRB, gbc);
        
        gbc.insets = new Insets(10, 6, 4, 6);;
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(matchedValuesCB, gbc);
        
        filler = new JPanel();
        filler.setPreferredSize(new Dimension(24, 16));

        JPanel valPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        valPanel.add(filler, gbc);
        
        gbc.gridx = 1;
        valPanel.add(specificValueCB, gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        valPanel.add(matchedValueTF, gbc);
        
        gbc = new GridBagConstraints();
        gbc.insets = insets;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(valPanel, gbc);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("OverlapsDialog.DefineDest");
    }

    /**
     * Tier name and linguistgic type have been set before.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
        multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
    }

    /**
     * Checks whether conditions are met to enable the Finish button.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
    @Override
	public void enterStepForward() {
        // check, just in case we have been here before
        valueChanged(null);
    }

    /**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        if (table.getSelectedRowCount() > -1) {
            if (!validTierName()) {
                return false;
            }

            multiPane.putStepProperty("DestTier", nameField.getText());

            int row = table.getSelectedRow();
            Object sel = table.getValueAt(row, 0);
            multiPane.putStepProperty("Type", (String) sel);
            multiPane.putStepProperty("Content",
            	Boolean.valueOf(durationRB.isSelected()));

            if (durationRB.isSelected()) {
                multiPane.putStepProperty("ContentType", "Duration");
                if (msRB.isSelected()) {
                    multiPane.putStepProperty("Format",
                    	Integer.valueOf(Constants.MS));
                } else if (secRB.isSelected()) {
                    multiPane.putStepProperty("Format",
                    	Integer.valueOf(Constants.SSMS));
                } else if (hourRB.isSelected()) {
                    multiPane.putStepProperty("Format",
                    	Integer.valueOf(Constants.HHMMSSMS));
                }
        	} 
            
            multiPane.putStepProperty("MatchingValuesOnly",
            		Boolean.valueOf(matchedValuesCB.isSelected()));
            multiPane.putStepProperty("SpecificValueOnly", 
            		Boolean.valueOf(specificValueCB.isSelected()));
            if (specificValueCB.isSelected()) {
            	multiPane.putStepProperty("SpecificValue", matchedValueTF.getText());
            }

            return true;
        }

        return false;
    }

    /**
     * Delegates the actual operation to the next step, so finish is equivalent
     * to next.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
     */
    @Override
	public boolean doFinish() {
        multiPane.nextStep();

        return false;
    }

    /**
     * Table row selection event handling.
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
	public void valueChanged(ListSelectionEvent e) {
        checkConditions();
    }

    /**
     * Receives events from the textfield and the content checkbox.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == durationRB || e.getSource() == concatValuesRB) {
            msRB.setEnabled(durationRB.isSelected());
            secRB.setEnabled(durationRB.isSelected());
            hourRB.setEnabled(durationRB.isSelected());
    	}  else if (e.getSource() == matchedValuesCB) {
        	specificValueCB.setEnabled(matchedValuesCB.isSelected());
        	if (!matchedValuesCB.isSelected()) {
        		matchedValueTF.setEnabled(false);
        	}
        } else if (e.getSource() == specificValueCB) {
        	if (specificValueCB.isSelected()) {
        		matchedValueTF.setEnabled(true);
        		//matchedValueTF.setText("");
        		matchedValueTF.grabFocus();
        	} else {
        		matchedValueTF.setEnabled(false);       		
        	}
        } else {
            checkConditions();
        }
    }

    /**
     * Receives events from the textfield.
     *
     * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
     */
    @Override
	public void caretUpdate(CaretEvent e) {
        checkConditions();
    }

    /**
     * Checks whether the tier name field contains a valid tiername.
     *
     * @return true if a valid tier name has been entered,false otherwise
     */
    private boolean validTierName() {
        String name = nameField.getText();

        if ((name == null) || (name.length() == 0)) {
            return false;
        }

        // this loose check is obsolete, other elements should be taken into account
        if (transcription.getTierWithId(name) == null) {
            return true;
        }

        return false;
    }

    /**
     * Checks the tiername field and the type table and enablees or disables
     * the Next button.
     */
    void checkConditions() {
        if (table.getSelectedRow() > -1) {
            if (validTierName()) {
                //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
            } else {
                //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
                multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
            }
        } else {
            //multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
            multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, false);
        }
    }
}
