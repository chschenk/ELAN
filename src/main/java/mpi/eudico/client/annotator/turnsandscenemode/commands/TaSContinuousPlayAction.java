package mpi.eudico.client.annotator.turnsandscenemode.commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import mpi.eudico.client.annotator.turnsandscenemode.TurnsAndSceneViewer;

/**
 * An action to set the flag whether or not the media player should 
 * stop when a new segment has been created. 
 * The default behavior is to stop when a new segment is created; the new segment 
 * is always activated (which sets the selection and sets the media time to the
 * begin of the selection which stops the player).
 * This Action could be turned into a more general CommandAction and Command pair
 * created via the ELANCommandFactory?
 */
@SuppressWarnings("serial")
public class TaSContinuousPlayAction extends AbstractAction {
	private TurnsAndSceneViewer viewer;
	
	/**
	 * Creates the action and sets the SELECTED_KEY property to FALSE.
	 * By setting this property a check box will update itself after a
	 * change in this property e.g. triggered by a keyboard shortcut. 
	 */
	public TaSContinuousPlayAction() {
		super();
		putValue(Action.SELECTED_KEY, Boolean.FALSE);
	}

	/**
	 * Creates an action that changes the the continuous play back
	 * flag in the specified viewer.
	 * 
	 * @param viewer the viewer, can initially be null
	 */
	public TaSContinuousPlayAction(TurnsAndSceneViewer viewer) {
		this();
		this.viewer = viewer;
	}

	/**
	 * Changes the flag for continuous play back.
	 * 
	 * @param e the event itself is ignored
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		changeContinuousPlaybackFlag();
	}

	/**
	 * Flips the setting for continuous play back in the viewer.
	 * Continuous here means that the player does not stop when a new 
	 * annotation is created.
	 * The new value of the flag is stored in the Action.SELECTED_KEY property as a
	 * result of which a check box will update its state. 
	 */
	private void changeContinuousPlaybackFlag() {
		if (viewer != null) {
			viewer.setContinuousPlayMode(!viewer.isContinuousPlayMode());
			putValue(Action.SELECTED_KEY, Boolean.valueOf(viewer.isContinuousPlayMode()));
		} else {
			// take care of changing the state if there is no viewer?
		}
	}

	/**
	 * @return the viewer
	 */
	public TurnsAndSceneViewer getViewer() {
		return viewer;
	}

	/**
	 * @param viewer the viewer to set
	 */
	public void setViewer(TurnsAndSceneViewer viewer) {
		this.viewer = viewer;
	}
}
