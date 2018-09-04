package mpi.eudico.client.annotator.commands.global;

import mpi.eudico.client.annotator.ElanFrame2;
import mpi.eudico.client.annotator.search.viewer.EAFMultipleFileSearchFrame;

import java.awt.event.ActionEvent;


/**
 * A menu action to show the multiple file search frame.
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
@SuppressWarnings("serial")
public class SearchMultipleMA extends FrameMenuAction {
    /**
     * Creates a new SearchMultipleMA instance
     *
     * @param name the name of the command
     * @param frame the parent frame
     */
    public SearchMultipleMA(String name, ElanFrame2 frame) {
        super(name, frame);
    }

    /**
     * @see mpi.eudico.client.annotator.commands.global.MenuAction#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	EAFMultipleFileSearchFrame searchFrame = new EAFMultipleFileSearchFrame(frame);
    	searchFrame.setLocationRelativeTo(frame);
    	searchFrame.setVisible(true);
    }
}
