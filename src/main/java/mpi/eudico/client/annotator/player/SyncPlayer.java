package mpi.eudico.client.annotator.player;

/**
 * Identifies a media player as a synchronisation-only player, 
 * i.e. the layout manager should only add this player to the layout 
 * and to the viewer manager in synchronisation mode.
 * Used for non-audio, non-video media/files that need to be synchronized.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public interface SyncPlayer {
	
	public boolean isSyncConnected();

	public void setSyncConnected(boolean syncConnected);
}
