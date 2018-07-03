package mpi.eudico.client.annotator.tier;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.gui.EditTypeDialog2;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

/**
 * Abstract Step pane to specify the attributes for the new tier.
 * Allows to specify a tierName, linguistic type and the parent tier
 * for the new tier
 * 
 * @author Jeffrey Lemein
 * @updatedBy aarsom
 * @version November, 2011
 */
@SuppressWarnings("serial")
public abstract class AbstractDestTierAndTypeSpecStepPane extends StepPane implements ListSelectionListener, ActionListener, KeyListener, ClientLogger{
	
	
	private JLabel tierNameDesLabel, decideRootOrChildLabel, lingTypeLabel, destTypeLabel, typeDesLabel;
	private JScrollPane lingTypeTableScrollPane;
	private JPanel destinationTierConfigurationPanel;
	private ButtonGroup rootChildButtonGroup;
	private JTextField newTierNameField;	
	private JTable linguisticTypeTable;
	private Insets globalInset, singleTabInset;
	private TreeSet<String> tierSet;
	
	protected JRadioButton rootTierRB, childTierRB;
	protected JComboBox parentTierCB;
	protected JCheckBox hideTypesWithCVCB;
	
	protected JButton createTypesButton;
	protected CreateTypeDialog createTypeDialog;
	
	
	private TranscriptionImpl transcription;
	protected List<String> rootTierTypeNames;	
	protected List<String> childTierTypeNames;	
	
	protected List<String> newRootTypeList;	
	protected List<String> newChildypeList;	
	protected List<String> completeTypeNamesList;
	
	private boolean validName = true;
	private boolean validType = true;
	private boolean multiFileMode = false;
	
	/**
	 * Constructor
	 */
	public AbstractDestTierAndTypeSpecStepPane(MultiStepPane mp, TranscriptionImpl trans){
		super(mp);
		globalInset = new Insets(5, 10, 5, 10);
		singleTabInset = new Insets(0, 30, 0, 10);
		transcription = trans;
		
		multiFileMode = transcription == null;
		initComponents();
	}	

