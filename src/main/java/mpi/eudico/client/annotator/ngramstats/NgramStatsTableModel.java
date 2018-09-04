package mpi.eudico.client.annotator.ngramstats;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;

/**
 * Relays the result from the ngram collection to the JTable
 * @author Larwan Berke, DePaul University
 * @version 1.0
 * @since August 2013
 */
public class NgramStatsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -3664457083389501427L;

	private NgramStatsResult result;
		
	// stores the column headers we export to the JTable
	private final String[] tableColumns = new String[]{
		ElanLocale.getString("Statistics.NGram"),
		ElanLocale.getString("Statistics.Occurrences"),
		ElanLocale.getString("Statistics.AverageDuration"),
		ElanLocale.getString("Statistics.MinimalDuration"),
		ElanLocale.getString("Statistics.MaximalDuration"),
		ElanLocale.getString("Statistics.AverageAnnotationTime"),
		ElanLocale.getString("Statistics.AverageIntervalTime")
	};
		
	public NgramStatsTableModel(NgramStatsResult r) {
		result = r;
	}

	/**
	 * Retrieves the result object backing this model
	 * @return NgramStatsResult the result
	 */
	public NgramStatsResult getResult() {
		return result;
	}

	@Override
	public String getColumnName(int col) {
		return tableColumns[col];
	}

	@Override
	public int getColumnCount() {
		return tableColumns.length;
	}

	@Override
	public int getRowCount() {
		if (result != null) {
			return result.getNumCollectedNgrams();
		} else {
			// used to display blank table
			return 0;
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (result != null) {
			NgramCollection nc = result.getCollectedNgramAt(row);
			switch (col) {
				case 0:
					return nc.getName();
				case 1:
					return nc.getOccurrences();
				case 2:
					return nc.getAvgDuration();
				case 3:
					return nc.getMinDuration();
				case 4:
					return nc.getMaxDuration();
				case 5:
					return nc.getAvgAnnotationTime();
				case 6:
					return nc.getAvgIntervalTime();
				default :
					// huh?
					return null;
			}
		} else {
			// used to display blank table
			return null;
		}
	}
}