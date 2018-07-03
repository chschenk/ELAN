package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ViewerManager2;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A CommandAction to clear the current selection.
 * 
 * Jul 2006: changed the keyboard shortcut from ctrl/com + C to Alt + C to make Ctrl/Com + C available 
 * for the copy annotation command. 
 * @author MPI
 * @version 1.1 
 */
public class ClearSelectionCA extends CommandAction {
    private Icon icon;

    /**
     * Creates a new ClearSelectionCA instance
     *
     * @param theVM DOCUMENT ME!
     */
    public ClearSelectionCA(ViewerManager2 theVM) {
        //super();
        super(theVM, ELANCommandFactory.CLEAR_SELECTION);

        icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/ClearSelectionButton.gif"));
        putValue(SMALL_ICON, icon);
        putValue(Action.NAME, "");
    }

    /**
     * DOCUMENT ME!
     */
    @Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.CLEAR_SELECTION);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object getReceiver() {
        return vm.getSelection();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
	protected Object[] getArguments() {
        Object[] args = new Object[1];
        args[0] = vm.getMediaPlayerController();

        return args;
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