	@Override
	protected void initComponents(){			
		newRootTypeList = new ArrayList<String>();
		newChildypeList = new ArrayList<String>();
		
		JLabel newTierNameLabel = new JLabel( ElanLocale.getString("DestTierAndType.Label.newTierNameLabel") );
		
		destTypeLabel  = new JLabel( ElanLocale.getString("DestTierAndType.Label.lingTypeLabel.IncludedIn"));
		destTypeLabel.setFont(new Font(destTypeLabel.getFont().getFontName(), Font.PLAIN, 10));		
		
		lingTypeLabel = new JLabel( ElanLocale.getString("DestTierAndType.Label.lingTypeLabel") );
		
		tierNameDesLabel = new JLabel();
		tierNameDesLabel.setForeground(Color.RED);	
		//tierNameDesLabel.setFont(new Font(destTypeLabel.getFont().getFontName(), Font.BOLD, 10));	
		
		typeDesLabel = new JLabel();
		typeDesLabel.setForeground(Color.RED);	
		//typeDesLabel.setFont(new Font(destTypeLabel.getFont().getFontName(), Font.BOLD, 10));		
		
		//create text field
		newTierNameField = new JTextField();
			
		//create parent tier combobox + checkbox
		decideRootOrChildLabel = new JLabel(ElanLocale.getString("DestTierAndType.Label.RootChildLabel"));
		rootTierRB = new JRadioButton(ElanLocale.getString("DestTierAndType.Radio.RootTier"), true);
		rootTierRB.addActionListener(this);
		childTierRB = new JRadioButton(ElanLocale.getString("DestTierAndType.Radio.ChildTier"));
		childTierRB.addActionListener(this);
		
		rootChildButtonGroup = new ButtonGroup();
		rootChildButtonGroup.add(rootTierRB);
		rootChildButtonGroup.add(childTierRB);
		hideTypesWithCVCB = new JCheckBox(ElanLocale.getString("DestTierAndType.Label.HideTypesWithCV"), false);
		
		createTypesButton = new JButton(ElanLocale.getString("DestTierAndType.Button.AddType"));
				
		parentTierCB = new JComboBox();	
		
		//create table
		linguisticTypeTable = new JTable();
		linguisticTypeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		linguisticTypeTable.setModel(new LinguisticTypeTableModel());
		linguisticTypeTable.getColumnModel().getColumn(0).setHeaderValue( ElanLocale.getString("DestTierAndType.Column.LinguisticType") );
		linguisticTypeTable.getColumnModel().getColumn(1).setHeaderValue( ElanLocale.getString("DestTierAndType.Column.Stereotype") );		
		lingTypeTableScrollPane = new JScrollPane(linguisticTypeTable);
		
		//layout components
		destinationTierConfigurationPanel = new JPanel(new GridBagLayout());
		destinationTierConfigurationPanel.setBorder(new TitledBorder(ElanLocale.getString("DestTierAndType.Panel.Title.DestTierConfiguration")));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = globalInset;
		destinationTierConfigurationPanel.add(newTierNameLabel, gbc);
		
		gbc.gridx = 1;
		destinationTierConfigurationPanel.add(tierNameDesLabel, gbc);		
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;	
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		destinationTierConfigurationPanel.add(newTierNameField, gbc);
		
		gbc.gridy = 2;
		destinationTierConfigurationPanel.add(decideRootOrChildLabel, gbc);
		
		gbc.gridy = 3;
		gbc.insets = singleTabInset;
		destinationTierConfigurationPanel.add(rootTierRB, gbc);
		
		JPanel childTierPanel = new JPanel(new GridBagLayout());
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		destinationTierConfigurationPanel.add(childTierPanel, gbc);
		
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;		
		
		gbc1.anchor = GridBagConstraints.NORTHWEST;		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		childTierPanel.add(childTierRB, gbc1);
		
		gbc1.gridx = 1;
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.weightx = 0.0;
		childTierPanel.add(parentTierCB, gbc1);
		
		
//		gbc.gridy = 4;
//		gbc.gridwidth = 1;
//		gbc.weightx = 0.0;
//		destinationTierConfigurationPanel.add(childTierRB, gbc);
//		
//		gbc.gridx = 1;
//		gbc.weightx = 1.0;	
//		destinationTierConfigurationPanel.add(parentTierCB, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets =  new Insets(0, 60, 5, 10);;
		destinationTierConfigurationPanel.add(destTypeLabel, gbc);	
		
		gbc.gridy = 6;
		gbc.insets = globalInset;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		destinationTierConfigurationPanel.add(lingTypeLabel, gbc);
		
		gbc.gridx = 1;
		destinationTierConfigurationPanel.add(typeDesLabel, gbc);		
		
		gbc.gridx = 0;
		gbc.gridy = 7;		
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		destinationTierConfigurationPanel.add(lingTypeTableScrollPane, gbc);
		
		gbc.gridy = 8;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, globalInset.left, 2, 0);
		destinationTierConfigurationPanel.add(createTypesButton, gbc);			
		createTypesButton.addActionListener(this);
		
		if (!multiFileMode) {	
			gbc.gridy = 9;
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;			
			destinationTierConfigurationPanel.add(hideTypesWithCVCB, gbc);
			Boolean storedPref = Preferences.getBool("DestinationType.HideTypesWithCV", null);
			if (storedPref != null) {
				hideTypesWithCVCB.setSelected(storedPref);
			}
			hideTypesWithCVCB.addActionListener(this);			
		}
		
		setLayout(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1.0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = globalInset;			
		add(destinationTierConfigurationPanel, gbc);
	}
	
	@Override
	public abstract String getStepTitle();
	
