package mpi.eudico.client.annotator.commands.global;

import java.awt.event.ActionEvent;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.gui.ShortcutPanel;


/**
 * A menu action that creates a window for editing the keyboard shortcuts.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class EditShortcutsMA extends FrameMenuAction {
    /**
     * Creates a new EditShortcutsMA instance
     *
     * @param name name of the action
     * @param frame the parent frame
     */
    public EditShortcutsMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * Creates a window for editing the keyboard shortcuts.
     *
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	ShortcutPanel.createAndShowGUI(frame);
    }
}
