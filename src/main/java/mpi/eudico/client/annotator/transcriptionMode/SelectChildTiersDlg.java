package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.ClosableDialog;
import mpi.eudico.client.util.CheckBoxTableCellRenderer;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Class to select the tiers for each column and
 * to show or hide the tiers
 * 
 * @author Aarthy Somasundaram
 *
 */
@SuppressWarnings("serial")
public class SelectChildTiersDlg extends ClosableDialog implements ActionListener{
	
	/** default String */
	private static final String defaultValue = ElanLocale.getString("TranscriptionManager.SelectTierDlg.DefaultValue");
	
	/** default String */
	private static final String NO_TIER = ElanLocale.getString("TranscriptionManager.SelectTierDlg.No_Tier");
	
	/** default String */
	private static final String EMPTY_TIER = ElanLocale.getString("TranscriptionManager.SelectTierDlg.Empty_Tier");
	
	/** column id for the tier name column */
    private final String TIER_NAME_COLUMN = ElanLocale.getString("TranscriptionManager.SelectTierDlg.Tier_Name_Column");    
    
    /** column id for the check box to show/hide tiers */
    private final String HIDE_COLUMN = ElanLocale.getString("TranscriptionManager.SelectTierDlg.Show_Hide_Tiers"); 
    
    private final String COLUMN_PREFIX = ElanLocale.getString("TranscriptionTable.ColumnPrefix");
    
    private JTable selectionTable;
	DefaultTableModel model;
    private Transcription transcription ;

    private JButton okButton;
    private JButton cancelButton;
    
    /** structure : Map<refTier, List of all linked tiers> 
     *  the current tierMap of the transcription table
     */
 	private Map<TierImpl, List<TierImpl>> tierMap;
 	
 	/**structure : Map<refTierName, < typeName, List of all tierNames of this type>  */
 	private Map<String, Map<String, List<String>>> typeTierMap;
 	
 	private List<String> columnTypeList; 	
 	private List<String> hiddenTiers;
 	
 	private boolean valueChanged = false;
 	
 	/**
 	 * Creates an instance of SelectChildTiersDlg
 	 * @param tierMap 
 	 * 
 	 * @param layoutManager, ElanLayoutManager
 	 * @param type1, type for first column
 	 * @param type2, type for second column
 	 * @param type3, type for third column
 	 */
    public SelectChildTiersDlg(ElanLayoutManager layoutManager, Map<TierImpl, List<TierImpl>> tierMap, List<String> hiddenTiers, List<String> columnTypeList){
        super(layoutManager.getElanFrame(), ElanLocale.getString("TranscriptionManager.SelectTierDlg.Title"), true);    
       
        transcription = layoutManager.getViewerManager().getTranscription();
        this.hiddenTiers = new ArrayList<String>();
        this.columnTypeList = new ArrayList<String>();
        this.tierMap = new HashMap<TierImpl, List<TierImpl>>();
        
        this.columnTypeList.addAll(columnTypeList);
        if(tierMap== null){
        	loadMap();
        } else {
        	this.tierMap.putAll(tierMap);
        }
        
        if(hiddenTiers != null){
        	this.hiddenTiers.addAll(hiddenTiers);
        } 
		typeTierMap = new HashMap<String, Map<String, List<String>>>();
		initComponents();
		fillTable();
		postInit();
    }
    
    /**
     * Pack, size and set location.
     */
    private void postInit() {
    	pack();       
        setResizable(true);
        setLocationRelativeTo(getParent());
    }
	