	@Override
	public void enterStepForward(){		
		tierSet = (TreeSet<String>) multiPane.getStepProperty("AllTiers");		
		
		// if multiple file import
		if(multiFileMode){
			rootTierTypeNames = 	(List<String>) multiPane.getStepProperty("RootTierTypes");
			childTierTypeNames = 	(List<String>) multiPane.getStepProperty("ChildTierTypes");	
			completeTypeNamesList = (List<String>) multiPane.getStepProperty("CompleteTierTypes");	
			
			if(completeTypeNamesList == null){
				completeTypeNamesList = new ArrayList<String>();
			}
			
			if(newRootTypeList.size() > 0){
				if(rootTierTypeNames == null){
					rootTierTypeNames = new ArrayList<String>();
				}
				
				for(String type : newRootTypeList){
					if(!rootTierTypeNames.contains(type)){
						rootTierTypeNames.add(type);
					}
					
					if(!completeTypeNamesList.contains(type)){
						completeTypeNamesList.add(type);
					}
				}
			}
			
			if(newChildypeList.size() > 0){
				if(childTierTypeNames == null){
					childTierTypeNames = new ArrayList<String>();
				}
				
				for(String type : newChildypeList){
					if(!childTierTypeNames.contains(type)){
						childTierTypeNames.add(type);
					}
					
					if(!completeTypeNamesList.contains(type)){
						completeTypeNamesList.add(type);
					}
				}
			}
		}
		
		
		String selectedParentTier = null;		
		if(childTierRB.isSelected()){
			selectedParentTier = (String) parentTierCB.getSelectedItem();
		}
				
		//get selected tiers
		List<String> tierList = (List<String>) multiPane.getStepProperty("SelectedTiers");
		parentTierCB.removeAllItems();
		
		//add tier names that are selected in tier table in step 1
		for( int i=0; i<tierList.size(); i++ ){
			//TierImpl tier = (TierImpl) transcription.getTierWithId( (String)tierList.get(i) );
			parentTierCB.addItem(tierList.get(i));
			//parentTierCB.addItem( tierList.get(i).toString() );
		}			
		
		if(tierList.contains(selectedParentTier)){
			parentTierCB.setSelectedItem(selectedParentTier);
		} else if(childTierRB.isSelected()){
			selectedParentTier = (String) parentTierCB.getSelectedItem();
		}
		
		((LinguisticTypeTableModel)linguisticTypeTable.getModel()).updateLinguisticTypes(selectedParentTier);	
		if(linguisticTypeTable.getRowCount() == 1){
			linguisticTypeTable.setRowSelectionInterval(0,0);
		}
		
		updateButtonStates();
		if(!validName){
			validateTierName();
		}
		
		if(!validType){
			validateType();
		}
		
		newTierNameField.requestFocusInWindow();
	}	

	@Override
	public void enterStepBackward(){
		updateButtonStates();
	}
	
	@Override
	public boolean leaveStepForward(){
		// validate inputs		
		validateTierName();
		validateType();		
		if(!validName){			
			newTierNameField.addKeyListener(this);
		}
		
		if(!validType){
			linguisticTypeTable.getSelectionModel().addListSelectionListener(this);
		}
		
		if(!validType || !validName){
			updateButtonStates();
			return false;
		}
			
		//retrieve name for destination tier
		multiPane.putStepProperty("DestinationTierName", newTierNameField.getText().trim());		
				
		//retrieve linguistic type
		int selectedRow = linguisticTypeTable.getSelectedRow();
		String linguisticType;
				
		linguisticType = (String)linguisticTypeTable.getModel().getValueAt(selectedRow, 0);
		
		multiPane.putStepProperty("linguisticType", linguisticType);
		
		//parent tier	
		String parentTierName = null;
		if( childTierRB.isSelected() ){
			parentTierName = (String) parentTierCB.getSelectedItem();
			//parentTier = transcription.getTierWithId(tierName);
		}				
		multiPane.putStepProperty("ParentTierName", parentTierName);
		if (!multiFileMode) {
			Preferences.set("DestinationType.HideTypesWithCV", Boolean.valueOf(hideTypesWithCVCB.isSelected()), 
				null, false, false);
		}
		
		return true;
	}		
	
	/**
	 * Validates the tier name entered in the text box and
	 * shows a error message if required
	 */
	private void validateTierName(){
		String newTierName = newTierNameField.getText().trim();
		String message = null;		
		if(newTierName.length() > 0 && !tierSet.contains(newTierName)) {
			validName = true;	
			newTierNameField.setForeground(Color.BLACK);			
			tierNameDesLabel.setText("");			
		} else{		
			validName = false;
			newTierNameField.setForeground(Color.RED);				
			if(newTierName.length() <= 0){
				message =  ElanLocale.getString("DestTierAndType.Message2");
			}else{
				message =  ElanLocale.getString("DestTierAndType.Message1.Part1") + 
						newTierName + ElanLocale.getString("DestTierAndType.Message1.Part2");
			}
			tierNameDesLabel.setText(" - " + message);
		}		
	}
	
