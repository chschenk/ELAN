package mpi.eudico.client.annotator.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListCellRenderer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.PreferencesListener;
import mpi.eudico.client.annotator.TierOrderListener;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.grid.GridViewer;
import mpi.eudico.client.annotator.tiersets.TierSetUtil;
import mpi.eudico.client.annotator.util.ClientLogger;
import mpi.eudico.client.annotator.util.TierMenuStringFormatter;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;


/**
 * A panel that consist mainly of a ComboBox containing the names of tiers.
 * When the user selects a tier the connected
 * <code>SingleTierViewerPanel</code> receives a notification by the
 * ViewerManager.
 * @version Dec 2011 Added (and modified) code by Coralie Villes allowing to 
 * select a tier with symbolic subdivision tiers in the table
 */
@SuppressWarnings("serial")
public class SingleTierViewerPanel extends JPanel implements ACMEditListener,
    ElanLocaleListener, ItemListener, TierOrderListener, PreferencesListener, MouseListener, ActionListener {
    /** This means no tier has been selected! */
    private String EMPTY_ITEM = ElanLocale.getString("SingleTierViewerPanel.ComboBoxDefaultString");
    private String none_item = ElanLocale.getString("SingleTierViewerPanel.ComboBoxSelectNone");
    private ViewerManager2 viewerManager;
    private SingleTierViewer viewer;
    private JComboBox tierComboBox;
    private Map<String, Tier> tierTable;
    private Tier currentTier;
    private List<String> tierOrder;
    private JButton optionIcon;
        
    private JPopupMenu popupMenu;
	private JRadioButtonMenuItem normalMI;
	private JRadioButtonMenuItem multiAssMI;
	private JRadioButtonMenuItem multiSubMI;
	private int gridMode;
    
	private boolean workWithTierSet = false;
	
	private Boolean alphabeticTierOrder = false;

    /**
     * Creates a new SingleTierViewerPanel instance
     *
     * @param viewerManager the ViewerManager
     */
    public SingleTierViewerPanel(ViewerManager2 viewerManager) {
        this.viewerManager = viewerManager;
        tierComboBox = new JComboBox();
        tierComboBox.setMaximumRowCount(Constants.COMBOBOX_VISIBLE_ROWS);
        optionIcon = new JButton();
        
        tierTable = new HashMap<String, Tier>();
        
        processAlphabeticTierOrderPreference();
        
        fillComboBox();
        tierComboBox.addItemListener(this);
        setLayout(new BorderLayout());
        tierComboBox.setRenderer(new EmptyRenderer(tierComboBox.getRenderer()));
        /**
         * when this panel gets hidden, then exit from edit 
         * mode if editing
         */
        this.addComponentListener(new ComponentAdapter(){
        	@Override
			public void componentHidden(ComponentEvent e) {
        		if(viewer instanceof AbstractViewer){
        			((AbstractViewer)viewer).isClosing();
        		}
        	}
        });
        Preferences.addPreferencesListener(viewerManager.getTranscription(), this);
    }
    
    /**
     * Connects a <code>SingleTierViewer</code> to this panel.
     *
     * @param viewer the connected viewer
     */
    public void setViewer(SingleTierViewer viewer) { // take care of removing an existing viewer?
        this.viewer = viewer;
        if (currentTier != null) {
        	 viewerManager.setTierForViewer(viewer, currentTier);
        }     

        if (viewer instanceof GridViewer) {
            Icon icon;
            try {
            	icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/triangle_down.gif"));
            	optionIcon.setIcon(icon);
            } catch (Exception ex) {
            	optionIcon.setText("Options");
            }
            //optionIcon.setPreferredSize(new Dimension (24, tierComboBox.getPreferredSize().height));
            
            optionIcon.addMouseListener(this);
            optionIcon.setToolTipText(ElanLocale.getString("SingleTierViewerPanel.Label.ToolTip"));

            JPanel jp = new JPanel();
            jp.setLayout(new BorderLayout());
            //tick the multiple tier association type checkbox or the multiple tier subdivision type checkbox
            //JPanel checkboxPanel=new JPanel();
            //checkboxPanel.add(optionIcon);
            //checkboxPanel.add(multiCheckBoxAssociationType);
    		//checkboxPanel.add(multiCheckBoxSubdivisionType);
    		//jp.add(checkboxPanel, BorderLayout.WEST);
    		jp.add(optionIcon, BorderLayout.WEST);
            jp.add(tierComboBox, BorderLayout.CENTER);
            add(jp, BorderLayout.NORTH);
            gridMode = ((GridViewer) viewer).getMode();
        } else {
            add(tierComboBox, BorderLayout.NORTH);
        }

        add((AbstractViewer) viewer, BorderLayout.CENTER);
    }

    /**
     * Getter for the viewer on this panel.
     *
     * @return the viewer
     *
     * @since oct 04 HS: for storage of preferences
     */
    public SingleTierViewer getViewer() {
        return viewer;
    }

    private void fillComboBox() {
    	tierComboBox.removeAllItems();
        tierTable.clear();
        
        // add the empty tier
        tierComboBox.addItem(EMPTY_ITEM);
        if(tierOrder != null){ 
        	for (int i = 0; i < tierOrder.size(); i++) {
        		String tierName = tierOrder.get(i);
        		TierImpl tier = ((TranscriptionImpl)viewerManager.getTranscription())
        						.getTierWithId(tierName);
        		
        		if(tier != null){
        			String label = TierMenuStringFormatter.GetFormattedString(tier);
        			tierComboBox.addItem(label);
        			
        			tierTable.put(label, tier);
        		}
			}
		} else {
			try {
	            List<TierImpl> tiers = ((TranscriptionImpl)viewerManager.getTranscription())
	            						.getTiers();
	            int tiers_size = tiers.size();
	
	            for (int i = 0; i < tiers_size; i++) {
	                TierImpl tier = tiers.get(i);
	                
	                //retrieve tier name and create the combobox label for it
	                String label = TierMenuStringFormatter.GetFormattedString(tier);
	               	                
	                tierComboBox.addItem(label);
	                tierTable.put(label, tier);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
    }

    /**
     * Responds only to tier related operation.
     *
     * @param e the ACMEditEvent
     */
    @Override
	public void ACMEdited(ACMEditEvent e) {
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:
        // hier... for annotations changes the combobox does not need to be updated 
        //unless the count annotations option is on
        case ACMEditEvent.ADD_ANNOTATION_HERE:
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
        case ACMEditEvent.ADD_ANNOTATION_AFTER:
        case ACMEditEvent.CHANGE_ANNOTATIONS:
        case ACMEditEvent.REMOVE_ANNOTATION:

        // fallthrough		
        case ACMEditEvent.REMOVE_TIER:
            updateComboBox();

            break;

        case ACMEditEvent.CHANGE_TIER:

            if (e.getInvalidatedObject() instanceof TierImpl) {
                tierChanged((TierImpl) e.getInvalidatedObject());
            }

            break;

        default:
            return;
        }
    }

    /**
     * Action following a CHANGE_TIER ACMEditEvent. Update the combo box if
     * a tier name has been changed.
     *
     * @param tier the invalidated tier
     */
    private void tierChanged(TierImpl tier) {
    	// if the current tier's name has changed, update the combo box
        if (tier == currentTier) {
        	String currentLabel = (String) tierComboBox.getSelectedItem();
            String newLabel = TierMenuStringFormatter.GetFormattedString(tier);

            if ( !newLabel.equals(currentLabel) ) {
                tierTable.remove(currentLabel);
                tierTable.put(newLabel, currentTier);

                for (int i = 0; i < tierComboBox.getItemCount(); i++) {
                    if (tierComboBox.getItemAt(i).equals(currentLabel)) {
                        tierComboBox.removeItemAt(i);
                        tierComboBox.insertItemAt(newLabel, i);
                        tierComboBox.setSelectedItem(newLabel);

                        break;
                    }
                }
            }
        } else {
        	String newLabel = TierMenuStringFormatter.GetFormattedString(tier);

            if ( !tierTable.containsKey(newLabel) ) {
                Iterator<String> nameIt = tierTable.keySet().iterator();

                while (nameIt.hasNext()) {
                	String name = nameIt.next();

                    if (tierTable.get(name) == tier) {
                        tierTable.remove(name);
                        tierTable.put(newLabel, tier);

                        for (int i = 0; i < tierComboBox.getItemCount(); i++) {
                            if (tierComboBox.getItemAt(i).equals(name)) {
                                tierComboBox.removeItemAt(i);
                                tierComboBox.insertItemAt(newLabel, i);

                                break;
                            }
                        }

                        break;
                    }
                }
            }
        }

        if (viewer instanceof GridViewer) {
	        if (gridMode == GridViewer.MULTI_TIER_ASSOCIATION_MODE || gridMode == GridViewer.MULTI_TIER_SUBDIVISION_MODE) {
	            // the tier hierarchy might have been changed, only relevant in 
	            // multi tier mode
	            updateComboBox();
	        }
        }
    }

    private void updateComboBox() {
        try {
            Tier selTier = currentTier;

            switch (gridMode) {
            case GridViewer.SINGLE_TIER_MODE:
            	fillComboBox();
            	break;
            case GridViewer.MULTI_TIER_ASSOCIATION_MODE:
            	fillComboBoxMulti(Constraint.SYMBOLIC_ASSOCIATION);
                break;
            case GridViewer.MULTI_TIER_SUBDIVISION_MODE:
            	fillComboBoxMulti(Constraint.SYMBOLIC_SUBDIVISION);
            	break;
            	default:
            }
            
            if (selTier != null) {
            	selectTier(selTier);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * ItemListener method. Handles selection changes in the tier combobox and
     * the multi tier checkbox, if present.
     *
     * @param e the item event
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        Object objSource = e.getSource();

        if (objSource == tierComboBox) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedTierName = (String) tierComboBox.getSelectedItem();
                Tier tier = null;

                if (!selectedTierName.equals(EMPTY_ITEM)) {
                    tier = tierTable.get(selectedTierName);
                }

                viewerManager.setTierForViewer(viewer, tier);
                currentTier = tier;
            }
        } 
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == normalMI) {
			if (normalMI.isSelected()) {
				gridMode = GridViewer.SINGLE_TIER_MODE;
				if (viewer instanceof GridViewer) {
					((GridViewer) viewer).setMode(GridViewer.SINGLE_TIER_MODE);
				}
				updateComboBox();
			}
		} else if (e.getSource() == multiAssMI) {
			if (multiAssMI.isSelected()) {
				gridMode = GridViewer.MULTI_TIER_ASSOCIATION_MODE;
				if (viewer instanceof GridViewer) {
					((GridViewer) viewer).setMode(GridViewer.MULTI_TIER_ASSOCIATION_MODE);
				}
				updateComboBox();
			}
		} else if (e.getSource() == multiSubMI) {
			if (multiSubMI.isSelected()) {
				gridMode = GridViewer.MULTI_TIER_SUBDIVISION_MODE;
				if (viewer instanceof GridViewer) {
					((GridViewer) viewer).setMode(GridViewer.MULTI_TIER_SUBDIVISION_MODE);
				}
				updateComboBox();
			}
		}
	}

    /**
     * Method to show the tiers which respect the constraint (symbolic_association or subdivision) mod. Coralie Villes
     * @param constraint the constraint applied to the types
     */
    private void fillComboBoxMulti(int constraint) {
        tierComboBox.removeAllItems();
        tierTable.clear();
        tierComboBox.addItem(EMPTY_ITEM);

        if(tierOrder != null){
        	for (int i = 0; i <tierOrder.size(); i++) {
        		String tierName = tierOrder.get(i);
        		TierImpl tier = ((TranscriptionImpl)viewerManager.getTranscription())
        						.getTierWithId(tierName);
        		//if(!tier.hasParentTier()){
        			List<TierImpl> dependentTiers = tier.getDependentTiers();
        			if (dependentTiers != null) {
        				for(int d=0; d < dependentTiers.size();d++){
        					TierImpl depeTier = dependentTiers.get(d);
        					if (depeTier.getLinguisticType().getConstraints().getStereoType() == constraint) {
        						addToCombo(tier);
        						break;
        					}
        				}
        			}
        		//}
        	}
        } else {
	        try {
	            List<TierImpl> tiers = ((TranscriptionImpl)viewerManager.getTranscription())
						.getTiers();
	            int tiers_size = tiers.size();
	
	            for (int i = 0; i < tiers_size; i++) {
	                TierImpl tier = tiers.get(i);
	                
	                LinguisticType lt = tier.getLinguisticType();
	                Constraint c = lt.getConstraints();
	
	                if (c != null) {
	                    if (c.getStereoType() == constraint) {
	                        Tier tierParent = tier.getParentTier();
	                        addToCombo(tierParent);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
    }

    private void addToCombo(Tier tierParent) {
    	String label = TierMenuStringFormatter.GetFormattedString((TierImpl) tierParent);
        //only add if parent not yet in combobox
        String str = null;
        int combo_size = tierComboBox.getItemCount();

        for (int i = 0; i < combo_size; i++) {
            str = (String) tierComboBox.getItemAt(i);

            if (str != null) {
                if (label.equals(str)) {
                    return;
                }
            }
        }

        tierComboBox.addItem(label);
        tierTable.put(label, tierParent);
    }

    /**
     * Selects the tier with the specified name in the combobox and  updates
     * the viewer. Multimode is only relevant for the GridViewer.
     *
     * @param tierName the name of the tier
     *
     * @since oct 04 HS: for restoring of preferences
     */
    public void selectTier(String tierName) {
        if (tierName != null) {
        	if (tierTable.containsKey(tierName)) {
        		tierComboBox.setSelectedItem(tierName);
        	} else {
        		Tier t = viewerManager.getTranscription().getTierWithId(tierName);
        		if (t != null) {
        			selectTier(t);
        		}
        	}
            
            //viewer.setTier(tier);
        }
    }
    
    /**
     * Sets the specified tier selected.
     * 
     * @param tier
     */
    private void selectTier(Tier tier) {
    	if (tier == null) {
    		tierComboBox.setSelectedIndex(0);
    	} else {
			// iterate over tier table and find the key for the tier
			Iterator<String> labelIt = tierTable.keySet().iterator();
			String key;
			Tier tierIt;
			while (labelIt.hasNext()) {
				key = labelIt.next();
				tierIt = tierTable.get(key);
				if (tierIt == tier) {
					tierComboBox.setSelectedItem(key);
					break;
				}
			}
    	}
    }

    /**
     * Returns the name of the selected tier.
     *
     * @return the name of the selected tier
     */
    public String getSelectedTierName() {
        String name = null;

        if (currentTier != null) {
            name = currentTier.getName();
        }

        return name;
    }

    /**
     * Returns whether or not the GridViewer is in multi tier mode, if the
     * viewer  is an instance of GridViewer.
     *
     * @return true when the viewer is a GridViewer and in multitier mode,
     *         false otherwise
     */
    public boolean isMultiTierMode() {
        if (viewer instanceof GridViewer) {
            return gridMode == GridViewer.MULTI_TIER_ASSOCIATION_MODE || gridMode == GridViewer.MULTI_TIER_SUBDIVISION_MODE;        		
        }

        return false;
    }

    /**
     * Changes the multi tier mode for the GridViewer. Ignored by all other
     * SingleTierViewers.
     *
     * @param multiMode when true the gridviewer switches to multi tier mode
     * 
     * @deprecated there are now two modes for multiple tiers. 
     * Use setTierMode(int) instead
     */
    @Deprecated
	public void setMultiTierMode(boolean multiMode) {
        // old options
    	if (!multiMode) {
    		setTierMode(GridViewer.SINGLE_TIER_MODE);
    	} else {
    		setTierMode(GridViewer.MULTI_TIER_ASSOCIATION_MODE);
    	}
    }
    
    /**
     * Sets the display mode for tiers in the GridViewer
     * 
     * @param mode either SINGLE_TIER_MODE, MULTI_TIER_ASSOCIATION_MODE, MULTI_TIER_SUBDIVISION_MODE
     */
    public void setTierMode(int mode) {
    	if (viewer instanceof GridViewer) {
    		gridMode = mode;
    		//((GridViewer) viewer).setMode(mode);
    	}
    }

    /**
     * Update label(s).
     */
    @Override
	public void updateLocale() {
    	String oldItem = EMPTY_ITEM;
    	EMPTY_ITEM = ElanLocale.getString("SingleTierViewerPanel.ComboBoxDefaultString");
    	none_item = ElanLocale.getString("SingleTierViewerPanel.ComboBoxSelectNone");
    	// update the combo box and the label - tier mapping
    	if (!oldItem.equals(EMPTY_ITEM) && tierComboBox.getItemCount() > 0) {
    		tierComboBox.removeItemListener(this);
    		tierComboBox.removeItemAt(0);
    		tierComboBox.insertItemAt(EMPTY_ITEM, 0);
    		tierComboBox.addItemListener(this);
    	}
    	
    	optionIcon.setToolTipText(ElanLocale.getString("SingleTierViewerPanel.Label.ToolTip"));
    	if (popupMenu != null) {
    		normalMI.setText(ElanLocale.getString("SingleTierViewerPanel.Label.SingleTier"));
    		multiAssMI.setText(ElanLocale.getString("SingleTierViewerPanel.Label.MultiTier.Association"));
    		multiSubMI.setText(ElanLocale.getString("SingleTierViewerPanel.Label.MultiTier.Subdivision"));
    	}

    }

	@Override
	public void updateTierOrder(List<String> tierOrder) {
		if(workWithTierSet){
			List<String> tierList = new ArrayList<String>();
			TierSetUtil tierSetUtil = 	TierSetUtil.getTierSetUtilInstance();
			for(String tierSetName : tierSetUtil.getVisibleTierSets()){
				for(String tierName : tierSetUtil.getTierSet(tierSetName).getTierList()){
					if(!tierList.contains(tierName)){
						tierList.add(tierName);
					}
				}
			}
			
//			for(String tierName : tierSetUtil.getTierOrder(viewerManager.getTranscription())){
//				if(!tierList.contains(tierName)){
//					tierList.add(tierName);
//				}
//			}
			tierOrder = tierList;			
		} else {
			if(tierOrder == null){
				return;
			}
		}
		
		tierComboBox.removeItemListener(this);
		this.tierOrder = tierOrder;
		Object selectedObj = tierComboBox.getSelectedItem();	
		
		if (viewer instanceof GridViewer) {
			switch (gridMode) {
			case GridViewer.SINGLE_TIER_MODE:
				fillComboBox();
				break;
			case GridViewer.MULTI_TIER_ASSOCIATION_MODE:
				fillComboBoxMulti(Constraint.SYMBOLIC_ASSOCIATION);
				break;
			case GridViewer.MULTI_TIER_SUBDIVISION_MODE:
				fillComboBoxMulti(Constraint.SYMBOLIC_SUBDIVISION);
				break;
				default:
			}
		} else if (viewer instanceof TextViewer || viewer instanceof SubtitleViewer) {
			fillComboBox();
		} else {
			if(workWithTierSet){
				fillComboBox();
			}
		}

		for(int i=0; i< tierComboBox.getItemCount(); i++){
			if(selectedObj.equals(tierComboBox.getItemAt(i))){
				tierComboBox.setSelectedItem(selectedObj);
				break;
			}
		}		
		tierComboBox.addItemListener(this);
	} 		
	
	@Override
	public void preferencesChanged() {
		Boolean val = Preferences.getBool("WorkwithTierSets", null);
		if(val != null){
			workWithTierSet = val.booleanValue();
		}
		
		processAlphabeticTierOrderPreference();
		if(workWithTierSet){
			updateTierOrder(null);
		} else if(alphabeticTierOrder) {
			updateTierOrder(tierOrder);
		} else {
			updateTierOrder(viewerManager.getMultiTierControlPanel().getTierOrder()); // This works,
			// as in it is tested, but it is perhaps not stable if the MultiTierControlPanel is
			// notified of the "Use tier sets"-preference after this viewer.
			// TODO Micha Hulsbosch
		}
		
		updateComboBox();
	}

	private void processAlphabeticTierOrderPreference() {
		Boolean boolPref = Preferences.getBool("SingleTierViewer.TierOrderInDropdown", null);
	    if (boolPref instanceof Boolean) {
	    	if (boolPref) {
				List<TierImpl> tiers = ((TranscriptionImpl) viewerManager.getTranscription()).getTiers();
				List<String> tierOrder = new ArrayList<String>();
				for (TierImpl tier : tiers) {
					tierOrder.add(tier.getName());
				}
				Collections.sort(tierOrder);
				this.tierOrder = tierOrder;
				alphabeticTierOrder = true;
			} else {
				alphabeticTierOrder = false;
			}
	    }
	}

	@Override
	public void mouseClicked(MouseEvent e) {	
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ( !(viewer instanceof GridViewer)) {
			return;
		}
		if (e.getSource() == optionIcon) {
			if (popupMenu == null) {
				popupMenu = new JPopupMenu();
				normalMI = new JRadioButtonMenuItem(ElanLocale.getString("SingleTierViewerPanel.Label.SingleTier"));
				normalMI.addActionListener(this);
				multiAssMI = new JRadioButtonMenuItem(ElanLocale.getString("SingleTierViewerPanel.Label.MultiTier.Association"));
				multiAssMI.addActionListener(this);
				multiSubMI = new JRadioButtonMenuItem(ElanLocale.getString("SingleTierViewerPanel.Label.MultiTier.Subdivision"));
				multiSubMI.addActionListener(this);
				ButtonGroup bg = new ButtonGroup();
				bg.add(normalMI);
				bg.add(multiAssMI);
				bg.add(multiSubMI);
				
				switch (gridMode) {
				case GridViewer.SINGLE_TIER_MODE:
					normalMI.setSelected(true);
					break;
				case GridViewer.MULTI_TIER_ASSOCIATION_MODE:
					multiAssMI.setSelected(true);
					break;
				case GridViewer.MULTI_TIER_SUBDIVISION_MODE:
					multiSubMI.setSelected(true);
					break;
				default:
					normalMI.setSelected(true);
							
				}
				popupMenu.add(normalMI);
				popupMenu.add(multiAssMI);
				popupMenu.add(multiSubMI);
			}
			
			popupMenu.show(optionIcon, 5, 5);
		}		
	}

	@Override
	public void mouseReleased(MouseEvent e) {	
	}

	@Override
	public void mouseEntered(MouseEvent e) {		
	}

	@Override
	public void mouseExited(MouseEvent e) {	
	}

	/**
	 * A cell renderer that solely changes the label of the first item
	 * to "<select none>" if there is a tier selected instead of "<select a tier>".
	 *
	 */
	private class EmptyRenderer implements ListCellRenderer {
		private ListCellRenderer mainRenderer;

		/**
		 * Constructor with the real renderer as the parameter
		 */
		EmptyRenderer (ListCellRenderer renderer) {
			mainRenderer = renderer;
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (index == 0 && currentTier != null) {
				return mainRenderer.getListCellRendererComponent(
						list, 
						none_item, 
						index, isSelected, cellHasFocus);
			} else {
				return mainRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
			
		}
		
	}
}
