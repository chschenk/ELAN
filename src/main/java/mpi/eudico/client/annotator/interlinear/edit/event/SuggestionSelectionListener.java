package mpi.eudico.client.annotator.interlinear.edit.event;

/**
 * Interface for receiving suggestion selection events when one suggestion has been selected from a
 * set of suggestions.
 *  
 * @author Han Sloetjes
 */
public interface SuggestionSelectionListener {
	
	/**
	 * Called when a suggestion has been selected.
	 * 
	 * @param event the event object, containing the selected suggestion
	 */
	public void suggestionSelected(SuggestionSelectionEvent event);

	/**
	 * Called when a suggestion has been ignored.
	 * The GUI can then present the next one.
	 * 
	 * @param event the event object, containing the selected suggestion
	 */
	public void suggestionIgnored(SuggestionSelectionEvent event);

	/**
	 * Called when a suggestion has been closed.
	 * The GUI should show no further suggestions.
	 * 
	 * @param event the event object, containing the selected suggestion
	 */
	public void suggestionClosed(SuggestionSelectionEvent event);
}
