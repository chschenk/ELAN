package mpi.eudico.client.annotator.gui;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.tier.TierExportTableModel;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.server.corpora.clomimpl.abstr.ParseException;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.dobes.EAFSkeletonParser;

@SuppressWarnings("serial")
public class FilesTierSortAndSelectPanel extends AbstractTierSortAndSelectPanel implements ActionListener,
ChangeListener, ItemListener, TableModelListener {
	protected List<File> files;
	
	/**
	 * For each type name, has a List of in which Tiers it occurs.
	 */
	protected Map<String,List<String>> tierTypeMap;
	/**
	 * For each participant, has a List of in which Tiers it occurs.
	 */
	protected Map<String,List<String>> tierParticipantMap;
	/**
	 * For each annotator, has a List of in which Tiers it occurs.
	 */
	protected Map<String,List<String>> tierAnnotatorMap;
	/**
	 * For each language, has a List of in which Tiers it occurs.
	 */
	protected Map<String,List<String>> tierLanguageMap;
	
	protected List<String> rootTiers;
	protected List<String> rootTypes;
	
	/**
	 * Constructor for initializing the panel without a specific tier order and/or 
	 * list of selected tiers
	 * 
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(List<File> files) {
		this(files, null, null, true, true);
	}
	
	/**
	 * Constructor for initializing the panel without a specific tierorder and/or 
	 * list of selected tiers, but with the option to specify the tier mode
	 * 
	 * @param transcription
	 * @param tierMode the tier mode for the panel one of the Modes enum
	 */
	public FilesTierSortAndSelectPanel(List<File> files, Modes tierMode) {
		this(files, null, null, true, true, tierMode);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(ArrayList<File> files, List<String> tierOrder,
			List<String> selectedTiers) {
		this(files, tierOrder, selectedTiers, true, true);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(List<File> files, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering) {
		this(files, tierOrder, selectedTiers, allowReordering, true);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(List<File> files, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting) {
		this(files, tierOrder, selectedTiers, allowReordering, allowSorting, Modes.ALL_TIERS);
	}
	
	/**
	 * @param transcription
	 */
	public FilesTierSortAndSelectPanel(List<File> files, List<String> tierOrder,
			List<String> selectedTiers, boolean allowReordering, boolean allowSorting,
			Modes tierMode) {
		super();
		this.files = files;
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
		
        tierTypeMap = new LinkedHashMap<String, List<String>>();
    	tierParticipantMap = new LinkedHashMap<String, List<String>>();
    	tierAnnotatorMap = new LinkedHashMap<String, List<String>>();
    	tierLanguageMap = new LinkedHashMap<String, List<String>>();
    	rootTiers = new ArrayList<String>();
    	rootTypes = new ArrayList<String>();
    	
		initComponents();
	}
	
	
	
	/**
	 * Populates the tables with data from the transcription
	 */
	@Override
	protected void initTables() {
        if (allTierNames == null) {
        	allTierNames = new ArrayList<String>();
        }
        //allTierNames.clear();  
    	tierTypeMap.clear();
    	tierParticipantMap.clear();
    	tierAnnotatorMap.clear();
    	tierLanguageMap.clear();
		
        for (int i = 0; i < files.size(); i++) {
        	File file = files.get(i);
        	if (file == null) {
        		continue;
        	}
        	String path = file.getAbsolutePath();

            try {
            	EAFSkeletonParser parser = new EAFSkeletonParser(path);
                parser.parse();
                List<TierImpl> pts = parser.getTiers();

                for (TierImpl tier : pts) {
                	String tierName = tier.getName();

                    if (!allTierNames.contains(tierName)) {
                    	allTierNames.add(tierName);
                    }
                	String typeName = tier.getLinguisticType().getLinguisticTypeName();

                	// store the root tiers separately
                    if (tier.getParentTier() == null) {
                    	if (!rootTiers.contains(tierName)) {
                    		rootTiers.add(tierName);
                    	}
                		// store the types for root tiers separately
            			if (!rootTypes.contains(typeName)) {
            				rootTypes.add(typeName);
            			}
                    }
                    
                    storeInMap(tier.getParticipant(), tierName, tierParticipantMap);
                    storeInMap(tier.getAnnotator(),   tierName, tierAnnotatorMap);
                    storeInMap(typeName,              tierName, tierTypeMap);
                    storeInMap(tier.getLangRef(),     tierName, tierLanguageMap);
                }
            } catch (ParseException pe) {
                ClientLogger.LOG.warning(pe.getMessage());
                    //pe.printStackTrace();
            } catch (Exception ex) {
                ClientLogger.LOG.warning("Could not load file: " + path);
            }
        }
        
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
	    	model.removeRow(i);
	    }

	    for (int i = 0; i < allTierNames.size(); i++) {
	    	if(!selectedTierNames.isEmpty()) {
	    		if(selectedTierNames.contains(allTierNames.get(i))){
	    			model.addRow(Boolean.TRUE, allTierNames.get(i));
	    		} else {
	    			model.addRow(Boolean.FALSE, allTierNames.get(i));
	    		}
	    	} else if(i ==0){
	    		model.addRow(Boolean.TRUE, allTierNames.get(i));
	    		selectedTierNames.add(allTierNames.get(i));
	    	} else {
	    		model.addRow(Boolean.FALSE, allTierNames.get(i));
	    	}
	    }
	    
	    if (model.getRowCount() > 0) {
	    	tierTable.setRowSelectionInterval(0, 0);
	    }
	    
	    fillModel(typeModel, tierTypeMap.keySet());
	    fillModel(partModel, tierParticipantMap.keySet());
	    fillModel(annotModel, tierAnnotatorMap.keySet());
	    fillModel(langModel, tierLanguageMap.keySet());
	}

	/**
	 * The map maps from each value (participant, type name, annotator, language)
	 * to a List of tier names in which that value occurs.
	 * 
	 * @param value
	 * @param tierName
	 * @param tierFeatureMap
	 */
	private void storeInMap(String value, String tierName, Map<String, List<String>> tierFeatureMap) {
		if (value == null || value.isEmpty()) {
			value = NOT_SPECIFIED;
		}
        
		if (tierFeatureMap.get(value) == null) {
			tierFeatureMap.put(value, new ArrayList<String>());
		}
		
		List<String> list = tierFeatureMap.get(value);
		if (!list.contains(tierName)) {
			list.add(tierName);
		}

	}
	
	protected void fillModel(TierExportTableModel model, Collection<String> keys) {
		for (String key : keys) {
	    	model.addRow(Boolean.FALSE, key);
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
		    for (int i = model.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) model.getValueAt(i, nameCol);

		    	if (!rootTiers.contains(name)) {
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
			
		    for (int i = typeModel.getRowCount() - 1; i >= 0; i--) {
		    	name = (String) typeModel.getValueAt(i, nameCol);
		    	
		    	if (!rootTypes.contains(name)) {
		    		typeModel.removeRow(i);
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
						model.addRow(unfilteredTiers.get(key), key);
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
						typeModel.addRow(unfilteredTypes.get(key), key);
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
    	updateTierField(typeModel, new TierImpl.LinguisticTypeNameGetter(), tierTypeMap);
    }
    
    /**
     * Update the participants tab changes
     */
	@Override
    protected void updateParticipants(){
    	updateTierField(partModel, new TierImpl.ParticipantGetter(), tierParticipantMap);
    }
    
    /**
     * Update the annotator tab changes
     */
	@Override
    protected void updateAnnotators(){
    	updateTierField(annotModel, new TierImpl.AnnotatorGetter(), tierAnnotatorMap);
    }
    /**
     * Update the language tab changes
     */

	@Override
	protected void updateLanguages() {
		updateTierField(langModel, new TierImpl.LanguageGetter(), tierLanguageMap);
	}

	protected void updateTierField(DefaultTableModel model, TierImpl.ValueGetter getter, Map<String,List<String>> tierFieldMap) {
    	if (pendingChanges) {
    		selectedTierNames.clear();
        	// update based on table model
    	   	int includeCol = model.findColumn(SELECT_COLUMN);
    	    int nameCol = model.findColumn(TIER_NAME_COLUMN);
    	    
    		for (int i = 0; i < model.getRowCount(); i++) {
    			Object include = model.getValueAt(i, includeCol);
    			if ((Boolean) include) {
    				String fieldValue = (String) model.getValueAt(i, nameCol);
    				
    				List<String> tierList = tierFieldMap.get(fieldValue);
    				for (String s : tierList) {
    					if (!selectedTierNames.contains(s)) {
    						selectedTierNames.add(s);
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
