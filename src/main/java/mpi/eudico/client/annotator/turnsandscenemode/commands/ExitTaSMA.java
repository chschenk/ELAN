package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.commands.global.FrameMenuAction;

@SuppressWarnings("serial")
public class ExitTaSMA extends FrameMenuAction {

	/**
	 * @param name action name
	 * @param frame the frame this action is connected to
	 */
	public ExitTaSMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

	/**
	 * Checks if there is anything to save and then exits the application.
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (frame.getViewerManager() != null && frame.getViewerManager().getTranscription() != null) {
			frame.checkSaveAndClose();
		} 
		//else {
		//
		//}
		/*
		 * If checkSaveAndClose() popped up a save? dialog and the user clicked Cancel the viewer manager is not null
		 * and we shouldn't exit.
		 */
		if (frame.getViewerManager() == null) {
			System.exit(0);
		}
	}

}
