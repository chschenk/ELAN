package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;

import mpi.eudico.client.annotator.gui.PlaybackToggleDialog;

import java.awt.event.ActionEvent;


/**
 * A menu action that creates a ui to set toggle values for playback rate and volume.
 * 
 * @author Han Sloetjes	
 * @version 1.0
  */
public class SetPlaybackToggleMA extends FrameMenuAction {
    /**
     * Creates a new SetPlaybackToggleMA instance
     *
     * @param name the name of the command
     * @param frame the parent frame
     */
    public SetPlaybackToggleMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        new PlaybackToggleDialog(frame).setVisible(true);
    }
}
