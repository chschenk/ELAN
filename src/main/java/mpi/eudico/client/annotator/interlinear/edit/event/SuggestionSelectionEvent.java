package mpi.eudico.client.annotator.interlinear.edit.event;

import java.util.EventObject;

/**
 * An event class generated when a suggestion set has been selected from a list of suggestion sets. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class SuggestionSelectionEvent extends EventObject {
	private int selectedRow;
	
	/**
	 * Constructor
	 * 
	 * @param source the source of the event object, the suggestion set model
	 */
	public SuggestionSelectionEvent(Object source) {
		super(source);
	}

	/**
	 * Constructor with the selected row as argument
	 * 
	 * @param source the source of the event object, the suggestion set model
	 * @param row the selected row (is index of the selected set in the list)
	 */
	public SuggestionSelectionEvent(Object source, int row) {
		super(source);
		selectedRow = row;
	}
	
	/**
	 * Returns the index of the selected row.
	 * 
	 * @return the index of the selected row
	 */
	public int getSelectedRow() {
		return selectedRow;
	}

	/**
     * Returns a String representation of this EventObject.
     *
     * @return  A a String representation of this EventObject.
     */
	@Override
    public String toString() {
        return "SuggestionSelectionEvent[selectedRow=" + String.valueOf(selectedRow) +
        		", source=" + String.valueOf(source) + "]";
    }
}
