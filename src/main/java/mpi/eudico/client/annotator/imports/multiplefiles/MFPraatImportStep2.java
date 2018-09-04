package mpi.eudico.client.annotator.imports.multiplefiles;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

@SuppressWarnings("serial")
public class MFPraatImportStep2 extends AbstractMFImportStep2 implements ChangeListener {
	
	private JCheckBox pointTiersCB;
	private JSpinner durationSp;
	private JLabel durationLabel;
	private JCheckBox skipEmptyCB;
	private final Integer DEF_DUR = Integer.valueOf(40);
	
	public MFPraatImportStep2(MultiStepPane multiPane) {
		super(multiPane);
		
		initComponents();
	}
	 
	/**
	 * Initializes ui components.
	 */
	@Override
	protected void initComponents() {
	     setLayout(new GridBagLayout());
	     setBorder(new EmptyBorder(12, 12, 12, 12));	    
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
	     gbc.insets = insets;	    
	     gbc.anchor = GridBagConstraints.WEST;
	     gbc.fill = GridBagConstraints.HORIZONTAL;
	     gbc.weightx = 1.0;
	     add(pointTiersCB, gbc);
	
	     gbc.gridx = 0;
	     gbc.gridy = 1;
	     gbc.gridwidth = 1;
	     gbc.insets = insets;
	     gbc.fill = GridBagConstraints.NONE;
	     gbc.weightx = 0.0;
	     add(durationSp, gbc);
	     
	     gbc.gridx = 1;
	     add(durationLabel, gbc);
	     
	     gbc.gridx = 0;
	     gbc.gridy = 2;
	     gbc.gridwidth = 2;
	     gbc.fill = GridBagConstraints.HORIZONTAL;
	     gbc.weightx = 1.0;
	     add(skipEmptyCB, gbc);	     
	     
	     gbc.gridy = 3;
	     gbc.fill = GridBagConstraints.BOTH;
	     gbc.weighty =1.0;
	     add(new JPanel(), gbc);	  
	    
	     pointTiersCB.addChangeListener(this);
	 }

    /**
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {             
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
	 
