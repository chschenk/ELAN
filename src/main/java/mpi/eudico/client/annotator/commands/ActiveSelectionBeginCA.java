package mpi.eudico.client.annotator.commands;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to move the media player crosshair to the begin of the selection.
 */
@SuppressWarnings("serial")
public class ActiveSelectionBeginCA extends CommandAction {

	public ActiveSelectionBeginCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.SELECTION_BEGIN);
		
		try {
			Icon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/CrosshairInSelectionLeft.gif"));
			putValue(SMALL_ICON, icon);
		} catch (Throwable t) {
			//putValue(SMALL_ICON, "Left");
		}

        putValue(Action.NAME, "");
	}

	@Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SELECTION_BEGIN);
	}

	@Override
	protected Object getReceiver() {
		return vm.getMasterMediaPlayer();
	}

	@Override
	protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getSelection();
        args[1] = ELANCommandFactory.SELECTION_BEGIN;
        
        return args;
	}
	
}
