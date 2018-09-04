package mpi.eudico.client.annotator;

import java.util.EventListener;
import java.util.EventObject;

/**
 * An interface to specify a hook to be called during the shutdown process of Elan.
 * 
 * @author olasei
 */

public interface ShutdownListener extends EventListener {
	/**
	 * Called at various points in the shutdown process.
	 * @param e WindowCloseListener.Event
	 */
	public void somethingIsClosing(ShutdownListener.Event e);
	
	@SuppressWarnings("serial")
	public static class Event extends EventObject {
		public Event(Object source) {
			super(source);
		}

		public Event(Object source, int type) {
			super(source);
			this.type = type;
		}

		/** Identifies that a window (source is ElanFrame2) closes, early in the process */
		public static final int WINDOW_CLOSES_EARLY = 0;
		/** Identifies that a window (source is ElanFrame2) closes, very late in the process */
		public static final int WINDOW_CLOSES_LATE = 2;
		/** Identifies that Elan as a whole is terminating, early in the process */
		public static final int ELAN_EXITS_EARLY = 3;
		/** Identifies that Elan as a whole is terminating, very late in the process */
		public static final int ELAN_EXITS_LATE = 4;

		private int type;
		
		/**
	     * Returns the event type. The possible values are:
	     * <ul>
	     * <li> {@link #WINDOW_CLOSES_EARLY}
	     * <li> {@link #WINDOW_CLOSES_LATE}
	     * <li> {@link #ELAN_EXITS_EARLY}
	     * <li> {@link #ELAN_EXITS_LATE}
	     * </ul>
	     *
	     * @return an int representing the type value
	     */
	    public int getType() { return type; }
	}
}
