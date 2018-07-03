package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.SelectionListener;
import mpi.eudico.client.annotator.ViewerManager2;


/**
 * An action to shift all annotations on the active tier that are completely
 * within the  selected time interval.
 *
 * @author Han Sloetjes
 * @version 1.0, Nov 2008
 */
public class ShiftAnnotationsInSelectionCA extends CommandAction
    implements SelectionListener {
    /**
     * Creates a new ShiftAnnotationsInSelectionCA instance
     *
     * @param viewerManager the viewer manager
     */
    public ShiftAnnotationsInSelectionCA(ViewerManager2 viewerManager) {
        super(viewerManager, ELANCommandFactory.SHIFT_ANNOS_IN_SELECTION);
        vm.connectListener(this);
    }

    /**
     * Creates a new shift annotations (dialog) command.
     */
    @Override
    protected void newCommand() {
        if (vm.getSelection().getBeginTime() != vm.getSelection().getEndTime()) {
            command = ELANCommandFactory.createCommand(vm.getTranscription(),
                    ELANCommandFactory.SHIFT_ANN_DLG);
        }
    }

    /**
     * Returns the receiver of the command.
     *
     * @return the receiver of the command
     */
    @Override
	protected Object getReceiver() {
        return vm.getTranscription();
    }

    /**
     * The active tier, the selection begin time and the selection end time.
     *
     * @return an Object array size = 3, Tier, Long, Long
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
     * If there is a selection this action will be enabled, otherwise it will
     * be disabled.
     */
    @Override
	public void updateSelection() {
        setEnabled(vm.getSelection().getBeginTime() != vm.getSelection()
                                                         .getEndTime());
    }
}
