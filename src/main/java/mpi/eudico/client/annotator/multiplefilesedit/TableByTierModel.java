package mpi.eudico.client.annotator.multiplefilesedit;

import javax.swing.table.AbstractTableModel;

import mpi.eudico.client.annotator.ElanLocale;

public class TableByTierModel extends AbstractTableModel {
	private static final long serialVersionUID = 4075133967965660649L;
	private MFEModel model;
	int new_row_counter = 0;

	public TableByTierModel(MFEModel model) {
		super();
		this.model = model;
	}
	
	public void removeRows(int[] selectedRows) {
		if(getColumnCount()>0) {
			for(int i=selectedRows.length-1;i>=0;i--) {
				System.err.println(this.getValueAt(selectedRows[i], 0));
				model.flagTier(selectedRows[i]);
//				model.removeTierRow(selectedRows[i]);
			}
			model.removeFlaggedTiers();
			fireTableRowsDeleted(0, model.getTierRowCount());
		}
	}
	
	public void clear() {
	}
	
	@Override
	public int getColumnCount() {
		return model.getTierColumnCount();
	}

	@Override
	public int getRowCount() {
		return model.getTierRowCount();
	}

	@Override
	public String getColumnName(int col) {
		return model.getTierColumnName(col);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		return model.getTierValueAt(row, col);
	}
	
	@Override
	public Class<? extends Object> getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if(col==MFEModel.TIER_NAMECOLUMN ||
		   (col==MFEModel.TIER_TYPECOLUMN && model.isTypeConsistentTier(row)) ||
		   col==MFEModel.TIER_PARTICIPANTCOLUMN ||
	       col==MFEModel.TIER_LANGUAGECOLUMN ||
		   col==MFEModel.TIER_ANNOTATORCOLUMN)
			return true;
		return false;
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		model.changeTierValueAt(value, row, col);
		fireTableCellUpdated(row, col);
	}

	public int newRow() {
		int row = model.addTier(ElanLocale.getString("MFE.Tables.NewLine")+(new_row_counter++),
				model.getConsistentLinguisticTypeNames()[0], "", "", null);
		fireTableRowsInserted(row, row);
		
		return row;
	}
	
	public int newRowForChildTier(String parentName, int stereotype ) {
		int row = model.addChildTier(ElanLocale.getString("MFE.Tables.NewLine")+(new_row_counter++),
				model.getConsistentLinguisticTypeNames(stereotype)[0], "", "", parentName, null);
		fireTableRowsInserted(row, row);
		
		return row;
	}
}
