package mpi.eudico.client.annotator.viewer;

import javax.swing.JComponent;

import com.apple.eawt.event.GestureAdapter;
import com.apple.eawt.event.GesturePhaseEvent;
import com.apple.eawt.event.GestureUtilities;
import com.apple.eawt.event.MagnificationEvent;
import com.apple.eawt.event.SwipeEvent;

/**
 * A class that links a (ELAN) gesture listener to a platform specific, in this case
 * Mac OS specific, gesture listener.
 * Scrolling by gestures (two fingers on the trackpad) on the Mac has been implemented
 * in Java as mouse scroll wheel events with the shift key down. Swiping is, by default,
 * performed with three fingers.
 * 
 * @author Han Sloetjes
 *
 */
public class GestureMacDispatcher extends GestureAdapter implements GestureDispatcher {
	private GesturesListener listener;
	private JComponent component;
	//private final int SWIPE_PIXELS = 10;
	private enum SWIPE_DIRECTION {
		LEFT,
		RIGHT,
		UP,
		DOWN
	};
	private Swiper curThread = null;
	// flag for gesture-in-progress
	//private boolean gestureStarted = false;
	
	/**
	 * Adds the component as a gesture listener.
	 */
	public GestureMacDispatcher(JComponent component, GesturesListener listener) {
		this.component = component;
		this.listener = listener;		
	}
	
	/**
	 * Connects the listener. Adds this instance as a listener for the component.
	 */
	@Override
	public void connect() {
		if (component != null) {
			GestureUtilities.addGestureListenerTo(component, this);
		}
	}

	/**
	 * Disconnects the component and the gesture listener. 
	 */
	@Override
	public void disconnect() {
		listener = null;
		if (component != null) {
			GestureUtilities.removeGestureListenerFrom(component, this);
		}
		component = null;
	}

	/**
	 * Magnification event.
	 */
	@Override
	public void magnify(MagnificationEvent event) {
		if (listener != null) {
			//System.out.println("Magnify: " + event.getMagnification());
			listener.magnify(event.getMagnification());
		}
	}

	/**
	 * Swipe down event.
	 */
	@Override
	public void swipedDown(SwipeEvent event) {
		if (listener != null) {
			if (curThread != null && curThread.isAlive()) {
				try {
					curThread.interrupt();
				} catch (SecurityException se) {}
			}
			//System.out.println("Swipe down... ");
			//listener.swipe(0, SWIPE_PIXELS);
			curThread = new Swiper(SWIPE_DIRECTION.DOWN);
			curThread.start();
		}
		
	}

	/**
	 * Swipe left event.
	 */
	@Override
	public void swipedLeft(SwipeEvent event) {
		if (listener != null) {
			if (curThread != null && curThread.isAlive()) {
				if (curThread.getDirection() == SWIPE_DIRECTION.LEFT) {
					curThread.amplifyGesture();
					return;
				} else {
					try {
						curThread.interrupt();
					} catch (SecurityException se) {}
				}
//				try {
//					curThread.interrupt();
//				} catch (SecurityException se) {}
			}
			//System.out.println("Swipe left... ");
//			listener.swipe(-SWIPE_PIXELS, 0);
			curThread = new Swiper(SWIPE_DIRECTION.LEFT);// TODO change implementation, don't make it dependent on gestureStarted flag
			curThread.start();
		}
	}

	/**
	 * Swipe right event.
	 */
	@Override
	public void swipedRight(SwipeEvent event) {
		if (listener != null) {
			if (curThread != null && curThread.isAlive()) {
				if (curThread.getDirection() == SWIPE_DIRECTION.RIGHT) {
					curThread.amplifyGesture();
					return;
				} else {
					try {
						curThread.interrupt();
					} catch (SecurityException se) {}
				}
			}
			//System.out.println("Swipe right... ");
			//listener.swipe(SWIPE_PIXELS, 0);
			curThread = new Swiper(SWIPE_DIRECTION.RIGHT);
			curThread.start();
		}
	}

	/**
	 * Swipe up event.
	 */
	@Override
	public void swipedUp(SwipeEvent event) {
		if (listener != null) {
			//System.out.println("Swipe up... ");
			//listener.swipe(0, -SWIPE_PIXELS);
			if (curThread != null && curThread.isAlive()) {
				try {
					curThread.interrupt();
				} catch (SecurityException se) {}
			}
			curThread = new Swiper(SWIPE_DIRECTION.UP);
			curThread.start();
		}
	}

	/**
	 * The begin of a gesture event.
	 */
	@Override
	public void gestureBegan(GesturePhaseEvent event) {
		//System.out.println("Begin: " + event.toString());
		//gestureStarted = true;
	}

	/**
	 * The end of a gesture event.
	 */
	@Override
	public void gestureEnded(GesturePhaseEvent event) {
		//System.out.println("End: " + event.toString());
		//gestureStarted = false;
	}
	
	private class Swiper extends Thread {
		private SWIPE_DIRECTION direction;
		private final int[] HOR_AMOUNTS = new int[] {200, 170, 140, 110, 90, 75, 55, 38, 28, 19, 10, 8, 7, 6, 5, 4, 3, 2, 1};
		private final int[] VER_AMOUNTS = new int[] {56, 40, 28, 18, 10, 8, 7, 6, 5, 4, 3, 2, 1};
		
		private volatile int numIncreases = 0;
		/**
		 * @param direction
		 */
		public Swiper(SWIPE_DIRECTION direction) {
			super();
			this.direction = direction;
		}	
		
		/**
		 * Amplifies or re-enforces the current ongoing gesture.
		 */
		public void amplifyGesture() {
			numIncreases++;
		}
		
		/**
		 * Returns the direction of the gesture.
		 * 
		 * @return
		 */
		public SWIPE_DIRECTION getDirection() {
			return direction;
		}

		/**
		 * Calls one of the swipe methods repeatedly, until swiping stopped.
		 */
		@Override
		public void run() {
			//System.out.println("Start swipe thread");
			int maxCount = 0;
			if (direction == SWIPE_DIRECTION.LEFT || direction == SWIPE_DIRECTION.RIGHT) {
				maxCount = HOR_AMOUNTS.length;
			} else {
				maxCount = VER_AMOUNTS.length;
			}
			int count = 0;
			while (count < maxCount) {
				if (isInterrupted()) {
					break;
				}
				//System.out.println("Still swiping...");
				if (direction == SWIPE_DIRECTION.LEFT) {
					if (listener != null) {
						listener.swipe(HOR_AMOUNTS[count], 0);
					}
				} else if (direction == SWIPE_DIRECTION.RIGHT) {
					if (listener != null) {
						listener.swipe(-HOR_AMOUNTS[count], 0);
					}
				} else if (direction == SWIPE_DIRECTION.UP) {
					if (listener != null) {
						listener.swipe(0, -VER_AMOUNTS[count]);
					}
				} else if (direction == SWIPE_DIRECTION.DOWN) {
					if (listener != null) {
						listener.swipe(0, VER_AMOUNTS[count]);
					}
				}
				count++;
				try {
					Thread.sleep(15 + 2 * count);
				} catch (InterruptedException ie) {
					break;
				}
				if (numIncreases > 0) {
					//System.out.println("Amplifying swiping... from " + count + " to 0");
					count = 0;
					numIncreases--;
					
				}
			}
		}
		
	}

	
}
