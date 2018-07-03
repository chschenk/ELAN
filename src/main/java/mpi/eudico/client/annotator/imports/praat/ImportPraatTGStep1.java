package mpi.eudico.client.annotator.imports.praat;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.FileExtension;
import mpi.eudico.client.annotator.gui.FileChooser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * First step of an import Praat TextGrid process.
 */
@SuppressWarnings("serial")
public class ImportPraatTGStep1 extends StepPane implements ActionListener,
    ChangeListener {
    private JTextField sourceField;
    private JButton browseButton;
    private JCheckBox pointTiersCB;
    private JSpinner durationSp;
    private JLabel durationLabel;
    private JCheckBox skipEmptyCB;
    private final Integer DEF_DUR = Integer.valueOf(40);
    private String encoding;

    /**
     * Creates a new instance of the first step of the wizard.
     *
     * @param multiPane the parent pane
     */
    public ImportPraatTGStep1(MultiStepPane multiPane) {
        super(multiPane);

        initComponents();
    }

    /**
     * Initializes ui components.
     */
    @Override
	public void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        sourceField = new JTextField();
        sourceField.setEditable(false);
        browseButton = new JButton(ElanLocale.getString("Button.Browse"));
        pointTiersCB = new JCheckBox(ElanLocale.getString("ImportDialog.Praat.Label.PointTiers"));
        durationSp = new JSpinner(new SpinnerNumberModel(DEF_DUR.intValue(), 1, 10000, 10));
        durationSp.setEnabled(false);
        durationLabel = new JLabel(ElanLocale.getString("ImportDialog.Praat.Label.PointDuration"));
        durationLabel.setEnabled(false);
        skipEmptyCB = new JCheckBox(ElanLocale.getString("ImportDialog.Praat.Label.SkipEmpty"));

        Insets insets = new Insets(4, 6, 4, 6);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(sourceField, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(browseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(pointTiersCB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(durationSp, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(durationLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        //gbc.insets = new Insets(20, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(skipEmptyCB, gbc);
        
        browseButton.addActionListener(this);
        pointTiersCB.addChangeListener(this);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#getStepTitle()
     */
    @Override
	public String getStepTitle() {
        return ElanLocale.getString("ImportDialog.Praat.Title1");
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepBackward()
     */
    @Override
	public void enterStepBackward() {
        multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        multiPane.setButtonEnabled(MultiStepPane.CANCEL_BUTTON, true);
    }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {
        multiPane.putStepProperty("Source", sourceField.getText());
        multiPane.putStepProperty("Encoding", encoding);
        if (pointTiersCB.isSelected()) {
            multiPane.putStepProperty("PointTier", Boolean.TRUE);
            System.out.println(durationSp.getValue().getClass().getName());
            multiPane.putStepProperty("PointDuration", 
                    (durationSp.getValue() != null ? durationSp.getValue() : DEF_DUR));
        }
        if (skipEmptyCB.isSelected()) {
        	multiPane.putStepProperty("SkipEmpty", Boolean.TRUE);
        } else {
        	multiPane.putStepProperty("SkipEmpty", Boolean.FALSE);
        }

        return true;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        File file = getFileName();

        if (file != null) {
            sourceField.setText(file.getAbsolutePath());
            multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
        }
    }

    private File getFileName() {
    	FileChooser chooser = new FileChooser(null);
        
        String[] encodings = new String[]{ElanLocale.getString("Button.Default"), 
        		FileChooser.UTF_8, FileChooser.UTF_16};
        
        chooser.createAndShowFileAndEncodingDialog(ElanLocale.getString("ImportDialog.Title.Select"), FileChooser.OPEN_DIALOG, 
        		null, FileExtension.PRAAT_TEXTGRID_EXT, "LastUsedPraatDir", encodings, FileChooser.UTF_8, null);
        encoding = chooser.getSelectedEncoding();

        return chooser.getSelectedFile();
    }

    /**
     * Enables or disables the duration spinner of the PointTier checkbox is checked or 
     * unchecked.
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
	public void stateChanged(ChangeEvent e) {
        durationSp.setEnabled(pointTiersCB.isSelected());
        durationLabel.setEnabled(pointTiersCB.isSelected());
    }
}
