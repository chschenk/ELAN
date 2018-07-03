package mpi.eudico.client.annotator.tier;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;

/**
 * Panel for step 2: Overlaps Computation Panel for specifying criteria
 *
 * @author Jeffrey Lemein
 * @author aarsom
 * @version November, 2011
 */
@SuppressWarnings("serial")
public class OverlapsStep2 extends StepPane{
	protected JLabel createAnnotationLabel;
	protected JButton valuesMatchConstraintsBtn;
	protected JRadioButton overlapRB, valuesTheSameRB, valuesDifferentRB, valuesMatchConstraintsRB;	
	protected JPanel criteriaPanel;
	protected Insets globalInset, singleTabInset;	
	private Frame parentFrame;
	protected List<String[]> constraintValues;
	private ButtonGroup criteriaButtonGroup;
	
	/**
	 * Constructor
	 * 
	 * @param mp, the multiStepPane
	 * @param owner
	 */
	public OverlapsStep2(MultiStepPane mp, Frame owner){
		super(mp);
		parentFrame = owner;
		globalInset = new Insets(5, 10, 5, 10);
		singleTabInset = new Insets(0, 30, 0, 10);
		initComponents();
	}
	
	/**
	 * Initialize the ui components
	 */
	@Override
	protected void initComponents(){			
		//create label
		createAnnotationLabel = new JLabel(ElanLocale.getString("OverlapsDialog.Label.CreateAnnotation"));
		
		//create radio buttons
		RadioButtonListener radioButtonListener = new RadioButtonListener();
		overlapRB = new JRadioButton(ElanLocale.getString("OverlapsDialog.Radio.Overlap"), true);
		overlapRB.addActionListener(radioButtonListener);
		valuesTheSameRB = new JRadioButton(ElanLocale.getString("OverlapsDialog.Radio.ValuesTheSame"));
		valuesTheSameRB.addActionListener(radioButtonListener);
		valuesDifferentRB = new JRadioButton(ElanLocale.getString("OverlapsDialog.Radio.ValuesDifferent"));
		valuesDifferentRB.addActionListener(radioButtonListener);
		valuesMatchConstraintsRB = new JRadioButton(ElanLocale.getString("OverlapsDialog.Radio.ValuesMatchConstraints"));
		valuesMatchConstraintsRB.addActionListener(radioButtonListener);
		
		//create button for annotations based on different values for different tiers
		valuesMatchConstraintsBtn = new JButton(ElanLocale.getString("OverlapsDialog.Button.Constraints"));
		valuesMatchConstraintsBtn.addActionListener(new ButtonListener());
		valuesMatchConstraintsBtn.setEnabled(false);
		
		//add buttons to button group
		criteriaButtonGroup = new ButtonGroup();
		criteriaButtonGroup.add(overlapRB);
		criteriaButtonGroup.add(valuesTheSameRB);
		criteriaButtonGroup.add(valuesDifferentRB);
		criteriaButtonGroup.add(valuesMatchConstraintsRB);
		
		//create panel
		criteriaPanel = new JPanel(new GridBagLayout());
		criteriaPanel.setBorder(new TitledBorder(ElanLocale.getString("OverlapsDialog.Panel.Title.Criteria")));
		
		//add radio buttons to criteria panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = globalInset;
		criteriaPanel.add(createAnnotationLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = singleTabInset;
		criteriaPanel.add(overlapRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = singleTabInset;
		criteriaPanel.add(valuesTheSameRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		criteriaPanel.add(valuesDifferentRB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		criteriaPanel.add(valuesMatchConstraintsRB, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		criteriaPanel.add(valuesMatchConstraintsBtn, gbc);
		
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
		return ElanLocale.getString("OverlapsDialog.Title.Step2Title");
	}	
	
	@Override
	public void enterStepForward(){		
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
    	 //retrieve overlaps criteria
        int overlapsCriteria = 0;
        if( overlapRB.isSelected() ) {
			overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_OVERLAP;
		} else if( valuesTheSameRB.isSelected() ) {
			overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_VALUES_EQUAL;
		} else if( valuesDifferentRB.isSelected() ) {
			overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_VALUES_NOT_EQUAL;
		} else if( valuesMatchConstraintsRB.isSelected() ) {
			overlapsCriteria = AnnotationFromOverlaps.OVERLAP_CRITERIA_USE_SPECIFIED_CONSTRAINTS;
		}
        
        if(constraintValues == null){
        	constraintValues = new ArrayList<String[]>();
        }
        
        multiPane.putStepProperty("overlapsCriteria", overlapsCriteria);
        multiPane.putStepProperty("tierValueConstraints", constraintValues);
       
        updateButtonStates();        
   		return true;    
    }
	
	public void updateButtonStates(){
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}

	private class RadioButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			boolean b = valuesMatchConstraintsRB.isSelected();			
			valuesMatchConstraintsBtn.setEnabled(b);
			updateButtonStates();
		}
	}
	
	private class ButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			JButton btn = (JButton)e.getSource();
			
			if( btn == valuesMatchConstraintsBtn ){
				ConstraintDialog dialog = new ConstraintDialog(constraintValues);
				// TODO restore size + location
				
				dialog.setSize(400, 500);
				dialog.setLocationRelativeTo(OverlapsStep2.this);
				dialog.setVisible(true);
				constraintValues = dialog.getConstraintValues();
				
				// TODO store size + location
			}
		}
	}
	
	private class ConstraintDialog extends ClosableDialog implements ActionListener, ItemListener{
		private JPanel constraintPanel, buttonPanel;
		private JButton removeButton, removeAllButton, addButton, okButton;
		private JLabel containsLabel;
		private JPanel specifyConstraintPanel, constraintOverviewPanel;
		
		private JComboBox tierNameCB;
		private JTable tierValueTable;	
		private JTextField annotationValueTF;
		private JScrollPane scrollPane;
		private DisplayableContentTableModel tableModel;
		
		public ConstraintDialog(List<String[]> constraintValues){
			super(parentFrame, true);
			
			setTitle(ElanLocale.getString("OverlapsDialog.Dialog.ConstraintDialog.Title"));
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			
			//initialize constraint panel
			initConstraintPanel(constraintValues);
			
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(4, 5, 4, 4);
			
			add(constraintPanel, gbc);
		}
		
		private void initConstraintPanel(List<String[]> initConstraintValues){
			
			//create elements for constraint panel
			tierNameCB = new JComboBox();
			tableModel = new DisplayableContentTableModel(4);
			tierValueTable = new JTable(tableModel);
			tierValueTable.getColumnModel().getColumn(0).setMaxWidth(35);
			tierValueTable.getColumnModel().getColumn(2).setMaxWidth(60);
			
			//add tier names that are selected in tier table in step 1
			List<String> tierList = (List<String>) multiPane.getStepProperty("SelectedTiers");	
			for( int i=0; i<tierList.size(); i++ ) {
				tierNameCB.addItem( tierList.get(i).toString() );
			}		
			
			//remove all rows in tableModel that contain elements referring to nonselected tiers
			for( int r=tableModel.getRowCount()-1; r>=0; r-- ) {
				if( !tierList.contains(tableModel.getValueAt(r, 1)) ) {
					tableModel.removeRow(r);
				}
			}
			
			tierNameCB.addItemListener(this);
			
			//create panel
			constraintPanel = new JPanel(new GridBagLayout());
			
			specifyConstraintPanel = new JPanel(new GridBagLayout());
			specifyConstraintPanel.setBorder(new TitledBorder(ElanLocale.getString("OverlapsDialog.Panel.Title.SpecifyConstraint")));
			
			constraintOverviewPanel = new JPanel(new GridBagLayout());
			constraintOverviewPanel.setBorder(new TitledBorder(ElanLocale.getString("OverlapsDialog.Panel.Title.ConstraintsOverview")));
			
			//create components
			annotationValueTF = new JTextField();
			
			containsLabel = new JLabel(ElanLocale.getString("OverlapsDialog.Combo.Contains"));
			
			addButton = new JButton(ElanLocale.getString("Button.Add"));
			addButton.addActionListener(this);
			
			tierValueTable.getColumnModel().getColumn(0).setHeaderValue("");
			tierValueTable.getColumnModel().getColumn(0).setWidth(10);
			tierValueTable.getColumnModel().getColumn(1).setHeaderValue(ElanLocale.getString("OverlapsDialog.Column.Header.Tier"));
			tierValueTable.getColumnModel().getColumn(2).setHeaderValue("");
			tierValueTable.getColumnModel().getColumn(2).setWidth(30);
			tierValueTable.getColumnModel().getColumn(3).setHeaderValue(ElanLocale.getString("OverlapsDialog.Column.Header.Value"));
			tableModel.connectTable(tierValueTable);
			scrollPane = new JScrollPane(tierValueTable);
			
			removeButton = new JButton(ElanLocale.getString("OverlapsDialog.Button.Remove"));
			removeButton.addActionListener(this);
			removeAllButton = new JButton(ElanLocale.getString("OverlapsDialog.Button.RemoveAll"));
			removeAllButton.addActionListener(this);
			okButton = new JButton(ElanLocale.getString("Button.OK"));
			okButton.addActionListener(this);
			
			//layout components
			GridBagConstraints gbc = new GridBagConstraints();
			
			//== specify constraint panel ====
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.weightx = 0.5;
			gbc.insets = new Insets(5, 10, 5, 0);
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			specifyConstraintPanel.add(tierNameCB, gbc);
			
			gbc.gridy = 0;
			gbc.gridx = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 0.0;
			specifyConstraintPanel.add(containsLabel, gbc);
			
			gbc.gridx = 2;
			gbc.weightx = 0.5;
			gbc.fill = GridBagConstraints.BOTH;
			specifyConstraintPanel.add(annotationValueTF, gbc);
			
			gbc.gridx = 3;
			gbc.weightx = 0.0;
			gbc.insets = globalInset;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;
			specifyConstraintPanel.add(addButton, gbc);				
			
			//== Constraint overview panel ========
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.insets = globalInset;
			constraintOverviewPanel.add(scrollPane, gbc);
			
			buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(removeButton);
			buttonPanel.add(removeAllButton);
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.gridwidth = 1;
			Insets insets = new Insets(0, globalInset.left, globalInset.bottom, globalInset.right);
			gbc.insets = insets;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;
			constraintOverviewPanel.add(buttonPanel, gbc);
			
			//== Add panels to view ====
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.insets = new Insets(0, 0, 0, 0);
			constraintPanel.add(specifyConstraintPanel, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1.0;
			constraintPanel.add(constraintOverviewPanel, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = globalInset;
			constraintPanel.add(okButton, gbc);
			
			if (initConstraintValues != null) {
				String[] row;
				String[] oldRow;
				for (int r = 0; r < initConstraintValues.size(); r++) {
					oldRow = initConstraintValues.get(r);
					row = new String[4];
					if (r > 0) {
						row[0] = ElanLocale.getString("OverlapsDialog.Message.And"); 
					} else {
						row[0] = "";
					}
					row[1] = oldRow[0];
					row[2] = ElanLocale.getString("OverlapsDialog.Combo.Contains");
					row[3] = oldRow[1];
					tableModel.addRow(row);
				}
				tableModel.updateTables();
			}
		}
		
		public List<String[]> getConstraintValues(){
			List<String[]> tierValuePairs = new ArrayList<String[]>();
			if(tableModel.getRowCount() > 0){	
				for( int i=0; i< tableModel.getRowCount(); i++ ){
	             	tierValuePairs.add(new String[]{(String)tableModel.getValueAt(i, 1), (String)tableModel.getValueAt(i, 3)});
	        	}
			}			
			return tierValuePairs;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton) e.getSource();
			
			if( btn == addButton ){
				addButton.setEnabled(false);
				int selectedIndex = tierNameCB.getSelectedIndex();
				
				//add tier in constraint panel
				String[] value = new String[]{"", tierNameCB.getItemAt(selectedIndex).toString(), ElanLocale.getString("OverlapsDialog.Combo.Contains"), annotationValueTF.getText()};
				int rowIndex = findIndexToAdd(value[1], value[3]);
				
				//don't add duplicate tier constraints
				if( tableModel.contains(value[1], 1)) {
					return;
				}
				
				//check if value in front of the to be added value corresponds to the same tier
				if( rowIndex > 0 ){
					value[0] = ElanLocale.getString("OverlapsDialog.Message.And");
					tableModel.addRowAt(value, rowIndex);
				}else{
					tableModel.addRowAt(value, rowIndex);
					if( tableModel.getRowCount() >= 2 ) {
						tableModel.setValueAt(ElanLocale.getString("OverlapsDialog.Message.And"), 1, 0);
					}
				}
				
				tableModel.updateTables();
			}else if( btn == removeButton ){
				int[] selectedRows = tierValueTable.getSelectedRows();
				for( int i=selectedRows.length-1; i>=0; i-- ) {
					tableModel.removeRow(selectedRows[i]);
				}
				
				//update first column values
				for( int i=0; i<tableModel.getRowCount(); i++ ){
					if( i == 0 ) {
						tableModel.setValueAt("", 0, 0);
					} else {
						tableModel.setValueAt(ElanLocale.getString("OverlapsDialog.Message.And"), i, 0);
					}
				}
				tierValueTable.getSelectionModel().clearSelection();
				
				String tierName = (String) tierNameCB.getSelectedItem();
				addButton.setEnabled(!tableModel.contains(tierName, 1));
				updateAddButtonState();
			}else if(btn == removeAllButton ){
				for( int i=tableModel.getRowCount()-1; i>=0; i-- ) {
					tableModel.removeRow(i);
				}
				
				tierValueTable.getSelectionModel().clearSelection();
				updateAddButtonState();
			}else if( btn == okButton ){
				setVisible(false);
				dispose();
			}
		}
		
		private int findIndexToAdd(String tiername, String value){
			Collator collator = Collator.getInstance();
			
			for( int r=0; r<tableModel.getRowCount(); r++ ){
				int res = collator.compare(tiername, tableModel.getValueAt(r,1));
				if( res == -1 ){
					return r;
				}else if( res == 0 ){
					if( collator.compare(value, tableModel.getValueAt(r,3)) < 0 ) {
						return r;
					}
				}					
			}
			
			return tableModel.getRowCount();
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			updateAddButtonState();			
		}
		
		private void updateAddButtonState(){
			String tierName = (String) tierNameCB.getSelectedItem();
			addButton.setEnabled(!tableModel.contains(tierName, 1));	
		}
	}
}
