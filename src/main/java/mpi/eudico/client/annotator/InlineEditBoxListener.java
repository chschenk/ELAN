package mpi.eudico.client.annotator;

/**
 * Interface that gives the methods to be implemented by an
 * InlineEditBoxListener. 
 * 
 * @author aarsom
 *
 */
public interface InlineEditBoxListener {	
	
	/**
	 * Called when editing is committed by
	 * the InlineEditbox	 
	 */
	public void editingCommitted();
	
	/**
	 * Called when editing is cancelled by
	 * the InlineEditbox	 
	 */
	public void editingCancelled();
	
//	public void editingInterrupted();

}