	/**
	 * Checks whether a linguistic type is selection and
	 * shows a error message to the user if required
	 */
	private void validateType(){	
		if(linguisticTypeTable.getRowCount() > 0){
			if(linguisticTypeTable.getSelectedRowCount() > 0){
				validType = true;
				lingTypeLabel.setForeground(Color.BLACK);
			}else{			
				lingTypeLabel.setForeground(Color.RED);	
				validType = false;
			}
		} else {
			validType = false;			
		}
	}	
	
	/**
	 * Updates the button states according to some constraints (like everything has to be filled in, consistently)
	 */
	public void updateButtonStates(){			
		parentTierCB.setEnabled(childTierRB.isSelected());		
		destTypeLabel.setVisible(childTierRB.isSelected());
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, validName && validType);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, true);
	}

	/**
	 * Value changed event handler that updates button states when new selection is made
	 */
	@Override
	public void valueChanged(ListSelectionEvent arg0) {	
		validateType();
		updateButtonStates();	
	}

	/**
	 * Not used
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		validateTierName();
	}

	/**
	 * Key release handler that updates the button states
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		validateTierName();
		updateButtonStates();
	}

	/**
	 * Key typed handler that updates the button states
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		validateTierName();
		updateButtonStates();
	}
	
	//action listener for parent tier checkbox and parent tier combobox
	@Override
	public void actionPerformed(ActionEvent e) {	
		
		if(e.getSource() == createTypesButton){
			if(multiFileMode){
				// create a small dialog
				if(createTypeDialog == null){
					createTypeDialog = new CreateTypeDialog();
					createTypeDialog.updateStereoType();
				}
				
				
				createTypeDialog.setVisible(true);
				String typeName = createTypeDialog.getTypeName();
				if(typeName != null && typeName.trim().length() > 0){	
					if(rootTierRB.isSelected()){			
						if(rootTierTypeNames == null){
							rootTierTypeNames = new ArrayList<String>();
						}
						newRootTypeList.add(typeName);
						rootTierTypeNames.add(typeName);					
					} else {
						if(childTierTypeNames == null){
							childTierTypeNames = new ArrayList<String>();
						}					
						newChildypeList.add(typeName);
						childTierTypeNames.add(typeName);
					}	
					completeTypeNamesList.add(typeName);
					updateTypeList();						
				}							
			}else {
				int types = transcription.getLinguisticTypes().size();
				if(transcription.getLinguisticTypes() != null){
					types = transcription.getLinguisticTypes().size();
				}
				
				new EditTypeDialog2(ELANCommandFactory.getRootFrame(transcription), true, transcription, EditTypeDialog2.ADD).setVisible(true);
				updateTypeList();				
			}
		} else{
			//depending on the checkbox selected, update the possible linguistic types
			if(createTypeDialog != null){				
				createTypeDialog.updateStereoType();
			}
			updateTypeList();			
		}		
	}
	
	private void updateTypeList(){	
		
		String parentTierName = null;
		LinguisticTypeTableModel model = (LinguisticTypeTableModel) linguisticTypeTable.getModel();
		 
		if ( childTierRB.isSelected() ) {
			parentTierName = (String)parentTierCB.getSelectedItem();
		}
		// store selected
		int selectedRow = linguisticTypeTable.getSelectedRow();
		String curSelType = null;
		if (selectedRow > -1) {
			curSelType = (String) linguisticTypeTable.getValueAt(selectedRow, 0);
		}
		
		model.updateLinguisticTypes(parentTierName);
		
		if(model.getRowCount() == 1){
			linguisticTypeTable.getSelectionModel().setSelectionInterval(0, 0);
		}
		
		// restore selected
		if (curSelType != null) {
			String name;
			for (int i = 0; i < linguisticTypeTable.getRowCount(); i++) {
				name = (String) linguisticTypeTable.getValueAt(i, 0);
				if (curSelType.equals(name)) {
					linguisticTypeTable.setRowSelectionInterval(i, i);
					break;
				}
			}
		}	
		if(!validType){
			validateType();
		}
		updateButtonStates();
	}
	
	/**
	 * Simple dialog which allows you to specify the
	 * new linguistic type name
	 * 
	 * @author aarsom
	 *
	 */
	private class CreateTypeDialog extends JDialog{
		
		private JLabel stereotypeLabel, errLabel;
		private JTextField typeField;
		private JButton okButton;
		private JButton cancelButton;
		
		private String newTypeName = null;
		
		/**
		 * Constructor
		 */
		public CreateTypeDialog(){
			super(multiPane.getDialog(), ElanLocale.getString("EditTypeDialog.Title.Add"), true);			
			initialize();
			setLocationRelativeTo(getParent());
			pack();
		}
		
		/**
		 * Initialize
		 */
		private void initialize(){		
			getContentPane().setLayout(new GridBagLayout());
			stereotypeLabel = new JLabel();
			updateStereoType();	
			errLabel = new JLabel(ElanLocale.getString("EditTypeDialog.Message.Exists"));
			errLabel.setFont(new Font(errLabel.getFont().getFontName(), Font.PLAIN, 10));
			errLabel.setForeground(Color.RED);
			
			okButton = new JButton(ElanLocale.getString("Button.OK"));		
			cancelButton = new JButton(ElanLocale.getString("Button.Cancel"));			
			typeField = new JTextField();	
			typeField.addKeyListener(new KeyListener(){

				@Override
				public void keyTyped(KeyEvent e) {
					validateTypeName();					
				}

				@Override
				public void keyPressed(KeyEvent e) {
					validateTypeName();					
				}

				@Override
				public void keyReleased(KeyEvent e) {
					validateTypeName();					
				}
			});			
			
			ActionListener listener = new ActionListener(){
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(e.getSource() == okButton){
						newTypeName = typeField.getText().trim();	
					} else{
						newTypeName = null;
					}					
					dispose();	
								
				}				
			};			
			okButton.addActionListener(listener);
			cancelButton.addActionListener(listener);
			
			JPanel buttonPanel = new JPanel(new GridLayout(1,2));
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			
			GridBagConstraints gbc = new GridBagConstraints();		
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.NONE;		
			gbc.insets = new Insets(5,10,5,10);
			getContentPane().add(new JLabel(ElanLocale.getString("EditTypeDialog.Label.Stereotype") + " : "), gbc);
			
			gbc.gridx = 1;
			getContentPane().add(stereotypeLabel, gbc);
			
			gbc.gridx = 0;
			gbc.gridy = 1;
			getContentPane().add(new JLabel(ElanLocale.getString("EditTypeDialog.Label.Type")), gbc);
			
			gbc.gridx = 1;			
			gbc.fill = GridBagConstraints.HORIZONTAL;	
			gbc.weightx = 1.0;			
			getContentPane().add(typeField, gbc);
			
			gbc.gridx = 1;			
			gbc.fill = GridBagConstraints.HORIZONTAL;	
			gbc.weightx = 1.0;			
			getContentPane().add(typeField, gbc);
			
			gbc.gridy = 2;
			gbc.fill = GridBagConstraints.NONE;	
			gbc.weightx = 0.0;	
			getContentPane().add(errLabel, gbc);			
			
			gbc.gridx = 0;	
			gbc.gridy = 3;
			gbc.gridwidth = 2;
			
			gbc.anchor = GridBagConstraints.EAST;
			getContentPane().add(buttonPanel, gbc);
		}
		
		
		private void validateTypeName(){
			String typeName = typeField.getText();
			if(typeName == null || 
					typeName.trim().length() <= 0){
				typeField.setForeground(Color.BLACK);
				okButton.setEnabled(false);
				return;
			}
			
			if(completeTypeNamesList.contains(typeName)){
				typeField.setForeground(Color.RED);
				errLabel.setText(ElanLocale.getString("EditTypeDialog.Message.Exists"));
				okButton.setEnabled(false);
			} else {
				typeField.setForeground(Color.BLACK);
				errLabel.setText("");
				okButton.setEnabled(true);
			}
			
			
		}
		/**
		 * Returns the new type name
		 * 
		 * @return
		 */
		public String getTypeName(){
			return newTypeName;
		}
		
		/**
		 * 
		 */
		@Override
		public void setVisible(boolean b){
			newTypeName = null;
			typeField.setText("");
			errLabel.setText("");
			super.setVisible(b);
			if(b){
				typeField.requestFocusInWindow();
			}
		}
		
		/**
		 * Updates the stereotype
		 */
		public void updateStereoType(){
			String stereotype;
			if(rootTierRB.isSelected()){			
				stereotype = "None";							
			} else {
				stereotype = "Included In";
			}
			
			stereotypeLabel.setText(stereotype);
		}
	}
	
	/**
	 * Table Model for  linguistic types
	 * @author Jeffrey Lemein
	 * @version July, 2010
	 */
	private class LinguisticTypeTableModel extends AbstractTableModel{
		private String[] columnNames;
		private String[][] rowData;		
		
		/**
		 * Constructor
		 */
		public LinguisticTypeTableModel(){
			columnNames = new String[2];
			columnNames[0] = new String( ElanLocale.getString("DestTierAndType.Column.LinguisticType") );
			columnNames[1] = new String(ElanLocale.getString("DestTierAndType.Column.Stereotype") );			
			
			updateLinguisticTypes(null);
		}
		
		/**
		 * Adds the linguistic types to the linguistic type table (by looking at the parent tier name and ...)
		 * @param parentTierName
		 */
		public void updateLinguisticTypes(String parentTierName){
			List<String> linguisticTypeNames = new ArrayList<String>();
			String stereotypeName = null;
			//SINGLE FILE MODE
			if( !multiFileMode ){
				boolean hideTypesWithCV = hideTypesWithCVCB.isSelected();
				//only one file is used
				List<LinguisticType> linguisticTypeList = transcription.getLinguisticTypes();
				if( parentTierName == null ){
					//to be created tier will be root tier (so only add top-level linguistic types)
					for( LinguisticType lt : linguisticTypeList ) {
						if( !lt.hasConstraints() && (!lt.isUsingControlledVocabulary() || !hideTypesWithCV) ){
							linguisticTypeNames.add(lt.getLinguisticTypeName());							
						}
					}
					stereotypeName = "";
				} else{
					//to be created tier has a parent tier (so included_in, top-level linguistic type (if parent has same linguistic type))
					//Tier parentTier = transcription.getTierWithId(parentTierName);
					
					for( LinguisticType lt : linguisticTypeList ){
						//don't add linguistic types that are using controlled vocabulary
						if( lt.isUsingControlledVocabulary() && hideTypesWithCV) {
							continue;
						}
						
						//add linguistic type if it equals included_in
						if( lt.hasConstraints() && lt.getConstraints().getStereoType() == Constraint.INCLUDED_IN ){
							linguisticTypeNames.add(lt.getLinguisticTypeName());							
							//JOptionPane.showMessageDialog(null, lt.getLinguisticTypeName() + " added");
							continue;
						}
						stereotypeName = Constraint.stereoTypes[ Constraint.INCLUDED_IN ];
						
						//and if parent tier has top level linguistic type, add this type too
						/**if( !lt.hasConstraints() ){
							linguisticTypeNames.add(lt.getLinguisticTypeName());
							stereotypeNames.add("");
							//JOptionPane.showMessageDialog(null, lt.getLinguisticTypeName() + " added");
							continue;
						}	**/						
					}
				}
			//MULTIPLE FILE MODE
			}else{
				//we work with multiple transcription/files
			
				//NEW ROOT TIER
				if( parentTierName == null && rootTierTypeNames != null){
					linguisticTypeNames.addAll(rootTierTypeNames);
					stereotypeName = "";
				//NEW CHILD TIER
				}else{
					//a parent tier is selected, so add included_in
					if(childTierTypeNames != null){
						linguisticTypeNames.addAll(childTierTypeNames);
						stereotypeName = Constraint.stereoTypes[ Constraint.INCLUDED_IN ];
					}
				}
			}
			
			//add the linguistic type with its stereotype
			final int SIZE = linguisticTypeNames.size();
			rowData = new String[SIZE][];
			for( int i=0; i<SIZE; i++ ) {
				rowData[i] = new String[]{ linguisticTypeNames.get(i), stereotypeName };
			}			
			
			if(SIZE == 0){
				typeDesLabel.setText(ElanLocale.getString("DestTierAndType.Message.lingType"));		
				lingTypeLabel.setForeground(Color.BLACK);
			} else {
				typeDesLabel.setText("");						
			}
			fireTableDataChanged();
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			if( rowData != null ) {
				return rowData.length;
			} else {
				return 0;
			}
		}

		@Override
		public Object getValueAt(int row, int col) {
			return rowData[row][col];
		}		
	}
}