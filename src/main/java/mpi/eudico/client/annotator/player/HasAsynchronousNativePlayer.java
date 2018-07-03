package mpi.eudico.client.annotator.player;

/**
 * An interface for Java Media players that encapsulate a native media player 
 * that works asynchronously or at least supports asynchronous an 
 * asynchronous interaction mode.
 *  
 * @author Han Sloetjes
 */
public interface HasAsynchronousNativePlayer {
	
    /**
     * Returns whether the player is (still) playing an interval (a selection).
     * This can be used to test if playing a selection has completely finished. 
     *
     * @return true if the player is currently playing an interval
     */
    public boolean isPlayingInterval();
    
}
