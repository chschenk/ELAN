package mpi.eudico.client.annotator.interlinear.edit.event;
/**
 * An interface for classes that can post SuggestionSet events to registered listeners. 
 * 
 * @author Han Sloetjes
 */
public interface SuggestionSetProvider {
	/**
	 * Adds a listener
	 * 
	 * @param listener the SuggestionSetListener to add
	 */
	public void addSuggestionSetListener(SuggestionSetListener listener);
	
	/**
	 * Removes a listener
	 * 
	 * @param listener the SuggestionSetListener to remove
	 */
	public void removeSuggestionSetListener(SuggestionSetListener listener);
}
