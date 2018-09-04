package mpi.eudico.client.annotator.commands;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import mpi.eudico.client.annotator.ViewerManager2;

/**
 * An action to move the media player crosshair to the end of the selection.
 */
@SuppressWarnings("serial")
public class ActiveSelectionEndCA extends CommandAction {

	public ActiveSelectionEndCA(ViewerManager2 theVM) {
		super(theVM, ELANCommandFactory.SELECTION_END);
		
		try {
			Icon icon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/CrosshairInSelectionRight.gif"));
			putValue(SMALL_ICON, icon);
		} catch (Throwable t) {
			//putValue(SMALL_ICON, "Right");
		}

        putValue(Action.NAME, "");
	}

	@Override
	protected void newCommand() {
        command = ELANCommandFactory.createCommand(vm.getTranscription(),
                ELANCommandFactory.SELECTION_END);
	} 

	@Override
	protected Object getReceiver() {
		return vm.getMasterMediaPlayer();
	}
	
	@Override
	protected Object[] getArguments() {
        Object[] args = new Object[2];
        args[0] = vm.getSelection();
        args[1] = ELANCommandFactory.SELECTION_END;
        
        return args;
	}
}
