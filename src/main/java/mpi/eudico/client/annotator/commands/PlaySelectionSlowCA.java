package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Plays the selection with a different play back rate.
 */
@SuppressWarnings("serial")
public class PlaySelectionSlowCA extends CommandAction {
    /**
     * The rate that is used when playing the selection.
     */
    private float playRate;

    /**
     * Creates a new PlayAroundSelectionCA instance
     *
     * @param theVM the viewer manager
     * @param rate the play back rate to use
     */
    public PlaySelectionSlowCA(ViewerManager2 theVM, float rate) {
        super(theVM, ELANCommandFactory.PLAY_SELECTION_SLOW);

        playRate = rate;
    }

    /**
     * Creates a play selection command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAY_SELECTION);
    }

    /**
     * @return the master media player
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * In addition to the existing play selection command arguments (with the offset 
     * set to 0), this method add the play rate value.
     *
     * @return the arguments array
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[4];
        args[0] = vm.getSelection();
        args[1] = vm.getMediaPlayerController();
        args[2] = Integer.valueOf(0);
        args[3] = Float.valueOf(playRate);

        return args;
    }

    /**
     *
     * @param newValue the new play rate
     */
    public void setPlayRate(float newValue) {
        playRate = newValue;
    }

    /**
     *
     * @return the current play rate
     */
    public float getPlayRate() {
        return playRate;
    }
}
