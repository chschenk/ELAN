package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

/**
 * A class to switch to and from Kisok Mode. In this mode the player will play
 * the complete media file in a loop, all interaction is disabled.
 *
 * @author Han Sloetjes, MPI
 */
public class KioskModeCA extends CommandAction {
    private boolean selected = false;

    /**
     * Creates a new KioskMode command action.
     *
     * @param viewerManager the viewer manager
     */
    public KioskModeCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.KIOSK_MODE);
    }

    /**
     * Creates a new KioskMode command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.KIOSK_MODE);
    }

    /**
     * Object array of size 1: Boolean switching kiosk mode on or off.
     *
     * @return the arguments
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { Boolean.valueOf(selected) };
    }

    /**
     * Returns the viewermanager as the central object giving access to all 
     * necessary components.
     *
     * @return the viewermanager
     */
    @Override
	protected Object getReceiver() {
        return vm;
    }

    /**
     * Inverses the selected value and calls the super implementation.
     *
     * @param event the action event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        selected = !selected;

        super.actionPerformed(event);
    }
}
