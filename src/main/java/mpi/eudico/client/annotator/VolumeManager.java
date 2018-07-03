package mpi.eudico.client.annotator;

import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * Manage the volumes of all media players, given a "Master Volume" and a separate
 * "SubVolume" for each individual media player. The effective volume for each player is the
 * multiplication of the master volume and its subvolume.
 * Players can also be muted. In that case their effective volume is 0.
 * 
 * If the user doesn't want the bother of individual volume settings, the manager should
 * stay out of the way as much as possible. For that case we have the "simple volumes",
 * which simply sets the Master to 100% and all slaves to 0%. The GUI/Preferences logic
 * typically won't store such a setting in Preferences, so that it doesn't overwrite
 * the manually selected subvolumes.
 * 
 * The current version avoids all GUI and preferences related logic.
 * That is instead located in the ElanMediaPlayerController, which already has other 
 * code related to GUI/Preferences.
 * 
 * @author olasei
 *
 */
public class VolumeManager {
	private float masterVolume = 1;
	private ViewerManager2 vm;
	
	public VolumeManager(ViewerManager2 vm) {
		this.vm = vm;
		masterVolume = vm.getMediaPlayerController().getVolume();
		setSimpleVolumes();
	}

	public void setMasterVolume(float masterVolume) {
		this.masterVolume = masterVolume;
		
		setEffectiveVolume(vm.getMasterMediaPlayer());
		for (ElanMediaPlayer player : vm.getSlaveMediaPlayers()) {
			setEffectiveVolume(player);
		}
	}
	
	public float getMasterVolume() {
		return this.masterVolume;
	}
	
	public void setSubVolume(ElanMediaPlayer player, float volume) {
		player.setSubVolume(volume);
		setEffectiveVolume(player);
	}
	
	public float getSubVolume(ElanMediaPlayer player) {
		return player.getSubVolume();
	}
	
	private void setEffectiveVolume(ElanMediaPlayer player) {
		if (player.getMute()) {
			player.setVolume(0);
		} else {
			float sv = player.getSubVolume();
			player.setVolume(sv * masterVolume);
		}
	}
	
	/**
	 * Set all volumes to "simple": the master player at maximum volume
	 * and all slave players at zero.
	 * Unmute everything.
	 */
	public void setSimpleVolumes() {
		for (ElanMediaPlayer player : vm.getSlaveMediaPlayers()) {
			player.setSubVolume(0);
			player.setMute(false);
		}
		ElanMediaPlayer master = vm.getMasterMediaPlayer();
		master.setSubVolume(1);
		master.setMute(false);
		// Update all effective volumes at once.
		setMasterVolume(masterVolume);
	}
	
	/**
	 * Set the Mute property on a single player.
	 */
	public void setMute(ElanMediaPlayer toMute, boolean mute) {
		toMute.setMute(mute);
		setEffectiveVolume(toMute);
	}	
}
