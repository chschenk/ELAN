package mpi.eudico.client.annotator.viewer;
/**
 * An interface for gesture event dispatchers.
 * 
 * @author Han Sloetjes
 */
public interface GestureDispatcher {
	/**
	 * Connects a gesture listener in a platform dependent way.
	 */
	public void connect();
	
	/**
	 * Disconnects a gesture listener.
	 */
	public void disconnect();
}
