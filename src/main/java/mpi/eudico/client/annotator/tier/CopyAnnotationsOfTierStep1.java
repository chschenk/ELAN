package mpi.eudico.client.annotator.tier;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.EmptyStringComparator;

/**
 * The first step in this process is the selection of one source tier (and
 * possibly selection of multiple files).
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyAnnotationsOfTierStep1 extends AbstractFileAndTierSelectionStepPane implements 
		ListSelectionListener {
	
	/**
	 * Constructor
	 * @param multiPane 
	 * @param transcription the transcription or null in case of multiple files
	 */
	public CopyAnnotationsOfTierStep1(MultiStepPane multiPane, 
			TranscriptionImpl transcription) {
		super(multiPane, transcription);
		//initComponents();
	}
	
	/**
	 * Calls the super implementation and then adds a row sorter and a 
	 * selection listener to the table.
	 */
	@Override
	protected void initComponents() {
        super.initComponents();
        tierTable.getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CopyAnnotationsDialog.SelectSource");
	}

	@Override
	public void enterStepForward() {
		 // the next button is already disabled
	}
	
	/**
	 * Checks if there is a tier selected.
	 */
	@Override
	public boolean leaveStepForward() {
		if (tierTable.getSelectedRow() < 0) {
			return false;
		}
		// the super implementation stores several step properties
		// one of them a list of selected tiers
		return super.leaveStepForward();
	}

	/**
	 * Update button states.
	 */
	@Override
	public void enterStepBackward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, tierTable.getSelectedRow() > -1);
		multiPane.setButtonEnabled(MultiStepPane.PREVIOUS_BUTTON, Boolean.FALSE);
	}

	@Override
	public boolean leaveStepBackward() {
		// never called
		return true;
	}

	/**
	 * Updates the buttons depending on selected tiers.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int row = tierTable.getSelectedRow();
			if (row > -1) {
				// set the check box in column 0 selected, deselect all other check boxes
				int col = tierTable.convertColumnIndexToModel(0);// this can be less hardcoded
				for (int i = 0; i < tierTable.getRowCount(); i++) {
					if (i != row) {
						tierTable.setValueAt(Boolean.FALSE, i, col);
					} else {
						tierTable.setValueAt(Boolean.TRUE, i, col);
					}
				}
			}
		}
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, tierTable.getSelectedRow() > -1);		
	}
	
	/**
	 * This is a modified version of the implementation of the abstract super class.
	 * The table in this version only allows a single tier to be selected
	 */
	@Override
	protected void initTierSelectionPanel() {
		tierSelectionPanel = new JPanel(new GridBagLayout());
		globalInset = new Insets(5, 10, 5, 10);
		tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("CopyAnnotationsDialog.SelectSource")));
		
		//create table to show tiers in
		tierTable = new JTable();
		tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierTable.setShowVerticalLines(true);
		
		//first menu depends on multiple files or single files
		if ( transcription != null ) {
			String[] tierArray = tierSet.toArray(new String[0]);
			String[] linguisticArray = new String[tierArray.length];
			
			for (int i=0; i < tierArray.length; i++ ) {
				TierImpl tier = transcription.getTierWithId( tierArray[i] );
				linguisticArray[i] = tier.getLinguisticType().getLinguisticTypeName();
			}
			
			SelectableContentTableModel model = new SelectableContentTableModel( 
					new Object[][] { tierArray, linguisticArray},
					new String[]{ElanLocale.getString("FileAndTierSelectionStepPane.Column.TierName"), 
							ElanLocale.getString("FileAndTierSelectionStepPane.Column.LinguisticType")});
			model.addTableModelListener(new ModelChangedHandler());
			
			tierTable.setModel(model);
			tierTable.getColumnModel().getColumn(0).setMaxWidth(30);
			//Sorting
			TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(model);
	        EmptyStringComparator emptyComp = new EmptyStringComparator();
	        for (int i = 1; i < tierTable.getColumnCount(); i++) {
	    		rowSorter.setComparator(i, emptyComp);
	        }
	        tierTable.setRowSorter(rowSorter);
		} else {
			DisplayableContentTableModel model = new DisplayableContentTableModel(ElanLocale.getString("FileAndTierSelectionStepPane.Message1"));
			tierTable.setModel(model);
			tierTable.getColumnModel().getColumn(0).setHeaderValue(ElanLocale.getString("FileAndTierSelectionStepPane.Column.Header.Message"));			
		}
		
		//--- common layout code ----
		//add table to scroll pane
		tierTableScrollPane = new JScrollPane(tierTable);
		tierTableScrollPane.setColumnHeaderView(null);
		
		//add table
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = globalInset;
		tierSelectionPanel.add(tierTableScrollPane, gbc);
	}
	
	/**
	 * @see #valueChanged(ListSelectionEvent)
	 */
	@Override
	public void updateButtonStates() {
//		super.updateButtonStates();
		// enabling buttons is performed in the list selection listener
	}

	
}
