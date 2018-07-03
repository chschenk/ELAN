package mpi.eudico.client.annotator.tier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;

/**
 * Panel for Step 4: Specify the value of the 
 * resulting annotations 
 * 
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
public class OverlapsOrSubtractionStep4 extends AbstractDestTierAnnValueSpecStepPane{	
	boolean subtractionDialog;
	
	 /**
     * Constructor
     *
     * @param multiPane the container pane    
     */
	public OverlapsOrSubtractionStep4(MultiStepPane mp){
		this(mp, false);
	}
	
	 /**
     * Constructor
     *
     * @param multiPane the container pane
     * @param subtractionDialog if true, refers to the subtraction process
     */
	public OverlapsOrSubtractionStep4(MultiStepPane mp, boolean subtractionDialog){
		super(mp);
		this.subtractionDialog = subtractionDialog;
		makeLayout();
	}
	
	/**
     * @see mpi.eudico.client.tool.viewer.enhanced.multistep.Step#getStepTitle()
     */
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("OverlapsDialog.Title.Step4Title");
	}
	
	/**
	 * Updates the current layout
	 */
	private void makeLayout(){
		if(subtractionDialog){
			remove(tierValuePanel);	
			tierValueRadioButton.setText(ElanLocale.getString("SubtractAnnotationDialog.Radio.AnnotationValue"));	
			tierValuePanel.remove(tierSelectBox);
			if (concatValuesRB.isSelected()) {
				tierValueRadioButton.setSelected(true);	
			}
			tierValuePanel.remove(concatValuesRB);
			tierValuePanel.remove(sortByTimeRB);
			tierValuePanel.remove(sortBySelectionRB);
			tierValuePanel.remove(tierTableScrollPane);
			tierValuePanel.remove(buttonPanel);			

			//add panels to screen
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = new Insets(5, 10, 5, 10);			
			add(tierValuePanel, gbc);
			
			revalidate();
		}
	}
	
	/**     
     * @see mpi.eudico.client.annotator.gui.multistep.Step#enterStepForward()
     */
	@Override
	public void enterStepForward(){		
		if(!subtractionDialog){
			super.enterStepForward();
		}else{		
			updateButtonStates();
		}
	}
	
	/**
	 * Set the button states appropriately, according to constraints
	 */
	@Override
	public void updateButtonStates(){
		if(!subtractionDialog){
			super.updateButtonStates();
			return;
		}
		boolean b = durationRadioButton.isSelected();
		msecRB.setEnabled(b);
		secRB.setEnabled(b);
		hrRB.setEnabled(b);
		smpteRB.setEnabled(b);
		
		palRB.setEnabled(b && smpteRB.isSelected());
		pal50RB.setEnabled(b && smpteRB.isSelected());
		ntscRB.setEnabled(b && smpteRB.isSelected());	
		
		specificValueTF.setEnabled(specificValueRB.isSelected());
		
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, false);
		
		if( specificValueRB.isSelected() )
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, specificValueTF.getText().trim().length() > 0);
		else
			multiPane.setButtonEnabled(MultiStepPane.FINISH_BUTTON, true);
		
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}

//	/**
//	 * Calls the next step
//	 *
//	 * @see mpi.eudico.client.annotator.gui.multistep.Step#doFinish()
//     */
//	public boolean doFinish() {
//		multiPane.nextStep();		
//		return false;
//	}
}
	
	