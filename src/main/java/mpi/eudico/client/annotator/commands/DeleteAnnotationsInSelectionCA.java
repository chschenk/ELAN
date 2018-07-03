package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;


/**
 * Deletes annotations on the active tier within the selected time interval.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class DeleteAnnotationsInSelectionCA extends CommandAction
    implements SelectionListener {
    /**
     * Creates the action.
     *
     * @param viewerManager
     */
    public DeleteAnnotationsInSelectionCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.DELETE_ANNOS_IN_SELECTION);
        viewerManager.connectListener(this);
        setEnabled(false);
    }

    /**
     * Creates a new command.
     */
    @Override
    protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.DELETE_ANNOS_IN_SELECTION);
    }

    /**
     * Returns the arguments for the command.
     *
     * @return array containing the active tier, selection begin time and end
     *         time, or null
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getArguments()
     */
    @Override
    protected Object[] getArguments() {
        if (vm.getMultiTierControlPanel().getActiveTier() != null) {
            Long bt = new Long(vm.getSelection().getBeginTime());
            Long et = new Long(vm.getSelection().getEndTime());

            return new Object[] {
                vm.getMultiTierControlPanel().getActiveTier(), bt, et
            };
        }

        return null;
    }

    /**
     * Returns the receiver, the transcription.
     *
     * @return the transcription
     *
     * @see mpi.eudico.client.annotator.commands.CommandAction#getReceiver()
     */
    @Override
    protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * Checks whether there is a selection.
     *
     * @param event the event
     */
    @Override
	public void actionPerformed(ActionEvent event) {
        if (vm.getSelection().getBeginTime() == vm.getSelection().getEndTime()) {
            return;
        }

        super.actionPerformed(event);
    }

    /**
     * If there is a selection this action will be enabled, otherwise it will
     * be disabled.
     */
    @Override
	public void updateSelection() {
        setEnabled(vm.getSelection().getBeginTime() != vm.getSelection()
                                                         .getEndTime());
    }
}
