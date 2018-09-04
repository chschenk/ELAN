package mpi.eudico.client.annotator.tier;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.gui.multistep.StepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

/**
 * The second step in this process consists of selecting the 
 * target or destination tier.
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class CopyAnnotationsOfTierStep2 extends StepPane implements
		ListSelectionListener {
	private TranscriptionImpl transcription;
	private TierTableModel tierTableModel;
	private JTable tierTable;
	protected JScrollPane tierTableScrollPane;
	protected JPanel tierSelectionPanel;
	private Insets globalInset = new Insets(5, 10, 5, 10);
	protected String curSourceTierName;
	
	/**
	 * Constructor.
	 * 
	 * @param multiPane the parent pane
	 * @param transcription the transcription or null in case of multiple files
	 */
	public CopyAnnotationsOfTierStep2(MultiStepPane multiPane,
			TranscriptionImpl transcription) {
		super(multiPane);
		this.transcription = transcription;
		initComponents();
	}

	/**
	 * Creates a table and a model to for displaying possible target tiers.
	 * Target tiers can be direct depending tiers of the source or top-level 
	 * independent tiers.
	 */
	@Override
	protected void initComponents() {
        setBorder(new EmptyBorder(5, 10, 5, 10));
        setLayout(new BorderLayout());
        tierTableScrollPane = new JScrollPane();
		tierSelectionPanel = new JPanel(new GridBagLayout());
		globalInset = new Insets(5, 10, 5, 10);
		tierSelectionPanel.setBorder(new TitledBorder(ElanLocale.getString("CopyAnnotationsDialog.SelectTarget")));
		
		//create table to show tiers in
		tierTable = new JTable();
		tierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierTable.setShowVerticalLines(true);
        if (transcription != null) {
	        tierTableModel = new TierTableModel(new ArrayList<TierImpl>(), new String[]{TierTableModel.NAME, TierTableModel.PARENT});
	        tierTable = new JTable(tierTableModel);
	        tierTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        tierTable.getSelectionModel().addListSelectionListener(this);
	        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(tierTableModel);
	        tierTable.setRowSorter(rowSorter);
	        tierTableScrollPane.setViewportView(tierTable);
        }
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = globalInset;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;
         tierSelectionPanel.add(tierTableScrollPane, gbc);
         add(tierSelectionPanel, BorderLayout.CENTER);
	}
	
	
	@Override
	public String getStepTitle() {
		return ElanLocale.getString("CopyAnnotationsDialog.SelectTarget");
	}

	/**
	 * Fills the table depending on the selected source tier.
	 * Candidates are direct child tiers and top level, independent tiers.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void enterStepForward() {
		// fill or update the table
		String sourceTierName = null;
		List<Object> selectedTiers = (List<Object>) multiPane.getStepProperty("SelectedTiers");
		if (selectedTiers != null && !selectedTiers.isEmpty()) {
			sourceTierName = String.valueOf(selectedTiers.get(0));
		}
//		String sourceTierName = (String) multiPane.getStepProperty("SourceTier");
		if (sourceTierName != null) {
			if (curSourceTierName != null) {
				if (curSourceTierName.equals(sourceTierName)) {
					multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, tierTable.getSelectedRow() > -1);
					return;// table is already there
				} else {
					if (tierTableModel.getRowCount() > 0) {
						tierTable.setRowSorter(null);
						tierTableModel.removeAllRows();
					}
					curSourceTierName = sourceTierName;
				}
			} else {
				curSourceTierName = sourceTierName;
			}
			
			if (transcription != null) {
				TierImpl sourceTier = transcription.getTierWithId(sourceTierName);
				if (sourceTier != null) {
					// add children
					List<TierImpl> childTiers = sourceTier.getChildTiers();
					for (TierImpl tier : childTiers) {
						tierTableModel.addRow(tier);
					}
					// add root tiers
					for (TierImpl tier : transcription.getTiers()) {
						if (tier.getParentTier() == null && tier != sourceTier) {
							tierTableModel.addRow(tier);
						}
					}
				}
			} else {
				// multiple files to be implemented
			}
		} else {
			if (tierTableModel.getRowCount() > 0) {
				tierTable.setRowSorter(null);
				tierTableModel.removeAllRows();
			}
		}
	}

	@Override
	public void enterStepBackward() {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, true);
	}

	/**
	 * Stores the selected target tier name in the properties map
	 */
	@Override
	public boolean leaveStepForward() {
		if (tierTable.getSelectedRow() > -1) {
			// store selected tier
			int row = tierTable.getSelectedRow();
			int column = tierTableModel.findColumn(TierTableModel.NAME);
			String tierName = (String) tierTableModel.getValueAt(tierTable.convertRowIndexToModel(row), column);
			multiPane.putStepProperty("TargetTier", tierName);
			return true;
		}
		return false;
	}

	@Override
	public boolean leaveStepBackward() {
		return super.leaveStepBackward();
	}

	/**
	 * Checks if there is a tier selected and enables or disables the Next button.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		multiPane.setButtonEnabled(MultiStepPane.NEXT_BUTTON, tierTable.getSelectedRow() > -1);	
	}

}
