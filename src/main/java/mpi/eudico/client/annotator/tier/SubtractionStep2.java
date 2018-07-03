package mpi.eudico.client.annotator.tier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * Panel for step 2: Subtraction Computation Panel for specifying criteria
 *
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
public class SubtractionStep2 extends StepPane{
	
	private JRadioButton exclusiveOrRB, subtractionRB;	
	private JComboBox tierNameCB;
	private JDialog helpDialog;
	
	/**
	 * Constructor
	 * 
	 * @param mp, multiStepPane
	 */
	public SubtractionStep2(MultiStepPane mp){
		super(mp);
		initComponents();
	}
	
	/**
	 * Initializes the ui components
	 */
	@Override
	protected void initComponents(){			
		//create label
		JLabel createAnnotationLabel = new JLabel(ElanLocale.getString("SubtractAnnotationDialog.Label.CreateAnnotation"));
		JLabel subtractfromTierLabel = new JLabel(ElanLocale.getString("SubtractAnnotationDialog.Label.SubtractFromTier"));
		
		exclusiveOrRB = new JRadioButton(ElanLocale.getString("SubtractAnnotationDialog.Radio.ExclusiveOr"), true);
		subtractionRB = new JRadioButton(ElanLocale.getString("SubtractAnnotationDialog.Radio.Subtraction"));
		
		final JButton helpButton = new JButton(new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Help24.gif")));
		helpButton.setPreferredSize(new Dimension(24,24));
		helpButton.setToolTipText(ElanLocale.getString("SubtractAnnotationDialog.Button.Help.ToolTip"));
		
		//create elements for constraint panel
		tierNameCB = new JComboBox();
		tierNameCB.setEnabled(false);
		
		ButtonGroup criteriaButtonGroup = new ButtonGroup();
		criteriaButtonGroup.add(exclusiveOrRB);
		criteriaButtonGroup.add(subtractionRB);
		
		ActionListener radioButtonListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() instanceof JRadioButton){
					tierNameCB.setEnabled(subtractionRB.isSelected());
				} else if(e.getSource() == helpButton){
					showHelpDialog();
				}
			}
		};
		exclusiveOrRB.addActionListener(radioButtonListener);
		subtractionRB.addActionListener(radioButtonListener);
		helpButton.addActionListener(radioButtonListener);
		
		//create panel
		JPanel criteriaPanel = new JPanel(new GridBagLayout());
		criteriaPanel.setBorder(new TitledBorder(ElanLocale.getString("OverlapsDialog.Panel.Title.Criteria")));
		
		Insets globalInset = new Insets(5, 10, 5, 10);
		Insets singleTabInset = new Insets(5, 30, 5, 10);
		Insets doubleTabInset = new Insets(5, 50, 5, 10);
		
		//add radio buttons to criteria panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
//		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = globalInset;
		criteriaPanel.add(createAnnotationLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = singleTabInset;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		criteriaPanel.add(helpButton, gbc);
		
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = singleTabInset;
		criteriaPanel.add(exclusiveOrRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;		
		criteriaPanel.add(subtractionRB, gbc);
		
		JPanel controlPanel = new JPanel(new GridLayout(1, 3, 6, 6));	           
        controlPanel.add(subtractfromTierLabel);
        controlPanel.add(tierNameCB);
         
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = doubleTabInset;
		gbc.gridwidth = 1;		
		criteriaPanel.add(controlPanel, gbc);	
		
		//add components to panel
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = globalInset;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(criteriaPanel, gbc);
	}
	
	@Override
	public String getStepTitle(){
		return ElanLocale.getString("SubtractAnnotationDialog.Title.Step2Title");
	}	
	
	@Override
	public void enterStepForward(){
		//get selected tiers
		List<String> tierList = (List<String>) multiPane.getStepProperty("SelectedTiers");		
		tierNameCB.removeAllItems();
		
		//add tier names that are selected in tier table in step 1
		for( int i=0; i<tierList.size(); i++ ) {
			tierNameCB.addItem( tierList.get(i) );
		}	
		
		updateButtonStates();				
	}	
	
	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	/**
     * Check and store properties, if all conditions are met.
     *
     * @see mpi.eudico.client.annotator.gui.multistep.Step#leaveStepForward()
     */
    @Override
	public boolean leaveStepForward() {    	
    	//parent tier	
    	String refTierName = null;
    	if( subtractionRB.isSelected() ){
    		refTierName = (String) tierNameCB.getSelectedItem();    		
    	}				
    	multiPane.putStepProperty("ReferenceTierName", refTierName);
    
        updateButtonStates();
        
   		return true;       
    }

	public void updateButtonStates(){
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}
	
	private void showHelpDialog(){
		if(helpDialog == null){		
			helpDialog = new JDialog(multiPane.getDialog(), ElanLocale.getString("SubtractAnnotationDialog.Dialog.Help.Title") , false);
						
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			
			JScrollPane scrollPane = new JScrollPane(panel);
			helpDialog.add(scrollPane);
						
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(5, 10, 5, 10);
			panel.add(new JLabel (ElanLocale.getString("SubtractAnnotationDialog.Dialog.Help.Label1")), gbc);
			
			gbc.gridy = 2;			
			panel.add(new JLabel (ElanLocale.getString("SubtractAnnotationDialog.Dialog.Help.Label2")), gbc);
				
			gbc.gridy = 1;			
			gbc.insets = new Insets(5, 30, 5, 10);
			panel.add(new JLabel(new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Subtraction.png"))), gbc);
			
			gbc.gridy = 3;		
			panel.add(new JLabel(new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Exclusive-or.png"))), gbc);	
			
			gbc.gridy = 4;
			gbc.weighty = 1.0;
	        gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			panel.add(new JPanel(), gbc);			
			
		}
		
		if(helpDialog.isVisible()){
			return;
		}
		
		helpDialog.pack();		
		helpDialog.setVisible(true);
	}
}
