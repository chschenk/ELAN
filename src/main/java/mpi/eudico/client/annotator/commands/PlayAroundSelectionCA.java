package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;


/**
 * Plays the selection with a certain offset.
 *
 * @author Han Sloetjes
 */
public class PlayAroundSelectionCA extends CommandAction {
    /**
     * An offset that is used when playing the selection. This amount of
     * milliseconds is prepended and appended to the selection's begin and end
     * time.
     */
    private int playAroundSelectionValue;

    /**
     * Creates a new PlayAroundSelectionCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public PlayAroundSelectionCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.PLAY_AROUND_SELECTION);

        // start with a default value of 500 ms
        playAroundSelectionValue = 500;
    }

    /**
     * Play around selection and play selection use the same command; play
     * selection passes 0 as offset.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAY_SELECTION);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getMasterMediaPlayer();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[3];
        args[0] = vm.getSelection();
        args[1] = vm.getMediaPlayerController();
        args[2] = Integer.valueOf(playAroundSelectionValue);

        return args;
    }

    /**
     * DOCUMENT ME!
     *
     * @param newValue DOCUMENT ME!
     */
    public void setPlayAroundSelectionValue(int newValue) {
        playAroundSelectionValue = newValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getPlayAroundSelectionValue() {
        return playAroundSelectionValue;
    }
}
