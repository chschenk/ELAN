package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Selection;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;

/**
 * Command to move the crosshair explicitly to either begin or end of the selection.
 * This is independent of the concept of "active boundary" (as used by the 
 * ActiveSelectionBoundaryCommand).
 * 
 * @see ActiveSelectionBoundaryCommand
 */
public class ActiveSelectionBeginOrEndCommand implements Command {
	private String name;
	
	public ActiveSelectionBeginOrEndCommand(String name) {
		this.name = name;
	}

	/**
	 * @param receiver the (master) media player
	 * @param arguments 
	 * <ul><li>arguments[0] = the Selection object (Selection)</li>
	 * <li>arguments[1] = begin or end (String constant from ELANCommandFactory)</li></ul>
	 */
	@Override
	public void execute(Object receiver, Object[] arguments) {
		ElanMediaPlayer player = (ElanMediaPlayer) receiver;
		Selection selection = (Selection) arguments[0];
		String extreme = (String) arguments[1];
		
		if (selection.getBeginTime() != selection.getEndTime()) {			
			if (ELANCommandFactory.SELECTION_BEGIN.equals(extreme)) {
				player.setMediaTime(selection.getBeginTime());
			} else if (ELANCommandFactory.SELECTION_END.equals(extreme)) {
				player.setMediaTime(selection.getEndTime());
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

}
