package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;


/**
 * Action to center the selection in the Timeline viewer. Note: could implement
 * SelectionListener to enable and disable the action when appropriate.
 *
 * @author Han Sloetjes
 */
public class CenterSelectionCA extends CommandAction {
    /**
     * Creates a new CenterSelectionCA instance
     *
     * @param theVM viewer manager
     */
    public CenterSelectionCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.CENTER_SELECTION);
    }

    /**
     * Creates a new CenterSelection command
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.CENTER_SELECTION);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (vm.getSelection().getBeginTime() == vm.getSelection().getEndTime()) {
            return;
        }

        super.actionPerformed(event);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
    protected Object getReceiver() {
        return vm;
    }
}
