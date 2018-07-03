package mpi.eudico.client.annotator.commands;

import java.util.Timer;
import java.util.TimerTask;

import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;


/**
 * A Command to play the selection, possibly in a loop and/or
 * with a modified play back rate
 */
public class PlaySelectionCommand implements Command {
    private String commandName;
    private ElanMediaPlayer player;
    private Selection s;
    private ElanMediaPlayerController mediaPlayerController;
    private long beginTime;
    private long endTime;

    /**
     * Creates a new PlaySelectionCommand instance
     *
     * @param theName 
     */
    public PlaySelectionCommand(String theName) {
        commandName = theName;
    }

    /**
     * 
     *
     * @param receiver the media player
     * @param arguments the Selection, the player controller, the "play around" value
     *  and optionally a play back rate
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        // receiver is master ElanMediaPlayer
        // arguments[0] is Selection
        // arguments[1] is ElanMediaPlayerController
        // arguments[2] is the play around selection value
        player = (ElanMediaPlayer) receiver;
        
        Float selRate = null;
        if(arguments.length == 4) {
        	selRate = (Float) arguments[3]; 
        }
        
        s = (Selection) arguments[0];
        mediaPlayerController = (ElanMediaPlayerController) arguments[1];

        int playAroundSelectionValue = ((Integer) arguments[2]).intValue();

        if (player == null) {
            return;
        }

        //stop if a selection is being played
        if (player.isPlaying()) {
        	//mediaPlayerController.setPlaySelectionMode(false);
            player.stop();
            //mediaPlayerController.setLoopMode(false);
            mediaPlayerController.stopLoop();
            mediaPlayerController.setPlaySelectionMode(false);
            // this might better be implemented per player
            player.setStopTime(player.getMediaDuration());
            
            
            return;
        }
        
        long mediaTime = player.getMediaTime();

        beginTime = s.getBeginTime();
        endTime = s.getEndTime();

        //if there is no selection
        if (beginTime == endTime) {
            return;
        }

        //apply the play around selection value
        if (playAroundSelectionValue > 0) {
            beginTime -= playAroundSelectionValue;

            if (beginTime < 0) {
                beginTime = 0;
            }

            endTime += playAroundSelectionValue;

            if (endTime > player.getMediaDuration()) {
                endTime = player.getMediaDuration();
            }
        }

        //if not playing, start playing
        if ((player.isPlaying() == false) && (mediaTime >= beginTime) &&
                (mediaTime < endTime - 5)) {
        	if(!mediaPlayerController.isBeginBoundaryActive() ){
        		mediaPlayerController.toggleActiveSelectionBoundary();
        	}
        	//mediaPlayerController.toggleActiveSelectionBoundary();
            mediaPlayerController.setPlaySelectionMode(true);
            if (selRate == null) {
            	playInterval(mediaTime, endTime);
            } else {
            	playIntervalWithRate(mediaTime, endTime, selRate);
            }
            if (mediaPlayerController.getLoopMode()) {
	            // start the loop thread, delayed
	            delayedStartLoop();
            }

            return;
        }

        if (mediaPlayerController.getLoopMode() == true) {
            mediaPlayerController.setPlaySelectionMode(true);
            doStartLoop();
        } else {
        	mediaPlayerController.setPlaySelectionMode(true);
        	if (selRate == null) {
        		playInterval(beginTime, endTime);	
        	} else {
        		playIntervalWithRate(beginTime, endTime, selRate);
        	}
        }
    }

    private void playInterval(long begin, long end) {
        player.playInterval(begin, end);
    }
    
    /**
     * Play the selection after changing the rate. After play selection stopped the
     * rate should be set to the old value.
     * 
     * @param begin
     * @param end
     * @param tmpRate the temporary play rate
     */
    private void playIntervalWithRate(long begin, long end, float tmpRate) {
    	final float oldRate = player.getRate();
    	player.setRate(tmpRate);
        
    	player.playInterval(begin, end);
        
        if (tmpRate != oldRate) {
			// Next bit polls the player whether is has stopped
			// or is no longer in PlaySelectionMode to put the
			// old rate back.
			// TODO Find a more elegant way to do this.
			final Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					if (!player.isPlaying() || !mediaPlayerController.isPlaySelectionMode()) {
						player.setRate(oldRate);
						timer.cancel();
					}
				}

			}, 0, 100);
		}
    }

    /**
     *
     * @return the name
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * Starts the loop
     */
    public void doStartLoop() {
        mediaPlayerController.startLoop(beginTime, endTime);
    }
    
    /**
     * Starts the loop after a first partial play selection has finished
     */
    private void delayedStartLoop() {
        LoopThread loopthread = new LoopThread();
        loopthread.start();
    }

    /**
     * Calls the media player controllers startloop method after a first, partial selection playback has finished.
     */
    private class LoopThread extends Thread {
        /**
         * Starts the play back again with some delay (500 ms) 
         */
        @Override
		public void run() {
            if (mediaPlayerController.isPlaySelectionMode() && mediaPlayerController.getLoopMode() == true) {
	            try {// give player time to start
	            	Thread.sleep(200);
	            } catch (InterruptedException ie) {
	            	
	            }
	            while (player.isPlaying()) {// wait until stopped
	            	try {
	            		Thread.sleep(50);
	            	} catch (InterruptedException ie) {
	            		
	            	}
	            }
	            // then start the loop, if player not yet stopped
	            if (mediaPlayerController.isPlaySelectionMode()) {	 
	            	try {
	            		Thread.sleep(500);
	            	} catch (InterruptedException ie) {
	            		
	            	}
	            	mediaPlayerController.startLoop(beginTime, endTime);
	            }
	            
            	/*
                if (!player.isPlaying()) {
                    playInterval(beginTime, endTime);    
                }

                while (player.isPlaying() == true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                }

                try {
                    Thread.sleep(mediaPlayerController.getUserTimeBetweenLoops());
                } catch (Exception ex) {
                }
                */
            }
        }
    }
     //end of LoopThread
}
