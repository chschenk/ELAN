package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

import javax.swing.Action;


/**
 * Clears the selection and switches off the selection mode.
 * 
 * @author HS
 * @version 1.0
  */
public class ClearSelectionAndModeCA extends CommandAction {
    /**
     * Creates a new ClearSelectionAndModeCA instance
     *
     * @param theVM viewer manager
     */
    public ClearSelectionAndModeCA(ViewerManager2 theVM) {
        super(theVM, ELANCommandFactory.CLEAR_SELECTION_AND_MODE);

        putValue(Action.NAME, "");
    }

    /**
     * Creates a new command.
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.CLEAR_SELECTION_AND_MODE);
    }

    /**
     * Returns the receiver, the selection.
     *
     * @return the Selection object
     */
    @Override
	protected Object getReceiver() {
        return vm.getSelection();
    }

    /**
     * Returns the arguments.
     *
     * @return the arguments
     */
    @Override
	protected Object[] getArguments() {
        return new Object[] { vm.getMediaPlayerController() };
    }

    /**
     * Overrides CommandAction's actionPerformed by doing nothing when there is
     * no  selection. The ClearSelectionCommand is undoable and we don't want
     * meaningless commands in the unod/redo list.
     *
     * @param event the action event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (vm.getSelection().getBeginTime() == vm.getSelection().getEndTime()) {
            return;
        }

        super.actionPerformed(event);
    }
}
