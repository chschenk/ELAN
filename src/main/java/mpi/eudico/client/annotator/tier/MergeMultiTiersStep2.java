package mpi.eudico.client.annotator.tier;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Panel for step 2: Merging Computation Panel for specifying criteria
 *
 * @author aarsom
 * @version Feb, 2014
 */
public class MergeMultiTiersStep2 extends OverlapsStep2 implements KeyListener{
	private JCheckBox onlyOverlapsCB;
	private JCheckBox specificValueCB;
	private JTextField specificValueTF;
	
	private boolean validateCriteria = false;
	private boolean validCriteria= false;	
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param owner
	 */
	public MergeMultiTiersStep2(MultiStepPane mp, Frame owner){
		super(mp, owner);
		makeLayout();
	}
	
	/**
	 * Initialize the ui components
	 */
	private void makeLayout(){		
		ActionListener actionListener = new ActionListener(){			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateButtonStates();				
				specificValueTF.setEnabled(specificValueCB.isSelected());
				valuesTheSameRB.setEnabled(onlyOverlapsCB.isSelected());	
				valuesDifferentRB.setEnabled(onlyOverlapsCB.isSelected());
				specificValueCB.setEnabled(valuesTheSameRB.isEnabled() && valuesTheSameRB.isSelected());
			}
		};
		
		onlyOverlapsCB = new JCheckBox(ElanLocale.getString("MergeTiers.CheckBox.Overlap"));
		onlyOverlapsCB.addActionListener(actionListener);	
		
		specificValueCB = new JCheckBox(ElanLocale.getString("MergeTiers.Radio.SpecificValue"));
		specificValueCB.addActionListener(actionListener);		
		
		specificValueTF = new JTextField();
		specificValueTF.setEnabled(false);	
		specificValueTF.addKeyListener(this);
		
		valuesTheSameRB.addActionListener(actionListener);
		
		valuesTheSameRB.setEnabled(onlyOverlapsCB.isSelected());	
		valuesDifferentRB.setEnabled(onlyOverlapsCB.isSelected());	
		specificValueCB.setEnabled(valuesTheSameRB.isEnabled() && valuesTheSameRB.isSelected());
		
		createAnnotationLabel.setText(ElanLocale.getString("MergeTiers.Label.MergeAnn"));		
		overlapRB.setText(ElanLocale.getString("OverlapsDialog.Radio.Overlap"));
		valuesTheSameRB.setText(ElanLocale.getString("MergeTiers.Radio.ValuesTheSame"));
		valuesDifferentRB.setText(ElanLocale.getString("MergeTiers.Radio.ValuesDifferent"));
		valuesMatchConstraintsRB.setText(ElanLocale.getString("OverlapsDialog.Radio.ValuesMatchConstraints"));
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;		
		panel.add(specificValueCB, gbc);
		
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		panel.add(specificValueTF, gbc);
		
		criteriaPanel .remove(valuesTheSameRB);
		
		//add radio buttons to criteria panel
		gbc = new GridBagConstraints();	
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = singleTabInset;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		criteriaPanel.add(valuesTheSameRB, gbc);
		
		gbc.insets = new Insets(0, 60, 0, 10);
		gbc.gridy = 6;
		criteriaPanel.add(panel, gbc);
		
		gbc.gridy = 7;		
		gbc.weightx = 1.0;
		gbc.insets = globalInset;
		criteriaPanel.add(onlyOverlapsCB, gbc);
		
		//revalidate();
	}
	
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("MergeTiers.Title.Step2");
	}	
	
	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {       	
    	validateCriteria = true;
    	
    	updateButtonStates();  
    	
    	if(!validCriteria){
    		return false;
    	}
    	
    	 //retrieve overlaps criteria
        int overlapsCriteria = 0;
        if( overlapRB.isSelected() )
        	overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_OVERLAP;
        else if( valuesTheSameRB.isSelected() )
        	overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_VALUES_EQUAL;
   		else if( valuesDifferentRB.isSelected() )
   			overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_VALUES_NOT_EQUAL;
   		else if( valuesMatchConstraintsRB.isSelected() )
   			overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS;
        
        // add specific value
        if(constraintValues == null){
        	constraintValues = new ArrayList<String[]>();
        }
        
        if(specificValueCB.isSelected()){
        	constraintValues.clear();
        	constraintValues.add(new String[]{specificValueTF.getText().trim()});
        }
        
        // check inputs
        multiPane.putStepProperty("OnlyProcessOverlapingAnnotations", onlyOverlapsCB.isSelected());
        multiPane.putStepProperty("overlapsCriteria", overlapsCriteria);
        multiPane.putStepProperty("tierValueConstraints", constraintValues);
              
   		return true;    
    }    
	
    @Override
	public void updateButtonStates(){
    	if(validateCriteria){
    		createAnnotationLabel.setForeground(Color.BLACK);
    		createAnnotationLabel.setText(ElanLocale.getString("MergeTiers.Label.MergeAnn"));
    		specificValueCB.setForeground(Color.BLACK);
    		
    		if(valuesTheSameRB.isEnabled() && valuesTheSameRB.isSelected() && specificValueCB.isSelected()){
    			if(specificValueTF.getText().trim().length() <= 0){
    				specificValueCB.setForeground(Color.RED);
    				validCriteria = false;
    			} else {
    				validCriteria = true;
    			}
    			
    		} else if(!onlyOverlapsCB.isSelected() && (valuesTheSameRB.isSelected() || valuesDifferentRB.isSelected())){
    			createAnnotationLabel.setForeground(Color.RED);
    			createAnnotationLabel.setText(createAnnotationLabel.getText() + " - " + ElanLocale.getString("MergeTiers.Criteria.Select"));
    			validCriteria = false;
    		} else{
    			validCriteria = true;
    			validateCriteria = false;
    		}
    	} else {    		
    		validCriteria = true;
    	}
    	
    	multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, validCriteria);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);

	}

    /**
	 * Key pressed handler that updates the button states
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		updateButtonStates();
	}

	/**
	 * Key release handler that updates the button states
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		updateButtonStates();
	}

	/**
	 * Key typed handler that updates the button states
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		updateButtonStates();
	}
}
