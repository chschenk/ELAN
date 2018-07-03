package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.ArrayList;
import java.util.List;

import mpi.eudico.client.annotator.interlinear.edit.render.IGTSuggestionViewerRenderInfo;

/**
 * A data model for a list of SuggestionSets.
 * 
 * @author Han Sloetjes
 */
public class IGTSuggestionViewerModel {
	private List<IGTSuggestionModel> rowData;
//	private int selectedRow;
	public IGTSuggestionViewerRenderInfo renderInfo;
	private int recursionLevel;
	
	/**
	 * Constructor, initializes lists etc.
	 */
	public IGTSuggestionViewerModel(int recursionLevel) {
		super();
		rowData = new ArrayList<IGTSuggestionModel>();
		renderInfo = new IGTSuggestionViewerRenderInfo();
		this.recursionLevel = recursionLevel;
//		selectedRow = -1;
	}

	/**
	 * Returns the number of SuggestionSets in the list.
	 * 
	 * @return the number of SuggestionSets in the list
	 */
	public int getRowCount() {
		return rowData.size();
	}
	
	/**
	 * Returns the selected row, i.e. the index of the selected SuggestionSet 
	 * in the list of sets.
	 * 
	 * @return the selected row or -1 if no set is selected
	 */
//	public int getSelectedRow() {
//		return selectedRow;
//	}
	
	/**
	 * Sets the selected row (one SuggestionSet).
	 * 
	 * @param row the newly selected row
	 */
//	public void setSelectedRow(int row) {
//		if (row > -1 && row < rowData.size()) {
//			selectedRow = row;
//		}
//	}
	
	/**
	 * Adds a row of data to the model (one SuggestionSet).
	 * 
	 * @param sugModel the Suggestion Model
	 */
	public void addRow(IGTSuggestionModel sugModel) {
		if (sugModel != null) {
			rowData.add(sugModel);
		}
	}
	
	/**
	 * Returns the data at the given row (one SuggestionSet,
	 * i.e. one group of related sequential suggestions).
	 * 
	 * @param row the row index to retrieve the data from
	 * @return the data at row
	 * @throws ArrayIndexOutOfBoundsException if row < 0 or >= the size of the row
	 */
	public IGTSuggestionModel getRowData(int row) {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException("Get row, index is less than zero: " +  row);
		} else if (row > rowData.size() - 1){
			throw new ArrayIndexOutOfBoundsException("Get row, index is greater than model size: " +  row);
		}
		
		return rowData.get(row);
	}
	
	/**
	 * Returns the render information object of this model.
	 * 
	 * @return the render information object of this model
	 */
	public IGTSuggestionViewerRenderInfo getRenderInfo() {
		return renderInfo;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("IGTSuggestionViewerModel:[");
		buf.append(rowData.toString());
//		buf.append(" selectedRow=");
//		buf.append(String.valueOf(selectedRow));
		buf.append(" ");
		buf.append(String.valueOf(renderInfo));
		buf.append("]");
		
		return buf.toString();
	}

	public int getRecursionLevel() {
		return recursionLevel;
	}
}
