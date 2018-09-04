/**
 * 
 */
package mpi.eudico.client.annotator.interlinear.edit.event;

/**
 * An interface for listeners to SuggestionSet events. 
 * A SuggestionSet typically has to be presented to the user as a list of choices.
 *  
 * @author Han Sloetjes
 */
public interface SuggestionSetListener {

	public void suggestionSetDelivered(SuggestionSetEvent event);

	public void cancelSuggestionSet();
}
