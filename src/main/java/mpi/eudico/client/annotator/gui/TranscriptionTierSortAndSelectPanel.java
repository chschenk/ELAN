package mpi.eudico.client.annotator.gui;


import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;

@SuppressWarnings("serial")
public class TranscriptionTierSortAndSelectPanel extends AbstractTierSortAndSelectPanel 
	implements ActionListener, ChangeListener, ItemListener, TableModelListener {
  protected List<String> allTypeNames;
  protected List<String> allPartNames;
  protected List<String> allAnnNames;
  protected List<String> allLangNames;
	
  	private TranscriptionImpl transcription;
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers
	 * 
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(TranscriptionImpl transcription) {
		this(transcription, null, null, true, true);
	}
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers, but with the option to specify the tier mode
	 * 
	 * @param transcription
	 * @param tierMode the tier mode for the panel one of the Modes enum
	 */
	public TranscriptionTierSortAndSelectPanel(TranscriptionImpl transcription, Modes tierMode) {
		this(transcription, null, null, true, true, tierMode);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers) {
		this(transcription, tierOrder, selectedTiers, true, true);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering) {
		this(transcription, tierOrder, selectedTiers, allowReordering, true);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
		this(transcription, tierOrder, selectedTiers, allowReordering, allowSorting, Modes.ALL_TIERS);
	}
	
	/**
	 * @param transcription
	 */
	public TranscriptionTierSortAndSelectPanel(TranscriptionImpl transcription, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting,
			Modes tierMode) {
		super();
		this.transcription = transcription;
		allTierNames = tierOrder;
		if (selectedTiers  != null) {
			selectedTierNames = selectedTiers;
			initialSelectedTiersProvided = true;
		} else {
			selectedTierNames = new ArrayList<String>();
			initialSelectedTiersProvided = false;
		}
		this.allowReordering = allowReordering;
		this.allowSorting = allowSorting;
		mode = tierMode;
		tabIndices = new HashMap<Integer, String>(4);
		tabIndices.put(TIER_INDEX, BY_TIER);
		tabIndices.put(TYPE_INDEX, BY_TYPE);
		tabIndices.put(PART_INDEX, BY_PART);
		tabIndices.put(ANN_INDEX, BY_ANN);
		tabIndices.put(LANG_INDEX, BY_LANG);
		initComponents();
	}
	
	/**
	 * Populates the tables with data from the transcription
	 */
	@Override
	protected void initTables() {
        allTypeNames = new ArrayList<String>();          
    	allPartNames = new ArrayList<String>();          
    	allAnnNames = new ArrayList<String>();  
    	allLangNames = new ArrayList<String>();  
        
		if (allTierNames == null) {
			allTierNames = new ArrayList<String>();
			List<TierImpl> tiers = transcription.getTiers();
			
			for (int i = 0; i < tiers.size(); i++) {
				TierImpl tier = tiers.get(i);
				switch (mode) {
				case ALIGNABLE_TIERS:
					if (tier.isTimeAlignable()) {
						allTierNames.add(tier.getName());
					}
					break;
				case ROOT_TIERS:
					if (tier.getParentTier() == null) {
						allTierNames.add(tier.getName());
					}
					break;
				case ROOT_W_INCLUDED:
					if (tier.getParentTier() == null || 
							(tier.getLinguisticType().getConstraints() != null && 
									tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN)) {
						allTierNames.add(tier.getName());
					}
					break;
				default:
					allTierNames.add(tier.getName());	
				}				
			}
		} else {
			List<TierImpl> tiers = transcription.getTiers();
			
			for (int i = 0; i < tiers.size(); i++) {
				TierImpl tier = tiers.get(i);
				switch (mode) {
				case ALIGNABLE_TIERS:
					addOrRemoveTierName(tier.isTimeAlignable(), tier.getName());
					break;
				case ROOT_TIERS:
					addOrRemoveTierName(tier.getParentTier() == null, tier.getName());
					break;
				case ROOT_W_INCLUDED:
					addOrRemoveTierName(tier.getParentTier() == null || 
									(tier.getLinguisticType().getConstraints() != null && 
										tier.getLinguisticType().getConstraints().getStereoType() == Constraint.INCLUDED_IN), 
								tier.getName());
					break;
				default:
					addOrRemoveTierName(true, tier.getName());
				}				
			}
		}
        // this only adds the types, participants and annotators of (selected) tiers
        if (allTierNames != null) {
            for (String name : allTierNames) {
                if (initialSelectedTiersProvided) {
                    model.addRow(selectedTierNames.contains(name), name);
                } else {
                    model.addRow(Boolean.TRUE, name); // default is selected
                    selectedTierNames.add(name);
                }              
                
                TierImpl tier = transcription.getTierWithId(name);  

                addIfNotYetIncluded(tier.getParticipant(), allPartNames, partModel);
                addIfNotYetIncluded(tier.getAnnotator(),   allAnnNames,  annotModel);
                addIfNotYetIncluded(tier.getLinguisticType().getLinguisticTypeName(), allTypeNames, typeModel);
                addIfNotYetIncluded(tier.getLangRef(),     allLangNames, langModel);               
            }
            
            if (model.getRowCount() == 1) {
            	model.setValueAt(Boolean.TRUE, 0, 0);
            	String name = (String) model.getValueAt(0, model.findColumn(TIER_NAME_COLUMN));
            	
            	if (!selectedTierNames.contains(name)) {
            		selectedTierNames.add(name);
            	}
            }
        }
	}

	/**
	 * Makes sure the name is either included in or excluded from allTierNames.
	 * 
	 * @param include whether the name should be included
	 * @param name
	 */
	private void addOrRemoveTierName(boolean include, String name) {
		if (include) {
			if (!allTierNames.contains(name)) {
				allTierNames.add(name);
			}
		} else {
			allTierNames.remove(name);
		}
	}

	/**
	 * Adds the given name to both the list of names and the model
	 * if the name wasn't in the list already.
	 */
	private void addIfNotYetIncluded(String name, List<String> allNames, TierExportTableModel model) {
		if (name == null || name.isEmpty()) {
			name = NOT_SPECIFIED;
		}
        
		if (!allNames.contains(name)){
			allNames.add(name);
			model.addRow(Boolean.FALSE, name);
		}	
	}

	/**
	 * Switches dynamically between a view where only root tier are shown
	 * and the list of tiers for the mode of the panel.
	 *  
	 * @param rootsOnly
	 */
	@Override
	protected void toggleRootsOnly(boolean rootsOnly) {
		if (rootsOnly) {// from all tiers for mode to root only
			// store current lists and selections for tiers and types
		   	int includeCol = model.findColumn(SELECT_COLUMN);
		    int nameCol = model.findColumn(TIER_NAME_COLUMN);
		    unfilteredTiers = new LinkedHashMap<String, Boolean>(model.getRowCount());
			for (int i = 0; i < model.getRowCount(); i++) {
		    	unfilteredTiers.put((String) model.getValueAt(i, nameCol), 
		    			(Boolean) model.getValueAt(i, includeCol));
		    }
		    model.removeTableModelListener(this);
		    String name;
		    TierImpl t;
		    for (int i = model.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) model.getValueAt(i, nameCol);
		    	t = transcription.getTierWithId(name);
		    	if (t != null && t.hasParentTier()) {
		    		model.removeRow(i);
		    	}
		    }
		    
		    model.addTableModelListener(this);
		    pendingChanges = true;
		    
		    unfilteredTypes = new LinkedHashMap<String, Boolean>(typeModel.getRowCount());
		    includeCol = typeModel.findColumn(SELECT_COLUMN);
		    nameCol = typeModel.findColumn(TIER_NAME_COLUMN);
			for (int i = 0; i < typeModel.getRowCount(); i++) {
				unfilteredTypes.put((String) typeModel.getValueAt(i, nameCol), 
		    			(Boolean) typeModel.getValueAt(i, includeCol));
		    }
			
			LinguisticType type;
			List<LinguisticType> allTypes = transcription.getLinguisticTypes();
		    for (int i = typeModel.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) typeModel.getValueAt(i, nameCol);
		    	
		    	for (int j = 0; j < allTypes.size(); j++) {
		    		type = allTypes.get(j);
		    		if (type.getLinguisticTypeName().equals(name)) {
		    			if (type.getConstraints() != null) {
		    				typeModel.removeRow(i);
		    			}
		    			break;
		    		}
		    	}
		    }
		} else {// from root only to all tiers for the mode. Try to maintain changes in the current order
			if (unfilteredTiers == null) {
				return;
			}
			model.removeTableModelListener(this);
		   	int includeCol = model.findColumn(SELECT_COLUMN);
		    int nameCol = model.findColumn(TIER_NAME_COLUMN);
			// check if the unfiltered lists are there
			LinkedHashMap<String, Boolean> filteredTiers = new LinkedHashMap<String, Boolean>(model.getRowCount());
			for (int i = 0; i < model.getRowCount(); i++) {
				filteredTiers.put((String) model.getValueAt(i, nameCol), 
		    			(Boolean) model.getValueAt(i, includeCol));
		    }

			int insertAfter = -1;
			Iterator<String> keyIter = unfilteredTiers.keySet().iterator();
			String key;
			String name;
			while (keyIter.hasNext()) {
				key = keyIter.next();
				boolean shouldInsert = !filteredTiers.containsKey(key);
				if (shouldInsert) {
					if (insertAfter == -1) {
						model.insertRow(0, new Object[]{unfilteredTiers.get(key), key});
						insertAfter = 0;
					} else if (insertAfter >= model.getRowCount() - 1) {// add to end
						model.addRow(new Object[]{unfilteredTiers.get(key), key});
						insertAfter = model.getRowCount() - 1;
					} else {
						model.insertRow(insertAfter + 1, new Object[]{unfilteredTiers.get(key), key});
						insertAfter++;
					}					
				} else {// find index in current, filtered list
					for (int i = 0; i < model.getRowCount(); i++) {
						name = (String) model.getValueAt(i, nameCol);
						if (name.equals(key)) {
							insertAfter = i;
							break;
						}
				    }
				}				
			}
			
			model.addTableModelListener(this);
			pendingChanges = true;
			// do same for types
			LinkedHashMap<String, Boolean> filteredTypes = new LinkedHashMap<String, Boolean>(typeModel.getRowCount());
			for (int i = 0; i < typeModel.getRowCount(); i++) {
				filteredTypes.put((String) typeModel.getValueAt(i, nameCol), 
		    			(Boolean) typeModel.getValueAt(i, includeCol));
		    }
			
			insertAfter = -1;
			keyIter = unfilteredTypes.keySet().iterator();
			key = null;
			name = null;
			
			while (keyIter.hasNext()) {
				key = keyIter.next();
				boolean shouldInsert = !filteredTypes.containsKey(key);
				if (shouldInsert) {
					if (insertAfter == -1) {
						typeModel.insertRow(0, new Object[]{unfilteredTypes.get(key), key});
						insertAfter = 0;
					} else if (insertAfter >= typeModel.getRowCount() - 1) {// add to end
						typeModel.addRow(new Object[]{unfilteredTypes.get(key), key});
						insertAfter = typeModel.getRowCount() - 1;
					} else {
						typeModel.insertRow(insertAfter + 1, new Object[]{unfilteredTypes.get(key), key});
						insertAfter++;
					}					
				} else {// find index in current, filtered list
					for (int i = 0; i < typeModel.getRowCount(); i++) {
						name = (String) typeModel.getValueAt(i, nameCol);
						if (name.equals(key)) {
							insertAfter = i;
							break;
						}
				    }
				}				
			}
		}
	}
    
	 /**
     * 
     */
    @Override
	protected void showTiersTab(){     	
    	int includeCol = model.findColumn(SELECT_COLUMN);
        int nameCol = model.findColumn(TIER_NAME_COLUMN);
        model.removeTableModelListener(this);
        
    	for (int i = 0; i < model.getRowCount(); i++) {
    		Object value = model.getValueAt(i, nameCol);
    		if(selectedTierNames.contains(value.toString())){
    			model.setValueAt(Boolean.TRUE, i, includeCol);
    		} else {
    			model.setValueAt(Boolean.FALSE, i, includeCol);
    		}
    	}

    	model.addTableModelListener(this);
    }
    
    
    
    /**
     * Update the types tab changes
     */
    @Override
	protected void updateLinguisticTypes(){
    	updateTierField(typeModel, new TierImpl.LinguisticTypeNameGetter());
    }
    
    /**
     * Update the participants tab changes
     */
    @Override
	protected void updateParticipants(){
    	updateTierField(partModel, new TierImpl.ParticipantGetter());
    }
    
    /**
     * Update the annotator tab changes
     */
    @Override
	protected void updateAnnotators(){
    	updateTierField(annotModel, new TierImpl.AnnotatorGetter());
    }
    
    /**
     * Update the languages tab changes
     */
    @Override
	protected void updateLanguages(){
    	updateTierField(langModel, new TierImpl.LanguageGetter());
    }
    
	protected void updateTierField(DefaultTableModel model, TierImpl.ValueGetter getter) {
    	if (pendingChanges) {
    		selectedTierNames.clear();
        	// update based on table model
    	   	int includeCol = model.findColumn(SELECT_COLUMN);
    	    int nameCol = model.findColumn(TIER_NAME_COLUMN);
    	    String annotator;
    	    Object include;
    	    
    		for (int i = 0; i < model.getRowCount(); i++) {
    			include = model.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				annotator = (String) model.getValueAt(i, nameCol);
    				
    				for (String s : allTierNames) {
    					TierImpl tier = transcription.getTierWithId(s);
    	             	String annotName = getter.getSortValue(tier);           	
    	             	if ( (annotName == null || annotName.isEmpty()) && annotator == NOT_SPECIFIED){
    	             		selectedTierNames.add(tier.getName());
    	             	} else if (annotator.equals(annotName)) {
    	             		selectedTierNames.add(tier.getName());
    	             	}
    				}
    			}
    		}
    		
    		hiddenTiers.clear();
    		for (String s : allTierNames) {
    			if (!selectedTierNames.contains(s)) {
    				hiddenTiers.add(s);
    			}
    		}
    	}	
    }
}
