package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;

import mpi.eudico.client.annotator.gui.PlayAroundSelectionDialog;

import java.awt.event.ActionEvent;


/**
 * A menu action that creates a window to change the play around
 * selection value. This is now an application wide value.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public class SetPlayAroundSelectionMA extends FrameMenuAction {
    /**
     * Creates a new SetPlayAroundSelectionMA instance
     *
     * @param name the name of the action
     * @param frame the parent frame
     */
    public SetPlayAroundSelectionMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        new PlayAroundSelectionDialog(frame).setVisible(true);
    }
}
