package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that stores and sets the toggle value for the playback
 * volume shortcut.
 * Oct 2006: changed shortcut to make Ctrl+Alt+V available for paste 
 * annotations action.
 * @author Han Sloetjes
 */
public class PlaybackVolumeToggleCA extends CommandAction {
    /** the default rate */
    private final float defaultVolume = 1.0F;

    /** the initial toggle value */
    private float toggleValue = 0.7F;

    //start with the default rate
    private boolean isToggled = false;

    /**
     * Creates a new PlaybackVolumeToggleCA instance
     *
     * @param viewerManager the viewer manager
     */
    public PlaybackVolumeToggleCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);

        //putValue(Action.NAME, "");
    }

    /**
     * Creates a new PlaybackVolumeToggleCommand. Every time newCommand is
     * called the rate value should be toggled.
     */
    @Override
	protected void newCommand() {
        isToggled = !isToggled;

        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAYBACK_VOLUME_TOGGLE);
    }

    /**
     * Returns the reciever, the viewermanager. The command should set the rate
     * of the master media player  and update the rate slider.
     *
     * @return the viewermanager
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * Returns the new volume.
     *
     * @return the new volume
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];

        if (isToggled) {
            args[0] = new Float(toggleValue);
        } else {
            args[0] = new Float(defaultVolume);
        }

        return args;
    }

    /**
     * Sets the toggle value. The value should be between  0 and 100,
     * inclusive.
     *
     * @param value the toggle value
     */
    public void setToggleValue(float value) {
        if (value >= 0.0F && value <= 1.0F) {
            toggleValue = value;
        }
    }

    /**
     * Returns the toggle value.
     *
     * @return the toggle value.
     */
    public float getToggleValue() {
        return toggleValue;
    }
}
