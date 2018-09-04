package mpi.eudico.client.annotator.commands;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * Action to start or stop play back in step-and-repeat mode.
 * 
 * @author Han Sloetjes
 */
public class PlayStepAndRepeatCA extends CommandAction {
    private Icon playIcon;
    private Icon pauseIcon;
    
    /**
     * Constructor.
     * @param theVM the viewer manager
     */
	public PlayStepAndRepeatCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.PLAY_STEP_AND_REPEAT);
		
		try {
			playIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/StepAndRepeat_Col16.gif"));
			pauseIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/StepAndRepeatPause_Col16.gif"));
		} catch (Exception ex) {// any
			
		}
		
        putValue(SMALL_ICON, playIcon);
        putValue(Action.NAME, "");
	}

	@Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.PLAY_STEP_AND_REPEAT);
	}

	@Override
	protected Object getReceiver() {
		return vm.getMasterMediaPlayer();
	}

	@Override
	protected Object[] getArguments() {
		return new Object[]{vm.getMediaPlayerController()};
	}

	/**
	 * Changes the icon of the action in a play or pause icon.
	 * @param play if true the play icon is set (indicating the paused state
	 */
	public void setPlayIcon(boolean play) {
		if (play) {
			putValue(SMALL_ICON, playIcon);
		} else {
			putValue(SMALL_ICON, pauseIcon);
		}
	}
}
