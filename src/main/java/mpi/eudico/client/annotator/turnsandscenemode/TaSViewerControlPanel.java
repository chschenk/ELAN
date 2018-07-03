package mpi.eudico.client.annotator.turnsandscenemode;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;

/**
 * Control panel for the Turns and Scene viewer.
 * Could extend AbstractViewer and/or implement TierOrderListener
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class TaSViewerControlPanel extends JPanel implements /*TierOrderListener*/ ItemListener, ACMEditListener {
	private TurnsAndSceneViewer viewer;
	private JComboBox tierComboBox;
	// create font size panel here?
	
	public TaSViewerControlPanel(TurnsAndSceneViewer viewer) {
		this.viewer = viewer;
		initComponents();
	}
	
	private void initComponents() {
		setLayout(new GridBagLayout());
		tierComboBox = new JComboBox();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		this.add(tierComboBox, gbc);
		
		initTierBox();
		
		tierComboBox.addItemListener(this);
	}

	private void initTierBox() {
		List<TierImpl> tierList = ((TranscriptionImpl)viewer.getViewerManager().getTranscription()).getTiers();
		List<String> rootTiers = new ArrayList<String>();
		for (Tier t : tierList) {
			if (t.getParentTier() == null) {
				rootTiers.add(t.getName());
			}
		}
		setTierNames(rootTiers);
	}
	
	/**
	 * To add other panels, like the font size button panel.
	 * 
	 * @param otherPanel
	 */
	/*
	protected void addOtherPanel(JPanel otherPanel) {
		int numComp = this.getComponentCount();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = numComp;
		this.add(tierComboBox, otherPanel);
	}
	*/
	/**
	 * Initializes the list of available tiers.
	 * 
	 * @param tierList
	 */
	protected void setTierNames(List<String> tierList) {
		if (tierList != null) {
			Object selectedItem = tierComboBox.getSelectedItem();
			
			tierComboBox.removeItemListener(this);
			tierComboBox.removeAllItems();
			
			for (String s : tierList) {
				tierComboBox.addItem(s);
			}
			
			tierComboBox.addItemListener(this);
			
			if (tierList.contains(selectedItem)) {
				tierComboBox.setSelectedItem(selectedItem);
			} else if (tierComboBox.getItemCount() > 0){
				tierComboBox.setSelectedIndex(0);
			}
		}
	}
	
	/**
	 * Request to select the tier with the specified tier name.
	 * 
	 * @param tierName
	 */
	public void selectTierByName(String tierName) {
		if (tierName != null) {
			tierComboBox.setSelectedItem(tierName);
		}
	}
	
	/**
	 * Adds a tier name to the list
	 * 
	 * @param tierName
	 */
	private void addTierName(String tierName) {
		if (tierName != null) {
			int itemCount = tierComboBox.getItemCount();
			for( int i = 0; i < itemCount; i++) {
				if (tierComboBox.getItemAt(i).equals(tierName)) {
					return;
				}
			}
			
			tierComboBox.addItem(tierName);
		}
	}

	/**
	 * Removes a tier name from the list
	 * @param tierName 
	 */
	private void removeTierName(String tierName) {
		if (tierName != null) {
			int itemCount = tierComboBox.getItemCount();
			Object selItem = tierComboBox.getSelectedItem();
			
			for( int i = 0; i < itemCount; i++) {
				if (tierComboBox.getItemAt(i).equals(tierName)) {
					tierComboBox.removeItemAt(i);
					if (tierName.equals(selItem)) {
						tierComboBox.setSelectedIndex(Math.max(0, i - 1));
					} else {
						// otherwise select the old item again (if needed)
						tierComboBox.setSelectedItem(selItem);
					}
					
					return;
				}
			}		
		}
	}
	
	/*
	@Override
	public void updateTierOrder(List<String> tierOrder) {
		// TODO Auto-generated method stub
		
	}
	 */

	/**
	 * On selection of a tier in the combo box.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == tierComboBox) {
			String selTierName = (String) e.getItem();
			viewer.setTierByName(selTierName);
		}
	}

	//##### following methods to be implemented if this mode is integrated in ELAN 
	//##### (where tiers can be created, deleted, changed)
	private void tierAdded(TierImpl t) {
		
	}
	
	private void tierRemoved(TierImpl t) {
		
	}
	
	private void tierChanged(TierImpl t) {
		
	}
	
	/**
	 * Listen to tier related events.
	 */
	@Override
	public void ACMEdited(ACMEditEvent e) {
        //System.out.println("ACMEdited:: operation: " + e.getOperation() + ", invalidated: " + e.getInvalidatedObject());
        //System.out.println("\tmodification: " + e.getModification() + ", source: " + e.getSource());
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierAdded((TierImpl) e.getModification());

            }
            break;

        case ACMEditEvent.REMOVE_TIER:

            if (e.getModification() instanceof TierImpl) {
                tierRemoved((TierImpl) e.getModification());
            }

            break;

        case ACMEditEvent.CHANGE_TIER:

            if (e.getInvalidatedObject() instanceof TierImpl) {
                tierChanged((TierImpl) e.getInvalidatedObject());
            }

            break;
            default:
            	
        }
		
	}

}