	/**
     * Initializes the components for ui.
     */
    public void initComponents() {      	
//    	 setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         getContentPane().setLayout(new GridBagLayout());
        
         model = new DefaultTableModel(){     	   
        	 @Override
			public boolean isCellEditable(int row, int column) {        	
        		 if(getColumnName(column).equals(TIER_NAME_COLUMN)){
        			 return false;
        		 } 
        		 if(column != 0){
        			 Object value = this.getValueAt(row, 0);
        			 if(value instanceof Boolean && !((Boolean) value).booleanValue()){
        				 return false;
        			 }
        			 value = this.getValueAt(row, column);
                	 if(value != null && value instanceof String && value.equals(NO_TIER)){
                		 return false;
                	 } 
        		 }		
        		 
        		 return true;
        	 }
         };
         
         DefaultTableCellRenderer render = new DefaultTableCellRenderer(){
        	 
        	 @Override
			public Component getTableCellRendererComponent(JTable table,
        			 Object value, boolean isSelected, boolean hasFocus, int row,
        			 	int column){
        		 
        		 if(table.getColumnName(column).equals(TIER_NAME_COLUMN)){
        			 this.setText((String) value);  
        			 this.setFont(new Font(table.getFont().getFontName(), Font.ITALIC, table.getFont().getSize()));
        			 this.setBackground(Color.LIGHT_GRAY);
        			 this.setForeground(Color.BLACK);
        			 this.setOpaque(true);
        			 return this;         			
        		 }   
        		 
        		 boolean rowSelected = (Boolean)table.getValueAt(row, 0);    
        		 boolean deSelectRow = false;
        		 
        		 if(table.isEditing() && table.getEditingRow() == row && table.getEditingColumn() == 0){
        			 if(rowSelected){
        				 deSelectRow = true;        				 
        			 } 
        		 } else {
        			 if(!rowSelected){
        				 deSelectRow = true;
        			 } 
        		 }
        		 
        		 this.setText((String) value);  
        		 
        		 if(deSelectRow){
        			 setBackground(Color.LIGHT_GRAY);
        			 setForeground(Color.GRAY);
        			 setOpaque(true);
        		 }else {
        			 setBackground(Color.WHITE);
        			 setOpaque(false);
        			 
        			 if(value != null && (value.equals(defaultValue) || value.equals(NO_TIER))){    			   
            			 setForeground(Color.GRAY);
            		 } else {
            			 setForeground(Color.BLACK);
            		 } 
        		 }
        		        		 
        		 if(value != null && ((value.equals(NO_TIER)  || value.equals(EMPTY_TIER)))){    			   
        			 setFont(new Font(table.getFont().getFontName(), Font.ITALIC, table.getFont().getSize()));
        		 }     			       		 
        		 return this;    		   
        	 }
         };
         
         model.setColumnCount(columnTypeList.size()+1); 
         String columnIdentifiers[] = new String[columnTypeList.size()+2];
         columnIdentifiers[0] = HIDE_COLUMN;
         columnIdentifiers[1] = TIER_NAME_COLUMN;
         for(int i=0; i < columnTypeList.size(); i++){
        	 columnIdentifiers[i+2] = COLUMN_PREFIX + " " + (i+1) +" : " + columnTypeList.get(i);
         } 
         
         model.setColumnIdentifiers(columnIdentifiers);        
         selectionTable = new JTable(model);
         //selectionTable.setFont(Constants.DEFAULTFONT);
         selectionTable.setCellSelectionEnabled(true);		
         selectionTable.setShowGrid(true);
         selectionTable.setGridColor(Color.BLACK);
         selectionTable.setDefaultEditor(Object.class, new TableCellEditor());   
         selectionTable.setRowHeight(25);         
         selectionTable.setDefaultRenderer(Object.class, render);    
         selectionTable.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxTableCellRenderer());
         selectionTable.getColumnModel().getColumn(0).setCellEditor(new TableCellEditor(new JCheckBox()));
         
         JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 2));
         okButton = new JButton();
         okButton.setText(ElanLocale.getString("Button.Apply"));
         //okButton.setEnabled(false);
         okButton.addActionListener(this);
         cancelButton = new JButton();
         cancelButton.setText(ElanLocale.getString("Button.Cancel"));
         cancelButton.addActionListener(this);
         buttonPanel.add(okButton);
         buttonPanel.add(cancelButton);
                
         Insets insets = new Insets(2, 6, 2, 6);
         GridBagConstraints gridBagConstraints = new GridBagConstraints(); 
         
         JScrollPane tierScrollPane = new JScrollPane(selectionTable);
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 1;
         gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = insets;
         gridBagConstraints.fill = GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         getContentPane().add(tierScrollPane, gridBagConstraints);  
         
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = GridBagConstraints.SOUTH;
         gridBagConstraints.insets = insets;
         getContentPane().add(buttonPanel, gridBagConstraints); 
         
         addWindowListener(new WindowAdapter() {
             @Override
			public void windowClosing(WindowEvent we) {
                 doClose();
             }
         });
    }
    
    /**
     * Closes this dialog
     */
    private void doClose() {
		setVisible(false);
		dispose();
	}
    
    /**
     * Returns a list of tierNames of the given type 
     * linked with the given reference tier
     * 
     * @param refTier, the reference tier (top level tiers)
     * @param type, the linguistic type name of the tiers to be returned
     * @return linkedTiers, a list of all tier names of this type linked to the 
     * 						reference tier
     */
    private List<String> getLinkedTiersOfType(TierImpl refTier, String type){
    	List<TierImpl> childTiers = refTier.getChildTiers();
    	List<String> linkedTiers = new ArrayList<String>();
    	for(int i=0; i < childTiers.size(); i++){
			TierImpl childTier = childTiers.get(i);
			if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
				if(childTier.getLinguisticType().getLinguisticTypeName().equals(type)){	
					linkedTiers.add(childTier.getName());
				}
				// get all the types of the tiers depending on this child tier
				List<TierImpl> dependentTiers = childTier.getDependentTiers();
				for(int y=0; y < dependentTiers.size(); y++) {
					TierImpl dependantTier = dependentTiers.get(y);	
					if(dependantTier.getLinguisticType().getLinguisticTypeName().equals(type)){								
						linkedTiers.add(dependantTier.getName());	
					}
				}
			}
		} 	    	
    	return linkedTiers;	   
    }    
    
    /**
     * same as the above method getLinkedTiersOfType(TierImpl refTier, String type) except
     * the return type.This method returns list of TierImpl objects
     * 
     * @param refTier, the reference tier (top level tiers)
     * @param type, the linguistic type name of the tiers to be returned
     * @return linkedTiers, a list of all tiers of this type linked to the 
     * 						reference tier
     */
    private List<TierImpl> getLinkedTiers(TierImpl refTier, String type){
    	List<TierImpl> childTiers = refTier.getChildTiers();
    	List<TierImpl> linkedTiers = new ArrayList<TierImpl>();
    	for(int i=0; i < childTiers.size(); i++){
			TierImpl childTier = childTiers.get(i);
			if(childTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
				if(childTier.getLinguisticType().getLinguisticTypeName().equals(type)){	
					linkedTiers.add(childTier);
				}
				// get all the types of the tiers depending on this child tier
				List<TierImpl> dependentTiers = childTier.getDependentTiers();
				for(int y=0; y < dependentTiers.size(); y++) {
					TierImpl dependantTier = dependentTiers.get(y);	
					if(dependantTier.getLinguisticType().getLinguisticTypeName().equals(type)){								
						linkedTiers.add(dependantTier);	
					}
				}
			}
		}
    	return linkedTiers;	   
    }
    
    /**
     * Fills the table with values
     */
    private void fillTable(){       	
    	
    	boolean column1ParentType = false;
    	List<TierImpl> parentTierList = new ArrayList<TierImpl>();
    	List<? extends Tier> tierList = transcription.getTiersWithLinguisticType(this.columnTypeList.get(0));
    	for(int i= 0 ; i < tierList.size(); i++){
    		TierImpl tier = (TierImpl) tierList.get(i);		
    		
    		//if type selected for the first column is not symbolic associated type
    		if(tier.getLinguisticType().getConstraints() == null || tier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION ){	
    			column1ParentType = true;
    			if(parentTierList.contains(tier)){
    	    		continue;
    	    	}     				
    	    	parentTierList.add(tier);    		    	
    		} else {				
    			TierImpl parentTier = tier.getParentTier();
    			while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
    				parentTier = parentTier.getParentTier();
    			}	
    			
    			if(parentTierList.contains(parentTier)){
    	    		continue;
    	    	} 
    			parentTierList.add(parentTier);	    				
    		}
    	}
    	
    	
       	for(int x = 0; x < parentTierList.size(); x++){
        	TierImpl refTier = parentTierList.get(x);	
        	String refTierName = refTier.getName(); 
        	HashMap<String, List<String>> typeMap = new HashMap<String, List<String>>();
        	typeTierMap.put(refTierName, typeMap ); 
        	
        	if(hiddenTiers != null){
        		if(hiddenTiers.contains(refTierName)){
        			model.addRow(new Object[] {Boolean.FALSE, refTierName}); 
        		}else {
        			model.addRow(new Object[] {Boolean.TRUE, refTierName}); 
        		}
        	} else {
        		model.addRow(new Object[] {Boolean.TRUE, refTierName}); 
        	}
        	
        	
        	List<TierImpl> currentTierList = tierMap.get(refTier);
        	if( currentTierList != null && currentTierList.size() != columnTypeList.size()){
        		currentTierList = null;
        	}
        	for(int i=0; i < columnTypeList.size(); i++){  
        		
        		List<String> tierNames = new ArrayList<String>();
        		if(i==0 && column1ParentType){        			
        			tierNames.add(refTierName);  
        		} else {
        			tierNames = getLinkedTiersOfType(refTier, columnTypeList.get(i));
        		}
        		typeMap.put(columnTypeList.get(i), tierNames);  
        		switch(tierNames.size()){
        		case 0:
        			model.setValueAt(NO_TIER, model.getRowCount()-1, i+2);
        			break;
        		case 1 :        			
        			//checks for the current status in the table
        			if( currentTierList != null){
        				TierImpl tier = currentTierList.get(i);
        				if(tier != null){
        					String tierName = tier.getName();
        					if(tierNames.contains(tierName)){
        						model.setValueAt(tierName, model.getRowCount()-1, i+2);
        					} else {
        						model.setValueAt(EMPTY_TIER, model.getRowCount()-1, i+2);
        					}   
        				}else {
    						model.setValueAt(EMPTY_TIER, model.getRowCount()-1, i+2);
    					}   
        			} else {
        				boolean valueSet = false;
        				for (int c=2; c< i+2; c++){
    						String tierName = (String) model.getValueAt(model.getRowCount()-1,c);
    						if(tierNames.get(0).equals(tierName)){    							
    							model.setValueAt(EMPTY_TIER,model.getRowCount()-1,i+2);
    							valueSet = true;
    							break;
    						}
        				}
        				if(!valueSet){
        					model.setValueAt(tierNames.get(0), model.getRowCount()-1, i+2);
        				}
        			}
        			//model.setValueAt(tierNames.get(0), model.getRowCount()-1, i+2);
        			break;
        		default:        			
        			if( currentTierList != null){
        				TierImpl tier = currentTierList.get(i);
        				if(tier != null){
        					String tierName = tier.getName();
        					if(tierNames.contains(tierName)){
        						model.setValueAt(tierName, model.getRowCount()-1, i+2);
        					} else {
        						model.setValueAt(defaultValue, model.getRowCount()-1, i+2);
        					}   
        				} else {
            				model.setValueAt(defaultValue, model.getRowCount()-1, i+2);
            			}
        			} else {
        				model.setValueAt(defaultValue, model.getRowCount()-1, i+2);
        			}
        		}
        	}
       	}
    }
    
    
    /**
     * Loads the tierMap to fill the table if the transcription table is not yet loaded
     */
    private void loadMap(){    	
		List<? extends Tier> tiers  = transcription.getTiersWithLinguisticType(columnTypeList.get(0));	
		List<TierImpl> parentTierListType = new ArrayList<TierImpl>();	 	
		//List annotationsList = new ArrayList();
			
		List<String> types = new ArrayList<String>();
		for(int i=0; i< columnTypeList.size(); i++){
			if(!types.contains(columnTypeList.get(i))){
				types.add(columnTypeList.get(i));
			}
		}
		
		if(tiers != null && tiers.size() > 0){
			TierImpl tierC1 = (TierImpl)tiers.get(0);	
			
			// if columnType1 is symbolic Associatedtype
	 		if(tierC1.getLinguisticType().getConstraints() != null && tierC1.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){ 
	 			for(int x = 0; x < tiers.size(); x++){						
					TierImpl tier = (TierImpl)tiers.get(x);	
					TierImpl parentTier = tier.getParentTier();
					while(parentTier != null && (parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION)){
						parentTier = parentTier.getParentTier();
					}
					
					if(parentTierListType.contains(parentTier)){
						continue;
					} 
					
					parentTierListType.add(parentTier);
					List<TierImpl> linkedTiers = tierMap.get(parentTier);
					List<TierImpl> matchedTiersType = new ArrayList<TierImpl>(); 					
					
					if(linkedTiers !=null){						
						if(linkedTiers.size() < columnTypeList.size()){
							for(int i= linkedTiers.size(); i < columnTypeList.size(); i++){
								linkedTiers.add(null);
							}
						} else if(linkedTiers.size() > columnTypeList.size()){
							for(int i= linkedTiers.size(); i > columnTypeList.size(); i--){
								linkedTiers.remove(i-1);
							}
						}
					} else {
						linkedTiers = new ArrayList<TierImpl>();		
						for(int i= 0; i< columnTypeList.size(); i++){
							linkedTiers.add(null);
						}
					}	
					
					for(int c=0; c< types.size() ; c++){
						matchedTiersType.clear();
						matchedTiersType.addAll(getLinkedTiers(parentTier, types.get(c)));						
						
						
						if(types.size() != columnTypeList.size()){
							for(int i= c; i < columnTypeList.size();i++){
								if(columnTypeList.get(i).equals(types.get(c))){
									//int index = columnTypeList.indexOf(columnTypeList.get(i));									
									
									if(linkedTiers.get(i) == null || !matchedTiersType.contains(linkedTiers.get(i))){
										if(matchedTiersType.size() >=1){
											linkedTiers.set(i,matchedTiersType.get(0));
											matchedTiersType.remove(matchedTiersType.get(0));
										} else {
											linkedTiers.set(i,null);
										}
									}else if(matchedTiersType.contains(linkedTiers.get(i))){
										matchedTiersType.remove(linkedTiers.get(i));
									}
								}
							}						
						} else {
							int index = columnTypeList.indexOf(columnTypeList.get(c));
							if(linkedTiers.get(index) == null || !matchedTiersType.contains(linkedTiers.get(index))){
								if(matchedTiersType.size() >=1){
									linkedTiers.set(index,matchedTiersType.get(0));
									matchedTiersType.remove(matchedTiersType.get(0));
								} else {
									linkedTiers.set(index,null);
								}
							}else if(matchedTiersType.contains(linkedTiers.get(index))){
								matchedTiersType.remove(linkedTiers.get(index));
							}
						}
					}					
					tierMap.put(parentTier,linkedTiers);
	 			}
	 		} else {		
	 			
	 			//List<TierImpl> parentTiers = new ArrayList<TierImpl>();
	 			for(int x = 0; x < tiers.size(); x++){
	 				TierImpl tier = (TierImpl)tiers.get(x);
	 				parentTierListType.add(tier); 	
	 				
	 				List<TierImpl> linkedTiers = tierMap.get(tier);
					List<TierImpl> matchedTiersType = new ArrayList<TierImpl>(); 				
					
					if(linkedTiers !=null){						
						if(linkedTiers.size() < columnTypeList.size()){							
							for(int i= linkedTiers.size(); i < columnTypeList.size(); i++){
								linkedTiers.add(null);
							}
						}else if(linkedTiers.size() > columnTypeList.size())		{
							for(int i= linkedTiers.size(); i > columnTypeList.size(); i--){
								linkedTiers.remove(i-1);
							}
						}
					}else {
						linkedTiers = new ArrayList<TierImpl>();		
						for(int i= 0; i< columnTypeList.size(); i++){
							linkedTiers.add(null);
						}
					}	
					linkedTiers.set(0, tier);
					
					for(int c=1; c< types.size() ; c++){
						matchedTiersType.clear();
						matchedTiersType.addAll(getLinkedTiers(tier, types.get(c)));
						
						if(types.size() != columnTypeList.size()){
							for(int i= c; i < columnTypeList.size();i++){
								if(columnTypeList.get(i).equals(types.get(c))){
									//int index = columnTypeList.indexOf(columnTypeList.get(i));									
									
									if(linkedTiers.get(i) == null || !matchedTiersType.contains(linkedTiers.get(i))){
										if(matchedTiersType.size() >=1){
											linkedTiers.set(i,matchedTiersType.get(0));
											matchedTiersType.remove(matchedTiersType.get(0));
										} else {
											linkedTiers.set(i,null);
										}
									} else if(matchedTiersType.contains(linkedTiers.get(i))){
										matchedTiersType.remove(linkedTiers.get(i));
									}
								}
							}						
						} else {
							int index = columnTypeList.indexOf(columnTypeList.get(c));
							if(linkedTiers.get(index) == null || !matchedTiersType.contains(linkedTiers.get(index))){
								if(matchedTiersType.size() >=1){
									linkedTiers.set(index,matchedTiersType.get(0));
									matchedTiersType.remove(matchedTiersType.get(0));
								} else {
									linkedTiers.set(index,null);
								}
							} else if(matchedTiersType.contains(linkedTiers.get(index))){
								matchedTiersType.remove(linkedTiers.get(index));
							}
						}
					}
					
					tierMap.put(tier,linkedTiers);
	 			}
	 		}
		}
    }
    
    public boolean isValueChanged(){
    	return valueChanged;
    }
    
    @Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == okButton){
			
			if(selectionTable.isEditing()){
				selectionTable.editingStopped(new ChangeEvent(this));
			}			
			
			String message =ElanLocale.getString("TranscriptionManager.SelectTierDlg.ErrMessageSameTiers");
			HashMap<TierImpl, List<TierImpl>> newTierMap = new HashMap<TierImpl, List<TierImpl>>();
			List<String> newHiddenTiersList = new ArrayList<String>();
			
			for(int i= 0; i < selectionTable.getRowCount(); i++){		
				TierImpl refTier = (TierImpl) transcription.getTierWithId((String)selectionTable.getValueAt(i, 
						selectionTable.getColumnModel().getColumnIndex(TIER_NAME_COLUMN)));					
				List<TierImpl> tierList = new ArrayList<TierImpl>();
				List<String> tierNamesList = new ArrayList<String>();
				for(int j=0; j< selectionTable.getColumnCount(); j++){
					if(j==0){
						Boolean value = (Boolean)selectionTable.getValueAt(i, j);
						String refTierName = selectionTable.getValueAt(i, j+1).toString();
						if(!value){
							if(!newHiddenTiersList.contains(refTierName)){
								newHiddenTiersList.add(refTierName);
							}
							break;
						} else{
							continue;
						}
					}					
					// tier Names column
					if(j==1){
						continue;
					}
					
					String tierName = (String)selectionTable.getValueAt(i, j);
					
					
					if(tierName == null || tierName.equals(defaultValue) || tierName.equals(NO_TIER) || tierName.equals(EMPTY_TIER)){
						tierList.add(null);
						continue;
					}
					
					TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);	
					if(tier != null && tierNamesList.contains(tierName)){	
						Component cell = selectionTable.getDefaultRenderer(Object.class).getTableCellRendererComponent(selectionTable,
								tierName, false, false, i,j);
						cell.setForeground(Color.red);
						
						JOptionPane.showMessageDialog(this, message,
						            ElanLocale.getString("Message.Warning"), JOptionPane.WARNING_MESSAGE);		
						return;
					}
					
					tierNamesList.add(tierName);
					tierList.add(tier);
				}
				newTierMap.put(refTier, tierList);
			}	
			
			valueChanged = !hiddenTiers.equals(newHiddenTiersList);
			
			
			if(!valueChanged){			
					valueChanged = !tierMap.equals(newTierMap);				
			}
			
			if(valueChanged){
				tierMap = newTierMap;
				hiddenTiers = newHiddenTiersList;
			}
			
			doClose();			
		}else if (e.getSource() == cancelButton){
			tierMap = null;	
			hiddenTiers = null;
			valueChanged = false;
			doClose();
		}		
	}
    
    /**
     * Cell Editor for the selection JTable
     * 
     * @author aarsom
     *
     */
    private class TableCellEditor extends DefaultCellEditor implements ActionListener{    	
    	private int startEditInOneClick = 1;
    	private JComboBox comboBox;
    	private String previousValue;    

    	public TableCellEditor() {
    		super(new JComboBox());
    		setClickCountToStart(startEditInOneClick);      		
    	}
    	
    	public TableCellEditor(JCheckBox checkBox){
    		super(checkBox);     		
    	}
    	
    	@Override
		public Component getTableCellEditorComponent(
    			JTable table,
    			Object value,
    			boolean isSelected,
    			int row,
    			int column) { 
    		
    		if(value instanceof Boolean){
    			table.repaint();
    			return super.getTableCellEditorComponent(table, value, isSelected, row, column);    			
    		}
    		
    		if(value != null){
    			previousValue= value.toString();
    		} else {
    			previousValue = null;
    		}
    		
    		String refTier = (String) table.getValueAt(row,table.getColumnModel().getColumnIndex(TIER_NAME_COLUMN));
    		Map<String, List<String>> map = typeTierMap.get(refTier);
    		String typeName = columnTypeList.get(column-2);
    		List<String> linkedTiers = map.get(typeName);
    		
    		if(linkedTiers == null ){//|| linkedTiers.size() <= 1){
    			return null;
    		}   		
    		
    		comboBox = new JComboBox(); 
    		for(int i=0; i< linkedTiers.size(); i++){					
				comboBox.addItem(linkedTiers.get(i));
    		}
    		comboBox.setSelectedIndex(-1);
    		comboBox.addActionListener(this);
    		
    		return comboBox;
    	}
    	
    	
    	@Override
		public Object getCellEditorValue() { 
    		Object value = super.getCellEditorValue();
    		if (value instanceof Boolean){
    			return value;
    		} 
    		value =  comboBox.getSelectedItem();
    	    if(value == null){
    	    	return EMPTY_TIER;
    	    }    		
    		return value;
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == comboBox ){
				if (comboBox .getSelectedItem() != null){	
					int row = selectionTable.getSelectedRow();	
					String selectedTierName = comboBox .getSelectedItem().toString();
					
					if(previousValue != null && previousValue.equals(selectedTierName)){
						selectionTable.editingStopped(new ChangeEvent(this));						
						return;
					}
					
					for (int i=2; i< selectionTable.getColumnCount(); i++){
						String tierName = (String) selectionTable.getValueAt(row,i);
						if(selectedTierName.equals(tierName)){
							if(comboBox.getItemCount() ==1){
								selectionTable.setValueAt(EMPTY_TIER,row,i);
							} else if( comboBox.getItemCount() > 1){								
								List<String> usedTiersList = new ArrayList<String>();
								for(int x = 0; x < comboBox.getItemCount(); x++){
									String comboBoxValue = comboBox.getItemAt(x).toString();
									for(int c = 2; c < selectionTable.getColumnCount(); c++){
										tierName = (String) selectionTable.getValueAt(row,c);
										if(comboBoxValue.equals(tierName)){
											if(!usedTiersList.contains(comboBoxValue)){
												usedTiersList.add(comboBoxValue);
											}											
											break;
										}
									}
								}
								if(usedTiersList.size() < comboBox.getItemCount()){
									selectionTable.setValueAt(defaultValue,row,i);
								} else {
									selectionTable.setValueAt(EMPTY_TIER,row,i);
								}								
							}
						}
					}
					//okButton.setEnabled(true);	
					selectionTable.editingStopped(new ChangeEvent(this));
				} 
			} 
		}
    }
    

	public Map<TierImpl, List<TierImpl>> getTierMap(){		
		return tierMap;
	}	
	
	public List<String> getHiddenTiers(){
		return hiddenTiers;
	}
	
	public List<String> getColumnTypes(){
		return columnTypeList;
	}
}
