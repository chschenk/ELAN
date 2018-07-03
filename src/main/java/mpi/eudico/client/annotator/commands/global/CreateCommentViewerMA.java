package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;

@SuppressWarnings("serial")
public class CreateCommentViewerMA extends FrameMenuAction {	
	/**
     * Creates a new CreateCommentViewerMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
	public CreateCommentViewerMA(String name, ElanFrame2 frame) {
		super(name, frame);
	}

    /**
     * Sets the preference setting when changed 
     *
     * @param e the action event
     */
	@Override
	public void actionPerformed(ActionEvent e) {
		boolean checked;

		if (e.getSource() instanceof JCheckBoxMenuItem) {
			checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			Preferences.set(commandId, checked, null, false);
			if (frame.isIntialized()) {
				frame.getLayoutManager().updateViewer(
						ELANCommandFactory.COMMENT_VIEWER, checked);
				if (!checked) {
					frame.getViewerManager().destroyCommentViewer();
				}
			}
		}
	}
}
