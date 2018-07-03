package mpi.eudico.client.mediacontrol;



/**
 * Class that generates periodic TimeEvents. This class must implement the
 * Controller interface in order to be able to be coupled to a Player.
 */
public class PeriodicUpdateController extends EventPostingBase
    implements Controller, Runnable {
    /** Holds value of property DOCUMENT ME! */
    private final int STARTED = 0;

    /** Holds value of property DOCUMENT ME! */
    private final int STOPPED = 1;
    private long period;
    private float rate;
    private Thread thread;
    private volatile int state; // Thread docs advice to use volatile
    private TimeEvent timeEvent;
    private StartEvent startEvent;
    private StopEvent stopEvent;

    /*
     * Create a controller that must be connected to an ElanMediaPlayer and that
     * calls controllerUpdate on its connected listeners every period milli seconds.
     */
    public PeriodicUpdateController(long period) {
        // the pulse period in milli seconds
        this.period = period;

        // initialy the controller is not running
        state = STOPPED;

        // create the events
        timeEvent = new TimeEvent(this);
        startEvent = new StartEvent(this);
        stopEvent = new StopEvent(this);
    }

    /**
     * While in the started state send periodic ControlerEvents
     */
    @Override
	public void run() {
        long n = 0;

        // the run Thread started so set the state accordingly
        state = STARTED;

        while (state == STARTED) {
            // send a TimeEvent to the connected ControllerListeners
            postEvent(timeEvent);

            // sleep period milli seconds
            if (!Thread.currentThread().isInterrupted()) {
                // sleep until next event
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * does not need to be implemented, Controller interface asks for it
     */
    @Override
	public void setStopTime(long time) {
    	
    }
    
    /**
     * Notify listeners about a time event
     *
     * @param time DOCUMENT ME!
     */
    @Override
	public void setMediaTime(long time) {
        postEvent(timeEvent);
    }

    /**
     * The rate is ignored at the moment
     *
     * @param rate DOCUMENT ME!
     */
    @Override
	public void setRate(float rate) {
        this.rate = rate;
    }

    /**
     * Stop the periodic controllerUpdate calls.
     */
    @Override
	public void stop() {
        if (state == STOPPED) {
            return;
        }

        state = STOPPED;

        if (thread != null) {
            thread.interrupt();
        }

        postEvent(stopEvent);
    }

    /**
     * Start the periodic controllerUpdate calls
     */
    @Override
	public void start() {
        if (state == STARTED) {
            return;
        }

        // Tell all the listeners that we start
        postEvent(startEvent);

        // start the run method
        thread = new Thread(this, "PeriodicUpdateController");
        thread.start();
    }
}
