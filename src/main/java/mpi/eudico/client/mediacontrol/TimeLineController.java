package mpi.eudico.client.mediacontrol;

import java.util.List;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * Class that generates TimeEvents for the begin and end times of Tags in a
 * Tier This class must implement the javax.media.Controller interface in
 * order to be able to be coupled to a Player.
 * @version Aug 2005 Identity removed
 */
public class TimeLineController extends EventPostingBase implements Controller,
    Runnable {
    /** Holds value of property DOCUMENT ME! */
    private final int STARTED = 0;

    /** Holds value of property DOCUMENT ME! */
    private final int STOPPED = 1;
    private Tier tier;
    private ElanMediaPlayer controllingPlayer;
    private long mediaDuration;
    private float rate;
    private Thread thread;
    private volatile int state; // Thread docs advice to use volatile
    private TimeEvent timeEvent;
    private StartEvent startEvent;
    private StopEvent stopEvent;
    private long[] timeLine;
    private int nextEventIndex;

    /**
     * Create a controller that must be connected to an ElanMediaPlayer and
     * that calls controllerUpdate on its connected listeners for the begin
     * and end times of all the Tags in a Tier
     *
     * @param tier DOCUMENT ME!
     * @param controllingPlayer DOCUMENT ME!
     */
    public TimeLineController(Tier tier, 
        ElanMediaPlayer controllingPlayer) {
        // the Tier for which the time events must be generated
        this.tier = tier;

        // the ElanMediaPlayer that controls this Controller
        this.controllingPlayer = controllingPlayer;

        // the duration of the media from the controlling ElanMediaPlayer
        mediaDuration = controllingPlayer.getMediaDuration();

        // start with normal playing rate
        rate = 1.0f;

        // initialy the controller is not running
        state = STOPPED;

        // create the events
        timeEvent = new TimeEvent(this);
        startEvent = new StartEvent(this);
        stopEvent = new StopEvent(this);
    }

    /**
     * While in the started state send ControlerEvents, only plays forward;
     */
    @Override
	public void run() {
        // the run Thread started so set the state accordingly
        state = STARTED;

        while ((nextEventIndex < timeLine.length) && (state == STARTED)) {
            // calculate the sleep time until the next time event
            long now = controllingPlayer.getMediaTime();
            long sleepTime = (long) ((timeLine[nextEventIndex] - now) / rate);

            if (sleepTime > 0) {
                if (!Thread.currentThread().isInterrupted()) {
                    // sleep until next event
                    try {
						Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }
            } else {
                // sent an event and update the event index
                postEvent(timeEvent);
                nextEventIndex++;
            }
        }

        // explicitly set state because we might have past timeLineSize()
        state = STOPPED;
    }

    /**
     * Return the Tier used by this controller to generate events
     *
     * @return DOCUMENT ME!
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * does not need to be implemented, Controller interface asks for it
     */
    @Override
	public void setStopTime(long time) {
    	
    }
    
    /**
     * This method may only be called on a stopped controller
     *
     * @param time DOCUMENT ME!
     */
    @Override
	public void setMediaTime(long time) {
        postEvent(timeEvent);
    }

    /**
     * Set the relative rate at which the TimeEvents must happen
     *
     * @param rate DOCUMENT ME!
     */
    @Override
	public void setRate(float rate) {
        this.rate = rate;
    }

    /**
     * Stop the TimeLineController
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
        postEvent(timeEvent);
    }

    /**
     * Start the periodic controllerUpdate calls
     */
    @Override
	public void start() {
        if (state == STARTED) {
            return;
        }

        // calculate a fresh time line for the Tier
        // this is done before every start because
        // the Tier might be edited during the stop
        // aug 2005: replaced call to tier.getTags(id) by tierimpl.getAnnotations(id)
        try {
            List<? extends Annotation> annotations = ((TierImpl)tier).getAnnotations();
            long[] uniqueTimes = new long[2 * annotations.size()];
            int timeIndex = 0;
            long begin;
            long end;
            long previousEnd = -1;

            for (int i = 0; i < annotations.size(); i++) {
                begin = annotations.get(i).getBeginTimeBoundary();
                end = annotations.get(i).getEndTimeBoundary();

                // only add a new time
                if (begin != previousEnd) {
                    uniqueTimes[timeIndex++] = begin;
                }

                uniqueTimes[timeIndex++] = end;
                previousEnd = end;
            }

            // construct the time line
            timeLine = new long[timeIndex];

            for (int i = 0; i < timeLine.length; i++) {
                timeLine[i] = uniqueTimes[i];
            }

            // find the next event index
            long now = controllingPlayer.getMediaTime();

            for (nextEventIndex = 0; nextEventIndex < timeLine.length;
                    nextEventIndex++) {
                if (timeLine[nextEventIndex] > now) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tell all the listeners that we start
        postEvent(startEvent);

        // start the run method
        /* probably not needed
           if (thread != null) {
               while (thread.isAlive()) {
                   try {
                       Thread.currentThread().sleep(10);
                   } catch (InterruptedException e) {
        
                           }
                       }
                   }
         */
        thread = new Thread(this, "TimeLineController");
        thread.start();
    }
}
