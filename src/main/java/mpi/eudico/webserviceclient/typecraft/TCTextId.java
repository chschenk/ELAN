package mpi.eudico.webserviceclient.typecraft;
/**
 * A class that holds some properties of text items in the database.
 * 
 * @author Han Sloetjes
 */
public class TCTextId {
	public String id = null;
	public String title = "";
	public String titleTranslation = "";
	
	public TCTextId() {
		super();
	}
	/**
	 * Returns the "title".
	 */
	@Override
	public String toString() {
		return title;
	}

}
