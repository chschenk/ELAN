package mpi.eudico.client.annotator.interlinear.edit.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mpi.eudico.client.annotator.interlinear.edit.InterlinearEditor;

/**
 * An action intended to be attached to a keyboard shortcut which triggers
 * the text edit box for the active annotation. If there is no active annotation
 * it is up to the viewer/editor to either ignore the call or activate an 
 * annotation e.g. depending on selected row in the table. 
 * 
 * @author Han Sloetjes
 */
@SuppressWarnings("serial")
public class IGTStartEditAction extends AbstractAction {
	private InterlinearEditor interEditor;
	/**
	 * Constructor.
	 * @param interEditor the interlinear viewer / editor
	 * @param name name of the action
	 */
	public IGTStartEditAction(InterlinearEditor interEditor, String name) {
		super(name);
		this.interEditor = interEditor;
	}

	/**
	 * Just notifies the viewer / editor that this event occurred 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (interEditor != null) {
			interEditor.startEditAnnotation();
		}
	}

}
