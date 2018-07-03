package mpi.eudico.client.annotator.tier;

import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

public class MergeTierGroupStep1 extends CalcOverlapsStep1 {

	/**
	 * @param multiPane
	 * @param transcription
	 */
	public MergeTierGroupStep1(MultiStepPane multiPane,
			TranscriptionImpl transcription) {
		super(multiPane, transcription);
	}

	/**
	 * Removes all non toplevel tiers from the tables.
	 * 
	 * @see mpi.eudico.client.annotator.tier.CalcOverlapsStep1#initComponents()
	 */
	@Override
	public void initComponents() {
		super.initComponents();
		String name;
		TierImpl t1;
		for (int i = model1.getRowCount() - 1; i >= 0; i--) {
			name = (String) model1.getValueAt(i, model1.findColumn(TierTableModel.NAME));
			t1 = (TierImpl) transcription.getTierWithId(name);
			if (t1 != null && t1.getParentTier() != null) {
				model1.removeRow(i);
			}
		}
		for (int i = model2.getRowCount() - 1; i >= 0; i--) {
			name = (String) model2.getValueAt(i, model2.findColumn(TierTableModel.NAME));
			t1 = (TierImpl) transcription.getTierWithId(name);
			if (t1 != null && t1.getParentTier() != null) {
				model2.removeRow(i);
			}
		}
	}

	
}
