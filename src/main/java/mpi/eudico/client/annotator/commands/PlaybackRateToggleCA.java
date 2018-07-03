package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * A CommandAction that stores and sets the toggle value for  the playback rate
 * shortcut.
 *
 * @author Han Sloetjes
 */
public class PlaybackRateToggleCA extends CommandAction {
    /** the default rate */
    private final float defaultRate = 1.0F;
    /** the initial toggle value */
    private float toggleValue = 0.5F;

    //start with the default rate
    private boolean isToggled = false;

    /**
     * Creates a new PlaybackRateToggleCA instance
     *
     * @param viewerManager the viewer manager
     */
    public PlaybackRateToggleCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.PLAYBACK_RATE_TOGGLE);

		//putValue(Action.NAME, "");
    }

    /**
     * Creates a new PlaybackRateToggleCommand. Every time newCommand is
     * called the rate value should be toggled.
     */
    @Override
	protected void newCommand() {
        isToggled = !isToggled;

        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAYBACK_RATE_TOGGLE);
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
     * Returns the new rate.
     *
     * @return the new rate
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];

        if (isToggled) {
            args[0] = new Float(toggleValue);
        } else {
            args[0] = new Float(defaultRate);
        }

        return args;
    }

    /**
     * Sets the toggle value. The value should be between 
     * 0 and 200, inclusive.
     *
     * @param value the toggle value
     */
    public void setToggleValue(float value) {
    	if (value >= 0F && value <= 2.0F) {
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
