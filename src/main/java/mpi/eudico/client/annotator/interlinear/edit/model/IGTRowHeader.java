package mpi.eudico.client.annotator.interlinear.edit.model;

import java.util.ArrayList;
import java.util.List;


/**
 * A class containing the header labels for the rows in a IGT Data Model
 */
public class IGTRowHeader {
	private List<IGTRowHeaderItem> rowData;	
	
	public IGTRowHeader() {
		super();
		rowData = new ArrayList<IGTRowHeaderItem>();
	}
	
	/**
	 * Returns the index of the specified label, or -1 if not there
	 * @param rowLabel the label (tier name)
	 * @return the index of the specified label, or -1 if not there
	 */
	public int getIndexForLabel (String rowLabel) {
		final int size = rowData.size();
		for (int i = 0; i < size; i++) {
			IGTRowHeaderItem item = rowData.get(i);
			if (item.getFullHeaderText().equals(rowLabel)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the full label for the given row.
	 * 
	 * @param row the specified row
	 * @return the full label
	 */
	public String getLabelForIndex(int row) {
		if (row < 0) {
			throw new IndexOutOfBoundsException("The row index is < 0");
		} else if (row > rowData.size() - 1) {
			throw new IndexOutOfBoundsException("The row index is > the size of the list: " + row + " > " + rowData.size());
		}
		return rowData.get(row).getFullHeaderText();
	}

	/**
	 * Returns the short text for a row. 
	 * The index checking is done in the IGTDataModel (or in the end by the List itself)
	 * @param row the row index
	 * @return the short display text
	 */
	public String getHeaderText(int row) {
		return rowData.get(row).getHeaderText();
	}
	
	/**
	 * Returns the full text for a row. 
	 * The index checking is done in the IGTDataModel (or in the end by the List itself)
	 * 
	 * @param row the row index
	 * @return the full text
	 */
	public String getFullHeaderText(int row) {
		return rowData.get(row).getFullHeaderText();
	}

	/**
	 * Sets the short text for a row. 
	 * The index checking is done in the IGTDataModel (or in the end by the List itself)
	 * @param row the row index
	 * @param cutoffText the short display text
	 */
	public void setHeaderText(int row, String cutoffText) {
		rowData.get(row).setHeaderText(cutoffText);
	}
	
	/**
	 * Sets the full text for a row. 
	 * The index checking is done in the IGTDataModel (or in the end by the List itself)
	 * 
	 * @param row the row index
	 * @param fullText the full text
	 */
	public void setFullHeaderText(int row, String fullText) {
		rowData.get(row).setFullHeaderText(fullText);
	}
	
	/**
	 * Adds a header label to the end of the list.
	 * 
	 * @param fullText the full label
	 */
	public void addHeader(String fullText) {
		rowData.add(new IGTRowHeaderItem(fullText));
	}
	
	/**
	 * Removes a header label from the list
	 * 
	 * @param fullText the full label
	 */
	public void removeHeader(String fullText) {
		IGTRowHeaderItem item;
		for (int i = 0; i < rowData.size(); i++) {
			item = rowData.get(i);
			if (item.getFullHeaderText().equals(fullText)) {
				rowData.remove(item);
			}
		}
	}
	
	/**
	 * Inserts a header label in the list.
	 * 
	 * @param fullText the full label
	 */
	public void addHeader(int index, String fullText) throws ArrayIndexOutOfBoundsException {
		rowData.add(index, new IGTRowHeaderItem(fullText));
	}

	@Override
	public String toString() {
		return "IGTRowHeader:[" + String.valueOf(rowData) + "]";
	}
}
