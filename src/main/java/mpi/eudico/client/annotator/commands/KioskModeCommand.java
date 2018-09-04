package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.util.FrameConstants;


/**
 * A Command to switch to and from Kisok Mode.
 *
 * @author Han Sloetjes, MPI
 * @version 1.0
 */
public class KioskModeCommand implements Command {
    private String commandName;
    private ViewerManager2 vm;

    /**
     * Creates a new KioskModeCommand instance
     *
     * @param name the name of the command
     */
    public KioskModeCommand(String name) {
        commandName = name;
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the ViewerManager
     * @param arguments the arguments:  <ul><li>arg[0] = on or off flag
     *        (Boolean)</li> </ul>
     */
    @Override
	public void execute(Object receiver, Object[] arguments) {
        vm = (ViewerManager2) receiver;

        boolean onOff = ((Boolean) arguments[0]).booleanValue();

        if (onOff) {
            // start looping in Kiosk Mode, disable menu's and commands
            // re-use (abuse) the playingSelection flag of the controller
            if (vm.getMasterMediaPlayer().isPlaying()) {
                vm.getMasterMediaPlayer().stop();
            }

            ElanFrame2 ef2 = (ElanFrame2) ELANCommandFactory.getRootFrame(vm.getTranscription());
            ef2.enableCommands(false);
            enableMenus(ef2, false);

            vm.getMediaPlayerController().setPlaySelectionMode(true);

            vm.getMasterMediaPlayer().start();

            new LoopThread().start();
        } else {
            // stop Kiosk Mode, enable menu's and commands
            vm.getMediaPlayerController().setPlaySelectionMode(false);

            if (vm.getMasterMediaPlayer().isPlaying()) {
                vm.getMasterMediaPlayer().stop();
            }
            
            ElanFrame2 ef2 = (ElanFrame2) ELANCommandFactory.getRootFrame(vm.getTranscription());
            ef2.enableCommands(true);
            enableMenus(ef2, true);
        }
    }

    /**
     * Enables/disables the relevant menu's and menu items.
     * 
     * @param ef2 the frame 
     * @param enable the enable flag; if true all relevant menu's will be enabled
     */
    private void enableMenus(ElanFrame2 ef2, boolean enable) {
    	ef2.setMenuEnabled(FrameConstants.FILE, enable);
    	ef2.setMenuEnabled(FrameConstants.EDIT, enable);
    	ef2.setMenuEnabled(FrameConstants.ANNOTATION, enable);
    	ef2.setMenuEnabled(FrameConstants.TIER, enable);
    	ef2.setMenuEnabled(FrameConstants.TYPE, enable);
    	ef2.setMenuEnabled(FrameConstants.SEARCH, enable);
    	ef2.setMenuEnabled(FrameConstants.VIEW, enable);
    	ef2.setMenuEnabled(FrameConstants.WINDOW, enable);
    	ef2.setMenuEnabled(FrameConstants.HELP, enable);
    	// now the options menu
    	ef2.setMenuEnabled(FrameConstants.PROPAGATION, enable);
    	ef2.setMenuEnabled(FrameConstants.ANNOTATION_MODE, enable);
    	ef2.setMenuEnabled(FrameConstants.SYNC_MODE, enable);
    	ef2.setMenuEnabled(FrameConstants.PLAY_AROUND_SEL, enable);
    	ef2.setMenuEnabled(FrameConstants.RATE_VOL_TOGGLE, enable);
    	ef2.setMenuEnabled(FrameConstants.LANG, enable);
    	if (enable) {
    		if (vm.getMasterMediaPlayer().isFrameRateAutoDetected()) {
    			ef2.setMenuEnabled(FrameConstants.FRAME_LENGTH, enable);
    		}
    	} else {
    		ef2.setMenuEnabled(FrameConstants.FRAME_LENGTH, enable);
    	}
    }
    
    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    @Override
	public String getName() {
        return commandName;
    }

    /**
     * A thread that regularly checks if the player is still playing;  when the
     * player has stopped (= has reached the end) it starts the  player again
     * from the beginning.
     */
    private class LoopThread extends Thread {
        /**
         * The actual work
         */
        @Override
		public void run() {
            while (vm.getMediaPlayerController().isPlaySelectionMode()) {
            	if (!vm.getMasterMediaPlayer().isPlaying()) {
                    vm.getMasterMediaPlayer().setMediaTime(0);
                    vm.getMasterMediaPlayer().start();
            	}

                while (vm.getMasterMediaPlayer().isPlaying()) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                }

                try {
                    Thread.sleep(vm.getMediaPlayerController()
                                   .getUserTimeBetweenLoops());
                } catch (Exception ex) {
                }
            }
        }
    }
}
